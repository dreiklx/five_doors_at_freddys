package com.fdaf.mvc.models.artefactos;

import java.util.LinkedList;
import java.util.Queue;

public class ColaArtefactos {

	private Queue<Artefacto> cola;

	public ColaArtefactos() {
		cola = new LinkedList<>();
	}

	public void agregar(Artefacto artefacto) {
		cola.offer(artefacto);
	}

	public Artefacto obtener() {
		return cola.poll();
	}

	public boolean isEmpty() {
		return cola.isEmpty();
	}

	public int size() {
		return cola.size();
	}
}