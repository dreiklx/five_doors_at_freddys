package com.fdaf.mvc.models.juego;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

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

	// Solución definitiva "SIN CAMINO": construcción por
	// niveles (BFS). Garantiza un árbol binario COMPLETO para CUALQUIER
	// tamaño de lista: cada nodo recibe sus dos hijos SOLO si quedan al
	// menos 2 nodos disponibles; si queda 1 o 0, es hoja. Así nunca existe
	// un nodo con un solo hijo. El método anterior (partición por mitades)
	// fallaba para todo tamaño que no fuera 2^k-1; este funciona siempre.
	//
	// Requisito de GeneradorArbol: pasar una lista de tamaño IMPAR. Con
	// tamaño impar, este algoritmo no descarta ningún nodo (cero pérdida
	// de coleccionables). Con tamaño par descartaría exactamente 1 nodo
	// (el último sin pareja), por eso GeneradorArbol fuerza tamaño impar.
	public void construirCompleto(List<Puerta> puertas) {

		if (puertas == null || puertas.isEmpty()) {
			raiz = null;
			actual = null;
			return;
		}

		Nodo<Puerta>[] nodos = crearNodos(puertas);
		int n = nodos.length;

		Queue<Integer> cola = new ArrayDeque<Integer>();
		cola.offer(0);

		int siguiente = 1;

		while (!cola.isEmpty() && siguiente < n) {

			int idx = cola.poll();

			// Regla "ambos o ninguno": solo se asignan hijos si quedan al
			// menos 2 nodos disponibles. Si queda exactamente 1, no se
			// asigna a nadie (ese nodo sobrante se descarta del árbol).
			if (siguiente + 1 < n) {

				nodos[idx].setIzquierda(nodos[siguiente]);
				nodos[idx].setDerecha(nodos[siguiente + 1]);

				cola.offer(siguiente);
				cola.offer(siguiente + 1);

				siguiente += 2;

			} else {
				// Queda 1 solo nodo disponible: no se usa como hijo único.
				break;
			}
		}

		raiz = nodos[0];
		actual = raiz;
	}

	@SuppressWarnings("unchecked")
	private Nodo<Puerta>[] crearNodos(List<Puerta> puertas) {

		Nodo<Puerta>[] nodos = new Nodo[puertas.size()];

		for (int i = 0; i < puertas.size(); i++) {
			nodos[i] = new Nodo<Puerta>(puertas.get(i));
		}

		return nodos;
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