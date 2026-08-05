package com.fdaf.mvc.views.frames.pnl;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.fdaf.mvc.views.frames.VistaPrincipal;
import com.fdaf.util.CargarImagenes;
import com.fdaf.util.EscalarVista;

import java.awt.Color;
import javax.swing.JButton;
import java.awt.Dimension;
import javax.swing.JCheckBox;
import java.awt.Cursor;

public class PnlJuego extends JPanel {

	private JLabel lblIzqUno;
	private JLabel lblIzqDos;
	private JLabel lblIzqTres;
	private JLabel lblDerUno;
	private JLabel lblDerDos;
	private JLabel lblDerTres;
	private JPanel pnlComponentes;
	private JLabel lblTabAbrir;
	private JCheckBox botonIzq;
	private JCheckBox botonDer;
	private JCheckBox luzDer;
	private JCheckBox luzIzq;
	private JPanel panelDer;
	private JPanel panelIzq;

	private JLabel lblOverlay;
	private JLabel lblTabletTransicion;
	private JLabel lblImgOficina;
	private JLabel lblNariz;
	private JLabel lblAbanico;
	private JLabel lblPuertaDer;
	private JLabel lblPuertaIzq;
	private JLabel lblPowerLeft;
	private JLabel lblBateria;

	//botón para colgar la llamada telefónica.
	private JLabel lblMuteCall;

	/**
	 * Create the panel.
	 */
	public PnlJuego() {
		setBackground(Color.BLACK);

		setPreferredSize(new Dimension(1600, 900));
		setLayout(null);

		// overlay único centrado: (1000-500)/2=250, (600-400)/2=100.
		lblOverlay = new JLabel("");
		lblOverlay.setOpaque(false);
		lblOverlay.setBounds(0, 0, 129, 124);
		lblOverlay.setVisible(false);

		panelDer=new PnlAplicarImagen(CargarImagenes.panelDer);
		panelDer.setOpaque(false);
		panelDer.setBounds(1739, 400,70,210);
		panelDer.setLayout(null);

		botonDer = new JCheckBox("");
		botonDer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		botonDer.setBounds(5, 15, 48, 65);
		botonDer.setIcon(CargarImagenes.btnDer0);
		botonDer.setOpaque(false);
		botonDer.setHorizontalAlignment(SwingConstants.CENTER);

		luzDer = new JCheckBox("");
		luzDer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		luzDer.setBounds(5, 109, 48, 65);
		luzDer.setIcon(CargarImagenes.luzDer0);
		luzDer.setOpaque(false);
		luzDer.setHorizontalAlignment(SwingConstants.CENTER);

		panelIzq=new PnlAplicarImagen(CargarImagenes.panelIzq);
		panelIzq.setOpaque(false);
		panelIzq.setBounds(-194,400,70,210);
		panelIzq.setLayout(null);

		botonIzq = new JCheckBox("");
		botonIzq.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		botonIzq.setOpaque(false);
		botonIzq.setIcon(CargarImagenes.btnIzq0);
		botonIzq.setHorizontalAlignment(SwingConstants.CENTER);
		botonIzq.setBounds(17, 15, 48, 65);

		luzIzq = new JCheckBox("");
		luzIzq.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		luzIzq.setOpaque(false);
		luzIzq.setIcon(CargarImagenes.luzIzq0);
		luzIzq.setHorizontalAlignment(SwingConstants.CENTER);
		luzIzq.setBounds(17, 108, 48, 65);

		pnlComponentes = new JPanel();
		pnlComponentes.setBounds(0,0,1600,900);
		pnlComponentes.setOpaque(false);
		pnlComponentes.setLayout(null);

		lblIzqDos = new JLabel("zona dos");
		lblIzqDos.setBackground(Color.white);
		lblIzqDos.setBounds(88, 0, 253, 900);
		lblIzqDos.setOpaque(false);

		lblDerDos = new JLabel("zona dos");
		lblDerDos.setBackground(Color.white);
		lblDerDos.setBounds(1256, 0, 253, 900);
		lblDerDos.setOpaque(false);

		lblIzqUno = new JLabel("zona uno");
		lblIzqUno.setBackground(Color.RED);
		lblIzqUno.setBounds(335, 0, 273, 900);
		lblIzqUno.setOpaque(true);

		/*
		 * Asignar orden añadiendo mannual
		 */
		add(lblOverlay);

		lblTabletTransicion = new JLabel("");
		lblTabletTransicion.setOpaque(false);
		lblTabletTransicion.setBounds(0, 0, 1600, 900);
		lblTabletTransicion.setVisible(false);
		add(lblTabletTransicion);
		
		lblIzqTres = new JLabel("");
		lblIzqTres.setBounds(0, 0, 89, 900);
		lblIzqTres.setIcon(EscalarVista.getImagenEscalada(CargarImagenes.zonaTresIzq, lblIzqTres.getWidth(), lblIzqTres.getHeight()));
		lblIzqTres.setBackground(Color.green);
		lblIzqTres.setOpaque(false);
		pnlComponentes.add(lblIzqTres);

		lblDerTres = new JLabel("");
		lblDerTres.setBounds(1511, 0, 89, 900);
		lblDerTres.setIcon(EscalarVista.getImagenEscalada(CargarImagenes.zonaTresDer, lblDerTres.getWidth(), lblDerTres.getHeight()));
		lblDerTres.setBackground(Color.green);
		lblDerTres.setOpaque(false);
		pnlComponentes.add(lblDerTres);

		lblBateria = new JLabel("");
		pnlComponentes.add(lblBateria);
		lblBateria.setBounds(67, 854, 89, 30);

		lblPowerLeft = new JLabel("");
		pnlComponentes.add(lblPowerLeft);
		lblPowerLeft.setBounds(57, 808, 194, 35);
		
		//mutecall
		lblMuteCall = new JLabel("");
		lblMuteCall.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lblMuteCall.setOpaque(false);
		lblMuteCall.setBounds(64, 28, 140, 45);
		lblMuteCall.setVisible(false); // solo visible mientras hay llamada activa
		pnlComponentes.add(lblMuteCall);

		lblTabAbrir = new JLabel("");
		lblTabAbrir.setBounds(288, 808, 800, 55);
		lblTabAbrir.setOpaque(false);
		lblTabAbrir.setIcon(EscalarVista.getImagenEscalada(CargarImagenes.barraTableta, lblTabAbrir.getWidth(), lblTabAbrir.getHeight()));
		pnlComponentes.add(lblTabAbrir);

		lblPuertaDer = new JLabel("");
		lblPuertaDer.setBounds(1435, 0, 203, 900);
		pnlComponentes.add(lblPuertaDer);

		lblPuertaIzq = new JLabel("");
		lblPuertaIzq.setBounds(-39, 0, 203, 900);
		pnlComponentes.add(lblPuertaIzq);

		panelIzq.add(botonIzq);
		panelIzq.add(luzIzq);
		pnlComponentes.add(panelIzq);
		panelDer.add(botonDer);
		panelDer.add(luzDer);
		pnlComponentes.add(panelDer);

		lblImgOficina = new JLabel("");
		lblImgOficina.setBounds(-300, -83, 2200, 1020);
		lblImgOficina.setLayout(null);

		///para agregar componentes con la referencia de la imagen
		//lblImgOficina.setIcon(CargarImagenes.fondoJuego);

		lblNariz = new JLabel("");
		lblNariz.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lblNariz.setBounds(965, 360, 11, 8);

		lblAbanico = new JLabel("");
		lblAbanico.setBounds(1093, 443, 166, 243);
		lblAbanico.setOpaque(false);

		lblImgOficina.add(lblNariz);
		lblImgOficina.add(lblAbanico);

		pnlComponentes.add(lblImgOficina);

		pnlComponentes.add(lblIzqDos);
		pnlComponentes.add(lblDerDos);
		
				lblDerUno = new JLabel("zona uno");
				lblDerUno.setBackground(Color.RED);
				lblDerUno.setBounds(983, 0, 273, 900);
				lblDerUno.setOpaque(true);
				pnlComponentes.add(lblDerUno);
		pnlComponentes.add(lblIzqUno);

		add(pnlComponentes);

	}

