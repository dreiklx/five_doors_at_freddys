package com.fdaf.mvc.views.frames.pnl;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import com.fdaf.mvc.views.multimedia.Sonido;
import com.fdaf.util.EstrellaLogro;
import com.fdaf.util.Fuentes;
import com.fdaf.util.InteraccionBoton;
import com.fdaf.util.TextosInfo;

// Agradecimiento de NYDER tras cada victoria de Five Doors Escape (CLAUDE.md
// §1.67). Composición centrada sobre negro: logo de NYDER, (solo la primera
// vez) la estrella que se acaba de desbloquear, el título, el mensaje del
// equipo, la firma y "Continuar". Entra con un fundido desde negro y cada
// bloque aparece un poco después del anterior; de fondo, unas pocas chispas
// doradas y violetas que suben despacio (la celebración discreta). Un solo
// Timer de ~30 fps que se detiene al cerrar. Continuar (botón, Enter, Espacio
// o ESC) solo responde cuando el botón ya se ve, para que una tecla que venía
// apretada desde Escape no la cierre sin leerla. Lo dibuja todo en
// paintComponent a partir del tamaño real del panel (no pasa por EscalarVista).
public class PnlAgradecimiento extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final Color ACENTO = new Color(98, 96, 255); // violeta de NYDER, el mismo de PnlInfo
	private static final Color TEXTO = new Color(222, 222, 230);
	private static final Color ORO_TITULO = new Color(255, 226, 150);

	// línea de tiempo (ms desde que se muestra)
	private static final int FUNDIDO_ENTRADA_MS = 700;
	private static final int LOGO_MS = 250;
	private static final int ESTRELLA_MS = 900;
	private static final int TITULO_MS = 900;
	private static final int TEXTO_MS = 1400;
	private static final int BOTON_MS = 2600;
	private static final int FUNDIDO_SALIDA_MS = 550;
	private static final int APARICION_MS = 600;

	private final Image logo;
	private final boolean primeraVez;
	private final Runnable alContinuar;
	private final JButton btnContinuar = new JButton();
	private final List<float[]> chispas = new ArrayList<>(); // x, y, velocidad, fase, tamaño, violeta(0/1)
	private final Random azar = new Random(1987);
	private Timer timer;
	private long inicio = -1;
	private long inicioSalida = -1;
	private boolean sonidoEstrellaHecho;
	private Sonido sonidoEstrella;

	public PnlAgradecimiento(Image logoNyder, boolean primeraVez, Runnable alContinuar) {
		this.logo = logoNyder;
		this.primeraVez = primeraVez;
		this.alContinuar = alContinuar;
		setPreferredSize(new Dimension(1600, 900));
		setBackground(Color.BLACK);
		setOpaque(true);
		setLayout(null);

		btnContinuar.setText(TextosInfo.agradecimientoContinuar());
		btnContinuar.setFont(Fuentes.obtener(34));
		btnContinuar.setForeground(TEXTO);
		btnContinuar.setHorizontalAlignment(SwingConstants.CENTER);
		btnContinuar.setFocusPainted(false);
		btnContinuar.setBorderPainted(false);
		btnContinuar.setContentAreaFilled(false);
		btnContinuar.setOpaque(false);
		btnContinuar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnContinuar.setVisible(false);
		btnContinuar.addActionListener(e -> continuar());
		btnContinuar.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				btnContinuar.setForeground(ACENTO);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				btnContinuar.setForeground(TEXTO);
			}
		});
		InteraccionBoton.aplicarSonidoHover(btnContinuar, null);
		add(btnContinuar);

		AbstractAction accion = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				continuar();
			}
		};
		for (int tecla : new int[] { KeyEvent.VK_ENTER, KeyEvent.VK_SPACE, KeyEvent.VK_ESCAPE }) {
			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(tecla, 0), "agradecimientoContinuar");
		}
		getActionMap().put("agradecimientoContinuar", accion);

		for (int i = 0; i < 46; i++) {
			chispas.add(new float[] { azar.nextFloat(), azar.nextFloat(), 0.012f + azar.nextFloat() * 0.03f,
					azar.nextFloat() * 6.28f, 1.2f + azar.nextFloat() * 2.4f, azar.nextFloat() < 0.3f ? 1f : 0f });
		}
		if (primeraVez) {
			// el mismo destello de la estrella de NYDER (§1.65), cargado en segundo plano
			Thread carga = new Thread(() -> sonidoEstrella = new Sonido("sfx/nyder_star_sparkle.wav"), "agradecimiento-sonido");
			carga.setDaemon(true);
			carga.start();
		}
	}

	public void iniciar() {
		inicio = System.nanoTime();
		timer = new Timer(33, e -> avanzar());
		timer.start();
	}

	public boolean isPrimeraVez() {
		return primeraVez;
	}

	private int ms() {
		return inicio < 0 ? 0 : (int) ((System.nanoTime() - inicio) / 1_000_000L);
	}

	private void avanzar() {
		int t = ms();
		for (float[] c : chispas) {
			c[1] -= c[2] * 0.033f;
			if (c[1] < -0.02f) {
				c[1] = 1.02f;
				c[0] = azar.nextFloat();
			}
		}
		if (primeraVez && !sonidoEstrellaHecho && t >= ESTRELLA_MS) {
			sonidoEstrellaHecho = true;
			final Sonido s = sonidoEstrella;
			if (s != null) {
				Thread reproducir = new Thread(s::play, "agradecimiento-sonido-play");
				reproducir.setDaemon(true);
				reproducir.start();
			}
		}
		if (!btnContinuar.isVisible() && t >= BOTON_MS && inicioSalida < 0) {
			btnContinuar.setVisible(true);
		}
		if (inicioSalida >= 0 && (System.nanoTime() - inicioSalida) / 1_000_000L >= FUNDIDO_SALIDA_MS) {
			cerrar();
			alContinuar.run();
			return;
		}
		repaint();
	}

	// ESC lo atiende antes la raíz de la ventana (ControllerMenu.manejarEscape),
	// que delega aquí.
	public void continuarDesdeTeclado() {
		continuar();
	}

	private void continuar() {
		if (!btnContinuar.isVisible() || inicioSalida >= 0) {
			return;
		}
		btnContinuar.setVisible(false);
		inicioSalida = System.nanoTime();
	}

	// Detiene la animación; el sonido de la estrella, si sigue, termina solo y
	// se cierra solo (Sonido no reutilizable).
	public void cerrar() {
		if (timer != null) {
			timer.stop();
			timer = null;
		}
	}

	private static float aparicion(int t, int desde) {
		float u = (t - desde) / (float) APARICION_MS;
		u = Math.max(0f, Math.min(1f, u));
		return u * u * (3f - 2f * u);
	}

	@Override
	public void doLayout() {
		int w = getWidth(), h = getHeight();
		FontMetrics fm = getFontMetrics(btnContinuar.getFont());
		int bw = fm.stringWidth(btnContinuar.getText()) + fm.getHeight() * 2;
		int bh = (int) (fm.getHeight() * 1.6);
		int y = yBotonCalculado >= 0 ? yBotonCalculado : (int) (h * 0.875) - bh / 2;
		btnContinuar.setBounds((w - bw) / 2, y, bw, bh);
	}

	@Override
	protected void paintComponent(Graphics g0) {
		super.paintComponent(g0);
		Graphics2D g = (Graphics2D) g0.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		int w = getWidth(), h = getHeight();
		int t = ms();
		float global = Math.min(1f, t / (float) FUNDIDO_ENTRADA_MS);
		if (inicioSalida >= 0) {
			global *= 1f - Math.min(1f, (System.nanoTime() - inicioSalida) / 1_000_000f / FUNDIDO_SALIDA_MS);
		}

		// fondo: un resplandor violeta muy tenue detrás de la composición
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, global));
		float rf = Math.max(w, h) * 0.6f;
		g.setPaint(new RadialGradientPaint(w / 2f, h * 0.42f, rf, new float[] { 0f, 1f },
				new Color[] { new Color(40, 36, 90, 150), new Color(0, 0, 0, 0) }));
		g.fillRect(0, 0, w, h);

		// chispas
		for (float[] c : chispas) {
			float brillo = 0.35f + 0.65f * (0.5f + 0.5f * (float) Math.sin(t / 400f + c[3]));
			float tam = c[4] * h / 900f;
			Color col = c[5] > 0.5f ? new Color(150, 145, 255) : new Color(255, 214, 120);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, global * brillo * 0.55f));
			g.setColor(col);
			float x = c[0] * w, y = c[1] * h;
			g.fill(new java.awt.geom.Ellipse2D.Float(x - tam, y - tam, tam * 2, tam * 2));
		}

		// composición: se mide primero (sin dibujar) para centrarla en vertical
		// junto con el botón, y después se dibuja; el botón va justo debajo de
		// la firma, así nunca se monta sobre el texto aunque esté el bloque de
		// la estrella.
		FontMetrics fmb = getFontMetrics(btnContinuar.getFont());
		int altoBoton = (int) (fmb.getHeight() * 1.6f);
		float hueco = h * 0.03f;
		float alto = componer(g, false, w, h, t, global, 0f);
		float y0 = Math.max(h * 0.04f, (h - alto - hueco - altoBoton) / 2f);
		float fin = componer(g, true, w, h, t, global, y0);
		int yBoton = Math.round(fin + hueco);
		if (yBoton != yBotonCalculado) {
			yBotonCalculado = yBoton;
			javax.swing.SwingUtilities.invokeLater(this::doLayout);
		}
		g.dispose();
	}

	private int yBotonCalculado = -1;

	// Dibuja (o solo mide, si dibujar es false) el bloque central desde y0 y
	// devuelve dónde termina.
	private float componer(Graphics2D g, boolean dibujar, int w, int h, int t, float global, float y0) {
		float y = y0;
		float a = global * aparicion(t, LOGO_MS);
		int ladoLogo = (int) (h * 0.12f);
		if (dibujar && logo != null) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
			g.drawImage(logo, (w - ladoLogo) / 2, (int) y, ladoLogo, ladoLogo, null);
		}
		y += ladoLogo + h * 0.02f;

		if (primeraVez) {
			float radio = h * 0.046f;
			float u = Math.max(0f, Math.min(1f, (t - ESTRELLA_MS) / 700f));
			// entra creciendo con un pequeño rebote y queda con un pulso suave
			float escala = u <= 0f ? 0f : (float) (1 + 0.18 * Math.sin(u * Math.PI) - 0.18 * (1 - u) * (1 - u));
			float pulso = (float) (0.35 + 0.65 * Math.max(0, Math.sin((t - ESTRELLA_MS) / 900.0)) * (u >= 1f ? 0.5 : 1));
			float cy = y + radio;
			g.setFont(Fuentes.obtener(24));
			FontMetrics fme = g.getFontMetrics();
			if (dibujar && escala > 0f) {
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, global * Math.min(1f, u * 2f)));
				EstrellaLogro.pintar(g, w / 2.0, cy, radio * escala, 0.6f + 0.4f * pulso);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, global * aparicion(t, ESTRELLA_MS + 350)));
				g.setColor(ORO_TITULO);
				centrar(g, TextosInfo.agradecimientoEstrella(), w, (int) (cy + radio * 1.15f + fme.getAscent()));
			}
			y += radio * 2.15f + fme.getHeight() + h * 0.018f;
		}

		// título
		g.setFont(Fuentes.obtener(64, Font.BOLD));
		FontMetrics fmt = g.getFontMetrics();
		float at = global * aparicion(t, TITULO_MS);
		y += fmt.getAscent();
		if (dibujar) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, at * 0.35f));
			g.setColor(new Color(255, 200, 90));
			centrar(g, TextosInfo.agradecimientoTitulo(), w + 2, (int) y + 2);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, at));
			g.setColor(ORO_TITULO);
			centrar(g, TextosInfo.agradecimientoTitulo(), w, (int) y);
		}
		y += fmt.getDescent() + h * 0.012f;
		if (dibujar) {
			// línea de acento violeta bajo el título
			g.setColor(ACENTO);
			g.setStroke(new BasicStroke(Math.max(2f, h / 360f)));
			int largo = (int) (fmt.stringWidth(TextosInfo.agradecimientoTitulo()) * 0.5f * at);
			g.drawLine(w / 2 - largo / 2, (int) y, w / 2 + largo / 2, (int) y);
		}
		y += h * 0.032f;

		// párrafos
		g.setFont(Fuentes.obtener(27));
		FontMetrics fmc = g.getFontMetrics();
		int anchoMax = (int) (w * 0.66f);
		String[] parrafos = TextosInfo.agradecimientoParrafos();
		for (int i = 0; i < parrafos.length; i++) {
			if (dibujar) {
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, global * aparicion(t, TEXTO_MS + i * 220)));
				g.setColor(TEXTO);
			}
			for (String linea : ajustar(parrafos[i], fmc, anchoMax)) {
				y += fmc.getAscent();
				if (dibujar) {
					centrar(g, linea, w, (int) y);
				}
				y += fmc.getDescent() + fmc.getLeading() + 2;
			}
			y += fmc.getHeight() * 0.45f;
		}
		// firma
		g.setFont(Fuentes.obtener(30, Font.BOLD));
		FontMetrics fmf = g.getFontMetrics();
		y += fmf.getAscent() + h * 0.005f;
		if (dibujar) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, global * aparicion(t, TEXTO_MS + parrafos.length * 220 + 150)));
			g.setColor(new Color(170, 168, 255));
			centrar(g, TextosInfo.agradecimientoFirma(), w, (int) y);
		}
		return y + fmf.getDescent();
	}

	private static void centrar(Graphics2D g, String texto, int ancho, int baseY) {
		g.drawString(texto, (ancho - g.getFontMetrics().stringWidth(texto)) / 2, baseY);
	}

	private static List<String> ajustar(String texto, FontMetrics fm, int anchoMax) {
		List<String> lineas = new ArrayList<>();
		StringBuilder actual = new StringBuilder();
		for (String palabra : texto.split(" ")) {
			String prueba = actual.length() == 0 ? palabra : actual + " " + palabra;
			if (fm.stringWidth(prueba) > anchoMax && actual.length() > 0) {
				lineas.add(actual.toString());
				actual = new StringBuilder(palabra);
			} else {
				actual = new StringBuilder(prueba);
			}
		}
		if (actual.length() > 0) {
			lineas.add(actual.toString());
		}
		return lineas;
	}
}
