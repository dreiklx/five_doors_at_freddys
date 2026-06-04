package com.fdaf.mvc.models.juego;

import java.util.Random;

import com.fdaf.mvc.models.animatronicos.Animatronico;
import com.fdaf.mvc.models.artefactos.Artefacto;
import com.fdaf.mvc.models.artefactos.ColaArtefactos;
import com.fdaf.mvc.models.puerta.Puerta;
import com.fdaf.mvc.models.puerta.TipoPuerta;

public class GeneradorArbol {

	private Random random;

	public GeneradorArbol() {
		this.random = new Random();
	}
	/*
	 * Este método genera un árbol de puertas utilizando una cola de artefactos. 
	 * Se crean puertas con artefactos mientras la cola no esté vacía, 
	 * y luego se agregan puertas adicionales sin artefactos 
	 * para diversificar el contenido del árbol.
	 */
	public Arbol generar(ColaArtefactos cola) {

		Arbol arbol = new Arbol();

		while (!cola.isEmpty()) {

			Artefacto artefacto = cola.obtener();

			Puerta puerta = new Puerta(
					generarNumero(),
					TipoPuerta.ARTEFACTO,
					artefacto,
					null);

			arbol.store(puerta);

			int extras = random.nextInt(3);

			for(int i = 0; i < extras; i++) {

				arbol.store(crearPuertaSinArtefacto());

			}
		}

		int cantidadExtra = random.nextInt(5) + 3;

		for (int i = 0; i < cantidadExtra; i++) {

			Puerta puerta = crearPuertaSinArtefacto();

			arbol.store(puerta);
		}

		return arbol;
	}
	/*
	 * Este método se utiliza para crear puertas adicionales
	 *  sin artefactos, asegurando que el árbol tenga una mezcla 
	 *  de puertas con y sin artefactos.
	 */
	private Puerta crearPuertaSinArtefacto() {

		int numero = generarNumero();

		int opcion = random.nextInt(2);
		// Solo se generan puertas de tipo ANIMATRONICO o NADA, sin artefactos
		if (opcion == 0) {

			return new Puerta(
					numero,
					TipoPuerta.ANIMATRONICO,
					null,
					obtenerAnimatronicoAleatorio());
		}
		// En caso de que la opción sea 1, se genera una puerta sin artefacto ni animatrónico
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