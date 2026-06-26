package com.fdaf.mvc.models.coleccionables;

public class CargarColeccionables {

	public static void cargar(InventarioColeccionables inventario) {

		inventario.agregar(
				new Coleccionable("coleccionables/peluche_freddy.png"));

		inventario.agregar(
				new Coleccionable("coleccionables/cupcake.png"));

		inventario.agregar(
				new Coleccionable("coleccionables/microfono_freddy.png"));

		inventario.agregar(
				new Coleccionable("coleccionables/guitarra_bonnie.png"));

		inventario.agregar(
				new Coleccionable("coleccionables/garfio_foxy.png"));
	}

}