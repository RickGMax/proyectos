# Sistema de turnos en Scala

Proyecto académico para desarrollar la lógica de un videojuego por turnos usando programación orientada a objetos. La idea es modelar las entidades del juego y conectar sus acciones con un controlador de combate.

## Etapa actual

El modelo y el controlador de combate ya están integrados. Se pueden consultar las acciones de una unidad, seleccionar su turno, ejecutar una acción y restaurar el estado inicial del combate.

- Personajes y enemigos con vida, defensa, peso y posición.
- Armas compatibles con cada clase de personaje e inventarios con armas y pociones.
- Ataques físicos, equipamiento, consumo de pociones y movimiento entre paneles adyacentes.
- Magia blanca y negra, con validación y consumo de maná.
- Barras de acción y selección de turnos según el excedente de barra.
- Acciones disponibles construidas a partir del estado y el inventario, con Visitor para procesar los utilizables.
- Control de la unidad activa y eliminación de las unidades derrotadas del planificador.
- Reinicio del combate con restauración del estado capturado al construir el controlador.

La interfaz y el flujo completo de la partida quedan por desarrollar. `Player` permite consultar si todas sus unidades están derrotadas, pero el controlador todavía no usa ese dato para resolver la victoria.

## Organización

```text
ProyectoScala/
├── build.sbt
├── project/
│   ├── build.properties
│   └── plugins.sbt
├── src/
│   ├── main/scala/
│   │   ├── controller/
│   │   └── model/
│   └── test/scala/
└── README.md
```

- `controller/GameController.scala` coordina el combate y la ejecución de acciones.
- `model/` contiene unidades, acciones, armas, pociones, inventarios, jugadores, paneles y el planificador de turnos.
- `src/test/scala/` incluye pruebas del comportamiento del modelo y de la configuración.

## Requisitos y pruebas

La configuración del proyecto usa **Scala 3.8.2**, **sbt 1.12.5** y **MUnit 1.3.5**. Se necesita un JDK y sbt instalados; la comprobación de esta versión se realiza con JDK 21.

Desde esta carpeta:

```powershell
sbt test
```

La primera ejecución puede descargar las dependencias del proyecto. sbt utiliza la versión de Scala declarada en `build.sbt`.

Las pruebas cubren las entidades y sus validaciones, las acciones, el movimiento, las relaciones entre jugadores, la selección de turnos y el reinicio del combate. También comprueban que estén presentes los archivos de configuración necesarios.

## Uso del controlador

Al crear `GameController` se registran las unidades y se captura su estado inicial. Para comenzar, se llama a `nextTurn()`: el constructor no activa una unidad automáticamente.

`executeAction(source, actionName, target)` exige que el origen sea la unidad activa y que la acción esté disponible. Si se ejecuta correctamente, el controlador pasa al siguiente turno. `resetGame()` restaura las unidades al estado capturado al construir el controlador y deja el combate sin un turno activo.

El proyecto contiene la lógica y sus pruebas. Todavía no se incluye un programa principal ni una interfaz para jugar.
