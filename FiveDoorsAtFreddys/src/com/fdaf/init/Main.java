package com.fdaf.init;

import com.fdaf.mvc.controllers.ControllerMenu;
import com.fdaf.util.PersistenciaJuego;

public class Main {

	// Marcador que ReiniciadorJuego pasa como argumento al relanzar el mismo
	// proceso (ganar 1-4/perder/rendirse, ver CLAUDE.md #1.18) -- distingue
	// un reinicio interno (continuacion de la misma sesion de juego) de un
	// lanzamiento externo real (el jugador abre el juego desde cero). Solo
	// afecta si se muestra la advertencia inicial, ver ControllerMenu.
	private static final String ARG_REINICIO_INTERNO = "--reinicio-interno";

	public static void main(String[] args) {
		System.setProperty("awt.useSystemAAFontSettings", "off");
		System.setProperty("swing.aatext", "false");
	    PersistenciaJuego.cargar();
	    boolean reinicioInterno = args != null && java.util.Arrays.asList(args).contains(ARG_REINICIO_INTERNO);
	    new ControllerMenu(reinicioInterno).init();
	}

}