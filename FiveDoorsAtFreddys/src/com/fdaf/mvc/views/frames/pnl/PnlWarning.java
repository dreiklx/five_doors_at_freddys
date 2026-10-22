package com.fdaf.mvc.views.frames.pnl;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

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
			imagen = CargarImagenes.warning;
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
		g2.drawImage(imagenAdvertencia, (getWidth() - ancho) / 2, (getHeight() - alto) / 2, ancho, alto, this);
	}

	// 1.0 = totalmente visible, 0.0 = totalmente desvanecida.
	public void setAlpha(float alpha) {
		this.alpha = alpha;
		repaint();
	}
}