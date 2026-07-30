# Five Doors at Freddy's

Juego de terror y supervivencia en Java/Swing inspirado en *Five Nights at Freddy's*, desarrollado como proyecto final para el curso IF3001. El jugador debe sobrevivir varias noches vigilando cámaras, gestionando batería y decidiendo qué puertas abrir mientras evita a los animatronics.

> Proyecto de equipo (4 personas). Este repositorio refleja el trabajo conjunto del grupo; la contribución individual de cada integrante puede verse en el historial de commits.

## Características

- **Sistema de noches** con dificultad progresiva y una noche personalizada configurable.
- **Generación procedural de escenarios**: cada partida arma un árbol binario de puertas (`GeneradorArbol`/`Arbol`) que combina animatronics, coleccionables, baterías y puertas vacías, evitando partidas repetitivas.
- **Sistema de cámaras** para vigilar la ubicación de los animatronics (Freddy, Bonnie, Chica, Foxy) en tiempo real.
- **Inventario de coleccionables** con más de 5 objetos recuperables durante la partida.
- **Reproductor de audio propio** (`Sonido`, `ConfiguracionAudio`, `SonidosAmbientalesAleatorios`) con más de 40 efectos de sonido, sonido ambiental aleatorio y control de volumen.
- **Selector de idioma** (español / inglés) y pantalla de configuración de opciones.
- **Persistencia local** de partida y preferencias del jugador (`PersistenciaJuego`, `PreferenciasJuego`).
- Pantallas completas de victoria, game over y advertencias.

## Tecnologías

- **Java 8** (Swing / AWT para toda la interfaz gráfica, sin frameworks externos)
- **Eclipse** como entorno de desarrollo (proyecto sin gestor de dependencias tipo Maven/Gradle)
- `javax.sound.sampled` para la reproducción de audio

## Arquitectura

El proyecto sigue una separación **MVC** dentro del paquete `com.fdaf`:

```
com.fdaf
├── init/                    → punto de entrada (Main)
├── mvc/
│   ├── models/
│   │   ├── juego/           → Juego, Arbol, Nodo, GeneradorArbol, Noche
│   │   ├── puerta/          → Puerta, TipoPuerta
│   │   ├── coleccionables/  → Coleccionable, InventarioColeccionables, ColaColeccionables
│   │   └── animatronicos/   → Animatronico
│   ├── controllers/         → ControllerJuego, ControllerMenu, ControllerCamara, ControllerInterfaz
│   └── views/
│       ├── frames/          → VistaPrincipal + paneles (PnlJuego, PnlMenu, PnlOpciones, PnlTableta, ...)
│       └── multimedia/      → Sonido
├── util/                    → carga de imágenes/gifs, escalado de vista, persistencia, preferencias
└── resources/                → sprites, gifs, sonidos e íconos
```

La lógica de cada noche se modela como un **árbol binario** construido dinámicamente: cada nodo representa una puerta con un resultado (animatronic, coleccionable, batería o nada), lo que permite generar partidas distintas sin hardcodear rutas fijas.

## Instalación y ejecución

Requiere JDK 8 o superior.

1. Clona el repositorio.
2. Importa la carpeta `FiveDoorsAtFreddys` como proyecto existente en Eclipse (`File → Import → Existing Projects into Workspace`).
3. Ejecuta la clase `com.fdaf.init.Main`.

También puede compilarse y ejecutarse por línea de comandos desde `FiveDoorsAtFreddys/src`:

```bash
javac -d ../bin $(find com -name "*.java")
java -cp ../bin com.fdaf.init.Main
```

## Estado del proyecto

Jugable de inicio a fin (menú, noches, cámaras, inventario, game over/victoria). Como todo proyecto académico, prioriza que la mecánica funcione sobre la optimización del tamaño de los assets; los recursos multimedia (gifs, sonidos) se distribuyen junto al código fuente en lugar de un CDN o carpeta de builds separada.

## Créditos

Desarrollado por Elmer, Dani, Derek y Ramsey para el curso IF3001.
