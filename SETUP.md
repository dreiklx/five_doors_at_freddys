# Cómo obtener y ejecutar los dos juegos

Este proyecto (Five Doors at Freddy's, Swing) y su expansión **Five Doors
Escape** (LibGDX, `dreiklx/five_doors_escape`) son **dos repositorios Git
completamente independientes** -- distinto lenguaje, distinto build system,
distinta versión de Java. Es una decisión de arquitectura deliberada (ver
`FiveDoorsAtFreddys/CLAUDE.md` §0), no un descuido: no se fusionaron en un
monorepo ni se convirtió ninguno al sistema de build del otro.

Se comunican en tiempo de ejecución únicamente a través de un archivo de
traspaso (`~/.fivedoorsatfreddys/handoff.json`) y el ciclo de vida de un
proceso: al ganar la Noche 5, Swing lanza el proceso de Escape y espera a
que termine para volver al menú. Para que ese lanzamiento funcione **sin
copiar archivos ni ajustar rutas a mano**, los dos repositorios deben
clonarse como **carpetas hermanas**, con esos nombres exactos, bajo el
mismo directorio padre:

```
<una-carpeta-cualquiera>/
├── five_doors_at_freddys/      <- este repositorio
│   └── FiveDoorsAtFreddys/     <- proyecto Eclipse (Swing)
└── five_doors_escape/          <- el otro repositorio
```

Si tu compañero ya tiene una carpeta `git`/`repos` donde clona todo, basta
con clonar ambos ahí dentro con sus nombres por defecto -- no hace falta
ninguna carpeta contenedora nueva.

## 1. Clonar ambos repositorios

```bash
cd <una-carpeta-cualquiera>
git clone https://github.com/dreiklx/five_doors_at_freddys.git
git clone https://github.com/dreiklx/five_doors_escape.git
```

## 2. Prerrequisitos

- **JDK 8, con JavaFX incluido** (para el juego Swing). El video de victoria
  (`PnlWin.reproducirVideo`) usa `JFXPanel`, y varias distribuciones
  modernas de OpenJDK 8 (p. ej. Eclipse Temurin 8) **no traen JavaFX
  empaquetado**. Usa un JDK 8 que sí lo incluya (Oracle JDK 8, Azul Zulu
  8 "FX", Liberica JDK 8 Full) -- la máquina de desarrollo original usa
  `1.8.0_271`.
- **JDK 21** (para Five Doors Escape/LibGDX) -- cualquier distribución
  sirve, no necesita JavaFX. `LanzadorEscape.java` (lado Swing) lo busca
  automáticamente en las ubicaciones de instalación habituales de Windows
  (`C:\Program Files\Java`, `\Eclipse Adoptium`, `\Microsoft\jdk-21`,
  `\Zulu`, `\BellSoft`) o en la variable de entorno `JAVA_HOME` si ya
  apunta a un JDK 21 -- no hace falta configurar nada a mano si el JDK 21
  quedó instalado en una ruta estándar.
- **Eclipse** (recomendado para el proyecto Swing -- es un proyecto
  clásico sin Gradle/Maven, pensado para importarse como
  "Existing Projects into Workspace").

## 3. Ejecutar Five Doors at Freddy's (Swing)

1. Importa `five_doors_at_freddys/FiveDoorsAtFreddys` en Eclipse.
2. Ejecuta `com.fdaf.init.Main`.
3. Juega hasta ganar la Noche 5 -- el juego escribe el traspaso y lanza
   automáticamente Five Doors Escape (`gradlew.bat lwjgl3:run`, primera vez
   más lento porque Gradle descarga dependencias).

## 4. Ejecutar Five Doors Escape (LibGDX) de forma independiente

No hace falta pasar por Swing para probarlo solo:

```bash
cd five_doors_escape
gradlew.bat lwjgl3:run
```

Si no se corrió nunca desde Swing, `~/.fivedoorsatfreddys/handoff.json`
no existe todavía -- esto NO es un error: `HandoffReader` usa valores de
desarrollo por defecto y el juego arranca igual (comportamiento
documentado en `Architecture.md` §7).

## 5. Si algo falla al lanzar Escape desde Swing

`LanzadorEscape` imprime en consola (Eclipse: pestaña Console) el motivo
exacto -- por ejemplo, si no encontró `five_doors_escape` como carpeta
hermana, o si no encontró un JDK 21 instalado. Revisa ese mensaje antes de
asumir un bug nuevo.
