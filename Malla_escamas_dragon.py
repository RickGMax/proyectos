"""
dragon_scales_springs.py

Demo interactiva de una malla de escamas de dragón con:
- superficie base curva y animada;
- una escama rígida instanciada muchas veces;
- un grado de libertad angular por escama;
- resorte angular hacia el ángulo de reposo;
- resortes de acoplamiento entre escamas vecinas;
- amortiguamiento, viento y ondas de activación;
- renderizado instanciado con ModernGL.

Dependencias:
    pip install numpy pyglet moderngl

Ejecución:
    python dragon_scales_springs.py

Controles:
    Arrastrar botón izquierdo : orbitar cámara
    Rueda del mouse           : acercar/alejar
    Flecha arriba/abajo       : aumentar/disminuir viento
    Espacio                   : generar una onda en las escamas
    P                         : pausar/reanudar
    R                         : reiniciar escamas
    Escape                    : salir
"""

from __future__ import annotations

import math
import sys
from dataclasses import dataclass

import numpy as np

try:
    import pyglet
    import moderngl
except ImportError as exc:
    missing = getattr(exc, "name", "una dependencia")
    raise SystemExit(
        f"Falta instalar {missing!r}.\n"
        "Ejecuta: pip install numpy pyglet moderngl"
    ) from exc


pyglet.options["shadow_window"] = False

from pyglet.window import key, mouse  # noqa: E402


# ---------------------------------------------------------------------------
# Utilidades matemáticas
# ---------------------------------------------------------------------------

def normalize(v: np.ndarray, axis: int = -1, eps: float = 1.0e-8) -> np.ndarray:
    lengths = np.linalg.norm(v, axis=axis, keepdims=True)
    return v / np.maximum(lengths, eps)


def perspective(
    fov_y_radians: float,
    aspect: float,
    near: float,
    far: float,
) -> np.ndarray:
    f = 1.0 / math.tan(fov_y_radians * 0.5)

    matrix = np.zeros((4, 4), dtype=np.float32)
    matrix[0, 0] = f / max(aspect, 1.0e-6)
    matrix[1, 1] = f
    matrix[2, 2] = (far + near) / (near - far)
    matrix[2, 3] = (2.0 * far * near) / (near - far)
    matrix[3, 2] = -1.0
    return matrix


def look_at(
    eye: np.ndarray,
    target: np.ndarray,
    up: np.ndarray,
) -> np.ndarray:
    forward = normalize(target - eye)
    side = normalize(np.cross(forward, up))
    corrected_up = np.cross(side, forward)

    matrix = np.eye(4, dtype=np.float32)
    matrix[0, :3] = side
    matrix[1, :3] = corrected_up
    matrix[2, :3] = -forward

    matrix[0, 3] = -np.dot(side, eye)
    matrix[1, 3] = -np.dot(corrected_up, eye)
    matrix[2, 3] = np.dot(forward, eye)
    return matrix


def matrix_bytes(matrix: np.ndarray) -> bytes:
    """Convierte una matriz NumPy row-major al orden column-major de OpenGL."""
    return np.asarray(matrix.T, dtype=np.float32).tobytes()


# ---------------------------------------------------------------------------
# Modelo geométrico de la piel
# ---------------------------------------------------------------------------

@dataclass
class SurfaceParameters:
    width: float = 8.0
    length: float = 7.0
    transverse_curvature: float = 0.55
    transverse_frequency: float = 0.43
    wave_amplitude: float = 0.08
    wave_frequency: float = 0.95
    wave_speed: float = 1.40


