package com.fdaf.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// Lee la duración real de un gif animado sumando el delay real de cada uno
// de sus frames (metadata GraphicControlExtension, en centisegundos) --
// nunca un valor hardcodeado. Si el usuario vuelve a cambiar la velocidad
// del gif en el futuro, este valor se recalcula solo a partir del archivo
// real, sin tocar ninguna constante de código (mismo principio que
// WavDuration ya usa del lado de five_doors_escape para el audio).
public class DuracionGif {

	private DuracionGif() {
	}

	// Cacheado por ruta: estos gifs se reproducen en cada apertura/cierre de
	// puerta, potencialmente muchas veces por partida -- sin caché, cada
	// toggle repetiría la misma lectura de metadata de disco innecesariamente.
	private static final Map<String, Integer> CACHE_MS = new HashMap<>();

	public static int leerMilisegundos(String rutaClasspath, int respaldoMs) {
		Integer cacheado = CACHE_MS.get(rutaClasspath);
		if (cacheado != null) {
			return cacheado;
		}

		int resultado = calcularMilisegundos(rutaClasspath, respaldoMs);
		CACHE_MS.put(rutaClasspath, resultado);
		return resultado;
	}

	private static int calcularMilisegundos(String rutaClasspath, int respaldoMs) {
		try (InputStream entrada = DuracionGif.class.getResourceAsStream(rutaClasspath)) {
			if (entrada == null) {
				return respaldoMs;
			}

			try (ImageInputStream iis = ImageIO.createImageInputStream(entrada)) {
				Iterator<ImageReader> lectores = ImageIO.getImageReaders(iis);
				if (!lectores.hasNext()) {
					return respaldoMs;
				}

				ImageReader lector = lectores.next();
				lector.setInput(iis);

				int totalCentisegundos = 0;
				int numeroFrames = lector.getNumImages(true);
				for (int i = 0; i < numeroFrames; i++) {
					IIOMetadata metadata = lector.getImageMetadata(i);
					Node raiz = metadata.getAsTree("javax_imageio_gif_image_1.0");
					NodeList hijos = raiz.getChildNodes();
					for (int j = 0; j < hijos.getLength(); j++) {
						Node nodo = hijos.item(j);
						if ("GraphicControlExtension".equals(nodo.getNodeName())) {
							String delay = nodo.getAttributes().getNamedItem("delayTime").getNodeValue();
							totalCentisegundos += Integer.parseInt(delay);
						}
					}
				}
				lector.dispose();

				if (totalCentisegundos <= 0) {
					return respaldoMs;
				}
				return totalCentisegundos * 10;
			}
		} catch (Exception e) {
			return respaldoMs;
		}
	}
}
