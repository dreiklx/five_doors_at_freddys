package com.fdaf.mvc.views.frames.pnl;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.JPanel;

import com.fdaf.util.CargarImagenes;

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

		if (CargarImagenes.warning != null) {
			this.imagenAdvertencia = CargarImagenes.warning.getImage();
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (imagenAdvertencia != null) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
			g2.drawImage(imagenAdvertencia, 0, 0, getWidth(), getHeight(), this);
			g2.dispose();
		}
	}

	// 1.0 = totalmente visible, 0.0 = totalmente desvanecida.
	public void setAlpha(float alpha) {
		this.alpha = alpha;
		repaint();
	}
}