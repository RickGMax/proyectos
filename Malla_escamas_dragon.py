"""
Controles:
    Clic izquierdo            : generar una onda en el punto tocado
    Arrastrar botón izquierdo : orbitar cámara
    Rueda del mouse           : acercar/alejar
    Flecha arriba/abajo       : cambiar nivel de viento/corriente
    M                         : alternar aire, agua y aceite
    1 / 2 / 3                 : seleccionar aire / agua / aceite
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
import pyglet
import moderngl

pyglet.options["shadow_window"] = False

from pyglet.window import key, mouse  # noqa: E402

# Implementaciones matemáticas (vectores principalmente)


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

# Modelo geométrico de la piel


@dataclass
class SurfaceParameters:
    width: float = 8.0
    length: float = 7.0
    transverse_curvature: float = 0.55
    transverse_frequency: float = 0.43
    wave_amplitude: float = 0.08
    wave_frequency: float = 0.95
    wave_speed: float = 1.40


@dataclass(frozen=True)
class FluidMedium:
    """Propiedades físicas y visuales simplificadas del medio circundante."""

    name: str
    flow_name: str
    damping_multiplier: float
    inertia_multiplier: float
    flow_multiplier: float
    pulse_multiplier: float
    surface_motion_multiplier: float
    ambient_color: tuple[float, float, float]
    fog_color: tuple[float, float, float]
    fog_density: float
    background_color: tuple[float, float, float]
    light_color: tuple[float, float, float]
    specular_color: tuple[float, float, float]
    shininess: float


WIND_LEVELS: tuple[tuple[str, float], ...] = (
    ("calma", 0.000),
    ("brisa", 0.040),
    ("moderado", 0.085),
    ("fuerte", 0.145),
    ("tormenta", 0.220),
)


FLUID_MEDIA: tuple[FluidMedium, ...] = (
    FluidMedium(
        name="aire",
        flow_name="viento",
        damping_multiplier=1.0,
        inertia_multiplier=1.0,
        flow_multiplier=1.0,
        pulse_multiplier=1.0,
        surface_motion_multiplier=1.0,
        ambient_color=(0.16, 0.20, 0.17),
        fog_color=(0.009, 0.014, 0.012),
        fog_density=0.010,
        background_color=(0.009, 0.014, 0.012),
        light_color=(1.00, 0.96, 0.84),
        specular_color=(0.72, 0.82, 0.48),
        shininess=38.0,
    ),
    FluidMedium(
        name="agua",
        flow_name="corriente",
        damping_multiplier=3.4,
        inertia_multiplier=1.65,
        flow_multiplier=1.32,
        pulse_multiplier=0.72,
        surface_motion_multiplier=0.72,
        ambient_color=(0.08, 0.22, 0.25),
        fog_color=(0.018, 0.115, 0.145),
        fog_density=0.105,
        background_color=(0.010, 0.070, 0.095),
        light_color=(0.68, 0.90, 0.88),
        specular_color=(0.58, 0.90, 0.86),
        shininess=52.0,
    ),
    FluidMedium(
        name="aceite",
        flow_name="corriente",
        damping_multiplier=7.5,
        inertia_multiplier=2.15,
        flow_multiplier=1.12,
        pulse_multiplier=0.42,
        surface_motion_multiplier=0.38,
        ambient_color=(0.22, 0.16, 0.055),
        fog_color=(0.115, 0.075, 0.018),
        fog_density=0.175,
        background_color=(0.050, 0.032, 0.008),
        light_color=(1.00, 0.78, 0.38),
        specular_color=(1.00, 0.73, 0.25),
        shininess=70.0,
    ),
)


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

    def height_at(
        self,
        x: np.ndarray,
        y: np.ndarray,
        time_seconds: float,
    ) -> np.ndarray:
        """Altura de la superficie, usada también para detectar clics."""

        p = self.params
        phase = p.wave_frequency * y + p.wave_speed * time_seconds
        return (
            p.transverse_curvature
            * np.cos(p.transverse_frequency * x)
            + p.wave_amplitude * np.sin(phase)
        )

    def vertex_data(self, time_seconds: float) -> np.ndarray:
        positions, _, _, normals = self.evaluate(
            self.grid_x,
            self.grid_y,
            time_seconds,
        )
        return np.concatenate((positions, normals), axis=1).astype(np.float32)


# Sistema de resortes angulares

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
        max_angle: float = math.radians(76.0),
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
        return self.torque_with_medium(external_torque, 1.0)

    def torque_with_medium(
        self,
        external_torque: np.ndarray,
        damping_multiplier: float,
    ) -> np.ndarray:
        result = (
            np.asarray(external_torque, dtype=np.float32)
            - self.root_stiffness * (self.angles - self.rest_angles)
            - self.damping
            * float(damping_multiplier)
            * self.angular_velocities
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
        damping_multiplier: float = 1.0,
        inertia_multiplier: float = 1.0,
    ) -> None:
        if dt <= 0.0:
            return

        h = min(dt, 1.0 / 30.0) / max(substeps, 1)

        for _ in range(substeps):
            acceleration = self.torque_with_medium(
                external_torque,
                damping_multiplier,
            ) / (self.inertia * float(inertia_multiplier))

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


# Distribución de escamas y geometría de una escama


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

# Cámara orbital

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


# Aplicación ModernGL

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
uniform vec3 u_light_color;
uniform vec3 u_ambient_color;
uniform vec3 u_fog_color;
uniform float u_fog_density;
uniform vec3 u_specular_color;
uniform float u_shininess;

out vec4 fragment_color;

vec3 phong_lighting(
    vec3 base_color,
    vec3 normal,
    vec3 world_position,
    float ambient_coefficient,
    float diffuse_coefficient,
    float specular_coefficient
) {
    vec3 light_direction = normalize(-u_light_direction);
    vec3 view_direction = normalize(u_camera_position - world_position);

    float diffuse_factor = max(dot(normal, light_direction), 0.0);
    float specular_factor = 0.0;

    if (diffuse_factor > 0.0) {
        vec3 reflected_light = reflect(-light_direction, normal);
        specular_factor = pow(
            max(dot(view_direction, reflected_light), 0.0),
            u_shininess
        );
    }

    vec3 ambient = ambient_coefficient * u_ambient_color * base_color;
    vec3 diffuse =
        diffuse_coefficient * diffuse_factor * u_light_color * base_color;
    vec3 specular =
        specular_coefficient * specular_factor
        * u_light_color * u_specular_color;

    return ambient + diffuse + specular;
}

void main() {
    vec3 normal = normalize(v_normal);

    vec3 base_color = mix(
        vec3(0.025, 0.070, 0.045),
        vec3(0.070, 0.180, 0.085),
        0.5 + 0.5 * normal.z
    );

    vec3 color = phong_lighting(
        base_color,
        normal,
        v_world_position,
        1.0,
        0.82,
        0.22
    );

    float distance_to_camera = length(
        u_camera_position - v_world_position
    );
    float fog_factor = exp(
        -pow(u_fog_density * distance_to_camera, 2.0)
    );
    color = mix(u_fog_color, color, clamp(fog_factor, 0.0, 1.0));

    // Conversión lineal -> sRGB aproximada para evitar sombras aplastadas.
    color = pow(max(color, vec3(0.0)), vec3(1.0 / 2.2));
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
    // La inversa-transpuesta es necesaria porque cada escama usa escalado
    // no uniforme. Sin ella, la dirección de los brillos queda deformada.
    mat3 normal_matrix = transpose(inverse(mat3(model)));
    v_normal = normalize(normal_matrix * in_normal);
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
uniform vec3 u_light_color;
uniform vec3 u_ambient_color;
uniform vec3 u_fog_color;
uniform float u_fog_density;
uniform vec3 u_specular_color;
uniform float u_shininess;

out vec4 fragment_color;

vec3 phong_lighting(
    vec3 base_color,
    vec3 normal,
    vec3 world_position,
    float ambient_coefficient,
    float diffuse_coefficient,
    float specular_coefficient
) {
    vec3 light_direction = normalize(-u_light_direction);
    vec3 view_direction = normalize(u_camera_position - world_position);

    float diffuse_factor = max(dot(normal, light_direction), 0.0);
    float specular_factor = 0.0;

    if (diffuse_factor > 0.0) {
        vec3 reflected_light = reflect(-light_direction, normal);
        specular_factor = pow(
            max(dot(view_direction, reflected_light), 0.0),
            u_shininess
        );
    }

    vec3 ambient = ambient_coefficient * u_ambient_color * base_color;
    vec3 diffuse =
        diffuse_coefficient * diffuse_factor * u_light_color * base_color;
    vec3 specular =
        specular_coefficient * specular_factor
        * u_light_color * u_specular_color;

    return ambient + diffuse + specular;
}

void main() {
    vec3 normal = normalize(v_normal);

    if (!gl_FrontFacing) {
        normal = -normal;
    }

    vec3 view_direction = normalize(u_camera_position - v_world_position);
    float rim = pow(
        1.0 - max(dot(normal, view_direction), 0.0),
        2.4
    );

    vec3 dark_scale = vec3(0.055, 0.155, 0.075);
    vec3 bright_scale = vec3(0.28, 0.62, 0.20);
    vec3 base_color = mix(dark_scale, bright_scale, v_variant);

    vec3 color = phong_lighting(
        base_color,
        normal,
        v_world_position,
        1.0,
        0.92,
        0.58
    );
    color += base_color * vec3(0.12, 0.24, 0.08) * rim;

    float distance_to_camera = length(
        u_camera_position - v_world_position
    );
    float fog_factor = exp(
        -pow(u_fog_density * distance_to_camera, 2.0)
    );
    color = mix(u_fog_color, color, clamp(fog_factor, 0.0, 1.0));

    color = pow(max(color, vec3(0.0)), vec3(1.0 / 2.2));
    fragment_color = vec4(color, 1.0);
}
"""


