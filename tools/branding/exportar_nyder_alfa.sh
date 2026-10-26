#!/bin/sh
# Exporta la presentacion de NYDER GAMES con transparencia real (CLAUDE.md §1.69):
#   1) compila PnlWarning de FDAF y ExportarNyderAlfa con JDK 8 y renderiza la secuencia PNG RGBA (maestra);
#   2) la codifica a WebM VP9 con alfa (yuva420p) y a MOV ProRes 4444 con alfa (yuva444p10le), con el destello
#      sonoro (audio/sfx/nyder_star_sparkle.wav) sincronizado como en el juego.
# Uso (desde la raiz del repo five_doors_at_freddys, en Git Bash):
#   sh tools/branding/exportar_nyder_alfa.sh <carpeta_salida> [ancho] [alto] [fps]
# Requisitos: JDK 8 (JDK8, por defecto C:\Program Files\Java\jdk1.8.0_271), Python y ffmpeg con libvpx-vp9 y
# prores_ks (FFMPEG; si no se indica, ffmpeg del PATH o el del paquete de Python imageio-ffmpeg).
set -e
SALIDA=${1:?carpeta de salida}
ANCHO=${2:-1920}; ALTO=${3:-800}; FPS=${4:-30}
RAIZ=$(cd "$(dirname "$0")/../.." && pwd)
SRC=$RAIZ/FiveDoorsAtFreddys/src
JDK8=${JDK8:-/c/Program Files/Java/jdk1.8.0_271}
if [ -z "$FFMPEG" ]; then
  FFMPEG=$(command -v ffmpeg || python -c "import imageio_ffmpeg; print(imageio_ffmpeg.get_ffmpeg_exe())")
fi
TMP=$(mktemp -d)
rm -rf "$SALIDA/png_sequence"
mkdir -p "$SALIDA/png_sequence"
# solo PnlWarning y sus dependencias, no el juego entero
"$JDK8/bin/javac" -encoding UTF-8 -nowarn -d "$TMP" -sourcepath "$SRC" "$SRC/com/fdaf/mvc/views/frames/pnl/PnlWarning.java"
cp -r "$SRC/com/fdaf/resources/." "$TMP/"
"$JDK8/bin/javac" -encoding UTF-8 -nowarn -cp "$(cygpath -w "$TMP")" -d "$TMP" "$RAIZ/tools/branding/ExportarNyderAlfa.java"
"$JDK8/bin/java" -Djava.awt.headless=true -cp "$(cygpath -w "$TMP")" ExportarNyderAlfa "$(cygpath -w "$SALIDA/png_sequence")" "$ANCHO" "$ALTO" "$FPS"
SON="$SRC/com/fdaf/resources/audio/sfx/nyder_star_sparkle.wav"
SEQ="$SALIDA/png_sequence/nyder_games_%04d.png"
N=$(ls "$SALIDA/png_sequence" | wc -l)
DUR=$(python -c "print(round($N/$FPS, 4))")
# en el juego el destello sonoro arranca 160 ms despues de terminar el fundido de entrada (800 ms), es decir a los
# 0.96 s; la secuencia empieza en el segundo cuadro del fundido (t = 1/fps)
RETARDO=$(python -c "print(int(round(960 - 1000/$FPS)))")
AUDIO="[1:a]adelay=$RETARDO|$RETARDO,apad,atrim=0:$DUR[a]"
# WebM VP9 con alfa: -auto-alt-ref 0 es necesario para conservar el canal alfa con libvpx
"$FFMPEG" -hide_banner -loglevel error -y -framerate "$FPS" -i "$SEQ" -i "$SON" \
  -filter_complex "$AUDIO" -map 0:v -map "[a]" -t "$DUR" \
  -c:v libvpx-vp9 -pix_fmt yuva420p -b:v 0 -crf 18 -auto-alt-ref 0 -row-mt 1 -metadata:s:v:0 alpha_mode=1 \
  -c:a libopus -b:a 128k "$SALIDA/nyder_games_alpha.webm"
# MOV ProRes 4444 con alfa (para editores de video): alfa de 16 bits, audio PCM
"$FFMPEG" -hide_banner -loglevel error -y -framerate "$FPS" -i "$SEQ" -i "$SON" \
  -filter_complex "$AUDIO" -map 0:v -map "[a]" -t "$DUR" \
  -c:v prores_ks -profile:v 4444 -qscale:v 9 -pix_fmt yuva444p10le -alpha_bits 16 -vendor apl0 \
  -c:a pcm_s16le "$SALIDA/nyder_games_alpha_prores4444.mov"
rm -rf "$TMP"
echo "exportado en $SALIDA ($N cuadros, $DUR s)"
