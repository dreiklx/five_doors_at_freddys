package com.fdaf.mvc.views.Jframe.pnl;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;

import com.fdaf.util.CargarImagenes;

import javax.swing.JButton;
import java.awt.Dimension;


public class PnlOpciones extends JPanel {
	private JButton btnAtras;
	private JButton btnSalir;
	private JLabel lbl;
	private JPanel pnlOpciones;

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
		
		JLabel label = new JLabel("---");
		label.setForeground(Color.WHITE);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setFont(new Font("Tahoma", Font.PLAIN, 30));
		label.setBounds(493, 100, 341, 50);
		pnlOpciones.add(label);
		
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
