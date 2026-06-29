package com.fdaf.util;



import java.awt.Component;

import java.awt.Container;

import java.awt.Dimension;

import java.awt.Image;

import java.awt.Toolkit;

import java.io.InputStream;

import java.util.Arrays;

import java.util.List;



import javax.swing.ImageIcon;

import javax.swing.JButton;

import javax.swing.JCheckBox;

import javax.swing.JLabel;

import javax.swing.JPanel;

import javax.swing.JProgressBar;

import javax.swing.JTextPane;



import com.fdaf.mvc.views.Jframe.VistaPrincipal;

import com.fdaf.mvc.views.Jframe.pnl.PnlJuego;

import com.fdaf.mvc.views.Jframe.pnl.PnlMenu;

import com.fdaf.mvc.views.Jframe.pnl.PnlOpciones;

import com.fdaf.mvc.views.Jframe.pnl.PnlTableta;



public class EscalarVista {



public static ImageIcon getImagenEscalada(ImageIcon archivo, int ancho, int alto) {



ImageIcon icon = archivo;





Image img = icon.getImage();

Image imgEscalada = img.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);



return new ImageIcon(imgEscalada);

}



public static int getEscalaX(double componente){

Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();

int ancho= pantalla.width;



double escalaX = (double) ancho / 1599;




return (int)(componente * escalaX);

}


public static int getEscalaY(double componente){

Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();

int alto = pantalla.height; 



double escalaY = (double) alto / 900;


return (int)(componente * escalaY);

}



public static void adaptarInicio(VistaPrincipal vp,PnlMenu menu,PnlOpciones opciones) {

Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();

int alto = pantalla.height; 

int ancho= pantalla.width;

System.out.println("base ancho"+ancho);

System.out.println("base alto"+alto);



JButton btnOpciones=menu.getBtnOpciones();

JButton btnAtras=opciones.getBtnAtras();

JButton btnSalir=opciones.getBtnSalir();





vp.setBounds(0, 0, ancho, alto);

/*

* escalar parte del menu

*/

menu.setBounds(0,0,ancho,alto);

menu.getPnlMenu().setBounds(0, 0, ancho, alto);;

menu.getLblMenu().setBounds(0, 0, ancho, alto);





menu.getBtnOpciones().setBounds(getEscalaX(btnOpciones.getX()), getEscalaY(btnOpciones.getY()), 

getEscalaX(btnOpciones.getWidth()), getEscalaY(btnOpciones.getHeight()));







/*

* escalar parte de opciones

*/

opciones.setBounds(0,0,ancho,alto);

opciones.getPnlOpciones().setBounds(0, 0, ancho, alto);;



opciones.getBtnAtras().setBounds(getEscalaX(btnAtras.getX()),getEscalaX(btnAtras.getY()),

getEscalaX(btnAtras.getWidth()), getEscalaY(btnAtras.getHeight()));





opciones.getBtnSalir().setBounds(getEscalaX(btnSalir.getX()),getEscalaX(btnSalir.getY()),

getEscalaX(btnSalir.getWidth()), getEscalaY(btnSalir.getHeight()));









}

