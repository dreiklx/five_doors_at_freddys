package com.fdaf.mvc.models.juego;

import com.fdaf.mvc.models.coleccionables.CargarColeccionables;
import com.fdaf.mvc.models.coleccionables.ColaColeccionables;
import com.fdaf.mvc.models.coleccionables.Coleccionable;
import com.fdaf.mvc.models.coleccionables.InventarioColeccionables;
import com.fdaf.mvc.models.puerta.Puerta;
import com.fdaf.util.PreferenciasJuego;

public class Juego {

	private Arbol arbol;
	private InventarioColeccionables inventario;
	private GeneradorArbol generador;

	private int vidas;
	private int coleccionablesEncontrados;
	private Puerta ultimaPuerta;

	public Juego() {

		this.generador = new GeneradorArbol();
		reiniciar();

	}
	public void iniciarJuego() {

		ColaColeccionables cola = inventario.obtenerPendientes();

		arbol = generador.generar(cola);
	}
	public Puerta elegirIzquierda() {

		if(arbol == null)
			return null;

		if(arbol.moverIzquierda()) {

			Puerta puerta = arbol.getPuertaActual();

			procesarPuerta(puerta);

			verificarFinDeRama();

			return puerta;
		}

		return null;
	}

	public Puerta elegirDerecha() {

		if(arbol == null)
			return null;

		if(arbol.moverDerecha()) {

			Puerta puerta = arbol.getPuertaActual();

			procesarPuerta(puerta);

			verificarFinDeRama();

			return puerta;
		}

		return null;
	}

	private void procesarPuerta(Puerta puerta) {

		if(puerta == null)
			return;

		ultimaPuerta = puerta;

		switch(puerta.getTipo()) {

		case COLECCIONABLE:

			Coleccionable coleccionable = puerta.getColeccionable();

			if(coleccionable != null) {

				Coleccionable original =
						inventario.buscar(coleccionable.getId());

				if(original != null &&
						!original.isEncontrado()) {

					original.setEncontrado(true);

					coleccionablesEncontrados++;
				}
			}

			break;

		case ANIMATRONICO:

			break;

		case NADA:

			break;
		}
	}

	public void perderVidaPorAnimatronico() {
		vidas--;
	}

	private void verificarFinDeRama() {

		if(arbol.finDeRama()) {

			ColaColeccionables cola =
					inventario.obtenerPendientes();

			if(!cola.isEmpty()) {

				arbol =
						generador.generar(cola);
			}
		}
	}

	public void reiniciar() {

		Coleccionable.setIdCounter(0);
		Puerta.setIdCounter(0);

		// vidas y cantidad de coleccionables ya no son fijos.
		// Se leen de la noche/dificultad seleccionada por el jugador.
		this.vidas = PreferenciasJuego.nocheSeleccionada.getVidas();
		this.coleccionablesEncontrados = 0;
		this.ultimaPuerta = null;

		this.inventario = new InventarioColeccionables();
		CargarColeccionables.cargar(inventario,
				PreferenciasJuego.nocheSeleccionada.getCantidadColeccionables());

		iniciarJuego();
	}

	public boolean gano() {

		return inventario.todosEncontrados();
	}

	public boolean perdio() {

		return vidas <= 0;
	}

	public int getVidas() {
		return vidas;
	}

	public int getColeccionablesEncontrados() {
		return coleccionablesEncontrados;
	}

	public Arbol getArbol() {
		return arbol;
	}

	public InventarioColeccionables getInventario() {
		return inventario;
	}

	public Puerta getUltimaPuerta() {
		return ultimaPuerta;
	}

}