class EscamasDragon:
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
        self.wind_level = 2
        self.medium_index = 0
        self.mouse_press_position: tuple[float, float] | None = None
        self.mouse_drag_distance = 0.0

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
        def on_mouse_press(
            x: int,
            y: int,
            button: int,
            modifiers: int,
        ) -> None:
            del modifiers
            if button == mouse.LEFT:
                self.mouse_press_position = (float(x), float(y))
                self.mouse_drag_distance = 0.0

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
                self.mouse_drag_distance += math.hypot(dx, dy)
                self.camera.orbit(dx, dy)

        @self.window.event
        def on_mouse_release(
            x: int,
            y: int,
            button: int,
            modifiers: int,
        ) -> None:
            del x, y, modifiers

            if button != mouse.LEFT:
                return

            press_position = self.mouse_press_position
            self.mouse_press_position = None

            # Separa un clic intencional de un arrastre de cámara.
            if press_position is not None and self.mouse_drag_distance <= 5.0:
                self.trigger_pulse_from_screen(*press_position)

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
                self._change_wind_level(1)

            elif symbol == key.DOWN:
                self._change_wind_level(-1)

            elif symbol == key.M:
                self._set_medium((self.medium_index + 1) % len(FLUID_MEDIA))

            elif symbol == key._1:
                self._set_medium(0)

            elif symbol == key._2:
                self._set_medium(1)

            elif symbol == key._3:
                self._set_medium(2)

            elif symbol == key.P:
                self.paused = not self.paused
                self._update_caption()

            elif symbol == key.R:
                self.scale_system.reset()
                self.pulse_age = None
                self._update_instance_buffer()

    @property
    def medium(self) -> FluidMedium:
        return FLUID_MEDIA[self.medium_index]

    @property
    def wind_strength(self) -> float:
        return WIND_LEVELS[self.wind_level][1]

    def _change_wind_level(self, direction: int) -> None:
        self.wind_level = int(
            np.clip(
                self.wind_level + direction,
                0,
                len(WIND_LEVELS) - 1,
            )
        )
        self._update_caption()

    def _set_medium(self, index: int) -> None:
        self.medium_index = int(np.clip(index, 0, len(FLUID_MEDIA) - 1))
        surface_time = (
            self.time_seconds * self.medium.surface_motion_multiplier
        )
        self.surface_vbo.write(self.surface.vertex_data(surface_time).tobytes())
        self._update_instance_buffer()
        self._update_caption()

    def trigger_pulse(
        self,
        center: np.ndarray | None = None,
    ) -> None:
        if center is None:
            # El teclado conserva una onda automática para demostración.
            phase = 0.65 * self.time_seconds
            center = np.asarray(
                (
                    1.4 * math.sin(phase),
                    0.9 * math.cos(phase * 0.8),
                ),
                dtype=np.float32,
            )

        self.pulse_center = np.asarray(center[:2], dtype=np.float32)
        self.pulse_age = 0.0

    def trigger_pulse_from_screen(
        self,
        screen_x: float,
        screen_y: float,
    ) -> None:
        """Proyecta el clic a la superficie curva y crea allí la onda."""

        hit = self._screen_to_surface(screen_x, screen_y)
        if hit is not None:
            self.trigger_pulse(hit[:2])

    def _screen_to_surface(
        self,
        screen_x: float,
        screen_y: float,
    ) -> np.ndarray | None:
        """Intersección rayo-superficie para una malla definida como z=f(x,y)."""

        width = max(self.window.width, 1)
        height = max(self.window.height, 1)
        ndc_x = 2.0 * screen_x / width - 1.0
        ndc_y = 2.0 * screen_y / height - 1.0

        view_projection, _ = self._view_projection()
        inverse_view_projection = np.linalg.inv(view_projection)

        def unproject(ndc_z: float) -> np.ndarray:
            clip = np.asarray(
                (ndc_x, ndc_y, ndc_z, 1.0),
                dtype=np.float32,
            )
            world = inverse_view_projection @ clip
            return (world[:3] / world[3]).astype(np.float32)

        ray_origin = unproject(-1.0)
        ray_direction = normalize(unproject(1.0) - ray_origin)

        # Limita el recorrido al rectángulo (x, y) ocupado por la piel.
        params = self.surface.params
        bounds = (
            (-0.5 * params.width, 0.5 * params.width),
            (-0.5 * params.length, 0.5 * params.length),
        )
        t_start = 0.0
        t_end = 80.0

        for axis, (minimum, maximum) in enumerate(bounds):
            component = float(ray_direction[axis])
            origin = float(ray_origin[axis])

            if abs(component) < 1.0e-8:
                if origin < minimum or origin > maximum:
                    return None
                continue

            first = (minimum - origin) / component
            second = (maximum - origin) / component
            near_axis, far_axis = sorted((first, second))
            t_start = max(t_start, near_axis)
            t_end = min(t_end, far_axis)

            if t_start > t_end:
                return None

        surface_time = (
            self.time_seconds * self.medium.surface_motion_multiplier
        )
        sample_times = np.linspace(t_start, t_end, 96, dtype=np.float32)
        points = (
            ray_origin[np.newaxis, :]
            + sample_times[:, np.newaxis] * ray_direction[np.newaxis, :]
        )
        heights = self.surface.height_at(
            points[:, 0],
            points[:, 1],
            surface_time,
        )
        differences = points[:, 2] - heights

        crossings = np.flatnonzero(differences[:-1] * differences[1:] <= 0.0)
        if crossings.size == 0:
            return None

        low = float(sample_times[int(crossings[0])])
        high = float(sample_times[int(crossings[0]) + 1])

        # Bisección para que la onda nazca visualmente bajo el cursor.
        for _ in range(18):
            middle = 0.5 * (low + high)
            point = ray_origin + middle * ray_direction
            surface_z = float(
                self.surface.height_at(
                    np.asarray(point[0]),
                    np.asarray(point[1]),
                    surface_time,
                )
            )

            low_point = ray_origin + low * ray_direction
            low_surface_z = float(
                self.surface.height_at(
                    np.asarray(low_point[0]),
                    np.asarray(low_point[1]),
                    surface_time,
                )
            )
            low_difference = float(low_point[2]) - low_surface_z
            middle_difference = float(point[2]) - surface_z

            if low_difference * middle_difference <= 0.0:
                high = middle
            else:
                low = middle

        hit = ray_origin + 0.5 * (low + high) * ray_direction
        hit[2] = float(
            self.surface.height_at(
                np.asarray(hit[0]),
                np.asarray(hit[1]),
                surface_time,
            )
        )
        return hit.astype(np.float32)

    def _storm_chaos(self) -> float:
        """Activa progresivamente la turbulencia en fuerte y tormenta."""

        intensity = self.wind_level / max(len(WIND_LEVELS) - 1, 1)
        onset = max((intensity - 0.5) / 0.5, 0.0)
        return onset * onset

    def _external_torque(self) -> np.ndarray:
        x = self.anchor_uv[:, 0]
        y = self.anchor_uv[:, 1]

        wind_pattern = (
            0.52
            + 0.30 * np.sin(1.7 * y + 2.25 * self.time_seconds)
            + 0.18 * np.sin(2.4 * x - 1.15 * self.time_seconds)
        )

        chaos = self._storm_chaos()
        gusts = (
            0.58
            + 0.28 * math.sin(0.83 * self.time_seconds)
            + 0.14 * math.sin(2.17 * self.time_seconds + 1.3)
        )
        turbulence = (
            0.62
            * np.sin(
                5.2 * x
                - 5.7 * self.time_seconds
                + 1.8 * self.scale_phase
            )
            * np.sin(4.1 * y + 3.8 * self.time_seconds)
            + 0.38
            * np.sin(
                8.5 * x
                - 6.3 * y
                + 8.4 * self.time_seconds
                + self.scale_phase
            )
        )
        wind_pattern += chaos * (0.95 * gusts + 0.72 * turbulence)

        wind = (
            self.wind_strength
            * self.medium.flow_multiplier
            * wind_pattern
        )

        body_inertia = (
            0.018
            * (1.0 + 1.7 * chaos)
            / self.medium.inertia_multiplier
            * np.sin(
                1.20 * self.time_seconds
                + 0.72 * y
                + 0.35 * self.scale_phase
            )
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

            torque += (
                0.47
                * self.medium.pulse_multiplier
                * envelope
                * ring
            ).astype(np.float32)

            if self.pulse_age > 5.0:
                self.pulse_age = None

        return torque

    def _update_instance_buffer(self) -> None:
        x = self.anchor_uv[:, 0]
        y = self.anchor_uv[:, 1]

        positions, tangent_x, tangent_y, normal = self.surface.evaluate(
            x,
            y,
            self.time_seconds * self.medium.surface_motion_multiplier,
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
            damping_multiplier=(
                self.medium.damping_multiplier
                * (1.0 - 0.28 * self._storm_chaos())
            ),
            inertia_multiplier=self.medium.inertia_multiplier,
        )

        surface_data = self.surface.vertex_data(
            self.time_seconds * self.medium.surface_motion_multiplier
        )
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
        background = self.medium.background_color
        self.ctx.clear(*background, 1.0, depth=1.0)

        view_projection, camera_position = self._view_projection()
        light_direction = normalize(
            np.asarray((-0.6, -0.9, -1.7), dtype=np.float32)
        )

        self._set_lighting_uniforms(
            self.surface_program,
            camera_position,
            light_direction,
        )
        self.surface_program["u_mvp"].write(matrix_bytes(view_projection))

        self.surface_vao.render(mode=moderngl.TRIANGLES)

        self.scale_program["u_view_projection"].write(
            matrix_bytes(view_projection)
        )
        self._set_lighting_uniforms(
            self.scale_program,
            camera_position,
            light_direction,
        )

        self.scale_vao.render(
            mode=moderngl.TRIANGLES,
            instances=self.scale_count,
        )

    def _set_lighting_uniforms(
        self,
        program: moderngl.Program,
        camera_position: np.ndarray,
        light_direction: np.ndarray,
    ) -> None:
        """Carga el mismo modelo Phong y los parámetros del medio."""

        vector_uniforms = {
            "u_camera_position": camera_position,
            "u_light_direction": light_direction,
            "u_light_color": self.medium.light_color,
            "u_ambient_color": self.medium.ambient_color,
            "u_fog_color": self.medium.fog_color,
            "u_specular_color": self.medium.specular_color,
        }

        for name, values in vector_uniforms.items():
            program[name].value = tuple(float(value) for value in values)

        program["u_fog_density"].value = self.medium.fog_density
        program["u_shininess"].value = self.medium.shininess

    def _update_caption(self) -> None:
        state = "pausado" if self.paused else "activo"
        wind_name = WIND_LEVELS[self.wind_level][0]

        self.window.set_caption(
            "Escamas de dragón | "
            f"{self.scale_count} escamas | "
            f"medio: {self.medium.name} | "
            f"{self.medium.flow_name}: {wind_name} "
            f"({self.wind_level}/{len(WIND_LEVELS) - 1}) | "
            f"{state} | "
            "CLIC: onda  M/1-3: medio  ↑↓: flujo  P: pausa  R: reiniciar"
        )

    def run(self) -> None:
        pyglet.clock.schedule_interval(self.update, 1.0 / 120.0)
        pyglet.app.run()


def main() -> None:
    app = EscamasDragon()
    app.run()


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        sys.exit(0)
