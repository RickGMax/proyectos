import pyglet
from pyglet import shapes
from pyglet.window import key
from scipy.spatial import KDTree
import numpy as np
import random

# CONFIGURACIÓN

WIDTH  = 1200
HEIGHT = 920

NUM_HEALTHY = 40
NUM_CANCER  = 3
NUM_WHITE   = 5

MAX_SPEED = 68
MAX_FORCE = 20

# Radios de flocking
NEIGHBOR_RADIUS   = 60
SEPARATION_RADIUS = 25

# Infección
INFECTION_RADIUS = 30
INFECTION_CHANCE = 0.05

# Leucocitos
WHITE_KILL_RADIUS    = 20
WHITE_SPEED          = 150
WHITE_DETECT_RADIUS  = 200   # radio de detección del leucocito (KD-tree)

# Comportamientos nuevos habilitados por KD-tree
FEAR_RADIUS          = 80    # sanas huyen de cáncer cercano
CLUSTER_BOOST_MIN    = 4     # si hay >= N sanas cerca, aumentan velocidad (enjambre denso)
CANCER_HUNT_RADIUS   = 120   # cáncer busca sana más cercana dentro de este radio
OVERCROWD_RADIUS     = 30    # si hay > OVERCROWD_N del mismo tipo muy cerca, se dispersan
OVERCROWD_N          = 5

cursor_mode  = None
show_heatmap = False   # toggle con tecla G

# VENTANA

window = pyglet.window.Window(WIDTH, HEIGHT, "Cancer Boids")
batch        = pyglet.graphics.Batch()
ui_batch     = pyglet.graphics.Batch()
heatmap_batch = pyglet.graphics.Batch()

group_heatmap = pyglet.graphics.Group(order=0)   # debajo de las células
group_cells   = pyglet.graphics.Group(order=1)
group_ui_txt  = pyglet.graphics.Group(order=2)

# FPS

fps_display = pyglet.window.FPSDisplay(window)
fps_display.label.font_size = 11
fps_display.label.x = WIDTH - 80
fps_display.label.y = HEIGHT - 18

# HUD

hud_label = pyglet.text.Label(
    "",
    font_name="Courier New",
    font_size=11,
    x=14,
    y=HEIGHT - 14,
    anchor_x="left",
    anchor_y="top",
    color=(180, 255, 180, 255),
    multiline=True,
    width=340,
    batch=ui_batch,
    group=group_ui_txt
)

# UTILIDADES

def limit(vector, max_value):
    norm = np.linalg.norm(vector)
    if norm > max_value:
        return vector / norm * max_value
    return vector

# ESTADO GLOBAL KD-TREE
# Reconstruido una vez por frame en update()

# Posiciones como arrays (N, 2) para construir los árboles
_kd_all      = None   # KDTree de todas las células (Cell)
_kd_healthy  = None   # KDTree solo de sanas
_kd_cancer   = None   # KDTree solo de cancerígenas
_kd_white    = None   # KDTree de leucocitos

_pos_all     = None   # np.array (N,2) alineado con lista cells
_pos_healthy = None
_pos_cancer  = None
_pos_white   = None

_idx_healthy = None   # índices dentro de cells[] que son sanas
_idx_cancer  = None   # índices dentro de cells[] que son cáncer


