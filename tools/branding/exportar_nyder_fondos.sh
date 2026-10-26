#!/bin/sh
# Exporta la presentacion de NYDER GAMES sobre fondos lisos (CLAUDE.md §1.76): la MISMA animacion del juego (se dibuja
# con PnlWarning, igual que exportar_nyder_alfa.sh) con la composicion de pantalla completa del juego (1920x1080, logo
# al 55 % del ancho, ControllerMenu), compuesta sobre negro, blanco y azul marino, en MP4 H.264 + AAC con el destello
# sonoro sincronizado. Incluye los 250 ms de fondo previos que tiene el juego y 250 ms al final, para que cada video
# empiece y termine en el fondo liso. Ademas, una cuarta variante sobre blanco con las letras en gris muy oscuro
# (#16161C): en el logo original son blancas y sobre blanco no se ven; la estrella y el brillo no cambian.
# Uso (desde la raiz del repo five_doors_at_freddys, en Git Bash):
#   sh tools/branding/exportar_nyder_fondos.sh <carpeta_salida> [ancho=1920] [alto=1080] [fps=30]
# Requisitos: los de exportar_nyder_alfa.sh (JDK 8, Python, ffmpeg con libx264).
set -e
SALIDA=${1:?carpeta de salida}
ANCHO=${2:-1920}; ALTO=${3:-1080}; FPS=${4:-30}
FRACCION=0.55
PREVIO_MS=250; FINAL_MS=250
RAIZ=$(cd "$(dirname "$0")/../.." && pwd)
SRC=$RAIZ/FiveDoorsAtFreddys/src
JDK8=${JDK8:-/c/Program Files/Java/jdk1.8.0_271}
if [ -z "$FFMPEG" ]; then
  FFMPEG=$(command -v ffmpeg || python -c "import imageio_ffmpeg; print(imageio_ffmpeg.get_ffmpeg_exe())")
fi
TMP=$(mktemp -d)
mkdir -p "$SALIDA" "$TMP/png"
"$JDK8/bin/javac" -encoding UTF-8 -nowarn -d "$TMP" -sourcepath "$SRC" "$SRC/com/fdaf/mvc/views/frames/pnl/PnlWarning.java"
cp -r "$SRC/com/fdaf/resources/." "$TMP/"
"$JDK8/bin/javac" -encoding UTF-8 -nowarn -cp "$(cygpath -w "$TMP")" -d "$TMP" "$RAIZ/tools/branding/ExportarNyderAlfa.java"
"$JDK8/bin/java" -Djava.awt.headless=true -cp "$(cygpath -w "$TMP")" ExportarNyderAlfa "$(cygpath -w "$TMP/png")" "$ANCHO" "$ALTO" "$FPS" "$FRACCION" completa
mkdir -p "$TMP/png_oscuras"
"$JDK8/bin/java" -Djava.awt.headless=true -cp "$(cygpath -w "$TMP")" ExportarNyderAlfa "$(cygpath -w "$TMP/png_oscuras")" "$ANCHO" "$ALTO" "$FPS" "$FRACCION" completa letras=16161C
SON="$SRC/com/fdaf/resources/audio/sfx/nyder_star_sparkle.wav"
SEQ="$TMP/png/nyder_games_%04d.png"
N=$(ls "$TMP/png" | wc -l)
DUR=$(python -c "print(round(($PREVIO_MS + $FINAL_MS)/1000 + ($N - 1)/$FPS, 4))")
# destello sonoro: 960 ms desde el inicio del fundido de entrada (el primer cuadro de la secuencia, t = 0)
RETARDO=$((PREVIO_MS + 960))
cp "$SON" "$SALIDA/nyder_star_sparkle.wav"
for VARIANTE in "black:0x000000:png" "white:0xFFFFFF:png" "navy:0x0B1A33:png" "white_dark_letters:0xFFFFFF:png_oscuras"; do
  NOMBRE=${VARIANTE%%:*}; RESTO=${VARIANTE#*:}; COLOR=${RESTO%%:*}; CARPETA=${RESTO#*:}
  SEQ="$TMP/$CARPETA/nyder_games_%04d.png"
  "$FFMPEG" -hide_banner -loglevel error -y \
    -f lavfi -i "color=c=$COLOR:s=${ANCHO}x${ALTO}:r=$FPS:d=$DUR" \
    -framerate "$FPS" -i "$SEQ" -i "$SON" \
    -filter_complex "[1:v]format=rgba,setpts=PTS+$PREVIO_MS/1000/TB[l];[0:v][l]overlay=eof_action=pass:format=auto,format=yuv420p[v];[2:a]adelay=$RETARDO|$RETARDO,apad,atrim=0:$DUR[a]" \
    -map "[v]" -map "[a]" -t "$DUR" \
    -c:v libx264 -preset slow -crf 14 -profile:v high -pix_fmt yuv420p -movflags +faststart \
    -c:a aac -b:a 192k "$SALIDA/nyder_games_$NOMBRE.mp4"
done
rm -rf "$TMP"
echo "exportado en $SALIDA ($N cuadros de animacion, $DUR s por video)"
