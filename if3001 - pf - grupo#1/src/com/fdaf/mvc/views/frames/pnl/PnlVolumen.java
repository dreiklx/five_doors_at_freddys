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

public class PnlVolumen extends JPanel {

	private JPanel pnlVolumen;
	private JLabel lblTitulo;
	private JLabel lblNivel;
	private JButton btnSubir;
	private JButton btnBajar;
	private JButton btnAtras;
	private JLabel lbl;

	public PnlVolumen() {
		setPreferredSize(new Dimension(1600, 900));
		setLayout(null);

		pnlVolumen = new PnlAplicarImagen(CargarImagenes.menu);
		pnlVolumen.setBounds(0, 0, 1600, 900);
		add(pnlVolumen);
		pnlVolumen.setLayout(null);

		lblTitulo = new JLabel("Volumen");
		lblTitulo.setForeground(Color.WHITE);
		lblTitulo.setFont(Fuentes.obtener(60));
		lblTitulo.setBounds(227, 220, 600, 90);
		pnlVolumen.add(lblTitulo);

		lblNivel = new JLabel("Nivel: 10/10");
		lblNivel.setForeground(Color.WHITE);
		lblNivel.setFont(Fuentes.obtener(40));
		lblNivel.setBounds(227, 320, 600, 55);
		pnlVolumen.add(lblNivel);

		Font fuenteBoton = Fuentes.obtener(50);

		btnSubir = new JButton("Subir Volumen");
		estilizarBoton(btnSubir, fuenteBoton);
		btnSubir.setBounds(227, 374, 700, 70);
		pnlVolumen.add(btnSubir);

		btnBajar = new JButton("Bajar Volumen");
		estilizarBoton(btnBajar, fuenteBoton);
		btnBajar.setBounds(227, 457, 680, 70);
		pnlVolumen.add(btnBajar);

		btnAtras = new JButton("Atras");
		estilizarBoton(btnAtras, fuenteBoton);
		btnAtras.setBounds(227, 540, 269, 70);
		pnlVolumen.add(btnAtras);

		FontMetrics fmFlecha = getFontMetrics(fuenteBoton);
		int anchoFlecha = fmFlecha.stringWidth(">>") + 10;
		int altoFlecha = fmFlecha.getHeight();

		lbl = new JLabel(">>");
		lbl.setForeground(Color.WHITE);
		lbl.setFont(fuenteBoton);
		lbl.setBounds(0, 0, anchoFlecha, altoFlecha);
		lbl.setVisible(false);
		pnlVolumen.add(lbl);
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

	public void init(VistaPrincipal vp, PnlVolumen pantalla) {
		EscalarVista.adaptarVolumen(vp, pantalla);
		this.revalidate();
		this.repaint();
	}

	public JPanel getPnlVolumen() { return pnlVolumen; }
	public JLabel getLblTitulo() { return lblTitulo; }
	public JLabel getLblNivel() { return lblNivel; }
	public JButton getBtnSubir() { return btnSubir; }
	public JButton getBtnBajar() { return btnBajar; }
	public JButton getBtnAtras() { return btnAtras; }
	public JLabel getLbl() { return lbl; }
}