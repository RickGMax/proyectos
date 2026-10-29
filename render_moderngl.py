"""Primitivas 2D dibujadas con ModernGL y buffers reutilizables.

Pyglet conserva la ventana y el texto. Los círculos, rectángulos y líneas se
envían como instancias de un mismo cuadrilátero; el shader resuelve su borde.
Las posiciones se expresan en píxeles lógicos, con el origen abajo a la izquierda.
"""

from array import array
import math

import moderngl


_VERTEX_SHADER = """
#version 330
in vec2 in_corner;
in vec2 in_center;
in vec2 in_half_size;
in vec2 in_rotation;
in float in_radius;
in vec4 in_color;
uniform vec2 viewport_size;
uniform float edge_padding;
out vec2 local_position;
flat out vec2 half_size;
flat out float corner_radius;
flat out vec4 shape_color;
void main() {
    vec2 local = in_corner * (in_half_size + vec2(edge_padding));
    vec2 rotated = vec2(
        in_rotation.x * local.x - in_rotation.y * local.y,
        in_rotation.y * local.x + in_rotation.x * local.y
    );
    vec2 screen = in_center + rotated;
    gl_Position = vec4(screen / viewport_size * 2.0 - 1.0, 0.0, 1.0);
    local_position = local;
    half_size = in_half_size;
    corner_radius = in_radius;
    shape_color = in_color;
}
"""

_FRAGMENT_SHADER = """
#version 330
in vec2 local_position;
flat in vec2 half_size;
flat in float corner_radius;
flat in vec4 shape_color;
out vec4 fragment_color;
void main() {
    vec2 q = abs(local_position) - half_size + vec2(corner_radius);
    float distance_to_edge = length(max(q, 0.0))
        + min(max(q.x, q.y), 0.0) - corner_radius;
    float pixel_width = max(fwidth(distance_to_edge), 0.001);
    float coverage = 1.0 - smoothstep(
        -pixel_width * 0.5, pixel_width * 0.5, distance_to_edge
    );
    float alpha = shape_color.a * coverage;
    if (alpha <= 0.0) discard;
    fragment_color = vec4(shape_color.rgb, alpha);
}
"""

_QUAD_VERTICES = array("f", (
    -1, -1, 1, -1, 1, 1,
    -1, -1, 1, 1, -1, 1,
))
_FLOATS_PER_INSTANCE = 11
_BYTES_PER_INSTANCE = _FLOATS_PER_INSTANCE * 4


class Renderer2D:
    """Comparte un programa de shaders entre los lotes de una ventana."""

    def __init__(self, window, context=None):
        self.window = window
        if hasattr(window, "switch_to"):
            window.switch_to()
        self.ctx = context if context is not None else moderngl.create_context(require=330)
        self._owns_context = context is None
        self._released = False
        self._batches = []
        self._program = self.ctx.program(
            vertex_shader=_VERTEX_SHADER, fragment_shader=_FRAGMENT_SHADER,
        )
        self._quad = self.ctx.buffer(_QUAD_VERTICES.tobytes())

    def batch(self):
        """Crea un lote independiente, sin crear un programa por cada forma."""
        if self._released:
            raise RuntimeError("el renderer ya fue liberado")
        batch = Batch(self)
        self._batches.append(batch)
        return batch

    def _prepare_target(self):
        width = max(1, self.window.width)
        height = max(1, self.window.height)
        framebuffer_size = getattr(self.window, "get_framebuffer_size", None)
        pixels = framebuffer_size() if framebuffer_size else (width, height)
        pixel_width, pixel_height = max(1, pixels[0]), max(1, pixels[1])
        if self.ctx.screen is not None:
            self.ctx.screen.use()
        self.ctx.viewport = (0, 0, pixel_width, pixel_height)
        self.ctx.scissor = None
        self._program["viewport_size"].value = (width, height)
        self._program["edge_padding"].value = 1.5 * max(
            width / pixel_width, height / pixel_height,
        )

    def clear(self, color=(0.0, 0.0, 0.0, 1.0)):
        """Limpia la ventana con componentes RGB o RGBA entre cero y uno."""
        if self._released:
            return
        components = tuple(color)
        if len(components) == 3:
            components += (1.0,)
        if len(components) != 4:
            raise ValueError("el color de fondo debe tener tres o cuatro componentes")
        self._prepare_target()
        self.ctx.clear(*components)

    def release(self):
        """Libera los recursos antes de cerrar la ventana; admite varias llamadas."""
        if self._released:
            return
        for batch in tuple(self._batches):
            batch.release()
        self._quad.release()
        self._program.release()
        if self._owns_context:
            self.ctx.release()
        self._released = True