def rebuild_kdtrees(cells, white_cells):
    """Reconstruye todos los KD-trees. Llamar UNA vez por frame."""
    global _kd_all, _kd_healthy, _kd_cancer, _kd_white
    global _pos_all, _pos_healthy, _pos_cancer, _pos_white
    global _idx_healthy, _idx_cancer

    if not cells:
        _kd_all = _kd_healthy = _kd_cancer = None
        _pos_all = _pos_healthy = _pos_cancer = np.empty((0, 2))
        _idx_healthy = []
        _idx_cancer  = []
    else:
        _pos_all    = np.array([c.position for c in cells], dtype=np.float32)
        _kd_all     = KDTree(_pos_all)

        _idx_healthy = [i for i, c in enumerate(cells) if not c.cancer]
        _idx_cancer  = [i for i, c in enumerate(cells) if c.cancer]

        if _idx_healthy:
            _pos_healthy = _pos_all[_idx_healthy]
            _kd_healthy  = KDTree(_pos_healthy)
        else:
            _pos_healthy = np.empty((0, 2))
            _kd_healthy  = None

        if _idx_cancer:
            _pos_cancer = _pos_all[_idx_cancer]
            _kd_cancer  = KDTree(_pos_cancer)
        else:
            _pos_cancer = np.empty((0, 2))
            _kd_cancer  = None

    if not white_cells:
        _kd_white  = None
        _pos_white = np.empty((0, 2))
    else:
        _pos_white = np.array([w.position for w in white_cells], dtype=np.float32)
        _kd_white  = KDTree(_pos_white)


# CELL

class Cell:

    def __init__(self, position, velocity, cancer=False):
        self.position     = np.array(position, dtype=np.float32)
        self.velocity     = np.array(velocity,  dtype=np.float32)
        self.acceleration = np.zeros(2, dtype=np.float32)
        self.cancer    = cancer
        self.max_speed = MAX_SPEED
        self.max_force = MAX_FORCE
        self.radius    = 8 if cancer else 6
        self.alive     = True

        color = (220, 60, 90) if cancer else (80, 200, 120)
        self.circle = shapes.Circle(
            self.position[0], self.position[1],
            self.radius, color=color,
            batch=batch, group=group_cells
        )

    # 
    def update(self, dt):
        self.velocity     += self.acceleration * dt
        self.velocity      = limit(self.velocity, self.max_speed)
        self.position     += self.velocity * dt
        self.acceleration *= 0
        self.wrap_screen()
        self.circle.x = self.position[0]
        self.circle.y = self.position[1]

    def wrap_screen(self):
        if self.position[0] < 0:     self.position[0] = WIDTH
        if self.position[0] > WIDTH:  self.position[0] = 0
        if self.position[1] < 0:     self.position[1] = HEIGHT
        if self.position[1] > HEIGHT: self.position[1] = 0

    def apply_force(self, force):
        self.acceleration += force

    def destroy(self):
        self.circle.delete()
        self.alive = False

    # FLOCK,usa KD-trees globales ya construidos

    def flock(self, cells):
        pos = self.position

        if self.cancer:
            self._flock_cancer(cells, pos)
        else:
            self._flock_healthy(cells, pos)

    #célula sana
    def _flock_healthy(self, cells, pos):

        #vecinos para flocking estándar (todos) 
        if _kd_all is not None:
            idxs = _kd_all.query_ball_point(pos, NEIGHBOR_RADIUS)
        else:
            idxs = []

        neighbors = [cells[i] for i in idxs if cells[i] is not self]

        alignment  = self._alignment(neighbors)
        cohesion   = self._cohesion(neighbors)
        separation = self._separation_from(neighbors, SEPARATION_RADIUS)

        self.apply_force(alignment  * 1.0)
        self.apply_force(cohesion   * 0.8)
        self.apply_force(separation * 1.5)

        #huida del cáncer cercano (KD-tree cáncer)
        if _kd_cancer is not None:
            near_c_idxs = _kd_cancer.query_ball_point(pos, FEAR_RADIUS)
            if near_c_idxs:
                flee = np.zeros(2, dtype=np.float32)
                for ci in near_c_idxs:
                    diff = pos - _pos_cancer[ci]
                    d = np.linalg.norm(diff)
                    if d > 0:
                        flee += diff / (d * d)   # fuerza inversamente proporcional
                flee = limit(flee, self.max_force * 1.2)
                self.apply_force(flee)

        #boost de velocidad en enjambre denso (sanas cercanas)
        if _kd_healthy is not None:
            near_h = _kd_healthy.query_ball_point(pos, NEIGHBOR_RADIUS * 0.6)
            if len(near_h) >= CLUSTER_BOOST_MIN:
                # el enjambre denso se mueve más rápido temporalmente
                boost = self.velocity / (np.linalg.norm(self.velocity) + 1e-6) * 8
                self.apply_force(boost)

        #dispersión por sobrepoblación local
        if _kd_healthy is not None:
            crowd = _kd_healthy.query_ball_point(pos, OVERCROWD_RADIUS)
            if len(crowd) > OVERCROWD_N:
                self.apply_force(self._separation_from(
                    [cells[_idx_healthy[i]] for i in crowd], OVERCROWD_RADIUS
                ) * 2.0)

    #célula cancerigena
    def _flock_cancer(self, cells, pos):

        #alineación débil entre células cancerígenas
        if _kd_cancer is not None:
            idxs = _kd_cancer.query_ball_point(pos, NEIGHBOR_RADIUS)
            cancer_neighbors = [cells[_idx_cancer[i]] for i in idxs
                                 if cells[_idx_cancer[i]] is not self]
        else:
            cancer_neighbors = []

        self.apply_force(self._alignment(cancer_neighbors)  * 0.1)
        self.apply_force(self._separation_from(cancer_neighbors, SEPARATION_RADIUS) * 0.8)

        #caza dirigida de la sana más cercana dentro de radio
        if _kd_healthy is not None and len(_pos_healthy) > 0:
            dist, local_idx = _kd_healthy.query(pos, k=1)
            if dist < CANCER_HUNT_RADIUS:
                target_pos = _pos_healthy[local_idx]
                desired    = target_pos - pos
                norm       = np.linalg.norm(desired)
                if norm > 0:
                    desired = desired / norm * (self.max_speed * 1.4)
                steering = limit(desired - self.velocity, self.max_force * 1.5)
                self.apply_force(steering)

        #movimiento errático
        self.apply_force(np.random.uniform(-1, 1, 2) * 10)

    #helpers de steering

    def _alignment(self, neighbors):
        if not neighbors:
            return np.zeros(2)
        avg = np.mean([n.velocity for n in neighbors], axis=0)
        norm = np.linalg.norm(avg)
        if norm > 0:
            avg = avg / norm * self.max_speed
        return limit(avg - self.velocity, self.max_force)

    def _cohesion(self, neighbors):
        if not neighbors:
            return np.zeros(2)
        center = np.mean([n.position for n in neighbors], axis=0)
        desired = center - self.position
        norm = np.linalg.norm(desired)
        if norm > 0:
            desired = desired / norm * self.max_speed
        return limit(desired - self.velocity, self.max_force)

    def _separation_from(self, neighbors, radius):
        steering = np.zeros(2)
        total = 0
        for n in neighbors:
            offset = self.position - n.position
            d = np.linalg.norm(offset)
            if 0 < d < radius:
                steering += offset / d
                total += 1
        if total > 0:
            steering /= total
            norm = np.linalg.norm(steering)
            if norm > 0:
                steering = steering / norm * self.max_speed
            steering = limit(steering - self.velocity, self.max_force)
        return steering


