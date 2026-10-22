package com.fdaf.mvc.views.frames.pnl;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

// Intro de cada noche (introNoche1-5.gif) con la línea de la noche
// localizada (CLAUDE.md §1.41).
//
// Los gifs terminan con una tarjeta negra "12:00 AM / 1st Night" dibujada
// dentro del propio gif, en inglés y con ordinal. En vez de duplicar cinco
// gifs de 12-19 MB por idioma, el gif se pinta igual que siempre y, mientras
// la tarjeta está en pantalla, la línea "1st Night" se tapa con negro y se
// dibuja encima la imagen localizada "Noche N" / "Night N" del HUD.
//
// La tarjeta se detecta en el cuadro que el gif muestra EN ESE MOMENTO (no
// con un temporizador paralelo, que se desincronizaría con la carga del
// gif): una franja central casi negra con dos líneas de texto claro. El
// brillo de esas líneas da la opacidad, así la imagen nueva sigue el mismo
// fundido de salida que el texto original. Cuando no hay tarjeta (periódico,
// destellos, negro, reloj final) el panel se ve exactamente como antes.
public class PnlIntroNoche extends PnlAplicarImagen {

	private static final long serialVersionUID = 1L;

	// Zona del gif (coordenadas nativas 1600x900) donde está la tarjeta en
	// los cinco gifs: el texto ocupa x~664-954, y~337-461.
	static final int ZONA_X = 560, ZONA_Y = 300, ZONA_ANCHO = 480, ZONA_ALTO = 200;

	private static final int UMBRAL_TEXTO = 12;   // luminancia mínima de un píxel de texto
	private static final int UMBRAL_FONDO = 10;   // por encima de esto, el píxel no es fondo negro
	private static final double MAX_FRACCION_NO_NEGRA = 0.15;

	private final ImageIcon etiquetaNoche;
	private final BufferedImage muestra = new BufferedImage(ZONA_ANCHO, ZONA_ALTO, BufferedImage.TYPE_INT_RGB);

