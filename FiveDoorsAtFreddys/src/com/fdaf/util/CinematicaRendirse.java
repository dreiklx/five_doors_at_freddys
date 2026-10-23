package com.fdaf.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import com.fdaf.mvc.views.frames.VistaPrincipal;
import com.fdaf.mvc.views.multimedia.Sonido;

/**
 * Cinematica al rendirse (CLAUDE.md §1.59). Independiente del Game Over normal: solo se dispara cuando se completa
 * la barra de Rendirse (ControllerCamara.rendirse) y termina en el mismo reinicio completo de siempre.
 *
 * Inspirada en las alucinaciones de fin de noche de FNAF 2 (vista a traves de la mascara de Freddy, panoramica
 * que se recorre con el raton, Bonnie y Chica a los lados) y en el Game Over de FNAF 1 (el guardia metido en un
 * traje). Aqui el jugador que se rinde termina DENTRO del traje de Freddy en el escenario: la oficina se apaga,
 * se oyen los resortes del traje cerrarse, la vision vuelve por los ojos de la cabeza, Bonnie y Chica giran la
 * cabeza hacia el, Freddy cruza el comedor y se planta delante con los ojos encendidos mientras los focos fallan.
 *
 * Los fotogramas son un panorama equirectangular de 160 x 53 grados renderizado fuera del juego con Blender
 * (Cycles) a partir de los modelos reales de Five Doors Escape -- ver tools/cinematics/ en la raiz del repo. Se
 * decodifican de a poco en un hilo propio (nunca en el EDT) y solo se conservan los cercanos al actual.
 *
 * Linea de tiempo (ms desde que se completa la barra), sincronizada con la banda sonora surrender_mix.wav:
 * 0-1250 la oficina se apaga a tirones; 1250-2200 negro con los resortes; 2200-10150 los 160 fotogramas
 * (20 fps); 10150-10700 estatica; 10700-12300 negro con una frase; despues, el reinicio de siempre.
 * Clic o ESC lo saltan en cualquier momento a partir de 400 ms.
 */
public class CinematicaRendirse {

	private static final String RUTA_FOTOGRAMAS = "/videos/surrender/surrender_%04d.jpg";
	private static final String RUTA_AUDIO = "cinematics/surrender_mix.wav";
	private static final int TOTAL_FOTOGRAMAS = 160;
	private static final int FPS = 20;
	private static final int FIN_APAGON_MS = 1250;
	private static final int INICIO_FOTOGRAMAS_MS = 2200;
	private static final int FIN_FOTOGRAMAS_MS = INICIO_FOTOGRAMAS_MS + TOTAL_FOTOGRAMAS * 1000 / FPS;
	private static final int FIN_ESTATICA_MS = FIN_FOTOGRAMAS_MS + 550;
	private static final int FIN_TOTAL_MS = FIN_ESTATICA_MS + 1600;
	private static final int FUNDIDO_ENTRADA_MS = 900;
	private static final int SALTO_PERMITIDO_DESDE_MS = 400;
	private static final int FOTOGRAMAS_POR_DELANTE = 8;
	private static final int INTERVALO_REPINTADO_MS = 33;

	private final Random random = new Random();
	private final ConcurrentHashMap<Integer, BufferedImage> cache = new ConcurrentHashMap<>();
	private volatile int fotogramaPedido = 1;
	private volatile boolean terminado = false;

	private VistaPrincipal vp;
	private Runnable alTerminar;
	private JPanel panel;
	private Timer timer;
	private Thread cargador;
	private Sonido audio;
	private ImageIcon estatica;
	private BufferedImage mascara;
	private long inicioNanos;
	private float paneo = 0.5f;          // 0 = todo a la izquierda, 1 = todo a la derecha
	private float paneoObjetivo = 0.5f;
	private long ultimoMovimientoRatonNanos = Long.MIN_VALUE / 2;