# WHITE CELL

class WhiteCell:

    def __init__(self, position, velocity=None):
        self.position = np.array(position, dtype=np.float32)
        if velocity is None:
            velocity = np.random.uniform(-60, 60, 2)
        self.velocity     = np.array(velocity, dtype=np.float32)
        self.acceleration = np.zeros(2, dtype=np.float32)
        self.max_speed = WHITE_SPEED
        self.max_force = 60.0
        self.radius    = 7
        self.alive     = True

        self.circle = shapes.Circle(
            self.position[0], self.position[1],
            self.radius, color=(230, 240, 255),
            batch=batch, group=group_cells
        )
        self.nucleus = shapes.Circle(
            self.position[0], self.position[1],
            3, color=(40, 80, 180),
            batch=batch, group=group_cells
        )

    def update(self, dt):
        #búsqueda de cáncer más cercano con KD-tree + radio de detección
        if _kd_cancer is not None and len(_pos_cancer) > 0:
            dist, local_idx = _kd_cancer.query(self.position, k=1)
            if dist < WHITE_DETECT_RADIUS:
                target_pos = _pos_cancer[local_idx]
                desired    = target_pos - self.position
                norm       = np.linalg.norm(desired)
                if norm > 0:
                    desired = desired / norm * self.max_speed
                steering = limit(desired - self.velocity, self.max_force)
                self.acceleration += steering
            else:
                # fuera de rango: deambula
                self.acceleration += np.random.uniform(-1, 1, 2) * 15
        else:
            self.acceleration += np.random.uniform(-1, 1, 2) * 15

        #separación entre leucocitos (evitan aglomerarse)
        if _kd_white is not None and len(_pos_white) > 1:
            near = _kd_white.query_ball_point(self.position, SEPARATION_RADIUS * 1.5)
            sep  = np.zeros(2, dtype=np.float32)
            cnt  = 0
            for wi in near:
                offset = self.position - _pos_white[wi]
                d = np.linalg.norm(offset)
                if 0 < d < SEPARATION_RADIUS * 1.5:
                    sep += offset / d
                    cnt += 1
            if cnt > 0:
                sep /= cnt
                norm = np.linalg.norm(sep)
                if norm > 0:
                    sep = sep / norm * self.max_speed
                self.acceleration += limit(sep - self.velocity, self.max_force) * 0.5

        self.velocity     += self.acceleration * dt
        self.velocity      = limit(self.velocity, self.max_speed)
        self.position     += self.velocity * dt
        self.acceleration *= 0
        self.wrap_screen()

        self.circle.x  = self.position[0]
        self.circle.y  = self.position[1]
        self.nucleus.x = self.position[0]
        self.nucleus.y = self.position[1]

    def wrap_screen(self):
        if self.position[0] < 0:     self.position[0] = WIDTH
        if self.position[0] > WIDTH:  self.position[0] = 0
        if self.position[1] < 0:     self.position[1] = HEIGHT
        if self.position[1] > HEIGHT: self.position[1] = 0

    def destroy(self):
        self.circle.delete()
        self.nucleus.delete()
        self.alive = False


