package com.fdaf.mvc.controllers;

import javax.swing.Timer;

import com.fdaf.mvc.models.juego.Juego;
import com.fdaf.mvc.models.puerta.Puerta;
import com.fdaf.mvc.models.puerta.TipoPuerta;
import com.fdaf.mvc.views.multimedia.Sonido;

public class ControllerJuego {

	private Juego juego;

	private boolean puertaIzqAbierta = false;
	private boolean puertaDerAbierta = false;
	private boolean luzIzqEncendida = false;
	private boolean luzDerEncendida = false;

	private JuegoListener listener;

	private Puerta puertaPendienteIzq;
	private Puerta puertaPendienteDer;

	private String ladoRevelandoActual;
	private Timer timerRevelacion;

	private Sonido sonidoLuzIzq;
	private Sonido sonidoLuzDer;

	private boolean llamadaActiva = false;
	private boolean juegoTerminado = false;

	private static final int MS_ANIMATRONICO = 1000;
	private static final int MS_COLECCIONABLE = 6000;
	private static final int MS_NADA = 600;

	public ControllerJuego() {
		juego = new Juego();
	}

	public void init() {
	}

	/*
	 * ---- PUERTAS (toggle: abrir/cerrar) ----
	 */

	public void abrirIzquierda() {

		if (juegoTerminado) {
			new Sonido("botones/error_al_presionar_boton.wav").play();
			return;
		}

		if (llamadaActiva) {
			new Sonido("botones/error_al_presionar_boton.wav").play();
			return;
		}

		if ("izq".equals(ladoRevelandoActual)) {
			new Sonido("botones/error_al_presionar_boton.wav").play();
			return;
		}

		new Sonido("puertas/puerta.wav").play();

		if (!puertaIzqAbierta) {

			puertaIzqAbierta = true;

			if (puertaPendienteIzq == null) {
				puertaPendienteIzq = elegirIzquierda();
			}

			if (listener != null) listener.alAbrirPuerta("izq");

			if (luzIzqEncendida && puertaPendienteIzq != null) {
				evaluarRevelacionIzq();
			}

		} else {

			puertaIzqAbierta = false;

			if (listener != null) listener.alCerrarPuerta("izq");
		}
	}

	public void abrirDerecha() {

		if (juegoTerminado) {
			new Sonido("botones/error_al_presionar_boton.wav").play();
			return;
		}

		if (llamadaActiva) {
			new Sonido("botones/error_al_presionar_boton.wav").play();
			return;
		}

		if ("der".equals(ladoRevelandoActual)) {
			new Sonido("botones/error_al_presionar_boton.wav").play();
			return;
		}

		new Sonido("puertas/puerta.wav").play();

		if (!puertaDerAbierta) {

			puertaDerAbierta = true;

			if (puertaPendienteDer == null) {
				puertaPendienteDer = elegirDerecha();
			}

			if (listener != null) listener.alAbrirPuerta("der");

			if (luzDerEncendida && puertaPendienteDer != null) {
				evaluarRevelacionDer();
			}

		} else {

			puertaDerAbierta = false;

			if (listener != null) listener.alCerrarPuerta("der");
		}
	}

	private Puerta elegirIzquierda() {
		return juego.elegirIzquierda();
	}

	private Puerta elegirDerecha() {
		return juego.elegirDerecha();
	}

	/*
	 * ---- LUCES (toggle: encender/apagar, con loop mientras esté encendida) ----
	 */

	public void revelarIzquierda() {

		if (juegoTerminado) {
			new Sonido("botones/error_al_presionar_boton.wav").play();
			return;
		}

		if (llamadaActiva) {
			new Sonido("botones/error_al_presionar_boton.wav").play();
			return;
		}

		if ("izq".equals(ladoRevelandoActual)) {
			new Sonido("botones/error_al_presionar_boton.wav").play();
			return;
		}

		if (luzIzqEncendida) {

			luzIzqEncendida = false;
			detenerSonidoLuzIzq();

			if (listener != null) listener.alApagarLuz("izq");

			return;
		}

		luzIzqEncendida = true;
		sonidoLuzIzq = new Sonido("botones/luces_encendidas.wav");
		sonidoLuzIzq.loop();

		if (listener != null) listener.alEncenderLuz("izq");

		if (puertaIzqAbierta && puertaPendienteIzq != null) {
			evaluarRevelacionIzq();
		}
	}

