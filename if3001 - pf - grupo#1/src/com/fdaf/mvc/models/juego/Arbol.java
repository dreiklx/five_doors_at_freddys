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

				if (padre.getIzquierda() == null)
					padre.setIzquierda(nuevo);
				else
					store(padre.getIzquierda(), nuevo);

			} else {

				if (padre.getDerecha() == null)
					padre.setDerecha(nuevo);
				else
					store(padre.getDerecha(), nuevo);

			}

		}
	}

	public boolean moverIzquierda() {

		if (actual != null && actual.getIzquierda() != null) {

			actual = actual.getIzquierda();
			return true;
		}

		return false;
	}

	public boolean moverDerecha() {

		if (actual != null && actual.getDerecha() != null) {

			actual = actual.getDerecha();
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

		return actual.getIzquierda() == null
				&& actual.getDerecha() == null;
	}

	public void reiniciarRecorrido() {
		actual = raiz;
	}

}