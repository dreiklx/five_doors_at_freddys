package com.fdaf.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Registro de la consola en archivo (CLAUDE.md §1.81). Desde el .exe (launch4j, javaw) la salida estandar no va a
 * ninguna parte: cuando un probador decia "no aparece el video" o "no arranca Escape" no habia ningun mensaje que
 * mirar. Ahora todo lo que el juego escribe en System.out/err se copia tambien a
 * ~/.fivedoorsatfreddys/logs/fdaf.log, y la salida de Five Doors Escape (proceso hijo) a escape.log.
 *
 * Se anade al final del archivo (el juego se relanza a si mismo tras cada noche, §1.18, y el registro de la noche
 * anterior debe sobrevivir). Al pasar de 1 MB se rota a .1 (solo se conserva una copia anterior).
 */
public final class RegistroArchivo {

	private static final long TAMANO_MAXIMO = 1_000_000L;

	private RegistroArchivo() {
	}

	public static File carpeta() {
		return new File(System.getProperty("user.home") + File.separator + ".fivedoorsatfreddys" + File.separator + "logs");
	}

	/** Archivo de registro listo para anadir (carpeta creada y rotado si era grande). */
	public static File preparar(String nombre) {
		File dir = carpeta();
		dir.mkdirs();
		File archivo = new File(dir, nombre);
		if (archivo.length() > TAMANO_MAXIMO) {
			File anterior = new File(dir, nombre + ".1");
			anterior.delete();
			archivo.renameTo(anterior);
		}
		return archivo;
	}

	/** Copia System.out y System.err a fdaf.log. Si no se puede escribir, el juego sigue igual que antes. */
	public static void instalar() {
		try {
			File archivo = preparar("fdaf.log");
			OutputStream salidaArchivo = new FileOutputStream(archivo, true);
			PrintStream encabezado = new PrintStream(salidaArchivo, true, "UTF-8");
			encabezado.println();
			encabezado.println("===== " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
					+ " FDAF " + Version.texto() + " | java " + System.getProperty("java.version")
					+ " | " + System.getProperty("os.name") + " " + System.getProperty("os.version")
					+ " | dir " + System.getProperty("user.dir"));
			System.setOut(new PrintStream(new Doble(System.out, salidaArchivo), true, "UTF-8"));
			System.setErr(new PrintStream(new Doble(System.err, salidaArchivo), true, "UTF-8"));
		} catch (IOException | RuntimeException e) {
			System.out.println("[REGISTRO] No se pudo abrir el archivo de registro: " + e);
		}
	}

	/** Escribe en el archivo y en la salida original; un fallo de la original (javaw, tuberia cerrada) se ignora. */
	private static final class Doble extends OutputStream {
		private final OutputStream original;
		private final OutputStream archivo;

		Doble(OutputStream original, OutputStream archivo) {
			this.original = original;
			this.archivo = archivo;
		}

		@Override
		public void write(int b) throws IOException {
			write(new byte[] { (byte) b }, 0, 1);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			synchronized (archivo) {
				try {
					archivo.write(b, off, len);
				} catch (IOException ignorado) {
					// disco lleno o sin permisos: la consola sigue funcionando
				}
			}
			try {
				original.write(b, off, len);
			} catch (IOException ignorado) {
				// sin consola
			}
		}

		@Override
		public void flush() {
			try {
				archivo.flush();
			} catch (IOException ignorado) {
				// nada
			}
			try {
				original.flush();
			} catch (IOException ignorado) {
				// nada
			}
		}
	}
}
