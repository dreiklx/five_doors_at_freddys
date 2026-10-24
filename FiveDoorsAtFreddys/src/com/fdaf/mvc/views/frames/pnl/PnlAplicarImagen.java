package com.fdaf.mvc.views.frames.pnl;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.fdaf.util.CargarImagenes;

public class PnlAplicarImagen extends JPanel {

    private ImageIcon imagen;

    public PnlAplicarImagen(ImageIcon im) {
    	imagen=im;

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (imagen != null) {
            if (imagen == CargarImagenes.menu) {
                dibujarFondoMenu(g, imagen.getImage(), getWidth(), getHeight(), this);
            } else {
                g.drawImage(imagen.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    // El GIF del menú (menu_background.gif, Freddy con estática) trae en
    // algunos cuadros (12, 21 y 61 de 70) 1-3 filas blancas abajo y 2
    // columnas blancas a la derecha -- medido cuadro por cuadro, viene del
    // propio archivo, no del escalado (CLAUDE.md §1.63). Se dibuja un 1.2 %
    // más grande y centrado: en el lienzo de 1600x900 quedan fuera ~9.6 px a
    // cada lado y ~5.4 px arriba y abajo, de sobra para esos bordes, sin
    // cambiar la proporción ni mover a Freddy de forma apreciable. El
    // archivo original no se modifica. Lo usan todas las pantallas que
    // tienen este GIF de fondo.
    private static final float SOBREBARRIDO_MENU = 0.012f;

    public static void dibujarFondoMenu(Graphics g, Image fondo, int ancho, int alto, ImageObserver observador) {
        int dx = Math.round(ancho * SOBREBARRIDO_MENU / 2f);
        int dy = Math.round(alto * SOBREBARRIDO_MENU / 2f);
        g.drawImage(fondo, -dx, -dy, ancho + 2 * dx, alto + 2 * dy, observador);
    }

	public ImageIcon getImagen() {
		return imagen;
	}

	public void setImagen(ImageIcon imagen) {
		this.imagen = imagen;
	}
}