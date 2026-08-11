package com.fdaf.util;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.fdaf.mvc.views.frames.VistaPrincipal;
import com.fdaf.mvc.views.frames.pnl.PnlWin;
import com.fdaf.mvc.views.multimedia.Sonido;

/**
 * Transicion completa Noche 5 -> Escape: glitch agresivo (~4s) + pantalla de carga negra
 * "corrompida" ("SOY YO"/"IT'S ME" disperso, "EMPIEZA EL JUEGO"/"THE GAME BEGINS" fijo) +
 * LesToreadorsRemix (arranca con el glitch, se reduce ~8dB cuando Escape avisa que sono la risa
 * de Freddy) + LanzadorEscape.lanzar().
 *
 * Extraida de ControllerInterfaz (unico llamador hasta la sesion 2026-08-10, ver
 * iniciarTransicionAEscape) para que Custom Night -> ESCAPE (pedido explicito del usuario
 * 2026-08-10: "quiero que empiece exactamente desde la transicion de Night 5 -> Escape... NO debe
 * aparecer el video/pantalla de victoria de Night 5") pueda reutilizar EXACTAMENTE la misma
 * secuencia visual/sonora sin duplicar logica, en vez de la pantalla de "Cargando..." bare que se
 * usaba antes solo para ese camino. Cada instancia es de un solo uso (una transicion completa),
 * igual que antes cuando este estado vivia como campos de instancia de ControllerInterfaz.
 */
public class TransicionEscape {

	private static final int DURACION_GLITCH_MS = 4000;
	private static final int INTERVALO_GLITCH_MS = 45;
	private static final int INTERVALO_ESPERA_RISA_FREDDY_MS = 400;
	private static final int INTERVALO_GLITCH_TEXTO_MS = 90;
	private static final String RUTA_SENAL_RISA_FREDDY =
			System.getProperty("user.home") + java.io.File.separator + ".fivedoorsatfreddys"
					+ java.io.File.separator + "risa_freddy.flag";
	private static final Color[] COLORES_GLITCH_TEXTO = {
			Color.WHITE, Color.WHITE, Color.WHITE, Color.LIGHT_GRAY,
			new Color(255, 0, 90), new Color(0, 255, 200)
	};

	private Sonido sonidoTransicionEscape;
	private Timer timerEsperarRisaFreddy;
	private Timer timerGlitch;
	private JPanel pnlGlitchTexto;
	private Timer timerGlitchTexto;

	/**
	 * Punto de entrada para caminos que NO tienen ya una pantalla de victoria visible (p.ej.
	 * Custom Night -> ESCAPE): crea un PnlWin nuevo, lo muestra, y arranca la transicion completa
	 * directamente desde el glitch -- nunca el video/pantalla de victoria de Noche 5.
	 */
	public void iniciarDesdeMenu(VistaPrincipal vp, int vidasFinales, Runnable alTerminarYVolver) {
		PnlWin pantalla = new PnlWin();
		EscalarVista.adaptarWin(vp, pantalla);
		vp.setContenido(pantalla);
		pantalla.revalidate();
		pantalla.repaint();
		mostrarGlitchYLanzarEscape(vp, pantalla, vidasFinales, alTerminarYVolver);
	}

	/**
	 * Punto de entrada para el camino real de la Noche 5: reutiliza el PnlWin que ya esta visible
	 * (con el video de victoria recien terminado) -- el primer frame del glitch ya cubre toda la
	 * pantalla con un color solido opaco, asi que no hay diferencia visual real entre partir de
	 * este panel o de uno nuevo, pero se conserva el mismo panel para no alterar en nada el
	 * comportamiento ya verificado de este camino.
	 */
	public void continuarSobrePantallaExistente(PnlWin pantalla, VistaPrincipal vp, int vidasFinales,
			Runnable alTerminarYVolver) {
		mostrarGlitchYLanzarEscape(vp, pantalla, vidasFinales, alTerminarYVolver);
	}

