package com.fdaf.mvc.views.frames.pnl;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import com.fdaf.util.CargarImagenes;
import com.fdaf.util.Idioma;
import com.fdaf.util.PreferenciasJuego;

// Pantalla de advertencia inicial -- lo primero que ve el jugador, antes
// del menú. Mismo mecanismo que PnlGameOver/PnlWin: paintComponent
// directo (no JLabel+Icon), para poder controlar su propio alpha y
// hacer el fade-out con precisión.
public class PnlWarning extends JPanel {

	private Image imagenAdvertencia;
	private float alpha = 1.0f;

	public PnlWarning() {
		setPreferredSize(new Dimension(1600, 900));
		setBackground(Color.BLACK);
		setOpaque(true);
		setLayout(null);

		// La advertencia se elige según el idioma vigente al construir el
		// panel -- que ahora ocurre después del selector de idioma (§1.35),
		// así que nunca aparece en un idioma y luego cambia. Si faltara la
		// versión en español, se usa la inglesa.
		javax.swing.ImageIcon imagen = (PreferenciasJuego.idiomaSeleccionado == Idioma.ESPANOL)
				? CargarImagenes.getAdvertenciaEspanol() : null;
		if (imagen == null) {
			imagen = CargarImagenes.getWarning();
		}
		if (imagen != null) {
			this.imagenAdvertencia = imagen.getImage();
		}
	}

	// Variante para la presentación de NYDER (CLAUDE.md §1.29): misma
	// pantalla negra y mismo fade por alpha que la advertencia, pero la
	// imagen se dibuja centrada, conservando su proporción, ocupando como
	// máximo "fraccionAncho" del ancho de la pantalla.
	private double fraccionAncho = 0;

	public PnlWarning(Image imagen, double fraccionAncho) {
		this();
		this.imagenAdvertencia = imagen;
		this.fraccionAncho = fraccionAncho;
	}

	// Destello de la estrella de la D (CLAUDE.md §1.65). La estrella es el
	// único elemento violeta del logo NYDER GAMES (12 189 píxeles en x
	// 941-1219, y 145-379 del PNG de 2172x724, medido): la máscara se saca del
	// propio PNG por color, sin tocar el archivo, y solo esos píxeles se
	// aclaran. El destello (un brillo radial y una cruz de cuatro puntas muy
	// finas) se dibuja centrado en la estrella y no pasa de ~2.5 veces su
	// radio: el resto del logo y de la pantalla no cambian.
	private volatile BufferedImage mascaraEstrella;
	private float estrellaCx, estrellaCy, estrellaRadio;
	private float brilloEstrella = 0f;

	// Construye la máscara en un hilo aparte (recorre 1.5 M de píxeles); el
	// destello empieza más de 1 s después, y si la máscara no estuviera lista
	// simplemente no se dibuja.
	public void prepararDestelloEstrella() {
		final Image fuente = imagenAdvertencia;
		Thread hilo = new Thread(() -> mascaraEstrella = crearMascaraEstrella(fuente), "nyder-mascara-estrella");
		hilo.setDaemon(true);
		hilo.start();
	}

	public boolean isDestelloEstrellaListo() {
		return mascaraEstrella != null;
	}

	private BufferedImage crearMascaraEstrella(Image fuente) {
		if (fuente == null) {
			return null;
		}
		int w = new javax.swing.ImageIcon(fuente).getIconWidth();
		int h = new javax.swing.ImageIcon(fuente).getIconHeight();
		if (w <= 0 || h <= 0) {
			return null;
		}
		BufferedImage copia = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = copia.createGraphics();
		g.drawImage(fuente, 0, 0, null);
		g.dispose();
		BufferedImage mascara = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		int[] fila = new int[w];
		long sx = 0, sy = 0, n = 0;
		int minX = w, maxX = -1, minY = h, maxY = -1;
		for (int y = 0; y < h; y++) {
			copia.getRGB(0, y, w, 1, fila, 0, w);
			for (int x = 0; x < w; x++) {
				int c = fila[x];
				int a = c >>> 24, r = (c >> 16) & 0xFF, gr = (c >> 8) & 0xFF, b = c & 0xFF;
				if (a > 40 && b > r + 40 && b > gr + 40) {
					// violeta aclarado hacia un blanco lavanda, con el alfa original
					int nr = r + (255 - r) * 3 / 4, ng = gr + (255 - gr) * 3 / 4, nb = b + (255 - b) * 3 / 4;
					mascara.setRGB(x, y, (a << 24) | (nr << 16) | (ng << 8) | nb);
					sx += x;
					sy += y;
					n++;
					minX = Math.min(minX, x);
					maxX = Math.max(maxX, x);
					minY = Math.min(minY, y);
					maxY = Math.max(maxY, y);
				}
			}
		}
		if (n < 50) {
			return null;
		}
		estrellaCx = sx / (float) n;
		estrellaCy = sy / (float) n;
		estrellaRadio = Math.max(maxX - minX, maxY - minY) / 2f;
		return mascara;
	}

