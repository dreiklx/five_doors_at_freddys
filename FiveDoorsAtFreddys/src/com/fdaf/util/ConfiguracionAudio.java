package com.fdaf.util;

import java.util.ArrayList;
import java.util.List;

import com.fdaf.mvc.views.multimedia.Sonido;

// Volumen global único (0-10) que afecta TODO el sonido del proyecto.
// El constructor de Sonido aplica el nivel vigente automáticamente --
// ningún punto existente donde ya se crea un Sonido necesita cambiar.
// Los sonidos en loop (música, ambiente, estática) se autorregistran
// para poder actualizarse en vivo si el nivel cambia mientras ya están
// sonando.
public class ConfiguracionAudio {

	private static final List<Sonido> sonidosEnLoop = new ArrayList<>();
	
	// Curva exponencial en el dominio de GANANCIA (no en dB) -- así se
	// concentra la caída fuerte cerca del final en vez de repartirla
	// lineal en decibeles (eso hacía que el nivel 5 sonara casi mudo).
	// CURVA_EXPONENTE es el único número que controla qué tan gradual es
	// -- subirlo hace la caída más pronunciada al final; bajarlo hacia 1
	// la vuelve más parecida a la curva lineal anterior.
	private static final float CURVA_EXPONENTE = 3.0f;

	// Rango del sistema: 1-10, nunca 0. El caso "silencio real" quedó
	// inalcanzable con este piso -- se elimina en vez de dejarlo como
	// código muerto que sugiera un comportamiento que ya no existe.
	public static float calcularDb(float minimoDelClip, int nivel) {
		int nivelSeguro = Math.max(1, Math.min(10, nivel));

		float proporcion = nivelSeguro / 10.0f;
		float ganancia = (float) Math.pow(proporcion, CURVA_EXPONENTE);
		float db = (float) (20.0 * Math.log10(ganancia));

		return Math.max(minimoDelClip, db);
	}

	// Se llama una sola vez, desde el constructor de Sonido, apenas se
	// abre el Clip. Cualquier ajuste posterior (subirVolumen, fadeOut)
	// se compone encima de este valor automáticamente.
	public static void aplicarVolumenInicial(Sonido sonido) {
		sonido.aplicarNivelGlobal(PreferenciasJuego.volumenGeneral);
	}

	public static void registrarLoop(Sonido sonido) {
		sonidosEnLoop.add(sonido);
	}

	public static void desregistrarLoop(Sonido sonido) {
		sonidosEnLoop.remove(sonido);
	}

	// Cambia el nivel global, lo persiste, y reaplica de inmediato a
	// todo lo que esté sonando en loop en este momento. Los sonidos
	// cortos (hover, error, puertas, jumpscares) no necesitan esto --
	// heredan el nivel actual la próxima vez que suenan, que ocurre
	// constantemente durante el juego.
	public static void setNivelVolumen(int nuevoNivel) {
		PreferenciasJuego.volumenGeneral = Math.max(1, Math.min(10, nuevoNivel));

		for (Sonido s : new ArrayList<>(sonidosEnLoop)) {
			s.aplicarNivelGlobal(PreferenciasJuego.volumenGeneral);
		}

		PersistenciaJuego.guardar();
	}
}