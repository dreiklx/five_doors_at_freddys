package com.fdaf.mvc.models.coleccionables;

import java.util.HashMap;

public class InventarioColeccionables {

	private HashMap<Integer, Coleccionable> coleccionables;

	public InventarioColeccionables() {
		coleccionables = new HashMap<>();
	}

	public void agregar(Coleccionable coleccionable) {
		coleccionables.put(coleccionable.getId(), coleccionable);
	}
	
	public Coleccionable buscar(int id) {
		return coleccionables.get(id);
	}

	// Mismo criterio que obtenerPendientes()/todosEncontrados(): recorre
	// los coleccionables reales (máximo 10) buscando el que coincide en
	// tipo. Devuelve null si ese tipo no tiene objeto en el inventario --
	// eso significa que no existe esta noche, no que esté "sin encontrar".
	public Coleccionable buscarPorTipo(TipoColeccionable tipo) {
		for (Coleccionable c : coleccionables.values()) {
			if (c.getTipo() == tipo) {
				return c;
			}
		}
		return null;
	}

	public HashMap<Integer, Coleccionable> getColeccionables() {
		return coleccionables;
	}
	// Método para obtener una cola con los coleccionables pendientes de encontrar
	public ColaColeccionables obtenerPendientes() {

		ColaColeccionables cola = new ColaColeccionables();

		for(Coleccionable coleccionable : coleccionables.values()) {

			if(!coleccionable.isEncontrado()) {
				cola.agregar(coleccionable);
			}

		}

		return cola;
	}
	// Método para verificar si todos los coleccionables han sido encontrados
	public boolean todosEncontrados() {

		for(Coleccionable coleccionable : coleccionables.values()) {

			if(!coleccionable.isEncontrado()) {
				return false;
			}

		}

		return true;
	}
}