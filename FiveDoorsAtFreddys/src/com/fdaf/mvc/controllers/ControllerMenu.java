package com.fdaf.mvc.controllers;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import com.fdaf.mvc.models.juego.Noche;
import com.fdaf.mvc.views.frames.VistaPrincipal;
import com.fdaf.mvc.views.frames.pnl.PnlAplicarImagen;
import com.fdaf.mvc.views.frames.pnl.PnlIdioma;
import com.fdaf.mvc.views.frames.pnl.PnlMenu;
import com.fdaf.mvc.views.frames.pnl.PnlNochePersonalizada;
import com.fdaf.mvc.views.frames.pnl.PnlOpciones;
import com.fdaf.mvc.views.frames.pnl.PnlVolumen;
import com.fdaf.mvc.views.multimedia.Sonido;
import com.fdaf.util.CargarImagenes;
import com.fdaf.util.EscalarVista;
import com.fdaf.util.Idioma;
import com.fdaf.util.InteraccionBoton;
import com.fdaf.util.PreferenciasJuego;
import com.fdaf.util.TextosInfo;

@SuppressWarnings("unused")
public class ControllerMenu {

	private PnlMenu menu;
	private PnlOpciones opciones;
	private VistaPrincipal vp;
	private Sonido musicaMenu;
	private Sonido sonidoEstaticaMenu;
	private Timer timerTopeEstaticaMenu;
	private PnlIdioma pantallaIdioma;
	private PnlVolumen pantallaVolumen;
	private PnlNochePersonalizada pantallaNochePersonalizada;
	// Introducción inicial, "Cómo jugar" y "Acerca del proyecto" (CLAUDE.md §1.33-§1.34):
	// una sola instancia reutilizada por las tres pantallas.
	private com.fdaf.mvc.views.frames.pnl.PnlInfo pantallaInfo;
	private boolean introduccionActiva = false;



	// Referencia a la partida en curso (null si no hay ninguna) y al
	// temporizador de la intro activa (null si no hay intro corriendo).
	// Los usan tanto el flujo normal como los atajos de depuración.
	private ControllerInterfaz partidaActiva;
	private Timer timerIntroActivo;
	private String rutaIntroActiva;
	// Sonido de la tableta sincronizado con las franjas blancas de la intro
	// (CLAUDE.md §1.64): pendiente hasta que suena o la intro termina.
	private final java.util.concurrent.atomic.AtomicBoolean sonidoFranjasPendiente = new java.util.concurrent.atomic.AtomicBoolean();

	// "Warning activo" = true desde que arranca la secuencia, false
	// desde el instante en que se muestra el menú -- sin importar si
	// llegó ahí por el timer natural o por CTRL+S. timerAdvertenciaActual
	// apunta siempre al timer que está corriendo en ESE momento (el de
	// fade-in, el de espera, o el de fade-out), para poder detenerlo
	// desde afuera sin duplicar ninguno de los 3.
	private boolean advertenciaActiva = false;
	private Timer timerAdvertenciaActual;

	// true cuando este ControllerMenu nace de un reinicio interno de proceso
	// (ReiniciadorJuego, ganar 1-4/perder/rendirse -- CLAUDE.md #1.18), no de
	// un lanzamiento externo real. En ese caso init() nunca debe mostrar la
	// advertencia inicial otra vez -- para el jugador es la continuacion de
	// la misma sesion, no "entrar al juego desde cero".
	private final boolean reinicioInterno;

	// Timer del mensaje transitorio "se desbloquea al completar la Noche 5"
	// (boton ESCAPE bloqueado) -- mismo criterio que timerAdvertenciaActual:
	// un solo campo para poder cancelar el anterior si el jugador insiste
	// en hacer clic varias veces seguidas, sin apilar timers.
	private Timer timerMensajeEscapeBloqueado;

	public boolean isAdvertenciaActiva() {
		return advertenciaActiva;
	}

	public ControllerMenu() {
		this(false);
	}

	public ControllerMenu(boolean reinicioInterno) {

		this.reinicioInterno = reinicioInterno;

		menu = new PnlMenu();
	    opciones = new PnlOpciones();
	    pantallaIdioma = new PnlIdioma();
	    pantallaVolumen = new PnlVolumen();
	    pantallaNochePersonalizada = new PnlNochePersonalizada();
	    pantallaInfo = new com.fdaf.mvc.views.frames.pnl.PnlInfo();
	    vp = new VistaPrincipal();

	    EscalarVista.adaptarMenu(vp, menu);
	    EscalarVista.adaptarOpciones(vp, opciones);
	    EscalarVista.adaptarIdioma(vp, pantallaIdioma);
	    EscalarVista.adaptarVolumen(vp, pantallaVolumen);
	    EscalarVista.adaptarNochePersonalizada(vp, pantallaNochePersonalizada);

	    // CAMBIO: ya no se llama aquí. La música del menú debe iniciar
	    // únicamente cuando el menú se muestra por primera vez, después
	    // de la advertencia inicial -- se llama desde
	    // mostrarMenuPrincipal().
	}

	public void init() {
		botones();
		aplicarHoverYSonidoATodos();
		actualizarTextosIdioma();
		registrarAtajosDebug();
		registrarAtajoEscape();

		if (reinicioInterno) {
			// Reinicio interno (ver campo reinicioInterno arriba): el jugador
			// ya vio la advertencia al entrar por primera vez a esta sesion
			// de juego -- mostrarla otra vez en cada noche seria repetitiva
			// e innecesaria, exactamente lo que el usuario pidio evitar. Va
			// directo al mismo destino final que finalizarAdvertenciaYMostrarMenu(),
			// sin instanciar PnlWarning ni tocar advertenciaActiva.
			mostrarMenuPrincipal();
		} else {
			mostrarAdvertenciaInicial();
		}

		vp.init();

		// Con la primera pantalla ya creada y visible, adelanta en segundo
		// plano las imágenes grandes que se usarán más tarde (§1.52).
		javax.swing.SwingUtilities.invokeLater(CargarImagenes::precargarEnSegundoPlano);
	}
	
	// Presentación de NYDER (CLAUDE.md §1.29): va ANTES de la advertencia,
	// dentro de la misma secuencia de inicio (advertenciaActiva ya es true)
	// y con el mismo animarFade/timerAdvertenciaActual -- nunca hay dos
	// timers de la secuencia corriendo a la vez. ~3s en total.
	private static final String RUTA_NYDER = "/branding/nyder_games_splash.png"; // §1.37
	// 250 negro + 800 entrada + 1300 visible + 650 salida = 3.0s, contados
	// desde el primer pintado real de la ventana (§1.31).
	private static final int NYDER_NEGRO_MS = 250;
	private static final int NYDER_FADE_IN_MS = 800;
	private static final int NYDER_VISIBLE_MS = 1300;
	private static final int NYDER_FADE_OUT_MS = 650;
	private static final int NYDER_RESPALDO_SIN_PINTADO_MS = 1500;
	private boolean presentacionNyderActiva = false;
	private boolean entradaNyderIniciada = false;
	private java.awt.Image imagenNyder;

