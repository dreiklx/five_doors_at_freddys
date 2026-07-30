package com.fdaf.util;

import java.util.Random;

import javax.swing.Timer;

import com.fdaf.mvc.views.multimedia.Sonido;

// Sistema centralizado de sonidos ambientales aleatorios durante la
// partida. Garantía de "nunca se solapan" por construcción: el
// siguiente sonido solo se programa después de que la duración REAL del
// actual (medida, no supuesta) más una espera aleatoria hayan pasado --
// no depende de ningún booleano de control.
public class SonidosAmbientalesAleatorios {

	// Cada entrada: ruta + peso relativo. Peso más alto = más frecuente.
	// Agregar un sonido nuevo en el futuro es una sola línea aquí, sin
	// tocar ningún otro método de esta clase.
	private static final Object[][] POOL = {
			{ "random/knock1.wav", 3 },
			{ "random/knock2.wav", 3 },
			{ "random/interferencia1.wav", 2 },
			{ "random/interferencia2.wav", 2 },
			{ "random/susurros.wav", 3 },
			{ "random/risa_freddy1.wav", 3 },
			{ "random/risa_freddy2.wav", 3 },
			{ "random/foxy_cantando.wav", 2 },
			{ "random/chica_en_cocina.wav", 2 },
			{ "random/run.wav", 1 },
			// Peso bajo a propósito: comparte familia temática con
			// varios/respiracion_agitada.wav (la que ya suena justo
			// después de un jumpscare real) -- reduce el riesgo de
			// confusión sin excluir el recurso.
			{ "random/respiracion_turbia.wav", 1 },
	};

	private static final int ESPERA_MIN_MS = 8000;
	private static final int ESPERA_MAX_MS = 13000; 
	private static final Random random = new Random();

	private Timer timerProgramado;
	private Sonido sonidoActual;
	private boolean activo = false;
	private String ultimaRutaReproducida;

	public void iniciar() {
		activo = true;
		programarSiguiente(esperaAleatoria());
	}

	// Detiene tanto el timer que programa el próximo sonido como el que
	// pueda estar sonando en este instante -- se llama desde
	// detenerSonidosJuego(), el mismo punto central que ya garantiza que
	// nada de audio de oficina sobreviva a ganar/perder/rendirse.
	public void detener() {
		activo = false;
		if (timerProgramado != null && timerProgramado.isRunning()) {
			timerProgramado.stop();
		}
		if (sonidoActual != null) {
			sonidoActual.stop();
		}
	}

	private int esperaAleatoria() {
		return ESPERA_MIN_MS + random.nextInt(ESPERA_MAX_MS - ESPERA_MIN_MS + 1);
	}

	// Un único Timer que se reprograma a sí mismo cada ciclo -- no hay
	// timers anidados ni una cadena creciente de objetos.
	private void programarSiguiente(int delayMs) {
		if (timerProgramado != null && timerProgramado.isRunning()) {
			timerProgramado.stop();
		}
		timerProgramado = new Timer(delayMs, e -> {
			((Timer) e.getSource()).stop();
			if (!activo) {
				return;
			}
			reproducirSonidoAleatorio();
		});
		timerProgramado.setRepeats(false);
		timerProgramado.start();
	}

	private void reproducirSonidoAleatorio() {
		String ruta = elegirRutaPonderada();
	    System.out.println("[AMBIENTAL] Reproduciendo: " + ruta);
	    ultimaRutaReproducida = ruta;
		sonidoActual = new Sonido(ruta);
		sonidoActual.play();

		long duracion = sonidoActual.getDuracionMs();
		if (duracion <= 0) {
			duracion = 2000; // margen de seguridad si no se pudo leer
		}

		programarSiguiente((int) duracion + esperaAleatoria());
	}
	
	// Excluye del sorteo únicamente la ruta que sonó la vez anterior --
	// vuelve a estar disponible en el siguiente ciclo. Si el pool
	// tuviera un solo elemento, el excluido se ignora automáticamente
	// (no hay nada más entre qué elegir).
	private String elegirRutaPonderada() {

		int pesoTotal = 0;
		for (Object[] entrada : POOL) {
			if (!entrada[0].equals(ultimaRutaReproducida)) {
				pesoTotal += (int) entrada[1];
			}
		}

		if (pesoTotal == 0) {
			// Solo pasa si el pool tiene un único sonido -- se repite
			// porque no hay alternativa real.
			return ultimaRutaReproducida != null ? ultimaRutaReproducida : (String) POOL[0][0];
		}

		int punto = random.nextInt(pesoTotal);
		int acumulado = 0;
		for (Object[] entrada : POOL) {
			if (entrada[0].equals(ultimaRutaReproducida)) {
				continue;
			}
			acumulado += (int) entrada[1];
			if (punto < acumulado) {
				return (String) entrada[0];
			}
		}
		return (String) POOL[0][0]; // fallback teórico, nunca debería alcanzarse
	}
}