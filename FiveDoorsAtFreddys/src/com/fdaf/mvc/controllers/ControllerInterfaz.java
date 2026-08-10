package com.fdaf.mvc.controllers;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import com.fdaf.mvc.models.animatronicos.Animatronico;
import com.fdaf.mvc.models.coleccionables.Coleccionable;
import com.fdaf.mvc.models.coleccionables.TipoColeccionable;
import com.fdaf.mvc.views.frames.VistaPrincipal;
import com.fdaf.mvc.views.frames.pnl.PnlAplicarImagen;
import com.fdaf.mvc.views.frames.pnl.PnlGameOver;
import com.fdaf.mvc.views.frames.pnl.PnlJuego;
import com.fdaf.mvc.views.frames.pnl.PnlMenu;
import com.fdaf.mvc.views.frames.pnl.PnlTableta;
import com.fdaf.mvc.views.frames.pnl.PnlWin;
import com.fdaf.mvc.views.multimedia.Sonido;
import com.fdaf.util.CargarGifs;
import com.fdaf.util.CargarImagenes;
import com.fdaf.util.EscalarVista;
import com.fdaf.util.Fuentes;
import com.fdaf.util.Idioma;
import com.fdaf.util.PreferenciasJuego;

public class ControllerInterfaz implements JuegoListener {

	private Sonido sonido;
	private Sonido sonidoJuego1;
	private Sonido sonidoJuego2;
	private VistaPrincipal vistaPrincipal;
	private Timer timerSonido;

	private PnlJuego pnlJuego;
	private PnlTableta pnlTableta;

	private ControllerJuego controllerJuego;
	private ControllerCamara controllerCamara;

	private EscalarVista.GifEscalado gifOficinaNormal;
	private EscalarVista.GifEscalado gifOficinaLuzIzq;
	private EscalarVista.GifEscalado gifOficinaLuzDer;

	private Sonido sonidoLlamada;
	private Timer timerLlamada;
	// Timers que detienen los gifs de puerta al terminar su reproducción
		// real (están codificados en loop infinito -- confirmado: sin esto
		// se repetirían para siempre). Uno por lado, para poder cancelar el
		// anterior si el jugador togglea la puerta muy rápido
	private Timer timerGifPuertaIzq;
	private Timer timerGifPuertaDer;
	
	
	private boolean llamadaColgada = false;

	private PnlMenu menu;
	private Sonido sonidoGameOver;
	private Sonido sonidoWin;
	private ControllerMenu controllerMenuPadre;

	private JPanel pnlFadeNegro;
	private int alphaFade = 0;
	private boolean ultimoFueAnimatronico = false;

	// Repintado acotado, EXCLUSIVO de la ventana de 20s de Game Over --
	// no es una reintroducción de loopRepintado: se detiene siempre en el
	// único punto de salida de esta secuencia (t2, al cambiar de panel),
	// nunca queda corriendo sin dueño. Existe porque GifEscalado depende
	// de un observer nulo para saber cuándo repintar, y Game Over es el
	// único gif del proyecto que se muestra sin ningún timer de
	// respaldo detrás (loopRepintado ya se detuvo como primera línea de
	// alPerder(), antes de que este gif siquiera se asigne).
	private Timer timerRepintadoGameOver;

	// "Fase larga" = exactamente la ventana de t1+t2 (los 20s donde
	// gameover.gif sigue en pantalla). true desde el primer instante de
	// alPerder(), false desde el primer instante de
	// avanzarDespuesDelGifLargo() -- sin importar si esa transición
	// ocurrió por el timer natural o por CTRL+S. Ningún otro punto del
	// código la toca.
	private boolean faseLargaGameOverActiva = false;
	private Timer timerGameOverT1;
	private Timer timerGameOverT2;
	private Timer timerGameOverT3;
	private Timer timerGameOverT4;
	private PnlGameOver pantallaGameOver;

	public boolean isFaseLargaGameOverActiva() {
		return faseLargaGameOverActiva;
	}

	// sonidos precargados

	private Sonido respiracion = new Sonido("varios/respiracion_agitada.wav");
	private com.fdaf.util.SonidosAmbientalesAleatorios sonidosAmbientales = new com.fdaf.util.SonidosAmbientalesAleatorios();
	public ControllerInterfaz() {
		pnlJuego = new PnlJuego();
		pnlTableta = new PnlTableta();

		controllerJuego = new ControllerJuego();
		controllerJuego.setListener(this);
		controllerJuego.init();

		controllerCamara = new ControllerCamara(pnlJuego, pnlTableta, this);
	}

	public void init(VistaPrincipal vp, PnlMenu menu, ControllerMenu controllerMenuPadre) {
		this.vistaPrincipal = vp;
		this.menu = menu;
		this.controllerMenuPadre = controllerMenuPadre;

		controllerCamara.init(vp, menu);

		inicializarEstadoInventario();
		conectarClicsInventario();
		
		sonidoJuego1 = new Sonido("fondo/sonido_ambiente_oficina.wav");
		sonidoJuego1.loop();

		// sonidosAmbientales (los sonidos ALEATORIOS de ambiente -- golpes, susurros, risas
		// lejanas) ya NO arrancan aqui (pedido explicito del usuario 2026-08-05: la llamada debe
		// escucharse completamente limpia). Arrancan recien cuando la llamada termina, sea de
		// forma natural o porque el jugador la muteo -- ver iniciarLlamada()/colgarLlamada().
		// El ambiente continuo de fondo (sonidoJuego1/sonidoJuego2, debajo) SI sigue sonando
		// durante la llamada -- no es lo que el usuario pidio silenciar, son el hum/ambiente base
		// de la oficina, no "sonidos aleatorios" que puedan competir con la llamada.

		timerSonido = new Timer(3000, e -> {
			sonidoJuego2 = new Sonido("fondo/sonido_fondo_oficina.wav");
			sonidoJuego2.loop();
			sonidoJuego2.subirVolumen(-9.0f);
			});

		timerSonido.setRepeats(false);
		timerSonido.start();

		prepararInstanciasPreEscaladas();

		conectarPuertas();
		conectarLuces();
		narizFreddy();
		mutecall();

		alActualizarVidas(controllerJuego.getVidas());

		iniciarLlamada();
	}

	private void prepararInstanciasPreEscaladas() {
		int anchoOficina = pnlJuego.getLblImgOficina().getWidth();
		int altoOficina = pnlJuego.getLblImgOficina().getHeight();

		this.gifOficinaNormal = new EscalarVista.GifEscalado(CargarImagenes.fondoJuego, anchoOficina, altoOficina);
		this.gifOficinaLuzIzq = new EscalarVista.GifEscalado(CargarImagenes.juegoLuzIzq, anchoOficina, altoOficina);
		this.gifOficinaLuzDer = new EscalarVista.GifEscalado(CargarImagenes.juegoLuzDer, anchoOficina, altoOficina);
	}
	
	// Único punto de verdad de "¿puede iniciarse una acción de partida
	// ahora mismo?" -- no depende de setEnabled/setVisible (confirmado
	// que un JLabel con MouseListener crudo ignora setEnabled), consulta
	// directamente el estado real de ControllerJuego.
	private void siPartidaActiva(Runnable accion) {
		if (controllerJuego.isJuegoTerminado()) {
			return;
		}
		accion.run();
	}

	private void conectarPuertas() {
		pnlJuego.getBotonIzq().addActionListener(e -> siPartidaActiva(() -> controllerJuego.abrirIzquierda()));
		pnlJuego.getBotonDer().addActionListener(e -> siPartidaActiva(() -> controllerJuego.abrirDerecha()));
	}

