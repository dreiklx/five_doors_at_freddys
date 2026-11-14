package com.fdaf.mvc.views.frames.pnl;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;
import javax.swing.Timer;

import javafx.embed.swing.JFXPanel;

import com.fdaf.mvc.views.frames.VistaPrincipal;
import com.fdaf.mvc.views.multimedia.Sonido;
import com.fdaf.util.EscalarVista;
import com.fdaf.util.Fuentes;

public class PnlWin extends JPanel {

	private Image imagenActual;
	private JFXPanel panelVideo;
	private JButton btnJugarDeNuevo;
	private JButton btnSalir;
	private JLabel lblFlecha;
	private JLabel lblCargando;

	// Video de victoria (CLAUDE.md §1.81): fotogramas JPG + audio WAV extraidos de videos/victory.mp4 y dibujados con
	// Java2D. Antes lo reproducia JavaFX Media (GStreamer + decodificadores H.264/AAC de Windows): fallaba con
	// ERROR_MEDIA_INVALID 1 de cada 5-8 veces en la maquina de desarrollo y en todas las victorias en la PC de un
	// probador. Asi no depende de ningun codec del sistema: solo ImageIO y javax.sound, como la cinematica de
	// Rendirse. panelVideo (JFXPanel) se conserva solo porque EscalarVista.adaptarWin (congelada) lo dimensiona.
	private static final String RUTA_FOTOGRAMAS_VICTORIA = "/videos/victory/victory_%04d.jpg";
	private static final String RUTA_AUDIO_VICTORIA = "cinematics/victory_video_audio.wav";
	private static final int FPS_VICTORIA = 30;
	private static final int FUNDIDO_ENTRADA_MS = 1000;
	private static final int FOTOGRAMAS_PRECARGA = 6;
	private static final int FOTOGRAMAS_POR_DELANTE = 8;
	private static final int INTERVALO_REPINTADO_MS = 15;

	private final ConcurrentHashMap<Integer, BufferedImage> cacheFotogramas = new ConcurrentHashMap<>();
	private final AtomicBoolean videoTerminado = new AtomicBoolean(false);
	private volatile int fotogramaPedido = 1;
	private volatile int totalFotogramas = 0;
	private volatile boolean videoEnCurso = false;
	private Timer timerVideo;
	private Thread cargadorFotogramas;
	private Sonido audioVideo;
	private long inicioVideoNanos;
	private long duracionVideoMs;
	private Runnable alTerminarVideo;

	public PnlWin() {
		setPreferredSize(new Dimension(1600, 900));
		setBackground(Color.BLACK);
		setOpaque(true);
		setLayout(null);

		panelVideo = new JFXPanel();
		panelVideo.setBounds(0, 0, 1600, 900);
		add(panelVideo);

		Font fuenteBoton = Fuentes.obtener(50);

		// Geometría horizontal tipo PnlConfirmacion: 2 botones lado a
		// lado, misma Y, mismo ancho.
		btnJugarDeNuevo = new JButton("Volver a jugar");
		estilizarBoton(btnJugarDeNuevo, fuenteBoton);
		btnJugarDeNuevo.setBounds(260, 650, 520, 80);
		btnJugarDeNuevo.setVisible(false);
		add(btnJugarDeNuevo);

		btnSalir = new JButton("Salir del juego");
		estilizarBoton(btnSalir, fuenteBoton);
		btnSalir.setBounds(820, 650, 520, 80);
		btnSalir.setVisible(false);
		add(btnSalir);

		FontMetrics fmFlecha = getFontMetrics(fuenteBoton);
		int anchoFlecha = fmFlecha.stringWidth(">>") + 10;
		int altoFlecha = fmFlecha.getHeight();

		lblFlecha = new JLabel(">>");
		lblFlecha.setForeground(Color.WHITE);
		lblFlecha.setFont(fuenteBoton);
		lblFlecha.setBounds(0, 0, anchoFlecha, altoFlecha);
		lblFlecha.setVisible(false);
		add(lblFlecha);

		// Mensaje mostrado solo en la transicion Noche 5 -> Five Doors
		// Escape, mientras el proceso LibGDX arranca en segundo plano
		// (placeholder honesto hasta que exista el efecto de "glitch"
		// real -- ver memoria de Claude "project-libgdx-office-spawn-exit-design").
		lblCargando = new JLabel("", SwingConstants.CENTER);
		lblCargando.setForeground(Color.WHITE);
		lblCargando.setFont(Fuentes.obtener(36));
		lblCargando.setBounds(0, 650, 1600, 80);
		lblCargando.setVisible(false);
		add(lblCargando);
	}

