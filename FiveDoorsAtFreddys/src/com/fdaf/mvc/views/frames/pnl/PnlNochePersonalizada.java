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

public class PnlNochePersonalizada extends JPanel {

	private static final long serialVersionUID = 1L;

	private JPanel pnlNochePersonalizada;
	private JLabel lblTitulo;
	private JLabel lblNocheSeleccionada;
	private JButton[] btnsNoche;
	private JButton btnAtras;
	private JLabel lbl;
	private JButton btnEscape;
	private JLabel lblEscapeBloqueado;

	public PnlNochePersonalizada() {
		setPreferredSize(new Dimension(1600, 900));
		setLayout(null);

		pnlNochePersonalizada = new PnlAplicarImagen(CargarImagenes.menu);
		pnlNochePersonalizada.setBounds(0, 0, 1600, 900);
		add(pnlNochePersonalizada);
		pnlNochePersonalizada.setLayout(null);

		lblTitulo = new JLabel("Custom Night");
		lblTitulo.setForeground(Color.WHITE);
		lblTitulo.setFont(Fuentes.obtener(60));
		lblTitulo.setBounds(227, 160, 600, 90);
		pnlNochePersonalizada.add(lblTitulo);

		lblNocheSeleccionada = new JLabel("Noche seleccionada: 1");
		lblNocheSeleccionada.setForeground(Color.WHITE);
		lblNocheSeleccionada.setFont(Fuentes.obtener(40));
		lblNocheSeleccionada.setBounds(227, 260, 700, 55);
		pnlNochePersonalizada.add(lblNocheSeleccionada);

		Font fuenteBoton = Fuentes.obtener(50);

		btnsNoche = new JButton[5];
		int[] posicionesY = {314, 397, 480, 563, 646};

		for (int i = 0; i < 5; i++) {
			JButton btnNoche = new JButton("Noche " + (i + 1));
			estilizarBoton(btnNoche, fuenteBoton);
			btnNoche.setBounds(227, posicionesY[i], 350, 70);
			pnlNochePersonalizada.add(btnNoche);
			btnsNoche[i] = btnNoche;
		}

		btnAtras = new JButton("Atras");
		estilizarBoton(btnAtras, fuenteBoton);
		btnAtras.setBounds(227, 729, 269, 70);
		pnlNochePersonalizada.add(btnAtras);

		// ESCAPE -- siempre visible (pedido explicito del usuario: generar
		// expectativa aunque este bloqueado), en una columna aparte a la
		// derecha de las 5 noches para no reacomodar ningun boton existente.
		// Fuente propia mas chica que fuenteBoton (38 vs 50): el texto
		// bloqueado "ESCAPE (Bloqueado)"/"ESCAPE (Locked)" es mas largo que
		// cualquier otra etiqueta de este panel y no entraba legible al
		// tamano de los demas botones. El texto/color real (bloqueado vs
		// desbloqueado) lo fija ControllerMenu.actualizarTextosIdioma() --
		// aqui solo se crea con un estado inicial neutro.
		Font fuenteEscape = Fuentes.obtener(38);
		btnEscape = new JButton("ESCAPE");
		estilizarBoton(btnEscape, fuenteEscape);
		btnEscape.setBounds(920, 314, 550, 70);
		pnlNochePersonalizada.add(btnEscape);

		lblEscapeBloqueado = new JLabel("");
		lblEscapeBloqueado.setForeground(Color.WHITE);
		lblEscapeBloqueado.setFont(Fuentes.obtener(24));
		lblEscapeBloqueado.setBounds(920, 397, 550, 90);
		lblEscapeBloqueado.setVisible(false);
		pnlNochePersonalizada.add(lblEscapeBloqueado);

		FontMetrics fmFlecha = getFontMetrics(fuenteBoton);
		int anchoFlecha = fmFlecha.stringWidth(">>") + 10;
		int altoFlecha = fmFlecha.getHeight();

		lbl = new JLabel(">>");
		lbl.setForeground(Color.WHITE);
		lbl.setFont(fuenteBoton);
		lbl.setBounds(0, 0, anchoFlecha, altoFlecha);
		lbl.setVisible(false);
		pnlNochePersonalizada.add(lbl);
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

	public void init(VistaPrincipal vp, PnlNochePersonalizada pantalla) {
		EscalarVista.adaptarNochePersonalizada(vp, pantalla);
		this.revalidate();
		this.repaint();
	}

	public JPanel getPnlNochePersonalizada() { return pnlNochePersonalizada; }
	public JLabel getLblTitulo() { return lblTitulo; }
	public JLabel getLblNocheSeleccionada() { return lblNocheSeleccionada; }
	public JButton[] getBtnsNoche() { return btnsNoche; }
	public JButton getBtnAtras() { return btnAtras; }
	public JLabel getLbl() { return lbl; }
	public JButton getBtnEscape() { return btnEscape; }
	public JLabel getLblEscapeBloqueado() { return lblEscapeBloqueado; }
}