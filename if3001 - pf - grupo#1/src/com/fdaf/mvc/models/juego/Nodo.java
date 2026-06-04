package com.fdaf.mvc.models.juego;

public class Nodo<Puerta> {

	private Puerta puerta;
	private Nodo<Puerta> anterior;
	private Nodo<Puerta> siguiente;

	public Nodo(Puerta puerta) {
		this.puerta = puerta;
		this.anterior = null;
		this.siguiente = null;
	}

	public Puerta getPuerta() {
		return puerta;
	}

	public void setPuerta(Puerta puerta) {
		this.puerta = puerta;
	}

	public Nodo<Puerta> getAnterior() {
		return anterior;
	}

	public void setAnterior(Nodo<Puerta> anterior) {
		this.anterior = anterior;
	}

	public Nodo<Puerta> getSiguiente() {
		return siguiente;
	}

	public void setSiguiente(Nodo<Puerta> siguiente) {
		this.siguiente = siguiente;
	}
}