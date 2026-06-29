package com.fdaf.mvc.controllers;



import java.awt.Dimension;

import java.awt.event.MouseAdapter;

import java.awt.event.MouseEvent;



import javax.swing.ImageIcon;

import javax.swing.JLabel;

import javax.swing.JOptionPane;

import com.fdaf.mvc.models.animatronicos.Animatronico;

import com.fdaf.mvc.models.coleccionables.Coleccionable;

import com.fdaf.util.CargarImagenes;

import com.fdaf.util.EscalarVista;

import com.fdaf.mvc.views.Jframe.VistaPrincipal;

import com.fdaf.mvc.views.Jframe.pnl.PnlAplicarImagen;

import com.fdaf.mvc.views.Jframe.pnl.PnlJuego;

import com.fdaf.mvc.views.Jframe.pnl.PnlMenu;

import com.fdaf.mvc.views.Jframe.pnl.PnlTableta;



// ControllerInterfaz es DUEÑO de los paneles y de los demás controladores,

// conecta los listeners de gameplay, y ejecuta TODAS las órdenes visuales

// que ControllerJuego le pide vía JuegoListener. No toma decisiones de

// reglas. Se crea NUEVA en cada partida (ver ControllerMenu), por lo que

// sus paneles y listeners siempre arrancan limpios.

public class ControllerInterfaz implements JuegoListener {



private PnlJuego pnlJuego;

private PnlTableta pnlTableta;



private ControllerJuego controllerJuego;

private ControllerCamara controllerCamara;



public ControllerInterfaz() {

pnlJuego = new PnlJuego();

pnlTableta = new PnlTableta();



controllerJuego = new ControllerJuego();

controllerJuego.setListener(this);

controllerJuego.init();



controllerCamara = new ControllerCamara(pnlJuego, pnlTableta);

}



public void init(VistaPrincipal vp, PnlMenu menu) {

controllerCamara.init(vp, menu);



conectarPuertas();

conectarLuces();

}



private void conectarPuertas() {

pnlJuego.getBotonIzq().addActionListener(e -> controllerJuego.abrirIzquierda());

pnlJuego.getBotonDer().addActionListener(e -> controllerJuego.abrirDerecha());

}



private void conectarLuces() {

pnlJuego.getLuzIzq().addActionListener(e -> controllerJuego.revelarIzquierda());

pnlJuego.getLuzDer().addActionListener(e -> controllerJuego.revelarDerecha());



pnlJuego.getLblOverlay().addMouseListener(new MouseAdapter() {

@Override

public void mouseClicked(MouseEvent e) {

controllerJuego.recogerColeccionable();

}

});

}



/*

* ==== Implementación de JuegoListener (ejecución visual pura) ====

*/



@Override

public void alAbrirPuerta(String lado) {

if ("izq".equals(lado)) {

pnlJuego.getBotonIzq().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.btnIzq1,

pnlJuego.getBotonIzq().getWidth(), pnlJuego.getBotonIzq().getHeight()));



} else {

pnlJuego.getBotonDer().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.btnDer1,

pnlJuego.getBotonDer().getWidth(), pnlJuego.getBotonDer().getHeight()));



}

}



@Override

public void alEncenderLuz(String lado) {



if ("izq".equals(lado)) {

pnlJuego.getLuzIzq().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.luzIzq1,

pnlJuego.getLuzIzq().getWidth(), pnlJuego.getLuzIzq().getHeight()));


pnlJuego.getLblImgOficina().setImagen(CargarImagenes.juegoLuzIzq);

pnlJuego.revalidate();

pnlJuego.repaint();

} else {

pnlJuego.getLuzDer().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.luzDer1,

pnlJuego.getLuzDer().getWidth(), pnlJuego.getLuzDer().getHeight()));


// pnlJuego.getLblImgOficina().setIcon(CargarImagenes.juegoLuzDer);

pnlJuego.getLblImgOficina().setImagen(CargarImagenes.juegoLuzDer);

pnlJuego.revalidate();

