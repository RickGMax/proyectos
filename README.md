<div align="center">

<h1>Cells'War y Malla de Escamas de Dragón</h1>

<h3>Simulaciones gráficas de sistemas celulares y superficies deformables</h3>

![Python](https://img.shields.io/badge/Python-3.10%2B-3776AB?style=for-the-badge&logo=python&logoColor=white)
![Pyglet](https://img.shields.io/badge/Pyglet-2.1%2B-74B900?style=for-the-badge)
![ModernGL](https://img.shields.io/badge/ModernGL-5.10%2B-2D2D2D?style=for-the-badge)
![OpenGL](https://img.shields.io/badge/OpenGL-3.3%2B-5586A4?style=for-the-badge&logo=opengl&logoColor=white)

<p>
  Tres propuestas de simulación desarrolladas en Python: un modelo de
  interacción celular,una superficie dinámica inspirada en escamas de dragón y una red de impulsos nerviosos.
</p>

</div>

---

## Contenido

- [Descripción general](#descripción-general)
- [Cells'War](#cellswar)
- [Malla de escamas de dragón](#malla-de-escamas-de-dragón)
- [Alcance de los modelos](#alcance-de-los-modelos)
- [Redes de impulsos nerviosos](#redes-de-impulsos-nerviosos)

## Descripción general

| Simulación | Objetivo principal | Tecnologías destacadas |
| --- | --- | --- |
| **Cells'War** | Representar la interacción entre células normales, células blancas y células cancerígenas. | NumPy, SciPy, Pyglet, ModernGL y KD-Tree |
| **Malla de escamas de dragón** | Explorar superficies deformables, dinámica lagrangiana e iluminación en tiempo real. | NumPy, Pyglet, ModernGL y OpenGL 3.3+ |
| **Redes de impulsos nerviosos** | Representar la interacción del tejido nervioso y sus principales mecanismos. | NumPy,Pyglet|

> [!NOTE]
> Los proyectos son modelos visuales simplificados. Su propósito es explorar
> técnicas de simulación y computación gráfica, no reproducir con exactitud un
> sistema biológico o físico real.

---

## Cells'War

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

## Malla de escamas de dragón

Esta simulación representa una superficie deformable inspirada en una malla de
escamas. Su objetivo principal es experimentar con dinámica lagrangiana,
modelos de iluminación y fuerzas externas.

### Características principales

- Superficie compuesta por **52 × 46 vértices**.
- Simulación de resistencia al viento.
- Inclusión de efectos viscosos.
- Animación de la superficie en tiempo real.
- Cámara y sistema angular controlables durante la ejecución.
- Renderizado mediante **ModernGL** y **OpenGL 3.3 o superior**.
- Utiliza el modelo de iluminacion de Phong
- Tiene distintas velocidades para probar el efecto de cada fluido

### Controles

| Tecla | Acción |
| :---: | --- |
| `R` | Reinicia únicamente el sistema angular. |
| `Click izquierdo` | Genera una onda en el espacio clikeado. |
| `P` | Pausa/Reanuda la simulacion. |
| `1` | Efecto de viento. |
| `2` | Efecto de agua. |
| `3` | Efecto de aceite. |
| `Flecha arriba y abajo` | arriba aumenta el efecto de la corriente, abajo lo disminuye. |

> [!IMPORTANT]
> La tecla `R` no reinicia el tiempo de simulación, el viento, la cámara ni la
> animación de la superficie.

### Requisitos

- Python 3.10 o superior.
- NumPy.
- Pyglet.
- ModernGL.
- Controladores gráficos compatibles con OpenGL 3.3 o superior.


---
## Redes de impulsos nerviosos

### Características principales

-Utiliza propiedades de grafos, vertices y aristas para modelar una red de neuronas.

-Los vertices representan las neuronas y las aristas las conexiones(que vendrian siendo las conexiones entre dendritas), trae cierta componente grafica para mejorar la visualizacion de los organismos 

-Para evitar loops de propagacion existe un parametro de refraccion que funciona como ventana de tiempo. 

-Utiliza algoritmos como dikjstra para alcanzar la distancia mas corta entre 2 neuronas.

-Utiliza Kruskall para encontrar el arbol de costo minimo.

### Controles

| Tecla | Acción |
| :---: | --- |
| `R` | Reinicia la red. |
| `Click izquierdo` | Inicia un pulso desde una neurona. |
| `P` | Genera un pulso desde 2 neuronas aleatorias. |
| `M` | Arbol de costo minimo. |
| `Espacio` | Genera un pulso en algun punto aleatorio. |

### Requisitos

Pyglet


---


## Alcance de los modelos

Estas simulaciones permiten estudiar y visualizar:

- interacciones locales entre agentes;
- estructuras de búsqueda espacial;
- crecimiento y propagación de poblaciones;
- deformación de superficies;
- respuesta ante fuerzas externas;
- iluminación y renderizado en tiempo real.
- Estudio del uso de los grafos y sus propiedades
- Aplicaciones de tecnicas graficas para el desarrollo de aplicaciones relacionadads con la biologia

Los resultados deben interpretarse como aproximaciones computacionales con
fines educativos y experimentales.

---

<div align="center">

Desarrollado como una exploración de **simulación**, **física** y
**computación gráfica**.

</div>