	// Destello de la estrella de la D (CLAUDE.md §1.65), dentro de los
	// 1300 ms en que NYDER está totalmente visible: espera 120 ms, sube en
	// 420 ms, se sostiene 60 ms y se apaga en 600 ms (1200 ms en total). El
	// sonido arranca a los 160 ms para que su subida coincida con el pico de
	// luz. No alarga la presentación ni toca su orden.
	private static final int DESTELLO_ESPERA_MS = 120;
	private static final int DESTELLO_SUBIDA_MS = 420;
	private static final int DESTELLO_PICO_MS = 60;
	private static final int DESTELLO_BAJADA_MS = 600;
	private static final int DESTELLO_SONIDO_MS = 160;
	private Timer timerDestelloNyder;
	private volatile Sonido sonidoDestelloNyder;
	private boolean sonidoDestelloReproducido;

	private void mostrarAdvertenciaInicial() {
		advertenciaActiva = true;
		mostrarSelectorIdioma();
	}

	// Único punto que cambia el idioma: lo usan Opciones > Idioma y el
	// selector inicial (§1.35). Fija la preferencia, refresca todos los
	// textos del menú y la persiste en config.properties.
	private void cambiarIdioma(Idioma idioma) {
		PreferenciasJuego.idiomaSeleccionado = idioma;
		actualizarTextosIdioma();
		com.fdaf.util.PersistenciaJuego.guardar();
	}

	// Selector de idioma (CLAUDE.md §1.35): primer paso del inicio normal,
	// antes de NYDER, para que la advertencia, la introducción y el menú ya
	// se construyan en el idioma elegido. El reinicio interno no pasa por
	// aquí (init() va directo al menú). Sin timers: espera al jugador.
	private boolean selectorIdiomaActivo = false;

	private void mostrarSelectorIdioma() {
		selectorIdiomaActivo = true;
		com.fdaf.mvc.views.frames.pnl.PnlSelectorIdioma pantalla = new com.fdaf.mvc.views.frames.pnl.PnlSelectorIdioma(
				menu.getLblLogoNyder().getIcon(), PreferenciasJuego.idiomaSeleccionado, this::aplicarIdiomaInicial);
		vp.setContenido(pantalla);
	}

	private void aplicarIdiomaInicial(Idioma idioma) {
		if (!selectorIdiomaActivo) {
			return;
		}
		selectorIdiomaActivo = false;
		cambiarIdioma(idioma);
		mostrarPresentacionNyder();
	}

	public boolean isSelectorIdiomaActivo() {
		return selectorIdiomaActivo;
	}

	private void mostrarPresentacionNyder() {
		java.net.URL recurso = getClass().getResource(RUTA_NYDER);
		if (recurso == null) {
			System.out.println("[RECURSO NO ENCONTRADO] " + RUTA_NYDER);
			mostrarPantallaAdvertencia();
			return;
		}
		presentacionNyderActiva = true;
		imagenNyder = new javax.swing.ImageIcon(recurso).getImage();

		com.fdaf.mvc.views.frames.pnl.PnlWarning pantalla =
				new com.fdaf.mvc.views.frames.pnl.PnlWarning(imagenNyder, 0.55);
		EscalarVista.adaptarWarning(vp, pantalla);
		pantalla.setAlpha(0.0f);
		vp.setContenido(pantalla);
		prepararDestelloNyder(pantalla);

		// La secuencia arranca cuando el panel negro ya se pintó de verdad
		// (no al crearlo: la ventana todavía no es visible en ese momento).
		// El respaldo cubre el caso improbable de que nunca llegue a pintarse;
		// entradaNyderIniciada garantiza que solo una de las dos vías arranque.
		Timer respaldo = new Timer(NYDER_RESPALDO_SIN_PINTADO_MS, e -> iniciarEntradaNyder(pantalla));
		respaldo.setRepeats(false);
		timerAdvertenciaActual = respaldo;
		pantalla.alPrimerPintado(() -> {
			respaldo.stop();
			iniciarEntradaNyder(pantalla);
		});
		respaldo.start();
	}

	private void iniciarEntradaNyder(com.fdaf.mvc.views.frames.pnl.PnlWarning pantalla) {
		if (!presentacionNyderActiva || entradaNyderIniciada) {
			return; // ya arrancó, o CTRL+S ya saltó la presentación
		}
		entradaNyderIniciada = true;

		Timer negro = new Timer(NYDER_NEGRO_MS, e0 -> {
			((Timer) e0.getSource()).stop();
			animarFade(pantalla, 0.0f, 1.0f, NYDER_FADE_IN_MS, () -> {
				iniciarDestelloNyder(pantalla);
				Timer esperar = new Timer(NYDER_VISIBLE_MS, e1 -> {
					((Timer) e1.getSource()).stop();
					animarFade(pantalla, 1.0f, 0.0f, NYDER_FADE_OUT_MS, this::finalizarPresentacionNyder);
				});
				esperar.setRepeats(false);
				timerAdvertenciaActual = esperar;
				esperar.start();
			});
		});
		negro.setRepeats(false);
		timerAdvertenciaActual = negro;
		negro.start();
	}

	// Único cierre de la presentación (fade-out natural o CTRL+S): libera
	// la imagen (solo se usa una vez por inicio) y arranca la advertencia.
	private void finalizarPresentacionNyder() {
		presentacionNyderActiva = false;
		detenerDestelloNyder();
		if (imagenNyder != null) {
			imagenNyder.flush();
			imagenNyder = null;
		}
		mostrarPantallaAdvertencia();
	}

	// La máscara de la estrella y el sonido se preparan en segundo plano en
	// cuanto aparece la pantalla negra de NYDER (antes de la entrada).
	private void prepararDestelloNyder(com.fdaf.mvc.views.frames.pnl.PnlWarning pantalla) {
		pantalla.prepararDestelloEstrella();
		sonidoDestelloReproducido = false;
		Thread carga = new Thread(() -> sonidoDestelloNyder = new Sonido("sfx/nyder_star_sparkle.wav"),
				"nyder-sonido-destello");
		carga.setDaemon(true);
		carga.start();
	}

