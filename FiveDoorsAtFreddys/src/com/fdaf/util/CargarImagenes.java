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
	public static ImageIcon mutecall;
	public static ImageIcon fondoNegro;

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

	/*
	 * Imágenes grandes que NO hacen falta para el selector de idioma ni el menú
	 * (CLAUDE.md §1.52). Antes se cargaban en el bloque estático, antes de
	 * mostrar la primera ventana (~550 ms medidos, FondoJuego.png sola ~300 ms).
	 * Ahora: getter sincronizado que carga en el primer uso, y
	 * precargarEnSegundoPlano() las adelanta en un hilo de baja prioridad
	 * cuando el selector ya está en pantalla. Si alguien las pide antes de que
	 * termine, espera solo lo que falte (mismo lock) y recibe la imagen ya
	 * completa: new ImageIcon(URL) no vuelve hasta tener la imagen cargada.
	 * "win.png" ya no se carga: ningún código la usa desde §1.11.
	 */
	private static ImageIcon fondoJuego, inventario, gameOverFinal, warning;

	public static synchronized ImageIcon getFondoJuego() {
		if (fondoJuego == null) {
			fondoJuego = new ImageIcon(CargarImagenes.class.getResource("/images/fondos/FondoJuego.png"));
		}
		return fondoJuego;
	}

	public static synchronized ImageIcon getInventario() {
		if (inventario == null) {
			inventario = new ImageIcon(CargarImagenes.class.getResource("/images/fondos/inventario.png"));
		}
		return inventario;
	}

	public static synchronized ImageIcon getGameOverFinal() {
		if (gameOverFinal == null) {
			gameOverFinal = new ImageIcon(CargarImagenes.class.getResource("/images/fondos/GAME_OVER_.png"));
		}
		return gameOverFinal;
	}

	// Advertencia en inglés (la española es getAdvertenciaEspanol()).
	public static synchronized ImageIcon getWarning() {
		if (warning == null) {
			warning = cargarImagenSegura("/images/fondos/warning_fdaf.png");
		}
		return warning;
	}

	private static boolean precargaIniciada = false;

	// Se llama una vez, con el selector ya visible. Hilo daemon de prioridad
	// mínima: no toca ningún componente Swing, solo crea las ImageIcon.
	public static synchronized void precargarEnSegundoPlano() {
		if (precargaIniciada) {
			return;
		}
		precargaIniciada = true;
		Thread hilo = new Thread(() -> {
			getWarning();
			getAdvertenciaEspanol();
			getFondoJuego();
			getInventario();
			getGameOverFinal();
		}, "Precarga-Imagenes");
		hilo.setDaemon(true);
		hilo.setPriority(Thread.MIN_PRIORITY);
		hilo.start();
	}

	static {
		fondoNegro = new ImageIcon(CargarImagenes.class.getResource("/images/fondos/fondo_negro.png"));
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
		// Intros por idioma (CLAUDE.md §1.47): introNocheN_es.gif /
		// introNocheN_en.gif, ya con su tarjeta "12:00 AM / 1ra Noche" o
		// "1st Night" dibujada. Solo se carga la de la noche que se va a jugar.
		public static String rutaIntroPorNoche(int indiceNoche) {
			if (indiceNoche < 0 || indiceNoche > 4) {
				return "/gifs/fondos/empezar.gif";
			}
			String sufijo = (PreferenciasJuego.idiomaSeleccionado == Idioma.INGLES) ? "_en" : "_es";
			String ruta = "/gifs/fondos/introduccion/introNoche" + (indiceNoche + 1) + sufijo + ".gif";
			return (CargarImagenes.class.getResource(ruta) != null) ? ruta : "/gifs/fondos/empezar.gif";
		}

		// Carga defensiva: si el archivo aún no existe, devuelve null en vez
		// de lanzar una excepción que tumbaría el arranque completo de la
		// app (new ImageIcon(URL) con null revienta con NullPointerException
		// dentro del bloque estático). PnlWarning ya verifica null antes de
		// dibujar, así que sin el archivo solo se ve un fondo negro, no un
		// crash.
		// Advertencia inicial en español (CLAUDE.md §1.35): "warning" es la
		// versión en inglés. Se carga solo si se usa (mismo criterio lazy que
		// los respaldos de gifs de §1.13), una vez por proceso.
		private static ImageIcon advertenciaEspanol;

		public static synchronized ImageIcon getAdvertenciaEspanol() {
			if (advertenciaEspanol == null) {
				advertenciaEspanol = cargarImagenSegura("/images/fondos/advertencia_fdaf.png");
			}
			return advertenciaEspanol;
		}

		private static ImageIcon cargarImagenSegura(String ruta) {
			java.net.URL url = CargarImagenes.class.getResource(ruta);
			if (url == null) {
				System.out.println("[RECURSO NO ENCONTRADO] " + ruta);
				return null;
			}
			return new ImageIcon(url);
		}

		/*
		 * Etiquetas del HUD con texto dibujado (CLAUDE.md §1.40). Son imágenes
		 * (fuente pixelada) y existen en dos idiomas en /images/hud/idioma/,
		 * recortadas de los originales de "FDAF assets/FDAF HUD". Se eligen
		 * por PreferenciasJuego.idiomaSeleccionado en el momento de pedirlas
		 * (el idioma no cambia a mitad de una partida) y se cachean por ruta.
		 */
		private static final java.util.Map<String, ImageIcon> etiquetasHud = new java.util.HashMap<>();

		private static boolean ingles() {
			return PreferenciasJuego.idiomaSeleccionado == Idioma.INGLES;
		}

		// "Energía restante:" / "Power left:"
		public static ImageIcon getEtiquetaEnergia() {
			return etiquetaHud(ingles() ? "/images/hud/idioma/power_left_en.png"
					: "/images/hud/idioma/energia_restante_es.png");
		}

		// "Noche N" / "Night N" (numero 1-5).
		public static ImageIcon getEtiquetaNoche(int numero) {
			int n = Math.max(1, Math.min(5, numero));
			return etiquetaHud(ingles() ? "/images/hud/idioma/night" + n + "_en.png"
					: "/images/hud/idioma/noche" + n + "_es.png");
		}

		// Botones de la oficina con texto incrustado (CLAUDE.md §1.42): el
		// original en inglés ("DOOR"/"LIGHT", "MUTE CALL") y una variante
		// española generada a partir de él ("PUERTA"/"LUZ", "COLGAR").
		public static ImageIcon getPanelBotonesIzq() {
			ImageIcon es = ingles() ? null : etiquetaHud("/images/hud/idioma/pnlBotonesIzq_es.png");
			return es != null ? es : panelIzq;
		}

		public static ImageIcon getPanelBotonesDer() {
			ImageIcon es = ingles() ? null : etiquetaHud("/images/hud/idioma/pnlBotonesDer_es.png");
			return es != null ? es : panelDer;
		}

		// Rótulo de la cámara de la tableta, aparte del gif (CLAUDE.md §1.45).
		public static ImageIcon getRotuloCamara() {
			return etiquetaHud(ingles() ? "/images/hud/idioma/camera_disabled_en.png"
					: "/images/hud/idioma/camara_desactivada_es.png");
		}

		public static ImageIcon getMuteCall() {
			ImageIcon es = ingles() ? null : etiquetaHud("/images/hud/idioma/mutecall_es.png");
			return es != null ? es : mutecall;
		}

		private static ImageIcon etiquetaHud(String ruta) {
			ImageIcon icono = etiquetasHud.get(ruta);
			if (icono == null) {
				icono = cargarImagenSegura(ruta);
				if (icono != null) {
					etiquetasHud.put(ruta, icono);
				}
			}
			return icono;
		}

		// Coloca una etiqueta de texto (imagen) con la ALTURA DE MAYÚSCULA y la
		// LÍNEA BASE dadas (px de pantalla), alineada a la izquierda en x
		// (CLAUDE.md §1.48). Los PNG de cada idioma no tienen el mismo tamaño de
		// letra (la "E" de "Energía restante" mide 36 px y la "P" de "Power
		// left" 52 px en los recortes), así que encajarlas por ancho las dejaba
		// de tamaños distintos; medir la primera letra las iguala, y alinear por
		// la línea base hace que los descendentes ("g") no muevan el texto.
		public static void colocarEtiqueta(javax.swing.JLabel etiqueta, ImageIcon imagen, int x, int yBase, double altoMayuscula) {
			if (imagen == null || imagen.getIconHeight() <= 0) {
				return;
			}
			int[] letra = medirPrimeraLetra(imagen);
			double escala = altoMayuscula / (letra[1] - letra[0] + 1);
			int ancho = Math.max(1, (int) Math.round(imagen.getIconWidth() * escala));
			int alto = Math.max(1, (int) Math.round(imagen.getIconHeight() * escala));
			etiqueta.setText("");
			etiqueta.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
			etiqueta.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			etiqueta.setIcon(EscalarVista.getImagenEscalada(imagen, ancho, alto));
			etiqueta.setBounds(x, yBase - (int) Math.round((letra[1] + 1) * escala), ancho, alto);
		}

		// Reducción de una imagen grande a tamaño de icono por mitades sucesivas
		// con interpolación bilineal (CLAUDE.md §1.52): resultado comparable a
		// SCALE_SMOOTH pero en milisegundos. Solo para reducir, nunca ampliar.
		public static ImageIcon reducirPorPasos(ImageIcon original, int ancho, int alto) {
			int w = original.getIconWidth(), h = original.getIconHeight();
			java.awt.image.BufferedImage actual = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
			java.awt.Graphics2D g0 = actual.createGraphics();
			g0.drawImage(original.getImage(), 0, 0, null);
			g0.dispose();
			do {
				w = Math.max(ancho, w / 2);
				h = Math.max(alto, h / 2);
				java.awt.image.BufferedImage paso = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
				java.awt.Graphics2D g = paso.createGraphics();
				g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.drawImage(actual, 0, 0, w, h, null);
				g.dispose();
				actual = paso;
			} while (w > ancho || h > alto);
			return new ImageIcon(actual);
		}

		private static final java.util.Map<ImageIcon, int[]> metricasLetra = new java.util.HashMap<>();

		// {fila superior, fila inferior} de los píxeles opacos de las primeras
		// columnas (la primera letra: "E", "P", "N"), en px de la imagen.
		private static int[] medirPrimeraLetra(ImageIcon imagen) {
			int[] cache = metricasLetra.get(imagen);
			if (cache != null) {
				return cache;
			}
			java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(imagen.getIconWidth(), imagen.getIconHeight(),
					java.awt.image.BufferedImage.TYPE_INT_ARGB);
			java.awt.Graphics g = bi.getGraphics();
			g.drawImage(imagen.getImage(), 0, 0, null);
			g.dispose();
			int columnas = Math.max(4, bi.getWidth() / 14), arriba = bi.getHeight(), abajo = -1;
			for (int x = 0; x < columnas; x++) {
				for (int y = 0; y < bi.getHeight(); y++) {
					if ((bi.getRGB(x, y) >>> 24) > 128) {
						arriba = Math.min(arriba, y);
						abajo = Math.max(abajo, y);
					}
				}
			}
			int[] r = (abajo < 0) ? new int[] { 0, bi.getHeight() - 1 } : new int[] { arriba, abajo };
			metricasLetra.put(imagen, r);
			return r;
		}


}