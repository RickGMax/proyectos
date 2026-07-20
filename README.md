#*Cells'war:*          
##
Basicamente cellswar es una simulacion(semi-realista) sobre las interacciones entre celulas normales, celulas blancas y celulas cancerigenas. 

fallas evidentes: 

I) no considera los distintos tipos de celulas blancas 

II) no distingue entre colonias de cancer, y es un tanto ambiguo en el comportamiento, dado que no representa ninguno en particular 

III) La implementacion por Boids limita el volumen de la muestra


Aciertos relevantes:

I)cumple correctamente con la infeccion exponencial, matematicamente es fiel al modelo tipico 

II) Resulta ser bastante bueno con KD-trees, optimizando bastante la simulacion 

III) Los boids dan una aproximacion realista y biologicamente correcta sobre los comportamientos de las celulas

