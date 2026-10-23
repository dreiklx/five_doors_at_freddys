# Catálogo de recursos — Five Doors at Freddy's y Five Doors Escape

Fuente de verdad de **dónde está cada recurso** en los dos repositorios y en la carpeta de trabajo `C:\Users\dfarl\Desktop\FDAF assets`. Mantenerlo al día al añadir, renombrar o retirar un recurso (regla 10 de `FiveDoorsAtFreddys/CLAUDE.md`). Una copia está en `FDAF assets/CATALOGO_RECURSOS.md`.

## Convención

- Archivos y carpetas de recursos en **inglés y `snake_case`**: minúsculas, `_` como separador, sin espacios ni acentos.
- Forma: `<sujeto>_<descripcion>[_<estado>][_<idioma>].<ext>` — p. ej. `door_left_opening.gif`, `light_button_right_on.png`, `night_03_intro_es.gif`.
- Idioma siempre al final: `_en` / `_es`. Secuencias con 2 dígitos: `night_01`, `knock_02`. Estados con palabras: `_open/_closed`, `_on/_off`, `_locked`.
- Carpetas por **función** (audio, fondos, interfaz, coleccionables...), nunca por formato.
- El código Java (clases, paquetes, variables) conserva sus nombres en español; la convención solo aplica a los archivos de recursos.

## Correspondencia repositorio ↔ FDAF assets

| Repositorio (raíz de recursos) | Carpeta en `FDAF assets` |
|---|---|
| `five_doors_at_freddys/FiveDoorsAtFreddys/src/com/fdaf/resources/<ruta>` | `fdaf/<misma ruta>` |
| `five_doors_escape/assets/<ruta>` (salvo `models/`, `content/`, `i18n/`) | `escape/<misma ruta>` |
| `five_doors_escape/assets/models/*.glb` | `source_files/3d_models/...` (ver tabla de Escape; no se duplican) |

En `FDAF assets`, junto a un archivo del juego puede haber `<nombre>_original.<ext>`: el original del que se preparó la versión del juego (recorte, perspectiva, normalización o una versión anterior más pesada). Nunca sobrescribir el original con la versión preparada.

Otras carpetas de `FDAF assets`:

| Carpeta | Contenido |
|---|---|
| `source_files/3d_models/` | Antigua `3D Models/`, movida entera **sin tocar su interior**: los `.blend` de `Blender_WIP/` enlazan sus texturas con rutas relativas (`//..\Freddy\...`). Incluye los modelos de Sketchfab, los FBX de Mixamo, `LibGDX_Ready/` (los `.glb` exportados) y sus copias `.bak_*`. |
| `source_files/sound_library/` | Efectos de biblioteca sin uso directo en los juegos (antes en `FDAF Sounds/` y en la raíz), con el nombre normalizado a `snake_case` a partir del original. |
| `_reference/fnaf_assets_enterest/` | Pack de terceros "FNAF ASSETS REORGANIZED BY ENTEREST" (650 archivos), sin tocar: solo referencia. |
| `_archive/fdaf_repo_unused/` | Los 12 recursos retirados del repo FDAF en v2.8 por no tener ningún uso (con su ruta antigua). |
| `source_files/cinematics/` | Escenas de Blender y recursos externos descargados para las cinemáticas (desde 2026-10-23), una subcarpeta por escena; cada recurso externo con su procedencia y licencia en `LICENSES.md` de esa carpeta. |
| `_archive/old_versions/` | Versiones anteriores: iconos v1-v4, transiciones de tableta v1, copia de seguridad de las intros (zip). |
| `_manifiesto_reorganizacion_2026-10-22.tsv` | Cada movimiento/copia de la reorganización (origen → destino), para poder deshacerla. |

## Five Doors at Freddy's — recursos en uso

Ruta relativa a `src/com/fdaf/resources/` y a `FDAF assets/fdaf/`. «Derivado» = preparado a partir de otro archivo; si el original sigue existiendo, está al lado con sufijo `_original`.

