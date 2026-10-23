package com.fdaf.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Barra de carga retro segmentada (CLAUDE.md §1.60): panel gris con biseles al estilo de los dialogos clasicos de
 * escritorio, una ranura hundida y segmentos rectangulares azul oscuro separados que se llenan de izquierda a
 * derecha. Solo la barra: sin texto, sin botones. Se dibuja en coordenadas enteras y sin antialias para que los
 * biseles y los segmentos queden nitidos a cualquier resolucion.
 */
public final class BarraRetro {

	private static final Color GRIS = new Color(192, 192, 192);
	private static final Color LUZ = new Color(255, 255, 255);
	private static final Color LUZ_SUAVE = new Color(223, 223, 223);
	private static final Color SOMBRA = new Color(128, 128, 128);
	private static final Color SOMBRA_FUERTE = new Color(10, 10, 10);
	private static final Color RANURA = new Color(176, 176, 176);
	private static final Color SEGMENTO = new Color(0, 0, 128);
	private static final Color SEGMENTO_BRILLO = new Color(28, 28, 168);

	private BarraRetro() {
	}

	/** Ancho y alto proporcionados al area disponible (la barra nunca supera 520 x 56 px de base). */
	public static int ancho(int anchoPantalla) {
		return Math.min(520, Math.max(220, anchoPantalla * 34 / 100));
	}

	public static int alto(int anchoBarra) {
		return Math.max(26, anchoBarra * 11 / 100);
	}

	/** Dibuja la barra centrada en (cx, cy) con un progreso en [0, 1]. */
	public static void pintar(Graphics2D g, int cx, int cy, int anchoPantalla, float progreso) {
		int w = ancho(anchoPantalla);
		int h = alto(w);
		int x = cx - w / 2;
		int y = cy - h / 2;
		Object aa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

		// panel elevado: bisel claro arriba/izquierda, oscuro abajo/derecha (dos pixeles por lado)
		g.setColor(GRIS);
		g.fillRect(x, y, w, h);
		biselElevado(g, x, y, w, h);

		// ranura hundida donde viven los segmentos
		int m = Math.max(5, h / 5);
		int rx = x + m, ry = y + m, rw = w - 2 * m, rh = h - 2 * m;
		g.setColor(RANURA);
		g.fillRect(rx, ry, rw, rh);
		biselHundido(g, rx, ry, rw, rh);

		// segmentos: alto de la ranura menos 2 px por lado; separacion fija de 2 px
		int ix = rx + 3, iy = ry + 3, iw = rw - 6, ih = rh - 6;
		int ancho = Math.max(6, ih * 6 / 10);
		int sep = 2;
		int total = Math.max(1, (iw + sep) / (ancho + sep));
		int llenos = Math.round(Math.max(0f, Math.min(1f, progreso)) * total);
		for (int i = 0; i < llenos; i++) {
			int sx = ix + i * (ancho + sep);
			g.setColor(SEGMENTO);
			g.fillRect(sx, iy, ancho, ih);
			g.setColor(SEGMENTO_BRILLO);
			g.fillRect(sx, iy, ancho, Math.max(1, ih / 5));
		}
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa != null ? aa : RenderingHints.VALUE_ANTIALIAS_DEFAULT);
	}

	private static void biselElevado(Graphics2D g, int x, int y, int w, int h) {
		g.setColor(LUZ);
		g.drawLine(x, y, x + w - 2, y);
		g.drawLine(x, y, x, y + h - 2);
		g.setColor(LUZ_SUAVE);
		g.drawLine(x + 1, y + 1, x + w - 3, y + 1);
		g.drawLine(x + 1, y + 1, x + 1, y + h - 3);
		g.setColor(SOMBRA_FUERTE);
		g.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
		g.drawLine(x + w - 1, y, x + w - 1, y + h - 1);
		g.setColor(SOMBRA);
		g.drawLine(x + 1, y + h - 2, x + w - 2, y + h - 2);
		g.drawLine(x + w - 2, y + 1, x + w - 2, y + h - 2);
	}

	private static void biselHundido(Graphics2D g, int x, int y, int w, int h) {
		g.setColor(SOMBRA);
		g.drawLine(x, y, x + w - 2, y);
		g.drawLine(x, y, x, y + h - 2);
		g.setColor(SOMBRA_FUERTE);
		g.drawLine(x + 1, y + 1, x + w - 3, y + 1);
		g.drawLine(x + 1, y + 1, x + 1, y + h - 3);
		g.setColor(LUZ);
		g.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
		g.drawLine(x + w - 1, y, x + w - 1, y + h - 1);
		g.setColor(LUZ_SUAVE);
		g.drawLine(x + 1, y + h - 2, x + w - 2, y + h - 2);
		g.drawLine(x + w - 2, y + 1, x + w - 2, y + h - 2);
	}
}