	private void estilizarBoton(JButton boton, Font fuente) {
		boton.setFocusPainted(false);
		boton.setBorderPainted(false);
		boton.setContentAreaFilled(false);
		boton.setOpaque(false);
		boton.setBorder(null);
		boton.setFont(fuente);
		boton.setForeground(Color.WHITE);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (imagenActual != null) {
			g.drawImage(imagenActual, 0, 0, getWidth(), getHeight(), this);
		}
		if (videoEnCurso) {
			pintarFotogramaVideo((Graphics2D) g, getWidth(), getHeight());
		}
	}

	public void mostrarFondo(ImageIcon icono) {
		panelVideo.setVisible(false);
		this.imagenActual = (icono != null) ? icono.getImage() : null;
		repaint();
	}

	/**
	 * Reproduce el video de victoria (las mismas imagenes y el mismo sonido de videos/victory.mp4) y llama a
	 * alTerminar UNA sola vez en el EDT al acabar. Si faltara algun recurso, se salta el video y la partida sigue.
	 */
	public void reproducirVideoVictoria(Runnable alTerminar) {
		if (alTerminarVideo != null) {
			return;   // una sola reproduccion por pantalla: nunca dos transiciones encadenadas
		}
		alTerminarVideo = alTerminar;
		panelVideo.setVisible(false);
		Thread prep = new Thread(this::prepararVideo, "video-victoria-prep");
		prep.setDaemon(true);
		prep.start();
	}

	/** Fuera del EDT: cuenta los fotogramas, decodifica los primeros y abre el audio; despues arranca en el EDT. */
	private void prepararVideo() {
		int total = 0;
		while (PnlWin.class.getResource(String.format(RUTA_FOTOGRAMAS_VICTORIA, total + 1)) != null) {
			total++;
		}
		totalFotogramas = total;
		if (total == 0) {
			System.out.println("[VIDEO] no se encontraron los fotogramas de victoria: se omite el video");
			SwingUtilities.invokeLater(this::terminarVideo);
			return;
		}
		for (int f = 1; f <= Math.min(FOTOGRAMAS_PRECARGA, total) && !videoTerminado.get(); f++) {
			cacheFotogramas.put(f, decodificarFotograma(f));
		}
		Sonido audio = new Sonido(RUTA_AUDIO_VICTORIA);
		long duracion = Math.max(total * 1000L / FPS_VICTORIA, audio.getDuracionMs());
		SwingUtilities.invokeLater(() -> arrancarVideo(audio, duracion));
	}

	private void arrancarVideo(Sonido audio, long duracionMs) {
		if (videoTerminado.get()) {
			audio.cerrar();
			return;
		}
		audioVideo = audio;
		duracionVideoMs = duracionMs;
		System.out.println("[VIDEO] victoria: " + totalFotogramas + " fotogramas, " + duracionMs + " ms");
		videoEnCurso = true;
		audio.play();
		inicioVideoNanos = System.nanoTime();
		cargadorFotogramas = new Thread(this::cargarFotogramas, "video-victoria");
		cargadorFotogramas.setDaemon(true);
		cargadorFotogramas.start();
		timerVideo = new Timer(INTERVALO_REPINTADO_MS, e -> {
			long t = transcurridoVideoMs();
			if (t >= duracionVideoMs) {
				terminarVideo();
				return;
			}
			fotogramaPedido = fotogramaEn(t);
			repaint();
		});
		timerVideo.start();
	}

	private long transcurridoVideoMs() {
		return (System.nanoTime() - inicioVideoNanos) / 1_000_000L;
	}

	private int fotogramaEn(long t) {
		int f = 1 + (int) (t * FPS_VICTORIA / 1000L);
		return Math.max(1, Math.min(totalFotogramas, f));
	}

