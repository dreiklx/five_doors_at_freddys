# Cinemática de Rendirse — fuentes del render

Genera los 160 fotogramas de `src/com/fdaf/resources/videos/surrender/` y la banda sonora
`src/com/fdaf/resources/audio/cinematics/surrender_mix.wav`. Detalle de la escena en `FiveDoorsAtFreddys/CLAUDE.md` §1.59.

Requisitos: Blender 5.1 (Cycles; se usó GPU), Python 3 con numpy, y el repo `five_doors_escape` en
`C:\Users\dfarl\git\five_doors_escape` (los modelos se leen de `assets/models/`).

```sh
B="C:/Program Files/Blender Foundation/Blender 5.1/blender.exe"
"$B" -b --factory-startup -P build_surrender_scene.py -- surrender_base.blend
"$B" -b surrender_base.blend -P direct_surrender.py -- "<salida>/surrender_####.jpg" 1-160 32 1800
python mix_surrender_audio.py "<repo>/FiveDoorsAtFreddys/src/com/fdaf/resources/audio" surrender_mix.wav
```

`direct_surrender.py` acepta variables de entorno de depuración: `DEBUGCAM=1|B|C|F` (cámara normal apuntando a
un personaje), `EJES=...` (eje de los *Damped Track* de las cabezas) y `ESC=<W>` (potencia de la luz del escenario).
Las marcas de tiempo del audio y de `CinematicaRendirse` dependen de 20 fps y del inicio de los fotogramas a 2.2 s:
si se cambia uno, hay que cambiar los tres.

La escena ya construida (`surrender_base.blend`, 190 MB) está en `FDAF assets/source_files/cinematics/surrender/`.