class DragonSurface:
    def __init__(
        self,
        params: SurfaceParameters,
        nx: int = 52,
        ny: int = 46,
    ) -> None:
        self.params = params
        self.nx = nx
        self.ny = ny

        x = np.linspace(-params.width * 0.5, params.width * 0.5, nx)
        y = np.linspace(-params.length * 0.5, params.length * 0.5, ny)
        grid_x, grid_y = np.meshgrid(x, y)

        self.grid_x = grid_x.astype(np.float32).ravel()
        self.grid_y = grid_y.astype(np.float32).ravel()
        self.indices = self._build_indices()

    def _build_indices(self) -> np.ndarray:
        indices: list[int] = []

        for row in range(self.ny - 1):
            for col in range(self.nx - 1):
                i0 = row * self.nx + col
                i1 = i0 + 1
                i2 = i0 + self.nx
                i3 = i2 + 1

                indices.extend((i0, i1, i3))
                indices.extend((i0, i3, i2))

        return np.asarray(indices, dtype=np.uint32)

    def evaluate(
        self,
        x: np.ndarray,
        y: np.ndarray,
        time_seconds: float,
    ) -> tuple[np.ndarray, np.ndarray, np.ndarray, np.ndarray]:
        """
        Retorna:
            posiciones, tangente_x, tangente_y, normales.
        """
        p = self.params

        phase = p.wave_frequency * y + p.wave_speed * time_seconds

        z = (
            p.transverse_curvature * np.cos(p.transverse_frequency * x)
            + p.wave_amplitude * np.sin(phase)
        )

        dz_dx = (
            -p.transverse_curvature
            * p.transverse_frequency
            * np.sin(p.transverse_frequency * x)
        )
        dz_dy = p.wave_amplitude * p.wave_frequency * np.cos(phase)

        positions = np.column_stack((x, y, z)).astype(np.float32)

        tangent_x = np.column_stack(
            (
                np.ones_like(x),
                np.zeros_like(x),
                dz_dx,
            )
        )
        tangent_x = normalize(tangent_x).astype(np.float32)

        raw_normal = np.column_stack(
            (
                -dz_dx,
                -dz_dy,
                np.ones_like(x),
            )
        )
        normal = normalize(raw_normal).astype(np.float32)

        tangent_y = normalize(np.cross(normal, tangent_x)).astype(np.float32)

        return positions, tangent_x, tangent_y, normal

    def vertex_data(self, time_seconds: float) -> np.ndarray:
        positions, _, _, normals = self.evaluate(
            self.grid_x,
            self.grid_y,
            time_seconds,
        )
        return np.concatenate((positions, normals), axis=1).astype(np.float32)


# ---------------------------------------------------------------------------
# Sistema de resortes angulares
# ---------------------------------------------------------------------------

class AngularScaleSystem:
    def __init__(
        self,
        rest_angles: np.ndarray,
        edges: np.ndarray,
        inertia: float = 0.006,
        root_stiffness: float = 0.90,
        neighbor_stiffness: float = 0.12,
        damping: float = 0.12,
        min_angle: float = math.radians(-4.0),
        max_angle: float = math.radians(68.0),
    ) -> None:
        rest_angles = np.asarray(rest_angles, dtype=np.float32)

        self.rest_angles = rest_angles.copy()
        self.angles = rest_angles.copy()
        self.angular_velocities = np.zeros_like(rest_angles)

        self.edges = np.asarray(edges, dtype=np.int32)
        self.inertia = np.full_like(rest_angles, inertia)
        self.root_stiffness = np.full_like(rest_angles, root_stiffness)
        self.damping = np.full_like(rest_angles, damping)

        self.neighbor_stiffness = float(neighbor_stiffness)
        self.min_angle = float(min_angle)
        self.max_angle = float(max_angle)

    def reset(self) -> None:
        self.angles[:] = self.rest_angles
        self.angular_velocities.fill(0.0)

    def torque(self, external_torque: np.ndarray) -> np.ndarray:
        result = (
            np.asarray(external_torque, dtype=np.float32)
            - self.root_stiffness * (self.angles - self.rest_angles)
            - self.damping * self.angular_velocities
        )

        if self.edges.size:
            first = self.edges[:, 0]
            second = self.edges[:, 1]

            difference = self.angles[first] - self.angles[second]
            edge_torque = -self.neighbor_stiffness * difference

            np.add.at(result, first, edge_torque)
            np.add.at(result, second, -edge_torque)

        return result

    def step(
        self,
        external_torque: np.ndarray,
        dt: float,
        substeps: int = 6,
    ) -> None:
        if dt <= 0.0:
            return

        h = min(dt, 1.0 / 30.0) / max(substeps, 1)

        for _ in range(substeps):
            acceleration = self.torque(external_torque) / self.inertia

            # Euler semimplícito: más estable que Euler explícito.
            self.angular_velocities += h * acceleration
            self.angles += h * self.angular_velocities

            below = self.angles < self.min_angle
            above = self.angles > self.max_angle

            np.clip(
                self.angles,
                self.min_angle,
                self.max_angle,
                out=self.angles,
            )

            self.angular_velocities[
                below & (self.angular_velocities < 0.0)
            ] = 0.0
            self.angular_velocities[
                above & (self.angular_velocities > 0.0)
            ] = 0.0


