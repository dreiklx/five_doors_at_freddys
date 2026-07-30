package com.fdaf.mvc.models.juego;

import com.fdaf.mvc.models.coleccionables.CargarColeccionables;
import com.fdaf.mvc.models.coleccionables.ColaColeccionables;
import com.fdaf.mvc.models.coleccionables.Coleccionable;
import com.fdaf.mvc.models.coleccionables.InventarioColeccionables;
import com.fdaf.mvc.models.puerta.Puerta;
import com.fdaf.util.PreferenciasJuego;
import java.util.Random;

public class Juego {

	private Arbol arbol;
	private InventarioColeccionables inventario;
	private GeneradorArbol generador;

	private int vidas;
	private int coleccionablesEncontrados;
	private Puerta ultimaPuerta;

	private static final Random randomBateria = new Random();
	private static final int VIDAS_MAXIMAS = 4; // límite real de íconos en alActualizarVidas
	
	public Juego() {
		this.generador = new GeneradorArbol();
		reiniciar();
	}

	public void iniciarJuego() {
		ColaColeccionables cola = inventario.obtenerPendientes();
		arbol = generador.generar(cola);
	}

	public Puerta elegirIzquierda() {
		if (arbol == null) return null;

		if (arbol.moverIzquierda()) {
			Puerta puerta = arbol.getPuertaActual();
			procesarPuerta(puerta);
			verificarFinDeRama();
			return puerta;
		}
		return null;
	}

	public Puerta elegirDerecha() {
		if (arbol == null) return null;

		if (arbol.moverDerecha()) {
			Puerta puerta = arbol.getPuertaActual();
			procesarPuerta(puerta);
			verificarFinDeRama();
			return puerta;
		}
		return null;
	}
	
	
	private void procesarPuerta(Puerta puerta) {
		if (puerta == null) return;
		ultimaPuerta = puerta;
		switch (puerta.getTipo()) {
		case COLECCIONABLE:
			// CAMBIO: ya NO se marca aquí. Abrir la puerta solo consume el
			// nodo del árbol y determina el contenido; el conteo real
			// ocurre en marcarColeccionableEncontrado(), invocado
			// únicamente cuando el jugador efectivamente lo recoge con
			// clic. Si el timeout vence sin recogerlo, nunca se cuenta.
			break;
		case ANIMATRONICO:
			break;
		case NADA:
			// Reinterpretación dinámica, en el momento REAL de apertura
			// (no en la generación del árbol) -- las vidas usadas aquí
			// son las actuales de este instante exacto.
			if (calificaParaBateria()) {
				puerta.setTipo(com.fdaf.mvc.models.puerta.TipoPuerta.BATERIA);
			}
			break;
		default:
			break;
		}
	}
	
	// [noche][0]=probabilidad con 1 vida, [noche][1]=probabilidad con 2
	// vidas. La tabla completa vive aquí como dato -- ajustar el balance
	// en el futuro es editar estos números, no tocar la lógica de abajo.
	private static final double[][] PROBABILIDAD_BATERIA = {
			{ 0.08, 0.05 }, // Noche 1
			{ 0.11, 0.08 }, // Noche 2
			{ 0.28, 0.13 }, // Noche 3
			{ 0.32, 0.16 }, // Noche 4
			{ 0.36, 0.19 }, // Noche 5
	};

	private boolean calificaParaBateria() {

		if (vidas != 1 && vidas != 2) {
			return false;
		}

		int noche = PreferenciasJuego.nocheActual.ordinal();
		double probabilidad = PROBABILIDAD_BATERIA[noche][vidas - 1];

		return randomBateria.nextDouble() < probabilidad;
	}

	// Único punto real donde un coleccionable pasa a contar para la
	// victoria. Se invoca exclusivamente desde
	// ControllerJuego.recogerColeccionable(), es decir, solo cuando el
	// jugador hizo clic a tiempo -- nunca por timeout.
	public void marcarColeccionableEncontrado(Coleccionable coleccionable) {
		if (coleccionable == null) return;

		Coleccionable original = inventario.buscar(coleccionable.getId());

		if (original != null && !original.isEncontrado()) {
			original.setEncontrado(true);
			coleccionablesEncontrados++;
		}
	}

	public void perderVidaPorAnimatronico() {
		vidas--;
	}

	// Simétrico a perderVidaPorAnimatronico(). Tope duro en 4: límite
	// técnico real del proyecto (alActualizarVidas solo tiene íconos
	// hasta bateria4), no una preferencia de diseño.
	public void ganarVidaExtra() {
		if (vidas < VIDAS_MAXIMAS) {
			vidas++;
		}
	}

	private void verificarFinDeRama() {
		if (arbol.finDeRama()) {
			ColaColeccionables cola = inventario.obtenerPendientes();
			if (!cola.isEmpty()) {
				arbol = generador.generar(cola);
			}
		}
	}

	public void reiniciar() {
		Coleccionable.setIdCounter(0);
		Puerta.setIdCounter(0);

		this.vidas = PreferenciasJuego.nocheActual.getVidas();
		this.coleccionablesEncontrados = 0;
		this.ultimaPuerta = null;

		this.inventario = new InventarioColeccionables();
		CargarColeccionables.cargar(inventario,
				PreferenciasJuego.nocheActual.getCantidadColeccionables());

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