	public JLabel getLblMuteCall() {
		return lblMuteCall;
	}

	public void setLblMuteCall(JLabel lblMuteCall) {
		this.lblMuteCall = lblMuteCall;
	}

	public JLabel getLblPowerLeft() {
		return lblPowerLeft;
	}

	public void setLblPowerLeft(JLabel lblPowerLeft) {
		this.lblPowerLeft = lblPowerLeft;
	}

	public JLabel getLblBateria() {
		return lblBateria;
	}

	public void setLblBateria(JLabel lblBateria) {
		this.lblBateria = lblBateria;
	}

	public JLabel getLblPuertaDer() {
		return lblPuertaDer;
	}

	public void setLblPuertaDer(JLabel lblPuertaDer) {
		this.lblPuertaDer = lblPuertaDer;
	}

	public JLabel getLblPuertaIzq() {
		return lblPuertaIzq;
	}

	public void setLblPuertaIzq(JLabel lblPuertaIzq) {
		this.lblPuertaIzq = lblPuertaIzq;
	}

	public void init(VistaPrincipal vp,PnlJuego juego) {
		EscalarVista.adaptarJuego(vp, juego);
		this.revalidate();
		this.repaint();
	}

	public JLabel getLblAbanico() {
		return lblAbanico;
	}

	public void setLblAbanico(JLabel lblAbanico) {
		this.lblAbanico = lblAbanico;
	}

	public JLabel getLblNariz() {
		return lblNariz;
	}

	public void setLblNariz(JLabel lblNariz) {
		this.lblNariz = lblNariz;
	}

	public JLabel getLblOverlay() {
		return lblOverlay;
	}

	public void setLblOverlay(JLabel lblOverlay) {
		this.lblOverlay = lblOverlay;
	}
	public JLabel getLblTabletTransicion() {
		return lblTabletTransicion;
	}

	public void setLblTabletTransicion(JLabel lblTabletTransicion) {
		this.lblTabletTransicion = lblTabletTransicion;
	}

	public JPanel getPanelDer() {
		return panelDer;
	}

	public void setPanelDer(JPanel panelDer) {
		this.panelDer = panelDer;
	}

	public JPanel getPanelIzq() {
		return panelIzq;
	}

	public void setPanelIzq(JPanel panelIzq) {
		this.panelIzq = panelIzq;
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

	public JPanel getPnlComponentes() {
		return pnlComponentes;
	}

	public void setPnlComponentes(JPanel pnlComponentes) {
		this.pnlComponentes = pnlComponentes;
	}

	public JLabel getLblImgOficina() {
		return lblImgOficina;
	}

	public void setLblImgOficina(JLabel lblImgOficina) {
		this.lblImgOficina = lblImgOficina;
	}
}