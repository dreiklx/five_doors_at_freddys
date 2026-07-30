package com.fdaf.mvc.views.frames.pnl;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import com.fdaf.mvc.views.frames.VistaPrincipal;
import com.fdaf.util.EscalarVista;
import com.fdaf.util.Fuentes;

public class PnlGameOver extends JPanel {

	private Image imagenActual;
	private JLabel lblTitulo;
	private JButton btnReintentar;
	private JButton btnSalir;
	private JLabel lblFlecha;

	public PnlGameOver() {
		setPreferredSize(new Dimension(1600, 900));
		setBackground(Color.BLACK);
		setOpaque(true);
		setLayout(null);

		lblTitulo = new JLabel("GAME OVER");
		lblTitulo.setForeground(Color.WHITE);
		lblTitulo.setFont(Fuentes.obtener(60, Font.BOLD));
		lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitulo.setBounds(0, 150, 1600, 90);
		lblTitulo.setVisible(false);
		add(lblTitulo);

		Font fuenteBoton = Fuentes.obtener(50);

		// Geometría horizontal tipo PnlConfirmacion: 2 botones lado a
		// lado, misma Y, mismo ancho -- no apilados verticalmente como
		// Opciones/Idioma/Volumen/Custom Night.
		btnReintentar = new JButton("Volver a intentar");
		estilizarBoton(btnReintentar, fuenteBoton);
		btnReintentar.setBounds(260, 650, 520, 80);
		btnReintentar.setVisible(false);
		add(btnReintentar);

		btnSalir = new JButton("Salir del juego");
		estilizarBoton(btnSalir, fuenteBoton);
		btnSalir.setBounds(820, 650, 520, 80);
		btnSalir.setVisible(false);
		add(btnSalir);

		FontMetrics fmFlecha = getFontMetrics(fuenteBoton);
		int anchoFlecha = fmFlecha.stringWidth(">>") + 10;
		int altoFlecha = fmFlecha.getHeight();

		lblFlecha = new JLabel(">>");
		lblFlecha.setForeground(Color.WHITE);
		lblFlecha.setFont(fuenteBoton);
		lblFlecha.setBounds(0, 0, anchoFlecha, altoFlecha);
		lblFlecha.setVisible(false);
		add(lblFlecha);
	}

	private void estilizarBoton(JButton boton, Font fuente) {
		boton.setFocusPainted(false);
		boton.setBorderPainted(false);
		boton.setContentAreaFilled(false);
		boton.setOpaque(false);
		boton.setBorder(null);
		boton.setFont(fuente);
		boton.setForeground(Color.WHITE);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (imagenActual != null) {
			g.drawImage(imagenActual, 0, 0, getWidth(), getHeight(), this);
		}
	}

	public void mostrar(ImageIcon icono) {
		this.imagenActual = (icono != null) ? icono.getImage() : null;
		repaint();
	}

	public void init(VistaPrincipal vp, PnlGameOver pantalla) {
		EscalarVista.adaptarGameOver(vp, pantalla);
		this.revalidate();
		this.repaint();
	}

	public JLabel getLblTitulo() { return lblTitulo; }
	public void setLblTitulo(JLabel lblTitulo) { this.lblTitulo = lblTitulo; }
	public JButton getBtnReintentar() { return btnReintentar; }
	public void setBtnReintentar(JButton btnReintentar) { this.btnReintentar = btnReintentar; }
	public JButton getBtnSalir() { return btnSalir; }
	public void setBtnSalir(JButton btnSalir) { this.btnSalir = btnSalir; }
	public JLabel getLblFlecha() { return lblFlecha; }
	public void setLblFlecha(JLabel lblFlecha) { this.lblFlecha = lblFlecha; }
}