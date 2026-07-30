package com.fdaf.mvc.models.juego;

public class Nodo<Puerta> {

	private Puerta puerta;
	private Nodo<Puerta> izquierda;
	private Nodo<Puerta> derecha;

	public Nodo(Puerta puerta) {
		this.puerta = puerta;
		this.izquierda = null;
		this.derecha = null;
	}

	public Puerta getPuerta() {
		return puerta;
	}

	public void setPuerta(Puerta puerta) {
		this.puerta = puerta;
	}

	public Nodo<Puerta> getIzquierda() {
		return izquierda;
	}

	public void setIzquierda(Nodo<Puerta> izquierda) {
		this.izquierda = izquierda;
	}

	public Nodo<Puerta> getDerecha() {
		return derecha;
	}

	public void setDerecha(Nodo<Puerta> derecha) {
		this.derecha = derecha;
	}
}