	private void mostrarGlitchYLanzarEscape(VistaPrincipal vp, PnlWin pantalla, int vidasFinales,
			Runnable alTerminarYVolver) {
		// Arranca justo aqui, en el mismo instante en que empieza el glitch de colores (pedido
		// explicito del usuario) -- nunca se detiene durante toda la secuencia (glitch + pantallas
		// de carga negra + toda la partida de Escape), salvo que el jugador vuelva al menu antes.
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

				// Intercalado con el glitch de texto (pedido explicito del usuario 2026-08-10):
				// dentro de estos mismos ~4s, alternar frames de glitch puro con frames de
				// glitch+"SOY YO"/"IT'S ME" disperso, reutilizando pintarGlitchTexto (el mismo
				// metodo que ya usa la pantalla de carga posterior, sin duplicar logica) -- ~50%
				// de los repaints (cada ~45ms) superponen el texto, dando la sensacion de
				// interferencia alternante pedida ("glitch -> SOY YO -> glitch -> SOY YO") en vez
				// de separar ambos efectos en fases secuenciales.
				if (random.nextBoolean()) {
					pintarGlitchTexto(g2, w, h, random);
				}

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
				mostrarCargaYLanzarEscape(vp, pantalla, vidasFinales, alTerminarYVolver);
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
					sonidoTransicionEscape.subirVolumen(-8f);
				}
				((Timer) e.getSource()).stop();
			}
		});
		timerEsperarRisaFreddy.start();
	}

	private void mostrarCargaYLanzarEscape(VistaPrincipal vp, PnlWin pantalla, int vidasFinales,
			Runnable alTerminarYVolver) {
		pantalla.mostrarFondo(CargarImagenes.fondoNegro);

		JLabel lblCentro = pantalla.getLblCargando();
		lblCentro.setText(textoEmpiezaElJuego());
		lblCentro.setForeground(Color.WHITE);
		lblCentro.setFont(Fuentes.obtener(42));
		lblCentro.setBounds(0, pantalla.getHeight() / 2 - 40, pantalla.getWidth(), 80);
		lblCentro.setVisible(true);

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
		pantalla.setComponentZOrder(pnlGlitchTexto, 0);
		pantalla.setComponentZOrder(lblCentro, 0);
		pnlGlitchTexto.setVisible(true);

		pantalla.revalidate();
		pantalla.repaint();

		timerGlitchTexto = new Timer(INTERVALO_GLITCH_TEXTO_MS, e -> pnlGlitchTexto.repaint());
		timerGlitchTexto.start();

		LanzadorEscape.lanzar(PreferenciasJuego.idiomaSeleccionado, vidasFinales, () -> {
			detenerGlitchTexto(pantalla);
			// Defensivo: sonidoTransicionEscape dura ~104.5s reales, mas que de sobra para todo el
			// tramo de carga + la introduccion real del Escape -- pero si el jugador vuelve muy
			// rapido al menu, no debe seguir sonando sobre la musica del menu.
			if (sonidoTransicionEscape != null) {
				sonidoTransicionEscape.stop();
			}
			alTerminarYVolver.run();
		});
	}

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

	/** Detiene defensivamente todos los timers/el sonido de una transicion en curso -- pensado
	 * para ControllerInterfaz.detenerSonidosJuego() (por si un camino de fin de partida se
	 * disparara durante la transicion, aunque los controles ya esten deshabilitados en ese punto)
	 * o para cualquier otro llamador que necesite abortar una transicion en marcha. Misma defensa
	 * que ya existia como campos sueltos de ControllerInterfaz antes de esta extraccion. */
	public void detener() {
		if (timerGlitch != null && timerGlitch.isRunning()) {
			timerGlitch.stop();
		}
		if (timerGlitchTexto != null && timerGlitchTexto.isRunning()) {
			timerGlitchTexto.stop();
		}
		if (timerEsperarRisaFreddy != null && timerEsperarRisaFreddy.isRunning()) {
			timerEsperarRisaFreddy.stop();
		}
		if (sonidoTransicionEscape != null) {
			sonidoTransicionEscape.stop();
		}
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
}
