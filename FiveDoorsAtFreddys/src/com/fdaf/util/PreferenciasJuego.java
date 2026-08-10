package com.fdaf.util;

import com.fdaf.mvc.models.juego.Noche;

// Estado global del jugador: idioma, volumen general (0-10), y la noche
// activa. nocheActual cumple un único rol -- es lo que "Continuar"
// muestra y juega, Y lo que se elige desde Noche Personalizada. Es
// deliberadamente una sola variable: cambiar la noche desde Custom
// Night ES cambiar el punto desde el que se continúa, no un estado
// aparte que haya que sincronizar.
public class PreferenciasJuego {

	public static Idioma idiomaSeleccionado = Idioma.ESPANOL;
	public static Noche nocheActual = Noche.NOCHE_1;
	public static int volumenGeneral = 10;

	// true una vez que el jugador completo la Noche 5 al menos una vez
	// (pedido explicito del usuario 2026-08-09: desbloquea el boton ESCAPE
	// en Custom Night, persistido, nunca se vuelve a bloquear). Se fija en
	// ControllerInterfaz.alGanar() cuando eraNoche5==true.
	public static boolean escapeDesbloqueado = false;

}