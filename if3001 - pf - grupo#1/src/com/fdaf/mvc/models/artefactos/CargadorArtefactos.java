package com.fdaf.mvc.models.artefactos;

public class CargadorArtefactos {

	public static void cargar(InventarioArtefactos inventario) {

		inventario.agregar(
				new Artefacto("freddy_plush.png"));

		inventario.agregar(
				new Artefacto("cupcake.png"));

		inventario.agregar(
				new Artefacto("microfono.png"));

		inventario.agregar(
				new Artefacto("guitarra.png"));

		inventario.agregar(
				new Artefacto("garfio.png"));
	}

}