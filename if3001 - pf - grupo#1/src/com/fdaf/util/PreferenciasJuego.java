package com.fdaf.util;

import com.fdaf.mvc.models.juego.Noche;

// Guarda en memoria las preferencias seleccionadas por el jugador
// (idioma, dificultad). Viven mientras la aplicación esté corriendo; no
// se pidió persistirlas en disco entre ejecuciones distintas del juego.
public class PreferenciasJuego {

	public static Idioma idiomaSeleccionado = Idioma.ESPANOL;
	public static Noche nocheSeleccionada = Noche.NOCHE_1;

}