package com.fdaf.mvc.models.artefactos;

import java.util.HashMap;

public class InventarioArtefactos {

	private HashMap<Integer, Artefacto> artefactos;

	public InventarioArtefactos() {
		artefactos = new HashMap<>();
	}

	public void agregar(Artefacto artefacto) {
		artefactos.put(artefacto.getId(), artefacto);
	}
	
	public Artefacto buscar(int id) {
		return artefactos.get(id);
	}

	public HashMap<Integer, Artefacto> getArtefactos() {
		return artefactos;
	}
	// Método para obtener una cola con los artefactos pendientes de encontrar
	public ColaArtefactos obtenerPendientes() {

		ColaArtefactos cola = new ColaArtefactos();

		for(Artefacto artefacto : artefactos.values()) {

			if(!artefacto.isEncontrado()) {
				cola.agregar(artefacto);
			}

		}

		return cola;
	}
	// Método para verificar si todos los artefactos han sido encontrados
	public boolean todosEncontrados() {

		for(Artefacto artefacto : artefactos.values()) {

			if(!artefacto.isEncontrado()) {
				return false;
			}

		}

		return true;
	}
}