	public void revelarDerecha() {

		if (juegoTerminado) {
			new Sonido("botones/error_al_presionar_boton.wav").play();
			return;
		}

		if (llamadaActiva) {
			new Sonido("botones/error_al_presionar_boton.wav").play();
			return;
		}

		if ("der".equals(ladoRevelandoActual)) {
			new Sonido("botones/error_al_presionar_boton.wav").play();
			return;
		}

		if (luzDerEncendida) {

			luzDerEncendida = false;
			detenerSonidoLuzDer();

			if (listener != null) listener.alApagarLuz("der");

			return;
		}

		luzDerEncendida = true;
		sonidoLuzDer = new Sonido("botones/luces_encendidas.wav");
		sonidoLuzDer.loop();

		if (listener != null) listener.alEncenderLuz("der");

		if (puertaDerAbierta && puertaPendienteDer != null) {
			evaluarRevelacionDer();
		}
	}

	private void detenerSonidoLuzIzq() {
		if (sonidoLuzIzq != null) {
			sonidoLuzIzq.stop();
			sonidoLuzIzq = null;
		}
	}

	private void detenerSonidoLuzDer() {
		if (sonidoLuzDer != null) {
			sonidoLuzDer.stop();
			sonidoLuzDer = null;
		}
	}

	public void detenerTodosLosSonidos() {
		detenerSonidoLuzIzq();
		detenerSonidoLuzDer();
	}

	/*
	 * ---- REVELACIÓN: solo cuando puerta abierta + luz encendida ----
	 */

	private void evaluarRevelacionIzq() {
		evaluarRevelacion("izq", puertaPendienteIzq);
	}

	private void evaluarRevelacionDer() {
		evaluarRevelacion("der", puertaPendienteDer);
	}

	// CORREGIDO: el switch completo vuelve a estar aquí -- ya no está
	// separado en un método sin llamador. Este era el bug bloqueante.
	private void evaluarRevelacion(String lado, Puerta puerta) {

		if (ladoRevelandoActual != null) {
			return;
		}

		ladoRevelandoActual = lado;

		switch (puerta.getTipo()) {

		case ANIMATRONICO:
			juego.perderVidaPorAnimatronico();
			if (listener != null) listener.alActualizarVidas(getVidas());

			if ("izq".equals(lado)) detenerSonidoLuzIzq(); else detenerSonidoLuzDer();
			if (listener != null) listener.alRevelarAnimatronico(lado, puerta.getAnimatronico());
			programarLiberacion(MS_ANIMATRONICO);
			break;

		case COLECCIONABLE:
			if (listener != null) listener.alRevelarColeccionable(lado, puerta.getColeccionable());
			programarLiberacion(MS_COLECCIONABLE);
			break;

		case BATERIA:
			if (listener != null) listener.alRevelarBateria(lado);
			programarLiberacion(MS_COLECCIONABLE); // mismo tiempo/patrón visual que un coleccionable
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

		if (puerta.getTipo() == TipoPuerta.COLECCIONABLE) {
			juego.marcarColeccionableEncontrado(puerta.getColeccionable());
			if (listener != null) listener.alRecogerColeccionable(puerta.getColeccionable());

		} else if (puerta.getTipo() == TipoPuerta.BATERIA) {
			juego.ganarVidaExtra();
			if (listener != null) listener.alActualizarVidas(getVidas());

		} else {
			return;
		}

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
			puertaIzqAbierta = false;
			luzIzqEncendida = false;
			detenerSonidoLuzIzq();
			new Sonido("puertas/puerta.wav").play();
		} else if ("der".equals(lado)) {
			puertaPendienteDer = null;
			puertaDerAbierta = false;
			luzDerEncendida = false;
			detenerSonidoLuzDer();
			new Sonido("puertas/puerta.wav").play();
		}

		ladoRevelandoActual = null;

		if (lado != null && listener != null) {
			listener.alLiberar(lado);
		}

		verificarFinDeJuego();
	}

	private void verificarFinDeJuego() {
		if (perdio()) {
			if (listener != null) listener.alPerder();
		} else if (gano()) {
			if (listener != null) listener.alGanar();
		}
	}

	/*
	 * ---- CONSULTAS DE ESTADO ----
	 */

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

	public void setListener(JuegoListener listener) {
		this.listener = listener;
	}

	public void setLlamadaActiva(boolean llamadaActiva) {
		this.llamadaActiva = llamadaActiva;
	}

	public void setJuegoTerminado(boolean juegoTerminado) {
		this.juegoTerminado = juegoTerminado;
	}

	public boolean isJuegoTerminado() {
		return juegoTerminado;
	}

}