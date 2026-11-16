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

	// Historial de noches superadas (CLAUDE.md §1.66): índice = Noche.ordinal().
	// Es distinto de nocheActual, que es la noche que "Continuar" va a jugar
	// (y la que se elige en Noche personalizada), no lo que el jugador ya
	// ganó. Una noche personalizada solo se puede elegir si está aquí. Se
	// marca en ControllerInterfaz.alGanar(); nunca se desmarca salvo con
	// "Nueva Partida" (reiniciar todo el progreso), igual que ESCAPE.
	public static final boolean[] nochesSuperadas = new boolean[Noche.values().length];

	public static boolean nocheSuperada(Noche noche) {
		return nochesSuperadas[noche.ordinal()];
	}

	// Progreso de la partida (CLAUDE.md §1.82): la noche más alta a la que llegó
	// el jugador. Antes no existía y nocheActual hacía de progreso: repetir una
	// noche ya superada desde Noche personalizada hacía retroceder el avance
	// (ir por la 4, ganar la 1 -> quedaba en la 2). Ahora ganar cualquier noche
	// lleva a max(siguiente, nocheMaxima), y Noche personalizada permite elegir
	// las superadas y esta. Solo sube; "Nueva Partida" la vuelve a la Noche 1.
	public static Noche nocheMaxima = Noche.NOCHE_1;

	// Noches que se pueden jugar desde Noche personalizada: las superadas y la
	// noche a la que llegó el progreso (que "Continuar" ya permitía jugar).
	public static boolean nocheDisponible(Noche noche) {
		return nocheSuperada(noche) || noche == nocheMaxima;
	}

	// Valores de un jugador nuevo. PersistenciaJuego.cargar() parte siempre de
	// aquí (§1.82): una clave ausente o inválida queda en su valor por defecto,
	// no en lo que hubiera en memoria.
	public static void restablecerValoresPorDefecto() {
		idiomaSeleccionado = Idioma.ESPANOL;
		nocheActual = Noche.NOCHE_1;
		nocheMaxima = Noche.NOCHE_1;
		volumenGeneral = 10;
		escapeDesbloqueado = false;
		escapeCompletado = false;
		java.util.Arrays.fill(nochesSuperadas, false);
	}

	// Estrella permanente del menú (CLAUDE.md §1.66): true desde la primera
	// victoria real de Five Doors Escape (ver LogroEscape). A diferencia del
	// resto del progreso, "Nueva Partida" NO la borra: es un logro, no una
	// partida en curso.
	public static boolean escapeCompletado = false;

}