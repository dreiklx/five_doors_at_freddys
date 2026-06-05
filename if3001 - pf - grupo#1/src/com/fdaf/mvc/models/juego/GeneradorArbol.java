package com.fdaf.mvc.models.juego;

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
	 * Este método genera un árbol de puertas utilizando una cola de coleccionables. 
	 * Se crean puertas con coleccionables mientras la cola no esté vacía, 
	 * y luego se agregan puertas adicionales sin coleccionables 
	 * para diversificar el contenido del árbol.
	 */
	public Arbol generar(ColaColeccionables cola) {

		Arbol arbol = new Arbol();

		while (!cola.isEmpty()) {

			Coleccionable coleccionable = cola.obtener();

			Puerta puerta = new Puerta(
					generarNumero(),
					TipoPuerta.COLECCIONABLE,
					coleccionable,
					null);

			arbol.store(puerta);

			int extras = random.nextInt(3);

			for(int i = 0; i < extras; i++) {

				arbol.store(crearPuertaSinColeccionable());

			}
		}

		int cantidadExtra = random.nextInt(5) + 3;

		for (int i = 0; i < cantidadExtra; i++) {

			Puerta puerta = crearPuertaSinColeccionable();

			arbol.store(puerta);
		}

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
		// Genera un número aleatorio entre 1 y 1000 para asignar a la puerta
		return random.nextInt(1000) + 1;
	}

}