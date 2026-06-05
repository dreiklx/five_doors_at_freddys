package com.fdaf.mvc.models.puerta;

import com.fdaf.mvc.models.animatronicos.Animatronico;
import com.fdaf.mvc.models.coleccionables.Coleccionable;

public class Puerta {

	private static int idCounter = 0;

	private int id;
	private int numero;
	private TipoPuerta tipo;
	private Coleccionable coleccionable;
	private Animatronico animatronico;

	public Puerta() {}

	public Puerta(int numero, TipoPuerta tipo, Coleccionable coleccionable, Animatronico animatronico) {
		this.id = ++idCounter;
		this.numero = numero;
		this.tipo = tipo;
		this.coleccionable = coleccionable;
		this.animatronico = animatronico;
	}

	public static int getIdCounter() {
		return idCounter;
	}

	public static void setIdCounter(int idCounter) {
		Puerta.idCounter = idCounter;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getNumero() {
		return numero;
	}

	public void setNumero(int numero) {
		this.numero = numero;
	}

	public TipoPuerta getTipo() {
		return tipo;
	}

	public void setTipo(TipoPuerta tipo) {
		this.tipo = tipo;
	}

	public Coleccionable getColeccionable() {
		return coleccionable;
	}

	public void setColeccionable(Coleccionable coleccionable) {
		this.coleccionable = coleccionable;
	}

	public Animatronico getAnimatronico() {
		return animatronico;
	}

	public void setAnimatronico(Animatronico animatronico) {
		this.animatronico = animatronico;
	}

	@Override
	public String toString() {
		return "Puerta [id=" + id +
				", numero=" + numero +
				", tipo=" + tipo + "]";
	}
}