# CREACIÓN INICIAL

cells       = []
white_cells = []

for _ in range(NUM_HEALTHY):
    position = (random.uniform(0, WIDTH), random.uniform(0, HEIGHT))
    velocity = np.random.uniform(-50, 50, 2)
    cells.append(Cell(position, velocity, cancer=False))

for _ in range(NUM_CANCER):
    position = (WIDTH/2 + random.uniform(-50, 50), HEIGHT/2 + random.uniform(-50, 50))
    velocity = np.random.uniform(-80, 80, 2)
    cells.append(Cell(position, velocity, cancer=True))

for _ in range(NUM_WHITE):
    position = (random.uniform(0, WIDTH), random.uniform(0, HEIGHT))
    white_cells.append(WhiteCell(position))

# INFECCIÓN  —  usa KD-tree de sanas

def infection_step():
    if _kd_healthy is None or _kd_cancer is None:
        return

    # Para cada célula sana, busca cáncer cercano en O(log n)
    for li, hi in enumerate(_idx_healthy):
        h = cells[hi]
        near_c = _kd_cancer.query_ball_point(h.position, INFECTION_RADIUS)
        if near_c and random.random() < INFECTION_CHANCE:
            h.cancer        = True
            h.radius        = 8
            h.circle.radius = 8
            h.circle.color  = (220, 60, 90)

# ATAQUE LEUCOCITOS (usa KD-tree de cáncer)

def white_cell_attack():
    global cells, white_cells

    if _kd_cancer is None:
        return

    killed = set()
    for wc in white_cells:
        near = _kd_cancer.query_ball_point(wc.position, WHITE_KILL_RADIUS)
        for ci in near:
            real_idx = _idx_cancer[ci]
            if real_idx not in killed and cells[real_idx].alive:
                cells[real_idx].destroy()
                killed.add(real_idx)
                break   # un kill por leucocito por frame

    cells = [c for c in cells if c.alive]