| Recurso | Nombre anterior (hasta v2.7) | Tipo | Idioma | Uso en el juego | Origen |
|---|---|---|---|---|---|
| `backgrounds/office_fan.gif` | `gifs/fondos/Abanico.gif` | fondo | - | Ventilador animado del escritorio de la oficina | original |
| `ui/tablet/tablet_close.gif` | `gifs/fondos/CerrarTablet.gif` | ui | - | Transicion al cerrar la tableta | original |
| `backgrounds/menu_background.gif` | `gifs/fondos/Menu.gif` | fondo | - | Fondo animado del menu, selector, Como jugar y Acerca | original |
| `backgrounds/office_light_right.gif` | `gifs/fondos/OficinaLuzDer.gif` | fondo | - | Oficina con la luz derecha encendida | original |
| `backgrounds/office_light_left.gif` | `gifs/fondos/OficinaLuzIzq.gif` | fondo | - | Oficina con la luz izquierda encendida | original |
| `backgrounds/static.gif` | `gifs/fondos/Static.gif` | fondo | - | Estatica (Game Over y transicion a Escape) | original |
| `ui/tablet/tablet_open.gif` | `gifs/fondos/abrirTablet.gif` | ui | - | Transicion al abrir la tableta | original |
| `ui/tablet/camera_static.gif` | `gifs/fondos/camara.gif` | ui | - | Fondo de la tableta (estatica limpia, sin texto) | original |
| `intros/intro_fallback.gif` | `gifs/fondos/empezar.gif` | intro | - | Intro de respaldo si falta la de una noche | original |
| `backgrounds/game_over_sequence.gif` | `gifs/fondos/gameover.gif` | fondo | - | Secuencia larga de Game Over (oficina sin luz) | original |
| `intros/night_01_intro_en.gif` | `gifs/fondos/introduccion/introNoche1_en.gif` | intro | en | Intro de la Noche 1 | original |
| `intros/night_01_intro_es.gif` | `gifs/fondos/introduccion/introNoche1_es.gif` | intro | es | Intro de la Noche 1 | original |
| `intros/night_02_intro_en.gif` | `gifs/fondos/introduccion/introNoche2_en.gif` | intro | en | Intro de la Noche 2 | original |
| `intros/night_02_intro_es.gif` | `gifs/fondos/introduccion/introNoche2_es.gif` | intro | es | Intro de la Noche 2 | original |
| `intros/night_03_intro_en.gif` | `gifs/fondos/introduccion/introNoche3_en.gif` | intro | en | Intro de la Noche 3 | original |
| `intros/night_03_intro_es.gif` | `gifs/fondos/introduccion/introNoche3_es.gif` | intro | es | Intro de la Noche 3 | original |
| `intros/night_04_intro_en.gif` | `gifs/fondos/introduccion/introNoche4_en.gif` | intro | en | Intro de la Noche 4 | original |
| `intros/night_04_intro_es.gif` | `gifs/fondos/introduccion/introNoche4_es.gif` | intro | es | Intro de la Noche 4 | original |
| `intros/night_05_intro_en.gif` | `gifs/fondos/introduccion/introNoche5_en.gif` | intro | en | Intro de la Noche 5 | original |
| `intros/night_05_intro_es.gif` | `gifs/fondos/introduccion/introNoche5_es.gif` | intro | es | Intro de la Noche 5 | original |
| `ui/tablet/camera_rec_dot.gif` | `gifs/fondos/puntorojo.gif` | ui | - | Punto rojo de grabacion de la tableta | original |
| `jumpscares/bonnie_jumpscare.gif` | `gifs/jumpscares/JumpscareBonnie.gif` | jumpscare | - | Jumpscare de Bonnie (nombre construido por codigo) | original |
| `jumpscares/chica_jumpscare.gif` | `gifs/jumpscares/JumpscareChica.gif` | jumpscare | - | Jumpscare de Chica (nombre construido por codigo) | original |
| `jumpscares/foxy_jumpscare.gif` | `gifs/jumpscares/JumpscareFoxy.gif` | jumpscare | - | Jumpscare de Foxy (nombre construido por codigo) | original |
| `jumpscares/freddy_jumpscare.gif` | `gifs/jumpscares/JumpscareFreddy.gif` | jumpscare | - | Jumpscare de Freddy (nombre construido por codigo) | original |
| `jumpscares/freddy_power_out_jumpscare.gif` | `gifs/jumpscares/JumpscareFreddySinLuz.gif` | jumpscare | - | Jumpscare final del Game Over (sin luz) | original |
| `collectibles/locked/cupcake_locked.png` | `images/coleccionables/bloqueados/cupcake_bloq.png` | coleccionable | - | Silueta del cupcake en el inventario | versión ligera del compañero; original: `cupcake_locked_original.png` |
| `collectibles/locked/foxy_hook_locked.png` | `images/coleccionables/bloqueados/garfio_foxy_bloq.png` | coleccionable | - | Silueta del garfio de Foxy | versión ligera del compañero; original: `foxy_hook_locked_original.png` |
| `collectibles/locked/bonnie_guitar_locked.png` | `images/coleccionables/bloqueados/guitarra_bonnie_bloq.png` | coleccionable | - | Silueta de la guitarra de Bonnie | versión ligera del compañero; original: `bonnie_guitar_locked_original.png` |
| `collectibles/locked/balloon_boy_toy_locked.png` | `images/coleccionables/bloqueados/juguete_balloonboy_bloq.png` | coleccionable | - | Silueta del juguete de Balloon Boy | original |
| `collectibles/locked/freddy_microphone_locked.png` | `images/coleccionables/bloqueados/microfono_freddy_bloq.png` | coleccionable | - | Silueta del microfono de Freddy | versión ligera del compañero; original: `freddy_microphone_locked_original.png` |
| `collectibles/locked/bonnie_plush_locked.png` | `images/coleccionables/bloqueados/peluche_bonnie_bloq.png` | coleccionable | - | Silueta del peluche de Bonnie | original |
| `collectibles/locked/chica_plush_locked.png` | `images/coleccionables/bloqueados/peluche_chica_bloq.png` | coleccionable | - | Silueta del peluche de Chica | original |
| `collectibles/locked/foxy_plush_locked.png` | `images/coleccionables/bloqueados/peluche_foxy_bloq.png` | coleccionable | - | Silueta del peluche de Foxy | original |
| `collectibles/locked/freddy_plush_locked.png` | `images/coleccionables/bloqueados/peluche_freddy_bloq.png` | coleccionable | - | Silueta del peluche de Freddy | original |
| `collectibles/locked/golden_freddy_plush_locked.png` | `images/coleccionables/bloqueados/peluche_golden_freddy_bloq.png` | coleccionable | - | Silueta del peluche de Golden Freddy | original |
| `collectibles/cupcake.png` | `images/coleccionables/cupcake.png` | coleccionable | - | Coleccionable: cupcake | versión ligera del compañero; original: `cupcake_original.png` |
| `collectibles/foxy_hook.png` | `images/coleccionables/garfio_foxy.png` | coleccionable | - | Coleccionable: garfio de Foxy | versión ligera del compañero; original: `foxy_hook_original.png` |
| `collectibles/bonnie_guitar.png` | `images/coleccionables/guitarra_bonnie.png` | coleccionable | - | Coleccionable: guitarra de Bonnie | versión ligera del compañero; original: `bonnie_guitar_original.png` |
| `collectibles/balloon_boy_toy.png` | `images/coleccionables/juguete_balloonboy.png` | coleccionable | - | Coleccionable: juguete de Balloon Boy | original |
| `collectibles/freddy_microphone.png` | `images/coleccionables/microfono_freddy.png` | coleccionable | - | Coleccionable: microfono de Freddy | versión ligera del compañero; original: `freddy_microphone_original.png` |
| `collectibles/bonnie_plush.png` | `images/coleccionables/peluche_bonnie.png` | coleccionable | - | Coleccionable: peluche de Bonnie | original |
| `collectibles/chica_plush.png` | `images/coleccionables/peluche_chica.png` | coleccionable | - | Coleccionable: peluche de Chica | original |
| `collectibles/foxy_plush.png` | `images/coleccionables/peluche_foxy.png` | coleccionable | - | Coleccionable: peluche de Foxy | original |
| `collectibles/freddy_plush.png` | `images/coleccionables/peluche_freddy.png` | coleccionable | - | Coleccionable: peluche de Freddy | original |
| `collectibles/golden_freddy_plush.png` | `images/coleccionables/peluche_golden_freddy.png` | coleccionable | - | Coleccionable: peluche de Golden Freddy | original |
| `backgrounds/office.png` | `images/fondos/FondoJuego.png` | fondo | - | Oficina (luces apagadas) | original |
| `backgrounds/game_over.png` | `images/fondos/GAME_OVER_.png` | fondo | - | Fondo de la pantalla final de Game Over | original |
| `branding/nyder_games_splash.png` | `images/fondos/NYDER-GAMES.png` | branding | - | Presentacion NYDER GAMES al iniciar | original |
| `ui/screens/warning_es.png` | `images/fondos/advertencia_fdaf.png` | ui | es | Advertencia inicial | original |
| `backgrounds/black_screen.png` | `images/fondos/fondo_negro.png` | fondo | - | Fondo negro (pantalla de carga de Escape) | original |
| `ui/tablet/inventory_background.png` | `images/fondos/inventario.png` | ui | - | Fondo del inventario de la tableta | original |
| `backgrounds/office_edge_left.png` | `images/fondos/left.png` | fondo | - | Borde oscuro izquierdo de la oficina | original |
| `backgrounds/office_edge_right.png` | `images/fondos/right.png` | fondo | - | Borde oscuro derecho de la oficina | original |
| `ui/screens/warning_en.png` | `images/fondos/warning_fdaf.png` | ui | en | Advertencia inicial | original |
| `ui/hud/battery_0.png` | `images/hud/bateria0.png` | hud | - | Bateria vacia | original |
| `ui/hud/battery_1.png` | `images/hud/bateria1.png` | hud | - | Bateria con 1 carga | original |
| `ui/hud/battery_2.png` | `images/hud/bateria2.png` | hud | - | Bateria con 2 cargas | original |
| `ui/hud/battery_3.png` | `images/hud/bateria3.png` | hud | - | Bateria con 3 cargas | original |
| `ui/hud/battery_4.png` | `images/hud/bateria4.png` | hud | - | Bateria con 4 cargas | original |
| `branding/game_icon.png` | `images/hud/fdaf_icon.png` | branding | - | Icono de la ventana | original |
| `ui/tablet/camera_disabled_es.png` | `images/hud/idioma/camara_desactivada_es.png` | ui | es | Rotulo de la camara de la tableta (recorte) | derivado (recorte); original: `camera_disabled_es_original.png` |
| `ui/tablet/camera_disabled_en.png` | `images/hud/idioma/camera_disabled_en.png` | ui | en | Rotulo de la camara de la tableta (recorte) | derivado (recorte); original: `camera_disabled_en_original.png` |
| `ui/hud/power_left_es.png` | `images/hud/idioma/energia_restante_es.png` | hud | es | Etiqueta de energia (recorte) | derivado (recorte); original: `power_left_es_original.png` |
| `ui/office/mute_call_es.png` | `images/hud/idioma/mutecall_es.png` | ui | es | Boton COLGAR (generado desde el ingles) | derivado (generado desde la versión en inglés) |
| `ui/hud/night_01_en.png` | `images/hud/idioma/night1_en.png` | hud | en | Etiqueta de noche en la tableta (recorte) | derivado (recorte); original: `night_01_en_original.png` |
| `ui/hud/night_02_en.png` | `images/hud/idioma/night2_en.png` | hud | en | Etiqueta de noche en la tableta (recorte) | derivado (recorte); original: `night_02_en_original.png` |
| `ui/hud/night_03_en.png` | `images/hud/idioma/night3_en.png` | hud | en | Etiqueta de noche en la tableta (recorte) | derivado (recorte); original: `night_03_en_original.png` |
| `ui/hud/night_04_en.png` | `images/hud/idioma/night4_en.png` | hud | en | Etiqueta de noche en la tableta (recorte) | derivado (recorte); original: `night_04_en_original.png` |
| `ui/hud/night_05_en.png` | `images/hud/idioma/night5_en.png` | hud | en | Etiqueta de noche en la tableta (recorte) | derivado (recorte); original: `night_05_en_original.png` |
| `ui/hud/night_01_es.png` | `images/hud/idioma/noche1_es.png` | hud | es | Etiqueta de noche en la tableta (recorte) | derivado (recorte); original: `night_01_es_original.png` |
| `ui/hud/night_02_es.png` | `images/hud/idioma/noche2_es.png` | hud | es | Etiqueta de noche en la tableta (recorte) | derivado (recorte); original: `night_02_es_original.png` |
| `ui/hud/night_03_es.png` | `images/hud/idioma/noche3_es.png` | hud | es | Etiqueta de noche en la tableta (recorte) | derivado (recorte); original: `night_03_es_original.png` |
| `ui/hud/night_04_es.png` | `images/hud/idioma/noche4_es.png` | hud | es | Etiqueta de noche en la tableta (recorte) | derivado (recorte); original: `night_04_es_original.png` |
| `ui/hud/night_05_es.png` | `images/hud/idioma/noche5_es.png` | hud | es | Etiqueta de noche en la tableta (recorte) | derivado (recorte); original: `night_05_es_original.png` |
| `ui/office/door_panel_right_es.png` | `images/hud/idioma/pnlBotonesDer_es.png` | ui | es | Panel PUERTA/LUZ derecho (generado desde el ingles) | derivado (generado desde la versión en inglés) |
| `ui/office/door_panel_left_es.png` | `images/hud/idioma/pnlBotonesIzq_es.png` | ui | es | Panel PUERTA/LUZ izquierdo (generado desde el ingles) | derivado (generado desde la versión en inglés) |
| `ui/hud/power_left_en.png` | `images/hud/idioma/power_left_en.png` | hud | en | Etiqueta de energia (recorte) | derivado (recorte); original: `power_left_en_original.png` |
| `ui/office/mute_call_en.png` | `images/hud/mutecall.png` | ui | en | Boton MUTE CALL | original |
| `branding/nyder_logo.png` | `images/nyder/NYDER-logo.png` | branding | - | Logo de NYDER en el pie del menu | original |
| `doors/door_right_opening.gif` | `images/puertas/pDER-ABRIR.gif` | puerta | - | Puerta derecha abriendose | original |
| `doors/door_right_closing.gif` | `images/puertas/pDER-CERRAR.gif` | puerta | - | Puerta derecha cerrandose | original |
| `doors/door_left_opening.gif` | `images/puertas/pIZQ-ABRIR.gif` | puerta | - | Puerta izquierda abriendose | original |
| `doors/door_left_closing.gif` | `images/puertas/pIZQ-CERRAR.gif` | puerta | - | Puerta izquierda cerrandose | original |
| `ui/office/tablet_bar.png` | `images/ui/botones/barraTablet.fw.png` | ui | - | Barra inferior para abrir/cerrar la tableta | original |
| `ui/office/light_button_right_off.png` | `images/ui/botones/btnLuzDer0.fw.png` | ui | - | Boton de luz derecho apagado | original |
| `ui/office/light_button_right_on.png` | `images/ui/botones/btnLuzDer1.fw.png` | ui | - | Boton de luz derecho encendido | original |
| `ui/office/light_button_left_off.png` | `images/ui/botones/btnLuzIzq0.png` | ui | - | Boton de luz izquierdo apagado | original |
| `ui/office/light_button_left_on.png` | `images/ui/botones/btnLuzIzq1.png` | ui | - | Boton de luz izquierdo encendido | original |
| `ui/office/door_button_right_closed.png` | `images/ui/botones/btnPuertaDer0.fw.png` | ui | - | Boton de puerta derecho (puerta cerrada) | original |
| `ui/office/door_button_right_open.png` | `images/ui/botones/btnPuertaDer1png.fw.png` | ui | - | Boton de puerta derecho (puerta abierta) | original |
| `ui/office/door_button_left_closed.png` | `images/ui/botones/btnPuertaIzq0.png` | ui | - | Boton de puerta izquierdo (puerta cerrada) | original |
| `ui/office/door_button_left_open.png` | `images/ui/botones/btnPuertaIzq1.png` | ui | - | Boton de puerta izquierdo (puerta abierta) | original |
| `ui/office/door_panel_right_en.png` | `images/ui/botones/pnlBotonesDer.fw.png` | ui | en | Panel DOOR/LIGHT derecho | original |
| `ui/office/door_panel_left_en.png` | `images/ui/botones/pnlBotonesIzq.fw.png` | ui | en | Panel DOOR/LIGHT izquierdo | original |
| `doors/door_left_open.png` | `images/ui/puertas/103.png` | puerta | - | Puerta izquierda abierta (estatica) | original |
| `doors/door_right_open.png` | `images/ui/puertas/104.png` | puerta | - | Puerta derecha abierta (estatica) | original |
| `doors/door_right_closed.png` | `images/ui/puertas/puertaDer.png` | puerta | - | Puerta derecha cerrada (estatica) | original |
| `doors/door_left_closed.png` | `images/ui/puertas/puertaIzq.png` | puerta | - | Puerta izquierda cerrada (estatica) | original |
| `audio/sfx/jumpscare_scream.wav` | `sounds/animatronicos/jumpscare.wav` | sfx | - | Grito de los jumpscares | original |
| `audio/sfx/button_hover.wav` | `sounds/botones/botones_menu.wav` | sfx | - | Sonido hover de todos los botones | original |
| `audio/sfx/button_error.wav` | `sounds/botones/error_al_presionar_boton.wav` | sfx | - | Accion no disponible | original |
| `audio/sfx/lights_on.wav` | `sounds/botones/luces_encendidas.wav` | sfx | - | Luz de la puerta encendida (loop) | original |
| `audio/sfx/collectible_found.wav` | `sounds/coleccionables/coleccionable_encontrado.wav` | sfx | - | Coleccionable o bateria revelados | original |
| `audio/music/menu_music.wav` | `sounds/fondo/musica_fondo_menu.wav` | musica | - | Musica del menu | original |
| `audio/ambience/office_ambience.wav` | `sounds/fondo/sonido_ambiente_oficina.wav` | ambiente | - | Ambiente de la oficina | original |
| `audio/ambience/office_hum.wav` | `sounds/fondo/sonido_fondo_oficina.wav` | ambiente | - | Zumbido de fondo de la oficina | original |
| `audio/ambience/menu_static.wav` | `sounds/fondo/static_menu.wav` | ambiente | - | Estatica del menu | original |
| `audio/game_over/freddy_lurking.wav` | `sounds/gameover/freddy_acechando.wav` | game_over | - | Freddy acechando (Game Over) | original |
| `audio/game_over/freddy_footsteps.wav` | `sounds/gameover/pasos_freddy.wav` | game_over | - | Pasos de Freddy (Game Over) | original |
| `audio/game_over/power_down.wav` | `sounds/gameover/powerdown.wav` | game_over | - | Apagon (Game Over) | original |
| `audio/game_over/static.wav` | `sounds/gameover/static.wav` | game_over | - | Estatica final del Game Over | original |
| `audio/phone_calls/phone_pickup.wav` | `sounds/phoneguy/contestar_telefono.wav` | llamada | - | Aviso al contestar el telefono | original |
| `audio/phone_calls/night_01_call_en.wav` | `sounds/phoneguy/noche1_en.wav` | llamada | en | Llamada de la Noche 1 | original |
| `audio/phone_calls/night_01_call_es.wav` | `sounds/phoneguy/noche1_es.wav` | llamada | es | Llamada de la Noche 1 | original |
| `audio/phone_calls/night_02_call_en.wav` | `sounds/phoneguy/noche2_en.wav` | llamada | en | Llamada de la Noche 2 | original |
| `audio/phone_calls/night_02_call_es.wav` | `sounds/phoneguy/noche2_es.wav` | llamada | es | Llamada de la Noche 2 | original |
| `audio/phone_calls/night_03_call_en.wav` | `sounds/phoneguy/noche3_en.wav` | llamada | en | Llamada de la Noche 3 | original |
| `audio/phone_calls/night_03_call_es.wav` | `sounds/phoneguy/noche3_es.wav` | llamada | es | Llamada de la Noche 3 | original |
| `audio/phone_calls/night_04_call_en.wav` | `sounds/phoneguy/noche4_en.wav` | llamada | en | Llamada de la Noche 4 | original |
| `audio/phone_calls/night_04_call_es.wav` | `sounds/phoneguy/noche4_es.wav` | llamada | es | Llamada de la Noche 4 | original |
| `audio/phone_calls/night_05_call_en.wav` | `sounds/phoneguy/noche5_en.wav` | llamada | en | Llamada de la Noche 5 | original |
| `audio/phone_calls/night_05_call_es.wav` | `sounds/phoneguy/noche5_es.wav` | llamada | es | Llamada de la Noche 5 | original |
| `audio/sfx/door.wav` | `sounds/puertas/puerta.wav` | sfx | - | Abrir/cerrar puerta | original |
| `audio/random/chica_kitchen.wav` | `sounds/random/chica_en_cocina.wav` | aleatorio | - | Sonido ambiental aleatorio | original |
| `audio/random/foxy_singing.wav` | `sounds/random/foxy_cantando.wav` | aleatorio | - | Sonido ambiental aleatorio | original |
| `audio/random/interference_01.wav` | `sounds/random/interferencia1.wav` | aleatorio | - | Sonido ambiental aleatorio | original |
| `audio/random/interference_02.wav` | `sounds/random/interferencia2.wav` | aleatorio | - | Sonido ambiental aleatorio | original |
| `audio/random/knock_01.wav` | `sounds/random/knock1.wav` | aleatorio | - | Sonido ambiental aleatorio | original |
| `audio/random/knock_02.wav` | `sounds/random/knock2.wav` | aleatorio | - | Sonido ambiental aleatorio | original |
| `audio/random/raspy_breathing.wav` | `sounds/random/respiracion_turbia.wav` | aleatorio | - | Sonido ambiental aleatorio | original |
| `audio/random/freddy_laugh_01.wav` | `sounds/random/risa_freddy1.wav` | aleatorio | - | Sonido ambiental aleatorio | original |
| `audio/random/freddy_laugh_02.wav` | `sounds/random/risa_freddy2.wav` | aleatorio | - | Sonido ambiental aleatorio | original |
| `audio/random/running.wav` | `sounds/random/run.wav` | aleatorio | - | Sonido ambiental aleatorio | original |
| `audio/random/whispers.wav` | `sounds/random/susurros.wav` | aleatorio | - | Sonido ambiental aleatorio | original |
| `audio/sfx/freddy_nose_honk.wav` | `sounds/varios/nariz_freddy.wav` | sfx | - | Easter egg de la nariz de Freddy | original |
| `audio/sfx/heavy_breathing.wav` | `sounds/varios/respiracion_agitada.wav` | sfx | - | Respiracion tras sobrevivir un jumpscare | original |
| `audio/music/escape_transition_les_toreadors_remix.wav` | `sounds/win/LesToreadorsRemix.wav` | musica | - | Musica de la transicion Noche 5 -> Escape | original |
| `videos/victory.mp4` | `videos/ganar_fdaf.mp4` | video | - | Video de victoria (JavaFX) | original |
| `videos/surrender/surrender_0001.jpg` … `surrender_0160.jpg` | (nuevo en v2.9) | video (secuencia) | - | Fotogramas de la cinematica de Rendirse (§1.59), panorama 1800×600 a 20 fps | derivado: render de Blender (`tools/cinematics/surrender/`, escena en `FDAF assets/source_files/cinematics/surrender/`) |
| `audio/cinematics/surrender_mix.wav` | (nuevo en v2.9) | cinematica | - | Banda sonora de la cinematica de Rendirse (12.6 s) | derivado: mezcla de `power_down`, `freddy_footsteps`, `static` y `heavy_breathing` + efectos sintetizados (`mix_surrender_audio.py`) |