	private void iniciarDestelloNyder(com.fdaf.mvc.views.frames.pnl.PnlWarning pantalla) {
		if (!presentacionNyderActiva || !pantalla.isDestelloEstrellaListo()) {
			return;
		}
		final long inicio = System.nanoTime();
		final int total = DESTELLO_ESPERA_MS + DESTELLO_SUBIDA_MS + DESTELLO_PICO_MS + DESTELLO_BAJADA_MS;
		timerDestelloNyder = new Timer(16, e -> {
			int t = (int) ((System.nanoTime() - inicio) / 1_000_000L);
			if (!sonidoDestelloReproducido && t >= DESTELLO_SONIDO_MS) {
				// una sola vez por presentación, fuera del hilo de Swing
				sonidoDestelloReproducido = true;
				final Sonido s = sonidoDestelloNyder;
				if (s != null) {
					Thread reproducir = new Thread(s::play, "nyder-destello-play");
					reproducir.setDaemon(true);
					reproducir.start();
				}
			}
			float brillo;
			int u = t - DESTELLO_ESPERA_MS;
			if (u <= 0) {
				brillo = 0f;
			} else if (u < DESTELLO_SUBIDA_MS) {
				float k = u / (float) DESTELLO_SUBIDA_MS;
				brillo = k * k;
			} else if (u < DESTELLO_SUBIDA_MS + DESTELLO_PICO_MS) {
				brillo = 1f;
			} else {
				float k = Math.min(1f, (u - DESTELLO_SUBIDA_MS - DESTELLO_PICO_MS) / (float) DESTELLO_BAJADA_MS);
				brillo = (1f - k) * (1f - k);
			}
			pantalla.setBrilloEstrella(brillo);
			if (t >= total) {
				((Timer) e.getSource()).stop();
				pantalla.setBrilloEstrella(0f);
			}
		});
		timerDestelloNyder.start();
	}

	// Omitir la presentación (CTRL+S) o su fin natural: corta el destello y,
	// si el sonido todavía sonaba, lo detiene (en un hilo aparte: Clip.stop
	// puede bloquear en Java 8, §1.52). Un sonido que ya terminó se cerró solo.
	private void detenerDestelloNyder() {
		if (timerDestelloNyder != null) {
			timerDestelloNyder.stop();
			timerDestelloNyder = null;
		}
		final Sonido s = sonidoDestelloNyder;
		sonidoDestelloNyder = null;
		if (s != null && !sonidoDestelloReproducido) {
			Thread cerrar = new Thread(s::cerrar, "nyder-destello-cerrar");
			cerrar.setDaemon(true);
			cerrar.start();
		}
		sonidoDestelloReproducido = true;
	}

	private void mostrarPantallaAdvertencia() {

		com.fdaf.mvc.views.frames.pnl.PnlWarning pantalla = new com.fdaf.mvc.views.frames.pnl.PnlWarning();
		EscalarVista.adaptarWarning(vp, pantalla);
		pantalla.setAlpha(0.0f); // invisible desde el primer frame
		vp.setContenido(pantalla);

		animarFade(pantalla, 0.0f, 1.0f, 1000, () -> {

			// AJUSTE: 3 segundos visible (antes 6). Los fades (1000ms
			// cada uno) no se tocaron.
			Timer esperar = new Timer(3000, e1 -> {
				((Timer) e1.getSource()).stop();
				animarFade(pantalla, 1.0f, 0.0f, 1000, this::finalizarAdvertenciaYMostrarMenu);
			});
			esperar.setRepeats(false);
			timerAdvertenciaActual = esperar;
			esperar.start();
		});
	}

	// Rutina única de fade, parametrizable por dirección -- se reutiliza
	// tanto para el fade-in (0.0 -> 1.0) como para el fade-out
	// (1.0 -> 0.0) de la advertencia. Una sola implementación de la
	// animación basada en tiempo transcurrido, sin duplicarla.
	private void animarFade(com.fdaf.mvc.views.frames.pnl.PnlWarning pantalla,
			float desde, float hasta, int duracionMs, Runnable alTerminar) {

		long inicio = System.currentTimeMillis();

		Timer fade = new Timer(20, null);
		fade.addActionListener(e -> {
			long transcurrido = System.currentTimeMillis() - inicio;
			double progreso = (double) transcurrido / duracionMs;

			if (progreso >= 1.0) {
				((Timer) e.getSource()).stop();
				pantalla.setAlpha(hasta);
				if (alTerminar != null) {
					alTerminar.run();
				}
			} else {
				float valorActual = (float) (desde + (hasta - desde) * progreso);
				pantalla.setAlpha(valorActual);
			}
		});
		timerAdvertenciaActual = fade;
		fade.start();
	}

	// Único cuerpo real de "la advertencia terminó" -- lo dispara
	// tanto el fade-out natural como saltarAdvertencia(). Nunca hay
	// una segunda ruta.
	private void finalizarAdvertenciaYMostrarMenu() {
		advertenciaActiva = false;
		mostrarIntroduccion();
	}

	// Introducción informativa (CLAUDE.md §1.33): va después de la
	// advertencia y antes del menú, solo en el inicio normal del juego (el
	// reinicio interno tras ganar/perder/rendirse o volver de Escape va
	// directo al menú sin pasar por aquí). Sin timers: avanza solo con los
	// botones del jugador.
	private void mostrarIntroduccion() {
		introduccionActiva = true;
		pantallaInfo.configurar(TextosInfo.introTitulos(), TextosInfo.introCuerpos(), null, null,
				TextosInfo.botonOmitir(), TextosInfo.botonIrAlMenu(), this::cerrarIntroduccion);
		vp.setContenido(pantallaInfo);
	}

	// Único cierre de la introducción: "Omitir", "Ir al menú" en la
	// última pantalla o CTRL+S.
	private void cerrarIntroduccion() {
		if (!introduccionActiva) {
			return;
		}
		introduccionActiva = false;
		mostrarMenuPrincipal();
	}

	public boolean isIntroduccionActiva() {
		return introduccionActiva;
	}

	// "Cómo jugar" (§1.34): las pestañas permiten ir directo a un tema. La
	// sección de Escape solo se explica si ya está desbloqueado; si no, es
	// un consejo sobre la Noche 5 que no revela la sorpresa.
	private void abrirComoJugar() {
		boolean escape = PreferenciasJuego.escapeDesbloqueado;
		String[] titulos = TextosInfo.guiaTitulos(escape);
		pantallaInfo.configurar(titulos, TextosInfo.guiaCuerpos(escape), titulos, null,
				TextosInfo.botonVolverAlMenu(), TextosInfo.botonVolverAlMenu(), () -> vp.setContenido(menu));
		vp.setContenido(pantallaInfo);
	}

	// "Acerca del proyecto" (§1.34): una sola página, con el logo de NYDER
	// que el menú ya tiene escalado (no se vuelve a cargar el PNG).
	private void abrirAcercaDe() {
		pantallaInfo.configurar(new String[] { TextosInfo.acercaTitulo() },
				new String[] { TextosInfo.acercaCuerpo() }, null, menu.getLblLogoNyder().getIcon(), null,
				TextosInfo.botonVolverAlMenu(), () -> vp.setContenido(menu));
		vp.setContenido(pantallaInfo);
	}

