package com.fdaf.mvc.views.Jframe.pnl;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.fdaf.mvc.views.multimedia.Sprite;
import com.fdaf.util.CargarImagenes;

import java.awt.Label;
import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.Dimension;
import javax.swing.JCheckBox;

public class PnlJuego extends JPanel {
	private JLabel lblOficina;
	private JLabel lblIzqUno;
	private JLabel lblIzqDos;
	private JLabel lblIzqTres;
	private JLabel lblDerUno;
	private JLabel lblDerDos;
	private JLabel lblDerTres;
	public JButton btnNewButton;
	private JPanel pnlOficina;
	private JButton btnPuertaDer;
	private JButton btnPuertaIzq;
	private JLabel lblTabAbrir;
	private JLabel lblTabCerrar;
	private JCheckBox botonIzq;
	private JCheckBox botonDer;
	private JCheckBox luzDer;
	private JCheckBox luzIzq;
	

	/**
	 * Create the panel.
	 */
	public PnlJuego() {
		setLayout(null);
		

		
		pnlOficina = new JPanel();
		pnlOficina.setBounds(0, 0, 1000, 600);
		add(pnlOficina);
		pnlOficina.setLayout(null);
		
		botonDer = new JCheckBox("");
		botonDer.setOpaque(false);
		botonDer.setHorizontalAlignment(SwingConstants.CENTER);
		botonDer.setIcon(CargarImagenes.puertaIzq0);
		botonDer.setBounds(1150, 266, 60, 88);
		pnlOficina.add(botonDer);
		
		luzDer = new JCheckBox("");
		luzDer.setOpaque(false);
		luzDer.setHorizontalAlignment(SwingConstants.CENTER);
		luzDer.setIcon(CargarImagenes.luzIzq0);
		luzDer.setBounds(1150, 354, 60, 88);
		pnlOficina.add(luzDer);
		
		botonIzq = new JCheckBox("");
		botonIzq.setOpaque(false);
		botonIzq.setHorizontalAlignment(SwingConstants.CENTER);
		botonIzq.setIcon(CargarImagenes.puertaIzq0);
		botonIzq.setBounds(-194, 266, 60, 88);
		pnlOficina.add(botonIzq);
		
		luzIzq = new JCheckBox("");
		luzIzq.setOpaque(false);
		luzIzq.setHorizontalAlignment(SwingConstants.CENTER);
		luzIzq.setIcon(CargarImagenes.luzIzq0);
		luzIzq.setBounds(-194, 354, 60, 88);
		pnlOficina.add(luzIzq);
		
		lblTabAbrir = new JLabel("");
		lblTabAbrir.setBounds(203, 547, 375, 42);
		lblTabAbrir.setOpaque(false);
		lblTabAbrir.setIcon(CargarImagenes.barraTableta);
		pnlOficina.add(lblTabAbrir);
		
		
		btnNewButton = new JButton("New button");
		btnNewButton.setBounds(443, 60, 89, 23);
		pnlOficina.add(btnNewButton);
		
//		btnPuertaDer=new JButton();
//		btnPuertaDer.setBounds(1150, 266, 50, 60);
//		btnPuertaDer.setBackground(Color.blue);
//		pnlOficina.add(btnPuertaDer);
//		
//		btnPuertaIzq = new JButton();
//		btnPuertaIzq.setBackground(Color.BLUE);
//		btnPuertaIzq.setBounds(-194, 266, 50, 60);
//		pnlOficina.add(btnPuertaIzq);
		
		lblOficina = new JLabel("");
		lblOficina.setIcon(new ImageIcon(PnlJuego.class.getResource("/images/fondos/FondoJuego.png")));
		lblOficina.setBackground(Color.WHITE);
		lblOficina.setHorizontalAlignment(SwingConstants.CENTER);
		lblOficina.setBounds(-292, -59, 1600, 676);
		pnlOficina.add(lblOficina);
		
		lblIzqTres = new JLabel("");
		lblIzqTres.setOpaque(true);
		lblIzqTres.setBackground(Color.green);
		//lblIzqTres.setIcon(new ImageIcon(PnlJuego.class.getResource("/images/fondos/zonaTresIzq.png")));
		lblIzqTres.setBounds(0, 0, 65, 600);
		pnlOficina.add(lblIzqTres);
		
		lblDerTres = new JLabel("");
		lblDerTres.setOpaque(true);
		lblDerTres.setBackground(Color.green);
		//lblDerTres.setIcon(new ImageIcon(PnlJuego.class.getResource("/images/fondos/zonaTresDer.png")));
		
		lblDerTres.setBounds(935, 0, 65, 600);
		pnlOficina.add(lblDerTres);
		
		lblIzqDos = new JLabel("zona dos");
		lblIzqDos.setMinimumSize(new Dimension(44, 14));
		lblIzqDos.setMaximumSize(new Dimension(44, 14));
		lblIzqDos.setPreferredSize(new Dimension(44, 14));
		lblIzqDos.setOpaque(true);
		lblIzqDos.setBackground(Color.white);
		lblIzqDos.setBounds(65, 0, 163, 600);
		pnlOficina.add(lblIzqDos);
		
		lblDerDos = new JLabel("zona dos");
		lblDerDos.setMinimumSize(new Dimension(44, 14));
		lblDerDos.setMaximumSize(new Dimension(44, 14));
		lblDerDos.setPreferredSize(new Dimension(44, 14));
		lblDerDos.setOpaque(true);
		lblDerDos.setBackground(Color.white);
		lblDerDos.setBounds(773, 0, 163, 600);
		pnlOficina.add(lblDerDos);
		
		lblIzqUno = new JLabel("zona uno");
		lblIzqUno.setOpaque(true);
		lblIzqUno.setBackground(Color.RED);
		lblIzqUno.setBounds(229, 0, 163, 600);
		pnlOficina.add(lblIzqUno);
		
		lblDerUno = new JLabel("zona uno");
		lblDerUno.setOpaque(true);
		lblDerUno.setBackground(Color.RED);
		lblDerUno.setBounds(610, 0, 163, 600);
		pnlOficina.add(lblDerUno);

	}