	private void conectarLuces() {
		pnlJuego.getLuzIzq().addActionListener(e -> siPartidaActiva(() -> controllerJuego.revelarIzquierda()));
		pnlJuego.getLuzDer().addActionListener(e -> siPartidaActiva(() -> controllerJuego.revelarDerecha()));

		pnlJuego.getLblOverlay().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				siPartidaActiva(() -> controllerJuego.recogerColeccionable());
			}
		});
	}

	/*
	 * ==== Llamada telefónica ====
	 */

	// Unico punto de resolucion noche+idioma -> archivo (pedido explicito del usuario
	// 2026-08-05: "no quiero logica duplicada... quiero que el sistema quede escalable por
	// noche e idioma"). PreferenciasJuego.nocheActual.ordinal() ya es la fuente de verdad de
	// "noche actual" en todo el proyecto (ver alGanar()) -- una 6ta noche solo necesitaria
	// agregar el archivo "noche6_es.wav"/"noche6_en.wav" bajo resources/sounds/phoneguy/, sin
	// tocar ninguna linea de codigo aqui.
	private String rutaLlamadaNocheActual() {
		int numeroNoche = PreferenciasJuego.nocheActual.ordinal() + 1;
		boolean ingles = (PreferenciasJuego.idiomaSeleccionado == Idioma.INGLES);
		return "phoneguy/noche" + numeroNoche + (ingles ? "_en" : "_es") + ".wav";
	}

	private void iniciarLlamada() {

		llamadaColgada = false;
		controllerJuego.setLlamadaActiva(true);

		sonidoLlamada = new Sonido("phoneguy/contestar_telefono.wav");
		sonidoLlamada.play();

		// aoparece cuando empieza la llamada.
		pnlJuego.getLblMuteCall().setVisible(true);

		long duracionMs = sonidoLlamada.getDuracionMs();
		if (duracionMs <= 0)
			duracionMs = 2000;

		timerLlamada = new Timer((int) duracionMs, e -> {
			timerLlamada.stop();

			if (llamadaColgada)
				return;

			// Llamada real de la noche actual, en el idioma actual (antes: el mismo audio
			// generico las 5 noches, solo variaba por idioma -- ver rutaLlamadaNocheActual).
			String archivo = rutaLlamadaNocheActual();

			sonidoLlamada = new Sonido(archivo);
			sonidoLlamada.play();

			// Duracion real leida del propio archivo (Sonido.getDuracionMs() -> Clip.
			// getMicrosecondLength()) -- nunca hardcodeada, asi que el timer se adapta solo
			// sin importar cuanto dure cada llamada real de cada noche.
			long duracionLlamadaMs = sonidoLlamada.getDuracionMs();
			if (duracionLlamadaMs <= 0)
				duracionLlamadaMs = 3000;

			// 2. si termina sola (nadie colgó), se oculta el botón y recien ahi se reanudan
			// los sonidos ambientales aleatorios (pedido explicito del usuario 2026-08-05: la
			// llamada debe escucharse completamente limpia, sin ambiente superpuesto).
			timerLlamada = new Timer((int) duracionLlamadaMs, ev -> {
				timerLlamada.stop();
				if (!llamadaColgada) {
					pnlJuego.getLblMuteCall().setVisible(false);
					controllerJuego.setLlamadaActiva(false);
					sonidosAmbientales.iniciar();
				}
			});
			timerLlamada.setRepeats(false);
			timerLlamada.start();
		});
		timerLlamada.setRepeats(false);
		timerLlamada.start();
	}

	private void colgarLlamada() {
		// Colgado real (boton Mute Call): SI reanuda el ambiente, la llamada realmente termino
		// para el jugador. Ver la sobrecarga de abajo para el otro caso (fin de partida).
		colgarLlamada(true);
	}

	// reanudarAmbiente=false: usado unicamente desde detenerSonidosJuego() (fin de partida por
	// victoria/derrota) -- ese metodo ya detiene sonidosAmbientales explicitamente porque el
	// juego esta terminando, asi que reanudarlo aqui justo despues seria un bug real (sonidos
	// de ambiente arrancando en medio de la secuencia de Game Over/Win).
	private void colgarLlamada(boolean reanudarAmbiente) {

		llamadaColgada = true;

		if (timerLlamada != null && timerLlamada.isRunning()) {
			timerLlamada.stop();
		}

		if (sonidoLlamada != null) {
			sonidoLlamada.stop();
		}

		// 1. colgado manual, se oculta
		pnlJuego.getLblMuteCall().setVisible(false);
		controllerJuego.setLlamadaActiva(false);

		if (reanudarAmbiente) {
			sonidosAmbientales.iniciar();
		}
	}

	private void mutecall() {
		pnlJuego.getLblMuteCall().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				siPartidaActiva(() -> colgarLlamada());
			}
		});
	}

	/*
	 * ==== Implementación de JuegoListener ====
	 */
	
	// Duracion de respaldo (ms) si el gif no pudiera leerse por algun motivo --
		// nunca es el valor real usado en la practica (DuracionGif siempre logra leer
		// los 4 gifs reales), solo una red de seguridad. Igual a la duracion real medida
		// de los 4 gifs actuales (750ms, 15 frames) para que incluso ese caso raro se
		// vea razonable.
		private static final int MS_RESPALDO_GIF_PUERTA = 500;

		// Reproduce un gif de puerta UNA SOLA VEZ, respetando su duración REAL
		// (leida del propio archivo via DuracionGif -- pedido explicito del
		// usuario 2026-08-05: "no quiero que el timer este hardcodeado... si en
		// el futuro vuelvo a cambiar la velocidad del gif, el codigo debe
		// adaptarse sin modificar constantes"), y al terminar lo congela en la
		// imagen estática correspondiente -- corta el loop infinito del archivo
		// desde afuera.
		private void reproducirGifPuertaUnaVez(String lado, JLabel destino, String rutaGif, ImageIcon imagenFinal) {

			Timer anterior = "izq".equals(lado) ? timerGifPuertaIzq : timerGifPuertaDer;
			if (anterior != null && anterior.isRunning()) {
				anterior.stop();
			}

			ImageIcon gif = com.fdaf.util.CargarGifs.cargarFresco(rutaGif, imagenFinal);
			destino.setIcon(new EscalarVista.GifEscalado(gif, destino.getWidth(), destino.getHeight()));

			int duracionMs = com.fdaf.util.DuracionGif.leerMilisegundos(rutaGif, MS_RESPALDO_GIF_PUERTA);

			Timer detenerGif = new Timer(duracionMs, e -> {
				((Timer) e.getSource()).stop();
				destino.setIcon(EscalarVista.getImagenEscalada(imagenFinal, destino.getWidth(), destino.getHeight()));
				pnlJuego.revalidate();
				pnlJuego.repaint();
			});
			detenerGif.setRepeats(false);
			detenerGif.start();

			if ("izq".equals(lado)) {
				timerGifPuertaIzq = detenerGif;
			} else {
				timerGifPuertaDer = detenerGif;
			}
		}
		
		private static final int MS_OCULTAR_BOTONES_TABLET = 840;
		private Timer timerOcultarBotonesTablet;
		private Timer timerTransicionTablet;

		private boolean transicionTabletActiva = false;

		public boolean isTransicionTabletActiva() {
			return transicionTabletActiva;
		}
		
		// APERTURA -- exactamente la misma lógica de siempre (GifEscalado/
		// CargarGifs), ahora autocontenida -- ya no depende de ningún
		// método compartido con el cierre, porque ambos flujos divergen
		// demasiado (pnlJuego vs pnlTableta) para justificar compartir
		// una abstracción común.
		public void mostrarTransicionTablet(String rutaGif, Runnable alTerminar) {
			mostrarTransicionTablet(rutaGif, null, alTerminar);
		}

		public void mostrarTransicionTablet(String rutaGif, Runnable accionInmediata, Runnable alTerminar) {

			if (timerTransicionTablet != null && timerTransicionTablet.isRunning()) {
				timerTransicionTablet.stop();
			}
			if (timerOcultarBotonesTablet != null && timerOcultarBotonesTablet.isRunning()) {
				timerOcultarBotonesTablet.stop();
			}

			com.fdaf.util.InteraccionBoton.obtenerSonidoHover().play();

			transicionTabletActiva = true;
			controllerCamara.detenerMovimientoCamara();

			pnlJuego.getLblTabAbrir().setVisible(false);
			pnlJuego.getLblTabAbrir().setEnabled(false);
			pnlTableta.getLblTabletCerrar().setVisible(false);
			pnlTableta.getLblTabletCerrar().setEnabled(false);

			Runnable mostrarGif = () -> {
				JLabel destino = pnlJuego.getLblTabletTransicion();

				ImageIcon gif = com.fdaf.util.CargarGifs.cargarFresco(rutaGif, null);
				destino.setIcon(new EscalarVista.GifEscalado(gif, destino.getWidth(), destino.getHeight()));
				destino.setVisible(true);
				pnlJuego.setComponentZOrder(destino, 0);
				destino.repaint();

				timerTransicionTablet = new Timer(360, e -> {
					((Timer) e.getSource()).stop();
					destino.setVisible(false);
					destino.setIcon(null);
					if (alTerminar != null) {
						alTerminar.run();
					}
				});
				timerTransicionTablet.setRepeats(false);
				timerTransicionTablet.start();
			};

			if (accionInmediata != null) {
				accionInmediata.run();
				javax.swing.SwingUtilities.invokeLater(mostrarGif);
			} else {
				mostrarGif.run();
			}

			timerOcultarBotonesTablet = new Timer(MS_OCULTAR_BOTONES_TABLET, e -> {
				((Timer) e.getSource()).stop();
				pnlJuego.getLblTabAbrir().setVisible(true);
				pnlJuego.getLblTabAbrir().setEnabled(true);
				pnlTableta.getLblTabletCerrar().setVisible(true);
				pnlTableta.getLblTabletCerrar().setEnabled(true);

				javax.swing.SwingUtilities.invokeLater(() -> transicionTabletActiva = false);
			});
			timerOcultarBotonesTablet.setRepeats(false);
			timerOcultarBotonesTablet.start();
		}
		@Override
		public void alAbrirPuerta(String lado) {
			if ("izq".equals(lado)) {
				
				pnlJuego.getBotonIzq().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.btnIzq1,
						pnlJuego.getBotonIzq().getWidth(), pnlJuego.getBotonIzq().getHeight()));

				//ESTO ES PARA REPRODUCIR GIFS DE LAS PUERTAS 
				// NO BORRAR
				reproducirGifPuertaUnaVez("izq", pnlJuego.getLblPuertaIzq(),
						"/images/puertas/pIZQ-ABRIR.gif", CargarImagenes.puertaIzq1);
				

				
			} else {
				pnlJuego.getBotonDer().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.btnDer1,
						pnlJuego.getBotonDer().getWidth(), pnlJuego.getBotonDer().getHeight()));

				//ESTO ES PARA REPRODUCIR GIFS DE LAS PUERTAS 
				// NO BORRAR
				reproducirGifPuertaUnaVez("der", pnlJuego.getLblPuertaDer(),
						"/images/puertas/pDER-ABRIR.gif", CargarImagenes.puertaDer1);
				

				
				
			}
			pnlJuego.revalidate();
			pnlJuego.repaint();
		}

		@Override
		public void alCerrarPuerta(String lado) {
			if ("izq".equals(lado)) {
				pnlJuego.getBotonIzq().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.btnIzq0,
						pnlJuego.getBotonIzq().getWidth(), pnlJuego.getBotonIzq().getHeight()));

				
				//ESTO ES PARA REPRODUCIR GIFS DE LAS PUERTAS 
				// NO BORRAR
				reproducirGifPuertaUnaVez("izq", pnlJuego.getLblPuertaIzq(),
						"/images/puertas/pIZQ-CERRAR.gif", CargarImagenes.puertaIzq0);
				

				
			} else {
				pnlJuego.getBotonDer().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.btnDer0,
						pnlJuego.getBotonDer().getWidth(), pnlJuego.getBotonDer().getHeight()));
				
				//ESTO ES PARA REPRODUCIR GIFS DE LAS PUERTAS 
				// NO BORRAR
				reproducirGifPuertaUnaVez("der", pnlJuego.getLblPuertaDer(),
						"/images/puertas/pDER-CERRAR.gif", CargarImagenes.puertaDer0);
				

				
			}
			pnlJuego.revalidate();
			pnlJuego.repaint();
		}

	@Override
	public void alEncenderLuz(String lado) {

		if ("izq".equals(lado)) {
			pnlJuego.getLuzIzq().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.luzIzq1,
					pnlJuego.getLuzIzq().getWidth(), pnlJuego.getLuzIzq().getHeight()));

			pnlJuego.getLblImgOficina().setIcon(gifOficinaLuzIzq);

		} else {
			pnlJuego.getLuzDer().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.luzDer1,
					pnlJuego.getLuzDer().getWidth(), pnlJuego.getLuzDer().getHeight()));

			pnlJuego.getLblImgOficina().setIcon(gifOficinaLuzDer);
		}

		pnlJuego.revalidate();
		pnlJuego.repaint();
	}

	@Override
	public void alApagarLuz(String lado) {
		if ("izq".equals(lado)) {
			pnlJuego.getLuzIzq().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.luzIzq0,
					pnlJuego.getLuzIzq().getWidth(), pnlJuego.getLuzIzq().getHeight()));
		} else {
			pnlJuego.getLuzDer().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.luzDer0,
					pnlJuego.getLuzDer().getWidth(), pnlJuego.getLuzDer().getHeight()));
		}

		pnlJuego.getLblImgOficina().setIcon(gifOficinaNormal);

		pnlJuego.revalidate();
		pnlJuego.repaint();
	}

	@Override
	public void alRevelarAnimatronico(String lado, Animatronico animatronico) {
		ultimoFueAnimatronico = true;

		// Silencia el ambiente aleatorio ANTES de que empiece el
		// jumpscare -- cubre tanto el que te mata (alPerder() tarda ~1s
		// más en ejecutarse) como el que sobrevives (alPerder() nunca
		// llega a llamarse en ese caso, así que sin esto el ambiente
		// nunca se pausaría).
		sonidosAmbientales.detener();

		sonido = reproducirJumpscareConFade("animatronicos/jumpscare.wav");

		String nombre = animatronico.name();
		String capitalizado = nombre.charAt(0) + nombre.substring(1).toLowerCase();
		String ruta = "/gifs/jumpscares/Jumpscare" + capitalizado + ".gif";

		mostrarEnOverlay(ruta, 1, lado);
	}

	@Override
	public void alRevelarColeccionable(String lado, Coleccionable coleccionable) {
		sonido = new Sonido("coleccionables/coleccionable_encontrado.wav");
		sonido.play();
		
		String ruta = "/images/" + coleccionable.getArchivoImagen();
		mostrarEnOverlay(ruta, 0, lado);
	}

	@Override
	public void alRevelarNada(String lado) {
	}

	@Override
	public void alRevelarBateria(String lado) {
		// Mismo sonido de hallazgo que ya usan los coleccionables --
		// reutilizado, no uno nuevo.
		sonido = new Sonido("coleccionables/coleccionable_encontrado.wav");
		sonido.play();

		mostrarIconoEnOverlay(CargarImagenes.bateria1, lado, 45);
	}

	private void mostrarEnOverlay(String ruta, int c, String lado) {
		URL recurso = getClass().getResource(ruta);
		if (recurso == null) {
			System.out.println("[RECURSO NO ENCONTRADO] " + ruta);
			return;
		}

		JLabel overlay = pnlJuego.getLblOverlay();

		if (c == 1) {
			overlay.setCursor(Cursor.getDefaultCursor());
			overlay.setBounds(0, 0, EscalarVista.getEscalaX(1610), EscalarVista.getEscalaY(910));
			ImageIcon jumpscareFresco = com.fdaf.util.CargarGifs.cargarFresco(ruta, new ImageIcon(recurso));
			overlay.setIcon(
					new EscalarVista.GifEscalado(jumpscareFresco, overlay.getWidth(), overlay.getHeight()));

		} else {
			mostrarIconoEnOverlay(new ImageIcon(recurso), lado);
			return;
		}

		overlay.setVisible(true);
		pnlJuego.setComponentZOrder(overlay, 0);
		overlay.repaint();
		pnlJuego.repaint();
	}

	private void mostrarIconoEnOverlay(ImageIcon icono, String lado) {
		mostrarIconoEnOverlay(icono, lado, 80);
	}

	// altoBase: mismo ancho de siempre (80), altura configurable -- solo
	// la batería usa un valor distinto (proporción más "aplastada", menos
	// alargada) sin afectar el tamaño de los coleccionables normales.
	private void mostrarIconoEnOverlay(ImageIcon icono, String lado, int altoBase) {
		JLabel overlay = pnlJuego.getLblOverlay();
		overlay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		if ("izq".equals(lado)) {
			overlay.setBounds(EscalarVista.getEscalaX(350), EscalarVista.getEscalaY(710),
					EscalarVista.getEscalaX(80), EscalarVista.getEscalaY(altoBase));
		} else {
			overlay.setBounds(EscalarVista.getEscalaX(1200), EscalarVista.getEscalaY(740),
					EscalarVista.getEscalaX(80), EscalarVista.getEscalaY(altoBase));
		}
		overlay.setIcon(EscalarVista.getImagenEscalada(icono, overlay.getWidth(), overlay.getHeight()));

		overlay.setVisible(true);
		pnlJuego.setComponentZOrder(overlay, 0);
		overlay.repaint();
		pnlJuego.repaint();
	}

	// Jumpscare con fade-out real: volumen normal hasta 0.8s, luego se
	// desvanece durante 0.7s más -- 1.5s de audio en total. Es la ÚNICA
	// función que reproduce sonido de jumpscare en todo el proyecto,
	// tanto para revelaciones normales como para el Freddy sin luz de
	// Game Over -- no existe ninguna otra ruta que reproduzca este sonido.
	private Sonido reproducirJumpscareConFade(String rutaSonido) {

		Sonido s = new Sonido(rutaSonido);
		s.play();

		Timer iniciarFade = new Timer(0, e -> {
			((Timer) e.getSource()).stop();
			s.fadeOut(1500);
		});
		iniciarFade.setRepeats(false);
		iniciarFade.start();

		return s;
	}

	@Override
	public void alRecogerColeccionable(Coleccionable coleccionable) {
		String ruta = "/images/" + coleccionable.getArchivoImagen();
		URL recurso = getClass().getResource(ruta);
		if (recurso == null) {
			System.out.println("[RECURSO NO ENCONTRADO] " + ruta);
			return;
		}

		agregarAlInventario(coleccionable, recurso);
		pnlTableta.getPnlColeccionables().revalidate();
		pnlTableta.getPnlColeccionables().repaint();
	}
	
	// Se llama una sola vez al arrancar la partida. Para cada uno de los
	// 10 TipoColeccionable, decide si esta noche lo incluye o no
	// comparando su ordinal contra Noche.getCantidadColeccionables() --
	// la misma regla que ya usa CargarColeccionables internamente, sin
	// inventar una segunda fuente de verdad. Los que sí existen esta
	// noche arrancan con su silueta bloqueada (archivoBloqueado, ya
	// existente en el proyecto y nunca usado hasta ahora); los que no
	// existen muestran una X de texto -- no hay ningún recurso de imagen
	// "X" en el proyecto, así que se evita depender de uno nuevo.
	private void inicializarEstadoInventario() {

		int cantidadEstaNoche = PreferenciasJuego.nocheActual.getCantidadColeccionables();

		for (com.fdaf.mvc.models.coleccionables.TipoColeccionable tipo :
				com.fdaf.mvc.models.coleccionables.TipoColeccionable.values()) {

			javax.swing.JLabel destino = obtenerLabelInventario(tipo);
			if (destino == null) continue;

			if (tipo.ordinal() < cantidadEstaNoche) {
				URL recursoBloqueado = getClass().getResource("/images/" + tipo.getArchivoBloqueado());
				if (recursoBloqueado != null) {
					destino.setText("");
					// Lee el tamano real del slot (destino.getWidth()/getHeight()) en vez de un
					// 100x92 hardcodeado -- bug real: el commit que redujo los slots del
					// inventario a 74x68 (ver PnlTableta) nunca actualizo este valor, asi que
					// cada icono se escalaba a un tamano mayor que su propio contenedor.
					destino.setIcon(EscalarVista.getImagenEscalada(new ImageIcon(recursoBloqueado),
							destino.getWidth(), destino.getHeight()));
				}
			} else {
				destino.setIcon(null);
				destino.setText("X");
				destino.setHorizontalAlignment(SwingConstants.CENTER);
				destino.setVerticalAlignment(SwingConstants.CENTER);
				destino.setForeground(Color.BLACK);
				// Sans-serif en negrita, no Consolas: la X debe leerse
				// como marcador gráfico de bloqueo, no como texto -- una
				// fuente monoespaciada de código no llena el espacio con
				// el mismo peso visual. Tamaño proporcional al slot real
				// (100x92), ~70% del alto para llenarlo sin desbordar.
				destino.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 68));
			}
		}
	}
	
	private void conectarClicsInventario() {
		conectarClicColeccionable(pnlTableta.getPELUCHE_FREDDY(), TipoColeccionable.PELUCHE_FREDDY);
		conectarClicColeccionable(pnlTableta.getPELUCHE_BONNIE(), TipoColeccionable.PELUCHE_BONNIE);
		conectarClicColeccionable(pnlTableta.getPELUCHE_FOXY(), TipoColeccionable.PELUCHE_FOXY);
		conectarClicColeccionable(pnlTableta.getPELUCHE_CHICA(), TipoColeccionable.PELUCHE_CHICA);
		conectarClicColeccionable(pnlTableta.getPELUCHE_GOLDEN_FREDDY(), TipoColeccionable.PELUCHE_GOLDEN_FREDDY);
		conectarClicColeccionable(pnlTableta.getCUPCAKE(), TipoColeccionable.CUPCAKE);
		conectarClicColeccionable(pnlTableta.getMICROFONO_FREDDY(), TipoColeccionable.MICROFONO_FREDDY);
		conectarClicColeccionable(pnlTableta.getGUITARRA_BONNIE(), TipoColeccionable.GUITARRA_BONNIE);
		conectarClicColeccionable(pnlTableta.getGARFIO_FOXY(), TipoColeccionable.GARFIO_FOXY);
		conectarClicColeccionable(pnlTableta.getBALLOONBOY(), TipoColeccionable.BALLOONBOY);
	}

	// Cada label captura directamente su propio tipo en el lambda -- no
	// hace falta ningún mapeo inverso label->tipo.
	private void conectarClicColeccionable(JLabel label, TipoColeccionable tipo) {
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				manejarClicColeccionable(tipo);
			}
		});
	}

	private void manejarClicColeccionable(TipoColeccionable tipo) {

		if (controllerJuego.isJuegoTerminado()) {
			return;
		}

		int cantidadEstaNoche = PreferenciasJuego.nocheActual.getCantidadColeccionables();

		if (tipo.ordinal() >= cantidadEstaNoche) {
			// No existe esta noche: sonido de error, sin tocar la
			// selección ni la descripción actualmente mostradas.
			new Sonido("botones/error_al_presionar_boton.wav").play();
			return;
		}

		com.fdaf.util.InteraccionBoton.reproducirSonidoHover();

		boolean ingles = (PreferenciasJuego.idiomaSeleccionado == Idioma.INGLES);

		Coleccionable coleccionable = controllerJuego.getJuego().getInventario().buscarPorTipo(tipo);
		boolean encontrado = (coleccionable != null && coleccionable.isEncontrado());

		String ruta;
		String descripcion;

		if (encontrado) {
			ruta = "/images/" + tipo.getArchivoDesbloqueado();
			descripcion = tipo.getDescripcion(ingles);
		} else {
			ruta = "/images/" + tipo.getArchivoBloqueado();
			descripcion = ingles ? "Unknown" : "Desconocido";
		}

		mostrarDetalleColeccionable(ruta, descripcion);
	}

	// Escala la imagen respetando proporción, calculando el cuadro
	// máximo como PORCENTAJE del tamaño real (ya escalado) de lblMostrar
	// -- no píxeles fijos del lienzo base -- para que funcione igual en
	// cualquier resolución, igual que el resto del proyecto.
	private void mostrarDetalleColeccionable(String rutaImagen, String descripcion) {

		URL recurso = getClass().getResource(rutaImagen);
		if (recurso == null) {
			System.out.println("[RECURSO NO ENCONTRADO] " + rutaImagen);
			return;
		}

		ImageIcon original = new ImageIcon(recurso);
		int anchoNatural = original.getIconWidth();
		int altoNatural = original.getIconHeight();

		JLabel lblMostrar = pnlTableta.getLblMostrar();

		double proporcionAncho = 381.0 / 412.0;
		double proporcionAlto = 226.0 / 257.0;

		int maxAncho = (int) (lblMostrar.getWidth() * proporcionAncho);
		int maxAlto = (int) (lblMostrar.getHeight() * proporcionAlto);

		double escala = Math.min((double) maxAncho / anchoNatural, (double) maxAlto / altoNatural);
		int anchoFinal = Math.max(1, (int) (anchoNatural * escala));
		int altoFinal = Math.max(1, (int) (altoNatural * escala));

		lblMostrar.setHorizontalAlignment(SwingConstants.CENTER);
		lblMostrar.setVerticalAlignment(SwingConstants.CENTER);
		lblMostrar.setIcon(EscalarVista.getImagenEscalada(original, anchoFinal, altoFinal));

		pnlTableta.getLblDescripcion().setText(
				"<html><div style='text-align:center;'>" + descripcion + "</div></html>");
	}

	// Mismo mapeo tipo->label que ya usa agregarAlInventario, extraído
	// para reutilizarse desde ambos métodos sin duplicar el switch.
	private javax.swing.JLabel obtenerLabelInventario(com.fdaf.mvc.models.coleccionables.TipoColeccionable tipo) {
		switch (tipo) {
		case PELUCHE_FREDDY: return pnlTableta.getPELUCHE_FREDDY();
		case PELUCHE_BONNIE: return pnlTableta.getPELUCHE_BONNIE();
		case PELUCHE_FOXY: return pnlTableta.getPELUCHE_FOXY();
		case PELUCHE_CHICA: return pnlTableta.getPELUCHE_CHICA();
		case PELUCHE_GOLDEN_FREDDY: return pnlTableta.getPELUCHE_GOLDEN_FREDDY();
		case CUPCAKE: return pnlTableta.getCUPCAKE();
		case MICROFONO_FREDDY: return pnlTableta.getMICROFONO_FREDDY();
		case GUITARRA_BONNIE: return pnlTableta.getGUITARRA_BONNIE();
		case GARFIO_FOXY: return pnlTableta.getGARFIO_FOXY();
		case BALLOONBOY: return pnlTableta.getBALLOONBOY();
		default: return null;
		}
	}

	
	public void agregarAlInventario(Coleccionable coleccionable, URL recurso) {
		switch (coleccionable.getTipo()) {
		// Cada icono se escala al tamano REAL de su propio JLabel (getWidth()/getHeight()), no a
		// un 100x92 hardcodeado -- bug real corregido 2026-08-05: el commit que redujo los slots
		// del inventario a 74x68 (ver PnlTableta) nunca actualizo estos 10 valores, asi que cada
		// coleccionable encontrado se escalaba a un tamano mayor que su propio contenedor.
		case PELUCHE_FREDDY:
			pnlTableta.getPELUCHE_FREDDY().setIcon(EscalarVista.getImagenEscalada(new ImageIcon(recurso),
					pnlTableta.getPELUCHE_FREDDY().getWidth(), pnlTableta.getPELUCHE_FREDDY().getHeight()));
			break;
		case PELUCHE_BONNIE:
			pnlTableta.getPELUCHE_BONNIE().setIcon(EscalarVista.getImagenEscalada(new ImageIcon(recurso),
					pnlTableta.getPELUCHE_BONNIE().getWidth(), pnlTableta.getPELUCHE_BONNIE().getHeight()));
			break;
		case PELUCHE_FOXY:
			pnlTableta.getPELUCHE_FOXY().setIcon(EscalarVista.getImagenEscalada(new ImageIcon(recurso),
					pnlTableta.getPELUCHE_FOXY().getWidth(), pnlTableta.getPELUCHE_FOXY().getHeight()));
			break;
		case PELUCHE_CHICA:
			pnlTableta.getPELUCHE_CHICA().setIcon(EscalarVista.getImagenEscalada(new ImageIcon(recurso),
					pnlTableta.getPELUCHE_CHICA().getWidth(), pnlTableta.getPELUCHE_CHICA().getHeight()));
			break;
		case PELUCHE_GOLDEN_FREDDY:
			pnlTableta.getPELUCHE_GOLDEN_FREDDY().setIcon(EscalarVista.getImagenEscalada(new ImageIcon(recurso),
					pnlTableta.getPELUCHE_GOLDEN_FREDDY().getWidth(), pnlTableta.getPELUCHE_GOLDEN_FREDDY().getHeight()));
			break;
		case CUPCAKE:
			pnlTableta.getCUPCAKE().setIcon(EscalarVista.getImagenEscalada(new ImageIcon(recurso),
					pnlTableta.getCUPCAKE().getWidth(), pnlTableta.getCUPCAKE().getHeight()));
			break;
		case MICROFONO_FREDDY:
			pnlTableta.getMICROFONO_FREDDY().setIcon(EscalarVista.getImagenEscalada(new ImageIcon(recurso),
					pnlTableta.getMICROFONO_FREDDY().getWidth(), pnlTableta.getMICROFONO_FREDDY().getHeight()));
			break;
		case GUITARRA_BONNIE:
			pnlTableta.getGUITARRA_BONNIE().setIcon(EscalarVista.getImagenEscalada(new ImageIcon(recurso),
					pnlTableta.getGUITARRA_BONNIE().getWidth(), pnlTableta.getGUITARRA_BONNIE().getHeight()));
			break;
		case GARFIO_FOXY:
			pnlTableta.getGARFIO_FOXY().setIcon(EscalarVista.getImagenEscalada(new ImageIcon(recurso),
					pnlTableta.getGARFIO_FOXY().getWidth(), pnlTableta.getGARFIO_FOXY().getHeight()));
			break;
		case BALLOONBOY:
			pnlTableta.getBALLOONBOY().setIcon(EscalarVista.getImagenEscalada(new ImageIcon(recurso),
					pnlTableta.getBALLOONBOY().getWidth(), pnlTableta.getBALLOONBOY().getHeight()));
			break;
		default:
			break;
		}
	}

	@Override
	public void alLiberar(String lado) {
		JLabel overlay = pnlJuego.getLblOverlay();
		overlay.setVisible(false);
		overlay.setIcon(null);

		// Reanuda el ambiente SOLO si venía de un jumpscare real (donde
		// alRevelarAnimatronico lo pausó explícitamente). Para
		// COLECCIONABLE/NADA, el ambiente nunca se detuvo -- no hay
		// nada que reanudar, y llamar iniciar() aquí sin condición era
		// exactamente lo que reiniciaba el temporizador en cada puerta
		// revisada, sin importar qué se revelara.
		if (ultimoFueAnimatronico) {
			sonidosAmbientales.iniciar();
		}

		if ("izq".equals(lado)) {
			pnlJuego.getLuzIzq().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.luzIzq0,
					pnlJuego.getLuzIzq().getWidth(), pnlJuego.getLuzIzq().getHeight()));

			// Cierre automatico animado (pedido explicito del usuario 2026-08-05: cuando el
			// jugador deja de sostener la puerta abierta, debe verse natural, no cerrarse de
			// golpe). Antes este cierre "automatico" (al soltar tras una revelacion) quedaba
			// comentado -- solo el cierre MANUAL (alCerrarPuerta, toggle del boton) reproducia
			// el gif; este quedaba con un snap instantaneo al icono estatico cerrado. Mismo
			// helper/gif que ya usa el cierre manual, sin logica duplicada.
			reproducirGifPuertaUnaVez("izq", pnlJuego.getLblPuertaIzq(),
					"/images/puertas/pIZQ-CERRAR.gif", CargarImagenes.puertaIzq0);

			pnlJuego.getLblImgOficina().setIcon(gifOficinaNormal);

			pnlJuego.getBotonIzq().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.btnIzq0,
					pnlJuego.getBotonIzq().getWidth(), pnlJuego.getBotonIzq().getHeight()));
		} else {
			pnlJuego.getLuzDer().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.luzDer0,
					pnlJuego.getLuzDer().getWidth(), pnlJuego.getLuzDer().getHeight()));

			// Ver comentario del lado "izq" arriba -- mismo cierre automatico animado.
			reproducirGifPuertaUnaVez("der", pnlJuego.getLblPuertaDer(),
					"/images/puertas/pDER-CERRAR.gif", CargarImagenes.puertaDer0);

			pnlJuego.getLblImgOficina().setIcon(gifOficinaNormal);

			pnlJuego.getBotonDer().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.btnDer0,
					pnlJuego.getBotonDer().getWidth(), pnlJuego.getBotonDer().getHeight()));
		}
		pnlJuego.revalidate();
		pnlJuego.repaint();

		if (ultimoFueAnimatronico) {
			ultimoFueAnimatronico = false;
			respiracion.play();
			iniciarFadeDesdeNegro();
		}
	}

	private static final int DURACION_FADE_MS = 4000; // 4 segundos
	private Timer timerFadeNegro;

	private void iniciarFadeDesdeNegro() {

		if (pnlFadeNegro == null) {
			pnlFadeNegro = new JPanel() {
				@Override
				protected void paintComponent(Graphics g) {
					Graphics2D g2 = (Graphics2D) g.create();
					g2.setColor(new Color(0, 0, 0, Math.max(alphaFade, 0)));
					g2.fillRect(0, 0, getWidth(), getHeight());
					g2.dispose();
				}
			};
			pnlFadeNegro.setOpaque(false);
			pnlJuego.add(pnlFadeNegro);
		}

		// Si un fade anterior sigue corriendo (dos animatrónicos revelados
		// con poca diferencia de tiempo), se detiene primero -- evita dos
		// timers escribiendo sobre el mismo alphaFade a la vez.
		if (timerFadeNegro != null && timerFadeNegro.isRunning()) {
			timerFadeNegro.stop();
		}

		pnlFadeNegro.setBounds(0, 0, pnlJuego.getWidth(), pnlJuego.getHeight());
		pnlJuego.setComponentZOrder(pnlFadeNegro, 0);
		pnlFadeNegro.setVisible(true);

		alphaFade = 255;

		long inicio = System.currentTimeMillis();

		timerFadeNegro = new Timer(20, null);
		timerFadeNegro.addActionListener(e -> {
			long transcurrido = System.currentTimeMillis() - inicio;
			double progreso = (double) transcurrido / DURACION_FADE_MS; // 0.0 a 1.0

			if (progreso >= 1.0) {
				alphaFade = 0;
				pnlFadeNegro.setVisible(false);
				((Timer) e.getSource()).stop();
			} else {
				alphaFade = (int) (255 * (1.0 - progreso));
				pnlFadeNegro.repaint();
			}
		});
		timerFadeNegro.start();
	}

	@Override
	public void alActualizarVidas(int vidas) {

		ImageIcon icono;

		switch (vidas) {
		case 4:
			icono = CargarImagenes.bateria4;
			break;
		case 3:
			icono = CargarImagenes.bateria3;
			break;
		case 2:
			icono = CargarImagenes.bateria2;
			break;
		case 1:
			icono = CargarImagenes.bateria1;
			break;
		default:
			icono = CargarImagenes.bateria0;
			break;
		}

		pnlJuego.getLblBateria().setIcon(EscalarVista.getImagenEscalada(icono, pnlJuego.getLblBateria().getWidth(),
				pnlJuego.getLblBateria().getHeight()));
		pnlTableta.getLblCopyBateria().setIcon(EscalarVista.getImagenEscalada(icono,
				pnlTableta.getLblCopyBateria().getWidth(), pnlTableta.getLblCopyBateria().getHeight()));

		pnlJuego.revalidate();
		pnlJuego.repaint();
	}
	
	// Misma filosofía que ControllerMenu.actualizarTextosIdioma(): un
	// solo punto de decisión, sin ifs repartidos. Vive aquí (no en
	// ControllerMenu) porque es ControllerInterfaz quien construye estas
	// pantallas.
	private void aplicarIdiomaGameOver(PnlGameOver pantalla) {
		boolean ingles = (PreferenciasJuego.idiomaSeleccionado == Idioma.INGLES);
		pantalla.getBtnReintentar().setText(ingles ? "Retry" : "Volver a intentar");
		pantalla.getBtnSalir().setText(ingles ? "Exit Game" : "Salir del juego");
	}

	

	@Override
	public void alPerder() {

		// Ningún sonido de oficina debe sobrevivir a la muerte del
		// jugador -- se apaga TODO primero (ambiente, fondo, respiración,
		// llamada, loops de luz).
		detenerSonidosJuego();

		// Bloquea cualquier clic de puerta/luz desde este instante.
		controllerJuego.setJuegoTerminado(true);

		// Apaga ventilador/nariz/tablet, pero deja puertas y luces
		// visibles (apagadas, con sonido de error si se intentan usar).
		oscurecerOficinaParaGameOver();

		faseLargaGameOverActiva = true;

		// SEGUNDO 0: gif de game over en el MISMO lblImgOficina que usa
		// la oficina normal -- mismo tamaño, misma escala, y el
		// movimiento de cámara + zonas de interferencia de
		// ControllerCamara siguen funcionando exactamente igual porque
		// es el mismo componente, no una copia. Cero lógica duplicada.
		int anchoOficina = pnlJuego.getLblImgOficina().getWidth();
		int altoOficina = pnlJuego.getLblImgOficina().getHeight();
		ImageIcon gameOverFresco = CargarGifs.cargarFresco("/gifs/fondos/gameover.gif", CargarImagenes.getGameOverRespaldo());
		pnlJuego.getLblImgOficina().setIcon(new EscalarVista.GifEscalado(gameOverFresco, anchoOficina, altoOficina));
		pnlJuego.revalidate();
		pnlJuego.repaint();

		if (timerRepintadoGameOver != null && timerRepintadoGameOver.isRunning()) {
			timerRepintadoGameOver.stop();
		}
		timerRepintadoGameOver = new Timer(33, evRepintado -> {
			if (pnlJuego != null && pnlJuego.isShowing()) {
				pnlJuego.repaint();
			}
		});
		timerRepintadoGameOver.start();

		sonidoGameOver = new Sonido("gameover/powerdown.wav");
		sonidoGameOver.play();

		// SEGUNDO 7: Freddy acechando (sin cambio visual).
		timerGameOverT1 = new Timer(7000, e1 -> {
			((Timer) e1.getSource()).stop();

			if (sonidoGameOver != null)
				sonidoGameOver.stop();

			sonidoGameOver = new Sonido("gameover/freddy_acechando.wav");
			sonidoGameOver.play();

			// SEGUNDO 20 (13s después): se abandona la oficina por
			// completo -- aquí sí corresponde el apagado total.
			timerGameOverT2 = new Timer(13000, e2 -> {
				((Timer) e2.getSource()).stop();
				avanzarDespuesDelGifLargo();
			});
			timerGameOverT2.setRepeats(false);
			timerGameOverT2.start();
		});
		timerGameOverT1.setRepeats(false);
		timerGameOverT1.start();
	}

	// Único destino posible del salto de CTRL+S, sin importar en qué
	// tramo se presione: detiene los 4 timers (los que estén corriendo)
	// y el sonido actual, garantizando pantalla/listeners ya montados
	// (los arma avanzarDespuesDelGifLargo si todavía no existían), y
	// aterriza directo en la pantalla final. Un solo camino, sin
	// preguntar "en qué fase estoy".
	public void saltarFaseLargaGameOver() {
		if (!faseLargaGameOverActiva) {
			return;
		}

		if (timerGameOverT1 != null && timerGameOverT1.isRunning()) {
			timerGameOverT1.stop();
		}
		if (timerGameOverT2 != null && timerGameOverT2.isRunning()) {
			timerGameOverT2.stop();
		}

		if (pantallaGameOver == null) {
			// Todavía en t1/t2: arma pantalla, listeners y arranca t3 --
			// se detiene inmediatamente abajo, antes de que pueda sonar.
			avanzarDespuesDelGifLargo();
		}

		if (timerGameOverT3 != null && timerGameOverT3.isRunning()) {
			timerGameOverT3.stop();
		}
		if (timerGameOverT4 != null && timerGameOverT4.isRunning()) {
			timerGameOverT4.stop();
		}
		if (sonidoGameOver != null) {
			sonidoGameOver.stop();
		}

		mostrarPantallaFinalGameOver();
	}
	
	
	// Único cuerpo real de "la fase larga terminó" -- arma pantalla,
	// listeners y arranca t3. Ya NO apaga faseLargaGameOverActiva aquí:
	// el salto sigue siendo válido durante pasos/jumpscare/estática,
	// hasta que mostrarPantallaFinalGameOver() la cierra de verdad.
	private void avanzarDespuesDelGifLargo() {

		if (sonidoGameOver != null)
			sonidoGameOver.stop();

		if (timerRepintadoGameOver != null && timerRepintadoGameOver.isRunning()) {
			timerRepintadoGameOver.stop();
		}

		deshabilitarControles();

		pantallaGameOver = new PnlGameOver();
		EscalarVista.adaptarGameOver(vistaPrincipal, pantallaGameOver);
		vistaPrincipal.setContenido(pantallaGameOver);
		pantallaGameOver.revalidate();
		pantallaGameOver.repaint();

		sonidoGameOver = new Sonido("gameover/pasos_freddy.wav");
		sonidoGameOver.subirVolumen(5.0f); // ~20% más fuerte (aprox.)
		sonidoGameOver.play();

		// SEGUNDO 25 (5s de pasos): jumpscare de Freddy sin luz.
		timerGameOverT3 = new Timer(5000, e3 -> {
			((Timer) e3.getSource()).stop();

			if (sonidoGameOver != null)
				sonidoGameOver.stop();

			pantallaGameOver.mostrar(com.fdaf.util.CargarGifs.cargarFresco("/gifs/jumpscares/JumpscareFreddySinLuz.gif", CargarImagenes.getJumpScareRespaldo()));

			sonidoGameOver = new Sonido("animatronicos/jumpscare.wav");
			sonidoGameOver.play();

			timerGameOverT4 = new Timer(1000, e4 -> {
				((Timer) e4.getSource()).stop();

				if (sonidoGameOver != null) sonidoGameOver.stop();

				pantallaGameOver.mostrar(com.fdaf.util.CargarGifs.cargarFresco("/gifs/fondos/Static.gif", CargarImagenes.getEstaticaRespaldo()));
				sonidoGameOver = new Sonido("gameover/static.wav");
				sonidoGameOver.play();

				Timer t5 = new Timer(5000, e5 -> {
					((Timer) e5.getSource()).stop();
					mostrarPantallaFinalGameOver();
				});
				t5.setRepeats(false);
				t5.start();
			});
			timerGameOverT4.setRepeats(false);
			timerGameOverT4.start();
		});
		timerGameOverT3.setRepeats(false);
		timerGameOverT3.start();

		com.fdaf.util.InteraccionBoton.aplicarHoverYSonido(pantallaGameOver.getBtnReintentar(), pantallaGameOver.getLblFlecha(), 30);
		com.fdaf.util.InteraccionBoton.aplicarHoverYSonido(pantallaGameOver.getBtnSalir(), pantallaGameOver.getLblFlecha(), 30);

		pantallaGameOver.getBtnReintentar().addActionListener(ev -> {
			vistaPrincipal.setContenido(menu);
			if (controllerMenuPadre != null) {
				controllerMenuPadre.reanudarMusicaMenu();
				controllerMenuPadre.limpiarPartidaActiva();
			}
		});
		pantallaGameOver.getBtnSalir().addActionListener(ev -> System.exit(0));
	}

	// Único destino final, alcanzado por t5 naturalmente o por el salto
	// manual -- mismo cuerpo, sin ninguna versión "especial" para
	// CTRL+S. Aquí sí se cierra faseLargaGameOverActiva: a partir de
	// este punto ya no hay nada más que saltar.
	private void mostrarPantallaFinalGameOver() {

		faseLargaGameOverActiva = false;

		if (sonidoGameOver != null)
			sonidoGameOver.stop();

		pantallaGameOver.mostrar(CargarImagenes.gameOverFinal);

		aplicarIdiomaGameOver(pantallaGameOver);

		pantallaGameOver.getLblTitulo().setVisible(true);
		pantallaGameOver.getBtnReintentar().setVisible(true);
		pantallaGameOver.getBtnSalir().setVisible(true);

		pantallaGameOver.revalidate();
		pantallaGameOver.repaint();
	}

	@Override
	public void alGanar() {

		// Capturado ANTES de avanzar el progreso: alGanar() se llama al
		// ganar CUALQUIER noche, no solo la 5 -- sólo si la noche recién
		// completada era la última se dispara la transición a Five
		// Doors Escape en vez de la pantalla de victoria normal (ver
		// memoria de Claude "project-libgdx-office-spawn-exit-design").
		boolean eraNoche5 = PreferenciasJuego.nocheActual == com.fdaf.mvc.models.juego.Noche.NOCHE_5;
		int vidasFinales = controllerJuego.getVidas();

		// Avance automático de progreso: NOCHE_1->2->3->4->5, y NOCHE_5
		// se queda en NOCHE_5. Al inicio del método, antes de cualquier
		// efecto visual o de audio, para que quede persistido sin
		// importar qué haga el jugador después en esta pantalla.
		int siguienteOrdinal = PreferenciasJuego.nocheActual.ordinal() + 1;
		if (siguienteOrdinal < com.fdaf.mvc.models.juego.Noche.values().length) {
			PreferenciasJuego.nocheActual = com.fdaf.mvc.models.juego.Noche.values()[siguienteOrdinal];
			com.fdaf.util.PersistenciaJuego.guardar();
		}
		if (controllerMenuPadre != null) {
			controllerMenuPadre.actualizarTextosIdioma();
		}

		controllerJuego.setJuegoTerminado(true);
		deshabilitarControles();
		detenerSonidosJuego();

		PnlWin pantalla = new PnlWin();
		EscalarVista.adaptarWin(vistaPrincipal, pantalla);
		vistaPrincipal.setContenido(pantalla);
		pantalla.revalidate();
		pantalla.repaint();

		// Etapa 1: video con sonido (~9s, termina solo cuando el video acaba)
		pantalla.reproducirVideo("/videos/ganar_fdaf.mp4", () -> {

			if (eraNoche5) {
				iniciarTransicionAEscape(pantalla, vidasFinales);
				return;
			}

			// Pedido explicito del usuario 2026-08-05: ya no se muestra la pantalla de
			// "Volver a jugar"/"Salir del juego" al completar una noche -- el progreso ya
			// quedo guardado arriba (antes de cualquier efecto visual/de audio, lineas
			// 1223-1227), asi que aqui solo se vuelve directamente al menu principal, igual
			// que hacia btnJugarDeNuevo manualmente antes de este cambio.
			vistaPrincipal.setContenido(menu);
			if (controllerMenuPadre != null) {
				controllerMenuPadre.reanudarMusicaMenu();
				controllerMenuPadre.limpiarPartidaActiva();
			}
		});

	}

	// Dispara tras ganar la Noche 5 (Architecture.md #7): escribe el
	// archivo de traspaso y lanza Five Doors Escape en un proceso
	// aparte, sin bloquear el EDT. Antes de lanzarlo se reproduce un
	// efecto de "glitch" agresivo (pedido explicito del usuario
	// 2026-08-05: el juego debe sentirse como si "se rompiera" antes de
	// entrar al Escape) -- ver mostrarGlitchYLanzarEscape. Esta ventana
	// se deja visible debajo (Escape la tapa al abrir su propia ventana)
	// y solo vuelve al frente cuando el proceso de Escape termina -- por
	// Game Over, el botón manual o la victoria del Escape, todos
	// comparten el mismo mecanismo real de salida (cierre del proceso).
	private void iniciarTransicionAEscape(PnlWin pantalla, int vidasFinales) {
		mostrarGlitchYLanzarEscape(pantalla, vidasFinales);
	}

	/** Duracion del efecto glitch, en milisegundos, antes de mostrar la pantalla de carga negra y
	 * lanzar Five Doors Escape (pedido explicito del usuario 2026-08-06: "aproximadamente 4
	 * segundos", antes eran 2.6s). */
	private static final int DURACION_GLITCH_MS = 4000;
	private static final int INTERVALO_GLITCH_MS = 45;
	private Timer timerGlitch;

	/** Musica de la transicion completa Noche 5 -> Escape (pedido explicito del usuario
	 * 2026-08-06): arranca justo cuando empieza el glitch de colores y NUNCA se detiene durante
	 * toda la secuencia (glitch + pantallas de carga negra + TODA la partida de Escape) -- sigue
	 * sonando incluso despues de lanzar el proceso de Escape, hasta que termina sola (el usuario
	 * la re-exporto a ~5 minutos reales especificamente para que alcance a cubrir una partida
	 * completa de Escape, no solo la transicion). Se detiene de forma defensiva solo si el
	 * jugador vuelve al menu antes de que termine sola. Su volumen baja ~25% (unico sonido que
	 * se toca) justo cuando Escape avisa que sono la risa de Freddy -- ver
	 * timerEsperarRisaFreddy, que vigila la señal de archivo compartido porque Swing/Escape son
	 * procesos independientes sin otra forma de comunicarse ese instante. */
	private Sonido sonidoTransicionEscape;
	private Timer timerEsperarRisaFreddy;
	private static final String RUTA_SENAL_RISA_FREDDY =
			System.getProperty("user.home") + java.io.File.separator + ".fivedoorsatfreddys"
					+ java.io.File.separator + "risa_freddy.flag";

	/** Pantalla de carga negra "corrompida" (rediseño pedido explicito del usuario 2026-08-06:
	 * "ya no quiero un unico label... quiero que 'SOY YO' aparezca por toda la pantalla,
	 * muchisimas veces, en posiciones aleatorias, con diferentes tamaños, entrando y
	 * desapareciendo constantemente... debe sentirse como una pantalla completamente
	 * corrompida"). "THE GAME BEGINS"/"EMPIEZA EL JUEGO" (lblCargando) queda fijo y legible en
	 * el centro exacto -- unico texto estatico. pnlGlitchTexto es un overlay transparente que
	 * redibuja, en cada tick de timerGlitchTexto, un numero aleatorio de instancias de "SOY
	 * YO"/"IT'S ME" en posiciones/tamaños/colores nuevos (nunca las mismas dos veces seguidas,
	 * ver pintarGlitchTexto) -- mismo patron ya usado por pnlGlitch (JPanel+Timer+
	 * paintComponent propio, sin libreria nueva). */
	private JPanel pnlGlitchTexto;
	private Timer timerGlitchTexto;
	private static final int INTERVALO_GLITCH_TEXTO_MS = 90;

	/** Efecto de "corrupcion de senal" agresivo -- generado enteramente con Graphics2D dentro de
	 * un JPanel transparente superpuesto, repintado por un javax.swing.Timer, EXACTAMENTE el
	 * mismo patron ya usado por iniciarFadeDesdeNegro() (panel + Timer + paintComponent
	 * personalizado) -- sin ninguna libreria o pipeline de postprocesado nueva, tal como pidio
	 * el usuario ("investiga que puede hacerse usando unicamente las capacidades actuales del
	 * proyecto"). Reutiliza ademas Static.gif (el mismo asset que ya se usa como interferencia
	 * en la secuencia de Game Over) como una capa mas de corrupcion visual real, no solo
	 * geometria dibujada a mano. Al terminar, muestra el mensaje de carga (ya existente antes de
	 * este cambio) y recien ahi lanza el proceso de Escape -- LanzadorEscape.lanzar() en si no
	 * cambio, solo el momento en que se invoca. */
	private void mostrarGlitchYLanzarEscape(PnlWin pantalla, int vidasFinales) {
		// Arranca justo aqui, en el mismo instante en que empieza el glitch de colores (pedido
		// explicito del usuario) -- ver el campo sonidoTransicionEscape para el resto del
		// contrato (nunca se detiene durante toda la secuencia).
		sonidoTransicionEscape = new Sonido("win/LesToreadorsRemix.wav");
		sonidoTransicionEscape.play();
		iniciarEsperaRisaFreddy();

		Random random = new Random();
		Color[] paletaGlitch = {
				new Color(255, 0, 90), new Color(0, 255, 200), new Color(255, 230, 0),
				new Color(120, 0, 255), Color.WHITE, Color.BLACK
		};
		ImageIcon estaticaGlitch = com.fdaf.util.CargarGifs.cargarFresco(
				"/gifs/fondos/Static.gif", CargarImagenes.getEstaticaRespaldo());

		JPanel pnlGlitch = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				int w = getWidth();
				int h = getHeight();
				if (w <= 0 || h <= 0) {
					return;
				}
				Graphics2D g2 = (Graphics2D) g.create();

				// Base: parpadeo de color solido saturado (simula corte de señal).
				g2.setColor(paletaGlitch[random.nextInt(paletaGlitch.length)]);
				g2.fillRect(0, 0, w, h);

				// Interferencia real (mismo gif que el Game Over), desplazada al azar.
				estaticaGlitch.paintIcon(this, g2, random.nextInt(41) - 20, random.nextInt(41) - 20);

				// Barras de "tearing" horizontales con desplazamiento y color aleatorios.
				int franjas = 6 + random.nextInt(6);
				for (int i = 0; i < franjas; i++) {
					int altoFranja = 4 + random.nextInt(Math.max(1, h / 6));
					int y = random.nextInt(Math.max(1, h - altoFranja));
					int desplazamientoX = random.nextInt(121) - 60;
					g2.setColor(paletaGlitch[random.nextInt(paletaGlitch.length)]);
					g2.fillRect(desplazamientoX, y, w, altoFranja);
				}

				// Franjas de canal de color (simula separacion RGB de una señal corrupta).
				g2.setColor(new Color(255, 0, 0, 140));
				g2.fillRect(random.nextInt(21) - 10, 0, w, h / 3);
				g2.setColor(new Color(0, 255, 255, 140));
				g2.fillRect(random.nextInt(21) - 10, h / 3, w, h / 3);

				g2.dispose();
			}
		};
		pnlGlitch.setOpaque(false);
		pantalla.add(pnlGlitch);
		pnlGlitch.setBounds(0, 0, pantalla.getWidth(), pantalla.getHeight());
		pantalla.setComponentZOrder(pnlGlitch, 0);
		pnlGlitch.setVisible(true);

		long inicio = System.currentTimeMillis();
		timerGlitch = new Timer(INTERVALO_GLITCH_MS, null);
		timerGlitch.addActionListener(e -> {
			long transcurrido = System.currentTimeMillis() - inicio;
			if (transcurrido >= DURACION_GLITCH_MS) {
				((Timer) e.getSource()).stop();
				pantalla.remove(pnlGlitch);
				pantalla.revalidate();
				pantalla.repaint();
				mostrarCargaYLanzarEscape(pantalla, vidasFinales);
			} else {
				pnlGlitch.repaint();
			}
		});
		timerGlitch.start();
	}

	// Textos de la pantalla de carga corrompida, i18n via el mismo patron de ternarios que usa
	// el resto del proyecto (FDAF no tiene archivos de localizacion propios).
	private String textoEmpiezaElJuego() {
		boolean ingles = (PreferenciasJuego.idiomaSeleccionado == Idioma.INGLES);
		return ingles ? "THE GAME BEGINS" : "EMPIEZA EL JUEGO";
	}

	private String textoSoyYo() {
		boolean ingles = (PreferenciasJuego.idiomaSeleccionado == Idioma.INGLES);
		return ingles ? "IT'S ME" : "SOY YO";
	}

	/** Pantalla de carga negra con mensajes tipo error apareciendo de forma aleatoria/glitcheada
	 * (pedido explicito del usuario 2026-08-06: "ya NO quiero usar el fondo de Win... fondo
	 * completamente negro... que no sigan un patron perfecto, que sea aleatorio y de miedo tipo
	 * errores") mientras el proceso de Escape termina de arrancar -- puede tardar bastante mas
	 * que el glitch en si. sonidoTransicionEscape (arrancado en mostrarGlitchYLanzarEscape) sigue
	 * sonando sin interrupcion durante todo este tramo -- no se toca aqui en absoluto. */
	// Vigila la señal de archivo compartido que Escape (proceso independiente) escribe justo
	// cuando suena la risa de Freddy en su propia introduccion (ver
	// com.fivedoorsescape.io.SenalRisaFreddy del lado Escape) -- pedido explicito del usuario
	// 2026-08-06: bajar el volumen de sonidoTransicionEscape ~25% en ese instante exacto, sin
	// tocar el volumen de ningun otro sonido. Se borra cualquier señal vieja antes de empezar a
	// vigilar (por si quedo de una sesion anterior) para no disparar el cambio de volumen de
	// inmediato por error.
	private static final int INTERVALO_ESPERA_RISA_FREDDY_MS = 400;
	private void iniciarEsperaRisaFreddy() {
		java.io.File archivoSenal = new java.io.File(RUTA_SENAL_RISA_FREDDY);
		archivoSenal.delete();

		if (timerEsperarRisaFreddy != null && timerEsperarRisaFreddy.isRunning()) {
			timerEsperarRisaFreddy.stop();
		}
		timerEsperarRisaFreddy = new Timer(INTERVALO_ESPERA_RISA_FREDDY_MS, e -> {
			if (archivoSenal.exists()) {
				archivoSenal.delete();
				if (sonidoTransicionEscape != null) {
					// ~25% de reduccion de volumen LINEAL equivale a aproximadamente -2.5dB
					// (20*log10(0.75)) -- unico sonido que se toca, pedido explicito del usuario.
					sonidoTransicionEscape.subirVolumen(-2.5f);
				}
				((Timer) e.getSource()).stop();
			}
		});
		timerEsperarRisaFreddy.start();
	}

	private void mostrarCargaYLanzarEscape(PnlWin pantalla, int vidasFinales) {
		pantalla.mostrarFondo(CargarImagenes.fondoNegro);

		// "THE GAME BEGINS"/"EMPIEZA EL JUEGO" -- unico texto fijo y legible, centrado de verdad
		// en la pantalla (antes vivia en y=650, pensado para el ciclo de mensajes viejo). Se fija
		// UNA sola vez aqui y no vuelve a cambiar durante toda la carga.
		JLabel lblCentro = pantalla.getLblCargando();
		lblCentro.setText(textoEmpiezaElJuego());
		lblCentro.setForeground(Color.WHITE);
		lblCentro.setFont(Fuentes.obtener(42));
		lblCentro.setBounds(0, pantalla.getHeight() / 2 - 40, pantalla.getWidth(), 80);
		lblCentro.setVisible(true);

		// Overlay transparente de pantalla completa -- redibuja en cada tick un lote nuevo de
		// instancias de "SOY YO" en posiciones/tamaños/colores aleatorios (nunca las mismas dos
		// veces, ver pintarGlitchTexto) para que se sienta como una pantalla corrompida, no un
		// patron repetitivo. Mismo patron ya probado por pnlGlitch (JPanel+Timer+
		// paintComponent propio).
		Random random = new Random();
		pnlGlitchTexto = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				pintarGlitchTexto(g, getWidth(), getHeight(), random);
			}
		};
		pnlGlitchTexto.setOpaque(false);
		pantalla.add(pnlGlitchTexto);
		pnlGlitchTexto.setBounds(0, 0, pantalla.getWidth(), pantalla.getHeight());
		// lblCentro siempre al frente (z=0); el overlay de "SOY YO" queda detras, nunca tapa el
		// mensaje fijo por completo.
		pantalla.setComponentZOrder(pnlGlitchTexto, 0);
		pantalla.setComponentZOrder(lblCentro, 0);
		pnlGlitchTexto.setVisible(true);

		pantalla.revalidate();
		pantalla.repaint();

		timerGlitchTexto = new Timer(INTERVALO_GLITCH_TEXTO_MS, e -> pnlGlitchTexto.repaint());
		timerGlitchTexto.start();

		com.fdaf.util.LanzadorEscape.lanzar(PreferenciasJuego.idiomaSeleccionado, vidasFinales, () -> {
			detenerGlitchTexto(pantalla);
			// Defensivo: sonidoTransicionEscape dura 104.5s reales, mas que de sobra para todo
			// el tramo de carga + la introduccion real del Escape -- pero si el jugador vuelve
			// muy rapido al menu (p.ej. cerro Escape casi de inmediato), no debe seguir sonando
			// sobre la musica del menu.
			if (sonidoTransicionEscape != null) {
				sonidoTransicionEscape.stop();
			}
			vistaPrincipal.setContenido(menu);
			vistaPrincipal.toFront();
			vistaPrincipal.requestFocus();
			if (controllerMenuPadre != null) {
				controllerMenuPadre.reanudarMusicaMenu();
				controllerMenuPadre.limpiarPartidaActiva();
			}
		});
	}

	/** Dibuja un lote nuevo de instancias de "SOY YO"/"IT'S ME" en posiciones, tamaños y colores
	 * aleatorios -- llamado en cada tick de timerGlitchTexto, así que el conjunto entero cambia
	 * por completo en cada repintado (nunca la misma composición dos veces, sin necesidad de
	 * animar cada instancia por separado). */
	private static final Color[] COLORES_GLITCH_TEXTO = {
			Color.WHITE, Color.WHITE, Color.WHITE, Color.LIGHT_GRAY,
			new Color(255, 0, 90), new Color(0, 255, 200)
	};
	private void pintarGlitchTexto(Graphics g, int w, int h, Random random) {
		if (w <= 0 || h <= 0) {
			return;
		}
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int instancias = 12 + random.nextInt(14); // 12-25 por lote
		for (int i = 0; i < instancias; i++) {
			int tamano = 16 + random.nextInt(70); // 16-85px
			g2.setFont(Fuentes.obtener(tamano));
			int alpha = 70 + random.nextInt(186); // 70-255, nunca invisible del todo
			Color base = COLORES_GLITCH_TEXTO[random.nextInt(COLORES_GLITCH_TEXTO.length)];
			g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha));

			String texto = textoSoyYo();
			FontMetrics fm = g2.getFontMetrics();
			int anchoTexto = fm.stringWidth(texto);
			int x = random.nextInt(Math.max(1, w - anchoTexto));
			int y = fm.getAscent() + random.nextInt(Math.max(1, h - fm.getHeight()));
			g2.drawString(texto, x, y);
		}
		g2.dispose();
	}

	private void detenerGlitchTexto(PnlWin pantalla) {
		if (timerGlitchTexto != null) {
			timerGlitchTexto.stop();
		}
		if (pnlGlitchTexto != null) {
			pantalla.remove(pnlGlitchTexto);
			pnlGlitchTexto = null;
			pantalla.revalidate();
			pantalla.repaint();
		}
	}

	private void deshabilitarControles() {
		pnlJuego.getLblPuertaDer().setEnabled(false);
		pnlJuego.getLblPuertaIzq().setEnabled(false);
		pnlJuego.getLblAbanico().setEnabled(false);
		pnlJuego.getPanelDer().setEnabled(false);
		pnlJuego.getPanelIzq().setEnabled(false);
		pnlJuego.getPanelDer().setVisible(false);
		pnlJuego.getPanelIzq().setVisible(false);
		pnlJuego.getLblTabAbrir().setEnabled(false);
		pnlJuego.getBotonIzq().setEnabled(false);
		pnlJuego.getBotonDer().setEnabled(false);
		pnlJuego.getLuzIzq().setEnabled(false);
		pnlJuego.getLuzDer().setEnabled(false);
		pnlJuego.getBotonIzq().setVisible(false);
		pnlJuego.getBotonDer().setVisible(false);
		pnlJuego.getLuzIzq().setVisible(false);
		pnlJuego.getLuzDer().setVisible(false);
		pnlJuego.revalidate();
		pnlJuego.repaint();
	}

	public void narizFreddy() {
		pnlJuego.getLblNariz().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				siPartidaActiva(() -> {
					Sonido sonido = new Sonido("varios/nariz_freddy.wav");
					sonido.play();
				});
			}
		});
	}

	public void detenerSonidosJuego() {

		controllerCamara.detenerLoopRepintado();
		sonidosAmbientales.detener();
		pantallaGameOver = null;

		// Detiene TODO timer de la partida que pudiera ejecutar un
		// callback después de este punto y modificar la interfaz --
		// tablet, fade, puertas, cámara y barra de rendirse. Único
		// punto de cierre real: se llama desde alGanar(), alPerder() y
		// ControllerCamara.rendirse() (progreso>=100), cubriendo las 3
		// formas de terminar una partida con una sola llamada.
		if (timerTransicionTablet != null && timerTransicionTablet.isRunning()) {
			timerTransicionTablet.stop();
		}
		if (timerOcultarBotonesTablet != null && timerOcultarBotonesTablet.isRunning()) {
			timerOcultarBotonesTablet.stop();
		}
		if (timerFadeNegro != null && timerFadeNegro.isRunning()) {
			timerFadeNegro.stop();
		}
		// Detener el timer no basta: si una revelación de animatrónico
		// disparó iniciarFadeDesdeNegro() justo antes de perder/ganar
		// (mismo instante -- alLiberar() la arranca y verificarFinDeJuego()
		// llama a alPerder()/alGanar() a continuación, sin que el timer
		// llegue a correr ni un tick), el panel negro queda congelado
		// completamente opaco y al frente (z=0) tapando para siempre lo
		// que se dibuje debajo -- incluido gameover.gif, que sí se pinta
		// correctamente pero nunca se ve. Ocultarlo explícitamente aquí
		// es la corrección real, no un parche sobre CargarGifs/EscalarVista.
		if (pnlFadeNegro != null) {
			pnlFadeNegro.setVisible(false);
		}
		alphaFade = 0;
		if (timerGifPuertaIzq != null && timerGifPuertaIzq.isRunning()) {
			timerGifPuertaIzq.stop();
		}
		if (timerGifPuertaDer != null && timerGifPuertaDer.isRunning()) {
			timerGifPuertaDer.stop();
		}
		if (timerGlitch != null && timerGlitch.isRunning()) {
			timerGlitch.stop();
		}
		if (timerGlitchTexto != null && timerGlitchTexto.isRunning()) {
			timerGlitchTexto.stop();
		}
		if (timerEsperarRisaFreddy != null && timerEsperarRisaFreddy.isRunning()) {
			timerEsperarRisaFreddy.stop();
		}
		controllerCamara.detenerTimersDePartida();

		// CORREGIDO: faltaba detener 'sonido' -- el jumpscare/coleccionable
		// más reciente podía seguir sonando (con su fade en curso) durante
		// los primeros instantes del Game Over.
		if (sonido != null) {
			sonido.stop();
		}

		if (sonidoJuego1 != null) {
			sonidoJuego1.stop();
		}

		if (sonidoJuego2 != null) {
			sonidoJuego2.stop();
		}

		if (respiracion != null) {
			respiracion.stop();
		}
		if (timerSonido != null) {
			timerSonido.stop();
		}

		if (sonidoGameOver != null) {
			sonidoGameOver.stop();
		}

		if (sonidoTransicionEscape != null) {
			sonidoTransicionEscape.stop();
		}

		if (sonidoWin != null) {
			sonidoWin.stop();
		}

		// false: el juego esta terminando (victoria/derrota), sonidosAmbientales ya se detuvo
		// arriba -- reanudarlo aqui seria un bug real (ver comentario en colgarLlamada(boolean)).
		colgarLlamada(false);

	// Ningún sonido de oficina debe sobrevivir a ganar/perder --
	// incluye los loops de luz, que viven dentro de ControllerJuego.
		controllerJuego.detenerTodosLosSonidos();
	}

	// Apaga visualmente los elementos "electrónicos" de la oficina para el
	// tramo inicial del Game Over (segundos 0-20), SIN ocultar puertas ni
	// luces -- esas se mantienen visibles pero inertes: ControllerJuego ya
	// bloquea cualquier clic (juegoTerminado) y reproduce el sonido de
	// error, así que no hace falta ocultarlas ni deshabilitarlas aquí.
	private void oscurecerOficinaParaGameOver() {
		pnlJuego.getLblAbanico().setVisible(false);
		pnlJuego.getLblNariz().setEnabled(false);

		// Tablet eliminada visualmente por completo (no solo deshabilitada).
		pnlJuego.getLblTabAbrir().setVisible(false);
		pnlJuego.getLblTabAbrir().setEnabled(false);

		// Las puertas (gráfico grande) desaparecen del todo. Los botones
		// de puerta/luz siguen visibles pero inertes -- juegoTerminado ya
		// bloquea cualquier clic con sonido de error.
		pnlJuego.getLblPuertaIzq().setVisible(false);
		pnlJuego.getLblPuertaDer().setVisible(false);

		// Solo un empujón puntual a la posición inicial -- no es un
		// mecanismo de fijado, no agrega condiciones a la cámara. Después
		// de esto, el botón se mueve exactamente igual que siempre.
		controllerCamara.moverPanelIzquierdoInicialmente(EscalarVista.getEscalaX(-75));

		pnlJuego.revalidate();
		pnlJuego.repaint();
	}

	public ControllerJuego getControllerJuego() {
		return controllerJuego;
	}

	public PnlJuego getPnlJuego() {
		return pnlJuego;
	}

	public PnlTableta getPnlTableta() {
		return pnlTableta;
	}
	
}