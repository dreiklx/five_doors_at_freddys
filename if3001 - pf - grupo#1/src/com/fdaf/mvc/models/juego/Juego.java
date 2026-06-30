package com.fdaf.mvc.models.juego;

import com.fdaf.mvc.models.coleccionables.CargarColeccionables;
import com.fdaf.mvc.models.coleccionables.ColaColeccionables;
import com.fdaf.mvc.models.coleccionables.Coleccionable;
import com.fdaf.mvc.models.coleccionables.InventarioColeccionables;
import com.fdaf.mvc.models.puerta.Puerta;

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
	// Método para iniciar el juego y generar el árbol de decisiones
	public void iniciarJuego() {

		ColaColeccionables cola = inventario.obtenerPendientes();

		arbol = generador.generar(cola);
	}
	/*
	 * Estos siguientes 2 metodos se encargan de procesar la puerta elegida por el jugador, 
	 * actualizando el estado del juego según el tipo de puerta (coleccionable, animatrónico o nada).
	 */
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

	/*
	 * Este método se encarga de procesar la puerta elegida por el jugador, 
	 * actualizando el estado del juego según el tipo de puerta (coleccionable, animatrónico o nada).
	 */
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

			vidas--;

			break;

		case NADA:

			break;
		}
	}
	/*
	 * Este método verifica si el jugador ha llegado al final de una rama del árbol. 
	 * Si es así, se obtiene una nueva cola de coleccionables pendientes y se genera un nuevo árbol 
	 * para continuar el juego.
	 */
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

		this.vidas = 4;
		this.coleccionablesEncontrados = 0;
		this.ultimaPuerta = null;

		this.inventario = new InventarioColeccionables();
		CargarColeccionables.cargar(inventario);

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