	public JCheckBox getLuzDer() {
		return luzDer;
	}

	public void setLuzDer(JCheckBox luzDer) {
		this.luzDer = luzDer;
	}

	public JCheckBox getLuzIzq() {
		return luzIzq;
	}

	public void setLuzIzq(JCheckBox luzIzq) {
		this.luzIzq = luzIzq;
	}

	public JCheckBox getBotonIzq() {
		return botonIzq;
	}

	public void setBotonIzq(JCheckBox botonIzq) {
		this.botonIzq = botonIzq;
	}

	public JCheckBox getBotonDer() {
		return botonDer;
	}

	public void setBotonDer(JCheckBox botonDer) {
		this.botonDer = botonDer;
	}

	public JLabel getLblTabAbrir() {
		return lblTabAbrir;
	}

	public void setLblTabAbrir(JLabel lblTabAbrir) {
		this.lblTabAbrir = lblTabAbrir;
	}

	public JLabel getLblTabCerrar() {
		return lblTabCerrar;
	}

	public void setLblTabCerrar(JLabel lblTabCerrar) {
		this.lblTabCerrar = lblTabCerrar;
	}

	public JButton getBtnPuertaIzq() {
		return btnPuertaIzq;
	}

	public void setBtnPuertaIzq(JButton btnPuertaIzq) {
		this.btnPuertaIzq = btnPuertaIzq;
	}

	public JButton getBtnPuertaDer() {
		return btnPuertaDer;
	}

	public void setBtnPuertaDer(JButton btnPuertaDer) {
		this.btnPuertaDer = btnPuertaDer;
	}

	public JLabel getLblOficina() {
		return lblOficina;
	}

	public void setLblOficina(JLabel lblOficina) {
		this.lblOficina = lblOficina;
	}

	public JLabel getLblIzqUno() {
		return lblIzqUno;
	}

	public void setLblIzqUno(JLabel lblIzqUno) {
		this.lblIzqUno = lblIzqUno;
	}

	public JLabel getLblIzqDos() {
		return lblIzqDos;
	}

	public void setLblIzqDos(JLabel lblIzqDos) {
		this.lblIzqDos = lblIzqDos;
	}

	public JLabel getLblIzqTres() {
		return lblIzqTres;
	}

	public void setLblIzqTres(JLabel lblIzqTres) {
		this.lblIzqTres = lblIzqTres;
	}

	public JLabel getLblDerUno() {
		return lblDerUno;
	}

	public void setLblDerUno(JLabel lblDerUno) {
		this.lblDerUno = lblDerUno;
	}

	public JLabel getLblDerDos() {
		return lblDerDos;
	}

	public void setLblDerDos(JLabel lblDerDos) {
		this.lblDerDos = lblDerDos;
	}

	public JLabel getLblDerTres() {
		return lblDerTres;
	}

	public void setLblDerTres(JLabel lblDerTres) {
		this.lblDerTres = lblDerTres;
	}
}