# ---------------------------------------------------------------------------
# Distribución de escamas y geometría de una escama
# ---------------------------------------------------------------------------

def build_scale_layout(
    rows: int,
    columns: int,
    spacing_x: float,
    spacing_y: float,
) -> tuple[np.ndarray, np.ndarray]:
    anchors: list[tuple[float, float]] = []
    edges: list[tuple[int, int]] = []

    def index(row: int, column: int) -> int:
        return row * columns + column

    for row in range(rows):
        offset = 0.5 * spacing_x if row % 2 else 0.0
        y = (row - (rows - 1) * 0.5) * spacing_y

        for column in range(columns):
            x = (column - (columns - 1) * 0.5) * spacing_x + offset
            anchors.append((x, y))

            current = index(row, column)

            if column + 1 < columns:
                edges.append((current, index(row, column + 1)))

            if row + 1 < rows:
                edges.append((current, index(row + 1, column)))

                diagonal = column - 1 if row % 2 == 0 else column + 1
                if 0 <= diagonal < columns:
                    edges.append((current, index(row + 1, diagonal)))

    return (
        np.asarray(anchors, dtype=np.float32),
        np.asarray(edges, dtype=np.int32),
    )


def build_scale_mesh() -> np.ndarray:
    """
    Malla local de una escama.

    La raíz está cerca de y=0 y la punta cerca de y=1.
    Cada triángulo duplica sus vértices para conservar un aspecto facetado.
    """
    points = np.asarray(
        [
            (-0.30, 0.00, 0.00),  # 0 raíz izquierda
            ( 0.30, 0.00, 0.00),  # 1 raíz derecha
            (-0.50, 0.44, 0.10),  # 2 lado izquierdo
            ( 0.50, 0.44, 0.10),  # 3 lado derecho
            (-0.20, 0.82, 0.05),  # 4 cerca de punta izquierda
            ( 0.20, 0.82, 0.05),  # 5 cerca de punta derecha
            ( 0.00, 1.00, 0.00),  # 6 punta
        ],
        dtype=np.float32,
    )

    triangles = [
        (0, 1, 3),
        (0, 3, 2),
        (2, 3, 5),
        (2, 5, 4),
        (4, 5, 6),
    ]

    vertices: list[np.ndarray] = []

    for a, b, c in triangles:
        pa, pb, pc = points[a], points[b], points[c]
        normal = normalize(np.cross(pb - pa, pc - pa)).astype(np.float32)

        for point in (pa, pb, pc):
            vertices.append(
                np.asarray(
                    (
                        point[0],
                        point[1],
                        point[2],
                        normal[0],
                        normal[1],
                        normal[2],
                    ),
                    dtype=np.float32,
                )
            )

    return np.vstack(vertices).astype(np.float32)


# ---------------------------------------------------------------------------
# Cámara orbital
# ---------------------------------------------------------------------------

class OrbitCamera:
    def __init__(self) -> None:
        self.yaw = math.radians(-52.0)
        self.pitch = math.radians(31.0)
        self.distance = 11.5
        self.target = np.asarray((0.0, 0.0, 0.25), dtype=np.float32)

    def position(self) -> np.ndarray:
        horizontal = self.distance * math.cos(self.pitch)

        return self.target + np.asarray(
            (
                horizontal * math.cos(self.yaw),
                horizontal * math.sin(self.yaw),
                self.distance * math.sin(self.pitch),
            ),
            dtype=np.float32,
        )

    def view_matrix(self) -> np.ndarray:
        return look_at(
            self.position(),
            self.target,
            np.asarray((0.0, 0.0, 1.0), dtype=np.float32),
        )

    def orbit(self, dx: float, dy: float) -> None:
        self.yaw -= dx * 0.008
        self.pitch += dy * 0.008
        self.pitch = float(
            np.clip(
                self.pitch,
                math.radians(-5.0),
                math.radians(82.0),
            )
        )

    def zoom(self, scroll_y: float) -> None:
        self.distance *= math.exp(-0.10 * scroll_y)
        self.distance = float(np.clip(self.distance, 5.0, 24.0))


