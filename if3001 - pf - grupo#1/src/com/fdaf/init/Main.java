package com.fdaf.init;

import com.fdaf.mvc.controllers.ControllerMenu;
import com.fdaf.util.PersistenciaJuego;

public class Main {

	public static void main(String[] args) {
		System.setProperty("awt.useSystemAAFontSettings", "off");
		System.setProperty("swing.aatext", "false");
	    PersistenciaJuego.cargar();
	    new ControllerMenu().init();
	}

}