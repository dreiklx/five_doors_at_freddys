package com.fdaf.mvc.views.Jframe.pnl;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class PnlAplicarImagen extends JPanel {

    private ImageIcon imagen;

    public PnlAplicarImagen(ImageIcon im) {
    	imagen=im;

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (imagen != null) {
            g.drawImage(imagen.getImage(), 0, 0, getWidth(), getHeight(), this);
        }
    }

	public ImageIcon getImagen() {
		return imagen;
	}

	public void setImagen(ImageIcon imagen) {
		this.imagen = imagen;
	}
}