# ---------------------------------------------------------------------------
# Aplicación ModernGL
# ---------------------------------------------------------------------------

SURFACE_VERTEX_SHADER = """
#version 330

in vec3 in_position;
in vec3 in_normal;

uniform mat4 u_mvp;

out vec3 v_world_position;
out vec3 v_normal;

void main() {
    v_world_position = in_position;
    v_normal = in_normal;
    gl_Position = u_mvp * vec4(in_position, 1.0);
}
"""


SURFACE_FRAGMENT_SHADER = """
#version 330

in vec3 v_world_position;
in vec3 v_normal;

uniform vec3 u_camera_position;
uniform vec3 u_light_direction;

out vec4 fragment_color;

void main() {
    vec3 normal = normalize(v_normal);
    vec3 light = normalize(-u_light_direction);
    vec3 view_direction = normalize(u_camera_position - v_world_position);
    vec3 half_direction = normalize(light + view_direction);

    float diffuse = max(dot(normal, light), 0.0);
    float specular = pow(max(dot(normal, half_direction), 0.0), 24.0);

    vec3 base_color = mix(
        vec3(0.025, 0.070, 0.045),
        vec3(0.070, 0.180, 0.085),
        0.5 + 0.5 * normal.z
    );

    vec3 color =
        base_color * (0.23 + 0.77 * diffuse)
        + vec3(0.13, 0.22, 0.14) * specular;

    fragment_color = vec4(color, 1.0);
}
"""


SCALE_VERTEX_SHADER = """
#version 330

in vec3 in_position;
in vec3 in_normal;

in vec4 i_model_0;
in vec4 i_model_1;
in vec4 i_model_2;
in vec4 i_model_3;
in float i_variant;

uniform mat4 u_view_projection;

out vec3 v_world_position;
out vec3 v_normal;
out float v_variant;

void main() {
    mat4 model = mat4(
        i_model_0,
        i_model_1,
        i_model_2,
        i_model_3
    );

    vec4 world_position = model * vec4(in_position, 1.0);

    v_world_position = world_position.xyz;
    v_normal = normalize(mat3(model) * in_normal);
    v_variant = i_variant;

    gl_Position = u_view_projection * world_position;
}
"""


SCALE_FRAGMENT_SHADER = """
#version 330

in vec3 v_world_position;
in vec3 v_normal;
in float v_variant;

uniform vec3 u_camera_position;
uniform vec3 u_light_direction;

out vec4 fragment_color;

void main() {
    vec3 normal = normalize(v_normal);

    if (!gl_FrontFacing) {
        normal = -normal;
    }

    vec3 light = normalize(-u_light_direction);
    vec3 view_direction = normalize(u_camera_position - v_world_position);
    vec3 half_direction = normalize(light + view_direction);

    float diffuse = max(dot(normal, light), 0.0);
    float rim = pow(
        1.0 - max(dot(normal, view_direction), 0.0),
        2.4
    );
    float specular = pow(max(dot(normal, half_direction), 0.0), 38.0);

    vec3 dark_scale = vec3(0.055, 0.155, 0.075);
    vec3 bright_scale = vec3(0.28, 0.62, 0.20);
    vec3 base_color = mix(dark_scale, bright_scale, v_variant);

    vec3 color =
        base_color * (0.22 + 0.78 * diffuse)
        + vec3(0.13, 0.30, 0.09) * rim
        + vec3(0.72, 0.82, 0.48) * specular * 0.45;

    fragment_color = vec4(color, 1.0);
}
"""


