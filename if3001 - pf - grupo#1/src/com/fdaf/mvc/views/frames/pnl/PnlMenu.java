package com.fdaf.mvc.views.frames.pnl;

import javax.swing.JPanel;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JTextPane;

import com.fdaf.mvc.views.frames.VistaPrincipal;
import com.fdaf.util.CargarImagenes;
import com.fdaf.util.EscalarVista;

import java.awt.Font;
import javax.swing.JButton;
import java.awt.Cursor;
import java.awt.Dimension;

public class PnlMenu extends JPanel {
	private JPanel pnlMenu;
	private JButton btnEmpezar;
	private JButton btnOpciones;
	private JLabel lblMenu;
	private JLabel lbl;
	private JTextPane txtpnFiveDoorsAt;

	/**
	 * Create the panel.
	 */
	public PnlMenu() {
		setPreferredSize(new Dimension(1600, 900));
		setBackground(Color.ORANGE);
		setLayout(null);
		
		pnlMenu = new PnlAplicarImagen(CargarImagenes.menu);
		pnlMenu.setBackground(Color.BLACK);
		pnlMenu.setPreferredSize(new Dimension(1600, 900));
		pnlMenu.setBounds(0, 0, 1600, 900);
		add(pnlMenu);
		pnlMenu.setLayout(null);
		
		txtpnFiveDoorsAt = new JTextPane();
		txtpnFiveDoorsAt.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		txtpnFiveDoorsAt.setFocusable(false);
		txtpnFiveDoorsAt.setOpaque(false);
		txtpnFiveDoorsAt.setFont(new Font("Tahoma", Font.PLAIN, 40));
		txtpnFiveDoorsAt.setText("Five\r\nDoors\r\nat\r\nFreddy's");
		txtpnFiveDoorsAt.setBounds(112, 40, 201, 211);
		txtpnFiveDoorsAt.setForeground(Color.WHITE);
		pnlMenu.add(txtpnFiveDoorsAt);
		
		btnEmpezar = new JButton("Empezar");
		btnEmpezar.setFocusPainted(false);
		btnEmpezar.setBorderPainted(false);
		btnEmpezar.setContentAreaFilled(false);
		btnEmpezar.setOpaque(false);
		btnEmpezar.setFont(new Font("Tahoma", Font.PLAIN, 30));
		btnEmpezar.setBorder(null);
		btnEmpezar.setBounds(114, 391, 127, 37);
		btnEmpezar.setForeground(Color.WHITE);
		pnlMenu.add(btnEmpezar);
		
		btnOpciones = new JButton("Opciones");
		btnOpciones.setOpaque(false);
		btnOpciones.setFocusPainted(false);
		btnOpciones.setContentAreaFilled(false);
		btnOpciones.setBorderPainted(false);
		btnOpciones.setFont(new Font("Tahoma", Font.PLAIN, 30));
		btnOpciones.setBounds(1339, 833, 164, 37);
		btnOpciones.setForeground(Color.WHITE);
		pnlMenu.add(btnOpciones);
		
		
		lbl = new JLabel(">>");
		lbl.setForeground(Color.WHITE);
		lbl.setFont(new Font("Tahoma", Font.PLAIN, 30));
		lbl.setBounds(1310, 833, 46, 37);
		lbl.setVisible(false);
		pnlMenu.add(lbl);
		
		
		lblMenu = new JLabel("");
		lblMenu.setPreferredSize(new Dimension(1600, 900));
		lblMenu.setBounds(0, 0, 1600, 900);
		pnlMenu.add(lblMenu);

	}

	public void init(VistaPrincipal vp,PnlMenu menu) {
		EscalarVista.adaptarMenu(vp, menu);;
		this.revalidate();
		this.repaint();
	}


	public JPanel getPnlMenu() {
		return pnlMenu;
	}



	public void setPnlMenu(JPanel pnlMenu) {
		this.pnlMenu = pnlMenu;
	}



	public JLabel getLblMenu() {
		return lblMenu;
	}



	public void setLblMenu(JLabel lblMenu) {
		this.lblMenu = lblMenu;
	}



	public JTextPane getTxtpnFiveDoorsAt() {
		return txtpnFiveDoorsAt;
	}



	public void setTxtpnFiveDoorsAt(JTextPane txtpnFiveDoorsAt) {
		this.txtpnFiveDoorsAt = txtpnFiveDoorsAt;
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
