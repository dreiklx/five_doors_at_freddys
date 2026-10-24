package com.fdaf.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

import com.fdaf.mvc.models.juego.Noche;

// Persistencia con java.util.Properties -- JDK estándar, cero
// dependencias nuevas. Un único archivo de texto plano con 3 valores.
// Se guarda en la carpeta del usuario (no junto al .jar), para evitar
// problemas de permisos de escritura si el juego se ejecuta desde una
// ubicación protegida.
public class PersistenciaJuego {

	private static final String CARPETA =
			System.getProperty("user.home") + File.separator + ".fivedoorsatfreddys";
	private static final String ARCHIVO = CARPETA + File.separator + "config.properties";

	// Se llama una sola vez, al arrancar la aplicación, ANTES de que
	// cualquier pantalla lea PreferenciasJuego. Si el archivo no existe
	// (primera ejecución) o algún valor es inválido, se conservan los
	// valores por defecto ya declarados en PreferenciasJuego -- nunca
	// deja el estado a medio cargar ni lanza una excepción hacia afuera.
	public static void cargar() {

		File archivo = new File(ARCHIVO);
		if (!archivo.exists()) {
			return;
		}

		Properties props = new Properties();

		try (FileInputStream entrada = new FileInputStream(archivo)) {
			props.load(entrada);
		} catch (IOException e) {
			System.out.println("[PERSISTENCIA] No se pudo leer el archivo: " + e.getMessage());
			return;
		}

		try {
			String idioma = props.getProperty("idioma");
			if (idioma != null) {
				PreferenciasJuego.idiomaSeleccionado = Idioma.valueOf(idioma);
			}
		} catch (IllegalArgumentException e) {
			System.out.println("[PERSISTENCIA] Idioma inválido en el archivo, se usa el valor por defecto.");
		}

		try {
			String noche = props.getProperty("nocheActual");
			if (noche != null) {
				PreferenciasJuego.nocheActual = Noche.valueOf(noche);
			}
		} catch (IllegalArgumentException e) {
			System.out.println("[PERSISTENCIA] Noche inválida en el archivo, se usa el valor por defecto.");
		}

		try {
			String volumen = props.getProperty("volumen");
			if (volumen != null) {
				PreferenciasJuego.volumenGeneral = Integer.parseInt(volumen);
			}
			// Migra valores guardados antes de este cambio (podían ser 0)
			// al nuevo piso del sistema, sin esperar a que el jugador
			// toque el control manualmente.
			PreferenciasJuego.volumenGeneral = Math.max(1, Math.min(10, PreferenciasJuego.volumenGeneral));
		} catch (NumberFormatException e) {
			System.out.println("[PERSISTENCIA] Volumen inválido en el archivo, se usa el valor por defecto.");
		}

		// Ausente en archivos guardados antes de este cambio -- se interpreta como
		// "todavia no desbloqueado", nunca lanza excepcion.
		PreferenciasJuego.escapeDesbloqueado = Boolean.parseBoolean(props.getProperty("escapeDesbloqueado", "false"));

		// Noches superadas (§1.66), p. ej. "NOCHE_1,NOCHE_2". Migración
		// conservadora de archivos anteriores a este cambio (sin la clave): no
		// se concede nada que el archivo no demuestre. nocheActual no sirve
		// como prueba (Noche personalizada la cambiaba libremente); lo único
		// demostrado es escapeDesbloqueado, que solo se activa al ganar la
		// Noche 5 -- así que esa, y ninguna otra, queda superada.
		java.util.Arrays.fill(PreferenciasJuego.nochesSuperadas, false);
		String superadas = props.getProperty("nochesSuperadas");
		if (superadas == null) {
			if (PreferenciasJuego.escapeDesbloqueado) {
				PreferenciasJuego.nochesSuperadas[Noche.NOCHE_5.ordinal()] = true;
			}
		} else {
			for (String nombre : superadas.split(",")) {
				try {
					if (!nombre.trim().isEmpty()) {
						PreferenciasJuego.nochesSuperadas[Noche.valueOf(nombre.trim()).ordinal()] = true;
					}
				} catch (IllegalArgumentException e) {
					System.out.println("[PERSISTENCIA] Noche superada inválida en el archivo: " + nombre);
				}
			}
		}

		// Ausente en archivos anteriores: todavía no completó Escape.
		PreferenciasJuego.escapeCompletado = Boolean.parseBoolean(props.getProperty("escapeCompletado", "false"));
	}

	// Se llama cada vez que algo en PreferenciasJuego cambia y debe
	// sobrevivir al cierre del juego. Reescribe el archivo completo con
	// el estado actual -- simple y suficiente para 3 valores.
	public static void guardar() {

		Properties props = new Properties();
		props.setProperty("idioma", PreferenciasJuego.idiomaSeleccionado.name());
		props.setProperty("nocheActual", PreferenciasJuego.nocheActual.name());
		props.setProperty("volumen", String.valueOf(PreferenciasJuego.volumenGeneral));
		props.setProperty("escapeDesbloqueado", String.valueOf(PreferenciasJuego.escapeDesbloqueado));
		StringBuilder superadas = new StringBuilder();
		for (Noche noche : Noche.values()) {
			if (PreferenciasJuego.nocheSuperada(noche)) {
				superadas.append(superadas.length() > 0 ? "," : "").append(noche.name());
			}
		}
		props.setProperty("nochesSuperadas", superadas.toString());
		props.setProperty("escapeCompletado", String.valueOf(PreferenciasJuego.escapeCompletado));

		File carpeta = new File(CARPETA);
		if (!carpeta.exists()) {
			carpeta.mkdirs();
		}

		// Se escribe a un temporal y se renombra encima (§1.66): un cierre o un
		// corte a mitad de escritura nunca deja config.properties a medias.
		File temporal = new File(CARPETA, "config.properties.tmp");
		try (FileOutputStream salida = new FileOutputStream(temporal)) {
			props.store(salida, "Five Doors at Freddy's - configuracion del jugador");
		} catch (IOException e) {
			System.out.println("[PERSISTENCIA] No se pudo guardar el archivo: " + e.getMessage());
			return;
		}
		try {
			try {
				Files.move(temporal.toPath(), new File(ARCHIVO).toPath(), StandardCopyOption.REPLACE_EXISTING,
						StandardCopyOption.ATOMIC_MOVE);
			} catch (java.nio.file.AtomicMoveNotSupportedException e) {
				Files.move(temporal.toPath(), new File(ARCHIVO).toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			System.out.println("[PERSISTENCIA] No se pudo reemplazar el archivo: " + e.getMessage());
		}
	}
}