#*Cells'war:*          
##
Basicamente cellswar es una simulacion(semi-realista) sobre las interacciones entre celulas normales, celulas blancas y celulas cancerigenas.Aqui se encuentran 2 ejemplos distintos con enfoque distintos, Cells2D y Cells3D, que se plantean de la misma manera pero con enfoques y herramientas distintas.

Requerimientos:

numpy>=1.26

scipy>=1.12

pyglet>=2.1

moderngl>=5.10

pillow>=10.0

Detalles importantes:

I) La grilla permite una visualizacion estilo warm-map, que permite caracterizar mejor la presencia de las poblaciones en el espacio.

II) El espacio en general es reducido con respecto a las dimensiones, pues resulta relevante para el KD-Tree.

III) En general el comportamiento del cancer respecto a la infeccion que provocan es de tipo exponencial, pues al infectar un objetivo este muta y se permite infectar a otras sin capacidad de regeneracion. De forma que es un crecimiento periodico sin decaimiento. 

IV) Ademas, se pueden modificar las poblaciones en tiempo real, tal que se pueden probar distintos volumenes de cada tipo.

V) El modelo solo considera versiones ambiguas de cada tipo de celula.




###Malla escamas de dragon:

Requerimientos:

Python 3.10 o superior.

NumPy.

Pyglet.

ModernGL.

Controladores gráficos compatibles con OpenGL 3.3 o superior.

Detalles importantes:

I) Es un modelo para probar principalmente algunos detalles sobre el paradigma lagrangiano y modelos de iluminacion.

II) Permite probar distintos efectos como la resistencia al viento y algunos elementos viscosos.

III)La superficie contiene 52 × 46 vértices.

IV)La tecla R solo reinicia el sistema angular, no reinicia el tiempo, el viento, la cámara ni la animación de la superficie.



