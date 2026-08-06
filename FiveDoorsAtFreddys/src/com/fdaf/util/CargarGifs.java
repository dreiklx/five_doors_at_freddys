package com.fdaf.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
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

	// Observador dedicado y persistente, propio de esta clase -- NUNCA el Component "c" del
	// paintIcon de un Icon (eso ya esta prohibido, ver la REGLA ABSOLUTA en
	// EscalarVista.GifEscalado -- causo corrupcion visual documentada). Causa raiz real
	// investigada 2026-08-05: GifEscalado.paintIcon pasa null como observer a drawImage, e
	// ImageIcon solo mantiene un observador (via MediaTracker) hasta que el PRIMER frame esta
	// completo, no durante el resto de la animacion. Sin NINGUN consumidor activo registrado
	// despues de eso, el hilo de decodificacion de gifs multi-frame de AWT puede pausarse tras un
	// periodo sin observadores -- confirmado real con gameover.gif (37 frames, el primero con un
	// delay de 7 SEGUNDOS): tiempo mas que suficiente para que el decodificador se pause y jamas
	// retome, dejando la animacion congelada en el frame 0 durante toda la secuencia de Game
	// Over real. Los gifs de puertas nunca mostraron este bug porque sus delays son uniformemente
	// cortos (~50ms), sin ningun hueco así de largo. Registrar este observador (que no hace nada
	// mas que seguir escuchando) mantiene el consumo activo durante toda la vida del Image, sin
	// tocar el pintado en absoluto.
	private static final ImageObserver MANTENER_ANIMACION_ACTIVA = (img, infoflags, x, y, w, h) -> true;

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

			// Mantiene el consumo activo durante toda la vida del Image -- ver
			// MANTENER_ANIMACION_ACTIVA arriba para la causa raiz completa.
			Toolkit.getDefaultToolkit().prepareImage(imagenFresca, -1, -1, MANTENER_ANIMACION_ACTIVA);

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