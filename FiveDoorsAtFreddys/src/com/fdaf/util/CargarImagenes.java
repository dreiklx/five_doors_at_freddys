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
	public static ImageIcon camara;
	public static ImageIcon puntoCamara;
	
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
	public static ImageIcon puertaIzqCE;
	public static ImageIcon puertaIzqAB;
	public static ImageIcon puertaDerCE;
	public static ImageIcon puertaDerAB;

	public static ImageIcon bateria0;
	public static ImageIcon bateria1;
	public static ImageIcon bateria2;
	public static ImageIcon bateria3;
	public static ImageIcon bateria4;
	public static ImageIcon powerLeft;
	public static ImageIcon mutecall;
	public static ImageIcon inventario;
	public static ImageIcon WIN;
	public static ImageIcon fondoNegro;
	public static ImageIcon gameOverFinal;
	public static ImageIcon warning;

	// Optimizacion de memoria investigada 2026-08-06 (el usuario reporto consumo excesivo en
	// partidas largas -- "sospecho que la gran cantidad de gifs influye bastante"): gameOver,
	// JUMP_SCARE, introduccion y estatica (mas abajo) NUNCA se pintan en pantalla realmente --
	// su UNICO uso real en todo el proyecto es como "respaldo" de CargarGifs.cargarFresco(ruta,
	// respaldo), que solo se devuelve si la carga real desde bytes frescos falla (practicamente
	// nunca, para recursos empaquetados con el build). Como argumento de metodo, Java evalua
	// "new ImageIcon(url)" ANTES de la llamada, sin importar si el respaldo termina usandose --
	// eso forzaba decodificar 4 gifs completos (cada uno con su propio hilo "Image Animator" de
	// AWT corriendo para siempre) en cada arranque, sin que ninguno se mostrara jamas en el
	// camino normal. Diferidos a la primera vez que realmente se necesiten (que en la practica es
	// nunca) en vez de al arrancar la app.
	private static ImageIcon gameOverRespaldo;
	private static ImageIcon jumpScareRespaldo;
	private static ImageIcon introduccionRespaldo;
	private static ImageIcon estaticaRespaldo;

	public static ImageIcon getGameOverRespaldo() {
		if (gameOverRespaldo == null) {
			gameOverRespaldo = new ImageIcon(CargarImagenes.class.getResource("/gifs/fondos/gameover.gif"));
		}
		return gameOverRespaldo;
	}

	public static ImageIcon getJumpScareRespaldo() {
		if (jumpScareRespaldo == null) {
			jumpScareRespaldo = new ImageIcon(CargarImagenes.class.getResource("/gifs/jumpscares/JumpscareFreddySinLuz.gif"));
		}
		return jumpScareRespaldo;
	}

	public static ImageIcon getIntroduccionRespaldo() {
		if (introduccionRespaldo == null) {
			introduccionRespaldo = new ImageIcon(CargarImagenes.class.getResource("/gifs/fondos/empezar.gif"));
		}
		return introduccionRespaldo;
	}

	public static ImageIcon getEstaticaRespaldo() {
		if (estaticaRespaldo == null) {
			estaticaRespaldo = new ImageIcon(CargarImagenes.class.getResource("/gifs/fondos/Static.gif"));
		}
		return estaticaRespaldo;
	}

	static {
		WIN=new ImageIcon(CargarImagenes.class.getResource("/images/fondos/win.png"));
		inventario = new ImageIcon(CargarImagenes.class.getResource("/images/fondos/inventario.png"));
		fondoNegro = new ImageIcon(CargarImagenes.class.getResource("/images/fondos/fondo_negro.png"));
		warning = cargarImagenSegura("/images/fondos/warning_fdaf.png");
		gameOverFinal = new ImageIcon(CargarImagenes.class.getResource("/images/fondos/GAME_OVER_.png"));	    
		juegoLuzIzq = new ImageIcon(CargarImagenes.class.getResource("/gifs/fondos/OficinaLuzIzq.gif"));
	    juegoLuzDer = new ImageIcon(CargarImagenes.class.getResource("/gifs/fondos/OficinaLuzDer.gif"));
	    // El archivo real en disco es "Abanico.gif" (A mayuscula) -- "abanico.gif" (minuscula)
	    // funcionaba por pura casualidad al correr desde una carpeta de clases suelta en Windows
	    // (NTFS es insensible a mayusculas), pero rompe con NullPointerException real al empaquetar
	    // en un .jar (las entradas de un .jar SI son sensibles a mayusculas) -- encontrado al probar
	    // una distribucion real empaquetada (2026-08-09/10, investigacion de distribucion portable).
	    abanico = new ImageIcon(CargarImagenes.class.getResource("/gifs/fondos/Abanico.gif"));
	    

	    
	    


	    Toolkit.getDefaultToolkit().prepareImage(juegoLuzIzq.getImage(), -1, -1, null);
	    Toolkit.getDefaultToolkit().prepareImage(juegoLuzDer.getImage(), -1, -1, null);
	    Toolkit.getDefaultToolkit().prepareImage(abanico.getImage(), -1, -1, null);
	

	    menu=new ImageIcon(CargarImagenes.class.getResource("/gifs/fondos/Menu.gif"));
 		camara=new ImageIcon(CargarImagenes.class.getResource("/gifs/fondos/camara.gif"));
 		puntoCamara=new ImageIcon(CargarImagenes.class.getResource("/gifs/fondos/puntorojo.gif"));
 		
	
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
		
		
	    puertaDerAB=new ImageIcon(CargarImagenes.class.getResource("/images/puertas/pDER-ABRIR.gif"));
	    puertaDerCE=new ImageIcon(CargarImagenes.class.getResource("/images/puertas/pDER-CERRAR.gif"));
	    
	    puertaIzqAB=new ImageIcon(CargarImagenes.class.getResource("/images/puertas/pIZQ-ABRIR.gif"));
	    puertaIzqCE=new ImageIcon(CargarImagenes.class.getResource("/images/puertas/pIZQ-CERRAR.gif"));
		
		
		puertaDer0=new ImageIcon(CargarImagenes.class.getResource("/images/ui/puertas/puertaDer.png"));
		puertaIzq0=new ImageIcon(CargarImagenes.class.getResource("/images/ui/puertas/puertaIzq.png"));
		puertaDer1=new ImageIcon(CargarImagenes.class.getResource("/images/ui/puertas/104.png"));
		puertaIzq1=new ImageIcon(CargarImagenes.class.getResource("/images/ui/puertas/103.png"));

		powerLeft=new ImageIcon(CargarImagenes.class.getResource("/images/hud/powerLeft.png"));
		bateria0=new ImageIcon(CargarImagenes.class.getResource("/images/hud/bateria0.png"));
		bateria1=new ImageIcon(CargarImagenes.class.getResource("/images/hud/bateria1.png"));
		bateria2=new ImageIcon(CargarImagenes.class.getResource("/images/hud/bateria2.png"));
		bateria3=new ImageIcon(CargarImagenes.class.getResource("/images/hud/bateria3.png"));
		bateria4=new ImageIcon(CargarImagenes.class.getResource("/images/hud/bateria4.png"));

		mutecall=new ImageIcon(CargarImagenes.class.getResource("/images/hud/mutecall.png"));

	}
	
	// Ruta del gif de introducción según la noche (índice 0-4). Si el
		// archivo específico de esa noche no existe todavía, cae de vuelta al
		// gif genérico -- nada se rompe mientras se agregan los 5 reales.
		public static String rutaIntroPorNoche(int indiceNoche) {
			String[] rutas = {
					"/gifs/fondos/introduccion/introNoche1.gif",
					"/gifs/fondos/introduccion/introNoche2.gif",
					"/gifs/fondos/introduccion/introNoche3.gif",
					"/gifs/fondos/introduccion/introNoche4.gif",
					"/gifs/fondos/introduccion/introNoche5.gif"
			};

			if (indiceNoche < 0 || indiceNoche >= rutas.length) {
				return "/gifs/fondos/empezar.gif";
			}

			String ruta = rutas[indiceNoche];
			return (CargarImagenes.class.getResource(ruta) != null) ? ruta : "/gifs/fondos/empezar.gif";
		}

		// Carga defensiva: si el archivo aún no existe, devuelve null en vez
		// de lanzar una excepción que tumbaría el arranque completo de la
		// app (new ImageIcon(URL) con null revienta con NullPointerException
		// dentro del bloque estático). PnlWarning ya verifica null antes de
		// dibujar, así que sin el archivo solo se ve un fondo negro, no un
		// crash.
		private static ImageIcon cargarImagenSegura(String ruta) {
			java.net.URL url = CargarImagenes.class.getResource(ruta);
			if (url == null) {
				System.out.println("[RECURSO NO ENCONTRADO] " + ruta);
				return null;
			}
			return new ImageIcon(url);
		}

}