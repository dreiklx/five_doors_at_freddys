package com.fdaf.mvc.views.Jframe.pnl;



import javax.swing.JPanel;

import javax.swing.JLabel;

import javax.swing.SwingConstants;

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

private JButton btnPuertaDer;

private JButton btnPuertaIzq;

private JLabel lblTabAbrir;

private JLabel lblTabCerrar;

private JCheckBox botonIzq;

private JCheckBox botonDer;

private JCheckBox luzDer;

private JCheckBox luzIzq;

private JPanel panelDer;

private JPanel panelIzq;



// overlay único, centrado, fijo en pantalla (agregado al

// panel raíz, NO a pnlOficina ni a panelIzq/Der). Aquí se muestran los

// gifs de animatrónico y las imágenes de coleccionable. panelIzq/Der

// vuelven a usarse EXCLUSIVAMENTE para botones y luces.



private PnlAplicarImagen lblOverlay;

private PnlAplicarImagen lblImgOficina;

private JLabel lblNariz;

private JPanel lblAbanico;





/**

* Create the panel.

*/

public PnlJuego() {

setPreferredSize(new Dimension(1600, 900));

setLayout(null);



// overlay único centrado: (1000-500)/2=250, (600-400)/2=100.

lblOverlay = new PnlAplicarImagen(null);

lblOverlay.setOpaque(false);

lblOverlay.setBounds(0, 0, 129, 124);



lblOverlay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

lblOverlay.setVisible(false);

//lblIzqTres.setOpaque(false);





lblTabAbrir = new JLabel("");

lblTabAbrir.setBounds(288, 786, 690, 77);



lblTabAbrir.setOpaque(false);

lblTabAbrir.setIcon(EscalarVista.getImagenEscalada(CargarImagenes.barraTableta, lblTabAbrir.getWidth(), lblTabAbrir.getHeight()));





panelDer=new PnlAplicarImagen(CargarImagenes.panelDer);

panelDer.setOpaque(false);

panelDer.setBounds(1794, 400,70,210);

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





lblImgOficina = new PnlAplicarImagen(CargarImagenes.fondoJuego);

lblImgOficina.setBounds(-300, -83, 2200, 1020);



lblImgOficina.setLayout(null);



lblNariz = new JLabel("");

lblNariz.setBackground(Color.YELLOW);

lblNariz.setBounds(965, 360, 11, 8);





lblAbanico = new PnlAplicarImagen(CargarImagenes.abanico);



lblAbanico.setBounds(1093, 443, 166, 243);

pnlComponentes = new JPanel();

lblImgOficina.setBounds(-300, -83, 2200, 1020);


pnlComponentes.setOpaque(false);

pnlComponentes.setLayout(null);









lblDerTres = new JLabel("");

lblDerTres.setBounds(1542, 0, 58, 900);

lblDerTres.setIcon(EscalarVista.getImagenEscalada(CargarImagenes.zonaTresDer, lblDerTres.getWidth(), lblDerTres.getHeight()));

lblDerTres.setBackground(Color.green);

lblDerTres.setOpaque(false);







lblIzqTres = new JLabel("");

lblIzqTres.setBounds(0, 0, 58, 900);

lblIzqTres.setIcon(EscalarVista.getImagenEscalada(CargarImagenes.zonaTresIzq, lblIzqTres.getWidth(), lblIzqTres.getHeight()));

lblIzqTres.setBackground(Color.green);

lblIzqTres.setOpaque(false);





lblIzqDos = new JLabel("zona dos");

lblIzqDos.setBackground(Color.white);

lblIzqDos.setBounds(58, 0, 193, 900);

lblIzqDos.setOpaque(false);





lblDerDos = new JLabel("zona dos");

lblDerDos.setBackground(Color.white);

lblDerDos.setBounds(1349, 0, 193, 900);

lblDerDos.setOpaque(false);





lblIzqUno = new JLabel("zona uno");

lblIzqUno.setBackground(Color.RED);

lblIzqUno.setBounds(251, 0, 179, 900);

lblIzqUno.setOpaque(false);





lblDerUno = new JLabel("zona uno");

lblDerUno.setBackground(Color.RED);

lblDerUno.setBounds(1170, 0, 179, 900);

lblDerUno.setOpaque(false);





/*

* Asignar orden añadiendo mannual

*/

add(lblOverlay);




panelDer.add(botonDer);

panelDer.add(luzDer);

pnlComponentes.add(panelDer);



pnlComponentes.add(lblTabAbrir);

pnlComponentes.add(lblDerTres);

pnlComponentes.add(lblIzqTres);

pnlComponentes.add(lblIzqDos);

pnlComponentes.add(lblDerDos);

pnlComponentes.add(lblIzqUno);

pnlComponentes.add(lblDerUno);

add(pnlComponentes);




lblImgOficina.add(lblNariz);

lblImgOficina.add(lblAbanico);

add(lblImgOficina);





}



public JPanel getLblAbanico() {

return lblAbanico;

}



public void setLblAbanico(JPanel lblAbanico) {

this.lblAbanico = lblAbanico;

}



public JLabel getLblNariz() {

return lblNariz;

}



public void setLblNariz(JLabel lblNariz) {

this.lblNariz = lblNariz;

}



public PnlAplicarImagen getLblOverlay() {

return lblOverlay;

}



public void setLblOverlay(PnlAplicarImagen lblOverlay) {

this.lblOverlay = lblOverlay;

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



public PnlAplicarImagen getLblImgOficina() {

return lblImgOficina;

}



public void setLblImgOficina(PnlAplicarImagen lblImgOficina) {

this.lblImgOficina = lblImgOficina;

}

}