pnlJuego.repaint();

}



}



@Override

public void alRevelarAnimatronico(String lado, Animatronico animatronico) {

String nombre = animatronico.name();

String capitalizado = nombre.charAt(0) + nombre.substring(1).toLowerCase();

String ruta = "/gifs/jumpscares/Jumpscare" + capitalizado + ".gif";

mostrarEnOverlay(ruta,1);

}



@Override

public void alRevelarColeccionable(String lado, Coleccionable coleccionable) {

String ruta = "/images/" + coleccionable.getArchivoImagen();

mostrarEnOverlay(ruta,0);

}



@Override

public void alRevelarNada(String lado) {

// Sin ícono: la oscuridad se mantiene.

}



private void mostrarEnOverlay(String ruta,int c) {

java.net.URL recurso = getClass().getResource(ruta);

if (recurso == null) {

System.out.println("[RECURSO NO ENCONTRADO] " + ruta);

return;

}


PnlAplicarImagen overlay = pnlJuego.getLblOverlay();

if(c!=0) {

overlay.setBounds(0, 0,EscalarVista.getEscalaX(2200), EscalarVista.getEscalaY(1020));

}else {


}


overlay.setImagen(new ImageIcon(recurso));

overlay.setVisible(true);

pnlJuego.setComponentZOrder(overlay, 0);

overlay.repaint();

pnlJuego.repaint();

}



@Override

public void alRecogerColeccionable(Coleccionable coleccionable) {

String ruta = "/images/" + coleccionable.getArchivoImagen();

java.net.URL recurso = getClass().getResource(ruta);

if (recurso == null) {

System.out.println("[RECURSO NO ENCONTRADO] " + ruta);

return;

}

JLabel item = new JLabel(EscalarVista.getImagenEscalada(new ImageIcon(recurso), 80, 80));

item.setPreferredSize(new Dimension(80, 80));

pnlTableta.getPnlColeccionables().add(item);

pnlTableta.getPnlColeccionables().revalidate();

pnlTableta.getPnlColeccionables().repaint();

}



@Override

public void alLiberar(String lado) {

PnlAplicarImagen fondo=pnlJuego.getLblImgOficina();

PnlAplicarImagen overlay = pnlJuego.getLblOverlay();

overlay.setVisible(false);

overlay.setImagen(null);



if ("izq".equals(lado)) {

pnlJuego.getLuzIzq().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.luzIzq0,

pnlJuego.getLuzIzq().getWidth(), pnlJuego.getLuzIzq().getHeight()));


pnlJuego.getLblImgOficina().setImagen(CargarImagenes.fondoJuego);


pnlJuego.getBotonIzq().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.btnIzq0,

pnlJuego.getBotonIzq().getWidth(), pnlJuego.getBotonIzq().getHeight()));

} else {

pnlJuego.getLuzDer().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.luzDer0,

pnlJuego.getLuzDer().getWidth(), pnlJuego.getLuzDer().getHeight()));


pnlJuego.getLblImgOficina().setImagen(CargarImagenes.fondoJuego);


pnlJuego.getBotonDer().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.btnDer0,

pnlJuego.getBotonDer().getWidth(), pnlJuego.getBotonDer().getHeight()));

}

pnlJuego.revalidate();

pnlJuego.repaint();

}



@Override

public void alPerder() {

deshabilitarControles();

JOptionPane.showMessageDialog(null, "GAME OVER");

}



@Override

public void alGanar() {

deshabilitarControles();

JOptionPane.showMessageDialog(null, "¡GANASTE! Encontraste todos los coleccionables.");

}



private void deshabilitarControles() {

pnlJuego.getBotonIzq().setEnabled(false);

pnlJuego.getBotonDer().setEnabled(false);

pnlJuego.getLuzIzq().setEnabled(false);

pnlJuego.getLuzDer().setEnabled(false);

}



public PnlJuego getPnlJuego() {

return pnlJuego;

}



public PnlTableta getPnlTableta() {

return pnlTableta;

}



} 

