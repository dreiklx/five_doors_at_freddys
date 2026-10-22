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

		int resultado = leerBloques(rutaClasspath);
		if (resultado <= 0) {
			resultado = calcularMilisegundos(rutaClasspath, respaldoMs);
		}
		CACHE_MS.put(rutaClasspath, resultado);
		return resultado;
	}

	// Lectura directa de la estructura del gif (CLAUDE.md §1.52): recorre los
	// bloques, suma el retardo de cada Graphic Control Extension y SALTA los
	// datos de imagen sin decodificarlos. Con ImageIO, la intro de la Noche 1
	// (12-14 MB) tardaba 480-895 ms en el hilo de Swing al empezar la
	// partida; así se lee en lo que tarda recorrer el archivo. Devuelve -1 si
	// el archivo no tiene la forma esperada (entonces se usa ImageIO).
	private static int leerBloques(String rutaClasspath) {
		try (InputStream bruto = DuracionGif.class.getResourceAsStream(rutaClasspath)) {
			if (bruto == null) {
				return -1;
			}
			java.io.DataInputStream in = new java.io.DataInputStream(new java.io.BufferedInputStream(bruto, 65536));
			byte[] cabecera = new byte[13];
			in.readFully(cabecera);
			if (cabecera[0] != 'G' || cabecera[1] != 'I' || cabecera[2] != 'F') {
				return -1;
			}
			int banderas = cabecera[10] & 0xFF;
			if ((banderas & 0x80) != 0) {
				saltar(in, 3 * (1 << ((banderas & 0x07) + 1)));
			}
			int totalCentisegundos = 0;
			while (true) {
				int bloque = in.read();
				if (bloque == 0x3B || bloque == -1) {
					break;
				}
				if (bloque == 0x21) {
					int etiqueta = in.readUnsignedByte();
					if (etiqueta == 0xF9) {
						int tam = in.readUnsignedByte();
						byte[] gce = new byte[tam];
						in.readFully(gce);
						if (tam >= 3) {
							totalCentisegundos += (gce[1] & 0xFF) | ((gce[2] & 0xFF) << 8);
						}
					}
					saltarSubbloques(in);
				} else if (bloque == 0x2C) {
					byte[] descriptor = new byte[9];
					in.readFully(descriptor);
					int local = descriptor[8] & 0xFF;
					if ((local & 0x80) != 0) {
						saltar(in, 3 * (1 << ((local & 0x07) + 1)));
					}
					in.readUnsignedByte(); // tamaño mínimo de código LZW
					saltarSubbloques(in);
				} else {
					return -1;
				}
			}
			return totalCentisegundos > 0 ? totalCentisegundos * 10 : -1;
		} catch (Exception e) {
			return -1;
		}
	}

	private static void saltarSubbloques(java.io.DataInputStream in) throws java.io.IOException {
		int tam;
		while ((tam = in.readUnsignedByte()) != 0) {
			saltar(in, tam);
		}
	}

	private static void saltar(java.io.DataInputStream in, int n) throws java.io.IOException {
		while (n > 0) {
			int s = (int) in.skip(n);
			if (s <= 0) {
				in.readByte();
				s = 1;
			}
			n -= s;
		}
	}

	// Solo para pruebas: duración calculada por cada método, sin caché.
	static int[] compararMetodos(String rutaClasspath) {
		return new int[] { leerBloques(rutaClasspath), calcularMilisegundos(rutaClasspath, -1) };
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