	// Único punto de entrada externo. Si la advertencia ya no está
	// activa, no hace nada.
	public void saltarAdvertencia() {
		if (!advertenciaActiva) {
			return;
		}
		// El idioma tiene que elegirse: CTRL+S no salta el selector.
		if (selectorIdiomaActivo) {
			return;
		}
		if (timerAdvertenciaActual != null && timerAdvertenciaActual.isRunning()) {
			timerAdvertenciaActual.stop();
		}
		// Durante NYDER, CTRL+S salta solo la presentación: la advertencia
		// se sigue mostrando completa, como antes.
		if (presentacionNyderActiva) {
			// Omitida: si el destello ya sonaba, se corta junto con la imagen.
			final Sonido destello = sonidoDestelloNyder;
			if (destello != null && sonidoDestelloReproducido) {
				sonidoDestelloNyder = null;
				Thread cerrar = new Thread(destello::cerrar, "nyder-destello-cerrar");
				cerrar.setDaemon(true);
				cerrar.start();
			}
			finalizarPresentacionNyder();
			return;
		}
		finalizarAdvertenciaYMostrarMenu();
	}

	// Se muestra el menú por primera vez y, exactamente aquí, arranca su
	// música -- nunca antes de que el jugador vea el menú realmente.
		private void mostrarMenuPrincipal() {
			// Victoria de Five Doors Escape pendiente (CLAUDE.md §1.66-§1.67):
			// primero el agradecimiento (y la estrella, si es la primera vez),
			// después el menú de siempre, ya con la estrella.
			int victoria = com.fdaf.util.LogroEscape.consumirVictoriaPendiente();
			if (victoria != com.fdaf.util.LogroEscape.SIN_VICTORIA) {
				actualizarTextosIdioma();
				mostrarAgradecimiento(victoria == com.fdaf.util.LogroEscape.PRIMERA_VICTORIA);
				return;
			}
			vp.setContenido(menu);
			ponerMusicaMenu();
			iniciarEstaticaMenu();
		}

		private void mostrarAgradecimiento(boolean primeraVez) {
			java.net.URL recursoLogo = getClass().getResource("/branding/nyder_logo.png");
			final java.awt.Image logo = recursoLogo != null ? new javax.swing.ImageIcon(recursoLogo).getImage() : null;
			final com.fdaf.mvc.views.frames.pnl.PnlAgradecimiento[] pantalla = { null };
			pantalla[0] = new com.fdaf.mvc.views.frames.pnl.PnlAgradecimiento(logo, primeraVez, () -> {
				if (logo != null) {
					logo.flush();
				}
				vp.setContenido(menu);
				ponerMusicaMenu();
				iniciarEstaticaMenu();
			});
			pantalla[0].setBounds(0, 0, menu.getWidth(), menu.getHeight());
			vp.setContenido(pantalla[0]);
			pantalla[0].iniciar();
		}

		// Efecto ambiental TEMPORAL, no estática permanente. Decide en
		// tiempo de ejecución (no por suposición) si el archivo ya dura lo
		// suficiente por sí solo o si necesita loop -- y si necesita loop,
		// lo corta exactamente a los 32 segundos. Misma instancia siempre;
		// se reutiliza tanto al mostrar el menú por primera vez como al
		// regresar de una partida, aplicando la misma regla cada vez.
		private void iniciarEstaticaMenu() {

			if (sonidoEstaticaMenu == null) {
				sonidoEstaticaMenu = new Sonido("ambience/menu_static.wav");
			}

			if (timerTopeEstaticaMenu != null && timerTopeEstaticaMenu.isRunning()) {
				timerTopeEstaticaMenu.stop();
			}

			long duracionMs = sonidoEstaticaMenu.getDuracionMs();

			if (duracionMs >= 32000) {
				// Ya dura lo suficiente por sí solo -- una sola reproducción.
				sonidoEstaticaMenu.play();
			} else {
				// Corto: necesita loop, pero se limita a 32s como efecto
				// temporal, no estática permanente durante toda la estancia
				// en el menú.
				sonidoEstaticaMenu.loop();

				timerTopeEstaticaMenu = new Timer(32000, e -> {
					((Timer) e.getSource()).stop();
					if (sonidoEstaticaMenu != null) {
						sonidoEstaticaMenu.stop();
					}
				});
				timerTopeEstaticaMenu.setRepeats(false);
				timerTopeEstaticaMenu.start();
			}
		}
	
	private void ponerMusicaMenu() {
	    musicaMenu = new Sonido("music/menu_music.wav");
	    musicaMenu.loop();
	    musicaMenu.subirVolumen(3.5f);
	}

	// Respaldo cuando el proceso no se pudo relanzar (ReiniciadorJuego, "siFalla"):
	// vuelve al menú en este mismo proceso, pero antes recoge una victoria de
	// Escape pendiente (agradecimiento y estrella, §1.66-§1.67, auditoría M5).
	public void volverAlMenuSinReiniciar() {
		int victoria = com.fdaf.util.LogroEscape.consumirVictoriaPendiente();
		if (victoria != com.fdaf.util.LogroEscape.SIN_VICTORIA) {
			actualizarTextosIdioma();
			mostrarAgradecimiento(victoria == com.fdaf.util.LogroEscape.PRIMERA_VICTORIA);
			return;
		}
		vp.setContenido(menu);
		reanudarMusicaMenu();
	}

	public void reanudarMusicaMenu() {
		if (musicaMenu != null) {
			musicaMenu.loop();
		}
		iniciarEstaticaMenu();

	}

	// Mensaje transitorio bajo el boton ESCAPE cuando todavia esta
	// bloqueado -- mismo patron de "mostrar 3s y ocultar solo" que ya usa
	// mostrarAdvertenciaInicial() para su ventana de espera, aplicado aqui
	// sin fundido (no hace falta, es solo texto informativo).
	private void mostrarMensajeEscapeBloqueado() {
		boolean ingles = (PreferenciasJuego.idiomaSeleccionado == Idioma.INGLES);
		mostrarMensajeBloqueado(ingles
				? "Unlocks after completing the last night."
				: "Se desbloquea al completar la última noche.");
	}

	// Mismo rótulo y mismo temporizador de 3 s para ESCAPE y para las noches
	// bloqueadas (§1.66): un mensaje nuevo reemplaza al anterior.
	private void mostrarMensajeBloqueado(String texto) {
		pantallaNochePersonalizada.getLblEscapeBloqueado().setText("<html>" + texto + "</html>");
		pantallaNochePersonalizada.getLblEscapeBloqueado().setVisible(true);

		if (timerMensajeEscapeBloqueado != null && timerMensajeEscapeBloqueado.isRunning()) {
			timerMensajeEscapeBloqueado.stop();
		}
		timerMensajeEscapeBloqueado = new Timer(3000, ev -> {
			((Timer) ev.getSource()).stop();
			pantallaNochePersonalizada.getLblEscapeBloqueado().setVisible(false);
		});
		timerMensajeEscapeBloqueado.setRepeats(false);
		timerMensajeEscapeBloqueado.start();
	}

