package com.fdaf.mvc.models.juego;

import com.fdaf.mvc.models.artefactos.Artefacto;
import com.fdaf.mvc.models.artefactos.CargadorArtefactos;
import com.fdaf.mvc.models.artefactos.ColaArtefactos;
import com.fdaf.mvc.models.artefactos.InventarioArtefactos;
import com.fdaf.mvc.models.puerta.Puerta;

public class Juego {

	private Arbol arbol;
	private InventarioArtefactos inventario;
	private GeneradorArbol generador;

	private int vidas;
	private int artefactosEncontrados;
	private Puerta ultimaPuerta;

	public Juego() {

		this.vidas = 3;
		this.artefactosEncontrados = 0;

		this.inventario = new InventarioArtefactos();
		this.generador = new GeneradorArbol();

		CargadorArtefactos.cargar(inventario);

	}
	// Método para iniciar el juego y generar el árbol de decisiones
	public void iniciarJuego() {

		ColaArtefactos cola = inventario.obtenerPendientes();

		arbol = generador.generar(cola);
	}
	/*
	 * Estos siguientes 2 metodos se encargan de procesar la puerta elegida por el jugador, 
	 * actualizando el estado del juego según el tipo de puerta (artefacto, animatrónico o nada).
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
	 * actualizando el estado del juego según el tipo de puerta (artefacto, animatrónico o nada).
	 */
	private void procesarPuerta(Puerta puerta) {

		if(puerta == null)
			return;

		ultimaPuerta = puerta;

		switch(puerta.getTipo()) {

		case ARTEFACTO:

			Artefacto artefacto = puerta.getArtefacto();

			if(artefacto != null) {

				Artefacto original =
						inventario.buscar(artefacto.getId());

				if(original != null &&
						!original.isEncontrado()) {

					original.setEncontrado(true);

					artefactosEncontrados++;
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
	 * Si es así, se obtiene una nueva cola de artefactos pendientes y se genera un nuevo árbol 
	 * para continuar el juego.
	 */
	private void verificarFinDeRama() {

		if(arbol.finDeRama()) {

			ColaArtefactos cola =
					inventario.obtenerPendientes();

			if(!cola.isEmpty()) {

				arbol =
						generador.generar(cola);
			}
		}
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

	public int getArtefactosEncontrados() {
		return artefactosEncontrados;
	}

	public Arbol getArbol() {
		return arbol;
	}

	public InventarioArtefactos getInventario() {
		return inventario;
	}

	public Puerta getUltimaPuerta() {
		return ultimaPuerta;
	}

}