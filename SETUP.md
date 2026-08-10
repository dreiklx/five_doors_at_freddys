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

## 6. Alternativa sin Eclipse ni JDK 8 instalado: distribución portable de Swing

Si tu compañero no quiere instalar Eclipse ni un JDK 8 con JavaFX, se puede
generar una **carpeta portable auto-contenida** de Five Doors at Freddy's
-- runtime Java + JavaFX embebido, jar del juego y un lanzador `.bat`, todo
en una sola carpeta que se copia y ejecuta sin instalar nada en la máquina
destino (investigado y probado 2026-08-09/10):

```bash
cd five_doors_at_freddys/FiveDoorsAtFreddys
build-portable.bat
```

Esto solo requiere JDK 8 con JavaFX **en la máquina donde se genera la
carpeta** (la misma que ya pide §2) -- detecta el JDK automáticamente
(`JAVA8_HOME` o las rutas típicas de Windows) o se le puede pasar como
argumento: `build-portable.bat "C:\ruta\a\tu\jdk1.8"`.

Genera `FiveDoorsAtFreddys/dist/FiveDoorsAtFreddys/` (~575 MB, incluye el
runtime completo). **Copia esa carpeta entera** a la máquina destino --
verificado con una corrida real, `FiveDoorsAtFreddys.bat` abre el juego sin
ningún Java instalado en esa máquina.

**Importante para que Noche 5 → Escape siga funcionando:** `LanzadorEscape`
resuelve `five_doors_escape` de forma relativa (`../../five_doors_escape`,
ver §0 de `CLAUDE.md`) -- la carpeta portable debe quedar en la MISMA
posición relativa que tendría el checkout normal:

```
<una-carpeta-cualquiera>/
├── five_doors_at_freddys/
│   └── FiveDoorsAtFreddys/     <- contenido de dist/FiveDoorsAtFreddys/ copiado aqui
└── five_doors_escape/          <- carpeta hermana (clon normal, con JDK 21 instalado)
```

**Por qué no es un `.exe` nativo de un solo archivo:** se investigó
`jpackage --type app-image` (empaqueta un runtime completo en un `.exe`
real) y el build en sí funciona, pero el `.exe` resultante falla en Windows
con un error clásico de registro (`Software\JavaSoft\Java Runtime
Environment...`) -- causa real: `jpackage` está pensado para runtimes
modulares (JDK 9+, típicamente generados con `jlink`), no para un JDK 8
completo copiado tal cual; es una combinación que la herramienta acepta
pero no soporta de verdad. El MISMO runtime empaquetado, invocado
directamente (`runtime\bin\javaw.exe -jar FiveDoorsAtFreddys.jar`, que es
justo lo que hace `FiveDoorsAtFreddys.bat`), sí funciona -- verificado con
una corrida real, ventana del juego abierta y cerrada limpiamente. Si en el
futuro se quiere un `.exe` nativo de verdad, el camino real sería generar
primero un runtime modular con `jlink` (requiere modularizar el proyecto,
fuera de alcance de este cambio) en vez de apuntar `jpackage` a un JDK 8
crudo.

## 7. Distribución sin Gradle/sin internet para Five Doors Escape (investigado, no implementado)

`Five Doors Escape` ya tiene el plugin `application` de Gradle activado en
`lwjgl3/build.gradle` (`mainClass` ya configurado), así que **ya genera una
distribución autocontenida sin cambios adicionales**:

```bash
cd five_doors_escape
gradlew.bat lwjgl3:installDist
```

Verificado con una corrida real: produce
`lwjgl3/build/install/lwjgl3/` (~205 MB, todos los `.jar` + librerías
nativas de LWJGL + los assets del juego ya empaquetados dentro del jar +
un lanzador `bin\lwjgl3.bat`) -- una vez generada, esa carpeta se puede
copiar y ejecutar sin Gradle ni internet. **Sigue necesitando un JDK 21
instalado** en la máquina destino (el `.bat` generado llama a `java` vía
`JAVA_HOME`/PATH, no trae runtime embebido).

**No se integró todavía con `LanzadorEscape`** (que hoy sigue invocando
`gradlew.bat lwjgl3:run`, ya extensamente probado esta sesión) -- cambiar
el mecanismo real de lanzamiento de la transición Noche 5 → Escape es un
cambio de mayor riesgo que se prefirió no forzar en la misma ronda que ya
tocó otras partes del flujo de fin de noche. Si en el futuro se necesita
que Escape funcione sin internet en absoluto (hoy el primer `gradlew.bat
lwjgl3:run` real sí necesita internet para descargar Gradle y las
dependencias, una sola vez), el camino más directo es: generar
`installDist`, empaquetarlo con un runtime JDK 21 embebido (mismo patrón
que `build-portable.bat` para el lado Swing, con `jlink` esta vez sí
disponible porque LibGDX/Java 21 es totalmente modular-compatible), y
cambiar `LanzadorEscape.lanzar()` para invocar ese lanzador en vez de
`gradlew.bat`.
