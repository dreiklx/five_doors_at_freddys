package com.fdaf.mvc.views.frames.pnl;

import javax.swing.JPanel;
import java.awt.Color;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.SwingConstants;

import com.fdaf.mvc.views.frames.VistaPrincipal;
import com.fdaf.util.CargarImagenes;
import com.fdaf.util.EscalarVista;
import com.fdaf.util.Fuentes;

import javax.swing.JButton;
import java.awt.Dimension;

public class PnlIdioma extends JPanel {

	private JPanel pnlIdioma;
	private JLabel lblTitulo;
	private JLabel lblIdiomaActual;
	private JButton btnEspanol;
	private JButton btnIngles;
	private JButton btnAtras;
	private JLabel lbl;

	public PnlIdioma() {
		setPreferredSize(new Dimension(1600, 900));
		setLayout(null);

		pnlIdioma = new PnlAplicarImagen(CargarImagenes.menu);
		pnlIdioma.setBounds(0, 0, 1600, 900);
		add(pnlIdioma);
		pnlIdioma.setLayout(null);

		lblTitulo = new JLabel("Idioma");
		lblTitulo.setForeground(Color.WHITE);
		lblTitulo.setFont(Fuentes.obtener(60));
		lblTitulo.setBounds(227, 220, 600, 90);
		pnlIdioma.add(lblTitulo);

		lblIdiomaActual = new JLabel("Español");
		lblIdiomaActual.setForeground(Color.WHITE);
		lblIdiomaActual.setFont(Fuentes.obtener(26));
		lblIdiomaActual.setBounds(227, 320, 600, 40);
		pnlIdioma.add(lblIdiomaActual);

		Font fuenteBoton = Fuentes.obtener(50);

		btnEspanol = new JButton("Español");
		estilizarBoton(btnEspanol, fuenteBoton);
		btnEspanol.setBounds(227, 464, 350, 70);
		pnlIdioma.add(btnEspanol);

		btnIngles = new JButton("English");
		estilizarBoton(btnIngles, fuenteBoton);
		btnIngles.setBounds(227, 547, 350, 70);
		pnlIdioma.add(btnIngles);

		btnAtras = new JButton("Atras");
		estilizarBoton(btnAtras, fuenteBoton);
		btnAtras.setBounds(227, 630, 269, 70);
		pnlIdioma.add(btnAtras);

		FontMetrics fmFlecha = getFontMetrics(fuenteBoton);
		int anchoFlecha = fmFlecha.stringWidth(">>") + 10;
		int altoFlecha = fmFlecha.getHeight();

		lbl = new JLabel(">>");
		lbl.setForeground(Color.WHITE);
		lbl.setFont(fuenteBoton);
		lbl.setBounds(0, 0, anchoFlecha, altoFlecha);
		lbl.setVisible(false);
		pnlIdioma.add(lbl);
	}

	private void estilizarBoton(JButton boton, Font fuente) {
		boton.setHorizontalAlignment(SwingConstants.LEFT);
		boton.setFocusPainted(false);
		boton.setBorderPainted(false);
		boton.setContentAreaFilled(false);
		boton.setOpaque(false);
		boton.setBorder(null);
		boton.setFont(fuente);
		boton.setForeground(Color.WHITE);
	}

	public void init(VistaPrincipal vp, PnlIdioma pantalla) {
		EscalarVista.adaptarIdioma(vp, pantalla);
		this.revalidate();
		this.repaint();
	}

	public JPanel getPnlIdioma() { return pnlIdioma; }
	public JLabel getLblTitulo() { return lblTitulo; }
	public JLabel getLblIdiomaActual() { return lblIdiomaActual; }
	public JButton getBtnEspanol() { return btnEspanol; }
	public JButton getBtnIngles() { return btnIngles; }
	public JButton getBtnAtras() { return btnAtras; }
	public JLabel getLbl() { return lbl; }
}