class DragonScaleDemo:
    def __init__(self) -> None:
        self.window = self._create_window()
        self.window.switch_to()

        try:
            self.ctx = moderngl.create_context(require=330)
        except Exception as exc:
            self.window.close()
            raise SystemExit(
                "No fue posible crear un contexto OpenGL 3.3.\n"
                "Actualiza el controlador de video o prueba una versión "
                "más reciente de ModernGL."
            ) from exc

        self.ctx.enable(moderngl.DEPTH_TEST)
        self.ctx.disable(moderngl.CULL_FACE)

        self.camera = OrbitCamera()
        self.surface = DragonSurface(SurfaceParameters())

        self.time_seconds = 0.0
        self.paused = False
        self.wind_strength = 0.075

        self.pulse_age: float | None = 0.0
        self.pulse_center = np.asarray((0.0, -0.4), dtype=np.float32)

        self._create_surface_renderer()
        self._create_scale_system()
        self._create_scale_renderer()
        self._register_events()

        self.caption_timer = 0.0
        self._update_caption()

        print(__doc__)

    @staticmethod
    def _create_window() -> pyglet.window.Window:
        try:
            config = pyglet.gl.Config(
                double_buffer=True,
                depth_size=24,
                major_version=3,
                minor_version=3,
            )
            return pyglet.window.Window(
                width=1280,
                height=800,
                caption="Escamas de dragón con resortes",
                resizable=True,
                config=config,
                vsync=True,
            )
        except pyglet.window.NoSuchConfigException:
            config = pyglet.gl.Config(
                double_buffer=True,
                depth_size=24,
            )
            return pyglet.window.Window(
                width=1280,
                height=800,
                caption="Escamas de dragón con resortes",
                resizable=True,
                config=config,
                vsync=True,
            )

    def _create_surface_renderer(self) -> None:
        self.surface_program = self.ctx.program(
            vertex_shader=SURFACE_VERTEX_SHADER,
            fragment_shader=SURFACE_FRAGMENT_SHADER,
        )

        initial_data = self.surface.vertex_data(0.0)
        self.surface_vbo = self.ctx.buffer(initial_data.tobytes(), dynamic=True)
        self.surface_ibo = self.ctx.buffer(self.surface.indices.tobytes())

        self.surface_vao = self.ctx.vertex_array(
            self.surface_program,
            [
                (
                    self.surface_vbo,
                    "3f 3f",
                    "in_position",
                    "in_normal",
                )
            ],
            index_buffer=self.surface_ibo,
            index_element_size=4,
        )

    def _create_scale_system(self) -> None:
        self.scale_rows = 25
        self.scale_columns = 33

        self.anchor_uv, self.scale_edges = build_scale_layout(
            rows=self.scale_rows,
            columns=self.scale_columns,
            spacing_x=0.235,
            spacing_y=0.275,
        )

        self.scale_count = len(self.anchor_uv)

        rng = np.random.default_rng(8)

        rest_angles = np.radians(
            rng.uniform(7.0, 13.5, self.scale_count)
        ).astype(np.float32)

        self.scale_system = AngularScaleSystem(
            rest_angles=rest_angles,
            edges=self.scale_edges,
        )

        self.scale_variants = rng.uniform(
            0.18,
            0.95,
            self.scale_count,
        ).astype(np.float32)

        self.scale_size_x = rng.uniform(
            0.38,
            0.46,
            self.scale_count,
        ).astype(np.float32)

        self.scale_size_y = rng.uniform(
            0.54,
            0.66,
            self.scale_count,
        ).astype(np.float32)

        self.scale_size_z = np.full(
            self.scale_count,
            0.42,
            dtype=np.float32,
        )

        self.scale_phase = rng.uniform(
            0.0,
            math.tau,
            self.scale_count,
        ).astype(np.float32)

    def _create_scale_renderer(self) -> None:
        self.scale_program = self.ctx.program(
            vertex_shader=SCALE_VERTEX_SHADER,
            fragment_shader=SCALE_FRAGMENT_SHADER,
        )

        scale_vertices = build_scale_mesh()
        self.scale_vbo = self.ctx.buffer(scale_vertices.tobytes())

        self.instance_stride_floats = 17
        self.instance_data = np.empty(
            (self.scale_count, self.instance_stride_floats),
            dtype=np.float32,
        )
        self.instance_vbo = self.ctx.buffer(
            reserve=self.instance_data.nbytes,
            dynamic=True,
        )

        self.scale_vao = self.ctx.vertex_array(
            self.scale_program,
            [
                (
                    self.scale_vbo,
                    "3f 3f",
                    "in_position",
                    "in_normal",
                ),
                (
                    self.instance_vbo,
                    "4f 4f 4f 4f 1f /i",
                    "i_model_0",
                    "i_model_1",
                    "i_model_2",
                    "i_model_3",
                    "i_variant",
                ),
            ],
        )

        self._update_instance_buffer()

    def _register_events(self) -> None:
        @self.window.event
        def on_draw() -> None:
            self.draw()

        @self.window.event
        def on_resize(width: int, height: int):
            self.ctx.viewport = (0, 0, max(width, 1), max(height, 1))

        @self.window.event
        def on_mouse_drag(
            x: int,
            y: int,
            dx: int,
            dy: int,
            buttons: int,
            modifiers: int,
        ) -> None:
            del x, y, modifiers
            if buttons & mouse.LEFT:
                self.camera.orbit(dx, dy)

        @self.window.event
        def on_mouse_scroll(
            x: int,
            y: int,
            scroll_x: float,
            scroll_y: float,
        ) -> None:
            del x, y, scroll_x
            self.camera.zoom(scroll_y)

        @self.window.event
        def on_key_press(symbol: int, modifiers: int) -> None:
            del modifiers

            if symbol == key.ESCAPE:
                self.window.close()

            elif symbol == key.SPACE:
                self.trigger_pulse()

            elif symbol == key.UP:
                self.wind_strength = min(self.wind_strength + 0.015, 0.22)
                self._update_caption()

            elif symbol == key.DOWN:
                self.wind_strength = max(self.wind_strength - 0.015, 0.0)
                self._update_caption()

            elif symbol == key.P:
                self.paused = not self.paused
                self._update_caption()

            elif symbol == key.R:
                self.scale_system.reset()
                self.pulse_age = None
                self._update_instance_buffer()

    def trigger_pulse(self) -> None:
        # Centro ligeramente distinto en cada activación.
        phase = 0.65 * self.time_seconds
        self.pulse_center = np.asarray(
            (
                1.4 * math.sin(phase),
                0.9 * math.cos(phase * 0.8),
            ),
            dtype=np.float32,
        )
        self.pulse_age = 0.0

    def _external_torque(self) -> np.ndarray:
        x = self.anchor_uv[:, 0]
        y = self.anchor_uv[:, 1]

        wind_pattern = (
            0.52
            + 0.30 * np.sin(1.7 * y + 2.25 * self.time_seconds)
            + 0.18 * np.sin(2.4 * x - 1.15 * self.time_seconds)
        )

        wind = self.wind_strength * wind_pattern

        body_inertia = 0.018 * np.sin(
            1.20 * self.time_seconds
            + 0.72 * y
            + 0.35 * self.scale_phase
        )

        torque = (wind + body_inertia).astype(np.float32)

        if self.pulse_age is not None:
            dx = x - self.pulse_center[0]
            dy = y - self.pulse_center[1]
            distance = np.sqrt(dx * dx + dy * dy)

            radius = 1.65 * self.pulse_age
            width = 0.34 + 0.04 * self.pulse_age
            envelope = math.exp(-0.72 * self.pulse_age)

            ring = np.exp(
                -((distance - radius) ** 2) / (2.0 * width * width)
            )

            torque += (0.47 * envelope * ring).astype(np.float32)

            if self.pulse_age > 5.0:
                self.pulse_age = None

        return torque

    def _update_instance_buffer(self) -> None:
        x = self.anchor_uv[:, 0]
        y = self.anchor_uv[:, 1]

        positions, tangent_x, tangent_y, normal = self.surface.evaluate(
            x,
            y,
            self.time_seconds,
        )

        # Evita z-fighting entre la piel y la raíz de la escama.
        positions = positions + 0.018 * normal

        angles = self.scale_system.angles
        cosine = np.cos(angles)
        sine = np.sin(angles)

        rotation_x = np.zeros(
            (self.scale_count, 3, 3),
            dtype=np.float32,
        )

        rotation_x[:, 0, 0] = 1.0
        rotation_x[:, 1, 1] = cosine
        rotation_x[:, 1, 2] = -sine
        rotation_x[:, 2, 1] = sine
        rotation_x[:, 2, 2] = cosine

        basis = np.stack(
            (tangent_x, tangent_y, normal),
            axis=2,
        ).astype(np.float32)

        scales = np.column_stack(
            (
                self.scale_size_x,
                self.scale_size_y,
                self.scale_size_z,
            )
        ).astype(np.float32)

        local_transform = rotation_x * scales[:, np.newaxis, :]
        linear = np.einsum(
            "nij,njk->nik",
            basis,
            local_transform,
            optimize=True,
        )

        matrices = np.zeros(
            (self.scale_count, 4, 4),
            dtype=np.float32,
        )
        matrices[:, :3, :3] = linear
        matrices[:, :3, 3] = positions
        matrices[:, 3, 3] = 1.0

        # Cada fila debe contener las cuatro columnas consecutivas de la matriz.
        self.instance_data[:, :16] = matrices.transpose(0, 2, 1).reshape(
            self.scale_count,
            16,
        )
        self.instance_data[:, 16] = self.scale_variants

        self.instance_vbo.write(self.instance_data.tobytes())

    def update(self, dt: float) -> None:
        if self.paused:
            return

        dt = min(float(dt), 1.0 / 30.0)
        self.time_seconds += dt

        if self.pulse_age is not None:
            self.pulse_age += dt

        self.scale_system.step(
            external_torque=self._external_torque(),
            dt=dt,
            substeps=6,
        )

        surface_data = self.surface.vertex_data(self.time_seconds)
        self.surface_vbo.write(surface_data.tobytes())
        self._update_instance_buffer()

        self.caption_timer += dt
        if self.caption_timer > 0.25:
            self.caption_timer = 0.0
            self._update_caption()

    def _view_projection(self) -> tuple[np.ndarray, np.ndarray]:
        aspect = self.window.width / max(self.window.height, 1)
        projection = perspective(
            math.radians(47.0),
            aspect,
            0.08,
            80.0,
        )
        view = self.camera.view_matrix()
        return projection @ view, self.camera.position()

    def draw(self) -> None:
        self.ctx.viewport = (
            0,
            0,
            max(self.window.width, 1),
            max(self.window.height, 1),
        )
        self.ctx.clear(0.009, 0.014, 0.012, 1.0, depth=1.0)

        view_projection, camera_position = self._view_projection()
        light_direction = normalize(
            np.asarray((-0.6, -0.9, -1.7), dtype=np.float32)
        )

        self.surface_program["u_mvp"].write(
            matrix_bytes(view_projection)
        )
        self.surface_program["u_camera_position"].value = tuple(
            float(value) for value in camera_position
        )
        self.surface_program["u_light_direction"].value = tuple(
            float(value) for value in light_direction
        )

        self.surface_vao.render(mode=moderngl.TRIANGLES)

        self.scale_program["u_view_projection"].write(
            matrix_bytes(view_projection)
        )
        self.scale_program["u_camera_position"].value = tuple(
            float(value) for value in camera_position
        )
        self.scale_program["u_light_direction"].value = tuple(
            float(value) for value in light_direction
        )

        self.scale_vao.render(
            mode=moderngl.TRIANGLES,
            instances=self.scale_count,
        )

    def _update_caption(self) -> None:
        state = "pausado" if self.paused else "activo"
        wind_percent = 100.0 * self.wind_strength / 0.22

        self.window.set_caption(
            "Escamas de dragón | "
            f"{self.scale_count} escamas | "
            f"viento {wind_percent:4.0f}% | "
            f"{state} | "
            "ESPACIO: onda  ↑↓: viento  P: pausa  R: reiniciar"
        )

    def run(self) -> None:
        pyglet.clock.schedule_interval(self.update, 1.0 / 120.0)
        pyglet.app.run()


def main() -> None:
    app = DragonScaleDemo()
    app.run()


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        sys.exit(0)