class Batch:
    """Lote de formas ordenadas por grupo y por su orden de creación."""

    def __init__(self, renderer):
        self.renderer = renderer
        self._shapes = {}
        self._next_order = 0
        self._capacity = 64
        self._released = False
        self._instances = renderer.ctx.buffer(
            reserve=self._capacity * _BYTES_PER_INSTANCE, dynamic=True,
        )
        self._vao = renderer.ctx.vertex_array(renderer._program, [
            (renderer._quad, "2f", "in_corner"),
            (self._instances, "2f 2f 2f 1f 4f /i", "in_center", "in_half_size",
             "in_rotation", "in_radius", "in_color"),
        ])

    def _add(self, shape):
        if self._released:
            raise RuntimeError("el lote ya fue liberado")
        self._shapes[shape] = self._next_order
        self._next_order += 1

    def _remove(self, shape):
        self._shapes.pop(shape, None)

    def draw(self):
        if self._released or self.renderer._released:
            return
        data = array("f")
        ordered = sorted(
            self._shapes,
            key=lambda shape: (getattr(shape.group, "order", 0), self._shapes[shape]),
        )
        for shape in ordered:
            if not shape.visible or shape.opacity <= 0:
                continue
            geometry = shape._geometry()
            if geometry is not None:
                data.extend(geometry)
                data.extend(component / 255.0 for component in shape.color)
        count = len(data) // _FLOATS_PER_INSTANCE
        if not count:
            return
        if count > self._capacity:
            self._capacity = 1 << (count - 1).bit_length()
            self._instances.orphan(self._capacity * _BYTES_PER_INSTANCE)
        self._instances.write(data.tobytes())
        self.renderer._prepare_target()
        ctx = self.renderer.ctx
        ctx.disable(moderngl.DEPTH_TEST | moderngl.CULL_FACE)
        ctx.enable(moderngl.BLEND)
        ctx.blend_equation = moderngl.FUNC_ADD
        ctx.blend_func = (
            moderngl.SRC_ALPHA, moderngl.ONE_MINUS_SRC_ALPHA,
            moderngl.ONE, moderngl.ONE_MINUS_SRC_ALPHA,
        )
        self._vao.render(mode=moderngl.TRIANGLES, vertices=6, instances=count)

    def release(self):
        if self._released:
            return
        for shape in self._shapes:
            shape.batch = None
        self._shapes.clear()
        self._vao.release()
        self._instances.release()
        self._released = True
        if self in self.renderer._batches:
            self.renderer._batches.remove(self)


class _Shape:
    def __init__(self, x, y, color, batch, group):
        if batch is None:
            raise ValueError("cada forma necesita un lote del Renderer2D")
        self.x = x
        self.y = y
        self.group = group
        self.visible = True
        self._rgb = (255, 255, 255)
        self._opacity = 255
        self.color = color
        self.batch = batch
        batch._add(self)

    @property
    def position(self):
        return self.x, self.y

    @position.setter
    def position(self, value):
        self.x, self.y = value

    @property
    def color(self):
        return self._rgb + (self._opacity,)

    @color.setter
    def color(self, value):
        values = tuple(value)
        if len(values) not in (3, 4):
            raise ValueError("el color debe tener tres o cuatro componentes")
        self._rgb = tuple(max(0, min(255, int(component))) for component in values[:3])
        if len(values) == 4:
            self.opacity = values[3]

    @property
    def opacity(self):
        return self._opacity

    @opacity.setter
    def opacity(self, value):
        self._opacity = max(0, min(255, int(value)))

    def delete(self):
        if self.batch is not None:
            self.batch._remove(self)
            self.batch = None


class Circle(_Shape):
    """Círculo centrado en (x, y), con radio expresado en píxeles lógicos."""

    def __init__(self, x, y, radius, color=(255, 255, 255), batch=None, group=None):
        self.radius = radius
        super().__init__(x, y, color, batch, group)

    def _geometry(self):
        radius = max(0.0, self.radius)
        if radius == 0:
            return None
        return self.x, self.y, radius, radius, 1.0, 0.0, radius


class Rectangle(_Shape):
    """Rectángulo cuya esquina inferior izquierda está en (x, y)."""

    def __init__(self, x, y, width, height, color=(255, 255, 255), batch=None, group=None):
        self.width = width
        self.height = height
        super().__init__(x, y, color, batch, group)

    def _geometry(self):
        width, height = abs(self.width), abs(self.height)
        if width == 0 or height == 0:
            return None
        return (self.x + self.width / 2, self.y + self.height / 2,
                width / 2, height / 2, 1.0, 0.0, 0.0)


class RoundedRectangle(Rectangle):
    def __init__(self, x, y, width, height, radius=10, color=(255, 255, 255), batch=None, group=None):
        self.radius = radius
        super().__init__(x, y, width, height, color, batch, group)

    def _geometry(self):
        geometry = super()._geometry()
        if geometry is None:
            return None
        radius = max(0.0, min(self.radius, geometry[2], geometry[3]))
        return geometry[:-1] + (radius,)


class Line(_Shape):
    """Segmento dibujado como un cuadrilátero, sin depender de glLineWidth."""

    def __init__(self, x, y, x2, y2, thickness=1.0, color=(255, 255, 255), batch=None, group=None):
        self.x2 = x2
        self.y2 = y2
        self.thickness = thickness
        super().__init__(x, y, color, batch, group)

    def _geometry(self):
        dx, dy = self.x2 - self.x, self.y2 - self.y
        length = math.hypot(dx, dy)
        thickness = max(0.0, self.thickness)
        if length == 0 or thickness == 0:
            return None
        return ((self.x + self.x2) / 2, (self.y + self.y2) / 2,
                length / 2, thickness / 2, dx / length, dy / length, 0.0)
