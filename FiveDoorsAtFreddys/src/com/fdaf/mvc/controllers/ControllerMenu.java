package com.fdaf.mvc.controllers;

import java.awt.BorderLayout;
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
import javax.swing.SwingConstants;
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



	// Referencia a la partida en curso (null si no hay ninguna) y al
	// temporizador de la intro activa (null si no hay intro corriendo).
	// Los usan tanto el flujo normal como los atajos de depuración.
	private ControllerInterfaz partidaActiva;
	private Timer timerIntroActivo;

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
	}
	
	private void mostrarAdvertenciaInicial() {

		advertenciaActiva = true;

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
		mostrarMenuPrincipal();
	}

	// Único punto de entrada externo. Si la advertencia ya no está
	// activa, no hace nada.
	public void saltarAdvertencia() {
		if (!advertenciaActiva) {
			return;
		}
		if (timerAdvertenciaActual != null && timerAdvertenciaActual.isRunning()) {
			timerAdvertenciaActual.stop();
		}
		finalizarAdvertenciaYMostrarMenu();
	}

	// Se muestra el menú por primera vez y, exactamente aquí, arranca su
	// música -- nunca antes de que el jugador vea el menú realmente.
		private void mostrarMenuPrincipal() {
			vp.setContenido(menu);
			ponerMusicaMenu();
			iniciarEstaticaMenu();
		}

		// Efecto ambiental TEMPORAL, no estática permanente. Decide en
		// tiempo de ejecución (no por suposición) si el archivo ya dura lo
		// suficiente por sí solo o si necesita loop -- y si necesita loop,
		// lo corta exactamente a los 32 segundos. Misma instancia siempre;
		// se reutiliza tanto al mostrar el menú por primera vez como al
		// regresar de una partida, aplicando la misma regla cada vez.
		private void iniciarEstaticaMenu() {

			if (sonidoEstaticaMenu == null) {
				sonidoEstaticaMenu = new Sonido("fondo/static_menu.wav");
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
	    musicaMenu = new Sonido("fondo/musica_fondo_menu.wav");
	    musicaMenu.loop();
	    musicaMenu.subirVolumen(3.5f);
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
		pantallaNochePersonalizada.getLblEscapeBloqueado().setText(
				"<html>" + (ingles
						? "Unlocks after completing the last night."
						: "Se desbloquea al completar la ultima noche.") + "</html>");
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

	// Acceso directo a Five Doors Escape desde Custom Night (pedido
	// explicito del usuario 2026-08-09), una vez desbloqueado -- reutiliza
	// LanzadorEscape.lanzar() tal cual, el mismo mecanismo real que ya usa
	// la victoria de la Noche 5 (Architecture.md #7), sin duplicar el
	// glitch/pantalla de PnlWin (ese efecto es especifico de la secuencia
	// de victoria, no tiene sentido fuera de ella). vidasFinales=2 (las
	// vidas iniciales reales de la Noche 5, Noche.NOCHE_5.getVidas()) --
	// hoy en dia HandoffData.vidasFinales no se lee del lado Escape mas
	// alla de guardarse (verificado, ningun HUD lo usa todavia), asi que
	// cualquier valor razonable es equivalente; se usa este por
	// consistencia narrativa con el contexto real de Noche 5.
	private void lanzarEscapeDesdeCustomNight() {
		detenerAudioMenu();

		JPanel pantallaCarga = new JPanel(new BorderLayout());
		pantallaCarga.setBackground(Color.BLACK);
		boolean ingles = (PreferenciasJuego.idiomaSeleccionado == Idioma.INGLES);
		JLabel lblCarga = new JLabel(ingles ? "Loading..." : "Cargando...", SwingConstants.CENTER);
		lblCarga.setForeground(Color.WHITE);
		lblCarga.setFont(com.fdaf.util.Fuentes.obtener(40));
		pantallaCarga.add(lblCarga, BorderLayout.CENTER);
		vp.setContenido(pantallaCarga);

		com.fdaf.util.LanzadorEscape.lanzar(PreferenciasJuego.idiomaSeleccionado,
				com.fdaf.mvc.models.juego.Noche.NOCHE_5.getVidas(), () -> {
					vp.setContenido(menu);
					reanudarMusicaMenu();
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

		for (JButton btnNoche : pantallaNochePersonalizada.getBtnsNoche()) {
			InteraccionBoton.aplicarHoverYSonido(btnNoche, pantallaNochePersonalizada.getLbl(), 0);
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
		opciones.getLblTitulo().setText(ingles ? "Options" : "Opciones");
		opciones.getBtnIdioma().setText(ingles ? "Language" : "Idioma");
		opciones.getBtnCustomNight().setText("Custom Night");
		opciones.getBtnVolumen().setText(ingles ? "Volume" : "Volumen");
		opciones.getBtnAtras().setText(ingles ? "Back" : "Atras");
		opciones.getBtnSalir().setText(ingles ? "Exit Game" : "Salir del Juego");

		pantallaIdioma.getLblTitulo().setText(ingles ? "Language" : "Idioma");
		pantallaIdioma.getLblIdiomaActual().setText(ingles ? "English" : "Español");
		pantallaIdioma.getBtnEspanol().setText(!ingles ? "> Español" : "Español");
		pantallaIdioma.getBtnIngles().setText(ingles ? "> English" : "English");
		pantallaIdioma.getBtnAtras().setText(ingles ? "Back" : "Atras");

		pantallaVolumen.getLblTitulo().setText(ingles ? "Volume" : "Volumen");
		pantallaVolumen.getLblNivel().setText((ingles ? "Level: " : "Nivel: ") + PreferenciasJuego.volumenGeneral + "/10");
		pantallaVolumen.getBtnSubir().setText(ingles ? "Increase Volume" : "Subir Volumen");
		pantallaVolumen.getBtnBajar().setText(ingles ? "Decrease Volume" : "Bajar Volumen");
		pantallaVolumen.getBtnAtras().setText(ingles ? "Back" : "Atras");

		int numeroNoche = PreferenciasJuego.nocheActual.ordinal() + 1;
		menu.getLblNocheActual().setText((ingles ? "Night " : "Noche ") + numeroNoche);

		pantallaNochePersonalizada.getLblNocheSeleccionada().setText(
				(ingles ? "Selected Night: " : "Noche seleccionada: ") + numeroNoche);
		pantallaNochePersonalizada.getBtnAtras().setText(ingles ? "Back" : "Atras");

		int ordinalNocheActual = PreferenciasJuego.nocheActual.ordinal();
		for (int i = 0; i < pantallaNochePersonalizada.getBtnsNoche().length; i++) {
			String etiqueta = (ingles ? "Night " : "Noche ") + (i + 1);
			boolean esLaSeleccionada = (i == ordinalNocheActual);
			pantallaNochePersonalizada.getBtnsNoche()[i].setText(esLaSeleccionada ? "> " + etiqueta : etiqueta);
		}

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

		pantallaIdioma.getBtnEspanol().addActionListener(e -> {
			PreferenciasJuego.idiomaSeleccionado = Idioma.ESPANOL;
			actualizarTextosIdioma();
			com.fdaf.util.PersistenciaJuego.guardar();
		});

		pantallaIdioma.getBtnIngles().addActionListener(e -> {
			PreferenciasJuego.idiomaSeleccionado = Idioma.INGLES;
			actualizarTextosIdioma();
			com.fdaf.util.PersistenciaJuego.guardar();
		});

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
				new Sonido("botones/error_al_presionar_boton.wav").play();
				mostrarMensajeEscapeBloqueado();
				return;
			}
			lanzarEscapeDesdeCustomNight();
		});

		Noche[] noches = Noche.values();
		for (int i = 0; i < pantallaNochePersonalizada.getBtnsNoche().length; i++) {
			Noche noche = noches[i];
			pantallaNochePersonalizada.getBtnsNoche()[i].addActionListener(e -> {
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
		
		JPanel panelIntro = new PnlAplicarImagen(introFresca);
		panelIntro.setBounds(0, 0,
				EscalarVista.getEscalaX(1600),
				EscalarVista.getEscalaY(900));

		vp.setContenido(panelIntro);

		timerIntroActivo = new Timer(20000, ev -> {
			((Timer) ev.getSource()).stop();
			finalizarIntroYComenzarPartida();
		});
		timerIntroActivo.setRepeats(false);
		timerIntroActivo.start();
	}

	// Único punto real de "la intro terminó, empieza el juego" -- lo
	// dispara tanto el timer natural de 20s como el atajo CTRL+S. Nunca
	// hay dos rutas distintas para esta transición.
	private void finalizarIntroYComenzarPartida() {

		if (timerIntroActivo != null && timerIntroActivo.isRunning()) {
			timerIntroActivo.stop();
		}
		timerIntroActivo = null;

		ControllerInterfaz interfaz = new ControllerInterfaz();
		partidaActiva = interfaz;
		interfaz.init(vp, menu, this);
	}

	public void opciones() {
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