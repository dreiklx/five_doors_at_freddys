package com.fdaf.mvc.views.frames.pnl;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;

import com.fdaf.mvc.views.frames.VistaPrincipal;
import com.fdaf.util.CargarImagenes;
import com.fdaf.util.EscalarVista;

import javax.swing.JButton;
import java.awt.Dimension;


public class PnlOpciones extends JPanel {
	private JButton btnAtras;
	private JButton btnSalir;
	private JLabel lbl;
	private JPanel pnlOpciones;

	private JButton btnEspanol;
	private JButton btnIngles;
	private JLabel lblIdiomaSeleccionado;

	// CAMBIO: selector de dificultad/noche, mismo patrón que idioma.
	private JButton[] btnsNoche;
	private JLabel lblNocheSeleccionada;

	/**
	 * Create the panel.
	 */
	public PnlOpciones() {
		setPreferredSize(new Dimension(1600, 900));
		setLayout(null);
		
		pnlOpciones = new PnlAplicarImagen(CargarImagenes.menu);
		pnlOpciones.setBounds(0, 0, 1600, 900);
		add(pnlOpciones);
		pnlOpciones.setLayout(null);
		
		JLabel lblIdioma = new JLabel("Idioma");
		lblIdioma.setForeground(Color.WHITE);
		lblIdioma.setHorizontalAlignment(SwingConstants.CENTER);
		lblIdioma.setFont(new Font("Tahoma", Font.PLAIN, 30));
		lblIdioma.setBounds(51, 100, 341, 50);
		pnlOpciones.add(lblIdioma);
		
		lblIdiomaSeleccionado = new JLabel("Español");
		lblIdiomaSeleccionado.setForeground(Color.WHITE);
		lblIdiomaSeleccionado.setHorizontalAlignment(SwingConstants.CENTER);
		lblIdiomaSeleccionado.setFont(new Font("Tahoma", Font.PLAIN, 30));
		lblIdiomaSeleccionado.setBounds(493, 100, 341, 50);
		pnlOpciones.add(lblIdiomaSeleccionado);

		btnEspanol = new JButton("Español");
		btnEspanol.setFocusPainted(false);
		btnEspanol.setContentAreaFilled(false);
		btnEspanol.setBorderPainted(false);
		btnEspanol.setForeground(Color.WHITE);
		btnEspanol.setFont(new Font("Tahoma", Font.PLAIN, 24));
		btnEspanol.setBounds(51, 180, 200, 50);
		pnlOpciones.add(btnEspanol);

		btnIngles = new JButton("Inglés");
		btnIngles.setFocusPainted(false);
		btnIngles.setContentAreaFilled(false);
		btnIngles.setBorderPainted(false);
		btnIngles.setForeground(Color.WHITE);
		btnIngles.setFont(new Font("Tahoma", Font.PLAIN, 24));
		btnIngles.setBounds(270, 180, 200, 50);
		pnlOpciones.add(btnIngles);

		// selector de dificultad/noche.
		JLabel lblDificultad = new JLabel("Dificultad");
		lblDificultad.setForeground(Color.WHITE);
		lblDificultad.setHorizontalAlignment(SwingConstants.CENTER);
		lblDificultad.setFont(new Font("Tahoma", Font.PLAIN, 30));
		lblDificultad.setBounds(51, 260, 341, 50);
		pnlOpciones.add(lblDificultad);

		lblNocheSeleccionada = new JLabel("Noche 1");
		lblNocheSeleccionada.setForeground(Color.WHITE);
		lblNocheSeleccionada.setHorizontalAlignment(SwingConstants.CENTER);
		lblNocheSeleccionada.setFont(new Font("Tahoma", Font.PLAIN, 30));
		lblNocheSeleccionada.setBounds(493, 260, 341, 50);
		pnlOpciones.add(lblNocheSeleccionada);

		btnsNoche = new JButton[5];
		String[] etiquetas = {"Noche 1", "Noche 2", "Noche 3", "Noche 4", "Noche 5"};

		for (int i = 0; i < 5; i++) {
			JButton btnNoche = new JButton(etiquetas[i]);
			btnNoche.setFocusPainted(false);
			btnNoche.setContentAreaFilled(false);
			btnNoche.setBorderPainted(false);
			btnNoche.setForeground(Color.WHITE);
			btnNoche.setFont(new Font("Tahoma", Font.PLAIN, 20));
			btnNoche.setBounds(51 + i * 160, 320, 140, 45);
			pnlOpciones.add(btnNoche);
			btnsNoche[i] = btnNoche;
		}
		
		btnAtras = new JButton("Atras");
		btnAtras.setFocusPainted(false);
		btnAtras.setContentAreaFilled(false);
		btnAtras.setBorderPainted(false);
		btnAtras.setForeground(Color.WHITE);
		btnAtras.setFont(new Font("Tahoma", Font.PLAIN, 30));
		btnAtras.setBounds(1447, 832, 120, 37);
		pnlOpciones.add(btnAtras);
		
		btnSalir = new JButton("Salir del Juego");
		btnSalir.setFocusPainted(false);
		btnSalir.setContentAreaFilled(false);
		btnSalir.setBorderPainted(false);
		btnSalir.setForeground(Color.WHITE);
		btnSalir.setFont(new Font("Tahoma", Font.PLAIN, 30));
		btnSalir.setBounds(1332, 27, 235, 37);
		pnlOpciones.add(btnSalir);
		
		lbl = new JLabel(">>");
		lbl.setForeground(Color.WHITE);
		lbl.setFont(new Font("Tahoma", Font.PLAIN, 30));
		lbl.setBounds(1418, 832, 46, 37);
		lbl.setVisible(false);
		pnlOpciones.add(lbl);

	}
	