# GRILLA GEOMÉTRICA
#
#  Dibuja las líneas de la grilla (horizontales + verticales).
#  Cada nodo de intersección pulsa de color según qué tipo de célula
#  domina en su vecindad inmediata (KD-tree).
#
#  Colores de línea:
#    Sin actividad → gris oscuro tenue
#    Sanas cerca   → verde
#    Cáncer cerca  → rojo
#    Leucos cerca  → azul
#
#  Implementación: las líneas son shapes.Line estáticas (posición fija).
#  Solo su color se actualiza cada frame. Segmentos horizontales y
#  verticales se tratan por separado para colorear tramo a tramo.

GRID_COLS = 24
GRID_ROWS = 16
CELL_W    = WIDTH  // GRID_COLS     # ancho de cada celda
CELL_H    = HEIGHT // GRID_ROWS     # alto  de cada celda

GRID_RADIUS = 70    # radio de influencia sobre cada nodo
GRID_SAT    = 6     # células que saturan el color

# Color base de la grilla (inactiva)
_GRID_DIM = (30, 32, 40, 90)

# Crear líneas horizontales: una por cada fila × columna (segmento entre nodos)
# Cada segmento va de (col*CW, row*CH) a ((col+1)*CW, row*CH)
_hlines = []   # shape: (GRID_ROWS+1, GRID_COLS)
for _row in range(GRID_ROWS + 1):
    row_segs = []
    for _col in range(GRID_COLS):
        x0 = _col       * CELL_W
        x1 = (_col + 1) * CELL_W
        y  = _row       * CELL_H
        seg = shapes.Line(
            x0, y, x1, y,
            color=_GRID_DIM,
            batch=heatmap_batch, group=group_heatmap
        )
        row_segs.append(seg)
    _hlines.append(row_segs)

# Crear líneas verticales: una por cada col × fila (segmento entre nodos)
_vlines = []   # shape: (GRID_COLS+1, GRID_ROWS)
for _col in range(GRID_COLS + 1):
    col_segs = []
    for _row in range(GRID_ROWS):
        x  = _col       * CELL_W
        y0 = _row       * CELL_H
        y1 = (_row + 1) * CELL_H
        seg = shapes.Line(
            x, y0, x, y1,
            color=_GRID_DIM,
            batch=heatmap_batch, group=group_heatmap
        )
        col_segs.append(seg)
    _vlines.append(col_segs)


def _node_color(nx, ny):
    """
    Devuelve el color RGBA de un nodo de la grilla en (nx, ny)
    según la densidad de cada tipo de célula en su vecindad.
    """
    pt = np.array([nx, ny], dtype=np.float32)

    h = len(_kd_healthy.query_ball_point(pt, GRID_RADIUS)) if _kd_healthy else 0
    c = len(_kd_cancer .query_ball_point(pt, GRID_RADIUS)) if _kd_cancer  else 0
    w = len(_kd_white  .query_ball_point(pt, GRID_RADIUS)) if _kd_white   else 0

    total = h + c + w
    if total == 0:
        return _GRID_DIM

    # Normalizar scores
    hn = min(h / GRID_SAT, 1.0)
    cn = min(c / GRID_SAT, 1.0)
    wn = min(w / GRID_SAT, 1.0)
    tn = hn + cn + wn

    r = int((cn / tn) * 220)
    g = int((hn / tn) * 200)
    b = int((wn / tn) * 220)

    # Brillo mínimo para que se vea algo
    r = max(r, 18)
    g = max(g, 20)
    b = max(b, 25)

    alpha = int(min(220, 60 + tn * 100))
    return (r, g, b, alpha)


