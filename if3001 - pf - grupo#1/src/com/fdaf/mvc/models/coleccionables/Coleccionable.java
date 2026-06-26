package com.fdaf.mvc.models.coleccionables;

public class Coleccionable {

	private static int idCounter = 0;

	private int id;
	private String archivoImagen;
	private boolean encontrado;

	public Coleccionable() {}

	public Coleccionable(String archivoImagen) {
		this.id = ++idCounter;
		this.archivoImagen = archivoImagen;
		this.encontrado = false;
	}

	public static int getIdCounter() {
		return idCounter;
	}

	public static void setIdCounter(int idCounter) {
		Coleccionable.idCounter = idCounter;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getArchivoImagen() {
		return archivoImagen;
	}

	public void setArchivoImagen(String archivoImagen) {
		this.archivoImagen = archivoImagen;
	}

	public boolean isEncontrado() {
		return encontrado;
	}

	public void setEncontrado(boolean encontrado) {
		this.encontrado = encontrado;
	}

	public void marcarEncontrado() {
		this.encontrado = true;
	}

	@Override
	public String toString() {
		return "Coleccionable #" + id +
				" | Encontrado: " + encontrado +
				" | Imagen: " + archivoImagen;
	}

}