Rutas construidas por código (no aparecen literales): `intros/night_%02d_intro_{en,es}.gif` (`CargarImagenes.rutaIntroPorNoche`), `ui/hud/night_%02d_{en,es}.png` (`getEtiquetaNoche`), `audio/phone_calls/night_%02d_call_{en,es}.wav` (`ControllerInterfaz.rutaLlamadaNocheActual`), `jumpscares/<animatronico>_jumpscare.gif` (`alRevelarAnimatronico`, nombre del enum en minúsculas). Todos los sonidos se piden a `Sonido` con la ruta relativa a `audio/`.

Sin cambios y fuera de este catálogo: `images/nyder/` (`NYDER.png` con cambios locales de otra persona y 3 PNG sin seguimiento; ninguno se usa en el código).

### Retirados del repositorio en v2.8 (sin ningún uso; copia en `FDAF assets/_archive/fdaf_repo_unused/`)

| Ruta anterior | Motivo |
|---|---|
| `images/hud/450.png`, `images/hud/Cusor.png`, `images/hud/FNAFtitle.png` | Nunca referenciados |
| `images/hud/powerLeft.png` | Reemplazado por `ui/hud/power_left_{en,es}.png` (§1.40) |
| `images/fondos/win.png` | Sin uso desde §1.11 |
| `sounds/phoneguy/phoneguy_espanol.wav`, `phoneguy_ingles.wav` | Reemplazados por las llamadas por noche (§1.11) |
| `sounds/win/circus.wav` | Sin uso desde §1.11 (Escape tiene su propia copia: `audio/music/victory_circus.wav`) |
| `gifs/fondos/introduccion/AbrirTablet.gif`, `gifs/fondos/cerrarTablet_generado.gif` | Nunca referenciados |
| `sounds/random/risa_niña.wav` | Excluido a propósito del grupo de sonidos aleatorios (§1.5) |
| `sounds/random/robotvoice.wav` | Excluido a propósito del grupo de sonidos aleatorios; su original es el mismo archivo que `escape/audio/sfx/golden_freddy_robot_voice_original.wav` |

