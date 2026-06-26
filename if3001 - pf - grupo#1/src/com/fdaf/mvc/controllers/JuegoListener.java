package com.fdaf.mvc.controllers;

import com.fdaf.mvc.models.animatronicos.Animatronico;
import com.fdaf.mvc.models.coleccionables.Coleccionable;

// Contrato de comunicación entre ControllerJuego
// (lógica pura, sin Swing) y la capa visual (ControllerInterfaz, que sí
// toca Swing). ControllerJuego invoca estos métodos para PEDIR efectos
// visuales, sin saber cómo se implementan.
public interface JuegoListener {

	void alAbrirPuerta(String lado);          // pintar puerta abierta en ese lado
	void alRevelarAnimatronico(String lado, Animatronico animatronico);
	void alRevelarColeccionable(String lado, Coleccionable coleccionable);
	void alRevelarNada(String lado);
	void alEncenderLuz(String lado);
	void alLiberar(String lado);              // ocultar overlay, restaurar iconos de ese lado
	void alRecogerColeccionable(Coleccionable coleccionable); // agregarlo a la tablet
	void alGanar();
	void alPerder();
}