public static void adaptarJuego(VistaPrincipal vp,PnlJuego juego,PnlTableta tablet) {

Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();

int alto = pantalla.height; 

int ancho= pantalla.width;

/*

* juego

*/

JLabel zonaUnoIzq=juego.getLblIzqUno();

JLabel zonaDosIzq=juego.getLblIzqDos();

JLabel zonaTresIzq=juego.getLblIzqTres();


JLabel zonaUnoDer=juego.getLblDerUno();

JLabel zonaDosDer=juego.getLblDerDos();

JLabel zonaTresDer=juego.getLblDerTres();


JPanel abanico=juego.getLblAbanico();

JLabel tableta=juego.getLblTabAbrir();

JPanel pIzq=juego.getPanelIzq();

JPanel pDer=juego.getPanelDer();

JCheckBox bIzq=juego.getBotonIzq();

JCheckBox lIzq=juego.getLuzIzq();

JCheckBox bDer=juego.getBotonDer();

JCheckBox lDer=juego.getLuzDer();




/*

* tablet

*/


JProgressBar barra=tablet.getPbarRendirse();

JLabel cerrar=tablet.getLblTabletCerrar();




/*

* escalar parte del Juego

*/

juego.setBounds(0,0,ancho,alto);

juego.getPnlComponentes().setBounds(0,0, ancho, alto);;

juego.getLblImgOficina().setBounds(getEscalaX(-300),getEscalaY(-83),getEscalaX(2200), getEscalaY(1020));


juego.getLblAbanico().setBounds(getEscalaX(abanico.getX()), getEscalaY(abanico.getY()),

getEscalaX(abanico.getWidth()), getEscalaY(abanico.getHeight()));




juego.getLblTabAbrir().setBounds(getEscalaX(tableta.getX()),getEscalaY(tableta.getY()),

getEscalaX(tableta.getWidth()), getEscalaY(tableta.getHeight()));

juego.getLblTabAbrir().setIcon(getImagenEscalada(CargarImagenes.barraTableta,

juego.getLblTabAbrir().getWidth(), juego.getLblTabAbrir().getHeight()));


//panel de botones

//Izquierda

System.out.println("izquierda "+juego.getPanelIzq().getX());

juego.getPanelIzq().setBounds(getEscalaX(pIzq.getX()),getEscalaY(pIzq.getY()),getEscalaX(pIzq.getWidth()),getEscalaY(pIzq.getHeight()));

System.out.println("izquierda "+juego.getPanelIzq().getX());

////botones

///////////puerta

juego.getBotonIzq().setBounds(getEscalaX(bIzq.getX()),getEscalaY(bIzq.getY()),getEscalaX(bIzq.getWidth()),getEscalaY(bIzq.getHeight()));

juego.getBotonIzq().setIcon(getImagenEscalada(CargarImagenes.btnIzq0,

juego.getBotonIzq().getWidth(), juego.getBotonIzq().getHeight()));

///////////


///////////luz

juego.getLuzIzq().setBounds(getEscalaX(lIzq.getX()),getEscalaY(lIzq.getY()),getEscalaX(lIzq.getWidth()),getEscalaY(lIzq.getHeight()));

juego.getLuzIzq().setIcon(getImagenEscalada(CargarImagenes.luzIzq0,

juego.getLuzIzq().getWidth(), juego.getLuzIzq().getHeight()));

//////////

////

//


//Derecha

System.out.println("derecha "+juego.getPanelDer().getX());

juego.getPanelDer().setBounds(getEscalaX(pDer.getX()),getEscalaY(pDer.getY()),getEscalaX(pDer.getWidth()),getEscalaY(pDer.getHeight()));

System.out.println("derecha "+juego.getPanelDer().getX());

////botones

///////////puerta

juego.getBotonDer().setBounds(getEscalaX(bDer.getX()),getEscalaY(bDer.getY()),getEscalaX(bDer.getWidth()),getEscalaY(bDer.getHeight()));

juego.getBotonDer().setIcon(getImagenEscalada(CargarImagenes.btnDer0,

juego.getBotonDer().getWidth(), juego.getBotonDer().getHeight()));

//////////


//////////luz

juego.getLuzDer().setBounds(getEscalaX(lDer.getX()),getEscalaY(lDer.getY()),getEscalaX(lDer.getWidth()),getEscalaY(lDer.getHeight()));

juego.getLuzDer().setIcon(getImagenEscalada(CargarImagenes.luzDer0,

juego.getLuzDer().getWidth(), juego.getLuzDer().getHeight()));

//////////

////

//





//////////////////////////////////////////////ZONAS DE INTERFERENCIA/////////////////////////////////////////////////////

//izquierda

juego.getLblIzqUno().setBounds(getEscalaX(zonaUnoIzq.getX()), getEscalaY(zonaUnoIzq.getY()), 

getEscalaX(zonaUnoIzq.getWidth()), getEscalaY(zonaUnoIzq.getHeight()));


juego.getLblIzqDos().setBounds(getEscalaX(zonaDosIzq.getX()), getEscalaY(zonaDosIzq.getY()), 

getEscalaX(zonaDosIzq.getWidth()), getEscalaY(zonaDosIzq.getHeight()));


juego.getLblIzqTres().setBounds(getEscalaX(zonaTresIzq.getX()), getEscalaY(zonaTresIzq.getY()), 

getEscalaX(zonaTresIzq.getWidth()), getEscalaY(zonaTresIzq.getHeight()));

juego.getLblIzqTres().setIcon(getImagenEscalada(CargarImagenes.zonaTresIzq,

juego.getLblIzqTres().getWidth(), juego.getLblIzqTres().getHeight()));



//derecha

juego.getLblDerUno().setBounds(getEscalaX(zonaUnoDer.getX()), getEscalaY(zonaUnoDer.getY()), 

getEscalaX(zonaUnoDer.getWidth()), getEscalaY(zonaUnoDer.getHeight()));


juego.getLblDerDos().setBounds(getEscalaX(zonaDosDer.getX()), getEscalaY(zonaDosDer.getY()), 

getEscalaX(zonaDosDer.getWidth()), getEscalaY(zonaDosDer.getHeight()));


juego.getLblDerTres().setBounds(getEscalaX(zonaTresDer.getX()), getEscalaY(zonaTresDer.getY()), 

getEscalaX(zonaTresDer.getWidth()), getEscalaY(zonaTresDer.getHeight()));

juego.getLblDerTres().setIcon(getImagenEscalada(CargarImagenes.zonaTresDer,

juego.getLblDerTres().getWidth(), juego.getLblDerTres().getHeight()));

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////





// /*

// * escalar parte de la tableta 

// */

tablet.setBounds(0,0,ancho,alto);

tablet.getPanel().setBounds(0, 0, ancho, alto);;



tablet.getPbarRendirse().setBounds(getEscalaX(barra.getX()),getEscalaY(barra.getY()),

getEscalaX(barra.getWidth()), getEscalaY(barra.getHeight()));





tablet.getLblTabletCerrar().setBounds(getEscalaX(cerrar.getX()),getEscalaY(cerrar.getY()),

getEscalaX(cerrar.getWidth()), getEscalaY(cerrar.getHeight()));

tablet.getLblTabletCerrar().setIcon(getImagenEscalada(CargarImagenes.barraTableta,

tablet.getLblTabletCerrar().getWidth(), tablet.getLblTabletCerrar().getHeight()));










}









}