	public void init(VistaPrincipal vp,PnlOpciones opciones) {
		EscalarVista.adaptarOpciones(vp, opciones);
		this.revalidate();
		this.repaint();
	}
	
	public JButton getBtnEspanol() {
		return btnEspanol;
	}

	public void setBtnEspanol(JButton btnEspanol) {
		this.btnEspanol = btnEspanol;
	}

	public JButton getBtnIngles() {
		return btnIngles;
	}

	public void setBtnIngles(JButton btnIngles) {
		this.btnIngles = btnIngles;
	}

	public JLabel getLblIdiomaSeleccionado() {
		return lblIdiomaSeleccionado;
	}

	public void setLblIdiomaSeleccionado(JLabel lblIdiomaSeleccionado) {
		this.lblIdiomaSeleccionado = lblIdiomaSeleccionado;
	}

	public JButton[] getBtnsNoche() {
		return btnsNoche;
	}

	public void setBtnsNoche(JButton[] btnsNoche) {
		this.btnsNoche = btnsNoche;
	}

	public JLabel getLblNocheSeleccionada() {
		return lblNocheSeleccionada;
	}

	public void setLblNocheSeleccionada(JLabel lblNocheSeleccionada) {
		this.lblNocheSeleccionada = lblNocheSeleccionada;
	}

	public JPanel getPnlOpciones() {
		return pnlOpciones;
	}

	public void setPnlOpciones(JPanel pnlOpciones) {
		this.pnlOpciones = pnlOpciones;
	}

	public JLabel getLbl() {
		return lbl;
	}

	public void setLbl(JLabel lbl) {
		this.lbl = lbl;
	}

	public JButton getBtnAtras() {
		return btnAtras;
	}

	public void setBtnAtras(JButton btnAtras) {
		this.btnAtras = btnAtras;
	}

	public JButton getBtnSalir() {
		return btnSalir;
	}

	public void setBtnSalir(JButton btnSalir) {
		this.btnSalir = btnSalir;
	}

}