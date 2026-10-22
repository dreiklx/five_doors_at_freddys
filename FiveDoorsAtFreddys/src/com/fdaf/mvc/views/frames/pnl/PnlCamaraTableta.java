package com.fdaf.mvc.views.frames.pnl;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.ImageIcon;

// Fondo de la tableta: camara.gif (estática animada, SIN texto) con el
// rótulo "-CAMERA DISABLED- / AUDIO ONLY" o "-CÁMARA DESACTIVADA- / SOLO
// AUDIO" dibujado encima como imagen aparte (CLAUDE.md §1.45). Un solo gif
// para ambos idiomas; solo cambia el PNG del rótulo.
public class PnlCamaraTableta extends PnlAplicarImagen {

	private static final long serialVersionUID = 1L;

	// Posición del rótulo en coordenadas del gif (1600x900): mismo alto y
	// centro que el texto en inglés que traía incrustado el gif anterior
	// (medido: x 448-1153, y 57-170), conservando la proporción del PNG.
	// Los dos PNG comparten el mismo lienzo recortado (2160x390 del
	// original, desde x4-y155), así que se colocan en el mismo rectángulo.
	private static final double ROTULO_X = 468.6, ROTULO_Y = 51.2, ROTULO_ANCHO = 663.8, ROTULO_ALTO = 119.8;

	private final ImageIcon rotulo;

	public PnlCamaraTableta(ImageIcon gif, ImageIcon rotulo) {
		super(gif);
		this.rotulo = rotulo;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (rotulo == null) {
			return;
		}
		double ex = getWidth() / 1600.0, ey = getHeight() / 900.0;
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(rotulo.getImage(), (int) Math.round(ROTULO_X * ex), (int) Math.round(ROTULO_Y * ey),
				(int) Math.round(ROTULO_ANCHO * ex), (int) Math.round(ROTULO_ALTO * ey), null);
		g2.dispose();
	}
}
