package com.fdaf.mvc.models.coleccionables;

public class Coleccionable {

	private static int idCounter = 0;

	private int id;
	private TipoColeccionable tipo;
	private String archivoImagen;
	private boolean encontrado;

	public Coleccionable() {}

	//el constructor ahora recibe el TipoColeccionable (identidad
	// estable, no depende del orden de carga) en vez de un String suelto.
	// archivoImagen se sigue derivando y guardando igual que antes, así
	// que getArchivoImagen() no cambia su comportamiento en ningún lugar
	// que ya lo use.
	public Coleccionable(TipoColeccionable tipo) {
		this.id = ++idCounter;
		this.tipo = tipo;
		this.archivoImagen = tipo.getArchivoDesbloqueado();
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

	public TipoColeccionable getTipo() {
		return tipo;
	}

	public void setTipo(TipoColeccionable tipo) {
		this.tipo = tipo;
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
				" | Tipo: " + tipo +
				" | Encontrado: " + encontrado +
				" | Imagen: " + archivoImagen;
	}

}