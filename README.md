<div align="center">

<h1>Proyectos</h1>

<h3>Simulaciones gráficas, bases de datos y proyectos escalables</h3>

![Python](https://img.shields.io/badge/Python-3.10%2B-3776AB?style=for-the-badge&logo=python&logoColor=white)
![Pyglet](https://img.shields.io/badge/Pyglet-2.1%2B-74B900?style=for-the-badge)
![ModernGL](https://img.shields.io/badge/ModernGL-5.10%2B-2D2D2D?style=for-the-badge)
![OpenGL](https://img.shields.io/badge/OpenGL-3.3%2B-5586A4?style=for-the-badge&logo=opengl&logoColor=white)
![Scala](https://img.shields.io/badge/Scala-3.8.2-DC322F?style=for-the-badge&logo=scala&logoColor=white)
![SQL](https://img.shields.io/badge/SQL-Database-4479A1?style=for-the-badge&logo=postgresql&logoColor=white)

<p>
  Proyectos e ideas para explorar temas de biología, computación gráfica, teoría de grafos, bases de datos y programación orientada a objetos.
</p>

</div>

---

## Contenido

- [Descripción general](#descripción-general)
- [Cells'War](#cellswar)
- [Redes de impulsos nerviosos](#redes-de-impulsos-nerviosos)
- [Biodiversidad de Chile](#bases-de-datos-biodiversidad-de-chile)
- [Sistema de turnos en Scala](#poo-sistema-de-turnos-en-scala)
- [Alcance de los modelos](#alcance-de-los-modelos)


## Descripción general

| Proyecto | Objetivo principal | Tecnologías y conceptos principales |
| --- | --- | --- |
| **Cells'War** | Representar la interacción entre células normales, células blancas y células cancerígenas. | NumPy, SciPy, Pyglet, ModernGL y KD-Tree |
| **Redes de impulsos nerviosos** | Explorar la propagación de impulsos en una red de neuronas representada como un grafo en 3D. | Python, Pyglet, Dijkstra y Prim |
| **Biodiversidad de Chile** | Modelar información sobre especies, regiones y clasificaciones de conservación. | Modelo entidad-relación, modelo relacional y FNBC |
| **Scala y POO** | Modelar entidades y programar turnos de un videojuego. | Scala 3, POO y MUnit |

> [!NOTE]
> Las simulaciones son modelos visuales simplificados. Su propósito es explorar
> técnicas de simulación y computación gráfica, no reproducir con exactitud un
> sistema biológico o físico real.

---

## Cells'War

![Simulación de células](assets/cellswar2d.JPG)
![Simulación de células](assets/cellswar3d.JPG)

**Cells'War** es una simulación semi-realista de las interacciones entre tres
poblaciones celulares:

- células normales;
- células blancas;
- células cancerígenas.

El proyecto incluye dos variantes, **Cells2D** y **Cells3D**. Ambas comparten el
mismo planteamiento general, pero emplean herramientas y enfoques de
representación diferentes.

### Características principales

- Visualización de la distribución espacial mediante un **mapa de calor**.
- Uso de un **KD-Tree** para acelerar las consultas de proximidad.
- Modificación de las poblaciones celulares en tiempo real.
- Posibilidad de probar diferentes cantidades iniciales de cada tipo de célula.
- Propagación del cáncer mediante un crecimiento acumulativo: una célula
  infectada muta y puede infectar nuevos objetivos.

### Consideraciones técnicas

1. El espacio de simulación es intencionalmente acotado para favorecer las
   consultas espaciales y el comportamiento del KD-Tree.
2. El modelo de infección no incorpora regeneración ni decaimiento de la
   población cancerígena.
3. Cada población celular se representa mediante una versión generalizada de
   su comportamiento.
4. El modelo prioriza la experimentación visual y computacional por sobre la
   precisión biomédica.

### Requisitos

```text
numpy>=1.26
scipy>=1.12
pyglet>=2.1
moderngl>=5.10
pillow>=10.0
```

---

## Redes de impulsos nerviosos

![Simulación de redes](assets/redcompleja.JPG)

La idea es utilizar las propiedades de los grafos para representar una red de neuronas y observar cómo se propaga un impulso entre ellas. Los vértices representan las neuronas y las aristas sus conexiones. La vista 3D permite recorrer la red y comparar la propagación con una ruta mínima o un árbol de costo mínimo.

### Características principales

- Parte con **120 neuronas** distribuidas en el espacio, con conexiones cercanas y algunos enlaces largos.
- Cada neurona pasa por reposo, disparo y un período refractario que limita las activaciones seguidas.
- Para evitar loops de propagación, cada onda puede activar una sola vez a cada neurona, aunque encuentre otro camino para regresar.
- Utiliza **Dijkstra** para calcular la ruta con menor suma de distancias entre dos neuronas. El impulso de esa ruta no se bifurca.
- Utiliza **Prim** para construir el árbol que conecta la distribución inicial y para calcular el árbol de costo mínimo de la red final. Al mostrarlo, las demás conexiones siguen transmitiendo pulsos.
- Separa la lógica del grafo de la visualización, lo que permite comprobar el modelo sin abrir una ventana.

### Controles

| Control | Acción |
| :---: | --- |
| Clic izquierdo sobre una neurona | Inicia un pulso si está en reposo. |
| Arrastrar con el botón izquierdo | Gira la cámara alrededor de la red. |
| Rueda del mouse | Acerca o aleja la cámara. |
| `Espacio` | Intenta iniciar una onda desde una neurona aleatoria. |
| `P` | Calcula una ruta mínima entre dos neuronas aleatorias e intenta enviar un impulso por ella. |
| `M` | Muestra u oculta el árbol de costo mínimo. |
| `R` | Genera otra red y borra la actividad anterior. |
| `C` | Restablece la orientación y centra la cámara. |
| `Esc` | Cierra la simulación. |

### Requisitos

- Python; esta versión se probó con **Python 3.11.4** en Windows.
- **Pyglet 2.1**, la única dependencia externa de esta simulación.
- Soporte gráfico para **OpenGL 3.3 o superior**.

El archivo `requirements.txt` del comprimido especifica `pyglet>=2.1,<3`.

### Cómo ejecutarlo

La versión actualizada está en [Redes_complejas.zip](Redes_complejas.zip). Desde la carpeta del repositorio, ejecuta lo siguiente en PowerShell:

```powershell
Expand-Archive -LiteralPath .\Redes_complejas.zip -DestinationPath .\Redes_complejas -Force
Set-Location .\Redes_complejas\3D_neuronas
py -m pip install -r requirements.txt
py .\simulacion_red_neuronal_3d.py
```

Dentro de `3D_neuronas` se incluyen la explicación del modelo, los parámetros para experimentar y las pruebas del núcleo. Para ejecutar esas pruebas desde la misma carpeta:

```powershell
py -m unittest discover -s tests -v
```

---

## Bases de datos: Biodiversidad de Chile

**Proyecto académico grupal**  

Diseño de una base de datos para organizar información sobre especies presentes en Chile y facilitar la consulta de su taxonomía, distribución regional y estado de conservación.

**Aspectos del modelo:**

- Jerarquía taxonómica desde reino hasta especie.
- Distribución de especies por región.
- Nombres comunes y sinónimos de cada especie.
- Clasificaciones de conservación actuales e históricas.
- Modelo entidad-relación, esquema relacional y análisis de FNBC.
- [Página del proyecto](https://grupo36.cc3201.dcc.uchile.cl/).

**Modelo entidad-relación**

![Modelo entidad-relación del proyecto de biodiversidad](assets/modelo_entidad_relacion.jpg)



---

## POO: Sistema de turnos en Scala

**Proyecto académico · Scala 3 · MUnit**  
**Etapa actual:** modelo del juego y programador de turnos.

Desarrollo de las estructuras de un videojuego por turnos: personajes, enemigos, armas, pociones, paneles de mapa y acciones.

**Aspectos implementados:**

- Jerarquías de clases para unidades, utilizables y acciones.
- Paneles con coordenadas, unidades y referencias a paneles adyacentes.
- Barras de acción basadas en el peso de cada unidad y su arma equipada.
- `TurnScheduler` que detecta las unidades listas y asigna el siguiente turno según el excedente de barra.
- Pruebas unitarias con MUnit para las entidades y el programador de turnos.

> **Siguiente etapa:** conectar estas mecánicas en `GameController` para ejecutar las acciones y el flujo del combate.

## Alcance de los modelos

Estas simulaciones permiten estudiar y visualizar:

- interacciones locales entre agentes;
- estructuras de búsqueda espacial;
- crecimiento y propagación de poblaciones;
- uso de grafos y sus propiedades;
- aplicaciones de técnicas gráficas relacionadas con la biología.

Los resultados deben interpretarse como aproximaciones computacionales con
fines educativos y experimentales.

---

<div align="center">

Desarrollado como una exploración de **simulación**, **física** y
**computación gráfica**.


</div>