	// 0 = sin destello, 1 = máximo.
	public void setBrilloEstrella(float brillo) {
		this.brilloEstrella = Math.max(0f, Math.min(1f, brillo));
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (imagenAdvertencia != null) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
			if (fraccionAncho > 0) {
				dibujarCentrada(g2);
			} else {
				g2.drawImage(imagenAdvertencia, 0, 0, getWidth(), getHeight(), this);
			}
			g2.dispose();
		}
		if (alPrimerPintado != null && isShowing()) {
			Runnable accion = alPrimerPintado;
			alPrimerPintado = null;
			javax.swing.SwingUtilities.invokeLater(accion);
		}
	}

	// Se ejecuta una sola vez, cuando este panel se pinta por primera vez
	// ya visible en pantalla. La ventana tarda ~400ms en pintarse después
	// de setVisible(true): arrancar un fade antes de eso lo hace invisible
	// para el jugador (causa raíz del fade in de NYDER, CLAUDE.md §1.31).
	private Runnable alPrimerPintado;

	public void alPrimerPintado(Runnable accion) {
		this.alPrimerPintado = accion;
	}

	private void dibujarCentrada(Graphics2D g2) {
		int anchoImg = imagenAdvertencia.getWidth(this);
		int altoImg = imagenAdvertencia.getHeight(this);
		if (anchoImg <= 0 || altoImg <= 0) {
			return;
		}
		double escala = Math.min(getWidth() * fraccionAncho / anchoImg, getHeight() * 0.8 / altoImg);
		int ancho = (int) (anchoImg * escala);
		int alto = (int) (altoImg * escala);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		int x0 = (getWidth() - ancho) / 2;
		int y0 = (getHeight() - alto) / 2;
		g2.drawImage(imagenAdvertencia, x0, y0, ancho, alto, this);
		BufferedImage mascara = mascaraEstrella;
		if (brilloEstrella > 0f && mascara != null) {
			dibujarDestelloEstrella(g2, mascara, x0, y0, escala);
		}
	}

	private void dibujarDestelloEstrella(Graphics2D g2, BufferedImage mascara, int x0, int y0, double escala) {
		float b = brilloEstrella;
		Graphics2D g = (Graphics2D) g2.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// 1) la estrella misma se aclara (mismo alfa global que el fade del panel)
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * Math.min(1f, b * 1.1f)));
		g.drawImage(mascara, x0, y0, (int) (mascara.getWidth() * escala), (int) (mascara.getHeight() * escala), null);
		float cx = (float) (x0 + estrellaCx * escala);
		float cy = (float) (y0 + estrellaCy * escala);
		float r = (float) (estrellaRadio * escala);
		// 2) halo suave alrededor del centro
		float radioHalo = r * (0.55f + 0.75f * b);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.55f * b));
		g.setPaint(new RadialGradientPaint(cx, cy, radioHalo, new float[] { 0f, 0.35f, 1f },
				new Color[] { new Color(255, 255, 255, 230), new Color(205, 195, 255, 120), new Color(160, 140, 255, 0) }));
		g.fill(new java.awt.geom.Ellipse2D.Float(cx - radioHalo, cy - radioHalo, radioHalo * 2, radioHalo * 2));
		// 3) cruz de cuatro puntas finas (el "chispazo"), solo cerca del pico
		float chispa = Math.max(0f, (b - 0.45f) / 0.55f);
		if (chispa > 0f) {
			float largo = r * (1.1f + 1.4f * chispa);
			float grosor = Math.max(1.5f, r * 0.07f);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.85f * chispa));
			g.setPaint(new RadialGradientPaint(cx, cy, largo, new float[] { 0f, 0.25f, 1f },
					new Color[] { new Color(255, 255, 255, 255), new Color(225, 220, 255, 170), new Color(190, 180, 255, 0) }));
			Path2D.Float cruz = new Path2D.Float();
			cruz.moveTo(cx - largo, cy);
			cruz.lineTo(cx, cy - grosor);
			cruz.lineTo(cx + largo, cy);
			cruz.lineTo(cx, cy + grosor);
			cruz.closePath();
			cruz.moveTo(cx, cy - largo);
			cruz.lineTo(cx + grosor, cy);
			cruz.lineTo(cx, cy + largo);
			cruz.lineTo(cx - grosor, cy);
			cruz.closePath();
			g.fill(cruz);
		}
		g.dispose();
	}

	// 1.0 = totalmente visible, 0.0 = totalmente desvanecida.
	public void setAlpha(float alpha) {
		this.alpha = alpha;
		repaint();
	}
}