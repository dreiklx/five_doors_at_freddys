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

@SuppressWarnings("unused")
public class PnlOpciones extends JPanel {

	private static final long serialVersionUID = 1L;

	private JPanel pnlOpciones;
	private JLabel lblTitulo;
	private JButton btnIdioma;
	private JButton btnCustomNight;
	private JButton btnVolumen;
	private JButton btnAtras;
	private JButton btnSalir;
	private JLabel lbl;

	public PnlOpciones() {
		setPreferredSize(new Dimension(1600, 900));
		setLayout(null);

		pnlOpciones = new PnlAplicarImagen(CargarImagenes.menu);
		pnlOpciones.setBounds(0, 0, 1600, 900);
		add(pnlOpciones);
		pnlOpciones.setLayout(null);

		lblTitulo = new JLabel("Opciones");
		lblTitulo.setForeground(Color.WHITE);
		lblTitulo.setFont(Fuentes.obtener(60));
		lblTitulo.setBounds(227, 220, 600, 90);
		pnlOpciones.add(lblTitulo);

		Font fuenteBoton = Fuentes.obtener(50);

		btnIdioma = new JButton("Idioma");
		estilizarBoton(btnIdioma, fuenteBoton);
		btnIdioma.setBounds(227, 374, 269, 70);
		pnlOpciones.add(btnIdioma);

		btnCustomNight = new JButton("Custom Night");
		estilizarBoton(btnCustomNight, fuenteBoton);
		btnCustomNight.setBounds(227, 457, 450, 70);
		pnlOpciones.add(btnCustomNight);

		btnVolumen = new JButton("Volumen");
		estilizarBoton(btnVolumen, fuenteBoton);
		btnVolumen.setBounds(227, 540, 269, 70);
		pnlOpciones.add(btnVolumen);

		btnAtras = new JButton("Atras");
		estilizarBoton(btnAtras, fuenteBoton);
		btnAtras.setBounds(227, 623, 269, 70);
		pnlOpciones.add(btnAtras);

		btnSalir = new JButton("Salir del Juego");
		estilizarBoton(btnSalir, fuenteBoton);
		btnSalir.setBounds(227, 706, 450, 70);
		pnlOpciones.add(btnSalir);

		FontMetrics fmFlecha = getFontMetrics(fuenteBoton);
		int anchoFlecha = fmFlecha.stringWidth(">>") + 10;
		int altoFlecha = fmFlecha.getHeight();

		lbl = new JLabel(">>");
		lbl.setForeground(Color.WHITE);
		lbl.setFont(fuenteBoton);
		lbl.setBounds(0, 0, anchoFlecha, altoFlecha);
		lbl.setVisible(false);
		pnlOpciones.add(lbl);
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

	public void init(VistaPrincipal vp, PnlOpciones opciones) {
		EscalarVista.adaptarOpciones(vp, opciones);
		this.revalidate();
		this.repaint();
	}

	public JPanel getPnlOpciones() { return pnlOpciones; }
	public void setPnlOpciones(JPanel pnlOpciones) { this.pnlOpciones = pnlOpciones; }
	public JLabel getLblTitulo() { return lblTitulo; }
	public void setLblTitulo(JLabel lblTitulo) { this.lblTitulo = lblTitulo; }
	public JButton getBtnIdioma() { return btnIdioma; }
	public void setBtnIdioma(JButton btnIdioma) { this.btnIdioma = btnIdioma; }
	public JButton getBtnCustomNight() { return btnCustomNight; }
	public void setBtnCustomNight(JButton btnCustomNight) { this.btnCustomNight = btnCustomNight; }
	public JButton getBtnVolumen() { return btnVolumen; }
	public void setBtnVolumen(JButton btnVolumen) { this.btnVolumen = btnVolumen; }
	public JButton getBtnAtras() { return btnAtras; }
	public void setBtnAtras(JButton btnAtras) { this.btnAtras = btnAtras; }
	public JButton getBtnSalir() { return btnSalir; }
	public void setBtnSalir(JButton btnSalir) { this.btnSalir = btnSalir; }
	public JLabel getLbl() { return lbl; }
	public void setLbl(JLabel lbl) { this.lbl = lbl; }
}