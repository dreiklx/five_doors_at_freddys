package com.fdaf.mvc.models.coleccionables;

public class CargarColeccionables {

	public static void cargar(InventarioColeccionables inventario) {

		inventario.agregar(
				new Coleccionable("artefactos/peluche_freddy.png"));

		inventario.agregar(
				new Coleccionable("artefactos/cupcake.png"));

		inventario.agregar(
				new Coleccionable("artefactos/microfono_freddy.png"));

		inventario.agregar(
				new Coleccionable("artefactos/guitarra_bonnie.png"));

		inventario.agregar(
				new Coleccionable("artefactos/garfio_foxy.png"));
	}

}