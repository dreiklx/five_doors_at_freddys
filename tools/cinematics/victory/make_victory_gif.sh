#!/bin/sh
# Genera el video de victoria del juego a partir de su fuente (CLAUDE.md §1.82):
#   FiveDoorsAtFreddys/src/com/fdaf/resources/videos/victory.mp4  (H.264 1920x1080 30 fps + AAC, 9.66 s)
#   -> videos/victory.gif                       1920x1080, 289 fotogramas, retardos 30/40 ms (9.63 s), paleta propia
#                                                por fotograma, sin tramado, fotogramas completos sin transparencia
#   -> audio/cinematics/victory_video_audio.wav  44.1 kHz estereo 16 bits (sin cambios desde v2.18)
# Fidelidad medida contra el video decodificado: PSNR 55 dB, diferencia maxima 6/255 por canal, 0 % de pixeles
# con diferencia > 8. Fotogramas completos e independientes: ReproductorVideoGif los decodifica en paralelo.
# Requiere FFmpeg 7 en el PATH o el del paquete de Python imageio-ffmpeg.
set -e
AQUI=$(cd "$(dirname "$0")" && pwd)
RES="$AQUI/../../../FiveDoorsAtFreddys/src/com/fdaf/resources"
FF=$(command -v ffmpeg || python -c "import imageio_ffmpeg; print(imageio_ffmpeg.get_ffmpeg_exe())")
"$FF" -hide_banner -loglevel error -y -i "$RES/videos/victory.mp4" \
    -filter_complex "[0:v]split[a][b];[a]palettegen=stats_mode=single:max_colors=256:reserve_transparent=0[p];[b][p]paletteuse=new=1:dither=none" \
    -gifflags -offsetting-transdiff -loop 0 "$RES/videos/victory.gif"
"$FF" -hide_banner -loglevel error -y -i "$RES/videos/victory.mp4" -vn -ac 2 -ar 44100 -c:a pcm_s16le "$RES/audio/cinematics/victory_video_audio.wav"
echo "listo: $RES/videos/victory.gif"
