package com.fdaf.mvc.models.coleccionables;

public class CargarColeccionables {

	public static void cargar(InventarioColeccionables inventario) {

		inventario.agregar(
				new Coleccionable("freddy_plush.png"));

		inventario.agregar(
				new Coleccionable("cupcake.png"));

		inventario.agregar(
				new Coleccionable("microfono.png"));

		inventario.agregar(
				new Coleccionable("guitarra.png"));

		inventario.agregar(
				new Coleccionable("garfio.png"));
	}

}