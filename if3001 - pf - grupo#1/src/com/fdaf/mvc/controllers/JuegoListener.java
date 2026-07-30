package com.fdaf.mvc.controllers;

import com.fdaf.mvc.models.animatronicos.Animatronico;
import com.fdaf.mvc.models.coleccionables.Coleccionable;
// Contrato de comunicación entre ControllerJuego (lógica pura, sin Swing)
// y la capa visual (ControllerInterfaz). ControllerJuego invoca estos
// métodos para PEDIR efectos visuales, sin saber cómo se implementan.
public interface JuegoListener {

	void alAbrirPuerta(String lado);
	void alCerrarPuerta(String lado);
	void alRevelarAnimatronico(String lado, Animatronico animatronico);
	void alRevelarColeccionable(String lado, Coleccionable coleccionable);
	void alRevelarNada(String lado);
	void alEncenderLuz(String lado);
	void alApagarLuz(String lado);
	void alLiberar(String lado);
	void alRecogerColeccionable(Coleccionable coleccionable);
	void alActualizarVidas(int vidas);  // sincroniza el HUD de batería
	void alGanar();
	void alPerder();
	void alRevelarBateria(String lado);
}