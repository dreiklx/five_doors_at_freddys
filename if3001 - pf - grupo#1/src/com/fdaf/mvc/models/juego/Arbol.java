package com.fdaf.mvc.models.juego;

import com.fdaf.mvc.models.puerta.Puerta;

public class Arbol {

	private Nodo<Puerta> raiz;
	private Nodo<Puerta> actual;

	public Arbol() {
		this.raiz = null;
		this.actual = null;
	}

	public Nodo<Puerta> getRaiz() {
		return raiz;
	}

	public void setRaiz(Nodo<Puerta> raiz) {
		this.raiz = raiz;
		this.actual = raiz;
	}

	public Nodo<Puerta> getActual() {
		return actual;
	}

	public void setActual(Nodo<Puerta> actual) {
		this.actual = actual;
	}

	public boolean isEmpty() {
		return raiz == null;
	}

	public void store(Puerta puerta) {
		store(raiz, new Nodo<Puerta>(puerta));
	}
	/*
	 * Almacena un nuevo nodo en el árbol siguiendo 
	 * la lógica de un árbol binario de búsqueda.
	 */
	private void store(Nodo<Puerta> padre, Nodo<Puerta> nuevo) {

		if (isEmpty()) {

			raiz = nuevo;
			actual = raiz;

		} else {

			if (nuevo.getPuerta().getNumero()
					<= padre.getPuerta().getNumero()) {

				if (padre.getAnterior() == null)
					padre.setAnterior(nuevo);
				else
					store(padre.getAnterior(), nuevo);

			} else {

				if (padre.getSiguiente() == null)
					padre.setSiguiente(nuevo);
				else
					store(padre.getSiguiente(), nuevo);

			}

		}
	}

	public boolean moverIzquierda() {

		if (actual != null && actual.getAnterior() != null) {

			actual = actual.getAnterior();
			return true;
		}

		return false;
	}

	public boolean moverDerecha() {

		if (actual != null && actual.getSiguiente() != null) {

			actual = actual.getSiguiente();
			return true;
		}

		return false;
	}
	// Obtiene la puerta del nodo actual
	public Puerta getPuertaActual() {

		if (actual == null)
			return null;

		return actual.getPuerta();
	}
	// Verifica si el nodo actual es una hoja (no tiene hijos)
	public boolean finDeRama() {

		if (actual == null)
			return false;

		return actual.getAnterior() == null
				&& actual.getSiguiente() == null;
	}

	public void reiniciarRecorrido() {
		actual = raiz;
	}

}