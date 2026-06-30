package com.fdaf.util;




import java.awt.Toolkit;

import javax.swing.ImageIcon;


public class CargarImagenes {


	public CargarImagenes() {
		
	}
	
		/*
		 * Gifs
		*/
	// Ejemplo de cómo debería verse el bloque de inicialización en tu clase de carga:

	public static ImageIcon juegoLuzIzq;
	public static ImageIcon juegoLuzDer;
	public static ImageIcon abanico;
	public static ImageIcon menu;
	public static ImageIcon estatica;
	public static ImageIcon fondoJuego;
	public static ImageIcon zonaTresIzq;
	public static ImageIcon zonaTresDer;
	public static ImageIcon barraTableta;
	public static ImageIcon panelIzq;
	public static ImageIcon panelDer;
	public static ImageIcon btnIzq0;
	public static ImageIcon btnDer0;
	public static ImageIcon btnIzq1;
	public static ImageIcon btnDer1;
	public static ImageIcon luzIzq0;
	public static ImageIcon luzDer0;
	public static ImageIcon luzIzq1;
	public static ImageIcon luzDer1;
	public static ImageIcon puertaDer0;
	public static ImageIcon puertaIzq0;
	public static ImageIcon puertaDer1;
	public static ImageIcon puertaIzq1;

	public static ImageIcon bateria0;
	public static ImageIcon bateria1;
	public static ImageIcon bateria2;
	public static ImageIcon bateria3;
	public static ImageIcon bateria4;
	public static ImageIcon powerLeft;


	static {

	    juegoLuzIzq = new ImageIcon(CargarImagenes.class.getResource("/gifs/fondos/OficinaLuzIzq.gif"));
	    juegoLuzDer = new ImageIcon(CargarImagenes.class.getResource("/gifs/fondos/OficinaLuzDer.gif"));
	    abanico = new ImageIcon(CargarImagenes.class.getResource("/gifs/fondos/abanico.gif"));


	    Toolkit.getDefaultToolkit().prepareImage(juegoLuzIzq.getImage(), -1, -1, null);
	    Toolkit.getDefaultToolkit().prepareImage(juegoLuzDer.getImage(), -1, -1, null);
	    Toolkit.getDefaultToolkit().prepareImage(abanico.getImage(), -1, -1, null);
	

 menu=new ImageIcon(CargarImagenes.class.getResource("/gifs/fondos/Menu.gif"));
estatica=new ImageIcon(CargarImagenes.class.getResource("/gifs/fondos/Static.gif"));

	
	/*
	 * Fondos
	 */
	
fondoJuego=new ImageIcon(CargarImagenes.class.getResource("/images/fondos/FondoJuego.png"));
 zonaTresIzq=new ImageIcon(CargarImagenes.class.getResource("/images/fondos/left.png"));
zonaTresDer=new ImageIcon(CargarImagenes.class.getResource("/images/fondos/right.png"));
	
	
	/*
	 * ui
	 */
	
	/*botones*/
barraTableta=new ImageIcon(CargarImagenes.class.getResource("/images/ui/botones/barraTablet.fw.png"));
panelIzq=new ImageIcon(CargarImagenes.class.getResource("/images/ui/botones/pnlBotonesIzq.fw.png"));
panelDer=new ImageIcon(CargarImagenes.class.getResource("/images/ui/botones/pnlBotonesDer.fw.png"));
	
btnIzq0=new ImageIcon(CargarImagenes.class.getResource("/images/ui/botones/btnPuertaIzq0.png"));
 btnDer0=new ImageIcon(CargarImagenes.class.getResource("/images/ui/botones/btnPuertaDer0.fw.png"));
 btnIzq1=new ImageIcon(CargarImagenes.class.getResource("/images/ui/botones/btnPuertaIzq1.png"));
btnDer1=new ImageIcon(CargarImagenes.class.getResource("/images/ui/botones/btnPuertaDer1png.fw.png"));
	
	
luzIzq0=new ImageIcon(CargarImagenes.class.getResource("/images/ui/botones/btnLuzIzq0.png"));
luzDer0=new ImageIcon(CargarImagenes.class.getResource("/images/ui/botones/btnLuzDer0.fw.png"));
luzIzq1=new ImageIcon(CargarImagenes.class.getResource("/images/ui/botones/btnLuzIzq1.png"));
luzDer1=new ImageIcon(CargarImagenes.class.getResource("/images/ui/botones/btnLuzDer1.fw.png"));



	
	/*puertas*/
puertaDer0=new ImageIcon(CargarImagenes.class.getResource("/images/ui/puertas/puertaDer.png"));
puertaIzq0=new ImageIcon(CargarImagenes.class.getResource("/images/ui/puertas/puertaIzq.png"));
puertaDer1=new ImageIcon(CargarImagenes.class.getResource("/images/ui/puertas/103.png"));
puertaIzq1=new ImageIcon(CargarImagenes.class.getResource("/images/ui/puertas/104.png"));

powerLeft=new ImageIcon(CargarImagenes.class.getResource("/images/hud/powerLeft.png"));
bateria0=new ImageIcon(CargarImagenes.class.getResource("/images/hud/bateria0.png"));
bateria1=new ImageIcon(CargarImagenes.class.getResource("/images/hud/bateria1.png"));
bateria2=new ImageIcon(CargarImagenes.class.getResource("/images/hud/bateria2.png"));
bateria3=new ImageIcon(CargarImagenes.class.getResource("/images/hud/bateria3.png"));
bateria4=new ImageIcon(CargarImagenes.class.getResource("/images/hud/bateria4.png"));

	}

	

}