## Five Doors Escape — recursos en uso

Ruta relativa a `five_doors_escape/assets/`. Los JSON de `content/` (definiciones de entidades y del mapa) y los `.properties` de `i18n/` conservan sus nombres: son identificadores de código.

| Recurso | Nombre anterior | Tipo | Idioma | Uso en el juego | En FDAF assets |
|---|---|---|---|---|---|
| `models/bonnie.glb` | `models/Bonnie.glb` | modelo | - | Bonnie (retargeting Walking/Breathing Idle) | `source_files/3d_models/LibGDX_Ready/Bonnie.glb` (idéntico) |
| `models/chica.glb` | `models/Chica.glb` | modelo | - | Chica (retargeting Walking/Breathing Idle) | `source_files/3d_models/LibGDX_Ready/Chica.glb` (idéntico) |
| `models/foxy.glb` | `models/Foxy.glb` | modelo | - | Foxy (retargeting Walking/Breathing Idle) | `source_files/3d_models/LibGDX_Ready/Foxy.glb` (idéntico) |
| `models/freddy.glb` | `models/Freddy.glb` | modelo | - | Freddy (retargeting Walking/Breathing Idle) | `source_files/3d_models/LibGDX_Ready/Freddy.glb` (idéntico) |
| `models/golden_freddy.glb` | `models/GoldenFreddy.glb` | modelo | - | Golden Freddy (estatico, sin animaciones) | `source_files/3d_models/Golden Freddy/tmg_withered_golden_freddy.glb` (idéntico) |
| `models/key.glb` | `models/Llave.glb` | modelo | - | Llave de la puerta de salida | `source_files/3d_models/llave.glb` (idéntico) |
| `models/pizzeria.glb` | `models/Pizzeria.glb` | modelo | - | Mapa de la pizzeria (con techo propio) | `source_files/3d_models/LibGDX_Ready/Pizzeria.glb` (idéntico) |
| `effects/golden_freddy_its_me.gif` | `gifs/ItsMe.gif` | efecto | - | Texto IT'S ME de Golden Freddy (con clave de luminancia) | `escape/effects/golden_freddy_its_me.gif` |
| `effects/static.gif` | `gifs/Static.gif` | efecto | - | Estatica tras un jumpscare (igual que FDAF backgrounds/static.gif) | `escape/effects/static.gif` |
| `ui/heart_full.png` | `textures/corazon_relleno.png` | ui | - | Corazon de vida llena | `escape/ui/heart_full.png` |
| `ui/heart_empty.png` | `textures/corazon_vacio.png` | ui | - | Corazon de vida perdida | `escape/ui/heart_empty.png` |
| `jumpscares/golden_freddy_jumpscare.png` | `textures/golden_freddy_jumpscare.png` | jumpscare | - | Imagen del jumpscare de Golden Freddy | `escape/jumpscares/golden_freddy_jumpscare.png` |
| `audio/sfx/item_pickup.wav` | `sounds/agarrando_objeto.wav` | sfx | - | Recoger la llave | `escape/audio/sfx/item_pickup.wav` |
| `audio/music/victory_circus.wav` | `sounds/circus.wav` | musica | - | Musica de la pantalla de victoria | `escape/audio/music/victory_circus.wav` |
| `audio/voice/escape_intro_narration_en.wav` | `sounds/escape_en.wav` | voz | en | Narracion de la cinematica inicial | `escape/audio/voice/escape_intro_narration_en.wav` |
| `audio/voice/escape_intro_narration_es.wav` | `sounds/escape_es.wav` | voz | es | Narracion de la cinematica inicial | `escape/audio/voice/escape_intro_narration_es.wav` |
| `audio/sfx/door_locked_rattle.wav` | `sounds/forzando_puerta.wav` | sfx | - | Intentar abrir la salida sin llave | `escape/audio/sfx/door_locked_rattle.wav` |
| `audio/sfx/golden_freddy_glitch.wav` | `sounds/glitch_grave.wav` | sfx | - | Glitch de Golden Freddy (normalizado, ver §2.42) | `escape/audio/sfx/golden_freddy_glitch.wav`; original: `golden_freddy_glitch_original.wav` |
| `audio/sfx/jumpscare_scream.wav` | `sounds/jumpscare.wav` | sfx | - | Grito de jumpscare (igual que FDAF) | `escape/audio/sfx/jumpscare_scream.wav` |
| `audio/sfx/heartbeat_normal.wav` | `sounds/latido_normal.wav` | sfx | - | Latido a distancia media | `escape/audio/sfx/heartbeat_normal.wav` |
| `audio/sfx/heartbeat_fast.wav` | `sounds/latido_rapido.wav` | sfx | - | Latido cercano | `escape/audio/sfx/heartbeat_fast.wav` |
| `audio/sfx/flashlight_click.wav` | `sounds/linterna_click.wav` | sfx | - | Encender/apagar la linterna | `escape/audio/sfx/flashlight_click.wav` |
| `audio/music/freddy_music_box.wav` | `sounds/musica_escape_freddy.wav` | musica | - | Musica espacial de Freddy | `escape/audio/music/freddy_music_box.wav` |
| `audio/sfx/heavy_breathing.wav` | `sounds/respiracion_agitada.wav` | sfx | - | Respiracion tras el jumpscare de Golden Freddy (igual que FDAF) | `escape/audio/sfx/heavy_breathing.wav` |
| `audio/sfx/freddy_laugh.wav` | `sounds/risa_freddy.wav` | sfx | - | Risa de Freddy (igual que FDAF audio/random/freddy_laugh_01.wav) | `escape/audio/sfx/freddy_laugh.wav` |
| `audio/sfx/golden_freddy_robot_voice.wav` | `sounds/robotvoice.wav` | sfx | - | Voz robotica de IT'S ME (normalizada, ver §2.42) | `escape/audio/sfx/golden_freddy_robot_voice.wav`; original: `golden_freddy_robot_voice_original.wav` |
| `audio/sfx/static.wav` | `sounds/static.wav` | sfx | - | Estatica tras un jumpscare (igual que FDAF audio/game_over/static.wav) | `escape/audio/sfx/static.wav` |
| `audio/sfx/golden_freddy_scream.wav` | `sounds/xscream2.wav` | sfx | - | Grito del jumpscare de Golden Freddy | `escape/audio/sfx/golden_freddy_scream.wav` |
| `audio/ambience/night_parking_lot.wav` | (nuevo, §2.49) | ambiente | - | Ambiente nocturno del estacionamiento (bucle 18.5 s) | `escape/audio/ambience/night_parking_lot.wav`; sintetizado (`tools/cinematics/escape_victory/synth_victory_audio.py`) |
| `audio/sfx/exit_door_unlock.wav` | (nuevo, §2.49) | sfx | - | Llave y apertura de la puerta de salida | `escape/audio/sfx/exit_door_unlock.wav`; sintetizado (`tools/cinematics/escape_victory/synth_victory_audio.py`) |
| `audio/sfx/emergency_door_open.wav` | (nuevo, §2.49) | sfx | - | Barra antipanico y golpe de la puerta de emergencia | `escape/audio/sfx/emergency_door_open.wav`; sintetizado (`tools/cinematics/escape_victory/synth_victory_audio.py`) |
| `audio/sfx/car_door_open.wav` | (nuevo, §2.49) | sfx | - | Puerta del auto abriendose | `escape/audio/sfx/car_door_open.wav`; sintetizado (`tools/cinematics/escape_victory/synth_victory_audio.py`) |
| `audio/sfx/car_door_close.wav` | (nuevo, §2.49) | sfx | - | Puerta del auto cerrandose | `escape/audio/sfx/car_door_close.wav`; sintetizado (`tools/cinematics/escape_victory/synth_victory_audio.py`) |
| `audio/sfx/car_engine_start.wav` | (nuevo, §2.49) | sfx | - | Arranque del motor y ralenti | `escape/audio/sfx/car_engine_start.wav`; sintetizado (`tools/cinematics/escape_victory/synth_victory_audio.py`) |
| `audio/sfx/car_drive_away.wav` | (nuevo, §2.49) | sfx | - | El auto acelera y se aleja | `escape/audio/sfx/car_drive_away.wav`; sintetizado (`tools/cinematics/escape_victory/synth_victory_audio.py`) |
| `audio/sfx/gate_barrier_raise.wav` | (nuevo, §2.49) | sfx | - | Barrera del porton subiendo | `escape/audio/sfx/gate_barrier_raise.wav`; sintetizado (`tools/cinematics/escape_victory/synth_victory_audio.py`) |
| `models/exterior_parking.glb` | (nuevo, §2.49) | modelo | - | Exterior: fachada, estacionamiento, valla, porton, faroles, letrero (build_exterior.py) | `source_files/cinematics/escape_victory/`; terceros CC0 en su `LICENSES.md` |
| `models/car_sedan.glb` | (nuevo, §2.49) | modelo | - | Auto de la huida (Car de Quaternius, CC0, adaptado con prep_car.py) | `source_files/cinematics/escape_victory/`; original `downloads/quaternius_car_original.glb` |
| `audio/sfx/defeat_drag.wav` | (nuevo, §2.50) | sfx | - | Tiron de arrastre por el piso | `escape/audio/sfx/defeat_drag.wav`; sintetizado (`tools/cinematics/escape_defeat/synth_defeat_audio.py`) |
| `audio/sfx/defeat_body_thud.wav` | (nuevo, §2.50) | sfx | - | Golpe del cuerpo sobre el escenario | `escape/audio/sfx/defeat_body_thud.wav`; sintetizado (`tools/cinematics/escape_defeat/synth_defeat_audio.py`) |
| `audio/sfx/defeat_servo.wav` | (nuevo, §2.50) | sfx | - | Servo del animatronico al inclinarse | `escape/audio/sfx/defeat_servo.wav`; sintetizado (`tools/cinematics/escape_defeat/synth_defeat_audio.py`) |
| `audio/sfx/defeat_light_short.wav` | (nuevo, §2.50) | sfx | - | Cortocircuito del foco del escenario | `escape/audio/sfx/defeat_light_short.wav`; sintetizado (`tools/cinematics/escape_defeat/synth_defeat_audio.py`) |
