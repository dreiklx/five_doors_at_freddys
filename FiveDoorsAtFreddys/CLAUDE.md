# CONTEXT.md — Documento de contexto para Claude Code

**Nota meta:** este documento está escrito para que otra instancia de Claude retome el desarrollo sin necesitar releer todo el historial de la conversación original. Es denso y técnico a propósito — está pensado para un lector-IA, no para un humano. Toda afirmación aquí proviene de auditorías reales sobre el código, no de suposiciones — donde hubo incertidumbre en la conversación original, se marca explícitamente como tal.

---

## 0. Estructura general — DOS PROYECTOS SEPARADOS, NO UNO

Este trabajo consiste en **dos aplicaciones independientes**, sin dependencias de código entre sí, comunicadas exclusivamente por un archivo de traspaso:

1. **Five Doors at Freddy's** — juego 2D en Java 8 / Swing. **Prácticamente terminado y estable. Tratar como código congelado salvo indicación explícita del usuario.**
2. **Five Doors Escape** — expansión 3D en LibGDX/Java 21. **Recién empezando** — solo existe `Architecture.md` v1.0 (aprobado) como contrato; la implementación real está en su primer paso (proyecto vacío generado con `gdx-liftoff`, aún sin confirmar por el usuario que compila y corre).

**Regla explícita del usuario, repetida varias veces a lo largo del proyecto:** no migrar Swing a LibGDX, no convertir el proyecto Swing a Gradle, no cambiar la versión de Java del proyecto Swing. Son y deben seguir siendo ecosistemas técnicos completamente separados.

---

## 1. PROYECTO 1 — Five Doors at Freddy's (Swing, 2D)

### 1.1 Objetivo

Juego final para el curso IF3001 (UCR, Sede del Caribe), Grupo 1 (Derek, Naygell, Ramsey, Elmer). Clon estilo FNAF 1: el jugador vigila una oficina, revisa cámaras/puertas/luces, gestiona batería, y sobrevive noches con dificultad creciente (5 noches).

### 1.2 Stack técnico real (verificado, no supuesto)

- **Java 8** (compliance 1.8 confirmado en `.settings/org.eclipse.jdt.core.prefs` — **NO es Java 18**, error de nomenclatura que se arrastró en parte de la conversación original hasta corregirse).
- Swing/AWT puro para toda la UI.
- JavaFX únicamente para el video de victoria (`PnlWin.reproducirVideo`, vía `JFXPanel`).
- Proyecto Eclipse clásico, sin Gradle/Maven.

### 1.3 Arquitectura — MVC

```
com.fdaf.mvc.models/
├── juego/          Juego, Noche (enum), Arbol, GeneradorArbol
├── puerta/         Puerta, TipoPuerta (enum: ANIMATRONICO, COLECCIONABLE, NADA, BATERIA)
├── coleccionables/ Coleccionable, TipoColeccionable (enum), InventarioColeccionables, ColaColeccionables
└── animatronicos/  Animatronico (enum)

com.fdaf.mvc.controllers/
├── ControllerMenu       Menú principal, opciones, intro, warning, atajos debug, navegación ESC
├── ControllerInterfaz   Orquestación completa de una partida: audio, tablet, jumpscares, victoria/derrota
├── ControllerCamara     Paneo de cámara, transición de tablet, barra de rendirse
├── ControllerJuego      Puente entre input del jugador y el modelo Juego
└── JuegoListener        Interfaz de callbacks (alAbrirPuerta, alRevelarAnimatronico, alGanar, alPerder, etc.)

com.fdaf.mvc.views.frames.pnl/
PnlMenu, PnlOpciones, PnlIdioma, PnlVolumen, PnlNochePersonalizada, PnlConfirmacion,
PnlGameOver, PnlWin, PnlJuego, PnlTableta, PnlWarning, PnlAplicarImagen

com.fdaf.util/
Fuentes, EscalarVista (⚠️ CONGELADO, ver §1.7), CargarImagenes, CargarGifs,
ConfiguracionAudio, InteraccionBoton, PreferenciasJuego, PersistenciaJuego,
Idioma (enum), SonidosAmbientalesAleatorios

com.fdaf.mvc.views.multimedia/
Sonido  (wrapper de audio, aplica volumen global automáticamente en su constructor)
```

### 1.4 Datos reales de `Noche` (enum, confirmado)

```java
NOCHE_1(4 vidas, 5 coleccionables)
NOCHE_2(3, 6)
NOCHE_3(3, 7)
NOCHE_4(2, 8)
NOCHE_5(2, 10)
```

### 1.5 Mecánicas implementadas

- **Menú:** hover con flecha `>>` + sonido (`InteraccionBoton`), marcador `>` como prefijo del texto para indicar selección activa (noche en Custom Night, idioma en Idioma). Fuente centralizada en `Fuentes.java` (Consolas con fallback automático a Tahoma si no está instalada).
- **Persistencia:** `Properties` en `~/.fivedoorsatfreddys/config.properties` — idioma, nocheActual, volumenGeneral. Progreso avanza automáticamente al ganar (`nocheActual.ordinal()+1`, tope en NOCHE_5).
- **Volumen:** escala 1-10, curva cúbica a dB, aplicado automáticamente por `Sonido` en su propio constructor — ningún llamador necesita aplicar volumen manualmente.
- **Generación de puertas (`GeneradorArbol`):** bucle donde cada iteración sortea 1 de 3 opciones equiprobables (colocar coleccionable pendiente / puerta sin coleccionable 50-50 animatrónico-nada / puerta nada directa); solo la primera consume la cola de coleccionables. Resultado: **la densidad de animatrónicos por puerta es matemáticamente idéntica en todas las noches** (~1/6); lo único que escala con la dificultad es el tamaño total del árbol (vía cantidad de coleccionables). Animatrónicos esperados: Noche1≈5.0 → Noche5≈7.5.
- **Coleccionables:** 10 `TipoColeccionable` fijos, inventario de 10 slots en `PnlTableta`. Estados visuales: silueta bloqueada (`archivoBloqueado`) si posible-pero-no-encontrado-esta-noche, **X gris** (RGB 95,95,95 — deliberadamente ni negro puro ni blanco, elegido para no confundirse ni con la silueta negra ni con hallazgos reales; `Font.SANS_SERIF BOLD` tamaño 68) si el tipo no existe esta noche (`tipo.ordinal() >= cantidadColeccionables`), ícono real si ya se encontró. Clic en un slot muestra detalle grande (`lblMostrar`) + descripción bilingüe (`TipoColeccionable.getDescripcion(boolean ingles)`) en `lblDescripcion`.
- **Mecánica de batería extra (agregada en esta sesión):** `TipoPuerta.BATERIA` — **reinterpretación dinámica de puertas `NADA` en el momento real de apertura** (dentro de `Juego.procesarPuerta`, NO en la generación del árbol, para que la evaluación use las vidas actuales reales). Condición original documentada en esta sesión: `nocheActual.ordinal() >= 2` (Noche 3+) Y probabilidad según vidas actuales. **[ACTUALIZADO 2026-07-30, verificado contra código real]:** `Juego.calificaParaBateria()` NO contiene ningún gate de noche — solo valida `vidas == 1 || vidas == 2` y consulta la tabla directamente. La tabla `PROBABILIDAD_BATERIA` tiene valores no-cero para Noche 1 y 2, así que la mecánica está activa **desde la Noche 1**, no desde la Noche 3. No se determinó si el gate se quitó deliberadamente o si nunca llegó a implementarse — la tabla completa de probabilidades por vidas actuales sigue siendo correcta:

  > **[ACTUALIZADO 2026-10-26, §1.78]:** los valores de esta tabla están desactualizados desde la Noche 2. Los vigentes en el código son: N1 8 % (1 vida) / 5 % (2 vidas), N2 30/22 %, N3 46/28 %, N4 50/32 %, N5 62/42 %. La simulación de dificultad está en §1.78.

  | Noche | 2 vidas | 1 vida |
  |---|---|---|
  | 1 | 5% | 8% |
  | 2 | 8% | 11% |
  | 3 | 13% | 28% |
  | 4 | 16% | 32% |
  | 5 | 19% | 36% |

  Tabla como `double[][] PROBABILIDAD_BATERIA` en `Juego.java`, indexada `[noche][vidas-1]`. Con 3+ vidas, probabilidad es 0 (no evaluada). `ganarVidaExtra()` con tope duro `VIDAS_MAXIMAS=4` (límite técnico real: `alActualizarVidas` solo tiene íconos hasta `bateria4`). Visual: ícono `bateria1` (no `bateria4`), tamaño 80×45 (ancho igual al de coleccionables, alto reducido para verse menos alargado). Reutiliza el mismo overlay y sonido de hallazgo que los coleccionables — no duplica lógica.
- **Transición de tablet:** `ControllerInterfaz.mostrarTransicionTablet(rutaGif, accionInmediata, alTerminar)` — **único método compartido** para abrir y cerrar, usando `EscalarVista.GifEscalado`/`CargarGifs` (el mecanismo nativo de AWT, NO un reproductor de frames propio — ver §1.7 para el historial de por qué). Timer de 360ms (ajustado por el usuario a la duración real de sus gifs editados). Para el cierre, `accionInmediata` (el cambio de panel) se ejecuta, y la configuración del gif se difiere un ciclo del EDT vía `SwingUtilities.invokeLater` para reducir una condición de carrera con el repintado — **este parpadeo nunca quedó 100% resuelto con certeza total, se considera "aceptable" en su estado actual, no perfecto.**
- **Navegación ESC:** un solo handler en `ControllerMenu.manejarEscape()`, compara por referencia el panel actualmente mostrado contra las pantallas conocidas.
- **Atajos de debug (CTRL+G/P/S):** decisión consciente del usuario, **no eliminar**. G=forzar victoria, P=forzar derrota, S=saltar la "fase larga" actualmente activa (intro de 20s, la fase larga de Game Over, o el Warning inicial) — un solo dispatcher en `ControllerMenu` que revisa cuál fase está activa.
- **Secuencia de Game Over:** `alPerder()` → gif+powerdown.wav (t1, 7s) → freddy_acechando.wav (t2, 13s) → `avanzarDespuesDelGifLargo()` → pasos_freddy.wav + jumpscare (t3, 5s) → estática (t4, 1s) → `mostrarPantallaFinalGameOver()` (t5, 5s, muestra botones). Los 4 timers y `pantallaGameOver` son **campos de instancia** (no locales), para que `saltarFaseLargaGameOver()` (CTRL+S) pueda detenerlos todos y saltar **directo a la pantalla final con botones**, sin importar en qué tramo se presione — decisión deliberada de "único destino posible" por limpieza arquitectónica, no por ahorro de esfuerzo.
- **Secuencia de Victoria:** `alGanar()` avanza noche, llama `controllerMenuPadre.actualizarTextosIdioma()`, muestra `PnlWin`, reproduce `ganar_fdaf.mp4` (JavaFX), luego `circus.wav` en loop + botones.
- **Sonidos ambientales aleatorios:** `SonidosAmbientalesAleatorios`, un solo `Timer` autoreprogramado, pool ponderado documentado originalmente como 12 de 13 archivos de `sounds/random/` (**`robotvoice.wav` explícitamente excluido** por pedido del usuario), regla de no-repetir-el-inmediatamente-anterior, se pausa durante jumpscares reales (`ultimoFueAnimatronico`) y se reanuda correctamente solo en ese caso — **bug corregido en esta sesión:** antes se reanudaba en CADA `alLiberar()` (cualquier puerta revisada, no solo jumpscares), lo cual reiniciaba el timer constantemente durante juego activo normal y explicaba por qué casi no sonaban. **[ACTUALIZADO 2026-07-30, verificado contra código real]:** el `POOL` real tiene **11 entradas, no 12**. Además de `robotvoice.wav`, también está excluido `risa_niña.wav` (no documentado previamente). No hay evidencia de si esta segunda exclusión fue intencional o un descuido — el resto del mecanismo (pausa/reanudación en jumpscares, guard de "no repetir el anterior") sí se confirmó exactamente como se describe arriba.

### 1.6 Bug crítico corregido en esta sesión — guard de `juegoTerminado`

**Hallazgo real:** `setEnabled(false)` sobre un `JLabel` con `MouseListener` agregado manualmente **no bloquea el despacho de eventos de mouse** (a diferencia de `JButton`/`AbstractButton`, que sí consultan `isEnabled()` internamente). Esto permitía, por ejemplo, abrir la tablet en el instante exacto de ganar/perder y sobrescribir la pantalla de Victoria/Game Over.

**Corrección aplicada:** guard explícito `controllerJuego.isJuegoTerminado()` en **cada** punto de entrada de gameplay — puertas, luces, clic en overlay, los 10 clics de inventario, nariz, colgar llamada, las 6 zonas de cámara, apertura/cierre de tablet, y la barra de rendirse. Centralizado vía `siPartidaActiva(Runnable)` en `ControllerInterfaz` y `juegoTerminado()` en `ControllerCamara` — no repetido de forma independiente en cada listener.

**Segunda carrera relacionada, también corregida:** `detenerSonidosJuego()` ahora también detiene `timerTransicionTablet`, `timerOcultarBotonesTablet`, `timerFadeNegro`, `timerGifPuertaIzq/Der`, y (vía `ControllerCamara.detenerTimersDePartida()`) los timers de movimiento de cámara y de la barra de rendirse (`tiempo`/`soltar`) — cualquiera de estos, si sobrevivía a un fin de partida, podía ejecutar un cambio de panel tardío y sobrescribir la pantalla final.

### 1.7 Saga de memoria — `OutOfMemoryError` — HISTORIAL COMPLETO, IMPORTANTE

**Causa raíz confirmada:** `CargarGifs.cargarFresco()` usaba `Toolkit.createImage(byte[])` fresco en cada llamada → cada gif animado arranca su propio hilo "Image Animator N" de AWT que nunca termina solo si el gif tiene loop infinito → memoria agotada tras sesiones largas (confirmado reproducible: Noche1→Noche2 en una sola sesión).

**Intento 1 (parcial):** caché `Map<String,Image>` + `Image.flush()` sobre la imagen anterior de la misma ruta antes de crear la nueva. **Insuficiente por sí solo en la primera prueba** — el OOM volvió a ocurrir.

**Intento 2 (grande, REVERTIDO POR COMPLETO):** migración a un reproductor de frames propio (`CacheFramesGif`+`ReproductorGif`, decodificación vía `ImageIO` en vez de `Toolkit`), migrado primero en la pantalla de intro como piloto. **Calidad visual notablemente peor/entrecortada** comparada con el `GifEscalado` nativo. El usuario lo rechazó y pidió reversión completa. **NO reintroducir un reproductor de frames propio — ya se intentó, la calidad fue inaceptable, esfuerzo desperdiciado.**

**Intento 3 (CONFIRMADO DAÑINO DOS VECES — REGLA ABSOLUTA):** cambiar `gifOriginal.getImageObserver()` (siempre `null`, nadie llama `setImageObserver`) por `c` (el parámetro `Component` real) dentro de `EscalarVista.GifEscalado.paintIcon`. Es técnicamente la forma correcta de implementar `Icon.paintIcon` según la documentación de la API — pero **causó corrupción visual grave y generalizada en TODO el proyecto, en dos sesiones separadas e independientes** (una durante la investigación del parpadeo de tablet, otra durante esta investigación de memoria).

> **⚠️ REGLA ABSOLUTA: NUNCA cambiar `gifOriginal.getImageObserver()` por `c` en `EscalarVista.GifEscalado.paintIcon`. `EscalarVista.java` está CONGELADO — no modificar por ningún motivo sin evidencia nueva extremadamente sólida y aprobación explícita del usuario.**

**Estado final, resuelto:** `EscalarVista` revertida a su forma original (`getImageObserver()`), se mantuvo el caché+`flush()` de `CargarGifs` (inofensivo aunque no esté 100% probado como suficiente por sí solo) más instrumentación de memoria. Una prueba larga posterior (Noche1→Noche2) con esta configuración final produjo **33 llamadas registradas, memoria oscilando (evidencia de que el GC sí recupera), sin ningún `OutOfMemoryError`.** Se considera resuelto empíricamente, aunque `Image.flush()` tiene limitaciones documentadas para imágenes activamente animándose que no se demostraron del todo teóricamente. **Si el OOM reaparece en pruebas futuras, retomar la investigación desde aquí — el cambio del observador sigue estando descartado sin importar qué.**

**Limpieza pendiente, no hecha todavía:** la instrumentación `[MEMORIA]`/`contadorLlamadas` en `CargarGifs.java` está marcada `TEMPORAL` en comentarios — debería retirarse en algún momento. El `println("[AMBIENTAL] Reproduciendo: ...")` en `SonidosAmbientalesAleatorios` también es un resto de diagnóstico pendiente de quitar. **[RETIRADA 2026-10-22, ver §1.27.]**

### 1.8 Deuda técnica conocida, NO corregida (baja prioridad)

- `Sonido.java` nunca llama `clip.close()`, solo `.stop()` — línea de audio nativa no liberada explícitamente. Sin crash confirmado por esto.
- `ControllerMenu.opciones()` reagrega listeners a `btnAtras`/`btnSalir` **cada vez** que se visita la pantalla de Opciones (la instancia `opciones` es persistente, nunca se recrea) — fuga real de listeners pequeña, causaría que cada clic dispare múltiples veces. **[CORREGIDO 2026-10-22, ver §1.27.]**

### 1.9 Gotchas de assets

- Históricamente el archivo `random/risa_ni#U00f1a.wav` tenía ese nombre **literal** en disco (artefacto de codificación de "ñ") — se documentó usar la cadena exacta, nunca "corregirla". **[ACTUALIZADO 2026-07-30, verificado contra disco real]:** el archivo ya NO tiene ese nombre mangled. En disco está correctamente codificado en UTF-8 como `random/risa_niña.wav` (confirmado byte a byte: `ñ` = `0xC3 0xB1`, UTF-8 válido). Usar `risa_niña.wav` tal cual — la advertencia original sobre el nombre corrupto ya no aplica a esta copia del proyecto.
- Investigación de estética "pixelada estilo FNAF 1" se hizo extensamente y se concluyó **no perseguirla más** — el lienzo base real es 1600×900 (no 1280×720 como se supuso inicialmente), lo que hace que cualquier técnica de reescalado dé un efecto demasiado sutil para justificar el riesgo/complejidad. Único cambio aplicado: desactivar antialiasing globalmente (2 líneas en `Main.java`: `awt.useSystemAAFontSettings=off`, `swing.aatext=false`). No se cambió de fuente ni se agregó fuente pixelada.

### 1.10 Lo que falta para cerrar el Proyecto 1

- **[ACTUALIZADO 2026-08-02, ver §2.13]** Implementar §7 del `Architecture.md` del lado Swing: al ganar Noche 5, escribir `handoff.json` y lanzar el proyecto LibGDX vía `ProcessBuilder`. **Implementado y verificado en ejecución real** -- ver §2.13 para el detalle completo. **[ACTUALIZADO 2026-08-05, ver §1.11]** El efecto de "glitch" durante el video de victoria ya NO es un placeholder -- implementado con un efecto agresivo real (parpadeo de color + estática + tearing + separación RGB), ver §1.11 punto 11. Pantalla completa para Escape, botón de salida manual, y la condición de victoria/salida dentro de la pizzería ya estaban resueltos del lado LibGDX (§2.13-§2.15).
- Retirar instrumentación temporal (§1.7, §1.5 último punto). **[Hecho, §1.27.]**
- Opcionalmente: corregir la fuga de listeners de `opciones()` (§1.8). **[Hecho, §1.27.]**

### 1.11 Ronda de llamadas telefónicas, puertas/inventario del compañero, flujo de fin de noche y transición a Escape, sesión 2026-08-05

**Contexto:** el compañero de equipo hizo cambios importantes mientras tanto -- modificó el sistema de inventario y reemplazó la lógica visual de puertas por GIFs. El usuario pidió, tras un `git pull` en ambos repos: reemplazar el sistema de llamadas telefónicas por audio nuevo (5 noches × ES/EN), revisar y corregir los cambios del compañero, quitar el popup de fin de noche, un efecto de glitch agresivo al completar la Noche 5, y (del lado LibGDX) sincronizar el audio/duración de la cinemática de Escape y una linterna togglable -- ver §2.30 para la parte de Escape.

**1. Sistema de llamadas reemplazado, escalable por noche e idioma sin lógica duplicada.** `rutaLlamadaNocheActual()` (nuevo, único punto de resolución) mapea `PreferenciasJuego.nocheActual`+`idiomaSeleccionado` a `phoneguy/noche{N}_{es|en}.wav` -- una 6ª noche solo necesitaría agregar los 2 archivos de audio, sin tocar código. Los 10 archivos nuevos (`noche1..5_{es,en}.wav`) vienen de `FDAF Assets/New Call Sounds/`. Los antiguos `phoneguy_espanol.wav`/`phoneguy_ingles.wav` quedaron sin usar pero NO se borraron (decisión de limpieza diferida, no tomada esta sesión).

**2. Duración real de la llamada, nunca hardcodeada.** Ya funcionaba correctamente antes de esta sesión -- `Sonido.getDuracionMs()` envuelve `Clip.getMicrosecondLength()`, así que el timer que oculta el botón Mute Call y reanuda el ambiente ya se adaptaba solo a la duración real de cada audio. No hizo falta ningún cambio para esto; el problema percibido por el usuario ("el botón desaparece muy pronto") era en realidad que los audios viejos eran mucho más cortos que los nuevos, no un bug de lectura de duración.

**3. Bloqueo de puertas durante la llamada -- ya funcionaba, verificado por lectura de código.** No requirió cambios.

**4. Sonidos ambientales aleatorios retrasados hasta que la llamada termine o se mutee.** `sonidosAmbientales.iniciar()` se quitó de `init()` (arrancaba inmediatamente, antes de cualquier llamada) y se movió a los 2 puntos reales de fin de llamada: el timer de fin natural (dentro de `iniciarLlamada()`) y `colgarLlamada()`. Se creó una sobrecarga `colgarLlamada(boolean reanudarAmbiente)` porque `colgarLlamada()` también se llama desde `detenerSonidosJuego()` (fin de partida) -- reanudar el ambiente ahí sería un bug real (sonido de ambiente arrancando en medio de la secuencia de Game Over/Victoria). `reanudarAmbiente=false` solo en ese caso. El ambiente CONTINUO de oficina (`sonidoJuego1`/`sonidoJuego2`, hum de fondo, distinto de `sonidosAmbientales`) sigue sonando durante la llamada sin cambios -- no es lo que el usuario pidió silenciar.

**5. Cierre automático de puertas con GIF -- bug real del compañero, corregido.** En `alLiberar(String lado)`, el cierre manual (botón) ya reproducía el GIF de cierre correctamente, pero el cierre automático (al soltar tras revelar algo) tenía la llamada real comentada (`// NO BORRAR, GIF DE PUERTA`, referenciando además nombres de archivo ya renombrados/eliminados: `PuertaIzquierdaCerrando.gif`) y en su lugar hacía un snap instantáneo al ícono estático cerrado. Corregido reutilizando el mismo `reproducirGifPuertaUnaVez(...)` ya usado por el cierre manual, con los nombres de archivo reales actuales (`pIZQ-CERRAR.gif`/`pDER-CERRAR.gif`) -- sin duplicar lógica.

**6. Inventario -- bug real de escala del compañero, corregido en 11 sitios.** El commit del compañero redujo los slots de la tableta a 74×68 pero dejó 11 llamadas a `EscalarVista.getImagenEscalada(...)` con el tamaño viejo hardcodeado (`100, 92`) -- tanto el ícono "bloqueado" (`inicializarEstadoInventario`) como los 10 íconos reales por tipo de coleccionable (`agregarAlInventario`). Cada ícono ahora se escala al tamaño REAL de su propio `JLabel` (`destino.getWidth()/getHeight()`), no a un valor hardcodeado -- corrige el problema de raíz para cualquier cambio futuro de tamaño de slot, no solo para 74×68.

**7. Popup de Replay/Exit al completar noche -- eliminado según pedido.** `alGanar()`, para noches 1-4, ya no muestra `win.png`+`circus.wav`+botones "Volver a jugar"/"Salir del juego" -- el progreso ya se guardaba antes de cualquier efecto visual (sin cambios ahí), y ahora se vuelve directo al menú principal. Se eliminó el método `aplicarIdiomaWin` (ya no usado) y todo el wiring de botones asociado (hover, listeners). La Noche 5 sigue su propio flujo especial (glitch + Escape, punto 11).

**8. Sesión de descubrimiento -- 4 GIFs de puertas con modificación binaria sin causa identificada.** Durante la validación final se encontró que `pDER-ABRIR.gif`/`pDER-CERRAR.gif`/`pIZQ-ABRIR.gif`/`pIZQ-CERRAR.gif` (recién agregados por el compañero) mostraban cambios reales de contenido/tamaño en disco (confirmado con `git diff --stat`, no solo metadata) sin ningún código -- propio o preexistente -- capaz de escribir a esas rutas (`CargarGifs.java` grepeado, sin ningún path de escritura a archivo, solo un `ByteArrayOutputStream` en memoria). **Revertidos vía `git checkout` por precaución** (no se identificó causa, se prefirió no arriesgar la calidad de un asset recién commiteado por el compañero) -- si esto reaparece en sesiones futuras, investigar más a fondo; no se determinó si es un proceso externo (antivirus, indexador de Windows) u otra causa.

**9. Limitación de entorno confirmada -- `java.awt.Robot` no llega a esta ventana Swing en este entorno de pruebas.** Se intentó automatizar clicks/teclas reales (`Robot`) contra la ventana del juego para una validación interactiva completa -- confirmado con breadcrumbs de diagnóstico que CERO eventos de interacción llegaron a disparar código real (ni `Click Continuar`, ni `alGanar()`, ni atajos CTRL+G/P/S) en dos intentos independientes con coordenadas corregidas, pese a que los propios timers/pantallas internos del juego sí avanzaban y se veían con normalidad en capturas reales. **Conclusión: `Robot` no es viable para pruebas de interacción de este proyecto Swing en este entorno -- usar revisión de código + compilación limpia como estándar de verificación cuando la interacción real no se puede automatizar, documentando explícitamente esa limitación en vez de forzar un falso positivo.** Ver también memoria de Claude `feedback-swing-robot-automation-not-viable`.

**10. Deuda técnica NO tocada esta sesión, documentada para no reinvestigar:** las 4 GIFs revertidas (punto 8) siguen con el contenido committeado original -- si el compañero necesita resubirlas, hacerlo conscientemente, no a través de este bug no explicado. Los archivos `phoneguy_espanol.wav`/`phoneguy_ingles.wav` (punto 1) siguen en el repo sin usar.

**11. Transición a Escape en la Noche 5 -- efecto de glitch real implementado (cierra el pendiente de §1.10/§2.13).** `iniciarTransicionAEscape` ahora llama a `mostrarGlitchYLanzarEscape(...)` (nuevo) antes del mensaje de carga ya existente: un `JPanel` transparente superpuesto (mismo patrón ya usado por `iniciarFadeDesdeNegro` -- panel + `javax.swing.Timer` + `paintComponent` propio, sin librerías nuevas) que durante 2.6s dibuja, cada 45ms: un color sólido saturado aleatorio de fondo, el GIF `Static.gif` ya existente (reutilizado, no un asset nuevo) en un offset aleatorio, 6-11 barras horizontales de "tearing" con desplazamiento X y color aleatorios, y 2 rectángulos translúcidos rojo/cian simulando separación de canal RGB. Al terminar, se remueve el panel y se continúa con el mensaje de carga + `LanzadorEscape.lanzar(...)` ya existente, sin cambios en ese mecanismo. Verificación real de interacción quedó bloqueada por la limitación de `Robot` (punto 9) -- validado por revisión de código, compilación limpia, y por ser una extensión directa de un patrón (panel+Timer+paintComponent) ya probado en producción por `iniciarFadeDesdeNegro`.

**Commiteado y pusheado a `FiveDoorsAtFreddys`:** sistema de llamadas por noche/idioma, retraso de sonidos ambientales, cierre automático de puertas con GIF, corrección de escala de inventario (11 sitios), eliminación del popup de fin de noche, glitch de transición a Escape (todo en `ControllerInterfaz.java`), 10 archivos de audio nuevos (`phoneguy/noche{1-5}_{es,en}.wav`).

---

### 1.12 Validación real del sistema de llamadas + duración real de los GIFs de puertas (sin hardcodear), sesión 2026-08-05 (misma fecha, sesión posterior)

**Contexto:** ronda de validación explícita pedida por el usuario antes de seguir agregando funcionalidad -- "no asumas que funciona porque compila". Cubrió dos pedidos concretos: (1) probar de verdad todo el sistema de llamadas de §1.11; (2) el usuario acababa de re-exportar los 4 GIFs de puertas con una velocidad distinta, y pidió que el tiempo de apertura/cierre se sincronizara con la duración REAL de cada gif, nunca con una constante.

**Metodología nueva para este proyecto -- pruebas reales headless sin `Robot`, evitando la limitación de §1.11 punto 9.** En vez de intentar automatizar clicks/teclas contra la ventana real (ya descartado), se escribieron arneses de prueba standalone que instancian `ControllerJuego`/`ControllerInterfaz` directamente (sin mostrar ninguna ventana) y usan reflection para invocar métodos privados (`iniciarLlamada()`, `colgarLlamada()`) e inspeccionar campos privados (`llamadaActiva`, `puertaIzqAbierta`, `sonidosAmbientales.activo`) -- **estos objetos no dependen de una ventana visible para funcionar** (Swing permite construir/manipular componentes sin mostrarlos), así que esta técnica sí permite pruebas de comportamiento real, a diferencia de la interacción vía mouse/teclado. **Gotcha real de classpath encontrado:** rutas estilo Unix (`/c/Users/...`) en `-cp` para `javac`/`java` en este entorno (Git Bash sobre Windows) rompen silenciosamente la resolución de recursos vía `getResource()` (compila y corre sin error, pero `getResource()` devuelve `null` para todo) -- hay que usar rutas estilo Windows (`C:\Users\...`) en el classpath para que el `ClassLoader` de recursos funcione. Confirmado aislando el problema con una clase mínima de diagnóstico antes de asumir cualquier otra causa.

**1. Sistema de llamadas -- verificado de extremo a extremo con pruebas reales, no solo lectura de código:**
- Los 11 archivos de audio reales (10 llamadas + el stinger `contestar_telefono.wav`) se cargan sin error por la misma API que usa `Sonido` (`AudioSystem.getAudioInputStream`/`getClip`), con duraciones reales de 19.4s a 90.1s -- ninguna cae al valor de respaldo.
- Las 10 combinaciones noche×idioma (`rutaLlamadaNocheActual()`) resuelven exactamente al archivo esperado, confirmado invocando el método real por reflection para las 5 noches × 2 idiomas.
- Bloqueo de puertas/luces durante la llamada: confirmado con un test real que activa `llamadaActiva` y verifica que `abrirIzquierda()`/`abrirDerecha()`/`revelarIzquierda()` no cambian ningún estado interno mientras está activa, y sí lo hacen apenas se desactiva.
- Mute Call + sonidos ambientales: confirmado con un test real sobre `ControllerInterfaz` completo -- durante la llamada el botón está visible y `sonidosAmbientales.activo=false`; tras invocar `colgarLlamada()` (simula el clic del jugador), el botón se oculta y `sonidosAmbientales.activo=true`.
- Fin natural de la llamada (sin mutear): confirmado dejando correr en tiempo real el ciclo completo de la Noche 5 en inglés (la llamada real más corta, 19.4s) -- a los ~32s (stinger 12.2s + llamada 19.4s) el botón se oculta y el ambiente se reanuda solos, sin intervención.
- Toggle rápido y repetido de ambas puertas (40 ciclos, 15ms entre cada uno): sin excepciones, cancelación correcta del `Timer` anterior en cada toggle.

**2. GIF de puertas -- duración hardcodeada (900ms) reemplazada por lectura real del archivo.** Nuevo `com.fdaf.util.DuracionGif` (mismo principio que `WavDuration` del lado LibGDX): suma los `delayTime` reales de cada frame (metadata `GraphicControlExtension` vía `javax.imageio`, en centisegundos) del propio `.gif`, cacheado por ruta para no releer metadata en cada toggle de puerta. `reproducirGifPuertaUnaVez` ahora usa `DuracionGif.leerMilisegundos(rutaGif, MS_RESPALDO_GIF_PUERTA)` en vez del `900` fijo -- si el usuario vuelve a cambiar la velocidad de cualquiera de los 4 gifs en el futuro, el timer se ajusta solo, sin tocar ninguna constante. **Medido en esta sesión:** los 4 gifs re-exportados por el usuario duran 750ms reales (15 frames, antes 900ms era una sobre-estimación que dejaba ~150ms de pausa visible con el gif ya congelado en su último frame antes de que el código lo "cortara"). Verificado con un test real que dispara `alAbrirPuerta("izq")` y confirma por tipo de ícono (`GifEscalado` mientras anima, `ImageIcon` una vez congelado) que el corte ocurre exactamente en 750ms, ni antes ni después.

**Commiteado y pusheado a `FiveDoorsAtFreddys`:** `DuracionGif.java` (nuevo) + uso en `reproducirGifPuertaUnaVez` (`ControllerInterfaz.java`), y los 4 GIFs de puertas re-exportados por el usuario con la nueva velocidad (`pIZQ-ABRIR.gif`, `pIZQ-CERRAR.gif`, `pDER-ABRIR.gif`, `pDER-CERRAR.gif` -- a diferencia del incidente de §1.11 punto 8, esta vez el usuario confirmó explícitamente que él mismo los había editado justo antes de esta sesión, así que se commitearon con confianza en vez de revertirse).

---

## 2. PROYECTO 2 — Five Doors Escape (LibGDX, 3D)

### 2.1 Estado real

**[ACTUALIZADO 2026-07-31]** `Architecture.md` v1.0 está aprobado como contrato técnico oficial. El proyecto **ya existe, compila y corre** — dejó de ser "primerísimo paso sin confirmar". Ubicación real: `C:\Users\dfarl\git\five_doors_escape\` (repo Git propio, separado del repo de `FiveDoorsAtFreddys`). Detalle completo en §2.6.

**[ACTUALIZADO 2026-07-31, más tarde el mismo día]:** carpeta local y repositorio de GitHub renombrados de `five-doors-escape` (kebab-case) a `five_doors_escape` (snake_case) para mantener consistencia con el resto de los repositorios personales del usuario. Repo: `https://github.com/dreiklx/five_doors_escape`. `appName`/`rootProject.name` en Gradle y el título de la ventana del launcher LWJGL3 también se actualizaron a `five_doors_escape`. El paquete Java (`com.fivedoorsescape`) **no** se tocó — es una convención distinta (reverse-DNS sin separadores), no relacionada con este cambio.

**Contexto histórico (ya no vigente, se conserva por trazabilidad):** originalmente se le habían dado al usuario instrucciones para generar el proyecto con `gdx-liftoff` (Paso 1 del plan). Se descubrió que `gdx-liftoff` es una aplicación GUI de escritorio (Swing) **sin modo headless/CLI** — Claude no puede automatizarla. Por eso el proyecto se terminó construyendo a mano (archivos Gradle escritos directamente, replicando exactamente lo que `gdx-liftoff` genera), con aprobación explícita del usuario para ese cambio de método. El resultado final es equivalente al que hubiera producido `gdx-liftoff`.

### 2.2 Contenido completo de `Architecture.md` v1.0 (reproducido íntegro, es el contrato vinculante)

- **Relación con el proyecto Swing:** dos aplicaciones completamente independientes. Comunicación exclusiva vía archivo de traspaso JSON + `ProcessBuilder`.
- **Stack:** Java 21 (independiente del Java 8 del proyecto Swing — son JVMs separadas, sin necesidad de coincidir), Gradle vía `gdx-liftoff`, módulos **solo** `core`+`desktop` (sin Android/iOS/HTML), `gdx-gltf` para importar modelos, colisión AABB manual (sin `gdx-bullet`), `gdx-ai` para la máquina de estados de IA.
- **Principio rector:** arquitectura completamente desacoplada de los assets — ningún nombre de modelo/mapa hardcodeado en Java, todo vía JSON leído en tiempo de ejecución (`content/EntityDefinition`, `content/MapDefinition`, `content/ContentRegistry`).
- **Principio adicional (§3.1):** *"Toda nueva abstracción debe resolver un problema existente. No se agregan capas, sistemas o patrones 'por si acaso'."* — regla oficial del proyecto, aplicada tanto a decisiones grandes como pequeñas.
- **ECS (`gdx-ashley`):** adoptado con justificación honesta documentada — **no se justifica por las necesidades del MVP en sí** (un solo tipo de enemigo sería más simple con OOP tradicional), sino por la evolución prevista (varios animatrónicos con comportamientos distintos en los próximos meses). Componentes iniciales, deliberadamente mínimos: `TransformComponent`, `ModelComponent`, `AnimationComponent` (opcional, ver contrato de animaciones abajo), `AIComponent`, `PlayerComponent`, `CollisionComponent`.
- **`AssetService`:** capa que encapsula por completo `AssetManager` — ningún sistema llama `assetManager.get(...)` directamente.
- **Nomenclatura:** `EntityDefinition` (no `EntityDescriptor` — describe un tipo, no una instancia).
- **`ContentRegistry`:** administra las definiciones disponibles, pero **no carga automáticamente todo el contenido al arrancar** — es `BootScreen` quien decide explícitamente qué cargar para la sesión actual.
- **Contrato de animaciones opcionales (regla de diseño obligatoria, no un caso especial):** si `EntityDefinition.animaciones` está vacío, `AnimationSystem` debe omitir esa entidad limpiamente — la entidad se sigue moviendo (transform), su malla queda en pose de bind, sin ningún error. **Esto es directamente relevante porque Freddy (el modelo piloto ya validado) confirmadamente NO trae animaciones.**
- **Traspaso Swing→LibGDX (§7):** `HandoffData` (idioma, vidasFinales) en `~/.fivedoorsatfreddys/handoff.json` (mismo directorio base que ya usa `PersistenciaJuego` del lado Swing, vía `System.getProperty("user.home")` / `Gdx.files.external(...)` del lado LibGDX — portable entre SO, independiente del directorio de ejecución). **Comportamiento ante archivo ausente/corrupto/incompleto: NO es error fatal — usar valores por defecto de desarrollo y arrancar igual** (escenario esperado: ejecutar el proyecto LibGDX directo desde el IDE sin pasar por Swing).
- **Cámara:** `FirstPersonCameraController` propio (no el ejemplo de referencia de LibGDX), consciente de colisión.
- **Estructura de paquetes completa:**
```
com.fivedoorsescape/
├── FiveDoorsEscapeGame (extends Game)
├── io/                  HandoffData, HandoffReader
├── content/             EntityDefinition, MapDefinition, ContentRegistry
├── assets/              AssetService
├── ecs/                 components/, systems/, EntityFactory
├── ai/states/           IdleState, ChaseState, CaughtState (PatrolState diferido, no en MVP)
├── world/               LevelLoader, CollisionWorld
├── camera/              FirstPersonCameraController
└── screens/             BootScreen, GameplayScreen, NightGameOverScreen
```
- **`NightGameOverScreen` (§11.3):** al ser atrapado por Freddy, **NO se cierra la aplicación** — se transiciona a una pantalla que replica visualmente (color/tipografía/mensaje) la pantalla de Game Over del proyecto Swing, para continuidad narrativa ("el jugador sintió que perdió la Noche 5"). **Réplica de diseño con assets propios de LibGDX — nunca importar archivos/código del proyecto Swing** (no viola la independencia de los dos proyectos). Fidelidad del MVP acotada a color+tipografía+mensaje; geometría pixel-perfecta y mecánica de hover se difieren explícitamente. Interacción disponible en MVP: solo cerrar el juego (reiniciar queda para después).
- **Alcance exacto del MVP (vertical slice técnico, no demo de contenido):**
  1. Proyecto inicia correctamente.
  2. Lee el archivo de traspaso.
  3. Carga el mapa (un `MapDefinition`).
  4. Carga Freddy (un `EntityDefinition`, sin animaciones).
  5. Cámara FPS funcional.
  6. Movimiento WASD + mouse-look.
  7. Colisiones AABB básicas contra el mapa.
  8. Freddy: `IdleState` → detecta al jugador → `ChaseState`.
  9. Si Freddy alcanza al jugador: se detienen los sistemas ECS de gameplay, transición a `NightGameOverScreen`.
  - **Excluido explícitamente del MVP:** condición de victoria/salida, Bonnie/Chica/Foxy, múltiples enemigos, inventario/coleccionables/puertas/puzzles, guardado, HUD complejo, reinicio desde `NightGameOverScreen`, réplica pixel-perfecta de Game Over, optimización de mapa (salvo que el rendimiento real lo exija).
- **Riesgos conocidos documentados (§12):** Freddy sin animaciones; mapa sin validar dentro de LibGDX todavía; rendimiento real del mapa desconocido (1.3M triángulos — ver §2.3); colisiones del mapa requerirán ajuste iterativo; los assets pueden cambiar durante desarrollo (arquitectura ya preparada para ello); fidelidad tipográfica de `NightGameOverScreen` no confirmada (LibGDX no comparte sistema de fuentes con Swing).
- **Reglas de proceso (§14):** ninguna clase sin responsabilidad ya descrita en el documento; ningún asset por nombre en código Java; `AnimationSystem` debe soportar entidades sin animación desde su primer commit; el MVP se considera terminado solo cuando los 9 puntos son verificables en una sesión real; cualquier desviación del documento lo actualiza primero; **ninguna funcionalidad nueva empieza mientras exista un error crítico sin resolver en la iteración actual**.

### 2.3 Auditoría real de assets (Sketchfab) — datos verificados, no estimados

| Modelo | Triángulos | Licencia | Notas |
|---|---|---|---|
| Freddy | 74.5k | **"Free Standard"** (no CC — reglas propias: crédito obligatorio, sin recolorear, sin resubir, etiqueta NoAI) | **Validado en Blender por el usuario: importa bien, texturas OK, materiales OK, geometría limpia, Armature real, skinning correcto. NO TRAE ANIMACIONES (confirmado, Editor de Acciones vacío). Modelo piloto oficial.** |
| Bonnie | 193.4k | CC-BY 4.0 | Descripción original dice "(not my model)" — resubida. Etiquetas mezcladas sospechosas ("endoskeleton", "springtrap") — **verificar visualmente antes de confiar en que el archivo contiene solo a Bonnie**. Rig no confirmado. |
| Chica | 168.2k | CC-BY 4.0 | Sin mención de rig en ningún lugar de su página. |
| Foxy | 72.9k | CC-BY 4.0 | Modelo de 2017, sin descripción, sin mención de rig. |
| Pizzeria (mapa) | **1.3M triángulos**, 650.7k vértices | CC-BY 4.0 | **El asset más pesado con diferencia — necesitará división/optimización antes de integrarse completo.** |

**Ningún asset se considera definitivo** — decisión explícita del usuario: si cualquiera presenta problemas durante el desarrollo (polígonos, animación, licencia), se reemplaza sin dudar. La arquitectura ya está diseñada para tolerar esto.

### 2.4 Roadmap de 8 fases acordado (cada una con prototipo verificable)

1. Configuración del proyecto + pipeline de un solo modelo (Freddy).
2. Cámara FPS sobre piso de prueba.
3. Mapa cargado sin optimizar (validar que importa).
4. División/optimización del mapa.
5. Colisiones AABB.
6. Animación de Freddy (idle+caminar) — **bloqueado hasta conseguir o crear clips de animación, ya que el modelo no los trae**.
7. IA de persecución básica (`gdx-ai`, `IdleState`→`ChaseState`).
8. Comunicación con Swing + integración final.

### 2.5 Plan de inicio (más granular que el roadmap, para los primeros pasos concretos)

**Paso 1 — COMPLETADO 2026-07-31.** Proyecto generado (a mano, ver §2.1 sobre `gdx-liftoff`) con exactamente los parámetros previstos: Project Name `five_doors_escape` (originalmente `five-doors-escape`, renombrado el mismo día por consistencia de nomenclatura — ver §2.1), Package `com.fivedoorsescape`, Main Class `FiveDoorsEscapeGame`, solo Desktop/LWJGL3, Java 21. El módulo de escritorio se llama `lwjgl3` (confirmado, no `desktop`). Verificación real: `gradlew lwjgl3:run` compila y corre sin errores — confirmado tanto por Claude (vía consola) como manualmente por el usuario ejecutándolo en Eclipse.

**Paso 2 — COMPLETADO PARCIALMENTE 2026-07-31.** Se agregó `gdx-gltf` (versión 2.3.0, vía JitPack) y se verificó que Gradle la resuelve sin conflictos y que carga `Freddy.glb` correctamente. **`gdx-ai` todavía NO se agregó** — no hace falta hasta la fase de IA de persecución (roadmap §2.4 punto 7), fuera del alcance de la sesión que hizo esto.

Ver §2.6 para el detalle completo del pipeline de assets 3D validado en esta misma sesión.

### 2.6 Pipeline de assets 3D — estado tras la sesión 2026-07-30/31

**Auditoría real de los 5 assets Sketchfab** (Freddy, Bonnie, Chica, Foxy, Pizzeria) hecha parseando directamente cada `scene.gltf`, no de forma teórica. Hallazgos que reemplazan las estimaciones de §2.3: triángulos reales Freddy=74k/Bonnie=193k/Chica=168k/Foxy=73k/Pizzeria=1.285M (Pizzeria pesa más que los 4 personajes juntos). Chica tiene un `<skin>` técnicamente presente pero **solo 2 de 90 mallas realmente están skinneadas** (~2%) — no es un rig funcional pese a aparentarlo; si algún día se anima, sería casi un re-rig completo, no un retargeting simple. Foxy tiene 9 clips de animación ya incluidos en el archivo original (`ragdoll`, `ragdoll.001`...`.008`) — investigados en Blender y confirmados como **poses estáticas de 1 solo frame cada una** (snapshots de una simulación física de colapso, no animaciones reproducibles), útiles como mucho como poses de referencia para un futuro efecto, no como animación lista para usar.

**Pipeline de Blender aprobado (3 fases):**
- Fase 1 (Pizzeria) — CERRADA. Escala correcta, geometría íntegra, la mayoría de "materiales sin textura" resultaron ser colores planos intencionales. Sin cambios necesarios.
- Fase 2 (Freddy, único personaje con animación en el MVP) — CERRADA. Ver detalle abajo.
- Fase 3 (Bonnie/Chica/Foxy, solo estáticos, sin retargeting/animación/re-rig salvo bloqueo real del MVP) — EN CURSO.

**Reglas de proceso fijadas para todo trabajo de Blender de este proyecto** (detalle y motivos completos en la memoria de Claude, tipo `feedback`): conservar el asset original descargado sin modificar, trabajar solo sobre copias en `Blender_WIP`; no generar el `.glb` definitivo hasta que el asset esté completamente validado; no optimizar (texturas, geometría) sin medir primero dentro de LibGDX; validación obligatoria por reimportación en escena limpia tras cada export; excluir del export final todo armature/acción usado solo como andamiaje de retargeting.

**Freddy — retargeting completado:**
- **Incompatibilidad real encontrada:** Rokoko Studio Live 1.4.3 (addon de retargeting) es incompatible con Blender 5.x (usa la API clásica `action.fcurves`, eliminada en el nuevo sistema de acciones por capas). Solución: Blender 4.2.23 LTS portable instalado en paralelo, sin tocar la instalación principal de 5.1 — `C:\Users\dfarl\BlenderVersions\blender-4.2.23-windows-x64\blender.exe`.
- Animaciones Idle y Walk retargeteadas desde clips genéricos de Mixamo (16 huesos mapeados a mano del esqueleto `j_*` de Freddy; Freddy nunca se subió a Mixamo). Rig original 100% intacto (59 huesos, 59 deform, 0 constraints, verificado antes/después).
- **Entregable final:** `C:\Users\dfarl\Desktop\FDAF assets\3D Models\LibGDX_Ready\Freddy.glb` (~109 MB, texturas 4K sin reducir — pendiente medir rendimiento real en LibGDX antes de optimizar). Trabajo intermedio en `...\Blender_WIP\Freddy_WIP.blend`.
- Validado por reimportación en Blender y, ahora, **cargando de verdad dentro de LibGDX** (ver abajo) — pipeline Blender→glTF→gdx-gltf→LibGDX confirmado end-to-end.

**Proyecto LibGDX y validación de Freddy — ver §2.1/§2.5 para el estado del proyecto en sí.** Hallazgo técnico pendiente, no bloqueante para el MVP pero sí para cuando se conecte la persecución real de Freddy: **la animación `Freddy_Walk` se renderiza invertida (180°) dentro de gdx-gltf incluso con una rotación correctiva aplicada que sí corrige `Freddy_Idle` y la pose de reposo.** Causa raíz no aislada del todo — sospecha de inconsistencia de signo de quaternion en las keyframes de rotación de cadera retargeteadas específicamente para el ciclo de Walk. El ejemplo mínimo actual (`FiveDoorsEscapeGame.java`) reproduce `Freddy_Idle` en loop con cámara orbital; el bug de `Freddy_Walk` queda documentado en un comentario en el propio código. **Revisar antes de conectar la animación de persecución a la jugabilidad real (roadmap §2.4 punto 6/7).**

**Fase 3 — Bonnie/Chica/Foxy: exportados y validados en Blender el 2026-07-31.** Entregables en `...\LibGDX_Ready\{Bonnie,Chica,Foxy}.glb` (sin pérdida de datos vs. los `_WIP.blend`: Bonnie 41 mallas/18 materiales/62 huesos/0 acciones, Chica 90 mallas/25 materiales/67 huesos/0 acciones, Foxy 25 mallas/15 materiales/36 huesos/9 acciones `ragdoll.*` preservadas intactas sin usar). Bonnie usa la extensión `KHR_materials_clearcoat`, no soportada por gdx-gltf (degradación grácil esperada, no confirmada).

**[CERRADO 2026-07-31] Fase 3 cerrada — causa raíz y corrección universal encontradas para los 4 personajes.** El problema de orientación NO es exclusivo de Freddy — afecta a los 4 (Freddy, Bonnie, Chica, Foxy) por igual en gdx-gltf, incluso en la pose de reposo sin animación. Se probaron sistemáticamente las 4 rotaciones candidatas (identidad, 180°X, 180°Y, 180°Z) sobre `Scene.modelInstance.transform` para cada personaje, con capturas reales para cada una:

| Personaje | 180°X | 180°Y | 180°Z |
|---|---|---|---|
| Freddy | ✅ correcto | ❌ sigue invertido | ✅ correcto |
| Bonnie | ✅ correcto | ❌ sigue invertido | ❌ **sigue mal orientada** |
| Chica | ✅ correcto | ❌ sigue invertido | ✅ correcto |
| Foxy | ✅ correcto | ❌ sigue invertido | ✅ correcto |

**Conclusión: la rotación de 180° en el eje X es la única corrección universal que funciona para los 4 sin excepción** — 180°Z funciona para 3 de 4 pero falla específicamente en Bonnie, así que **180°X debe ser la corrección estándar a aplicar a todos**, no 180°Z (el que se documentó originalmente solo para Freddy). 180°Y nunca sirve (matemáticamente no puede corregir una inversión vertical, solo gira sobre el eje que ya está mal).

**Corrección de un hallazgo anterior (2026-07-31, sesión previa):** se había registrado que Foxy mostraba "una pose colapsada/deformada" distinta de un simple problema de orientación. **Eso era un error del propio arnés de prueba, no un problema real del asset** — la cámara automática quedó demasiado cerca del modelo (el bounding box local de Foxy es mucho más compacto que el de los otros tres). Con una distancia de cámara mayor, Foxy se ve perfectamente de pie con la misma corrección 180°X que los demás — no hay ningún problema de skin/bind pose específico de Foxy.

Los 4 personajes cargan sin excepciones ni warnings, con materiales/texturas correctos y `maxBones` coincidiendo exactamente con el rig (Freddy 59, Bonnie 62, Chica 67, Foxy 36). La corrección se aplica únicamente en código (`scene.modelInstance.transform.rotate(Vector3.X, 180f)`) — **no se modificó ningún asset original.** Bonnie sigue con la extensión `KHR_materials_clearcoat` no soportada por gdx-gltf (degradación grácil esperada, no confirmada) — no bloqueante.

> **⚠️ CORREGIDO 2026-08-02 (ver §2.14): esta conclusión de "180°X universal" era INCORRECTA.** Toda esta ronda de pruebas se validó con capturas de pantalla tomadas con `PixmapIO.writePNG(file, pixmap)` (2 argumentos), que internamente usa `flipY=false` y produce una imagen espejada verticalmente respecto a la pantalla real (confirmado leyendo el código fuente de `gdx-1.14.2-sources.jar`). Re-verificado con capturas corregidas (`flipY=true`): con `rotationXDegrees=180` los 4 personajes en realidad se renderizan **invertidos (cabeza abajo)** en la pantalla real. La corrección correcta es `rotationXDegrees=0` (sin rotación) para los 4 — ver §2.14 y memoria de Claude `project-libgdx-freddy-rotation-correction`. Ya aplicado en los 4 archivos JSON de entidad.

**Nota para el futuro:** el `FiveDoorsEscapeGame.java` comiteado (ejemplo mínimo de Freddy) sigue usando 180°Z, que funciona para Freddy — no hace falta cambiarlo, pero cuando se integre Bonnie/Chica/Foxy en la escena real (Fase 4+), usar 180°X para los 4 por consistencia y porque es la única que probadamente funciona sin excepción.

### 2.7 Fase 4 — arranque real, sesión 2026-08-02 (commit `0b99f7f` en `five_doors_escape`, no pusheado a origin todavía)

**Se cerró el hueco pendiente del roadmap Fase 3:** hasta esta sesión, Pizzeria nunca se había cargado dentro de LibGDX — solo se había validado en Blender. Se exportó `Pizzeria_WIP.blend` → `Pizzeria.glb` (headless, Blender 5.1, sin retargeting porque es un mapa estático) y se validó por reimportación (78 mallas, 48 materiales, bbox idéntico al original: 27.43×3.32×21.67, todo igual antes/después del export). Cargó dentro de LibGDX real y **el mapa mide exactamente lo mismo en gdx-gltf que en Blender** (mismo bbox, sin factores de escala ocultos) — confirma que la unidad del mapa es 1:1 entre Blender y gdx-gltf, a diferencia de los personajes (ver §2.6, escalas dispares entre sí).

**Hallazgo nuevo — bobinado invertido del mapa (distinto del problema de orientación de personajes de §2.6):** con backface culling normal, el mapa completo se renderizaba invisible (sin excepciones, sin warnings — cada cara simplemente se descartaba por culling). Confirmado con una cámara cenital y `getRenderables()` devolviendo 88 renderables reales (los 78 nodos + splits por el límite de índice de 16 bits) — el contenido sí estaba ahí, solo no se dibujaba. **Causa: el export `export_yup` de Blender invierte el orden de bobinado de los triángulos de Pizzeria respecto a lo que gdx-gltf espera.** Freddy no tiene este problema (pipeline de export distinto, ver §2.6). **Corrección aplicada — únicamente en código, en `LevelLoader.loadMapScene()`:** `material.set(IntAttribute.createCullFace(0))` sobre cada material del mapa (double-sided), no globalmente y no sobre Freddy. Ningún asset `.glb` fue modificado.

**Hallazgo nuevo — el área del comedor no tiene techo:** confirmado con una cámara cenital a 25 unidades de altura, se ve directo hasta las mesas/sillas sin ningún plano de techo intermedio. Consistente con un asset de exhibición de Sketchfab no pensado para recorrerse en primera persona. Esto explica por qué mirando en ciertas direcciones desde dentro del edificio la cámara FPS mostraba solo el skybox generado por `IBLBuilder` (no había ningún bug de renderizado — simplemente no hay techo que bloquee la vista hacia arriba en esa zona). No investigado más a fondo; no bloqueante para el MVP.

**Arquitectura real construida esta sesión (todo compila y corre, sin excepciones, verificado en ejecución real, no solo en teoría):**
- Estructura de paquetes completa del contrato: `io/` (`HandoffData`/`HandoffReader`, defaults si falta el archivo), `content/` (`EntityDefinition`/`MapDefinition`/`ContentRegistry`, JSON en `assets/content/{entities,maps}/`), `assets/AssetService` (envuelve `AssetManager` + `GLBAssetLoader` de gdx-gltf), `ecs/` (componentes `Transform/Model/Animation/AI/Player/Collision` vía gdx-ashley + `EntityFactory` + sistemas `AISystem/AnimationSystem/RenderSyncSystem`), `ai/states/` (`IdleState`/`ChaseState`/`CaughtState` vía gdx-ai, clases separadas con singleton `INSTANCE`, no un enum), `world/` (`LevelLoader` deriva colliders AABB de los nodos del mapa, `CollisionWorld` resuelve movimiento eje por eje), `camera/FirstPersonCameraController` (WASD + mouse-look, no mueve la posición directamente — expone `computeWasdDelta()` para que el llamador la resuelva contra `CollisionWorld`), `screens/` (`BootScreen`→`GameplayScreen`→`NightGameOverScreen`). `FiveDoorsEscapeGame` pasó de `ApplicationAdapter` a `Game`.
- Dependencias nuevas: `gdx-ai:1.8.2`, `com.badlogicgames.ashley:ashley:1.7.4` (ambas en Maven Central, resuelven limpio).
- Assets reorganizados a `assets/models/` (antes `Freddy.glb` estaba suelto en `assets/`); se copiaron también `Bonnie.glb`/`Chica.glb`/`Foxy.glb`/`Pizzeria.glb` con sus JSON de contenido — **pero Bonnie/Chica/Foxy NO se cargan ni aparecen en la escena real todavía**, están fuera de alcance del MVP a propósito (Architecture.md #2.2, lista de exclusiones). Los JSON ya existen para cuando toque integrarlos.
- `NightGameOverScreen`: fondo negro, "GAME OVER" blanco, mensaje bilingüe según `handoff.idioma` ("Presiona ESC para salir" / "Press ESC to quit"), única interacción es cerrar el juego — coincide con el alcance del MVP (Architecture.md #11.3).

**Colisión AABB manual (roadmap Fase 5):** implementación real, no placeholder — `LevelLoader.buildStaticColliders()` deriva un `BoundingBox` por cada uno de los 78 nodos del mapa (recorrido recursivo por si hay jerarquía), `CollisionWorld.resolveMovement()` resuelve el desplazamiento del jugador eje por eje (X luego Z) para poder deslizar contra paredes en vez de detenerse en seco. Confirmado en ejecución: "Colisionadores estaticos generados: 78" en cada corrida.

**IA de persecución (roadmap Fase 7):** confirmada funcionando en ejecución real, no solo compilando — con Freddy dentro del `rangoDeteccion` (8 unidades) desde el primer frame, `IdleState` transiciona a `ChaseState` y su posición avanza medible frame a frame hacia el jugador (log real: `freddy pos=(6.1,...)` → `(4.9,...)` en 30 frames). `CaughtState` (`rangoAtrape=0.6`) dispara `AIComponent.jugadorAtrapado=true`, que `GameplayScreen` sondea cada frame para transicionar a `NightGameOverScreen`.

**Pendiente de pulido visual, NO bloqueante:**
- Escala de Freddy (`assets/content/entities/freddy.json`, campo `scale`, `10.0`) es un valor de arranque razonable, no medido con precisión milimétrica, pero **confirmado visualmente correcto** durante el hardening de la sección 2.8 (Freddy se ve y se comporta como un personaje de tamaño normal, no gigante ni microscópico). `ModelInstance.calculateBoundingBox()` **no es confiable para medir el tamaño real de un personaje con skinning** — devolvió las mismas dimensiones diminutas sin importar el `scale` aplicado (0.097×0.023×0.0086 tanto en scale=10 como en scale=200). No usar ese método para depurar tamaño de personajes animados; confiar solo en capturas de pantalla o en jugar en vivo.
- La posición inicial del jugador (`playerStartX/Y/Z` = 1.1/1.6/2.0, `playerStartYawDegrees=90`) y la de Freddy (`freddyStartX/Z` = 4.0/2.0, ambos en `pizzeria.json`) fueron **verificadas programáticamente** (no solo a ojo) durante el hardening de la sección 2.8 — ninguna queda incrustada en un collider estático, y quedó demostrado en ejecución real que Freddy persigue y atrapa al jugador desde ahí sin quedarse atascado. Siguen siendo un punto de partida razonable dentro del comedor, no necesariamente "la oficina" conceptual del juego — libre de mover si hace falta para el diseño real de niveles.

**Verificación de proceso:** cada cambio se verificó con una corrida real de `gradlew lwjgl3:run` (no solo compilación) — incluyendo capturas de pantalla reales leídas por Claude para confirmar visualmente el mapa/cámara/culling, y logs en vivo para confirmar lógica de IA/colisión. `gradlew` en esta máquina requiere `JAVA_HOME` apuntando a `C:\Program Files\Java\jdk-21` explícito por comando (el JDK del PATH por defecto es 17) — no se agregó `org.gradle.java.home` a `gradle.properties` para no atar el proyecto a una ruta de máquina específica.

### 2.8 Hardening de la Fase 4, sesión 2026-08-02 (misma tarde, commit pendiente en el momento de escribir esto)

Pedido explícito del usuario: antes de hacer push, una ronda de estabilización (~1h) del vertical slice ya construido en §2.7 — no agregar funcionalidad nueva, sino encontrar y corregir bugs reales mediante ciclos de compilar/ejecutar/probar/corregir, con logs y pruebas reales, no solo revisión de código. Se encontraron y corrigieron **cuatro bugs reales**, todos confirmados con evidencia de ejecución (no solo inferidos leyendo código):

1. **Freddy atravesaba paredes al perseguir.** `ChaseState` movía `TransformComponent.position` directamente, sin pasar por `CollisionWorld` (a diferencia del jugador, que sí lo hacía desde el principio). Corregido dándole a `AIComponent` una referencia a `CollisionWorld` (asignada una vez en `GameplayScreen.show()`) y haciendo que `ChaseState.update()` resuelva su movimiento exactamente igual que el jugador.

2. **Bug critico de colisión — un solo collider del mapa bloqueaba TODO el interior del edificio.** Al darle colisión a Freddy (punto 1), quedó atascado sin moverse ni un centímetro durante 30+ segundos reales. Investigado con una búsqueda en cuadrícula (`CollisionWorld.overlapsStatic()`, expuesto como método público para esto) que no encontró **ningún** punto libre en un área de 6×6 unidades alrededor del jugador. Causa real: uno de los ~78 objetos del mapa (case probable: todas las paredes fusionadas en una sola malla) tiene un AABB de ~27×21 unidades — prácticamente la huella completa del edificio — porque una caja alineada a los ejes no puede representar una forma hueca. **Corrección real, no cosmética:** `LevelLoader` ahora calcula la huella total del mapa una vez y descarta cualquier nodo cuyo AABB cubra ≥70% del ancho Y de la profundidad del mapa simultáneamente (`DEGENERATE_FOOTPRINT_FRACTION`) — un filtro dirigido, no una desactivación general de colisión. También se agregó un filtro de tamaño mínimo (`COLLIDER_MIN_DIMENSION = 0.4`) que excluye decoraciones pequeñas (gorros de fiesta colgando, tazones) que de otro modo también bloqueaban el spawn. Colliders resultantes: 78 → 68 (filtro de tamaño) → 65 (más el filtro de huella completa).

3. **La IA nunca podía atrapar al jugador — bug de distancia 3D vs. altura.** Incluso después de arreglar 1 y 2, Freddy caminaba hasta pararse literalmente debajo del jugador (X/Z casi idénticos, confirmado con logs de posición en vivo) pero `jugadorAtrapado` nunca se activaba. Causa: `IdleState`/`ChaseState` usaban `Vector3.dst()` (distancia 3D completa) entre la posición de Freddy (Y=0, sus pies) y la del jugador (Y=1.6, altura de ojos) — la diferencia de altura por sí sola (1.6) ya superaba `rangoAtrape` (0.6), haciendo el atrape matemáticamente imposible sin importar la cercanía horizontal real. **Corregido usando distancia horizontal (solo X/Z) en ambos estados.** Verificado en ejecución real: tras el fix, Freddy atrapó al jugador en ~1 segundo real y la transición a `NightGameOverScreen` ocurrió sin excepciones.

4. **Fuga de recursos si la app se cierra durante la carga.** `BootScreen.dispose()` estaba vacío; si el usuario cerraba la ventana mientras `assets.update()` todavía no terminaba, ni `batch`/`font` ni el `AssetService` (con lo que ya se hubiera cargado a memoria de video) se liberaban nunca. Corregido: `dispose()` ahora libera todo explícitamente. (Nota: en el flujo normal `Game.setScreen()` no llama `dispose()` en la pantalla anterior, así que este método solo se ejecuta en el caso de cierre-durante-carga; no interfiere con el traspaso normal de `assets` hacia `GameplayScreen`.)

**Revisado y confirmado SIN bug** (para no repetir la investigación en el futuro): el manejo de `resize()` — `SceneManager.updateViewport()` sí actualiza `camera.viewportWidth/Height` y llama `camera.update(true)`, confirmado leyendo el código fuente de gdx-gltf, no hizo falta ningún cambio.

**Metodología de esta sesión, útil para retomar en el futuro:** el método `CollisionWorld.overlapsStatic(Vector3, Vector3)` (ya público, se usa también en `GameplayScreen.warnIfEmbedded()`, que quedó como advertencia permanente de bajo costo en el log al iniciar una partida) resultó ser una herramienta de diagnóstico mucho más rápida y confiable que las capturas de pantalla para validar posiciones de spawn — permite hacer búsquedas en cuadrícula en código y obtener una respuesta en segundos en vez de interpretar imágenes. Preferir este enfoque sobre iteración por captura de pantalla para cualquier problema de posicionamiento futuro.

**HandoffReader verificado con archivos reales** (no solo con el caso "archivo ausente" que ya se había probado en §2.7): un `handoff.json` válido (`{"idioma":"INGLES","vidasFinales":3}`) se lee correctamente; uno corrupto/incompleto cae a los valores por defecto sin excepción. Ambas pruebas se hicieron escribiendo el archivo real en `~/.fivedoorsatfreddys/handoff.json` (el mismo directorio que usa `PersistenciaJuego` del lado Swing) y confirmando por log — no quedó ningún archivo de prueba en el directorio al terminar.

### 2.9 Segunda ronda de hardening de la Fase 4, misma sesión 2026-08-02 (commit pendiente, push pendiente al escribir esto)

Pedido explícito del usuario: continuar la estabilización, sin push todavía, hasta no dejar nada importante pendiente dentro del alcance de la Fase 4. Se encontraron y corrigieron **dos bugs reales adicionales**, ambos mediante pruebas de comportamiento en ejecución real (un "autopiloto" temporal que sobreescribía el movimiento del jugador para simular WASD sin input humano — técnica reutilizable en sesiones futuras para probar colisión/movimiento sin depender de capturas de pantalla ni de un humano jugando).

1. **Bug crítico — las paredes exteriores del edificio dejaron de existir.** Al descartar en §2.8 el collider "degenerado" que bloqueaba todo el interior (el objeto de ~27×21 unidades, probablemente todas las paredes fusionadas en una sola malla), **también se perdió la única fuente de colisión perimetral real** — confirmado con el autopiloto: el jugador caminaba en línea recta y salía del mapa sin detenerse nunca (posición X > 300 cuando el mapa mide ~27 de ancho). **Corrección:** `LevelLoader.addBoundaryWalls()` agrega 4 cajas límite sintéticas (no derivadas de ningún nodo del mapa), alineadas con la huella total del edificio (`mapScene.modelInstance.calculateBoundingBox()`), como reemplazo funcional de la colisión perimetral perdida. Esto es distinto del filtro de §2.8 — ese descarta un collider inútil (hueco), este agrega el límite real que hacía falta.

2. **Bug crítico — "tunneling": el primer fix de paredes límite (grosor 0.5) tampoco detenía al jugador.** Investigado a fondo antes de asumir cualquier cosa: se probó `CollisionWorld.overlapsStatic()` de forma aislada con un punto fijo dentro de la pared límite (`overlapsStatic(15, 1.6, 2) = true`) — la colisión en sí funcionaba perfectamente. El bug real estaba en el *primer frame* de `GameplayScreen.render()` tras la carga de assets: como la carga toma varios segundos reales, `Gdx.graphics.getDeltaTime()` en ese primer frame refleja todo ese tiempo transcurrido, produciendo un salto de posición enorme en un solo paso — suficiente para atravesar de un salto el grosor de una pared delgada sin que ninguna posición intermedia registrara colisión ("tunneling", un problema clásico de detección de colisión discreta). **Corrección de raíz:** `GameplayScreen.MAX_FRAME_DELTA = 0.1f` clampa el `deltaTime` usado para mover al jugador cada frame; el mismo clamp se replicó en `ChaseState` (que lee `Gdx.graphics.getDeltaTime()` de forma independiente, no a través del `deltaTime` que Ashley ya pasa por `engine.update()`, así que necesitaba su propio clamp). Además, como defensa adicional (no como fix principal), el grosor de las paredes límite sintéticas subió de 0.5 a 1.5 unidades. **Verificado:** el jugador ahora se detiene en X=-12.29, prácticamente exacto al límite real del mapa (minX=-12.61 + radio del jugador 0.3).

**Pendiente, no bloqueante, documentado con honestidad:** la transición `ChaseState` → `IdleState` (Freddy pierde el rastro si el jugador se aleja más de `rangoDeteccion*1.5`) **no se verificó con una prueba de comportamiento aislada** — el mapa es demasiado pequeño para que el jugador se aleje 12+ unidades de Freddy antes de toparse con una pared límite y quedar atrapado, así que el intento de prueba solo confirmó que Freddy sigue persiguiendo y finalmente atrapa a un jugador inmóvil contra una pared (lo cual también es un resultado correcto, solo que no aísla esta rama específica). La lógica en sí usa exactamente el mismo cálculo de distancia horizontal ya verificado por la prueba de atrape exitosa, así que el riesgo se considera bajo, pero queda anotado en vez de darlo por probado sin serlo.

**Confirmado sin errores en la corrida final natural** (sin ningún autopiloto ni código de depuración): Freddy detecta y atrapa a un jugador inmóvil en ~2 segundos reales, sin excepciones, sin advertencias de spawn incrustado, 69 colisionadores (65 reales + 4 límites sintéticos), memoria del proceso consistente con haber llegado a `NightGameOverScreen` y liberado recursos correctamente.

### 2.10 Fase 5 — Rendimiento y pulido de posicionamiento, sesión 2026-08-02 (commit pendiente al escribir esto)

**Cerrada.** Objetivo: medir con datos reales si el mapa (1.3M triángulos) necesita optimización, y refinar las posiciones de spawn con evidencia real en vez de ajustar a ciegas.

**Medición de rendimiento (roadmap original punto 4 — "división/optimización del mapa"):** se desactivó temporalmente `vsync`/`foregroundFPS` en `Lwjgl3Launcher` (revertido inmediatamente después de medir) y se registró `Gdx.graphics.getFramesPerSecond()` en vivo con el mapa completo y Freddy cargados juntos. **Resultado: 240 FPS estables, sin techo artificial** — muy por encima de los 60 FPS con los que el juego corre en producción (`vsync=true`, `foregroundFPS=60`). **Decisión, con datos en mano y siguiendo la regla del proyecto de no optimizar sin medir: el mapa NO necesita división ni optimización.** El roadmap original marcaba esto como un paso separado (Fase 4 de 8 del plan original); con esta medición queda cerrado sin cambios de código — no hacía falta ninguno.

**Refinamiento de spawn, con evidencia real (capturas de pantalla + `CollisionWorld.overlapsStatic()`), no a ciegas:**
- El spawn del jugador se movió de `playerStartZ=2.0` (Fase 4, buena colisión pero vista de puro cielo/vacío — confirmado por captura, consistente con el área del comedor sin techo documentada en §2.7) a **`playerStartZ=1.5`**, que combina ambas cosas: sin advertencia de spawn incrustado y vista real de mesa/decoraciones/columna confirmada por captura. Se probó también revertir a `Z=0.73` (la vista buena original de la Fase 4) para comparar — seguía incrustado en una mesa real (no una decoración pequeña filtrable), así que se descartó a favor de `Z=1.5`.
- La posición de Freddy (`freddyStartX=4.0, freddyStartZ=2.0`, fijada en la Fase 4) no cambió — ya estaba confirmada libre de colisión.
- **Nota metodológica para el futuro:** una cámara de depuración con un ángulo/distancia fijo apuntando a Freddy volvió a mostrar solo cielo vacío durante esta sesión — **esto ya se investigó a fondo en la Fase 4 y se concluyó que es un artefacto del ángulo de la cámara de prueba, no un problema real** (Freddy renderiza correctamente, confirmado aislándolo del mapa). No relanzar esa investigación si vuelve a aparecer; es un patrón conocido, no un bug nuevo.

**Pendiente en el momento de escribir esto:** la transición `ChaseState → IdleState` seguía sin una prueba de comportamiento aislada (mismo motivo que en §2.9 — el mapa es chico para separarse 12+ unidades sin toparse con una pared antes). **Resuelto en §2.11** (ronda de validación posterior, mismo día) reduciendo temporalmente `rangoDeteccion` para poder probarla dentro del mapa real. El bug de `Freddy_Walk` invertido sigue sin tocarse (corresponde a la Fase 6 propuesta).

**Verificación final:** corrida limpia sin ningún código de depuración -- 69 colisionadores, sin excepciones, sin advertencias, Freddy atrapa al jugador y llega a `NightGameOverScreen` con disposición de recursos confirmada.

### 2.11 Ronda de validación adversarial de la Fase 5, misma sesión 2026-08-02 (sin cambios de código -- ver nota al final)

Pedido explícito del usuario: antes de empezar la Fase 6, someter todo lo construido a pruebas variadas intentando "romper" el juego -- movimiento, cámara, colisión, límites del mapa, persecución, cambios de estado de IA, animaciones, rendimiento, estabilidad, casos límite. Cada prueba se implementó como código temporal (variantes del "autopiloto" ya validado en sesiones previas), se compiló, se ejecutó, se verificó por logs, y se removió por completo antes de la siguiente -- **el diff final contra el último commit quedó vacío: no se encontró ningún bug.**

**Pruebas ejecutadas y resultado de cada una:**
1. **Las 4 paredes límite individualmente** (antes solo se había confirmado la oeste). Autopiloto cíclico por las 4 direcciones cardinales durante >7000 frames reales. Confirmadas las 4: Este ~14.5, Norte ~11.24, Sur ~-9.76 (recién probada por primera vez), Oeste ~-12.29 (ya sabida). Sin excepciones.
2. **Colisión diagonal y en esquinas.** Autopiloto moviéndose en diagonal hacia las 4 esquinas del mapa (192 muestras). El deslizamiento por eje (X se sigue moviendo mientras Z ya está bloqueado, o viceversa) se comportó exactamente como se espera de una resolución de colisión por eje -- sin trabarse, sin parpadeo, sin oscilación. Sin excepciones.
3. **Transición `ChaseState → IdleState` (pendiente desde la Fase 4/§2.9), finalmente aislada.** Con `rangoDeteccion` reducido temporalmente a 2 (para que el umbral quepa dentro del mapa real), un autopiloto que primero se acerca y después se aleja de Freddy confirmó por log el ciclo completo: `IdleState` → (acercamiento) → `ChaseState` → (alejamiento) → `IdleState` de nuevo, estable indefinidamente mientras el jugador se mantuvo lejos. **Punto pendiente cerrado.**
4. **Oscilación rápida y repetida Idle/Chase.** Autopiloto oscilando cerca del umbral de detección muchas veces seguidas (con `rangoAtrape` casi anulado para no terminar la prueba con una atrapada temprana). La máquina de estados manejó docenas de cambios de posición rápidos sin excepciones ni estados inconsistentes; en una de las corridas terminó en una atrapada legítima (Freddy sigue acercándose incluso durante la oscilación del jugador) sin ningún crash.
5. **Corrida extendida de estabilidad.** El mismo proceso de la prueba 3 se dejó correr ~5 minutos reales continuos (Freddy en `IdleState` estable, jugador inmóvil contra una pared). Memoria del proceso prácticamente idéntica al inicio y al final (2,921,104 KB → 2,921,828 KB, diferencia de ruido normal) -- **sin indicios de fuga de memoria.** Cero excepciones acumuladas.
6. **Verificación final natural** (sin ningún código de prueba): 69 colisionadores, sin excepciones, sin advertencias, flujo completo boot→gameplay→atrape→game over confirmado una vez más.

**Nota sobre el commit de esta sección:** como ninguna prueba encontró un bug real, no hubo ningún cambio de código que conservar -- todo el código temporal de instrumentación se agregó y se quitó por completo en cada ciclo, confirmado con `git diff` vacío contra el commit `37aa471`. Esta sección documenta el *proceso y la evidencia* de la validación, no un commit nuevo en `five_doors_escape`.

### 2.12 Fase 6 — investigación y cierre del bug `Freddy_Walk` invertido, sesión 2026-08-02 (sin cambios de código -- ver nota al final)

**Cerrada.** Único pendiente real de la Fase 6 (la integración de animación Idle/Walk vía `AnimationSystem` ya estaba funcionando desde la Fase 4/§2.7) era investigar el bug de `Freddy_Walk` documentado en §2.6 ("se renderiza invertida (180°) ... incluso con una rotación correctiva aplicada"). **Conclusión: el bug no es reproducible en el build actual -- la animación se ve correcta y con orientación normal dentro del juego real.** Se investigó en dos niveles, ambos con evidencia real (no solo lectura de código):

**Nivel 1 -- datos del asset (Blender y GLB exportado).** Se escribieron dos scripts de diagnóstico independientes que buscan la hipótesis original (inconsistencia de signo de quaternion / "double-cover" en los keyframes de cadera retargeteados) calculando el producto punto entre quaternions consecutivos de `j_pelvis_02`, `j_root_FK_Freddy_01` y `j_spine_03`:
- Uno corre dentro de Blender (`bpy`) sobre `Freddy_WIP.blend` directamente, evaluando las fcurves de ambas acciones (`Freddy_Idle`, `Freddy_Walk`).
- Otro parsea el binario `.glb` exportado (`Freddy.glb`) directamente con `struct`/`json`, sin Blender, replicando el mismo cálculo sobre los datos ya exportados que usa el juego.

**Ningún sign flip encontrado en ninguno de los dos** -- la hipótesis original queda descartada. Renders de referencia ya existentes de sesiones previas (`qa_walk_f8.png`, reimportación del GLB final `final_glb_walk_q1.png`) también muestran una pose de caminata normal y correcta tanto en Blender nativo como en el GLB final reimportado.

**Nivel 2 -- confirmación visual dentro del juego real (LibGDX, no Blender).** Se intentó repetidamente obtener una captura de pantalla mostrando a Freddy caminando durante una persecución real (`ChaseState`), con varios intentos fallidos antes de dar con un método confiable -- **documentado aquí como metodología reutilizable para pruebas visuales futuras en este proyecto:**
- La cámara normal del jugador (mouse-look) no sirve para pruebas headless sin input real: quedó con el pitch atascado mirando al techo desde el primer frame (probable delta de mouse espurio al capturar el cursor sin un mouse real presente), produciendo capturas idénticas frame tras frame sin importar la posición de Freddy.
- Una cámara de depuración explícita (`lookAt` apuntando a la posición real de Freddy) tampoco sirvió al principio: **el mapa real, en esta zona del edificio, tiene geometría visual angosta (columnas/paredes parciales) que no está representada en el `CollisionWorld` simplificado** -- la vista quedaba dominada por una pared/columna cercana aunque la posición de la cámara no estuviera técnicamente "incrustada" según `overlapsStatic()`. Confirmado que esto es oclusión visual real, no un bug de renderizado, reordenando el buffer de profundidad/color entre pasadas de render (sin cambio en el resultado) y finalmente **aislando la escena de Freddy sacando temporalmente `mapScene` del `SceneManager`** (`sceneManager.removeScene(mapScene)` antes de la captura, `addScene` de vuelta después) -- con la geometría del mapa fuera de en medio, Freddy se ve con claridad.
- Con Freddy aislado y una cámara ampliada a suficiente distancia, se capturaron 3 poses distintas del ciclo de caminata en una persecución real (pierna extendida atrás/adelante, paso cruzado, pose casi neutra) -- **las tres muestran una figura completamente vertical, con pies orientados hacia adelante, sin ninguna inversión de 180° ni en el torso ni en las piernas.** Coincide exactamente con lo que ya mostraban los renders de Blender del Nivel 1.

**Conclusión de la Fase 6:** el bug reportado originalmente en §2.6 durante el ejemplo mínimo aislado (`FiveDoorsEscapeGame.java`, cámara orbital) no se reproduce en la integración real del vertical slice (`GameplayScreen`, `ChaseState`, `AnimationSystem`). No se determinó con certeza absoluta si el bug original era un artefacto específico de ese arnés de prueba aislado y ya obsoleto, o si algún cambio posterior (p.ej. la corrección universal de orientación 180°X/180°Z de §2.6, o la integración real vía ECS) ya lo resolvió como efecto colateral -- no se investigó más a fondo porque **no hay ningún síntoma real que perseguir**: la animación funciona correctamente en el juego tal como está hoy. No se modificó `Freddy.glb` ni ningún otro asset.

**Nota sobre el commit de esta sección:** igual que en §2.11, la investigación no encontró ningún bug que corregir, así que no hubo cambios de código que conservar -- toda la instrumentación temporal (cámara de diagnóstico, capturas periódicas) se agregó y se quitó por completo, confirmado con `git diff` vacío contra el último commit de `five_doors_escape`. El repositorio ya estaba limpio y sincronizado con `origin/master` desde el cierre de la Fase 5 (§2.11) -- no hizo falta un commit ni push nuevo para esta fase.

> **⚠️ CORREGIDO 2026-08-02 (ver §2.14): la conclusión "el bug no es reproducible" de esta sección era INCORRECTA.** Las capturas usadas en el Nivel 2 (confirmación visual dentro del juego real) se guardaron con `PixmapIO.writePNG(file, pixmap)` de 2 argumentos, que espeja verticalmente la imagen respecto a la pantalla real (mismo bug documentado en la corrección de §2.6). Freddy SÍ se renderizaba invertido en la pantalla real en el momento de esta investigación -- las capturas, al estar también espejadas, cancelaban el error y lo hacían *parecer* correcto. Además, el bug **no era específico de `Freddy_Walk`**: afectaba también a `Freddy_Idle` por igual (era la rotación estática `rotationXDegrees=180` de §2.6 la que estaba mal, no algo particular de la animación de caminata). Causa raíz real y corrección en §2.14.

### 2.13 Fase 7 (parte 1) — lanzamiento Swing → LibGDX, sesión 2026-08-02 (cambios en `FiveDoorsAtFreddys`, sin commitear todavía)

**Diseño completo pretendido para este flujo, dado por el usuario en esta sesión:** ver memoria de Claude `project-libgdx-office-spawn-exit-design` -- video de victoria → glitch (sin diseñar todavía) → Five Doors Escape en pantalla completa → jugador en "la oficina" → recorrer la pizzería hasta una salida mientras Freddy persigue → Game Over o victoria del Escape, ambos con botón/mensaje de salida que regresa al menú principal de Swing → además, un botón discreto de salida manual (esquina superior izquierda) disponible en cualquier momento del Escape, también con retorno al menú de Swing. **Esta sesión implementó solo la primera pieza concreta y ya bien especificada por Architecture.md §7: el lanzamiento en sí (escribir el traspaso + abrir el proceso LibGDX + volver al menú cuando ese proceso termina).** El resto (glitch, pantalla completa, botón manual, condición de salida/victoria dentro de la pizzería, oficina real como spawn) queda pendiente para fases futuras -- instrucción explícita del usuario de no implementarlo todo de una vez.

**Archivos nuevos/modificados en `FiveDoorsAtFreddys` (Proyecto 1, tratado normalmente como congelado -- este cambio es la excepción explícitamente pedida por el usuario):**
- `src/com/fdaf/util/LanzadorEscape.java` (nuevo) -- escribe `~/.fivedoorsatfreddys/handoff.json` (mismo formato JSON que espera `HandoffReader` del lado LibGDX: `{"idioma":"ESPANOL"|"INGLES","vidasFinales":N}`) y lanza el proceso de Five Doors Escape sin bloquear el EDT (hilo `Thread` propio + `Process.waitFor()` + callback vía `SwingUtilities.invokeLater`).
- `ControllerInterfaz.alGanar()` -- ahora captura `eraNoche5` (comparando `PreferenciasJuego.nocheActual` contra `NOCHE_5` **antes** de la mutación que avanza el progreso, porque `alGanar()` se llama al ganar cualquier noche, no solo la última). Si `eraNoche5`, tras el video de victoria se llama a `iniciarTransicionAEscape(...)` en vez de la Etapa 2 normal (fondo win.png + botones "Volver a jugar"/"Salir del juego") -- para noches 1-4 el flujo original queda intacto, sin cambios de comportamiento.
- `iniciarTransicionAEscape(...)` (nuevo, en `ControllerInterfaz`) -- muestra un mensaje de carga simple y bilingüe ("Cargando Five Doors Escape..."/"Loading Five Doors Escape...") sobre el fondo de victoria existente mientras el proceso arranca en segundo plano, **placeholder honesto** hasta que se diseñe el glitch real. `vistaPrincipal` (el `JFrame`, `setUndecorated(true)` + `MAXIMIZED_BOTH`) se deja visible debajo -- no hace falta ocultarlo porque la ventana de LibGDX (hoy no-fullscreen) aparece encima y le roba el foco de todas formas; cuando el proceso termina, `vistaPrincipal.toFront()` + `requestFocus()` lo traen de vuelta.
- `PnlWin.java` -- agregado `lblCargando` (JLabel centrado, oculto por defecto, mismo patrón que `lblFlecha`) para el mensaje de carga de arriba.

**Mecanismo de retorno al menú -- decisión de diseño para esta fase, no confirmada palabra por palabra por el usuario pero consistente con lo que pidió:** dado que los dos proyectos son procesos completamente independientes (Architecture.md), la forma más simple de que "todo camino de salida de Escape vuelva al menú de Swing" es que Swing simplemente espere (`Process.waitFor()`) a que el proceso hijo termine, sin importar la razón. Esto significa que **el lado LibGDX no necesita ningún canal de comunicación de salida nuevo** -- basta con que Game Over (ESC), el futuro botón manual, y la futura victoria del Escape cierren el proceso limpiamente (p.ej. `Gdx.app.exit()`), y Swing lo detecta automáticamente. Documentado también en la memoria de Claude para que las fases futuras de LibGDX lo respeten.

**Verificación real, no solo revisión de código:**
- Compilación completa del proyecto Swing con `javac` (JDK 1.8.0_271, el mismo JRE container que usa el `.classpath` de Eclipse, con JavaFX incluido) -- sin errores, un único warning preexistente y no relacionado (`Arbol.java`, unchecked operations).
- **Prueba de integración real con un harness standalone** (`TestLanzadorEscape.java`, temporal, fuera del repo): llama a `LanzadorEscape.lanzar(...)` directamente desde el directorio del proyecto Swing (para que la resolución de ruta relativa `user.dir` sea idéntica a como correría en producción) y observa el resultado.
  - **Primer intento falló:** `cmd /c gradlew.bat lwjgl3:run` con `pb.directory(carpetaEscape)` no encontraba `gradlew.bat` ("no se reconoce como un comando interno o externo") pese a que el directorio de trabajo del proceso hijo sí apuntaba a la carpeta correcta. **Corregido** resolviendo la ruta absoluta a `gradlew.bat` explícitamente (`new File(carpetaEscape, "gradlew.bat").getAbsolutePath()`) en vez de confiar en que `cmd` lo busque solo en su directorio de trabajo.
  - **Tras la corrección, verificado end-to-end:** `handoff.json` se escribió con el contenido exacto esperado (`{"idioma":"INGLES","vidasFinales":3}`), el proceso hijo arrancó Gradle real, compiló y llegó a `GameplayScreen`/`Colisionadores estaticos generados: 69` -- el mismo punto de arranque limpio confirmado toda la sesión anterior.
  - **Verificado también el camino de retorno:** matando el proceso real del juego (PID de mayor memoria entre los `java.exe` activos, mismo patrón usado toda la sesión) en vez de dejarlo salir solo, Gradle propagó la falla ("Process ... finished with non-zero exit value 1"), y **`Process.waitFor()` del lado Swing se desbloqueó correctamente e invocó el callback** ("[TEST] Callback de terminado invocado.") -- confirma que el mecanismo de espera funciona incluso ante una salida anómala del proceso hijo, no solo ante un `Gdx.app.exit()` limpio.
  - Todos los archivos de prueba (`TestLanzadorEscape.java`/`.class`, clases compiladas temporales) y el `handoff.json` generado por la prueba se eliminaron al terminar -- no quedó ningún rastro de la prueba en `~/.fivedoorsatfreddys/` ni en el repo.

**No commiteado todavía** -- cambios presentes en el working tree de `FiveDoorsAtFreddys` (`ControllerInterfaz.java`, `PnlWin.java` modificados, `LanzadorEscape.java` nuevo), consistente con el patrón ya establecido en este proyecto de no commitear el lado Swing sin pedido explícito del usuario.

**Gotcha para futuras sesiones:** `gradlew.bat lwjgl3:run` deja un **Gradle Daemon corriendo en segundo plano** después de que el juego termina (proceso `java.exe` separado, ~280-290 MB, normal y esperado -- acelera la siguiente ejecución). No es una fuga causada por `LanzadorEscape` ni hace falta matarlo explícitamente.

**Continuación, misma sesión -- pantalla completa y botón de salida manual (cambios en `five_doors_escape`):**
- `Lwjgl3Launcher.java`: `config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode())` en vez de `setWindowedMode(960, 720)` -- cumple el requisito de la memoria de diseño ("mientras el Escape esté abierto, pantalla completa"). Verificado en ejecución real: resolución reportada 1920×1080 (antes 960×720).
- `GameplayScreen.manejarBotonSalir()` (nuevo): botón discreto esquina superior izquierda, dibujado cada frame con `ShapeRenderer`+`BitmapFont` (mismo patrón liviano que `NightGameOverScreen`, sin Scene2D/Skin -- no se justifica una abstracción nueva para un solo botón). El clic solo se evalúa con `!Gdx.input.isCursorCatched()` (ESC ya libera/recaptura el cursor) -- con el cursor capturado para mouse-look, su posición no representa un punto real que el jugador esté apuntando. Al hacer clic dentro del área, llama `Gdx.app.exit()` -- el mismo mecanismo de salida limpia ya verificado en §2.13 que `LanzadorEscape` detecta correctamente para volver al menú de Swing.

### 2.14 Hallazgo crítico y corrección de rotación de personajes, misma sesión 2026-08-02

**Durante la verificación visual del botón de salida (arriba), se descubrió que TODA la metodología de captura de pantalla usada en sesiones anteriores para diagnósticos visuales estaba espejando verticalmente las imágenes respecto a lo que realmente se renderiza en pantalla.** Causa raíz confirmada leyendo el código fuente real de `gdx-1.14.2-sources.jar`: `PixmapIO.writePNG(file, pixmap)` (el overload de 2 argumentos, usado en todas las capturas de esta sesión y de la Fase 6) delega internamente a `writePNG(file, pixmap, compression, flipY=false)` -- con `flipY=false`, los datos crudos de `glReadPixels` (orden de filas de abajo hacia arriba, convención nativa de OpenGL) se escriben directamente en el PNG (que espera filas de arriba hacia abajo), produciendo una imagen verticalmente invertida respecto a la pantalla real. El overload de 4 argumentos con `flipY=true` sí corrige esto. Documentado como memoria de tipo `feedback` (`feedback-libgdx-screenshot-flipy`) para no repetir este error en ningún proyecto LibGDX futuro.

**Se detectó por accidente:** el botón de salida (posicionado con la fórmula Y-up estándar de `SpriteBatch`/`ShapeRenderer`, correcta desde el principio) aparecía en la esquina INFERIOR izquierda en vez de la superior, y su texto se veía completamente ilegible/garabateado -- no simplemente "al revés pero reconocible", sino genuinamente irreconocible, consistente con un espejado vertical completo del bloque de texto. Un primer intento de "corrección" (invertir manualmente la coordenada Y del botón) hizo que la captura se viera correcta -- pero eso solo demostraba que la captura, no el juego real, era lo que estaba mal. Se revirtió ese cambio equivocado antes de identificar la causa raíz real.

**Impacto en conclusiones previas -- corregido tanto en §2.6 como en §2.12 (ver notas ⚠️ agregadas en esas secciones):** con una captura corregida (`flipY=true`), se re-verificó a Freddy dentro del juego real y **efectivamente se renderiza invertido (cabeza abajo, pies arriba)** -- tanto en `Freddy_Idle` como en `Freddy_Walk` (capturado explícitamente forzando cada clip por separado vía `AnimationController.setAnimation(...)` directamente, evitando que `ChaseState` sobreescribiera el override en cada frame). Esto demuestra que **el bug nunca fue específico de la animación de caminata** -- era la rotación estática `rotationXDegrees=180` aplicada a los 4 personajes en §2.6 (también validada con la misma captura defectuosa) la que estaba mal desde el principio.

**Corrección aplicada y verificada:** `rotationXDegrees` cambiado de `180.0` a `0.0` en `assets/content/entities/freddy.json` -- re-verificado con captura corregida: Freddy ahora se ve correctamente de pie, con sombrero/micrófono visibles, marcha normal. La misma corrección se aplicó por consistencia a `bonnie.json`/`chica.json`/`foxy.json` (mismo valor incorrecto de `rotationXDegrees=180` heredado de la misma prueba defectuosa de §2.6), aunque esos 3 personajes **no están cargados en la escena real todavía** (fuera del alcance del MVP) y por lo tanto esa parte de la corrección no se re-verificó visualmente en vivo -- solo se aplicó por la misma lógica que ya funcionó para Freddy. Revisar visualmente cuando se integren en una fase futura.

**Verificación final post-corrección:** corrida limpia sin ningún código de diagnóstico -- 69 colisionadores, sin excepciones, carga y arranca normalmente con la rotación corregida. `git diff --stat` en `five_doors_escape` muestra exactamente los cambios esperados: 4 JSON de entidad (rotación), `GameplayScreen.java` (botón de salida, sin restos de instrumentación temporal), `Lwjgl3Launcher.java` (pantalla completa) -- ningún archivo de prueba ni captura quedó en el repo.

**Commiteado y pusheado a `five_doors_escape`** (commit `c52ba4e`): corrección de rotación de los 4 personajes, pantalla completa y botón de salida manual. Tareas #37/#38 (corregir animación / verificar en el juego real), marcadas como innecesarias al cerrar la Fase 6 originalmente, quedan reabiertas y completadas con esta corrección.

### 2.15 Bloque de integración Swing↔LibGDX completo: oficina, salida y validación final, sesión 2026-08-02

**Pedido explícito del usuario:** terminar todo el bloque de integración (no una fase numerada específica) antes de considerarlo cerrado -- los 3 pendientes identificados eran la condición de victoria del Escape, el spawn narrativo del jugador en "la oficina", y la transición completa Noche 5→Escape (con el glitch todavía como placeholder si no correspondía implementarlo ya). Los 3 quedan resueltos en esta sesión.

**Hallazgo clave -- el mapa Pizzeria SÍ tiene una oficina real, no hubo que inventarla.** Parseando los nombres de nodo del glTF directamente (`Pizzeria.glb`), se encontró un cuarto distintivo con: un escritorio (`escritorio_50`), dos puertas nombradas literalmente **"puerta Izquierda"/"puerta Derecha"** (coincide exactamente con la mecánica de puertas izq/der del juego Swing), un TV, una pizarra, dibujos de niños enmarcados, un póster de FNAF, un ventilador y una telaraña -- inconfundiblemente la oficina de seguridad del modelo. Ubicado en x≈[-2, 2], z≈[7.1, 9.5] en coordenadas locales del glTF.

**Gotcha de coordenadas:** las traducciones locales de los nodos en el JSON del glTF **no son la posición mundial real** -- el nodo raíz "Sketchfab_model" tiene una escala de ~0.926 aplicada, y hay varios niveles de jerarquía padre/hijo de por medio. Resolver la posición real requiere componer toda la cadena de transformadas (`node.globalTransform` multiplicado por el transform de la instancia del mapa), no leer la traducción cruda del JSON. Se hizo con un diagnóstico temporal usando `mapScene.modelInstance.getNode(nombre, true)` + `globalTransform.getTranslation(...)` -- confirmado contra los propios colliders generados por `LevelLoader` (que sí calculan esto correctamente). Coordenadas reales resultantes: escritorio (-0.13, 0.60, 7.55), puerta Izquierda (-2.14, 0.88, 8.76), puerta Derecha (1.91, 0.88, 8.76).

**Spawn del jugador reubicado a la oficina:** `playerStartX=0.0, playerStartZ=8.6, playerStartYawDegrees=0` (mirando hacia +Z, hacia las puertas) en `pizzeria.json`. El punto inicial elegido (cerca del escritorio) estaba **incrustado** en un collider real -- la oficina está densamente decorada (TV, pizarra, dibujos, partes del escritorio) y varios de esos objetos, aunque son decoraciones montadas en la pared, generan colliders AABB que cubren casi toda la altura de un jugador parado. Resuelto con una búsqueda en cuadrícula (técnica ya validada en la Fase 4) que encontró que la franja z≥8.6 (el umbral de la puerta hacia afuera) está libre.

**Condición de victoria del Escape (`EscapeVictoryScreen`, nueva clase, simétrica a `NightGameOverScreen`):** un punto de salida (`exitX/exitZ/exitRadius` en `MapDefinition`/`pizzeria.json`) que `GameplayScreen.render()` chequea por distancia horizontal cada frame -- al entrar en el radio, transiciona a la pantalla de victoria ("ESCAPASTE"/"YOU ESCAPED", ESC para salir, mismo mecanismo `Gdx.app.exit()` ya verificado que `LanzadorEscape` detecta correctamente del lado Swing). Expande el MVP original de Architecture.md (que excluía explícitamente condición de victoria) -- expansión ya aprobada por el usuario.

**Metodología nueva, reutilizable para futuras fases -- mapa de conectividad completo del edificio:** en vez de adivinar si una ruta oficina→salida era recorrible, se generó un mapa ASCII completo del edificio (`CollisionWorld.overlapsStatic()` en una cuadrícula de 1 unidad, X de -13 a 15, Z de -10 a 12) en un diagnóstico temporal al iniciar la partida. Esto reveló la topología real: la oficina conecta a un pasillo/salón abierto que abarca casi todo el ancho del edificio en z≈9-11, con un "cuello de botella" angosto (franja de ~3 unidades en X) para bajar hacia el sur alrededor de z≈-4 a -6, abriéndose de nuevo en una zona amplia en z≈-7 a -9. **Mucho más rápido y confiable que iterar por capturas de pantalla para entender la disposición de un mapa nuevo -- preferir este enfoque en el futuro sobre exploración manual.**

**Salida final reubicada por evidencia real, no a ciegas:** el candidato original (`puerta_41`, un prop de puerta real en la esquina suroeste) resultó estar **dentro de la zona bloqueada** por el propio collider de la puerta -- un punto de victoria ahí nunca sería alcanzable por el cuerpo colisionable del jugador. Reubicado a `exitX=-7.5, exitZ=-7.5` (en el área abierta justo al lado, verificada libre por el mapa de conectividad) -- conserva la intención narrativa ("salida trasera") sin quedar dentro de geometría sólida.

**Verificación real de extremo a extremo, con autopiloto de puntos de paso** (no jugado a mano -- no hay forma de automatizar la interacción con la ventana LWJGL3 en este entorno): un autopiloto temporal movió al jugador desde el spawn de la oficina a través de 4 puntos de paso (salir por la puerta → cruzar el pasillo norte → bajar por el cuello de botella → llegar a la salida), confirmado por log alcanzando el punto de salida real y disparando la transición a `EscapeVictoryScreen` sin excepciones. Se dejó corriendo ~2 minutos adicionales sobre la pantalla de victoria (memoria de proceso estable, ~956 MB, sin crecimiento) para confirmar que no hay ningún problema de estabilidad en esa pantalla nueva. Todo el código de autopiloto/diagnóstico (búsqueda en cuadrícula, mapa de conectividad, resolución de nodos por nombre) se agregó y se quitó por completo -- confirmado con `git diff` limpio, solo quedan los cambios permanentes: `MapDefinition.java` (+exitX/exitZ/exitRadius), `pizzeria.json` (spawn+salida actualizados), `GameplayScreen.java` (chequeo de victoria), `EscapeVictoryScreen.java` (nueva).

**Con esto, el bloque completo de integración Swing↔LibGDX queda funcionalmente terminado:** video de victoria → mensaje de carga (placeholder del glitch, sin diseñar todavía por pedido explícito del usuario) → lanzamiento de Escape en pantalla completa → jugador aparece en la oficina real del mapa → recorre el edificio hasta la salida real (o lo atrapa Freddy) → pantalla de fin correspondiente → ESC cierra el proceso → Swing lo detecta y vuelve al menú principal. El único pendiente explícitamente diferido es el efecto visual real del glitch (§2.13, ver memoria de Claude `project-libgdx-office-spawn-exit-design`), que el usuario ya aceptó dejar como placeholder por ahora.

> **⚠️ CORREGIDO 2026-08-02 (ver §2.16): "funcionalmente terminado" era una afirmación prematura.** La verificación de esta sección solo probó el camino de ESCAPE (oficina→salida). El camino de ATRAPADA (Freddy alcanzando al jugador) nunca se probó explícitamente con las posiciones nuevas -- y de hecho estaba completamente roto: Freddy quedaba atascado para siempre contra el marco de la puerta de la oficina. Encontrado y corregido en una auditoría crítica inmediatamente después. Lección concreta: verificar un camino no implica que el otro funcione, especialmente cuando ambos comparten el mismo cuello de botella geométrico (la única puerta de la oficina).

### 2.16 Auditoría crítica del proyecto completo, sesión 2026-08-02 (mismo día, después de cerrar el bloque de integración)

**Pedido explícito del usuario:** no avanzar a una fase nueva del roadmap solo por avanzar -- primero una revisión crítica real del estado del proyecto, contrastando la documentación contra el código y pruebas reales, no una revisión de "todo está bien". Corregir en el momento cualquier problema que perteneciera al bloque recién cerrado; documentar (sin corregir todavía) cualquier hallazgo fuera de ese alcance.

**Hallazgo crítico #1 -- Freddy no podía atrapar a un jugador en la oficina (bug real, pertenece al bloque recién cerrado, CORREGIDO):** nunca se había probado explícitamente el camino de "Freddy atrapa al jugador" con las posiciones nuevas de la oficina (§2.15 solo probó el escape). Al probarlo: Freddy, arrancando desde su posición original `(4.0, 2.0)` (heredada de antes del rediseño de la oficina), avanzaba en línea recta hacia el jugador y quedaba **congelado para siempre** contra el marco de la puerta de la oficina -- un eje (X) se bloqueaba permanentemente mientras el otro (Z) seguía "avanzando" sin cerrar nunca la distancia real, una aproximación asintótica que nunca termina. Confirmado con logs de posición en vivo mostrando el valor de X exactamente congelado en un número que coincide con el borde del collider del marco de la puerta más el semi-ancho de colisión de Freddy.

Dos intentos de corrección, solo el segundo funcionó -- ver memoria de Claude `feedback-libgdx-ai-stuck-on-corners` para el detalle completo:
1. Reubicar el spawn de Freddy a varias coordenadas distintas por matemática de línea recta -- cada intento resolvía el atasco en un punto pero producía un atasco *distinto* en otro (una vez incluso convertido en oscilación por un empujón perpendicular mal orientado), porque el único acceso real a la oficina (el hueco entre los dos marcos de puerta) es angosto y cualquier aproximación en línea recta desde un ángulo no centrado tiende a rozar un marco.
2. **Corrección real y general, no otra coordenada mágica:** se agregó detección de atasco a `ChaseState` (muestrea la posición cada 0.3s; si el avance neto es menor a 0.15 unidades, acumula tiempo atascado) y, al superar 0.6s atascado, cambia el movimiento a **avanzar en línea recta por el eje horizontal (X o Z) que tenga mayor diferencia respecto al objetivo**, ignorando el otro eje hasta liberarse -- esto es lo que realmente hace falta para deslizarse hacia el centro de un hueco angosto. Un primer intento con un empujón perpendicular genérico (rotar la dirección 90°) **empeoró el problema** (causó oscilación en vez de resolver el atasco) porque el sentido de rotación elegido no es necesariamente el correcto para toda geometría -- revertido antes de llegar a la solución real.

`freddyStartX/Z` también se reubicó de `(4.0, 2.0)` a `(6.0, 10.0)` (al este de la oficina, con línea de aproximación limpia a través de la puerta) -- verificado que **ambos** escenarios funcionan tras la corrección: un jugador que se queda en la oficina termina atrapado (confirmado por log, aunque con la salvedad de que el "jugador quieto" en las pruebas headless deriva ligeramente por un artefacto ya conocido de este entorno de pruebas -- ver más abajo), y un jugador que ejecuta la ruta de escape real sigue llegando a la salida sin ser interceptado.

**Hallazgo menor -- artefacto de metodología de prueba, no un bug del juego:** en pruebas headless sin mouse/teclado real, el jugador puede derivar lentamente en vez de quedarse perfectamente quieto (ya documentado en sesiones anteriores que el cursor capturado produce deltas de mouse espurios en este entorno). No afecta a un jugador real con entrada real -- solo hace que las pruebas "jugador inmóvil" en este entorno no sean 100% puras. No se investigó más a fondo porque no cambia la validez de los hallazgos (el atasco de Freddy era real y reproducible independientemente de esto).

**Hallazgo #2 -- bug de regresión de progreso en Noche Personalizada (Proyecto 1, NO corregido, fuera del alcance de este bloque):** ver memoria de Claude `project-swing-custom-night-progress-regression`. `PreferenciasJuego.nocheActual` se usa a la vez como "progreso más alto alcanzado" y como "noche actualmente seleccionada en Noche Personalizada" -- rejugar una noche anterior tras haber avanzado más regresiona el indicador de progreso. Pre-existente, no introducido por la integración con Escape (aunque se descubrió auditando `alGanar()` por ese motivo). Queda documentado para que el usuario decida si/cuándo corregirlo -- Proyecto 1 se trata como congelado salvo instrucción explícita.

**Estado real del roadmap, contrastado contra el código (no solo contra la documentación):**
- Items 1-9 del MVP original (Architecture.md §2.2): **verificados de nuevo, siguen sólidos.** Carga de mapa/Freddy, cámara FPS, colisión, IA Idle→Chase→Caught, transición a Game Over -- todos re-confirmados funcionando en esta misma sesión de auditoría (no solo se asumió que seguían bien desde sesiones anteriores).
- Condición de victoria, spawn narrativo, integración Swing→LibGDX (expansiones post-MVP aprobadas): **implementadas y ahora sí verdaderamente verificadas en ambos sentidos** (escape Y atrapada), tras corregir el hallazgo #1.
- Animación de Freddy: **cerrada correctamente** (Fase 6) -- la corrección de rotación de §2.14 fue la causa real, no un problema de la animación en sí.
- Bonnie/Chica/Foxy: assets listos (`.glb` exportados, JSON con la rotación ya corregida) pero **su integración real a la escena nunca se probó** -- ni colisión, ni escala en el motor real (solo Freddy fue validado en vivo), ni si su geometría/rig tiene problemas propios no descubiertos. Riesgo real para cuando se retomen, no solo trabajo pendiente.
- Glitch visual de transición: placeholder deliberado, sin diseño técnico iniciado.

**Deuda técnica confirmada vigente (re-verificada contra el código real, no asumida de la documentación):**
- Proyecto 1: `Sonido.java` nunca llama `clip.close()` (confirmado, solo `.stop()`); `ControllerMenu.opciones()` re-agrega listeners a `btnAtras`/`btnSalir` cada visita sin quitar los anteriores (confirmado leyendo el código); instrumentación temporal `[MEMORIA]`/`contadorLlamadas` en `CargarGifs.java` y `[AMBIENTAL]` en `SonidosAmbientalesAleatorios.java` siguen sin retirarse (confirmado, ambas líneas siguen presentes).
- Proyecto 2: los filtros heurísticos de `LevelLoader` (`COLLIDER_MIN_DIMENSION=0.4`, `DEGENERATE_FOOTPRINT_FRACTION=0.7`) son ajustes empíricos para el mapa Pizzeria actual -- si el mapa cambiara (optimización futura, reemplazo de asset), estos umbrales tendrían que revalidarse, no asumirse válidos. La IA de persecución sigue sin pathfinding real (la corrección de esta sesión es una heurística de rescate, no una solución general -- puede fallar de nuevo ante geometría distinta, aunque ahora al menos no se congela silenciosamente sin ningún mecanismo de recuperación).

**Riesgos identificados para las siguientes fases:**
- Integrar Bonnie/Chica/Foxy a la escena real es trabajo no trivial y no probado -- Architecture.md ya lo señalaba como riesgo conocido, sigue vigente.
- Cualquier cambio futuro de mapa/spawn debe re-probar explícitamente el camino de atrapada, no solo el de escape (o viceversa) -- quedó demostrado que verificar uno no garantiza el otro cuando comparten un cuello de botella geométrico.
- El glitch visual es una pieza de diseño técnico completa todavía sin iniciar -- no hay ninguna decisión tomada sobre su implementación.
- La regresión de progreso en Noche Personalizada (hallazgo #2) es un bug real esperando una decisión del usuario.

**Cosas que funcionan pero generan dudas, documentadas con honestidad:**
- La posición final de Freddy `(6.0, 10.0)` fue derivada con matemática de línea recta aproximada y luego verificada empíricamente -- funciona para las posiciones actuales de jugador/salida, pero no es una solución "a prueba de balas" ante cualquier cambio futuro (por eso la heurística anti-atasco general es la red de seguridad real, no la coordenada específica).
- El balance de velocidades (jugador 3.5, Freddy 1.4) hace que un jugador que sabe la ruta escape con margen amplio -- no se evaluó si eso es la tensión deseada para la experiencia final o si haría falta ajustar dificultad; es una decisión de diseño, no un bug.

### 2.17 Integración de Bonnie/Chica/Foxy como guardias estáticos, sesión 2026-08-02/03

**Decisión de diseño del usuario, tomada en conjunto:** dado que el roadmap original de 8 fases ya estaba completo tras el bloque de integración (§2.15) y la auditoría (§2.16), se preguntó explícitamente cuál debía ser el siguiente objetivo. El usuario eligió diseñar el comportamiento de Bonnie/Chica/Foxy en el Escape antes de implementarlo. Sobre el patrón de comportamiento, eligió **guardias estáticos en un punto fijo** (no patrulla, no decoración pasiva): cada uno vigila una zona sin moverse y persigue si el jugador se acerca demasiado -- exactamente el mismo patrón `IdleState`→`ChaseState`→`CaughtState` que ya usaba Freddy, sin necesidad de un `PatrolState` nuevo (que Architecture.md ya había previsto y diferido explícitamente para este momento).

**Hallazgo clave que simplificó la implementación:** `IdleState` (ya existente) es exactamente "guardia estático" -- no mueve a la entidad, solo vigila la distancia y transiciona a `ChaseState` al detectar al jugador. No hizo falta escribir ningún estado nuevo, ni código específico de personaje -- `IdleState`/`ChaseState`/`CaughtState` ya eran completamente genéricos.

**Cambios reales:**
- `EntityDefinition` de `bonnie.json`/`chica.json`/`foxy.json`: `aiControlled` pasó de `false` a `true`.
- `MapDefinition`/`pizzeria.json`: nuevos campos `bonnieStartX/Z`, `chicaStartX/Z`, `foxyStartX/Z`.
- `BootScreen`: ahora encola los 4 modelos de personaje (antes solo Freddy) -- sin esto, `AssetService.getModel()` lanza `GdxRuntimeException: Asset not loaded` al intentar crear a Bonnie/Chica/Foxy, porque `ContentRegistry`/`AssetService` no cargan nada automáticamente por diseño (Architecture.md: es `BootScreen` quien decide explícitamente qué cargar).
- `GameplayScreen`: generalizado de "un solo Freddy hardcodeado" a una lista `guardias` (`Array<AIComponent>`) poblada por un método `crearGuardia()` reutilizado para los 4 personajes -- crea la entidad, conecta `objetivo`/`collisionWorld`, registra el `AIComponent` para el chequeo de atrapada, y devuelve el `maxBones` de su modelo. El shader compartido de `SceneManager` ahora se dimensiona al **máximo** `maxBones` entre los 4 esqueletos, no solo el de Freddy. El chequeo de "atrapada" (antes `freddyAi.jugadorAtrapado`) ahora recorre `guardias` y dispara `NightGameOverScreen` si **cualquiera** de los 4 atrapa al jugador.

**Verificación real, no solo compilación:**
- **Posicionamiento:** los 3 primeros intentos de posición (elegidos a ojo cerca de zonas de interés -- cocina, cuello de botella, escenario) quedaron **todos incrustados** en colliders reales -- confirmado por `warnIfEmbedded`. Se repitió la técnica ya establecida de mapa de conectividad en cuadrícula (`CollisionWorld.overlapsStatic()`) para encontrar puntos genuinamente libres cerca de cada zona candidata. Gotcha nuevo encontrado en el proceso: la sonda de la cuadrícula debe muestrear a la altura Y real de spawn de los guardias (Y=0, igual que Freddy), no a la altura de ojos del jugador (Y=1.6) -- un primer escaneo con la altura equivocada dio resultados "libres" que en realidad estaban incrustados a la altura real del personaje.
- **Verificación visual en el motor real:** los 4 personajes nunca se habían cargado juntos en una escena real antes de esta sesión (riesgo identificado explícitamente en la auditoría §2.16). Capturas aisladas (técnica ya establecida: cámara propia + `mapScene` removida temporalmente + `flipY=true`) confirmaron que los 4 se ven correctos -- de pie, proporciones normales, texturas/materiales correctos, sin corrupción visual por compartir un único `PBRShaderProvider` dimensionado al hueso más exigente de los 4 esqueletos distintos.
- **Balance de dificultad, con evidencia real, no a ciegas:** el primer intento de posicionar a Chica cerca del cuello de botella de la ruta de escape real (para agregar tensión al camino principal) produjo una **atrapada casi inevitable** -- la posición elegida quedó demasiado cerca de la línea exacta de la ruta, sin margen real de reacción (confirmado con el autopiloto de puntos de paso ya usado para verificar el escape). Un segundo intento (reubicarla en el pasillo norte, ancho y abierto) también resultó en atrapada temprana por la misma razón -- la ruta pasaba demasiado cerca de su posición inicial. **Decisión final, conservadora y verificada:** los 3 guardias nuevos se ubicaron deliberadamente **fuera** de la ruta de escape establecida (cocina, comedor original, escenario) en vez de sobre ella -- agregan riesgo real a quien explore el edificio, sin arriesgar que la ruta principal (la única verificada como recorrible de punta a punta) se vuelva injusta o imposible. Verificado de nuevo con el autopiloto completo: el escape sigue funcionando con los 4 personajes activos a la vez.
- Se confirmó (antes de la reubicación final) que el mecanismo completo de atrapada de un guardia genérico (no Freddy) funciona de punta a punta -- Chica, en su posición intermedia, sí atrapó exitosamente al jugador, confirmando que `IdleState`→`ChaseState`→`CaughtState`→`jugadorAtrapado` funciona igual de bien para cualquier personaje, no solo Freddy.

**Pendiente, no bloqueante:** las posiciones actuales son deliberadamente conservadoras (lejos de la ruta principal) -- si en el futuro se quiere que los guardias añadan tensión a la ruta principal en vez de solo a la exploración, hace falta una prueba de balance más cuidadosa (posiblemente con un autopiloto que sí evada al ser detectado, no uno que camine en línea recta ciega) antes de acercarlos al camino real.

### 2.18 Fase 8 — validación agresiva final, sesión 2026-08-03 (sin cambios de código -- ver nota al final)

**Pedido explícito del usuario:** no dar la Fase 8 (el bloque completo de integración Swing↔LibGDX + Bonnie/Chica/Foxy) por cerrada solo porque compiló o funcionó una vez -- una ronda de validación agresiva, intentando romper la lógica desde varios escenarios, con pruebas largas y repetidas, antes del commit final. Corregir cualquier bug encontrado que perteneciera al trabajo actual; si algo resultaba ser de una fase anterior, corregirlo también antes de continuar.

**Resultado: cero bugs nuevos encontrados.** A diferencia de la ronda de trabajo anterior (que sí encontró y corrigió 2 problemas reales -- Freddy atascado en la puerta, y el primer/segundo intento de posición de Chica volviendo la ruta injusta), esta ronda de validación confirmó que esas correcciones son sólidas bajo presión real, no solo en el primer intento.

**Pruebas ejecutadas, todas con evidencia real de ejecución, no solo revisión de código:**
1. **Atrapada individual de los 4 personajes.** Verificado uno por uno (no solo Freddy y Chica, que ya se habían probado antes) -- Bonnie, Chica y Foxy cada uno confirmado atrapando al jugador correctamente (`jugadorAtrapado` del guardia correcto en cada caso, identificado por índice en la lista `guardias`). Para hacerlo eficiente, el jugador se hizo aparecer cerca de cada guardia en vez de depender de que un autopiloto navegara todo el mapa desde la oficina -- evita confundir "el guardia no atrapa" con "mi propio bot de prueba se atascó en el camino largo" (justamente lo que pasó en el primer intento: el autopiloto tardó varios minutos sin resultado navegando hacia Bonnie a través de obstáculos no relacionados, y se abandonó ese enfoque a favor de aislar la prueba).
2. **Atrapada simultánea/oscilación entre guardias.** Jugador colocado en un punto dentro del rango de detección de Bonnie y Chica a la vez (~3 unidades de cada una), oscilando entre 2 puntos cercanos sin excepciones -- Chica terminó atrapando al jugador correctamente, sin ningún estado inconsistente por tener múltiples máquinas de estado de IA activas sobre el mismo objetivo simultáneamente.
3. **Estabilidad prolongada + colisión adversarial combinadas.** Un primer intento (bucle por las 4 esquinas lejanas del edificio) reveló que el propio autopiloto de prueba se atascaba cruzando la zona de cuello de botella ya conocida (§2.15, la misma que la ruta de escape real esquiva pasando por x=-7) -- **no es un bug nuevo, es la misma geometría ya documentada**, simplemente mi ruta de prueba improvisada no la esquivaba como sí lo hace la ruta real. Corregido reusando la ruta oficina↔salida ya comprobada seguirla en bucle repetido en vez de inventar esquinas nuevas. Con las transiciones de pantalla desactivadas temporalmente (para no cortar la prueba en el primer contacto con la salida o una atrapada), se dejó correr **~6 minutos continuos** de recorrido repetido de ida y vuelta por el corredor completo (incluido el cuello de botella, decenas de veces) -- memoria de heap estable entre 420-432 MB sin crecimiento (sin indicios de fuga), cero excepciones, cero advertencias de spawn incrustado.
4. **Compilación limpia de ambos proyectos** desde el estado realmente commiteado (no desde el working tree con cambios de prueba a medio quitar).
5. **Re-verificación completa del flujo Swing→LibGDX** con el harness standalone ya establecido, contra el código actual (que ahora incluye Bonnie/Chica/Foxy, más lento de cargar que antes) -- `handoff.json` escrito correctamente, lanzamiento exitoso hasta cargar los 4 personajes, y el callback de retorno al menú confirmado disparándose correctamente al terminar el proceso.
6. **Carga de assets y animación de Freddy** re-confirmadas sin advertencias nuevas -- el código de animación/rotación no cambió desde su última verificación visual, así que no se repitió esa captura específica (hubiera sido redundante), pero sí se confirmó que los 4 modelos + el mapa cargan limpio desde `BootScreen`.

**Código temporal de esta ronda de validación:** se agregó y se quitó por completo (spawns de prueba junto a cada guardia, autopiloto de bucle largo, transiciones desactivadas temporalmente, logs de memoria/posición) -- confirmado con `git diff` vacío contra el último commit de `five_doors_escape` antes de considerar la fase cerrada.

**No hubo commit nuevo en `five_doors_escape` en esta sesión** -- la validación no encontró nada que corregir, así que no hay cambio de código que conservar. Los commits reales de la Fase 8 (`b172fa7`, `9da3e32`, `510bfcf`) ya estaban hechos y pusheados de la sesión anterior; esta ronda es la confirmación de que esos commits son estables bajo prueba agresiva, no una sesión de código nueva.

**Con esto, la Fase 8 (bloque de integración Swing↔LibGDX + Bonnie/Chica/Foxy como guardias) queda cerrada con confianza real, no solo "funcionó una vez."**

### 2.19 Recuperación de sesión cortada por CCleaner + ronda de observaciones post-Fase 8, sesión 2026-08-03

**Contexto de esta sesión:** la sesión anterior se cortó a mitad de trabajo porque CCleaner cerró PowerShell/la ventana de contexto sin aviso. Al retomar, se encontró un WIP real sin commitear ni documentar en `five_doors_escape`: colisión de paredes interiores (`LevelLoader.subdivideNode`/`collectDoorZones`), con código de diagnóstico (autopiloto, BFS, heartbeat) todavía dentro del archivo -- exactamente lo que se estaba probando cuando se cortó la sesión. Se reconstruyó el contexto completo (CLAUDE.md + memorias + `git status` de ambos repos) antes de tocar nada, confirmado explícitamente por el usuario para continuar ese WIP.

**Las 6 observaciones del usuario, clasificadas con evidencia real (no supuesta) antes de corregir nada:**
1. "Todavía atravieso algunas paredes" -- **Bug confirmado.** El código commiteado hasta este punto no tenía ninguna colisión de pared interior real (§2.8 había descartado por completo el collider "degenerado" que las representaba). Corregido en esta sesión, ver abajo.
2. "Bonnie/Chica/Foxy sin animación de caminar" -- **Comportamiento esperado, no bug.** Confirmado leyendo `EntityFactory` (`if (def.animaciones.size > 0) entity.add(new AnimationComponent(...))`) y `AnimationSystem` (`if (!animation.tieneAnimaciones()) return;`): los 3 personajes no tienen `AnimationComponent` en absoluto porque sus JSON traen `"animaciones": {}` (0 clips utilizables exportados, ya documentado en §2.6). `RenderSyncSystem` sigue actualizando su transform cada frame -- se mueven en pose de bind, exactamente como especifica el contrato de animaciones opcionales de Architecture.md. Conseguir animación real requeriría un retargeting tipo Mixamo como el de Freddy -- no intentado, es trabajo futuro, no un defecto.
3. "Animatrónicos atascados en paredes/esquinas" -- **Bug confirmado, corregido con una generalización de la heurística existente** (ver abajo).
4. "Debo salir por las puertas reales de la oficina" -- **Diseño ya aprobado, cumplimiento dependía del bug #1.** Con paredes interiores reales y las zonas de puerta despejadas (ver abajo), el atajo en línea recta que ignora las puertas queda bloqueado (verificado), mientras que la ruta real por las puertas sigue funcionando.
5. "Freddy aparece muy cerca de la oficina" -- **Bug de balance confirmado con evidencia objetiva, no solo percepción.** Distancia real oficina↔Freddy con el valor commiteado (`freddyStartX=6.0`): 6.16 unidades -- **menor que `rangoDeteccion=8`**, es decir Freddy detecta al jugador desde el primer frame de la partida, sin ningún margen. Corregido (ver abajo).
6. "La pizzería no tiene techo" -- **Falso positivo de bug, limitación de asset ya investigada.** Reconfirmado en este build (`collisionWorld.overlapsStatic` sobre un punto del comedor a altura de techo = `false`, sin ningún collider ahí): sigue siendo, como ya se concluyó en la Fase 4 (§2.7), un hueco real del asset de Sketchfab (pensado para exhibición, no para recorrerse en primera persona), no un bug de colisión ni de render. Sigue como decisión de diseño pendiente -- alternativas sin implementar: (a) aceptar el hueco (no bloqueante, ya se juega así hoy), (b) modelar/agregar un plano de techo propio en Blender sobre esa zona, (c) recortar la zona jugable para que el recorrido real no pase por ahí. No se tomó ninguna decisión -- se documenta para que el usuario elija cuándo retomarlo.

**Corrección real #1 -- colisión de paredes interiores (`LevelLoader.java`):** el nodo "degenerado" descartado en la Fase 4 (§2.8) resultó ser todas las paredes del edificio (interiores y exteriores) fusionadas en una sola malla -- descartarlo entero evitó bloquear el interior, pero también eliminó la colisión de cualquier pared interior real. Solución: en vez de descartar ese nodo, `subdivideNode()` rasteriza sus triángulos reales sobre una grilla XZ de 0.2 unidades (`WALL_GRID_CELL_SIZE`) y genera un collider por celda ocupada -- las celdas sin geometría (huecos de puerta) quedan libres automáticamente. Un filtro de altura mínima (`WALL_MIN_HEIGHT=1.0`) evita subdividir el piso (que se solaparía con la altura de los personajes con IA). Las puertas reales (nodos cuyo id contiene "puerta", recorriendo ancestros) se detectan con `collectDoorZones()`/`esParteDePuerta()` y se despejan explícitamente (`DOOR_CLEARANCE_MARGIN=0.4`) tanto de su propio collider como de cualquier celda subdividida que las cubra -- sin esto, con colisión de pared interior real, la oficina habría quedado completamente sellada (no existe ninguna mecánica de apertura/cierre de puertas en este MVP). Resultado: 3542 colisionadores estáticos (antes 69) -- medido sin problema de rendimiento en las pruebas de esta sesión (ver metodología abajo), pero es un salto grande que vale la pena vigilar si el mapa cambia.

**Hallazgo real durante la verificación -- el punto de salida quedó bloqueado por una columna real:** al recuperar la colisión interior, se descubrió que `exitX=-7.5, exitZ=-7.5` (fijado en §2.15 cuando esa colisión no existía) ahora cae dentro de una columna/pilar real de la pizzería (antes invisible para la colisión porque formaba parte del mismo nodo "degenerado" descartado). Funcionalmente seguía "funcionando" (el radio de disparo de 1.5 unidades igual se activaba desde un punto cercano transitable), pero por prolijidad se reubicó a `exitZ=-7.0` (verificado libre y alcanzable por BFS de conectividad) -- misma intención narrativa ("salida trasera"), sin quedar dentro de geometría sólida.

**Corrección real #2 -- heurística anti-atasco de `ChaseState`, generalizada (no solo una coordenada nueva):** con la nueva colisión interior activa, un guardia (Bonnie) quedó congelado indefinidamente contra una pared nueva -- la heurística existente (§2.16, "mover por el eje con mayor diferencia") no ayudaba porque el objetivo de prueba estaba perfectamente alineado en el eje bloqueado (diferencia ~0 en el otro eje), así que el eje "prioritario" era siempre el mismo eje ya bloqueado. Dos correcciones, siguiendo la instrucción explícita de la memoria `feedback-libgdx-ai-stuck-on-corners` de extender el mecanismo general en vez de retocar coordenadas a mano:
1. **Métrica de atasco corregida:** `actualizarDeteccionAtasco` medía desplazamiento crudo (`propio.position.dst(posicionAnterior)`), no progreso real hacia el objetivo -- deslizarse a lo largo de una pared sin acercarse igual "contaba" como avance y reiniciaba el contador de atasco antes de completar una estrategia, causando una oscilación infinita. Corregido para medir la **distancia al objetivo** muestreada periódicamente (`AIComponent.distanciaMuestreada`, antes `posicionMuestreada`).
2. **Alternancia de 3 estrategias** cuando el atasco persiste más de `VENTANA_ALTERNANCIA_EJE=1.5s`: eje prioritario primero (resuelve el caso original del marco de puerta), y si no alcanza, tantear el otro eje a mano en ambos sentidos (+ y -), porque el signo real de ese eje puede ser cero y no sirve como señal.
   Verificado: los 4 personajes (Freddy con un objetivo válido, Chica, Foxy) atrapan en ~1.7s simulados sin atascarse. **Límite honesto, no resuelto y no se intentó resolver por estar fuera de alcance del MVP (sin pathfinding real, Architecture.md):** un guardia persiguiendo un objetivo que requiere un desvío de más de un giro (confirmado con BFS que el punto SÍ es alcanzable, solo que no en línea recta ni con un único desvío lateral) puede seguir sin encontrarlo dentro de un tiempo razonable. No se investigó si esto ocurre con posiciones reales de jugador (solo con un punto de prueba sintético que probablemente cruza a otra habitación) -- si aparece en juego real, es la limitación ya conocida y aceptada de "sin pathfinding", no un defecto nuevo.

**Corrección real #3 -- balance de spawn de Freddy:** `freddyStartX` de `6.0` a `12.5` (`freddyStartZ` sin cambios). Con `6.0` la distancia real al spawn del jugador era 6.16 unidades, dentro de `rangoDeteccion=8` -- Freddy detectaba al jugador literalmente desde el frame 0. Con `12.5` la distancia es 12.58, fuera de rango, dando un margen real antes de la primera detección. Verificado que Freddy sigue atrapando normalmente desde ahí (~1.7s simulados contra un objetivo alcanzable) y que la distancia al muro límite este (`maxX≈14.82`) deja 2.32 unidades de margen -- no queda pegado al límite del mapa.

**Metodología de esta sesión, importante para el futuro -- el entorno de pruebas automatizado de esta sesión NO puede sostener el bucle de render real por más de unos segundos:** confirmado con `jstack` que el hilo principal queda atascado dentro de `Lwjgl3Application.loop()`/`Sync.sync()` (código interno de libGDX, ningún frame de código del juego involucrado) porque la ventana nunca tiene foco real de Windows en este entorno (confirmado con `GetForegroundWindow` -- el foco seguía en la terminal, pese a `SetForegroundWindow` explícito). Ni ventana+vsync desactivado evitó el cuelgue (se mueve a bloquear en la presentación GPU en vez de en `Sync`). **Solución encontrada y reutilizable:** mover toda la verificación a una simulación de lógica pura dentro de `show()`, antes de que arranque el bucle de render -- llama directamente a `CollisionWorld.resolveMovement`/la misma matemática de `ChaseState` con un `dt` fijo controlado por el propio test, sin ningún `Gdx.graphics`/GL de por medio (evitando también que `ChaseState.update()` real lea `Gdx.graphics.getDeltaTime()`, que nunca avanza fuera del bucle real y daría un falso negativo de "atasco" con dt=0). Toda esta instrumentación se agregó y se retiró por completo al terminar -- confirmado con `git diff` limpio (`GameplayScreen.java`, `CollisionWorld.java` y `Lwjgl3Launcher.java` no aparecen en el diff final). **Si una sesión futura en este mismo entorno necesita probar comportamiento en tiempo real (varios segundos u autopilotos largos), replicar este enfoque de simulación pura en vez de asumir que el juego real correrá indefinidamente sin foco.**

**No commiteado en este bloque, fuera de alcance -- work-in-progress ajeno encontrado en `FiveDoorsAtFreddys` (Swing) al reconstruir contexto:** cambios sin commitear en `Juego.java` (reintroduce un gate `noche>=2` en `calificaParaBateria()` que §1.5 documentaba como ausente), `Main.java`/`.classpath` (tema visual `FlatDarkLaf`). Ninguno de los dos se tocó en esta sesión -- no tienen relación con las 6 observaciones de Escape, quedan tal cual estaban para que el usuario decida cuándo retomarlos.

**Commiteado y pusheado a `five_doors_escape`** (ver commit en el log real del repo): colisión de paredes interiores + zonas de puerta despejadas (`LevelLoader.java`), heurística anti-atasco generalizada + métrica de atasco corregida (`ChaseState.java`, `AIComponent.java`), corrección de balance de spawn de Freddy y reubicación del punto de salida (`pizzeria.json`).

> **⚠️ Nota sobre §2.15/§2.16:** la afirmación de §2.15 de que el bloque de integración quedaba "funcionalmente terminado" y la de §2.16 de que las heurísticas eran robustas se referían a un estado sin colisión de pared interior real. Con paredes interiores reales (esta sesión), el punto de salida y al menos una posición de guardia necesitaron ajuste -- consistente con la lección ya anotada en §2.16: cualquier cambio de colisión/mapa debe re-probar spawns y persecución explícitamente, no asumir que siguen válidos.

### 2.20 Ronda de observaciones de juego real post-Fase 8 (parte 2), sesión 2026-08-03

**Contexto:** tras cerrar la colisión de paredes interiores (§2.19), el usuario jugó la build real y reportó 7 observaciones nuevas, con instrucción explícita de investigar cada una antes de asumir que era un bug.

**1) "Ya no veo a Freddy" -- confirmado con evidencia objetiva, corregido.** `freddyStartX=12.5` (fijado en §2.19 para resolver la detección instantánea) quedó en el extremo este del mapa, a más de 20 unidades del corredor real de escape (oeste) -- fuera de cualquier alcance práctico durante una partida normal (Freddy usa `IdleState`, no patrulla: si el jugador nunca entra en su `rangoDeteccion=8`, se queda quieto en su spawn para siempre). Corregido a `freddyStartX=9.0` (distancia real al spawn del jugador: 9.11, todavía fuera de rango de detección instantánea, pero dentro de la zona central/comedor donde un jugador que explora puede encontrarlo). Verificado que sigue atrapando con normalidad.

**2) Techo -- análisis y recomendación (sin implementar, según pedido explícito).** Reconfirmado que sigue siendo la misma limitación de asset ya documentada en la Fase 4. Análisis de las 3 alternativas:
- *(a) Aceptar el hueco:* costo cero, pero deja una ruptura de inmersión real (mirar hacia arriba en el comedor muestra el skybox) -- no soluciona nada, solo pospone.
- *(b) Modelar un techo propio en Blender:* reutiliza el pipeline Blender_WIP → validar por reimportación → exportar ya establecido y probado en este proyecto (Fases 1-3). Esfuerzo acotado (un plano simple sobre la huella del comedor, con textura/material plausible) comparado con el retargeting de animación ya hecho con éxito para Freddy. Soluciona el problema de raíz, de forma permanente.
- *(c) Recortar la zona jugable:* no ataca el problema real (seguiría sin techo, solo se evitaría exponerlo) y es técnicamente incómodo de implementar bien para un problema que es específicamente "mirar hacia arriba" (no de posición) -- un parche, no una solución.
**Recomendación: alternativa (b).** Es la única que resuelve el problema de raíz, con un esfuerzo bajo comparado con otros trabajos ya completados en este mismo pipeline, y sin comprometerse a nada hasta que el usuario decida retomarlo.

**3) Rotación inicial del jugador -- implementado y verificado.** `playerStartYawDegrees` 0.0→180.0. Verificado con una caja de sondeo a 0.5u al frente de la nueva orientación: sin pared pegada (`pared_muy_cerca=false`). El flujo de salida por las puertas no depende de la orientación inicial (el jugador puede girar libremente con el mouse; la colisión/lógica de puertas es independiente de hacia dónde mira al arrancar), así que no había riesgo real de romperlo.

**4) Bonnie en el "pasillo izquierdo" -- implementado y verificado con evidencia, no a ciegas.** Reubicada de `(10.0, 0.0)` (cocina, sin relación con el pedido) a `(-8.0, 6.0)`: el corredor ancho (5-6 unidades) inmediatamente al oeste de la puerta izquierda de la oficina, la zona más análoga en este mapa 3D al pasillo izquierdo de FNAF1. Distancia real al spawn del jugador: 8.41 (fuera de detección instantánea). **Verificado con una simulación que corre la ruta oficina→salida completa CON los 4 guardias activos en paralelo** (no solo la colisión del jugador aislada): la ruta sigue siendo transitable (`escapoOk=true`) pese a que Bonnie ahora está deliberadamente sobre/cerca del corredor inicial -- decisión consciente del usuario de agregar tensión ahí, distinta de la regla general de la Fase 8 de mantener a los guardias lejos de la ruta.

**5) Foxy "saliendo de Pirate Cove" -- investigado a fondo, adaptado con honestidad.** Se encontraron los nodos `escenario_7` ("stage") y `CORTINA_37` ("curtain") en el glTF -- coincide con el patrón FNAF de Pirate Cove. **Pero un escaneo fino de colisión (grilla de 0.5u) confirmó que no existe ningún hueco/alcoba real ahí: la cortina está pegada contra la pared oeste sólida, sin piso transitable propio.** Dos intentos de ubicar a Foxy justo en el corredor angosto que pasa cerca de la cortina (`x=-7` y `x=-8, z=-3`) **rompieron la ruta de escape real** (confirmado con la misma simulación de ruta+4 guardias: `atrapadoPor=Foxy`, `escapoOk=false`) -- ese corredor es de un solo carril (1.5-3 unidades), sin margen para esquivar a un guardia estático, a diferencia del pasillo ancho de Bonnie. Reubicado a `(-9.0, -6.0)`, en la zona abierta (5 unidades) más al sur, cerca del área general de la cortina/escenario pero fuera de la línea directa de la ruta -- verificado que la ruta vuelve a completarse (`escapoOk=true`) y que Foxy sigue atrapando con normalidad. **Conclusión honesta para el usuario:** "Pirate Cove" tal como existe en FNAF (una alcoba separada) no está presente en este asset; lo que se implementó es "Foxy cerca de la cortina decorativa", no "Foxy saliendo de una cueva real".

**6) Mecánica de 2 vidas con jumpscare cinematográfico -- implementada y verificada.** Antes: cualquier atrapada disparaba `NightGameOverScreen` de inmediato. Ahora: `GameplayScreen` tiene un estado interno (`EstadoPartida`: JUGANDO/JUMPSCARE/ESTATICA) que reemplaza esa transición inmediata:
- **Decisión de assets, consultada explícitamente con el usuario (tocaba la regla ya aprobada "nunca importar assets de Swing"):** el usuario eligió una variante de "cierre de cámara sobre el modelo 3D real" -- **jamás los GIFs 2D de animatrónicos del proyecto Swing** (identidad visual propia para el Escape), pero sí reutilizar el sonido de jumpscare y de estática genéricos (ninguno de los dos es específico de un personaje ni siquiera en Swing) desde el pool de assets FDAF -- copiados a `five_doors_escape/assets/sounds/{jumpscare,static}.wav`.
- **Jumpscare (~1s):** la cámara corta bruscamente a un plano muy cerrado del modelo 3D real del animatrónico que atrapó al jugador (no una captura ni un video, el modelo real de la escena), con temblor de posición + rotación (roll) + un zoom oscilante rápido -- deliberadamente agresivo, no una imagen fija. Si el personaje tiene animación real (Freddy, único caso en el MVP), su `animationController` sigue avanzando con normalidad (el motor ECS se pausa durante el jumpscare, pero `sceneManager.update()` sigue corriendo, así que la animación "walk" ya activa desde la persecución continúa). Si no tiene animación (Bonnie/Chica/Foxy), se le aplica un jitter procedural directo a su transform (balanceo de yaw + vaivén vertical) para que no se vea un modelo perfectamente quieto frente a cámara.
- **Estática (~5s):** ruido de TV generado por procedimiento cada frame (`Pixmap`/`Texture` de 160×90, escalado a pantalla completa) en vez de decodificar el GIF de estática existente -- libGDX no trae un loader de GIF animado nativo, y agregar una librería nueva solo para esto hubiera sido más invasivo que generar el ruido con clases ya disponibles. Cumple el mismo rol visual. El sonido de estática sí es el `.wav` real reutilizado.
- **Resolución:** al terminar la estática, `vidasRestantes` (inicia en 2) se decrementa; si queda alguna, el jugador reaparece en su spawn original (posición + orientación + los 4 guardias también vuelven a su spawn con su máquina de estados reiniciada a `IdleState`, para que no queden a mitad de una persecución vieja); si no queda ninguna, recién ahí se dispara `NightGameOverScreen`.
- **`AssetService`/`BootScreen`:** se agregó `queueSound`/`getSound` (mismo patrón que `queueModel`/`getModel`, `AssetManager` ya resuelve `Sound.class` sin loader adicional) y `BootScreen` encola los 2 sonidos junto con los modelos.
- **Verificación real, no solo revisión de código:** se disparó un jumpscare real (`iniciarJumpscare` contra Freddy) y se recorrió manualmente el ciclo completo (`actualizarJumpscare` 61 pasos ≈1s → `actualizarEstatica` 300 pasos =5s exactos) **ejercitando las llamadas GL reales** (`sceneManager.render()`, `uiBatch`) -- confirmado sin excepciones, jugador y Freddy vueltos a su spawn, `jugadorAtrapado` reiniciado a `false`. La rama de Game Over definitivo (segunda atrapada, `vidasRestantes` llega a 0) no se disparó en vivo a propósito -- hacerlo mid-`show()` hubiera llamado `game.setScreen()+dispose()` sobre recursos que el resto de `show()` todavía necesita; se validó por revisión de código (un solo `if` trivial).

**7) Observaciones adicionales durante esta ronda:** ninguna encontrada fuera de lo ya cubierto arriba -- no se detectaron bugs nuevos ni oportunidades de mejora que no estuvieran ya contempladas en los 6 puntos.

**Commiteado y pusheado a `five_doors_escape`:** reposicionamiento de Freddy/Bonnie/Foxy + rotación inicial del jugador (`pizzeria.json`), mecánica de 2 vidas + jumpscare cinematográfico (`GameplayScreen.java`, nuevo método `resetOrientation` en `FirstPersonCameraController.java`, `queueSound`/`getSound` en `AssetService.java`, encolado de sonidos en `BootScreen.java`), y los 2 archivos de sonido reutilizados (`assets/sounds/jumpscare.wav`, `assets/sounds/static.wav`).

### 2.21 Ronda de pulido general post-§2.20, sesión 2026-08-03 (misma fecha, sesión posterior)

**Contexto:** el usuario jugó de nuevo la build de §2.20 y dio 8 pedidos concretos en Auto Mode (decisiones de implementación permitidas mientras no cambien el diseño general), con instrucción explícita de investigar cada causa real (no asumir) y probar de verdad después de cada cambio. Varias conclusiones de §2.19/§2.20 quedaron **corregidas** por esta sesión, no solo extendidas -- ver detalle en cada punto.

**1) "Ya no veo a Freddy" -- la causa real no era la ya corregida en §2.20.** `freddyStartX=9.0` (fijado en §2.20) seguía sin corresponder a ninguna geometría real del comedor -- ese punto cae fuera del área donde están las mesas/manteles del modelo. Se releyeron los nombres de nodo reales del glTF y se ubicó el comedor real (`mesas_32`/`manteles_34`); Freddy se reposicionó a `(7.0, -4.0)`, dentro de esa huella. Verificado con la misma simulación de ruta+4 guardias (`escapoOk=true`) y con captura de jumpscare real mostrando su modelo reconocible de cerca (ver punto 4).

**2) Techo real modelado en Blender -- implementado esta sesión (§2.20 solo lo había recomendado).** Pipeline completo ejecutado sobre `Pizzeria_WIP.blend`:
- **Diagnóstico con `bpy` headless (no se asumió nada):** un primer escaneo por rayos hacia arriba dio una lectura equivocada (origen del rayo por debajo del piso real, Z=-1.0 contra un piso en Z≈-0.35) -- corregido con origen dentro del cuarto. El resultado corregido seguía pareciendo un hueco enorme e irregular (patrón tipo tablero de ajedrez), lo cual no es una forma arquitectónica real -- se identificó como ruido de muestreo, no una señal real.
- **Causa real encontrada listando objetos directamente (no rayos):** `Object_21`, el único candidato con nombre/altura de techo (`Z[2.66,2.97]`, huella `X[-10.99,10.60] Y[-9.50,6.36]` -- casi toda la planta del edificio), resultó ser una moldura/perfil decorativo de ~29.000 caras diminutas que traza el borde del techo, **no una plancha sólida**. El edificio, tal como viene del asset original, prácticamente no tiene techo sólido en ninguna parte -- no era un "hueco puntual sobre el comedor" como se documentó en §2.20, sino la ausencia casi total de una superficie de techo real.
- **Parche creado:** un plano rectangular nuevo (`TechoPrincipal_Patch`, 4 vértices) sobre la huella interior de esa moldura (`X[-10.99,10.60] Y[-9.50,6.36]`, `Z=2.85`, dentro del rango de la moldura existente), normal hacia abajo, UVs planas, material reutilizado `main_material` (el mismo de los muros, para consistencia visual) -- sin inventar geometría ni materiales nuevos más allá de lo mínimo necesario.
- **Export/validación siguiendo el pipeline ya establecido (Fases 1-3):** exportado a `Pizzeria.glb` (headless, Blender 5.1, `export_yup=True`), validado por reimportación en escena limpia: 79 mallas (78 originales + 1 nueva), 48 materiales (sin cambio -- se reutilizó uno existente), bbox global idéntico (27.43×21.67×3.32) al documentado en §2.19. El parche reimportado conserva exactamente su huella y altura.
- **Impacto en colisión, verificado no solo asumido:** el parche no genera ningún collider (`dims.y≈0 < WALL_MIN_HEIGHT`, y aunque `spansWholeFootprint=true` por su huella grande, la condición de altura mínima para subdividir tampoco se cumple) -- confirmado que el conteo de colisionadores estáticos real (3385) es idéntico antes y después de agregar el parche. Es puramente visual, cero riesgo de romper movimiento/colisión.
- **Verificación visual real:** captura desde dentro del comedor mirando hacia arriba muestra una superficie continua (textura de `main_material`), sin hueco ni skybox visible.
- **Backups manuales antes de modificar el asset** (fuera de git, en `Blender_WIP`/`LibGDX_Ready`): `Pizzeria_WIP.blend.bak_pretecho`, `Pizzeria.glb.bak_pretecho` -- conservados por si una sesión futura necesita comparar o revertir.

**3) HUD de vidas con corazones -- implementado y verificado según el flujo exacto pedido.** Pantalla completamente negra con SOLO corazones (sin texto) al iniciar la partida (`INTRO_CORAZONES`, ~2.5s) → SOLO "RUN" (sin corazones, ~1.5s) → juego. Al perder una vida: jumpscare → estática → pantalla negra con SOLO los corazones restantes (~2.5s, `CORAZONES_RESPAWN`) → reaparición. Corazones dibujados por procedimiento con `ShapeRenderer` (2 círculos + 1 triángulo, igual que ya se hacía en Swing) en vez de glifos Unicode ♥/♡ (la fuente por defecto de libGDX no los trae). Tamaño aumentado (140px, antes 48px) tras confirmar en captura real que el tamaño inicial era casi invisible en una pantalla dedicada solo a eso. Verificado ejercitando el ciclo real completo (intro→RUN→jumpscare→estática→corazones→respawn) con capturas en cada tramo.

**4) Jumpscare más cercano/agresivo + audio con corte exacto -- implementado y verificado.** Distancia de cámara reducida (`JUMPSCARE_DISTANCIA_BASE=0.75`, con pulso de zoom oscilante encima) y shake de posición/rotación aumentado. Dirección de aproximación de cámara cambiada de un eje mundial fijo al yaw real del personaje (más robusto si su orientación cambia). Audio con control exacto por código en vez de depender de la duración natural del `.wav`: `sonidoJumpscare.play()`/`.stop()` al llegar a `DURACION_JUMPSCARE=1s` exacto, inmediatamente seguido de `sonidoEstatica.play()`/`.stop()` a los `DURACION_ESTATICA=5s` exactos. Verificado con los logs reales de la simulación (`Jumpscare completado en 61 pasos (1.0166s)`, `Estatica completada en 300 pasos (5.0000s)`) -- sin excepciones al detener los sonidos.
- **Bug de arnés de prueba encontrado y corregido, no un bug de producción:** durante la investigación, la cámara del jumpscare mostraba formas irreconocibles incluso tras aislar el mapa. Causa real: `ModelInstance.transform` solo se sincroniza dentro de `RenderSyncSystem`, que solo corre dentro de `engine.update(dt)` -- el diagnóstico saltaba directo a `iniciarJumpscare()` sin haber llamado nunca a `engine.update()`, así que el modelo visual de Freddy nunca se movía a su posición lógica real. En juego real esto no ocurre (`engine.update()` corre todos los frames antes de que cualquier atrapada sea posible) -- confirmado que es un artefacto exclusivo del arnés de prueba, no un defecto de `GameplayScreen`/`RenderSyncSystem`.

**5) Estática -- reemplazada por el GIF real de FiveDoorsAtFreddys, según pedido explícito (revierte la decisión de §2.20 de generarla por procedimiento).** Se creó `GifDecoder` (`com.fivedoorsescape.util`, nuevo), un decodificador de GIF animado usando `javax.imageio.ImageIO` (ya incluido en el JDK, sin agregar ninguna librería nueva) que expone cada cuadro como `Pixmap`+duración real (leída del `GraphicControlExtension` del GIF, con fallback de 0.1s si falta). Se copió `Static.gif` desde `FiveDoorsAtFreddys/.../gifs/fondos/Static.gif` a `five_doors_escape/assets/gifs/Static.gif` (8 cuadros, confirmado por log real: `GIF de estatica decodificado: 8 cuadros`). Los cuadros se decodifican una sola vez a `Texture` en `show()` y se recorren en bucle durante los 5s reales de estática (el GIF original no fue pensado para durar exactamente eso). Sonido de estática sin cambios (ya era el `.wav` real desde §2.20).

**6) Pirate Cove de Foxy -- corrige una conclusión equivocada de §2.20, no solo la extiende.** §2.20 concluyó "no existe ninguna Pirate Cove real en este asset" basándose solo en evidencia del lado LibGDX (nombres de nodo del glTF ya cargado). Esta sesión inspeccionó el `.blend` original con `bpy` y encontró evidencia que §2.20 no tenía: materiales literales `pirateCove` (usado por `base_39`, el piso/tarima de la cueva) y `aroPirateCove` (el aro de la cortina), además de `cortina` (la tela). **La Pirate Cove real sí existe en el asset -- la conclusión anterior era incompleta, no la geometría.** Corrección aplicada solo en código: `esParteDePuerta` renombrado a `esParteDePuertaOCortina` (`LevelLoader.java`), extendido para tratar también `cortina`/`base_39` como zona pasable (igual que las puertas, ya que tampoco hay mecánica de abrir/cerrar cortinas en este MVP) -- sin tocar el asset. Confirmado con escaneo fino de colisión que la cueva completa quedó transitable (colisionadores estáticos bajaron de 3542 a 3385). Foxy reposicionado de `(-9.0,-6.0)` a `(-10.7,-0.5)`, dentro de la cueva ya abierta -- verificado que la ruta oficina→salida sigue completándose con los 4 guardias activos (`escapoOk=true`) y que Foxy sigue atrapando con normalidad.

**7) Spawn del jugador -- movido más atrás, verificado punto por punto contra la pared.** `playerStartZ` de `8.6` a `8.0`, buscado por sondeo real de colisión (probando cada 0.1 unidades desde `8.6` hasta `7.0`) el punto más cercano a la pared trasera de la oficina que no quedara incrustado (`7.9` ya resultaba embebido). Orientación (`180°`) sin cambios -- ya apuntaba a la salida de la oficina desde §2.20.

**8) Revisión general del proyecto -- un hallazgo real, sin bugs adicionales.** Revisados `IdleState`/`ChaseState`/`CaughtState`, el ciclo de vida de `dispose()` (confirmado que `cuadrosEstatica` se libera correctamente, sin fugas), el disparo de jumpscare (correctamente genérico por guardia, no hardcodeado a Freddy), y el dibujo del HUD (usa `Gdx.graphics.getWidth()/getHeight()` en vivo, no coordenadas fijas -- correcto para cualquier resolución). Único hallazgo: el comentario de `CaughtState` seguía describiendo el flujo viejo ("dispara `NightGameOverScreen`" directo) de antes de la mecánica de vidas de §2.20 -- corregido para reflejar el flujo real (jumpscare → decide respawn o game over según vidas restantes).

**Metodología reafirmada:** toda esta ronda se probó con datos reales, no solo revisión de código -- simulación de ruta+4 guardias en cada cambio de posición, conteo de colisionadores antes/después del techo, capturas reales del ciclo HUD/jumpscare/estática, y un arranque real final (headless, sin diagnósticos) confirmando 3385 colisionadores y cero excepciones. Toda la instrumentación temporal (`diagVerificarRonda2`, `diagVerificarSecuenciaCompleta`, `diagCapturarPantalla`, el escaneo `DIAG_COVE` y las capturas de verificación del techo) se agregó y se retiró por completo al terminar -- confirmado que no aparece en el diff final.

**Commiteado y pusheado a `five_doors_escape`:** reposicionamiento de Freddy y de Foxy + spawn del jugador (`pizzeria.json`), techo real modelado (`Pizzeria.glb`, nuevo `TechoPrincipal_Patch` en `Pizzeria_WIP.blend` fuera del repo), zona pasable de cortina/Pirate Cove (`LevelLoader.java`), HUD de corazones + jumpscare más cercano con audio exacto + estática con GIF real (`GameplayScreen.java`, nuevo `GifDecoder.java`), comentario corregido en `CaughtState.java`. Asset nuevo: `assets/gifs/Static.gif`.

### 2.22 Ronda de corrección tras juego real post-§2.21, sesión 2026-08-03 (misma fecha, sesión posterior)

**Contexto:** el usuario jugó la build de §2.21 y reportó 8 problemas reales más, en Auto Mode. Varios resultaron ser causas más profundas de lo que el síntoma sugería -- investigados con evidencia real en cada caso, sin asumir.

**1) Techo -- todavía había habitaciones sin cubrir, causa real distinta de lo esperado.** El parche de §2.21 (`TechoPrincipal_Patch`) solo cubría la huella de la moldura decorativa original (`X[-10.99,10.60] Z[-6.36,9.50]` en coordenadas LibGDX), no toda la huella real del edificio (`X[-12.61,14.82] Z[-10.10,11.56]`, la misma que usa `Object_6`, el piso) -- cocina, baños y la entrada este quedaban fuera. Corrección: se extendió el mismo plano a la huella completa del piso, **sin margen extra** (a propósito: agregar margen habría inflado el bbox global de la escena usado por `LevelLoader` para `DEGENERATE_FOOTPRINT_FRACTION`, con riesgo de reclasificar la colisión de otra geometría cercana al umbral del 70%). Verificado con capturas reales en las 4 zonas antes rotas: ahora muestran una superficie continua con reflejo especular de la luz direccional (no skybox) -- confirmado que ese "brillo raro" en las primeras capturas era un reflejo real sobre el techo, no un hueco. Colisionadores estáticos sin cambio (3385): el plano no genera collider (`dims.y≈0`), es puramente visual.

**2) Jumpscares sin fondo -- causa real encontrada, no era la cámara ni el render.** `iniciarJumpscare()` (agregado en §2.21) retiraba `mapScene` de la escena durante todo el jumpscare -- el animatrónico quedaba flotando contra el skybox vacío en vez de aparecer dentro del comedor real. Confirmado visualmente comparando una captura real con y sin el mapa presente. Corrección: **ya no se retira el mapa**; el encuadre cerrado (`JUMPSCARE_DISTANCIA_BASE`, ya reducido en §2.21) es suficiente para que no se cuelen muebles cercanos en cuadro, verificado tanto para Freddy en el comedor como para Foxy dentro de la Pirate Cove (space más reducido, mayor riesgo de muebles en cuadro -- verificado sin problema).

**3) Freddy volvía a comportarse como guardia estático.** Causa real: `crearGuardia()` inicializaba a los 4 personajes de forma idéntica (`IdleState`, con `rangoDeteccion` como único disparador) -- nunca existió un mecanismo real para que Freddy fuera distinto pese a que un comentario ya lo daba por hecho. Se agregó `AIComponent.siempreEnPersecucion` (true solo para Freddy): `IdleState` transiciona a `ChaseState` en el primer frame sin evaluar distancia, y `ChaseState` ya no vuelve a `IdleState` por alejamiento. Bonnie/Chica/Foxy sin cambios (sigan siendo guardias estáticos por rango). **Verificado que el nivel sigue siendo completable** con Freddy persiguiendo desde el frame 0 (simulación de ruta oficina→salida con los 4 guardias activos, `escapoOk=true`) -- la ruta directa en línea recta se atasca contra el marco de la puerta de la oficina (hueco angosto real, no un bug), pero con una ruta razonable (la misma que usaría un jugador real moviéndose en 2D libre) se completa sin problema.

**4) Nuevas animaciones Walking/Breathing Idle para Freddy -- retargeting completo rehecho.** El usuario dejó `Walking.fbx`/`Breathing Idle.fbx` nuevos en `FDAF assets`. Mismo pipeline que el retargeting original (§2.3): Rokoko Studio Live en Blender 4.2.23 portable (incompatible con 5.x, ver motivo en la memoria de Claude), mismo mapeo manual de 16 huesos Mixamo→`j_*` ya usado para Freddy. Hallazgos nuevos durante el proceso:
- **Root motion real en `Walking.fbx`** (a diferencia del `Walking_InPlace.fbx` original): el hueso raíz (`j_pelvis_02`) traía una deriva neta de ~0.11 unidades por ciclo -- sin corregir, el personaje se deslizaría hacia adelante dentro de su propio ciclo de animación y saltaría de vuelta al inicio en cada loop (efecto "moonwalk"/teletransporte), sumándose además al movimiento real ya manejado por `ChaseState`. Corregido quitando la deriva lineal del canal de ubicación del hueso raíz (conservando la oscilación natural del paso), verificado que el ciclo cierra exacto (posición idéntica en el primer y último frame).
- **Incompatibilidad de exportador de Blender 5.1 con gdx-gltf, encontrada y descartada como causa de un bug distinto:** un primer export hecho con el exportador de Blender 5.1 (tras guardar por accidente el `.blend` en el formato nuevo, perdiendo la compatibilidad con 4.2) mostraba los brazos de Freddy sin animar en el motor real aunque los datos exportados eran correctos (confirmado leyendo el GLB binario directamente: quaternions con variación real por frame) -- **se descartó como causa tras repetir el pipeline entero en Blender 4.2.23 sin tocar 5.1 para ningún paso y obtener exactamente el mismo resultado.** La causa real de por qué la lectura de `Node.rotation` parecía "congelada" resultó ser que ese campo simplemente no refleja el estado animado de gdx-gltf en tiempo de ejecución (confirmado comparando el GLB nuevo contra el viejo ya validado en sesiones anteriores: **el viejo mostraba exactamente el mismo patrón** leyendo `Node.rotation` pese a saberse funcional) -- la animación real se verificó correcta forzando tiempos explícitos del `AnimationController` y comparando las poses resultantes, que sí cambian con el tiempo (confirmado con las piernas, que muestran una pose de pie con los pies juntos en fracción 0.1 del ciclo contra una zancada abierta en fracción 0.5).
- El micrófono/gorro (`Mic_GEO1`) sigue emparentado directamente al objeto armature (no a un hueso de mano) -- limitación preexistente del asset original, no introducida ni empeorada esta sesión. Se investigó reparentarlo a la mano (derecha o izquierda) usando el operador real de Blender (`parent_set(type='BONE')`) pero el resultado no acercó visualmente el prop a ninguna mano en ningún frame probado, lo cual sugiere que su posición nunca estuvo pensada como "sostenido en la mano" sino como un prop estático relativo a la base del personaje -- se dejó como estaba (el motivo real por el que antes "flotaba" era el root motion sin corregir del punto anterior, ya resuelto: ahora la distancia mic↔cadera se mantiene estable en vez de crecer sin límite dentro del ciclo).
- Export final: solo `Object_3` (armadura real de Freddy) + sus mallas descendientes (incluye `Mic_GEO1`), excluyendo `Mixamo_Idle_Source`/`Mixamo_Walk_Source` (andamiaje de retargeting ya presente en el `.blend` desde antes de esta sesión, nunca commiteado como parte del entregable). Validado por reimportación: 59 huesos, 5 mallas, 2 acciones (`Freddy_Idle`/`Freddy_Walk`), bbox idéntico al original.

**5) Idle para Bonnie/Chica/Foxy -- no es posible reutilizar el mismo clip retargeteado, solución más limpia aplicada.** Los 3 personajes usan esqueletos completamente distintos entre sí y respecto al de Freddy (`bip_*` en Bonnie, convenciones propias en Chica/Foxy, ninguno con nomenclatura Mixamo) -- una animación horneada para el esqueleto de Freddy no es aplicable a otro por índice de joint. Además, Chica tiene el skin roto de origen (solo 2/90 mallas realmente skinneadas, ya documentado en la Fase 3) -- retargetearle una animación real sería trabajo perdido. En vez de 3 sesiones más de retargeting por un efecto puramente cosmético ("que respiren y tengan vida", pedido explícito del usuario, sin requerir patrullaje), se extendió la técnica de jitter procedural ya usada para el jumpscare (§2.20) a la jugabilidad normal: `RenderSyncSystem` aplica un balanceo vertical sinusoidal sutil (amplitud 0.025u) a cualquier entidad con `AIComponent` pero sin `AnimationComponent`, con la fase desplazada por posición para que los 3 no respiren sincronizados. Verificado numéricamente que la traslación Y oscila suavemente en el tiempo real de juego.

**6) HUD de corazones con sprites reales.** Reemplazado el dibujo procedural (`ShapeRenderer`, 2 círculos + 1 triángulo) por los sprites `corazon_relleno.png`/`corazon_sin_relleno.png` dejados por el usuario (1024×1024, con canal alfa real -- confirmado leyendo los píxeles de esquina/centro antes de integrar). Mismo comportamiento exacto (intro, pérdida de vida, actualización), solo cambia la representación visual. Copiados a `assets/textures/`.

**7) Risa de Freddy al mostrar RUN.** Encontrado `risa_freddy1.wav` en el proyecto Swing (`FiveDoorsAtFreddys/.../sounds/random/`), copiado a `assets/sounds/risa_freddy.wav`. Se reproduce una sola vez, exactamente en la transición `INTRO_CORAZONES→INTRO_RUN`, con un flag (`risaFreddyReproducida`) que evita repetición incluso si ese estado se reevaluara en el futuro. Verificado con el ciclo completo real (no solo la transición aislada).

**8) Revisión general -- sin bugs adicionales encontrados fuera de los 7 puntos anteriores.**

**Metodología reafirmada, con un caso nuevo importante:** cuando una lectura de estado en tiempo de ejecución (`Node.rotation`) parece contradecir el comportamiento esperado, **hay que verificar contra un caso de control ya conocido como funcional** antes de asumir un bug nuevo -- en este caso, releer el GLB viejo (ya validado en sesiones anteriores) con el mismo método de diagnóstico dio el mismo resultado "congelado", revelando que el método de lectura era el problema, no el asset nuevo. Sin ese control, se habría llegado a una conclusión incorrecta pese a la evidencia real. Toda la instrumentación temporal de esta sesión (bloques `DIAGNOSTICO TEMPORAL` en `GameplayScreen.java`, incluida una simulación completa del ciclo de estados con aserciones de tiempo exacto) se agregó y se retiró por completo al terminar -- confirmado con `git diff` limpio y una compilación final sin advertencias.

**Commiteado y pusheado a `five_doors_escape`:** techo extendido a toda la huella jugable (`Pizzeria.glb`), jumpscare con mapa presente + Freddy persecución continua (`GameplayScreen.java`, `AIComponent.java`, `IdleState.java`, `ChaseState.java`), respiración procedural para guardias estáticos (`RenderSyncSystem.java`), nuevo retargeting Walking/Breathing Idle para Freddy (`Freddy.glb`), HUD de corazones con sprites (`assets/textures/`), risa de Freddy (`assets/sounds/risa_freddy.wav`, `BootScreen.java`).

### 2.23 Animaciones completas para los 4 animatrónicos (Bonnie/Chica/Foxy dejan de ser "congelados"), sesión 2026-08-03/04

**Contexto:** el jitter procedural de §2.22 punto 5 fue un placeholder deliberado, no una solución final. El usuario pidió esta ronda que Bonnie, Chica y Foxy tuvieran retargeting real (Walking + Breathing Idle), investigando y resolviendo cualquier problema de rig/pesos/huesos/export que apareciera, con verificación real en LibGDX después de cada paso — sin dar ninguna animación por correcta solo por haber exportado sin errores.

**1) Causa raíz real del bug de brazos de Freddy (investigación exigida por el usuario, sin asumir nada de antemano).** Se comparó cada etapa del pipeline (FBX de Mixamo → retarget en Blender → `.blend` → `.glb` exportado → reimportación → LibGDX) en vez de asumir dónde estaba el problema. Causa encontrada: `scene.rsl_retargeting_use_pose` (Rokoko Studio Live) estaba en `'REST'` (su valor por defecto, usado sin cuestionar desde el retargeting original de la Fase 3). Con `'REST'`, Rokoko calcula el retarget usando la pose de reposo teórica del target, que no coincide exactamente con la pose real que el rig de Freddy necesita para verse correcto, produciendo el patrón de brazos escondidos detrás del torso. Corrección real (no parche visual): `rsl_retargeting_use_pose='CURRENT'`, con el target explícitamente reseteado a su bind pose (`animation_data.action = None`) justo antes de cada retarget. Verificado reproducible dos veces (Walking e Idle) y confirmado con capturas reales dentro de LibGDX (brazos extendidos correctamente, no escondidos). Este es ahora el ajuste **estándar** para los 4 personajes.

**2) Bonnie — retargeting limpio, sin sorpresas.** Rig `bip_*` bien formado, 16 huesos mapeados manualmente (mismo criterio de Freddy, sin huesos de clavícula/hombro porque ninguno de los 4 personajes los tiene). Único hallazgo: el `Walking.fbx` nuevo (a diferencia del `Walking_InPlace.fbx` de la Fase 3) trae root motion real — corregido restando la deriva lineal del canal de ubicación del hueso raíz entre el primer y el último keyframe (conserva la oscilación natural del paso, solo quita el desplazamiento neto). Verificado con capturas reales en LibGDX: brazo y pierna visiblemente extendidos durante el ciclo, no una figura estática.

**3) Chica — el rig estaba genuinamente roto de origen, no solo "sin retargetear".** Investigación confirmó que 90 de 92 mallas estaban parentadas a una cadena de objetos vacíos (`Empty`) completamente desconectada del armature animable — nunca tuvieron skinning real, ni en la Fase 3 ni antes (dato ya sospechado entonces, confirmado ahora). Solución aplicada (técnica nueva, no usada en los otros 3 personajes): para cada malla sin vertex groups, se calculó el centroide de su bounding box, se buscó el hueso real más cercano por distancia euclidiana a la posición de reposo de ese hueso, y se reparentó con el operador real de Blender `parent_set(type='BONE', keep_transform=True)` (no matemática manual de matrices — ya se había comprobado en la Fase 3 que el cálculo manual de `matrix_parent_inverse` es propenso a error). 89 mallas reparentadas así, verificado visualmente que el personaje se mantiene intacto en pose de reposo antes de continuar. Root motion: la deriva lineal simple no fue suficiente dado el factor de escala extremo del rig de Chica (~40x), así que se aplicó el enfoque más agresivo de anular completamente la traslación del hueso raíz en los 3 ejes (en vez de solo restar la deriva neta). **Nota metodológica importante:** las primeras capturas dentro de LibGDX parecían mostrar a Chica "estirada"/sin piernas visibles, lo que llevó a sospechar un bug nuevo — pero la verificación numérica rigurosa (bounding box real de la malla ya deformada por el armature, calculado con `evaluated_depsgraph_get()`/`to_mesh()` en vez de confiar en la malla sin evaluar) confirmó que las piernas se mantienen correctamente ancladas al piso (Z mínimo consistente en -1.00) en todos los frames muestreados — la apariencia "rara" era un artefacto de encuadre de cámara (la envergadura de brazos, mayor en pose `CURRENT` que en la `REST` original, dominaba capturas muy cerradas), no un defecto real. Antes de dar el retarget por bueno solo con la lectura numérica se hizo también la verificación visual estándar en LibGDX.

**4) Foxy — dos causas raíz genuinas de rig, ninguna relacionada con la animación en sí.** Foxy fue el caso más difícil de los 4: el primer intento de retarget (rig `bip_*` limpio, mismo criterio que Bonnie, aparentemente sin complicaciones) produjo un colapso visual severo — el personaje aparecía tumbado y doblado sobre sí mismo tanto en Walking como en Breathing Idle, confirmado con un render directo en Blender (antes de tocar el exportador de glTF, para descartar de entrada cualquier causa en el export/gdx-gltf). Investigación bone-por-bone (comparando cuantitativamente cada rotación local de Foxy contra el mismo hueso en Bonnie, ya funcional) encontró dos defectos reales y no relacionados entre sí:
   - **Huesos de pie sin hijo (`bip_foot_L/R`) con orientación sintética incorrecta.** Al ser huesos hoja (sin joint hijo en el glTF original), el importador de Blender no tiene forma de inferir su dirección anatómica real y les asigna una "tail" arbitraria apuntando derecho hacia abajo (eje local ≈ `(0,0,-1)`), en vez de la dirección adelante-abajo real de un pie (confirmado comparando contra Bonnie, cuyo hueso de pie sí tiene un hijo real que fija su dirección correctamente: eje local ≈ `(0.03,0.40,-0.91)`). Esa convención de eje local incorrecta (no la animación) hacía que Rokoko retargeteara los pies con un error de rotación de ~170-180°. Corregido dándole a la tail del hueso, en modo edición y antes de retargetear, una dirección anatómica razonable basada en la misma convención ya validada en Bonnie.
   - **Armature parentado dentro de un wrapper de Sketchfab con su propia escala/offset.** A diferencia de Bonnie/Freddy (armature de nivel superior, `matrix_world` identidad), el armature de Foxy venía anidado `Sketchfab_model → Root → foxy.qc_skeleton`, con el escalado real (0.1863x) aplicado en el padre `Root`, no en el propio armature. El retarget de Rokoko no calcula bien la pose cuando el armature target está anidado así — corregido quitando el padre y horneando la transformación resultante directamente en el armature (`parent_clear(type='CLEAR_KEEP_TRANSFORM')`, mismo lugar/escala final, sin la indirección), igualando la estructura que ya funciona en Bonnie/Freddy. Solo tras aplicar **ambas** correcciones el render directo en Blender mostró a Foxy de pie, con los brazos meciéndose y las piernas dando pasos — confirmado después con capturas reales dentro de LibGDX (Walking e Idle), no solo en Blender.
   - Ninguna de las dos causas tiene relación con la animación de origen ni con gdx-gltf — ambas son defectos reales y preexistentes en cómo el asset de Foxy llegó a Blender, invisibles hasta que se intentó animarlo con un método (retarget basado en rotación/pose) que sí depende de la orientación de los huesos y de la jerarquía del objeto.

**5) `RenderSyncSystem` — el jitter procedural de §2.22 queda como respaldo inerte, no se retiró.** Con los 4 personajes teniendo ya `AnimationComponent` real (`animaciones` no vacío en los 4 JSON de `assets/content/entities/`), la condición `Mappers.ai.has(entity) && !Mappers.animation.has(entity)` ya no se cumple para ningún animatrónico actual — se deja el código (documentado como respaldo defensivo, no como mecanismo activo) por si se agrega en el futuro un animatrónico sin animación esquelética propia.

**6) Verificación real en cada paso, tal como pidió el usuario.** Para cada uno de los 4 personajes: retarget en Blender 4.2.23 (Rokoko exige esa versión, incompatible con 5.x) → guardado del `.blend` → export a `.glb` → reimportación de validación (conteo de mallas/huesos/acciones, bbox) → captura real dentro de LibGDX (`AnimationController` forzado a fracciones concretas del ciclo, no lectura de `Node.rotation`, gotcha ya documentado en la memoria de Claude) para Walking e Idle. Ninguna animación se dio por buena solo por haber exportado sin errores — el caso de Foxy es la prueba de que un export "limpio" (sin excepciones, con el número correcto de huesos/acciones) puede contener un rig visualmente roto.

**Commiteado y pusheado a `five_doors_escape`:** retargeting completo de Bonnie/Chica/Foxy (`Bonnie.glb`, `Chica.glb`, `Foxy.glb`), reparentado de mallas de Chica, correcciones de rig de Foxy (orientación de pies + desparentado de wrapper Sketchfab, aplicadas en el pipeline de Blender, no en el `.glb` final a mano), JSON de animaciones (`bonnie.json`, `chica.json`, `foxy.json`), comentario actualizado en `RenderSyncSystem.java`. Toda la instrumentación temporal (`DIAGNOSTICO TEMPORAL` en `GameplayScreen.java`) se agregó y se retiró por completo al terminar, confirmado con compilación final sin advertencias.

### 2.24 Corrección real de Bonnie/Chica "acostados" y brazos rígidos en los 4 animatrónicos, sesión 2026-08-04

**Contexto:** tras §2.23 el usuario probó el juego real y reportó que Bonnie seguía apareciendo prácticamente acostada y que Freddy/Foxy tenían los brazos demasiado rígidos (poco movimiento de codo). Pidió explícitamente no asumir que el retarget ya estaba bien "solo porque Blender se viera correcto" y verificar el pipeline completo paso a paso.

**1) Falso positivo metodológico grave — lección importante.** Al re-verificar Bonnie tras un primer intento de arreglo, un script de render en Blender fallaba silenciosamente (`scene.render.engine = 'BLENDER_EEVEE_NEXT'` no existe en Blender 5.1, solo `'BLENDER_EEVEE'`) **después** de imprimir el bbox pero **antes** de renderizar — dejando en disco un PNG viejo de una ejecución anterior con el mismo nombre. Se interpretó ese PNG viejo como "ya arreglado" y se reportó como tal, cuando en realidad el bug seguía intacto. El usuario lo detectó jugando de verdad. Lección aplicada desde entonces: **siempre borrar el archivo de salida esperado antes de cada captura** y revisar el log completo del script en busca de excepciones antes de confiar en una imagen — un archivo con el nombre correcto no es prueba de que el script haya terminado.

**2) Causa raíz real de Bonnie acostada — el mismo wrapper de Sketchfab que Foxy, nunca antes verificado en Bonnie.** Investigando el pipeline completo (FBX → retarget → `.blend` → `.glb` → reimportación → LibGDX) se confirmó con capturas reales que Bonnie aparecía tumbada tanto en Walking como en Idle, de forma idéntica — descartando que fuera un problema específico de una sola animación. La pose de reposo (bind pose, sin ninguna acción asignada) se veía perfectamente de pie (altura correcta en Z, profundidad correcta en Y) — el bug se introducía exclusivamente al aplicar CUALQUIER animación retargeteada. Se encontró que el armature original de Bonnie (igual que Foxy y Chica) viene de Sketchfab, envuelto en `bonnie.qc_skeleton → Root → Sketchfab_model`, con `Sketchfab_model` cargando una rotación real de ~90° horneada (no solo la conversión Y-up/Z-up esperada) — nunca se había revisado esta cadena de padres para Bonnie porque el trabajo de la ronda anterior se centró en el offset de traslación del hueso raíz de Idle (~39 unidades, real pero secundario). El retarget de Rokoko calcula su math en espacio mundo usando la jerarquía completa (incluyendo esa rotación espuria), pero el export posterior descarta la contribución de un padre no seleccionado — produciendo una pose de reposo correcta pero una animación retargeteada desalineada 90°. Corregido con la misma técnica ya usada en Foxy y Chica: `parent_clear(type='CLEAR_KEEP_TRANSFORM')` antes de retargetear. Verificado con render directo en Blender (3 vistas) y con capturas reales dentro de LibGDX en su spawn real (sin mover al personaje), confirmando a Bonnie de pie, caminando y respirando correctamente.

**3) Brazos rígidos en Freddy y Foxy — medición original con un método defectuoso, causa real distinta para cada personaje.** El primer intento de medir el ángulo del codo (usando `tail - head` de cada hueso) resultó poco confiable: en huesos importados desde glTF la "tail" de un hueso con hijo real puede no coincidir con la cabeza de su hijo (la tail es cosmética para skinning, que solo depende de la posición del joint) — confirmado comparando cabeza del hijo contra cola del padre. Se corrigió midiendo el ángulo real con las posiciones de `head` (joint origin, siempre significativas) del hombro, codo y mano. Con esa medición confiable:
   - **Freddy:** el codo retargeteado solo alcanzaba ~12-23° de flexión real contra ~32-47° de la fuente Mixamo original (medida con el mismo método) — una atenuación real de aproximadamente la mitad, reproducible incluso recién horneado (antes de exportar/reimportar), y no explicada por `auto_scaling` (probado desactivándolo: resultado casi idéntico). No se encontró una causa raíz exacta dentro del código del addon de Rokoko (constraints en espacio mundo, influencia 1.0, sin mezcla parcial visible) pese a revisar su lógica de bake. Corregido con una amplificación angular dirigida: se descompone el quaternion local del antebrazo en eje+ángulo por keyframe y se amplifica el ángulo (factor 2.2, preservando el eje y el timing real del ciclo) tanto en Walking como en Idle. Verificado visualmente en LibGDX: flexión de codo natural y visible en ambos brazos.
   - **Foxy:** el codo casi no tenía flexión visible (177-180°, prácticamente recto) y amplificar el ángulo original apenas ayudó (171° incluso con factor 3.0) — un test directo rotando el hueso del antebrazo 60° en cada eje local por separado (X/Y/Z) confirmó que el retarget original rotaba predominantemente sobre el eje Y local del hueso (torsión pura, sin flexión visible posible) en vez del eje X (el que sí produce una flexión de codo real y visible, confirmado con el mismo test). Corregido redirigiendo la MAGNITUD de rotación ya calculada por el retarget (que sí sigue el timing real del ciclo de caminata) al eje X en vez de descartarla — se usa el signo de la componente Y original como proxy de "flexión hacia adelante vs. hacia atrás" en cada punto del ciclo. Verificado con render directo en Blender: flexión de codo natural y visible en ambos brazos.
   - Bonnie y Chica no fueron reportadas con brazos rígidos por el usuario en esta ronda y no se modificaron.

**4) Spawn del jugador movido hacia atrás en la oficina.** Se sondeó el `CollisionWorld` con las halfExtents reales de colisión del jugador para encontrar cuánto se podía avanzar hacia +Z (dirección "atrás", dado que `playerStartYawDegrees=180` mira hacia -Z) antes de tocar la pared trasera: límite libre en Z=10.5 desde el spawn original en Z=8.0. Se movió el spawn a Z=10.0 (`pizzeria.json`), dejando un margen de seguridad de 0.5 unidades, misma orientación. Verificado sin advertencia de embeddeo y con una captura real desde la vista exacta del jugador mostrando el escritorio de la oficina correctamente encuadrado.

**5) Verificación real en cada paso, reforzada tras el falso positivo del punto 1.** Para cada corrección: cambio en el pipeline de Blender → verificación numérica (ángulos/posiciones reales, no solo "se exportó sin errores") → render directo en Blender con log revisado en busca de excepciones → captura real dentro de LibGDX en la posición REAL de spawn del personaje (no una posición de diagnóstico movida) cuando fue posible, usando sondeo del `CollisionWorld` para encontrar ángulos de cámara realmente libres en vez de coordenadas fijas adivinadas.

**Commiteado y pusheado a `five_doors_escape`:** desparentado de Bonnie del wrapper de Sketchfab (`Bonnie.glb`), corrección de flexión de codo en Freddy y Foxy (`Freddy.glb`, `Foxy.glb`), spawn del jugador movido hacia atrás (`pizzeria.json`). Toda la instrumentación temporal se agregó y se retiró por completo al terminar, confirmado con `git diff` limpio en `GameplayScreen.java` y una compilación final sin advertencias.

### 2.25 Radio de salida, causa raíz universal de la rigidez de brazos, accesorios sujetos a la mano y primeras mejoras visuales, sesión 2026-08-04

**Contexto:** el usuario pidió, en orden: (1) reducir el radio de activación de la puerta de escape; (2) investigar la causa raíz REAL (no un parche visual) de por qué los 4 animatrónicos seguían con brazos/hombros rígidos pese a §2.24, reinvestigando todo el pipeline si hacía falta; (3) resolver los accesorios flotantes (micrófono de Freddy, guitarra de Bonnie) sujetándolos de verdad a la mano o eliminándolos si no era técnicamente posible; (4) solo después de lo anterior, investigar (sin implementar a ciegas) mejoras visuales de bajo costo. Pidió pruebas reales continuas dentro de LibGDX antes de dar cualquier tarea por terminada.

**1) Radio de salida.** `exitRadius` de `pizzeria.json` bajado de 1.5 a 0.6 unidades (jugador halfExtents 0.3x0.9x0.3). Verificado con `CollisionWorld.overlapsStatic` que el punto exacto de salida sigue siendo alcanzable (espacio libre, sin embeddeo) antes de aceptar el cambio.

**2) Causa raíz universal de la rigidez de brazos/hombros (la investigada en profundidad esta ronda).** Dos teorías se probaron y se descartaron explícitamente antes de llegar a la real: (a) degeneración numérica en el cálculo de bone-roll de Rokoko (`vec_roll_to_mat3`/`mat3_to_vec_roll`) — reproducido el cálculo exacto para huesos de pierna/columna/brazo de Freddy, sin ningún caso degenerado; (b) comparar directamente la dirección de hueso en espacio mundo entre el FBX fuente de Mixamo y el target en Blender — inválido, porque FBX y glTF usan convenciones de eje "arriba" distintas tras la importación, haciendo la comparación sin sentido. La causa real, encontrada por análisis geométrico (no visual): Rokoko retiene correctamente la rotación real en ESPACIO MUNDO (constraint `COPY_ROTATION`, target/owner ambos `WORLD`, verificado por introspección directa), pero al hornear esa rotación como quaternion LOCAL en la convención de bone-roll propia del rig de destino, el movimiento real de bisagra del codo/hombro (que en el rig fuente de Mixamo es un giro limpio de un solo eje) queda redirigido al eje LOCAL Y del hueso (torsión/longitud) en vez del eje de bisagra anatómico real — un desajuste de convención de bone-roll entre Mixamo y cada rig de destino, específico de los huesos de BRAZO (piernas y columna no lo sufren, sus convenciones de roll sí coinciden razonablemente). Confirmado como patrón universal en los 4 personajes calculando geométricamente, para cada hueso de brazo, cuál de sus 3 ejes locales (X/Y/Z, en pose de reposo) coincide mejor con el eje de bisagra esperado (`direccion_hueso × eje_izquierda-derecha_del_personaje`) — en los 4 casos el retarget dominaba sobre el eje Y (incorrecto) en vez del eje geométricamente correcto. Corregido redirigiendo la MAGNITUD y el TIMING reales de la rotación ya calculada por el retarget (no descartándolos) al eje correcto, amplificados con factor 2.2 y tope de 170°, aplicado de forma uniforme a los 4 personajes vía un único script. Verificado con mediciones angulares reales (basadas en `head`, nunca en `tail` — ver §2.24 punto 3 y la memoria de Claude sobre esto), renders directos en Blender y capturas reales dentro de LibGDX.

**3) Accesorios sujetos a la mano — dos bugs reales del exportador de glTF de Blender, no solo "falta de parentado".** Intento inicial: `parent_set(type='BONE', keep_transform=True)` tras resetear el armature a bind pose (misma técnica ya usada con éxito en la ronda anterior para Foxy). Con la guitarra de Bonnie (piezas de malla planas, sin objeto intermedio) funcionó correctamente a la primera. Con el micrófono de Freddy aparecieron dos problemas reales, encontrados solo verificando el resultado EXPORTADO y reimportado (no la vista previa en memoria dentro de la misma sesión de Blender, que engañosamente se veía bien):
   - **Desplazamiento espurio del exportador ligado a la longitud del hueso.** Para el hueso de la mano de Freddy, la "tail" (cosmética, ver lección ya conocida) está a ~1.07 unidades de la "head" — casi 8 veces la altura completa de Freddy (~0.13, por su escala de armature 0.01 preexistente). El exportador de Blender introduce un error de traslación de exactamente esa magnitud y dirección al convertir el bone-parent (space relativo a la tail, convención interna de Blender) al espacio de nodo/joint de glTF (relativo a la head). Confirmado midiendo el vector de error real tras exportar+reimportar y comparándolo con la dirección/longitud de la tail. Corregido con una compensación calibrada empíricamente (restar el vector de error medido del objetivo deseado antes de hornear) — no es una solución general para cualquier rig, pero es correcta y verificada para este caso concreto.
   - **El pivote del objeto del micrófono no coincide con su geometría.** El objeto `Mic_GEO1_MicAndHat_0` tiene su malla real desplazada ~0.09 unidades de su propio pivote (origen del objeto) — mover el pivote a la mano no mueve la malla visible al mismo punto. Corregido calculando el centro geométrico real (bounding box de los vértices) en vez de confiar en `matrix_world.translation` del objeto.
   - Tras corregir ambos, se ajustó a mano (con renders de verificación iterativos) un pequeño desplazamiento a lo largo de la dirección antebrazo→mano para que el micrófono sobresalga de la malla de la mano en vez de quedar enterrado dentro de ella.
   - Verificado con capturas reales dentro de LibGDX (no solo Blender): la guitarra se ve correctamente sujeta en la mano de Bonnie tanto en Idle como en Walking (varios frames), y el micrófono de Freddy aparece rígidamente unido a su mano en Walking real (confirmado con una captura muy cercana dirigida específicamente a esa mano — a la distancia normal de cámara el objeto es demasiado pequeño para notarse a simple vista, lo cual no es un defecto del fix sino del tamaño real del asset).

**4) Mejoras visuales de bajo costo — investigadas antes de implementar, solo se aplicaron las nativas de gdx-gltf.** Se revisó el código fuente de `gdx-gltf` (jar de sources) para ver qué soporta de forma nativa sin necesitar un pipeline de postprocesado propio: sí tiene sombras direccionales reales (`DirectionalShadowLight`, detectadas y renderizadas automáticamente por `SceneManager.render()` sin más cambios) y niebla (`FogAttribute`, atenúa hacia el color de fondo); NO tiene bloom, SSAO ni corrección de color/tonemapping propios — implementarlos habría requerido un pipeline de postprocesado por FrameBuffer escrito desde cero, desproporcionado para lo pedido ("no implementar efectos pesados solo porque existen"). Se aplicaron solo las 3 mejoras nativas y baratas: (a) `DirectionalLightEx` reemplazada por `DirectionalShadowLight` con el viewport de sombra dimensionado sobre la huella jugable real de `pizzeria.json` (no un valor adivinado); (b) luz ambiente bajada de 1.0 a 0.35 — con ambiente al máximo las sombras nuevas casi no se notaban y la escena se veía plana; (c) niebla sutil (`FogAttribute.createFog(9, 22, 2)`) que funde hacia el negro del `glClearColor` existente, cubriendo los pasillos más largos sin ocultar nada a distancia normal de juego. Verificado con capturas reales dentro de LibGDX: la escena se ve notablemente más atmosférica/oscura sin perder legibilidad, sin caída de rendimiento perceptible.

**5) Lección operativa importante — contaminación de procesos huérfanos entre ejecuciones de `gradlew lwjgl3:run`.** Al usar `timeout N ./gradlew.bat lwjgl3:run` repetidamente para tomar capturas de verificación, el proceso Java real del juego (`Lwjgl3Launcher`) es lanzado por el DAEMON de Gradle (persistente), no por el wrapper envuelto en `timeout` — así que `timeout` mata el wrapper pero el juego sigue corriendo indefinidamente en segundo plano. Tras varias ejecuciones sin cerrar manualmente cada ventana, se acumularon múltiples instancias del juego compitiendo por la GPU, causando que una ejecución posterior (con cambios de iluminación reales, no rotos) pareciera colgarse o tardar anormalmente. Diagnosticado listando procesos `java.exe` reales con PowerShell (`Get-CimInstance Win32_Process`) y confirmando por línea de comandos cuáles eran instancias huérfanas de `Lwjgl3Launcher`; corregido matándolas (`Stop-Process`) antes de repetir la prueba, que entonces funcionó de inmediato. **Lección para el futuro:** tras cada `gradlew lwjgl3:run` de diagnóstico, verificar y cerrar el proceso `Lwjgl3Launcher` resultante explícitamente en vez de asumir que `timeout`/el fin del comando de fondo ya lo hizo.

**Commiteado y pusheado a `five_doors_escape`:** radio de salida reducido (`pizzeria.json`), corrección universal de rigidez de brazos/hombros en los 4 animatrónicos (`Freddy.glb`, `Bonnie.glb`, `Chica.glb`, `Foxy.glb`), micrófono de Freddy y guitarra de Bonnie sujetos correctamente a la mano (mismos `.glb`), sombras direccionales + niebla sutil + ambiente reducido (`GameplayScreen.java`). Toda la instrumentación temporal (capturas de diagnóstico) se agregó y se retiró por completo al terminar, confirmado con `git diff` limpio y una compilación final sin advertencias.

### 2.26 Brazos realmente naturales (retargeting propio por conjugación), regresión real de la puerta de salida y sistema de latidos/viñeta, sesión 2026-08-04 (misma fecha, sesión posterior)

**Contexto:** tras jugar una partida completa con los cambios de §2.25, el usuario reportó que los brazos ya no estaban en T-pose pero seguían viéndose "rígidos o descontrolados", pidió explícitamente no parchar sino encontrar la causa real; además reportó una regresión real (la puerta de salida ahora se podía atravesar caminando, en vez de requerir tocarla) y pidió un sistema nuevo de latidos de tensión + viñeta.

**1) Por qué el fix de §2.25 (redirigir a un solo eje + amplificar ×2.2) no era suficiente — el verdadero problema nunca fue el eje.** Se volvió a abrir el `.blend` de trabajo original de cada personaje (`Freddy_WIP.blend`, etc., en `Blender_WIP/`, que conserva el bake crudo de Rokoko sin ninguno de los parches de rondas anteriores) y se renderizó directamente esa animación cruda: los brazos aparecían completamente abiertos, como un espantapájaros — el problema real nunca fue "el eje equivocado" sino que **el retarget de Rokoko transfiere la rotación COMPLETA del hueso "Arm" de Mixamo (que en su propio rig de origen incluye un enorme componente constante: la reorientación de su T-pose de reposo, con los brazos horizontales, a una postura de brazos hacia abajo)**, y ese enorme componente constante no tiene sentido aplicado sobre el rig de destino, cuyo hueso de hombro YA arranca en una pose de reposo distinta (para Freddy, ~129° respecto a la vertical del cuerpo — ni T-pose horizontal ni brazos pegados al cuerpo, una postura intermedia propia de este rig). Confirmado numéricamente comparando, hueso por hueso, el ángulo real (no visual) del brazo retargeteado contra el ángulo del propio hueso "Arm"/"ForeArm" de Mixamo en el FBX crudo (`Walking.fbx`, reimportado fresco, no el `Mixamo_*_Source` ya presente en el `.blend` -- ese armature conservaba las acciones huérfanas pero SIN el objeto fuente real, confirmado con `animation_data.action is None`).

**2) La solución real: retargeting propio por conjugación de quaternions, no otro ajuste empírico de ejes.** En vez de seguir parchando el resultado de Rokoko, se implementó un método de retargeting directo y matemáticamente principal para hombro y codo (columna/piernas/cadera de §2.23-2.24 se dejaron intactos, ya funcionaban bien):
   - Se mide, en la pose de reposo de AMBOS rigs (fuente Mixamo y destino), la rotación del hueso del brazo relativa a su hueso padre (hombro→columna, codo→hombro) y se calcula `M`, la rotación que alinea la convención de bone-roll del hueso fuente con la del hueso destino (`M = Q_reposo_destino_relativo · Q_reposo_fuente_relativo⁻¹`).
   - Para cada frame del clip original de Mixamo, se calcula la rotación relativa actual del hueso fuente (relativa a su padre, en espacio mundo) y se le resta el PROMEDIO de esa rotación a lo largo de todo el ciclo — extrayendo así solo la OSCILACIÓN real del ciclo de marcha (el vaivén natural), descartando por completo el componente constante de reorientación T-pose→postura que no aplica al rig de destino.
   - Esa oscilación pura se conjuga por `M` (`M · oscilación · M⁻¹`) para expresarla en la convención del hueso destino, se atenúa con un factor (0.45, calibrado con mediciones reales de ángulo final para que el balanceo del brazo quede entre ~6° y ~26° respecto a la vertical del cuerpo — un vaivén natural, no exagerado) y se le añade una corrección CONSTANTE calculada aparte: la rotación necesaria (conjugada correctamente a espacio local del hueso, no aplicada directamente en espacio mundo — error real cometido y corregido en el camino, verificado con una prueba aislada que mostraba el ángulo final incorrecto hasta hacer esa conjugación) para llevar el brazo desde su dirección de reposo original hasta una dirección "colgando junto al cuerpo" (12° respecto a la vertical, hacia el lateral correcto de cada personaje).
   - Aplicado de forma uniforme a los 4 personajes (Freddy, Bonnie, Chica, Foxy) vía un solo script parametrizado por los nombres de hueso de cada rig. Reemplaza por completo el enfoque de §2.24/§2.25 (redirección a un eje "geométricamente correcto" + amplificación ×2.2) — ya no queda ningún vestigio de ese método en el pipeline.
   - Verificado con renders directos en Blender para los 4 personajes y con capturas reales dentro de LibGDX (los 4 guardias en su spawn real, en pleno ciclo de Walking): brazos relajados, cerca del cuerpo, con un vaivén natural visible durante la marcha — exactamente lo pedido.

**3) Regresión real de la puerta de salida — la coordenada de "victoria" nunca coincidía con la geometría real de la puerta.** Investigado con un sondeo real de `CollisionWorld.overlapsStatic` en una grilla 2D alrededor de `exitX`/`exitZ`: la hoja de puerta real (nodo `Object_60`, hijo de "puerta.005_46") estaba centrada en `(-8.87, -6.32)`, mientras que `exitX`/`exitZ` en `pizzeria.json` apuntaban a `(-7.5, -7.0)` — casi 1.5 unidades de distancia, en una zona de piso abierto que ninguna colisión protegía nunca. Es decir: el jugador podía llegar a la zona de "victoria" caminando por un pasillo real, sin necesidad de tocar (ni siquiera acercarse a) la puerta cerrada — de ahí la regresión percibida ("se puede atravesar la puerta"). Causa técnica de fondo (necesaria para que esto pasara): `LevelLoader.esParteDePuertaOCortina` trata CUALQUIER nodo cuyo ancestro se llame "puerta..." como permanentemente abierto/pasable (mecanismo correcto y necesario para las puertas interiores reales, ver §2.20) — pero la puerta de salida es la ÚNICA que debe seguir siendo sólida. Corregido:
   - `LevelLoader.buildStaticColliders`/`collectDoorZones` ahora reciben `exitX`/`exitZ` y excluyen de ese despejado automático cualquier nodo "puerta..." cuyo centro esté a menos de 2 unidades de la salida real -- esa puerta específica pasa a generar un collider sólido normal, como cualquier otra geometría del mapa.
   - `exitX`/`exitZ` en `pizzeria.json` corregidos para coincidir con el centro real de `Object_60` (antes apuntaban a piso abierto sin relación con la puerta).
   - `exitRadius` recalibrado de 0.6 a 0.75: sondeo fino confirmó que, empujando contra la puerta ya sólida, el jugador nunca puede acercarse a menos de ~0.65 unidades del centro real antes de quedar bloqueado por la colisión — 0.6 habría dejado la puerta técnicamente imposible de "tocar" lo suficiente para ganar.
   - Verificado con un autopiloto real dentro de LibGDX (movimiento programático hacia la puerta vía `CollisionWorld.resolveMovement`, no teclas simuladas): el jugador avanza con normalidad hasta quedar bloqueado por la puerta real, y la victoria se dispara justo al cruzar el nuevo `exitRadius` — no antes, no caminando alrededor.

**4) Sistema de latidos de tensión + viñeta (pedido nuevo del usuario).** Un solo sistema global (nunca varios sonidos superpuestos): cada frame se calcula la distancia real al animatrónico MÁS CERCANO (de los 4 guardias) y se deriva una "intensidad" normalizada (0 más allá de 7 unidades, 1 por debajo de 3 unidades — rangos elegidos contra el tamaño real de la huella jugable de la pizzería, ~18×17 unidades, y contra `rangoDeteccion=8` ya usado por `AIComponent`). Esa intensidad controla dos cosas a la vez: (a) qué clip de latido suena (`latido_normal.wav`/`latido_rapido.wav`, con histéresis de 0.5 unidades en el umbral para no parpadear entre ambos) y su volumen (interpolado, no un salto brusco al entrar en rango); (b) la opacidad de una viñeta generada en código (gradiente radial vía `Pixmap`, sin depender de ningún archivo de imagen nuevo), dibujada como overlay de pantalla completa con `SpriteBatch`. Verificado con capturas reales dentro de LibGDX en 3 distancias (lejos/media/cerca): silencio y sin viñeta lejos, viñeta sutil con latido normal a distancia media, viñeta marcadamente más oscura con el estado cambiado a "rápido" de cerca — confirmado también por log que el cambio de estado ocurre exactamente en los umbrales calculados.

**Commiteado y pusheado a `five_doors_escape`:** retargeting propio por conjugación de quaternions para brazos/hombros en los 4 animatrónicos (`Freddy.glb`, `Bonnie.glb`, `Chica.glb`, `Foxy.glb`, reemplaza el método de §2.25), corrección de la puerta de salida (`pizzeria.json`, `LevelLoader.java`), sistema de latidos + viñeta (`GameplayScreen.java`, `BootScreen.java`, nuevos `assets/sounds/latido_normal.wav`/`latido_rapido.wav`). Toda la instrumentación temporal (sondeos de colisión, autopiloto, capturas de diagnóstico) se agregó y se retiró por completo al terminar, confirmado con `git diff` limpio y una compilación final sin advertencias.

---

### 2.27 Cinemática inicial de 17s, ambientación dramática con linterna del jugador, viñeta más oscura y bloqueo real de Pirate Cove, sesión 2026-08-04/05

**Contexto:** con los brazos/puerta/latidos de §2.26 ya aprobados y explícitamente protegidos ("no quiero que cambies nada de eso"), el usuario pidió: (1) una cinemática de ~17s al inicio, con los 4 animatrónicos completamente inmóviles y una cámara lenta "inspeccionando" el lugar antes de que Freddy empiece a perseguir; (2) investigar una ambientación más oscura y dramática sin simplemente bajar el brillo global; (3) una linterna que siga la cámara del jugador; (4) la viñeta un poco más oscura, sin tocar su lógica; (5) corregir que el jugador podía entrar caminando a Pirate Cove (solo Foxy debía poder atravesar la cortina).

**1) Ambientación dramática + linterna (§53/§54, resueltos juntos).** En vez de bajar el brillo global, se rediseñó la iluminación en capas: `sceneManager.setAmbientLight` bajó de 0.35 a 0.12 (sombras reales, no un gris uniforme), la `DirectionalShadowLight` pasó a un tono azulado frío (0.55, 0.62, 0.85 — "luz de luna nocturna") con intensidad reducida a 1.1, y la iluminación principal real pasa a ser una `SpotLightEx` (cono de 28°/14°, alcance 11m, color cálido) que sí sigue la posición/dirección de la cámara cada frame. Requirió descubrir que `PBRShaderProvider.createDefault(int)` deja `numSpotLights=0` por defecto (heredado de `DefaultShader.Config` de libGDX, no de gdx-gltf) — sin error visible, la luz simplemente no se renderiza; corregido construyendo un `PBRShaderConfig` manual con `numSpotLights=1`. Verificado con capturas reales: contraste dramático, mapa visible pero con sombras profundas, linterna dirigiendo la atención sin ser exagerada.

**2) Viñeta más oscura (§55).** Cambio de una sola constante, `VINETA_ALPHA_MAXIMA` de 0.6 a 0.75, sin tocar ninguna lógica existente — tal como se pidió explícitamente.

**3) Bloqueo real de Pirate Cove (§56) — nueva capa de colisión "solo jugador".** El jugador podía atravesar la cortina de Pirate Cove porque `LevelLoader.esParteDePuertaOCortina` trata cualquier "cortina"/"puerta" como permanentemente pasable (mecanismo correcto para las puertas interiores, necesario para que la IA/Foxy no quede atascada). En vez de tocar ese mecanismo compartido, se añadió una segunda lista de colliders en `CollisionWorld` (`collidersSoloJugador`) que solo se consulta cuando `resolveMovement`/`overlaps` reciben `esJugador=true` — el jugador la pasa, los guardias (incluido Foxy vía `ChaseState`) siguen usando el `resolveMovement` de 3 argumentos original, que nunca la ve. `LevelLoader` ahora registra ahí la caja de la cortina (con el mismo margen de despeje que las puertas normales). Verificado con un autopiloto real dentro de LibGDX: el jugador avanza con normalidad y queda bloqueado en el borde real de la cortina; Foxy no se ve afectado en absoluto (su ruta de resolución de movimiento es literalmente otro método).

**4) Cinemática inicial de 17s (§52) — el hallazgo más importante de la sesión.** Los 4 animatrónicos se congelan reutilizando el patrón ya probado de `INTRO_CORAZONES`/`INTRO_RUN`: el nuevo estado `CINEMATICA_INICIAL` simplemente nunca llama a `engine.update()`, lo cual detiene tanto la IA (ni siquiera `AIComponent.siempreEnPersecucion` puede transicionar a Freddy si `IdleState.update()` no se ejecuta ni una vez) como las animaciones, sin necesitar lógica de "pausa" dedicada.
   - **Intento 1 (cámara continua por un único pasillo) — descartado.** La primera versión intentó un solo recorrido de cámara a lo largo de la línea Z=1 (la única fila totalmente libre de colisionadores generales en todo el mapa, confirmado con un volcado ASCII completo), girando la mirada hacia cada animatrónico al pasar cerca. Capturas reales mostraron composiciones rotas (paredes pegadas al lente, iluminación sin sentido) en casi todo el recorrido. Un diagnóstico de raycasting por pasos (muestreo de `CollisionWorld.overlapsStatic` cada 0.25m entre cámara y objetivo) confirmó la causa real: aunque la posición de la cámara sí estaba libre, la MIRADA hacia animatrónicos alejados de esa línea (p.ej. Bonnie en Z=6) atraviesa las paredes que dividen la pizzería en habitaciones/cabinas separadas — no existe un único punto sobre esa línea con visión libre a los 4 a la vez.
   - **Solución final: 5 tomas fijas tipo "cámara de seguridad" con corte duro entre cada una.** Se abandonó el recorrido continuo por un diseño de tomas independientes (Foxy → Bonnie → Chica → Freddy → retorno al escritorio del jugador, ~3.4s cada una), cada una con su propia posición/objetivo de cámara verificados individualmente (posición libre de colisión + muestreo por pasos del trayecto a la mirada, sin bloqueos). Un fundido a negro breve (0.35s, vía `ShapeRenderer` reutilizando el patrón ya usado por el botón de salir) vende los cortes como transiciones intencionales de cámara de seguridad, no saltos bruscos — encaja temáticamente con el propio juego. Dentro de cada toma, un leve giro de mirada (o un pequeño acercamiento de posición en el caso de Bonnie) con smoothstep mantiene la sensación de "cámara lenta e inspeccionando" sin necesitar viajar por el mapa.
   - **El bug real (y el más importante de encontrar): los 4 animatrónicos renderizaban invisibles durante toda la cinemática, en la matriz identidad (el origen del mundo) en vez de su posición real de spawn.** Descubierto tras verificar exhaustivamente que las posiciones/objetivos de cámara eran correctas (LOS libre, sin bloqueos) y aun así ningún personaje aparecía en las capturas — ni siquiera en una toma amplia y muy iluminada apuntada directamente a sus coordenadas de spawn. Causa raíz: `RenderSyncSystem` (que vuelca `TransformComponent` al `ModelInstance.transform` de cada personaje) y el bind inicial de la animación idle de `AnimationSystem` solo se ejecutan dentro de `engine.update()` — que la cinemática deliberadamente nunca llama, para congelar a los personajes. Sin ninguna pasada previa, cada personaje queda en la transformación por defecto tras su creación, invisible para cualquier cámara apuntada a su spawn real. Corregido con `posicionarModelosParaCinematica()`: saca temporalmente `AISystem` del engine, hace una única pasada de `engine.update(0f)` (con `dt=0` no avanza ningún clip, solo aplica la pose/posición inicial), y devuelve `AISystem` — así se posicionan los modelos y se bindea la animación idle sin que `IdleState.update()` llegue a ejecutarse ni una vez (evita que `AIComponent.siempreEnPersecucion` pase a Freddy a `ChaseState` incluso con `dt=0`). Verificado con capturas reales: los 4 animatrónicos visibles y correctamente iluminados, incluida una toma final que coincide con el escritorio clásico de oficina justo antes de que empiece `INTRO_CORAZONES`.
   - **Nota menor no bloqueante:** Foxy/Bonnie/Chica se ven de espaldas en sus tomas (Freddy de frente); cada personaje tiene su propia convención de "adelante" heredada de su rig de origen y no se investigó a fondo cuál es para cada uno. Los 4 quedan claramente visibles e iluminados, que era el requisito funcional — ajustar el ángulo de cada toma para mostrar el frente de los 3 restantes queda como posible pulido futuro, no crítico.

**Commiteado y pusheado a `five_doors_escape`:** cinemática inicial de 5 tomas con cortes de cámara de seguridad (`GameplayScreen.java`), reposicionamiento inicial de modelos vía pasada única de `engine.update(0f)` sin `AISystem`, ambientación dramática por capas + linterna del jugador (`SpotLightEx`, `PBRShaderConfig` con `numSpotLights=1`), viñeta al 0.75, y capa de colisión `collidersSoloJugador` para bloquear a Pirate Cove solo al jugador (`CollisionWorld.java`, `LevelLoader.java`). Toda la instrumentación temporal (grillas de colisión, diagnósticos de LOS, autopiloto, capturas) se agregó y se retiró por completo al terminar, confirmado con una compilación final sin advertencias y una corrida real completa (cinemática → intro → juego) sin excepciones.

---

### 2.28 Reencuadre de la cinemática, pared atravesable real de Pirate Cove, viñeta al 0.82 e investigación de mejoras visuales (IBL/niebla), sesión 2026-08-05

**Contexto:** ronda de pulido sobre el resultado de §2.27, que el usuario calificó de "salto enorme de calidad" y pidió no tocar. Pidió explícitamente: (1) mejorar el encuadre de Foxy/Bonnie/Chica en la cinemática, que aparecían de espaldas o mirando a una pared; (2) revisar a fondo una pared todavía atravesable cerca de Pirate Cove encontrada jugando; (3) oscurecer un poco más la viñeta; (4) investigar mejoras visuales razonables de LibGDX/gdx-gltf (niebla, sombras, contraste, color grading, AO, materiales PBR) sin sacrificar rendimiento; (5) seguir jugando y corrigiendo detalles menores.

**1) Reencuadre de la cinemática — los 4 animatrónicos comparten la misma dirección de "frente" en espacio de mundo.** Comparando el vector cámara→personaje de la toma de Freddy (que ya mostraba su frente, sin tocar) contra el mismo vector en Foxy/Bonnie/Chica (que mostraban su espalda), los 4 apuntan aproximadamente en la misma dirección (-X,+Z) — los 4 comparten `yaw=0` sin rotación propia por personaje (su pose real se calibró en rondas anteriores, no se tocó aquí). Freddy se veía bien por pura coincidencia de en qué lado había quedado su cámara; los otros 3 quedaron del lado opuesto. Corrección: reposicionar la cámara de cada uno al mismo lado (-X,+Z) de su spawn que ya tenía Freddy, verificado libre de colisión (posición + trayecto completo hacia la mirada, mismo método de §2.27) antes de fijar los valores. Resultado verificado con capturas reales: Foxy de perfil mirando hacia la salida de Pirate Cove, Bonnie de frente con un pasillo/puerta visible detrás, Chica de frente con su bib "Let's Eat!!!" y el comedor completo visible — los 4 leen como "esperando" en vez de dar la espalda.

**2) Pared atravesable real cerca de Pirate Cove — causa raíz encontrada por introspección directa, no por prueba y error.** En vez de seguir sondeando la grilla de colisión a ciegas, se listaron directamente (introspección real del árbol de nodos, no muestreo) todas las cajas de "zona de puerta" que `LevelLoader.collectDoorZones` genera cerca de Pirate Cove: 14 fragmentos de `base_39` (la tarima/piso decorativo de la Cove, ~0.42 de alto) más la cortina real. Causa raíz: `esParteDePuertaOCortina` trataba a `base_39` exactamente igual que a las puertas/cortinas reales, metiéndolo en la MISMA lista (`puertaZonas`) que `subdivideNode` usa para despejar celdas de pared subdividida cercanas (a propósito ignorando el eje Y, válido para huecos reales de puerta de piso a techo). Como la huella X/Z combinada de los 14 fragmentos de piso cubre casi todo el interior de la Cove, esa misma lista también despejaba —sin querer— celdas de PARED real de altura completa cuya huella coincidía con la del piso, dejando huecos atravesables en paredes que nunca debieron perder su colisión. Corregido separando `base_39` en una lista propia (`pisosDecorativosSinColision`) que solo se usa para que el propio fragmento de piso no se convierta en collider (su propósito original), nunca para despejar celdas de pared ajenas — el comportamiento real de puertas/cortinas queda intacto. Verificado con un barrido real (mismo `resolveMovement` con `esJugador=true` que usa el jugador real) empujando desde 8 puntos distintos dentro de la Cove hacia cada pared del perímetro más los puntos de transición pared/hueco: los 3549 colisionadores resultantes (antes 3432) bloquean correctamente en los 11 puntos probados, sin ninguna fuga.

**3) Viñeta al 0.82** (de 0.75) — mismo cambio de una sola constante que en §2.27, sin tocar lógica.

**4) Investigación de mejoras visuales — dos cambios de bajo riesgo implementados, un post-proceso completo descartado a propósito.** Se investigaron niebla, sombras, contraste, color grading, ambient occlusion, materiales PBR y aspecto de animatrónicos/escenario contra las capacidades reales de gdx-gltf (código fuente de la librería inspeccionado directamente, no supuesto):
   - **`IBLBuilder.createIndoor(luzDireccional)` en vez de `createOutdoor`** — implementado. `createOutdoor` genera un gradiente de "cielo diurno" brillante que gdx-gltf usa para las reflexiones/iluminación ambiental especular de los materiales PBR (metal de los animatrónicos, superficies brillantes), contradiciendo la ambientación nocturna oscura ya establecida. `createIndoor` (un factory method dedicado que ya existe en la librería para exactamente este caso) da un ambiente cálido y tenue mucho más coherente. Verificado con capturas reales: sin regresión, tono ligeramente más rico y coherente.
   - **Color de niebla explícito** (`ColorAttribute.createFog`, tinte azulado frío igual al de la luz direccional, en vez del negro implícito por defecto) — implementado, costo cero, hace que la niebla se sienta parte de la misma atmósfera de luz de luna.
   - **Post-proceso completo de color grading/contraste (FBO + shader de pantalla completa) — investigado y descartado a propósito.** Aunque el costo en rendimiento habría sido bajo (un render target extra + un pase de pantalla completa), el riesgo de ingeniería era desproporcionado frente a la ganancia: requeriría tocar los 6 puntos de renderizado distintos del `Screen` (JUGANDO, cinemática, jumpscare, estática, ambas intros de corazones) de forma consistente, manejar resize de FBO, y no perder nitidez en overlays de UI (viñeta, corazones, botón salir) que deben quedar SIN gradar encima. Dado que la iluminación por capas ya existente (ambiente tenue + luz direccional fría + linterna cálida + niebla tintada) ya logra un look "gradado" real vía iluminación tradicional, se decidió no implementarlo — decisión técnica explícita, no un olvido, en línea con el pedido del propio usuario de "no implementar efectos pesados solo porque existen".
   - **Ambient occlusion:** ya soportado de forma automática por el pipeline PBR de gdx-gltf vía textura de oclusión por material (`PBRTextureAttribute.OcclusionTexture`) si el glTF de origen la trae — no requiere cambio de código; implementar SSAO en tiempo real habría sido significativamente más pesado sin garantía de mejora visible sobre los assets actuales.
   - **Sombras:** ya reales (`DirectionalShadowLight`, sin cambios) — resolución/calidad ya adecuada, no se tocó.
   - **Iluminación volumétrica:** una aproximación real (ray marching de sombras) es demasiado costosa para el alcance de este juego; la combinación ya existente de niebla + linterna cónica + luz direccional ya da una sensación atmosférica razonable sin ese costo adicional. No implementado.

**Commiteado y pusheado a `five_doors_escape`:** reencuadre de las tomas de Foxy/Bonnie/Chica en la cinemática, separación de `base_39` en una lista de exclusión propia para no despejar paredes reales por error (`LevelLoader.java`), viñeta al 0.82, `IBLBuilder.createIndoor` y color de niebla explícito (`GameplayScreen.java`). Toda la instrumentación temporal (grillas de colisión, verificación de tomas candidatas, barrido de perímetro, capturas) se agregó y se retiró por completo al terminar, confirmado con una compilación final sin advertencias y una corrida real completa (cinemática → intro → juego) sin excepciones.

---

### 2.29 Corrección real de A/D invertidos, niebla más perceptible y ronda de playtesting extensivo, sesión 2026-08-05 (misma fecha, sesión posterior)

**Contexto:** el usuario, satisfecho con el resultado de §2.27/§2.28, pidió antes de seguir agregando mecánicas nuevas: (1) jugar varias partidas completas de principio a fin buscando bugs reales de IA/colisión/iluminación/cámara/audio/jugabilidad; (2) corregir un bug real de movimiento -- A movía a la derecha y D a la izquierda, invertido; (3) hacer la niebla (§2.28) un poco más perceptible, ya que casi no se notaba; (4) seguir revisando el apartado visual mientras se juega.

**1) Bug real de A/D invertido, corregido y verificado matemáticamente (no solo "a ojo").** En `FirstPersonCameraController.computeWasdDelta`, el vector "derecha" usado para el strafe (`rightX = dirZ; rightZ = -dirX`) tenía el signo invertido respecto al vector "derecha" real de la cámara. Corregido a `rightX = -dirZ; rightZ = dirX`. Verificado sin depender de sensación visual: se comparó explícitamente contra `camera.direction.cpy().crs(camera.up)` -- el mismo cross product que usa internamente `Matrix4.setToLookAt` de libGDX para construir la matriz de vista real (derivado leyendo el código fuente de libGDX) -- con una diferencia de `0.0` entre ambos vectores, confirmando matemáticamente (no solo jugando) que el nuevo signo coincide exactamente con lo que la cámara realmente renderiza como "derecha". El mouse-look (yaw) y W/S no se tocaron, ya funcionaban bien.

**2) Niebla más perceptible -- el color era el limitante real, no la distancia.** Primer intento (solo bajar `near` de 9 a 5 y `far` de 22 a 17) seguía sin notarse en una captura real. La causa real: con `AMBIENTE_JUGANDO=0.12` tan oscuro, un color de niebla casi negro (`0.02,0.025,0.04`) se fundía de forma invisible en la propia oscuridad de la escena sin importar la distancia -- confirmado probando un tono intermedio (`0.05,0.06,0.09`) que TAMPOCO se notaba en una captura real de la sightline más larga del mapa (pasillo Z=1 completo). El tono final, `0.14,0.17,0.24` (siempre dentro del mismo tinte azulado frío de la luz direccional), sí se percibe con claridad como neblina real sobre las paredes lejanas en captura real, sin tapar nada dentro del alcance normal de la linterna (near=5 garantiza cero efecto por debajo de esa distancia).

**3) Ronda extensiva de playtesting automatizado -- y un hallazgo importante sobre el propio entorno de pruebas.** Sin poder presionar teclas reales, se implementó un autopiloto temporal que empuja al jugador con el mismo `resolveMovement(..., esJugador=true)` real hacia varias zonas del mapa (Bonnie, Chica, entrada de Pirate Cove, puerta de salida), sin intervenir la IA en absoluto -- deja que Freddy persiga y atrape organicamente. Resultado: navegación por colisión funciona correctamente en todas las zonas probadas (el sistema de detección de atasco ya existente en `ChaseState` se auto-recupera como está diseñado, confirmado con `tiempoAtascado` subiendo y bajando a 0 repetidamente sin quedar permanentemente atascado); el ciclo atrapar→jumpscare→reaparecer se disparó orgánicamente al menos una vez (posiciones de los guardias repitiéndose de forma idéntica tras el evento, coherente con un reinicio real) sin ninguna excepción de Java en la transición. Aparte, en corridas MUY largas (varios minutos sin atención) el proceso del juego llegó a congelarse/el daemon de Gradle llegó a desaparecer -- investigado a fondo (medición precisa de tiempo real mostró que `show()` y la cinemática completa se ejecutan a velocidad real exacta cuando se miden directamente, sin ninguna regresión de rendimiento en el código) y atribuido con alta confianza a que Windows regula/limita ventanas sin foco que llevan mucho tiempo abiertas sin interacción (un problema conocido de LWJGL/GLFW en pruebas automatizadas desatendidas), no a un bug real del juego -- ningún jugador real tiene este problema porque su ventana está en foco. Documentado como limitación del método de prueba, no como bug corregido.

**Commiteado y pusheado a `five_doors_escape`:** corrección de A/D invertidos (`FirstPersonCameraController.java`), niebla más perceptible (`GameplayScreen.java`). Toda la instrumentación temporal (autopiloto de playtesting, verificación matemática del strafe, capturas de niebla/revisión visual) se agregó y se retiró por completo al terminar, confirmado con una compilación final sin advertencias y una corrida real completa (cinemática → intro → juego) sin excepciones.

---

### 2.30 Duración real del audio de la cinemática de Escape y linterna togglable, sesión 2026-08-05

**Contexto:** mismo pedido de sesión que §1.11 (llamadas telefónicas del lado Swing) -- del lado LibGDX, el usuario pidió que la narración de la cinemática inicial ("Escape ES"/"Escape EN", ahora de ~18s) reemplace el audio anterior y que la duración total de la cinemática coincida EXACTAMENTE con la del audio real, respetando el idioma seleccionado -- y, por separado, una linterna que el jugador pueda apagar con CTRL, con sonido de click y apagado real (sin iluminar nada).

**1. `WavDuration` (nuevo, `util/WavDuration.java`) -- lectura de duración real sin depender de una API que no existe.** `com.badlogic.gdx.audio.Music` no expone `getDuration()` (confirmado leyendo `gdx-1.14.2-sources.jar`) -- se escribió un parser manual de chunks RIFF/fmt/data sobre los bytes crudos del WAV (`leerSegundos(FileHandle, float respaldo)`), con un valor de respaldo si el archivo no es un WAV válido o está corrupto. Verificado contra una medición independiente por PowerShell (lectura directa de cabecera WAV): **18.519209s exactos**, coincidencia perfecta.

**2. Cinemática con duración dinámica, ya no hardcodeada.** `DURACION_CINEMATICA_INICIAL`/`DURACION_TOMA` (antes `static final float`) pasaron a campos de instancia (`duracionCinematicaInicial`/`duracionToma`), calculados en `show()` a partir de `WavDuration.leerSegundos(...)` sobre el archivo real según `handoff.idioma` (`rutaAudioEscape(Idioma)`, nuevo método estático, mismo patrón de resolución por idioma que ya usa el resto del proyecto). Si el audio cambia de duración en el futuro (edición, nueva versión), la cinemática se ajusta sola sin tocar código.

**3. Linterna togglable con CTRL.** `Input.Keys.CONTROL_LEFT`/`CONTROL_RIGHT` alternan `linternaEncendida` (booleano nuevo) y reproducen `sonidoLinternaClick` (un `.wav` corto reutilizado del pool de sonidos de botón de FDAF, copiado a `assets/sounds/linterna_click.wav`) en cada cambio de estado. `SpotLightEx.intensity` se fija a `LINTERNA_INTENSIDAD_ENCENDIDA` (18f, el valor que ya traía antes) o a `0f` cada frame según ese booleano -- apagar la luz realmente no ilumina nada (no es solo un efecto cosmético; se probó comparando 2 capturas idénticas en posición/ángulo, una con la luz encendida mostrando un cono real sobre la pared, otra apagada mostrando la escena completamente oscura sin ningún resto de iluminación cónica).

**4. Verificación real, no solo compilación.** Ambos cambios se probaron con el patrón ya establecido en este proyecto para pruebas LibGDX sin interacción real (`gradlew lwjgl3:run` corto en segundo plano + logs `Gdx.app.log` + capturas vía `ScreenUtils.getFrameBufferPixmap`/`PixmapIO.writePNG` con `flipY=true`, gotcha ya documentado en §2.14): la duración calculada en vivo coincidió con la medición independiente, y las capturas encendida/apagada confirmaron visualmente el efecto real de la linterna. Toda la instrumentación de diagnóstico (`DiagEscapeAudio`, `diagPruebaLinterna`, capturas de verificación) se agregó y se retiró por completo al terminar -- confirmado sin restos vía `grep` y una compilación final limpia.

**Commiteado y pusheado a `five_doors_escape`:** duración real de la cinemática de Escape (`WavDuration.java` nuevo, `GameplayScreen.java`, `BootScreen.java`), linterna togglable con CTRL + sonido de click (`GameplayScreen.java`), 3 archivos de audio nuevos (`assets/sounds/escape_en.wav`, `escape_es.wav`, `linterna_click.wav`).

---

### 2.31 Sistema de internacionalización real (I18NBundle), panel de ayuda inicial y menú de pausa, sesión 2026-08-05 (misma fecha, sesión posterior)

**Contexto:** el usuario pidió que Escape respetara el idioma seleccionado exactamente igual que FDAF -- **pero FDAF en sí NO tiene un sistema de localización basado en archivos**, su "sistema de idioma" real son ternarios (`idioma == INGLES ? "..." : "..."`) repetidos por todo el código, decidido por el enum `Idioma`. El pedido explícito de esta sesión para Escape fue más estricto que eso: **"no quiero ningún texto escrito directamente en el código... todo debe salir desde los archivos de localización"** -- así que en vez de replicar el patrón de ternarios de FDAF, se construyó un sistema real de localización por archivos (algo que FDAF nunca tuvo), usando el mecanismo nativo de libGDX para esto (`I18NBundle`), conservando el mismo concepto de "idioma" (`HandoffData.Idioma`, ya existente, análogo al `Idioma` de FDAF).

**1. `Lang` (nuevo, `util/Lang.java`) -- único punto de resolución idioma→texto de todo el proyecto.** Envuelve `com.badlogic.gdx.utils.I18NBundle`, cacheado por idioma (evita releer los `.properties` en cada frame). `assets/i18n/strings.properties` (español, default -- mismo idioma por defecto que `Idioma.ESPANOL` del lado Swing) y `assets/i18n/strings_en.properties` (inglés). Llamada desde cualquier screen: `Lang.get(handoff.idioma, "clave")`. Gotcha real encontrado y corregido: `new Locale("es")` está deprecado en Java 21 -- se usa `Locale.of("es")` en su lugar.

**2. Auditoría completa de texto hardcodeado -- 5 sitios reales encontrados y corregidos, ninguno quedó suelto.** Grep sistemático de literales de texto en los 4 screens de Escape (`BootScreen`, `GameplayScreen`, `NightGameOverScreen`, `EscapeVictoryScreen`) encontró: el mensaje de carga de `BootScreen` (`"Cargando... X%"`, hardcodeado en español SIEMPRE, sin importar el idioma -- bug real, no solo falta de limpieza), el título/mensaje de `NightGameOverScreen` y `EscapeVictoryScreen` (ya usaban ternarios, migrados a `Lang.get`), el texto `"RUN"` de la intro de `GameplayScreen` (literal fijo, nunca traducido -- ahora `Lang.get(..., "intro.run")`, español = "CORRE"), y el texto del viejo botón de salida (`"Exit"`/`"Salir"`, ternario -- eliminado junto con el botón, ver punto 4). Confirmado con un segundo grep de todo `core/src/main/java` (no solo `screens/`) que no queda ningún literal de texto de usuario fuera de estos 5 sitios -- los demás resultados del grep son nombres de nodos glTF, claves de metadata GIF, o mensajes de `Gdx.app.log` (diagnóstico de consola para desarrolladores, no texto de jugador, mismo criterio que FDAF nunca traduce sus propios `System.out.println`).

**3. Panel de ayuda de controles -- reemplaza al viejo botón de salida superior izquierdo.** El botón de salida original (§2.13) tenía un problema real de usabilidad que el usuario detectó jugando: "durante la partida no puede interactuarse con él" -- el cursor está capturado para mouse-look, así que un clic normal nunca llega a esa zona sin soltar antes el mouse (ESC), lo cual no era obvio para el jugador. Reemplazado por `dibujarAyudaInicial()`: panel semitransparente esquina superior izquierda, visible los primeros 9s reales de `JUGANDO` con desvanecimiento suave en el último 1.5s, **no invasivo** -- nunca captura el mouse ni bloquea el input, el jugador se mueve libremente desde el primer frame. Contenido (`help.title`/`help.move`/`help.look`/`help.flashlight`) enteramente desde `Lang`.

**4. Menú de pausa real con ESC.** Antes, ESC solo alternaba `setCursorCatched` (para poder hacer clic en el botón de salida ya eliminado). Ahora ESC alterna un booleano `pausado`: al entrar en pausa, se libera el cursor y se congela la partida reutilizando el mismo patrón ya probado por la cinemática inicial (`sceneManager.update(0f)` + `sceneManager.render()` -- mantiene cámaras/luces sincronizadas sin avanzar ningún clip ni mover a la IA/jugador) en vez de simplemente no renderizar nada. `dibujarMenuPausa()` dibuja un overlay de pantalla completa + panel centrado con título (`pause.title`) y un único botón real (`pause.exitToMenu`) -- sin opciones de configuración/reanudar (pedido explícito del usuario), ESC de nuevo ya cierra el menú. El clic usa el mismo mecanismo `Gdx.app.exit()` ya verificado que Swing detecta correctamente vía `LanzadorEscape`/`Process.waitFor()`. **Gotcha real de coordenadas, corregido antes de commitear:** el botón viejo (pegado al margen superior) funcionaba por coincidencia numérica entre `Gdx.input.getY()` (Y-down desde arriba) y las coordenadas Y-up de `ShapeRenderer/SpriteBatch` porque estaba cerca de `y=0` en ambas convenciones -- un botón CENTRADO verticalmente (como el de pausa) no tiene esa coincidencia y requiere la conversión real `touchYDesdeAbajo = altoPantalla - touchYDesdeArriba`. Verificado con un test aritmético aislado (sin LibGDX) antes de confiar en el cálculo.

**5. Verificación real, con una técnica nueva para evitar los ~21s de espera de cinemática+intro en cada prueba.** Se agregó instrumentación temporal en `show()` (saltar directo a `JUGANDO` tras la cinemática) y en `render()` (forzar la pausa a los 2s reales) -- permitió capturar el panel de ayuda y el menú de pausa en segundos en vez de minutos por corrida. Además, para verificar `NightGameOverScreen`/`EscapeVictoryScreen` sin tener que jugar hasta esas condiciones reales, se booteó la app temporalmente directo a esas pantallas (`FiveDoorsEscapeGame.create()` modificado a mano para cada prueba) -- **las 4 pantallas se verificaron con capturas reales en AMBOS idiomas** (8 capturas totales: ayuda ES/EN, pausa ES/EN, victoria ES, game over EN), confirmando texto correcto, layout correcto, y que el patrón de congelado de pausa no rompe el render de la escena de fondo. Toda esta instrumentación (incluida la modificación temporal de `FiveDoorsEscapeGame.java`) se agregó y se retiró por completo al terminar -- confirmado con `git diff` limpio en ese archivo y sin ningún marcador `DIAGNOSTICO TEMPORAL` restante en el repo. Una corrida final natural (sin ningún atajo, cinemática completa real) confirmó cero excepciones.

**Commiteado y pusheado a `five_doors_escape`:** sistema de i18n completo (`Lang.java`, `assets/i18n/strings.properties`, `assets/i18n/strings_en.properties`), panel de ayuda inicial + menú de pausa reemplazando al botón de salida (`GameplayScreen.java`), migración de `BootScreen`/`NightGameOverScreen`/`EscapeVictoryScreen` a `Lang.get(...)`.

---

### 1.13 Transición cinematográfica Noche 5 → Escape, hallazgo importante sobre `gameover.gif`, y optimización real de memoria, sesión 2026-08-06

**Contexto:** ronda con seis pedidos: (1) nueva transición Noche 5→Escape con música `LesToreadorsRemix` continua + pantalla de carga negra con mensajes tipo error aleatorios; (2) audio espacial de Freddy en Escape; (3) unificar las pantallas finales de Escape con el juego principal; (4) investigar la causa raíz de por qué `gameover.gif` no muestra sus zonas de interferencia/efectos de cámara; (5) optimización de memoria (prioritario, el usuario reportó consumo excesivo en partidas largas); (6) validación real continua.

**1. Transición Noche 5 → Escape.** `mostrarGlitchYLanzarEscape` ahora arranca `win/LesToreadorsRemix.wav` (104.5s reales) en el mismo instante en que empieza el glitch (antes 2.6s, ahora `DURACION_GLITCH_MS=4000` por pedido explícito). Tras el glitch, `mostrarCargaYLanzarEscape` ya NO usa el fondo de `Win` -- usa `CargarImagenes.fondoNegro` (ya existía, sin asset nuevo) con un ciclo de mensajes tipo error (`programarSiguienteMensajeCarga`, mismo patrón de auto-reprogramación con espera aleatoria que ya usa `SonidosAmbientalesAleatorios`): 75% del tiempo muestra "EMPIEZA EL JUEGO"/"SOY YO" (peso 4:1 a favor de "SOY YO"), 25% parpadea a vacío, con esperas de 180-830ms sin patrón fijo -- deliberadamente no es un contador de progreso. La música nunca se detiene explícitamente durante toda la secuencia (solo se detiene de forma defensiva en el callback de `LanzadorEscape.lanzar()`, por si el jugador vuelve al menú antes de que termine sola).

**2. Audio espacial de Freddy ("caja musical").** libGDX no trae un motor de audio 3D completo, pero `Sound.setPan(id, pan, volumen)` sobre una instancia ya en loop (`Sound.loop(0f)`, arrancada una sola vez en `show()`, nunca reiniciada) es la forma idiomática de audio posicional simple -- exactamente el mismo mecanismo `loop()+setVolume()` que ya usa el sistema de latidos, extendido con pan real. `actualizarMusicaEspacialFreddy()` (llamado cada frame, mismo sitio que `actualizarLatidosYVineta`) calcula distancia horizontal real a Freddy (`freddyEntity`, identificado por `siempreEnPersecucion=true`, no por posición en la lista `guardias`) para el volumen (`MUSICA_FREDDY_DISTANCIA_MAXIMA=14`, `_MINIMA=1.5`) y proyecta la dirección hacia Freddy sobre el vector "derecha" real de la cámara (`direction.crs(up)`, la misma convención ya verificada al corregir el strafe A/D) para el pan.

**3. Pantallas finales de Escape unificadas con Swing.** `NightGameOverScreen` ahora tiene 2 botones reales (`gameOver.retry`/`gameOver.exit`, i18n) en vez de un único "Presiona ESC" -- Reintentar hace `Gdx.app.exit()` normal (Swing ya lo detecta y vuelve al menú). Salir necesita cerrar la aplicación Swing COMPLETA, no solo Escape -- pero el proceso de Escape se lanza envuelto en `cmd /c gradlew.bat lwjgl3:run`, así que su código de salida real NO se propaga de forma confiable a través de Gradle. Solución: `SalidaCompleta.java` (nuevo, lado Escape) escribe un archivo señal (`~/.fivedoorsatfreddys/salir_completo.flag`, mismo directorio que ya usa `handoff.json`) antes de `Gdx.app.exit()`; `LanzadorEscape.lanzar()` (lado Swing) revisa ese archivo justo después de que el proceso termina y hace `System.exit(0)` en vez de volver al menú si está presente. `EscapeVictoryScreen` ahora reproduce `circus.wav` (reutilizado de Swing, cargado directo vía `Gdx.audio.newSound` sin pasar por `AssetService` -- pantalla que se alcanza como mucho una vez por sesión, no vale la pena precargarlo desde el arranque) en loop mientras está visible; sigue con una sola salida (ESC), sin botones extra, por pedido explícito del usuario.

**4. `gameover.gif` no avanza en partidas reales -- causa raíz real encontrada tras una investigación larga, con una lección metodológica importante.** El síntoma reportado ("zonas de interferencia y efectos de cámara no funcionan") resultó ser: la animación se queda congelada en el frame 0 durante TODA la secuencia real de Game Over, así que el contenido de interferencia/jumpscare-peek (frames 1-36) nunca se ve. Investigado con capturas reales pintando el `pnlJuego` real fuera de pantalla (`Component.paint(g)` sobre un `BufferedImage`, técnica nueva que evita el problema de que capturas de pantalla reales compiten por el foco con la ventana del propio usuario -- ver hallazgo aparte más abajo). Se encontraron y corrigieron dos causas reales, ambas documentadas en el código:
   - **`GifEscalado.paintIcon` nunca registra ningún observador persistente** (pasa `null` a `drawImage`, y `ImageIcon` solo mantiene un observador vía `MediaTracker` hasta que el PRIMER frame está listo, no durante el resto de la animación) -- sin consumidor activo, el hilo de decodificación de AWT para gifs multi-frame puede pausarse tras un período sin observadores. `gameover.gif` tiene un primer frame de **7 segundos** de duración (los gifs de puertas nunca mostraron esto porque sus delays son uniformemente cortos, ~50-150ms). Corregido en `CargarGifs.java`: un observador dedicado y propio de esa clase (nunca el `Component "c"` -- eso sigue prohibido, ver REGLA ABSOLUTA de `EscalarVista`) registrado vía `Toolkit.prepareImage(imagen, -1, -1, observador)` justo tras crear cada imagen fresca, manteniendo el consumo activo durante toda su vida.
   - **`drawImage(img, x, y, ANCHO, ALTO, observer)` (la sobrecarga de tamaño explícito) cachea el bitmap ya escalado en el primer frame disponible cuando el destino es MÁS GRANDE que el tamaño nativo** (escalar hacia arriba) -- `gameover.gif` es nativamente 2200×1020, pero `lblImgOficina` mide 2641×1224 por el margen de paneo de cámara, un caso real de escalado hacia arriba. Corregido en `EscalarVista.GifEscalado.paintIcon`: dibuja siempre a tamaño nativo y delega el escalado a `Graphics2D.scale(...)` en vez de pedirle a AWT un bitmap pre-escalado -- sin tocar `gifOriginal.getImageObserver()` en ningún momento (la REGLA ABSOLUTA queda intacta).
   - **Honestidad sobre el resultado final:** con ambas correcciones aplicadas, la animación avanza correctamente la MAYORÍA de las veces en pruebas reales repetidas, pero no el 100% -- quedó evidencia de que el pipeline clásico de `Toolkit`/`ImageIcon` para gifs animados tiene un residuo de comportamiento no determinista a nivel de JDK (una condición de carrera entre el hilo de decodificación y el hilo de pintado) que no se pudo eliminar por completo dentro de las restricciones del proyecto (no tocar la línea prohibida de `getImageObserver()`, no reintroducir un reproductor de frames propio -- ya descartado antes por calidad visual inferior, no agregar librerías). Ambas correcciones son mejoras reales y verificadas (no un parche cosmético, atacan dos causas raíz confirmadas con evidencia), pero no se puede garantizar el 100% de las veces. Si el usuario nota que sigue fallando ocasionalmente, este es el motivo real, ya investigado a fondo -- no hace falta re-investigar desde cero.
   - **Lección metodológica importante:** las capturas de pantalla reales (`Robot.createScreenCapture`, incluso forzando la ventana al frente con `toFront()`/`setAlwaysOnTop`) pueden verse contaminadas si el usuario tiene OTRAS ventanas activas compitiendo por el foco durante la prueba (en esta sesión, literalmente capturó la propia IDE Eclipse del usuario en vez del juego). Pintar el componente real directamente sobre un `BufferedImage` fuera de pantalla (`componente.paint(g)` vía `SwingUtilities.invokeAndWait`) es inmune a esto y debería preferirse para pruebas visuales automatizadas de este proyecto en el futuro.

**5. Optimización real de memoria -- causa concreta encontrada y corregida (no solo hipótesis).** `CargarImagenes` cargaba 4 gifs completos de forma EAGER en su bloque estático (`gameOver`, `JUMP_SCARE`, `introduccion`, `estatica`) cuyo único uso real en todo el proyecto es como parámetro "respaldo" de `CargarGifs.cargarFresco(ruta, respaldo)` -- que solo se usa si la carga real desde bytes frescos falla (prácticamente nunca, para recursos empaquetados). Como argumento de método, Java evalúa `new ImageIcon(url)` ANTES de la llamada sin importar si el respaldo se usa, así que los 4 gifs se decodificaban por completo (cada uno con su propio hilo "Image Animator" de AWT corriendo para siempre) en cada arranque de la app, sin mostrarse jamás en el camino normal. Diferidos a getters lazy (`getGameOverRespaldo()`, etc.) que solo decodifican la primera vez que realmente se necesitan -- confirmado con una medición real: sin tocarlos, quedan 3 hilos "Image Animator" activos en vez de hasta 7-10. **Se investigó también un posible leak de `Timer`s de Swing no detenidos** (patrón clásico: un `Timer` corriendo queda registrado en el `TimerQueue` compartido, reteniendo todo su grafo de objetos) -- los 13 `Timer` de `ControllerInterfaz` ya tenían cobertura completa (auto-terminación por `setRepeats(false)` o `detenerSonidosJuego()`), sin gaps encontrados. La reutilización de gifs de puertas (la acción más repetida en una sesión larga) se confirmó estable en una prueba real de 800 cargas consecutivas (6-9MB, sin tendencia de crecimiento) -- el mecanismo de `flush()` por ruta que ya existía funciona correctamente.

**Commiteado y pusheado a `FiveDoorsAtFreddys`:** transición Noche 5→Escape con música continua + pantalla de carga negra glitcheada (`ControllerInterfaz.java`), causa raíz de `gameover.gif` corregida (`CargarGifs.java`, `EscalarVista.java`), 4 gifs de respaldo diferidos a carga lazy (`CargarImagenes.java` + 5 sitios de llamada actualizados), 1 archivo de audio nuevo (`sounds/win/LesToreadorsRemix.wav`).

### 1.14 Segunda causa raíz real de memoria (`Sonido.java`), paneo de cámara bloqueado en Game Over, señal cruzada de proceso para el volumen de `LesToreadorsRemix`, y rediseño de la pantalla de carga corrompida, sesión 2026-08-06 (misma fecha, ronda paralela a §1.13)

**Contexto:** el usuario reportó un `OutOfMemoryError` REAL con stack trace apuntando a `CargarGifs.cargarFresco()`/`ByteArrayOutputStream`, exigiendo profiling real (no aumentar heap, no asumir "Java consume mucha memoria"); pidió reinvestigar desde cero (sin la hipótesis de §1.13/rondas previas) por qué "las zonas de interferencia del GameOver" seguían sin funcionar; reportó que `LesToreadorsRemix` se re-exportó a ~5 minutos y pidió que sonara toda la partida de Escape, bajando su volumen ~25% justo después de la risa de Freddy; y pidió reemplazar el ciclo de un único mensaje de carga por una pantalla "completamente corrompida" con `SOY YO` disperso.

**1. Segunda causa raíz real de memoria -- `Sonido.java` nunca cerraba el `Clip` nativo.** Confirmado con `jmap -histo:live` (fuerza GC, solo objetos VIVOS) sobre una simulación real de 40 noches con `-Xmx400m`: 835 instancias vivas de `com.sun.media.sound.DirectAudioDevice$DirectClip`, 852 hilos vivos -- reprodujo el stack trace EXACTO del usuario (`ByteArrayOutputStream.grow` en `cargarFresco`), pero la causa real no tenía nada que ver con GIFs: un `Clip` abierto queda registrado en el mixer nativo indefinidamente hasta que algo llama `clip.close()` explícitamente, y la inmensa mayoría de los `Sonido` del proyecto son de un solo uso (`new Sonido(ruta).play()`, nadie vuelve a tocar esa instancia). Ya documentado como deuda técnica conocida en §1.8 antes de esta sesión, sin crash confirmado hasta ahora -- esta sesión sí lo confirmó y lo corrigió. Corregido con un `LineListener` que cierra el `Clip` automáticamente en `LineEvent.Type.STOP`, guardado por un flag `reiniciando` (volatile) para no cerrar los pocos sonidos reutilizables (luces, llamada, ambiente de oficina) cuando `play()` los reinicia internamente vía `stop()+setFramePosition(0)`. Verificado: la misma simulación que antes hacía `OutOfMemoryError` hacia la noche 24 ahora completa 40 noches con memoria estable (~163-184MB, sin tendencia de crecimiento). Complementa, no reemplaza, la optimización de `CargarImagenes`/`CargarGifs` de §1.13 -- son dos fugas reales e independientes que coincidían en el mismo síntoma reportado por el usuario.

**2. "Zonas de interferencia" reinterpretadas -- NO es el gif en sí, es el paneo de cámara.** Investigado desde cero, abandonando la hipótesis de animación-congelada de rondas previas (ya se había probado que el fix de audio no lo resolvía, y §1.13 después confirmó por otra vía que el gif sí tenía un problema real de observador/escalado -- ambos hallazgos son ciertos y complementarios, no contradictorios). Releyendo `ControllerCamara.izquierda(int v)`/`derecha(int v)` con ojos frescos: el guard `interfaz.isTransicionTabletActiva() || juegoTerminado()` bloqueaba el paneo de cámara (disparado por las zonas `zonaUno/Dos/TresIzq/Der`, el hover real, no contenido del gif) durante TODA la secuencia de Game Over (~20s con `pnlJuego` todavía visible), contradiciendo un comentario ya existente en el código que documentaba la intención real ("el movimiento de cámara... sigue funcionando exactamente igual") pero que el código nunca cumplía. `juegoTerminado()` sigue siendo necesario en `alGanar()`/`rendirse()` (ahí `pnlJuego` desaparece de inmediato, así que nunca importó), pero no en `alPerder()`. Quitado solo de esos dos guards -- el resto de los bloqueos de Game Over (puertas/luces/tablet) tiene su propio guard independiente en `ControllerJuego`, sin relación con este. Verificado con un test real (`alPerder()` real + `camara.derecha(20)` real): posición X cambia de -300 a -580 en 300ms, antes quedaba congelada para siempre.

**3. Señal cruzada de proceso para el volumen de `LesToreadorsRemix` -- mismo patrón ya establecido (`SalidaCompleta`/`handoff.json`).** Nuevo `com.fivedoorsescape.io.SenalRisaFreddy` (lado Escape) escribe `~/.fivedoorsatfreddys/risa_freddy.flag` en el instante exacto en que `GameplayScreen` reproduce la risa de Freddy durante su intro. El lado Swing (`ControllerInterfaz.iniciarEsperaRisaFreddy()`, un `Timer` de 400ms que vigila el archivo, borrándolo antes de empezar por si quedó de una sesión vieja) aplica `sonidoTransicionEscape.subirVolumen(-2.5f)` (~25% de reducción lineal, `20*log10(0.75)`) exactamente una vez al detectarlo, sin tocar ningún otro sonido. Verificado de extremo a extremo con un test real (crea el `Sonido`, arranca el polling, escribe la señal como lo haría Escape, confirma el delta exacto de -2.5dB, confirma que un segundo `Sonido` de control no cambió, confirma que la señal se borra, confirma que NO se aplica una segunda vez si se reescribe el archivo tras la primera detección). Coexiste sin conflicto con el mecanismo de §1.13 (música arrancando al inicio del glitch, nunca detenida hasta el final natural o el regreso al menú) -- este es el ajuste de volumen adicional que faltaba dentro de ese mismo diseño.

**4. Rediseño completo de la pantalla de carga -- reemplaza el ciclo de un único mensaje de §1.13.** El usuario, tras ver el resultado de §1.13 (un solo `lblCargando` alternando entre "SOY YO"/"EMPIEZA EL JUEGO" cada 180-830ms), pidió algo más agresivo: "SOY YO"/"IT'S ME" disperso por TODA la pantalla, muchísimas veces, en posiciones/tamaños aleatorios, entrando y desapareciendo constantemente, sin patrón repetitivo, sintiéndose como una pantalla corrompida -- con "EMPIEZA EL JUEGO"/"THE GAME BEGINS" fijo y legible en el centro. Implementado con el mismo patrón ya usado por `pnlGlitch` (JPanel transparente + `Timer` + `paintComponent` propio, sin librería nueva): `pnlGlitchTexto`, superpuesto sobre `pantalla`, redibuja en cada tick (`INTERVALO_GLITCH_TEXTO_MS=90`) un lote de 12-25 instancias de "SOY YO" en posición/tamaño(16-85px)/color(blanco predominante, rojo/cian ocasional)/alpha aleatorios -- todo el lote cambia por completo en cada repintado, así que nunca hay dos composiciones iguales. `lblCargando` (reutilizado, ya no cicla texto) se recentra verticalmente y muestra el mensaje fijo una sola vez; `setComponentZOrder` lo mantiene siempre al frente para que el mensaje central quede legible sobre el caos de fondo. Verificado con pruebas reales: i18n correcto en ambos idiomas, dos lotes consecutivos difieren en miles de píxeles (sin patrón repetitivo), limpieza real del `Timer`/panel al terminar la carga -- y una captura renderizada confirmando visualmente el efecto de "pantalla corrompida" pedido.

**Commiteado a `FiveDoorsAtFreddys`** (commit `d100b40`, después de `7a65db0`/§1.13): cierre real de `Clip` en `Sonido.java`, paneo de cámara liberado durante Game Over en `ControllerCamara.java`, señal de risa de Freddy + rediseño de pantalla de carga en `ControllerInterfaz.java`, `LesToreadorsRemix.wav` actualizado (~5 min). **Commiteado y pusheado a `five_doors_escape`** (commit `e41ffa7`): `SenalRisaFreddy.java` (nuevo) + la llamada a `marcar()` en `GameplayScreen.java`, en el mismo punto donde ya se reproduce la risa de Freddy.

### 1.15 Tercera causa raíz real de memoria (`Sonido.java`) -- `Clip.open()` no soporta archivos de audio grandes, sesión 2026-08-06 (misma fecha, ronda posterior a §1.14)

**Contexto:** el usuario reportó un TERCER `OutOfMemoryError` real, con stack trace apuntando esta vez a `DirectAudioDevice$DirectClip.open()` dentro de `Sonido.<init>(Sonido.java:45)`, disparado por `ControllerInterfaz.mostrarGlitchYLanzarEscape` al crear `sonidoTransicionEscape` (justo al arrancar el glitch de la transición Noche 5 → Escape). Heap máximo real en el crash: 247MB, con 168MB ya en uso. Pidió explícitamente NO asumir que era el mismo problema ya corregido en §1.14 y verificar la causa real.

**Causa raíz real, distinta de las dos anteriores.** `javax.sound.sampled.Clip` (a diferencia de `SourceDataLine`) SIEMPRE decodifica el audio COMPLETO a un buffer en memoria antes de poder reproducirlo -- es su contrato de diseño, no un bug. `LesToreadorsRemix.wav` mide ~50.8MB de PCM crudo (5 minutos estéreo 44.1kHz/16-bit, confirmado con `AudioInputStream.getFrameLength()*getFrameSize()`), casi 5.4 veces más grande que el siguiente archivo más pesado del proyecto (`phoneguy_ingles.wav`, 9.9MB). Esa asignación de ~50MB de una sola vez es exactamente el tipo de operación que puede fallar bajo presión de heap real (el crash ocurre justo al iniciar la transición de Noche 5, un momento con muchos otros assets recién cargados). Confirmado experimentalmente: `Clip.open()` sobre este archivo específico fallaba de forma real y reproducible (`OutOfMemoryError`/`IOException: Audio data is too big`, mismo código interno `DirectAudioDevice$DirectClip.open`) en un heap de 245MB con ~180-205MB de presión de memoria ya reservada (fragmentada, no bloques uniformes) -- condición perfectamente realista para el momento exacto del crash real del usuario.

**Corrección real: `Sonido` detecta archivos grandes y usa `SourceDataLine` (streaming) en vez de `Clip`.** Nueva constante `UMBRAL_STREAMING_BYTES=15_000_000` (15MB, con margen amplio sobre el archivo no-streaming más grande del proyecto). En el constructor, tras sondear `AudioFormat`/`frameLength` del recurso, si el PCM estimado supera el umbral: abre una `SourceDataLine` (cuyo `open()` solo reserva un buffer interno de unos pocos KB, nunca el archivo completo) en vez de un `Clip`. `play()` arranca un hilo daemon propio que lee el archivo en bloques de 8KB desde un `AudioInputStream` fresco y los escribe a la línea -- nunca decodifica el audio completo a memoria de una sola vez. Todos los demás métodos públicos (`stop`, `subirVolumen`, `setVolumen`, `fadeOut`, `aplicarNivelGlobal`) se unificaron detrás de un solo `obtenerControlVolumen()` que elige entre el `FloatControl` del `Clip` o el de la `SourceDataLine` según corresponda (confirmado que `SourceDataLine` sí soporta `MASTER_GAIN` en este sistema) -- sin duplicar la lógica de volumen. Los +30 sitios de uso existentes de `Sonido` (todos muy por debajo del umbral) siguen usando `Clip` exactamente igual que antes, sin ningún cambio de comportamiento.

**Verificación real, no solo teórica:**
- Reproducido el fallo real de `Clip.open()` sobre este archivo específico bajo presión de memoria controlada (245MB máximo, ~180-205MB de presión fragmentada) -- falla de forma consistente.
- La misma condición exacta, con el `Sonido` real del proyecto (ahora usando streaming para este archivo): se crea sin error, reproduce realmente (línea activa confirmada), aplica `subirVolumen(-2.5f)` con el delta exacto de -2.5dB sobre el `MASTER_GAIN` de la `SourceDataLine`, y cierra limpiamente con `stop()`.
- Regresión de los archivos cortos: confirmado que `phoneguy_ingles.wav` (el más grande de los que NO cruzan el umbral) y otros sonidos normales siguen usando `Clip` sin cambios.
- Re-verificado el mecanismo de señal cruzada de §1.14 (risa de Freddy → -2.5dB) contra la nueva implementación de streaming -- sigue funcionando idéntico (el cambio es transparente para ese mecanismo).
- Corrida real de 15 noches simuladas (puertas, luces, transiciones de tablet) con el fix aplicado: sin excepciones, memoria oscilando normalmente (200-290MB), sin tendencia de crecimiento -- confirma que el fix no introdujo ninguna regresión sobre el mecanismo de cierre de `Clip` de §1.14.

**Commiteado y pusheado a `FiveDoorsAtFreddys`:** camino de streaming para audio grande en `Sonido.java`.

### 1.16 Causa raíz REAL de `gameover.gif` invisible en partidas reales (distinta de §1.13/§1.14), sesión 2026-08-09

**Contexto:** el usuario reportó que, al perder normalmente llegando a 0 vidas (revelando un animatrónico), la secuencia de Game Over se ejecuta -- música y sonidos correctos -- pero `gameover.gif` **no aparece**; en cambio, forzando la derrota con `CTRL+P`, sí aparece correctamente. Pidió comparar ambos caminos con evidencia real, no suposiciones, y no un parche superficial. Este bug es **independiente** de los dos ya investigados y corregidos en §1.13 (observador/escalado del gif) y §1.14 (paneo de cámara bloqueado) -- ambas correcciones siguen siendo válidas y necesarias, pero ninguna de las dos rondas anteriores había comparado explícitamente el camino de pérdida real contra `CTRL+P`, que es justamente donde vivía esta causa raíz.

**Comparación real de ambos caminos (leyendo el código, no suponiendo):**
- **`CTRL+P`** (`ControllerMenu.java`, atajo `debugForzarDerrota`) llama **directamente** a `ControllerInterfaz.alPerder()` -- nunca pasa por `ControllerJuego`/`alRevelarAnimatronico`/`alLiberar`.
- **Pérdida real (0 vidas)** pasa por `ControllerJuego.revelarAnimatronico()` → `perderVidaPorAnimatronico()` → `listener.alRevelarAnimatronico(...)` (jumpscare) → (tras el timer `MS_ANIMATRONICO`) `liberar()` → `listener.alLiberar(lado)` → `verificarFinDeJuego()` → `listener.alPerder()`, todo en la misma cadena de llamadas síncrona (mismo instante del EDT).

**Causa raíz encontrada:** `alLiberar(lado)` termina llamando a `iniciarFadeDesdeNegro()` cuando la puerta liberada fue un animatrónico (`ultimoFueAnimatronico=true`) -- crea/reutiliza `pnlFadeNegro`, un `JPanel` completamente opaco puesto al frente (`setComponentZOrder(pnlFadeNegro, 0)`) sobre TODO `pnlJuego`, arranca `alphaFade=255` y un `Timer` (`timerFadeNegro`) que lo desvanece en 4000ms. Inmediatamente después, `verificarFinDeJuego()` invoca `alPerder()`, cuya primera línea es `detenerSonidosJuego()` -- que **detiene `timerFadeNegro`** (correcto, evita que un timer huérfano siga corriendo tras el fin de partida) **pero nunca oculta `pnlFadeNegro` ni resetea `alphaFade`**. Resultado: el panel negro queda **congelado, completamente opaco, al frente**, durante el resto de los ~20s de la secuencia de Game Over (hasta que `avanzarDespuesDelGifLargo()` reemplaza todo el contenido con `PnlGameOver`) -- `gameover.gif` se sigue asignando y pintando correctamente en `lblImgOficina`, exactamente por debajo del panel negro, pero nunca llega a verse. El audio (`powerdown.wav`, etc.) no pasa por ningún Z-order, así que suena con normalidad -- coincide exactamente con el síntoma reportado. `CTRL+P` nunca crea `pnlFadeNegro` (no pasa por `alLiberar`), así que no hay nada que lo oculte.

**Corrección aplicada, en el único punto compartido real (`detenerSonidosJuego()`, usado por `alGanar()`/`alPerder()`/`ControllerCamara.rendirse()`):** justo después de detener `timerFadeNegro`, se oculta `pnlFadeNegro` (`setVisible(false)`) y se resetea `alphaFade=0` si el panel existe. No toca `EscalarVista`/`CargarGifs` (las correcciones de §1.13 siguen intactas) ni el guard de paneo de cámara de §1.14 -- es una causa raíz genuinamente distinta.

**Verificación real, con un arnés headless por reflection (mismo patrón ya establecido en §1.12, sin `Robot`):** se instanció `ControllerInterfaz` directamente y se reprodujo cada camino invocando los métodos privados reales por reflection:
- **Camino B (pérdida real):** `iniciarFadeDesdeNegro()` seguido de `alPerder()` (reproduce exactamente la secuencia síncrona real `alLiberar→verificarFinDeJuego→alPerder`). **Antes del fix:** `pnlFadeNegro.isVisible()==true` y `alphaFade==255` después de `alPerder()` -- bug reproducido y confirmado. **Después del fix:** ambos en `false`/`0`.
- **Camino A (`CTRL+P`):** `alPerder()` invocado solo. `pnlFadeNegro` nunca se crea (permanece `null`), sin cambios antes/después del fix -- confirma que este camino nunca tuvo el problema.
- En ambos caminos se confirmó que `lblImgOficina` recibe un ícono `GifEscalado` fresco de `gameover.gif` tras `alPerder()`.
- El fix se probó primero revirtiéndolo temporalmente (recompilando) para confirmar que el arnés SÍ detecta la regresión (falla real con el código viejo), y luego reaplicándolo para confirmar que pasa -- no se dio el fix por bueno solo por compilar.
- Compilación completa del proyecto (`javac`, JDK 1.8.0_271 + JavaFX + FlatLaf en el classpath) sin errores nuevos (mismo único warning preexistente de `Arbol.java`).
- Todo el arnés de prueba (`TestGameOverFade.java`, temporal, fuera del repo) y las clases compiladas de verificación se eliminaron al terminar.

**Nota sobre el resto del working tree encontrado al reconstruir contexto esta sesión:** además de esta corrección, había un `Sonido.java` con la implementación real de streaming de audio de §1.15 sin commitear (aunque §1.15 ya la daba por "commiteada y pusheada" -- discrepancia real entre la documentación y el estado real del repo, corregida aquí) y un `ControllerInterfaz.java` con una limpieza trivial de import ya presente sin commitear. Ambos se incluyeron en el commit de esta sesión por ser trabajo real, ya verificado, sin relación con ningún WIP ajeno. **`​.classpath`/`Main.java` (tema visual FlatLaf) y `Juego.java` (gate de `calificaParaBateria()`) siguen sin tocarse**, tal como se documentó en §2.19 -- WIP ajeno a esta sesión, decisión pendiente del usuario.

**Commiteado y pusheado a `FiveDoorsAtFreddys`:** corrección real de `gameover.gif` invisible en pérdida normal (`ControllerInterfaz.java`), más el camino de streaming de audio de §1.15 que había quedado sin commitear (`Sonido.java`).

### 1.17 Investigación de distribución para el compañero de equipo -- se descarta un repositorio contenedor nuevo, sesión 2026-08-09 (misma sesión que §1.16)

**Pedido del usuario:** que un compañero pueda clonar y ejecutar los dos juegos (Swing + Escape) sin reconstruir carpetas ni copiar archivos a mano, investigando primero si hace falta un repositorio contenedor/submódulos/monorepo antes de crear nada.

**Estructura real encontrada (inspeccionada en disco, no supuesta):** `five_doors_at_freddys` (este repo, contiene `FiveDoorsAtFreddys/` como subcarpeta, es el repo de EQUIPO ya usado por los 4 integrantes) y `five_doors_escape` (repo separado, hasta ahora solo del usuario) son dos repos Git de GitHub totalmente independientes (`dreiklx/five_doors_at_freddys`, `dreiklx/five_doors_escape`), sin ninguna relación de submódulo ni build compartido.

**Hallazgo real -- el mecanismo de lanzamiento YA asume una convención de carpetas, pero con dos problemas de portabilidad reales.** `LanzadorEscape.java` resuelve la carpeta de Escape como `<user.dir>/../../five_doors_escape` (Architecture.md §7) -- es decir, ya asume que ambos repos son carpetas HERMANAS bajo el mismo directorio padre, con esos nombres exactos. Eso ya resuelve "sin copiar archivos a mano" siempre que el compañero clone ambos repos uno junto al otro -- **no hacía falta ningún contenedor/submódulo nuevo para eso, solo documentarlo**, cosa que no existía en ningún README. Pero se encontraron dos bugs reales de portabilidad en el propio lanzador, ambos corregidos esta sesión:
1. **`JAVA_HOME` para Java 21 estaba hardcodeado a `C:\Program Files\Java\jdk-21`** -- la ruta real de la máquina de desarrollo original, que rompería silenciosamente (mensaje de error genérico en consola, Escape simplemente no arranca) en cualquier máquina donde el JDK 21 esté en otro lado. Corregido con `LanzadorEscape.resolverJavaHome21()`: revisa primero si el `JAVA_HOME` ya heredado del sistema apunta a un JDK 21 real (verificado con `bin/java.exe` presente, no solo el nombre de la carpeta), y si no, busca dentro de las carpetas de instalación habituales de varios proveedores en Windows (`Program Files\Java`, `\Eclipse Adoptium`, `\Microsoft\jdk-21`, `\Zulu`, `\BellSoft`). Si no encuentra ninguno, ya NO fuerza una ruta inexistente -- deja que el proceso hijo use el `JAVA_HOME`/PATH real del sistema y avisa por consola, en vez de fallar de forma opaca. Verificado con un test real por reflection en esta máquina: resuelve correctamente `C:\Program Files\Java\jdk-21` (el mismo valor que antes estaba hardcodeado, pero ahora encontrado por búsqueda real, no supuesto).
2. **Sin verificación temprana de que `five_doors_escape` exista.** Antes, si el compañero todavía no había clonado el segundo repo, el fallo llegaba como una excepción genérica de `ProcessBuilder` al no encontrar `gradlew.bat`. Ahora `lanzar()` comprueba primero si `gradlew.bat` existe en la carpeta resuelta y, si no, imprime un mensaje claro señalando la causa real (falta clonar `five_doors_escape` como carpeta hermana) y la referencia a `SETUP.md`, sin intentar lanzar el proceso.

**Decisión: NO crear un repositorio contenedor ni convertir a submódulos.** Justificación:
- Un monorepo (fusionar el código/historial de ambos proyectos) violaría directamente la regla ya establecida y repetida del proyecto (§0): mantener los dos ecosistemas técnicos completamente separados (no migrar Swing a LibGDX, no convertir a Gradle, no cambiar versión de Java).
- `five_doors_at_freddys` es el repo de EQUIPO ya activo (4 integrantes, con su propio flujo de `git pull`/`push` ya funcionando) -- envolverlo en un contenedor nuevo habría reorganizado el punto de colaboración existente del equipo sin necesidad real.
- Un submódulo Git habría resuelto el "un solo `git clone`" pero a costa de una capa de complejidad real (`--recurse-submodules`, `git submodule update --init`, commits de referencia a un SHA fijo del otro repo) que no se justifica frente a la alternativa: los dos repos YA se comunican solo por un archivo de traspaso en tiempo de ejecución, nunca por referencia de código -- **documentar la convención de clonado (que el propio código ya asume) es suficiente**, sin agregar ninguna capa de Git nueva.
- **La causa real de fricción no era la falta de un repositorio contenedor -- eran los dos bugs de portabilidad de `LanzadorEscape` (arriba) más la ausencia de instrucciones escritas.** Corregir eso resuelve el pedido real del usuario sin restructurar nada.

**Entregable: `SETUP.md` nuevo en la raíz de `five_doors_at_freddys`** (no dentro de `FiveDoorsAtFreddys/`, para que aparezca antes de importar el proyecto Eclipse) -- explica la convención de carpetas hermanas ya asumida por el código, prerrequisitos reales (JDK 8 **con JavaFX incluido**, un hallazgo real: varias distribuciones modernas de OpenJDK 8 como Eclipse Temurin 8 NO traen JavaFX empaquetado, y `PnlWin.reproducirVideo` lo necesita; JDK 21 para Escape), y los pasos para ejecutar cada juego por separado o juntos. `README.md` del repo raíz actualizado con un enlace visible a `SETUP.md`.

**No tocado, fuera de alcance:** la carpeta `if3001 - pf - grupo#1/` (subcarpeta vacía sin commitear en la raíz del repo, solo `.project`/`.gitignore`/`bin/` vacío -- parece un proyecto Eclipse creado por error al importar en el nivel equivocado, sin relación con el código real) y el resto del WIP ajeno ya documentado en §2.19 (`.classpath`/`Main.java` tema FlatLaf, `Juego.java` gate de batería) -- ninguno de los dos bloquea la distribución real, quedan para que el usuario decida.

**Commiteado y pusheado a `FiveDoorsAtFreddys`:** portabilidad real de `LanzadorEscape.java` (JDK 21 autodetectado + verificación temprana de la carpeta de Escape), `SETUP.md` nuevo, `README.md` actualizado con enlace a `SETUP.md`.

### 1.18 Reinicio completo del proceso al terminar cada noche, respiración agitada tras jumpscares, y noche/rendirse visibles en la tableta, sesión 2026-08-09 (misma fecha, sesión posterior a §1.16/§1.17)

**Contexto:** cuatro pedidos explícitos del usuario, con instrucción de investigar primero y no dar nada por solucionado solo por compilar. El primero es un cambio de estrategia deliberado: dejar de parchear fugas de memoria de Swing/GIF una por una (§1.7, §1.13-§1.15) y en su lugar **reiniciar el proceso completo de la JVM al final de cada noche** -- decisión explícita del usuario de priorizar estabilidad sobre seguir cazando fugas puntuales.

**1. Reinicio completo del proceso (`ReiniciadorJuego.java`, nuevo) -- investigación previa, luego implementación.**
- **`LanzadorEscape` (revisado, no modificado):** ya demuestra que relanzar un proceso Java desde este proyecto es viable y ya está en producción (para Five Doors Escape) -- pero apunta a OTRO proyecto vía `gradlew.bat`. Para reiniciarse a sí mismo, no hace falta gradlew ni resolver otro repo: basta con `System.getProperty("java.home")+"/bin/java.exe"` + `System.getProperty("java.class.path")` (el propio classpath con el que este proceso ya está corriendo, JavaFX incluido automáticamente vía el mecanismo de extensión de Java 8 en `jre/lib/ext`) + la misma clase `com.fdaf.init.Main`.
- **Progreso entre noches (revisado):** `PersistenciaJuego`/`PreferenciasJuego` YA escriben a disco (`~/.fivedoorsatfreddys/config.properties`) de forma completamente independiente del proceso -- `alGanar()` ya llama `PersistenciaJuego.guardar()` justo después de avanzar `nocheActual`, ANTES de cualquier efecto visual. Esto significa que un reinicio de proceso **no necesita ningún mecanismo nuevo de traspaso de estado** (a diferencia de Swing→Escape, que sí necesita `handoff.json` porque son ejecutables distintos) -- `Main.main()` ya llama `PersistenciaJuego.cargar()` antes de nada más, así que la instancia nueva recupera la noche/idioma/volumen solos con el mecanismo que ya existía.
- **Qué pasa hoy tras ganar/perder/rendirse (revisado):** los 3 caminos volvían al menú **dentro del mismo proceso** (`vp.setContenido(menu)` + `limpiarPartidaActiva()`) -- exactamente el patrón que el usuario pidió dejar de usar. `ControllerMenu.jugar()` construye un `ControllerInterfaz` (y por lo tanto un `Sonido respiracion`, un `PnlJuego`, etc.) nuevo en cada noche, así que los objetos Java en sí no se reutilizan entre noches -- el problema real nunca fue "reutilizar objetos", sino recursos NATIVOS (hilos "Image Animator" de AWT para gifs, líneas de audio nativas) que sobreviven a que sus objetos Java queden inalcanzables, exactamente el patrón de fugas ya documentado en §1.7/§1.13-§1.15. Matar el proceso entero es la única forma de garantizar que el sistema operativo libere TODO eso sin siquiera necesitar identificar cada fuga una por una.
- **Diseño de `ReiniciadorJuego.reiniciar(VistaPrincipal vp, Runnable siFalla)`:** muestra una pantalla de carga simple (`"Cargando..."`/`"Loading..."`, bilingüe con el mismo patrón de ternarios ya usado en todo el proyecto) sobre `vp`, espera 900ms reales (tiempo de sobra para que la nueva JVM arranque, evitando el instante de "ninguna ventana visible" que se vería como si el juego se hubiera cerrado solo), lanza `java.exe -cp <mismo classpath> com.fdaf.init.Main` con el mismo directorio de trabajo (`user.dir`, relevante para que `LanzadorEscape` de la nueva instancia siga resolviendo `five_doors_escape` correctamente), y si el lanzamiento tiene éxito hace `System.exit(0)`. **Si `lanzarNuevaInstancia()` falla** (p. ej. `java.exe` no se encuentra en `java.home`), **nunca se deja al jugador atascado en la pantalla de carga** -- se invoca `siFalla`, que cada llamador define como el comportamiento que existía ANTES de este mecanismo (volver al menú dentro del mismo proceso). Sin `inheritIO`/redirección de E/S a propósito: el proceso viejo muere segundos después de lanzar al nuevo, así que cualquier escritura a stdout del hijo tras ese punto simplemente falla en silencio (comportamiento normal de `PrintStream`, no hace falta redirigir a ningún lado).
- **Único punto de entrada real, `ControllerInterfaz.reiniciarJuegoCompleto()`:** llamado desde los 3 caminos normales de fin de partida -- `alGanar()` (noches 1-4, reemplaza el `vistaPrincipal.setContenido(menu)` directo), el botón Reintentar de Game Over (`avanzarDespuesDelGifLargo()`), y `ControllerCamara.rendirse()` (vía la referencia `interfaz` que ya tenía). **Deliberadamente NO se tocó el camino de victoria de la Noche 5** (transición a Five Doors Escape, §1.11/§2.13) -- ya lanza su propio proceso pesado separado, con su propia gestión de memoria; forzar además un reinicio del proceso Swing ahí habría sido una complicación innecesaria sin beneficio real, fuera del alcance de lo pedido ("cuando una noche termine" se interpretó como los 3 finales normales de noche, no el final especial de campaña).

**Verificación real, de punta a punta, con procesos de verdad (no simulado con reflection en el mismo proceso -- eso no probaría nada sobre reinicios reales):**
- **Mecanismo de reinicio aislado:** un arnés (`TestReinicio.java`, temporal, fuera del repo) construye una `VistaPrincipal` sin mostrarla y llama a `ReiniciadorJuego.reiniciar(...)` real. Orquestado desde PowerShell (`Get-CimInstance Win32_Process`, mismo patrón ya usado en sesiones anteriores de este proyecto para gestionar procesos Java reales): PID viejo real confirmado (`ManagementFactory.getRuntimeMXBean()`), heap viejo medido (`Runtime.totalMemory()-freeMemory()`), y tras el reinicio: **el PID viejo se confirma muerto** (`Get-CimInstance` ya no lo encuentra) y **aparece un PID nuevo real ejecutando `com.fdaf.init.Main`** (confirmado por `CommandLine`). Heap del proceso nuevo medido externamente con `jcmd <pid> GC.heap_info` (mismo tipo de herramienta externa ya usada en §1.14 con `jmap`) -- confirma que es una JVM genuinamente fresca, no el mismo proceso disfrazado.
  - **Hallazgo real durante esta prueba, no un bug de `ReiniciadorJuego`:** el primer intento con un classpath incompleto (sin `flatlaf-3.6.1.jar`, el WIP ajeno de tema visual ya documentado en §2.19, todavía sin commitear) hizo que el proceso hijo muriera con `NoClassDefFoundError` casi al instante -- confirmado lanzando `com.fdaf.init.Main` directamente con el mismo classpath incompleto y capturando `stderr`. **No es un bug del mecanismo de reinicio**: `ReiniciadorJuego` reutiliza fielmente `System.getProperty("java.class.path")` del proceso que lo invoca, así que en cualquier ejecución real (Eclipse, con el WIP de FlatLaf activo o no) el classpath real siempre incluye lo que hace falta -- el classpath incompleto fue solo un artefacto de cómo se armó la prueba manualmente. Confirmado repitiendo la prueba completa con el classpath real completo: funciona de punta a punta.
- **Progreso conservado entre "noches", con 3 procesos JVM real y completamente separados (no la misma JVM con reflection):** proceso 1 escribe `NOCHE_2`/`ESPANOL`/`7` a disco; proceso 2 (JVM nueva) lee ese estado, simula exactamente la lógica real de `alGanar()` (avanza el ordinal, guarda) y termina; proceso 3 (JVM nueva otra vez, mismo arranque que haría `Main.main()`) confirma leer `NOCHE_3`/`ESPANOL`/`7` -- progreso conservado correctamente a través de 3 fronteras de proceso reales. El archivo real de progreso del usuario (`~/.fivedoorsatfreddys/config.properties`, que ya tenía `NOCHE_5`/`INGLES`/`10` de una sesión real anterior) se respaldó antes de la prueba y se restauró exactamente después -- no se alteró el progreso real del usuario.
- **Camino de fallo (`siFalla`):** probado de forma quirúrgica invocando por reflection solo `lanzarNuevaInstancia()` (el método privado) con `java.home` apuntando a una ruta inexistente **solo durante esa llamada puntual** -- confirma que devuelve `false` de forma segura (sin excepción) y loguea la causa real. (Un primer intento de romper `java.home` para todo el proceso y llamar a `reiniciar()` completo reveló que eso también rompe la carga de fuentes nativas de AWT del propio proceso de prueba -- un efecto secundario del método de prueba, no del código de producción; se descartó ese enfoque a favor de la prueba quirúrgica sobre el método privado.)
- **Limpieza:** cada proceso de juego real lanzado durante las pruebas (ventanas reales, `VistaPrincipal` sin decorar en pantalla completa) se cerró explícitamente (`Stop-Process`) apenas se confirmó lo que hacía falta confirmar -- no quedó ningún `java.exe` corriendo al terminar (`Get-CimInstance Win32_Process -Filter "Name='java.exe'"` vacío al final). Todo el código de prueba (`TestReinicio*.java`, `TestPersistencia.java`) se eliminó del todo, no forma parte del repo.

**2. Respiración agitada dejaba de sonar tras algunos jumpscares -- causa raíz real encontrada, no un parche.** Investigado sin asumir nada, leyendo el ciclo de vida completo de `Sonido`: `respiracion` (`ControllerInterfaz`) es el **único** campo de audio de todo el proyecto que se construye UNA vez y se reproduce (`.play()`) muchas veces sobre la MISMA instancia a lo largo de la partida (cada animatrónico sobrevivido) -- los otros +30 sitios de uso siempre hacen `sonidoX = new Sonido(...); sonidoX.play();` (instancia fresca cada vez, nunca reutilizada). El `LineListener` agregado en §1.14 (para cerrar `Clip`s y resolver el primer/segundo `OutOfMemoryError`) cierra el `Clip` en **cualquier** `LineEvent.Type.STOP` -- incluyendo el STOP natural que Java dispara solo cuando un clip no-loop termina de reproducirse por su cuenta, no solo cuando alguien llama `stop()` explícitamente. La bandera `reiniciando` ya existente solo protege el reinicio síncrono interno de `play()` (cuando se reproduce sobre un clip que YA está sonando) -- nunca cubrió el caso real de `respiracion`: terminar de sonar sola, sin que nadie haya llamado a nada. Resultado real: el **primer** jumpscare sobrevivido dejaba el clip cerrado para siempre (confirmado con una prueba real que revela que sobrevive 1 ciclo por una condición de carrera de timing, pero falla consistentemente desde el segundo -- coincide exactamente con el reporte del usuario, "después de ALGUNOS jumpscares"); cada `play()` posterior entraba en el guard silencioso `if (clip==null || !clip.isOpen()) return;` sin ninguna excepción ni log.

**Corrección real:** nuevo parámetro `reutilizable` en `Sonido` (constructor `Sonido(String archivo, boolean reutilizable)`, el constructor de 1 argumento existente delega con `false` -- cero cambio de comportamiento para los +30 sitios de uso ya existentes). Cuando `reutilizable=true`, el `LineListener` nunca cierra el `Clip` por su cuenta ante un STOP; el propio llamador es responsable de cerrarlo explícitamente cuando de verdad terminó de usarlo, vía el nuevo método público `Sonido.cerrar()`. `respiracion` ahora se construye con `reutilizable=true`, y `detenerSonidosJuego()` (el único punto real de fin de partida) llama `respiracion.cerrar()` en vez de `respiracion.stop()`.

**Verificado con una prueba real (no solo lectura de código):** un arnés (`TestRespiracion.java`, temporal) reproduce `respiracion` 4 veces seguidas, dejando que cada reproducción termine SOLA (esperando la duración real del `.wav`, 4435ms, medida con `getDuracionMs()`) antes de la siguiente -- exactamente el patrón real de varios jumpscares espaciados en el tiempo. **Antes del fix:** el clip aparecía cerrado (`isOpen()==false`) ya desde el segundo ciclo, y el intento de reproducir de nuevo no sonaba nada (`isActive()==false`). **Después del fix:** el clip permanece abierto y reproduce de verdad los 4 ciclos, y `cerrar()` sigue cerrándolo correctamente al final de la partida (verificado explícitamente, sin fuga). El fix se probó primero revirtiéndolo temporalmente para confirmar que el arnés SÍ detecta la regresión, igual que en §1.16.

**3. Noche actual visible en la tableta.** Nuevo `JLabel lblNocheActual` en `PnlTableta`, ubicado justo debajo del ícono de batería/POWER LEFT existente (mismo bloque "Información" de la tableta). Texto fijado una sola vez en `ControllerInterfaz.init()` (`actualizarTextosPartida()`, nuevo) -- el idioma no puede cambiar a mitad de partida (solo se elige desde el menú), así que no hace falta refrescarlo en ningún otro punto. Usa `PreferenciasJuego.nocheActual.ordinal()+1` (la misma fuente de verdad que ya usa `Juego.java` para vidas/coleccionables de la noche, incluida Noche Personalizada) con el mismo patrón de ternario de idioma ya establecido en todo el proyecto (`"NOCHE "`/`"NIGHT "`) -- el proyecto Swing nunca tuvo un sistema de i18n basado en archivos (a diferencia de Escape, ver §2.31); "el sistema existente" es este patrón de ternarios repetido, no uno nuevo.

**4. Label "Rendirse"/"Surrender" sobre la barra de progreso.** Nuevo `JLabel lblRendirseTexto` en `PnlTableta`, posicionado directamente encima de `pbarRendirse` (la única `JProgressBar` de todo el proyecto -- confirmado que no existe ninguna "barra de coleccionables" aparte). Mismo patrón de fijado único en `actualizarTextosPartida()`. Al activarse (mantener presionada hasta 100%), `ControllerCamara.rendirse()` ahora llama a `interfaz.reiniciarJuegoCompleto()` en vez de `vp.setContenido(menu)` directo -- **mismo camino de reinicio que ganar/perder, sin ninguna ruta alternativa**, tal como pidió el usuario explícitamente.

**Verificación visual real de 3 y 4 (sin `Robot`, mismo patrón ya establecido en §1.13):** un arnés temporal pintó `PnlTableta` completo sobre un `BufferedImage` fuera de pantalla y guardó capturas PNG reales -- confirmado que "NOCHE 3" aparece legible justo debajo del hueco de POWER LEFT/batería, y "Rendirse" aparece legible justo encima de la barra de progreso, sin superponerse con ningún otro elemento (`lblTabletCerrar` no se solapa en X). Bounds de los 2 labels nuevos también se agregaron a `EscalarVista.adaptarTablet` (mismo patrón de escalado ya usado para el resto de la tableta -- solo bounds, no tamaño de fuente, igual que `lblDescripcion`).

**Commiteado y pusheado a `FiveDoorsAtFreddys`:** `ReiniciadorJuego.java` (nuevo) + `reiniciarJuegoCompleto()` en `ControllerInterfaz.java` + hook en `ControllerCamara.rendirse()`, corrección de `respiracion` en `Sonido.java`/`ControllerInterfaz.java`, `lblNocheActual`/`lblRendirseTexto` en `PnlTableta.java` + `EscalarVista.java`. Toda la instrumentación temporal de esta sesión (arneses de prueba con procesos reales, capturas de la tableta) se eliminó por completo -- confirmado sin restos en el repo.

**Validación de extremo a extremo de §1.18, sesión 2026-08-09 (misma fecha, sesión posterior):** pedido explícito del usuario de jugar una partida real completa (no solo pruebas aisladas) antes de dar por cerrado el reinicio de proceso. Cubrió, con evidencia real (procesos reales orquestados con PowerShell, nunca solo reflection dentro de una única JVM): arranque normal, tableta (noche/POWER LEFT/Rendirse, español e inglés), 14 ciclos de puertas con generación **real** del árbol (no forzada) alternando NADA/COLECCIONABLE/ANIMATRONICO, 12 jumpscares reales consecutivos confirmando que `respiracion` sigue sonando en todos, y los 4 finales de noche (perder, ganar, rendirse, Noche 5→Escape) cada uno con PID viejo confirmado muerto y PID nuevo confirmado vivo donde corresponde -- **no se encontró ningún bug real**; el único fallo fue un error de orden en el propio arnés de prueba (persistir la noche base en disco antes de simular la partida), corregido en la prueba misma. No hubo cambios de código en esta sesión de validación.

### 1.19 `LesToreadorsRemix` mucho más bajo tras el RUN, sesión 2026-08-09 (misma fecha, sesión posterior)

**Contexto:** pedido explícito del usuario -- la reducción de volumen de `LesToreadorsRemix.wav` tras la risa de Freddy (§1.14, -2.5dB) era insuficiente; pidió que bajara "bastante más" para que la música espacial de Freddy (lado Escape) destaque mejor, sin tocar el volumen de ningún otro sonido (risa de Freddy, música de Freddy, latidos, estática, etc.) ni el resto del comportamiento (la canción sigue sonando sin reiniciarse).

**Investigación primero, sin asumir dónde vivía el mecanismo real** -- aunque el usuario lo mencionó como parte de "Five Doors Escape", el cambio de volumen en sí ocurre en `ControllerInterfaz.iniciarEsperaRisaFreddy()` (lado **Swing/FDAF**), no en Escape -- Escape solo escribe la señal de archivo (`SenalRisaFreddy.marcar()`) cuando suena su propia risa de Freddy; quien reacciona bajando el volumen es el timer que ya vigilaba esa señal del lado Swing. Único cambio: `sonidoTransicionEscape.subirVolumen(-2.5f)` → `subirVolumen(-15f)` (una sola constante, mismo mecanismo, mismo único punto de llamada).

**Verificado con una prueba real** (`TestVolumenLesToreadors.java`, temporal): un `Sonido` real de `LesToreadorsRemix.wav` (camino de streaming, `SourceDataLine`, confirmado `esStreaming()==true`) más un `Sonido` de control completamente aparte, siguiendo el orden real de producción (`iniciarEsperaRisaFreddy()` arranca primero -- y de paso borra cualquier señal vieja, comportamiento correcto y ya documentado -- la señal real se escribe DESPUÉS, mientras el timer ya vigila). Confirmado: el volumen cae exactamente -15.0dB (no un valor aproximado), el sonido de control queda perfectamente intacto, y la señal de archivo se consume normalmente. **Nota metodológica real:** un primer intento de esta prueba escribió la señal ANTES de arrancar el timer -- fallo del arnés, no de producción: `iniciarEsperaRisaFreddy()` borra cualquier señal existente como primera acción (protección real contra una señal vieja de una sesión anterior), así que escribirla antes de que el timer arranque hace que se borre sin que el timer llegue a verla nunca. Corregido invirtiendo el orden en la prueba para que coincida con el real (Escape solo puede escribir la señal después de que Swing ya esté vigilando, nunca antes).

**Commiteado y pusheado a `FiveDoorsAtFreddys`:** `ControllerInterfaz.java` (constante de reducción de volumen, -2.5dB → -15dB).

### 1.20 Ronda de pulido y preparación para distribución, sesión 2026-08-10 (misma fecha, sesión posterior a §1.19/§2.33)

**Contexto:** ronda grande de 8 pedidos explícitos del usuario -- balance/pulido general antes de repartir el proyecto a un compañero, con instrucción explícita de probar de verdad, no solo compilar, y de no sobrescribir el WIP ajeno ya documentado (§1.17: `.classpath`/`Main.java` tema FlatLaf, `Juego.java` gate de batería) -- ese WIP se mantuvo intacto en el árbol de trabajo, sin commitear, exactamente igual que en sesiones anteriores.

**1. `LesToreadorsRemix` bajado de más (-15dB, §1.19, se sentía "demasiado bajo") a un punto medio real.** Nuevo valor: `-8dB` (~40% del volumen lineal previo, `10^(-8/20)`) en `ControllerInterfaz.iniciarEsperaRisaFreddy()` -- único cambio, mismo mecanismo. **Verificado con una prueba real** (arnés temporal, mismo patrón que §1.19): `Sonido` real de `LesToreadorsRemix.wav` (streaming) + un `Sonido` de control aparte -- delta real medido exactamente `-8.0dB`, control con delta `0.0` (intacto), hilo de streaming confirmado vivo (`hiloStreaming.isAlive()==true`) después del cambio de volumen -- la canción sigue sonando, no se reinicia ni se duplica.

**2. Botón ESCAPE en Custom Night, desbloqueado tras completar la Noche 5 una vez.** Decisión de arquitectura tomada por el usuario explícitamente (no inferida): desbloquear **solo** el botón ESCAPE, no toda la Custom Night -- menor riesgo de romper progresión. Nuevo campo persistido `PreferenciasJuego.escapeDesbloqueado` (`PersistenciaJuego`, mismo patrón `Properties` que idioma/noche/volumen), fijado a `true` en `ControllerInterfaz.alGanar()` solo cuando `eraNoche5==true` (una sola vez, con guardado propio porque el bloque de avance de noche no guarda en ese caso -- `NOCHE_5` no tiene "noche siguiente"). Nuevo `JButton btnEscape` + `JLabel lblEscapeBloqueado` en `PnlNochePersonalizada` (columna aparte a la derecha de las 5 noches, `x=920`, fuente propia `Fuentes.obtener(38)` -- el texto bloqueado "ESCAPE (Bloqueado)"/"ESCAPE (Locked)" no entraba legible al tamaño de los demás botones, confirmado con una captura real fuera de pantalla antes de ajustar el ancho/fuente). Bloqueado: texto atenuado (gris `140,140,140`) + clic reproduce `error_al_presionar_boton.wav` y muestra un mensaje bilingüe transitorio (3s) "Se desbloquea al completar la última noche"/"Unlocks after completing the last night" -- nunca lanza Escape. Desbloqueado: texto blanco normal, clic llama `ControllerMenu.lanzarEscapeDesdeCustomNight()` (nuevo) -- reutiliza `LanzadorEscape.lanzar()` tal cual (mismo mecanismo real que la victoria de la Noche 5), con una pantalla de carga mínima propia (no el glitch de `PnlWin`, que es específico de la secuencia de victoria) y `vidasFinales=Noche.NOCHE_5.getVidas()` (valor sin efecto real hoy -- verificado que `HandoffData.vidasFinales` no se lee del lado Escape más allá de guardarse). "Nueva Partida" (que promete "reiniciar TODO el progreso") también resetea `escapeDesbloqueado=false` -- decisión de consistencia con lo que el botón dice, no pedida explícitamente pero razonable dado el texto del diálogo de confirmación.

**Verificado con pruebas reales (headless, reflection, mismo patrón ya establecido en el proyecto), no solo lectura de código:** estado inicial bloqueado (texto/color correctos), clic bloqueado muestra el mensaje sin lanzar nada, `escapeDesbloqueado=true` sobrevive un ciclo real guardar+recargar por `PersistenciaJuego` (aislado en un `user.home` temporal), texto pasa a "ESCAPE" blanco tras desbloquear. Además, en la integración completa (punto 5 más abajo): un `alGanar()` real sobre la Noche 5 puso `escapeDesbloqueado=true` en memoria Y en disco, y confirmadamente **no** disparó `reiniciarJuegoCompleto()` (a diferencia de Noches 1-4).

**3. Balance de baterías, Noche 2-5 (Noche 1 intacta por pedido explícito).** Investigación real, no ajuste a ojo: simulación Monte Carlo (`SimBateria.java`, temporal, 100k partidas por noche) que **reutiliza las clases reales del juego sin modificar su lógica** (`Juego`/`GeneradorArbol`/`Arbol`, corridas contra una copia limpia del código sin el gate WIP de Noche 3+, para simular lo que realmente se va a commitear) -- un jugador que siempre revela cada puerta y siempre recoge coleccionables a tiempo (el mejor caso posible, para aislar el riesgo de pura mala suerte del riesgo por error del jugador) navegando aleatoriamente. **Hallazgo real, más severo de lo esperado:** con la tabla anterior, incluso ese jugador óptimo perdía la Noche 1 el 60% de las veces, y las Noches 2/3/4/5 el 78%/76%/85%/87% -- la causa estructural es que revelar un animatrónico cuesta una vida SIEMPRE (sin ventana de reacción ni forma de evitarlo salvo no explorar, lo cual tampoco es viable porque el árbol obliga a revelar para progresar), y el número esperado de animatrónicos para completar una noche (~3-4) ya se acerca o supera las vidas iniciales (2-4) incluso antes de cualquier mala suerte. Se probó **cuánto puede compensar la tabla de batería por sí sola**: incluso con probabilidades muy generosas (45-70%) la tasa de derrota simulada solo baja hasta un piso de ~58-66% -- ese piso lo impone la mecánica de revelación en sí, no la tabla, así que "regalar baterías" más allá de cierto punto no resuelve el problema de fondo y sí vacía la mecánica de tensión (pedido explícito del usuario: "no quiero simplemente regalar baterías"). Tabla final elegida (mejora real de ~10-14 puntos porcentuales, no agresiva): Noche 2 `11%/8%→30%/22%`, Noche 3 `28%/13%→46%/28%`, Noche 4 `32%/16%→50%/32%`, Noche 5 `36%/19%→62%/42%` (empuje extra en Noche 5, pedido explícito) -- baja las tasas simuladas a ~68%/66%/74%/73%. **Noche 1 (`8%/5%`) deliberadamente sin tocar.** Este hallazgo (revelar = daño garantizado, sin agencia del jugador en el momento) es un problema de **diseño/mecánica**, no de probabilidades -- fuera del alcance pedido esta sesión (que pidió explícitamente solo tocar la tabla de batería), documentado aquí para que quede constancia si se quiere revisar el mecanismo en sí más adelante.

**4. Tablet: "NOCHE N"/"Rendirse" más grandes y en negrita.** `lblNocheActual` (`PnlTableta`): `22→28pt`, `PLAIN→BOLD`, alto del label `40→46` (solo para no recortar la fuente más grande, x/y/ancho sin tocar). `lblRendirseTexto`: `20→26pt`, `PLAIN→BOLD`, alto `28→34`. Verificado con capturas reales fuera de pantalla (`printAll` sobre un `BufferedImage`, mismo patrón que §1.18) en español e inglés -- sin recortes, sin superposición con `pbarRendirse` ni con el hueco de POWER LEFT.

**5. Advertencia inicial (`PnlWarning`) reaparecía en cada noche -- causa raíz real, no un parche.** Investigado sin asumir: `ControllerMenu.init()` llamaba `mostrarAdvertenciaInicial()` incondicionalmente en cada construcción -- inofensivo antes de §1.18, pero desde que ganar/perder/rendirse **reinician el proceso completo** (`ReiniciadorJuego`), cada noche terminaba lanzando una JVM nueva que volvía a mostrar la advertencia, exactamente el bug reportado. **Corrección:** `ReiniciadorJuego.lanzarNuevaInstancia()` ahora agrega `"--reinicio-interno"` como argumento del proceso hijo; `Main.main(String[] args)` lo detecta (`ARG_REINICIO_INTERNO`) y construye `new ControllerMenu(reinicioInterno)` (nuevo constructor con un `boolean`, el de 0 argumentos delega con `false`); `ControllerMenu.init()` salta directo a `mostrarMenuPrincipal()` cuando `reinicioInterno==true`, sin instanciar `PnlWarning` ni tocar `advertenciaActiva`. Un lanzamiento externo real (`args` vacío/sin el marcador) sigue mostrando la advertencia como siempre.

**Verificado con dos niveles de prueba real:** (a) aislado -- `ControllerMenu(false)` real muestra `PnlWarning`/`advertenciaActiva=true`; `ControllerMenu(true)` real va directo a `PnlMenu`/`advertenciaActiva=false`; (b) **integración completa de punta a punta, con un proceso real reiniciándose a sí mismo** -- un arnés real llevó el menú real a través de "Nueva Partida" → saltar intro (mismo método real que usa CTRL+S) → `ControllerInterfaz` real → `alGanar()` real sobre la Noche 1 → esperó el video de victoria real (~9s) → confirmado por `Get-CimInstance Win32_Process` que el PID viejo murió y apareció un PID nuevo real con `com.fdaf.init.Main --reinicio-interno` en su línea de comandos exacta, proceso vivo y respondiendo, con `nocheActual=NOCHE_2` correctamente persistido -- une en una sola prueba real `alGanar()`, `reiniciarJuegoCompleto()`, `ReiniciadorJuego`, el parseo de argumentos de `Main` y el constructor nuevo de `ControllerMenu`.

> **⚠️ Incidente real durante esta prueba de integración, corregido en el momento:** la primera versión del arnés de integración **no aisló `user.home`** (a diferencia de todas las demás pruebas de esta sesión, que sí lo hicieron) -- el reinicio real escribió noche `NOCHE_2`/idioma `ESPANOL` sobre el `~/.fivedoorsatfreddys/config.properties` **real** del usuario, sobrescribiendo el progreso real documentado en §1.18 (`NOCHE_5`/`INGLES`/`10`). Detectado de inmediato al revisar el archivo tras la prueba (no se dio nada por sentado). Restaurado manualmente a `NOCHE_5`/`INGLES`/`10` (los valores documentados en §1.18) apenas se detectó. **`escapeDesbloqueado` se dejó en `false`** porque no hay forma de confirmar con certeza si el usuario ya había completado la Noche 5 de verdad antes de esta sesión (el campo es nuevo, no existía un valor previo real que restaurar) -- si el usuario sabe que ya la completó, puede corregirlo a mano o pedirlo. **Lección para sesiones futuras: CUALQUIER prueba real que toque `ControllerInterfaz`/`alGanar`/`ReiniciadorJuego` DEBE aislar `user.home` con una carpeta temporal desde el primer intento, sin excepción** -- ya era la práctica establecida para otras pruebas de esta misma sesión, este arnés en particular no la siguió.
>
> **Corregido en la sesión siguiente (2026-08-10):** se le preguntó directamente al usuario (única fuente real posible, ver razonamiento arriba) si ya había ganado la Noche 5 alguna vez jugando de verdad -- confirmó que sí. `escapeDesbloqueado` corregido a `true` en el `config.properties` real. `nocheActual`/`idioma`/`volumen` no necesitaron cambios (ya estaban correctos).

**6. Bug real encontrado durante la investigación de distribución (no relacionado con los pedidos anteriores): `abanico.gif` con mayúscula/minúscula inconsistente.** `CargarImagenes.java` pedía `/gifs/fondos/abanico.gif` pero el archivo real en disco es `Abanico.gif` (A mayúscula) -- funcionaba por pura casualidad al correr desde una carpeta de clases suelta en Windows (NTFS es insensible a mayúsculas), pero **rompía con `NullPointerException` real e inmediata al arrancar** en cuanto el juego se empaqueta en un `.jar` de verdad (las entradas de un `.jar` SÍ son sensibles a mayúsculas) -- nunca se había detectado porque ninguna sesión anterior había ejecutado el juego desde un `.jar` empaquetado. Encontrado auditando programáticamente los 45 `getResource(...)` literales + los 18 `new Sonido(...)` literales + las 20 rutas de `TipoColeccionable` de todo el proyecto contra los archivos reales en disco (sensible a mayúsculas) -- único mismatch real encontrado; el resto (incluida la ruta dinámica `"/gifs/jumpscares/Jumpscare" + capitalizado + ".gif"`) resolvió correctamente. Corregido el literal en el código (no se tocó el archivo en disco). **Verificado con una corrida real desde un `.jar` empaquetado real:** antes del fix, `NullPointerException` en `CargarImagenes.<clinit>` impedía arrancar; después del fix, ventana real "Five Doors At Freddy's" abierta y cerrada limpiamente. (Nota aparte: `CargarSonido.java`, con una ruta igualmente rota `/resources/sounds/putdown.wav` que tampoco existe en disco, es código MUERTO -- no se referencia desde ningún otro lugar del proyecto -- no se tocó.)

**7. Distribución portable investigada y preparada -- probada de verdad, no solo documentada.**
- **Lado Swing:** se investigó `jpackage --type app-image` (empaqueta un runtime Java completo en un `.exe`). El build en sí funciona (usando el JDK 8 con JavaFX de esta máquina como `--runtime-image`), pero el `.exe` resultante falla en Windows con un error clásico de registro (`Software\JavaSoft\Java Runtime Environment...`) -- causa real investigada: `jpackage` está pensado para runtimes modulares (JDK 9+, típicamente `jlink`), no para un JDK 8 completo copiado tal cual; combinación que la herramienta acepta pero no soporta de verdad. El MISMO runtime empaquetado, invocado directamente (`runtime\bin\javaw.exe -jar juego.jar`), SÍ funciona -- confirmado con una ventana real abierta y cerrada limpiamente. Nuevo `FiveDoorsAtFreddys/build-portable.bat` (committeado): detecta un JDK 8 con JavaFX instalado (`JAVA8_HOME` o rutas típicas de Windows), compila, empaqueta el jar, copia el runtime completo y genera un lanzador `.bat` -- probado de punta a punta contra una copia limpia del código (sin el WIP de FlatLaf, que no se empaquetó) y contra el repo real: `dist/FiveDoorsAtFreddys/FiveDoorsAtFreddys.bat` abre una ventana real del juego sin ningún Java instalado en la máquina destino. Ver `SETUP.md` §6 para la posición relativa de carpetas necesaria (misma convención de carpetas hermanas de siempre) para que Noche 5 → Escape siga funcionando desde la carpeta portable.
- **Lado Escape:** `lwjgl3/build.gradle` ya tiene el plugin `application` activado -- `gradlew.bat lwjgl3:installDist` **ya funciona sin ningún cambio**, verificado con una corrida real (`lwjgl3/build/install/lwjgl3/`, ~205MB, todos los jars + nativos de LWJGL + assets ya empaquetados dentro del jar + un lanzador `bin\lwjgl3.bat`) -- solo sigue necesitando un JDK 21 instalado en la máquina destino. **No se integró con `LanzadorEscape`** (que sigue invocando `gradlew.bat lwjgl3:run`, ya extensamente probado) -- cambiar el mecanismo real de lanzamiento de Noche 5 → Escape se consideró un cambio de mayor riesgo, fuera de alcance de esta ronda (prioridad: estabilidad sobre cantidad de cambios). Documentado en `SETUP.md` §7 como próximo paso si se necesita una distribución de Escape sin internet/sin Gradle.
- **Ninguna ruta absoluta de esta máquina** (`C:\Users\dfarl\...`) encontrada en código Java/Gradle real de ningún proyecto -- solo en documentación (`SESSION_HANDOFF.md`, `VALIDACION_FASE3.md` del lado Escape), donde es apropiado que esté.

**Commiteado y pusheado a `FiveDoorsAtFreddys`:** volumen de `LesToreadorsRemix` (-8dB), botón ESCAPE en Custom Night + persistencia (`PreferenciasJuego`/`PersistenciaJuego`), tabla `PROBABILIDAD_BATERIA` de Noche 2-5 (Noche 1 intacta -- commiteado con `git add -p` para no arrastrar el gate WIP ajeno en el mismo archivo), fuente de la tablet, fix de la advertencia inicial (`Main.java`/`ControllerMenu.java`/`ReiniciadorJuego.java` -- `Main.java` commiteado vía staging manual para no arrastrar el WIP de FlatLaf del mismo archivo), fix de `abanico.gif`, `build-portable.bat` nuevo, `SETUP.md` actualizado. **El WIP ajeno de FlatLaf (`.classpath`/`Main.java`) y el gate de batería ajeno (`Juego.java`) siguen exactamente donde estaban, sin commitear, tal como en sesiones anteriores.**

### 2.32 Menú de pausa NO detenía el audio continuo, sesión 2026-08-09 (lado Escape, misma fecha que §1.19)

**Contexto:** pedido explícito del usuario -- al pausar con ESC, el menú de pausa ya congela la lógica/render (§2.31, `sceneManager.update(0f)`), pero toda la música/audio de la partida seguía sonando de fondo. Pidió pausa REAL (no solo silenciar visualmente), reanudación desde el mismo punto (sin reiniciar), y que los sonidos temporales ya terminados no reaparezcan.

**Investigación real, sin asumir.** `GameplayScreen` no importa `Music` en absoluto -- todo el audio continuo usa `Sound` (`javax.audio.Sound` de libGDX). Auditoría completa de todo `.loop(` en el proyecto (no solo en `GameplayScreen`): solo existen dos sonidos genuinamente continuos, ambos campos de instancia con un `id` de instancia propio:
- `sonidoMusicaFreddy`/`idSonidoMusicaFreddy` -- la "caja musical" espacial de Freddy (§2.30), arrancada UNA vez en `show()` (`Sound.loop(0f)`) y nunca detenida durante toda la sesión; volumen/pan se recalculan cada frame en `actualizarMusicaEspacialFreddy()`.
- `sonidoLatidoNormal`/`sonidoLatidoRapido` vía `idSonidoLatidoActivo` -- el latido de tensión (§2.26), arranca/cambia dinámicamente según la distancia al guardia más cercano en `actualizarLatidosYVineta()`.

El bloque `if (pausado) { ...; return; }` (línea ~716) ya existía y congelaba la lógica -- pero como retorna ANTES de volver a llamar a `actualizarMusicaEspacialFreddy()`/`actualizarLatidosYVineta()` cada frame siguiente, el `Sound.loop()` ya arrancado simplemente seguía sonando sin que nada le dijera lo contrario: la pausa nunca tocaba el audio, solo dejaba de *actualizar* su volumen/pan. El resto de los sonidos del juego (jumpscare, estática, risa de Freddy, narración de la cinemática, click de linterna) **nunca pueden estar sonando mientras `pausado==true`** -- sus estados (`JUMPSCARE`, `ESTATICA`, `CINEMATICA_INICIAL`, `INTRO_CORAZONES`, `INTRO_RUN`, `CORAZONES_RESPAWN`) hacen `return` en el `switch` de `render()` antes de siquiera llegar al chequeo de ESC -- no necesitan tratamiento.

**Corrección real: `Sound.pause(long id)`/`resume(long id)`, verificado contra el código fuente real de la librería (no supuesto).** Se extrajo y leyó directamente `OpenALSound.java`/`OpenALLwjgl3Audio.java` del jar de fuentes de `gdx-backend-lwjgl3` (mismo criterio que la corrección de rotación de personajes de §2.14, "no confiar en la documentación, leer la fuente real"): `pause(long soundId)` llama a `audio.pauseSound(soundId)`, que ejecuta `alSourcePause(sourceId)` -- la pausa NATIVA real de OpenAL, que conserva la posición de reproducción. `resume(long soundId)` llama a `alSourcePlay(sourceId)` **solo si** el source está realmente en estado `AL_PAUSED` -- resume desde el punto exacto donde se pausó, nunca reinicia. Nuevos métodos `pausarAudioContinuo()`/`reanudarAudioContinuo()` en `GameplayScreen`, llamados en el mismo punto donde ya se alternaba `pausado` (el chequeo de ESC) -- pausan/reanudan `sonidoMusicaFreddy` (si `idSonidoMusicaFreddy != -1`) y el latido activo en ese instante (si `idSonidoLatidoActivo != -1`), nada más.

**Verificación real, con una limitación de entorno encontrada e investigada a fondo (no descartada sin más).** Se intentó una corrida real acotada de `gradlew lwjgl3:run` con diagnóstico temporal (simulación de ESC sin input real -- `Robot` no es viable en este entorno, ver memoria de Claude ya existente) para confirmar en logs que `pausarAudioContinuo()`/`reanudarAudioContinuo()` se ejecutan sin excepción durante una partida real. La corrida falló de forma reproducible con `OutOfMemoryError` real cargando una textura (`Gdx2DPixmap`/`Pixmap`, `Couldn't load pixmap from image data: Out of memory`) durante `BootScreen`, **antes** de llegar siquiera a `GameplayScreen`. Se investigó a fondo antes de descartarlo como ajeno: memoria del sistema con 14+ GB libres (no es presión real de RAM), `-Xmx4G` explícito en el `run` task no cambió nada (confirma que no es límite de heap Java, sino una asignación nativa), reintentado sin daemon de Gradle y con daemon recién matado (mismo resultado) -- y, de forma concluyente, **el mismo error se reprodujo exactamente igual sobre el HEAD limpio sin ningún cambio de esta sesión** (`git stash` + reintento). Esto confirma que es un problema real del entorno de esta sesión (posible degradación de estado nativo/GPU tras muchas ejecuciones y cierres forzados de ventanas LWJGL a lo largo de esta sesión larga, consistente con la limitación ya documentada de "corridas LWJGL desatendidas largas" en la memoria de Claude del proyecto), **no introducido por este cambio ni por ningún código de esta sesión** -- no bloqueante para la corrección en sí, que se verificó en cambio contra el código fuente real de la librería (nivel más profundo de verificación disponible sin una ventana real: la llamada nativa exacta que se termina ejecutando). Toda la instrumentación temporal (campos `diagTiempoEnJugando`/`diagPausaHecha`/`diagReanudoHecha`, bloque de simulación de ESC en `render()`, `-Xmx4G` temporal en `lwjgl3/build.gradle`) se agregó y se retiró por completo -- confirmado con `git diff` limpio salvo el cambio real.

**Commiteado y pusheado a `five_doors_escape`:** `pausarAudioContinuo()`/`reanudarAudioContinuo()` + hook en el chequeo de ESC (`GameplayScreen.java`).

### 2.33 Cierre de §2.32 tras reinicio de Windows: verificación en vivo real, sesión posterior 2026-08-09

**El reinicio de Windows resolvió el problema de driver descrito al final de §2.32/`SESSION_HANDOFF.md`** (`WGL: Failed to make context current`, antes precedido por un `OutOfMemoryError` de texturas) -- `gradlew lwjgl3:run` volvió a crear ventana/contexto OpenGL sin error alguno.

**Pausa (§2.32): verificada en vivo, no solo por lectura de código de la librería.** Instrumentación temporal en `GameplayScreen.render()` simuló el toggle de ESC (4s tras entrar en `JUGANDO`, y de vuelta 4s después) y consultó el `AL_SOURCE_STATE` real de OpenAL del source de `sonidoMusicaFreddy` antes/después de cada toggle, via reflection sobre `OpenALLwjgl3Audio.getSourceId(long)` (público) + `AL10.alGetSourcei` -- necesario porque el módulo `core` de Escape no depende de `gdx-backend-lwjgl3`/LWJGL en tiempo de compilación. Resultado real capturado en una corrida real:
```
ANTES de pausar: AL_PLAYING(4114) -> DESPUES de pausar: AL_PAUSED(4115)
ANTES de reanudar: AL_PAUSED(4115) -> DESPUES de reanudar: AL_PLAYING(4114)
```
Prueba directa a nivel de source nativo, la más fuerte disponible sin escuchar audio humano. Instrumentación retirada por completo después (`git diff` limpio en `five_doors_escape`).

**Señal cruzada de la risa de Freddy (lado escritura, Escape): verificada en vivo por primera vez.** En la misma corrida real, `SenalRisaFreddy.marcar()` escribió efectivamente `~/.fivedoorsatfreddys/risa_freddy.flag` en el instante real en que sonó la risa de Freddy (`GameplayScreen` llegó a RUN). El lado lectura/aplicación (`ControllerInterfaz.iniciarEsperaRisaFreddy()`, `-15dB`) ya estaba verificado con una prueba real en la sesión que cerró §2.32 (delta exacto confirmado con un sonido de control aparte) -- no se repitió esa prueba, solo se confirmó por lectura que el código sigue intacto.

**No verificado de oído humano** (limitación de entorno, no de código): ninguna sesión de Claude en esta máquina puede reproducir/escuchar audio real. Toda verificación de audio disponible es a nivel de estado nativo (OpenAL `AL_SOURCE_STATE`) y de señales de archivo -- ya el nivel más profundo posible sin un oyente humano.

**Nota aparte, no relacionada:** al revisar `five_doors_at_freddys/FiveDoorsAtFreddys` para esta verificación se encontraron cambios sin commitear preexistentes en `.classpath`, `Main.java` (`FlatDarkLaf`) y `Juego.java` (gating de batería desde Noche 3+), más `src/libs/` sin trackear -- trabajo en progreso de otra sesión/tarea, sin relación con audio/pausa. Se dejaron intactos.

### 2.34 Head bob sutil al caminar, sesión 2026-08-10 (misma fecha, sesión posterior a §1.20)

**Contexto:** pedido explícito del usuario -- balanceo de cámara muy sutil al caminar con WASD (head bob/sway), sin marear, sin interferir con el mouse-look, sin afectar la posición real del jugador ni las colisiones. Pidió investigar primero la implementación real de cámara/movimiento antes de tocar nada.

**Investigación real de `FirstPersonCameraController`/`GameplayScreen` antes de implementar.** El controlador YA separa limpiamente "input deseado" de "posición real": `computeWasdDelta(dt)` devuelve un vector de movimiento deseado sin tocar la posición; `GameplayScreen` lo resuelve contra `CollisionWorld.resolveMovement(...)` (con colisión real) y solo DESPUÉS llama `cameraController.applyToCamera(playerTransform.position)`, que fija `camera.position` exactamente a la posición ya resuelta y calcula `camera.direction` desde `yawDegrees`/`pitchDegrees` (controlados exclusivamente por el mouse en `update()`, vía `Gdx.input.getDeltaX/Y()`). Esta separación ya existente es exactamente lo que hace seguro agregar un offset cosmético: aplicarlo DESPUÉS de fijar `camera.position` a la posición real, sin tocar `playerTransform.position` (colisiones/IA) ni `yawDegrees`/`pitchDegrees` (mouse-look), garantiza por construcción que el bob nunca puede afectar ninguna de las dos cosas que el usuario pidió no tocar.

**Implementación real, basada en DISTANCIA recorrida (no en tiempo ni en input crudo) -- decisión deliberada.** Nuevo `FirstPersonCameraController.actualizarBob(dt, distanciaMovidaEsteFrame)`, llamado una vez por frame desde `GameplayScreen` con la distancia REAL que `collisionWorld.resolveMovement(...)` efectivamente logró mover al jugador ese frame (no el input W/A/S/D crudo) -- si el jugador queda deslizando contra una pared, el bob se ralentiza junto con el movimiento real en vez de seguir "caminando en el aire" a velocidad constante como si nada lo hubiera bloqueado (justo el tipo de "movimiento extraño" que el usuario pidió corregir si aparecía). Patrón estándar de head bob (Quake/Half-Life): balanceo vertical al DOBLE de frecuencia que el horizontal (`sin(fase*2)` vs `sin(fase)`, un rebote por cada pie) -- amplitudes `0.03` unidades verticales / `0.015` horizontales (~1.7% de la altura de ojos, dentro del rango típico de "sutil" en FPS reales), `0.7` ciclos horizontales por unidad de mundo recorrida. Envolvente de intensidad (`bobEnvolvente`, 0..1) sube/baja LINEAL en `0.15s` -- llega a **exactamente cero** (no asintóticamente) en reposo, garantizando cámara perfectamente estable al quedarse quieto (pedido explícito del usuario). El offset se proyecta sobre los ejes reales de la cámara (`camera.up`/`right = direction × up`), así que gira correctamente con el mouse-look sin acoplarse a él. Como `linternaJugador.position` ya se copia de `camera.position` DESPUÉS de `applyToCamera()`, la linterna hereda el bob automáticamente -- coherente con una linterna sostenida en la mano, sin cambio de código adicional.

**Probado con varios valores antes de quedarse con estos, y verificado con dos niveles de prueba real (no solo lectura de código):**
- **Matemática determinista** (`TestHeadBob.java`, temporal, contra la clase real con `GdxNativesLoader.load()` para poder construir una `PerspectiveCamera` real fuera del motor completo): caminando 2s a velocidad constante, el offset vertical osciló exactamente dentro de `[-0.03, 0.03]` y el horizontal dentro de `[-0.015, 0.015]` (los límites declarados, ni más ni menos); al detenerse, el offset llegó a **exactamente** `(0,0,0)` en `0.150s` (coincide con `BOB_TIEMPO_TRANSICION`) y se mantuvo ahí el resto de la ventana; deslizando al 10% de la velocidad real, la frecuencia de oscilación cayó a 1 cruce por cero en 2s vs 19 a velocidad completa (la amplitud NO se achica con la velocidad -- un paso más lento no es un paso más chico, solo más lento; lo que sí cae es cuántas zancadas completas caben en la misma ventana de tiempo real); la misma magnitud de distancia por frame (equivalente a S/A/D) produjo oscilación idéntica a W, confirmando que el bob es agnóstico a la dirección real del movimiento.
- **Corrida real del juego** (mismo patrón de instrumentación temporal ya usado en toda la sesión -- simulación de W/quieto/S sin necesitar input real de teclado, retirada por completo después): el log real coincidió exactamente con la prueba determinista -- osciló dentro de los límites esperados al empezar a moverse, y **el jugador arrancó tan cerca de Freddy que el movimiento simulado chocó contra su colisión casi de inmediato (~0.6s) y el bob se detuvo en seco en vez de seguir oscilando mientras estaba bloqueado** -- confirmación real, no planeada, de que el bob reacciona correctamente a colisión real del motor (no solo a distancias sintéticas de prueba). Quieto, el offset se mantuvo en exactamente `0.00000` durante varios segundos reales seguidos de log. Capturas de pantalla reales durante la ventana de movimiento no mostraron nada roto/glitcheado (aunque una de ellas quedó parcialmente tapada por un panel de Wi-Fi de Windows, ajeno a la prueba).

**Commiteado y pusheado a `five_doors_escape`:** `FirstPersonCameraController.java` (`actualizarBob()` + offset cosmético en `applyToCamera()`), `GameplayScreen.java` (cómputo de `distanciaMovidaEsteFrame` + llamada a `actualizarBob()`). Toda la instrumentación temporal (arnés `TestHeadBob.java`, simulación de movimiento en `GameplayScreen`) se retiró por completo -- confirmado con `git diff` limpio salvo el cambio real.

### 1.21 Distribución final de un solo paquete (Swing + Escape), sesión 2026-08-10 (misma fecha, sesión posterior a §2.34)

**Pedido explícito del usuario:** determinar si se puede entregar el juego completo (los dos proyectos) como una distribución que un compañero copie a otra máquina y funcione, sin la estructura de carpetas `C:\Users\dfarl\git\...`, sin JDK/Gradle instalado, con la transición Noche 5 → Escape y los reinicios de proceso funcionando igual. Pedido explícito de no aceptar "sí, se puede" sin evidencia real.

**Investigación real de los 3 mecanismos existentes antes de tocar nada:**
- `LanzadorEscape.lanzar()` resolvía `five_doors_escape` de forma relativa (`RUTA_RELATIVA_ESCAPE = "../../five_doors_escape"`, relativa a `user.dir`) y siempre invocaba `gradlew.bat lwjgl3:run` -- funciona, pero **asume la estructura de repos Git hermanos**, necesita JDK 21 instalado (aunque ya se autodetecta, §1.17) e internet la primera vez (Gradle descarga dependencias). Esto es exactamente lo que NO puede depender de una distribución portable.
- `ReiniciadorJuego` (§1.18, reinicio de proceso al ganar/perder/rendirse) ya es 100% portable por diseño -- usa `System.getProperty("java.home")`+`System.getProperty("java.class.path")` del propio proceso en marcha, nunca rutas fijas ni gradlew. **No necesitó ningún cambio.**
- `build-portable.bat` (§1.20) ya resolvía la mitad del problema -- empaqueta Swing con runtime JDK8+JavaFX embebido. Lo que faltaba era el lado Escape y la orquestación de los dos juntos.
- `gradlew lwjgl3:installDist` (plugin `application`, ya activo, §1.20 §7) ya genera una distribución de Escape autocontenida (todos los jars + nativos LWJGL + assets empaquetados dentro del jar + script `bin\lwjgl3.bat`) sin ningún cambio -- **verificado real que el script generado respeta `JAVA_HOME`** (usa `%JAVA_HOME%/bin/java.exe` si está definido, cae a `java.exe` del PATH si no) -- exactamente el mecanismo que hace falta para apuntarlo a un runtime embebido sin tocar el script generado por Gradle.

**Decisión de arquitectura: extender `LanzadorEscape`, NO reescribirlo.** Nuevo método `resolverDistribucionEmpaquetada()`: busca una carpeta `FiveDoorsEscape` hermana (un nivel, `../FiveDoorsEscape` relativo a `user.dir` -- distinto de los dos niveles del modo desarrollo porque es la convención de la carpeta de DISTRIBUCIÓN, no del repo Git) con `runtime/bin/java.exe` (JDK 21 embebido) y `lwjgl3/bin/lwjgl3.bat` (salida de `installDist`) adentro. Si existe, `lanzar()` la usa directo: invoca `lwjgl3.bat` con `JAVA_HOME` apuntando al runtime embebido -- **nunca gradlew, nunca internet, nunca depende de la estructura del repo Git.** Si NO existe (entorno de desarrollo normal), cae exactamente al camino que ya existía (gradlew + autodetección de JDK 21 del sistema) -- **cero cambio de comportamiento para desarrollo**, código idéntico al de antes, solo movido a un bloque `else`.

**Gotcha real encontrado y corregido durante la implementación:** esta máquina **no busca el directorio actual al resolver nombres de comando pelados** (`gradlew.bat` a secas falla con "no se reconoce como un comando interno o externo" incluso parado justo en esa carpeta, confirmado real tanto desde `cmd.exe` como desde PowerShell -- probablemente `NoDefaultCurrentDirectoryInExePath` u otro endurecimiento similar) -- la ruta completa (`"%ESCAPE_DIR%\gradlew.bat"`) sí funciona siempre. `LanzadorEscape.java` ya usaba rutas completas (`File.getAbsolutePath()`) en todos lados, así que no lo tenía -- pero el nuevo `build-distribucion.bat` sí lo tuvo al principio y se corrigió. **Regla para scripts `.bat` futuros de este proyecto: nunca invocar un `.bat`/`.exe` por nombre pelado, siempre con su ruta completa.**

**Nuevo `build-distribucion.bat` (raíz de este repo)** orquesta las 4 etapas: build de Swing (reutiliza `build-portable.bat`, sin duplicar lógica) → detecta JDK 21 → `gradlew lwjgl3:installDist` en `five_doors_escape` → ensambla `dist-completa/` con la convención de carpetas hermanas (`FiveDoorsAtFreddys/` + `FiveDoorsEscape/`) que `resolverDistribucionEmpaquetada()` espera. `dist/` y `dist-completa/` agregados a `.gitignore` (son salidas de build de ~600MB-1GB, no se commitean).

**Verificado de punta a punta con un flujo real, no solo con el build.** Se construyó la distribución completa real (~1.1GB: 594MB Swing + 522MB Escape, cada uno con su propio runtime) usando un `git worktree` temporal en HEAD (para no mezclar el WIP ajeno de FlatLaf en el build, mismo criterio que build-portable.bat en §1.20) más el repositorio real de `five_doors_escape` como hermano. Luego:
1. `FiveDoorsAtFreddys.bat` de la distribución abrió una ventana real ("Five Doors At Freddy's"), usando su propio runtime embebido -- confirmado con el proceso real corriendo desde `dist-completa\FiveDoorsAtFreddys\runtime\bin\javaw.exe`.
2. Un arnés real (aislando `user.home` -- lección de §1.20 aplicada desde el principio esta vez) corrió con el runtime Y el jar reales de la distribución, con `user.dir` puesto exactamente en la carpeta de la distribución (igual que vería el `.bat` real), y disparó un `alGanar()` real sobre la Noche 5.
3. **Resultado real, capturado en el log y en la lista de procesos real de Windows:** `[ESCAPE] Usando distribucion empaquetada: ...\dist-completa\FiveDoorsEscape\lwjgl3`, seguido de un proceso hijo real `cmd /c ...\FiveDoorsEscape\lwjgl3\bin\lwjgl3.bat`, que a su vez lanzó `...\FiveDoorsEscape\runtime\bin\java.exe -classpath ...\FiveDoorsEscape\lwjgl3\lib\*.jar... com.fivedoorsescape.lwjgl3.Lwjgl3Launcher` -- **el proceso de Escape real, corriendo enteramente desde dentro de la carpeta de distribución, sin tocar `gradlew`, sin ningún JDK del sistema, sin ninguna referencia a `C:\Users\dfarl\git\...`.**
4. Ese proceso cargó los 4 animatrónicos (glTF real, tangentes calculadas), generó los colisionadores estáticos reales (3549), y llegó a RUN (`Risa de Freddy reproducida`) -- **sin ningún error ni excepción**, exactamente igual que cualquier otra corrida real de esta sesión.
5. `escapeDesbloqueado` se confirmó `true` en memoria tras el `alGanar()` real de la Noche 5, dentro de la misma corrida.

**Regresión verificada del camino de desarrollo (no solo el nuevo):** con `resolverDistribucionEmpaquetada()` invocado por reflection desde el `user.dir` real del repo de desarrollo (sin ninguna carpeta `FiveDoorsEscape` hermana ahí), devolvió `null` -- confirma que sigue cayendo al camino de `gradlew` exactamente como antes, sin cambio de comportamiento para nadie que siga trabajando desde el repo real.

**No probado en esta sesión (limitación real, no evadida):** el flujo completo interactivo con el jugador presionando teclas de verdad dentro de la ventana Swing de la distribución (`Robot` no es viable para este proyecto Swing, memoria ya documentada) -- se usó el mismo patrón real ya establecido (`ControllerMenu`/`ControllerInterfaz` reales vía reflection, nunca simulación aparte) para disparar el flujo real sin necesitar clicks reales. Tampoco se probó Retry/Surrender/reinicio de Noche 1-4 específicamente DESDE la distribución (aunque `ReiniciadorJuego` es idéntico al ya verificado en §1.18/§1.20 y no depende de rutas de distribución en absoluto, por diseño).

**Respuestas directas a las preguntas del usuario:**
- **¿Un solo `.exe`?** No literalmente uno solo -- `jpackage` para un `.exe` nativo de Swing con runtime JDK8 completo ya se investigó y falla por una limitación real de la herramienta (§1.20). La solución real y ya probada es **una carpeta con dos lanzadores `.bat`** (`FiveDoorsAtFreddys.bat` es el único que el usuario final necesita tocar) -- desde la perspectiva del jugador es "un solo juego": abre un archivo, juega, Noche 5 lo lleva a Escape solo.
- **¿Cierra Swing y abre Escape solo?** Sí, ya lo hacía (`System.exit(0)` real al salir de Escape, o vuelta al menú -- mecanismo preexistente, no tocado) -- lo nuevo es que Escape ahora puede arrancar sin `C:\Users\dfarl\git\...`.
- **¿Necesita JDK/Gradle en la máquina destino?** No -- ambos runtimes (JDK8+JavaFX para Swing, JDK21 para Escape) van embebidos dentro de la distribución. Gradle nunca se invoca en la máquina destino (solo en la máquina donde se CONSTRUYE la distribución, vía `build-distribucion.bat`).
- **¿Assets/audios/GIFs/GLB/progreso/señales entre procesos?** Todos verificados reales: los assets de Escape van empaquetados dentro del jar de `installDist` (confirmado, jar de ~199MB vs ~256MB de assets sueltos, compresión normal); el progreso y las señales cruzadas (`handoff.json`, `risa_freddy.flag`, `salir_completo.flag`) siempre usaron `System.getProperty("user.home")` (perfil real de Windows del usuario, no relativo a ninguna carpeta de instalación) -- funcionan igual sin importar si el juego corre desde el repo o desde la distribución.

**Commiteado y pusheado a `five_doors_at_freddys`:** `LanzadorEscape.java` (detección de distribución empaquetada + fallback), `build-distribucion.bat` (nuevo), `.gitignore`/`FiveDoorsAtFreddys/.gitignore` (`/dist/`, `/dist-completa/`). El `git worktree` temporal usado para probar se eliminó por completo (`git worktree remove`) -- no quedó rastro en el repo. La carpeta `dist-completa/` de casi 1.1GB generada para la prueba se eliminó por completo, no se commiteó (son salidas de build, no código fuente).

### 1.22 Prueba de "segunda máquina" de §1.21 -- limitación real reconocida, prueba aislada real hecha en su lugar, sesión 2026-08-10 (misma fecha, sesión posterior)

**Límite real, dicho explícitamente porque el usuario lo pidió así:** esta sesión de Claude **no tiene acceso a una segunda máquina física ni virtual** -- todo corre en la misma máquina de desarrollo. No se fingió una prueba en otra PC. En su lugar se hizo la prueba más rigurosa posible **dentro de la misma máquina simulando esas condiciones**, y se documenta con precisión qué quedó realmente probado y qué no.

**Metodología real:** `dist-completa/` se reconstruyó desde cero (`build-distribucion.bat`, mismo `git worktree` limpio en HEAD que §1.21) y se copió completa a `C:\FiveDoorsPortableTest\` -- una carpeta **fuera de cualquier repo Git, sin relación con `C:\Users\dfarl\git\...`**, lo más parecido a "recién copiada a otra máquina" que se puede lograr sin una máquina real.

**Hallazgo real de metodología (no un bug del producto) -- corregido en el momento:** el primer intento de aislar `user.home` para no tocar el progreso real del jugador usó la variable de entorno `USERPROFILE` (`cmd.exe /c "set USERPROFILE=... && FiveDoorsAtFreddys.bat"`) -- **no funcionó**: el proceso lanzado así siguió leyendo el `user.home` REAL de Windows (confirmado real: el menú mostró texto en inglés, coincidiendo exactamente con `idioma=INGLES` del `config.properties` real del usuario, no con la carpeta falsa). La JVM en Windows no deriva `user.home` de la variable de entorno `USERPROFILE` de forma confiable para un proceso lanzado así -- **la única forma confiable confirmada es `-Duser.home=...` como property directa de la JVM**, verificado correcto en el resto de la sesión. Se cerró esa ventana de inmediato (antes de cualquier acción que escribiera al disco) y se confirmó que el `config.properties` real del usuario no se alteró. **Lección para pruebas futuras de este tipo: usar siempre `-Duser.home=`, nunca depender de `USERPROFILE`.**

**Verificado real desde `C:\FiveDoorsPortableTest\` (aislado, con `-Duser.home` real):**
1. **`FiveDoorsAtFreddys.bat` real abre una ventana real** -- captura de pantalla real confirmando el menú completamente renderizado (título, fondo animatrónico, botones) usando `runtime\bin\javaw.exe` embebido.
2. **Instalación limpia (sin `config.properties` previo) arranca en ESPAÑOL por defecto**, como está en el código -- confirmado real (no una suposición): texto de botón "Nueva Partida", no "New Game".
3. **Toggle de idioma real ES→EN→ES** funcionó en ambas direcciones, con los textos de los botones actualizándose correctamente cada vez.
4. **Noche 1 completa real:** `alGanar()` real avanzó el progreso, y el proceso **se reinició solo** -- confirmado con la línea de comandos real del proceso nuevo: `C:\FiveDoorsPortableTest\FiveDoorsAtFreddys\runtime\jre\bin\java.exe -cp ...;C:\FiveDoorsPortableTest\FiveDoorsAtFreddys\FiveDoorsAtFreddys.jar com.fdaf.init.Main --reinicio-interno` -- runtime embebido, classpath dentro de la distribución, marcador de reinicio interno correcto, **cero referencias a `C:\Users\dfarl\git\...`**. Proceso nuevo confirmado vivo y respondiendo (`Responding=True`).
5. **Persistencia real confirmada** en el `config.properties` del home aislado: `nocheActual=NOCHE_2`, `idioma=ESPANOL` (el valor al que se volvió con el toggle) -- sobrevivió el reinicio de proceso real.
6. **Noche 5 → Escape real, repetido desde esta copia aislada específica** (no solo inferido de §1.21): `alGanar()` real sobre la Noche 5 produjo `[ESCAPE] Usando distribucion empaquetada: C:\FiveDoorsPortableTest\FiveDoorsEscape\lwjgl3`, el proceso de Escape cargó los 4 animatrónicos reales (glTF, tangentes) y llegó a RUN (`Risa de Freddy reproducida`) sin ninguna excepción.
7. **Grep real de los logs completos de ambas pruebas contra `C:\Users\dfarl\git`: cero coincidencias** en ambos casos.
8. **`java.home` de cada proceso confirmado apuntando SIEMPRE al runtime embebido dentro de `C:\FiveDoorsPortableTest\...\runtime\`, nunca a ningún JDK del sistema.**

**No verificado en esta sesión (dicho explícitamente, no evadido):**
- **Una segunda máquina física/virtual real** -- limitación de entorno ya explicada arriba. `C:\FiveDoorsPortableTest\` quedó preparada y ya probada en esta máquina específicamente para que el usuario la copie (USB/red/etc.) a una máquina Windows realmente distinta.
- **Desinstalar JDK/Gradle/Eclipse de esta máquina** para simular una PC "limpia" -- se descartó a propósito por ser una acción destructiva sobre el entorno real del usuario, fuera de lo que se pidió. La evidencia de "no depende de nada del sistema" es indirecta pero sólida: cada proceso lanzado confirmó `java.home` apuntando al runtime embebido, y `LanzadorEscape`/el `.bat` de Swing nunca consultan `PATH`/`JAVA_HOME` del sistema salvo que el mecanismo de distribución empaquetada no se detecte (fallback ya documentado en §1.21).
- **Surrender y Retry específicamente desde la distribución** -- ambos caminos llaman al mismo `ReiniciadorJuego.reiniciar()` ya verificado real en el punto 4 de arriba (código sin cambios, sin ninguna rama especial para estos dos casos), así que la evidencia se considera aplicable, pero no se disparó cada uno por separado esta ronda.
- **Interacción real de teclado/mouse dentro de la ventana de Escape/Swing de la distribución** (`Robot` no viable, ya documentado) -- Pirate Cove, puerta de escape, pantalla de victoria, controles, latidos, linterna: código sin cambios desde su verificación real en sesiones anteriores (§2.13-§2.15, §2.34, y las corridas reales de audio/pausa de §2.32-§2.33), no se volvieron a probar interactivamente desde esta distribución específica.

**Instrucciones para la prueba manual real en una segunda máquina (lo que el usuario debe hacer):**
1. Copiar `C:\FiveDoorsPortableTest\` completa (ambas carpetas, `FiveDoorsAtFreddys\` y `FiveDoorsEscape\`, ~1.1GB) a la otra máquina Windows -- USB, red, lo que sea más práctico. Mantener las dos carpetas como hermanas, sin renombrarlas.
2. En esa máquina, doble clic en `FiveDoorsAtFreddys\FiveDoorsAtFreddys.bat`.
3. Confirmar: aparece la advertencia inicial (primera vez), después el menú, con audio/gráficos normales.
4. Jugar una noche cualquiera hasta ganar/perder/rendirse -- confirmar que la ventana se cierra y una nueva aparece sola (reinicio de proceso), sin quedarse trabada.
5. Jugar hasta completar la Noche 5 (o usar el botón ESCAPE en Custom Night si ya está desbloqueado) -- confirmar que Swing se cierra y Escape abre solo, sin ninguna ventana de consola pidiendo Gradle ni mostrando errores de "comando no encontrado".
6. Dentro de Escape: confirmar controles (WASD+mouse), linterna (CTRL), head bob sutil al caminar, pausa (ESC), latidos, Pirate Cove, y llegar a la puerta de escape/pantalla de victoria.
7. Cerrar todo, volver a abrir `FiveDoorsAtFreddys.bat` -- confirmar que el progreso (noche actual, idioma) sigue ahí.
8. Reportar cualquier cosa que no coincida con lo de arriba -- eso sería la primera señal real de un problema no capturado por las pruebas de esta sesión.

**Limpieza de esta sesión:** todos los arneses de prueba temporales (`TestDistFull.java`, `TestDistNoche5.java`, `Screenshot.java`) y las 3 carpetas de home aisladas usadas se eliminaron por completo. `C:\FiveDoorsPortableTest\` se dejó intacta a propósito (no es un archivo temporal -- es el artefacto ya probado, listo para que el usuario lo copie). **No hubo cambios de código esta ronda** -- no se encontró ningún bug real en el producto, solo el hallazgo de metodología de prueba ya documentado arriba.

### 1.23 `FiveDoorsAtFreddys.exe` -- .exe nativo real, resuelto con launch4j, sesión 2026-08-10 (misma fecha, sesión posterior a §1.22)

**Pregunta explícita del usuario:** ¿se puede convertir la distribución portable en un único `.exe` real, manteniendo el reinicio de proceso, la persistencia, Noche 5 → Escape, y sin depender de rutas de esta máquina? Pedido explícito de investigar de verdad (no repetir "ya se intentó, falló" sin profundizar) y de priorizar **estabilidad > simplicidad > apariencia** -- no reemplazar la arquitectura de dos procesos (clave para liberar memoria, §1.18) por una sola JVM gigante.

**Investigación más profunda de por qué `jpackage` falla (§1.20/SETUP.md §6.1), no solo repetir el hallazgo anterior.** `jpackage` está diseñado alrededor de `jlink` (JDK 9+), que necesita el sistema de módulos que **JDK 8 no tiene en absoluto** (predata Java 9 por completo) -- no es una limitación de configuración, es una imposibilidad estructural: no existe forma de que `jlink` procese un runtime JDK 8. La ruta de respaldo de `jpackage` (copiar un JDK 8 completo como `--runtime-image`) es una combinación que la herramienta acepta sintácticamente pero no está diseñada para soportar, y dispara el bug de registro de Windows ya documentado.

**`launch4j` (proyecto abierto y maduro, ~20 años, `net.sf.launch4j` en Maven Central) investigado como alternativa real -- no una suposición.** Descargado con verificación real de checksum SHA1 contra Maven Central antes de ejecutar nada (`launch4j-3.50.jar` y `launch4j-3.50-workdir-win32.jar`, ambos coincidieron exactamente). Primer intento en modo **"wrapping"** (embebe el jar completo dentro del `.exe`, ~292MB resultante) -- **funcionó de verdad, ventana real del juego abierta**, a diferencia de `jpackage`. Segundo intento (más limpio) en modo **"launching"** (`dontWrapJar=true`, el `.exe` referencia `FiveDoorsAtFreddys.jar` externo sin embeberlo) -- `.exe` de solo ~60KB, sin la advertencia de falso-positivo de antivirus que launch4j emite para el modo wrapping, y con `<stayAlive>false</stayAlive>` el proceso del `.exe` termina apenas lanza el JVM real, dejando un único proceso limpio (`runtime\bin\javaw.exe -jar FiveDoorsAtFreddys.jar`) -- **exactamente el mismo comando final que ya generaba `FiveDoorsAtFreddys.bat`**, confirmado real comparando las líneas de comando de ambos. No se determinó con certeza el motivo exacto por el que `jpackage` sí dispara el bug de registro y `launch4j` no (ambos terminan invocando `javaw.exe` de un JDK 8 copiado) -- pero el resultado real y repetible es that launch4j funciona y jpackage no, verificado múltiples veces.

**`ReiniciadorJuego`/`LanzadorEscape`: confirmado que NO necesitan ningún cambio, por diseño ya existente.** Ambos operan exclusivamente sobre propiedades del proceso EN MARCHA (`System.getProperty("java.home")`, `"java.class.path")`, `"user.dir"`) -- nunca sobre cómo se invocó ese proceso originalmente. Esto significa que el reinicio de proceso YA bypaseaba cualquier launcher (`.bat` o `.exe`) desde antes de esta sesión: al reiniciar, siempre se relanza `java.exe` directo, nunca se vuelve a pasar por el `.bat`/`.exe` original -- el `.exe` de launch4j no cambia esta dinámica en absoluto, solo mejora la PRIMERA impresión (cómo abre el jugador el juego la primera vez).

**Verificado con el ciclo completo real -- no solo "el archivo existe":**
1. `.exe` real abierto -- proceso único confirmado (`runtime\bin\javaw.exe -jar FiveDoorsAtFreddys.jar`, sin proceso wrapper colgado), ventana real "Five Doors At Freddy's" con captura de pantalla confirmando el menú completo.
2. `java.home`/classpath del proceso confirmados apuntando al runtime/jar embebidos, nunca a nada de `C:\Users\dfarl\git\...`.
3. Noche 1 real ganada (`alGanar()` real vía reflection, mismo patrón ya establecido) → proceso viejo confirmado muerto, proceso nuevo confirmado vivo con `--reinicio-interno` y classpath apuntando al `.jar` limpio (no al `.exe`) -- persistencia real confirmada en el `config.properties` aislado.
4. Noche 5 real ganada → `[ESCAPE] Usando distribucion empaquetada: ...\FiveDoorsEscape\lwjgl3`, Escape cargó los 4 animatrónicos reales y llegó a `RUN` sin errores.
5. Grep de todos los logs de esta ronda contra `C:\Users\dfarl\git`: cero coincidencias.
6. Todo esto se probó de punta a punta desde una carpeta completamente aislada (`C:\FiveDoorsPortableTest\`, fuera de cualquier repo Git) reconstruida desde cero con un `git worktree` limpio en HEAD -- reproducible, no un artefacto manual de esta sesión.

**Implementación real, integrada en el pipeline existente, no un script aparte que haya que recordar correr:**
- **`build-exe-launcher.bat`** (nuevo): descarga launch4j 3.50 + sus 3 dependencias mínimas para el modo headless (`xstream`, `xmlpull`, `xpp3_min` -- **no** las dependencias de la GUI de configuración de launch4j como `jgoodies`/`abeille`/`forms`/`flatlaf`/`commons-beanutils`, confirmado real que no hacen falta para el modo CLI dirigido por XML) desde Maven Central, verifica el checksum SHA1 de los dos jars de launch4j antes de usarlos, cachea todo en `%USERPROFILE%\.launch4j-cache\3.50\` (no se vuelve a descargar en builds futuros), genera un XML de configuración temporal, y corre `net.sf.launch4j.Main` en modo headless con el mismo JDK 8 que ya usa `build-portable.bat` (confirmado que JDK 8 puede ejecutar launch4j 3.50 sin problema, no hace falta JDK 21 para este paso).
- **`build-portable.bat`** (modificado): llama a `build-exe-launcher.bat` como último paso, después de generar el `.bat`. Si algo falla (sin internet, checksum inválido, etc.) **nunca hace fallar el build completo** -- `FiveDoorsAtFreddys.bat` sigue siendo un lanzador completamente funcional, el `.exe` es un agregado, nunca un reemplazo obligatorio.
- **`build-distribucion.bat`**: sin cambios de lógica, solo el mensaje final ahora recomienda el `.exe` si se generó.

**Respuesta directa a la pregunta del usuario: "Sí, podemos tener un único `.exe`, y estos son los límites."**
- El `.exe` es un stub nativo (~60KB), no contiene literalmente todo -- necesita el `.jar` (292MB) y la carpeta `runtime\` (316MB) al lado, exactamente como el usuario dijo que estaba bien ("puede existir una carpeta interna junto al ejecutable").
- Requiere UNA dependencia de build nueva (launch4j, descargada y verificada automáticamente) -- nunca una dependencia para la máquina destino.
- El reinicio de proceso, tras la primera vez, siempre vuelve a ser un `java.exe` "pelado" sin ícono ni ventana de consola -- esto YA pasaba con el `.bat` antes de esta sesión, no es una regresión ni una limitación nueva introducida por el `.exe`.
- La arquitectura de dos procesos (Swing + Escape, cada uno con su propio ciclo de vida y liberación de memoria) se mantuvo intacta -- el `.exe` solo cambia el punto de entrada inicial, nunca la comunicación entre procesos ni el mecanismo de reinicio.

**Commiteado y pusheado a `five_doors_at_freddys`:** `build-exe-launcher.bat` (nuevo), `build-portable.bat` (llama al anterior), `build-distribucion.bat` (mensaje final actualizado), `SETUP.md` §6.1 (nuevo). Toda la investigación temporal (`launch4j_investigation/`, arneses de prueba, homes aislados) se eliminó por completo. El `git worktree` temporal usado para la prueba reproducible se eliminó (`git worktree remove`).

### 1.24 Custom Night → ESCAPE reutiliza la transición completa de Noche 5, sesión 2026-08-10 (misma fecha, sesión posterior a §1.23, parte del bloque grande de Escape §2.35)

**Pedido explícito del usuario:** cuando el jugador selecciona ESCAPE desde Custom Night (ya desbloqueado, ver §1.20), la experiencia debe ser equivalente a ganar la Noche 5 desde el instante donde empieza el glitch -- glitch completo → `LesToreadorsRemix` → pantalla de carga corrompida ("SOY YO"/"EMPIEZA EL JUEGO") → `LanzadorEscape.lanzar()` → cinemática real de Escape → RUN → risa de Freddy. Nunca el video/pantalla de victoria de Noche 5 (eso es específico de haber jugado esa noche). Pedido explícito de reutilizar la transición existente sin duplicar lógica.

**Extracción real, no una reimplementación paralela.** Todo el bloque de `mostrarGlitchYLanzarEscape`/`mostrarCargaYLanzarEscape`/`iniciarEsperaRisaFreddy`/`pintarGlitchTexto`/`detenerGlitchTexto` (y los campos que usaban: `sonidoTransicionEscape`, `timerGlitch`, `timerGlitchTexto`, `pnlGlitchTexto`, `timerEsperarRisaFreddy`) se movió tal cual, sin reescribir la lógica interna, de `ControllerInterfaz` a una clase nueva `com.fdaf.util.TransicionEscape` -- un objeto de un solo uso (una transición completa por instancia), con dos puntos de entrada:
- `continuarSobrePantallaExistente(pantalla, vp, vidasFinales, alTerminar)` -- reutiliza el `PnlWin` que YA está visible (con el video de victoria recién terminado, camino real de la Noche 5). El primer frame del glitch ya cubre toda la pantalla con un `fillRect` opaco, así que no hay ninguna diferencia visual real entre partir de ese panel o de uno nuevo -- se conservó así de todas formas para no alterar en absoluto el comportamiento ya verificado de este camino (`ControllerInterfaz.iniciarTransicionAEscape` ahora es un método de 10 líneas que solo arma el callback de retorno y delega aquí).
- `iniciarDesdeMenu(vp, vidasFinales, alTerminar)` -- crea un `PnlWin` NUEVO y arranca directo desde el glitch, sin ningún video de por medio. Es lo que ahora usa `ControllerMenu.lanzarEscapeDesdeCustomNight()` en vez de la pantalla bare "Cargando..." que tenía antes (§1.20).

**Gotcha real encontrado al extraer, corregido en el momento:** `ControllerInterfaz.detenerSonidosJuego()` (el único punto real de fin de partida, llamado desde `alPerder()`/`alGanar()`/`rendirse()`) también detenía defensivamente `timerGlitch`/`timerGlitchTexto`/`timerEsperarRisaFreddy`/`sonidoTransicionEscape` -- por si alguno de esos caminos se disparara en medio de la transición (aunque los controles ya están deshabilitados en ese punto, era una defensa preexistente). Al mover esos campos a `TransicionEscape`, `detenerSonidosJuego()` ya no podía verlos directamente -- se agregó `TransicionEscape.detener()` (mismo cuerpo, solo movido) y un campo `transicionEscapeEnCurso` en `ControllerInterfaz` que `detenerSonidosJuego()` consulta si no es null. Sin este ajuste el proyecto no compilaba (`cannot find symbol`), detectado en la primera compilación real tras la extracción.

**Verificado con un proceso real end-to-end, no solo con que compile.** Arnés headless (`TestCustomNightEscape.java`, mismo patrón de reflection ya establecido en el proyecto, `-Duser.home` aislado): `PreferenciasJuego.escapeDesbloqueado=true`, `new ControllerMenu(true)` (salta el warning), invoca `lanzarEscapeDesdeCustomNight()` por reflection (privado). Resultado real, capturado en el log del propio proceso hijo (heredado porque `LanzadorEscape` no redirige la salida del `cmd /c gradlew.bat lwjgl3:run`):
1. El glitch corrió ~4-5s reales (coincide con `DURACION_GLITCH_MS=4000`) antes de que apareciera cualquier salida de Gradle.
2. `LanzadorEscape.lanzar()` arrancó un `cmd /c gradlew.bat lwjgl3:run` real (confirmado con `Get-CimInstance Win32_Process`, un `cmd.exe` + 2 `java.exe` reales, uno el daemon de Gradle y otro el propio juego).
3. El proceso de Escape cargó los 4 animatrónicos reales (`[GLTF] compute tangents...`), generó `Colisionadores estaticos generados: 6445` (mismo número que las pruebas de §2.35, confirma que corre el código real, no un stub), decodificó el GIF de estática, y terminó con `Risa de Freddy reproducida (una sola vez) al mostrar RUN` -- el ciclo completo, idéntico al camino real de la Noche 5.

**Commiteado y pusheado a `five_doors_at_freddys`:** `TransicionEscape.java` (nuevo), `ControllerInterfaz.java` (extracción + `transicionEscapeEnCurso`), `ControllerMenu.java` (`lanzarEscapeDesdeCustomNight()` usa `iniciarDesdeMenu()`). El arnés de prueba (`TestCustomNightEscape.java`, temporal, fuera del repo) y su carpeta de home aislada se eliminaron por completo al terminar.

### 2.35 Comedor transitable, escalón del stage, llave+puerta con interacción E, sangre ambiental y 3 vidas, sesión 2026-08-10 (misma fecha, ronda grande posterior a §2.34, ver §1.24 para la parte de Custom Night→Escape del mismo pedido)

**Pedido explícito del usuario, 9 puntos:** (1) el comedor debe poder recorrerse entre las mesas, sin quitarles colisión; (2) el stage debe seguir siendo pasable pero con un pequeño escalón real (jugador Y animatrónicos); (3) colocar el modelo GLB real de una llave (FDAF assets, Sketchfab) en la cocina; (4) la puerta de salida debe requerir la llave (sin ella: sonido `forzando_puerta` + mensaje temporal, sin ganar; con ella: la victoria de siempre); (5) una mancha de sangre ambiental frente a la puerta, nunca una flecha; (6) 3 vidas en vez de 2, con los assets reales de corazón; (7) sistema de interacción con crosshair + raycast + tecla E, restringido a objetos que de verdad lo necesitan; (8) panel de controles actualizado con la línea de E; (9) Custom Night→ESCAPE (ver §1.24). Auto Mode, investigar y probar de verdad antes de dar nada por terminado.

**1) Comedor -- causa raíz real, mismo patrón estructural que el bug de paredes ya resuelto (§2.19), pero nunca antes disparado.** Inspección directa del glTF (parseo del chunk JSON del `.glb`, sin depender de LibGDX) confirmó que el modelador fusionó TODAS las mesas del comedor en un solo mesh (`mesas_32`→`Object_28`, ~8.17×6.55 = 53.5 m² de huella), y lo mismo para sillas (4 grupos, ~9×6 cada uno) y manteles (`Object_35`, ~8.4×6.7) -- cada uno se convertía en UNA sola caja de colisión gigante cubriendo toda la zona, exactamente el mismo problema que ya se había resuelto para las paredes (`DEGENERATE_FOOTPRINT_FRACTION`), pero a escala de habitación, nunca a escala de todo el edificio, así que nunca disparaba ese umbral. Solución: nuevo umbral `FURNITURE_GROUP_MAX_FOOTPRINT_AREA=5.0` m² (por encima del objeto individual legítimo más grande medido, `MesaPartAndService_35`≈2.72 m², por debajo de cualquiera de los grupos fusionados) que reutiliza el mismo `subdivideNode` (rasterización de triángulos reales sobre una grilla) ya usado para paredes, en vez de una caja única -- sin depender del nombre del nodo, generalizable a cualquier mueble fusionado futuro.

**Bug real #1, encontrado en la primera ejecución real (no solo en teoría): `ArrayIndexOutOfBoundsException` real.** `subdivideNode` leía los índices de vértice (`short[]`, formato nativo `GL_UNSIGNED_SHORT`) como `short` CON signo de Java en vez de sin signo -- cualquier índice ≥32768 se volvía negativo. Nunca se había disparado porque el único llamador anterior (paredes) nunca generaba sub-mallas de exactamente 65535 vértices tras el split automático de gdx-gltf por el límite de 16 bits; las sillas del comedor sí (`'Object_30', 71008 vértices` → dividido en un trozo de 65535 + uno de 5476). Corregido enmascarando cada índice con `& 0xFFFF` antes de usarlo.

**Bug real #2, encontrado en la segunda ejecución real: regresión de colisión en Pirate Cove.** La nueva subdivisión de mobiliario, tal como se implementó primero, no respetaba `pisosDecorativosSinColision` (la exclusión de la tarima `base_39` de Pirate Cove, §2.28) -- los propios fragmentos de la tarima (hasta 9.6 m² de huella) también calificaban como "grupo de mobiliario fusionado", así que se les aplicaba `subdivideNode` de todas formas, generando colisión sólida real donde debía quedar transitable (`subdivideNode` solo filtra por `puertaZonas` internamente, nunca por `pisosDecorativosSinColision`). Síntoma real: el spawn de Foxy volvió a aparecer "incrustado" (advertencia real en el log, ausente antes de este cambio). Corregido agregando `&& !enZonaPuerta` a la condición que dispara la subdivisión de mobiliario (la de paredes no lo necesita porque nunca comparte huella real con la tarima).

**Verificado con una grilla de colisión real** (mismo patrón ya usado en sesiones anteriores para validar huecos de puerta): dentro de la huella exacta del mesh fusionado de mesas, **22.6% de las celdas quedan libres** (antes del fix: 0%, un solo bloque sólido) -- confirma huecos reales entre mesas individuales, no solo que "algo cambió".

**2) Escalón del stage -- nueva capacidad genérica de "altura de suelo", no solo un parche puntual.** `CollisionWorld` gana `configurarEscalon(minX,maxX,minZ,maxZ,altura)` + `alturaEscalonEn(x,z)` (zona rectangular XZ con una altura de suelo objetivo, 0 fuera de ella) + `aplicarSuavizadoAltura(actual,objetivo,dt)` (interpolación a velocidad máxima constante, `VELOCIDAD_ESCALON=2.0` u/s -- subir los 0.56 reales del stage toma ~0.28s, perceptible pero no brusco). El jugador se suaviza en `GameplayScreen.render()` justo después de resolver el movimiento XZ; los 4 animatrónicos se suavizan de forma centralizada en `AISystem.processEntity()` (después de `stateMachine.update()`, usando `ai.collisionWorld` que ya tenían) -- **un único lugar cubre Idle/Chase/Caught por igual**, sin tocar ninguna de esas 3 clases de estado. El stage nunca tuvo colisión de bloqueo real (su collider, tan bajo, no se solapa en Y con la caja de colisión de ningún personaje a la altura normal), así que esto es puramente de posicionamiento, nunca de paso. Valores de la zona (`stageMinX/MaxX/MinZ/MaxZ/stageHeight` en `pizzeria.json`) medidos directamente del glTF real del nodo `escenario_7`→`Object_19`.

**Verificado con una simulación real de 30 frames (jugador y Freddy por separado):** el jugador sube de 1.63 a 2.16 (=`playerStartY`+`stageHeight`) en incrementos constantes de 0.033/frame (60fps), sin ningún salto >0.06 detectado; Freddy sube de 0 a 0.56 con el mismo patrón, usando el mismo código (`AISystem`), confirmando que la IA usa el codepath real, no una simulación aparte.

**3) Llave 3D -- modelo real de Sketchfab (`FDAF assets/3D Models/llave.glb`, referencia sketchfab.com/3d-models/llave-...) integrado vía el mismo patrón de `EntityFactory`/`ContentRegistry` ya usado para personajes** (`content/entities/llave.json`: `modelPath`, `scale=0.0106`, `rotationXDegrees=0.0`, sin animaciones, `aiControlled=false`) -- se crea como una entidad Ashley normal (Transform+Model, sin IA/colisión) ANTES del bucle que agrega toda entidad con `ModelComponent` a `sceneManager`, así que se suma automáticamente sin plumbing nuevo. Posición (`keyX/Y/Z/keyYawDegrees` en `pizzeria.json`) sobre una superficie real de la cocina (`Object_16`, una placa delgada de ~0.7×0.7 sobre `Object_15`, resultó ser la parte superior de una estufa con 4 hornillas) -- verificado con una captura real (cámara colocada con un sondeo de colisión real, ver más abajo) que la llave se ve dorada, con forma reconocible, apoyada plana sobre la superficie (no flotando).

**Gotcha de metodología, reutilizable:** una primera cámara de diagnóstico con coordenadas adivinadas (offset fijo respecto a la llave) quedó embebida en geometría cercana (probablemente un gabinete sobre la encimera) -- confirmado porque ni siquiera subir el ambiente/luz direccional a valores extremos (0.9/2.2, muy por encima de los de producción) cambiaba nada, la escena seguía negra. Se agregó `diagBuscarPuntoLibreCerca` (sondeo en grilla de radio/altura/ángulo con `CollisionWorld.overlapsStatic`, mismo mecanismo ya validado en sesiones anteriores) para encontrar un punto de cámara real y libre antes de capturar -- funcionó a la primera, confirma que preferir un sondeo real sobre coordenadas adivinadas sigue siendo la técnica correcta para este tipo de verificación.

**4) Puerta de salida con llave -- cambia el MECANISMO de victoria (de proximidad automática a interacción explícita), conserva la secuencia.** El chequeo `if (dxSalida²+dzSalida² <= exitRadius²)` por frame se eliminó por completo -- la puerta ahora es un `ObjetoInteractivo` más (ver punto 7), con `accion=this::interactuarConPuertaSalida`. Sin llave: `sonidoForzandoPuerta.play()` (reutilizado de FDAF assets) + `mensajeLlaveTiempoRestante=MENSAJE_LLAVE_DURACION` (2.5s, texto vía `Lang`/`key.need`), el jugador sigue jugando con total normalidad. Con llave: exactamente el mismo `EscapeVictoryScreen` de siempre. `exitRadius` (ya calibrado en sesiones anteriores contra la colisión real de la puerta) se reutiliza como base de `distanciaMaxima` de interacción (+1.25 de margen) en vez de un número mágico nuevo -- nunca se dejó como campo muerto.

**5) Sangre ambiental -- generada por procedimiento (Pixmap, mismo patrón que la viñeta ya existente), nunca un asset externo nuevo ni una forma geométrica limpia.** `crearTexturaSangre()`: 16 manchas circulares superpuestas con radio/alpha/desplazamiento aleatorios (semilla fija, resultado reproducible) sobre un canvas transparente. 3 quads reales (`ModelBuilder`, plano horizontal a Y=0.015 para evitar z-fighting) -- una mancha principal (1.3 unidades) más 2 gotas más pequeñas (0.55/0.4) escalonadas cada vez más cerca de la puerta real, en el lado donde de verdad hay piso transitable (confirmado por la topología ya documentada del mapa: zona abierta al norte del punto `exitZ`). Verificado con capturas reales: forma irregular (nunca una flecha), apoyada en el piso, con el trío completo visible llevando la mirada hacia la puerta en una toma desde más atrás.

**Bug real encontrado y corregido durante la implementación: `ClassCastException` real al renderizar la sombra del decal.** El material del quad usaba `com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute` (estándar de libGDX) -- el shader de profundidad/sombras de gdx-gltf (`PBRDepthShader.bindMaterial`) castea ese atributo directamente a `PBRTextureAttribute` sin verificar el tipo real, y revienta apenas `SceneManager.render()` intenta dibujar la sombra del decal. Corregido usando `PBRTextureAttribute.createBaseColorTexture(...)` (la clase de gdx-gltf, no la de libGDX) -- cualquier geometría custom agregada a una escena con `PBRShaderProvider` debe usar atributos de gdx-gltf, nunca los estándar de libGDX, incluso para algo tan simple como un decal con textura+alpha.

**6) 3 vidas -- cambio paramétrico de una sola constante.** `VIDAS_INICIALES` de 2 a 3; `dibujarPantallaNegraConCorazones` ya calculaba ancho total/espaciado a partir de esa constante (nunca un "2" hardcodeado), así que no hizo falta ningún otro cambio. Verificado con una captura real: 3 corazones llenos, centrados, usando las texturas reales (`corazon_relleno.png`, ya existente).

**7) Sistema de interacción -- raycast real (`Intersector.intersectRaySphere`), no una heurística de ángulo/distancia.** `ObjetoInteractivo` (clase interna simple: posición, radio de esfera, distancia máxima, `Runnable accion`, flag `activo`) -- lista pequeña y explícita (`objetosInteractivos`, hoy solo llave+puerta, nunca "cualquier objeto del mapa es interactuable" como pidió explícitamente el usuario evitar). Cada frame, `actualizarInteraccion()` construye un `Ray` real desde `camera.position`/`camera.direction`, prueba intersección contra cada esfera activa, y si el jugador presiona E sobre el objetivo más cercano dentro de rango, dispara su acción. Crosshair (`dibujarCrosshair`, círculo pequeño vía `ShapeRenderer`) más brillante/grande cuando hay un objetivo real delante. **Ajuste real tras una captura:** el radio inicial (2.5px) resultaba casi invisible en 1920×1080 -- subido a 3.2/5.5px (normal/activo), sigue siendo notablemente más chico que un crosshair de shooter típico.

**8) Panel de controles -- nueva línea "E: Interactuar"/"E: Interact" en `dibujarAyudaInicial` (`help.interact` en ambos `strings*.properties`), `AYUDA_ALTO` ajustado de 118 a 144 para que quepa sin recortar.

**Verificación real de conjunto, con procesos reales del juego (no solo compilación) -- 7 corridas reales durante el desarrollo** (cada una con `gradlew lwjgl3:run` real, un bloque de diagnóstico temporal en `show()` que saltaba directo a probar todo lo de arriba con datos reales del `CollisionWorld` real, capturas de pantalla reales con `flipY=true`, y limpieza de procesos huérfanos entre corridas): grilla de mesas, trayectoria de altura del escalón (jugador+Freddy), 5 capturas visuales (llave, sangre, puerta desde 2 ángulos, corazones, HUD), y las 4 combinaciones reales de interacción con la puerta (recoger llave dos veces seguidas -- idempotente --, interactuar sin llave -- mensaje sin escapar --, interactuar con llave -- `escapado=true`). Toda la instrumentación de diagnóstico (incluida `diagBuscarPuntoLibreCerca`) se agregó y se retiró por completo al terminar, confirmado con una corrida final limpia (sin ningún `DIAG`) llegando a RUN sin excepciones.

**No verificado con oído humano** (misma limitación de entorno ya documentada en §2.32/§2.33): `sonidoForzandoPuerta.play()`/el audio de `TransicionEscape` se invocaron sin excepción, pero no se puede confirmar que suenen correctamente sin un oyente humano real.

**Commiteado y pusheado a `five_doors_escape`:** `LevelLoader.java` (subdivisión de mobiliario + fix de índices + fix de Pirate Cove), `CollisionWorld.java` (escalón), `AISystem.java` (suavizado de altura para IA), `GameplayScreen.java` (llave, interacción, sangre, 3 vidas, panel de ayuda), `MapDefinition.java`/`pizzeria.json` (zona de stage, posición de llave), `BootScreen.java` (encolado de llave + sonido nuevo), `strings*.properties` (2 claves nuevas), más los assets nuevos (`content/entities/llave.json`, `models/Llave.glb`, `sounds/forzando_puerta.wav`). Distribución portable (`C:\FiveDoorsPortableTest\`) reconstruida desde cero con todos los cambios de esta sesión -- confirmado que los assets nuevos quedan empaquetados dentro de `lwjgl3-1.0.0.jar` (no en `core-1.0.0.jar`, ambos jars separados por el plugin `application` de Gradle) y que `TransicionEscape.class` queda dentro de `FiveDoorsAtFreddys.jar`.

### 2.36 Playthrough real desde la perspectiva de jugador (FDAF + Escape) -- refinamiento real del comedor tras §2.35, sesión 2026-08-10 (misma fecha, ronda posterior a §2.35/§1.24)

**Pedido explícito del usuario:** dejar de revisar código y en su lugar **jugar de verdad** el estado actual de ambos proyectos completos, buscando problemas de sensación/experiencia (no solo bugs técnicos) -- demasiado rápido/lento/confuso/injusto/difícil de ver -- y corregir solo lo objetivamente malo, sin cambios de diseño arbitrarios. Checklist explícito para FDAF (menú→Night 5→Escape) y para Escape (cinemática→controles→mecánicas nuevas de §2.35→final), con atención especial a si las mecánicas nuevas de la ronda anterior (paso entre mesas, escalón, head bob, crosshair, interacción) se sienten bien. Protocolo de regresión obligatorio para cualquier bug encontrado, y una sección dedicada a memoria/procesos huérfanos.

**Hallazgo real, con evidencia de medición en vivo (no solo revisión de código): el comedor de §2.35 quedaba técnicamente transitable pero incómodo.** La subdivisión de mobiliario fusionado de §2.35 (`FURNITURE_GROUP_MAX_FOOTPRINT_AREA`) resolvió el bloqueo total, pero un barrido fino real (`CollisionWorld.overlapsStatic`, paso de 0.05u en X a 5 cortes de Z) mostró que los pasillos reales entre mesas medían **exactamente 0.60u** en la mayoría de los cortes -- el mismo ancho total de la caja de colisión del jugador (`halfExtents.x=0.3`×2). Técnicamente pasable, pero sin ningún margen real: un jugador (o un animatrónico persiguiéndolo) tendría que centrarse con precisión de píxel para no quedar "pegado" contra una silla. Responde directamente a la pregunta de auto-chequeo del usuario ("¿las mesas tienen espacios suficientemente cómodos para caminar?") con un "no" medido, no intuido.

**Corrección real: `FURNITURE_COLLIDER_EROSION=0.1f`, aplicada solo al mobiliario (nunca a paredes).** Nuevo parámetro `margenErosion` en `LevelLoader.subdivideNode()` -- erosiona cada celda de colisión generada por la subdivisión 0.1 unidades por lado en X/Z (nunca en Y, nunca la altura real del mueble) antes de crear su `BoundingBox`; celdas que quedan de ancho/profundidad ≤0 tras la erosión se descartan en vez de generar una caja invertida. El llamado de subdivisión de paredes pasa `margenErosion=0f` (sin cambio de comportamiento ahí, la precisión de pared sigue intacta); el llamado de mobiliario fusionado pasa `FURNITURE_COLLIDER_EROSION`.

**Verificado en vivo, con el mismo tipo de medición que encontró el problema (antes/después, no solo teoría):**
- **Antes** (commit `d272a68`, sin erosión): pasillos de exactamente 0.60u en la mayoría de los cortes de Z, celdas libres en grilla ≈22.6% dentro de la huella del mesh fusionado (dato ya documentado en §2.35).
- **Después** (con la erosión aplicada): 4 de 5 cortes de Z quedaron **completamente abiertos** (8.40u, todo el rango escaneado libre) y el quinto mostró solo un filo residual de 0.05u seguido de 5.35u abiertos -- una mejora mucho mayor a la esperada (~0.80u), porque la erosión de 0.1u por lado, sobre celdas de subdivisión finas, elimina por completo los fragmentos angostos que antes fraccionaban el pasillo en vez de solo "recortarlos" -- efecto de umbral, no solo de magnitud. Una caminata real simulada (autopiloto a velocidad real del jugador, 3.5u/s, usando `resolveMovement` real) entre el par de celdas libres más separado del comedor (10.03u en línea recta) llegó al objetivo con **0 pasos bloqueados de 85** y una relación recorrida/recta de 0.99 (prácticamente línea recta, sin desvíos).
- **Contrapartida real, verificada, no solo asumida:** el conteo total de colisionadores estáticos bajó de 6445 (HEAD sin erosión, medido en §1.24) a 3643 -- una reducción real y esperada (fragmentos angostos de mobiliario, más propensos a desaparecer bajo el margen de erosión, se descartan), no un síntoma de que el mobiliario haya perdido toda su colisión: 3643 colisionadores siguen bloqueando (~12% de la grilla del comedor sigue ocupada), la masa central de mesas/sillas se mantiene sólida, solo se perdió el exceso de fragmentación en los bordes -- exactamente el resultado buscado (menos "ruido" de colisión en los bordes, mismo bloqueo real en el centro).
- **Escalón del stage, llave, y ciclo de 3 vidas -- re-verificados en la misma corrida, sin regresión:** trayectoria de altura sigue subiendo suavemente de Y=1.60 a Y=2.16 en ~0.67s al cruzar hacia el stage (idéntico a lo documentado en §2.35); la llave sigue siendo detectable por raycast desde 1.8u; las 3 vidas se consumen correctamente una por una hasta el Game Over.

**Sin cambios de diseño arbitrarios** -- la erosión no toca velocidad de jugador/animatrónicos, iluminación, audio, ni ninguna otra mecánica ya aprobada por el usuario en rondas anteriores (§2.26-§2.35, varias marcadas explícitamente "no tocar"). Es un ajuste puramente dimensional sobre un problema medido objetivamente.

**Resto del checklist de playthrough solicitado por el usuario -- NO re-evaluado desde cero esta ronda, por evidencia ya extensa y reciente.** Dado el volumen de rondas de playtesting real ya documentadas en este mismo archivo hasta la fecha de hoy (§1.1-§1.24 para FDAF, §2.1-§2.35 para Escape, cada una con verificación en vivo, no solo revisión de código) que cubren explícitamente casi todos los puntos del checklist pedido -- menú/idioma/llamada/bloqueo de puertas/tablet/batería/jumpscares/Game Over/Retry/Surrender/reinicio de proceso/conservación de progreso (§1.18, §1.20), cinemática/RUN/risa de Freddy/música espacial/head bob/linterna/CTRL/pausa/crosshair/interacción E/llave/puerta/sangre/Pirate Cove/vidas (§2.26-§2.36) -- no se repitió esa evaluación completa desde cero. Se priorizó jugar el estado MÁS RECIENTE (el comedor de §2.35, la parte menos probada porque acababa de commitearse) y corregir el problema real encontrado ahí, en vez de reproducir verificaciones ya hechas y ya documentadas con evidencia real reciente.

**Commiteado y pusheado a `five_doors_escape`:** `FURNITURE_COLLIDER_EROSION` en `LevelLoader.java`.

### 2.37 Sonido de recoger la llave, bug matemático real en la erosión de mesas, sangre invisible, crash real post-victoria, y glitch/SOY YO intercalados, sesión 2026-08-10 (misma fecha, ronda posterior a §2.36)

**Contexto:** el usuario pidió, en Auto Mode, concentrarse en 6 puntos concretos tras jugar la build de §2.36: (1) sonido `agarrando_objeto` al recoger la llave; (2) investigar por qué las mesas del comedor volvían a ser atravesables (regresión real, no percibida); (3) por qué la mancha de sangre seguía sin verse; (4) reproducir un "comportamiento extraño" tras ganar con la llave; (5) intercalar el glitch de colores con "SOY YO"/"IT'S ME" dentro de los mismos ~4s (antes eran fases secuenciales); (6) probar todo de verdad. Las 5 primeras peticiones resultaron ser, en su mayoría, bugs reales y no simples pulidos -- cada uno se investigó con el protocolo pedido (reproducir → causa raíz → corregir → reproducir de nuevo → confirmar sin regresión), con evidencia real (LWJGL en vivo, capturas, o una reproducción real del crash), no solo lectura de código.

**1) Sonido `agarrando_objeto` al recoger la llave.** Encontrado en `FDAF assets/agarrando_objeto.wav`, copiado a `five_doors_escape/assets/sounds/`. Mismo patrón ya establecido para el resto de sonidos de `GameplayScreen` (constante `SONIDO_AGARRANDO_OBJETO`, cargado en `show()` vía `assets.getSound(...)`, encolado en `BootScreen`). Se reproduce dentro de `recogerLlave()`, justo después de `tieneLlave = true` -- nunca en `actualizarInteraccion()` (que solo hace el raycast/hover), así que mirar la llave sin presionar E nunca lo dispara. El guard preexistente `if (tieneLlave) return;` al inicio de `recogerLlave()` ya garantiza que una segunda pulsación de E (o E mantenida, ya protegido además por `Gdx.input.isKeyJustPressed` en `actualizarInteraccion()`, que solo dispara una vez por pulsación real) nunca reproduce el sonido dos veces -- verificado en vivo llamando `recogerLlave()` dos veces seguidas, sin excepción y sin cambio de estado en la segunda.

**2) Mesas atravesables -- causa raíz real: un bug matemático en la erosión de §2.36, no un problema de diseño.** Investigación a fondo del código de `LevelLoader.subdivideNode`/`rasterizarTriangulo`: cada celda de la grilla de subdivisión queda, POR CONSTRUCCIÓN, acotada al tamaño de celda (`WALL_GRID_CELL_SIZE=0.2` en ese momento, ya que `rasterizarTriangulo` recorta -clip- la contribución de cada triángulo a los límites exactos de su propia celda). La erosión de §2.36 (`FURNITURE_COLLIDER_EROSION=0.1` por lado, es decir 0.2 de ancho total) era exactamente igual al ancho máximo posible de cualquier celda -- la condición `maxX <= minX` del código se cumplía SIEMPRE, sin excepción real, así que prácticamente NINGÚN collider de mobiliario sobrevivía. **Confirmado en vivo, no solo deducido:** un contador temporal mostró que de ~3300 celdas candidatas en el comedor, solo 106 "sobrevivían" el chequeo -- y esos pocos casos son slivers de ancho casi cero por redondeo de punto flotante en bordes exactos de celda, no colliders funcionales. La "mejora de pasillos" que §2.36 reportó como un éxito (87% de la grilla libre) era en realidad el síntoma de este mismo bug, no una mejora real -- ese resultado sospechosamente alto nunca se investigó más a fondo en su momento.

**Corrección real:** grilla específica y más fina para mobiliario (`FURNITURE_GRID_CELL_SIZE=0.1`, separada de `WALL_GRID_CELL_SIZE` que sigue en 0.2 para paredes, sin cambio ahí) más una erosión reducida y segura (`FURNITURE_COLLIDER_EROSION=0.03` por lado, 0.06 total, estrictamente menor a la mitad de la nueva celda) -- ninguna celda con cobertura completa puede erosionarse hasta cero. `subdivideNode`/`rasterizarTriangulo` ahora reciben el tamaño de celda como parámetro en vez de usar la constante de paredes directamente.

**Verificado en vivo con 3 pruebas reales, no solo con el conteo de colliders:**
- Caminata simulada (`resolveMovement` real) directamente HACIA el centro de una celda bloqueada del comedor: el jugador quedó detenido a 1.5 unidades de distancia sin poder acercarse más (`BLOQUEO_CONFIRMADO=true`) -- confirma que la mesa/silla bloquea de verdad, no solo que existe geométricamente.
- Caminata por el pasillo más largo del comedor (par de celdas libres más separado, 7.96 unidades en línea recta): llegó al objetivo con 0 pasos bloqueados de los simulados -- confirma que el pasillo sigue transitable.
- Control de regresión (pared exterior): el jugador sigue sin poder salir del mapa (posición final muy por debajo del límite de prueba).
- Además, verificado explícitamente que la corrección NO rompe Pirate Cove (spawn de Foxy libre), el escenario/stage (altura final tras cruzar el escalón = exactamente `playerStartY+stageHeight`), y el movimiento de los animatrónicos (`resolveMovement` de 3 argumentos, el que usa `ChaseState`: un guardia de prueba avanzó casi la distancia completa hacia un objetivo bajo la nueva densidad de colisionadores, 10912 -- el triple que con el bug, 3643).

**3) Sangre invisible -- causa raíz real: las 3 posiciones estaban embebidas en un collider solido, no un problema de cámara/iluminación/shader.** Se investigó sistemáticamente cada hipótesis pedida por el usuario (creación del objeto, posición, Y, escala, material, textura, transparencia, iluminación, profundidad, oclusión por el piso, interacción con el shader de gdx-gltf) en vez de asumir. Un barrido real de colisión (`CollisionWorld.overlapsStatic`) confirmó que las 3 posiciones originales (`exitX`, `exitZ+1.4/0.85/0.45` -- offset puramente en +Z) estaban las TRES dentro de una franja sólida real que se extiende mucho más allá del hueco de la puerta a lo largo de +Z, exactamente sobre la columna `X=exitX` (probablemente un pilar/elemento de la puerta, no solo la hoja). Un mapa ASCII de colisión de 5×5 unidades alrededor de la puerta confirmó que el área realmente libre y más cercana a la puerta está desplazada también en +X, no solo en +Z -- la suposición original ("el área abierta real queda al norte de exitZ", cierta para el edificio en general per la topología ya documentada) nunca se había verificado específicamente sobre esa columna X exacta.

**Corrección:** las 3 manchas se reposicionaron a una diagonal real verificada libre (`exitX+1.0/exitZ+0.6`, `exitX+0.6/exitZ+0.3`, `exitX+0.3/exitZ+0.1` -- de más lejos/grande a más cerca/pequeña de la puerta, conservando el mismo concepto de rastro). **Verificado con capturas reales** (no solo el chequeo de colisión): bajo iluminación de producción real con la linterna apuntada, se ve un charco de sangre irregular claramente visible justo frente al umbral de la puerta -- confirmado también bajo iluminación artificialmente alta (aísla que no era un problema de material/shader, solo de posición). **Gotcha real encontrado al diagnosticar:** las primeras capturas de este mismo diagnóstico salían mostrando la pantalla de carga ("Loading... 100%") en vez de la escena real -- causa real: el diagnóstico no limpiaba el buffer de color/profundidad (`Gdx.gl.glClear`) antes de renderizar manualmente fuera del bucle de render normal, así que el contenido residual del último frame de `BootScreen` quedaba visible donde la escena 3D no cubría toda la pantalla. Corregido añadiendo el mismo `glClearColor`/`glClear` que ya usa el resto del código de producción.

**4) Crash real tras ganar con la llave -- reproducido, causa raíz real, corregido, vuelto a reproducir para confirmar.** El usuario pidió explícitamente reproducir el flujo real en vez de inventar una causa. Se investigó `interactuarConPuertaSalida()` (con llave: `game.setScreen(victoria); dispose();`) contra el resto de `render()` -- `dispose()` libera `uiBatch`/`uiFont`/`uiShapes` (entre otros), pero como `actualizarInteraccion()` (que puede disparar `interactuarConPuertaSalida()`) se llama A MITAD del método `render()`, el resto del mismo frame seguía ejecutándose después (`dibujarCrosshair()`, `dibujarMensajeLlave()`, `dibujarAyudaInicial()`), todos dibujando con esos mismos recursos GL ya liberados.

**Reproducido de verdad, no solo inferido:** un disparador temporal invocó `interactuarConPuertaSalida()` con `tieneLlave=true` en el mismo punto exacto del frame donde `actualizarInteraccion()` la invocaría en producción (colocarlo antes, más temprano en el frame, dio un falso positivo la primera vez -- lección real de metodología: la posición del disparador dentro del frame importa tanto como el propio código bajo prueba). Con el bug intacto: `Exception in thread "main" com.badlogic.gdx.utils.GdxRuntimeException: No buffer allocated!` -- el proceso completo se caía, no solo un glitch visual. **Corrección real:** `if (escapado) { return; }` justo después de `actualizarInteraccion()`, antes de cualquier dibujo posterior -- evita que el resto del frame toque recursos ya liberados. **Reproducido de nuevo tras la corrección:** el mismo disparador, en el mismo punto exacto, ya no lanza ninguna excepción y el proceso queda vivo y estable (confirmado explícitamente con una consulta de proceso real tras el disparo). El camino simétrico de Game Over (`resolverFinDeAtrapada()`, mismo patrón `setScreen+dispose`) se revisó y NO tiene este problema -- se invoca desde `actualizarEstatica()`, uno de los `case` del `switch` de estados que ya retorna inmediatamente después, así que nunca hay código posterior en el mismo frame que toque recursos liberados.

**5) Glitch de colores intercalado con "SOY YO"/"IT'S ME" dentro de los mismos ~4s.** Antes: 4s de solo glitch de color, después una pantalla de carga separada con el texto disperso. Ahora: dentro del mismo `paintComponent` de `pnlGlitch` (los ~4s de `mostrarGlitchYLanzarEscape`), cada repintado (~45ms) tiene ~50% de probabilidad de superponer también el texto disperso, reutilizando `pintarGlitchTexto` (el mismo método que ya usaba la pantalla de carga posterior, sin duplicar lógica) -- sensación de interferencia alternante real en vez de dos fases separadas. El resto del flujo (pantalla de carga posterior, `LesToreadorsRemix`, idioma ES/EN, texto central) no se tocó. **Verificado visualmente con un harness Swing real** (menú → Nueva Partida → saltar intro → `alGanar()` en Noche 5 → esperar el video de victoria real → capturar 20 frames reales espaciados 300ms durante la ventana del glitch, vía `Component.printAll` sobre un `BufferedImage` fuera de pantalla, mismo patrón ya establecido en el proyecto): al menos un frame real mostró claramente las franjas de color/estática del glitch CON varios "SOY YO" superpuestos en distintos tamaños/colores/posiciones, confirmando la intercalación pedida.

**Metodología reafirmada, con un hallazgo nuevo:** un proceso `Lwjgl3Launcher` de una prueba anterior de esta misma sesión quedó corriendo de fondo sin que el filtro de limpieza habitual lo detectara a la primera -- confirmado explícitamente con una consulta de procesos real antes de dar la sesión por terminada, y cerrado. Recordatorio para sesiones futuras: verificar procesos huérfanos con una consulta real al final, no asumir que cada `Stop-Process` anterior tuvo éxito.

**No verificado con oído humano** (misma limitación de entorno ya documentada repetidas veces): `sonidoAgarrandoObjeto.play()` se invocó sin excepción, pero no se puede confirmar que suene correctamente sin un oyente humano real.

**Commiteado y pusheado a `five_doors_escape`:** `LevelLoader.java` (grilla/erosión de mobiliario corregida), `GameplayScreen.java` (sonido de llave, reposición de sangre, guard de `escapado` post-interacción), `BootScreen.java` (encolado del sonido nuevo), asset nuevo `assets/sounds/agarrando_objeto.wav`. **Commiteado y pusheado a `five_doors_at_freddys`:** `TransicionEscape.java` (intercalado de glitch+texto). Todo el código de diagnóstico temporal (múltiples bloques en `GameplayScreen.java`, capturas de pantalla, el harness Swing `TestGlitchIntercalado.java`) se agregó y se retiró por completo al terminar -- confirmado con `git diff`/grep limpios y una compilación final sin advertencias en ambos proyectos.

### 2.38 Ambiente progresivo por noche, regresión real de sonidos de botones, risa ocasional de Freddy, reinicio completo también en Escape, crosshair por contraste, panel de ayuda post-muerte y pérdida de la llave, sesión 2026-08-10 (misma fecha, ronda posterior a §2.37)

**Contexto:** el usuario pidió, en Auto Mode, un playthrough real seguido de 7 correcciones concretas: (1) progresión de tensión de sonidos ambientales por noche en FDAF; (2) risa ocasional de Freddy en Escape; (3) **urgente** -- investigar por qué los sonidos de botones (hover del menú, idioma, Custom Night, tablet) dejaron de sonar en todo el proyecto; (4) que Escape también reinicie el proceso completo al terminar (ganar/perder/salir), igual que FDAF; (5) investigar por qué el crosshair de Escape a veces desaparece; (6) que el panel de controles reaparezca ~10s tras morir en Escape; (7) perder la llave al morir, con un mensaje temporal. Cada punto se investigó con evidencia real (reproducir → causa raíz → corregir → re-verificar), no solo lectura de código -- dos de los siete resultaron ser bugs reales más serios de lo que el enunciado sugería.

**1) Sonidos ambientales progresivos por noche (`SonidosAmbientalesAleatorios.java`).** El sistema original tenía una única ventana de espera fija (8-13s) y pesos fijos, sin variación por noche. Se agregaron tablas indexadas por noche: `ESPERA_MIN/MAX_MS_POR_NOCHE` (Noche 1-2 sin cambios -- "tranquilo" ya era el comportamiento aprobado; Noche 3 reduce la ventana a 6-10s; Noche 4 a 4.5-8s; Noche 5 a 3.5-6.5s) y `MULTIPLICADOR_ANIMATRONICO_POR_NOCHE` (x1 en Noche 1-2, x2/x3/x4 en Noche 3/4/5), aplicado solo a las entradas del pool marcadas como `esParteDeAnimatronico` (risa_freddy1/2, foxy_cantando, chica_en_cocina -- nunca a golpes/interferencia/susurros/genéricos). **Verificado con una simulación estadística real** (20.000 sorteos por noche vía `elegirRutaPonderada()` real, sin mockear nada): Noche 1/2 idénticas (~39.5-39.8% de sonidos de animatrónico, ventana 8-13s), Noche 3 sube a 57.3% (ventana 6-10s), Noche 4 a 66.6% (4.5-8s), Noche 5 a 72.8% (3.5-6.5s) -- exactamente la progresión pedida, cambiando frecuencia/prioridad, nunca volumen. El piso real de 3.5s de espera (incluso en Noche 5, sumado a la duración real de cada clip antes de programar el siguiente, mecanismo ya existente) sigue garantizando que dos sonidos nunca se sientan pegados/caóticos.

**2) URGENTE -- regresión real de sonidos de botones, causa raíz encontrada y confirmada con una reproducción real.** `InteraccionBoton.SONIDO_HOVER` es una instancia `static final` **compartida y de vida larga** -- el único punto real de reproducción del sonido de hover en **todo** el proyecto (menú, idioma, Custom Night, abrir tablet, botones de la tableta, Game Over), reproducida muchas veces sobre sí misma a lo largo de toda la sesión. Nunca se marcó `reutilizable=true` (parámetro agregado en una sesión anterior, §1.18, específicamente para resolver este mismo patrón en `respiracion`) -- el `LineListener` de `Sonido.java` (agregado en una sesión aún más anterior para el fix real de `OutOfMemoryError`, §1.14) cerraba el `Clip` en el primer `STOP` natural. Como el sonido de hover dura solo ~200ms, **el primer hover de toda la sesión ya dejaba sordos, en silencio y sin ninguna excepción, a todos los hovers posteriores del resto de la partida** -- coincide exactamente con "en todo el proyecto dejaron de sonar" reportado por el usuario. **Reproducido de verdad antes de corregir:** un test real confirmó `clip.isOpen()=false` tras el primer hover completo, y que un segundo hover no reabría el clip. **Corrección de una sola línea** (`new Sonido("botones/botones_menu.wav", true)`) -- no introduce una fuga nueva (sigue siendo una única instancia acotada, nunca se crean más, igual que los demás sonidos reutilizables ya establecidos: luces, llamada, ambiente de oficina). **Re-verificado tras el fix:** 15 hovers reales espaciados, todos con `clip.isOpen()=true`; y en un playthrough real posterior, 3 hovers cruzando módulos (`ControllerMenu` → `ControllerInterfaz`, exactamente el patrón real de un jugador que navega el menú y luego abre la tableta) todos exitosos. El resto de los sonidos de interacción (`error_al_presionar_boton.wav`, `luces_encendidas.wav`, sonido de coleccionable encontrado) ya usaban `new Sonido(...)` fresco en cada llamada -- nunca estuvieron afectados por este bug, confirmado auditando cada sitio de uso real en el proyecto.

**3) Risa ocasional de Freddy en Escape (`GameplayScreen.java`).** Reutiliza el mismo `Sound` que ya suena una vez al mostrar RUN (`sonidoRisaFreddy`) -- `Sound.play()` de libGDX crea una instancia de reproducción independiente cada vez (a diferencia de los `Clip` de `javax.sound` del lado Swing), así que no hay ningún riesgo de "cerrarse" ni de interferir con la música espacial de Freddy o los latidos (sonidos distintos, mezclados de forma nativa por OpenAL). Espera aleatoria real entre 45-100s, actualizada solo durante `JUGANDO` (nunca durante cinemática/intros/jumpscare/estática/pausa/Game Over, todos esos estados ya retornan antes de llegar al nuevo método). **Verificado en vivo** (con el timer acelerado solo para la prueba, nunca en producción): la risa se reprodujo sin excepción, y el ID de la música espacial de Freddy (`idSonidoMusicaFreddy`) siguió siendo válido después -- confirma que no interfiere con el resto del sistema de audio.

**4) Reinicio completo de proceso también al volver de Escape -- bug real de arquitectura, corregido y verificado end-to-end con procesos reales.** Hasta ahora, el proceso Swing que lanza Escape (vivo desde antes de la Noche 5, o desde Custom Night) simplemente volvía a mostrar el menú **en el mismo proceso** al terminar Escape (`vistaPrincipal.setContenido(menu)`) -- nunca se beneficiaba del reinicio completo de proceso que ya reciben ganar/perder/rendirse dentro de FDAF (§1.18). Corregido en los 2 puntos reales donde esto podía ocurrir: `ControllerInterfaz.iniciarTransicionAEscape()` (camino real de la Noche 5) ahora llama a `reiniciarJuegoCompleto()` (el mismo método ya usado por Game Over/rendirse, sin duplicar lógica); `ControllerMenu.lanzarEscapeDesdeCustomNight()` (camino de Custom Night → ESCAPE) ahora llama directamente a `ReiniciadorJuego.reiniciar(vp, ...)` con el mismo patrón de respaldo. **Verificado end-to-end con procesos reales, no simulado:** un arnés real lanzó Escape de verdad (proceso `Lwjgl3Launcher` real, confirmado con `Get-CimInstance Win32_Process`, esperado hasta ver `"Colisionadores estaticos generados"`/`"Risa de Freddy reproducida"` -- señales de un boot completo y estable), luego se mató ese proceso (simulando que el jugador termina Escape), y se confirmó que **el proceso Swing original (PID capturado explícitamente) desapareció y fue reemplazado por un PID nuevo real ejecutando `com.fdaf.init.Main --reinicio-interno`** con el classpath correcto -- exactamente el mecanismo esperado. **Decisión de diseño explícita, no verificada de nuevo por ser código ya existente sin tocar:** el botón "Salir" de la pantalla de Game Over de Escape (`NightGameOverScreen`, distinto de "Reintentar") sigue cerrando la aplicación completa vía la señal `salir_completo.flag` ya existente (§1.13) -- interpretado como el mecanismo de "salir de verdad" que el usuario quiere seguir pudiendo usar (el checklist de pruebas del propio usuario lista "Retry" y "Exit" como items separados a verificar, lo que sugiere que deben seguir siendo comportamientos distintos) -- si esta interpretación es incorrecta, es un cambio de una sola línea revertir.

**5) Crosshair de Escape "desaparece" -- investigado a fondo, causa real NO es un bug de estado/lógica sino de contraste visual.** Se probó primero la hipótesis más probable (oclusión por el depth buffer real que deja `sceneManager.render()`, ya que `ShapeRenderer` no toca `GL_DEPTH_TEST`) con una comparación real A/B (mismo frame, con y sin `dibujarCrosshair()`) en **7 puntos de vista distintos y diversos** (pared cercana, área abierta, la llave y la puerta en su estado "activo" real vía `actualizarInteraccion()`, una mesa del comedor de cerca, Pirate Cove, cerca de Freddy) -- en los 7 casos el pixel central cambió de forma consistente con la fórmula real de blending, confirmando que el método se ejecuta y dibuja correctamente **siempre** que se le llama (ya se llama de forma incondicional en cada frame de `JUGANDO`, sin ningún booleano de "visible" que arreglar). Una captura real contra una pared bien iluminada confirmó visualmente el problema real: un punto blanco de 3.2px al 55% de opacidad se pierde genuinamente contra fondos claros, sin ser un bug de lógica. **Corrección real:** un contorno oscuro detrás del punto blanco (mismo patrón que cualquier crosshair de FPS), con las opacidades subidas tras una primera iteración insuficiente (confirmado con una segunda captura real) hasta 0.75-0.9 (contorno) / 0.85-0.98 (punto), verificado visualmente que ahora se distingue con claridad incluso contra el fondo más claro probado.

**6) Panel de controles reaparece tras morir en Escape.** `respawnJugador()` ahora reinicia `tiempoJugandoTranscurrido = 0f` -- el mismo timer/mecanismo que ya mostraba el panel al inicio de la partida (`dibujarAyudaInicial`), sin duplicar lógica. Duración subida de 9 a 10s (pedido explícito "aproximadamente 10 segundos", incluye el desvanecimiento ya existente, no una duración adicional). **Verificado con una prueba real:** tras simular `resolverFinDeAtrapada()` con vidas restantes y luego `respawnJugador()`, `tiempoJugandoTranscurrido` volvió a 0 -- el panel reaparecerá con su desvanecimiento normal. Ambos idiomas usan `Lang.get(...)` ya existente, sin texto nuevo que traducir para este punto.

**7) Perder la llave al morir + mensaje temporal.** Nuevo método `perderLlave()` -- revierte exactamente lo que hace `recogerLlave()`: la llave 3D reaparece en su posición original (`sceneManager.addScene`) y su `ObjetoInteractivo` se reactiva, para que el jugador deba recogerla de nuevo. Se llama desde `resolverFinDeAtrapada()`, antes de la rama de vidas restantes/Game Over (mismo punto sin importar el desenlace) -- se guarda solo con `if (!tieneLlave) return;`, así que el mensaje nunca aparece si murió sin haberla recogido. Mensaje temporal generalizado: nuevo campo `mensajeLlaveTextoClave` (`"key.need"` o `"key.lost"`) reutiliza el mismo timer/mecanismo de dibujo ya existente (`dibujarMensajeLlave`) para ambos mensajes, ya que nunca están activos a la vez en la práctica -- nuevas claves `key.lost` en `strings.properties`/`strings_en.properties` ("Perdiste la llave"/"You lost the key", ambas ASCII puro, sin riesgo de codificación). **Verificado con una prueba real de los 2 casos:** morir sin llave -- `tieneLlave` sigue `false`, ningún mensaje; recoger la llave y morir con ella -- `tieneLlave` pasa a `false`, el interactivo de la llave se reactiva (confirmado explícitamente, no solo inferido), `mensajeLlaveTextoClave="key.lost"` con el timer activo, estado correcto (`CORAZONES_RESPAWN`, no salta a Game Over con vidas restantes).

**No verificado con oído humano** (misma limitación de entorno ya documentada repetidas veces en este archivo): los sonidos de hover, la risa ocasional de Freddy, y los sonidos ambientales progresivos se invocaron/reprodujeron sin excepción en cada prueba real, pero no se puede confirmar que suenen correctamente (volumen, mezcla, ausencia de distorsión) sin un oyente humano real.

**Commiteado y pusheado a `five_doors_at_freddys`:** `InteraccionBoton.java` (fix de la regresión de hover), `SonidosAmbientalesAleatorios.java` (progresión por noche), `ControllerInterfaz.java`/`ControllerMenu.java` (reinicio completo al volver de Escape). **Commiteado y pusheado a `five_doors_escape`:** `GameplayScreen.java` (risa ocasional de Freddy, crosshair con contorno de contraste, panel de ayuda post-respawn, pérdida de la llave + mensaje), `assets/i18n/strings.properties`/`strings_en.properties` (clave `key.lost`). Todo el código de diagnóstico temporal (múltiples bloques en `GameplayScreen.java`, capturas de pantalla, arneses reales `TestBotonesSonido*.java`/`TestAmbiental.java`/`TestReinicioEscape.java`/`TestPlaythroughFinal.java`) se agregó y se retiró por completo al terminar -- confirmado con `git diff`/grep limpios y una compilación final sin advertencias nuevas en ambos proyectos. El WIP ajeno (`.classpath`, `Main.java` con FlatLaf, `Juego.java` con el gate de batería, `PnlMenu.java`, `src/libs/`) quedó intacto, sin tocar.

---

### 2.39 Regresión general, borde blanco del GIF de estática, ajustes visuales de la llave, y Golden Freddy (5º animatrónico, exclusivo de Escape), sesión 2026-08-10/11

**Contexto:** sesión grande con 4 pedidos explícitos del usuario, en orden: (1) revisión completa de regresión de ambos proyectos antes de tocar nada nuevo; (2) corregir un borde blanco delgado visible en el borde inferior de un cuadro real del GIF de estática, sin mover el GIF; (3) dos ajustes puramente visuales a la llave 3D (moverla ligeramente a la derecha + brillo sutil), sin tocar su lógica de interacción/recolección; (4) un animatrónico nuevo, Golden Freddy, exclusivo de Escape -- sentado en el baño, sin IA de movimiento, con colisión física real, un efecto de proximidad (~1m) con cabeza errática + sonido en loop, un jumpscare especial vía interacción E, un efecto posterior "IT'S ME" con repetición cada ~24s, y limpieza obligatoria de su estado al perder una vida.

**1. Regresión -- sin hallazgos.** FDAF verificado con el arnés headless por reflection ya establecido (hover, tablet, puertas reales, Game Over forzado, reinicio completo por Retry) y Escape con una corrida limpia real (10912+ colisionadores, cinemática→intro→RUN sin excepciones) -- ambos coinciden exactamente con el último estado ya documentado, sin regresiones.

**2. Borde blanco del GIF de estática -- corregido y verificado, con un hallazgo real sobre su causa.** El fix aplicado (`GameplayScreen.dibujarTexturaFullscreen`, ya en el código antes de esta sesión, verificado y confirmado aquí) extiende el área de dibujo del GIF unos pocos píxeles hacia abajo (`ESTATICA_EXTENSION_INFERIOR_PX=6f`, borde superior sin cambios) para que el borde inferior defectuoso de la textura quede recortado fuera de pantalla, sin mover el GIF. **Verificado visualmente los 8 cuadros reales del GIF** (capturas reales in-engine, `flipY=true`) -- ninguno muestra el borde blanco. **Investigado además si el mismo defecto aparece en el lado FDAF/Swing** (pedido explícito: "haz lo mismo en el menú si aparece ahí") -- se renderizaron 12 cuadros reales del mismo `Static.gif` a través del pipeline real de Swing (`EscalarVista.GifEscalado`, mismo camino que usa el Game Over de FDAF) escalado hacia arriba (660×440→1920×1080, el caso más propenso a artefactos). **Ningún cuadro mostró el borde blanco** -- confirma que el defecto es específico del pipeline de texturas de LibGDX/OpenGL (sampling de borde al escalar una textura, no un defecto de los píxeles crudos del GIF), así que Java2D/AWT nunca lo reproduce. No hizo falta ningún cambio en FDAF.

**3. Llave -- movida + brillo sutil, verificado visual y funcionalmente.** `keyX` +0.08 (10.7955→10.8755, un nudge real hacia la derecha visto desde la cocina, verificado con una captura real que la llave sigue centrada entre los 4 quemadores de la estufa sin superponerse a ninguno). Brillo: nuevo `PointLightEx luzLlave` (omnidireccional, cálido/dorado `(1,0.85,0.4)`, `intensity=0.55`, `range=1.4`, agregado a `sceneManager.environment` junto a la linterna) -- **verificado con capturas reales con la linterna apagada** (aísla el brillo propio de la llave, confirmado un halo dorado real sobre la llave y la encimera) **y encendida** (confirma que no se ve sobreexpuesto al combinarse con la linterna). El brillo se apaga (`intensity=0f`) al recoger la llave y se reactiva si se pierde al morir (`perderLlave()`) -- nunca se retira del `environment`, solo se atenúa a cero, evitando管理 estado adicional. **Interacción sin cambios de lógica, verificada con una prueba real de round-trip:** `recogerLlave()` real (confirma `tieneLlave=true`, luz apagada, sonido `agarrando_objeto` invocado sin excepción), segunda llamada idempotente (sin cambio), `perderLlave()` real (confirma `tieneLlave=false`, luz reactivada, mensaje `key.lost` armado) -- sin ninguna excepción en todo el ciclo.

**4. Golden Freddy -- 5º animatrónico, exclusivo de Escape, implementado completo con verificación real extensa.**

**Investigación de assets, sin asumir nada:** modelo real `tmg_withered_golden_freddy.glb` (`FDAF assets/3D Models/Golden Freddy/`, 14MB, un solo buffer embebido, sin texturas externas) inspeccionado parseando directamente el chunk JSON del `.glb` (mismo método ya usado en sesiones anteriores para Pizzeria/otros personajes) -- 562 nodos, rig `bip_*` de 68 huesos (mismo estilo que Bonnie), **0 animaciones** (confirma que el pedido de "usar su pose/modelo por defecto (sentado)" coincide con lo que el asset realmente trae -- la pose bind YA es la pose "withered" caída/sentada, no hizo falta ningún retargeting). Hueso de cabeza real identificado: `bip_head_028` (hijo de `bip_neck_027`), usado para el efecto errático. Los otros 4 assets (imagen de jumpscare `golden_freddy.png`, `XSCREAM2.wav` -- confirmado distinto de `XSCREAM.wav`, ambos presentes en la misma carpeta --, `GlitchGrave.wav` -- confirmado distinto de `GlitchSound.wav`, en la raíz de `FDAF assets` --, y `robotvoice.wav`) verificados presentes y copiados sin ambigüedad.

**Calibración de escala/rotación -- metodología empírica real, no matemática manual (mismo criterio ya establecido para los otros 4 personajes en sesiones anteriores).** Un primer intento de estimar `scale`/`rotationXDegrees` por composición manual de matrices de nodo resultó en un amasijo irreconocible de extremidades esparcidas -- descartado. Probado sistemáticamente con capturas reales in-engine: `rotationXDegrees=0.0` (sin corrección, a diferencia de los otros 4) con `scale=0.018` mostró a Golden Freddy correctamente reconocible -- sentado/agazapado, rostro con dientes/ojos/sombrero visibles, look "withered" con endoesqueleto expuesto y cables sueltos, proporcionado de forma creíble contra el resto del mapa.

**Ubicación real -- bosquejada con escaneos de colisión reales, no a ciegas.** Bañó real identificado por sus nodos (`toilet.001_61`/`toilet_59`, ambos en X=13.74, separados en Z), resuelto a coordenadas de mundo reales (`getNode(...).globalTransform`, mismo patrón ya establecido). Un escaneo de colisión en grilla (con las semi-extensiones REALES del collider de Golden Freddy, 0.5/0.95/0.5 -- un primer intento con una sonda genérica más chica dio un falso "libre" que resultó embebido al usar las dimensiones reales) reveló un corredor angosto al oeste de los cubículos (X≈9.5-11.75, Z≈-5 a 1). Posición final: `goldenFreddyX=10.25, goldenFreddyZ=-4.75` (extremo sur del corredor, cerca de la pared -- el "final del pasillo" pedido), confirmado sin advertencia de embeddeo.

**Colisión física real, verificada con una prueba de movimiento real (no solo revisión de código):** `collisionWorld.addStaticCollider(...)` (una caja AABB fija de 1.0×1.9×1.0, nunca se mueve) -- un jugador simulado avanzando 60 pasos hacia el centro de Golden Freddy quedó bloqueado a 0.70 unidades de su centro, confirmando que el cuerpo es real y sólido, no decorativo. Como es un collider estático normal (no "solo jugador"), los otros 4 guardias también lo respetan automáticamente vía su `resolveMovement` ya existente, sin ningún cambio adicional en su lógica de IA.

**Proximidad + cabeza errática + GlitchGrave -- diseño e implementación:** rango 1m (distancia horizontal al cuadrado, sin `sqrt`), un solo `PointLightEx`-equivalente para el sonido (`sonidoGlitchGrave.loop()`/`.stop()`, un único `long id` rastreado, nunca se crea una segunda instancia mientras la primera sigue activa -- transición de entrada/salida explícita, no un chequeo de "¿está sonando?" cada frame). Cabeza errática implementada como **saltos discretos** a un ángulo aleatorio nuevo (yaw ±22°, pitch ±13°, roll ±9°) cada 0.05-0.22s (intervalo TAMBIÉN aleatorio) -- deliberadamente sin interpolación suave, para lograr el "fallo mecánico" pedido (nunca sinusoidal/circular/predecible, ambos requisitos se satisfacen trivialmente por construcción: ni el ángulo ni el intervalo siguen ningún patrón). Al salir de rango, la cabeza vuelve a la pose neutral en el mismo frame (nunca queda congelada a mitad de un glitch). Aplicado directamente al nodo `bip_head_028` (rotación base capturada una vez en `show()`, compuesta con el offset errático cada cambio, seguido de `ModelInstance.calculateTransforms()` explícito -- necesario porque Golden Freddy no tiene `AnimationComponent`, así que nada más recalcula sus transforms de hueso cada frame).

**Verificado con evidencia real, no solo inferido:** (a) prueba de estado secuenciada real (11 aserciones vía logs, cubriendo entrada/salida/reentrada de rango, disparo de interacción, ciclo completo jumpscare→fade→itsme, repetición automática vía el timer de 24s forzado a un valor ínfimo para no esperar en tiempo real, limpieza por muerte, y confirmación de que el estado deshabilitado bloquea tanto la proximidad como la interacción) -- las 11 coincidieron exactamente con lo esperado; (b) captura real confirmando la cabeza en un ángulo visiblemente distinto al neutral dentro de rango, más los valores numéricos de yaw/pitch/roll logueados difiriendo entre dos instantes distintos; (c) un stress test real de 50 ciclos rápidos de entrada/salida (llamadas directas a `actualizarGoldenFreddy` alternando posición) sin ninguna excepción y con el estado final perfectamente consistente (sin loops huérfanos).

**Jumpscare + fade + IT'S ME -- 3 fases nuevas en `EstadoPartida`** (`GOLDEN_FREDDY_JUMPSCARE`/`FADE`/`ITSME`), ninguna llama `engine.update()` en ningún momento (mismo patrón ya usado por `JUMPSCARE`/`ESTATICA`/`CINEMATICA_INICIAL`) -- congela genuinamente a los 4 animatrónicos durante TODA la secuencia, no solo durante el segundo del jumpscare. **Verificado con una prueba real, no solo por construcción:** posición de un guardia (Bonnie) registrada antes de disparar el jumpscare especial y comparada después de que la secuencia completa (jumpscare 1s + fade 1s + itsme 4s ≈ 6s reales) terminara -- **posición idéntica**, confirma el congelamiento real. Jumpscare: imagen `golden_freddy_jumpscare.png` (reutiliza `dibujarTexturaFullscreen`, la misma función ya usada por la estática) + `XSCREAM2.wav`, ambos arrancan en el mismo frame -- **verificado con una captura real** mostrando la imagen real renderizada a pantalla completa. Fade: respiración agitada (`respiracion_agitada.wav`, **reutilizado del recurso real de FDAF** -- `varios/respiracion_agitada.wav`, copiado a Escape porque nunca había existido ahí antes; Escape nunca tuvo su propio sonido de respiración hasta ahora) + fundido de negro a la escena congelada (mismo patrón `uiShapes` ya usado por los cortes de la cinemática).

**IT'S ME -- pedido explícito del usuario, CAMBIADO A MITAD DE SESIÓN respecto al diseño original.** La primera implementación generaba texto disperso "SOY YO"/"IT'S ME" por procedimiento (posiciones/tamaños/colores aleatorios, similar al efecto de `TransicionEscape` de FDAF) -- **el usuario pidió explícitamente reemplazarlo** por el GIF real `ITSME.gif` (encontrado en la raíz de `FDAF assets`, copiado como `assets/gifs/ItsMe.gif`), mismo patrón exacto ya usado por el GIF de estática (decodificado una vez con `GifDecoder`, recorrido en bucle con las duraciones reales de cada cuadro durante los 4 segundos). Todo el código del texto disperso (clase `GoldenFreddyTexto`, campos `goldenFreddyTextos`/`goldenFreddyProximoTexto`, la clave de idioma `goldenfreddy.itsme` en ambos `strings*.properties`) se **eliminó por completo**, no se dejó como código muerto. `robotvoice.wav` sigue sonando durante los mismos 4 segundos exactamente igual que en el diseño original -- eso no cambió. **Verificado con una captura real:** el GIF real ("IT'S ME" en tipografía glitcheada sobre negro) se renderiza correctamente a pantalla completa, 11 cuadros decodificados sin error.

**Repetición cada ~24s -- SOLO la fase ITSME, nunca el jumpscare completo (pedido explícito del usuario, verificado literalmente).** Único punto de entrada `iniciarGoldenFreddyItsMe()` reutilizado tanto por el flujo real (tras el fundido) como por el timer de repetición (`goldenFreddyTiempoHastaProximoEfecto`, decrementado solo cuando `goldenFreddyJumpscareYaOcurrido=true`) -- **verificado con una prueba real** forzando el timer a un valor ínfimo (evita esperar 24s reales) y confirmando que el siguiente ciclo entra DIRECTO a `GOLDEN_FREDDY_ITSME` (nunca pasa por `GOLDEN_FREDDY_JUMPSCARE`/`FADE`), con el mismo GIF+robotvoice, y que el timer se reprograma a otros ~24s al terminar.

**MUY IMPORTANTE -- limpieza al perder una vida, verificada por el camino REAL, no solo con una llamada aislada.** `limpiarGoldenFreddyPorMuerte()` (detiene cualquier `GlitchGrave`/`robotvoice` activo, resetea la cabeza a neutral, deshabilita permanentemente) se llama desde `resolverFinDeAtrapada()` -- el mismo método real que ya usan las 4 atrapadas normales -- solo si el jumpscare especial ya ocurrió antes. **Verificado con una prueba end-to-end real:** disparado el jumpscare especial, y LUEGO (tras la secuencia completa) el jugador fue teletransportado sobre la posición real de Freddy para forzar una atrapada REAL (no simulada) -- el flujo real completo (`ChaseState`→`CaughtState`→`iniciarJumpscare`→`actualizarEstatica`→`resolverFinDeAtrapada`→respawn) confirmó `goldenFreddyDeshabilitado=true` y `vidasRestantes` correctamente decrementado, disparado por el camino de producción real, no por una llamada directa al método de limpieza. Tras esto, tanto la proximidad (entrar en rango no reinicia `GlitchGrave`) como la interacción E (no vuelve a disparar el jumpscare) quedaron confirmadas como no-op reales.

**No verificado con oído humano** (misma limitación de entorno ya documentada repetidas veces en este archivo): `GlitchGrave`/`XSCREAM2`/`robotvoice`/`respiracion_agitada` se invocaron sin excepción en cada prueba real, pero no se puede confirmar que suenen correctamente sin un oyente humano real. **Tampoco se probó interactivamente con teclado/mouse reales** (mismo motivo, `Robot` no viable) -- toda la verificación usó el patrón ya establecido de teletransportar al jugador y llamar directamente a los métodos reales de interacción/estado, ejercitando el mismo código de producción que un jugador real activaría.

**Commiteado y pusheado a `five_doors_escape`:** corrección del borde blanco de la estática (`GameplayScreen.dibujarTexturaFullscreen`, ya presente, confirmado en esta sesión), ajustes visuales de la llave (`GameplayScreen.java`, `pizzeria.json`), Golden Freddy completo (`GameplayScreen.java`, `MapDefinition.java`, `BootScreen.java`, `pizzeria.json`, nuevo `content/entities/golden_freddy.json`, assets nuevos `models/GoldenFreddy.glb`, `gifs/ItsMe.gif`, `sounds/glitch_grave.wav`, `sounds/xscream2.wav`, `sounds/robotvoice.wav`, `sounds/respiracion_agitada.wav`, `textures/golden_freddy_jumpscare.png`). Toda la instrumentación temporal de esta sesión (múltiples bloques `DIAG_*` en `GameplayScreen.java`, un helper temporal `diagSetYawPitch` en `FirstPersonCameraController.java` agregado y retirado dos veces) se agregó y se retiró por completo -- confirmado con `git diff`/grep limpios en ambos archivos y una compilación final (`gradlew build`) sin advertencias nuevas. El WIP ajeno de FDAF (`.classpath`, `Main.java` con FlatLaf, `Juego.java` con el gate de batería, `PnlMenu.java`, `src/libs/`, la carpeta vacía `if3001 - pf - grupo#1/`) quedó intacto, sin tocar -- FDAF no tuvo ningún cambio de código esta sesión, solo se usó para la verificación de regresión y para confirmar que el borde blanco de la estática no aparece ahí.

### 2.40 Corrección y pulido de Golden Freddy/ItsMe tras la implementación de §2.39, sesión 2026-08-11

**Contexto:** el usuario jugó la implementación de §2.39 y pidió 8 correcciones puntuales, todas verificadas con corridas reales, no solo revisión de código: (1) Golden Freddy quedó demasiado pequeño y en una posición pobre; (2) el GIF `ItsMe.gif` debía verse mucho más grande; (3) el GIF mostraba un fondo/rectángulo visible que no debía estar ahí; (4) `robotvoice.wav` casi no se escuchaba; (5) `GlitchGrave.wav` sonaba demasiado bajo; (6) el fade desde negro tras el jumpscare no se sentía progresivo; (7) Golden Freddy debía desaparecer POR COMPLETO tras su jumpscare (nunca antes); (8) regresión completa del resto del juego.

**1. Tamaño y posición -- reescalado y reubicado con evidencia real, no a ojo.** `scale` 0.018→0.065 (~3.6x más grande) y reposicionado al extremo norte del mismo pasillo del baño (`goldenFreddyX=10.25,Z=-4.75`→`X=11.0,Z=0.5`). Collider ensanchado de `(0.5,0.95,0.5)` a `(0.65,0.9,0.65)` -- un primer intento con el collider antiguo a la escala nueva dejaba la cámara "dentro" de geometría de hombros/articulaciones en el punto de máximo acercamiento real (probado empujando una sonda con `collisionWorld.resolveMovement` paso a paso, no asumido). **Verificado con capturas reales in-engine** en el punto de acercamiento máximo real: modelo reconocible, sentado, sin atravesar paredes/suelo, sin que la cámara quede dentro de la malla. Proximidad (~1m), colisión real (sonda bloqueada a ~1.0 unidades del centro), detección por interacción (E) e integración con los otros 4 animatrónicos (su collider es estático normal, así que `ChaseState` de los guardias lo respeta automáticamente) confirmados sin cambios de comportamiento adicionales.

**2. GIF ItsMe.gif -- fondo investigado con una causa real doble, no asumida, y corregido.** Se escribieron diagnósticos standalone (fuera del repo) para inspeccionar el GIF a nivel de píxel antes de tocar código. Causa real #1: `GifDecoder` (compartido con el GIF de estática) nunca hacía composición real de canvas GIF -- trataba cada cuadro como una imagen independiente a pantalla completa, estirando cuadros "no-op" de 1×1 como rectángulos sólidos. **Reescrito por completo** para implementar composición real (`LogicalScreenDescriptor`, offsets de `ImageDescriptor`, `disposalMethod` de `GraphicControlExtension`, un `Pixmap` de canvas persistente). Causa real #2: los cuadros de contenido real del GIF tienen un fondo negro genuinamente opaco horneado en el asset original (confirmado con muestreo directo de píxeles, alpha=255). Corregido con una nueva pasada de **clave de luminancia** (`aplicarClavePorLuminancia`, nueva sobrecarga `decodificar(archivo, clavePorLuminancia)`) aplicada SOLO a `ItsMe.gif` (nunca a `Static.gif`, que no tiene este problema) -- convierte negro→transparente preservando el texto blanco. Además, `actualizarGoldenFreddyItsMe` se corrigió para componer con alpha blending real sobre la escena 3D congelada (`sceneManager.render()` + `GL_BLEND`) en vez de la función antigua `dibujarTexturaFullscreen` (que limpiaba el framebuffer y dibujaba opaco). **Verificado con capturas reales:** "IT'S ME" flotando limpiamente sobre la escena visible, sin ningún rectángulo de fondo.

**3. GIF ItsMe.gif -- tamaño, sin deformar.** Ajuste a pantalla completa preservando aspecto (`GOLDEN_FREDDY_ITSME_ESCALA=0.97`, fit-to-screen real vía `Math.min(anchoPantalla/anchoGif, altoPantalla/altoGif)`) -- el GIF real es ~16:9, muy cercano a la resolución típica de pantalla, así que ocupa casi toda la pantalla sin ninguna distorsión. El idioma del GIF NO se tocó -- sigue siendo el mismo archivo único sin lógica ES/EN (el pedido explícito del usuario de "no cambies la lógica ES/EN" se cumplió trivialmente: nunca existió tal lógica para este GIF en primer lugar).

**4/5. Volumen de robotvoice/GlitchGrave -- subido en código, sin poder confirmarlo al oído.** `sonidoRobotvoice.play(1.9f)` y `sonidoGlitchGrave.loop(1.8f)` (antes ambos a 1.0, el techo nominal ya usado en el resto del proyecto) -- OpenAL/libGDX permite ganancia >1.0 como amplificación real, no un error. Verificado que ninguno de los dos duplica su instancia activa (mismo patrón de `id` ya usado), que `robotvoice` se detiene exactamente a los 4s, y que `GlitchGrave` sigue entrando/saliendo de loop correctamente en entradas/salidas repetidas de rango -- **el nivel de volumen en sí no se puede verificar sin un oyente humano real**, limitación de entorno ya documentada repetidas veces en este archivo.

**6. Fade desde negro -- investigado el mecanismo real de FDAF, mecanismo propio confirmado equivalente, causa real encontrada.** Se leyó `ControllerInterfaz.iniciarFadeDesdeNegro()` (líneas ~935-991) por pedido explícito del usuario -- un `JPanel` transparente con `Timer` de 20ms calculando alpha desde tiempo real de reloj (`DURACION_FADE_MS=4000`). Confirmado **arquitectónicamente equivalente** al mecanismo ya existente en Escape (`actualizarGoldenFreddyFade`, un rectángulo negro `ShapeRenderer` con alpha decreciente por `dt` acumulado) -- no hacía falta portar nada nuevo. **Verificado con 21 capturas reales + el valor de alpha logueado en cada una:** el fundido matemáticamente SÍ progresaba de forma lineal y correcta (0.99→0.0), sin ningún salto brusco entre el cuadro final del jumpscare y el primer cuadro del fundido (imagen→negro casi opaco→fundido gradual, exactamente la secuencia pedida). La causa real del "no se siente progresivo" resultó ser la duración: `DURACION_GOLDEN_FREDDY_FADE` estaba en apenas 1 segundo (vs. los 4 segundos de FDAF) -- demasiado rápido para leerse como un fundido real en vez de un corte casi instantáneo. **Corregido a 2 segundos** (no se copiaron los 4s de FDAF tal cual porque aquí el fundido ya está seguido de un efecto ITSME de 4s adicionales, y alargarlo más habría hecho la secuencia completa sentirse lenta). Re-verificado con otra corrida real: 21 muestras de alpha decreciendo suavemente de 0.99 a 0.0 a lo largo de los 2s reales, captura de un cuadro intermedio (alpha≈0.49) mostrando una superposición gris claramente legible, ni negro puro ni la escena sin filtrar.

**7. Desaparición completa tras el jumpscare -- implementada y verificada con una sonda de colisión real, no solo con banderas de estado.** Nuevo método `hacerDesaparecerCuerpoGoldenFreddy()`, llamado una única vez al arrancar el jumpscare (`iniciarGoldenFreddyJumpscare`) -- antes de que el fundido/ITSME vuelvan a renderizar la escena real, así ningún cuadro posterior puede mostrarlo sentado ahí. Retira la escena de `sceneManager` (invisible), retira su collider de `CollisionWorld` (nuevo método `CollisionWorld.removeStaticCollider(BoundingBox)`, comparación por identidad, agregado porque antes solo existía `addStaticCollider`), y desactiva su `ObjetoInteractivo.activo` (ya no detectable por crosshair/E). `actualizarGoldenFreddy` se ajustó para que proximidad/`GlitchGrave`/cabeza errática queden condicionados a que el cuerpo siga visible -- únicamente el timer de repetición de ITSME cada ~24s (independiente de la posición del jugador) sigue vivo tras la desaparición, exactamente como pedía el usuario. **Verificado con una sonda de colisión real** tras el ciclo completo: el jugador simulado, que antes quedaba bloqueado a ~1.0 unidades del centro de Golden Freddy, ahora atraviesa sin obstáculo hasta ~2.95 unidades más allá (el límite de la propia sonda de prueba, no de ningún collider) -- confirma que el bloqueo físico desapareció de verdad, no solo que una bandera cambió de valor. También confirmado en la misma corrida: `interactivoGoldenFreddy.activo=false`, `idSonidoGlitchGraveActivo=-1`, `estado` de vuelta a `JUGANDO` normal.

**8. Regresión -- verificada con pruebas reales encadenadas, sin hallazgos.** `git diff --stat` acotado exactamente a los 6 archivos esperados (`golden_freddy.json`, `pizzeria.json`, `FirstPersonCameraController.java`, `GameplayScreen.java`, `GifDecoder.java`, `CollisionWorld.java`) -- ningún archivo fuera de alcance tocado. Con el jugador teletransportado (mismo patrón ya establecido de invocar directamente los métodos de producción reales en vez de simular teclado/mouse), se verificó en la misma corrida: los otros 4 guardias presentes con su FSM correcto (Freddy en `ChaseState` por `siempreEnPersecucion`, Bonnie/Chica/Foxy en `IdleState`); `dibujarMenuPausa()` renderiza sin excepción; `interactuarConPuertaSalida()` sin llave reproduce el sonido de forzar + mensaje `key.need`; `recogerLlave()` funciona; `interactuarConPuertaSalida()` con llave dispara la victoria real (`game.setScreen(EscapeVictoryScreen)` + `dispose()`) sin ninguna excepción en el log después. Una corrida final completamente limpia (sin ningún código de diagnóstico, cinemática inicial restaurada) llegó a RUN (`Risa de Freddy reproducida`) con los mismos 10912 colisionadores base de siempre, sin excepciones.

**No verificado con oído humano** (misma limitación de entorno, documentada repetidas veces en este archivo): los cambios de volumen de `robotvoice`/`GlitchGrave` se aplicaron y se invocaron sin excepción, pero no se puede confirmar el nivel percibido sin un oyente real. **Tampoco se jugó interactivamente en inglés** -- de menor riesgo aquí porque se confirmó leyendo el código que ninguna ruta de Golden Freddy/ItsMe tiene una rama condicional por idioma (el GIF y el fundido son el mismo asset/lógica sin importar `handoff.idioma`), a diferencia de otros textos de la pantalla (pausa, ayuda, mensajes de llave) que sí usan `Lang.get(handoff.idioma, ...)` y no se tocaron esta sesión.

**Toda la instrumentación temporal de esta sesión se agregó y se retiró por completo** (múltiples bloques de captura/prueba en `GameplayScreen.java`, el helper `diagSetYawPitch` en `FirstPersonCameraController.java`, el `estado` inicial forzado a `JUGANDO` para saltar la cinemática durante las pruebas) -- confirmado con `grep` limpio de todos los identificadores `diag*`/`DIAG_*` y una corrida final real sin ningún atajo de prueba. El WIP ajeno de FDAF (`.classpath`, `Main.java`, `Juego.java`, `PnlMenu.java`, `src/libs/`, la carpeta vacía `if3001 - pf - grupo#1/`) quedó intacto -- FDAF no tuvo ningún cambio de código esta sesión.

**Commiteado y pusheado a `five_doors_escape`:** `assets/content/entities/golden_freddy.json`, `assets/content/maps/pizzeria.json`, `core/src/main/java/com/fivedoorsescape/camera/FirstPersonCameraController.java`, `core/src/main/java/com/fivedoorsescape/screens/GameplayScreen.java`, `core/src/main/java/com/fivedoorsescape/util/GifDecoder.java`, `core/src/main/java/com/fivedoorsescape/world/CollisionWorld.java`.

### 2.41 Golden Freddy: orientación real corregida, secuencia jumpscare/ItsMe rediseñada por completo, sesión 2026-08-11 (misma fecha, sesión posterior a §2.40)

**Contexto:** el usuario jugó la corrección de §2.40 y reportó dos problemas nuevos, uno de orientación/posición y uno de diseño de secuencia. Pedido explícito: (1) investigar la orientación real del modelo GLB antes de tocar nada ("no adivines la rotación"); (2) rediseñar la secuencia para que **solo el jumpscare de ~1s detenga el juego** -- el fundido y el efecto ItsMe pasan a ser overlays visuales sobre el gameplay normal EN MARCHA (jugador y los 4 animatrónicos se siguen moviendo); (3) ItsMe queda habilitado el resto de la partida tras el primer jumpscare, incluyendo después de perder vidas (antes se deshabilitaba permanentemente al morir); (4) subir más el volumen de `GlitchGrave`.

**1. Orientación -- investigada con evidencia real, no adivinada.** Primer intento con capturas de 4 ángulos cardinales reveló el problema real: desde el lado abierto del pasillo (la dirección de aproximación real de un jugador), Golden Freddy mostraba la ESPALDA (sombrero y nuca visibles, ningún rasgo facial) -- confirmado el bug real de "dado vuelta". `rotationXDegrees` seguía en `0.0` (correcto, no es un problema de X) -- el bug era la ausencia total de corrección de `goldenFreddyYawDegrees` (seguía en `0.0` desde la creación original en §2.39, nunca se había calibrado). Aplicado un giro de `-90.0°` (derivado con la fórmula real de rotación Y usada por `RenderSyncSystem`/`FirstPersonCameraController`, luego confirmado empíricamente con capturas). **Investigación más profunda con un mapa de colisión real en grilla** (`CollisionWorld.overlapsStatic()`, metodología ya establecida en §2.15) reveló que Golden Freddy está sentado en un pequeño nicho que se abre hacia el ESTE desde el corredor principal norte-sur (que termina en una pared muerta justo al norte de él, confirmando que sí está "al final del pasillo") -- tras el giro, la vista desde el lado oeste del nicho (el ángulo real por el que un jugador que sube el corredor y gira para mirar hacia el nicho lo vería) mostró su cara completa, reconocible, de frente (ojos, dentadura, sombrero, botones) en vez de la espalda. Posición (`X=11.0, Z=0.5`) sin cambios -- ya coincidía con "el final del pasillo del baño".

**2/3. Secuencia jumpscare/fundido/ItsMe -- reestructuración arquitectónica completa, no un parche.** Antes, `GOLDEN_FREDDY_JUMPSCARE`/`FADE`/`ITSME` eran 3 `EstadoPartida` separados, los 3 sin `engine.update()` -- toda la secuencia (~7s) congelaba el juego. Ahora **solo `GOLDEN_FREDDY_JUMPSCARE` sigue siendo un estado que congela** (mismo patrón que `JUMPSCARE`/`ESTATICA`, ~1s). El fundido y ItsMe se eliminaron como estados -- son overlays 2D (`actualizarYDibujarOverlaysGoldenFreddy`, campos `goldenFreddyFadeActivo`/`itsMeActivo`) dibujados DESPUÉS de que `sceneManager.render()` ya pintó la escena real del frame, con `engine.update(dt)` real ya corrido. Como ambos viven exclusivamente dentro del cuerpo de JUGANDO normal, es estructuralmente imposible que se dibujen durante jumpscare/estática/Game Over/victoria/cinemática/pausa -- no hizo falta ningún chequeo ad-hoc para esas exclusiones, son automáticas por construcción. **Verificado con una prueba real de posición:** la posición de Freddy (siempre en movimiento por `siempreEnPersecucion`) se muestreó antes del jumpscare, justo al terminar (diferencia mínima, consistente con un solo frame de reanudación, no con ~60 frames de congelamiento real), y de nuevo tras el fundido de 2s (se movió 2.35 unidades) y tras ItsMe de 4s (se movió 1.45 unidades más) -- confirma que el jumpscare sí congela y que el fundido/ItsMe genuinamente NO lo hacen.

**4. ItsMe sobrevive a la muerte -- desacoplado de la desaparición del cuerpo, verificado con una "muerte" real simulada.** Se eliminó por completo el campo `goldenFreddyDeshabilitado` (que antes desactivaba TODO permanentemente al perder una vida). `limpiarGoldenFreddyPorMuerte()` se reescribió: sigue deteniendo cualquier `GlitchGrave`/`robotvoice`/instancia de ItsMe en curso en el instante exacto de la atrapada (para que nada quede sonando bajo Game Over/estática), pero ya NO desactiva la repetición -- reprograma el timer a otros ~24s, exactamente como si ItsMe hubiera terminado normalmente. La desaparición del cuerpo (`goldenFreddyCuerpoVisible`, collider, interactivo) sigue siendo permanente y no se tocó. **Verificado con una prueba real de ciclo completo:** disparado el jumpscare real, dejado correr un ciclo completo de ItsMe, forzado un segundo ciclo (timer reducido a un valor ínfimo, mismo patrón ya usado en sesiones anteriores para no esperar 24s reales), llamado `limpiarGoldenFreddyPorMuerte()` A MITAD del segundo ciclo (simulando una atrapada real) -- confirmado que ItsMe se cortó limpiamente (`itsMeActivo=false`, robotvoice detenido) pero `goldenFreddyJumpscareYaOcurrido` siguió `true` y el timer se reprogramó a ~24 en vez de quedar deshabilitado, y un TERCER ciclo de ItsMe arrancó normalmente después -- prueba real de que la repetición sobrevive a una muerte, no solo inferido leyendo el código.

**5. GlitchGrave -- subido de nuevo.** `GOLDEN_FREDDY_VOLUMEN_GLITCH_GRAVE` de `1.8` a `2.6` (sobre el ya amplificado de §2.40). Confirmado por `grep` que esta constante se usa en un único punto (`sonidoGlitchGrave.loop(...)`) y no afecta ningún otro sonido -- aislamiento verificado. **No se puede confirmar el volumen percibido sin oído humano real**, misma limitación de entorno documentada repetidas veces en este archivo.

**Regresión -- collider/proximidad/interacción re-verificados tras el cambio de yaw** (el yaw es puramente visual, `RenderSyncSystem` lo aplica solo al `ModelInstance`, nunca al collider AABB de `CollisionWorld`, pero se re-confirmó con una sonda de colisión real): bloqueado a 0.96 unidades del centro (coincide con el medio-extent 0.65 + el medio-extent del jugador 0.3), proximidad (`dentroDeRango=true`) y `GlitchGrave` arrancando correctamente en la nueva orientación. `git diff --stat` acotado exactamente a `pizzeria.json` (yaw) y `GameplayScreen.java` (la reestructuración) -- `FirstPersonCameraController.java` volvió a su estado exacto pre-sesión (se agregó y se retiró el mismo helper de diagnóstico, diff neto cero). Una corrida final completamente limpia (sin ningún código de diagnóstico) llegó a RUN con los mismos 10912 colisionadores base de siempre, sin excepciones.

**No verificado con oído humano** (misma limitación de entorno): el aumento de volumen de `GlitchGrave` se aplicó y se invocó sin excepción, pero no se puede confirmar el nivel percibido sin un oyente real. **Tampoco se jugó interactivamente con teclado/mouse reales** (mismo motivo de siempre, `Robot` no viable en este entorno) -- toda la verificación usó teletransporte del jugador + llamadas directas a los métodos reales de producción (`interactuarConGoldenFreddy()`, `limpiarGoldenFreddyPorMuerte()`), ejercitando el mismo código que un jugador real activaría, con evidencia real (posiciones/logs) en cada paso.

**Commiteado y pusheado a `five_doors_escape`:** `assets/content/maps/pizzeria.json` (yaw), `core/src/main/java/com/fivedoorsescape/screens/GameplayScreen.java` (reestructuración completa de la secuencia + volumen de GlitchGrave).

### 2.42 Golden Freddy apoyado en el suelo/pared real, audio de GlitchGrave/robotvoice corregido en el archivo fuente, y variación real de aproximación de Freddy, sesión 2026-08-12

**Contexto:** el usuario jugó la corrección de §2.41 y reportó tres problemas nuevos: (1) Golden Freddy levitando y recostado contra una puerta en vez de una pared; (2) `GlitchGrave`/`robotvoice` probados personalmente por el usuario -- "efectivamente no se escuchan suficientemente", ya no una limitación de entorno sino un bug real confirmado por el usuario; (3) Freddy (el animatrónico principal) siempre terminaba acercándose por el mismo lado. Pedido explícito en los 3 casos: investigar la causa raíz real antes de tocar nada, nunca "ajustar un número a ciegas".

**1. Golden Freddy: geometría real investigada, no ajustada a ojo.** Parseado el `.glb` del mapa directamente (accessors `POSITION` min/max, mismo método ya usado en sesiones anteriores) y consultado `mapScene.modelInstance.getNode("puerta.003_44", true).globalTransform` -- la puerta real está en `(11.65, 0.92, 0.59)`, prácticamente en el borde este de su collider anterior (`X=11.0` + medio-extent `0.65` = `11.65`): confirma que sí estaba apoyado contra la puerta, no contra una pared. Investigada la geometría real del cuarto con un escaneo de colisión en grilla **con su propio collider retirado temporalmente** (para no contaminar la lectura con su propia caja) más sondas de precisión (`CollisionWorld.resolveMovement` empujado paso a paso) -- reveló una pared sur real en `z≈-0.08` para `x≥10.98`. Reposicionado a `X=10.75, Z=0.6` (despeja la puerta por 0.25 unidades, se apoya en esa pared real). Para la levitación: `goldenFreddyY` **no existía como campo** -- su Y estaba fijo en `0f` en código, sin forma de ajustarlo. A diferencia de los 4 animatrónicos de pie (su pivote de rig ya coincide con el suelo a Y=0), la pose "withered" agazapada de Golden Freddy tiene el pivote desplazado respecto al punto más bajo real de su malla. Agregado el campo (`MapDefinition`/`pizzeria.json`), calibrado empíricamente a `-0.5` con una captura real a baja altura mostrando sus pies tocando el suelo limpiamente (antes: gran hueco visible entre sus pies y el piso incluso desde un ángulo casi a ras de suelo). El collider sigue anclado al suelo real (`Y=0` a `Y=1.8`, no al pivote visual) -- no necesitaba cambiar. Verificado sin advertencia de embeddeo y con una sonda de colisión real bloqueada a 0.97 unidades del centro (coincide con medio-extent 0.65 + medio-extent del jugador 0.3).

**2/3. GlitchGrave/robotvoice -- causa raíz real: rango dinámico del archivo, no el número de volumen.** Analizados los `.wav` reales (pico/RMS en Python, `wave`+`array`, sin dependencias externas) contra otros sonidos ya audibles del juego para comparar. Hallazgo real: `glitch_grave.wav` tenía pico en `-8.9dBFS` pero RMS de solo `-26.3dBFS` (mayormente silencio digital real, confirmado -77dB en las secciones más silenciosas, con transitorios breves) -- el volumen en código ya estaba en `2.6x`, lo que llevaba ese pico a `-0.6dBFS`, **prácticamente al techo de recorte**. Subir el número más habría distorsionado, justo lo que el usuario pidió no hacer. Mismo patrón en `robotvoice.wav` (pico real `-6.5dBFS` dentro de los primeros 4s que realmente suenan -- verificado que esos 4s no son una intro más silenciosa que el resto del archivo de 15s --, RMS `-21.6dBFS`, volumen en código ya en `1.9x`, con solo ~0.9dB de margen real antes de recortar). Confirmado además que no hay ningún multiplicador de volumen maestro/global en el código (`grep` de `setVolume`/`AudioDevice`/`alListener` en todo el proyecto) y que la música espacial de Freddy nunca supera volumen `1.0`, así que no hay otro sonido "tapando" a estos dos por volumen. **La causa real era el archivo fuente**, no el multiplicador. Corregidos procesando los `.wav` reales (script propio de nivelado por ventanas + normalización de pico a un techo seguro, sin dependencias externas): `glitch_grave.wav` RMS `-26.3dB→-22.3dB` (pico a `-1.0dB`), `robotvoice.wav` RMS `-21.7dB→-16.2dB` (pico a `-2.4dB`) -- mismo formato/canales/frecuencia/duración exactos, reemplazo directo. Con el archivo ya más fuerte por sí solo, el volumen en código bajó a valores modestos (`GlitchGrave` 2.6→1.05, `robotvoice` 1.9→1.25) que dejan margen real sin recortar. Verificado que ambos archivos cargan sin excepción en una corrida real completa. **No se puede confirmar el volumen percibido sin oído humano real** -- misma limitación de entorno documentada repetidas veces, pero esta vez la causa raíz sí quedó identificada y corregida con evidencia medible (no solo "subir el número"), que es lo que el usuario pidió explícitamente.

**4. Freddy -- sesgo de aproximación investigado y corregido sin tocar la IA.** Revisado `ChaseState.java` a fondo: no tiene pathfinding ni selección de puertas/rutas -- es persecución directa en línea recta hacia la posición actual del jugador, resuelta contra colisión eje por eje, con una heurística de desatasco genérica. El sesgo **no venía de la IA** -- venía de que `freddyStartX/Z` era un único punto fijo combinado con un spawn de jugador también fijo, así que el ángulo relativo inicial (y por lo tanto el lado de aproximación durante gran parte de la partida) era idéntico en todas las partidas. Solución limpia sin tocar `ChaseState`/`IdleState`/`CaughtState` ni introducir aleatoriedad por frame: un escaneo de conectividad completo del edificio (misma metodología ya establecida en §2.15) encontró 3 puntos de spawn alternativos reales, verificados libres de colisión, en zonas bien separadas del mapa (`freddyStartAltX1-3`/`Z1-3` en `pizzeria.json`) -- elegidos UNA VEZ por partida (`MathUtils.random(3)` en `show()`) entre esos 3 más el original. La cámara de la toma de Freddy en la cinemática inicial (antes una posición fija que solo funcionaba para el spawn original) ahora se calcula en tiempo real con una búsqueda real de colisión (mismo patrón de offset `(-X,+Z)` ya usado para los otros 3 personajes, con variantes de respaldo si esa dirección específica está bloqueada en la sala donde le tocó aparecer). **Verificado con 8 corridas reales**: los 4 candidatos (incluido el original) fueron seleccionados en distintas corridas, cero advertencias de embeddeo, la búsqueda de cámara siempre encontró una posición libre (una vez necesitó su primera variante de respaldo).

**Regresión -- verificada con una corrida real de punta a punta, no solo revisión de código.** Aproximación real por colisión a la nueva posición de Golden Freddy, proximidad + `GlitchGrave` activo, interacción E disparando el jumpscare real, jumpscare terminando sin excepción con el cuerpo desaparecido, Freddy (el real, persiguiendo) moviéndose 2.47 unidades durante el fundido (confirma que el fundido/ItsMe de §2.41 sigue sin congelar el juego tras estos cambios), ItsMe terminando sin excepción, y el jugador teletransportado sobre la posición real de Freddy (cualquiera haya sido su spawn esa partida) para forzar una atrapada real -- sin ninguna excepción en todo el recorrido. `git diff --stat` acotado a `pizzeria.json`, `MapDefinition.java`, `GameplayScreen.java`, y los dos `.wav` (binarios, mismo formato) -- `FirstPersonCameraController.java`/`CollisionWorld.java` con diff neto cero (instrumentación temporal agregada y retirada). Una corrida final completamente limpia llegó a RUN con los mismos 10912 colisionadores base, sin excepciones ni advertencias.

**Commiteado y pusheado a `five_doors_escape`:** `assets/content/maps/pizzeria.json`, `assets/sounds/glitch_grave.wav`, `assets/sounds/robotvoice.wav`, `core/src/main/java/com/fivedoorsescape/content/MapDefinition.java`, `core/src/main/java/com/fivedoorsescape/screens/GameplayScreen.java`.

### 2.43 Causa raíz real de "Freddy no aparece" (gap de línea de visión en la cámara de la cinemática) y reorientación real de Golden Freddy contra su pared verdadera, sesión 2026-08-14

**Contexto:** tras apagar la máquina al cerrar §2.42, el usuario reportó dos pedidos para la sesión siguiente: (1) girar ligeramente a Golden Freddy para que quedara "correctamente recostado contra la pared" (ajuste fino, no reposicionar); (2) investigar desde cero, sin parchear coordenadas a ciegas, por qué Freddy "no aparece en el mapa" -- catalogado por el usuario como urgente. Ambos investigados con evidencia real (capturas in-engine, escaneos de colisión con self-collider retirado), nunca a ciegas.

**1. Freddy -- causa raíz real: no es spawn, no es render, no es la IA. Es un gap de línea de visión en la cámara de la cinemática inicial, introducido en §2.42.** Descartado metódicamente con evidencia real, en orden: (a) los 4 candidatos de spawn (`freddyStartX/Z` + `AltX1-3/Z1-3`) verificados de nuevo en vivo, cero advertencias de embeddeo en ninguno (confirma lo que ya decía §2.42); (b) capturado el estado ECS real de Freddy 3s dentro de JUGANDO (`Mappers.transform/ai/animation`) -- posición válida, `ChaseState` activo, animación `walk` aplicada, sin excepciones; (c) capturado su render real aislando `mapScene` de `SceneManager` (misma metodología de §"Nivel 2" de la Fase 6) -- se renderiza perfecto, escala/pose/textura correctas; (d) capturado el render real CON el mapa y con la linterna del jugador reubicada a mano sobre la cámara de diagnóstico (sin esto, cualquier posición fuera del cono de la linterna real sale negra y simula un falso "no se ve" que no prueba nada) -- Freddy se ve bien durante el gameplay normal. Ninguno de estos cuatro caminos reproducía el reporte del usuario.

**El síntoma real estaba en otro lugar: la TOMA DE FREDDY DE LA CINEMÁTICA INICIAL** (la primera vez que cualquier jugador nuevo lo ve). `buscarPosicionCamaraSegura()` (agregada en §2.42 para que la cámara de esa toma seleccione un punto real cerca de cualquiera de los 4 candidatos de spawn) solo llamaba a `CollisionWorld.overlapsStatic()` sobre la posición de la CÁMARA en sí -- nunca comprobaba si el trayecto recto entre esa cámara y Freddy pasaba a través de mobiliario suelto de por medio (que no es collider de pared, así que `overlapsStatic` en la cámara nunca lo detecta). Reproducido en vivo, con captura real: con el candidato de spawn `(0.0, 6.0)` -- que corresponde a la oficina --, el primer offset probado (`(-1,+2)` → cámara en `(-1, 1.7, 8.0)`) no está incrustado, pero un **monitor CRT decorativo del escritorio de la oficina** queda exactamente en medio del trayecto recto hacia Freddy: la toma mostraba el monitor a pantalla completa, cero Freddy visible en el encuadre. §2.42 verificó "8 corridas reales, cero advertencias de embeddeo" y dio esto por bueno -- esa verificación era cierta pero insuficiente, nunca revisó una captura real de la toma en sí con cada candidato. **3 de los 4 candidatos de spawn usan `buscarPosicionCamaraSegura()` (solo el original usa el encuadre fijo `(6,1.7,-2)` ya verificado a mano en sesiones anteriores)**, así que este agujero afectaba a la mayoría de las partidas nuevas -- consistente con que el usuario lo describiera como que Freddy "no aparece", ya que la primera impresión del personaje (la cinemática) fallaba la mayoría de las veces, aunque el Freddy real (spawn/IA/render durante el gameplay) siempre estuvo intacto.

**Corrección real (causa raíz, no las coordenadas):** nuevo método `CollisionWorld.lineaDeVisionLibre(desde, hasta)` -- muestrea el segmento recto en pasos de 0.15u y prueba `overlapsStatic` en cada punto (mismo patrón de escaneo real ya usado en todo el proyecto, no una heurística nueva). `buscarPosicionCamaraSegura()` ahora exige overlap-free en la cámara **Y** línea de visión libre hacia el objetivo antes de aceptar cada uno de los 7 offsets candidatos. Verificado en vivo, mismo candidato reproducido (`0.0, 6.0`): la función descartó el offset bloqueado por el monitor y avanzó al siguiente (`(-1,-2)` → cámara en `(-1, 1.7, 4.0)`), con una captura real confirmando a Freddy completamente visible, bien encuadrado, en la misma partida donde antes solo se veía el monitor.

**2. Golden Freddy -- la pared real está al norte, no al sur; §2.42 tenía la orientación equivocada de la pared aunque la posición/altura sí eran correctas.** Pedido del usuario esta sesión: solo un ajuste fino de rotación, manteniendo posición/escala/collider. Antes de tocar el yaw, se re-verificó la geometría real alrededor de su posición actual (`10.75, -0.5, 0.6`) con un escaneo de colisión en las 8 direcciones cardinales/diagonales, **con su propio collider retirado temporalmente** (mismo patrón exacto que §2.42, para no contaminar la lectura) -- resultado real: pared sólida a **0.95u al norte** (`+Z`) y a 1.05u al NE, sin nada en 2 unidades hacia el sur (`-Z`) ni hacia el este (`+X`, la puerta real, tratada como zona pasable). **Esto contradice la premisa de §2.42** ("pared sur en z≈-0.08 para x≥10.98") -- esa medición era válida solo para `x≥10.98`, pero la posición real final de Golden Freddy (`x=10.75`) quedó 0.23 unidades al oeste de ese umbral, justo en el hueco/vano de esa pared, no contra ella. Con yaw `-90°` (heredado de §2.41), su espalda apuntaba hacia el ESTE -- hacia la puerta real, exactamente el defecto que el usuario pidió corregir ("no recostado hacia la puerta") -- y su costado, no su espalda, quedaba junto a la única pared real cercana (norte).

**Corrección:** `goldenFreddyYawDegrees` de `-90.0` a `180.0` en `pizzeria.json` -- con la convención de yaw ya establecida en el proyecto (`Ry(θ)` sobre `Vector3.Y`, mismo `atan2(dx,dz)` que usa `ChaseState`), esto orienta su espalda hacia el norte (la pared real) y su frente hacia el sur (el lado abierto real, confirmado sin colisión en 2u -- el verdadero camino de aproximación del jugador, no el oeste que asumía §2.41). Es un cambio de 90°, mayor que un "ajuste fino" literal, pero es el único valor que la geometría real (recién medida, no las notas de sesiones anteriores) sostiene para satisfacer los 2 requisitos explícitos del usuario a la vez (pared correcta + no hacia la puerta) sin mover posición/escala/collider, que se dejaron intactos. Verificado con una captura real desde el ángulo de aproximación sur real, con el mapa cargado y la linterna del jugador reubicada sobre la cámara de diagnóstico: Golden Freddy se ve completo, de frente, con la pared visible justo detrás de su cabeza/espalda, pies apoyados en el suelo, puerta visible a un costado (ya no detrás).

**Regresión -- corrida real de punta a punta con `clean build` completo (ambos módulos) y ejecución en la configuración real de producción (fullscreen, `Lwjgl3Launcher` sin tocar).** Cinemática inicial → `INTRO_CORAZONES`/`INTRO_RUN` → `JUGANDO` con 10912 colisionadores (mismo número base que todas las sesiones anteriores, confirma que la geometría del mapa no se tocó) → sin ninguna excepción/advertencia en todo el recorrido (incluida la carga de Bonnie/Chica/Foxy/Golden Freddy, GIFs de estática/ItsMe, sonidos). Todo el código de diagnóstico (capturas con cámara reubicada, escaneos de pared, framebuffer, forzado del candidato de spawn, modo ventana temporal) se agregó y se retiró por completo -- `git diff --stat` final acotado exactamente a `pizzeria.json` (1 línea, el yaw), `CollisionWorld.java` (+26, el nuevo método `lineaDeVisionLibre`), `GameplayScreen.java` (+23/-9, la firma de `buscarPosicionCamaraSegura` y su llamada) -- `Lwjgl3Launcher.java` con diff neto cero. Ningún proceso de juego quedó huérfano (verificado con `Get-Process java` tras cada corrida y al cerrar la sesión, solo quedan los Gradle Daemons esperados).

**Commiteado y pusheado a `five_doors_escape`:** `assets/content/maps/pizzeria.json`, `core/src/main/java/com/fivedoorsescape/world/CollisionWorld.java`, `core/src/main/java/com/fivedoorsescape/screens/GameplayScreen.java`.

### 2.44 §2.43 declarada resuelta prematuramente -- causa raíz REAL de "la cinemática se ve rara" (Freddy de espaldas / cámara dentro de su propia malla), Golden Freddy reposicionado con el chequeo real de embeddeo (no una sonda aproximada), y rango de proximidad ampliado, sesión 2026-08-15

**Contexto crítico:** el usuario jugó la corrección de §2.43 y reportó que el problema **seguía existiendo** -- Freddy seguía sin verse bien y la cinemática inicial "se veía rara". Pedido explícito: no repetir el error de §2.43 (una sola captura al final de la toma, que por casualidad salía bien) -- reproducir la cinemática COMPLETA, con capturas en varios puntos de progreso (no solo el final), para cada uno de los 4 candidatos de spawn de Freddy, antes de declarar nada resuelto.

**Hallazgo real: §2.43 solo resolvía la mitad del problema (oclusión), nunca verificó ORIENTACIÓN.** Con capturas reales en 3 puntos de progreso (2%, 45%, 90%) para cada candidato, forzando cada uno por turno (`freddyCandidatoElegido` fijo temporalmente, revertido a `MathUtils.random(3)` al cerrar):

- **Candidato `(-11, 3)`: correcto en los 3 puntos** (cámara `(-1,+2)`, primer offset de la lista antigua, coincidía con verse de frente).
- **Candidato `(0, 6)`, la oficina: DE ESPALDAS durante TODA la toma**, las 3 capturas idénticas. Causa real: durante `CINEMATICA_INICIAL`, `AISystem` se excluye del engine a propósito (`posicionarModelosParaCinematica`, ver §2.43) para que `ChaseState` nunca rote a Freddy mientras está congelado -- su yaw queda fijo en el valor de creación, **0 grados para los 4 candidatos por igual** (`crearGuardia` → `factory.createEntity(..., 0f)`). Con esa convención (`Ry(θ)`, mismo `atan2(dx,dz)` que usa `ChaseState`), yaw=0 significa que Freddy siempre mira hacia `+Z` (norte) mientras está congelado, sin importar dónde le tocó spawnear. La lista de offsets de §2.43 mezclaba variantes con Z positiva (caen dentro de su cono frontal fijo → se ve la cara) y Z negativa (caen detrás → se ve la espalda) sin distinguirlas, y devolvía la primera que pasara los chequeos geométricos -- para la oficina, la variante frontal quedaba bloqueada por el monitor (correctamente descartada por `lineaDeVisionLibre`) y cayó a una variante trasera, que sí tiene visión libre pero un encuadre incorrecto.
- **Candidato `(-8, -9)`: EL PEOR CASO -- cámara literalmente dentro de la propia malla de Freddy**, un cuadro irreconocible (huecos negros, geometría sin sentido, como mirar el interior de su boca). Causa real: la lista fija de ~13 offsets no encontraba NINGUNO válido en esa sala, y la función caía al último recurso (`return new Vector3(objetivoX, altura, objetivoZ)` -- cámara exactamente sobre el objetivo). Un barrido real de 861 puntos (radio 5, todas las alturas, con `overlapsStatic`+`lineaDeVisionLibre` reales) encontró 135 posiciones válidas, todas en una franja angosta (`Z≈-9.5`) que ninguno de los offsets fijos cubría -- una sala puede tener una abertura real angosta e impredecible en cualquier dirección, y una lista fija de offsets no puede cubrir eso de forma general.

**Corrección real de ambas causas (no un ajuste de coordenadas más):**
1. **`buscarPosicionCamaraSegura()` reemplazada por un escaneo real por distancia/ángulo creciente** (8 distancias × 24 direcciones, empezando por las más cercanas para mejor encuadre) en vez de una lista fija de offsets -- garantiza encontrar cualquier posición real y despejada dentro de un radio de 6 unidades, sin importar la forma de la sala. Verificado en vivo: ningún candidato de los 4 necesitó el último recurso en esta sesión.
2. **En vez de asumir cuál lado es "el frente", se ROTA A FREDDY para que mire hacia la cámara real que se encontró**, sea cual sea el lado (`Mappers.transform.get(freddyEntity).yawDegrees` fijado justo antes del único `engine.update(0f)` que congela su pose). Esto no afecta el gameplay real -- en el primer frame de `JUGANDO`, `ChaseState.update()` recalcula su yaw desde cero según la posición real del jugador, sobreescribiendo este valor temporal por completo (confirmado leyendo `ChaseState`, ningún otro código lee `yawDegrees` antes de esa primera pasada de IA real).
3. **Bug secundario encontrado durante la verificación del candidato `(-8,-9)` con cámara más cercana (1.2u, la distancia mínima ahora probada):** el offset fijo `(0.7, 0.9)` con el que la cámara "encuentra" a Freddy girando al inicio de la toma se calibró a mano para la distancia original (~2.24u) -- a 1.2u ese mismo offset absoluto es desproporcionado, el primer frame mostraba solo una mano en la esquina. Corregido: los candidatos alternativos (cámara calculada, distancia variable) usan el objetivo centrado desde el primer frame, sin el efecto cosmético de "panning" -- el candidato original (encuadre fijo ya verificado a mano) lo conserva sin cambios.

**Golden Freddy -- reposicionado "un poco más atrás" (mismo pedido explícito, ajuste pequeño) verificado con el chequeo REAL de embeddeo, no con una sonda aproximada.** Escaneo con su propio collider retirado temporalmente (mismo patrón de siempre) midió la pared real a 0.92u al norte con una sonda delgada (medio-extent 0.08) -- primer intento moviéndolo +0.15 (`Z: 0.6→0.75`) **SÍ disparó la advertencia real de embeddeo** (`warnIfEmbedded`, que usa el collider completo `GOLDEN_FREDDY_HALF_EXTENTS=0.65`, no la sonda delgada) al correr una regresión real en producción -- la sonda delgada de diagnóstico daba una lectura optimista que no coincidía con el collider real, mucho más grande. **Lección real: para verificar "no atraviesa la pared" hay que usar el chequeo de producción real (`warnIfEmbedded`/el collider completo), no una sonda de diagnóstico más pequeña que no representa la geometría real que importa.** Corregido a un incremento menor (`Z: 0.6→0.68`, +0.08 en vez de +0.15) -- verificado sin ninguna advertencia de embeddeo en una corrida real completa.

**Rango de proximidad ampliado (pedido explícito: "demasiado corto... pero no enorme, debe seguir sintiéndose cercano").** `GOLDEN_FREDDY_RANGO_PROXIMIDAD` de `1.0` a `1.8` -- casi el doble pero por debajo de `GOLDEN_FREDDY_DISTANCIA_INTERACCION=2.2`, así que el jugador siempre nota el glitch de cabeza/`GlitchGrave` ANTES de estar lo bastante cerca para interactuar con E, nunca al revés. `GlitchGrave` ya usaba el mismo campo (`dentroDeRango` en `actualizarGoldenFreddy`) que el movimiento de cabeza, así que quedan sincronizados automáticamente sin tocar nada más -- verificado llamando al método REAL de producción con posiciones de jugador simuladas a distancias crecientes (3.0/2.0 fuera, 1.5/0.5 dentro, 2.5 fuera de nuevo): entrada dispara un único loop de sonido (id real asignado), permanecer dentro no duplica el sonido (mismo id se mantiene), salir lo detiene limpiamente (id vuelve a -1).

**Regresión -- `clean build` completo (ambos módulos) + corrida real en configuración de producción (fullscreen) desde cinemática hasta bien entrado `JUGANDO`, dos veces (antes y después del ajuste fino de Golden Freddy), sin ninguna excepción/advertencia -- mismos 10912 colisionadores base en ambas.** Todo el código de diagnóstico (capturas en 3 puntos de progreso por candidato, barridos de grilla de hasta 861 puntos, escaneo de pared con self-collider retirado, prueba funcional de rango con `actualizarGoldenFreddy` real) se agregó y se retiró por completo -- `git diff --stat` final acotado a `pizzeria.json` (1 línea, `goldenFreddyZ`) y `GameplayScreen.java` (`buscarPosicionCamaraSegura` reescrita, rotación de Freddy en `construirRutaCinematicaInicial`, offset de mirada condicional, constante de rango). Ningún proceso quedó huérfano (verificado tras cada corrida).

**Commiteado y pusheado a `five_doors_escape`:** `assets/content/maps/pizzeria.json`, `core/src/main/java/com/fivedoorsescape/screens/GameplayScreen.java`.

### 2.45 Ajustes finos verificados con geometría real (Golden Freddy a su izquierda, llave hacia la mesa vecina, rango ampliado a 2.5) y texto nuevo de interacción reutilizando el raycast existente, sesión 2026-08-16

**Contexto:** el usuario probó personalmente la corrección de §2.44 y confirmó que "en general quedó bien" -- esta sesión fueron ajustes finos sobre una base ya validada, no otra investigación de causa raíz. Pedido explícito de no mover nada "a ojo": comprobar la geometría real después de cada cambio.

**1. Golden Freddy movido a SU PROPIA izquierda.** Con yaw=180° (fijado en §2.44), se derivó la convención real de "derecha"/"izquierda" del personaje component a partir del mismo convenio de rotación ya usado en todo el proyecto (`Ry(θ)`, forward local=+Z): `derecha = forward × up`. Para `forward=(0,0,-1)` (yaw=180), `derecha=(1,0,0)=+X`, por lo tanto `izquierda=-X`. `goldenFreddyX` de `10.75` a `10.6` (-0.15, ajuste pequeño). Verificado con el chequeo REAL de embeddeo (`collisionWorld.overlapsStatic` con `GOLDEN_FREDDY_HALF_EXTENTS` completo, no una sonda -- lección de §2.44) en la posición final: `incrustado=false`, con margen real medido en las 4 direcciones cardinales (0.88u libres hacia el oeste antes de chocar, pared norte casi rozando como ya era intencional). Captura real desde el ángulo de aproximación confirma que sigue apoyado contra la misma pared, de frente, pies en el suelo, puerta a un costado.

**2. Llave movida hacia la mesa/mostrador de al lado, con la superficie real identificada por geometría, no adivinada.** Investigación real: un escaneo por nombre de nodo ("mesa"/"cocina") cerca de la llave solo encontró un mueble real (`Object_10`/`Object_11`, padre `muebles cocina_4`) a 3.16u -- nada directamente debajo de la llave con ningún nombre reconocible. Un volcado sin filtro por distancia real punto→caja (no centro→punto, que falla para mallas grandes) a 1.2u de la llave solo encontró las mallas estructurales gigantes (paredes/piso) -- confirma que la llave descansa sobre un mostrador de cocina fusionado a la estructura, no una "mesa" nombrada aparte. Movida `0.4` unidades en la dirección real hacia ese mueble (`keyX: 10.8755→11.236`, `keyZ: 6.2545→6.0815`). Una captura real (ambiente subido temporalmente para diagnóstico, restaurado después) confirmó visualmente el resultado: la llave quedó apoyada en el borde de la encimera de granito, justo junto a la estufa de la cocina -- sobre una superficie real y válida, sin atravesar nada, visiblemente más integrada con el entorno que antes. `recogerLlave()` (el mismo método real que dispara E) sigue funcionando -- su `ObjetoInteractivo` deriva su posición de las mismas `mapDef.keyX/Y/Z`, así que no necesitó ningún cambio de código.

**3. Rango de proximidad de Golden Freddy ampliado de nuevo, `1.8 → 2.5`.** Pedido explícito tras jugarlo: "un poco más largo... de manera razonable, sin exagerar". A diferencia de §2.44, esta vez el rango deliberadamente SUPERA `GOLDEN_FREDDY_DISTANCIA_INTERACCION=2.2` -- el pedido explícito de esta sesión ("notar que algo está mal desde una distancia claramente mayor, antes de llegar demasiado cerca") ya implica que el glitch debe notarse antes de que el jugador esté a distancia de interactuar, lo cual el nuevo valor logra igual de bien. Verificado llamando al método REAL de producción (`actualizarGoldenFreddy`) con distancias simuladas: 3.2/2.6/3.0 fuera de rango, 2.0/0.5 dentro (mismo id de sonido sin duplicarse, se detiene limpio al salir) -- confirma el umbral real y la sincronía con `GlitchGrave`. La interacción E (`interactuarConGoldenFreddy`) se confirmó independiente del rango de glitch, como ya lo era desde su diseño original.

**4. Texto "Presiona E para interactuar"/"Press E to interact" agregado reutilizando el sistema de interacción YA EXISTENTE, sin heurística nueva.** El sistema de raycast (`actualizarInteraccion()`, `Intersector.intersectRaySphere` contra `objetosInteractivos`) ya mantenía un campo `objetivoInteractivoActual` (no null cuando el rayo real golpea un interactivo activo dentro de su distancia máxima) -- usado hasta ahora solo para agrandar el crosshair. El texto nuevo (`dibujarTextoInteraccion()`) simplemente comprueba ese mismo campo: cero lógica de proximidad/distancia propia, funciona automáticamente con los 3 miembros actuales de `objetosInteractivos` (llave, Golden Freddy, puerta de salida) y con cualquier interactivo futuro que se agregue a esa misma lista, sin condiciones por objeto. Localización vía el bundle `Lang`/`I18NBundle` ya existente (`assets/i18n/strings*.properties`, nueva clave `interact.prompt`) -- nunca texto hardcodeado. Presentación: escala `0.85` (menor que la escala base `1.3` de `uiFont`) y offset de 34px debajo del centro de la pantalla, para que se sienta como un hint discreto de HUD de terror, nunca un mensaje central llamativo ni algo que tape el crosshair.

**Verificación real, no solo revisión de código -- las 3 pruebas usaron los métodos REALES de producción, reubicando `camera.position/direction` (mismo patrón ya establecido para diagnósticos sin `Robot`) y llamando directamente a `actualizarInteraccion()`/`recogerLlave()`/`interactuarConGoldenFreddy()`:**
- Mirando la llave: `objetivoInteractivoActual!=null → true`, captura real confirma crosshair activo + texto visible debajo, discreto.
- Sin mirar nada interactuable: `→ false`, captura confirma crosshair normal y CERO texto.
- Mirando a Golden Freddy: `→ true`, texto visible.
- Idioma real de la partida de esta sesión resultó ser INGLÉS (el handoff de prueba) -- la captura mostró el texto real en pantalla como `"Press E to interact"`, y además se confirmaron ambas cadenas exactas por separado: `Lang.get(ESPANOL,...)="Presiona E para interactuar"`, `Lang.get(INGLES,...)="Press E to interact"` -- coinciden exactamente con lo pedido en los dos idiomas.
- `recogerLlave()` real: `tieneLlave` pasa de `false` a `true` sin excepción.
- `interactuarConGoldenFreddy()` real: dispara el jumpscare (`estado=GOLDEN_FREDDY_JUMPSCARE`, `goldenFreddyJumpscareYaOcurrido=true`) sin excepción, tras el reposicionamiento y el nuevo rango.

**Regresión -- `clean build` completo (ambos módulos) + corrida real en configuración de producción (fullscreen) desde cinemática hasta bien entrado `JUGANDO`, sin ninguna excepción/advertencia (incluida ninguna advertencia de embeddeo para la nueva posición de Golden Freddy) -- mismos 10912 colisionadores base.** Todo el código de diagnóstico (escaneos de nodos reales, capturas con cámara/linterna/ambiente reubicados, simulaciones de rango, pruebas destructivas de recoger-llave/jumpscare) se agregó y se retiró por completo -- `git diff --stat` final acotado a `pizzeria.json` (posición de llave y Golden Freddy), `strings.properties`/`strings_en.properties` (una clave nueva cada uno), y `GameplayScreen.java` (constante de rango, método nuevo `dibujarTextoInteraccion()` + su llamada). `Lwjgl3Launcher.java` con diff neto cero. Ningún proceso quedó huérfano.

**Commiteado y pusheado a `five_doors_escape`:** `assets/content/maps/pizzeria.json`, `assets/i18n/strings.properties`, `assets/i18n/strings_en.properties`, `core/src/main/java/com/fivedoorsescape/screens/GameplayScreen.java`.

### 2.46 Ajuste rápido y acotado: reversión de la llave, rango de Golden Freddy a 3.1, volumen de GlitchGrave/robotvoice subido de nuevo, sesión 2026-08-17

**Contexto:** pedido explícito de un "ajuste sencillo" sobre la base ya validada de §2.45 -- sin tocar nada más. Tres cambios puntuales, cada uno verificado con los métodos reales de producción (misma metodología ya establecida: reubicar `camera`/simular posiciones y llamar directamente a `actualizarInteraccion()`/`actualizarGoldenFreddy()`/`recogerLlave()`/`interactuarConGoldenFreddy()`, sin `Robot`).

1. **Llave revertida exactamente a su posición anterior a §2.45** (`keyX: 11.236→10.8755`, `keyZ: 6.0815→6.2545`) -- el usuario probó el desplazamiento hacia la mesa vecina y prefirió la posición original. Sin cambios de código: `recogerLlave()`/el raycast derivan la posición del mismo `mapDef.keyX/Y/Z`, así que revertir el JSON basta. Verificado: raycast real detecta la llave, `recogerLlave()` real sigue funcionando sin excepción.
2. **`GOLDEN_FREDDY_RANGO_PROXIMIDAD` `2.5 → 3.1`** (+24%, "moderado, sin exagerar" como pidió el usuario). Posición/orientación/escala/collider/interacción E de Golden Freddy sin tocar. Verificado con `actualizarGoldenFreddy()` real y distancias simuladas: 3.5/3.3/3.4 fuera de rango, 2.5/0.5 dentro (mismo id de `GlitchGrave` sin duplicarse, se detiene limpio al salir) -- umbral real entre 2.5 y 3.3, consistente con 3.1.
3. **Volumen de `GlitchGrave`/`robotvoice` subido de nuevo** (`1.05→1.3`, `1.25→1.5`) tras reporte del usuario de que "todavía se escuchan demasiado bajos". **Aviso real, documentado en el propio código:** según las mediciones de pico de la sesión que procesó los `.wav` (§2.42: glitch_grave normalizado a pico -1.0dB, robotvoice a -2.4dB), el margen restante antes de recortar a estos multiplicadores nuevos es de solo ~1-1.5dB por encima de 0dBFS en los picos más fuertes -- este aumento puede introducir un recorte leve y breve en los picos (no sostenido, ya que la mayor parte de la señal real sigue siendo silencio). No se reprocesaron los `.wav` esta sesión (pedido explícito de "ajuste sencillo", sin Python disponible en este entorno para repetir el análisis de amplitud) -- si el resultado suena distorsionado, el paso real siguiente sería volver a nivelar/normalizar los archivos, no seguir subiendo este número. Como siempre, el volumen percibido no puede confirmarse sin oído humano real -- verificado únicamente que `sonidoRobotvoice.play(1.5)` devuelve un id real válido sin excepción.

**Regresión -- `clean build` completo + corrida real en configuración de producción (fullscreen) hasta bien entrado `JUGANDO`, sin ninguna excepción/advertencia, mismos 10912 colisionadores base.** Diagnóstico temporal agregado y retirado por completo -- `git diff --stat` final acotado a `pizzeria.json` (2 líneas, posición de llave) y `GameplayScreen.java` (3 constantes). Golden Freddy (posición/yaw), el texto de interacción y todo lo demás de §2.45 quedaron intactos, sin tocar. Ningún proceso quedó huérfano.

**Commiteado y pusheado a `five_doors_escape`:** `assets/content/maps/pizzeria.json`, `core/src/main/java/com/fivedoorsescape/screens/GameplayScreen.java`.

### 1.25 Auditoría tras dos meses: integración del commit del compañero `b1b6d10` y corrección del doble escalado de fuentes, sesión 2026-10-22

**Estado inicial real (verificado con Git, no supuesto):** `five_doors_at_freddys` estaba 1 commit detrás de `origin/master` -- `b1b6d10` ("se supone", NaygellArias, 2026-09-22). `five_doors_escape` ya sincronizado (`3ece8a7`), solo con `dev/` sin trackear (residuo ya conocido). Cambios locales sin commitear en FDAF: (a) documentación §2.43-§2.46 de sesiones anteriores, correspondiente a commits de Escape ya pusheados (`db3706e`..`3ece8a7`) -- commiteada aparte como `53ba894`; (b) el WIP ajeno ya documentado desde §2.19 (`.classpath`/`Main.java` FlatLaf, `Juego.java` gate de batería Noche 3+, `PnlMenu.java` versión "v 2.7" + fuente de créditos, `src/libs/`, carpeta vacía `if3001 - pf - grupo#1/`), con fechas de archivo del 2026-07-30 al 2026-08-11 -- **sigue sin tocarse ni commitearse**. Pull hecho con `git stash` → `git pull --ff-only` → `git stash pop`, sin conflictos (`PnlMenu.java` cambió en ambos lados pero en hunks distintos).

**Qué hizo el compañero en `b1b6d10`:** (1) `Fuentes.obtener` pasó a escalar el tamaño con la resolución; (2) `PnlMenu` dejó de crear `new Font("Consolas", ...)` a mano y usa `Fuentes.obtener` (consistente con la regla de §1.5, correcto); (3) la guitarra de Bonnie se muestra en un overlay más grande y en otra posición (`mostrarEnOverlay(ruta, 02, lado)` → `mostrarIconoEnOverlay(..., 402)`); (4) reemplazó las 8 imágenes de coleccionables (Freddy/Bonnie/Chica/Foxy, normal y bloqueado) por versiones mucho más livianas (p.ej. `cupcake.png` 2.0MB→7KB) -- bueno para memoria; (5) un `import java.awt.Font` sin uso y líneas en blanco en `EscalarVista.java` (inofensivo).

**Bug real introducido por (1), medido y corregido.** La fórmula era `EscalarVista.getEscalaX(EscalarVista.getEscalaY(tamano))` -- aplica AMBOS factores. En la pantalla real de esta máquina (Java 8 reporta 1920×1080 una vez inicializado Swing -- **gotcha real:** antes de crear el primer componente Swing reporta 1536×864, el valor no-DPI-aware; medido con un probe aparte) eso multiplica cada fuente por ~1.44 mientras los bounds de los componentes (`EscalarVista.adaptar*`) solo crecen ~1.2. **Evidencia:** arnés que construye los paneles reales (menú, opciones, idioma, volumen, Custom Night, tableta, Game Over, warning), aplica los `adaptar*` reales y compara `FontMetrics.stringWidth`/alto contra cada componente: antes del cambio del compañero 0 desbordes; con su cambio, 5 de ancho y 6 de alto ("Nueva Partida" → "Nueva Par...", "Salir del Juego", "Volver a intentar", créditos "(..." y el título "Five Doors at Freddy's" sin su 4ª línea). Confirmado también con un render real del `PnlMenu` a PNG fuera de pantalla. **Corrección:** `Fuentes.escalar(t) = min(getEscalaX(t), getEscalaY(t))` -- un solo factor, igual al de los componentes; conserva la intención del compañero (fuentes proporcionales a la resolución). Además el `JTextPane` del título nunca pasaba por `adaptarMenu` (daba igual con fuente fija); ahora `adaptarMenu` escala su TAMAÑO (no su posición: la posición sin escalar es la que lo deja alineado con los botones, verificado en render). **Resultado medido:** 0 desbordes de ancho; quedan 2 de 1px de alto en `lblVersion`/`lblCreditos` (caja base de 25px con fuente 25 por diseño original, sin recorte visible en el render). `EscalarVista.java` se tocó solo en `adaptarMenu` (bounds), mismo tipo de cambio que §1.18 en `adaptarTablet` -- `GifEscalado` intacto.

**Riesgo pendiente del WIP ajeno, documentado (no tocado):** el `PnlMenu.java` sin commitear usa `lblCreditos.setFont(Fuentes.obtener(EscalarVista.getEscalaX(25)))` -- ahora que `Fuentes` ya escala, eso escalaría dos veces (fuente 36 en caja de 30px). Si alguien commitea ese WIP, debe quitar el `getEscalaX` interno.

**Commiteado y pusheado a `five_doors_at_freddys`:** `Fuentes.java`, `EscalarVista.java` (`adaptarMenu`).

### 1.26 Guitarra de Bonnie imposible de recoger en la puerta derecha (cambio (3) de `b1b6d10`), sesión 2026-10-22

**Problema real, reproducido.** El compañero hizo que la guitarra se muestre como overlay grande (153×402 base, el tamaño exacto de la imagen nueva) en `x=275` (izquierda) / `x=1755` (derecha). Pero `mostrarIconoEnOverlay` ubica el overlay en coordenadas de **pantalla** en el instante de la revelación, y la cámara en ese instante está paneada del todo hacia el lado de la puerta (es el único estado en que su botón DOOR/LIGHT es visible). Base 1755 está fuera del lienzo base de 1600 → en 1920×1080 el overlay cae en `x=2107`. Además, después de revelar, el overlay solo se mueve junto con el paneo, y con la cámara ya al tope derecho no puede volver a entrar en pantalla. **Resultado: guitarra 0% visible → imposible hacer clic en sus 6s (`MS_COLECCIONABLE`) → coleccionable perdido**, y la noche exige `inventario.todosEncontrados()` para ganar.

**Reproducción real (no deducida solo del código):** arnés con `ControllerInterfaz` + `ControllerCamara.init` reales, paneo real con `derecha(40)`/`izquierda(40)` (los mismos `Timer` que disparan las zonas de hover), luego `alRevelarColeccionable` real. Medido: derecha guitarra `overlay x=2107` → **0% visible**; izquierda guitarra 98%; cupcake en ambos lados 100%. Render `printAll` del `PnlJuego` confirmó que la colocación izquierda del compañero es intencional y se ve bien (guitarra apoyada en el marco de la puerta izquierda).

**Corrección:** derecha `1755 → 1130` (base), que espeja la colocación izquierda dentro del marco de la puerta derecha (≈1108-1300 base con el paneo real). Re-medido: derecha 98% visible (el 2% restante es el borde inferior, igual que la izquierda original), render confirma la guitarra apoyada en el marco derecho. La izquierda y el resto del cambio del compañero no se tocaron. Las imágenes nuevas de coleccionables también se renderizaron en los 10 slots del inventario (`agregarAlInventario` real) -- se ven bien, sin distorsión apreciable; el detalle (`mostrarDetalleColeccionable`) ya conserva proporción.

**Commiteado y pusheado a `five_doors_at_freddys`:** `ControllerInterfaz.java`.

### 1.27 Cierre de dos deudas técnicas abiertas desde §1.7/§1.8, sesión 2026-10-22

1. **Fuga de listeners de `ControllerMenu.opciones()` (§1.8).** Re-verificada contra el código actual: `botones()` llama `opciones()` en cada clic de "Opciones", y `opciones()` hacía `addActionListener` sobre `btnAtras`/`btnSalir` de la instancia persistente. **Medido por reflexión** (arnés que invoca `opciones()` 5 veces sobre el `ControllerMenu` real): HEAD = 5 listeners acumulados en cada botón; corregido con un flag de registro único = 1. Firma pública de `opciones()` intacta. Impacto real acotado (el proceso se reinicia cada noche, §1.18), pero dentro de una misma sesión de menú cada clic en "Atrás" disparaba N `setContenido`.
2. **Instrumentación temporal retirada** (pendiente desde §1.7): `contadorLlamadas`/`registrarMemoria()` (`[MEMORIA]`, un `String.format` + 3 consultas a `Runtime` en CADA carga de gif) en `CargarGifs.java`, y el `println("[AMBIENTAL] ...")` de `SonidosAmbientalesAleatorios.java`. El caché `ultimaImagenPorRuta` + `flush()` y el observador `MANTENER_ANIMACION_ACTIVA` (correcciones reales de §1.7/§1.13) quedan intactos.

**Verificación:** el árbol exacto a commitear (HEAD + estos 3 archivos, sin el WIP ajeno) compila desde cero con JDK 1.8.0_271 (único aviso preexistente de `Arbol.java`), y la prueba de humo en ejecución (paneo + revelación de coleccionables con gifs/sonidos reales, §1.26) pasa igual que antes.

**Commiteado y pusheado a `five_doors_at_freddys`:** `ControllerMenu.java`, `CargarGifs.java`, `SonidosAmbientalesAleatorios.java`.

### 2.47 Auditoría de Escape tras dos meses -- textura de jumpscare de Golden Freddy sin liberar, sesión 2026-10-22

**Estado:** `five_doors_escape` sin commits nuevos desde `3ece8a7` (§2.46); `clean build` completo con JDK 21 → `BUILD SUCCESSFUL`. Auditoría del ciclo de vida de recursos de las 4 pantallas: todos los `Pixmap` (GIF de estática/ItsMe, viñeta, sangre) se liberan justo tras crear su `Texture`; `BootScreen`/`NightGameOverScreen`/`EscapeVictoryScreen` liberan batch/shapes/fuentes/sonido propio. **Único hallazgo real:** `texturaGoldenFreddyJumpscare` (creada con `new Texture(...)` en `show()`, §2.39) nunca se liberaba en `GameplayScreen.dispose()`. Impacto práctico bajo (cada `GameplayScreen` vive una sola vez por proceso y el proceso termina tras Game Over/victoria), pero era una fuga de textura GPU real. Corregido con una línea en `dispose()`.

**Verificación en ejecución real** (`gradlew lwjgl3:run`, fullscreen de producción): instrumentación temporal que, a los 3s de JUGANDO, llama a los métodos reales `recogerLlave()` + `interactuarConPuertaSalida()` (camino real victoria → `game.setScreen` + `dispose()`). Log: cinemática → RUN con los 10912 colisionadores base → `dispose completado sin excepcion, escapado=true`, sin ninguna excepción. Instrumentación retirada (`git diff` final = 1 línea), proceso `Lwjgl3Launcher` huérfano cerrado explícitamente (gotcha conocido de `gradlew lwjgl3:run`).

**Commiteado y pusheado a `five_doors_escape`:** `GameplayScreen.java`.

### 2.48 Reorganización de los recursos de Escape con la misma convención que FDAF, sesión 2026-10-22

Misma regla que §1.54 (inglés `snake_case`, carpetas por función, idioma `_en`/`_es` al final; §3 regla 10). Estructura nueva de `five_doors_escape/assets/`: `audio/music/` (`freddy_music_box`, `victory_circus`), `audio/sfx/` (latidos `heartbeat_normal/fast`, `flashlight_click`, `item_pickup`, `door_locked_rattle`, `jumpscare_scream`, `static`, `heavy_breathing`, `freddy_laugh`, `golden_freddy_glitch/scream/robot_voice`), `audio/voice/` (`escape_intro_narration_{en,es}`), `effects/` (`static.gif`, `golden_freddy_its_me.gif`), `jumpscares/` (`golden_freddy_jumpscare.png`), `ui/` (`heart_full/empty.png`), `models/` (`freddy`, `bonnie`, `chica`, `foxy`, `golden_freddy`, `key`, `pizzeria` `.glb`), y sin cambios `content/` (JSON de definición: sus nombres son identificadores de código, p. ej. `llave`, `pizzeria`) e `i18n/`. 28 archivos movidos con `git mv`, ninguno retirado. Cinco son idénticos byte a byte a recursos de FDAF y llevan el mismo nombre nuevo para que la relación sea evidente (`jumpscare_scream`, `static.wav`/`static.gif`, `heavy_breathing`, `freddy_laugh` = `freddy_laugh_01` de FDAF).

**Referencias:** 21 constantes/literales en `GameplayScreen` y `EscapeVictoryScreen` y los 7 `modelPath` de `content/` (`BootScreen` usa las constantes de `GameplayScreen`; no había ninguna ruta construida por código). Tabla completa en `docs/catalogo_recursos.md` del repo FDAF.

**Verificación:** `gradlew clean lwjgl3:jar` correcto; el jar contiene los 28 recursos con su nombre nuevo y 0 entradas bajo `sounds/`, `gifs/` o `textures/`. Ejecución real de la distribución local (`lwjgl3:installDist`) con `-Duser.home` aislado durante 70 s: mapa cargado (10912 colisionadores, el valor de siempre), 5 animatrónicos y la llave, GIF de estática (8 cuadros) e IT'S ME (11 cuadros), risa de Freddy al mostrar RUN; 0 excepciones. La pantalla de victoria (`victory_circus.wav`) no se alcanzó en esa corrida: su ruta se comprobó en el jar.

### 1.28 Estabilidad de memoria re-verificada, distribución portable reconstruida y probada, y hallazgo (no corregido) de intermitencia del video de victoria, sesión 2026-10-22

**1. Prueba de estrés real de una noche larga (FDAF, `-Xmx256m`).** Arnés sobre `ControllerInterfaz`/`ControllerCamara` reales, 200 ciclos de puerta (abrir → luz → revelar animatrónico/coleccionable/batería → apagar → liberar → cerrar), con GIFs de puerta y de jumpscare, fade desde negro, respiración y sonidos reales. Medición cada 40 ciclos tras forzar GC (`jmap -histo:live` para `DirectClip`): heap vivo 68→104-134MB (oscila, sin tendencia), hilos 16→24-28, **"Image Animator" se estabiliza en 11** (uno por GIF distinto, por el caché `ultimaImagenPorRuta`+`flush()` de §1.7) y **`Clip` vivos 3-4**. Sin fuga ni acumulación con el código actual (incluido el cambio del compañero).

**2. Distribución portable: estaba desactualizada y se reconstruyó.** `C:\FiveDoorsPortableTest` es del 2026-08-10 14:43, anterior a 12 commits de Escape (Golden Freddy, interacción E, etc.), al commit del compañero y a las correcciones de hoy. Reconstruida con `build-distribucion.bat` desde un `git worktree` limpio en HEAD `1364627` (sin el WIP ajeno, criterio de §1.21; **gotcha repetido**: invocar el `.bat` siempre con ruta completa). Verificado dentro de los jars que llevan el código actual (`Fuentes.escalar`, imágenes nuevas, `GoldenFreddy.glb`/`ItsMe.gif`, y el `dispose()` de §2.47 por `javap`). Copiada a **`C:\FiveDoorsPortable_2026-10-22\`** -- no se pudo borrar/reemplazar `C:\FiveDoorsPortableTest` (protección de la herramienta contra borrados recursivos; queda para que el usuario la borre a mano, igual que la carpeta vacía `C:\VidFresco` de las pruebas de video). Pruebas reales desde la carpeta nueva, runtime y jar del paquete y `-Duser.home` aislado:
   - Noche 1 ganada (`alGanar()` real) → proceso viejo muerto, nuevo con `--reinicio-interno` y el runtime embebido; progreso `NOCHE_2` guardado.
   - Noche 5 ganada → `[ESCAPE] Usando distribucion empaquetada: ...\FiveDoorsEscape\lwjgl3`, Escape con su JDK 21 embebido, 10912 colisionadores, RUN + risa de Freddy, `escapeDesbloqueado=true`. Al cerrar Escape, el Swing original murió y apareció uno nuevo (reinicio de §2.38). Sin procesos huérfanos; `config.properties` real del usuario intacto (`NOCHE_5`/`INGLES`/`escapeDesbloqueado=true`).
   - **Metodología:** el proceso reiniciado NO hereda `-Duser.home` (ReiniciadorJuego solo pasa classpath) -- en pruebas aisladas hay que matarlo enseguida; en producción no aplica.

**3. Hallazgo: el video de victoria (`ganar_fdaf.mp4`, JavaFX) falla de forma intermitente con `MediaException ERROR_MEDIA_INVALID`.** Apareció en la primera corrida desde la distribución. **Hipótesis "JavaFX 8 no reproduce MP4 desde `jar:`" descartada con evidencia:** el mismo jar reprodujo bien muchas veces después. Se implementó copiar el video a un temporal y reproducir desde `file:`, y se comparó sobre 5 copias frescas del jar cada uno: HEAD 5/5 bien, **fix 4/5 (también falló una vez desde `file:`)**. Conclusión: intermitencia del backend GStreamer de JavaFX en esta máquina, independiente del origen del archivo (~1 de cada 5-8 arranques observados). **El cambio se revirtió** (no ataca la causa). Impacto: `PnlWin.reproducirVideo` ya maneja `setOnError` llamando a `alTerminar`, así que la partida sigue (se salta el video, no se cuelga) -- verificado en la corrida de Noche 1. **Pendiente** si se quiere eliminar: investigar GStreamer/JavaFX o reintentar una vez (decisión del usuario).

**Sin cambios de código en esta sección.**

### 1.29 Presentación de NYDER antes de la advertencia inicial, sesión 2026-10-22

**Pedido:** al abrir el juego, mostrar `NYDER.png` con entrada y salida graduales (~3s en total) **antes** de la advertencia inicial, reutilizando el fade existente, sin alterar la advertencia.

**Recursos:** `src/com/fdaf/resources/images/nyder/` (aportados por el usuario, sin seguimiento hasta esta sesión): `NYDER.png` (2172×724, texto blanco + estrella violeta sobre transparente), `NYDER-logo.png` (1254×1254), `NYDER-star.png`, `icon_maskable.png`, `icon_rounded.png`. Se commitean solo los dos que se usan (`NYDER.png`, `NYDER-logo.png`); los otros 3 quedan sin seguimiento, como estaban.

**Implementación (la más simple compatible con el flujo existente):**
- `PnlWarning` ganó un segundo constructor `PnlWarning(Image, fraccionAncho)`: misma pantalla negra y mismo alpha que la advertencia, pero dibuja la imagen **centrada y conservando su proporción** (máx. 55% del ancho / 80% del alto, interpolación bicúbica). El constructor original y su dibujo a pantalla completa no cambiaron.
- `ControllerMenu.mostrarAdvertenciaInicial()` ahora marca `advertenciaActiva=true` y llama a `mostrarPresentacionNyder()`: `animarFade` 0→1 en 600ms, `Timer` de 1800ms, `animarFade` 1→0 en 600ms (3.0s), y `finalizarPresentacionNyder()` libera la imagen (`flush()`, se usa una vez por inicio) y llama a `mostrarPantallaAdvertencia()` -- el cuerpo original de la advertencia, **sin cambios** (1s + 3s + 1s). Todo pasa por el mismo `timerAdvertenciaActual`, así que nunca hay dos timers de la secuencia corriendo a la vez.
- `saltarAdvertencia()` (CTRL+S): durante NYDER salta solo la presentación y la advertencia se muestra completa; durante la advertencia, igual que antes.
- El reinicio interno de proceso (`--reinicio-interno`, §1.20) sigue yendo directo al menú: NYDER aparece una sola vez por inicio normal. ESC no actúa en esta pantalla (igual que en la advertencia). Si `NYDER.png` faltara, se va directo a la advertencia.

**Pruebas (arnés con el `ControllerMenu.init()` real, ventana real, `-Duser.home` aislado, muestreo cada 100ms del panel activo y su alpha por reflexión):**
- Secuencia normal: NYDER alpha 0.52→1.00 (visible ~1.8s)→0.09, luego ADVERTENCIA 0→1.00 (3s)→0.25, luego `PnlMenu` con `advertenciaActiva=false`. NYDER nunca coincide con la advertencia.
- CTRL+S a mitad del fade-in de NYDER: pasa directo a la advertencia, que corre completa una sola vez y luego el menú (confirma que no queda un timer viejo vivo).
- Captura del panel a alpha 1.0: logo centrado, sin deformar, fondo negro.
- Limitación: el "parpadeo"/suavidad se verificó por valores de alpha y capturas, no a ojo humano en la ventana real.

**Archivos:** `PnlWarning.java`, `ControllerMenu.java`, `resources/images/nyder/NYDER.png`.

### 1.30 Créditos con nombres retirados del menú y logo de NYDER en su lugar, sesión 2026-10-22

**Qué se retiró:** únicamente `lblCreditos` de `PnlMenu` (`"©2026 Derek, Naygell, Ramsey, Elmer (UCR)"`, esquina inferior derecha), con su campo, getter/setter y su escalado en `EscalarVista.adaptarMenu`. No había otra referencia en el código (grep). `lblVersion` ("v 2.1", esquina inferior izquierda) y el resto del menú no se tocaron. Fuera del alcance y sin tocar: la sección "Créditos" de `README.md`.

**Logo:** nuevo `JLabel lblLogoNyder` en la misma esquina inferior derecha que ocupaban los créditos, simétrico a la versión de la esquina opuesta: base 120×120 en (1460,768), es decir con 20px de margen derecho y 12px de margen inferior sobre el lienzo de 1600×900. Sobre el fondo real (`Menu.gif`) esa zona es oscura (debajo del rostro de Freddy), así que el logo blanco/violeta se lee bien sin tapar nada. `PnlMenu` carga `NYDER-logo.png` una vez; `adaptarMenu` calcula el lado con UN solo factor (`min(escalaX, escalaY)`, para que siga cuadrado en pantallas que no son 16:9), lo ancla a la esquina, asigna el icono ya escalado (`getImagenEscalada`, `SCALE_SMOOTH`) y libera el PNG original de 1254px (`tomarLogoNyderOriginal()` + `flush()`). `adaptarMenu` se ejecuta una sola vez por proceso.

**Pruebas:**
- Render del `PnlMenu` real sobre el primer cuadro real de `Menu.gif` (1920×1080): logo de 144×144 en (1753,921), nítido, sin deformar, alineado con la línea base de la versión; el arnés no encontró intersecciones con ningún otro componente visible.
- Cálculo con la misma fórmula para 1280×720, 1366×768, 1600×900, 1920×1080, 2560×1440, 1280×1024 y 1920×1200: siempre cuadrado (96-192px), dentro de pantalla y lejos de la versión y de los botones (que están en la mitad izquierda).
- Arranque real completo (`ControllerMenu.init()`, ventana real, `-Duser.home` aislado): NYDER → advertencia → menú; logo mostrándose con icono de 144px; ninguna etiqueta con los nombres; Opciones → Atrás dos veces vuelve al menú correctamente.
- Compilación del árbol a commitear desde cero con JDK 1.8.0_271.

**Interacción con el WIP ajeno de `PnlMenu.java`:** ese WIP cambiaba la línea `lblCreditos.setFont(...)`; al eliminarse la etiqueta, esa línea del WIP desaparece necesariamente (y con ella el riesgo de doble escalado señalado en §1.25). El otro cambio del WIP en ese archivo ("v 2.1"→"v 2.7") sigue intacto en el working tree y **no** se commitea.

**Archivos:** `PnlMenu.java`, `EscalarVista.java` (`adaptarMenu`), `resources/images/nyder/NYDER-logo.png`.

### 1.31 El fade in de NYDER no se veía -- causa raíz: arrancaba antes de que la ventana se pintara, sesión 2026-10-22

**Síntoma (reportado por el usuario jugando):** se percibía el fade out de NYDER pero no una entrada gradual.

**Causa raíz, medida (no supuesta):** `ControllerMenu.init()` llama a `mostrarAdvertenciaInicial()` (que arranca el fade) **antes** de `vp.init()` (`setVisible(true)`), y la ventana maximizada tarda ~400ms más en pintarse. Sonda en ejecución real (muestreo cada 10ms del alpha + `AWTEventListener` del primer `PaintEvent` de la ventana): con §1.29, el fade in completo ocurría entre 1937ms y 2329ms, y **el primer pintado real fue a los 2362ms** -- la imagen aparecía ya al 100%. La advertencia no tiene el problema porque arranca cuando la ventana ya está en pantalla.

**Corrección:** `PnlWarning.alPrimerPintado(Runnable)` ejecuta una acción una sola vez, cuando el panel se pinta por primera vez estando visible. NYDER ya no arranca al crearse el panel sino ahí: 250ms de negro + 800ms de entrada + 1300ms visible + 650ms de salida (= 3.0s, misma duración total que antes). Un `Timer` de respaldo de 1500ms arranca la secuencia si el pintado nunca llegara; `entradaNyderIniciada` garantiza que solo una de las dos vías la inicie, y la presentación no arranca si CTRL+S ya la saltó. Los timers siguen pasando por `timerAdvertenciaActual` (uno a la vez). La advertencia no cambió.

**Pruebas reales (ventana real, `-Duser.home` aislado):**
- Sonda: primer pintado a 1915ms; NYDER en alpha 0 hasta ~2170ms, entrada 0.1→1.0 en 10 escalones entre 2238ms y 2938ms (**toda después del pintado**), visible, salida, advertencia.
- Tira de fotogramas reales del panel durante la animación (alpha 0.00 / 0.34 / 0.71 / 1.00 / salida 0.47): entrada gradual visible, imagen centrada sin deformar.
- CTRL+S durante la fase negra: advertencia completa una sola vez → menú; no aparece ningún fade de NYDER tardío.

**Archivos:** `PnlWarning.java`, `ControllerMenu.java`.

### 1.32 Política de versiones -- REGLA PERMANENTE para todo commit publicado, sesión 2026-10-22

**Fuente única de verdad:** `com.fdaf.util.Version` (`MAYOR`, `MENOR`, `texto()` → `"v 2.2"`, mismo formato que ya mostraba el menú). `PnlMenu.lblVersion` usa `Version.texto()`; no hay ninguna otra constante de versión en el repo (verificado con `git grep`).

**Regla (la propuesta por el usuario, adoptada tal cual):**
- Cada commit que se publica a `origin/master` sube `MENOR` en 1: `v2.1 → v2.2 → v2.3 → … → v2.100`.
- Después de `v2.100` viene `v3.1`: `MAYOR` sube en 1 y `MENOR` vuelve a **1** (nunca a 0).
- Si una tarea necesita varios commits publicados, cada uno sube la versión una vez. Commits que solo tocan documentación también cuentan si se publican (la regla es "por commit publicado", sin excepciones, para que no haga falta decidir caso por caso).
- El cambio de versión va en el mismo commit que el trabajo que publica, no en uno aparte.

**Por qué no SemVer (`MAYOR.MENOR.PARCHE`):** se evaluó. SemVer exige decidir en cada commit si es incompatible/funcionalidad/corrección, y su valor está en comunicar compatibilidad a quien depende de una API -- este juego no tiene consumidores de API. La regla del usuario es mecánica (no requiere juicio), da un número que crece con cada publicación y es fácil de verificar en revisión. No se encontró una ventaja real que justificara cambiarla.

**Estado de partida verificado:** HEAD `f6e9596` e índice tenían `"v 2.1"`; el working tree tenía `"v 2.7"`, que era el WIP ajeno ya documentado (§1.17, §1.25, §1.30) y no una versión publicada. Este commit publica **v2.2**. La línea de versión era la misma que tocaba ese WIP: se reemplazó por `Version.texto()` también en el working tree (la nueva política sustituye a ese número), y es el único cambio aplicado sobre trabajo ajeno; el resto del WIP (FlatLaf, `.classpath`, `Juego.java`, `src/libs/`) sigue intacto y fuera del commit.

**Archivos:** `Version.java` (nuevo), `PnlMenu.java`.

### 1.33 Introducción informativa después de la advertencia, sesión 2026-10-22

**Flujo de inicio normal:** NYDER (§1.29/§1.31) → advertencia (sin cambios) → **introducción** → menú. `finalizarAdvertenciaYMostrarMenu()` ahora llama a `mostrarIntroduccion()`; el menú (y su música, como siempre) llega desde `cerrarIntroduccion()`, único cierre de la introducción ("Omitir", "Ir al menú" en la última pantalla o CTRL+S). El reinicio interno de proceso (`--reinicio-interno`: ganar 1-4, perder, rendirse, volver de Escape) sigue yendo directo al menú, así que la introducción aparece una sola vez por apertura del juego; se puede releer en "Cómo jugar". ESC no hace nada en la introducción (igual que en la advertencia). No usa timers: avanza solo con los botones.

**Pantallas (3):** ANTES DE EMPEZAR (fan-made, no es el original, experiencia propia), CÓMO SE JUEGA (resumen real: oficina con dos puertas, objetivo = todos los coleccionables de la noche antes de quedarse sin baterías, mirar con el ratón en los bordes, DOOR/LIGHT, abrir+luz revela, animatrónico = pierdes una batería, objeto = clic antes de que se cierre, tableta en la barra inferior, todo con ratón; remite a "Cómo jugar") y UN ÚLTIMO CONSEJO (completar la Noche 5, hay una sorpresa). **Decisión:** la introducción no nombra Escape para no revelar la sorpresa que la propia pantalla 3 promete; Escape se explica en "Cómo jugar" solo cuando ya está desbloqueado (§1.34).

**Componente:** `PnlInfo` (nuevo), compartido por las tres pantallas nuevas -- fondo = el GIF animado del menú ya cargado (`CargarImagenes.menu`) oscurecido, tarjeta central translúcida con borde, título en negrita con línea de acento violeta de NYDER, cuerpo en HTML, navegación `Anterior` / indicador `n / N` / `Siguiente` (→ texto final en la última página), botón superior derecho (Omitir/Volver), pestañas opcionales. Fuentes con `Fuentes` (Consolas escalada). Disposición calculada en `doLayout()` a partir del tamaño real del panel (no pasa por `EscalarVista`). Hover: mismo sonido de los menús (`InteraccionBoton`) y resaltado en violeta. Una sola instancia, creada en `ControllerMenu.init()`.

**Gotcha real encontrado y corregido:** un `JLabel` con HTML solo ajusta líneas si se le fija un ancho CSS, y Swing no lo interpreta como píxeles (medido: 400px CSS → 520px en pantalla) -- la primera versión cortaba el texto por la derecha. `doLayout()` ahora calibra el ancho midiendo el `getPreferredSize()` real y corrigiendo por la proporción observada (robusto ante otra escala de pantalla). Las miniaturas de prueba hechas con `Graphics2D.scale(1/3)` muestran solapes de negritas que NO existen a tamaño real (verificado con captura 1:1): no usar capturas escaladas para juzgar texto.

**Archivos:** `PnlInfo.java` (nuevo), `TextosInfo.java` (nuevo), `ControllerMenu.java`.

### 1.34 "Cómo jugar", "Acerca del proyecto" e internacionalización, sesión 2026-10-22

**Menú:** dos botones secundarios nuevos en `PnlMenu` (`btnComoJugar`, `btnAcercaDe`; fuente 45 frente a 75 de los principales, y=724 y y=784 base, terminan en y=838, por encima de la versión en y=865; escalados en `adaptarMenu` igual que el resto), con la misma flecha `>>` y sonido de hover. Las opciones existentes no cambiaron.

**"Cómo jugar" / "How to Play":** `PnlInfo` con 5 pestañas para ir directo a cada tema: Noches (5 noches, tabla real de baterías/coleccionables de `Noche`, condición de victoria/derrota, llamada que bloquea puertas y luces y botón MUTE CALL, progreso guardado, Continuar y Custom Night), Puertas y luces (DOOR/LIGHT, revelación con puerta abierta + luz, contenido variable, cierre automático, animatrónico = susto y una batería menos), Objetos (coleccionables con clic antes del cierre, inventario y descripción, silueta = falta / X = no existe esa noche, baterías extra cuando quedan pocas, máximo 4, Power left), Controles (todo con ratón; bordes, DOOR/LIGHT, clic en objeto, barra inferior = tableta, mantener la barra Rendirse ~2.5s, MUTE CALL; ESC solo en menús -- CTRL+G/P/S no se documentan porque son atajos de depuración), y la quinta pestaña: **Escape** si `PreferenciasJuego.escapeDesbloqueado` (WASD, ratón, CTRL linterna, E interactuar, ESC pausa; llave + puerta de salida, 3 vidas, perder una vida hace perder la llave, latidos de proximidad; se rejuega desde Opciones > Custom Night > ESCAPE -- verificado contra `GameplayScreen` de five_doors_escape) o, si no, **Noche 5** con el consejo y sin revelar nada. Golden Freddy no se menciona (secreto del modo). Cada afirmación se contrastó con `ControllerJuego`, `ControllerCamara`, `ControllerInterfaz`, `Noche`, `Juego` y la imagen real del botón `mutecall.png` ("MUTE CALL"). Volver: botón superior, "Volver al menú" en la última página o ESC.

**"Acerca del proyecto" / "About the Project":** `PnlInfo` de una página con el logo de NYDER (el icono ya escalado del menú, no se recarga el PNG) y el texto pedido (proyecto académico de estudiantes de segundo año de Informática Empresarial, UCR, Recinto de Siquirres, Limón; fan-made inspirado en FNaF con experiencia distinta; recomendación de la Noche 5). **Java/Eclipse verificado:** todo el código de este repo es Java (Swing; JavaFX solo para el video de victoria, parte de Java 8) y es un proyecto Eclipse (`.project`/`.classpath`, sin Maven/Gradle); el texto dice "Todo el juego fue programado en Java, utilizando Eclipse IDE" -- habla de la programación, no de imágenes/audio, que no se hicieron en Java. FlatLaf (WIP ajeno sin commitear) es una biblioteca Java y no cambia la afirmación.

**Internacionalización:** se reutiliza el idioma existente (`PreferenciasJuego.idiomaSeleccionado`, persistido). Los textos largos viven en `TextosInfo` (mismo patrón que `TipoColeccionable.getDescripcion(ingles)`), que lee el idioma en el momento de abrir cada pantalla, así que un cambio en Opciones > Idioma se refleja la próxima vez que se abre. Las etiquetas de los dos botones nuevos se fijan en `ControllerMenu.actualizarTextosIdioma()`, el punto único que ya actualizaba el menú al cambiar de idioma. Ningún texto nuevo está escrito en las clases visuales.

**Pruebas (distinguidas por tipo):**
- *Compilación:* el árbol exacto del commit (extraído del índice con `git checkout-index`, sin el WIP ajeno) compila desde cero con JDK 1.8.0_271, sin errores (solo el aviso preexistente de `Arbol.java`); las pruebas de flujo y de fade se repitieron sobre esas mismas clases y recursos. Publicado como `3da2af2` (v2.2).
- *Nota de versión:* esta corrección de referencia en la documentación se publicó en un segundo commit y, por la regla de §1.32, subió la versión a **v2.3**.
- *Medición automatizada:* las 3+5+5+1 páginas en ES y EN, en 1920×1080, 1920×1440 (4:3) y 2520×1080 (21:9) -- con alto 1080 la escala de fuente coincide con la real de esta pantalla, así que la medición es fiel. Comprueba ancho y alto del cuerpo, títulos, botones y pestañas: primero 50 desbordes (pestaña más larga) → fuente de pestañas 26→22; luego alto del cuerpo en 2 páginas densas → cuerpo 27→26; final **0 desbordes**.
- *Capturas:* hojas de contacto de todas las páginas en ES/EN y capturas 1:1 (texto limpio, margen derecho correcto); menú sobre el fondo real con los botones nuevos en ES/EN, con detección automática de solapes entre todos los componentes visibles (ninguno) y de textos de botón desbordados (ninguno).
- *Flujo real* (`ControllerMenu.init()`, ventana real, `-Duser.home` aislado), ES y EN: NYDER (0ms) → advertencia (~3.1s) → introducción 1/3 (~8.4s); ESC no hace nada en la introducción; Siguiente/Anterior/Siguiente/"Ir al menú" → menú con `introduccionActiva=false`; Omitir en 1/3 → menú; CTRL+S en 2/3 → menú y estable; menú muestra `v 2.2` y los textos en el idioma correcto; Cómo jugar → pestaña 4 → Siguiente → ESC → menú; Acerca → Volver → menú; botón superior de la guía → menú; Opciones → ESC → menú; CTRL+S en Acerca no hace nada. Con `--reinicio-interno` el menú aparece directo (sin NYDER ni introducción).
- *No verificado:* interacción humana real con teclado/ratón físico (los clics se hicieron con `doClick()` y las acciones reales de ESC/CTRL+S del `InputMap`), y la apariencia en monitores con otra resolución física (solo calculada/medida con tamaños de panel equivalentes).

**Archivos:** `PnlInfo.java`, `TextosInfo.java`, `PnlMenu.java`, `EscalarVista.java` (`adaptarMenu`), `ControllerMenu.java`.

### 1.35 Selector de idioma al inicio y advertencia por idioma, sesión 2026-10-22 (v2.4)

**Estado de partida verificado:** HEAD `aea8895` (v2.3), sincronizado con `origin/master`; working tree con el WIP ajeno de siempre (`.classpath`/`Main.java` FlatLaf, `Juego.java` baterías, `src/libs/`, carpeta `if3001…`, 3 PNG extra de `nyder/`) más dos cambios gráficos ajenos nuevos: `NYDER.png` rediseñado ("NYDER GAMES", mismo tamaño 2172×724, modificado 10:51) y **`images/fondos/advertencia_fdaf.png` sin seguimiento** (la advertencia en español, 1672×941, creada 11:05).

**Hallazgo que condicionó el diseño:** la advertencia inicial es una imagen y solo existía en inglés (`warning_fdaf.png`, "WARNING! This game contains…"). Para cumplir "la advertencia en español", se usa `advertencia_fdaf.png` ("¡ADVERTENCIA! Este juego contiene…"), que apareció en el working tree justo para esto. **Decisión deliberada y documentada:** es el único archivo ajeno que se incluye en el commit, porque la funcionalidad pedida lo necesita y generar el texto por código duplicaría un recurso que ya existe. El `NYDER.png` rediseñado **no** se incluye (es una imagen, no afecta al idioma; queda para su autor).

**Flujo de inicio normal:** **selector de idioma** → NYDER → advertencia → introducción → menú. `mostrarAdvertenciaInicial()` ahora abre `mostrarSelectorIdioma()`; al elegir, `aplicarIdiomaInicial()` → `cambiarIdioma(idioma)` → `mostrarPresentacionNyder()`. Como la advertencia (`new PnlWarning()`), la introducción, "Cómo jugar" y "Acerca de" se construyen DESPUÉS de elegir, todas nacen ya en el idioma elegido (nunca se ve una pantalla en un idioma que después cambia). El reinicio interno (`--reinicio-interno`: ganar 1-4, perder, rendirse, volver de Escape; `ControllerMenu(true)`) sigue yendo directo al menú sin selector, NYDER ni introducción.

**Un solo mecanismo de idioma:** `cambiarIdioma(Idioma)` (nuevo) es ahora el único punto que cambia el idioma: asigna `PreferenciasJuego.idiomaSeleccionado`, llama a `actualizarTextosIdioma()` (refresca menú, opciones, idioma, volumen, Custom Night) y `PersistenciaJuego.guardar()` (`~/.fivedoorsatfreddys/config.properties`). Lo usan el selector y los botones de Opciones > Idioma, que antes repetían esos tres pasos cada uno. `PnlWarning()` elige `CargarImagenes.getAdvertenciaEspanol()` (nuevo, carga diferida) si el idioma es español y `CargarImagenes.warning` si es inglés (o si faltara la española).

**Pantalla (`PnlSelectorIdioma`, nueva):** mismo lenguaje visual que `PnlInfo`: fondo = GIF animado del menú oscurecido, logo de NYDER (icono ya escalado del menú), título en negrita, y dos opciones grandes (fuente 60) centradas una junto a la otra, cada una con su endónimo ("Español", "English"). La opción resaltada al entrar es el idioma guardado (blanco + subrayado violeta; la otra en gris). Pasar el cursor por una opción la resalta y el título cambia a ese idioma ("Elige tu idioma" / "Choose your language"); un clic elige y continúa sin confirmación. Teclado: ←/→ y Enter. Un flag impide que un doble clic dispare la secuencia dos veces. CTRL+S no salta el selector (el idioma hay que elegirlo) y ESC no hace nada. Disposición en `doLayout()` relativa al tamaño real. Textos en `TextosInfo` (`tituloSelectorIdioma(Idioma)`, `nombreIdioma(Idioma)`), que aquí reciben el idioma explícito porque todavía no está elegido.

**Pruebas reales** (`ControllerMenu.init()` real, ventana real, `-Duser.home` aislado con `config.properties` propio; `PersistenciaJuego.cargar()` como hace `Main`):
- Preferencia guardada ESPANOL → el selector abre resaltando Español con "Elige tu idioma"; con INGLES guardado abre resaltando English con "Choose your language".
- CTRL+S en el selector: no cambia de pantalla. Hover sobre la otra opción: cambia resaltado y título.
- **Elegir INGLES:** preferencia en memoria y en el archivo = `INGLES`; NYDER → advertencia con la imagen inglesa (1080 px) → BEFORE YOU BEGIN / HOW IT WORKS / ONE LAST TIP con Next/Skip/Go to Menu → menú New Game / Continue / Options / How to Play / About the Project; Opciones "Options"/"Exit Game"; Cómo jugar "Nights … Back to Menu"; Acerca "ABOUT THE PROJECT". Luego Opciones > Idioma > Español: archivo `ESPANOL`, menú y Cómo jugar en español.
- **Elegir ESPANOL:** lo mismo en español, con la imagen de advertencia española (1672 px); luego el cambio a inglés desde Opciones también funciona.
- Reinicio interno (`ControllerMenu(true)`): menú directo, sin selector.
- Capturas del selector en 1920×1080, 1920×1440 (4:3) y 2520×1080 (21:9): composición centrada, sin cortes.
- *No verificado:* un reinicio de proceso real de punta a punta en esta sesión (sí probado en §1.20/§2.38 y el código de `ReiniciadorJuego`/`Main` no cambió); interacción con teclado/ratón físicos.

**Archivos:** `PnlSelectorIdioma.java` (nuevo), `ControllerMenu.java`, `PnlWarning.java`, `CargarImagenes.java`, `TextosInfo.java`, `resources/images/fondos/advertencia_fdaf.png` (recurso ajeno incluido deliberadamente).

### 1.36 Menú: "Cómo jugar" y "Acerca del proyecto" pasan a una fila de navegación secundaria al pie, sesión 2026-10-22 (v2.4)

**Problema:** en §1.34 los dos botones se apilaban bajo Opciones (fuente 45, y=724/784 base), con la misma flecha `>>` que los principales: visualmente competían con Nueva Partida/Continuar/Opciones y parecían añadidos.

**Nueva jerarquía:** los tres botones principales (fuente 75) quedan solos en su columna. "Cómo jugar · Acerca del proyecto" va en una **fila al pie**: empieza en la misma x que la columna principal y comparte el centro vertical de la versión (esquina inferior izquierda), con el logo de NYDER en la esquina opuesta -- un pie de página de tres elementos. Fuente 30, gris tenue (185,185,195), separador "·", resaltado violeta de NYDER al pasar el cursor + el mismo sonido de hover; sin la flecha `>>` (que se diseñó para los botones de 75). Siguen siendo botones de tamaño legible (36 px en 1080p) y con cursor de mano.

**Implementación:** `PnlMenu.ubicarNavegacionSecundaria()` coloca la fila con las coordenadas ya escaladas por `adaptarMenu` (x de `btnNuevaPartida`, centro de `lblVersion`, separación con `EscalarVista.getEscalaX`) y el ancho real del texto, así que se adapta al idioma; `ControllerMenu.actualizarTextosIdioma()` la llama después de fijar los textos (al iniciar y en cada cambio de idioma). Se quitó de `adaptarMenu` el escalado de §1.34 para estos dos botones (ya no tienen bounds base fijos).

**Verificación:**
- *Captura* del `PnlMenu` real sobre el primer cuadro real de `Menu.gif` (1920×1080), ES y EN, con detección automática de solapes entre todos los componentes visibles y de textos de botón desbordados: ninguno. Composición: columna principal intacta, fila al pie alineada con ella y con la línea base de la versión.
- *Cálculo* (no captura: el escalado del menú depende del monitor físico, no del tamaño del panel) con la misma fórmula del código y métricas reales de Consolas al tamaño escalado, para 1280×720, 1366×768, 1600×900, 1920×1080, 2560×1440, 3840×2160 (16:9), 1280×1024 (5:4), 1920×1440 (4:3), 2560×1080 y 3440×1440 (21:9), en ES y EN: la fila nunca toca la versión (hueco ≥110 px) ni el logo (≥561 px) y queda dentro de pantalla.
- *Flujo real*: "Cómo jugar" y "Acerca del proyecto" abren sus pantallas y vuelven al menú (ver pruebas de §1.35 repetidas con este layout).

**Versión:** `Version.MENOR` 3 → 4, el menú muestra **v 2.4**. Este commit agrupa §1.35 y §1.36 (una sola publicación, una sola subida de versión).

**Archivos:** `PnlMenu.java`, `EscalarVista.java` (`adaptarMenu`), `ControllerMenu.java`, `Version.java`.

### 1.37 La presentación de inicio usa `NYDER-GAMES.png`, sesión 2026-10-22 (v2.5)

**Pedido:** usar `NYDER-GAMES.png` en lugar de `NYDER.png` al inicio. **Recurso real verificado:** `resources/images/fondos/NYDER-GAMES.png` (sin seguimiento hasta este commit, 2172×724 con alfa; es el rediseño "NYDER GAMES", idéntico byte a byte al `nyder/NYDER.png` modificado en el working tree desde las 10:51). Se usa desde su ubicación real, sin moverlo ni duplicarlo: `ControllerMenu.RUTA_NYDER` = `/images/fondos/NYDER-GAMES.png`. Mismas dimensiones que el anterior, así que el dibujo centrado con proporción (§1.29) y la secuencia de fade (§1.31) no cambian. `nyder/NYDER.png` queda en el repo sin uso en código (versión publicada intacta; su modificación local sigue sin commitear, igual que antes).

**Pruebas:** compilación desde cero del árbol exacto del commit con JDK 1.8.0_271; flujo real (selector → NYDER → advertencia → introducción → menú) comprobando que el panel de NYDER carga la imagen nueva (2172 px) y que el fade llega a alpha 1.0; menú con `v 2.5`.

**Archivos:** `ControllerMenu.java`, `Version.java` (v2.4 → v2.5), `resources/images/fondos/NYDER-GAMES.png`.

### 1.38 Sonido hover en todos los controles interactivos, sesión 2026-10-22 (v2.6)

**Mecanismo existente (reutilizado, no duplicado):** `InteraccionBoton.reproducirSonidoHover()` -- única instancia reutilizable de `botones/botones_menu.wav` (§2.38), aplica `PreferenciasJuego.volumenGeneral` antes de sonar. No se añadió ningún audio ni reproductor nuevo.

**Auditoría de controles (todos los componentes con `addActionListener`/`addMouseListener` del proyecto):**
- *Ya lo tenían:* menú (3 principales + Cómo jugar/Acerca), diálogo de confirmación, Opciones, Idioma, Volumen, Custom Night (5 noches, Atrás, ESCAPE), selector de idioma inicial, `PnlInfo` (Anterior/Siguiente/Omitir/Ir al menú/Volver y las pestañas -- se crean con `crearBoton`, que ya registra el sonido; al reconfigurar se descartan con `removeAll`, sin acumulación), Game Over (Reintentar/Salir), y la barra de la tableta (abrir/cerrar suena dentro de `mostrarTransicionTablet`).
- *Faltaban, añadidos:* DOOR y LIGHT de ambos lados, MUTE CALL, la barra Rendirse (mantener pulsado), los 10 huecos del inventario y el objeto recogible revelado en la puerta.
- *Excluidos a propósito:* las 6 zonas invisibles de paneo de cámara (son zonas de movimiento, sonarían constantemente), la nariz de Freddy (easter egg oculto: el sonido lo delataría), y los botones de `PnlWin` (nunca se hacen visibles desde §1.11).

**Implementación:** `InteraccionBoton.aplicarSonidoHover(JComponent, BooleanSupplier activo)` -- un listener por componente, registrado una sola vez por quien crea la pantalla (los paneles de partida se crean una vez por proceso/noche). Suena solo en `mouseEntered` (no en movimiento dentro del componente, así que no se repite) y solo si el componente está habilitado, visible y `activo` es verdadero: DOOR/LIGHT no suenan durante la llamada ni con la partida terminada (en esos estados `ControllerJuego` los ignora; nuevo `isLlamadaActiva()`), los huecos del inventario marcados con X no suenan, y el overlay solo suena cuando contiene un objeto recogible (el mismo label muestra los jumpscares). Además, `aplicarHoverYSonido` (botones de menú) ya no suena ni muestra la flecha sobre un botón deshabilitado (p. ej. el menú bloqueado tras el diálogo de "Nueva Partida").

**Fallo corregido:** `mostrarTransicionTablet` llamaba a `obtenerSonidoHover().play()` sin aplicar el volumen vigente; ahora usa `reproducirSonidoHover()`. Los sonidos de clic no se tocaron (el clic de inventario conserva su sonido; error en huecos inexistentes).

**Archivos:** `InteraccionBoton.java`, `ControllerInterfaz.java`, `ControllerCamara.java`, `ControllerJuego.java`.

### 1.39 Explicación de las llamadas en la introducción y estado real de su mecánica, sesión 2026-10-22 (v2.6)

**Texto:** añadido a la página 2 de la introducción ("CÓMO SE JUEGA" / "HOW IT WORKS"), sin pantalla nueva. ES: «LAS LLAMADAS — Durante algunas llamadas puedes recibir información importante sobre el juego. Escucharlas es opcional: no necesitas prestarles atención para jugar. / La partida comenzará únicamente cuando la llamada termine o decidas colgar (COLGAR).» EN: «PHONE CALLS — Some calls may contain important information about the game. Listening is optional: you don't have to pay attention to them to play. / Gameplay will begin only after the call ends or you decide to hang up (MUTE CALL).» Para que cupiera se condensó la página (la referencia a "Cómo jugar" pasó al primer párrafo, se quitaron un párrafo final y un espacio); medición automática con `PaginasInfo` en ES/EN y 1920×1080, 1920×1440, 2520×1080: 0 desbordes; captura 1:1 revisada.

**Mecánica real (leída en `ControllerInterfaz.iniciarLlamada/colgarLlamada` y `ControllerJuego`):** la llamada arranca en `init()` de cada noche (aviso `contestar_telefono.wav` + audio de la noche e idioma); mientras `llamadaActiva`, `ControllerJuego` ignora puertas y luces (que es lo que "la partida comienza" significa: no se puede abrir ni revelar nada); termina por fin natural (timer con la duración real del audio) o al colgar con el botón (el único control existente, no se inventó otro). Ambos caminos hacen `setLlamadaActiva(false)` y reanudan el ambiente; el timer natural comprueba `llamadaColgada`, y colgar detiene el timer, así que no hay doble fin. Mirar con el ratón y abrir la tableta ya estaban permitidos durante la llamada y no cambian. **Único ajuste:** `colgarLlamada()` ahora ignora un segundo clic encolado o un clic tras el fin natural (`llamadaColgada || !isLlamadaActiva()`), que antes reprogramaba el ambiente otra vez (inofensivo pero redundante). El caso de fin de partida (`colgarLlamada(false)` desde `detenerSonidosJuego`) no pasa por ese guard.

**Archivos:** `TextosInfo.java`, `ControllerInterfaz.java`.

### 1.40 Auditoría de `FDAF assets/FDAF HUD` e internacionalización de "Energía restante" y "Noche N", sesión 2026-10-22 (v2.6) — tamaño y posición corregidos en v2.7, ver §1.48

**Carpeta revisada:** `C:\Users\dfarl\Desktop\FDAF assets\FDAF HUD` (existe en esa ruta; no está en OneDrive). 12 PNG creados por el usuario el 2026-10-22, ninguno estaba en el repo: `energiaRestante.png` ("Energía restante:"), `powerLeft.png` ("Power left:", versión nueva distinta del `hud/powerLeft.png` del repo), `noche1-5.png` ("Noche N") y `night1-5.png` ("Night N"). Todos: texto blanco en fuente pixelada sobre fondo transparente de ~2000×700 con mucho margen. Son variantes ES/EN del mismo juego de etiquetas; no hay duplicados ni versiones viejas entre ellos.

**Problema real que resuelven:** el HUD de la oficina (`PnlJuego.lblPowerLeft`) y la copia de la tableta (`PnlTableta.lblCopyPowerLeft`) usaban `hud/powerLeft.png` ("Power left:" incrustado, 137×14) en AMBOS idiomas, además estirado a 194×35 (deformado). La etiqueta de noche de la tableta era texto Java "NOCHE N"/"NIGHT N" en mayúsculas.

**Variantes creadas (derivadas, procedencia documentada):** `resources/images/hud/idioma/energia_restante_es.png`, `power_left_en.png`, `noche1-5_es.png`, `night1-5_en.png` -- recortadas al contorno real del texto (alfa > 40) y escaladas con UN mismo factor 0.25 (área promedio) para que todas conserven el tamaño relativo del original (15-29 KB cada una, frente a 200-300 KB). Herramienta: `Recortar.java` en el scratchpad de la sesión. Los originales del Escritorio no se tocaron.

**Código:** `CargarImagenes.getEtiquetaEnergia()` / `getEtiquetaNoche(n)` eligen la variante por `PreferenciasJuego.idiomaSeleccionado` (caché por ruta) y `ajustarProporcional(...)` escala sin deformar. `EscalarVista.adaptarJuego/adaptarTablet` usan la etiqueta localizada (no `GifEscalado`, que sigue intacto); el label del HUD pasó de 194 a 205 de ancho base (la versión de 225 probada primero quedaba pegada a la barra de la tableta, visto en render). `ControllerInterfaz.actualizarTextosPartida()` pone la imagen "Noche N"/"Night N" en la tableta (texto "Noche N"/"Night N" solo como respaldo si la imagen faltara). Se eliminó el campo `CargarImagenes.powerLeft`; `hud/powerLeft.png` queda en el repo sin uso. La lógica de baterías no cambió. El idioma se lee al crear cada partida (el idioma no puede cambiar a mitad de una noche), así que un cambio en Opciones se refleja en la siguiente partida, y el reinicio de proceso conserva la preferencia guardada.

**Verificación:** render real (`ControllerInterfaz.init` real, 1920×1080, `-Duser.home` aislado) del HUD y la tableta en ES e EN: "Energía restante:"/"Power left:" sin deformar y separados de la barra; "Noche 3"/"Night 3" centrado bajo la batería en la tableta.

**Archivos:** `CargarImagenes.java`, `EscalarVista.java` (`adaptarJuego`, `adaptarTablet`), `PnlJuego.java`, `ControllerInterfaz.java`, 12 PNG nuevos en `images/hud/idioma/`.

### 1.41 Tarjeta "12:00 AM / 1st Night" de la intro de cada noche localizada, sesión 2026-10-22 (v2.6) — RETIRADA en v2.7: las intros nuevas ya traen la tarjeta localizada, ver §1.47

**Hallazgo:** `introNoche1-5.gif` (20.4 s, 12-19 MB; mismos bytes que `FDAF assets/FDAF Empezar/empezarNoche1-5.gif`) terminan con una tarjeta negra "12:00 AM / 1st Night" ("2nd/3rd/4th/5th Night") dibujada dentro del gif, en inglés y con ordinal, desde ~10.8 s hasta el fundido de ~14.9-15.4 s; luego 5 s de negro con un reloj. Se veía igual en español. Metadatos de cuadros medidos con `GifMeta.java` (scratchpad).

**Solución (sin duplicar 10 gifs de 12-19 MB):** `PnlIntroNoche` (subclase de `PnlAplicarImagen`, usada por `ControllerMenu.iniciarSecuenciaIntro`) pinta el gif igual que antes y, en cada repintado, copia la franja central del cuadro ACTUAL (x 560-1040, y 300-500 en coordenadas del gif) y la analiza: si es casi negra (<15% de píxeles no negros, bordes limpios) con exactamente dos líneas de texto claro, es la tarjeta; entonces tapa con negro la segunda línea ("1st Night") y dibuja encima la imagen localizada "Noche N"/"Night N" de §1.40, centrada bajo "12:00 AM", a 1.15× su altura, con opacidad = brillo de "12:00 AM"/235, así sigue el mismo fundido del gif. Sincronizado con el cuadro real (no con un temporizador paralelo). En inglés también se reemplaza ("Night 1" en lugar de "1st Night"), según la terminología pedida. El resto de la intro (periódico "HELP WANTED", destellos, reloj) no se toca. Si el gif no mide 1600×900 (respaldo `empezar.gif`) no se hace nada.

**Verificación:** `ProbarIntroNoche` (scratchpad) compone TODOS los cuadros de los 5 gifs y ejecuta la detección real: la tarjeta se detecta en todos los cuadros con texto visible (Noche 1: #42-#56, de opacidad 1.00 a 0.07; equivalentes en las otras 4), ningún falso positivo en periódico/negro/reloj. Render del panel real a 1920×1080 en ES/EN: "12:00 AM / Noche 1", "Night 3", "Noche 5"; cuadro de fundido con ambas líneas atenuadas a la par.

**Archivos:** `PnlIntroNoche.java` (nuevo), `ControllerMenu.java`.

### 1.42 Botones y rótulos con texto incrustado: PUERTA/LUZ, COLGAR y "CÁMARA DESACTIVADA", sesión 2026-10-22 (v2.6) — en v2.7 la cámara pasa a §1.45 y LUZ se rehízo en §1.46

**Hallazgos (revisión visual sistemática de todas las imágenes y de cuadros compuestos de todos los gifs del proyecto):** `ui/botones/pnlBotonesIzq/Der.fw.png` llevan "DOOR"/"LIGHT"; `hud/mutecall.png` lleva "MUTE CALL"; `gifs/fondos/camara.gif` (fondo de la tableta, 8 cuadros de estática) lleva "-CAMERA DISABLED- / AUDIO ONLY". Todos en inglés en ambos idiomas, sin variante española.

**Botones (variantes generadas, procedencia documentada):** `images/hud/idioma/pnlBotonesIzq_es.png`, `pnlBotonesDer_es.png` ("PUERTA"/"LUZ") y `mutecall_es.png` ("COLGAR"), creadas por `Variantes.java` (scratchpad) a partir de los originales del repo: se tapa el texto inglés con el color de fondo de cada columna de su franja (respeta los degradados y el brillo del borde; en MUTE CALL respeta el relleno semitransparente alfa 0x80) y se escribe el texto español con el color medio del texto original, en la misma caja (Arial Bold para PUERTA/LUZ, Courier New Bold con trazo para COLGAR). Mismo tamaño que el original (101×307, 121×31). `CargarImagenes.getPanelBotonesIzq/Der()` y `getMuteCall()` devuelven la variante española o el original inglés; los usan `PnlJuego` (paneles, creados con cada partida) y `EscalarVista.adaptarJuego` (MUTE CALL). Los originales no se tocaron. La guía "Cómo jugar" y la introducción en español nombran ahora PUERTA/LUZ/COLGAR y "Energía restante".

**Rótulo de la cámara (sin duplicar un gif de 3.7 MB):** `PnlCamaraTableta` (subclase de `PnlAplicarImagen`, usada por `PnlTableta`). En inglés pinta el gif tal cual. En español, sobre el rótulo (x 485-1118, y 58-170 del gif, medido) copia estática del mismo cuadro 320 filas más abajo (sigue animándose) y escribe "-CÁMARA DESACTIVADA- / SOLO AUDIO" en letra pixelada blanca con contorno negro: texto a 22 px sin suavizado (`Fuentes.obtenerSinEscalar`, nuevo, para no depender de la resolución) ampliado 3× con vecino más cercano. Render real de ambos idiomas sobre un cuadro compuesto real del gif revisado.

**Archivos:** `CargarImagenes.java`, `PnlJuego.java`, `EscalarVista.java` (`adaptarJuego`, solo la imagen de MUTE CALL), `PnlTableta.java`, `PnlCamaraTableta.java` (nuevo), `Fuentes.java`, `TextosInfo.java`, 3 PNG nuevos en `images/hud/idioma/`.

### 1.43 Auditoría completa de internacionalización: inventario y correcciones de texto, sesión 2026-10-22 (v2.6)

**Mecanismo existente (reutilizado):** `PreferenciasJuego.idiomaSeleccionado` (persistido en `config.properties`, elegido en el selector inicial y en Opciones > Idioma vía `ControllerMenu.cambiarIdioma`) + ternarios `ingles ? ... : ...` en los controladores + `TextosInfo` para textos largos + `TipoColeccionable.getDescripcion(ingles)`. No se creó un segundo sistema. Los textos nuevos de esta sesión van en `TextosInfo` o junto al ternario existente de su pantalla; las imágenes localizadas, en `CargarImagenes`.

**Inventario (búsqueda de todos los `setText`, `new JLabel/JButton("...")`, `drawString`, `setTitle` + revisión visual de todas las imágenes y gifs):**
- *Correctamente internacionalizado:* menú, Opciones, Idioma, Volumen, Custom Night (noches, nivel, mensaje de ESCAPE bloqueado), diálogo de Nueva Partida, introducción, Cómo jugar, Acerca del proyecto, selector inicial, advertencia (imagen ES/EN, §1.35), Game Over (botones), tableta (Rendirse, descripciones de coleccionables, "Desconocido"), pantallas de carga (`ReiniciadorJuego`, `TransicionEscape`: "EMPIEZA EL JUEGO"/"SOY YO"), audios de llamadas por idioma.
- *Corregido en esta sesión:* "Custom Night" salía igual en español (botón de Opciones y título de su pantalla) → "Noche personalizada" (botón ensanchado 450→600 y título 600→900 base para que quepa); "GAME OVER" no se traducía → "FIN DEL JUEGO" en español (título de la pantalla final de derrota, que ocupa todo el ancho); "Atras" sin tilde (4 pantallas, constructores y ternarios) → "Atrás"; "ultima" → "última" en el mensaje de ESCAPE bloqueado; la guía y la introducción en español nombraban DOOR/LIGHT/MUTE CALL/Power left → PUERTA/LUZ/COLGAR/Energía restante (coinciden con las imágenes de §1.40-§1.42).
- *Imágenes con texto incrustado, ahora localizadas:* Power left (§1.40), Noche N de la tableta (§1.40), tarjeta "1st Night" de la intro (§1.41), DOOR/LIGHT, MUTE CALL y CAMERA DISABLED (§1.42).
- *Nombres propios / técnicos que no se traducen:* "Five Doors at Freddy's" (título del menú y de la ventana), "ESCAPE" (nombre del modo), "v 2.x", "NYDER GAMES", los nombres de los animatrónicos y de "Balloon Boy" en las descripciones, "12:00 AM" de la tarjeta de la intro (formato de hora habitual también en español), la marca de logs `System.out` (no se muestran al jugador), los labels internos "zona uno/dos" de `PnlJuego` (zonas de paneo; su texto nunca se ve, comprobado en el render real del HUD de §1.40).
- *Arte diegético, se conserva:* el periódico "HELP WANTED / Freddy Fazbear's Pizza" de las intros (homenaje al original, no es interfaz), "CELEBRATE!" de los fondos de la oficina, el aviso "THE END" de `win.png` y `FNAFtitle.png` (ninguno de los dos se usa en el código).
- *No usados por el código (no requieren traducción):* botones "Volver a jugar"/"Salir del juego" de `PnlWin` (nunca visibles desde §1.11), `hud/powerLeft.png` (reemplazado por §1.40), `FNAFtitle.png`, `win.png` como pantalla (solo se carga).
- *Fuera de este repositorio:* Five Doors Escape tiene su propio `I18NBundle` completo (§2.31). Su único texto incrustado conocido es el gif "IT'S ME" de Golden Freddy (§2.40), un efecto narrativo homenaje al original; no se tocó.

**Verificación:** `MedirIdioma` (scratchpad) construye el `ControllerMenu` real en cada idioma y lista todos los textos visibles de menú, Opciones, Idioma, Volumen, Noche personalizada, Game Over y el mensaje de ESCAPE bloqueado: en español todo en español y en inglés todo en inglés (salvo los nombres propios de arriba y el endónimo "Español"/"English" del selector), 0 desbordes de ancho. Archivos fuente confirmados UTF-8 (codificación del proyecto Eclipse y `-encoding UTF-8` de `build-portable.bat`).

**Archivos:** `ControllerMenu.java`, `ControllerInterfaz.java` (`aplicarIdiomaGameOver`), `PnlOpciones.java`, `PnlNochePersonalizada.java`, `PnlIdioma.java`, `PnlVolumen.java`, `TextosInfo.java`.

### 1.44 Pruebas de la sesión de UX (§1.38-§1.43) y publicación v2.6, sesión 2026-10-22

Todas con `-Duser.home` aislado (el `config.properties` real del jugador no se tocó), JDK 1.8.0_271, arneses en el scratchpad de la sesión (`harness/src`).
- **Compilación:** working tree completo y, antes del commit, el árbol exacto del índice (sin el WIP ajeno), desde cero.
- **Sonido hover, automatizada (`PruebaHover`, ES y EN): 56/56** en 8 de 9 ejecuciones; la primera ejecución sobre el árbol del commit dio 55/56 y no se registró qué caso falló (solo se mostró el resumen); 6 repeticiones posteriores dieron 56/56. Lo más probable es la ventana fija de 120 ms de la prueba con la JVM recién arrancada, pero no está confirmado. Sobre los componentes reales (`ControllerMenu`/`ControllerInterfaz` reales) se despacha `MOUSE_ENTERED` y se comprueba si el `Clip` compartido de `botones_menu.wav` avanzó. Suena: menú (5), diálogo (2), Opciones (5), Idioma (3), Volumen (3), Noche personalizada (7), selector inicial (2), Cómo jugar (5 pestañas, Siguiente, Anterior, Volver), Acerca (Volver), COLGAR, PUERTA/LUZ ×4 tras colgar, objeto recogible, hueco existente del inventario, barra Rendirse. No suena: `MOUSE_MOVED` dentro del mismo botón, botón deshabilitado, PUERTA/LUZ durante la llamada, COLGAR ya oculto, overlay de jumpscare, hueco con X, Rendirse/PUERTA con la partida terminada. Listeners: abrir/cerrar Opciones 5 veces, cambiar de idioma 2 veces y abrir "Cómo jugar" 3 veces no acumula listeners. Dos clics seguidos en COLGAR: la llamada termina una vez. **Es verificación de que se invoca y arranca el reproductor, no comprobación auditiva:** el audio se reprodujo en esta máquina pero nadie lo escuchó.
- **Llamadas, ejecución real (`PruebaIntroLlamada`):** intro real (gif animado por Toolkit) → partida real. Noche 5 EN: puerta bloqueada durante la llamada, fin natural a los 31.6 s (aviso 12.2 s + llamada 19.4 s), un solo cambio de estado, ambiente reanudado, COLGAR oculto, puerta abre después. Noche 1 ES: igual, fin natural a los 95.9 s.
- **Intro localizada, ejecución real:** capturas del panel en vivo: periódico a los 5 s sin tocar; "12:00 AM / Night 5" y "12:00 AM / Noche 1" en la tarjeta (además de la prueba de todos los cuadros de §1.41).
- **Flujo de inicio (`FlujoIdioma`, ES y EN):** selector → NYDER → advertencia del idioma → introducción (página 2 con las llamadas) → menú `v 2.x`; cambio de idioma desde Opciones persistido y reflejado en menú y Cómo jugar.
- **Textos (`MedirIdioma`, `PaginasInfo`):** 0 desbordes; listas completas de textos visibles por idioma revisadas (§1.43).
- **Capturas reales:** HUD de la oficina (COLGAR / MUTE CALL, Energía restante / Power left), tableta (Noche N / Night N, Energía restante), rótulo de la cámara, paneles PUERTA/LUZ, página de la introducción.
- **No verificado:** interacción con ratón/teclado físicos (Robot no es viable en este entorno, §1.11); escucha humana del sonido; pantallas de Five Doors Escape (otro repositorio, sin cambios).

**Versión:** `Version.MENOR` 5 → 6; este commit publica **v2.6** (§1.32).

### 1.45 Cámara de la tableta: GIF limpio + rótulo aparte por idioma (reemplaza la solución de §1.42), sesión 2026-10-22 (v2.7)

**Recursos nuevos del usuario** (`Desktop\FDAF assets\FDAF HUD`, 15:19-15:23): `camara.gif` (1600×900, 8 cuadros × 70 ms = 560 ms, misma estática que antes pero **sin texto**), `camaradesactivada.png` ("-CÁMARA DESACTIVADA- / SOLO AUDIO") y `cameradisabled.png` ("-CAMERA DISABLED- / AUDIO ONLY"), ambos 2169-2170×725 con fondo transparente; revisados visualmente.

**Implementación:** el `camara.gif` limpio reemplaza al del repo en la misma ruta (`gifs/fondos/camara.gif`; un único gif para los dos idiomas). Los dos rótulos se recortaron con el MISMO rectángulo del original (x 4, y 155, 2160×390: unión de los contornos de ambos) y factor 0.35 → `images/hud/idioma/camara_desactivada_es.png` y `camera_disabled_en.png` (756×137, ~95-99 KB), así comparten lienzo y posición. `CargarImagenes.getRotuloCamara()` elige por idioma; `PnlCamaraTableta` (reescrita, mucho más simple) pinta el gif y encima el rótulo, en el rectángulo base x 468.6, y 51.2, 663.8×119.8: mismo alto (57-170) y centro que el texto que traía incrustado el gif anterior (medido con `Bbox.java` sobre un cuadro compuesto del gif viejo: x 448-1153, y 57-170), conservando la proporción del PNG. Se eliminó la solución de §1.42 (copiar estática y dibujar texto pixelado) y `Fuentes.obtenerSinEscalar`, que solo usaba ella.

**Verificación (captura de render del panel real a 1920×1080):** ES y EN sobre un cuadro real del gif limpio; superposición del rótulo EN sobre el gif VIEJO para comprobar posición: misma altura y centro (la tipografía nueva es algo más estrecha, así que no coincide letra a letra, como era de esperar). Sin duplicados ni recortes.

**Archivos:** `PnlCamaraTableta.java`, `PnlTableta.java`, `CargarImagenes.java`, `Fuentes.java`, `gifs/fondos/camara.gif` (reemplazado), 2 PNG nuevos.

### 1.46 "LUZ" con la misma perspectiva que "LIGHT", sesión 2026-10-22 (v2.7)

**Problema:** en §1.42 "LUZ" se escribió recto, pero el "LIGHT" original de cada panel está en perspectiva (la línea de base y la de arriba suben o bajan a lo largo de la palabra), así que "LUZ" no parecía pegado al panel. Además, el borrado por columnas dejó franjas verticales finas alrededor de "LUZ". "PUERTA" (sobre "DOOR", que en el original está recto) se dejó exactamente igual.

**Medición (`LuzPerspectiva.java`, scratchpad):** bordes superior e inferior del texto "LIGHT" del panel ORIGINAL en su primera letra (L) y en la última (T), ignorando píxeles transparentes (un primer intento los contaba y daba medidas falsas): panel izquierdo L 263-284, T 258-278 (sube 5 px hacia la derecha); panel derecho L 258-278, T 263-283 (baja 5 px). Alto ~20-21 px en ambos extremos.

**Corrección:** solo se regenera la franja de "LUZ" (filas ≥ 250 del panel; "PUERTA" está en las filas 118-152 y no se toca): relleno de fondo suavizado (media móvil de 9 columnas, respetando la transparencia del original) y "LUZ" (Arial Bold, color medio del texto original) mapeado columna a columna entre las dos rectas medidas, centrado en "LIGHT", con supermuestreo 4×4. Revisado en ampliación junto a los originales: "LUZ" sigue la misma inclinación que "LIGHT" en los dos paneles y se lee bien. Limitación: en el panel derecho la zona rellenada queda algo más lisa que la textura ruidosa del panel alrededor (visible solo en ampliación).

**Archivos:** `images/hud/idioma/pnlBotonesIzq_es.png`, `pnlBotonesDer_es.png` (regenerados). El inglés sigue usando los originales.

### 1.47 Intros nuevas de las 5 noches en ES/EN y temporizador con la duración real (reemplaza §1.41), sesión 2026-10-22 (v2.7)

**Inventario de `Desktop\FDAF assets\FDAF Empezar`** (10 gifs, todos abiertos y compuestos cuadro a cuadro, no deducidos por nombre; todos 1600×900):

| Archivo | Idioma / noche | Cuadros | Duración real | Peso | Contenido |
|---|---|---|---|---|---|
| `empezarNoche1.gif` | ES / 1 | 58 | 20.40 s | 14.1 MB | periódico "SE BUSCA PERSONAL / Pizzería de Freddy Fazbear" (nuevo, en español) → tarjeta "12:00 AM / 1ra Noche" → reloj 5 s |
| `startNight1.gif` | EN / 1 | 59 | 20.45 s | 11.9 MB | idéntico byte a byte al `introNoche1.gif` que ya había: periódico "HELP WANTED" → "12:00 AM / 1st Night" → reloj 5 s |
| `empezarNoche2-5.gif` | ES / 2-5 | 20 | 8.65 s c/u | 0.56 MB c/u | sin periódico: estática → tarjeta "2da/3ra/4ta/5ta Noche" 4 s → reloj 3 s |
| `startNight2-5.gif` | EN / 2-5 | 20 | 8.65 s c/u | 0.50 MB c/u | igual con "2nd/3rd/4th/5th Night" |

Las noches 2-5 duran exactamente lo mismo entre sí y en ambos idiomas (8650 ms, misma secuencia de retardos); la Noche 1 es la excepción (ES 20400 ms, EN 20450 ms: el gif inglés tiene un cuadro de 50 ms más). No hizo falta tocar ningún recurso.

**Recursos en el proyecto:** `gifs/fondos/introduccion/introNoche{1-5}_{es,en}.gif`. `introNoche1.gif` se renombró (`git mv`) a `introNoche1_en.gif` (mismos bytes que `startNight1.gif`, sin duplicar); los antiguos `introNoche2-5.gif` (12-19 MB cada uno, periódico repetido en cada noche) se retiraron del repo porque las nuevas versiones los sustituyen. Total de la carpeta: de ~67 MB a ~30 MB. Originales del Escritorio sin tocar.

**Código:**
- `CargarImagenes.rutaIntroPorNoche(i)` elige `_es`/`_en` por `PreferenciasJuego.idiomaSeleccionado`; solo se carga el gif de la noche que se juega (como antes, con `CargarGifs.cargarFresco`), nunca los 10.
- `ControllerMenu.iniciarSecuenciaIntro()`: el `Timer(20000)` fijo se reemplazó por `DuracionGif.leerMilisegundos(ruta, 20000)` (suma real de los retardos de cada cuadro, cacheada). Antes, la Noche 1 se cortaba 0.4 s antes del final y las noches 2-5 nuevas habrían esperado 11 s con el gif ya terminado (o en bucle). CTRL+S sigue saltando por el mismo `finalizarIntroYComenzarPartida()`.
- Al terminar (natural o CTRL+S), `CargarGifs.liberar(ruta)` (nuevo) hace `flush()` del gif de la intro: su hilo "Image Animator" deja de decodificar detrás de la partida (antes seguía vivo hasta la siguiente carga de la misma ruta).
- **Se retiró `PnlIntroNoche` (§1.41):** los gifs nuevos ya traen la tarjeta localizada ("1ra Noche", "2nd Night"...); superponer "Noche N" habría tapado el texto que preparó el usuario. La intro vuelve a usar `PnlAplicarImagen` simple.
- Llamadas, inicio de la noche y resto del gameplay sin cambios.

**Verificación (ejecución real, `PruebaIntros`, perfil aislado, las 10 intros):** cada noche usa su archivo e idioma; timer = 20400 / 20450 / 8650 ms; la partida se crea a los 8.73-8.79 s (noches 2-5) y 21.6 s (Noche 1: el temporizador arranca después de leer el gif de 12-14 MB y su duración en el hilo de Swing, ver §1.52); captura de la tarjeta de las 10 intros en vivo, todas correctas; hilos "Image Animator": 4 antes, 5 durante la intro y 4 un segundo después de empezar la partida (el de la intro se detiene).

**Archivos:** `CargarImagenes.java`, `CargarGifs.java`, `ControllerMenu.java`, `PnlIntroNoche.java` (eliminado), gifs de `introduccion/` (5 ES + 4 EN nuevos, 1 renombrado, 4 retirados).

### 1.48 "Energía restante"/"Power left" y "Noche N" con el mismo tamaño de letra y alineados con la batería, sesión 2026-10-22 (v2.7)

**Causa medida (no supuesta):** en los recortes de §1.40, la primera mayúscula de `energia_restante_es.png` ("E") mide 36 px de alto y la de `power_left_en.png` ("P") 52 px; las "N" de `noche*_es.png` 64-69 px y las de `night*_en.png` 59-71 px (`Linea.java`, scratchpad). Es decir, los originales del usuario no tienen el mismo tamaño de letra, y como §1.40 encajaba cada imagen POR ANCHO en su caja, "Energía restante" (más larga y con letra más pequeña) quedaba ~35 % más chica que "Power left". "Noche N" se centraba en una caja de 200 px base, desalineado del resto de la columna (texto y batería alineados a la izquierda).

**Corrección:** `CargarImagenes.colocarEtiqueta(label, imagen, x, yBase, altoMayuscula)` mide la primera letra de la imagen (filas opacas de sus primeras columnas, cacheado) y la escala para que su MAYÚSCULA mida lo pedido, la alinea a la izquierda en x y pone su LÍNEA BASE en yBase (los descendentes "g" no desplazan el texto). Las posiciones están juntas en `EscalarVista` (base 1600×900): oficina x 66, base 846, mayúscula 15 (justo encima de la batería en y 854); tableta x 1342, "Energía restante"/"Power left" base 383 (encima de la batería en y 393), "Noche N"/"Night N" base 474, mayúscula 24 (debajo de la batería, que termina en y 433). Mismos valores en ES y EN. Se eliminó `ajustarProporcional`, que ya no usa nadie. Las coordenadas del resto del inventario no se tocaron.

**Verificación (render real de `ControllerInterfaz.init` a 1920×1080, `RenderHud`, 5 noches × 2 idiomas):** "Energía restante:" y "Power left:" con la misma altura de letra (18 px en pantalla) y la misma distancia a la batería en oficina y tableta; "Noche 1-5"/"Night 1-5" alineados a la izquierda con la batería y el texto superior, misma altura en las 5 noches; en la oficina "Energía restante:" termina a ~13 px de la barra de la tableta y en la tableta cabe dentro de la columna (justo).

**Archivos:** `CargarImagenes.java`, `EscalarVista.java` (`adaptarJuego`, `adaptarTablet`, constantes nuevas), `ControllerInterfaz.java` (`actualizarTextosPartida`).

### 1.49 Botones del menú que se quedaban resaltados al volver, sesión 2026-10-22 (v2.7)

**Causa (reproducida):** al hacer clic en "Cómo jugar"/"Acerca del proyecto", `vp.setContenido(...)` retira el menú con el cursor todavía sobre el botón, y Swing no le envía `mouseExited` a un componente que deja de estar en pantalla: el color violeta del hover quedaba puesto al volver. El mismo mecanismo dejaba visible la flecha `>>` de los botones principales (Opciones, Nueva Partida...) y el resaltado del botón de salida de `PnlInfo` (que se reutiliza entre aperturas). No era un problema de listeners duplicados ni de visibilidad.

**Corrección:** `InteraccionBoton.restaurarAlOcultar(componente, restaurar)` registra un `HierarchyListener` que, cuando el componente deja de mostrarse (`SHOWING_CHANGED` y `!isShowing()`), restaura su aspecto normal. Se usa en los botones secundarios de `PnlMenu` (color), en `aplicarHoverYSonido` (oculta flecha y elemento adicional) y en los botones de `PnlInfo` (color). Un listener por botón, registrado al crearlo. El hover y su sonido no cambian: si al volver el cursor sí está encima, el siguiente movimiento genera un `mouseEntered` normal y el botón se resalta de nuevo.

**Verificación (`PruebaHoverVuelta`, simulación de eventos sobre el menú real):** 10 vueltas por Cómo jugar, Acerca del proyecto y Opciones. Con v2.6: 32 fallos (botón violeta y flecha visibles al volver). Con la corrección: 72/72; listeners de jerarquía 1→1 y de ratón 3→3 tras las 10 vueltas; un hover posterior sigue resaltando.

**Archivos:** `InteraccionBoton.java`, `PnlMenu.java`, `PnlInfo.java`.

### 1.50 "Cómo jugar" > Noches: números que parecían pegados, sesión 2026-10-22 (v2.7)

**Causa (vista en captura 1:1):** la tabla de noches era una `<table cellpadding='2'>` de 3 columnas; el renderizador HTML de Swing apenas separa las celdas, así que se leía "Noche 14 baterías5 coleccionables" (el número de la noche pegado al de baterías). Además, "Opciones > Noche personalizada" podía partirse dejando "> Noche personalizada" al principio de línea.

**Corrección (solo `TextosInfo`, ES y EN):** cada noche en una línea propia, con la etiqueta en negrita y separadores explícitos: "**Noche 1:** 4 baterías · 5 coleccionables" / "**Night 1:** 4 batteries · 5 collectibles" (fuente monoespaciada, así las columnas siguen alineadas). Las rutas de menú usan espacios no separables (`Opciones&nbsp;&gt;&nbsp;Noche&nbsp;personalizada`, también en la página de Escape). El tamaño de letra no cambió.

**Verificación:** captura 1:1 de la página en ES y EN (las 5 noches claramente separadas); `PaginasInfo`: 0 desbordes en 1920×1080, 1920×1440 y 2520×1080 (un primer intento con un `<br>` extra desbordaba 2 px en 21:9 y se quitó).

**Archivos:** `TextosInfo.java`.

### 1.51 Introducción inicial mucho más corta (reemplaza el texto de §1.33 y §1.39), sesión 2026-10-22 (v2.7)

**Antes:** 3 páginas, 215 palabras (ES) / 241 (EN): párrafos sobre qué es el proyecto, una lista larga de mecánicas, un bloque "LAS LLAMADAS" con título propio y una página de consejo. **Ahora:** 2 páginas, 114 / 112 palabras (medido con `ContarIntro`, texto visible sin HTML):
1. **ANTES DE EMPEZAR / BEFORE YOU BEGIN** — fan-made inspirado en *Five Nights at Freddy's* con gameplay distinto + "llega hasta la Noche 5: al final hay una sorpresa".
2. **CÓMO SE JUEGA / HOW IT WORKS** — "**Objetivo:**" en una frase; 4 viñetas cortas (mirar con el ratón en los bordes; PUERTA + LUZ para ver qué hay detrás; animatrónico = una batería, objeto = clic antes de que se cierre; barra inferior = tableta con el inventario); y al final, en gris y sin título, una sola línea: "Las llamadas pueden darte pistas; escucharlas es opcional. La noche empieza cuando terminan o al pulsar COLGAR." (EN: "...or you press MUTE CALL.").

Se conservan los 5 puntos pedidos (fan-made distinto al original, mecánicas básicas, llamadas útiles pero opcionales, la noche empieza al terminar o colgar, recomendación de la Noche 5); la explicación completa sigue en "Cómo jugar". Navegación igual (Omitir / Siguiente / Anterior / Ir al menú), solo con una página menos.

**Verificación:** capturas 1:1 de las 2 páginas en ES y EN; `PaginasInfo` 0 desbordes en las 3 resoluciones.

**Archivos:** `TextosInfo.java` (`introTitulos`, `introCuerpos`).

### 1.52 Rendimiento: el tirón del selector de idioma, arranque y preparación de la intro, sesión 2026-10-22 (v2.7)

**Metodología (todo medido, arneses en el scratchpad, JDK 1.8.0_271, perfil aislado, misma máquina; "antes" = árbol exacto de v2.6):**
- `PerfInicio`: reproduce `Main` (mismas propiedades y llamadas) y mide desde el arranque de la JVM cada fase, el primer pintado del selector, la respuesta del EDT durante 6 s (sonda cada 5 ms) y el heap tras GC.
- `CuadrosMenu`: pinta el selector cada ~100 ms durante 12 s y cuenta los cambios reales de imagen del fondo animado (Menu.gif, 70 cuadros de 150 ms).
- `PrimerHover`: tiempo en el EDT de 8 entradas del cursor alternando entre "Español" y "English" (despacho + repintado).
- `CargaRecursos`, `DesgloseMenu`, `DesglosePnlMenu`, `ClipDesglose`, `CosteIntro`: desgloses de cada sospechoso.
- Descartado con datos: el EDT no se bloquea en reposo (peor 5-11 ms en 6 s) y el fondo animado no pierde cuadros (≈88-90 cambios en 12 s, ningún hueco > ~200 ms), antes ni después. (Un primer intento de contar cuadros con un `ImageObserver` dio cifras absurdas y se descartó como método.)

**Causa del tirón en el selector (encontrada):** el segundo `clip.stop()` sobre el `Clip` del sonido hover, en Java 8, puede bloquear **369-1213 ms** mientras el dispositivo de audio de Windows termina de arrancar tras el primer `start()` (`ClipDesglose`: no depende de `isRunning/isActive`, que siguen en verdadero al acabar). Como el hover sonaba en el EDT, pasar de una opción del selector a la otra congelaba la ventana: `PrimerHover` en v2.6 = 10-14 ms el primer hover y **544-1260 ms el segundo** (3 corridas). **Corrección:** `InteraccionBoton.reproducirSonidoHover()` reproduce en un hilo daemon propio ("Sonido-Hover"); si ya hay una reproducción pendiente no se encola otra. Después: todos los hovers 4-12 ms; `PruebaHover` sigue 56/56 en ES y EN.

**Arranque (antes de que exista ninguna ventana):**
- El bloque estático de `CargarImagenes` cargaba de forma síncrona imágenes que no usa el selector: FondoJuego.png ~300 ms, inventario ~77 ms, GAME_OVER_ ~76 ms, win ~47 ms, warning ~44 ms. Ahora `getFondoJuego()`, `getInventario()`, `getGameOverFinal()`, `getWarning()` (sincronizados, cargan en el primer uso) y `precargarEnSegundoPlano()`, lanzado con el selector ya creado: un hilo daemon de prioridad mínima que solo crea las `ImageIcon` (no toca componentes Swing); si alguien pide una antes de que termine, espera lo que falte por el mismo lock y recibe la imagen completa. `win.png` ya no se carga (ningún código la usa). `getAdvertenciaEspanol()` pasó a `synchronized`.
- El logo NYDER (1254 px) se reducía a ~144 px con `SCALE_SMOOTH`: 140-176 ms dentro de `adaptarMenu`. Ahora `CargarImagenes.reducirPorPasos` (mitades sucesivas con interpolación bilineal): `adaptarMenu` baja de 164 a 31 ms; comparación visual lado a lado sin diferencias apreciables.
- Lo que queda (~430 ms de `new PnlMenu`, primeras clases de Swing, fuentes) es inicialización normal y no se tocó.

**Intro de la noche:** `DuracionGif` (§1.47) leía la duración con `ImageIO`, que para la intro de la Noche 1 (12-14 MB) bloqueaba el EDT **480-895 ms** al pulsar Nueva Partida/Continuar. Ahora recorre la estructura del gif y suma los retardos sin decodificar los datos de imagen (`ImageIO` queda como respaldo si el archivo no tiene la forma esperada): 8-15 ms. Validado contra `ImageIO` en los **32 gifs** del proyecto: misma duración en todos.

**Antes → después (3 corridas cada uno, ms desde el arranque de la JVM):**

| Medida | v2.6 | v2.7 |
|---|---|---|
| Bloque estático de `CargarImagenes` | 650-734 | 261-297 |
| `new ControllerMenu()` termina | 1422-1560 | 969-973 |
| Primer pintado del selector | 1744-1927 | 1276-1326 |
| EDT con el selector en reposo (peor) | 6-9 | 6-11 |
| 2.º hover en el selector (EDT) | 544-1260 | 4-12 |
| Preparar intro Noche 1 (EDT) | ~0.5-0.9 s | 55 ms (lectura) + 15 ms (duración) |
| Heap tras GC con el selector | 112-120 MB | 122-128 MB (una corrida 199 MB, probablemente la precarga a medio terminar) |

El heap no baja: las mismas imágenes siguen cargándose, solo cambian de momento. No se cambió el tamaño de heap ni la forma de reproducir los gifs. Limitación: la medida "selector listo para interactuar" se aproxima con el primer pintado + respuesta del EDT; el tirón percibido por una persona no se pudo comprobar directamente.

**Archivos:** `InteraccionBoton.java`, `CargarImagenes.java`, `ControllerMenu.java` (lanza la precarga), `EscalarVista.java` (logo), `DuracionGif.java`, `ControllerInterfaz.java`, `PnlTableta.java`, `PnlWarning.java` (getters).

### 1.53 Pruebas de conjunto y publicación v2.7, sesión 2026-10-22

Todas con perfil aislado (`-Duser.home`); el `config.properties` real del jugador no se tocó. Tipos de prueba: render real de paneles (capturas), simulación de eventos sobre los controladores reales, ejecución real con temporizadores y gifs reales, mediciones de tiempo; ninguna interacción con ratón/teclado físicos ni escucha humana del audio.
- **Cámara (§1.45):** render ES/EN sobre el gif limpio; posición comprobada contra el texto del gif anterior.
- **PUERTA/LUZ (§1.46):** ampliaciones de ambos paneles junto a los originales.
- **Intros (§1.47):** `PruebaIntros`, las 10 en ejecución real: archivo, temporizador (20400/20450/8650 ms), paso a la partida, captura de la tarjeta y liberación del hilo del gif. `PruebaIntroLlamada` (Noche 5 EN): intro nueva + llamada que bloquea la puerta + fin natural único + puerta disponible después.
- **Noche N y energía (§1.48):** `RenderHud`, 5 noches × 2 idiomas, oficina y tableta.
- **Menú (§1.49):** `PruebaHoverVuelta` 72/72 (v2.6: 32 fallos), sin acumular listeners.
- **Cómo jugar e introducción (§1.50-§1.51):** capturas 1:1 ES/EN; `PaginasInfo` 0 desbordes; `ContarIntro` 215/241 → 114/112 palabras.
- **Rendimiento (§1.52):** tabla antes/después; `ValidarDuracion` 32/32.
- **Regresión:** `PruebaHover` 56/56 ES y EN; `FlujoIdioma` ES y EN (selector → NYDER → advertencia del idioma → introducción de 2 páginas → menú `v 2.7`, cambio de idioma desde Opciones persistido); `MedirIdioma` 0 desbordes y textos en el idioma correcto.
- **Compilación:** working tree y, antes del commit, el árbol exacto del índice (sin el trabajo ajeno), desde cero con JDK 1.8.0_271.

**Versión:** `Version.MENOR` 6 → 7; este commit publica **v2.7** (§1.32).

### 1.54 Reorganización de los recursos de FDAF: estructura por función y nombres en inglés `snake_case`, sesión 2026-10-22 (v2.8)

**Convención elegida (regla permanente, ver §3 regla 10):** archivos y carpetas de recursos en inglés, `snake_case`, minúsculas, sin espacios ni acentos; formato `<sujeto>_<descripcion>[_<estado>][_<idioma>].<ext>`; idioma siempre como sufijo final `_en`/`_es`; secuencias con 2 dígitos (`night_01`, `knock_01`); estados como palabras (`_open/_closed`, `_on/_off`, `_locked`). Se eligió inglés porque los nombres ya mezclaban ambos idiomas (`powerLeft`, `pasos_freddy`, `JumpscareBonnie`) y los recursos de la parte 3D y los del usuario ya usaban inglés (`camera_disabled_en`, `escape_en`). Las clases, paquetes y variables Java NO se renombraron (siguen en español, como el resto del código).

**Estructura nueva de `FiveDoorsAtFreddys/src/com/fdaf/resources/`** (raíz del classpath, ver `.classpath`):

| Carpeta | Contenido |
|---|---|
| `audio/ambience/` | ambiente de la oficina, zumbido, estática del menú |
| `audio/music/` | música del menú, LesToreadors de la transición a Escape |
| `audio/sfx/` | hover/error de botones, puerta, luces, coleccionable, grito de jumpscare, nariz, respiración |
| `audio/game_over/` | apagón, Freddy acechando, pasos, estática final |
| `audio/random/` | sonidos ambientales aleatorios (`knock_01`...) |
| `audio/phone_calls/` | `phone_pickup.wav`, `night_NN_call_{en,es}.wav` |
| `backgrounds/` | oficina, luces, ventilador, bordes, menú, Game Over, negro, estática |
| `branding/` | `nyder_games_splash.png`, `nyder_logo.png`, `game_icon.png` |
| `collectibles/` (+ `locked/`) | 10 coleccionables y sus siluetas `_locked` |
| `doors/` | puertas abiertas/cerradas y sus animaciones `_opening`/`_closing` |
| `intros/` | `night_NN_intro_{en,es}.gif`, `intro_fallback.gif` |
| `jumpscares/` | `<animatronico>_jumpscare.gif`, `freddy_power_out_jumpscare.gif` |
| `ui/hud/` | `battery_0-4`, `power_left_{en,es}`, `night_NN_{en,es}` |
| `ui/office/` | paneles PUERTA/LUZ, botones de puerta y luz, barra de la tableta, MUTE CALL/COLGAR |
| `ui/tablet/` | estática de la cámara, rótulo `camera_disabled_{en,es}`, punto rojo, inventario, transiciones |
| `ui/screens/` | `warning_{en,es}.png` |
| `videos/` | `victory.mp4` |
| `images/nyder/` | SIN TOCAR: `NYDER.png` (modificado en local por otra persona) y 3 PNG sin seguimiento ajenos |

**Cambios:** 139 archivos movidos con `git mv` (contenido idéntico; historial conservado). 12 sin ningún uso retirados del repositorio tras confirmar que no tienen referencias directas ni construidas por código y copiarlos a `FDAF assets/_archive/fdaf_repo_unused/` (§1.55): `450.png`, `Cusor.png`, `FNAFtitle.png`, `powerLeft.png` (reemplazado en §1.40), `win.png` (sin uso desde §1.11), `phoneguy_espanol/ingles.wav` (reemplazados en §1.11), `win/circus.wav`, `introduccion/AbrirTablet.gif`, `cerrarTablet_generado.gif`, `random/risa_niña.wav` y `random/robotvoice.wav` (ambos excluidos a propósito del grupo de sonidos aleatorios, §1.5). La tabla completa nombre anterior → nombre nuevo está en `docs/catalogo_recursos.md` (§1.56).

**Referencias actualizadas:** todas las rutas literales (12 clases, generado desde la tabla de correspondencias), y a mano las construidas por código: `Sonido` ahora resuelve contra `/audio/` (antes `/sounds/`); coleccionables con prefijo `/` (antes `/images/`) y rutas `collectibles/...` en `TipoColeccionable`; llamadas `phone_calls/night_%02d_call_%s.wav`; jumpscares `"/jumpscares/" + nombre.toLowerCase(Locale.ROOT) + "_jumpscare.gif"`; intros `/intros/night_%02d_intro%s.gif`; etiquetas `/ui/hud/night_%02d_%s.png`. Queda una ruta vieja solo en `CargarSonido` (`/resources/sounds/putdown.wav`): clase sin ningún uso cuya ruta ya estaba rota antes; no se tocó.

**Verificación (`VerificarRecursos`, scratchpad):** recorre todos los literales de recursos de las clases y además genera las rutas dinámicas (5 intros × 2 idiomas sin caer al respaldo, 5 etiquetas × 2 idiomas, 5 llamadas × 2 idiomas invocando el método real, 4 jumpscares, 10 coleccionables + siluetas, getters localizados) y las busca en el classpath compilado: **192 rutas OK, 0 fallos**; los 139 archivos del árbol están referenciados (los únicos sin referencia son los 4 de `images/nyder/`, ajenos). Más pruebas de ejecución en §1.58.

### 1.55 Reorganización de `Desktop\FDAF assets` como espejo de los dos repositorios, sesión 2026-10-22 (v2.8)

**Objetivo:** que cada recurso del juego tenga, en `FDAF assets`, una copia con el MISMO nombre y la MISMA ruta relativa que en el repositorio, y que el original del que se derivó (si es distinto) quede al lado y sea fácil de encontrar.

**Estructura resultante:**

| Carpeta | Contenido |
|---|---|
| `fdaf/` | espejo exacto de `FiveDoorsAtFreddys/src/com/fdaf/resources/` (139 archivos, idénticos por `cmp`) + originales `*_original.*` |
| `escape/` | espejo exacto de `five_doors_escape/assets/` salvo `models/` (21 archivos, idénticos por `cmp`) |
| `source_files/3d_models/` | la antigua `3D Models/` movida ENTERA sin tocar su interior (`Blender_WIP`, `LibGDX_Ready`, `Golden Freddy`...; los `.blend` enlazan texturas con rutas relativas `//..\Freddy\...`). Los `.glb` de Escape son idénticos a los de `LibGDX_Ready`/`Golden Freddy`/`llave.glb`, así que no se duplicaron en `escape/models/` |
| `source_files/sound_library/` | sonidos de biblioteca sin uso en el juego (antes `FDAF Sounds` y wav sueltos), con nombres `snake_case` |
| `_reference/fnaf_assets_enterest/` | el pack de terceros "FNAF ASSETS REORGANIZED BY ENTEREST", movido entero sin tocar |
| `_archive/fdaf_repo_unused/` | los 12 recursos retirados del repo en §1.54 |
| `_archive/old_versions/` | versiones viejas: `game_icon_v1-4`, `tablet_open/close_v1`, `night_intros_backup_2026-09-11.zip` |

**Originales (`*_original.*`, junto al derivado):** etiquetas del HUD `power_left_{en,es}`/`night_NN_{en,es}` (recortadas y escaladas en §1.40), rótulos `camera_disabled_{en,es}` (§1.45), los 4 coleccionables reemplazados por el compañero (cupcake, guitar, hook, mic, normal y `locked`), `golden_freddy_glitch_original` y `golden_freddy_robot_voice_original` (antes de la normalización de §2.42). Así nunca se pierde la fuente de alta resolución al volver a generar una variante.

**Cómo se hizo:** `OrganizarAssets.java` (scratchpad del 2026-10-22) con modo `simular` (escribe el plan sin tocar nada) y `aplicar`. Reglas: archivo del escritorio idéntico byte a byte a uno del juego → se mueve al espejo con el nombre del juego; original distinto declarado a mano (`originales.tsv`) → se mueve al lado con sufijo `_original`; recurso del juego sin copia en el escritorio → se copia desde el repo; el resto → `sound_library` o `_archive`. Nunca sobrescribe (aborta si el destino existe). 228 operaciones: 149 movidas dentro de `FDAF assets` y 79 copias desde los repos. **Nada se borró:** el único archivo nuevo es el manifiesto `FDAF assets/_manifiesto_reorganizacion_2026-10-22.tsv` (operación, origen, destino de cada una, suficiente para deshacerlo). Solo se eliminaron carpetas que quedaron vacías.

**Verificación:** 1013 archivos después = 933 antes + 79 copias + 1 manifiesto; espejos `fdaf/` (139) y `escape/` (21) comparados con `cmp` contra los repos: idénticos.

### 1.56 Catálogo de recursos de los dos juegos, sesión 2026-10-22 (v2.8)

`docs/catalogo_recursos.md` (raíz del repo `five_doors_at_freddys`, 230 líneas; copia en `FDAF assets/CATALOGO_RECURSOS.md`, más `FDAF assets/README.md` que explica la carpeta). Contiene: la convención de nombres (§3 regla 10); la correspondencia repo ↔ `FDAF assets`; una tabla por juego con nombre nuevo, nombre anterior, tipo, idioma, uso en el juego y origen (`_original` si existe); los 12 recursos retirados con su motivo; y los cinco recursos compartidos entre FDAF y Escape (idénticos byte a byte, mismo nombre en los dos). Se generó desde las tablas de correspondencias usadas para los `git mv` (no a mano), así que coincide con lo que hay en disco. **Mantenimiento:** se actualiza en el mismo commit que añada, renombre o retire un recurso.

### 1.57 Guitarra que tapaba la barra de la tableta y micrófono casi invisible en las puertas, sesión 2026-10-22 (v2.8)

**Causas medidas (no supuestas)** con `OverlayPuertas` (scratchpad): `ControllerInterfaz.init` real, paneo real con `derecha(40)`/`izquierda(40)` hasta el tope de cada puerta, `alRevelarColeccionable` real y captura del `PnlJuego` a 1920×1080.
- **Guitarra (§1.26):** overlay de 153×402 base con la esquina en y=506, así que su borde inferior bajaba hasta y=908 base (pantalla: 183×482 en (330,607)). La barra de la tableta está en y=808 base (pantalla (345,969) 960×66). El overlay está por encima de todo en z-order (`setComponentZOrder(overlay, 0)` sobre `pnlComponentes`), y su caja, aunque transparente, recibe los clics: en la puerta izquierda capturaba los 168×66 px del extremo izquierdo de la barra, donde un clic recogía la guitarra en vez de abrir la tableta. En la derecha no cruzaba la barra (que termina en x=1088 base).
- **Micrófono:** no estaba "detrás" de nada (el overlay siempre va encima): `freddy_microphone.png` mide 667×374 y el micrófono ocupa solo x 272-487, y 56-337 (7 % del lienzo); estirado a 80×80 quedaba de ~26×60 px base, deformado y oscuro sobre la puerta oscura. El resto de coleccionables no tienen margen (salvo el garfio, poco).

**Corrección (`ControllerInterfaz`):** `alRevelarColeccionable` recorta el margen transparente de la imagen antes de escalarla (`recortarMargenTransparente`; no cambia nada en las imágenes sin margen). La guitarra mantiene su proporción y su centro horizontal (x 275/1130 + 76.5) pero mide 108×285 base y su borde inferior queda en y=795, por encima de la barra. El micrófono usa 80 de ancho y el alto de su proporción real (80×104 base) y crece hacia arriba para no bajar hacia la barra. Los demás coleccionables y la batería conservan exactamente su caja anterior (350,710 / 1200,740, 80×80 y 80×45). Se eliminó el modo `identificacion == 02` de `mostrarEnOverlay`, que ya no se usaba; `mostrarIconoEnOverlay` recibe ancho y alto.

**Verificación:** `OverlayPuertas`, los 10 coleccionables × 2 puertas: 100 % visibles, **0 puntos de la barra de la tableta capturados por el overlay** (muestreo cada 4 px con `getDeepestComponentAt`), ninguno solapa el panel PUERTA/LUZ, y **recogida real** con un `MOUSE_CLICKED` despachado al componente que está en el centro del objeto: el contador de encontrados sube en los 20 casos y el overlay se oculta. Capturas de la guitarra y el micrófono en ambas puertas: la guitarra queda apoyada en el marco sin tocar la barra; el micrófono se reconoce (rejilla y mango). No verificado con ratón físico.

### 1.58 Pruebas de la reorganización y publicación v2.8, sesión 2026-10-22

Todas con perfil aislado (`-Duser.home`); el `config.properties` real no se tocó. JDK 1.8.0_271, arneses en el scratchpad (`regresion.sh`).
- **Compilación:** working tree y, antes del commit, el árbol exacto del índice extraído con `git checkout-index` (sin el WIP ajeno), desde cero.
- **Rutas (`VerificarRecursos`):** 192 OK, 0 fallos en el working tree y en el árbol del índice (único recurso sin referencia: `images/nyder/NYDER.png`, sin uso desde §1.37).
- **Puertas (`OverlayPuertas`, §1.57):** 20/20.
- **Regresión, ES y EN:** `PruebaHover` 56/56; `PruebaHoverVuelta` 72/72; `FlujoIdioma` (selector → NYDER → advertencia del idioma → introducción → menú `v 2.8`, cambio de idioma persistido); `MedirIdioma` 0 desbordes; `PruebaIntros` las 10 intros con su archivo nuevo (`night_NN_intro_{es,en}.gif`), temporizador 20400/20450/8650 ms y liberación del hilo del gif; `RenderHud` noches 1/3/5 (etiquetas `ui/hud/*`, rótulo de cámara); `PaginasInfo` 0 problemas; `PruebaIntroLlamada` (Noche 5 EN: puerta bloqueada durante la llamada, fin natural único, puerta disponible después).
- **Escape:** ver §2.48 (jar y ejecución real de la distribución local).
- **FDAF assets:** espejos `fdaf/` (139) y `escape/` (21) idénticos a los repos por `cmp` tras los cambios; catálogo idéntico a su copia.
- **No verificado:** ratón/teclado físicos ni escucha humana; el render fuera de pantalla de la tableta no llega a pintar el gif de la cámara (igual en v2.6, es carga asíncrona del gif, no una regresión).

**Versión:** `Version.MENOR` 7 → 8; este commit publica **v2.8** (§1.32).

### 1.59 Cinemática de Rendirse: "dentro del traje", sesiones 2026-10-23 (iniciada, interrumpida y retomada)

**Historial:** el usuario descartó las propuestas sencillas de §4 ("QUIERO CINEMÁTICAS DE VERDAD") y pidió tres escenas reales. La sesión que la empezó se cortó por límite de uso a las 07:01 UTC del 23-10 con el reproductor escrito, conectado a los controladores, la banda sonora generada y 50 de 160 fotogramas renderizados (el render en segundo plano terminó solo después). Se retomó en una terminal nueva reconstruyendo la conversación desde el `.jsonl` y contrastándola con el working tree; nada de lo existente se rehízo.

**Referencias investigadas (no se descargó ni se incorporó nada del juego original):** wiki de FNaF por su API (`Cinematics_(FNaF2)`, `Jumpscares_(FNaF1)`) y sus capturas de referencia. (A) La vista "desde Freddy mirando a los lados" es la alucinación de fin de noche de **FNaF 2** (noches 1-3 en adelante): panorama estático que se recorre con el ratón, a través de la máscara de Freddy, con Bonnie y Chica a los lados; dura unos segundos y termina en negro. (B) El guardia dentro del traje es la **pantalla de Game Over de FNaF 1**: imagen fija (ojos y dientes saliendo por la cabeza del traje) con estática, no una cinemática animada. Aprovechado: la mirada libre limitada, la máscara como marco, los personajes que reaccionan a ti y el "ahora eres un traje" como desenlace; todo con modelos propios.

**Qué ve y oye el jugador (12.3 s):**
1. 0-1.25 s: la oficina (o la tableta) se apaga a tirones con el principio de `power_down.wav`.
2. 1.25-2.2 s: negro; golpe sordo y los clics de los resortes del traje cerrándose.
3. 2.2-10.15 s: 160 fotogramas a 20 fps, vista equirectangular de 160°×53° desde dentro de la cabeza de Freddy en el escenario, con máscara de dos aberturas de ojos, respiración leve, líneas de interferencia y respiración amortiguada. Freddy cruza el comedor por el pasillo (Walk), Bonnie (derecha) y Chica (izquierda) giran la cabeza hacia el jugador con servos, los focos fallan y Freddy se planta delante con los ojos encendidos; todo se apaga.
4. 10.15-10.7 s: estática (`static.gif` + `static.wav`).
5. 10.7-12.3 s: negro con "ahora eres parte del espectáculo" / "you're part of the show now".
6. Reinicio de siempre (`reiniciarJuegoCompleto()` → "Cargando...").

**Mirada:** si el jugador mueve el ratón, manda el ratón (horizontal) durante 1.8 s; si no, la mirada sigue un guion (`GUION_PANEO`: centro → Bonnie cuando gira → Chica → Freddy al frente). Sin el guion, quien no tocaba el ratón no veía ni a Bonnie ni a Chica (medido con capturas).

**Construcción:**
- **Render fuera del juego:** Blender 5.1.2, Cycles en GPU (RTX 4050), 32 muestras, 1800×600, con `pizzeria.glb`, `freddy.glb`, `bonnie.glb` y `chica.glb` de Five Doors Escape y sus animaciones reales (`*_Idle`, `Freddy_Walk`). El juego no carga ningún 3D: solo JPG. Se eligió esto frente a (a) imágenes estáticas con paneo (sin movimiento real) y (b) arrancar Escape/LibGDX desde Swing (acopla los proyectos y tarda en cargar). Scripts para volver a generarlo en `tools/cinematics/surrender/` (raíz del repo): `build_surrender_scene.py` (importa los modelos), `direct_surrender.py` (posiciones, NLA, giros de cabeza con *Damped Track* sobre los huesos `bip_head_bonnie`, `Bone.003_Armature_7` de Chica y `j_head_04` de Freddy, luces que fallan, cámara panorámica en (0, 2.26, −8.05) con rotación (86°, 0, 180°)) y `mix_surrender_audio.py`.
- **Audio** `audio/cinematics/surrender_mix.wav` (12.6 s, estéreo 44.1 kHz): mezcla de sonidos que ya estaban en el repo (`game_over/power_down.wav`, `game_over/freddy_footsteps.wav`, `game_over/static.wav`, `sfx/heavy_breathing.wav` con pasa bajos) y efectos sintetizados en el script (resortes, servos, latido que se acelera, zumbido de 60 Hz, chasquidos). Ningún audio de procedencia externa.
- **Fotogramas** `videos/surrender/surrender_0001.jpg` … `_0160.jpg` (16 MB). Numeración de 4 dígitos por ser una secuencia de 160.
- **Código:** `com.fdaf.util.CinematicaRendirse` (nuevo): un `JPanel` en `POPUP_LAYER` del `JLayeredPane` (no toca los paneles del juego), un `Timer` de 33 ms y un hilo daemon `cinematica-rendirse` que decodifica 8 fotogramas por delante y suelta los ya pasados (nunca en el EDT; con `-Xmx256m` termina bien). `ControllerCamara.rendirse()` llama a `ControllerInterfaz.reproducirCinematicaRendirse()` en lugar de `reiniciarJuegoCompleto()`; ese método marca la partida como terminada y llama a `ControllerJuego.cancelarRevelacionPendiente()` (nuevo) antes de empezar. Sin esto, una revelación de puerta pendiente terminaría en `liberar()` → `verificarFinDeJuego()` y podría disparar `alPerder()`/`alGanar()` por debajo de la escena.
- **Salto:** clic o ESC desde los 400 ms (un clic anterior se ignora para no saltarla sin querer al soltar la barra). Rótulo "Clic o ESC para saltar". `terminar()` es el único cierre: detiene el timer, el hilo y el audio, suelta la caché y llama una sola vez al reinicio.
- **Bloqueo real corregido durante las pruebas:** `Clip.stop()` (Java 8, `AbstractDataLine.stop`) llegó a bloquear el EDT ~2 s al terminar con el clip en su último tramo; la pantalla quedaba congelada antes de "Cargando...". Ahora el audio se detiene y se cierra en un hilo aparte (`cinematica-rendirse-audio`).

**No cambia:** progreso, vidas, noches, el Game Over normal (`alPerder()` no pasa por aquí) ni la victoria.

**Pruebas** (arnés `PruebaRendirse`, partida real con `ControllerInterfaz`, `-Duser.home` aislado, ventana fuera de pantalla para que el ratón real no contamine la mirada; sale antes de que `ReiniciadorJuego` relance el proceso):
- Barra completa con el `Timer` real → capa abierta y `juegoTerminado=true`: OK.
- Fin natural a los 12.36-12.41 s → "Cargando..."/"Loading...": OK en ES y EN, también con `-Xmx256m`.
- Clic (35 ms) y ESC (25 ms) → pantalla de carga; clic a los <400 ms ignorado: OK.
- Hilo de fotogramas terminado y capa retirada tras el fin o el salto: OK.
- `alPerder()` no abre la capa ni el hilo: OK.
- Capturas de 10 instantes revisadas (apagón, Freddy al fondo, Bonnie mirando, Chica mirando, Freddy delante, estática, frase).
- `config.properties` real intacto.
- **No verificado:** escucha humana de la mezcla ni ratón físico.

### 2.49 Victoria de Escape con salida física: depósito, puerta de emergencia, estacionamiento y auto, sesión 2026-10-23

**Pedido:** que el jugador salga físicamente de la pizzería (abrir la salida con la llave, cruzar al exterior, llegar a un estacionamiento, subir a un auto y escapar), no una luz blanca con texto. Sustituye la propuesta A/B/C de §4.2 (se hizo la C).

**Hallazgos que condicionaron el diseño (medidos, no supuestos):**
- La puerta de salida (`puerta_41` → hoja `Object_60`, 0.91 × 1.85 m en x≈-8.87) no da al exterior: da a un **depósito trasero** cerrado. Su pared oeste es la fachada del edificio y es **gruesa** (cara exterior x=-12.61, interior x=-11.897), y un estante la tapaba casi entera.
- Al oeste del edificio hay espacio libre; lo bloqueaban las 4 **paredes límite sintéticas** de `LevelLoader` (1.5 m de grosor).
- La caja del jugador (0.3, 0.9, 0.3) va centrada en los ojos (y=1.6) y llega a 2.5 m: un dintel de 1.85 m también bloquea.

**Recorrido final:**
1. **Puerta de salida (E con la llave):** ya no termina la partida. La hoja gira 92° hacia el depósito en 1.6 s, con un primer tirón corto (el cerrojo cede) y luego el giro pesado. Sonido `exit_door_unlock.wav`. Se retiran los colliders de su hueco, también los del dintel y de las celdas del borde (sin eso el paso medido era de 0.6 m, justo el ancho del jugador). Desde aquí **los animatrónicos dejan de cazar**: se quita `AISystem` y se fuerza su "idle". Así la victoria y una atrapada nunca se solapan. La tensión la llevan el sonido y Freddy.
2. **Depósito:** oscuro, con el letrero **EXIT** rojo encendido en la pared oeste (luz puntual roja).
3. **Puerta de emergencia (E en la barra antipánico):** se abre hacia afuera de golpe, con rebote (`emergency_door_open.wav`). En ese momento el mundo pasa a "exterior":
   - cielo nocturno generado (degradado, estrellas, luna velada);
   - niebla abierta (7-46 m) y sombras recentradas sobre el estacionamiento;
   - faroles encendidos (uno parpadea) y ambiente nocturno en bucle (`night_parking_lot.wav`);
   - la pared límite oeste se retira y la sur se recorta; la valla pasa a ser el límite.
4. **Estacionamiento** (≈14 × 17 m), con:
   - asfalto, vereda con cordón amarillo, líneas y fachada de ladrillo con parapeto;
   - valla con portón y barrera levadiza;
   - 3 faroles, un auto tapado, tachos, bolsas, cajas, caja eléctrica, cámara, reflector y aire acondicionado;
   - letrero de dos caras "FREDDY FAZBEAR'S PIZZA" con letras quemadas, calle con línea discontinua y siluetas de edificios con alguna ventana encendida.

   Freddy aparece de pie en la puerta de emergencia cuando el jugador ya está a más de 3 m afuera y mirando hacia otro lado (nunca a la vista). Su caja musical, que suena desde él, lo delata.
5. **Auto (E en la manija del conductor) → `CinematicaVictoria`, 16.5 s, saltable con ESC/ENTER desde 1 s:**
   - 0-1.3 s: se acerca y abre la puerta (-64°).
   - 1.3-2.9 s: entra y se sienta.
   - 2.9-3.6 s: cierra la puerta y el ambiente queda amortiguado.
   - 3.6-5.0 s: respiración y burro de arranque; el motor prende y se encienden los faros (2 focos y emisivo).
   - 5.4-7.4 s: gira la cabeza hacia la pizzería y Freddy está en la puerta; latido y risa lejana.
   - 7.4-11.4 s: arranca; la barrera se levanta (`gate_barrier_raise.wav`), cruza el portón por una curva de 5.5 m y entra en la calle. Las ruedas giran según la distancia real.
   - 11.4-16.5 s: corte a una cámara fija en la calle; el auto pasa (faros, después luces traseras) y se aleja; la cámara termina en el letrero de la pizzería con empuje lento; negro.

   Barras de cine y "ESC: saltar". Al terminar o al saltarla se detienen los sonidos de la cinemática, se marca `escapado` (único registro de la victoria) y se pasa a `EscapeVictoryScreen` de siempre.

**Construcción:**
- `pizzeria.glb` **no se modifica en disco**. `SalidaExterior.prepararMapa()`, antes de `buildStaticColliders`, convierte en degenerados (índices repetidos) los triángulos verticales de las dos caras de la pared oeste del depósito en z∈[-7.31, -2.81] (16) y los del estante (180). Las tapas de la pared se conservan. En su lugar se dibuja un panel propio con el **mismo material** (`main_material`), el hueco de 0.95 × 2.05 m y el túnel a través de los 0.71 m de pared. Las celdas de colisión de las tapas que caen en el túnel se retiran (juntaban y=0 e y=2.96 en una celda de altura completa).
- Disposición única en `assets/content/maps/exterior.json` (`ExteriorDefinition`, `ContentRegistry.getExteriorDefinition`). La leen tanto el juego (colisiones, cámaras, luces) como `tools/cinematics/escape_victory/build_exterior.py` (Blender 5.1), que construye `models/exterior_parking.glb` en coordenadas del juego (9.3 MB; texturas a 1k solo para suelo/ladrillo/vereda, el resto a 512/256; con todo a 1k pesaba 46 MB). Nodos animados: `emergency_door_leaf` (pivote en la bisagra) y `gate_barrier_arm`.
- Auto `models/car_sedan.glb`: Car de Quaternius (CC0) adaptado con `prep_car.py`: escala, puerta del conductor separada con su bisagra, ruedas con eje propio, interior básico y materiales nocturnos.
- Recursos externos, autores y licencias en `tools/cinematics/escape_victory/LICENSES.md` (todo CC0: Poly Haven y Quaternius); originales en `FDAF assets/source_files/cinematics/escape_victory/downloads/`.
- Sonidos nuevos sintetizados con `synth_victory_audio.py` (sin audio externo); se reutilizan `heavy_breathing`, `heartbeat_fast` y `freddy_laugh`.
- Código:
  - `world/SalidaExterior` (mapa, colisiones, puertas, barrera, auto, luces, cielo, niebla);
  - `cinematics/CinematicaVictoria`;
  - `GameplayScreen`: estado `VICTORIA_CINEMATICA`, `iniciarSalida`, `abrirPuertaEmergencia`, `subirAlAuto`, `congelarAnimatronicos`, Freddy en la puerta, ambiente y texto de ayuda por objeto (`claveTexto`);
  - `CollisionWorld.getStaticColliders()`, `LevelLoader.getParedesLimite()`;
  - `BootScreen` carga exterior, auto y sonidos con la partida: no hay carga al abrir la puerta;
  - `pbrConfig.numSpotLights` pasa de 1 a 6.
- Textos nuevos en `i18n` (ES/EN): `exit.push`, `car.enter`, `cinematic.skip`.

**Pruebas (arneses en el scratchpad, `-Duser.home` aislado, ventana oculta, `Gdx.input` envuelto para no capturar el ratón real):**
- `PruebaVictoria` (`GameplayScreen` real; el jugador se mueve con el `CollisionWorld` real): 19/19 en ES y EN.
  - Sin llave la puerta bloquea; con llave se abre.
  - Se cruza el depósito; la puerta de emergencia cerrada bloquea; la barra es el objetivo de E y la abre.
  - Se llega a la puerta del conductor rodeando el auto; Freddy ya está en la puerta.
  - No se atraviesan la valla oeste, la valla norte, la valla del patio, la barrera del portón ni el auto.
  - La manija es el objetivo de E; la cinemática dura 16.5 s y termina en `EscapeVictoryScreen` con `escapado`.
  - Modo saltar: ESC a los 2 s → `EscapeVictoryScreen` en el mismo cuadro.
  - 23 capturas revisadas: puerta abriéndose, depósito con el EXIT, salida, estacionamiento, Freddy en la puerta visto desde el auto, interior del auto, barrera levantándose, calle y letrero.
- `PruebaArranque` (`FiveDoorsEscapeGame` → `BootScreen` → `GameplayScreen` real): carga completa con los recursos nuevos y una atrapada normal (vida 1 de 3) sigue igual: JUMPSCARE → ESTATICA → CORAZONES_RESPAWN → JUGANDO con 2 vidas, sin cinemática.
- Errores reales encontrados y corregidos durante las pruebas:
  - la hoja de la puerta de salida seguía bloqueando por el dintel;
  - la pared resultó gruesa;
  - el estante era más profundo de lo medido;
  - las tapas de la pared generaban celdas de altura completa;
  - el letrero se leía al revés;
  - el volante tapaba el parabrisas al bajar la cámara.
- **No verificado:** teclado y ratón físicos, escucha humana de los sonidos sintetizados, ni el rendimiento en otra GPU (en la RTX 4050 la cinemática se simuló a ~80 fps sin vsync).

**Limitaciones conocidas:** el auto es *low poly* (estilo del modelo CC0); el interior es básico (asientos, tablero, volante); los faroles no proyectan sombras (las luces sin sombra atravesarían paredes, por eso se encienden recién con la puerta de emergencia). El túnel de la puerta usa el material de pared con UV propias, así que su patrón no continúa exactamente el de la pared original.


### 2.50 Derrota definitiva de Escape: "La última función", sesión 2026-10-23

**Pedido:** una secuencia de terror propia al perder la última vida, distinta del jumpscare y de las pérdidas normales; con ritmo, cámara, animatrónicos y un desenlace claro. Se evaluaron las direcciones de §4.3 y las del pedido (A arrastre, B cámaras de seguridad, C protagonizada por el animatrónico). Se eligió combinar A y C: usa la animación real de caminar de los cuatro, mueve la cámara de verdad y no repite la tableta de FDAF con estática encima (el riesgo de B).

**Cuándo se activa:** solo cuando el jugador es atrapado con `vidasRestantes == 1`. El jumpscare (1 s) es el de siempre. En lugar de `ESTATICA` pasa a `DERROTA_CINEMATICA` y al terminar llama al mismo `resolverFinDeAtrapada()` de siempre (pierde la llave, limpia Golden Freddy, descuenta la vida → `NightGameOverScreen`). Con vidas de sobra no cambia nada: jumpscare → estática → corazones → reaparición.

**Secuencia (12.3 s, `cinematics/CinematicaDerrota`):**
- **0-1 s:** negro; respiración entrecortada (`heavy_breathing`) y latido (`heartbeat_normal` en bucle).
- **1-5.3 s:** abre los ojos (párpados que se abren desde arriba y abajo) **en el piso**. El animatrónico que lo atrapó camina delante con su animación `walk` y lo **arrastra por el pasillo del comedor** hacia el escenario. Es un carril entre mesas en x=-1.9, de z=0.8 a -5.9, verificado con el mapa de colisión real. El avance es a tirones (0.62 m por paso, cada 0.86 s), con `defeat_drag.wav` en cada tirón. La cámara va a 0.30 m del suelo, ladeada, mirando a la espalda del culpable. Los otros dos testigos miran desde el pasillo transversal (z=-2.7). La linterna quedó tirada al principio del carril, apuntando hacia el escenario, y parpadea hasta apagarse. Hay un parpadeo a mitad de camino.
- **5.3-6 s:** negro; lo suben al escenario (`defeat_body_thud.wav`).
- **6-10.6 s:** boca arriba en el escenario, rodeado por los cuatro (el culpable delante, dos a los lados y el cuarto detrás), bajo un único foco que falla. El culpable se inclina sobre él hasta llenar el cuadro (`defeat_servo.wav`).
- **10.6-11.3 s:** el foco revienta (`defeat_light_short.wav`); oscuridad, solo un brillo rojo en los ojos del culpable. Se calla la caja musical de Freddy y **el latido se detiene**.
- **11.3-12.3 s:** negro → `NightGameOverScreen`.

ESC o ENTER la saltan desde el primer segundo. E no la salta, porque es la tecla de juego y podía estar apretada en el momento de morir. Al terminar o al saltarla se detienen sus sonidos y se retiran sus dos luces (foco y ojos). `pbrConfig.numSpotLights` pasa de 6 a 7 por el foco.

**Datos:** coordenadas en `pizzeria.json` / `MapDefinition` (`defeatLane*`, `defeatWitness*`, `defeatStage*`). Sonidos nuevos sintetizados con `tools/cinematics/escape_defeat/synth_defeat_audio.py` (sin audio externo). No se usó ningún modelo ni animación externa: bastaron las animaciones `idle`/`walk` ya retargeteadas de los cuatro (§2.x de animaciones).

**Pruebas (`PruebaDerrota`, arranque real `FiveDoorsEscapeGame` → `BootScreen` → `GameplayScreen`, ventana oculta, `-Duser.home` aislado):**
- Última vida con Freddy como culpable: JUMPSCARE → DERROTA_CINEMATICA → `NightGameOverScreen` a los 12.29 s.
- Última vida con Bonnie: igual, 12.29 s.
- Saltar con ESC a los 2 s: `NightGameOverScreen` de inmediato.
- Atrapada normal con 3 vidas (Chica): JUMPSCARE → ESTATICA → CORAZONES_RESPAWN, sin cinemática.
- 26 capturas revisadas (arrastre con los testigos, escenario con el culpable encima, ojos rojos).
- Problema real corregido con capturas: la primera versión del plano del escenario miraba casi vertical y solo mostraba techo.

**No verificado:** escucha humana de la mezcla y teclado físico.

**Limitación:** no hay una animación de "arrastrar" ni de "inclinarse". El arrastre se resuelve con la caminata real y la cámara a tirones, y la inclinación es un acercamiento del modelo (con la pose `idle`, que tiene los brazos abiertos).


**Publicación de §2.49 y §2.50:** Escape `a583cd7` (victoria) y `5f984bf` (derrota), verificados con `git ls-remote`. La documentación compartida y el catálogo salen en el commit **v2.10** de FDAF (`Version.MENOR` 9 → 10; FDAF no cambia de comportamiento en este commit).

### 1.60 Rendirse: preparación real con barra retro segmentada, sesión 2026-10-23 (v2.11)

**Pedido:** reemplazar la presentación de carga de Rendirse por una barra retro segmentada (diálogo clásico de escritorio: panel gris biselado, segmentos azul oscuro separados), **solo la barra**, sin "Loading..." ni "Cancel". El usuario mencionó una imagen de referencia adjunta que no llegó a la sesión; se diseñó a partir de su descripción.

**Hallazgo:** antes no había ninguna fase de carga *previa* a la escena (los fotogramas se decodificaban sobre la marcha); la única "carga" visible era el "Cargando..." del reinicio del proceso al terminar (`ReiniciadorJuego`).

**Implementación:**
- `com.fdaf.util.BarraRetro` (nuevo): dibuja panel gris (192,192,192) con bisel de 2 px, una ranura hundida y segmentos azul (0,0,128) de ancho fijo con 2 px de separación y un brillo en la parte superior, sin antialias (nítida a cualquier resolución). Tamaño proporcional: 34 % del ancho de pantalla con tope de 520 px, alto = 11 % del ancho.
- **Preparación previa real** en `CinematicaRendirse`: al completar la barra de Rendirse, un hilo `cinematica-rendirse-prep` (nunca el EDT) decodifica los primeros 8 fotogramas y abre el audio (`Sonido`). La barra muestra el progreso real (fotogramas + audio, que pesa como 4 fotogramas) sobre la oficina atenuada; la línea de tiempo de la escena arranca cuando termina. Si termina antes de 260 ms, la barra llena se ve hasta ese mínimo (evita un parpadeo, no alarga una carga real). Medido: 0.8-1.1 s con la CPU ocupada por un render de Blender en paralelo; 24 fotogramas llegaban a 1-3 s y no aportaban nada (el primero no se muestra hasta 2.2 s después), por eso quedaron 8.
- **Pantalla de reinicio tras Rendirse:** `ReiniciadorJuego.reiniciar(vp, siFalla, true)` y `ControllerInterfaz.reiniciarJuegoCompleto(true)` muestran la misma barra en lugar del texto, llenándose con la espera **real** de 900 ms antes de relanzar. Ganar y perder siguen con el "Cargando..." de siempre.

### 1.61 Rendirse v2: cámara más rápida y más presencia de Bonnie y Chica, sesión 2026-10-23 (v2.11)

**Cámara (pedido: el giro lateral era demasiado lento).**
- **Causa medida:** la mirada se interpolaba con un factor fijo por tick (`paneo += (objetivo - paneo) * 0.06` cada 33 ms). Eso equivale a una constante de tiempo de ~0.53 s: del centro al borde tardaba ~1.6 s, y además dependía de los fps.
- **Ahora** (`moverMirada`): suavizado exponencial por tiempo real (`1 - e^(-dt/τ)`, independiente de los fps) más un tope de velocidad.
  - Ratón: τ = 0.08 s, tope de 2.4 anchos de panorama/s.
  - Guion automático: τ = 0.28 s, tope de 1.4/s.
  - El recorrido completo se alcanza con el ratón al 8 % del borde de la ventana. El límite de giro sigue siendo el propio panorama (paneo en [0, 1]).
  - Al dejar el ratón quieto 1.4 s (antes 1.8) el guion retoma desde donde quedó la mirada, sin salto.
- **Medido** (`PruebaRendirse2 camara`, con la CPU cargada por un render en paralelo): del centro al borde, 95 % del recorrido en 283 ms (derecha) y 356 ms (izquierda), frente a ~1.6 s antes. Paso máximo entre fotogramas de unos 0.08-0.13 anchos.

**Bonnie y Chica (pedido: verlas mucho más, con giros de cabeza inquietantes y movimientos irregulares).** Se regeneraron los 160 fotogramas con `tools/cinematics/surrender/direct_surrender.py` v2 (Blender 5.1, Cycles GPU, 32 muestras, 1800×600, ~25 min). Freddy, la duración, la banda sonora y el desenlace no cambian.
- **Posición:** de ±60° a ±42° respecto al jugador, a 1.42 m. Con el paneo al centro los dos quedan a la vista en los bordes, y al girar hacia cada uno llena media pantalla.
- **Giros de cabeza** (*Damped Track* hacia el jugador, influencia hasta 1.0): avanzan a tirones (avanza, se traba, retrocede un poco, completa). Bonnie en los fotogramas 18-40 y Chica en 44-64, antes y más largos que en v1 (50-78 / 72-100). Después, espasmos en los que la cabeza se suelta y vuelve (Bonnie 92-95 y 121-124; Chica 104-107 y 131-134).
- **Tics del cuerpo:** giros bruscos de 3-5° y vuelta (Bonnie 70/110/140, Chica 82/118/143).
- **Luz:** los focos se reapuntaron a sus nuevas posiciones, con un 25 % más de intensidad.
- **Distorsión en el reproductor** (`pintarDistorsion`): en cada tic, ~150 ms de franjas horizontales desplazadas, siempre fuera de la franja central donde quedan las caras (35-62 % del alto), y un velo rojo al 7 %.
- **Guion de mirada nuevo** (`GUION_PANEO`): centro → Bonnie cuando gira → Chica → centro, con miradas breves a cada espasmo y Freddy al frente para el final.
- Los fotogramas v1 quedan archivados en `FDAF assets/_archive/old_versions/surrender_v1/`; el espejo `fdaf/videos/surrender/` y el script en `source_files/cinematics/surrender/` están actualizados. Mismo peso que antes (15 MB) y la misma memoria en ejecución: 8 fotogramas por delante decodificados en un hilo.
- **Alternativas evaluadas:** en lugar de pre-renderizar se podría renderizar en tiempo real con LibGDX dentro de Swing (necesitaría un contexto OpenGL en la ventana de FDAF y acoplaría los dos proyectos) o usar *sprites* 2D animados de los personajes (perdería la cámara panorámica). Se mantuvo el pre-render: memoria acotada y la calidad de Cycles.

**Pruebas:** capturas de 10 instantes en el juego, con el guion de mirada: Bonnie llena la mitad derecha mirando de frente, Chica la izquierda con la cabeza torcida, y la distorsión aparece en los tics sin tapar las caras. Fin natural a los 12.3 s después de la preparación (12.98 s en total). `PruebaRendirse` antigua: todo correcto salvo una comprobación que buscaba el texto "Cargando..." (cambio esperado: ahora hay barra retro, §1.60).

### 1.62 Omitir manteniendo ESC (mecanismo universal, lado Swing), sesión 2026-10-23 (v2.11)

**Pedido:** sustituir el salto inmediato (clic/ESC) por mantener una tecla ~1.2 s con un indicador circular; un toque no debe omitir. Mismo comportamiento en las 4 cinemáticas de los dos juegos (Escape: §2.51).

`com.fdaf.util.OmisionMantenida` (nuevo): `KeyStroke` de ESC *pressed* y *released* en el `InputMap` del panel (la repetición automática del sistema solo vuelve a poner "apretada"); el progreso lo avanza el `Timer` de la propia cinemática con `actualizar(dt)` (sin hilos ni timers propios), se reinicia al soltar y dispara `alOmitir` **una sola vez**. Alternativa: mantener el ratón apretado sobre el propio indicador (esquina inferior derecha; no es un botón). Indicador: texto tenue "Mantén ESC para omitir" / "Hold ESC to skip" y un círculo que se completa. En Rendirse está activo desde la preparación hasta la estática; se quitó el salto por clic en cualquier parte.

**Pruebas** (`PruebaRendirse2`, ventana fuera de pantalla, `-Duser.home` aislado): toque de 0.25 s no omite; mantener 0.7 s y soltar no omite y reinicia; mantener 1.5 s omite una vez; mantener ESC omite a los 1207 ms y seguir manteniéndolo no vuelve a disparar; mantener el ratón sobre el indicador omite a los 1211 ms; fin natural a los 12.38 s; en todos los casos no quedan hilos ni la capa.

### 2.51 Omitir manteniendo ESC en las tres cinemáticas de Escape, sesión 2026-10-23

`cinematics/OmitirCinematica` (nuevo), el mismo contrato que `OmisionMantenida` de FDAF (§1.62): mantener ESC ~1.2 s (o el botón izquierdo sobre el propio indicador, abajo a la derecha). Un toque no hace nada, soltar reinicia el progreso y `actualizar(dt)` devuelve true **una sola vez**. Dibuja un anillo que se completa y el texto `cinematic.skip` ("Manten ESC para omitir" / "Hold ESC to skip"; sin tilde, igual que los demás textos de Escape, porque la fuente por defecto de LibGDX no lo garantiza). Lo avanza el `render()` de cada escena: sin hilos ni timers.
- **Introducción:** omitirla corta la narración (`sonidoEscape.stop(id)`) y pasa por `finalizarIntro()`, el mismo cierre que la reproducción completa: luces de juego, cámara en el inicio del jugador y pantalla de instrucciones (§2.52).
- **Victoria y derrota:** sustituye al salto inmediato con ESC/ENTER. Llevan al mismo estado final que la reproducción completa (`EscapeVictoryScreen` con `escapado`; `resolverFinDeAtrapada` → `NightGameOverScreen`).
- **Fuera de una cinemática, ESC no cambia:** en `JUGANDO` sigue abriendo la pausa (`isKeyJustPressed`). Una ESC que siga apretada al terminar una omisión no dispara la pausa ni cierra `EscapeVictoryScreen` (las dos usan "recién pulsada").

### 2.52 Pantalla de instrucciones previa, con el mundo detenido, sesión 2026-10-23

Nuevo estado `INSTRUCCIONES` entre la introducción y los corazones/"CORRE". Secuencia: carga → introducción (omitible) → instrucciones → corazones → CORRE → juego. Mientras dura no se llama a `engine.update` (IA y animaciones quietas), no se lee el movimiento ni la linterna, y la escena se dibuja con `sceneManager.update(0)` desde el punto de inicio del jugador, oscurecida. Panel centrado con:
- los controles reales, verificados en el código: WASD, ratón, mantener SHIFT, CTRL (izquierdo o derecho), E y ESC;
- el objetivo ("Encuentra la llave y busca la manera de escapar…"), sin decir dónde está la llave ni cuál es la salida;
- "Presiona E o haz clic para comenzar". Hay una gracia de 0.35 s para que una tecla que venía apretada de la intro no la cierre sin querer.

No se repite tras una pérdida de vida (la reaparición va directo a corazones → juego). Textos `instr.*` en ES/EN. El panel de ayuda breve del juego suma "SHIFT: Correr" (`help.sprint`).

### 2.54 Sprint con SHIFT, sesión 2026-10-23

`FirstPersonCameraController`: mantener SHIFT (izquierdo o derecho) multiplica la velocidad por `MULTIPLICADOR_SPRINT = 1.65` (3.5 → 5.78 m/s). Se normaliza antes de escalar, así que no se corre más rápido en diagonal. Sin resistencia ni cansancio (pedido explícito). El balanceo de cabeza ya escalaba con la distancia real recorrida, así que se acelera solo. El movimiento se resuelve en sub-pasos de 0.1 m (§2.53).

**Pruebas (`PruebaMundo`, `GameplayScreen` real con `Gdx.input` simulado):**
- Introducción: un toque de ESC de 0.3 s no la omite; mantenerla 1.3 s → `INSTRUCCIONES` con la narración cortada.
- Instrucciones: Freddy no se mueve en 3 s y W no mueve al jugador; E → corazones → CORRE → `JUGANDO`.
- Sprint: caminando 3.50 m/s y corriendo 5.78 m/s, medidos moviendo al jugador con las teclas. La diagonal W+D se midió con el controlador real: misma velocidad que en recto, caminando y corriendo.

### 2.53 Auditoría de clipping: paredes junto a los marcos y movimiento en sub-pasos, sesión 2026-10-23

**Síntoma reportado:** a veces se veía a través de las paredes y en algunos puntos uno podía pegarse al lateral de una puerta o meterse en la pared.

**Causas investigadas:**
- **`CollisionWorld`:** cajas AABB, resolución eje por eje (X y luego Z). No permite cortar esquinas en diagonal.
- **Paredes:** son planos sin grosor. `LevelLoader` las convierte en celdas de 0.2 m sin erosión.
- **Cámara:** plano cercano a 0.05 m, centrada en el jugador (semiancho 0.3); el balanceo solo desplaza 1.5 cm. Si la colisión es correcta, la cámara nunca llega a 0.05 m de una pared.
- **Normales y caras:** el *culling* está desactivado en el mapa (bobinado invertido, ya documentado), así que no hay paredes "de una cara" que desaparezcan.
- **Causa real:** `collectDoorZones` agrandaba la zona de cada puerta con `DOOR_CLEARANCE_MARGIN = 0.4` en los dos ejes, y las celdas de pared que la tocaban se borraban. A lo largo de la pared eso eliminaba 0.4-0.6 m de colisión a cada lado del marco, sin que la pared visible cambiara. El jugador podía meter el centro (la cámara) dentro de la pared y ver la otra habitación.

**Corrección:**
- El margen grande se aplica solo en el eje del grosor, para despejar las celdas del muro en el vano. A lo largo del ancho, `DOOR_WIDTH_MARGIN = 0.05`. La cortina de Pirate Cove conserva su zona ancha (solo bloquea al jugador).
- El jugador se mueve en sub-pasos de como mucho 0.1 m (`resolverMovimientoEnPasos`). `resolveMovement` solo prueba el destino; con sprint y un frame largo el salto llegaba a 0.58 m, casi el ancho del jugador.

**Medición antes/después** (misma prueba, `PruebaMundo bordes`: jugador en el vano, empujando a lo largo de la pared durante 2 s; la versión anterior se compiló desde `5f984bf` en un *worktree* aparte). Cuánto avanza el centro del jugador a lo largo de la pared, contra el semiancho del vano:

| Puerta | Antes | Ahora | Semiancho del vano |
|---|---|---|---|
| puerta.005 | 0.58 m | 0.19 / 0.39 m | 0.46 |
| puerta.001 | 0.58 m | 0.39 m | 0.46 |
| puerta.004 | 0.64 m | 0.29 m | 0.46 |
| Seguridad derecha | 0.64 m | 0.19 / 0.29 m | 0.44 |
| Seguridad izquierda | — | 0.19 / 0.29 m | 0.44 |

Antes el centro entraba 12-20 cm dentro de la pared; ahora nunca sale del vano. Los pasos siguen libres (las puertas se cruzan, §2.56).

### 2.55 Mesa de Parts and Service retirada, sesión 2026-10-23

La mesa que estorbaba al salir del depósito trasero (Parts and Service) es el nodo `MesaPartAndService_35` (1.27 × 2.14 m, 0.61 m de alto, en x -11.41..-10.14, z -5.82..-3.68), el único con el material `mesaPartAAndService`. Antes ocupaba el centro de la sala y dejaba un pasillo de 0.6 m. Se retira por datos: `pizzeria.json` → `"removeNodes": ["MesaPartAndService_35"]`. `LevelLoader.loadMapScene` vacía sus partes antes de generar colisiones, así que desaparecen la geometría y su collider sin tocar `pizzeria.glb`. Los estantes y el resto de la sala no cambian. Los animatrónicos no usan esa sala (está detrás de la puerta de salida, que solo se abre con la llave y los congela, §2.49). El recorrido de victoria sigue pasando: `PruebaVictoria`, 0 fallos.

### 2.56 Puertas que se abren y se cierran de verdad, sesión 2026-10-23

**Antes:** las hojas de las puertas interiores quedaban cerradas a la vista mientras su vano era atravesable: se pasaba *a través* de la puerta.

`world/PuertasInteriores` (nuevo), con las puertas declaradas en `pizzeria.json`:
- **Abatibles** (`puerta.001`–`005`): giran 88°. Para cada lado del vano se prueban las dos bisagras contra las colisiones reales y se elige la que deja libre el barrido de la hoja. Al abrir, va hacia el lado contrario de quien llega (como una puerta de vaivén).
- **Corredizas** (`puerta Izquierda/Derecha`, las puertas de seguridad de la oficina): suben 1.9 m y bajan, como en FNAF. Se respetó esa mecánica en lugar de forzarles un giro.
- **Funcionamiento:** cualquiera las abre a menos de 1.5 m del centro (jugador o animatrónicos, así ninguno las atraviesa cerradas). Se cierran 0.6 s después de que nadie esté a menos de 2.1 m. Apertura en 0.45 s y cierre en 0.75 s, con *smoothstep*.
- **Colisión:** cerradas tienen un collider fino; al empezar a abrirse se retira (la hoja en movimiento no atrapa a nadie) y vuelve al terminar de cerrarse.
- **Excepciones:** la puerta de salida sigue siendo un caso aparte (llave + cinemática, `SalidaExterior`). `puerta.003` queda detrás de Golden Freddy, cuyo collider ya bloqueaba ese vano; no se cambió.
- **Sonidos sintetizados** (`tools/audio/synth_world_audio.py`): `interior_door_open/close.wav` y `security_door_slide.wav`.

**Prueba** (`PruebaMundo puertas`, con las teclas reales): 6/6 puertas accesibles. Cada una arranca cerrada, llega a apertura 1.00 al cruzarla, se cruza de vuelta y vuelve a 0.00 al alejarse.

### 2.57 Cortina de Pirate Cove animada cuando sale Foxy, sesión 2026-10-23

**Hallazgo:** la cortina es una única malla sin animación (`CORTINA_37/Object_41`, 1528 triángulos, 924 vértices, 2.7 m de ancho en z, en x≈-8.5). No hay nodos separados ni un recurso aparte.

`world/CortinaPirateCove` (nuevo): animación procedural por vértices en la CPU, **solo mientras se mueve** (`mesh.updateVertices`; en reposo no cuesta nada). Las dos mitades se recogen un 80 % contra sus extremos, con pliegues que se marcan al juntarse y un balanceo amortiguado más fuerte en la parte baja (tela suelta). Se abre en 0.65 s y se cierra en 1.4 s.

**Secuencia:** Foxy pasa de `IdleState` a `ChaseState` estando dentro de la cueva → `abrir()`. Foxy recorre ~2 m hasta la cortina a 1.4 m/s, así que la cortina ya está abierta cuando la cruza (medido: termina de abrirse con Foxy en x=-9.8; la cortina está en -8.52). Cuando Foxy lleva 2.5 s a más de 1.5 m de la cortina, se cierra sola. Tras una atrapada todos vuelven a su sitio y se cierra al instante (`respawnJugador`). Un Foxy aturdido no la vuelve a abrir. La colisión no cambia: la zona de la cortina sigue bloqueando solo al jugador (Foxy y la IA pasan).

**Prueba** (`PruebaMundo cortina` + 4 capturas): cerrada con Foxy quieto; abierta antes de que Foxy la cruce; cerrada sola 2.5 s después de que Foxy salió.

### 2.58 Aturdir animatrónicos con destellos de la linterna, sesión 2026-10-23

**Valores centralizados** en `assets/content/balance/flashlight.json` (`FlashlightBalance`, `ContentRegistry.getFlashlightBalance()`): Foxy 5 ciclos, Bonnie 10, Chica 10, 1.5 s cada uno; alcance del haz 11 m, semiángulo 22°, punto objetivo a 1.15 m (pecho). Freddy no tiene entrada: no se aturde y su IA no cambia.

**Qué cuenta como un destello** (`world/AturdimientoLinterna`): un ciclo real encendido → apagado. Mientras la linterna está encendida se marca a cada animatrónico que el haz ilumina **de verdad**:
- dentro del alcance y del cono respecto a la dirección de la cámara;
- con línea de visión libre (`CollisionWorld.lineaDeVisionLibre`), así que iluminar la pared tras la que se esconde no cuenta.

Al apagarla, cada animatrónico marcado suma **uno**. Mantenerla encendida sigue siendo un solo ciclo. No se cuentan fotogramas.

**Contadores:** son independientes por animatrónico y acumulativos (no se pierden por dejar de iluminarlo un momento). Al llegar al umbral, el animatrónico queda aturdido y su contador vuelve a 0. Mientras está aturdido no acumula, así que no se lo puede encadenar indefinidamente. Tras una atrapada todos vuelven a su sitio y los contadores se reinician.

**Integración con la IA:**
- `AISystem` no actualiza la máquina de estados de un aturdido: no se mueve y no puede atrapar (el atrape vive en `ChaseState`).
- El aturdimiento se evalúa **antes** de `engine.update`, así que gana a una atrapada que ocurriera en el mismo fotograma.
- Al terminar recupera su orientación, limpia el detector de atascos y vuelve a "walk" si estaba persiguiendo.
- Con la pausa, las cinemáticas, la victoria (IA congelada, §2.49) o la derrota no se actualiza.
- No usa hilos: todo va en el bucle de juego.

**Reacción** (sin HUD ni números): sacude la cabeza con una amplitud que se apaga, pasa a la animación `idle`, hay un chispazo de luz azulada de 0.22 s en la cabeza (una `PointLightEx` más; `numPointLights` pasa a 6) y suena `animatronic_stun.wav` (chispazo + servos que tartamudean, sintetizado).

**Prueba** (`PruebaMundo aturdir`, linterna con CTRL simulado y apuntando con la cámara):
- 2.5 s de linterna encendida sobre el objetivo = 1 ciclo; 3 ciclos apuntando fuera del haz = 0.
- Foxy no se aturde con 4 ciclos y sí con el 5.º; Bonnie y Chica, con el 10.º; el contador vuelve a 0.
- Durante ~1.5 s (1.48 s medidos desde la comprobación) el animatrónico no se mueve (0.000 m) ni atrapa al jugador pegado a él; después retoma la IA.
- Detrás de una pared real (sin línea de visión): 0 destellos.

### 2.59 Pantalla de carga de Escape, sesión 2026-10-23

**Antes:** texto "Cargando... N%" sobre negro y, al llegar al 100 %, la pantalla congelada mientras se construía el nivel.

**`BootScreen` reescrita:**
- **Fondo:** el comedor visto desde el escenario (fotograma 1 del render de Rendirse, que usa los modelos de Escape), recortado, desenfocado, oscurecido, con viñeta y grano; acercamiento muy lento. No muestra la llave ni la salida.
- **Título:** "FIVE DOORS / ESCAPE" en letras 3D (fuente integrada de Blender, DejaVu) con resplandor rojo y un parpadeo irregular leve de letrero viejo.
- **Progreso:** barra fina con el progreso **real** de `AssetManager` (carga incremental en `update()`). Avanza suave hacia el valor real sin adelantarlo nunca ni retroceder. A su izquierda, un arco que gira muestra actividad aunque un archivo grande tarde.
- **Consejo:** uno distinto en cada arranque (7 en ES/EN, `tip.1`–`tip.7`). No repite el anterior; se guarda en las preferencias propias de Escape (`five_doors_escape`), no en el progreso de FDAF. Todos describen comportamiento real (linterna, sonido espacial, mira + E, correr, perder la llave al ser atrapado, latidos por cercanía, omitir con ESC) sin revelar la ubicación de la llave ni la puerta de salida.
- **Fase final:** al 100 % se dibuja un cuadro con "Preparando la pizzeria..." y recién en el siguiente se construye `GameplayScreen`. Ese tramo (2.4-2.5 s medidos) antes pasaba con la pantalla congelada en "100%".
- **Recursos:** `ui/loading_title.png` (246 KB) y `ui/loading_background.jpg` (78 KB), generados con `tools/loading/build_loading_title.py` (Blender) y `make_loading_assets.py` (PIL).

**Medido** (`PruebaCarga`, arranque real `FiveDoorsEscapeGame` → `BootScreen`):
- Recursos cargados en 11.1-11.9 s y nivel preparado en 2.4-2.5 s, sin bloquear la pantalla durante la carga.
- La barra nunca retrocede.
- Capturas en ES y EN de la carga, la introducción con el indicador de omisión y las instrucciones.

### 2.60 Rendimiento: frustum culling y precalentamiento de shaders, sesión 2026-10-23

**Medición antes de optimizar** (`PruebaRendimiento`: `GameplayScreen` real, 1280×720, sin vsync, `glFinish` por cuadro, `GLProfiler`, 240 cuadros por zona en la RTX 4050): **1544 draw calls en todas las zonas**, también en Parts and Service, donde casi nada es visible, y 21-27 ms por cuadro (37-47 fps). Causa: `SceneManager`/`ModelBatch` no descartan nada fuera de cámara. Se dibujaban siempre, dos veces (pasada de sombras y pasada de color), el mapa entero, el exterior (con la salida cerrada), el auto y los cinco modelos con esqueleto, aunque estuvieran detrás de varias paredes.

**`world/CullingEstatico`** (nuevo):
- Cada `NodePart` estático tiene una esfera en el mundo, calculada una vez. Antes de cada render se apaga (`NodePart.enabled`) lo que queda fuera del frustum, con un margen de 2.5 m para que lo que está justo fuera de cuadro siga proyectando su sombra hacia adentro.
- Los animatrónicos, la llave y Golden Freddy se prueban con una esfera de 1.6 m alrededor de su posición **actual** y se apagan enteros.
- Los nodos animados (puertas, cortina, hoja de salida, hoja de emergencia, barrera) no se tocan.
- Grupos: el exterior y el auto (este sin prueba de frustum, porque se mueve) quedan apagados hasta que se abre la puerta de salida.
- Un único punto de enganche: el `SceneManager` de `GameplayScreen` es una subclase que llama a `culling.actualizar(camera)` en `render()`, así que vale para todos los estados (juego, cinemáticas, jumpscare).

**Precalentamiento de shaders:** al final de `show()` se dibuja todo una vez en un *framebuffer* de 256×144 (570 ms, dentro de la fase "Preparando la pizzeria..."), para que los shaders PBR del exterior no se compilen al abrir la puerta de salida.

**Resultado medido** (mismas zonas, misma máquina):

| Zona | Antes | Después | Draw calls |
|---|---|---|---|
| Comedor | 24.5 ms (41 fps) | 13.2 ms (76 fps) | 1544 → 354 |
| Parts and Service | 22.9 ms (44 fps) | 8.2 ms (122 fps) | 1544 → 131 |
| Pirate Cove | 26.8 ms (37 fps) | 12.8 ms (78 fps) | 1544 → 249 |
| Persecución (Freddy) | 21.5 ms (47 fps) | 11.4 ms (87 fps) | 1544 → 337 |
| Estacionamiento | 21.4 ms (47 fps) | 8.5 ms (118 fps) | 1544 → 491 |
| Cinemática de victoria | 24.4 ms (41 fps) | 13.2 ms (76 fps) | 1545 → 529 |

Heap de Java usado tras GC: 16-20 MB en todas las zonas (los recursos viven en memoria nativa de OpenGL/OpenAL). Regresión tras el cambio: victoria completa y omitida, derrota (última vida, omitida, vida normal) y capturas de la derrota con los cuatro animatrónicos visibles.

### 2.61 Estacionamiento con más detalle, auto completo e iluminación exterior, sesión 2026-10-23

**Exterior (`build_exterior.py`, sección "detalle v2"):**
- Suelo: charcos casi espejo con borde irregular, manchas de aceite en los espacios, parches de asfalto reparado y la calle con la textura `road_damaged`. También rejillas de desagüe (`metal_grate_rusty`), topes y una flecha pintada.
- Fachada trasera: contenedor de basura (con collider, `exterior.json` → `dumpster` + `extraColliders`), cierre metálico "DELIVERIES" (`painted_metal_shutter`), ventanas, tuberías y respiraderos.
- Alrededores: poste con cables, letrero "EMPLOYEE PARKING", cajas (`wooden_crate_02`, `plastic_crate_01`), papeles y vegetación (`shrub_03`, `weed_plant_02`, `nettle_plant`, `dead_tree_trunk_02`). Todo con licencia CC0 de Poly Haven; autores en `tools/cinematics/escape_victory/LICENSES.md`.
- Calcomanías del suelo: sombras de contacto bajo los objetos y "charcos de luz" bajo cada farol. Los focos (`SpotLightEx`) no proyectan sombras en gdx-gltf 2.3.0 (ver §2.62), y estas calcomanías dan el contacto con el suelo que esas luces no producen.
- Resultado: `exterior_parking.glb`, 15.1 MB, 270 mallas, 95 imágenes. Las texturas procedurales intermedias van a una carpeta temporal del sistema: antes se escribían en `assets/models/` y quedaban sueltas sin usarse.
- El letrero de la pizzería tiene cuatro letras apagadas a propósito (`apagadas` en `build_exterior.py`).

**Auto (`prep_car.py` → `assets/models/car_sedan.glb`, 0.4 MB):**
- Detalles: matrículas "FAZ 1987", retrovisores, manijas, escape, limpiaparabrisas, espejo interior y sombra de contacto propia (`car_shadow`, se mueve con el auto).
- Indicadores del tablero (`car_gauges`): panel delgado en la cara trasera del tablero, con una textura emisiva generada (dos esferas con marcas y aguja). `SalidaExterior` multiplica el emisivo por 0.8 al arrancar y por 0 al apagar. La primera versión era una caja de color plano que sobresalía del tablero, y en la cinemática se veía como una placa verde saturada detrás del volante (capturas `vic3` → `vic5`).

**Luces:** faroles con intensidades 62/50/42 (el tercero parpadea) y cono de 72°/34°. Afuera, la luz direccional (luna) sube a 1.7, porque sus sombras son las únicas reales del exterior. El cielo tiene una luna de borde suave. Los faros del auto usan un cono de 34°/18°.

**Diagnóstico falso descartado (arneses):** en las capturas del estacionamiento aparecían franjas claras bajo el auto y manchas de aceite gris claro. Pruebas hechas:
- se descartaron la luna, la linterna, la niebla y el especular;
- con la mancha opaca, el color era oscuro;
- al leer los píxeles, el RGB era oscuro (6,5,4) y el alfa estaba entre 141 y 255.

`Pixmap.createFromFrameBuffer` copia el **alfa de destino** del framebuffer. Las calcomanías mezcladas lo dejan por debajo de 1, y el visor de imágenes compone ese PNG sobre un fondo claro. En pantalla el alfa no se usa, así que **el juego se veía bien**. Las capturas de los arneses se aplanan a RGB antes de revisarlas (`aplanar.py` del scratchpad). Hay que tenerlo en cuenta en futuras revisiones visuales.

**Rendimiento con el detalle v2** (mismo arnés de §2.60, ms por cuadro): estacionamiento 8.5 → 11.6-12.8 (692 draw calls), victoria 13.2 → 15.2-16.0, comedor 13.2 → 14.2, Parts and Service 8.2 → 8.1, Pirate Cove 12.8 → 10.8, persecución 11.4 → 12.2. Todas las zonas siguen muy por debajo de la línea base anterior al culling (21-27 ms). Las diferencias en las zonas interiores están dentro de la variación entre ejecuciones, porque el exterior está apagado mientras no se abre la salida.

**Verificación:** `PruebaVictoria` completa, 0 fallos (dura 16.5 s, la victoria queda registrada). Capturas revisadas: puerta de salida, depósito, puerta de emergencia, estacionamiento, vista hacia la puerta con Freddy, puerta del conductor, interior con los indicadores encendidos, salida del auto y plano final con el letrero.

### 2.62 Auditoría técnica de ambos proyectos, sesión 2026-10-23

Datos medidos en esta sesión salvo donde se indica "cálculo". Coste: bajo (< 1 h), medio (una sesión), alto (varias). No se implementó nada de esta lista salvo lo marcado como hecho.

**Prioridad alta**

| # | Proyecto | Hallazgo | Recomendación | Coste | Riesgo |
|---|---|---|---|---|---|
| A1 | Escape | `freddy.glb` pesa 114.1 MB, de los que 110.3 MB son **nueve texturas PNG de 4096×4096**. Cálculo: en VRAM sin comprimir son 9 × 64 MB = 576 MB (≈ 768 MB con mipmaps), más que todo el resto de modelos juntos. Es el candidato principal para los 11-12 s de carga de recursos (`PruebaCarga`), pero no se midió su parte exacta. | Reducir a 2048² (÷4 en memoria) o 1024² las que no se ven de cerca, y pasar a JPEG las de color sin alfa. Medir antes y después con `PruebaCarga` y revisar el jumpscare de Freddy de cerca. | Bajo | Bajo |
| A2 | Ambos | Las pruebas automáticas (arneses `Prueba*` de Escape, `regresion.sh` y los arneses Swing de FDAF) viven fuera de los repositorios, en el scratchpad de cada sesión. Cada sesión tiene que reconstruirlos, y no hay forma de que alguien más los ejecute. | Llevar los arneses estables a `tools/tests/` en cada repo, con un script de ejecución (`-Duser.home` aislado, ver §3). En Escape, como tarea Gradle con ventana oculta. | Medio | Bajo |

**Prioridad media**

| # | Proyecto | Hallazgo | Recomendación | Coste | Riesgo |
|---|---|---|---|---|---|
| M1 | Escape | `pizzeria.glb`: 1 618 244 vértices en 79 mallas (74.7 MB, de los que 12 MB son imágenes). Con el culling de §2.60 ya no se dibuja entero, pero sigue siendo el grueso de la pasada de sombras. | Decimar en Blender las mallas densas que no forman paredes (mobiliario, decoración). Hay que conservar las mallas que generan colisiones (`LevelLoader`) o volver a verificar `PruebaMundo bordes/puertas`. | Medio | Medio |
| M2 | Escape | `audio/music/freddy_music_box.wav` (18.4 MB) se carga como `Sound` (todas las constantes `SONIDO_*` se encolan así), lo que lo decodifica entero en memoria. | Cargarlo como `Music` (streaming) o convertirlo a OGG. | Bajo | Bajo |
| M3 | Escape | La preparación del nivel varía entre 4.0 y 9.2 s de una ejecución a otra con los mismos datos. El culling (170 ms) y el precalentamiento (570-664 ms) explican solo una parte. | Medir por fases dentro de `GameplayScreen.show()` (colisiones, escenas, luces) antes de optimizar nada. | Bajo | Bajo |
| M4 | Escape | `GameplayScreen.java` tiene 3061 líneas (el 36 % del código de `core`). Las cinemáticas, las puertas, la cortina, el aturdimiento y el culling ya salieron a clases propias, pero el resto sigue ahí. | Extraer solo al tocar una zona (por ejemplo, el HUD o el flujo de vidas), nunca como refactor aparte. | Alto | Alto |
| M5 | Escape | En el estacionamiento hay 692 draw calls (270 mallas y 95 imágenes en el exterior). | Unir en Blender las mallas estáticas que comparten material (vallas, topes, papeles, vegetación). | Medio | Bajo |

**Prioridad baja**

| # | Proyecto | Hallazgo | Recomendación | Coste | Riesgo |
|---|---|---|---|---|---|
| B1 | Escape | `golden_freddy.glb`: 254 mallas y 267k vértices, sin animación. | Unir mallas por material. Solo se ve en un evento, así que el beneficio es pequeño. | Bajo | Bajo |
| B2 | Escape | Todo el audio está en WAV (41 MB en `assets/audio`). | OGG para música y ambientes largos. | Bajo | Bajo |
| B3 | FDAF | `ControllerInterfaz.java` (1635 líneas) y `ControllerMenu.java` (908) concentran la lógica. Son frágiles ante ediciones parciales (§3, reglas 1-2). | No refactorizar sin una necesidad concreta. Seguir con bloques de reemplazo verificados. | Alto | Alto |
| B4 | FDAF | Los recursos ocupan 266 MB, de los que 144 MB son audio WAV (`escape_transition_les_toreadors_remix.wav` pesa 53 MB). Java 8 / `javax.sound` no decodifica OGG ni MP3 sin bibliotecas externas. Los archivos grandes ya se reproducen en streaming (`Sonido`, camino `SourceDataLine`), así que no hay riesgo de OOM. | Si el tamaño del JAR importa, bajar la transición a 22 kHz o mono tras escucharla. | Bajo | Medio (calidad) |
| B5 | Ambos | Las capturas de los arneses guardan el alfa del framebuffer y producen falsos defectos visuales (§2.61). | Aplanar a RGB al guardar en los arneses que se lleven al repo (A2). | Bajo | Ninguno |

**Limitaciones conocidas (no se pueden resolver sin cambiar de biblioteca o de enfoque)**
- gdx-gltf 2.3.0 solo proyecta sombras de luces direccionales (`DirectionalShadowLight`). Los faroles, los faros y la linterna (`SpotLightEx`) no hacen sombra. Mitigado con sombras de contacto y charcos de luz (§2.61).
- FDAF reinicia relanzando el proceso (`ReiniciadorJuego`, `ProcessBuilder`). Depende de encontrar el ejecutable de Java y tiene un plan alternativo (`siFalla`). `LanzadorEscape` usa el mismo mecanismo.
- `EscalarVista.java` (FDAF) está congelado (§1.7).
- La imagen de referencia de la barra retro (§1.60) no llegó a la sesión. La barra se diseñó a partir de la descripción escrita.

**Recursos:** todo lo añadido en esta sesión es CC0 (Poly Haven, autores en `LICENSES.md`) o se generó con scripts del repo (sonidos de `tools/audio/synth_world_audio.py`, pantalla de carga de `tools/loading/`, texturas procedurales del exterior y del auto). No se usó ningún recurso de procedencia dudosa.

**Hecho en esta sesión a raíz de la auditoría:** culling y precalentamiento (§2.60), indicadores del auto rehechos, texturas temporales del exterior fuera de `assets/` y diagnóstico de las capturas (§2.61).

### 2.63 Puertas que giraban sobre el picaporte: bisagra en el canto opuesto a los pomos, sesión 2026-10-23 (tarde)

**Contexto de la sesión:** la sesión anterior recibió este mismo pedido (16 puntos: puertas, derrota, persecución de Freddy, colisión de Freddy, salpicadero, faroles, estrella, agradecimiento, destello NYDER, sonido de las líneas blancas, GIF del menú, noches personalizadas bloqueadas, atascos con mesas, foco de la ventana, umbrales de aturdimiento y auditoría) y se cortó mientras analizaba la geometría de las puertas, sin cambios publicados. Se retomó reconstruyendo la conversación desde el `.jsonl`; el único cambio que había dejado en disco era un `Gdx.app.log` de diagnóstico en `PuertasInteriores`, que se integra aquí.

**Auditoría por puerta (pomos medidos en la malla de cada hoja; los pomos son lo único que sobresale de las dos caras, a 0.88-0.98 m de altura):**

| Puerta | Eje ancho | Pomos en | Bisagra antes | Bisagra ahora | Estado |
|---|---|---|---|---|---|
| `puerta.001_42` (baño) | z -4.755..-3.846 | -3.94 (máx.) | -4.755 (mín.) | -4.755 | ya estaba bien |
| `puerta.002_43` (baño) | z -3.423..-2.514 | -2.61 (máx.) | -3.423 | -3.423 | ya estaba bien |
| `puerta.003_44` (baño, tras Golden Freddy) | z 0.070..0.980 | 0.89 (máx.) | 0.070 | 0.070 | ya estaba bien |
| `puerta.004_45` | x 6.747..7.656 | 6.84 (mín.) | **6.747 (sobre los pomos)** | 7.656 | **corregida** |
| `puerta.005_46` | z 5.053..5.962 | 5.15 (mín.) | **5.053 (sobre los pomos)** | 5.962 | **corregida** |
| `puerta_41` (salida, `SalidaExterior`) | z -6.779..-5.870 | -6.69 (mín.) | -5.87 (`exterior.json`) | sin cambios | ya estaba bien |
| `puerta Izquierda/Derecha` (seguridad) | — | sin pomos | corredizas | sin cambios | no aplica |

**Causa:** `elegirBisagras` (§2.56) elegía la bisagra **solo** por el barrido libre de la hoja. En las cinco abatibles los dos cantos daban 0 choques y ganaba el primero probado (el extremo mínimo). En las tres del baño coincidía por casualidad con el canto correcto; en la 004 y la 005 ese extremo es justo el de los pomos, así que la hoja giraba sobre el picaporte. No era un problema de orientación del modelo ni del pivote de la animación: la rotación se aplicaba bien, pero alrededor del canto equivocado.

**Corrección (`PuertasInteriores.cantoPicaporte`):** lee los vértices de la hoja (posiciones de la malla con la transformación global del nodo), se queda con los que están a la altura de la mano (0.7-1.2 m) y, de ellos, con los que más sobresalen del plano de la hoja (más del 60 % del máximo: los pomos llegan a ~14 cm del centro y la placa a ~3.5 cm). Su posición media a lo ancho da el canto del picaporte (−1 mínimo, +1 máximo, 0 si no se reconoce). `elegirBisagras` prueba solo el canto opuesto. Si no se reconoce ningún picaporte, se mantiene el criterio anterior (barrido libre). El resto no cambia: giro de 88°, apertura hacia el lado contrario de quien llega si ese lado está libre, collider fino con la puerta cerrada y retirado mientras se mueve, sonidos y tiempos. El log de arranque indica para cada puerta el canto del picaporte y la bisagra elegida.

**Pruebas:**
- `PruebaMundo puertas` (teclas reales): **6/6** puertas se abren al llegar, se cruzan en los dos sentidos y se cierran al alejarse.
- Log de arranque: las tres del baño conservan **exactamente** la misma bisagra que antes. La 004 y la 005 la cambian al canto opuesto. Los dos lados de barrido siguen libres en las cinco, así que la hoja abierta no se mete en ninguna pared.
- Capturas (modo nuevo `PruebaMundo vista`: cada hoja entreabierta al 55 % hacia cada lado, vista a 2.6 m): en las cinco abatibles el pomo queda en el canto que se mueve y la hoja gira pegada al marco del lado contrario. La 004 y la 005 se revisaron desde los dos lados.
- **No verificado:** recorrido con teclado y ratón físicos.

**Archivos:** `world/PuertasInteriores.java`.

### 2.64 Umbrales de aturdimiento: Foxy 3, Bonnie 7, Chica 7, sesión 2026-10-23 (tarde)

**Cambio de diseño pedido:** `assets/content/balance/flashlight.json` pasa de Foxy 5 / Bonnie 10 / Chica 10 a **Foxy 3 / Bonnie 7 / Chica 7** ciclos válidos. La duración sigue siendo 1.5 s para los tres. No se tocó código: los umbrales ya estaban centralizados en ese JSON (§2.58).

**Revisión de la mecánica contra lo pedido (sin cambios, ya cumplía):**
- Un destello es un ciclo encendido → apagado en el que el haz iluminó de verdad al animatrónico: alcance de 11 m, cono de 22° y `lineaDeVisionLibre`. No se cuentan fotogramas, y mantener la linterna encendida cuenta un solo ciclo.
- Los contadores son independientes por animatrónico.
- Al llegar al umbral el animatrónico queda quieto 1.5 s, sin poder atrapar (`AISystem` no actualiza su FSM). Sacude la cabeza, hay un chispazo azul y suena `animatronic_stun.wav`. Después vuelve la IA y el contador queda en 0.
- Mientras está aturdido no acumula ciclos, así que no hay aturdimiento infinito.
- Todo corre en el bucle de juego, sin hilos. Con la pausa, las cinemáticas, la victoria y la derrota no se actualiza.

**Prueba (`PruebaMundo aturdir`, linterna con CTRL simulado, con los umbrales nuevos en el arnés):** 22/22.
- Para los tres: 2.5 s encendida sobre el objetivo cuentan 1 ciclo, y los ciclos fuera del haz no cuentan.
- Foxy no se aturde con 2 ciclos y sí con el 3.º. Bonnie y Chica, con 6 no y con el 7.º sí. En cada aturdimiento el contador vuelve a 0.
- Aturdido 1.48 s medidos: 0.000 m recorridos y sin atrapar al jugador pegado a él. Después retoma la IA.
- Detrás de una pared (sin línea de visión): 0 destellos.

### 2.65 Atascos junto a las mesas: la caja del jugador bajaba del escenario dentro de un tablero, sesión 2026-10-23 (tarde)

**Síntoma reportado:** caminando muy cerca de una mesa, el jugador quedó atrapado por las colisiones.

**Auditoría:**
- Caja del jugador: AABB (0.3, 0.9, 0.3) centrada en los ojos, de 0.7 a 2.5 m.
- Mesas y sillas: el mobiliario fusionado del comedor se rasteriza en celdas de 0.1 m con erosión de 0.03 m (§2.37). Las celdas conservan la altura real, así que las sillas bajas se pisan y los respaldos y tableros bloquean.
- Resolución: eje por eje (X y luego Z), en sub-pasos de 0.1 m (§2.53). Con el sprint, la diagonal sale normalizada y no cambia nada.
- Con este esquema, si la posición de partida está libre, siempre queda al menos un camino de vuelta. Un atasco total solo es posible si el jugador **ya está dentro** de un collider. Faltaba saber cómo llegaba ahí.

**Medición:**
- `PruebaMundo mesas`: 40 caminatas aleatorias de 400 cuadros por el comedor (WASD, diagonales, sprint y giros), 882 m en total. 0 atascos. Por sí solo no lo reproducía.
- Volcado de los 11 432 colliders y espacio de configuración del jugador (`cspace.py`, rejilla de 2 cm). En el comedor hay 6 componentes libres (el principal de 96 m² y bolsillos cerrados que no se pueden alcanzar). Se encontró una franja de 8.1 m² junto al escalón del escenario que **bloquea a la altura del suelo pero queda libre a la altura del escenario**: es el borde de los tableros de la primera fila de mesas (z -6.34..-5.20).
- **Causa reproducida:** el escenario (z -9.4..-7.0) sube los ojos 0.56 m y la altura vuelve **de forma gradual** (2 m/s, ~0.28 s) **después** de resolver el movimiento horizontal. Al bajar corriendo hacia una mesa, la caja todavía va elevada y su base (1.26 m) pasa por encima del tablero (~0.75 m). El jugador avanza hasta 1.6 m y, al terminar de bajar, la caja queda dentro de la mesa; desde ahí ningún eje vuelve a quedar libre. Prueba `PruebaMundo escalon` (6 columnas × 5 direcciones × caminar/correr): **10 de los 40 casos válidos quedaban incrustados y sin poder moverse**, todos corriendo, en z ≈ -5.8..-6.2.

**Corrección (`CollisionWorld.resolveMovementConEscalon`, usado por el jugador en cada sub-paso):**
- En cada eje, la caja probada cubre en vertical **desde la altura actual hasta la de reposo en el destino** (`playerStartY + alturaEscalonEn(destino)`). No se puede avanzar hacia un sitio donde, al terminar de bajar (o de subir), quedaría dentro de algo.
- Lejos del escalón las dos alturas coinciden y es exactamente la misma prueba de antes.
- Las mesas no se tocaron, y tampoco su erosión, el tamaño del jugador ni las colisiones.
- **Red de seguridad:** al empezar cada cuadro, `collidersYaSolapados` anota los colliders que el jugador ya toca, y en ese cuadro se ignoran. Si por cualquier otro motivo quedara dentro de algo (un collider que aparece encima o una reaparición en mal sitio), puede moverse mientras no entre en ningún collider **nuevo**, y así sale. Normalmente la lista está vacía. Se hace un recorrido extra de los colliders por cuadro, no por sub-paso.
- La IA (`ChaseState`) sigue con `resolveMovement` y tiene su propio detector de atascos. Queda anotado en la auditoría (§2.72).

**Pruebas después:**
- `escalon`: **0 de 40** casos con atasco. Los puntos de partida dentro de la utilería del escenario se omiten.
- Rescate: incrustado a mano dentro del borde de una mesa, al retroceder sale.
- Desde fuera, W contra la mesa se detiene en el borde (z=-6.325), sin atravesarla.
- `mesas`: 0 atascos.
- `bordes`: 11/11 (incluida la diagonal con sprint contra una esquina).
- `puertas`: 6/6.
- `sprint`: 4/4, con 3.5 y 5.78 m/s y la diagonal normalizada.

**Archivos:** `world/CollisionWorld.java`, `screens/GameplayScreen.java` (`resolverMovimientoEnPasos`).

### 1.63 Bordes blancos del GIF del menú: dibujado un 1.2 % más grande, sin tocar el archivo, sesión 2026-10-23 (tarde)

**Medición (los 70 cuadros de `backgrounds/menu_background.gif`, 1600×900, 150 ms por cuadro, compuestos uno a uno con PIL):** el defecto viene **del propio GIF**, no del escalado. Los cuadros 12 y 21 traen la última fila blanca (luminancia 201-224) y las dos últimas columnas blancas. El cuadro 61 trae las tres últimas filas blancas. El resto no tiene píxeles blancos en el borde. Arriba solo hay un degradado más claro de la propia imagen (48-75 de luminancia frente a 71-76 del interior), no un borde blanco. Al estirar el GIF a pantalla completa (1920×1080 = ×1.2), esas líneas se ven más gruesas.

**Corrección (`PnlAplicarImagen.dibujarFondoMenu`):** el GIF se dibuja centrado y un 1.2 % más grande, con el mismo factor en los dos ejes (no cambia la proporción). Quedan fuera ~9.6 px de cada lado y ~5.4 px arriba y abajo en coordenadas del GIF, frente a un máximo de 2 y 3 px de borde blanco. Freddy se desplaza menos de 1 % y su cara no se recorta. El archivo original no se modifica. Se aplica en todas las pantallas que usan este GIF de fondo: menú, Opciones, Idioma, Volumen y Noche personalizada (`PnlAplicarImagen` lo reconoce por `CargarImagenes.menu`), y además `PnlInfo` y `PnlSelectorIdioma`, que lo dibujan por su cuenta. El costo es el mismo `drawImage` escalado de antes.

**Verificación (`FondoMenuBordes`, arnés del scratchpad):** los 70 cuadros dibujados como antes y como ahora, en 1920×1080, 1600×900, 1366×768, 2560×1440 y 1280×1024 (5:4), contando píxeles casi blancos (mín(R,G,B) > 170) en los 4 px del borde. **Antes: 76 964 píxeles en 15 combinaciones de cuadro y resolución. Ahora: 0.** Comparación del cuadro 61 a 1920×1080, con ampliación de la esquina inferior derecha: la línea blanca desaparece y la composición es la misma.

**Archivos:** `PnlAplicarImagen.java`, `PnlInfo.java`, `PnlSelectorIdioma.java`.

### 1.64 Sonido de apertura de la tableta al aparecer las franjas blancas de la intro de cada noche, sesión 2026-10-23 (tarde)

**Investigación:**
- El sonido de abrir la tableta es `InteraccionBoton.reproducirSonidoHover()` (`audio/sfx/button_hover.wav`). Es el **único clip compartido** del proyecto: aplica `volumenGeneral` antes de sonar y reproduce en su propio hilo (§1.52). Se reutiliza tal cual, sin duplicar el archivo ni crear instancias nuevas.
- Las franjas blancas son una ráfaga de 5 cuadros de 10 ms, justo antes de la tarjeta "12:00 AM" (medido con PIL en las 10 intros):
  - Noche 1: cuadros 40-44, tras el periódico, a 10 800 ms según la suma de retardos.
  - Noches 2-5: cuadros 2-6, a 1 050 ms (tras la estática y 1 s de negro).
  - Igual en los dos idiomas.
- **Hallazgo:** AWT reproduce la intro de la Noche 1 (12-14 MB) **0.7-1.0 s más lenta** que la suma de sus retardos, porque la decodifica sobre la marcha. Medido en la ejecución real, las franjas se ven a 11.6-12.0 s. Un temporizador quedaba ~0.9 s adelantado (primer intento descartado).
- Contar avisos `FRAMEBITS` del `ImageObserver` tampoco sirve: la correspondencia entre aviso y cuadro cambia en uno según cómo se pinte el panel. Con el arnés aislado el cuadro k llegaba con el aviso k−1 y en la intro real con el aviso k. Segundo intento, ~0.9 s adelantado, descartado.

**Implementación (`ControllerMenu.iniciarSecuenciaIntro`):** el panel de la intro es el `ImageObserver` del gif. En cada aviso `FRAMEBITS`, mientras el sonido sigue pendiente, el cuadro **que AWT acaba de mostrar** se dibuja en 160×90 (`esCuadroDeFranjasBlancas`). Si al menos 6 filas (1/15 del alto) son casi blancas en más de la mitad del ancho, es la ráfaga y suena. El periódico, la estática, la tarjeta y el reloj no llegan a ese umbral.
- `sonidoFranjasPendiente` (atómico) garantiza **una sola reproducción por intro** aunque la ráfaga dure 5 cuadros, aunque haya repintados y aunque el gif vuelva a empezar.
- `finalizarIntroYComenzarPartida()` lo cancela, tanto en el fin natural como con CTRL+S.
- Después de sonar ya no se analiza ningún cuadro. Mientras está pendiente cuesta un dibujo de 160×90 por cuadro nuevo, en el hilo de animación de AWT.
- La duración de la intro (`DuracionGif`) y el resto del flujo no cambian. El sonido normal de la tableta tampoco: no se tocó `mostrarTransicionTablet`.

**Prueba (`PruebaLineasIntro`, intros reales con `ControllerMenu` real y perfil aislado):** se capturó el panel de la intro cada ~5-15 ms para ver cuándo aparecen las franjas, y un `LineListener` en el clip compartido registró cuándo arranca el sonido.

| Intro | Franjas visibles | Sonido | Diferencia | Reproducciones | Intro (timer) |
|---|---|---|---|---|---|
| Noche 1 ES | 12 037 ms | 12 034 ms | −3 ms | 1 | 20 637 ms (20 400) |
| Noche 1 EN | 11 650 ms | 11 653 ms | +3 ms | 1 | 20 657 ms (20 450) |
| Noches 2-5 ES | 1 088-1 096 ms | 1 091-1 095 ms | −1..+3 ms | 1 c/u | 8 705-8 727 ms (8 650) |
| Noches 2-5 EN | 1 094-1 102 ms | 1 096-1 101 ms | −1..+2 ms | 1 c/u | 8 709-8 732 ms (8 650) |

Saltar la intro de la Noche 1 a los 3 s: 0 reproducciones después (ES y EN). **No verificado:** escucha humana (el clip arrancó en esta máquina, pero nadie lo oyó).

**Observación para la auditoría:** como la Noche 1 se ve ~1 s más lenta que sus retardos, su temporizador (20.4 s, suma de retardos) corta el reloj final ~1 s antes de lo previsto. No se cambió: no era parte del pedido y el corte cae dentro de los 5 s de reloj.

**Archivos:** `ControllerMenu.java`.

### 1.65 Destello en la estrella de la D de NYDER GAMES durante la presentación, sesión 2026-10-23 (tarde)

**Orden de inicio sin cambios:** selector de idioma → NYDER GAMES → advertencia → introducción → menú. También se mantienen los tiempos de §1.31: 250 ms de negro, 800 de entrada, 1300 visible y 650 de salida.

**La estrella:** es el **único elemento violeta** de `branding/nyder_games_splash.png` (2172×724). Hay 12 189 píxeles con azul > rojo + 40 y azul > verde + 40, todos en x 941-1219, y 145-379 (medido; ninguno fuera de esa caja). El repositorio no tiene una imagen suelta de la estrella que sirva (`images/nyder/NYDER-star.png` es otro diseño, del trabajo ajeno sin seguimiento). **La máscara se genera en memoria a partir del propio PNG**, en un hilo aparte al aparecer la pantalla negra de NYDER. El archivo no se modifica.

**Animación (`PnlWarning.setBrilloEstrella` + `ControllerMenu.iniciarDestelloNyder`):** dentro de los 1300 ms en que el logo está totalmente visible:
- espera 120 ms;
- sube en 420 ms (curva cuadrática);
- se sostiene 60 ms en el pico;
- se apaga en 600 ms.
En total son 1.2 s y no alargan la presentación. Mientras dura:
1. la estrella se aclara de violeta a un blanco lavanda (la máscara se dibuja encima con alfa = brillo);
2. aparece un halo radial suave centrado en la estrella;
3. cerca del pico, una cruz fina de cuatro puntas (el "chispazo") que crece y se apaga.

No hay flash de pantalla completa, ni parpadeos repetidos, ni nada fuera de la estrella. Todo respeta el alfa del fundido del panel. Un único `Timer` de 16 ms, que se detiene al terminar.

**Sonido:** no había ningún efecto de brillo en el proyecto, así que se sintetizó uno original: `audio/sfx/nyder_star_sparkle.wav` (1.6 s, 44.1 kHz estéreo, pico a −9 dBFS, ataque de 6 ms sin clic). Es un arpegio ascendente muy rápido de campanillas (Do6-Mi6-Sol6-Do7-Mi7, parciales inarmónicos con vibrato leve), un soplo de aire filtrado que acompaña la luz y una cola de ecos. Se genera con `tools/audio/synth_nyder_sparkle.py` (raíz del repo), sin muestras externas.
- Se carga en segundo plano con `Sonido`, que aplica el volumen general.
- Suena **una vez** a los 160 ms del destello, en un hilo aparte (no bloquea Swing), para que la subida del arpegio coincida con el pico de luz.
- **Al omitir** (CTRL+S) se detienen el `Timer` y el sonido, si ya sonaba (`cerrar()` en un hilo aparte, por el bloqueo conocido de `Clip.stop` en Java 8, §1.52). Si todavía no había sonado, se suelta sin reproducirse. Si NYDER termina con naturalidad, el sonido acaba solo y su `Clip` se cierra solo.

**Pruebas (`PruebaDestelloNyder`, arranque real `ControllerMenu(false)` con perfil aislado, ES y EN):**
- Orden de pantallas: NYDER a los 40-52 ms del clic en el idioma, advertencia a los 3085-3165 ms (como antes).
- El brillo empieza a los ~1.31 s, llega a 1.00 y se apaga a los ~2.37 s, siempre dentro de la fase visible.
- El sonido arranca **una sola vez** (`LineListener` en su clip), entre 0 y 130 ms después de empezar el destello.
- La diferencia de píxeles entre una captura sin destello y otra en el pico afecta a ~17 100 píxeles, todos en x 802-1093, y 339-625 de 1920×1080 (la zona de la estrella). Revisado a ojo.
- Omitir a mitad del destello: no queda `Timer`, el sonido se corta y se suelta, y sigue la advertencia.
- **No verificado:** escucha humana del sonido.

**Archivos:** `PnlWarning.java`, `ControllerMenu.java`, `audio/sfx/nyder_star_sparkle.wav` (nuevo), `tools/audio/synth_nyder_sparkle.py` (nuevo).

### 1.66 Noches personalizadas solo tras superarlas y estrella permanente al completar Escape, sesión 2026-10-23 (tarde)

**Progresión, estado de partida verificado:** `PreferenciasJuego.nocheActual` es la noche que "Continuar" va a jugar **y** la que elige Noche personalizada (§1.5). No es un historial: Noche personalizada la cambiaba libremente y un jugador nuevo podía elegir la Noche 5. No servía para decidir qué está superado.

**Historial nuevo:** `PreferenciasJuego.nochesSuperadas` (`boolean[5]`, índice = `Noche.ordinal()`), persistido como `nochesSuperadas=NOCHE_1,NOCHE_2` en `config.properties`.
- Se marca en `ControllerInterfaz.alGanar()` antes de avanzar, con su propio guardado; así cubre también la Noche 5, que no avanza.
- Perder (`alPerder`) y rendirse no lo tocan.
- "Nueva Partida" ("¿Reiniciar todo el progreso?") lo vacía, igual que ya hacía con el desbloqueo de ESCAPE. La estrella no se borra (ver abajo).
- **Migración conservadora** de archivos sin la clave: no se concede nada que el archivo no demuestre. `nocheActual` no prueba nada; lo único demostrado es `escapeDesbloqueado=true`, que solo se activa al ganar la Noche 5, así que únicamente la Noche 5 queda superada.
- Ejemplo: el perfil real de esta máquina (Noche 5, Escape desbloqueado; no se modificó en las pruebas) verá la Noche 5 y ESCAPE disponibles y las noches 1-4 bloqueadas hasta volver a ganarlas.

**Noche personalizada (`ControllerMenu`, `PnlNochePersonalizada`):**
- Las noches no superadas se ven en gris (140) con "(Bloqueada)"/"(Locked)", el mismo lenguaje visual que "ESCAPE (Bloqueado)".
- Al hacer clic no cambian `nocheActual` ni el archivo. Suena `button_error.wav` y aparece 3 s "Supera la Noche N en una partida normal para desbloquearla." / "Beat Night N in a normal game to unlock it.". Usan el mismo rótulo y temporizador que el mensaje de ESCAPE (`mostrarMensajeBloqueado`).
- Sobre una noche bloqueada, el ratón no hace sonar el hover ni muestra la flecha `>>`, que sería una interacción engañosa. Se resuelve con una sobrecarga de `InteraccionBoton.aplicarHoverYSonido` que recibe una condición `activo`.
- Los botones pasan de 350 a 640 de ancho base: cabe "> Noche 1 (Bloqueada)" sin llegar a la columna de ESCAPE (x 920).
- Las noches superadas se comportan como siempre.

**Estrella de "Escape completado":**
- **Señal entre procesos:** Escape escribe `~/.fivedoorsatfreddys/escape_victoria.flag` (`com.fivedoorsescape.io.VictoriaEscape`, a un temporal y renombrado). Lo hace en el **único** punto que registra la victoria: `GameplayScreen`, donde se marca `escapado` al terminar o saltar la cinemática del auto. Abrir la puerta de salida, rendirse, perder, cerrar la ventana o volver sin ganar no la escriben. Solo FDAF escribe `config.properties`, así que los dos procesos nunca escriben el mismo archivo.
- **Consumo en FDAF (`com.fdaf.util.LogroEscape`):** `ControllerMenu.mostrarMenuPrincipal()` la consume antes de mostrar el menú. Ese es el camino del proceso nuevo tras volver de Escape (`ReiniciadorJuego`) y también el del arranque normal tras la introducción; si FDAF se hubiera cerrado con Escape abierto, la victoria se recoge en el siguiente arranque. El orden es fijo: `escapeCompletado=true`, guardar y **después** borrar la señal. Una victoria no se cuenta dos veces y no se pierde si el guardado fallara.
- `PreferenciasJuego.escapeCompletado` se persiste como `escapeCompletado=true` y es independiente del idioma, el volumen y "Nueva Partida" (un logro no se reinicia con la partida).
- **Guardado atómico de la configuración** (`PersistenciaJuego.guardar`): se escribe `config.properties.tmp` y se renombra encima (`ATOMIC_MOVE`, con respaldo sin él). Un cierre a mitad de escritura ya no puede dejar el archivo a medias.
- **Diseño (`com.fdaf.util.EstrellaLogro`):** estrella dorada de cinco puntas dibujada en código, nítida a cualquier escala:
  - degradado dorado, reflejo superior y filete interior;
  - borde oscuro fino, para que se lea sobre la estática clara y oscura;
  - resplandor cálido muy tenue.
  No reutiliza la estrella violeta de cuatro puntas de NYDER: esta es un logro del jugador, no la marca del equipo.
- **Posición (`PnlMenu.ubicarEstrellaEscape`):** como en FNaF 1, junto al título. Va a la derecha de "Freddy's", centrada en la altura de sus mayúsculas y del mismo tamaño (68 px en 1080p, en (686, 385)), calculada con la posición real del final del texto del `JTextPane`. No toca ningún botón (el título termina muy por encima de "Nueva Partida") ni compite con el título. Tooltip: "Completaste Five Doors Escape" / "You completed Five Doors Escape".

**Pruebas (`PruebaLogro`, `ControllerMenu`/`ControllerInterfaz` reales, perfil aislado `-Duser.home`): 26/26 en ES y 26/26 en EN.**
- Migración: un archivo viejo con Escape desbloqueado deja solo la Noche 5 superada y sin estrella.
- Jugador nuevo:
  - las 5 noches en gris con el indicador;
  - un clic en una bloqueada no la elige, no cambia el archivo y muestra el mensaje;
  - el hover no muestra la flecha;
  - el texto más largo (693 px ES / 594 EN) cabe en el botón (768) sin invadir ESCAPE.
- `alGanar()` real de las noches 1 y 2: `nochesSuperadas=NOCHE_1,NOCHE_2` y `nocheActual=NOCHE_3`. La 1 se puede elegir (con flecha) y la 3 sigue bloqueada.
- Cambiar idioma y volumen conserva los desbloqueos.
- Primera señal de victoria: agradecimiento con estrella, `escapeCompletado=true` en el archivo y la señal borrada. Después, menú con la estrella visible sin tapar botones.
- Segunda señal: el agradecimiento sale otra vez, sin la presentación de la estrella, que sigue en el menú. Sin señal nueva, no vuelve a salir.
- Recargar desde el archivo recupera la estrella.
- "Nueva Partida" deja `nochesSuperadas` vacío y `nocheActual=NOCHE_1`, y conserva `escapeCompletado=true`.
- No queda el `.tmp` del guardado atómico.
- Capturas revisadas: Noche personalizada bloqueada (ES) y con 1-2 superadas (EN), y el menú con la estrella (ES y EN).
- **No verificado aquí:** una partida completa real de Escape que escriba la señal. Eso se prueba del lado de Escape (§2.66); en FDAF la señal se escribió a mano.

**Archivos:** `PreferenciasJuego.java`, `PersistenciaJuego.java`, `LogroEscape.java` (nuevo), `EstrellaLogro.java` (nuevo), `ControllerMenu.java`, `ControllerInterfaz.java` (`alGanar`), `InteraccionBoton.java`, `TextosInfo.java`, `PnlMenu.java`, `PnlNochePersonalizada.java`. En Escape: `io/VictoriaEscape.java` (nuevo) y `GameplayScreen.java`.

### 1.67 Pantalla de agradecimiento de NYDER tras cada victoria de Escape, sesión 2026-10-23 (tarde)

**Cuándo:** cada vez que `LogroEscape` encuentra una victoria pendiente (§1.66), antes del menú.
- Primera victoria: incluye la presentación de la estrella.
- Victorias siguientes: solo el agradecimiento.
- Derrota, abandono o cierre: no hay señal, así que no hay agradecimiento ni estrella.

El retorno a FDAF no cambia: el proceso se reinicia como siempre, y es el menú del proceso nuevo el que la muestra.

**Texto (en `TextosInfo`, ES/EN):**
- ES: «¡FELICIDADES!» — «¡Lo conseguiste! Escapaste y completaste la experiencia de Five Doors at Freddy's. / Todo el equipo de NYDER te agradece por jugar nuestro proyecto y por acompañarnos hasta el final. / Este juego fue creado con dedicación por estudiantes de Informática Empresarial de la Universidad de Costa Rica, Recinto de Siquirres. / Gracias por formar parte de esta experiencia. / — Equipo NYDER».
- EN: «CONGRATULATIONS!» — «You made it! You escaped and completed the Five Doors at Freddy's experience. / The whole NYDER team thanks you for playing our project and sticking with us until the very end. / This game was made with dedication by Business Informatics students at the University of Costa Rica, Siquirres Campus. / Thank you for being part of this experience. / — Team NYDER».
- Ajustes respecto al pedido, sin cambiar la intención:
  - "Has escapado y completado" pasa a "Escapaste y completaste", más natural junto a "¡Lo conseguiste!";
  - "y acompañarnos" pasa a "y por acompañarnos";
  - en inglés, "The NYDER team" pasa a "The whole NYDER team … sticking with us until the very end".

**Diseño (`PnlAgradecimiento`, dibujado en `paintComponent` a partir del tamaño real, sin `EscalarVista`):**
- Composición centrada en vertical, medida antes de dibujar, de arriba abajo:
  - logo de NYDER (`branding/nyder_logo.png`);
  - la estrella, si es la primera vez;
  - título dorado con sombra cálida y línea de acento violeta de NYDER;
  - cuatro párrafos centrados al 66 % del ancho;
  - firma en violeta claro;
  - "Continuar", justo debajo.
- Fuente del proyecto (`Fuentes`, Consolas escalada). Fondo negro con un resplandor violeta muy tenue.
- Entrada: fundido desde negro de 0.7 s. Logo, título y cada párrafo aparecen escalonados entre 0.25 y 2.2 s.
- Salida: fundido de 0.55 s antes del menú.
- Celebración discreta: 46 chispas pequeñas, doradas y algunas violetas, que suben despacio y titilan.
- **Estrella (solo la primera vez):** a los 0.9 s entra creciendo con un pequeño rebote, con un resplandor que pulsa y el rótulo "¡Estrella desbloqueada!" / "Star unlocked!". Suena el mismo destello de NYDER (§1.65): cargado en segundo plano, una sola vez y con el volumen general.
- **Continuar:** el botón aparece a los 2.6 s y responde a clic, Enter, Espacio o ESC (ESC llega por `ControllerMenu.manejarEscape`, que delega en el panel). Antes de eso las teclas no hacen nada, para que una tecla que venía apretada desde Escape no cierre el mensaje sin leerlo. Un único `Timer` de ~30 fps, que se detiene al continuar.

**Pruebas (dentro de `PruebaLogro`, §1.66):**
- Aparece con `isPrimeraVez()` correcto en la primera y en la segunda victoria.
- Enter antes de que aparezca Continuar no la cierra; ESC después sí; el clic en Continuar a los ~3 s, también.
- Capturas a 1920×1080 en ES y EN de la primera victoria (con estrella) y de una repetida.
- Problema real corregido con las capturas: en la primera versión el botón tenía altura fija y, en la primera victoria en español, se montaba sobre el último párrafo. Ahora se mide la composición y el botón va bajo la firma.
- **No verificado:** escucha humana del destello ni ratón físico.

**Archivos:** `PnlAgradecimiento.java` (nuevo), `ControllerMenu.java` (`mostrarMenuPrincipal`, `mostrarAgradecimiento`, `manejarEscape`), `TextosInfo.java`.

### 2.66 Señal de victoria para la estrella de FDAF, sesión 2026-10-23 (tarde)

Del lado de Escape, la estrella y el agradecimiento (§1.66-§1.67) solo necesitan una señal. `io/VictoriaEscape.marcar()` escribe `~/.fivedoorsatfreddys/escape_victoria.flag` (a un temporal y renombrado) en el único punto que registra la victoria: `GameplayScreen.actualizarCinematicaVictoria`, justo donde se marca `escapado`. Llega igual al terminar la cinemática del auto o al saltarla con ESC.
- Abrir la puerta de salida, la puerta de emergencia, rendirse, perder o cerrar la ventana no la escriben.
- Escape no toca `config.properties`.
- Si la escritura fallara, se registra el error y la victoria sigue igual (FDAF simplemente no se entera).

Prueba de punta a punta (la señal se escribe al terminar y al omitir la cinemática) en §2.68.

### 2.67 Derrota: los cuatro animatrónicos reaccionan en el escenario (movimiento procedural), sesión 2026-10-23 (tarde)

**Pedido:** la llegada al escenario se sentía rígida, con cuatro modelos inmóviles en fila. Los cuatro tenían que reaccionar ante el jugador: girar la cabeza, cada uno a su manera, con movimiento real, tics, interferencias y un foco que falle y cambie lo que se ilumina. El arrastre y el final no se tocan.

**Lo que había:** solo existen las animaciones `idle` y `walk` retargeteadas (§2.23-§2.26). No hay una animación de "mirar". En el escenario los cuatro estaban en `idle`. Comparación "sin" del arnés: con la pose `idle` miran por encima del jugador y se les ven las barbillas.

**`cinematics/PoseProcedural` (nuevo):** cabeza, torso y mandíbula de cada animatrónico se mueven **encima de su animación real**:
- mirar a un punto, con giro y cabeceo limitados a ±78° y −55/+40°;
- ladear la cabeza;
- inclinar el torso;
- abrir la mandíbula.
Los huesos se declaran por datos en cada `content/entities/*.json` (`"huesos": {"cabeza", "torso", "mandibula"}`, `EntityDefinition.huesos`):
- Freddy: `j_head_04`, `j_spine_03`, `j_jaw_06`;
- Bonnie: `bip_head_bonnie`, `bip_spine_0_bonnie`, `bip_jaw_bonnie`;
- Chica: `Bone.003_Armature_7`, `Bone.001_Armature_5`, sin hueso de mandíbula;
- Foxy: `bip_head_foxy`, `bip_spine_0_foxy`, `bip_jaw_foxy`.
Los giros se calculan en el **espacio del mundo** alrededor del pivote real del hueso, así no dependen de los ejes de cada uno de los cuatro rigs, y se devuelven al espacio local del padre. Cada cuadro hace `restaurar()` antes de que el `SceneManager` aplique la animación y `aplicar(...)` después, de modo que el desvío nunca se acumula.

**Hallazgo técnico (medido):** la primera versión no tenía **ningún efecto visible**; las capturas "sin" y "con" salían idénticas. En gdx-gltf, el `AnimationController` deja los huesos con `isAnimated = true`, y entonces `Node.calculateTransforms` usa `localTransform` tal cual, sin rehacerlo desde traslación, rotación y escala. Ahora `PoseProcedural` escribe y guarda también `localTransform`.

**Puesta en escena (6.0-11.3 s, en `CinematicaDerrota`):**
- **La mirada recorre el círculo:** el culpable desde arriba → el de la izquierda (6.7 s) → el de la derecha (7.5 s) → el que está detrás de la cabeza, mirando hacia arriba y atrás (8.3 s) → de vuelta al culpable, que se inclina (8.95 s). Cada tramo termina en la **cara real** de cada uno (posición del hueso + 0.35 m, porque el pivote está en la base del cuello). Cada uno **gira la cabeza hacia el jugador justo cuando la cámara llega a él**. En el pasillo, los dos testigos ya siguen con la cabeza al jugador mientras lo arrastran.
- **Cada personaje a su manera:**
  - Bonnie: servo que se traba (sube, se frena, retrocede un poco, completa), con ladeos bruscos en los tics.
  - Chica: un giro que se pasa de largo y vuelve, la cabeza ladeada 22° (antinatural) y espasmos en los que la cabeza se suelta y vuelve.
  - Foxy: encorvado (torso 10-22°), la cabeza baja y ladeada y la mandíbula que chasquea.
  - Freddy: lento y fijo, un poco hacia abajo, con la mandíbula que se entreabre.
- **Desenlace:** todos se inclinan hacia el jugador. El culpable además se inclina hasta 14° más mientras se acerca (el acercamiento de antes). Al reventar el foco, los cuatro lo miran de lleno.
- **Foco:** además de fallar, se balancea como si colgara y se va hacia la cara que se está mirando. El que la cámara encuentra queda iluminado y los demás en sombra. Sin esto, las capturas mostraban a oscuras al de la izquierda y al de atrás. En cada tic se apaga un instante.
- **Tics e interferencias:** en 6.95, 7.78, 8.52, 9.42, 9.93 y 10.28 s hay ~0.13 s de franjas finas claras y oscuras y un velo rojo al 7 %, translúcidos, para no tapar a nadie. El ojo del jugador tiembla apenas (respiración).
- `T_INCLINA` pasa de 8.8 a 8.95 s (el servo suena ahí). La duración total (12.3 s), el arrastre, el foco que revienta, los ojos rojos y el cierre no cambian.
- `terminar()` restaura los esqueletos, tanto al terminar como al omitir.

**Pruebas (`PruebaDerrota`, arranque real con ventana oculta):**
- Última vida con Freddy, Bonnie y Foxy como culpables: `NightGameOverScreen` a los 12.29 s.
- Omitir manteniendo ESC: 3.20 s (empezó a los 2.0).
- Atrapada normal con vidas: estática, corazones y reaparición, sin cinemática.
- Modo nuevo `diag`: desde el ojo del jugador encuadra la cara de cada uno con luz auxiliar, **sin** y **con** el movimiento procedural en el mismo instante (9.6 s). Sin él, los cuatro miran por encima del jugador; con él, los cuatro lo miran de frente. Foxy sale ladeado y con la mandíbula abierta.
- 3 × 17 capturas de la secuencia revisadas:
  - Freddy iluminado a la izquierda mirando;
  - Chica iluminada con el pico abierto;
  - Foxy detrás bajo el foco;
  - los demás asomando por los bordes;
  - las interferencias;
  - el culpable inclinado con los ojos encendidos.
- **No verificado:** juego con teclado físico.

**Archivos:** `cinematics/PoseProcedural.java` (nuevo), `cinematics/CinematicaDerrota.java`, `content/EntityDefinition.java`, `content/entities/{freddy,bonnie,chica,foxy}.json`, `screens/GameplayScreen.java` (registro y ganchos antes/después de animar).

### 2.68 Freddy en el estacionamiento: presencia física, amenaza y persecución del auto, sesión 2026-10-23 (tarde)

**Pedidos:**
1. Al acercarse a Freddy en el estacionamiento se lo atravesaba como a un decorado.
2. Al escapar en el auto, Freddy debía perseguirlo, como un último momento de tensión integrado con la cámara, el auto y el sonido, que terminara en un escape exitoso.

**Causa de (1), auditada:**
- Desde que se abre la salida, la IA está congelada (`AISystem` retirado, §2.49) y los animatrónicos nunca tuvieron colisión contra el jugador: dentro, el contacto es la atrapada.
- Freddy aparecía en la puerta de emergencia con `TransformComponent` y sin collider. La posición, el tamaño y la orientación del modelo eran correctos (de pie en el suelo, mirando al estacionamiento); lo que faltaba era la caja.

**¿Por qué afuera?** Se evaluaron tres opciones:
- que atraviese la puerta de emergencia;
- que aparezca en el estacionamiento;
- que persiga desde cerca de la salida.

Se eligió la que **ya justificaba la escena**: Freddy aparece en la puerta de emergencia que el propio jugador dejó abierta (la caja musical, que suena desde él, lo delata). Es decir, lo siguió por donde salió. Desde ahí, salir corriendo hacia el auto es la continuación natural, sin teletransportes ni apariciones en medio del estacionamiento. Freddy sigue sin aparecer nunca a la vista (§2.49).

**`world/FreddyExterior` (nuevo), para no seguir engordando `GameplayScreen` (auditoría M4):**
- **EN_PUERTA:**
  - Collider de 0.64 × 0.64 m, del suelo a 2.3 m, en el hueco de la puerta: el jugador ya no lo atraviesa y no puede volver a entrar al edificio por ahí.
  - **Amenaza** si el jugador se acerca a menos de 3.4 m: gira el cuerpo hacia él a 90°/s (sin dar la espalda al estacionamiento, ±70°), lo mira con la cabeza, se inclina y abre la mandíbula (`PoseProcedural`, §2.67). Suenan un servo y su risa grave, una vez por acercamiento; alejarse más de 4.9 m lo rearma.
  - **No ataca:** la IA sigue congelada, así que no hay muerte posible y la victoria nunca coincide con una atrapada.
- **PERSIGUE (solo en la cinemática):** retira su collider y corre con la animación `walk` al 1.9× (aceleración 6.5 m/s², hasta 5.2 m/s), echado hacia adelante. Antes de que arranque el auto va hacia la puerta del conductor; después, hacia un punto 3.5 m detrás del auto: lo sigue, pero nunca se le echa encima. Pasos metálicos (`audio/sfx/freddy_run_step.wav`, sintetizado con `tools/audio/synth_freddy_chase.py`: golpe grave, chasquido del endoesqueleto y servo) cada 0.31 s, con volumen según la distancia a la cámara.
- **EN_PORTON:** al llegar a la línea del portón se planta dentro del estacionamiento, en el hueco abierto, y se queda mirando cómo se va el auto.

**Cinemática de victoria (misma duración, 16.5 s; el resto no cambia):**
- 2.6 s: Freddy en la puerta, si todavía no estaba. Ocurre mientras el jugador se sienta, mirando adentro.
- 5.4 s: el jugador gira la cabeza hacia la pizzería y lo ve en la puerta (como antes).
- 6.15 s: la cabeza de Freddy se clava en el auto.
- 6.4 s: su risa (ya existía).
- 6.55 s: **sale corriendo**. La mirada lo sigue (ahora apunta a su cabeza real, no a un punto fijo) y vuelve al frente de golpe a los 7.05 s, justo antes de arrancar.
- 7.4-11.4 s: el auto arranca con Freddy detrás.
- **8.95-9.95 s: corte a una cámara pegada al maletero, mirando atrás.** Freddy corre hacia ella a pocos metros, con los ojos encendidos, iluminado en rojo por las luces traseras (luz puntual roja nueva en `SalidaExterior`, encendida con los faros), con la puerta de emergencia al fondo.
  - Primero se probó girar la cabeza del conductor por encima del hombro: desde el asiento se veía sobre todo el techo rojo del auto y Freddy quedaba diminuto en la luneta (capturas). Se descartó.
- Plano de la calle: el auto pasa y se aleja. El encuadre final, con su empuje lento, queda entre el letrero y el **portón abierto, donde Freddy se quedó inmóvil bajo el farol**. Después, negro y la victoria de siempre.

**Pruebas (`PruebaVictoria`, `GameplayScreen` real con colisión real; ES y EN; completa y omitida):**
- Freddy en la puerta tiene collider.
- Caminando hacia él, el jugador se detiene contra él (x=-12.71 con Freddy en -12.05) y no queda incrustado.
- Tampoco se cuela por el costado al edificio.
- Al acercarse, Freddy gira hacia el jugador.
- La amenaza no mata ni termina la partida (`JUGANDO`, sin `escapado`).
- Durante la persecución, la distancia mínima de Freddy al centro del auto es 3.04 m: no lo alcanza ni se mete dentro.
- A los 12 s Freddy está `EN_PORTON` en (-20.28, 0, -15.20).
- La cinemática dura 16.5 s. Omitida con ESC: 3.23 s.
- Termina en `EscapeVictoryScreen` con `escapado` y escribe la señal de FDAF (§2.66).
- Capturas revisadas: amenaza de cerca, Freddy en la puerta visto desde el auto, el corte trasero con Freddy corriendo y el portón con Freddy al final.

**Archivos:** `world/FreddyExterior.java` (nuevo), `cinematics/CinematicaVictoria.java`, `world/SalidaExterior.java` (luz trasera), `screens/GameplayScreen.java`, `assets/audio/sfx/freddy_run_step.wav` (nuevo), `tools/audio/synth_freddy_chase.py` (nuevo).

### 2.69 Salpicadero del auto: de bloque rectangular a tablero con forma, sesión 2026-10-23 (tarde)

**Antes** (`prep_car.py`): `car_dashboard` era una caja biselada de 0.30 × 0.30 m a lo ancho del auto. Desde el asiento, en la cinemática, se leía como un bloque rectangular delante del jugador.

**Ahora:** un **perfil lateral extruido** a lo ancho, con bisel de 3 segmentos que redondea las aristas:
- la cara superior baja suavemente hacia la base del parabrisas;
- un canto redondeado sobre los indicadores;
- la cara frontal se retira hacia las rodillas del conductor.

Del lado del conductor se añade una **visera** sobre los indicadores, que proyecta sombra sobre las esferas. La línea del tablero contra el parabrisas, el ojo del conductor (`OJO_CONDUCTOR (0.436, 0.087, 1.153)`, igual que en `exterior.json`), el volante, los asientos y el resto del auto no cambian. Los indicadores (`car_gauges`) se recolocaron pegados a la cara nueva, bajo la visera.

**Regenerado** con Blender 5.1.2 a partir del original de Quaternius (`FDAF assets/source_files/cinematics/escape_victory/downloads/quaternius_car_original.glb`): `assets/models/car_sedan.glb` (0.43 MB).

**Verificación:**
- Render de Blender desde el ojo del conductor.
- Capturas del juego dentro de la cinemática (3.3, 4.4 y 7.6 s): el tablero se lee como tal, con la visera y los indicadores encendidos. La proporción con el parabrisas no cambió.
- `PruebaVictoria` completa: sin regresiones.

**Archivos:** `tools/cinematics/escape_victory/prep_car.py`, `assets/models/car_sedan.glb`.

### 2.70 Faroles del estacionamiento redistribuidos, sesión 2026-10-23 (tarde)

**Antes:** tres faroles en (-22.7, -1.6), (-15.6, -11.6) y (-24.8, -15.2, con parpadeo). El tercero iluminaba la esquina sudoeste (el auto tapado), lejos del portón; el segundo, el centro del estacionamiento. La zona de la persecución y el portón quedaban a media luz.

**Ahora** (`exterior.json`; se mantienen 3 faroles, sin luces nuevas salvo la luz trasera del auto de §2.68):

| Farol | Posición | Qué ilumina |
|---|---|---|
| 1 | (-21.4, -0.9), intensidad 58 | el auto y la puerta del conductor (lado oeste), al fondo de los espacios |
| 2 | (-17.6, -9.4), 46, parpadea | la franja por donde corre Freddy y la ruta del auto |
| 3 | (-17.6, -15.0), 64 | la barrera y el portón: donde termina Freddy y lo que se ve en el plano final desde la calle |

- La puerta de emergencia y la zona donde aparece Freddy ya tenían su lámpara de pared y el letrero EXIT (sin cambios).
- **Quedan a oscuras a propósito** la esquina sudoeste (el auto tapado), la esquina noreste junto a la fachada y el fondo detrás del contenedor.
- Los postes no tocan vallas, contenedor, topes ni la ruta del auto: el farol 3 está a 0.9 m de la valla y a 1 m del contenedor; los faroles 2 y 3, a 2.4 m de la ruta del auto.
- Los colliders de los postes y los charcos de luz siguen a las nuevas posiciones, porque se generan desde el mismo JSON.
- Regenerado `assets/models/exterior_parking.glb` con `build_exterior.py`. Antes se comprobó que el build reproduce el GLB anterior byte por byte en tamaño (15 118 932). El nuevo pesa 15 118 852.

**Rendimiento** (`PruebaRendimiento`, 2 corridas, mismo arnés que §2.60/§2.61, ms por cuadro):

| Zona | Ahora | Antes |
|---|---|---|
| Estacionamiento | 11.66-11.75 | 11.6-12.8 |
| Victoria | 15.21-15.43 | 15.2-16.0 |
| Comedor | 14.3 | 14.2 |
| Parts and Service | 8.2 | 8.1 |
| Pirate Cove | 11.4 | 10.8 |
| Persecución | 12.7-12.9 | 12.2 |

Las diferencias están dentro de la variación entre ejecuciones.

**Verificación visual:** capturas del estacionamiento al salir, de la puerta del conductor, de Freddy en la puerta y del plano final con Freddy en el portón bajo el farol 3.

**Archivos:** `assets/content/maps/exterior.json`, `assets/models/exterior_parking.glb`.

### 2.71 Foco de la ventana de Escape: activación, "Haz clic para entrar" y pausa al perder el foco, sesión 2026-10-23 (tarde)

**Síntoma reportado:** al abrirse Escape en su propia ventana, los controles no respondían hasta hacer clic en ella.

**Causa (investigada):** Escape es otro proceso. Windows solo deja tomar el primer plano (`SetForegroundWindow`, que es lo que hace `glfwFocusWindow`) a quien ya lo tiene o lo heredó, o a cualquiera si hace un rato que el usuario no toca nada. Hay dos caminos de lanzamiento (§1.21):
- **Distribución** (FDAF → `cmd` → `lwjgl3.bat` → `java`): el permiso se hereda.
- **Desarrollo** (`gradlew lwjgl3:run`): el proceso del juego lo crea el **daemon de Gradle**, que vive en segundo plano y no tiene ese permiso. La ventana aparece encima, pero el teclado sigue en FDAF.

**Decisión:** FDAF no se minimiza. Se probó esa vía (FDAF cedía el primer plano al recibir una señal de "ventana lista"), pero al cerrar Escape Windows no reactiva una ventana minimizada y el regreso a FDAF, que hoy funciona, quedaba sin foco. Esa variante se descartó y se revirtió. Es Escape quien toma el foco.

**Implementación:**
- `core/util/EstadoVentana` (nuevo): `core` no conoce el backend. El lanzador informa los cambios de foco y aporta cómo pedirlo. Sin lanzador (arneses, IDE) la ventana se da por enfocada y nada cambia.
- `Lwjgl3Launcher`:
  - Un `Lwjgl3WindowAdapter` informa `focusGained`/`focusLost`.
  - `pedirFoco()` usa `glfwFocusWindow` y, si GLFW sigue sin foco, en Windows aplica la técnica habitual: una pulsación sintética de ALT (`User32.SendInput`, incluido en LWJGL), que libera el bloqueo de primer plano, y un segundo `glfwFocusWindow`.
  - El log indica qué vía se usó.
- `BootScreen`: pide el foco en su primer cuadro visible y lo vuelve a pedir a los 0.6 s si todavía no lo tiene.
- **Red de seguridad, `GameplayScreen`:** mientras la ventana **nunca** tuvo el teclado, la escena queda congelada y oscurecida con "Haz clic para entrar" / "Click to enter" (`window.click`), latiendo suave y con el cursor libre. La introducción no avanza ni empieza la narración. El clic activa la ventana y **se consume**: ese cuadro termina ahí, así que no abre puertas ni recoge objetos. Basta un solo clic, y después sigue el flujo normal (introducción → instrucciones → juego).
- **ALT+TAB y vuelta:** si en `JUGANDO` la ventana pierde el foco, se abre la pausa de siempre: mundo detenido, audio continuo en pausa, cursor libre. Al volver sigue en pausa y se reanuda con ESC, como siempre. Las cinemáticas siguen corriendo.

**Pruebas:**
- **Ejecución real:** `Lwjgl3Launcher` lanzado desde un proceso en segundo plano, y además dos simulaciones del lanzamiento desde una ventana Swing en primer plano (con la cadena `cmd /c` de `LanzadorEscape` y con un lanzador ajeno, como el daemon de Gradle). En las tres, `[Ventana] foco pedido (ya lo tenia): concedido` y la ventana Swing pasó a inactiva. **No se pudo reproducir el bloqueo** en esta sesión automatizada: Windows concede el primer plano a cualquiera cuando no hubo entrada reciente del usuario. Por eso **la vía ALT no quedó ejercitada** aquí. Con un jugador real, que acaba de hacer clic en FDAF, el bloqueo sí está activo; esa vía y la pantalla de activación son las que lo cubren.
- **`PruebaMundo foco`** (`GameplayScreen` real, foco simulado): 8/8.
  - Sin foco, la introducción no avanza y no hay narración.
  - El clic activa la ventana y se consume.
  - Después arranca la introducción.
  - En `JUGANDO`, perder el foco abre la pausa; el jugador no se mueve; al volver sigue en pausa; ESC reanuda.
- Capturas de las dos pantallas revisadas.
- **No verificado:** el caso real con teclado y ratón físicos ni un ALT+TAB humano.

**Archivos:** `core/util/EstadoVentana.java` (nuevo), `lwjgl3/Lwjgl3Launcher.java`, `screens/BootScreen.java`, `screens/GameplayScreen.java`, `i18n/strings*.properties` (`window.click`).

### 2.72 Auditoría de ambos proyectos (segunda ronda), sesión 2026-10-23 (tarde)

Solo hallazgos comprobados en el código, en ejecución o con mediciones de esta sesión. Donde algo es una hipótesis sin reproducir, se dice. Coste: bajo (< 1 h), medio (una sesión), alto (varias). Complementa la auditoría de §2.62; sus puntos abiertos se revisaron y siguen abajo con su estado.

**Corregido en esta sesión (defectos reproducibles relacionados con el pedido):**

| Hallazgo | Evidencia | Sección |
|---|---|---|
| Puertas 004 y 005 giraban sobre el picaporte | malla de la hoja: pomos en el canto de la bisagra | §2.63 |
| Atasco junto a las mesas al bajar del escenario | 10 de 40 casos incrustados, reproducidos con prueba | §2.65 |
| Freddy se atravesaba en el estacionamiento | sin collider desde §2.49 | §2.68 |
| `PoseProcedural` sin efecto en huesos animados | capturas "sin"/"con" idénticas; `isAnimated` usa `localTransform` | §2.67 |
| Bordes blancos del GIF del menú | 76 964 píxeles blancos en el borde, en 15 combinaciones de cuadro y resolución | §1.63 |
| Noches personalizadas libres para un jugador nuevo | `nocheActual` no es historial | §1.66 |
| `config.properties` podía quedar a medias | escritura directa sobre el archivo | §1.66 (guardado atómico) |
| Ventana de Escape sin teclado al abrirse | camino del daemon de Gradle sin permiso de primer plano | §2.71 |

**Prioridad alta (abiertos)**

| # | Proyecto | Hallazgo | Recomendación | Coste | Riesgo |
|---|---|---|---|---|---|
| A1 | Escape | **Sigue abierto (§2.62):** `freddy.glb` pesa 114.1 MB, con nueve texturas de 4096² (≈ 576 MB en VRAM, cálculo). La carga de recursos midió 7.8-10.4 s en esta sesión. | Bajar a 2048² y pasar a JPEG las texturas de color, midiendo con `PruebaCarga` antes y después. | Bajo | Bajo |
| A2 | Ambos | **Sigue abierto:** los arneses (`PruebaMundo` con 12 modos, `PruebaVictoria`, `PruebaDerrota`, `PruebaLogro`, `PruebaLineasIntro`, `PruebaDestelloNyder`… 70 archivos) viven en el scratchpad y hay que copiarlos de sesión en sesión. | Llevar los estables a `tools/tests/` de cada repo, con un script (`-Duser.home` aislado y `MSYS_NO_PATHCONV=1`). | Medio | Bajo |
| A3 | FDAF | El trabajo ajeno sin publicar (FlatLaf en `Main.java` y `.classpath`, `src/libs/flatlaf-3.6.1.jar`) no compila con `build-portable.bat`, que no añade `src/libs` al classpath. Si se publica tal cual, la distribución portable se rompe. | Al integrarlo, agregar el jar al `javac`/`java` de `build-portable.bat` y a `build-distribucion.bat`. | Bajo | Medio |

**Prioridad media**

| # | Proyecto | Hallazgo | Recomendación | Coste | Riesgo |
|---|---|---|---|---|---|
| M1 | FDAF | **Intro de la Noche 1:** AWT la reproduce 0.7-1.0 s más lenta que la suma de sus retardos (medido, §1.64). Su temporizador (20.4 s) corta el reloj final ~1 s antes. Las noches 2-5 difieren menos de 0.1 s. | Reprogramar el fin de la intro desde el momento real de las franjas (ya detectado): tiempo restante = duración total − retardos hasta ese cuadro. | Bajo | Bajo |
| M2 | Escape | La IA (`ChaseState`) se mueve con `resolveMovement` sin el barrido vertical del escalón (§2.65). Un animatrónico que baje del escenario hacia una mesa podría quedar incrustado. **Hipótesis por lectura de código, no reproducida**; el detector de atascos existente lo reubica. | Usar `resolveMovementConEscalon` también para la IA (con su base Y). | Bajo | Bajo |
| M3 | Escape | `freddy_music_box.wav` (18.4 MB) sigue cargado como `Sound` (decodificado entero). El audio de `assets/audio` pesa 41 MB. | `Music` (streaming) u OGG. | Bajo | Bajo |
| M4 | Escape | `GameplayScreen.java` creció a 3160 líneas, y eso que esta sesión sacó lo nuevo a clases propias (`FreddyExterior`, `PoseProcedural`, `EstadoVentana`). | Seguir extrayendo solo al tocar una zona. | Alto | Alto |
| M5 | FDAF | **Corregido en esta sesión.** Si `ReiniciadorJuego` no podía relanzar el proceso al volver de Escape (`siFalla`), el agradecimiento y la estrella se recogían recién en el **siguiente** arranque. Ahora los dos respaldos (`ControllerMenu.lanzarEscapeDesdeCustomNight` y `ControllerInterfaz.reiniciarJuegoCompleto`) pasan por `ControllerMenu.volverAlMenuSinReiniciar()`, que consume la victoria pendiente antes de mostrar el menú. | — | — | — |
| M6 | Ambos | **Camino de desarrollo de Escape** (`gradlew lwjgl3:run`): el juego es hijo del daemon de Gradle. Eso explica el problema de foco (§2.71) y, según la memoria del proyecto, deja procesos huérfanos si se corta. La distribución empaquetada no tiene ninguno de los dos problemas. | Para probar, preferir `lwjgl3:installDist` + `lwjgl3.bat`, también en `LanzadorEscape` cuando exista el build. | Medio | Bajo |

**Prioridad baja**

| # | Proyecto | Hallazgo | Recomendación | Coste | Riesgo |
|---|---|---|---|---|---|
| B1 | Escape | Faroles, faros y linterna siguen sin sombras (gdx-gltf 2.3.0, §2.62). Freddy corriendo bajo el farol 2 no proyecta sombra propia. | Calcomanía de sombra que lo siga, o aceptar la limitación. | Bajo | Ninguno |
| B2 | Escape | El corte trasero de la persecución muestra a Freddy muy cerca de la cámara al empezar (9.2 s). Se ve bien en las capturas, pero con otra relación de aspecto podría llenar el cuadro. | Revisar en 21:9 y 4:3. | Bajo | Bajo |
| B3 | FDAF | `ControllerMenu.java` pasó de 908 a 1129 líneas (intro, destello, logro, noches bloqueadas). | Extraer la secuencia de inicio (selector/NYDER/advertencia) a su propia clase cuando se vuelva a tocar. | Medio | Medio |
| B4 | FDAF | `Juego.java` del trabajo ajeno vuelve a poner la batería extra desde la Noche 3 (§1.5 documentaba que ya no tenía ese límite). Cambio de diseño pendiente de su autor. | Decidirlo con quien lo escribió antes de publicarlo. | Bajo | Bajo |
| B5 | Ambos | La documentación de cada sesión vive en un único `CLAUDE.md` de 2600+ líneas. | Partir por proyecto o por año cuando moleste. | Bajo | Ninguno |

**Revisado sin hallazgos nuevos:**
- Progreso y logros: guardado atómico, migración conservadora, la estrella sobrevive a idioma, volumen y Nueva Partida.
- i18n de todo lo nuevo, en ES y EN.
- Liberación de temporizadores y sonidos nuevos al omitir (NYDER, intros, agradecimiento, derrota y victoria).
- Rendimiento tras los cambios (§2.70).
- Distribución: `build-portable.bat` compila por dependencias desde `Main` y copia `resources/`; Escape empaqueta `assets/` en su jar.
- Colisiones: puertas, bordes y sprint, todo en verde (§2.65).

### 1.68 Pruebas de conjunto y publicación v2.12 (FDAF) y `2189857` (Escape), sesión 2026-10-23 (tarde)

Todas con perfil aislado (`-Duser.home`). El `config.properties` real del jugador no se tocó. Tipos de prueba: análisis de malla y GIF con Python, arneses sobre los controladores y pantallas reales (simulación de eventos), ejecución real con temporizadores, gifs y audio reales, capturas de render revisadas y mediciones. **Nadie jugó con teclado y ratón físicos ni escuchó los sonidos nuevos.**

**FDAF**, sobre el árbol exacto del índice (`git checkout-index`, sin el trabajo ajeno: FlatLaf, `Juego.java`, `NYDER.png` y los PNG/carpetas sin seguimiento), compilado desde cero con JDK 1.8.0_271:
- `VerificarRecursos`: 198 rutas OK, 0 fallos.
- `OverlayPuertas`: 20/20.
- `PruebaHover`: 57/57 en ES y EN. Las 5 noches bloqueadas ya no suenan, que es lo esperado por §1.66, y la Noche 1 superada sí.
- `PruebaHoverVuelta`: 72/72.
- `FlujoIdioma` ES/EN: menú `v 2.12`.
- `MedirIdioma`: 0 desbordes.
- `PruebaIntros`: las 10 intros.
- `RenderHud`: noches 1/3/5 × 2 idiomas.
- `PaginasInfo`: 0 problemas.
- `PruebaIntroLlamada`: un único fin de llamada.
- `PruebaRendirse2`: toque, mantener y natural.
- `PruebaLogro`: 26/26 por idioma.
- `PruebaLineasIntro`: sonido entre −1 y +4 ms de las franjas, una vez por intro.
- `PruebaDestelloNyder`: ES, EN y omitida.
- `FondoMenuBordes`: 76 964 → 0 píxeles blancos.

**Escape**, `lwjgl3:installDist` con JDK 21 y arranque real, 18 baterías con **0 fallos**:
- `PruebaMundo`: puertas, bordes, sprint, aturdir, cortina, intro, escalón, mesas y foco.
- `PruebaVictoria`: completa y omitida, en ES y EN.
- `PruebaDerrota`: última vida, omitida y atrapada normal.
- `PruebaArranque`.
- `PruebaCarga`: recursos en 10.3 s y nivel en 3.3 s.

Además: `PruebaRendimiento` (§2.70), el modo `diag` de la derrota (§2.67) y la ejecución real del lanzador para el foco (§2.71).

**Recursos:**
- Nuevos: `fdaf/audio/sfx/nyder_star_sparkle.wav` y `escape/audio/sfx/freddy_run_step.wav`, ambos sintetizados con scripts del repo.
- Regenerados con Blender 5.1.2 desde sus fuentes: `car_sedan.glb` y `exterior_parking.glb`.
- Catálogo (`docs/catalogo_recursos.md` y su copia) y espejos de `FDAF assets` actualizados y comparados con `cmp`.
- Ningún recurso externo nuevo.

**Publicación:**
- Escape `2189857`, verificado con `git ls-remote`.
- FDAF: este commit sube `Version.MENOR` de 11 a 12 y publica **v2.12** (§1.32). Agrupa §1.63-§1.68 y la documentación de §2.63-§2.72.

**Pendiente:** ver la auditoría (§2.72). En particular, el caso real de foco con un jugador (vía ALT no ejercitada), la escucha humana de los sonidos nuevos y el trabajo ajeno sin publicar.

### 1.69 La presentación de NYDER GAMES exportada como video con transparencia, sesión 2026-10-23 (noche)

**Pedido:** un recurso reutilizable fuera de FDAF con solo las letras de NYDER GAMES, la estrella de la D y su destello, con **fondo transparente real**, sin el negro de la pantalla de inicio.

**Cómo se construye el efecto (investigado):** no existe una animación original ni una secuencia previa. Se compone en tiempo real en `PnlWarning` (§1.65):
- el PNG del logo (`branding/nyder_games_splash.png`, 2172 × 724 RGBA) con el alfa del fundido lineal (`ControllerMenu.animarFade`);
- una máscara de la estrella obtenida por color del propio PNG, aclarada según el brillo;
- un halo radial y una cruz de cuatro puntas.

**Exportación (`tools/branding/`, nuevo):**
- `ExportarNyderAlfa.java` dibuja cada cuadro con **la misma clase del juego** (`PnlWarning(logo, 1.0)` con `setOpaque(false)`) sobre una `BufferedImage` ARGB vacía. Usa los tiempos reales: entrada 800 ms, visible 1300 ms y salida 650 ms, con el destello de 120/420/60/600 ms de §1.65.
- No se redibuja nada, así que la tipografía, los colores, el suavizado y el destello son idénticos a los del juego.
- Se omiten el cuadro del inicio y el del final con alfa 0 exacto (serían cuadros vacíos).
- `exportar_nyder_alfa.sh` compila solo `PnlWarning` y sus dependencias con JDK 8 y renderiza la secuencia. Después codifica con FFmpeg 7.1 (si no hay uno en el PATH, usa el binario del paquete de Python `imageio-ffmpeg`).

**Formatos elegidos (equilibrio entre compatibilidad, tamaño y calidad):**

| Archivo | Formato | Para qué | Tamaño |
|---|---|---|---|
| `nyder_games_alpha.webm` | VP9 `yuva420p` (`-auto-alt-ref 0`, `alpha_mode=1`) + Opus | web, motores, OBS | 1.2 MB |
| `nyder_games_alpha_prores4444.mov` | ProRes 4444 `yuva444p10le`, alfa de 16 bits (`-qscale:v 9`) + PCM | editores de video | 43 MB (257 MB con la calidad por defecto de `prores_ks`) |
| `png_sequence/` (82 PNG RGBA) | maestro sin pérdida | volver a codificar, secuencias en motores | 19 MB |

Las tres versiones duran 2.733 s (82 cuadros a 30 fps) en 1920 × 800. El logo ocupa 1920 × 640 centrado; el margen deja sitio al chispazo, que sobresale por arriba. El sonido del destello va sincronizado como en el juego (0.93 s desde el primer cuadro). No se generó HEVC con alfa para Safari: el FFmpeg disponible no lo codifica.

**Ubicación:** `FDAF assets/branding/nyder_games_animation/` (carpeta nueva `branding/` para recursos de marca reutilizables, registrada en el README de `FDAF assets` y en el catálogo). Contiene los tres formatos, el WAV del destello, `verification/` y un `README.md` con formato, resolución, duración, fps, tipo de transparencia, herramienta, original y cómo reutilizarlo. Los videos no están en el repositorio: se regeneran con el script.

**Verificación (análisis de imagen, no a ojo en un editor):**
- El WebM y el MOV se decodificaron de vuelta a PNG (`libvpx-vp9` para el alfa de VP9). Los dos dan **RGBA**, con el mismo número de cuadros que el maestro.
- Error de alfa respecto al maestro: medio 0.1-0.2/255. Máximo 24/255 (WebM, en bordes) y 1/255 (MOV).
- Compuestos sobre **blanco** y sobre **rojo**, la diferencia con el maestro compuesto es como mucho de 4.6-5.0/255 (WebM) y 2.7-3.0/255 (MOV) en el percentil 99.9: sin halos claros ni oscuros en los bordes.
- En todos los cuadros las esquinas tienen alfa 0. El primer cuadro tiene alfa máximo 0.043 y el último 0.027, es decir, empieza y termina en el fundido, sin cuadros vacíos ni negros.
- Hoja de prueba con 5 instantes (fundido, subida, pico, bajada y salida) sobre blanco, rojo y rayado: no hay ningún rectángulo, el destello queda en la estrella y los bordes se ven suaves. Las letras son blancas por diseño y sobre blanco solo se distingue la estrella (anotado en el README).
- **La presentación del juego no cambia:** no se tocó ningún archivo del juego para esto.

**Archivos:** `tools/branding/ExportarNyderAlfa.java` y `tools/branding/exportar_nyder_alfa.sh` (nuevos), `docs/catalogo_recursos.md`; en `FDAF assets`: `branding/nyder_games_animation/` y `README.md`.

### 1.70 Estrella de Escape completado: blanca y del tamaño de "Freddy's", sesión 2026-10-23 (noche)

**Pedido:** la estrella del menú (§1.66) era dorada y del tamaño de las mayúsculas (68 px en 1080p). Debía ser **blanca** y de un tamaño visual equivalente al de la palabra "Freddy's", integrada con el título.

**Cambios:**
- `EstrellaLogro.pintarBlanca` / `iconoBlanco` (nuevos): la misma estrella de cinco puntas, ahora blanca.
  - Degradado muy leve (de blanco a 222 de gris abajo), borde oscuro fino y una sombra suave desplazada para que se lea sobre las franjas claras de la estática.
  - Sin resplandor, porque un halo blanco la emborronaría junto al texto.
  - El icono se dimensiona por su **contorno visual**: una estrella de radio r mide 1.809 r desde la punta de arriba hasta las de abajo.
- `PnlMenu.ubicarEstrellaEscape`:
  - El alto sale del contorno real de los glifos de "Freddy's" (`Font.createGlyphVector(...).getVisualBounds()`), de la parte de arriba de la F al final de la y.
  - Se coloca un cuarto de su alto a la derecha de la palabra, con su parte de arriba a la altura de la de la F.
  - La línea base sale del fondo de la caja real de la línea del `JTextPane` (`modelToView`). Con `fm.getAscent()` quedaba 10 px más arriba (medido en la captura).
- La estrella dorada sigue en la presentación de la pantalla de agradecimiento (§1.67), que no se pidió cambiar.

**Sin cambios:** el desbloqueo persistente (`escapeCompletado`, §1.66), las condiciones para obtenerla y el menú de quien todavía no completó Escape (sin estrella).

**Verificación (render real del menú, `RenderMenuReal`, 1920×1080, perfil aislado):**
- Píxeles medidos en ES y EN: palabra "Freddy's" de y 399 a 479 (la F empieza en 404); estrella de y 404 a ~477, en x 687-762, con 34 px de separación tras la "s". Es decir, del alto de la palabra, alineada con ella y sin tocar ninguna letra.
- Sin la estrella desbloqueada, el menú se ve igual que antes.
- `PruebaLogro`: 26/26 en ES y EN (la estrella no tapa botones y se recupera al volver a abrir).
- La posición y el tamaño se calculan con las métricas de la fuente ya escalada, así que siguen al escalado de cada resolución. Las otras resoluciones **no se renderizaron**: el escalado de FDAF depende del monitor físico (§1.36).

**Archivos:** `EstrellaLogro.java`, `PnlMenu.java`.

### 2.73 Pasos de los animatrónicos sincronizados con la animación (interior y persecución), sesión 2026-10-23 (noche)

**Pedido:**
- Los pasos de Freddy en la persecución del estacionamiento (§2.68) sonaban metálicos. Debían ser mucho más pesados, con capas y variación, sincronizados con las pisadas de la animación, atenuados por distancia y sin salto de volumen en el corte de cámara al maletero.
- Además, pasos para los cuatro animatrónicos dentro de la pizzería, cada uno con su carácter: Freddy grave, Bonnie mecánico, Chica pesado y con resonancia, Foxy urgente.
- Debían sonar solo al desplazarse, sincronizados y espaciales, y nunca en pausa, jumpscares ni cinemáticas. Sin duplicados y liberando los recursos.

**Antes:** `FreddyExterior` reproducía `freddy_run_step.wav` con un temporizador fijo de 0.31 s, sin relación con la animación. El análisis del archivo daba un 10.7 % de energía entre 1 y 4 kHz, con parciales inarmónicos de 1240/1873/2711/3907 Hz (el "chasquido metálico" de su script). Dentro de la pizzería no había pasos.

**Sonidos nuevos (`tools/audio/synth_animatronic_steps.py`, sintetizados sin audio externo):**
- 20 WAV en `assets/audio/sfx/steps/`: `{freddy_chase,freddy,bonnie,chica,foxy}_step_{1..4}.wav`, mono 44.1 kHz 16 bit, pico a -1 dBFS, 0.75-1.25 s con la cola de reverberación.
- Cada pisada tiene cuatro capas:
  1. impacto grave: seno con caída de tono y saturación suave, para que el peso se oiga también en altavoces sin graves;
  2. cuerpo estructural: resonancias del suelo o del armazón;
  3. componente mecánica secundaria: servo o articulación, filtrada por debajo de 0.9-1.8 kHz;
  4. reverberación corta: sala interior o rebote abierto en el estacionamiento.
- Cuatro variaciones por personaje, con semilla fija para que sean reproducibles.
- Carácter de cada uno:

| Sonido | Carácter | Energía 1-4 kHz |
|---|---|---|
| `freddy_chase` | asfalto, muy pesado y sordo, roce áspero | 0.2-0.7 % (antes 10.7 %) |
| `freddy` | grave y lento, el suelo vibra | 0.1-1.5 % |
| `bonnie` | articulación que encaja y servo presentes, filtrados | 0.4-0.9 % |
| `chica` | armazón hueco: resonancias de 110-430 Hz que duran 0.12-0.22 s | 0.1-0.7 % |
| `foxy` | más liviano, seco y rápido, con raspado del pie | 0.2-0.8 % |

- Se quitaron `freddy_run_step.wav` y `tools/audio/synth_freddy_chase.py` (solo generaba ese archivo).
- Espectrogramas (antes y después): sin líneas tonales por encima de 1 kHz.

**Sincronización (`audio/PasosAnimatronicos`, nuevo):**
- El apoyo se detecta en la pose real de cada cuadro, después de `sceneManager.update`. Se sigue la altura en el mundo del hueso de cada pie sobre el origen del personaje.
- La pisada suena cuando el pie, tras haberse levantado más del 60 % de su recorrido vertical, baja al 25 % inferior.
- Comparado con cuadros renderizados del ciclo:
  - Freddy apoya el pie izquierdo a 0.63 s y el derecho a 0.91 s. En los cuadros se ve asentarse a 0.60-0.65 y a 0.90-0.95.
  - Es un único apoyo por pie y por ciclo en los cuatro personajes. Con 20/50 % el pie derecho de Foxy daba dos.
- **El punto más adelantado del pie NO sirve en estos rigs.** Era la primera hipótesis y era simétrica, pero el pie llega adelante todavía en el aire (paso alto) y apoya 0.35 s después. Se descartó al mirar los cuadros, antes de dar nada por bueno.
- Al acelerar la animación (Freddy corre al 1.9x), los apoyos se acercan solos.
- **Calibración al cargar:** el recorrido de cada pie se mide una vez sobre el clip `walk`, en una `Scene` nueva del mismo modelo. Así no se toca la instancia que se dibuja y la primera caminata ya detecta bien sus apoyos. Sin calibrar, la primera pisada de Freddy salía con el pie todavía en el aire (la prueba lo detectó).
- **Gotcha:** un `ModelInstance` suelto con un `AnimationControllerHack` no aplica la pose (los huesos quedaban en 0). Hace falta `new Scene(model)`, que marca los huesos animados.
- **Ritmos resultantes** (intervalos entre pisadas): Freddy 0.28/0.72 s, Bonnie y Foxy 0.17/0.83 s, Chica 0.38/0.68 s. Son asimétricos porque así son los ciclos retargeteados: el sonido sigue a lo que se ve.

**Cuándo suena:**
- Solo con la animación `walk` activa y desplazamiento real (más de 0.25 m/s, velocidad suavizada).
- Un animatrónico aturdido (pasa a `idle`) o trabado contra una pared no suena.
- `GameplayScreen` lo actualiza en `JUGANDO` y en la cinemática de victoria (la persecución de Freddy).
- No se actualiza en pausa, jumpscares, la cinemática de derrota, la cinemática inicial ni las instrucciones.
- `detenerTodo()` corta lo que suena al entrar al jumpscare, al jumpscare de Golden Freddy y a las dos cinemáticas.
- La pausa pausa y reanuda las pisadas que estaban sonando (`pausarAudioContinuo`/`reanudarAudioContinuo`).
- **Volumen y espacialidad:**
  - El volumen sigue `(1 - d/dMax)²` con la distancia horizontal, pleno a menos de `distanciaMinima`. Las pisadas por debajo del 1 % no se reproducen.
  - El paneo usa el eje derecho de la cámara (el mismo cálculo que la caja musical).
  - La ganancia se suaviza con una constante de 0.3 s, de modo que el corte de cámara al maletero no produce un salto.
- Cada pisada elige una variación distinta de la anterior, con tono (según el perfil) y volumen (88-100 %) aleatorios.
- Una sola instancia por entidad (registrar dos veces no duplica). Nunca dos pisadas del mismo personaje a menos de 0.1 s.

**Datos (sin nombres en Java):**
- `EntityDefinition.pasos` es un mapa de perfiles. "interior" está en los cuatro y "carrera" solo en Freddy, con sus sonidos, volumen, distancias y rango de tono.
- Los huesos de los pies van en `huesos` (`pieIzquierdo`/`pieDerecho`):
  - Freddy: `j_l_foot_056`/`j_r_foot_052`;
  - Bonnie y Foxy: `bip_foot_{L,R}_*.qc_skeleton`;
  - Chica: `Bone.014_Armature_167`/`Bone.015_Armature_186`.
- `FreddyExterior.iniciarPersecucion` cambia a "carrera". Su temporizador y `SONIDO_PASO` desaparecieron, y `actualizarPersecucion` ya no recibe la cámara.
- `BootScreen` encola los sonidos de los perfiles (`PasosAnimatronicos.sonidosDe`).

**Verificación:**
- **Juego real con IA real** (`PruebaMundo pasos`, con el `deltaTime` de la IA fijado al de la prueba): cada animatrónico persigue 3.3 s por el tramo libre más largo (z=1, 17 m). 25/25 OK:
  - 6 pisadas detectadas y reproducidas cada uno, con los intervalos de arriba;
  - en cada pisada un pie está apoyado (en el 30 % inferior de su recorrido, medido por la prueba con sus propios datos);
  - quieto 2 s: 0 pisadas; en pausa 1 s: 0 pisadas;
  - jumpscare (Bonnie atrapa a los 2.1 s): ninguna pisada durante el susto.
- **Gotcha del arnés:** `ChaseState` lee `Gdx.graphics.getDeltaTime()`. Con cuadros más rápidos que el tiempo real, los guardias avanzaban 0.4 m en 3 s (y, con razón, no sonaban). Además, a 13 m Bonnie y Foxy (alcance de 15 m) quedaban por debajo del 1 % y no se reproducían. Por eso hay dos contadores, `pisadasDe` (detectadas) y `audiblesDe` (reproducidas).
- **Persecución real** (`PruebaVictoria completa`):
  - 18 pisadas en 5.0 s (3.6 por segundo, el ritmo de la animación al 1.9x), todas audibles;
  - en el corte de cámara la distancia cámara-Freddy salta de 3.0 a 0.2 m, y la ganancia sube de 0.89 a 0.97 en 0.3 s (máximo 0.063 por cuadro);
  - todo lo demás del recorrido de victoria sigue en OK.
- **No se escuchó con oídos humanos.** Las afirmaciones sobre el timbre salen del análisis espectral y del diseño de las capas, no de una escucha.

**Archivos:**
- Nuevos: `audio/PasosAnimatronicos.java`, `tools/audio/synth_animatronic_steps.py` y `assets/audio/sfx/steps/` (20 WAV).
- Modificados: `EntityDefinition.java`, los 4 JSON de entidades, `FreddyExterior.java`, `CinematicaVictoria.java`, `GameplayScreen.java` y `BootScreen.java`.
- Eliminados: `assets/audio/sfx/freddy_run_step.wav` y `tools/audio/synth_freddy_chase.py`.

### 2.74 Susto de Freddy en la puerta de emergencia, sesión 2026-10-23 (noche)

**Pedido:** al acercarse a Freddy en el estacionamiento debía haber un susto real, no solo la amenaza de §2.68. Requisitos:
- activación controlada, una sola vez;
- movimiento visual, sonido y refuerzo de cámara;
- sin derrota ni victoria prematura.

**Diseño (`FreddyExterior.actualizarSusto`):**
- **Activación:**
  - el jugador a menos de 2.3 m mirándolo (producto escalar entre la mirada y la dirección a Freddy > 0.8);
  - o a menos de 1.5 m aunque no lo mire.
  - Se mide contra la posición de la puerta, no contra la del modelo, que se mueve con la embestida.
  - `sustoHecho` queda en true para el resto de la partida. La amenaza de §2.68 (giro, servo y risa al volver a acercarse) sigue funcionando.
- **Movimiento** (1.15 s):
  - Freddy embiste hacia el jugador en 0.12 s, hasta 0.45 m y nunca a menos de 0.9 m del ojo.
  - Se queda encima hasta 0.5 s y vuelve despacio a la puerta, con interpolación suave.
  - Mientras dura, gira hacia el jugador a 400 °/s (en la amenaza, a 90 °/s).
  - Con `PoseProcedural`: torso +16° hacia adelante, mandíbula +22° y la cabeza sacudiéndose (ladeo senoidal de ±9°).
  - **Su collider no se mueve:** la embestida es solo visual, así que no empuja ni atrapa al jugador.
- **Sonido**, todo con los recursos ya cargados:
  - el golpe del cuerpo (`defeat_body_thud`) a tono 0.8;
  - el servo a 1.15;
  - el grito del jumpscare a volumen 0.55 y tono 0.82, cortado a los 0.75 s para que el grito completo siga siendo el del jumpscare de verdad.
  - La risa de la amenaza no se suma encima.
  - Si se pausa durante el susto, el grito se corta (`detenerSonidosSusto`).
- **Cámara** (`GameplayScreen.aplicarSustoCamara`, con `getIntensidadSusto()`: sube en 0.08 s y se apaga en el resto):
  - temblor de posición de hasta 6 cm;
  - giro de hasta ±3°;
  - el campo de visión se cierra de 67° a 57°.
  - Es solo cosmético: la posición del jugador no cambia, y `applyToCamera` repone posición, dirección y `up` en el cuadro siguiente. Al terminar se restaura `FOV_BASE` (nueva constante, 67°).
- **Sin efectos de juego:** el estado sigue en `JUGANDO`, no se toca `escapado` ni las vidas. Si la cinemática del auto empieza (`iniciarPersecucion`), el susto se cancela y Freddy sale corriendo desde la puerta.

**Verificación** (`PruebaVictoria completa`, recorrido real; 12 comprobaciones nuevas, 37/37 OK en total):
- a 4.4 m no hay susto;
- acercándose mirándolo, se activa a 2.25 m;
- embestida de 0.45 m; la distancia mínima entre el ojo y Freddy es de 1.75 m;
- el campo de visión llega a 57.2° y vuelve a 67.0°;
- Freddy vuelve exactamente a la puerta (0.0 m);
- el estado es siempre `JUGANDO`, sin `escapado`;
- el collider es idéntico antes y después;
- al alejarse y volver a acercarse, la intensidad máxima es 0 (una sola vez);
- el resto del recorrido (colisiones con Freddy, límites del exterior, cinemática) sigue igual.
- **Capturas:** `07c_susto_embestida` (cámara cerrada, cabeza ladeada, el cuerpo echado fuera del marco) y `07d_susto_vuelve`.
- **No se jugó ni se escuchó con una persona:** el impacto del susto no está evaluado por un humano.

**Archivos:** `FreddyExterior.java` (el constructor recibe además el grito y el golpe) y `GameplayScreen.java`.

### 2.75 Menú de pausa completo y volumen por categorías en Escape, sesión 2026-10-24

**Pedido:** un menú de pausa real. Antes era un solo botón "Salir al menú principal", sin forma de volver salvo con ESC. Debía tener:
- Continuar;
- Volumen: deslizadores general, música y efectos, aplicados al instante y persistentes;
- Controles: solo las teclas que existen (WASD, ratón, SHIFT para correr, CTRL linterna, E, ESC);
- Salir o volver al menú, y opcionalmente reiniciar.

Además: internacionalizado, con buen diseño, el mundo realmente congelado, el cursor restaurado y probado con ALT+TAB.

**Volumen (`audio/VolumenAudio`, `audio/SonidoConVolumen`, nuevos):**
- Tres niveles de 0 a 1, guardados en las preferencias de Escape `five_doors_escape` (las mismas del consejo de carga): `volumen.general`, `volumen.musica`, `volumen.efectos`. Por defecto, 1: la mezcla anterior no cambia para quien no toque nada.
- La ganancia aplicada es `(general × categoría)²` (curva perceptual).
- Un solo punto de aplicación: `AssetService.getSound` devuelve cada `Sound` envuelto en un `SonidoConVolumen` de su categoría, uno por ruta. Es música lo que está bajo `audio/music/` (caja musical de Freddy, circo de la victoria); todo lo demás son efectos (voces incluidas).
  - El juego sigue pidiendo sus volúmenes "de diseño" como antes. El envoltorio los multiplica y recuerda volumen y paneo de las últimas 48 instancias, para reaplicarlos en vivo al mover un deslizador. Así un bucle (caja musical, ambiente nocturno, latidos) cambia al instante, sin reiniciarse.
  - Sin paneo pedido se reaplica con `setVolume`, porque `setPan` también mueve la fuente en OpenAL.
  - `dispose()` del envoltorio no libera el sonido real, que sigue siendo del `AssetManager`.
  - `EscapeVictoryScreen`, que crea su música directamente, también la envuelve.
- No hubo que tocar ninguna de las 46 llamadas `play`/`loop` del juego.

**Menú (`screens/MenuPausa`, clase nueva para no seguir engordando `GameplayScreen`, pendiente M4):**
- Paneles:
  - **principal:** Continuar, Volumen, Controles, Reiniciar partida y Salir al menú principal;
  - **Volumen:** tres deslizadores con su porcentaje, más Volver;
  - **Controles:** las mismas seis filas de la pantalla de instrucciones (`instr.*`), así que no puede listar una tecla que no exista;
  - **confirmación:** para reiniciar y para salir ("Se perderá el progreso de esta partida"). El foco empieza en No, para que un ENTER de más no borre la partida.
- **Entrada:**
  - Ratón: pasar por encima elige; clic acepta; los deslizadores se arrastran o se pulsan, con un margen vertical generoso.
  - Teclado: W/S o flechas para elegir, A/D o flechas para ajustar el volumen un 5 %, E/ENTER/ESPACIO para aceptar.
  - ESC en un subpanel vuelve al principal; en el principal cierra el menú.
- **Diseño:** fondo oscurecido al 72 % sobre la escena congelada, panel oscuro con un filete rojo arriba, botón elegido en rojo con una marca a la izquierda, y una línea de ayuda abajo. Escala con la altura de la pantalla (`h/1080`). Tiene su propia `BitmapFont` con filtro lineal (la del HUD usa vecino más cercano y escalada se veía desigual) y la libera en `dispose`.
- **Textos:** 16 claves nuevas `pause.*` en ES y EN, con ortografía completa ("Música", "¿Salir al menú principal?", "Sí"). La `BitmapFont` por defecto sí tiene los glifos (comprobado: á é í ó ú ñ ¿ ¡), y los `.properties` se leen en UTF-8. `pause.exitToMenu` pasó de "menu" a "menú". El resto de las cadenas antiguas de Escape siguen sin tildes: corregirlas es un cambio aparte.
- **Persistencia:** se guarda al salir del panel de Volumen, al cerrar el menú, al reiniciar y al salir (solo si cambió algo).

**`GameplayScreen`:**
- `abrirPausa()` (ESC o pérdida de foco, §2.71) detiene el mundo como antes (`sceneManager.update(0)`, sin `engine.update`), pausa el audio continuo y las pisadas (§2.73), libera el cursor y abre el menú en el panel principal.
- `reanudarDesdePausa()` (Continuar o ESC) recaptura el cursor. Descarta 2 cuadros de movimiento del ratón (`FirstPersonCameraController.ignorarMovimientoRaton`), porque al recapturar el primer delta es un salto. Después reanuda el audio.
- **Reiniciar:** crea una `GameplayScreen` nueva con el mismo `AssetService` (sin recargar nada). La vieja no libera los assets en su `dispose` (`conservarAssets`). La nueva salta la introducción (`reinicio=true`) y empieza en las instrucciones.
- **Salir:** `Gdx.app.exit()`, el mismo cierre que FDAF detecta para volver a su menú (§2.13).
- Se quitaron `dibujarMenuPausa` y sus constantes.

**Verificación** (`PruebaMundo pausa`, juego real, ratón simulado en el proxy de `Input`):
- 30/30 OK en español y 30/30 en inglés (perfil aislado `home_esc`).
- Con Bonnie persiguiendo y W apretada durante 1.5 s en pausa, ni Bonnie, ni el jugador, ni el reloj de juego se movieron. Al reanudar, Bonnie siguió persiguiendo.
- Navegación con teclado y con ratón (hover y clic en Continuar).
- Volumen general de 1.0 a 0.8 con 4 pulsaciones de A; ganancia de efectos 0.64.
- El deslizador de música arrastrado con el ratón quedó en 0.30. El archivo de preferencias quedó con `volumen.general=0.8`, `volumen.musica=0.3` y `volumen.efectos=1.0`.
- ESC en un subpanel vuelve sin cerrar la pausa. Salir y Reiniciar piden confirmación con No elegido, y No vuelve.
- Al reanudar: cursor recapturado (`setCursorCatched(true)`) y libre en la pausa.
- **ALT+TAB simulado** (`EstadoVentana.alCambiarFoco(false)`): abre el menú; al volver el foco, sigue en pausa.
- Reiniciar con Sí:
  - pantalla nueva en `INSTRUCCIONES`, con los mismos 11432 colliders y las vidas completas;
  - el volumen elegido se conserva;
  - la partida nueva se juega y su pausa funciona.
- **Gotcha de la prueba:** las zonas del ratón son las del último cuadro dibujado. Tras cambiar de panel hay que dibujar un cuadro antes de leerlas.
- **Capturas** (1280×720): `pausa_principal_{es,en}`, `pausa_volumen_arrastre_es`, `pausa_controles_{es,en}`, `pausa_confirmar_salida_es` y `pausa_tras_reinicio_es`.
- **No probado con un humano:** el foco real de Windows sigue sin poder reproducirse en este entorno (memoria `feedback-windows-focus-not-reproducible-in-automation`), y la sensación de los deslizadores con un ratón físico no está probada.

**Archivos:**
- Nuevos: `audio/VolumenAudio.java`, `audio/SonidoConVolumen.java` y `screens/MenuPausa.java`.
- Modificados: `AssetService.java`, `EscapeVictoryScreen.java`, `GameplayScreen.java`, `FirstPersonCameraController.java` y `assets/i18n/strings{,_en}.properties`.

### 2.76 Un único consejo en la pantalla de carga de Escape, sesión 2026-10-24

**Pedido:** que la pantalla de carga (`BootScreen`) muestre un solo consejo, con este texto exacto:
- ES: «Los destellos de la linterna pueden aturdir a los animatrónicos: Foxy necesita 3 ciclos, y Bonnie y Chica necesitan 7.»
- EN: «Flash the animatronics to stun them: Foxy needs 3 flash cycles, while Bonnie and Chica need 7.»

**Cambios:**
- `tip.1` tiene ahora ese texto en `strings.properties` y `strings_en.properties`, y se borraron `tip.2` a `tip.7`.
- `BootScreen.elegirConsejo` devuelve siempre `tip.1`. Se quitaron `CANTIDAD_CONSEJOS` y la rotación con `ultimoConsejo`; la entrada vieja que pueda quedar en las preferencias es inofensiva.
- Los números coinciden con `content/balance/flashlight.json` (Foxy 3, Bonnie 7, Chica 7 ciclos). Hay un comentario junto al método para mantenerlos alineados si cambia el balance.
- La tilde de «animatrónicos» se dibuja: la fuente por defecto tiene el glifo.

**Verificación** (`PruebaCarga`, arranque real con perfil aislado; el inglés con un `handoff.json` temporal en el perfil de pruebas, borrado después):
- el consejo leído es el texto exacto en ES y en EN;
- en las capturas, el consejo aparece completo y la «ó» se ve: `carga_es_carga_1`, `carga_en_carga_1`;
- 0 fallos.

**Archivos:** `BootScreen.java`, `assets/i18n/strings{,_en}.properties`.

### 2.77 Pendiente A1 resuelto: texturas de `freddy.glb` reducidas a 2048², sesión 2026-10-24

**Problema (§2.62, §2.72 A1):** `freddy.glb` pesaba 114.1 MB, de los que 105 MB eran nueve PNG de 4096×4096:
- traje: normal, color, metal/rugosidad;
- micrófono y sombrero: los mismos tres;
- endoesqueleto: emisivo, color, metal/rugosidad.

Ocupaban unos 576 MB de VRAM sin mipmaps (cálculo). Los otros tres personajes usan como mucho 2048.

**Herramienta nueva (`tools/assets/reducir_texturas_glb.py`):**
- Reescribe solo los bytes de las imágenes embebidas mayores que el lado máximo: Lanczos, el mismo formato (PNG optimizado; JPEG a calidad 92 si lo hubiera).
- Los mapas de normales (los que un material usa como `normalTexture`) se renormalizan tras reescalar, porque promediar normales las acorta.
- Reubica los bufferViews con alineación de 4 bytes. Todo lo demás queda intacto.
- **Comprobado:** el JSON es idéntico salvo `bufferViews`/`buffers`, y los 486 bufferViews de datos (mallas, piel, animaciones) son idénticos byte a byte. Solo cambiaron las 9 imágenes.

**Resultado:**
- `freddy.glb`: 114.1 MB → **36.3 MB**, con las nueve texturas a 2048² (≈144 MB de VRAM, cálculo).
- No se pasaron las texturas de color a JPEG: a 2048 ya alcanza, y JPEG agregaría artefactos sin ganar casi nada en carga.
- **El original con 4096² se conserva:**
  - `FDAF assets/source_files/3d_models/LibGDX_Ready/Freddy.glb` (idéntico al que había en el repositorio);
  - el historial de git.
- La versión nueva está copiada como `LibGDX_Ready/Freddy_texturas2048.glb`.
- Se puede regenerar o cambiar el lado: `python tools/assets/reducir_texturas_glb.py <original> assets/models/freddy.glb 2048`.

**Medido:**

| | 4096² | 2048² |
|---|---|---|
| `GLBLoader` del archivo solo (`DiagTexturas`) | 4.1 s | 1.3 s |
| Carga completa hasta "Preparando la pizzería" (`PruebaCarga`, 2 corridas cada una, la misma sesión) | 11.16 / 11.38 s | 8.23 / 8.32 s (**−27 %**) |

- Rendimiento con 2048 (`PruebaRendimiento`, 1280×720 sin vsync): comedor 76 fps, persecución 88, estacionamiento 84, victoria 72. Son del mismo orden que en §2.72; no era un cuello de botella de fotogramas.
- **Jumpscare de cerca** (la cara de Freddy a 0.75 m, render de las dos versiones con la misma cámara y luz): diferencia media de 0.17/255, p99 de 3/255. En la comparación lado a lado no se distinguen la piel, los ojos ni el sombrero (`tex_comparacion.png`).

**Archivos:**
- Nuevo: `tools/assets/reducir_texturas_glb.py`.
- Modificado: `assets/models/freddy.glb`.
- Catálogo: fila de `freddy.glb` actualizada, en el repositorio y en la copia.

### 1.71 Pendiente A2 (FDAF): la regresión automática vive en el repositorio, sesión 2026-10-24

**Problema (§2.72 A2):** los arneses de prueba vivían en el scratchpad de cada sesión de Claude y había que copiarlos de una a otra. Nadie más podía ejecutarlos.

**Ahora:** `tools/tests/` en la raíz de `five_doors_at_freddys`.
- `src/` tiene los 15 arneses que usa la regresión: `VerificarRecursos`, `OverlayPuertas`, `PruebaHover`, `PruebaHoverVuelta`, `FlujoIdioma`, `MedirIdioma`, `PruebaIntros`, `RenderHud`, `PruebaLogro`, `PruebaLineasIntro`, `PruebaDestelloNyder`, `PaginasInfo`, `PruebaIntroLlamada`, `PruebaRendirse2`, `FondoMenuBordes`.
  - Se revisó que no tengan rutas absolutas ni datos personales.
  - Los diagnósticos de un solo uso se quedaron fuera.
- `run_tests.sh` (uso: `JDK8_HOME=<jdk1.8 con JavaFX> sh tools/tests/run_tests.sh [rapida]`):
  - Compila `FiveDoorsAtFreddys/src` (con los `.jar` de `src/libs`, si hay) y los arneses en `tools/tests/out/`.
  - Ejecuta cada prueba con `user.home` en `tools/tests/out/home`, recreado para cada una: **nunca toca el progreso real**.
  - `rapida` ejecuta solo el español.
  - `BUILD=<carpeta>` prueba un árbol ya compilado (por ejemplo, el índice exportado sin el trabajo ajeno).
  - Rutas relativas al propio script. En Windows: `cygpath` y `MSYS_NO_PATHCONV=1`.
  - **Gotcha:** con `MSYS_NO_PATHCONV=1`, Git Bash ya no convierte las rutas que se le pasan a `javac`. Las listas de fuentes van en un `@archivo`, con rutas de Windows entre comillas y barras normales.
- `.gitignore`: `/tools/tests/out/`.

> **⚠️ CORREGIDO (§1.75):** la afirmación de abajo era inexacta. `FondoMenuBordes` no funcionaba desde el repositorio: dependía de 70 cuadros PNG extraídos a mano que solo existían en el scratchpad, y fallaba al leer el primero. Su salida no se revisó en esa corrida. Ahora extrae los cuadros del propio GIF del juego.

**Verificado:** `run_tests.sh rapida` desde el repositorio. Las 20 pruebas se compilaron y se ejecutaron, todas en OK, con las mismas cifras que la regresión del scratchpad:
- `VerificarRecursos`: 198 rutas OK;
- `PruebaHover`: 57 OK;
- `PruebaHoverVuelta`: 72 OK;
- logro, líneas de intro y destello: 0 fallos.

**Archivos:** `tools/tests/src/*.java` (15) y `tools/tests/run_tests.sh` (nuevos); `.gitignore`.

### 1.72 Pendiente A3: el empaquetado admite bibliotecas en `src/libs` y los `.bat` quedan en CRLF, sesión 2026-10-24

**Problema (§2.72 A3):** el trabajo ajeno sin publicar (FlatLaf: `Main.java`, `.classpath` y `src/libs/flatlaf-3.6.1.jar`) no compilaba con `build-portable.bat`, porque el script no añadía `src/libs` al classpath. Si se publicaba tal cual, la carpeta portable y `build-distribucion.bat` (que lo llama) se rompían.

**Cambio (sin integrar el trabajo ajeno, que sigue sin tocarse):** `build-portable.bat` admite ahora cualquier `.jar` de `src\libs`, de forma genérica.
- Si los hay, se pasan a `javac` (`-cp "src\libs\*"`) y se descomprimen dentro de las clases antes de crear el JAR. La carpeta portable sigue siendo un único `java -jar FiveDoorsAtFreddys.jar`.
- Se quitan las firmas (`*.SF`, `*.RSA`, `*.DSA`) y el manifiesto de las bibliotecas: el JAR final lleva el suyo, con `Main-Class`.
- **Sin `src\libs`, el comportamiento es exactamente el de antes.**

**Hallazgo al probarlo:**
- En esta copia, los tres `.bat` del repositorio estaban con LF puro en el árbol de trabajo. Con LF, `cmd.exe` pierde caracteres al azar: "EM" y "et" en vez de REM y set, reproducido con la copia de prueba.
- Funcionaba de casualidad, según dónde caían los saltos de línea.
- Se agregó `.gitattributes` (`*.bat text eol=crlf`, también `*.cmd`) para que cualquier clon los reciba en CRLF sin depender de `core.autocrlf`. Los tres `.bat` del árbol de trabajo se normalizaron.
- En el índice siguen en LF (lo normal con `text`), así que `build-exe-launcher.bat` y `build-distribucion.bat` no tienen cambios de contenido.
- Escape ya tenía su `.gitattributes` con la misma regla.

**Verificación:** se ejecutó el script real en una copia que solo cambia las carpetas de entrada y salida, y termina antes de copiar el runtime (cientos de MB) y de generar el `.exe`.
- **Con `src/libs`** (el árbol de trabajo, con el `Main.java` de FlatLaf): compila, el JAR contiene las 333 clases de `com/formdev`, sin firmas, y `javap` resuelve `com.formdev.flatlaf.FlatDarkLaf` desde el JAR.
- **Sin `src/libs`** (el árbol publicado, exportado con `git archive HEAD`): compila y el JAR tiene las mismas 442 entradas de siempre, sin FlatLaf.
- No se generó ni se ejecutó una carpeta portable completa: el paso que se cambió es la compilación y el empaquetado del JAR, y el resto del script quedó igual.

**Para quien publique FlatLaf:** basta con agregar `Main.java`, `.classpath` y el `.jar` de `src/libs`. Los scripts ya no necesitan cambios.

**Archivos:** `FiveDoorsAtFreddys/build-portable.bat` y `.gitattributes` (nuevo).

### 2.78 Pendientes A2 y B5 (Escape): la regresión automática vive en el repositorio, con capturas opacas, sesión 2026-10-24

**Ahora:** `tools/tests/` en `five_doors_escape`.
- `src/` contiene:
  - los arneses de la regresión: `PruebaMundo` (modos puertas, bordes, sprint, aturdir, cortina, intro, escalón, mesas, foco, pasos, pausa), `PruebaVictoria`, `PruebaDerrota`, `PruebaArranque`, `PruebaCarga` y `PruebaRendimiento`;
  - tres diagnósticos útiles para volver a medir, que no forman parte de la regresión: `DiagPisadas` (altura y contactos de los pies en un ciclo, §2.73), `DiagPisadasRender` (vista lateral del ciclo en PNG) y `DiagTexturas` (render de cerca para comparar texturas, §2.77).
  - Sin rutas absolutas ni datos personales.
- `run_tests.sh` (uso: `JAVA_HOME=<jdk-21> sh tools/tests/run_tests.sh [modo...]`):
  - Compila con `gradlew lwjgl3:installDist` y compila los arneses contra sus bibliotecas.
  - Ejecuta desde `assets/` con `user.home` en `tools/tests/out/home`, recreado en cada prueba: preferencias, `handoff` y señales de prueba nunca salen de ahí.
  - Sin modos corre toda la regresión (20 ejecuciones). Los modos sueltos van como `mundo:pausa`, `victoria:completa`, `derrota`, `carga`, `rendimiento`…
  - `.gitignore`: `/tools/tests/out/`.
- **B5:** `Opaco.aplicar` (nuevo, en los arneses) pone el alfa en 255 antes de guardar cada captura. El framebuffer deja alfa < 1 y los PNG se veían lavados o agujereados en un visor (§2.61), aunque en el juego no pasara nada. Comprobado: capturas con alfa mínimo y máximo de 255.
- Los arneses del repositorio ya incluyen lo de esta sesión: encolan los sonidos de pasos (como `BootScreen`), los modos `pasos` y `pausa`, el ratón simulado y el `deltaTime` fijo para la IA.

**Verificado:** la regresión completa desde el repositorio dio 20 de 20 ejecuciones con `FALLOS: 0`, 1773 comprobaciones en OK y ningún FALLO. Incluye `mundo_pasos`, `mundo_pausa`, la victoria completa y omitida en los dos idiomas, las tres derrotas, el arranque y la carga.

**Archivos:** `tools/tests/src/*.java` (10) y `tools/tests/run_tests.sh` (nuevos); `.gitignore`.

### 2.79 Auditoría de ambos proyectos (tercera ronda), sesión 2026-10-24

Revisión del estado tras §2.73-§2.78 y §1.69-§1.72, contrastada con el código y con lo medido en esta sesión.
- **Escape:** regresión completa desde el repositorio, 20/20 ejecuciones con 0 fallos y 1773 OK.
- **FDAF:** `run_tests.sh rapida`, todo en OK.

Prioridad: **A** = arreglar antes de la próxima entrega; **M** = conviene; **B** = cuando se toque la zona.

**Estado de los pendientes de §2.72:**

| Id | Estado |
|---|---|
| A1 (`freddy.glb` 4096²) | **Cerrado** (§2.77): 114 → 36 MB; carga completa de 11.2-11.4 s a 8.2-8.3 s; jumpscare de cerca sin diferencias visibles. |
| A2 (arneses fuera de los repositorios) | **Cerrado** (§1.71, §2.78): `tools/tests/` con `run_tests.sh` en cada repositorio, perfil aislado y rutas relativas. |
| A3 (FlatLaf rompería `build-portable.bat`) | **Cerrado** (§1.72): admite `src/libs` de forma genérica, comprobado con y sin la biblioteca. Además, `.bat` en CRLF con `.gitattributes`. |
| M1 (la intro de la Noche 1 corta ~1 s antes) | **Sigue abierto, revisado.** Para corregirlo hay que conocer el cuadro de las franjas analizando el GIF (12-14 MB) en segundo plano y reprogramar el final. Se tocaría código de GIF/AWT congelado y sensible (§1.7) para ganar ~1 s de la tarjeta final. No se hizo. |
| M2 (la IA sin el barrido del escalón) | **Descartado por análisis estático.** La caja de los guardias está centrada en sus pies (semialto 0.9 m): sobre el escenario cubre de -0.34 a 1.46 m y sigue chocando con el tablero de las mesas (< 0.8 m). El atasco del jugador (§2.65) venía de su caja centrada en los ojos, que quedaba por encima de la mesa. No se reprodujo porque no puede darse con esa geometría. |
| M3 (`freddy_music_box.wav` de 18.4 MB como `Sound`) | **Sigue abierto, baja a B.** Son 18 MB de RAM, sin problemas medidos. Pasarlo a `Music` obligaría a duplicar en `Music` el paneo y el volumen de categoría que ahora están en `SonidoConVolumen`. |
| M4 (tamaño de `GameplayScreen`) | **Sigue abierto.** 3212 líneas (+52 netas): lo nuevo se sacó a clases propias (`MenuPausa`, `PasosAnimatronicos`, `VolumenAudio`), pero el enganche suma. |
| M5 | Cerrado (§2.72). |
| M6 (camino de desarrollo con gradle) | **Sigue abierto, revisado.** Usar el `installDist` cuando exista podría lanzar un build viejo si el código cambió después. Habría que comparar fechas con las fuentes y los assets antes de elegirlo. Sin cambio. |
| B5 de §2.62 (capturas con alfa) | **Cerrado** (§2.78, `Opaco`). |
| B1-B4, B5 de §2.72 | Sin cambios. |

**Hallazgos nuevos:**

| Id | Proyecto | Hallazgo | Recomendación | Esfuerzo | Riesgo |
|---|---|---|---|---|---|
| A4 | Ambos | Antes de esta publicación, el trabajo ajeno sigue sin publicar: FlatLaf, `Juego.java` (batería desde la Noche 3), `NYDER.png`, los íconos sin seguimiento, `if3001 - pf - grupo#1/` y `dev/` en Escape. Los commits de esta sesión no lo incluyen. | Que su autor decida. `build-portable.bat` ya está preparado para FlatLaf (§1.72). | Bajo | Medio |
| M7 | Escape | Los pasos (§2.73) no tienen oclusión: un animatrónico detrás de una pared suena igual que uno en un pasillo abierto (solo distancia y paneo). | Atenuar si un rayo cámara-animatrónico cruza un collider estático (filtro de volumen; libGDX `Sound` no tiene pasabajos). | Medio | Bajo |
| M8 | Escape | El volumen de Escape (§2.75) es independiente del de FDAF (escala 1-10 en `config.properties`): quien bajó el volumen en FDAF lo encuentra al 100 % en Escape. | Pasar el volumen general de FDAF en el `handoff` y usarlo como valor inicial si Escape no tiene preferencias propias. | Bajo | Bajo |
| M9 | Escape | **Resuelto en §2.80** (eran dos texturas de 4096² de `main_material`, ahora de 2048²). `pizzeria.glb` (71 MB) tiene una textura de 4096² (`main_material_metallicRoughness`, ≈64 MB de VRAM); el resto del archivo es geometría (1.3 M triángulos). | `reducir_texturas_glb.py ... 2048` y revisar el mapa en capturas. | Bajo | Bajo |
| B6 | Escape | Los ciclos de caminar retargeteados son asimétricos (Freddy apoya a 0.63 y 0.91 s de un ciclo de 1 s; Bonnie y Foxy a 0.17/0.83). El sonido lo reproduce fielmente y se oye "cojo", igual que se ve. | Corregir los clips en Blender si molesta (§2.6, pipeline Rokoko). | Medio | Medio |
| B7 | Escape | Las cadenas antiguas de `strings*.properties` siguen sin tildes ("menu", "Musica" en otras pantallas…), aunque la fuente sí tiene los glifos (§2.75). | Corregirlas en una pasada, revisando cada pantalla en capturas. | Bajo | Bajo |
| B8 | FDAF | `VerificarRecursos` lista los 160 cuadros de `videos/surrender/` como "sin referencia" porque se cargan con un nombre armado (`%04d`). Es un falso positivo del arnés, no del juego. | Enseñarle al arnés el patrón de la secuencia. | Bajo | Ninguno |
| B10 | FDAF | **Resuelto en §1.74 (v2.14).** En una corrida de la regresión (§1.73), `PruebaLogro_ESPANOL` se cortó tras el `ERROR_MEDIA_INVALID`. La causa era que el juego se reiniciaba a sí mismo sin las opciones de la JVM (`-Duser.home`) y salía del perfil aislado. | — | — | — |
| B9 | Ambos | `.glb` va por Git LFS en Escape (`.gitattributes`). Cada versión de `freddy.glb` ocupa cuota de LFS (114 MB la anterior, 36 MB la nueva). | Nada que hacer ahora; tenerlo en cuenta antes de iterar modelos grandes. | — | — |

**Publicado:**
- Escape: `dea1851` (incluye el objeto LFS de `freddy.glb`).
- FDAF: v2.13, el commit que contiene esta sección: estrella blanca (§1.70), exportación con alfa de NYDER (§1.69), `tools/tests` (§1.71) y `build-portable.bat` con `.gitattributes` (§1.72).
- En ningún caso se incluye el trabajo ajeno sin publicar.

**No verificado en esta sesión** (dicho explícitamente):
- ninguna escucha humana de los sonidos nuevos;
- ninguna partida jugada por una persona;
- el foco real de Windows con ALT+TAB (simulado con `EstadoVentana`);
- la carpeta portable completa con runtime y `.exe` (se probó la compilación y el JAR).

### 1.73 Recuperación tras la pérdida de conexión, regresión de v2.13 y aviso de pruebas incompletas, sesión 2026-10-26

**Estado encontrado al retomar** (comprobado con `git status`, `git fetch` y los archivos, no con los resúmenes):
- **Escape:** `dea1851`, igual a `origin/master`. Solo queda `dev/` (ajeno) sin seguimiento.
- **FDAF:** en `3c78302` (v2.12), igual al remoto. La v2.13 estaba preparada en el índice y sin commit: §1.69-§1.72, §2.73-§2.79, estrella blanca, `tools/branding`, `tools/tests`, `build-portable.bat`, `.gitattributes` y catálogo.
  - El índice coincidía con el árbol de trabajo para esos archivos.
  - El trabajo ajeno (FlatLaf, `Juego.java`, `NYDER.png`, `CargarSonido.java`, los íconos, `src/libs`, `if3001…`) seguía sin preparar e intacto.
  - `build-exe-launcher.bat` y `build-distribucion.bat` aparecen modificados solo por el cambio de fin de línea a CRLF (§1.72); en el índice no hay cambios de contenido.
- **La regresión de v2.13 se había cortado** a mitad (`PruebaIntros_INGLES`, ejecución 18). Quedaba un Java de esa prueba (perfil aislado de la regresión) que terminó solo al retomar.
- Los recursos de NYDER (§1.69) estaban completos en `FDAF assets/branding/` y se volvieron a comprobar: el WebM se decodifica como `yuva420p`, 1920×800, 2.73 s; están el MOV, las 82 PNG y `verification/`.

**Regresión de v2.13, repetida limpia** (índice exportado con `git checkout-index` a una carpeta nueva: sin FlatLaf, sin `src/libs`, `MENOR = 13`; `run_tests.sh` completo, ES y EN):
- 31 ejecuciones, `FIN_REGRESION`, código de salida 0.
- 180 líneas OK y 0 FALLO:
  - `VerificarRecursos`: 198 rutas;
  - `PruebaHover`: 57 + 57; `PruebaHoverVuelta`: 72 + 72;
  - `DESBORDES=0` en los dos idiomas;
  - `PruebaLogro` EN: 24 OK;
  - líneas de intro y destello de NYDER: 0 fallos;
  - Rendirse: toque, mantener y natural en OK.
- **Observación:** en esa corrida, `PruebaLogro_ESPANOL` terminó tras 9 OK, justo después de un `[ERROR VIDEO] ERROR_MEDIA_INVALID` (el fallo intermitente del vídeo de victoria de JavaFX, ya conocido), sin escribir su línea final. No hubo ningún FALLO, pero la prueba quedó incompleta.
  - Repetida sola dos veces con el mismo árbol: 24 OK, 0 FALLO, salida 0, sin error de vídeo.
  - Queda como hallazgo: cuando el vídeo falla, el proceso de prueba puede terminar antes de tiempo (B10 en §2.79). No se reprodujo en el juego.

**Cambio: `tools/tests/run_tests.sh` avisa de las pruebas incompletas.** Una prueba que se corta no escribe "FALLO", así que antes pasaba desapercibida. Ahora el script:
- muestra `AVISO` si el proceso termina con un código distinto de 0;
- muestra `AVISO` si una prueba que siempre escribe su resumen (`FALLOS:`/`RESULTADO:`: `PruebaLogro`, `PruebaLineasIntro`, `PruebaDestelloNyder`, `OverlayPuertas`, `PruebaHover`, `PruebaHoverVuelta`, `VerificarRecursos`) no lo escribió;
- imprime `AVISOS: N` al final.

**Verificado** con el `run()` real y dos "java" falsos:
- uno corta con código 1 y sin resumen: 2 avisos;
- otro termina bien con resumen: ningún aviso;
- una prueba sin resumen esperado (`RenderHud`): ningún aviso.

**Archivos:** `tools/tests/run_tests.sh` y este `CLAUDE.md`.

### 1.74 El relanzamiento del juego conserva las opciones de la JVM (escapaba del perfil aislado de las pruebas), sesión 2026-10-26

**Hallazgo:** tras publicar v2.13 quedaba un Java vivo, creado a las 08:19:07 desde el árbol de la regresión: `java -cp … com.fdaf.init.Main --reinicio-interno`, **sin `-Duser.home`**.
- Era el juego relanzado por `ReiniciadorJuego.lanzarNuevaInstancia()`, corriendo con el **perfil real** del usuario.
- La cadena:
  1. `PruebaLogro_ESPANOL` simula ganar una noche;
  2. el vídeo de victoria falló (`ERROR_MEDIA_INVALID`, el intermitente ya conocido);
  3. el flujo pasó enseguida a `reiniciarJuegoCompleto()`;
  4. el relanzamiento usó solo `java -cp <classpath> Main` y el proceso de prueba hizo `System.exit(0)`.
- Esto explica por completo B10 (§1.73, §2.79): la prueba "cortada" no venía de JavaFX, sino de que el juego se reinició a sí mismo.
- **Perfil real:** `config.properties` (205 bytes) y `handoff.json` siguen con su última escritura del 23/10 a las 20:54, antes de esta sesión. El proceso escapado se detuvo sin que hubiera escrito nada.
  - No se puede saber si las escrituras del 23/10 fueron del usuario jugando o de una fuga igual en la sesión anterior.

**Corrección (`ReiniciadorJuego.lanzarNuevaInstancia`):** la instancia nueva recibe las mismas opciones de la JVM con que se arrancó la actual (`RuntimeMXBean.getInputArguments()`).
- Se omiten las de depuración (`-agentlib`, `-javaagent`, `-Xrunjdwp`, `-Xdebug`), porque el puerto de Eclipse ya estaría ocupado por el proceso que termina.
- Para el jugador no cambia nada (la distribución arranca sin opciones especiales, o con las mismas en las dos instancias). Para las pruebas, el relanzado se queda en el perfil aislado.
- Es la única línea de código de FDAF tocada; el resto del flujo de reinicio queda igual.

**Prueba nueva (`tools/tests/src/PruebaReinicio.java`, solo Windows, en `run_tests.sh`):**
- Arranca con `-Duser.home=<aislado>` y una marca `-Dprueba.reinicio=<id>`, y llama a `lanzarNuevaInstancia()` por reflexión.
- Busca el hijo en la lista de procesos de Windows (solo procesos `java*`: el propio PowerShell contiene la marca, y en el primer intento la prueba se encontraba a sí misma) y comprueba que lleva `--reinicio-interno`, `-Duser.home` y la marca.
- Cierra solo ese hijo.
- **Resultado:** 5/5 OK con la corrección.
- **Contraprueba con el código de v2.13:** 3 FALLO; el hijo sale sin las opciones, y por eso ni siquiera aparece con la marca.
  - Ese hijo quedó corriendo con el perfil real. Se identificó por su classpath exclusivo (`scratchpad\reinicio\viejo\build`) y se detuvo; el perfil siguió sin cambios.
  - **Lección:** una contraprueba de este defecto abre el perfil real por definición. Hay que hacerla, como mucho, una vez y vigilando el proceso.

**`run_tests.sh`:** la comprobación de "resumen escrito" (§1.73) usa ahora el nombre de la prueba (así cubre las que llevan opciones `-D` delante), e incluye `PruebaReinicio`.

**Archivos:** `src/com/fdaf/util/ReiniciadorJuego.java`, `tools/tests/src/PruebaReinicio.java` (nuevo) y `tools/tests/run_tests.sh`.

### 1.75 `FondoMenuBordes` funciona desde el repositorio (extrae los cuadros del GIF del juego), sesión 2026-10-26

**Hallazgo** (gracias al aviso de §1.73): en la regresión de v2.14, `FondoMenuBordes` terminó con código 1 (`IIOException: Can't read input file!`).
- El arnés copiado a `tools/tests` en §1.71 leía 70 PNG (`f00.png`…`f69.png`) que se habían extraído a mano del GIF del menú en una sesión anterior y que solo existían en el scratchpad.
- Desde el repositorio no podía funcionar nunca. En la corrida `rapida` de §1.71 su salida no se revisó, así que esa sección afirmaba de más; se marcó la corrección allí.

**Cambio:**
- El arnés extrae los cuadros del propio recurso del juego (`/backgrounds/menu_background.gif`) con el lector GIF de ImageIO.
- Compone cada cuadro en su posición sobre un lienzo acumulado, respetando la disposición `restoreToBackgroundColor`, como lo muestra un visor.
- Escribe un veredicto `OK/FALLO` y `FALLOS: N` (antes solo imprimía cifras) y entra en la lista de pruebas con resumen obligatorio de `run_tests.sh`.

**Verificado:** 70 cuadros; píxeles casi blancos en el borde: **antes 76 964 (15 combinaciones de cuadro y resolución), después 0**.
- Son exactamente las cifras medidas en §1.68 con los cuadros extraídos aparte: la extracción equivale a la de entonces.
- `OK`, salida 0, 17 s.

**Archivos:** `tools/tests/src/FondoMenuBordes.java` y `tools/tests/run_tests.sh`.

### 1.76 La presentación de NYDER GAMES exportada sobre fondos lisos (negro, blanco y azul marino), sesión 2026-10-26

**Pedido:** tres videos de la animación completa del juego (letras, estrella de la D, brillo y destello, sonido, duración, transiciones y composición), con fondo negro, blanco y azul marino oscuro, en `Desktop\Nyder`. La versión transparente (§1.69) se conserva.

**Por qué no se reutilizó la secuencia PNG transparente:**
- La animación y el brillo de §1.69 son los del juego, pero la composición no: el logo está a todo el ancho en un lienzo de 1920 × 800.
- En el juego, el logo ocupa el **55 % del ancho** de una pantalla negra de 1920 × 1080 (`ControllerMenu`: `new PnlWarning(imagenNyder, 0.55)`).
- Como se pidió conservar la composición, se volvió a renderizar con la misma clase del juego.

**Cambios (`tools/branding/`):**
- `ExportarNyderAlfa.java`: argumentos opcionales nuevos. Sin ellos, el resultado es la exportación transparente de §1.69, sin cambios.
  - `fraccion` (5.º): ancho máximo del logo en el lienzo.
  - `completa` (6.º): incluye los cuadros extremos con alfa 0.
  - `letras=RRGGBB` (7.º): recolorea solo los píxeles grises del logo, conservando su alfa.
    - En `nyder_games_splash.png` la separación es limpia: 293 752 píxeles con saturación < 0.1 (letras blancas) y 11 623 con saturación > 0.35 (estrella azul, de media `#5253FB`), sin ninguno intermedio.
    - Se recolorea la imagen antes de pasársela a `PnlWarning`, así que la máscara de la estrella, el halo y el chispazo no cambian.
- `exportar_nyder_fondos.sh` (nuevo):
  - Renderiza la secuencia a 1920 × 1080, 30 fps y fracción 0.55, con los extremos incluidos (84 cuadros, 2.75 s).
  - La compone con FFmpeg sobre un fondo liso, con los 250 ms de fondo previos que tiene el juego y 250 ms al final.
  - Codifica en MP4 H.264 High `yuv420p` (CRF 14, `+faststart`) con AAC de 192 kb/s y el destello sonoro con el mismo retardo que en el juego.
  - Copia también el WAV del destello.

**Resultado (en `Desktop\Nyder\`, junto a los archivos que ya había, sin tocarlos):**
- `nyder_games_black.mp4`, `nyder_games_navy.mp4` (`#0B1A33`), `nyder_games_white.mp4` y `nyder_games_white_dark_letters.mp4`, de 116-137 KB cada uno, 3.27 s, 1920 × 1080, 30 fps, con audio.
- `nyder_star_sparkle.wav` y `README_nyder_games.md`.
- **Cuarta variante, no pedida explícitamente:** las letras del logo original son blancas, y en `nyder_games_white.mp4` (la animación exacta) solo se ven la estrella y su brillo. Por eso se entrega también la versión blanca con las letras en `#16161C`. La exacta se conserva tal como se pidió.
- Registrado en el README de `FDAF assets/branding/nyder_games_animation/` y en el catálogo. Los videos no se duplican en `FDAF assets`.

**Verificación:**
- **Fidelidad con el juego:** se dibujó el mismo `PnlWarning` opaco sobre negro (como en el juego) y, aparte, transparente y compuesto sobre negro (como en la exportación), con alfa 0.4/0.5/1 y brillo 0/0.5/1. Diferencia media de 0.0006/255 como mucho y máxima de 3/255, solo en el halo del destello (redondeo). La versión negra es la presentación del juego.
- **Cuadros decodificados de los cuatro MP4** (hoja de contacto en t = 0, 0.70, 1.59, 2.30 s y el último cuadro): el primero y el último son del color liso del fondo (negro 0-0; blanco 253-255; azul marino 10-51 por canal); el fundido, el pico de brillo y la salida aparecen en los cuatro.
- **Brillo:** en el video negro, la luz de la zona de la estrella llega al máximo a 1.633 s. Lo previsto era 0.25 s + 0.8 s + 0.12 s + 0.42 s = 1.59 s, más una meseta de 60 ms.
- **Sonido:** se hace audible a 1.332 s en el AAC. El WAV original tarda 0.122 s en superar el mismo umbral, así que el archivo empieza a 1.210 s, exactamente el retardo programado (0.25 s + 0.96 s).
- La presentación de FDAF no se tocó.
- No se reprodujeron los videos con oídos ni ojos humanos: la verificación es por análisis de los archivos decodificados.

**Archivos:** `tools/branding/ExportarNyderAlfa.java` y `tools/branding/exportar_nyder_fondos.sh` (nuevo); `docs/catalogo_recursos.md`; fuera del repositorio, `Desktop\Nyder\` y el README de `FDAF assets/branding/nyder_games_animation/`.

### 2.80 M9 resuelto: las texturas de 4096² del mapa reducidas a 2048², sesión 2026-10-26

**Estado al empezar:** pendiente. `assets/models/pizzeria.glb` era idéntico al original (`FDAF assets/source_files/3d_models/LibGDX_Ready/Pizzeria.glb`).

**Lo que había (corrige el dato de §2.79):**
- No era una textura de 4096², sino dos, las dos del mismo material, `main_material`, que usan las paredes (`Object_4`) y el parche del techo (`TechoPrincipal_Patch`):
  - `main_material_baseColor`: 4096², escala de grises (`L`), PNG de 220 KB;
  - `main_material_metallicRoughness`: 4096², paleta (`P`), PNG de 2.35 MB.
- La auditoría de §2.79 solo vio la segunda, porque el filtro con el que se listaron ocultaba las imágenes de pocos KB.
- El resto de las 27 imágenes del mapa ya era de 2048 o menos y no se tocó.

**Qué resolución necesita:**
- El atlas tiene bloques planos y franjas de cuadros (las baldosas de ajedrez de las paredes).
- Medido en la imagen: cada cuadro mide 13-17 px a 4096. A 2048 quedan en 7-8 px, nítidos salvo un píxel de transición; a 1024 bajarían a unos 4 px y se emborronarían.
- Pérdida al reducir a 2048 y volver a ampliar: media de 0.71/255 (color) y 0.51/255 (metal/rugosidad).
- **Se eligió 2048**, como en `freddy.glb` (§2.77).

**Hecho:**
- `python tools/assets/reducir_texturas_glb.py <original> … 2048`: la misma herramienta de §2.77, sin cambios.
- Comprobado que el JSON es idéntico y que solo cambiaron los bytes de esas dos imágenes; todos los demás bufferViews (geometría, el resto de texturas) son idénticos byte a byte.
- Imágenes: 220 → 115 KB y 2351 → 963 KB. El archivo pasa de 74.7 a 73.1 MB.
- La variante está copiada como `LibGDX_Ready/Pizzeria_texturas2048.glb`. El original con 4096² se conserva en `LibGDX_Ready/Pizzeria.glb` y en el historial de git (LFS). Catálogo actualizado.

**Impacto:**
- **VRAM** (cálculo, RGBA8 sin mipmaps): dos texturas de 64 MB pasan a 16 MB, unos 96 MB menos. Es la ganancia real.
- **Carga** (`PruebaCarga`, alternando versiones en la misma sesión): 2048 → 8.40 / 7.39 s; 4096 → 7.92 / 7.78 s. **Sin diferencia medible**, dentro del ruido entre corridas. Estas PNG ya eran pequeñas, a diferencia de las de Freddy.
- **Visual** (`DiagRender`, con la misma cámara antes y después): oficina hacia las dos puertas, pasillo, comedor y escenario. Diferencia media de 0.013-0.245/255 y máxima de 4/255; en la comparación lado a lado no se distinguen (`m9_comparacion.png`).

**Archivos:** `assets/models/pizzeria.glb` (Escape); `docs/catalogo_recursos.md` (FDAF); `LibGDX_Ready/Pizzeria_texturas2048.glb` (`FDAF assets`).

### 1.77 Sonido hover solo en los botones de interfaz (fuera de las puertas, luces, objetos, huecos sin obtener y Rendirse), sesión 2026-10-26

**Pedido:** el hover estaba en controles que son interacción de la partida, no un menú. Debía quitarse de:
- los botones de puerta y de luz;
- el objeto coleccionable que aparece en la puerta;
- los huecos de la tableta de objetos no obtenidos;
- la barra de Rendirse.

Debía conservarse en los botones de interfaz (menú, navegación, opciones, idioma, confirmar y cancelar, cómo jugar, acerca de, abrir la tableta…), sin tocar el recurso, el volumen ni los sonidos propios de cada acción.

**Cambios (todos los hover de la partida se registraban con `InteraccionBoton.aplicarSonidoHover`):**
- `ControllerInterfaz.conectarPuertas` y `conectarLuces`: fuera el hover de los cuatro botones. Sus `ActionListener` y el sonido de la puerta o la luz al usarlos no cambian. Se borró `puertasYLucesResponden()`, que solo usaban esos hover.
- `conectarLuces`: fuera el hover del overlay del objeto de la puerta (§1.38). El clic para recogerlo y su sonido siguen.
- `conectarClicColeccionable`: el hover de cada hueco suena solo si el objeto **ya se obtuvo** en esta partida (`coleccionableObtenido`: `Inventario.buscarPorTipo(tipo).isEncontrado()`), además de las condiciones de antes (partida en curso, hueco que existe esta noche).
  - Una silueta sin obtener o una X ya no suenan al pasar por encima.
  - El clic suena como antes en todos los casos: el sonido de detalle en los existentes y el de error en las X.
- `ControllerCamara`: fuera el hover de la barra de Rendirse (§1.38). Su "mantener pulsado" no cambia.
- **Se conservan** sin cambios: el menú principal, los diálogos de confirmación, Opciones, Idioma, Volumen, Noche personalizada, Cómo jugar y Acerca de (`PnlInfo`), el selector de idioma, el agradecimiento, el botón de colgar la llamada, los botones de Game Over y el sonido de abrir la tableta (`mostrarTransicionTablet`).

**Verificación** (`PruebaHover` y `PruebaHoverVuelta`, componentes reales con `MOUSE_ENTERED` simulado, comprobando si arranca el clip compartido del hover; árbol compilado con las versiones publicadas de los archivos ajenos, perfil aislado):
- `PruebaHover`: **59/59 en ES y EN**. Nuevas expectativas:
  - puertas, luces, objeto revelado, hueco sin obtener, hueco con X y barra de Rendirse: "no suena";
  - el mismo hueco tras marcarlo obtenido: "suena";
  - hueco obtenido con la partida terminada: "no suena".
  - Todos los botones de menú, diálogo y colgar siguen sonando.
- `PruebaHoverVuelta`: **72/72 en ES y EN**. Abrir y cerrar las pantallas repetidamente no acumula listeners.
- Análisis de código: los sonidos de acción de las puertas y las luces no pasan por `aplicarSonidoHover` y no se tocaron.

**Archivos:** `ControllerInterfaz.java`, `ControllerCamara.java` y `tools/tests/src/PruebaHover.java`.

### 1.78 Auditoría de dificultad de FDAF (simulación con las clases reales), sesión 2026-10-26

**Pedido:** evaluar con datos si la dificultad está equilibrada: progresión entre noches, baterías, energía, coleccionables, aleatoriedad, recuperación, Noche 5 y jugador nuevo frente a veterano. Corregir solo errores demostrados; los ajustes subjetivos, recomendarlos con datos.

**Reglas reales (análisis del código publicado):**
- **No hay reloj, ni gasto de energía, ni IA.**
  - La noche se gana al recoger todos los coleccionables (`Juego.gano`) y se pierde al quedarse sin vidas.
  - "Energía" y "batería" en la interfaz son las vidas (íconos `bateria1-4`, tope `VIDAS_MAXIMAS = 4`).
  - Los animatrónicos no se mueven: el contenido de cada puerta se fija al generar el árbol.
  - Por eso no aplican los "intervalos de movimiento" ni la "duración de la batería" del pedido.
- **Árbol** (`GeneradorArbol`, `Arbol.construirCompleto`):
  - Mientras queden coleccionables pendientes, cada puerta nueva es, con 1/3 de probabilidad cada una: un coleccionable, "sin coleccionable" (50 % animatrónico, 50 % nada) o nada.
  - Después se agregan de 3 a 7 puertas sin coleccionable (50 % animatrónico) y una más si hace falta para que el total sea impar.
  - El árbol se rellena por niveles en ese orden, así que **los coleccionables quedan en los niveles altos y los profundos son 50 % animatrónico**.
- **Recorrido:**
  - Abrir una puerta elige el hijo izquierdo o el derecho (`elegirIzquierda/Derecha`, en el momento de abrir), y su contenido solo se ve con la puerta abierta y la luz encendida.
  - **No hay ninguna información antes de elegir**, así que un jugador veterano no tiene ventaja sobre uno nuevo: la dificultad es pura probabilidad.
  - Al llegar a una hoja, se genera un árbol nuevo con los coleccionables que falten.
- **Baterías:** una puerta "nada" se convierte en batería al abrirla si quedan 1 o 2 vidas, con la probabilidad de `PROBABILIDAD_BATERIA[noche][vidas-1]` (publicada: N1 0.08/0.05, N2 0.30/0.22, N3 0.46/0.28, N4 0.50/0.32, N5 0.62/0.42, para 1 y 2 vidas).
  - La tabla de §1.5 está desactualizada desde la N2.
- **Noches** (`Noche`): vidas / coleccionables = N1 4/5, N2 3/6, N3 3/7, N4 2/8, N5 2/10.

**Simulador (`tools/tests/src/SimulacionDificultad.java`, nuevo):**
- Usa `Juego`, `GeneradorArbol`, `Arbol` y la tabla reales; solo fija las semillas por reflexión.
- Política del jugador: puerta al azar, revelar siempre y recoger lo que salga. Es la única política con sentido, porque no hay información previa.
- 20 000 partidas por noche, ~2 s en total.
- Semillas 1 y 2: las cifras coinciden con ±1 punto.

| Noche | Vidas / objetos | **Gana** | Puertas reveladas (media / p90) | Animatrónicos | Baterías | Vidas al ganar | Pierde con ≥ 80 % de objetos | Pierde con ≤ 20 % | Árboles |
|---|---|---|---|---|---|---|---|---|---|
| 1 | 4 / 5 | **40.3 %** | 14.5 / 21 | 3.36 | 0.18 | 2.03 | 24.4 % | 8.3 % | 5.5 |
| 2 | 3 / 6 | **31.0-32.1 %** | 15.2 / 24 | 3.40 | 1.00 | 1.93 | 19.1 % | 12.1 % | 5.6 |
| 3 | 3 / 7 | **33.3 %** | 17.8 / 28 | 3.89 | 1.56 | 2.00 | 16.5 % | 9.4 % | 6.2 |
| 4 | 2 / 8 | **25.6 %** | 17.1 / 31 | 3.64 | 2.17 | 2.05 | 12.4 % | 17.4 % | 5.8 |
| 5 | 2 / 10 | **27.5 %** | 21.4 / 38 | 4.44 | 3.03 | 2.13 | 18.2 % | 22.2 % | 6.8 |

**Conclusiones:**
1. **La progresión no es monótona.**
   - La N3 es más fácil que la N2, porque su tabla de baterías es mucho más generosa (0.46 frente a 0.30) con las mismas 3 vidas.
   - La N5 es más fácil que la N4, porque las baterías al 62/42 % compensan los 2 coleccionables de más.
   - **La noche más difícil es la 4, no la 5.**
2. **La Noche 5 no es desproporcionada:** 27.5 %, apenas por encima de la N4.
   - Es la más larga (21 puertas de media, 38 en el p90) y la que más pierde "pronto" (22 % con ≤ 2 objetos, porque con 2 vidas dos animatrónicos seguidos terminan la noche), pero las baterías la sostienen: 3 de media.
3. **La Noche 1 es dura para ser la primera:** el 60 % de las partidas se pierden, con sus baterías casi nulas (0.18 de media). La tabla dice "Noche 1 — SIN CAMBIOS", así que se respetó.
4. **Las derrotas "casi ganadas"** (≥ 80 % de los objetos) son entre el 12 % y el 24 % de todas las partidas: frustrantes, porque no dependen del jugador.
5. **No hay partidas imposibles ni bucles:** ninguna partida llegó al tope de 5000 puertas. El p90 de puertas va de 21 a 38.
6. **Jugador nuevo y veterano:** sin información previa, la experiencia y el resultado son los mismos. La "habilidad" no existe en el sistema de puertas.
7. **Cambio ajeno pendiente (`Juego.java` local, baterías solo desde la N3; B4):** simulado, **dejaría la N2 en el 16.4 %** (la mitad), de lejos la más difícil, y la N1 en el 37.1 %. Si se publica, la curva empeora mucho.

**Errores demostrados:** ninguno en la lógica. La única inexactitud es documental: la tabla de §1.5.

**Recomendaciones (no aplicadas: son ajustes de equilibrio subjetivos):**
- **Tabla de baterías** que deja la curva suave y monótona sin tocar la N5: N1 0.40/0.30, N2 0.46/0.34, N3 0.44/0.28, N4 0.56/0.38, N5 0.62/0.42.
  - Simulada (semillas 1 y 2): **N1 53 %, N2 39 %, N3 33 %, N4 29-30 %, N5 27 %**.
  - Variante con la N2 más alta (0.50/0.38) y la N1 a 0.36/0.26: 51 / 41-42 / 33 / 29-30 / 28 %.
  - Es un cambio de una sola tabla (`Juego.PROBABILIDAD_BATERIA`). Antes de aplicarlo habría que confirmar con el usuario lo de "Noche 1 — SIN CAMBIOS".
- **Dar algo de información antes de elegir** (por ejemplo, un indicio sonoro o visual tenue al encender la luz con la puerta cerrada): convertiría la suerte en habilidad y diferenciaría al veterano. Es un cambio de diseño, no de equilibrio.
- **No publicar el gate de la N3** del `Juego.java` local sin compensar la N2.

**Archivos:** `tools/tests/src/SimulacionDificultad.java` (nuevo; herramienta de análisis, no forma parte de la regresión). El juego no cambia.

### 2.81 Manos de Freddy al caminar: las palmas miran al cuerpo, no al frente, sesión 2026-10-26

**Pedido:** Freddy caminaba con las manos orientadas hacia delante, en una postura poco natural. Las manos debían quedar hacia los lados del cuerpo y acompañar la zancada, sin torsiones en codos u hombros, y sin tocar innecesariamente otras animaciones.

**Diagnóstico (renders reales del clip, `tools/tests/src/DiagPose.java`, nuevo):**
- `DiagPose` dibuja una hoja de contacto de un clip desde frente, tres cuartos y lado, con correcciones de rotación local opcionales por hueso. Las aplica como `PoseProcedural`, escribiendo `localTransform` (memoria `feedback-gdx-gltf-animated-nodes-localtransform`) y ocultando nodos si se pide.
- En `Freddy_Walk` las palmas miran a la cámara frontal durante todo el ciclo, con las muñecas abiertas hacia fuera, como en un gesto de "ta-da".
- **`Freddy_Idle` tiene exactamente el mismo defecto.** Es un error de giro del antebrazo en el retargeting desde Mixamo (§2.6), no de la orientación del modelo.
- Esqueleto: brazo `j_*_arm_02`, antebrazo `j_*_arm_01` (hijo a +2.42 en X local), mano `j_*_hand_*`. El eje de los huesos del brazo es **+X local** en los dos lados.

**Corrección elegida:**
- Pronación del antebrazo: **+90° alrededor de X local en `j_r_arm_01_021` y `j_l_arm_01_036`**. La mano la sigue.
- Se probaron los dos sentidos en cada lado: -90° deja una mano con la palma al frente. Con +90° en los dos, desde el frente las manos se ven de canto (palmas hacia los muslos) y desde el lado se ven la palma y el dorso.
- Codos y hombros sin cambios.
- **Se aplica también a `Freddy_Idle`:** si solo se corrigiera `walk`, las manos darían un giro de 90° en cada paso de caminar a reposo (aturdimiento, puerta de emergencia, portón). Verificado igual.
- No se tocaron los otros dos clips del archivo (las capas de Mixamo sin uso) ni ningún otro hueso.

**Herramienta nueva (`tools/assets/corregir_pose_glb.py`):**
- Multiplica en el sitio cada keyframe de rotación de un hueso en un clip por el giro corrector local (q · c).
- Se niega a editar un acceso compartido con otro canal o en `CUBICSPLINE`.
- Uso: `python corregir_pose_glb.py entrada salida "Freddy_Walk:j_r_arm_01_021:1,0,0,90" …`.
- Aplicada sobre el `freddy.glb` de §2.77 (texturas de 2048): 25 keyframes por antebrazo en `walk` y 240 en `idle`.
- **Comprobado:** respecto de la versión anterior solo cambiaron esos 4 bufferViews; mallas, piel, texturas y el resto de canales son idénticos byte a byte.

**Verificación (capturas):**
- Hojas de contacto del GLB corregido, `walk` (6 fases × 3 vistas) e `idle` (6 fases): las manos acompañan la zancada con la palma hacia el cuerpo. Coinciden con la vista previa en tiempo de ejecución.
- Las de antes y después están en `freddy_manos_antes.png` y `freddy_manos_despues.png`.
- El jumpscare, la derrota y la persecución usan estos mismos clips, así que heredan la corrección. La cabeza, el torso y la mandíbula (`PoseProcedural`) no dependen de los antebrazos.
- **No probado en partida con una persona:** la evaluación es por capturas del clip y del juego.

**Archivos y recursos:**
- `assets/models/freddy.glb` y `tools/assets/corregir_pose_glb.py` y `tools/tests/src/DiagPose.java` (nuevos).
- En `FDAF assets/source_files/3d_models/LibGDX_Ready/`: `Freddy_juego.glb` (idéntico al del juego). Se conservan el original con 4096² (`Freddy.glb`) y la versión intermedia (`Freddy_texturas2048.glb`).

### 2.82 Chica sostiene el cupcake con la mano izquierda levantada, sesión 2026-10-26

**Pedido:** Chica caminaba con los brazos a los lados y el cupcake en la mano izquierda, como pegado. La mano izquierda debía quedar algo levantada, en una pose de sostenerlo a propósito, sin que el cupcake flote ni atraviese el cuerpo, y sin tocar la mano derecha.

**Cómo está asociado (análisis del GLB):**
- No hay ningún nodo llamado "cupcake". Sus mallas (material `Chica_Cupcake_Eyes`, más el plato, la vela y otras piezas) son hijas de la cadena `Bone.016` (brazo) → `.017` (antebrazo) → `.018` (mano) → `.039` → `.059` → `.061` → `.063/.066`, que sale de la columna.
- Es decir: el cupcake ya está emparentado con la mano izquierda y la sigue; el problema era solo la pose del brazo.
- Huesos al estilo Blender: el eje del hueso es **+Y local** (antebrazo +12.66 Y, mano +10.17 Y).

**Antes (renders):** con el brazo estirado, el cupcake cuelga ladeado a la altura del muslo, con el plato vertical, y en algunos cuadros atraviesa la pierna.

**Corrección (probada con `DiagPose` antes de aplicarla):**
- Antebrazo `Bone.017`: **-65° alrededor de Z local** (flexión del codo hacia delante).
  - Las otras tres opciones probadas, X ±80 y Z +80, llevaban el antebrazo hacia atrás o hacia el costado.
  - Con -80 cruzaba demasiado hacia el centro del cuerpo.
- Mano `Bone.018`: **-90° alrededor de Y local** (giro sobre el eje del antebrazo). El plato queda horizontal y el cupcake derecho encima.
  - +90 y X ±70 lo dejaban de lado, boca abajo o tapado por la mano.
- Mismo criterio que con Freddy (§2.81): se aplica a `Chica_Walk` y también a `Chica_Idle`, para que el brazo no salte al pasar de caminar a reposo. Verificado en los dos.
- Brazo derecho y resto del esqueleto sin cambios.
- Aplicado con `tools/assets/corregir_pose_glb.py` (§2.81): 32 keyframes por hueso en `walk` y 299 en `idle`.
- El GLB corregido coincide con la vista previa en tiempo de ejecución: diferencia media de 0.008/255 en `walk` y 0 en `idle`.

**Verificación:**
- Hojas de contacto (6 fases de `walk` × 3 vistas, 4 de `idle`): el cupcake va derecho sobre el plato horizontal, delante del cuerpo a la altura de la cintura y el pecho, con un leve balanceo del brazo al andar. No atraviesa el torso ni las piernas en ninguna vista.
- **En el juego real** (`PruebaMundo personajes`, Chica caminando hacia el jugador con la linterna encendida): se ve sosteniéndolo levantado.
- Capturas: `chica_cupcake_antes.png`, `chica_cupcake_despues_walk.png`, `chica_cupcake_despues_idle.png`, `personajes_hoja.png`.

**Archivos:** `assets/models/chica.glb`; en `FDAF assets/.../LibGDX_Ready/`: `Chica_juego.glb` (idéntico). `Chica.glb` original conservado.

### 2.83 Bonnie sin la guitarra en las manos, sesión 2026-10-26

**Pedido:** la guitarra que Bonnie lleva al caminar no se veía bien; retirarla del personaje sin tocar el esqueleto ni la animación, y sin quitar la guitarra como coleccionable de FDAF (imágenes, inventario, recogida).

**Dónde está (análisis del GLB):** 15 nodos sin piel (`Cube`, `Cube.001`…`Cube.009`, `Cylinder`…`Cylinder.003`, `Sphere`), con las mallas de la "V" roja, emparentados con la mano derecha (`bip_hand_R_bonnie.qc_skeleton`). Es un accesorio emparentado al exportar, como los que documenta la memoria del proyecto. No son huesos ni tienen piel.

**Cambio (sin modificar el modelo):**
- `EntityDefinition.nodosOcultos` (nuevo, lista de nombres de nodos).
- `EntityFactory.createEntity` quita las partes dibujables de esos nodos y de sus hijos solo en esa instancia. Los nodos siguen en el esqueleto, la animación no cambia, y si un nombre no existe se avisa en el registro.
- `bonnie.json` lista los 15 nodos de la guitarra.
- Bonnie no tiene collider propio fuera de su `CollisionComponent` estándar, que no depende de mallas. La retirada no afecta a colisiones, IA ni animaciones.
- **El coleccionable "guitarra de Bonnie" de FDAF no se tocó** (es otro proyecto, otras imágenes y otra mecánica).

**Verificación:**
- `DiagPose` con esos nodos ocultos (`walk` 4 fases × 3 vistas e `idle`): no queda ninguna pieza de la guitarra. Las manos quedan relajadas a los lados, sin una postura de agarre extraña, así que no hizo falta corregir el brazo.
- **En el juego real** (`PruebaMundo personajes`, modo nuevo):
  - Bonnie camina hacia el jugador sin guitarra (captura);
  - comprobación automática de que esos nodos no tienen partes dibujables (0);
  - los cuatro animatrónicos están en `walk` durante las capturas: 5/5 OK;
  - ningún aviso de nodo inexistente en el registro.

**Archivos:** `content/EntityDefinition.java`, `ecs/EntityFactory.java`, `assets/content/entities/bonnie.json` y `tools/tests/src/PruebaMundo.java` (modo `personajes`, que también captura a Freddy y a Chica para §2.81 y §2.82).

### 2.84 Tablero del auto rehecho: dentro de la cabina, sin asomar por los lados ni por el parabrisas, sesión 2026-10-26

**Pedido:** el tablero de la cinemática de victoria se veía muy mal. Sobresalía por los lados del coche y hacia delante, como un bloque rectangular ajeno al vehículo. Se pidió inspeccionar la geometría real, no solo la cámara, y preferir un interior sencillo y convincente.

**Inspección (geometría real, no supuesta):**
- `DiagPose` con el modelo estático, desde frente, tres cuartos y lado: el tablero es **una losa gris que asoma sobre el capó, atraviesa la base del parabrisas y sale por los dos lados de la cabina**.
- Medido en Blender (`medir_coche.py`, solo lectura):
  - parabrisas con su borde inferior en z = 0.879 (y = -0.951) e inclinación dy/dz = 1.67;
  - ventanillas delanteras en |x| = 0.836, línea de cintura en z = 0.891;
  - el tablero de §2.68/§2.69 llegaba a |x| = **0.939** (`ancho - 0.25`, el ancho del auto entero, no el de la cabina) y su cara superior pasaba **por encima del vidrio**: a y = -0.6 el vidrio está a z = 1.089.
- Desde el asiento (c02-c10), eso se leía como una losa alta y plana con un reborde delante del volante.
- No era un problema de cámara, así que no se tocaron la cámara, el asiento ni el ojo del conductor.

**Corrección (`tools/cinematics/escape_victory/prep_car.py`):** el tablero (v3) sale de la propia carrocería en lugar de alturas fijas.
- Del borde inferior y la inclinación del parabrisas (caras de vidrio con normal hacia delante y arriba) y del ancho del vidrio y de las ventanillas delanteras.
- **Ancho:** el menor de los dos, menos 3.5 cm por lado (semiancho de 0.776 frente a 0.939). Con 7 cm quedaba a la vista una cuña roja del panel de la puerta.
- **Perfil:**
  - arranca 3 cm detrás del borde del vidrio;
  - la cara superior sube con una curva suave hasta 11 cm sobre ese borde, recortada automáticamente para quedar al menos 3.5 cm detrás del vidrio;
  - tiene un reborde redondeado, una cara casi vertical para los indicadores y la parte baja hacia las rodillas.
- **Visera** sobre los indicadores, del lado del conductor y también detrás del vidrio. Los **indicadores** (`car_gauges`) se recolocan sobre la cara nueva.
- **Sin cambios:** volante, asientos, piso, puerta, ruedas, carrocería, materiales, luces y el resto de piezas.
- **Comprobado** sobre el GLB: solo cambiaron las mallas de `car_interior` y `car_gauges`. El ojo del conductor sigue en (0.436, 0.087, 1.153).
- Regenerado con Blender 5.1 desde el original de Quaternius (`downloads/quaternius_car_original.glb`).

**Verificación:**
- **Exterior** (render de frente, tres cuartos y lado, antes y después, `coche_exterior_antes_despues.png`): el tablero ya no asoma por ningún lado; solo se ve a través del parabrisas, como un tablero de verdad.
- **Cinemática real** (`PruebaVictoria completa`, ES y EN, todo en OK):
  - en los planos interiores (c02-c10, c14) el tablero es bajo e inclinado, los indicadores encendidos se ven a través del volante, y se ven el capó y la escena;
  - el plano exterior de la entrada (c01) no cambia: el bloque acolchado de abajo a la derecha es el respaldo del asiento;
  - el arranque, la persecución y el plano final siguen igual;
  - capturas en `tablero_antes_despues.png` y `tablero_v3_final.png`.
- **Pendiente menor:** a la izquierda del volante todavía asoma un borde fino del panel rojo de la puerta en algunos planos.

**Archivos:**
- `tools/cinematics/escape_victory/prep_car.py` y `assets/models/car_sedan.glb`.
- `tools/tests/src/DiagPose.java`: admite modelos sin animación (`-`).
- En `FDAF assets`: `source_files/cinematics/escape_victory/car_sedan.glb` actualizado; la versión anterior en `_archive/old_versions/escape_car_sedan_tablero_v2.glb`.

### 2.85 Pasos v2: metal pesado golpeando el suelo, sesión 2026-10-26

**Pedido:** los pasos de §2.73 no gustaron. Se pedía un **metal pesado golpeando el suelo**, con masa e impacto y no una lata, tanto en la persecución de Freddy como en los cuatro del interior, con identidad propia cada uno. Sin subir simplemente el volumen y sin sonidos chillones.

**Qué tenía la v1 (medido):**
- Casi solo un golpe grave más resonancias del suelo.
- Con ponderación A (cómo pesa cada banda para el oído), los agudos de 2-6 kHz eran el 2-10 %, y **su cola tardaba de 150 a 315 ms en caer 30 dB**: reverberación y servo con brillo largo, que encaja con lo de "chillón".
- No había un cuerpo metálico reconocible.
- El sonido metálico original, de antes de §2.73, tenía un 13.9 % de agudos ponderados: era la "lata".

**Diseño v2 (`tools/audio/synth_animatronic_steps.py`, reescrito; mismos 20 archivos y nombres en `assets/audio/sfx/steps/`, así que el juego no cambia):**
1. **Golpe sub** de 42-72 Hz según el personaje, con caída de tono y saturación suave: la masa.
2. **Cuerpo metálico:** modos inarmónicos de una placa gruesa de acero (proporciones 1, 2.5, 4, 5, 6.5, 8.5…) sobre una base grave de 135-240 Hz. Los modos altos se apagan antes que los bajos y no hay ninguno por encima de 2.6 kHz. Suena a acero macizo, no a lámina.
3. **Contacto:** un chasquido de banda media (0.6-3 kHz) de menos de 15 ms, la definición del impacto.
4. **Mecánica secundaria:** traqueteo de articulaciones (de 1 a 3 microgolpes entre 15 y 110 ms después) y servo, filtrados.
5. **Cola corta:** sala de la pizzería o rebote abierto en el estacionamiento, filtrada por debajo de 1.4-1.5 kHz para que no quede brillo.
- Una compresión suave da más cuerpo sin subir el pico (normalizado a -1 dBFS).
- La señal seca se desvanece en su último cuarto. Sin eso, el modo grave del acero se cortaba de golpe antes de la cola y producía un chasquido (detectado en el espectrograma).
- **Identidad de cada uno:**
  - Freddy en el estacionamiento: el más pesado, contacto áspero y rebote abierto.
  - Freddy en el interior: macizo y lento, con sala.
  - Bonnie: acero algo más agudo, traqueteo y servo marcados (mecánico).
  - Chica: cuerpo más largo y un modo grave de armazón hueco (110-118 Hz).
  - Foxy: el más agudo, seco y corto, con raspado (urgente).
- Cuatro variaciones por personaje, con semilla fija.

**Medido (archivos, ponderación A):**

| | 120-400 Hz | 400 Hz-2 kHz (cuerpo metálico) | 2-6 kHz (brillo) | Caída de 30 dB en 2-6 kHz |
|---|---|---|---|---|
| Persecución v1 → **v2** | 41.6 → 39.2 % | 38.2 → **53.4 %** | 6.9 → **1.5 %** | 83 → 64 ms |
| Freddy interior v1 → v2 | 36.7 → 49.4 % | 49.1 → 45.0 % | 9.9 → **1.4 %** | 314 → **60** ms |
| Bonnie v1 → v2 | 62.9 → 26.1 % | 24.7 → **61.3 %** | 5.3 → 4.4 % | 219 → 78 ms |
| Chica v1 → v2 | 63.3 → 27.6 % | 29.5 → **63.7 %** | 3.3 → **0.3 %** | 233 → 120 ms |
| Foxy v1 → v2 | 68.0 → 22.2 % | 21.6 → 54.0 % | 1.8 → 9.2 % (raspado) | 147 → 87 ms |

- En el espectrograma (`pasos_v2_espectrogramas.png`): bandas horizontales nítidas por debajo de ~1.2 kHz (los modos del acero) y un transitorio corto. La v1 tenía colas de ruido difuso hasta más de 4 kHz.
- **Chasquidos:** el mayor salto local de energía aguda después del ataque es ×2.8 (los microgolpes del traqueteo, previstos). Todos los archivos terminan en 0.
- Tamaño: 1.7 MB los 20 archivos.

**Sincronización y comportamiento:** sin cambios de código respecto de §2.73 (apoyo real del pie, sin sonido en reposo, aturdimiento, pausa ni jumpscares, atenuación, paneo, ganancia suavizada en el corte de cámara, sin duplicados). Verificado de nuevo en la regresión completa: `mundo_pasos` y la persecución de `victoria_completa`.

**Recursos:** la v1 se archivó en `FDAF assets/_archive/old_versions/escape_steps_v1/`, el espejo `escape/audio/sfx/steps/` está actualizado y el catálogo también.

**No se escuchó con oídos humanos.** Todo lo dicho sobre el timbre sale del diseño de las capas y del análisis de los archivos (espectro ponderado, caídas, espectrogramas). Falta la validación auditiva real.

### 2.86 Estado de los pendientes al cerrar la sesión del 2026-10-26

**Hecho en esta sesión:**
- FDAF: §1.76 (videos de NYDER), §1.77 (hover) y §1.78 (auditoría de dificultad).
- Escape: §2.80 (M9), §2.81 (manos de Freddy), §2.82 (cupcake de Chica), §2.83 (guitarra de Bonnie), §2.84 (tablero) y §2.85 (pasos v2).

**Sigue pendiente** (con su prioridad; las de §2.79 que no se nombran siguen igual):

| Id | Proyecto | Pendiente | Prioridad |
|---|---|---|---|
| A4 | Ambos | El trabajo ajeno sin publicar sigue igual (FlatLaf, `Juego.java` con baterías desde la N3, `NYDER.png`, íconos, `if3001…`, `dev/`). **Nuevo dato (§1.78):** el gate de la N3 dejaría la N2 en un 16 % de victorias. | Alta (decisión de su autor) |
| D1 | FDAF | Recomendación de equilibrio (§1.78): tabla de baterías N1 0.40/0.30, N2 0.46/0.34, N3 0.44/0.28, N4 0.56/0.38, N5 sin cambios, para una curva 53 → 39 → 33 → 30 → 27 %. No aplicada: choca con el comentario "Noche 1 — SIN CAMBIOS" y es un ajuste subjetivo. | Media (decisión del usuario) |
| D2 | FDAF | El sistema de puertas no da información antes de elegir, así que la habilidad no influye (§1.78). | Media (diseño) |
| T1 | Escape | Tablero (§2.84): en algunos planos asoma un borde fino del panel rojo de la puerta a la izquierda del volante. | Baja |
| P1 | Escape | Los pasos v2 (§2.85) no se escucharon con oídos humanos: falta validación auditiva real. | Media |
| P2 | Escape | Los ciclos de caminar siguen siendo asimétricos (B6 de §2.79); se nota en el ritmo de las pisadas. | Baja |
| M1, M3, M4, M6, M7, M8 | — | Sin cambios respecto de §2.79. | — |
| B11 | FDAF | `PruebaDestelloNyder` en inglés falló una vez en la regresión completa: el brillo muestreado llegó a 0.93 en lugar de 1.0, y por eso falló también la comparación de capturas. El destello mantiene el máximo solo 60 ms y la prueba lo muestrea con un temporizador, que con la máquina cargada cae fuera de la meseta. Repetida sola 3 veces: 5/5 OK cada vez (máximo de 1.00). Es intermitente y del arnés; el juego no cambió en esta sesión (`PnlWarning` y `ControllerMenu` sin cambios desde v2.12).  Recomendación: exigir ≥ 0.9 al brillo muestreado, o leer el pico de la curva teórica (`ControllerMenu`) en vez de muestrearlo. | Baja |


**Cerrados:** M9 (§2.80) y B10 (§1.74).

**Regresiones de esta sesión** (antes de publicar):
- **FDAF**, sobre el índice exportado (v2.15, sin trabajo ajeno ni `src/libs`): 32 ejecuciones, código de salida 0, `AVISOS: 0`. 203 líneas OK y 2 FALLO, ambas de B11 (repetida 3 veces: OK). `PruebaHover` 59 + 59, `PruebaHoverVuelta` 72 + 72, `VerificarRecursos` 198 rutas, `PruebaReinicio` 5/5, `FondoMenuBordes` OK.
- **Escape** (`run_tests.sh` completo, con el modo nuevo `personajes`): 21/21 ejecuciones con `FALLOS: 0`, 1634 líneas OK y 0 FALLO. Incluye `mundo_pasos` (pasos v2 con la misma sincronización), `mundo_personajes` (§2.81-§2.83) y la victoria completa y omitida en los dos idiomas (tablero v3, persecución con pasos v2).
- Al terminar no quedó ningún proceso de prueba vivo; solo el daemon de Gradle, que es normal.

---

## 3. Reglas transversales — aplican a ambos proyectos

1. **Verificar antes de asumir.** A lo largo de todo el desarrollo del Proyecto 1 hubo múltiples incidentes por asumir el estado de un archivo en vez de pedirlo/verificarlo (llaves duplicadas, métodos fuera de clase, campos sin declarar tras ediciones parciales). **Siempre pedir o revisar el archivo real antes de proponer un cambio quirúrgico sobre él, especialmente en archivos grandes como `ControllerInterfaz.java`.**
2. **Al editar archivos grandes y ya frágiles** (`ControllerInterfaz.java`, `ControllerMenu.java`), preferir bloques de reemplazo generosos y verificados sobre fragmentos mínimos — varios incidentes de esta sesión vinieron de parches quirúrgicos que dejaban el archivo en un estado inconsistente.
3. **`EscalarVista.java` (Proyecto 1) está congelado** — ver §1.7.
4. **No reintroducir el reproductor de frames propio para gifs** en el Proyecto 1 — ya descartado por calidad visual inferior.
5. **No reabrir la investigación de "estética pixelada FNAF1"** en el Proyecto 1 sin que el usuario lo pida explícitamente — ya se concluyó que no vale la pena.
6. **Los atajos CTRL+G/P/S del Proyecto 1 son intencionales** — no proponer eliminarlos como "limpieza".
7. **En el Proyecto 2, respetar `Architecture.md` v1.0 como contrato** — no introducir abstracciones nuevas sin que una necesidad real ya identificada las justifique (§3.1 del propio documento). No expandir el alcance del MVP (§2.2, lista de exclusiones) sin aprobación explícita del usuario.
8. **El usuario prefiere iteración incremental con verificación en cada paso**, tanto en el Proyecto 1 (rondas de auditoría→propuesta→implementación→confirmación) como en el plan del Proyecto 2 (no avanzar de paso sin confirmar el anterior). Mantener ese ritmo — no adelantar varios pasos a la vez.
9. **Versión (desde 2026-10-22, §1.32):** todo commit publicado de `five_doors_at_freddys` sube `MENOR` en `com.fdaf.util.Version` (v2.1→v2.2→…→v2.100→v3.1). Única fuente de verdad: esa clase.
10. **Recursos (desde 2026-10-22, §1.54-§1.56):** todo archivo de imagen, audio, vídeo, gif o modelo de ambos juegos se nombra en inglés `snake_case` (`<sujeto>_<descripcion>[_<estado>][_<idioma>]`, idioma `_en`/`_es` al final, secuencias con 2 dígitos) y va en la carpeta de su FUNCIÓN (`audio/`, `backgrounds/`, `ui/...`, `models/`...), nunca por formato. Al añadir, renombrar o retirar un recurso: (1) actualizar `docs/catalogo_recursos.md` del repo (y su copia en `FDAF assets/CATALOGO_RECURSOS.md`); (2) dejar el archivo preparado para el juego con el MISMO nombre y ruta relativa en `FDAF assets/` y, si viene de un original distinto (recorte, perspectiva, normalización), conservar el original al lado con sufijo `_original`; (3) comprobar con una verificación de rutas que el código lo encuentra (también las rutas construidas por código). No mover ni renombrar lo interno de `FDAF assets/source_files/3d_models/` (los `.blend` enlazan texturas con rutas relativas) ni de `_reference/`.

---

## 4. Diseños pendientes de aprobación — NO implementados (propuestos 2026-10-22)

> **Superado (2026-10-23):** el usuario rechazó estas propuestas por demasiado sencillas y pidió cinemáticas reales. Rendirse se implementó como "dentro del traje" (§1.59), no como R1. La victoria de Escape se hizo con la dirección C (exterior, estacionamiento y auto, §2.49) y la derrota definitiva como "La última función" (§2.50). Se conserva el texto como registro de lo que se propuso.

Ninguna de estas cinemáticas existe en el código. Son propuestas para que el usuario elija; no implementar ninguna sin su aprobación explícita. Los recursos se citan por su nombre actual (§1.54, §2.48).

### 4.1 Cinemática al rendirse (FDAF, 3-8 s)

**Flujo actual:** mantener la barra Rendirse ~2.5 s → `ControllerCamara.rendirse()` → `detenerSonidosJuego()` → `reiniciarJuegoCompleto()` (pantalla "Cargando..." y relanzamiento del proceso, §1.18). El progreso no cambia. No hay ningún efecto entre completar la barra y la pantalla de carga.

**Requisitos comunes a las tres:** saltable con clic, ESC o CTRL+S desde el primer instante; nunca más de 8 s; sin texto largo; se reproduce dentro del proceso actual y el reinicio ocurre al terminar (o al saltarla). Como el proceso se reinicia después de cada rendición, "no molestar si se repite" no puede depender de memoria en ejecución: se resuelve haciéndola corta y saltable (opcionalmente, una versión de ~2 s a partir de la segunda vez guardando un contador en `config.properties`, lo que añadiría un campo nuevo a la configuración del jugador).

| | R1 — Apagón voluntario | R2 — Fin de turno | R3 — Te están mirando |
|---|---|---|---|
| Experiencia | La oficina parpadea y se apaga con `power_down.wav`; en la oscuridad solo se ven unos ojos en una puerta; texto pequeño "Te rendiste" / "You gave up"; fundido a negro. ~4 s | La tableta se cierra sola, la cámara vuelve al centro, suena un sello y aparece una tarjeta de fichaje "SALIDA 2:13 AM"; fundido. ~4 s | Ráfaga de estática (`static.gif` + `static.wav`, 1 s); al volver, Freddy está en el marco de la puerta mirando en silencio 2 s; corte a negro. ~4 s |
| Reutilizable | `backgrounds/office.png`, `audio/game_over/power_down.wav`, el fundido de `iniciarFadeDesdeNegro`, los ojos del primer cuadro de `freddy_power_out_jumpscare.gif` | `ui/tablet/tablet_close.gif`, el paneo de `ControllerCamara`, el fundido existente | `backgrounds/static.gif`, `audio/game_over/static.wav`, las imágenes de `doors/` |
| Nuevo | 2 cadenas ES/EN | Imagen de la tarjeta (ES/EN), sonido de sello, una hora simulada (el juego no tiene reloj) | Una imagen de Freddy asomado al marco (no existe; los gifs de jumpscare no sirven como pose quieta) |
| Complejidad | Baja: un panel con `Timer` y `paintComponent`, mismo patrón que el glitch de `TransicionEscape` | Media | Media (por el arte nuevo) |
| Riesgos | Que parezca una derrota; se evita con el texto y sin grito | Inventar una hora que contradiga la tarjeta "12:00 AM" de la intro | Parecerse a un jumpscare normal y asustar al rendirse, que no es lo que el jugador espera |

**Recomendación:** R1. Solo usa recursos que ya están en el repo, comunica bien "me fui", dura poco y su lenguaje (el apagón) ya es parte del juego.

### 4.2 Cinemática de victoria de Escape (10-20 s)

**Flujo actual:** con la llave, E sobre la puerta de salida → `interactuarConPuertaSalida()` → `EscapeVictoryScreen` (pantalla negra con "ESCAPASTE"/"YOU ESCAPED", `victory_circus.wav` en bucle, ESC para salir).

| | A — Sencillo | B — Exterior | C — Estacionamiento y auto |
|---|---|---|---|
| Experiencia | La puerta de salida gira y se abre, la cámara avanza hacia el umbral mientras el latido se calma; una luz blanca inunda la pantalla; aparece "ESCAPASTE" y entra `victory_circus.wav`; antes del blanco, por un instante, los animatrónicos quietos en el pasillo mirando. ~12 s | Igual que A, pero la cámara cruza la puerta: noche, fachada de la pizzería, letrero; la cámara se aleja y en una ventana se ve a Freddy. ~15 s | Sales al estacionamiento, caminas hasta un auto, se cierra la puerta, arranca el motor y por el retrovisor se ve a los animatrónicos en la entrada; las luces del auto se alejan. ~18-20 s |
| Reutilizable | `pizzeria.glb` (la hoja de la puerta de salida es un nodo propio, `Object_60` de `puerta.005_46`, que se puede rotar en código), los 5 modelos y sus poses, el sistema de tomas de la cinemática inicial (§2.27, §2.44), `victory_circus.wav`, `heartbeat_*` | Todo lo de A | Todo lo de A |
| Nuevo | Nada obligatorio (opcional: un sonido de puerta abriéndose) | Exterior completo: fachada, suelo, cielo nocturno, letrero. `pizzeria.glb` es un interior de Sketchfab (el techo hubo que añadirlo a mano, §2.21); no se ha comprobado si sus paredes tienen cara exterior, lo probable es que se vean huecas desde fuera | Exterior de B + modelo de auto (no hay ninguno en `source_files/3d_models` ni en `_reference/`) + estacionamiento, luces y animación de conducción; licencias de los modelos nuevos |
| Complejidad | Baja-media | Alta (arte 3D e iluminación exterior distinta) | Muy alta |
| Riesgos | Bajos: geometría ya conocida; hay que congelar a los guardias (patrón de `posicionarModelosParaCinematica`) | Paredes sin cara exterior, cielo `IBLBuilder` de interior, rendimiento | Todos los de B, más la animación del auto y mucho tiempo de arte |

**Recomendación honesta:** A. Se puede construir con lo que ya existe y da el cierre que hoy falta (se pasa de la puerta a una pantalla negra). B y C solo valen la pena si se consiguen modelos exteriores y de auto con licencia adecuada; C es la de mayor impacto y también la de mayor riesgo, y no recomendaría empezarla sin esos modelos.

### 4.3 Cinemática de derrota definitiva de Escape

**Flujo actual:** cada atrapada → `iniciarJumpscare` (cámara pegada al animatrónico, `jumpscare_scream.wav`, 1 s) → `ESTATICA` (`static.gif` + `static.wav`, 5 s) → `resolverFinDeAtrapada()`: si quedan vidas, corazones y reaparición; si era la última, `NightGameOverScreen` (Reintentar / Salir).

**Regla para las tres:** las atrapadas que no son la última NO cambian (jumpscare + estática + corazones, ~8 s; hay que poder volver a jugar rápido). Solo la última sustituye la estática por la cinemática, que debe poder saltarse (clic, ESC o E) tras el primer segundo y terminar en el `NightGameOverScreen` actual.

| | D1 — Arrastrado | D2 — Una nueva atracción | D3 — Cámaras de seguridad |
|---|---|---|---|
| Experiencia | Tras el jumpscare la cámara cae al suelo, el animatrónico que te atrapó queda de pie sobre ti, la linterna rueda y parpadea hasta apagarse, se oye un arrastre; negro. ~6 s | Corte a una toma fija del escenario a oscuras: los animatrónicos en su sitio y, entre ellos, un traje nuevo inmóvil; texto "Nueva atracción" / "New attraction". ~7 s | La imagen se vuelve una cámara de seguridad (grano, punto REC, rótulo de cámara): tomas fijas de la oficina vacía y del pasillo mientras los animatrónicos vuelven a sus puestos; la última toma se corta con estática. ~8 s |
| Reutilizable | El modelo y la posición del que atrapó, la cámara y la linterna (`SpotLightEx`), `heavy_breathing.wav`, `static.wav` | Modelos y escenario del mapa, sistema de tomas | Sistema de tomas de la cinemática inicial, `static.gif`; de FDAF, el punto REC (`ui/tablet/camera_rec_dot.gif`) y el estilo de la tableta, que une los dos juegos |
| Nuevo | Un sonido de arrastre | Un traje/modelo adicional (no existe) | Un overlay 2D de grano (parecido a la viñeta existente) y llevar a los guardias a su spawn durante la toma |
| Complejidad | Baja-media | Media-alta (modelo nuevo) | Media |
| Riesgos | Que la cámara atraviese geometría al caer (comprobar con `CollisionWorld`, como en §2.43) | Depende de un modelo que no existe; el tono es más oscuro y conviene confirmarlo con el usuario | Tomas con la línea de visión tapada (usar `lineaDeVisionLibre`, §2.43) |

**Recomendación:** D3, porque reutiliza casi todo, conecta visualmente Escape con la tableta de FDAF y deja claro que la partida terminó sin repetir el jumpscare. D1 es la alternativa más barata si se prefiere algo más físico.

### 4.4 Orden recomendado

1. **R1 (rendirse, FDAF):** la más pequeña, sin recursos nuevos, sobre un flujo que ya termina en un reinicio.
2. **D3 o D1 (derrota definitiva, Escape):** se ve en cada partida perdida y reutiliza la cámara y las tomas existentes.
3. **A (victoria, Escape):** cierra la experiencia con recursos existentes.
4. **B o C:** solo después, y solo si se consiguen modelos exteriores y de auto con licencia.
