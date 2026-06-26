package com.fdaf.mvc.controllers;

import javax.swing.Timer;

import com.fdaf.mvc.models.juego.Juego;
import com.fdaf.mvc.models.puerta.Puerta;
import com.fdaf.mvc.models.puerta.TipoPuerta;
import com.fdaf.mvc.views.multimedia.Sonido;

// ControllerJuego concentra TODA la lógica del
// juego (árbol, puertas pendientes, revelaciones, timers de gameplay,
// victoria/derrota). No toca Swing salvo javax.swing.Timer, que aquí se
// usa como mecanismo de planificación de plazos de juego, no como UI.
// Comunica todos sus efectos visuales a través de un JuegoListener.
public class ControllerJuego {

	private Juego juego;
	private Sonido musica;

	private JuegoListener listener;

	// Buzón de puertas pendientes (estado de juego).
	private Puerta puertaPendienteIzq;
	private Puerta puertaPendienteDer;

	// Control de revelación: solo un lado puede revelar a la vez.
	private String ladoRevelandoActual; // null, "izq" o "der"
	private Timer timerRevelacion;

	// Duraciones de gameplay (decisiones de diseño del juego, no de UI).
	private static final int MS_ANIMATRONICO = 1500;
	private static final int MS_COLECCIONABLE = 6000;
	private static final int MS_NADA = 600;

	public ControllerJuego() {
		juego = new Juego();
	}

	public void setListener(JuegoListener listener) {
		this.listener = listener;
	}

	public void init() {
		musica = new Sonido("jumpscare.wav");
	}

	/*
	 * ---- APERTURA DE PUERTAS ----
	 */

	public void abrirIzquierda() {

		if (puertaPendienteIzq != null) {
			System.out.println("[BLOQUEADA] IZQUIERDA ya tiene una puerta pendiente");
			return;
		}

		Puerta resultado = elegirIzquierda();

		if (resultado == null) {
			System.out.println("[SIN CAMINO] IZQUIERDA no tiene rama disponible");
			return;
		}

		puertaPendienteIzq = resultado;
		System.out.println("IZQUIERDA -> " + resultado + " | vidas=" + getVidas());

		if (listener != null) listener.alAbrirPuerta("izq");
	}

	public void abrirDerecha() {

		if (puertaPendienteDer != null) {
			System.out.println("[BLOQUEADA] DERECHA ya tiene una puerta pendiente");
			return;
		}

		Puerta resultado = elegirDerecha();

		if (resultado == null) {
			System.out.println("[SIN CAMINO] DERECHA no tiene rama disponible");
			return;
		}

		puertaPendienteDer = resultado;
		System.out.println("DERECHA -> " + resultado + " | vidas=" + getVidas());

		if (listener != null) listener.alAbrirPuerta("der");
	}

	private Puerta elegirIzquierda() {
		Puerta puerta = juego.elegirIzquierda();
		procesarSonido(puerta);
		return puerta;
	}

	private Puerta elegirDerecha() {
		Puerta puerta = juego.elegirDerecha();
		procesarSonido(puerta);
		return puerta;
	}

	private void procesarSonido(Puerta puerta) {
		if (puerta == null) return;
		if (puerta.getTipo() == TipoPuerta.ANIMATRONICO) {
			musica = new Sonido("jumpscare.wav");
			musica.play();
		}
	}

	/*
	 * ---- REVELACIÓN (LUCES) ----
	 */

	public void revelarIzquierda() {
		revelar("izq", puertaPendienteIzq);
	}

	public void revelarDerecha() {
		revelar("der", puertaPendienteDer);
	}

	private void revelar(String lado, Puerta puerta) {

		if (puerta == null) {
			return; // nada pendiente en ese lado
		}

		if (ladoRevelandoActual != null) {
			System.out.println("[OVERLAY OCUPADO] espera a que termine la revelación actual");
			return;
		}

		ladoRevelandoActual = lado;

		if (listener != null) listener.alEncenderLuz(lado);

		if (timerRevelacion != null && timerRevelacion.isRunning()) {
			timerRevelacion.stop();
		}

		switch (puerta.getTipo()) {

			case ANIMATRONICO:
				if (listener != null) listener.alRevelarAnimatronico(lado, puerta.getAnimatronico());
				programarLiberacion(MS_ANIMATRONICO);
				break;

			case COLECCIONABLE:
				if (listener != null) listener.alRevelarColeccionable(lado, puerta.getColeccionable());
				// red de seguridad: si el jugador no recoge a tiempo, libera igual
				programarLiberacion(MS_COLECCIONABLE);
				break;

			case NADA:
				if (listener != null) listener.alRevelarNada(lado);
				programarLiberacion(MS_NADA);
				break;
		}
	}

	private void programarLiberacion(int ms) {
		timerRevelacion = new Timer(ms, ev -> {
			timerRevelacion.stop();
			liberar();
		});
		timerRevelacion.setRepeats(false);
		timerRevelacion.start();
	}

	/*
	 * ---- RECOGER COLECCIONABLE (clic del jugador en el overlay) ----
	 */

	public void recogerColeccionable() {

		if (ladoRevelandoActual == null) return;

		Puerta puerta = "izq".equals(ladoRevelandoActual)
				? puertaPendienteIzq : puertaPendienteDer;

		if (puerta == null) return;
		if (puerta.getTipo() != TipoPuerta.COLECCIONABLE) return;

		if (listener != null) listener.alRecogerColeccionable(puerta.getColeccionable());

		if (timerRevelacion != null && timerRevelacion.isRunning()) {
			timerRevelacion.stop();
		}

		liberar();
	}

	/*
	 * ---- LIBERACIÓN Y FIN DE JUEGO ----
	 */

	private void liberar() {

		String lado = ladoRevelandoActual;

		if ("izq".equals(lado)) {
			puertaPendienteIzq = null;
			System.out.println("[LIBERADA] IZQUIERDA");
		} else if ("der".equals(lado)) {
			puertaPendienteDer = null;
			System.out.println("[LIBERADA] DERECHA");
		}

		ladoRevelandoActual = null;

		if (lado != null && listener != null) {
			listener.alLiberar(lado);
		}

		verificarFinDeJuego();
	}

	private void verificarFinDeJuego() {
		if (perdio()) {
			System.out.println("[GAME OVER] vidas=" + getVidas());
			if (listener != null) listener.alPerder();
		} else if (gano()) {
			System.out.println("[VICTORIA] coleccionables=" + getColeccionablesEncontrados());
			if (listener != null) listener.alGanar();
		}
	}

	/*
	 * ---- CONSULTAS DE ESTADO ----
	 */

	public void reiniciar() {
		juego.reiniciar();
		puertaPendienteIzq = null;
		puertaPendienteDer = null;
		ladoRevelandoActual = null;
		if (timerRevelacion != null && timerRevelacion.isRunning()) {
			timerRevelacion.stop();
		}
	}

	public boolean gano() {
		return juego.gano();
	}

	public boolean perdio() {
		return juego.perdio();
	}

	public int getVidas() {
		return juego.getVidas();
	}

	public int getColeccionablesEncontrados() {
		return juego.getColeccionablesEncontrados();
	}

	public Juego getJuego() {
		return juego;
	}

}