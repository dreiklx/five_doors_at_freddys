package com.fdaf.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Lector de un GIF animado para reproducirlo sincronizado con un audio (video de victoria, CLAUDE.md §1.82).
 * AWT anima los GIF grandes con casi un segundo de retraso acumulado (§1.64), asi que aqui el llamador pide el
 * fotograma que corresponde a la posicion real del audio.
 *
 * Dos modos, elegidos al abrir:
 * - fotogramas independientes (cada uno cubre el lienzo entero, en 0,0, sin transparencia): cualquier fotograma
 *   se decodifica por si solo con {@link #fotograma(int)}, y varios lectores sobre los mismos bytes
 *   ({@link #otroLector()}) lo hacen en paralelo. Es el caso del GIF del video (paleta propia por fotograma).
 * - en otro caso, solo en orden con {@link #siguiente()}, componiendo posicion, transparencia y eliminacion.
 * Las imagenes devueltas son TYPE_INT_RGB nuevas, que el llamador puede conservar.
 */
public final class DecodificadorGif implements AutoCloseable {

	private final byte[] bytes;
	private final ImageInputStream flujo;
	private final ImageReader lector;
	private final int total;
	private final int ancho;
	private final int alto;
	private final int[] retardoMs;
	private final boolean independientes;

	private BufferedImage lienzo;
	private BufferedImage previo;
	private int indice = 0;
	private String disposicionAnterior = "none";
	private int[] rectAnterior;

	private DecodificadorGif(byte[] bytes, Info info) throws IOException {
		this.bytes = bytes;
		this.flujo = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes));
		Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName("gif");
		if (!it.hasNext()) {
			flujo.close();
			throw new IOException("sin lector de GIF");
		}
		this.lector = it.next();
		lector.setInput(flujo, false, false);
		if (info == null) {
			info = analizar(lector);
		}
		this.total = info.total;
		this.ancho = info.ancho;
		this.alto = info.alto;
		this.retardoMs = info.retardoMs;
		this.independientes = info.independientes;
	}

	private static final class Info {
		int total;
		int ancho;
		int alto;
		int[] retardoMs;
		boolean independientes = true;
	}

	/** Abre un GIF del classpath (lo lee entero a memoria). Devuelve null si no existe o no se puede leer. */
	public static DecodificadorGif abrir(String rutaClasspath) {
		try (InputStream in = DecodificadorGif.class.getResourceAsStream(rutaClasspath)) {
			if (in == null) {
				return null;
			}
			ByteArrayOutputStream buf = new ByteArrayOutputStream(1 << 20);
			byte[] trozo = new byte[1 << 16];
			int n;
			while ((n = in.read(trozo)) > 0) {
				buf.write(trozo, 0, n);
			}
			return new DecodificadorGif(buf.toByteArray(), null);
		} catch (IOException | RuntimeException e) {
			System.out.println("[GIF] No se pudo abrir " + rutaClasspath + ": " + e);
			return null;
		}
	}

	/** Un lector mas sobre los mismos bytes (para decodificar en paralelo en otro hilo). */
	public DecodificadorGif otroLector() throws IOException {
		Info info = new Info();
		info.total = total;
		info.ancho = ancho;
		info.alto = alto;
		info.retardoMs = retardoMs;
		info.independientes = independientes;
		return new DecodificadorGif(bytes, info);
	}

	private static Info analizar(ImageReader lector) throws IOException {
		Info info = new Info();
		info.total = lector.getNumImages(true);
		info.retardoMs = new int[info.total];
		IIOMetadata flujoMeta = lector.getStreamMetadata();
		if (flujoMeta != null) {
			Node pantalla = hijo(flujoMeta, "LogicalScreenDescriptor");
			info.ancho = atributoInt(pantalla, "logicalScreenWidth", 0);
			info.alto = atributoInt(pantalla, "logicalScreenHeight", 0);
		}
		for (int i = 0; i < info.total; i++) {
			IIOMetadata meta = lector.getImageMetadata(i);
			Node control = hijo(meta, "GraphicControlExtension");
			Node desc = hijo(meta, "ImageDescriptor");
			info.retardoMs[i] = atributoInt(control, "delayTime", 10) * 10;
			if (i == 0 && (info.ancho <= 0 || info.alto <= 0)) {
				info.ancho = atributoInt(desc, "imageWidth", 1);
				info.alto = atributoInt(desc, "imageHeight", 1);
			}
			boolean completo = atributoInt(desc, "imageLeftPosition", 0) == 0 && atributoInt(desc, "imageTopPosition", 0) == 0
					&& atributoInt(desc, "imageWidth", 0) == info.ancho && atributoInt(desc, "imageHeight", 0) == info.alto;
			boolean transparente = "TRUE".equalsIgnoreCase(atributo(control, "transparentColorFlag", "FALSE"));
			if (!completo || transparente) {
				info.independientes = false;
			}
		}
		return info;
	}

	public int getTotalFotogramas() {
		return total;
	}

	public int getAncho() {
		return ancho;
	}

	public int getAlto() {
		return alto;
	}

	/** Retardo de cada fotograma en ms, tal como lo guarda el GIF (centesimas x 10). */
	public int[] getRetardosMs() {
		return retardoMs.clone();
	}

	public boolean isIndependientes() {
		return independientes;
	}

	/** Fotograma i decodificado por si solo. Solo valido si {@link #isIndependientes()}. */
	public BufferedImage fotograma(int i) throws IOException {
		if (!independientes) {
			throw new IllegalStateException("el GIF compone fotogramas: usar siguiente()");
		}
		return aRgb(lector.read(i));
	}

	/** Siguiente fotograma compuesto sobre el lienzo, en orden; null al terminar. */
	public BufferedImage siguiente() throws IOException {
		if (indice >= total) {
			return null;
		}
		IIOMetadata meta = lector.getImageMetadata(indice);
		BufferedImage sub = lector.read(indice);
		indice++;
		if (lienzo == null) {
			lienzo = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
		}
		Graphics2D g = lienzo.createGraphics();
		if (rectAnterior != null) {   // eliminacion del fotograma anterior, antes de dibujar este
			if ("restoreToBackgroundColor".equals(disposicionAnterior)) {
				g.setComposite(AlphaComposite.Src);
				g.setColor(Color.BLACK);
				g.fillRect(rectAnterior[0], rectAnterior[1], rectAnterior[2], rectAnterior[3]);
			} else if ("restoreToPrevious".equals(disposicionAnterior) && previo != null) {
				g.setComposite(AlphaComposite.Src);
				g.drawImage(previo, 0, 0, null);
			}
		}
		Node desc = hijo(meta, "ImageDescriptor");
		int x = atributoInt(desc, "imageLeftPosition", 0);
		int y = atributoInt(desc, "imageTopPosition", 0);
		String disposicion = atributo(hijo(meta, "GraphicControlExtension"), "disposalMethod", "none");
		if ("restoreToPrevious".equals(disposicion)) {
			previo = aRgb(lienzo);
		}
		g.setComposite(AlphaComposite.SrcOver);   // respeta el indice transparente
		g.drawImage(sub, x, y, null);
		g.dispose();
		disposicionAnterior = disposicion;
		rectAnterior = new int[] { x, y, sub.getWidth(), sub.getHeight() };
		return aRgb(lienzo);
	}

	private static BufferedImage aRgb(BufferedImage origen) {
		BufferedImage rgb = new BufferedImage(origen.getWidth(), origen.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = rgb.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.drawImage(origen, 0, 0, null);
		g.dispose();
		return rgb;
	}

	private static Node hijo(IIOMetadata meta, String nombre) {
		Node raiz = meta.getAsTree(meta.getNativeMetadataFormatName());
		for (Node n = raiz.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (nombre.equals(n.getNodeName())) {
				return n;
			}
		}
		return null;
	}

	private static String atributo(Node n, String nombre, String porDefecto) {
		if (n == null) {
			return porDefecto;
		}
		NamedNodeMap attrs = n.getAttributes();
		Node a = attrs == null ? null : attrs.getNamedItem(nombre);
		return a == null ? porDefecto : a.getNodeValue();
	}

	private static int atributoInt(Node n, String nombre, int porDefecto) {
		try {
			return Integer.parseInt(atributo(n, nombre, String.valueOf(porDefecto)));
		} catch (NumberFormatException e) {
			return porDefecto;
		}
	}

	@Override
	public void close() {
		cerrar();
	}

	/** Libera el lector y su flujo. Se puede llamar varias veces. */
	public void cerrar() {
		try {
			lector.dispose();
		} catch (RuntimeException ignorado) {
			// ya liberado
		}
		try {
			flujo.close();
		} catch (IOException ignorado) {
			// ya cerrado
		}
		lienzo = null;
		previo = null;
	}
}
