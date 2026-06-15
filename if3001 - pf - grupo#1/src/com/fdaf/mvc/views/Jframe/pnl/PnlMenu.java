package com.fdaf.mvc.views.Jframe.pnl;

import javax.swing.JPanel;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JTextPane;

import com.fdaf.util.CargarImagenes;

import java.awt.Font;
import javax.swing.JButton;
import java.awt.Cursor;

public class PnlMenu extends JPanel {
	private JPanel pnlMenu;
	private JButton btnEmpezar;
	private JButton btnOpciones;
	private JLabel lblMenu;
	private JLabel lblEstatica;
	private JLabel lbl;

	/**
	 * Create the panel.
	 */
	public PnlMenu() {
		setBackground(Color.pink);
		setLayout(null);
		
		pnlMenu = new PnlAplicarImagen(CargarImagenes.menu);
		pnlMenu.setBounds(0, 0, 1000, 600);
		add(pnlMenu);
		pnlMenu.setLayout(null);
		
		JTextPane txtpnFiveDoorsAt = new JTextPane();
		txtpnFiveDoorsAt.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		txtpnFiveDoorsAt.setFocusable(false);
		txtpnFiveDoorsAt.setOpaque(false);
		txtpnFiveDoorsAt.setFont(new Font("Tahoma", Font.PLAIN, 40));
		txtpnFiveDoorsAt.setText("Five\r\nDoors\r\nat\r\nFreddy's");
		txtpnFiveDoorsAt.setBounds(65, 64, 201, 211);
		txtpnFiveDoorsAt.setForeground(Color.WHITE);
		pnlMenu.add(txtpnFiveDoorsAt);
		
		btnEmpezar = new JButton("Empezar");
		btnEmpezar.setFocusPainted(false);
		btnEmpezar.setBorderPainted(false);
		btnEmpezar.setContentAreaFilled(false);
		btnEmpezar.setOpaque(false);
		btnEmpezar.setFont(new Font("Tahoma", Font.PLAIN, 30));
		btnEmpezar.setBorder(null);
		btnEmpezar.setBounds(65, 350, 127, 37);
		btnEmpezar.setForeground(Color.WHITE);
		pnlMenu.add(btnEmpezar);
		
		btnOpciones = new JButton("Opciones");
		btnOpciones.setOpaque(false);
		btnOpciones.setFocusPainted(false);
		btnOpciones.setContentAreaFilled(false);
		btnOpciones.setBorderPainted(false);
		btnOpciones.setFont(new Font("Tahoma", Font.PLAIN, 30));
		btnOpciones.setBounds(789, 507, 164, 37);
		btnOpciones.setForeground(Color.WHITE);
		pnlMenu.add(btnOpciones);
		
		
		lbl = new JLabel(">>");
		lbl.setForeground(Color.WHITE);
		lbl.setFont(new Font("Tahoma", Font.PLAIN, 30));
		lbl.setBounds(762, 507, 46, 37);
		lbl.setVisible(false);
		pnlMenu.add(lbl);
		
		
		lblMenu = new JLabel("");
		lblMenu.setBounds(0, 0, 1000, 600);
		pnlMenu.add(lblMenu);

	}



	public JLabel getLbl() {
		return lbl;
	}



	public void setLbl(JLabel lbl) {
		this.lbl = lbl;
	}



	public JButton getBtnEmpezar() {
		return btnEmpezar;
	}

	public void setBtnEmpezar(JButton btnEmpezar) {
		this.btnEmpezar = btnEmpezar;
	}



	public JButton getBtnOpciones() {
		return btnOpciones;
	}

	public void setBtnOpciones(JButton btnOpciones) {
		this.btnOpciones = btnOpciones;
	}
}
