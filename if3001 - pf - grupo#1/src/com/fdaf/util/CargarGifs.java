package com.fdaf.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

// Carga EXCLUSIVA de gifs animados. CargarImagenes se mantiene solo para
// imágenes estáticas -- ahí new ImageIcon(getResource(...)) es
// suficiente porque no hay estado de animación que pueda compartirse.
//
// Para gifs, ImageIcon(URL) pasa por la caché interna de Toolkit,
// indexada por URL/nombre de archivo: aunque se construya un ImageIcon
// "nuevo" cada vez, si apunta a la MISMA url que una vez anterior, el
// Image subyacente (y su estado de reproducción/frame actual) puede
// seguir siendo el mismo objeto compartido. Por eso un gif que ya se
// mostró antes "continúa" en vez de reiniciar.
//
// La única forma garantizada de evitarlo es leer los bytes nosotros
// mismos y construir la imagen con Toolkit.createImage(byte[]) -- esa
// variante no tiene ninguna clave con la que una caché pueda
// interceptarla, así que decodifica desde cero siempre, sin excepción.
//
// CORRECCIÓN DE MEMORIA: cada gif decodificado con Toolkit.createImage
// arranca su propio hilo "Image Animator" en AWT. Si el gif está
// codificado en loop infinito, ese hilo nunca termina solo -- retiene
// el búfer de frames decodificados en memoria indefinidamente, sin
// importar si algún componente Swing todavía lo referencia. Perder la
// referencia desde el llamador NO es suficiente para liberarlo.
//
// Se mantiene la última Image entregada por cada ruta, y se llama
// Image.flush() (API estándar de AWT para esto) sobre la anterior justo
// antes de crear la siguiente -- así nunca hay más de una imagen
// "huérfana" por ruta viva a la vez, sin importar cuántas veces se
// reproduzca. La decodificación sigue siendo fresca en cada llamada
// (mismo mecanismo de siempre): esto NO reintroduce el bug de "el gif
// continúa desde un frame anterior", solo libera lo que ya no se usa.
public class CargarGifs {

	private static final Map<String, Image> ultimaImagenPorRuta = new HashMap<>();

	// TEMPORAL: solo para reunir evidencia real antes de decidir el
	// siguiente paso. Retirar una vez cerrada esta investigación.
	private static int contadorLlamadas = 0;

	public static ImageIcon cargarFresco(String rutaClasspath, ImageIcon respaldo) {
		try (InputStream entrada = CargarGifs.class.getResourceAsStream(rutaClasspath)) {
			if (entrada == null) {
				System.out.println("[RECURSO NO ENCONTRADO] " + rutaClasspath);
				return respaldo;
			}
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] trozo = new byte[8192];
			int leidos;
			while ((leidos = entrada.read(trozo)) != -1) {
				buffer.write(trozo, 0, leidos);
			}

			// Libera la imagen anterior de ESTA MISMA ruta antes de crear
			// la nueva -- detiene su hilo Animator y su búfer de frames.
			Image anterior = ultimaImagenPorRuta.get(rutaClasspath);
			if (anterior != null) {
				anterior.flush();
			}

			Image imagenFresca = Toolkit.getDefaultToolkit().createImage(buffer.toByteArray());
			ultimaImagenPorRuta.put(rutaClasspath, imagenFresca);

			contadorLlamadas++;
			registrarMemoria(rutaClasspath);

			return new ImageIcon(imagenFresca);
		} catch (IOException e) {
			e.printStackTrace();
			return respaldo;
		}
	}

	public static ImageIcon cargarFresco(String rutaClasspath) {
		return cargarFresco(rutaClasspath, null);
	}

	// TEMPORAL: memoria completa, no solo freeMemory() (que por sí solo
	// puede verse engañosamente estable si el GC recién corrió, o
	// engañosamente bajo si todavía no le tocaba correr). Los 3 valores
	// juntos permiten distinguir "el GC no ha pasado todavía" de
	// "hay más memoria usada de la que el GC puede recuperar".
	private static void registrarMemoria(String ruta) {
		Runtime rt = Runtime.getRuntime();
		long libre = rt.freeMemory() / 1024 / 1024;
		long total = rt.totalMemory() / 1024 / 1024;
		long usada = total - libre;
		long maxima = rt.maxMemory() / 1024 / 1024;

		System.out.println(String.format(
				"[MEMORIA] Llamada #%d (%s) | Usada: %dMB | Heap total: %dMB | Heap maximo: %dMB",
				contadorLlamadas, ruta, usada, total, maxima));
	}
}