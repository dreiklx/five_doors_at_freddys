package com.fdaf.util;

/**
 * Unica fuente de verdad del numero de version de FDAF (CLAUDE.md §1.32, regla nueva desde v2.19 en §1.82). El menu,
 * "Acerca del proyecto", el registro (fdaf.log), la distribucion (VERSION.txt) y la etiqueta de Git salen de aqui.
 *
 * Regla de la siguiente version (una por publicacion a origin/master, ver §1.82):
 *   x.m        -> x.(m+1)        mientras m < 20                    (v2.18 -> v2.19 -> v2.20)
 *   x.20       -> x.20.1                                             (v2.20 -> v2.20.1)
 *   x.20.p     -> x.20.(p+1)     hasta p = 5                         (v2.20.1 -> ... -> v2.20.5)
 *   x.20.5     -> (x+1).0.0                                          (v2.20.5 -> v3.0.0)
 *   x.0.p      -> x.0.(p+1)      hasta p = 5                         (v3.0.0 -> ... -> v3.0.5)
 *   x.0.5      -> x.1                                                (v3.0.5 -> v3.1 -> ... -> v3.20 -> v3.20.1 ...)
 * PARCHE = -1 significa "sin tercer numero". {@link #siguiente(int[])} la calcula; `java -cp <jar> com.fdaf.util.Version
 * siguiente` la imprime para los scripts.
 */
public final class Version {

	public static final int MAYOR = 2;
	public static final int MENOR = 19;
	/** -1: version de dos numeros (v2.19); 0-5: tercer numero (v2.20.1, v3.0.0...). */
	public static final int PARCHE = -1;

	private static final int MENOR_TOPE = 20;
	private static final int PARCHE_TOPE = 5;

	private Version() {
	}

	/** Version actual como {mayor, menor, parche}. */
	public static int[] actual() {
		return new int[] { MAYOR, MENOR, PARCHE };
	}

	/** La version que sigue a v (ver la regla arriba). v = {mayor, menor, parche}, parche -1 si no tiene. */
	public static int[] siguiente(int[] v) {
		int x = v[0], m = v[1], p = v[2];
		if (p < 0) {
			if (m < MENOR_TOPE) {
				return new int[] { x, m + 1, -1 };
			}
			return new int[] { x, m, 1 };                    // x.20 -> x.20.1
		}
		if (m == MENOR_TOPE) {
			return p < PARCHE_TOPE ? new int[] { x, m, p + 1 } : new int[] { x + 1, 0, 0 };
		}
		if (m == 0) {
			return p < PARCHE_TOPE ? new int[] { x, 0, p + 1 } : new int[] { x, 1, -1 };
		}
		throw new IllegalArgumentException("version fuera de la regla: " + formato(v));
	}

	/** "2.19", "2.20.1", "3.0.0". */
	public static String formato(int[] v) {
		return v[0] + "." + v[1] + (v[2] >= 0 ? "." + v[2] : "");
	}

	/** Formato del menu, el mismo que ya mostraba ("v 2.19"). */
	public static String texto() {
		return "v " + formato(actual());
	}

	/** Etiqueta de Git y de la documentacion ("v2.19"). */
	public static String etiqueta() {
		return "v" + formato(actual());
	}

	/** Uso: (sin argumentos) imprime la version actual; "siguiente" imprime la que sigue. */
	public static void main(String[] args) {
		boolean siguiente = args.length > 0 && "siguiente".equals(args[0]);
		System.out.println("v" + formato(siguiente ? siguiente(actual()) : actual()));
	}
}