def update_grid():
    """
    Colorea cada segmento interpolando entre los colores de sus dos nodos.
    Se llama solo cuando show_heatmap es True.
    """
    # Cache de colores por nodo para no recalcular
    node_color = {}

    def get_nc(col, row):
        k = (col, row)
        if k not in node_color:
            node_color[k] = _node_color(col * CELL_W, row * CELL_H)
        return node_color[k]

    # Segmentos horizontales: nodo (col, row) → (col+1, row)
    for row in range(GRID_ROWS + 1):
        for col in range(GRID_COLS):
            c0 = get_nc(col,     row)
            c1 = get_nc(col + 1, row)
            # Color del segmento = promedio de sus dos nodos
            avg = tuple(
                (c0[i] + c1[i]) // 2 for i in range(4)
            )
            _hlines[row][col].color = avg

    # Segmentos verticales: nodo (col, row) → (col, row+1)
    for col in range(GRID_COLS + 1):
        for row in range(GRID_ROWS):
            c0 = get_nc(col, row)
            c1 = get_nc(col, row + 1)
            avg = tuple(
                (c0[i] + c1[i]) // 2 for i in range(4)
            )
            _vlines[col][row].color = avg

# UPDATE

def update(dt):
    # 1. Construir KD-trees UNA sola vez para todo el frame
    rebuild_kdtrees(cells, white_cells)

    # 2. Calcular fuerzas (leen los árboles, no los modifican)
    for cell in cells:
        cell.flock(cells)

    # 3. Integrar posiciones
    for cell in cells:
        cell.update(dt)

    for wc in white_cells:
        wc.update(dt)

    # 4. Lógica de juego
    infection_step()
    white_cell_attack()

    # 5. Grilla (solo si está activa; reutiliza KD-trees del frame)
    if show_heatmap:
        update_grid()

    update_ui()


pyglet.clock.schedule_interval(update, 1 / 60)

# UI

def update_ui():
    healthy_n = sum(1 for c in cells if not c.cancer)
    cancer_n  = sum(1 for c in cells if c.cancer)
    white_n   = len(white_cells)
    mode_text = cursor_mode if cursor_mode else "NONE"

    hud_label.text = (
        f"[ CANCER BOIDS ]\n\n"
        f"Healthy : {healthy_n}\n"
        f"Cancer  : {cancer_n}\n"
        f"Leucos  : {white_n}\n\n"
        f"Mode    : {mode_text}\n\n"
        f"H : healthy\n"
        f"C : cancer\n"
        f"W : white\n"
        f"G : grid {'[ON] ' if show_heatmap else '[OFF]'}\n"
        f"ESC : cancel"
    )

# DRAW

@window.event
def on_draw():
    window.clear()
    if show_heatmap:
        heatmap_batch.draw()
    batch.draw()
    ui_batch.draw()
    fps_display.draw()


# MOUSE

@window.event
def on_mouse_press(x, y, button, modifiers):
    global cursor_mode, cells, white_cells

    if button != pyglet.window.mouse.LEFT:
        return

    if cursor_mode is None:
        return

    velocity = np.random.uniform(-60, 60, 2)

    if cursor_mode == "healthy":
        cells.append(Cell((x, y), velocity, cancer=False))

    elif cursor_mode == "cancer":
        cells.append(Cell((x, y), velocity, cancer=True))

    elif cursor_mode == "white":
        white_cells.append(WhiteCell((x, y), velocity))


# KEYBOARD

@window.event
def on_activate():
    pass  # garantiza foco de teclado en Windows

@window.event
def on_key_press(symbol, modifiers):
    global cursor_mode, show_heatmap

    if symbol == key.ESCAPE:
        cursor_mode = None

    elif symbol == key.H:
        cursor_mode = None if cursor_mode == "healthy" else "healthy"

    elif symbol == key.C:
        cursor_mode = None if cursor_mode == "cancer" else "cancer"

    elif symbol == key.W:
        cursor_mode = None if cursor_mode == "white" else "white"

    elif symbol == key.G:
        show_heatmap = not show_heatmap
        if not show_heatmap:
            for row_segs in _hlines:
                for seg in row_segs:
                    seg.color = _GRID_DIM
            for col_segs in _vlines:
                for seg in col_segs:
                    seg.color = _GRID_DIM

    update_ui()


# RUN

update_ui()
pyglet.app.run()
