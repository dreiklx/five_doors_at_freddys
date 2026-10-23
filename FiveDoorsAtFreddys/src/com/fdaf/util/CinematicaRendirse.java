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
	private static final int FOTOGRAMAS_POR_DELANTE = 8;
	private static final int INTERVALO_REPINTADO_MS = 33;
	/** Fotogramas que se decodifican ANTES de empezar (preparacion con la barra retro, §1.60). Pocos a proposito: el
	 * primero no se muestra hasta 2.2 s despues (apagon + negro) y el hilo continuo tiene ese margen; con 24 la
	 * preparacion medida llego a 1-3 s con la CPU cargada. */
	private static final int FOTOGRAMAS_PRECARGA = 8;
	/** Si la preparacion termina antes, la barra se ve llena este tiempo minimo: evita un parpadeo, no alarga una
	 * carga real (con recursos listos la preparacion entera dura ~0.4-0.6 s, medido). */
	private static final int BARRA_MINIMA_MS = 260;
	/** Suavizado de la mirada (constantes de tiempo, s) y velocidad maxima de giro (anchos de panorama/s), §1.61. */
	private static final float TAU_RATON = 0.08f;
	private static final float TAU_GUION = 0.28f;
	private static final float VELOCIDAD_MAX_RATON = 2.4f;
	private static final float VELOCIDAD_MAX_GUION = 1.4f;

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
	private long ultimoTickNanos;

	// Preparacion previa con la barra retro (§1.60): el hilo "cinematica-rendirse-prep" decodifica los primeros
	// fotogramas y abre el audio; el Timer arranca la linea de tiempo cuando termina.
	private volatile boolean preparando = true;
	private volatile int precargados = 0;
	private volatile boolean audioListo = false;
	private volatile boolean preparacionLista = false;
	private long inicioPreparacionNanos;
	private long finPreparacionNanos;
	private OmisionMantenida omision;

	/** Mientras el jugador no mueva el raton, la mirada sigue la accion (escena v2, §1.61): Bonnie (derecha) gira la
	 * cabeza a tirones en los fotogramas 18-40, Chica (izquierda) en 44-64; vuelven los espasmos de los dos y Freddy
	 * termina delante (130-152). Con el paneo al centro los dos quedan a la vista en los bordes. Pares {fotograma,
	 * paneo}; entre ellos se interpola. */
	private static final float[][] GUION_PANEO = { { 1, 0.5f }, { 14, 0.5f }, { 22, 0.82f }, { 42, 0.82f },
			{ 48, 0.18f }, { 68, 0.18f }, { 78, 0.5f }, { 90, 0.62f }, { 98, 0.62f }, { 102, 0.38f }, { 110, 0.38f },
			{ 118, 0.52f }, { 160, 0.52f } };
	/** Fotogramas con un tic de Bonnie o de Chica (direct_surrender.py): ahi la imagen tiene una rafaga breve de
	 * distorsion por franjas que nunca tapa el centro de la cara. */
	private static final int[] FOTOGRAMAS_TIC = { 70, 82, 92, 104, 110, 118, 121, 131, 140, 143 };
	/** Tiempo que manda el raton despues de moverse antes de devolver la mirada al guion. */
	private static final long RATON_MANDA_NANOS = 1_400_000_000L;

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
					// el recorrido completo se alcanza antes del borde de la ventana (8 % de margen por lado)
					float x = (float) e.getX() / panel.getWidth();
					paneoObjetivo = Math.max(0f, Math.min(1f, (x - 0.08f) / 0.84f));
					ultimoMovimientoRatonNanos = System.nanoTime();
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				mouseMoved(e);
			}
		};
		panel.addMouseListener(raton);
		panel.addMouseMotionListener(raton);
		// Omitir: mantener ESC ~1.2 s (o el raton sobre el indicador). Un toque no hace nada (§1.62).
		omision = OmisionMantenida.instalar(panel, this::terminar);
		// Capa por encima de cualquier contenido (tableta u oficina): no toca los paneles del juego.
		vp.getLayeredPane().add(panel, JLayeredPane.POPUP_LAYER);
		panel.requestFocusInWindow();

		inicioPreparacionNanos = System.nanoTime();
		ultimoTickNanos = inicioPreparacionNanos;
		Thread prep = new Thread(this::preparar, "cinematica-rendirse-prep");
		prep.setDaemon(true);
		prep.start();

		timer = new Timer(INTERVALO_REPINTADO_MS, e -> {
			long ahora = System.nanoTime();
			float dt = Math.min(0.1f, (ahora - ultimoTickNanos) / 1e9f);
			ultimoTickNanos = ahora;
			if (omision.actualizar(dt) || terminado) {
				return;
			}
			if (preparando) {
				if (preparacionLista && (ahora - inicioPreparacionNanos) / 1_000_000L >= BARRA_MINIMA_MS) {
					arrancarLineaDeTiempo();
				}
				panel.repaint();
				return;
			}
			int t = transcurridoMs();
			if (t >= FIN_TOTAL_MS) {
				terminar();
				return;
			}
			fotogramaPedido = fotogramaEn(t);
			moverMirada(t, dt, ahora);
			panel.repaint();
		});
		timer.start();
	}

	/** Hilo de preparacion: primeros fotogramas + apertura del audio. Nunca en el EDT. */
	private void preparar() {
		for (int f = 1; f <= FOTOGRAMAS_PRECARGA && !terminado; f++) {
			cache.put(f, decodificar(f));
			precargados = f;
		}
		Sonido s = new Sonido(RUTA_AUDIO);
		if (terminado) {
			s.cerrar();
			return;
		}
		audio = s;
		audioListo = true;
		finPreparacionNanos = System.nanoTime();
		preparacionLista = true;
	}

	/** Fraccion real de la preparacion: fotogramas decodificados y audio abierto (el audio pesa como 4 fotogramas). */
	private float progresoPreparacion() {
		return (precargados + (audioListo ? 4f : 0f)) / (FOTOGRAMAS_PRECARGA + 4f);
	}

	private void arrancarLineaDeTiempo() {
		preparando = false;
		log("preparacion " + (finPreparacionNanos - inicioPreparacionNanos) / 1_000_000L + " ms");
		audio.play();
		inicioNanos = System.nanoTime();
		cargador = new Thread(this::cargarFotogramas, "cinematica-rendirse");
		cargador.setDaemon(true);
		cargador.start();
	}

	private static void log(String mensaje) {
		System.out.println("[RENDIRSE] " + mensaje);
	}

	/** Mirada (§1.61): suavizado exponencial por tiempo real (independiente de los fps) con velocidad maxima. El
	 * raton responde rapido (tau 0.07 s); el guion, mas pausado. Al dejar de mover el raton, el guion retoma desde
	 * donde quedo la mirada, sin salto. El limite de giro es el propio panorama (paneo en [0, 1]). */
	private void moverMirada(int t, float dt, long ahora) {
		boolean mandaRaton = ahora - ultimoMovimientoRatonNanos < RATON_MANDA_NANOS;
		float objetivo = mandaRaton ? paneoObjetivo : paneoGuion(t);
		float tau = mandaRaton ? TAU_RATON : TAU_GUION;
		float vmax = mandaRaton ? VELOCIDAD_MAX_RATON : VELOCIDAD_MAX_GUION;
		float paso = (objetivo - paneo) * (1f - (float) Math.exp(-dt / tau));
		float limite = vmax * dt;
		paneo += Math.max(-limite, Math.min(limite, paso));
		paneo = Math.max(0f, Math.min(1f, paneo));
	}

	private int transcurridoMs() {
		return preparando ? 0 : (int) ((System.nanoTime() - inicioNanos) / 1_000_000L);
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
			cache.put(siguiente, decodificar(siguiente));
		}
	}

	private static BufferedImage decodificar(int fotograma) {
		try (InputStream in = CinematicaRendirse.class.getResourceAsStream(String.format(RUTA_FOTOGRAMAS, fotograma))) {
			BufferedImage img = in == null ? null : ImageIO.read(in);
			return img != null ? img : new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		} catch (Exception e) {
			return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		}
	}

	private void pintar(Graphics2D g, int w, int h) {
		boolean ingles = PreferenciasJuego.idiomaSeleccionado == Idioma.INGLES;
		if (preparando) {
			// La oficina queda atenuada debajo y la barra retro muestra la preparacion real.
			g.setColor(new Color(0f, 0f, 0f, 0.45f));
			g.fillRect(0, 0, w, h);
			BarraRetro.pintar(g, w / 2, h / 2, w, progresoPreparacion());
			omision.pintar(g, w, h, ingles);
			return;
		}
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
		if (t < FIN_ESTATICA_MS) {
			omision.pintar(g, w, h, ingles);
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
			pintarDistorsion(g, img, w, h, f, -desplazamiento, temblorY, anchoEscalado, escala);
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

	/** Rafaga de distorsion (~150 ms) en cada tic de Bonnie o Chica: 2-3 franjas horizontales desplazadas, siempre
	 * fuera de la franja central donde quedan las caras (35 %-62 % del alto), mas un velo rojo muy tenue. */
	private void pintarDistorsion(Graphics2D g, BufferedImage img, int w, int h, int f, int x0, int y0, int anchoEscalado, double escala) {
		boolean enTic = false;
		for (int tic : FOTOGRAMAS_TIC) {
			if (f >= tic && f <= tic + 2) {
				enTic = true;
				break;
			}
		}
		if (!enTic) {
			return;
		}
		int franjas = 2 + random.nextInt(2);
		for (int i = 0; i < franjas; i++) {
			int alto = h / 30 + random.nextInt(h / 18);
			int y = random.nextBoolean() ? random.nextInt(Math.max(1, (int) (h * 0.35f) - alto)) : (int) (h * 0.62f) + random.nextInt(Math.max(1, (int) (h * 0.38f) - alto));
			int dx = (random.nextBoolean() ? 1 : -1) * (w / 90 + random.nextInt(w / 45));
			int sy0 = (int) ((y - y0) / escala);
			int sy1 = (int) ((y + alto - y0) / escala);
			g.drawImage(img, x0 + dx, y, x0 + dx + anchoEscalado, y + alto, 0, Math.max(0, sy0), img.getWidth(), Math.min(img.getHeight(), sy1), panel);
		}
		g.setColor(new Color(0.6f, 0f, 0f, 0.07f));
		g.fillRect(0, 0, w, h);
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
