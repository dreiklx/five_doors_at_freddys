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
	}

	/*
	 * ---- PUERTAS (toggle: abrir/cerrar) ----
	 * CAMBIO: abrir la puerta ya NO afecta las vidas. Solo consume el
	 * árbol y determina el contenido; la vida se pierde exclusivamente
	 * en evaluarRevelacion() cuando el tipo resulta ser ANIMATRONICO.
	 */

	public void abrirIzquierda() {

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

		}
	}

	/*
	 * ---- LUCES (toggle: encender/apagar, con loop mientras esté encendida) ----
	 */

	public void revelarIzquierda() {

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

	/*
	 * ---- REVELACIÓN: solo cuando puerta abierta + luz encendida ----
	 */

	private void evaluarRevelacionIzq() {
		evaluarRevelacion("izq", puertaPendienteIzq);
	}

	private void evaluarRevelacionDer() {
		evaluarRevelacion("der", puertaPendienteDer);
	}

	private void evaluarRevelacion(String lado, Puerta puerta) {

		if (ladoRevelandoActual != null) {
			return;
		}

		ladoRevelandoActual = lado;

		switch (puerta.getTipo()) {

		case ANIMATRONICO:
			// aquí, y solo aquí, se pierde la vida  en el momento
			// real de la revelación, no al abrir la puerta.
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

	public void reiniciar() {
		juego.reiniciar();
		puertaPendienteIzq = null;
		puertaPendienteDer = null;
		puertaIzqAbierta = false;
		puertaDerAbierta = false;
		luzIzqEncendida = false;
		luzDerEncendida = false;
		detenerSonidoLuzIzq();
		detenerSonidoLuzDer();
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