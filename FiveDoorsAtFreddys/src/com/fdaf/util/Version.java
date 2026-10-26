package com.fdaf.util;

// Única fuente de verdad del número de versión (CLAUDE.md §1.32). Ningún
// otro archivo escribe la versión a mano: el menú la lee de aquí.
//
// Política: cada commit publicado a origin/master sube MENOR en 1
// (v2.1 -> v2.2 -> ... -> v2.100). Después de v2.100 el siguiente es
// v3.1: MAYOR sube en 1 y MENOR vuelve a 1 (nunca a 0).
public final class Version {

	public static final int MAYOR = 2;
	public static final int MENOR = 13;

	private Version() {
	}

	// Formato que el menú ya mostraba ("v 2.1").
	public static String texto() {
		return "v " + MAYOR + "." + MENOR;
	}
}