	// Detiene el audio del menu sin arrancar ninguna intro -- version
	// minima de detenerAudioMenuYComenzarPartida() para el unico otro
	// caso real que necesita silenciar el menu sin ir a jugar una noche:
	// lanzar Escape directo desde Custom Night.
	private void detenerAudioMenu() {
		if (musicaMenu != null) {
			musicaMenu.stop();
		}
		if (sonidoEstaticaMenu != null) {
			sonidoEstaticaMenu.stop();
		}
		if (timerTopeEstaticaMenu != null && timerTopeEstaticaMenu.isRunning()) {
			timerTopeEstaticaMenu.stop();
		}
	}

	// Acceso directo a Five Doors Escape desde Custom Night, una vez
	// desbloqueado. Pedido explicito del usuario 2026-08-10: la experiencia
	// debe ser EQUIVALENTE a ganar la Noche 5 desde el punto donde empieza
	// el glitch (nunca el video/pantalla de victoria de Noche 5 en si, que
	// es especifico de haber jugado esa noche) -- reutiliza
	// com.fdaf.util.TransicionEscape (extraida de ControllerInterfaz en
	// esta misma sesion) para que ambos caminos compartan exactamente la
	// misma secuencia (glitch + LesToreadorsRemix + pantalla de carga
	// corrompida + risa de Freddy), sin duplicar logica. vidasFinales=2
	// (las vidas iniciales reales de la Noche 5, Noche.NOCHE_5.getVidas())
	// -- hoy en dia HandoffData.vidasFinales no se lee del lado Escape mas
	// alla de guardarse (verificado, ningun HUD lo usa todavia), asi que
	// cualquier valor razonable es equivalente; se usa este por
	// consistencia narrativa con el contexto real de Noche 5.
	private void lanzarEscapeDesdeCustomNight() {
		detenerAudioMenu();
		new com.fdaf.util.TransicionEscape().iniciarDesdeMenu(vp,
				com.fdaf.mvc.models.juego.Noche.NOCHE_5.getVidas(), () -> {
					// BUG REAL corregido en esta sesion (mismo hallazgo que
					// ControllerInterfaz.iniciarTransicionAEscape): al volver de Escape este
					// proceso Swing seguia siendo el mismo de toda la sesion en vez de
					// beneficiarse del reinicio completo que ya reciben ganar/perder/rendirse --
					// se llama al mismo mecanismo real (ReiniciadorJuego), con el mismo
					// respaldo si el reinicio no pudiera completarse.
					vp.toFront();
					vp.requestFocus();
					com.fdaf.util.ReiniciadorJuego.reiniciar(vp, this::volverAlMenuSinReiniciar);
				});
	}

	// Se llama al volver de una partida (ganada o perdida) -- indica que
	// ya no hay ninguna partida activa, para que CTRL+G/CTRL+P dejen de
	// funcionar hasta que se empiece una nueva.
	public void limpiarPartidaActiva() {
		partidaActiva = null;
	}

	private void aplicarHoverYSonidoATodos() {
		InteraccionBoton.aplicarHoverYSonido(menu.getBtnContinuar(), menu.getLbl(), 30, menu.getLblNocheActual());
		InteraccionBoton.aplicarHoverYSonido(menu.getBtnNuevaPartida(), menu.getLbl(), 30);
		InteraccionBoton.aplicarHoverYSonido(menu.getBtnOpciones(), menu.getLbl(), 30);
		InteraccionBoton.aplicarHoverYSonido(menu.getBtnComoJugar());
		InteraccionBoton.aplicarHoverYSonido(menu.getBtnAcercaDe());

		InteraccionBoton.aplicarHoverYSonido(menu.getPnlConfirmacion().getBtnConfirmar(), menu.getPnlConfirmacion().getLblFlecha(), 0);
		InteraccionBoton.aplicarHoverYSonido(menu.getPnlConfirmacion().getBtnCancelar(), menu.getPnlConfirmacion().getLblFlecha(), 0);

		InteraccionBoton.aplicarHoverYSonido(opciones.getBtnIdioma(), opciones.getLbl(), 0);
		InteraccionBoton.aplicarHoverYSonido(opciones.getBtnCustomNight(), opciones.getLbl(), 0);
		InteraccionBoton.aplicarHoverYSonido(opciones.getBtnVolumen(), opciones.getLbl(), 0);
		InteraccionBoton.aplicarHoverYSonido(opciones.getBtnAtras(), opciones.getLbl(), 0);
		InteraccionBoton.aplicarHoverYSonido(opciones.getBtnSalir(), opciones.getLbl(), 0);

		InteraccionBoton.aplicarHoverYSonido(pantallaIdioma.getBtnEspanol(), pantallaIdioma.getLbl(), 0);
		InteraccionBoton.aplicarHoverYSonido(pantallaIdioma.getBtnIngles(), pantallaIdioma.getLbl(), 0);
		InteraccionBoton.aplicarHoverYSonido(pantallaIdioma.getBtnAtras(), pantallaIdioma.getLbl(), 0);

		InteraccionBoton.aplicarHoverYSonido(pantallaVolumen.getBtnSubir(), pantallaVolumen.getLbl(), 0);
		InteraccionBoton.aplicarHoverYSonido(pantallaVolumen.getBtnBajar(), pantallaVolumen.getLbl(), 0);
		InteraccionBoton.aplicarHoverYSonido(pantallaVolumen.getBtnAtras(), pantallaVolumen.getLbl(), 0);

		// Una noche bloqueada (§1.66) no suena ni muestra la flecha: no se puede elegir.
		Noche[] nochesHover = Noche.values();
		for (int i = 0; i < pantallaNochePersonalizada.getBtnsNoche().length; i++) {
			Noche noche = nochesHover[i];
			InteraccionBoton.aplicarHoverYSonido(pantallaNochePersonalizada.getBtnsNoche()[i], pantallaNochePersonalizada.getLbl(), 0,
					null, () -> PreferenciasJuego.nocheSuperada(noche));
		}
		InteraccionBoton.aplicarHoverYSonido(pantallaNochePersonalizada.getBtnAtras(), pantallaNochePersonalizada.getLbl(), 0);
		InteraccionBoton.aplicarHoverYSonido(pantallaNochePersonalizada.getBtnEscape(), pantallaNochePersonalizada.getLbl(), 0);
	}	
	// Único punto de todo el proyecto donde se decide qué texto mostrar
	// según el idioma -- nada de "if (idioma == INGLES)" repartido en
	// otros métodos. Lee la preferencia actual y fija TODOS los textos
	// relevantes de una sola vez, sin tocar tamaños ni posiciones.
	
