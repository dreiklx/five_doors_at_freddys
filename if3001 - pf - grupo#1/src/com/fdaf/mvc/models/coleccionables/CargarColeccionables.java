package com.fdaf.mvc.models.coleccionables;

public class CargarColeccionables {

	// activa los primeros
	// "cantidad" elementos del catálogo canónico TipoColeccionable, en
	// orden. Progresión acumulativa: Noche 1 = primeros 5, Noche 5 = los
	// 10 — nunca subconjuntos distintos.
	public static void cargar(InventarioColeccionables inventario, int cantidad) {

		TipoColeccionable[] catalogo = TipoColeccionable.values();

		int limite = Math.min(cantidad, catalogo.length);

		for (int i = 0; i < limite; i++) {
			inventario.agregar(new Coleccionable(catalogo[i]));
		}
	}

}