	public PnlIntroNoche(ImageIcon gif, ImageIcon etiquetaNoche) {
		super(gif);
		this.etiquetaNoche = etiquetaNoche;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		ImageIcon gif = getImagen();
		if (gif == null || etiquetaNoche == null) {
			return;
		}
		Image img = gif.getImage();
		int anchoGif = img.getWidth(null), altoGif = img.getHeight(null);
		if (anchoGif != 1600 || altoGif != 900) {
			return; // gif de respaldo u otro formato: sin tarjeta conocida
		}

		Graphics2D gm = muestra.createGraphics();
		gm.setColor(Color.BLACK);
		gm.fillRect(0, 0, ZONA_ANCHO, ZONA_ALTO);
		gm.drawImage(img, 0, 0, ZONA_ANCHO, ZONA_ALTO, ZONA_X, ZONA_Y, ZONA_X + ZONA_ANCHO, ZONA_Y + ZONA_ALTO, null);
		gm.dispose();

		Tarjeta t = analizar(muestra);
		if (t == null) {
			return;
		}

		double ex = getWidth() / 1600.0, ey = getHeight() / 900.0;
		Graphics2D g2 = (Graphics2D) g.create();
		// Tapa la línea original ("1st Night") con el negro del fondo.
		int margen = 4;
		g2.setColor(Color.BLACK);
		g2.fillRect((int) Math.floor((ZONA_X + t.x0 - margen) * ex), (int) Math.floor((ZONA_Y + t.y0 - margen) * ey),
				(int) Math.ceil((t.x1 - t.x0 + 1 + 2 * margen) * ex), (int) Math.ceil((t.y1 - t.y0 + 1 + 2 * margen) * ey));

		// Imagen localizada: misma altura de mayúscula que el "12:00 AM" de
		// arriba (la imagen puede incluir el descendente de "g", de ahí el 1.15),
		// centrada bajo esa línea y en la posición vertical de la original.
		int altoCap = t.altoLinea1;
		double altoDestino = altoCap * 1.15 * ey;
		double escala = altoDestino / etiquetaNoche.getIconHeight();
		int w = (int) Math.round(etiquetaNoche.getIconWidth() * escala);
		int h = (int) Math.round(altoDestino);
		int cx = (int) Math.round((ZONA_X + t.centroX) * ex);
		int y = (int) Math.round((ZONA_Y + t.y0) * ey);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) t.opacidad));
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(etiquetaNoche.getImage(), cx - w / 2, y, w, h, null);
		g2.dispose();
	}

	// Resultado del análisis de un cuadro: caja de la segunda línea (en
	// coordenadas de la muestra), centro horizontal y alto de la primera
	// línea, y opacidad (brillo de la primera línea / 255).
	static final class Tarjeta {
		int x0, y0, x1, y1, centroX, altoLinea1;
		double opacidad;
	}

	static Tarjeta analizar(BufferedImage m) {
		int w = m.getWidth(), h = m.getHeight();
		int noNegros = 0;
		boolean[] filaConTexto = new boolean[h];
		int[] brilloFila = new int[h];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int rgb = m.getRGB(x, y);
				int l = (((rgb >> 16) & 255) * 299 + ((rgb >> 8) & 255) * 587 + (rgb & 255) * 114) / 1000;
				if (l > UMBRAL_FONDO) {
					noNegros++;
				}
				if (l > UMBRAL_TEXTO) {
					filaConTexto[y] = true;
					brilloFila[y] = Math.max(brilloFila[y], l);
				}
			}
		}
		if (noNegros == 0 || noNegros > w * h * MAX_FRACCION_NO_NEGRA) {
			return null; // negro total, o periódico / destello
		}
		// Bordes de la zona limpios: la tarjeta queda dentro, sin cortar.
		for (int y = 0; y < 6; y++) {
			if (filaConTexto[y] || filaConTexto[h - 1 - y]) {
				return null;
			}
		}
		// Agrupa filas con texto en líneas (huecos de hasta 2 filas).
		int[][] lineas = new int[4][];
		int n = 0, inicio = -1, ultima = -10;
		for (int y = 0; y < h; y++) {
			if (filaConTexto[y]) {
				if (inicio < 0 || y - ultima > 3) {
					if (inicio >= 0) {
						if (n == lineas.length) return null;
						lineas[n++] = new int[] { inicio, ultima };
					}
					inicio = y;
				}
				ultima = y;
			}
		}
		if (inicio >= 0) {
			if (n == lineas.length) return null;
			lineas[n++] = new int[] { inicio, ultima };
		}
		if (n != 2) {
			return null;
		}
		int alto1 = lineas[0][1] - lineas[0][0] + 1;
		int alto2 = lineas[1][1] - lineas[1][0] + 1;
		if (alto1 < 6 || alto1 > 60 || alto2 < 6 || alto2 > 60) {
			return null;
		}

		Tarjeta t = new Tarjeta();
		t.y0 = lineas[1][0];
		t.y1 = lineas[1][1];
		t.x0 = w;
		t.x1 = -1;
		int l1x0 = w, l1x1 = -1, brillo1 = 0;
		for (int y = lineas[0][0]; y <= lineas[1][1]; y++) {
			boolean enLinea1 = y <= lineas[0][1];
			for (int x = 0; x < w; x++) {
				int rgb = m.getRGB(x, y);
				int l = (((rgb >> 16) & 255) * 299 + ((rgb >> 8) & 255) * 587 + (rgb & 255) * 114) / 1000;
				if (l <= UMBRAL_TEXTO) continue;
				if (enLinea1) {
					l1x0 = Math.min(l1x0, x);
					l1x1 = Math.max(l1x1, x);
					brillo1 = Math.max(brillo1, l);
				} else if (y >= t.y0) {
					t.x0 = Math.min(t.x0, x);
					t.x1 = Math.max(t.x1, x);
				}
			}
		}
		if (t.x1 < 0 || l1x1 < 0) {
			return null;
		}
		t.centroX = (l1x0 + l1x1) / 2;
		t.altoLinea1 = alto1;
		t.opacidad = Math.min(1.0, brillo1 / 235.0);
		return t;
	}
}
