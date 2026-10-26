#!/bin/sh
# Regresion automatica de Five Doors at Freddy's (CLAUDE.md §1.71). Compila el juego (FiveDoorsAtFreddys/src) y los
# arneses de tools/tests/src con un JDK 8 con JavaFX, y los ejecuta sin ventana visible del jugador real:
#   - perfil aislado: user.home apunta a tools/tests/out/home (nunca al progreso guardado de verdad);
#   - capturas y registros en tools/tests/out/.
# Uso (Git Bash en Windows, o sh en Linux/macOS), desde cualquier carpeta:
#   JDK8_HOME="/ruta/al/jdk1.8" sh tools/tests/run_tests.sh [rapida]
# "rapida" ejecuta solo el espanol. Sin JDK8_HOME se usa el java/javac del PATH (debe ser Java 8 con JavaFX).
# BUILD=<carpeta con clases y recursos ya compilados> prueba ese arbol en lugar de compilar FiveDoorsAtFreddys/src.
AQUI=$(cd "$(dirname "$0")" && pwd)
REPO=$(cd "$AQUI/../.." && pwd)
SRC="$REPO/FiveDoorsAtFreddys/src"
OUT="$AQUI/out"
if [ -n "$JDK8_HOME" ]; then JAVAC="$JDK8_HOME/bin/javac"; JAVA="$JDK8_HOME/bin/java"; else JAVAC=javac; JAVA=java; fi
case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*) SEP=';'; w() { cygpath -w "$1"; }; export MSYS_NO_PATHCONV=1 ;;
    *) SEP=':'; w() { printf '%s' "$1"; } ;;
esac

# lista de fuentes .java de una carpeta para un @archivo de javac: entre comillas (por los espacios) y con barras
# normales, que el javac de Windows acepta y no necesitan escaparse
lista() { find "$1" -name '*.java' | while read -r f; do printf '"%s"\n' "$(w "$f" | tr '\\' '/')"; done; }

rm -rf "$OUT/bin"; mkdir -p "$OUT/bin" "$OUT/reg/logro" "$OUT/reg/nyder" "$OUT/reg/rend" "$OUT/cap"
# bibliotecas locales opcionales (si existen en src/libs, igual que en el proyecto de Eclipse)
LIBS=""
for j in "$SRC"/libs/*.jar; do [ -f "$j" ] && LIBS="$LIBS$SEP$(w "$j")"; done
if [ -z "$BUILD" ]; then
    BUILD="$OUT/build"
    rm -rf "$BUILD"; mkdir -p "$BUILD"
    echo "compilando el juego..."
    lista "$SRC" > "$OUT/fuentes.txt"
    "$JAVAC" -encoding UTF-8 -nowarn -cp "$(w "$BUILD")$LIBS" -d "$(w "$BUILD")" @"$(w "$OUT/fuentes.txt")" 2>&1 | grep -v "^Note:"
    cp -r "$SRC/com/fdaf/resources/." "$BUILD/"
fi
lista "$AQUI/src" > "$OUT/arneses.txt"
"$JAVAC" -encoding UTF-8 -nowarn -cp "$(w "$BUILD")" -d "$(w "$OUT/bin")" @"$(w "$OUT/arneses.txt")" 2>&1 | grep -v "^Note:"

CP="$(w "$BUILD")$SEP$(w "$OUT/bin")$LIBS"
AVISOS=0
run() {
    n=$1; shift
    rm -rf "$OUT/home"; mkdir -p "$OUT/home"
    echo "===== $n $*"
    "$JAVA" -Duser.home="$(w "$OUT/home")" -cp "$CP" "$@" > "$OUT/$n.log" 2>&1
    salida=$?
    grep -v "^\[\(GLITCH\|CARGA\|DEBUG\|LOGRO\|INTRO\|PERSISTENCIA\)" "$OUT/$n.log" | tail -${TAILN:-15}
    # una prueba que se corta a medias no escribe "FALLO": avisar si termino mal o sin su resumen (CLAUDE.md §1.73)
    if [ "$salida" -ne 0 ]; then echo "AVISO: $n termino con codigo $salida"; AVISOS=$((AVISOS + 1)); fi
    case "$1" in
        PruebaLogro|PruebaLineasIntro|PruebaDestelloNyder|OverlayPuertas|PruebaHover|PruebaHoverVuelta|VerificarRecursos)
            grep -q "FALLOS:\|RESULTADO:" "$OUT/$n.log" || { echo "AVISO: $n no llego a escribir su resumen (prueba incompleta)"; AVISOS=$((AVISOS + 1)); } ;;
    esac
}
REG=$(w "$OUT/reg")
run VerificarRecursos VerificarRecursos "$(w "$SRC")" "$(w "$SRC/com/fdaf/resources")"
TAILN=25 run OverlayPuertas OverlayPuertas "$REG/ov"
IDIOMAS="ESPANOL INGLES"; [ "$1" = "rapida" ] && IDIOMAS="ESPANOL"
for L in $IDIOMAS; do
    run PruebaHover_$L PruebaHover $L
    run PruebaHoverVuelta_$L PruebaHoverVuelta $L
    run FlujoIdioma_$L FlujoIdioma $L
    run MedirIdioma_$L MedirIdioma $L true
    TAILN=12 run PruebaIntros_$L PruebaIntros "$REG" $L
    for N in 1 3 5; do TAILN=4 run RenderHud_${L}_$N RenderHud "$REG/hud_${L}_$N" $L $N; done
    TAILN=4 run PruebaLogro_$L PruebaLogro "$(w "$OUT/reg/logro")" $L
    TAILN=8 run PruebaLineasIntro_$L PruebaLineasIntro $L
    TAILN=8 run PruebaDestelloNyder_$L PruebaDestelloNyder "$(w "$OUT/reg/nyder")" $L
done
TAILN=6 run PruebaDestelloNyder_saltar PruebaDestelloNyder "$(w "$OUT/reg/nyder")" ESPANOL saltar
run PaginasInfo PaginasInfo "$REG/pag"
TAILN=25 run PruebaIntroLlamada PruebaIntroLlamada "$REG" INGLES 5
for M in toque mantener natural; do TAILN=8 run PruebaRendirse2_$M PruebaRendirse2 "$(w "$OUT/reg/rend")" $M ESPANOL; done
TAILN=3 run FondoMenuBordes FondoMenuBordes "$(w "$OUT/cap")"
echo "AVISOS: $AVISOS"
echo FIN_REGRESION