	public void actualizarTextosIdioma() {

		boolean ingles = (PreferenciasJuego.idiomaSeleccionado == Idioma.INGLES);

		menu.getBtnContinuar().setText(ingles ? "Continue" : "Continuar");
		menu.getBtnNuevaPartida().setText(ingles ? "New Game" : "Nueva Partida");
		menu.getBtnOpciones().setText(ingles ? "Options" : "Opciones");
		menu.getBtnComoJugar().setText(TextosInfo.menuComoJugar());
		menu.getBtnAcercaDe().setText(TextosInfo.menuAcercaDe());
		// Los anchos de la fila secundaria dependen del idioma (§1.36).
		menu.ubicarNavegacionSecundaria();
		opciones.getLblTitulo().setText(ingles ? "Options" : "Opciones");
		opciones.getBtnIdioma().setText(ingles ? "Language" : "Idioma");
		opciones.getBtnCustomNight().setText(ingles ? "Custom Night" : "Noche personalizada");
		pantallaNochePersonalizada.getLblTitulo().setText(ingles ? "Custom Night" : "Noche personalizada");
		opciones.getBtnVolumen().setText(ingles ? "Volume" : "Volumen");
		opciones.getBtnAtras().setText(ingles ? "Back" : "Atrás");
		opciones.getBtnSalir().setText(ingles ? "Exit Game" : "Salir del Juego");

		pantallaIdioma.getLblTitulo().setText(ingles ? "Language" : "Idioma");
		pantallaIdioma.getLblIdiomaActual().setText(ingles ? "English" : "Español");
		pantallaIdioma.getBtnEspanol().setText(!ingles ? "> Español" : "Español");
		pantallaIdioma.getBtnIngles().setText(ingles ? "> English" : "English");
		pantallaIdioma.getBtnAtras().setText(ingles ? "Back" : "Atrás");

		pantallaVolumen.getLblTitulo().setText(ingles ? "Volume" : "Volumen");
		pantallaVolumen.getLblNivel().setText((ingles ? "Level: " : "Nivel: ") + PreferenciasJuego.volumenGeneral + "/10");
		pantallaVolumen.getBtnSubir().setText(ingles ? "Increase Volume" : "Subir Volumen");
		pantallaVolumen.getBtnBajar().setText(ingles ? "Decrease Volume" : "Bajar Volumen");
		pantallaVolumen.getBtnAtras().setText(ingles ? "Back" : "Atrás");

		int numeroNoche = PreferenciasJuego.nocheActual.ordinal() + 1;
		menu.getLblNocheActual().setText((ingles ? "Night " : "Noche ") + numeroNoche);

		pantallaNochePersonalizada.getLblNocheSeleccionada().setText(
				(ingles ? "Selected Night: " : "Noche seleccionada: ") + numeroNoche);
		pantallaNochePersonalizada.getBtnAtras().setText(ingles ? "Back" : "Atrás");

		// Noches personalizadas (§1.66): solo las superadas se pueden elegir.
		// Las bloqueadas se ven en gris con "(Bloqueada)/(Locked)", como ESCAPE.
		int ordinalNocheActual = PreferenciasJuego.nocheActual.ordinal();
		Noche[] nochesTexto = Noche.values();
		for (int i = 0; i < pantallaNochePersonalizada.getBtnsNoche().length; i++) {
			boolean superada = PreferenciasJuego.nocheSuperada(nochesTexto[i]);
			String etiqueta = superada ? (ingles ? "Night " : "Noche ") + (i + 1) : TextosInfo.nocheBloqueada(i + 1);
			boolean esLaSeleccionada = (i == ordinalNocheActual);
			JButton btn = pantallaNochePersonalizada.getBtnsNoche()[i];
			btn.setText(esLaSeleccionada ? "> " + etiqueta : etiqueta);
			btn.setForeground(superada ? java.awt.Color.WHITE : new java.awt.Color(140, 140, 140));
		}

		menu.ubicarEstrellaEscape(PreferenciasJuego.escapeCompletado, TextosInfo.estrellaEscapeTooltip());

		// ESCAPE -- siempre visible (pedido explicito del usuario, genera
		// expectativa aunque este bloqueado). Bloqueado: texto atenuado
		// (gris) con indicador "(Bloqueado)/(Locked)". Desbloqueado: texto
		// normal en blanco, igual estilo que el resto de los botones.
		java.awt.Color colorEscape = PreferenciasJuego.escapeDesbloqueado
				? java.awt.Color.WHITE : new java.awt.Color(140, 140, 140);
		pantallaNochePersonalizada.getBtnEscape().setForeground(colorEscape);
		pantallaNochePersonalizada.getBtnEscape().setText(
				PreferenciasJuego.escapeDesbloqueado
						? "ESCAPE"
						: (ingles ? "ESCAPE (Locked)" : "ESCAPE (Bloqueado)"));
	}
	
	public void botones() {
		menu.getBtnOpciones().addActionListener(e -> {
			vp.setContenido(opciones);
			opciones();
		});
		menu.getBtnComoJugar().addActionListener(e -> abrirComoJugar());
		menu.getBtnAcercaDe().addActionListener(e -> abrirAcercaDe());

		pantallaIdioma.getBtnEspanol().addActionListener(e -> cambiarIdioma(Idioma.ESPANOL));
		pantallaIdioma.getBtnIngles().addActionListener(e -> cambiarIdioma(Idioma.INGLES));

		opciones.getBtnIdioma().addActionListener(e -> vp.setContenido(pantallaIdioma));
		pantallaIdioma.getBtnAtras().addActionListener(e -> vp.setContenido(opciones));

		opciones.getBtnVolumen().addActionListener(e -> vp.setContenido(pantallaVolumen));
		pantallaVolumen.getBtnAtras().addActionListener(e -> vp.setContenido(opciones));

		pantallaVolumen.getBtnSubir().addActionListener(e -> {
			int nuevoNivel = Math.min(10, PreferenciasJuego.volumenGeneral + 1);
			com.fdaf.util.ConfiguracionAudio.setNivelVolumen(nuevoNivel);
			actualizarTextosIdioma();
		});

		pantallaVolumen.getBtnBajar().addActionListener(e -> {
			int nuevoNivel = Math.max(1, PreferenciasJuego.volumenGeneral - 1);
			com.fdaf.util.ConfiguracionAudio.setNivelVolumen(nuevoNivel);
			actualizarTextosIdioma();
		});

		opciones.getBtnCustomNight().addActionListener(e -> vp.setContenido(pantallaNochePersonalizada));
		pantallaNochePersonalizada.getBtnAtras().addActionListener(e -> vp.setContenido(opciones));

		pantallaNochePersonalizada.getBtnEscape().addActionListener(e -> {
			if (!PreferenciasJuego.escapeDesbloqueado) {
				new Sonido("sfx/button_error.wav").play();
				mostrarMensajeEscapeBloqueado();
				return;
			}
			lanzarEscapeDesdeCustomNight();
		});

		Noche[] noches = Noche.values();
		for (int i = 0; i < pantallaNochePersonalizada.getBtnsNoche().length; i++) {
			Noche noche = noches[i];
			pantallaNochePersonalizada.getBtnsNoche()[i].addActionListener(e -> {
				if (!PreferenciasJuego.nocheSuperada(noche)) {
					new Sonido("sfx/button_error.wav").play();
					mostrarMensajeBloqueado(TextosInfo.mensajeNocheBloqueada(noche.ordinal() + 1));
					return;
				}
				PreferenciasJuego.nocheActual = noche;
				com.fdaf.util.PersistenciaJuego.guardar();
				actualizarTextosIdioma();
			});
		}
		
		menu.getBtnContinuar().addActionListener(e -> {
			detenerAudioMenuYComenzarPartida();
		});

		menu.getBtnNuevaPartida().addActionListener(e -> {

			boolean ingles = (PreferenciasJuego.idiomaSeleccionado == Idioma.INGLES);
			String mensaje = ingles ? "Reset all progress?" : "¿Reiniciar todo el progreso?";
			String textoConfirmar = ingles ? "Yes, reset" : "Sí, reiniciar";
			String textoCancelar = ingles ? "Cancel" : "Cancelar";

			bloquearBotonesMenuPrincipal(true);

			menu.getPnlConfirmacion().mostrar(mensaje, textoConfirmar, textoCancelar, () -> {
				PreferenciasJuego.nocheActual = Noche.NOCHE_1;
				// "Reiniciar TODO el progreso" incluye el desbloqueo de ESCAPE -- consistente
				// con lo que el propio boton promete, no una excepcion oculta.
				PreferenciasJuego.escapeDesbloqueado = false;
				// ...y las Noches personalizadas desbloqueadas (§1.66). La estrella
				// de Escape completado NO: es un logro, no progreso de partida.
				java.util.Arrays.fill(PreferenciasJuego.nochesSuperadas, false);
				com.fdaf.util.PersistenciaJuego.guardar();
				actualizarTextosIdioma();
				bloquearBotonesMenuPrincipal(false);

				// No se queda en el menú: arranca directo la intro de
				// Noche 1 (ya es la noche activa) y de ahí la partida.
				detenerAudioMenuYComenzarPartida();
			});
		});

		menu.getPnlConfirmacion().getBtnCancelar().addActionListener(e -> bloquearBotonesMenuPrincipal(false));
	}

