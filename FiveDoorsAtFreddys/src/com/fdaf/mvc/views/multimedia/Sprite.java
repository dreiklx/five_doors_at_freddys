package com.fdaf.mvc.views.multimedia;

import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class Sprite {

	private JLabel label;

	public Sprite(JLabel label) {
		this.label = label;
	}

	public JLabel getLabel() {
		return label;
	}

	public void setLabel(JLabel label) {
		this.label = label;
	}

	public void setImagen(ImageIcon archivo) {
		int w = label.getWidth();
		int h = label.getHeight();
		label.setIcon(getImagenEscalada(archivo, w, h));
	}
	
	// ESCALAR EL TAMAÑO DE UNA IMAGEN
	public ImageIcon getImagenEscalada(ImageIcon archivo, int ancho, int alto) {

		ImageIcon icon = new ImageIcon(getClass().getResource("/resources/images/" + archivo));

		Image img = icon.getImage();
		Image imgEscalada = img.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);

		return new ImageIcon(imgEscalada);
	}

}
