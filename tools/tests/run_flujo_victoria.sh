#!/bin/sh
# Prueba de extremo a extremo del fin de noche sobre una DISTRIBUCION construida (CLAUDE.md §1.81):
# ganar noches 1-5 y ESCAPE desde Noche personalizada, con el jar y los runtimes de la entrega.
#   - perfil aislado: JAVA_TOOL_OPTIONS=-Duser.home=<tools/tests/out/flujo/home_X> (lo heredan el juego relanzado y Escape);
#   - el proceso que el juego relanza al terminar lleva la marca -Dprueba.reinicio y se cierra al final.
# Uso (Git Bash en Windows):  sh tools/tests/run_flujo_victoria.sh "<carpeta de la distribucion>" [modos...]
# Modos por defecto: 1 2 3 4 5 custom. Ejemplo: sh tools/tests/run_flujo_victoria.sh "$HOME/Desktop/FiveDoors_TestBuild" 5
# Abre Five Doors Escape en pantalla completa unos segundos en los modos 5 y custom.
export MSYS_NO_PATHCONV=1
AQUI=$(cd "$(dirname "$0")" && pwd)
DIST="$1"; shift
[ -f "$DIST/FiveDoorsAtFreddys/FiveDoorsAtFreddys.jar" ] || { echo "uso: $0 <carpeta con FiveDoorsAtFreddys/ y FiveDoorsEscape/> [modos]"; exit 1; }
MODOS="${*:-1 2 3 4 5 custom}"
OUT="$AQUI/out/flujo"; rm -rf "$OUT"; mkdir -p "$OUT/bin" "$OUT/cap"
w() { cygpath -w "$1"; }
FD="$DIST/FiveDoorsAtFreddys"
"$FD/runtime/bin/javac.exe" -encoding UTF-8 -nowarn -cp "$(w "$FD/FiveDoorsAtFreddys.jar")" -d "$(w "$OUT/bin")" "$(w "$AQUI/src/PruebaFlujoVictoria.java")" || exit 1
TOTAL=0
IDIOMA=ESPANOL
for M in $MODOS; do
    MARCA="flujo$$$M"; mkdir -p "$OUT/home_$M"
    echo "===== PruebaFlujoVictoria $M $IDIOMA"
    ( cd "$FD" && JAVA_TOOL_OPTIONS="-Duser.home=$(w "$OUT/home_$M")" timeout 240 ./runtime/bin/java.exe "-Dprueba.reinicio=$MARCA" \
        -cp "FiveDoorsAtFreddys.jar;$(w "$OUT/bin")" PruebaFlujoVictoria "$M" "$IDIOMA" "$(w "$OUT/cap")" > "$OUT/$M.log" 2>&1 )
    sleep 4
    grep -E "^OK|^FALLO|video:|cerro este proceso" "$OUT/$M.log"
    grep -q "FALLOS: 0" "$OUT/$M.log" || { echo "AVISO: $M no termino con FALLOS: 0"; TOTAL=$((TOTAL + 1)); }
    # el juego se relanzo a si mismo (camino real): esa instancia lleva la marca y se cierra aqui
    powershell -NoProfile -Command "Get-CimInstance Win32_Process | Where-Object { \$_.Name -like 'java*' -and \$_.CommandLine -like '*$MARCA*' } | ForEach-Object { 'relanzado por el juego: pid ' + \$_.ProcessId + ' (cerrado)'; Stop-Process -Id \$_.ProcessId -Force }"
    [ "$IDIOMA" = ESPANOL ] && IDIOMA=INGLES || IDIOMA=ESPANOL
done
echo "MODOS CON FALLOS: $TOTAL"