	private void bloquearBotonesMenuPrincipal(boolean bloquear) {
		menu.getBtnContinuar().setEnabled(!bloquear);
		menu.getBtnNuevaPartida().setEnabled(!bloquear);
		menu.getBtnOpciones().setEnabled(!bloquear);
	
	}
	// Único punto que apaga el audio del menú y arranca la secuencia de
	// intro -- lo usan tanto "Continuar" como "Nueva Partida" (que ahora
	// también debe llevar directo a jugar, no dejar al jugador parado en
	// el menú).
	private void detenerAudioMenuYComenzarPartida() {
		if (musicaMenu != null) {
			musicaMenu.stop();
		}
		if (sonidoEstaticaMenu != null) {
			sonidoEstaticaMenu.stop();
		}
		if (timerTopeEstaticaMenu != null && timerTopeEstaticaMenu.isRunning()) {
			timerTopeEstaticaMenu.stop();
		}

		iniciarSecuenciaIntro();
	}

	private void iniciarSecuenciaIntro() {

		String rutaIntro = CargarImagenes.rutaIntroPorNoche(PreferenciasJuego.nocheActual.ordinal());
		
		ImageIcon introFresca = com.fdaf.util.CargarGifs.cargarFresco(rutaIntro, CargarImagenes.getIntroduccionRespaldo());
		rutaIntroActiva = rutaIntro;

		// Cada gif ya trae su tarjeta en el idioma correcto (CLAUDE.md §1.47).
		// El panel es el ImageObserver del gif: AWT le avisa (FRAMEBITS) cada
		// vez que muestra un cuadro nuevo, y ahí se mira si ese cuadro es el de
		// las franjas blancas (§1.64).
		sonidoFranjasPendiente.set(true);
		JPanel panelIntro = new PnlAplicarImagen(introFresca) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean imageUpdate(java.awt.Image img, int flags, int x, int y, int w, int h) {
				if ((flags & java.awt.image.ImageObserver.FRAMEBITS) != 0 && sonidoFranjasPendiente.get()
						&& esCuadroDeFranjasBlancas(img)) {
					reproducirSonidoFranjas();
				}
				return super.imageUpdate(img, flags, x, y, w, h);
			}
		};
		panelIntro.setBounds(0, 0,
				EscalarVista.getEscalaX(1600),
				EscalarVista.getEscalaY(900));

		vp.setContenido(panelIntro);