	/** Hilo de carga: decodifica por delante del fotograma actual y suelta los que ya pasaron (nunca en el EDT). */
	private void cargarFotogramas() {
		while (!videoTerminado.get()) {
			int actual = fotogramaPedido;
			for (Integer clave : cacheFotogramas.keySet()) {
				if (clave < actual - 1) {
					cacheFotogramas.remove(clave);
				}
			}
			int siguiente = -1;
			for (int f = actual; f <= Math.min(totalFotogramas, actual + FOTOGRAMAS_POR_DELANTE); f++) {
				if (!cacheFotogramas.containsKey(f)) {
					siguiente = f;
					break;
				}
			}
			if (siguiente < 0) {
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					return;
				}
				continue;
			}
			cacheFotogramas.put(siguiente, decodificarFotograma(siguiente));
		}
	}

	private static BufferedImage decodificarFotograma(int fotograma) {
		try (InputStream in = PnlWin.class.getResourceAsStream(String.format(RUTA_FOTOGRAMAS_VICTORIA, fotograma))) {
			BufferedImage img = in == null ? null : ImageIO.read(in);
			return img != null ? img : new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		} catch (Exception e) {
			return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		}
	}

	private void pintarFotogramaVideo(Graphics2D g, int w, int h) {
		long t = transcurridoVideoMs();
		int f = fotogramaEn(t);
		BufferedImage img = null;
		for (int k = f; k >= Math.max(1, f - 4) && img == null; k--) {
			img = cacheFotogramas.get(k);   // si la carga va justa, se repite el anterior en vez de un hueco negro
		}
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, w, h);
		if (img != null && img.getWidth() > 1) {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(img, 0, 0, w, h, this);
		}
		if (t < FUNDIDO_ENTRADA_MS) {
			// el mismo fundido de entrada de 1 s que tenia el MediaView
			g.setColor(new Color(0f, 0f, 0f, 1f - (float) t / FUNDIDO_ENTRADA_MS));
			g.fillRect(0, 0, w, h);
		}
	}

	/** Corta el video sin continuar el flujo de victoria (arneses de prueba). Despues no se puede volver a iniciar. */
	public void detenerVideo() {
		alTerminarVideo = () -> { };
		terminarVideo();
	}

	/** Unico cierre del video: timer, hilo y audio liberados; alTerminar se entrega una sola vez. */
	private void terminarVideo() {
		if (!videoTerminado.compareAndSet(false, true)) {
			return;
		}
		videoEnCurso = false;
		if (timerVideo != null) {
			timerVideo.stop();
		}
		if (cargadorFotogramas != null) {
			cargadorFotogramas.interrupt();
		}
		if (audioVideo != null) {
			// Clip.stop() de Java 8 puede bloquear ~2 s al final del clip (CLAUDE.md §1.59): se cierra fuera del EDT.
			final Sonido audioACerrar = audioVideo;
			audioVideo = null;
			Thread cierre = new Thread(audioACerrar::cerrar, "video-victoria-audio");
			cierre.setDaemon(true);
			cierre.start();
		}
		cacheFotogramas.clear();
		repaint();
		System.out.println("[VIDEO] victoria terminada");
		if (alTerminarVideo != null) {
			alTerminarVideo.run();
		}
	}

	public void init(VistaPrincipal vp, PnlWin pantalla) {
		EscalarVista.adaptarWin(vp, pantalla);
		this.revalidate();
		this.repaint();
	}

	public JFXPanel getPanelVideo() { return panelVideo; }
	public void setPanelVideo(JFXPanel panelVideo) { this.panelVideo = panelVideo; }
	public JButton getBtnJugarDeNuevo() { return btnJugarDeNuevo; }
	public void setBtnJugarDeNuevo(JButton btnJugarDeNuevo) { this.btnJugarDeNuevo = btnJugarDeNuevo; }
	public JButton getBtnSalir() { return btnSalir; }
	public void setBtnSalir(JButton btnSalir) { this.btnSalir = btnSalir; }
	public JLabel getLblFlecha() { return lblFlecha; }
	public void setLblFlecha(JLabel lblFlecha) { this.lblFlecha = lblFlecha; }
	public JLabel getLblCargando() { return lblCargando; }
	public void setLblCargando(JLabel lblCargando) { this.lblCargando = lblCargando; }
}