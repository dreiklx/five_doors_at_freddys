package com.fdaf.mvc.controllers;

import com.fdaf.mvc.models.juego.Juego;
import com.fdaf.mvc.models.puerta.Puerta;
import com.fdaf.mvc.models.puerta.TipoPuerta;
import com.fdaf.mvc.views.multimedia.Sonido;

public class ControllerJuego {
	

	private Juego juego;
	private Sonido musica;

	public ControllerJuego() {
		juego = new Juego();
	}

	public void init() {
		musica = new Sonido("jumpscare.wav");
	}

	public Puerta elegirIzquierda() {
		Puerta puerta = juego.elegirIzquierda();
		procesarSonido(puerta);
		return puerta;
	}

	public Puerta elegirDerecha() {
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

	public void reiniciar() {
		juego.reiniciar();
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