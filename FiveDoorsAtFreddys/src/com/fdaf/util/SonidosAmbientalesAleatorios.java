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

	// Cada entrada: ruta + peso relativo + si es un sonido asociado
	// directamente a un animatronico (Freddy/Foxy/Chica) -- pedido
	// explicito del usuario 2026-08-10: "prioriza especialmente los
	// sonidos relacionados con los animatronicos" a partir de la Noche 3.
	// Agregar un sonido nuevo en el futuro es una sola linea aqui, sin
	// tocar ningun otro metodo de esta clase.
	private static final Object[][] POOL = {
			{ "random/knock1.wav", 3, false },
			{ "random/knock2.wav", 3, false },
			{ "random/interferencia1.wav", 2, false },
			{ "random/interferencia2.wav", 2, false },
			{ "random/susurros.wav", 3, false },
			{ "random/risa_freddy1.wav", 3, true },
			{ "random/risa_freddy2.wav", 3, true },
			{ "random/foxy_cantando.wav", 2, true },
			{ "random/chica_en_cocina.wav", 2, true },
			{ "random/run.wav", 1, false },
			// Peso bajo a propósito: comparte familia temática con
			// varios/respiracion_agitada.wav (la que ya suena justo
			// después de un jumpscare real) -- reduce el riesgo de
			// confusión sin excluir el recurso.
			{ "random/respiracion_turbia.wav", 1, false },
	};

	// Progresion de tension por noche (pedido explicito del usuario
	// 2026-08-10): Noche 1-2 se quedan en el ritmo original (base
	// "tranquila" ya establecida y aprobada, sin tocar), Noche 3 en
	// adelante reduce la espera entre sonidos (mas frecuencia real, nunca
	// mas volumen) y aumenta el peso relativo de los sonidos de
	// animatronico via MULTIPLICADOR_ANIMATRONICO_POR_NOCHE. El piso de
	// espera nunca baja de ~3.5s incluso en Noche 5 -- suficiente margen
	// real para que dos sonidos nunca se sientan pegados/caoticos (ver
	// duracion real de cada clip, sumada a esta espera antes de programar
	// el siguiente, igual que ya garantizaba el sistema original).
	private static final int[] ESPERA_MIN_MS_POR_NOCHE = { 8000, 8000, 6000, 4500, 3500 };
	private static final int[] ESPERA_MAX_MS_POR_NOCHE = { 13000, 13000, 10000, 8000, 6500 };
	private static final double[] MULTIPLICADOR_ANIMATRONICO_POR_NOCHE = { 1.0, 1.0, 2.0, 3.0, 4.0 };

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

	// Indice de noche real, acotado por si algun dia se agregara una noche
	// fuera del rango de las tablas de arriba (defensivo, no se espera que
	// ocurra con las 5 noches actuales).
	private int indiceNocheAcotado() {
		int indice = PreferenciasJuego.nocheActual.ordinal();
		return Math.max(0, Math.min(indice, ESPERA_MIN_MS_POR_NOCHE.length - 1));
	}

	private int esperaAleatoria() {
		int indice = indiceNocheAcotado();
		int min = ESPERA_MIN_MS_POR_NOCHE[indice];
		int max = ESPERA_MAX_MS_POR_NOCHE[indice];
		return min + random.nextInt(max - min + 1);
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
	
	// Peso efectivo de una entrada del pool: el peso base, multiplicado
	// por MULTIPLICADOR_ANIMATRONICO_POR_NOCHE solo si esParteDeAnimatronico
	// (tercer campo de la entrada) es true -- el resto de los sonidos
	// (golpes, interferencia, susurros, etc.) no cambia de peso relativo
	// entre noches, solo el ritmo general (ver esperaAleatoria()).
	private double pesoEfectivo(Object[] entrada) {
		double base = (int) entrada[1];
		boolean esAnimatronico = (boolean) entrada[2];
		if (esAnimatronico) {
			return base * MULTIPLICADOR_ANIMATRONICO_POR_NOCHE[indiceNocheAcotado()];
		}
		return base;
	}

	// Excluye del sorteo únicamente la ruta que sonó la vez anterior --
	// vuelve a estar disponible en el siguiente ciclo. Si el pool
	// tuviera un solo elemento, el excluido se ignora automáticamente
	// (no hay nada más entre qué elegir).
	private String elegirRutaPonderada() {

		double pesoTotal = 0;
		for (Object[] entrada : POOL) {
			if (!entrada[0].equals(ultimaRutaReproducida)) {
				pesoTotal += pesoEfectivo(entrada);
			}
		}

		if (pesoTotal <= 0) {
			// Solo pasa si el pool tiene un único sonido -- se repite
			// porque no hay alternativa real.
			return ultimaRutaReproducida != null ? ultimaRutaReproducida : (String) POOL[0][0];
		}

		double punto = random.nextDouble() * pesoTotal;
		double acumulado = 0;
		for (Object[] entrada : POOL) {
			if (entrada[0].equals(ultimaRutaReproducida)) {
				continue;
			}
			acumulado += pesoEfectivo(entrada);
			if (punto < acumulado) {
				return (String) entrada[0];
			}
		}
		return (String) POOL[0][0]; // fallback teórico, nunca debería alcanzarse
	}
}