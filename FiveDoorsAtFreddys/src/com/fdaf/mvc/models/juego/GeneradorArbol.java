package com.fdaf.mvc.models.juego;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fdaf.mvc.models.animatronicos.Animatronico;
import com.fdaf.mvc.models.coleccionables.ColaColeccionables;
import com.fdaf.mvc.models.coleccionables.Coleccionable;
import com.fdaf.mvc.models.puerta.Puerta;
import com.fdaf.mvc.models.puerta.TipoPuerta;

public class GeneradorArbol {

	private Random random;

	public GeneradorArbol() {
		this.random = new Random();
	}
	/*
	 * Construye la LISTA COMPLETA de puertas (una por cada coleccionable
	 * pendiente + puertas extra aleatorias) y la pasa a
	 * arbol.construirCompleto(), que ahora arma un árbol binario COMPLETO
	 * por niveles. Con tamaño de lista IMPAR (forzado abajo), no se pierde
	 * ningún nodo y ningún nodo queda con un solo hijo.
	 */
	public Arbol generar(ColaColeccionables cola) {

		List<Puerta> puertas = new ArrayList<Puerta>();

		Puerta raiz = new Puerta(500, TipoPuerta.NADA, null, null);
		puertas.add(raiz);

		while (!cola.isEmpty()) {

			int tipoPuerta = random.nextInt(3) + 1;

			if (tipoPuerta == 2) {

				Coleccionable coleccionable = cola.obtener();

				Puerta puerta = new Puerta(
						generarNumero(),
						TipoPuerta.COLECCIONABLE,
						coleccionable,
						null);

				puertas.add(puerta);

			} else if (tipoPuerta == 1) {

				puertas.add(crearPuertaSinColeccionable());

			} else {

				puertas.add(new Puerta(
						generarNumero(),
						TipoPuerta.NADA,
						null,
						null));
			}
		}

		int cantidadExtra = random.nextInt(5) + 3;

		for (int i = 0; i < cantidadExtra; i++) {

			Puerta puerta = crearPuertaSinColeccionable();

			puertas.add(puerta);
		}

		// Con el algoritmo BFS de Arbol.construirCompleto(),
		// una lista de tamaño IMPAR garantiza DOS cosas a la vez: árbol
		// completo (ningún nodo con un solo hijo) y CERO descartes (no se
		// pierde ningún coleccionable). Si el total es par, se agrega una
		// puerta sin coleccionable para volverlo impar. Es seguro agregar
		// puertas sin coleccionable porque nunca contienen un coleccionable
		// que pudiera perderse.
		if (puertas.size() % 2 == 0) {
			puertas.add(crearPuertaSinColeccionable());
		}

		Arbol arbol = new Arbol();
		arbol.construirCompleto(puertas);

		return arbol;
	}
	/*
	 * Este método se utiliza para crear puertas adicionales
	 *  sin Coleccionables, asegurando que el árbol tenga una mezcla 
	 *  de puertas con y sin Coleccionables.
	 */
	private Puerta crearPuertaSinColeccionable() {

		int numero = generarNumero();

		int opcion = random.nextInt(2);
		// Solo se generan puertas de tipo ANIMATRONICO o NADA, sin Coleccionables
		if (opcion == 0) {

			return new Puerta(
					numero,
					TipoPuerta.ANIMATRONICO,
					null,
					obtenerAnimatronicoAleatorio());
		}
		// En caso de que la opción sea 1, se genera una puerta sin Coleccionable ni animatrónico
		return new Puerta(
				numero,
				TipoPuerta.NADA,
				null,
				null);
	}

	private Animatronico obtenerAnimatronicoAleatorio() {
		
		Animatronico[] animatronicos = Animatronico.values();

		return animatronicos[
				random.nextInt(animatronicos.length)
				];
	}

	private int generarNumero() {
		return random.nextInt(1000) + 1;
	}

}