		// Duración REAL del gif (suma de sus retardos, cacheada), no un valor
		// fijo: Noche 1 dura 20.4 s (ES) / 20.45 s (EN) y las noches 2-5,
		// 8.65 s. El antiguo Timer(20000) cortaba la Noche 1 0.4 s antes y
		// dejaba las noches 2-5 esperando 11 s con el gif ya terminado.
		int duracionMs = com.fdaf.util.DuracionGif.leerMilisegundos(rutaIntro, 20000);
		timerIntroActivo = new Timer(duracionMs, ev -> {
			((Timer) ev.getSource()).stop();
			finalizarIntroYComenzarPartida();
		});
		timerIntroActivo.setRepeats(false);
		timerIntroActivo.start();
	}

	// Las franjas blancas que preceden a la tarjeta "12:00 AM" (cuadro 40 de
	// la Noche 1, tras el periódico; cuadro 2 de las noches 2-5; en los dos
	// idiomas) suenan como la apertura de la tableta: el mismo sonido y el
	// mismo reproductor (InteraccionBoton.reproducirSonidoHover, que aplica
	// el volumen y reproduce fuera del hilo de Swing).
	//
	// Se sincroniza con el cuadro que AWT muestra de verdad, no con un reloj:
	// AWT reproduce la intro de la Noche 1 (12-14 MB) ~1 s más lenta que la
	// suma de sus retardos porque la decodifica sobre la marcha, y contar
	// avisos FRAMEBITS tampoco sirve (la correspondencia aviso-cuadro cambia
	// en uno según cómo se pinte el panel; medido). En cada aviso, mientras
	// el sonido está pendiente, el cuadro actual se dibuja en 160x90 y se
	// cuentan las filas casi blancas en más de la mitad del ancho: con al
	// menos 6 (1/15 del alto) es la ráfaga. El periódico, la estática, la
	// tarjeta y el reloj no llegan. Lo llama el hilo de animación de AWT.
	private static final int ANCHO_MUESTRA_FRANJAS = 160;
	private static final int ALTO_MUESTRA_FRANJAS = 90;
	private java.awt.image.BufferedImage muestraFranjas;

	private boolean esCuadroDeFranjasBlancas(java.awt.Image cuadro) {
		if (muestraFranjas == null) {
			muestraFranjas = new java.awt.image.BufferedImage(ANCHO_MUESTRA_FRANJAS, ALTO_MUESTRA_FRANJAS,
					java.awt.image.BufferedImage.TYPE_INT_RGB);
		}
		java.awt.Graphics2D g = muestraFranjas.createGraphics();
		g.setColor(java.awt.Color.BLACK);
		g.fillRect(0, 0, ANCHO_MUESTRA_FRANJAS, ALTO_MUESTRA_FRANJAS);
		g.drawImage(cuadro, 0, 0, ANCHO_MUESTRA_FRANJAS, ALTO_MUESTRA_FRANJAS, null);
		g.dispose();
		int filas = 0;
		for (int y = 0; y < ALTO_MUESTRA_FRANJAS; y++) {
			int blancos = 0;
			for (int x = 0; x < ANCHO_MUESTRA_FRANJAS; x++) {
				int c = muestraFranjas.getRGB(x, y);
				if (((c >> 16) & 0xFF) > 200 && ((c >> 8) & 0xFF) > 200 && (c & 0xFF) > 200) {
					blancos++;
				}
			}
			if (blancos * 2 > ANCHO_MUESTRA_FRANJAS) {
				filas++;
			}
		}
		return filas >= ALTO_MUESTRA_FRANJAS / 15;
	}

	private void reproducirSonidoFranjas() {
		if (sonidoFranjasPendiente.compareAndSet(true, false)) {
			com.fdaf.util.InteraccionBoton.reproducirSonidoHover();
		}
	}

	private void detenerSonidoLineasBlancas() {
		sonidoFranjasPendiente.set(false);
	}

	// Único punto real de "la intro terminó, empieza el juego" -- lo
	// dispara tanto el timer natural (duración real del gif) como el atajo CTRL+S. Nunca
	// hay dos rutas distintas para esta transición.
	private void finalizarIntroYComenzarPartida() {

		if (timerIntroActivo != null && timerIntroActivo.isRunning()) {
			timerIntroActivo.stop();
		}
		timerIntroActivo = null;
		detenerSonidoLineasBlancas();

		// El gif de la intro ya no se ve: se libera para que su hilo de
		// animación de AWT no siga decodificando detrás de la partida.
		if (rutaIntroActiva != null) {
			com.fdaf.util.CargarGifs.liberar(rutaIntroActiva);
			rutaIntroActiva = null;
		}

		ControllerInterfaz interfaz = new ControllerInterfaz();
		partidaActiva = interfaz;
		interfaz.init(vp, menu, this);
	}

	// Se llama en cada visita a Opciones, pero la instancia "opciones" es
	// persistente: registrar los listeners en cada visita los acumulaba
	// (fuga documentada en CLAUDE.md §1.8, corregida en §1.27).
	private boolean listenersOpcionesRegistrados = false;

	public void opciones() {
		if (listenersOpcionesRegistrados) {
			return;
		}
		listenersOpcionesRegistrados = true;
		opciones.getBtnAtras().addActionListener(e -> vp.setContenido(menu));
		opciones.getBtnSalir().addActionListener(e -> System.exit(0));
	}

	/*
	 * ==================== ATAJOS DE DEPURACIÓN (DEBUG SHORTCUTS) ====================
	 * Bloque agrupado a propósito: para eliminar esta funcionalidad en una
	 * versión final, basta con borrar este bloque completo y la llamada a
	 * registrarAtajosDebug() dentro de init(). No forma parte de la
	 * jugabilidad normal. Cada atajo reutiliza los métodos reales del
	 * proyecto (alGanar/alPerder/fin de intro) -- ninguna ruta simulada.
	 */
	// Único punto de registro de ESC para todo el proyecto -- ningún
		// panel individual tiene su propio KeyListener. Separado a propósito
		// de registrarAtajosDebug(): esto es navegación real del juego, no
		// una herramienta de desarrollo.
		private void registrarAtajoEscape() {

			JRootPane raiz = vp.getRootPane();
			InputMap mapaEntrada = raiz.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
			ActionMap mapaAccion = raiz.getActionMap();

			mapaEntrada.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "menuEscape");
			mapaAccion.put("menuEscape", new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					manejarEscape();
				}
			});
		}

		// Decide a dónde navega ESC según la pantalla realmente visible en
		// este instante -- comparando por referencia contra los paneles que
		// ControllerMenu ya mantiene, sin ningún estado nuevo de "pantalla
		// actual". Reutiliza hayPartidaJugable() para nunca interferir con
		// una partida en curso.
		private void manejarEscape() {

			if (hayPartidaJugable()) {
				return;
			}

			Component actual = (vp.pnlContenido.getComponentCount() > 0)
					? vp.pnlContenido.getComponent(0)
					: null;

			if (actual == pantallaIdioma || actual == pantallaVolumen || actual == pantallaNochePersonalizada) {
				vp.setContenido(opciones);
			} else if (actual == opciones) {
				vp.setContenido(menu);
			} else if (actual instanceof com.fdaf.mvc.views.frames.pnl.PnlAgradecimiento) {
				((com.fdaf.mvc.views.frames.pnl.PnlAgradecimiento) actual).continuarDesdeTeclado();
			} else if (actual == pantallaInfo && !introduccionActiva) {
				// "Cómo jugar" y "Acerca del proyecto" vuelven al menú; la introducción no reacciona a ESC.
				vp.setContenido(menu);
			}
			// menu, warning, intro, gameover, win: ESC no hace nada.
		}
	private void registrarAtajosDebug() {

		JRootPane raiz = vp.getRootPane();
		InputMap mapaEntrada = raiz.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap mapaAccion = raiz.getActionMap();

		// CTRL+G: forzar victoria. Solo si hay una partida jugable.
		mapaEntrada.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK), "debugForzarVictoria");
		mapaAccion.put("debugForzarVictoria", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (hayPartidaJugable()) {
					partidaActiva.alGanar();
				}
			}
		});

		// CTRL+P: forzar derrota. Solo si hay una partida jugable.
		mapaEntrada.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK), "debugForzarDerrota");
		mapaAccion.put("debugForzarDerrota", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (hayPartidaJugable()) {
					partidaActiva.alPerder();
				}
			}
		});
		
		// CTRL+S: si hay una intro activa, la salta. Si no, pero hay una
		// fase larga de Game Over activa, la salta. Si ninguna aplica,
		// no hace nada -- mismo criterio en ambos casos: "si existe una
		// fase larga activa, adelantarla".
		mapaEntrada.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "debugSaltarIntro");
		mapaAccion.put("debugSaltarIntro", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (advertenciaActiva) {
					saltarAdvertencia();
					return;
				}
				if (introduccionActiva) {
					cerrarIntroduccion();
					return;
				}
				if (timerIntroActivo != null && timerIntroActivo.isRunning()) {
					finalizarIntroYComenzarPartida();
					return;
				}
				if (partidaActiva != null && partidaActiva.isFaseLargaGameOverActiva()) {
					partidaActiva.saltarFaseLargaGameOver();
				}
			}
		});
	}

	// "Partida jugable" = existe una partida Y todavía no concluyó (ni
	// por victoria ni por derrota). Esta única condición cubre a la vez
	// menú, opciones, intro, victoria y derrota -- en todos esos casos,
	// o partidaActiva es null, o juegoTerminado ya es true.
	private boolean hayPartidaJugable() {
		return partidaActiva != null
				&& partidaActiva.getControllerJuego() != null
				&& !partidaActiva.getControllerJuego().isJuegoTerminado();
	}

}