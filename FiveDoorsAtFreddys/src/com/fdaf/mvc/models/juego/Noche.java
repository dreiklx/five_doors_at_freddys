package com.fdaf.mvc.models.juego;

// transporta datos de REGLA de juego (vidas, cantidad de coleccionables),

public enum Noche {

	NOCHE_1(4, 5),
	NOCHE_2(3, 6),
	NOCHE_3(3, 7),
	NOCHE_4(2, 8),
	NOCHE_5(2, 10);

	private final int vidas;
	private final int cantidadColeccionables;

	Noche(int vidas, int cantidadColeccionables) {
		this.vidas = vidas;
		this.cantidadColeccionables = cantidadColeccionables;
	}

	public int getVidas() {
		return vidas;
	}

	public int getCantidadColeccionables() {
		return cantidadColeccionables;
	}
}