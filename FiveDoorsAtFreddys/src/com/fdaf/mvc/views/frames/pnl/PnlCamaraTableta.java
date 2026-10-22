package com.fdaf.mvc.views.frames.pnl;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import com.fdaf.util.Fuentes;

// Fondo de la tableta (camara.gif) con su rótulo localizado (CLAUDE.md §1.42).
//
// camara.gif es estática animada con "-CAMERA DISABLED- / AUDIO ONLY"
// dibujado en los 8 cuadros. En inglés se pinta tal cual. En español, sobre
// el rótulo se copia estática del MISMO cuadro (unas filas más abajo, así
// sigue animándose) y encima se escribe "-CÁMARA DESACTIVADA- / SOLO AUDIO"
// en letra pixelada blanca con contorno negro, como la original. Evita
// duplicar un gif de 3.7 MB por idioma.
public class PnlCamaraTableta extends PnlAplicarImagen {

	private static final long serialVersionUID = 1L;

	// Rótulo original en coordenadas nativas del gif (1600x900): texto en
	// x 485-1118, y 58-170 (medido); se tapa con un margen.
	private static final int TAPA_X = 470, TAPA_Y = 50, TAPA_ANCHO = 665, TAPA_ALTO = 130;
	private static final int DESPLAZAMIENTO_ESTATICA = 320;
	private static final int PIXEL = 3; // la letra se dibuja a 2x y se amplía 3x: bloques de 3 px y contorno fino

	private final BufferedImage rotulo; // null = inglés: el gif ya trae el rótulo correcto

	public PnlCamaraTableta(ImageIcon gif, String linea1, String linea2) {
		super(gif);
		rotulo = (linea1 == null) ? null : crearRotulo(linea1, linea2);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		ImageIcon gif = getImagen();
		if (rotulo == null || gif == null) {
			return;
		}
		Image img = gif.getImage();
		if (img.getWidth(null) != 1600 || img.getHeight(null) != 900) {
			return;
		}
		double ex = getWidth() / 1600.0, ey = getHeight() / 900.0;
		Graphics2D g2 = (Graphics2D) g.create();
		int dx = (int) Math.floor(TAPA_X * ex), dy = (int) Math.floor(TAPA_Y * ey);
		int dw = (int) Math.ceil(TAPA_ANCHO * ex), dh = (int) Math.ceil(TAPA_ALTO * ey);
		g2.drawImage(img, dx, dy, dx + dw, dy + dh, TAPA_X, TAPA_Y + DESPLAZAMIENTO_ESTATICA,
				TAPA_X + TAPA_ANCHO, TAPA_Y + TAPA_ALTO + DESPLAZAMIENTO_ESTATICA, this);

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		int w = (int) Math.round(rotulo.getWidth() * PIXEL * ex);
		int h = (int) Math.round(rotulo.getHeight() * PIXEL * ey);
		int x = (int) Math.round(800 * ex) - w / 2;
		int y = (int) Math.round(52 * ey);
		g2.drawImage(rotulo, x, y, w, h, null);
		g2.dispose();
	}

	// Texto a resolución baja y sin suavizado; al ampliarlo con vecino más
	// cercano queda pixelado como el rótulo del gif.
	private static BufferedImage crearRotulo(String linea1, String linea2) {
		Font fuente = Fuentes.obtenerSinEscalar(22, Font.BOLD);
		BufferedImage medida = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gm = medida.createGraphics();
		FontMetrics fm = gm.getFontMetrics(fuente);
		gm.dispose();
		int ancho = Math.max(fm.stringWidth(linea1), fm.stringWidth(linea2)) + 4;
		int altoLinea = fm.getAscent() + 1;
		BufferedImage img = new BufferedImage(ancho, altoLinea * 2 + 4, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		g.setFont(fuente);
		String[] lineas = { linea1, linea2 };
		for (int i = 0; i < 2; i++) {
			int x = (ancho - fm.stringWidth(lineas[i])) / 2;
			int y = 2 + fm.getAscent() + i * altoLinea;
			g.setColor(Color.BLACK);
			for (int ox = -1; ox <= 1; ox++) {
				for (int oy = -1; oy <= 1; oy++) {
					g.drawString(lineas[i], x + ox, y + oy);
				}
			}
			g.setColor(Color.WHITE);
			g.drawString(lineas[i], x, y);
		}
		g.dispose();
		return img;
	}
}
