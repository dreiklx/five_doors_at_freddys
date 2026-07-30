package com.fdaf.mvc.models.coleccionables;

import java.util.LinkedList;
import java.util.Queue;

public class ColaColeccionables {

	private Queue<Coleccionable> cola;

	public ColaColeccionables() {
		cola = new LinkedList<>();
	}

	public void agregar(Coleccionable coleccionable) {
		cola.offer(coleccionable);
	}

	public Coleccionable obtener() {
		return cola.poll();
	}

	public boolean isEmpty() {
		return cola.isEmpty();
	}

	public int size() {
		return cola.size();
	}
}