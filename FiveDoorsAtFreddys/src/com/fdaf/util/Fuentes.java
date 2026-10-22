package com.fdaf.util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;

// Punto único de creación de fuentes para todo el proyecto. Ningún otro
// archivo debería volver a escribir "new Font(...)" para texto visible
// al jugador -- si se quiere cambiar la tipografía en el futuro, este es
// el único archivo que se toca.
public class Fuentes {

	private static final String FAMILIA_PREFERIDA = "Consolas";
	private static final String FAMILIA_RESPALDO = "Tahoma";

	// Se calcula una sola vez al cargar la clase.
	private static final String FAMILIA_REAL = calcularFamiliaDisponible();

	private static String calcularFamiliaDisponible() {
		String[] disponibles = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		boolean consolasDisponible = Arrays.asList(disponibles).contains(FAMILIA_PREFERIDA);
		return consolasDisponible ? FAMILIA_PREFERIDA : FAMILIA_RESPALDO;
	}

	// El tamaño se escala con la resolución real, igual que los bounds de
	// cada componente (EscalarVista.adaptar*). Se usa UN solo factor, el
	// menor entre X e Y: aplicar ambos (escalaX(escalaY(t))) multiplicaba
	// el tamaño por ~1.44 en 1920x1080 mientras los componentes solo
	// crecen ~1.2, y textos como "Nueva Partida" o "Salir del Juego"
	// quedaban recortados (ver CLAUDE.md §1.25).
	private static int escalar(int tamano) {
		return Math.min(EscalarVista.getEscalaX(tamano), EscalarVista.getEscalaY(tamano));
	}

	// Uso normal -- cubre todos los textos del proyecto salvo uno.
	public static Font obtener(int tamano) {
		return new Font(FAMILIA_REAL, Font.PLAIN, escalar(tamano));
	}

	// Único caso real con estilo distinto a PLAIN: el título de
	// PnlGameOver ("GAME OVER") usa BOLD. Se expone el estilo como
	// parámetro opcional en vez de forzarlo a PLAIN o duplicar la clase.
	public static Font obtener(int tamano, int estilo) {
		return new Font(FAMILIA_REAL, estilo, escalar(tamano));
	}
}