	/** Mientras el jugador no mueva el raton, la mirada sigue la accion: Bonnie gira la cabeza a la derecha
	 * (fotogramas 50-78), Chica a la izquierda (72-100) y Freddy termina delante (130-152). Pares
	 * {fotograma, paneo}; entre ellos se interpola. */
	private static final float[][] GUION_PANEO = { { 1, 0.5f }, { 34, 0.5f }, { 46, 1f }, { 74, 1f },
			{ 84, 0f }, { 106, 0f }, { 120, 0.56f }, { 160, 0.56f } };
	/** Tiempo que manda el raton despues de moverse antes de devolver la mirada al guion. */
	private static final long RATON_MANDA_NANOS = 1_800_000_000L;

	/** Arranca la cinematica sobre lo que haya en pantalla. alTerminar se ejecuta una sola vez en el EDT. */
	public void iniciar(VistaPrincipal vistaPrincipal, Runnable alTerminarCinematica) {
		this.vp = vistaPrincipal;
		this.alTerminar = alTerminarCinematica;
		estatica = CargarGifs.cargarFresco("/backgrounds/static.gif", CargarImagenes.getEstaticaRespaldo());

		panel = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				pintar((Graphics2D) g, getWidth(), getHeight());
			}
		};
		panel.setOpaque(false);
		panel.setBounds(0, 0, vp.getLayeredPane().getWidth(), vp.getLayeredPane().getHeight());
		panel.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(
				new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "invisible"));
		MouseAdapter raton = new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				if (panel.getWidth() > 0) {
					paneoObjetivo = Math.max(0f, Math.min(1f, (float) e.getX() / panel.getWidth()));
					ultimoMovimientoRatonNanos = System.nanoTime();
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				mouseMoved(e);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				saltar();
			}
		};
		panel.addMouseListener(raton);
		panel.addMouseMotionListener(raton);
		panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "saltar");
		panel.getActionMap().put("saltar", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				saltar();
			}
		});
		// Capa por encima de cualquier contenido (tableta u oficina): no toca los paneles del juego.
		vp.getLayeredPane().add(panel, JLayeredPane.POPUP_LAYER);
		panel.requestFocusInWindow();

		audio = new Sonido(RUTA_AUDIO);
		audio.play();
		inicioNanos = System.nanoTime();

		cargador = new Thread(this::cargarFotogramas, "cinematica-rendirse");
		cargador.setDaemon(true);
		cargador.start();

		timer = new Timer(INTERVALO_REPINTADO_MS, e -> {
			int t = transcurridoMs();
			if (t >= FIN_TOTAL_MS) {
				terminar();
				return;
			}
			fotogramaPedido = fotogramaEn(t);
			boolean mandaRaton = System.nanoTime() - ultimoMovimientoRatonNanos < RATON_MANDA_NANOS;
			float objetivo = mandaRaton ? paneoObjetivo : paneoGuion(t);
			paneo += (objetivo - paneo) * (mandaRaton ? 0.06f : 0.09f);   // giro lento, como dentro de una cabeza pesada
			panel.repaint();
		});
		timer.start();
	}

	private int transcurridoMs() {
		return (int) ((System.nanoTime() - inicioNanos) / 1_000_000L);
	}

	private static float paneoGuion(int t) {
		float f = 1f + (t - INICIO_FOTOGRAMAS_MS) * FPS / 1000f;
		if (f <= GUION_PANEO[0][0]) {
			return GUION_PANEO[0][1];
		}
		for (int i = 1; i < GUION_PANEO.length; i++) {
			if (f <= GUION_PANEO[i][0]) {
				float u = (f - GUION_PANEO[i - 1][0]) / (GUION_PANEO[i][0] - GUION_PANEO[i - 1][0]);
				u = u * u * (3f - 2f * u);
				return GUION_PANEO[i - 1][1] + (GUION_PANEO[i][1] - GUION_PANEO[i - 1][1]) * u;
			}
		}
		return GUION_PANEO[GUION_PANEO.length - 1][1];
	}

	private static int fotogramaEn(int t) {
		int f = 1 + (t - INICIO_FOTOGRAMAS_MS) * FPS / 1000;
		return Math.max(1, Math.min(TOTAL_FOTOGRAMAS, f));
	}

	/** Hilo de carga: decodifica por delante del fotograma actual y suelta los que ya pasaron. */
	private void cargarFotogramas() {
		while (!terminado) {
			int actual = fotogramaPedido;
			for (Integer clave : cache.keySet()) {
				if (clave < actual - 1) {
					cache.remove(clave);
				}
			}
			int siguiente = -1;
			for (int f = actual; f <= Math.min(TOTAL_FOTOGRAMAS, actual + FOTOGRAMAS_POR_DELANTE); f++) {
				if (!cache.containsKey(f)) {
					siguiente = f;
					break;
				}
			}
			if (siguiente < 0) {
				try {
					Thread.sleep(8);
				} catch (InterruptedException e) {
					return;
				}
				continue;
			}
			try (InputStream in = CinematicaRendirse.class.getResourceAsStream(String.format(RUTA_FOTOGRAMAS, siguiente))) {
				BufferedImage img = in == null ? null : ImageIO.read(in);
				cache.put(siguiente, img != null ? img : new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
			} catch (Exception e) {
				cache.put(siguiente, new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
			}
		}
	}

	private void pintar(Graphics2D g, int w, int h) {
		int t = transcurridoMs();
		if (t < FIN_APAGON_MS) {
			// La oficina (o la tableta) sigue debajo y se apaga a tirones: oscuridad creciente con parpadeos.
			float base = (float) t / FIN_APAGON_MS;
			float alfa = random.nextFloat() < 0.18f ? 0.15f : Math.min(1f, base * base * 1.1f);
			g.setColor(new Color(0f, 0f, 0f, Math.min(1f, alfa)));
			g.fillRect(0, 0, w, h);
			return;
		}
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, w, h);
		if (t < INICIO_FOTOGRAMAS_MS) {
			return;
		}
		if (t < FIN_FOTOGRAMAS_MS) {
			pintarPanorama(g, w, h, t);
		} else if (t < FIN_ESTATICA_MS) {
			if (estatica != null) {
				g.drawImage(estatica.getImage(), 0, 0, w, h, panel);
			}
		} else {
			pintarFrase(g, w, h);
		}
		if (t > 1000 && t < FIN_ESTATICA_MS) {
			pintarAyudaSalto(g, w, h);
		}
	}

	private void pintarPanorama(Graphics2D g, int w, int h, int t) {
		int f = fotogramaEn(t);
		BufferedImage img = null;
		for (int k = f; k >= Math.max(1, f - 3) && img == null; k--) {
			img = cache.get(k);   // si el hilo va justo, se repite el anterior en vez de dejar un hueco negro
		}
		if (img != null && img.getWidth() > 1) {
			// El panorama ocupa todo el alto; el ancho sobrante se recorre con el raton.
			double escala = (double) h / img.getHeight();
			int anchoEscalado = (int) Math.round(img.getWidth() * escala);
			int desplazamiento = (int) ((anchoEscalado - w) * paneo);
			// Temblor muy leve de la cabeza al respirar dentro del traje.
			int temblorY = (int) Math.round(Math.sin(t / 700.0) * h * 0.004);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(img, -desplazamiento, temblorY, anchoEscalado, h, panel);
		}
		// Lineas de interferencia sueltas, como en las alucinaciones de FNAF 2.
		if (random.nextFloat() < 0.35f) {
			g.setColor(new Color(1f, 1f, 1f, 0.05f + random.nextFloat() * 0.06f));
			for (int i = 0; i < 1 + random.nextInt(3); i++) {
				g.fillRect(0, random.nextInt(Math.max(1, h)), w, 1 + random.nextInt(3));
			}
		}
		g.drawImage(mascara(w, h), 0, 0, panel);
		int dentro = t - INICIO_FOTOGRAMAS_MS;
		if (dentro < FUNDIDO_ENTRADA_MS) {
			g.setColor(new Color(0f, 0f, 0f, 1f - (float) dentro / FUNDIDO_ENTRADA_MS));
			g.fillRect(0, 0, w, h);
		}
	}

	/** Bordes de la cabeza del traje: oscuridad alrededor de dos aberturas de ojos que se solapan. Se genera una vez. */
	private BufferedImage mascara(int w, int h) {
		if (mascara != null && mascara.getWidth() == w && mascara.getHeight() == h) {
			return mascara;
		}
		mascara = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = mascara.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(new Color(4, 3, 3));
		g.fillRect(0, 0, w, h);
		Area ojos = new Area(new Ellipse2D.Double(w * 0.02, h * 0.06, w * 0.56, h * 0.90));
		ojos.add(new Area(new Ellipse2D.Double(w * 0.42, h * 0.06, w * 0.56, h * 0.90)));
		g.setComposite(AlphaComposite.Clear);
		g.fill(ojos);
		g.setComposite(AlphaComposite.SrcOver);
		// Borde suave hacia adentro: el interior de la cabeza se funde con la imagen.
		float r = Math.max(w, h) * 0.62f;
		g.setPaint(new RadialGradientPaint(w / 2f, h / 2f, r, new float[] { 0f, 0.55f, 1f },
				new Color[] { new Color(0, 0, 0, 0), new Color(0, 0, 0, 40), new Color(0, 0, 0, 235) }));
		g.fillRect(0, 0, w, h);
		g.dispose();
		return mascara;
	}

	private void pintarFrase(Graphics2D g, int w, int h) {
		boolean ingles = PreferenciasJuego.idiomaSeleccionado == Idioma.INGLES;
		String frase = ingles ? "you're part of the show now" : "ahora eres parte del espectáculo";
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, Math.max(14, h / 40)));
		g.setColor(new Color(200, 200, 200));
		g.drawString(frase, w / 60, h / 22);
	}

	private void pintarAyudaSalto(Graphics2D g, int w, int h) {
		boolean ingles = PreferenciasJuego.idiomaSeleccionado == Idioma.INGLES;
		String texto = ingles ? "Click or ESC to skip" : "Clic o ESC para saltar";
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setFont(Fuentes.obtener(Math.max(12, h / 55)));
		FontMetrics fm = g.getFontMetrics();
		g.setColor(new Color(1f, 1f, 1f, 0.45f));
		g.drawString(texto, w - fm.stringWidth(texto) - w / 50, h - h / 30);
	}

	private void saltar() {
		if (transcurridoMs() >= SALTO_PERMITIDO_DESDE_MS) {
			terminar();
		}
	}

	/** Unico cierre: detiene audio, timer e hilo, suelta los fotogramas y entrega el control (una sola vez). */
	private void terminar() {
		if (terminado) {
			return;
		}
		terminado = true;
		if (timer != null) {
			timer.stop();
		}
		if (cargador != null) {
			cargador.interrupt();
		}
		if (audio != null) {
			// Medido con el arnes: Clip.stop() de Java 8 (AbstractDataLine.stop) llego a bloquear ~2 s
			// cuando el clip estaba sonando su ultimo tramo; en el EDT eso congelaba la pantalla antes
			// de "Cargando...". Se detiene y se cierra fuera del EDT.
			final Sonido audioACerrar = audio;
			audio = null;
			Thread cierre = new Thread(() -> {
				audioACerrar.stop();
				audioACerrar.cerrar();
			}, "cinematica-rendirse-audio");
			cierre.setDaemon(true);
			cierre.start();
		}
		cache.clear();
		mascara = null;
		vp.getLayeredPane().remove(panel);
		// Debajo queda negro hasta que alTerminar muestre la pantalla de carga del reinicio.
		JPanel negro = new JPanel();
		negro.setBackground(Color.BLACK);
		vp.setContenido(negro);
		vp.getLayeredPane().repaint();
		alTerminar.run();
	}
}
