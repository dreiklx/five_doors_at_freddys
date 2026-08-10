package com.fdaf.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.SwingUtilities;

// Traspaso Swing -> LibGDX (Architecture.md #7, disparado al ganar la
// Noche 5): escribe el archivo de traspaso y lanza el proceso de Five
// Doors Escape sin bloquear el hilo de Swing. Los dos proyectos siguen
// siendo procesos completamente independientes -- la unica
// comunicacion es este archivo mas el ciclo de vida del proceso (ver
// memoria de Claude "project-libgdx-office-spawn-exit-design": todos
// los caminos de salida de Escape -- Game Over, boton manual, victoria
// -- terminan el proceso, y eso es lo unico que este lanzador espera
// para volver a mostrar el menu de Swing).
public class LanzadorEscape {

	private static final String CARPETA_TRASPASO =
			System.getProperty("user.home") + File.separator + ".fivedoorsatfreddys";
	private static final String ARCHIVO_TRASPASO = CARPETA_TRASPASO + File.separator + "handoff.json";

	// Señal de "cerrar TODO" (pedido explicito del usuario 2026-08-06: unificar las pantallas
	// finales de Escape con las del juego principal -- boton "Salir" real, distinto de
	// "Reintentar"). Ver com.fivedoorsescape.io.SalidaCompleta del lado Escape para el porque de
	// este archivo compartido en vez de intentar leer el codigo de salida del proceso (no
	// confiable: el proceso real se lanza envuelto en "cmd /c gradlew.bat lwjgl3:run", y Gradle
	// no garantiza propagar el codigo de salida real de la aplicacion Java hacia afuera).
	private static final String ARCHIVO_SALIDA_COMPLETA = CARPETA_TRASPASO + File.separator + "salir_completo.flag";

	// Ruta relativa al propio proceso (Architecture.md #7): los dos
	// proyectos son repos Git hermanos bajo la misma carpeta "git" del
	// usuario -- five_doors_at_freddys/FiveDoorsAtFreddys (este
	// proyecto, de donde se ejecuta) y five_doors_escape (LibGDX),
	// confirmado en disco. Si esta suposicion deja de cumplirse en otra
	// maquina, este es el unico lugar que hace falta ajustar.
	// Camino de DESARROLLO (gradlew, requiere JDK 21 instalado + internet la
	// primera vez para que Gradle descargue dependencias) -- sigue siendo el
	// que usa cualquiera que corra el proyecto desde el repo real/Eclipse.
	private static final String RUTA_RELATIVA_ESCAPE =
			".." + File.separator + ".." + File.separator + "five_doors_escape";

	// Camino de DISTRIBUCION EMPAQUETADA (investigacion de distribucion
	// portable, 2026-08-10): si existe una carpeta "FiveDoorsEscape" hermana
	// de esta (misma convencion de carpetas hermanas que ya usa el proyecto,
	// solo un nivel en vez de dos porque es la carpeta de la distribucion,
	// no del repo Git) con un runtime JDK 21 y una distribucion ya
	// construida (gradlew lwjgl3:installDist) adentro, se usa DIRECTO --
	// nunca invoca gradlew, no necesita internet, no necesita ningun JDK 21
	// instalado en la maquina destino, no depende de la estructura de
	// carpetas del repositorio Git en absoluto. Ver build-distribucion.bat
	// (raiz del repo) para como se genera esta carpeta.
	private static final String RUTA_RELATIVA_DISTRIBUCION_ESCAPE =
			".." + File.separator + "FiveDoorsEscape";

	private static File resolverDistribucionEmpaquetada() {
		try {
			File carpetaDist = new File(System.getProperty("user.dir"), RUTA_RELATIVA_DISTRIBUCION_ESCAPE).getCanonicalFile();
			File runtimeJava = new File(carpetaDist, "runtime" + File.separator + "bin" + File.separator + "java.exe");
			File scriptLanzador = new File(carpetaDist, "lwjgl3" + File.separator + "bin" + File.separator + "lwjgl3.bat");
			if (runtimeJava.isFile() && scriptLanzador.isFile()) {
				return carpetaDist;
			}
		} catch (IOException e) {
			// Ruta inválida/inaccesible -- se trata igual que "no encontrada", cae al modo desarrollo.
		}
		return null;
	}

	// Five Doors Escape requiere Java 21 (Architecture.md, independiente
	// del Java 8 de este proyecto), y el JDK del PATH por defecto no
	// siempre es compatible (en la maquina de desarrollo original era
	// Java 17). Antes esto era una ruta absoluta fija
	// ("C:\Program Files\Java\jdk-21") -- funcionaba solo en esa maquina
	// concreta. resolverJavaHome21() la reemplaza por una busqueda real
	// entre ubicaciones de instalacion habituales de varios proveedores,
	// para que un companero de equipo con Java 21 instalado en otro lado
	// (u otra unidad) no necesite tocar el codigo para poder jugar Escape.
	private static final String[] CARPETAS_CANDIDATAS_JDK21 = {
			"C:\\Program Files\\Java",
			"C:\\Program Files\\Eclipse Adoptium",
			"C:\\Program Files\\Microsoft\\jdk-21",
			"C:\\Program Files\\Zulu",
			"C:\\Program Files\\BellSoft"
	};

	private LanzadorEscape() {
	}

	// Devuelve un JAVA_HOME real que apunte a un JDK 21, o null si no se
	// encontro ninguno (en ese caso se deja que el proceso hijo use el
	// JAVA_HOME/PATH que ya tenga el sistema, en vez de forzar una ruta
	// que no existe en esta maquina).
	private static String resolverJavaHome21() {
		String javaHomeActual = System.getenv("JAVA_HOME");
		if (javaHomeActual != null && esJdk21(new File(javaHomeActual))) {
			return javaHomeActual;
		}

		for (String carpeta : CARPETAS_CANDIDATAS_JDK21) {
			File[] hijos = new File(carpeta).listFiles();
			if (hijos == null) continue;

			for (File hijo : hijos) {
				if (esJdk21(hijo)) {
					return hijo.getAbsolutePath();
				}
			}
		}
		return null;
	}

	private static boolean esJdk21(File carpetaJdk) {
		if (!carpetaJdk.isDirectory() || !carpetaJdk.getName().contains("21")) {
			return false;
		}
		return new File(carpetaJdk, "bin" + File.separator + "java.exe").isFile();
	}

	// alTerminar se invoca en el EDT de Swing cuando el proceso de
	// Escape termina, sin importar por que salida (Game Over, boton
	// manual o victoria) -- todas comparten el mismo mecanismo real de
	// salida (cierre del proceso), asi que este metodo no necesita
	// distinguir entre ellas.
	public static void lanzar(Idioma idioma, int vidasFinales, Runnable alTerminar) {
		escribirTraspaso(idioma, vidasFinales);

		// Por si quedo de una sesion anterior (no deberia, pero evita un falso positivo si algo
		// dejo el archivo sin limpiar).
		new File(ARCHIVO_SALIDA_COMPLETA).delete();

		new Thread(() -> {
			try {
				ProcessBuilder pb;
				File carpetaDistribucion = resolverDistribucionEmpaquetada();

				if (carpetaDistribucion != null) {
					// Distribucion empaquetada (build-distribucion.bat): runtime JDK 21 propio +
					// gradlew lwjgl3:installDist ya construido -- nunca gradlew, nunca internet,
					// nunca depende de la estructura de carpetas del repo Git.
					File runtimeJava = new File(carpetaDistribucion, "runtime");
					File carpetaLwjgl3 = new File(carpetaDistribucion, "lwjgl3");
					File script = new File(carpetaLwjgl3, "bin" + File.separator + "lwjgl3.bat");

					System.out.println("[ESCAPE] Usando distribucion empaquetada: " + carpetaLwjgl3.getAbsolutePath());
					pb = new ProcessBuilder("cmd", "/c", script.getAbsolutePath());
					pb.directory(carpetaLwjgl3);
					pb.environment().put("JAVA_HOME", runtimeJava.getAbsolutePath());
				} else {
					// Camino de desarrollo (repo Git real, gradlew) -- comportamiento identico al
					// que ya existia antes de que se investigara la distribucion empaquetada.
					File carpetaEscape = new File(System.getProperty("user.dir"), RUTA_RELATIVA_ESCAPE).getCanonicalFile();
					File gradlew = new File(carpetaEscape, "gradlew.bat");

					if (!gradlew.isFile()) {
						System.out.println("[ESCAPE] No se encontro " + gradlew.getAbsolutePath()
								+ " -- clona five_doors_escape como carpeta hermana de five_doors_at_freddys "
								+ "(ver SETUP.md en la raiz del repositorio) antes de completar la Noche 5.");
						return;
					}

					pb = new ProcessBuilder("cmd", "/c", gradlew.getAbsolutePath(), "lwjgl3:run");
					pb.directory(carpetaEscape);

					String javaHome21 = resolverJavaHome21();
					if (javaHome21 != null) {
						pb.environment().put("JAVA_HOME", javaHome21);
					} else {
						System.out.println("[ESCAPE] No se encontro un JDK 21 instalado -- se usara el JAVA_HOME/PATH "
								+ "actual del sistema, que puede no ser compatible con Five Doors Escape.");
					}
				}
				pb.inheritIO();

				Process proceso = pb.start();
				proceso.waitFor();
			} catch (IOException | InterruptedException e) {
				System.out.println("[ESCAPE] No se pudo lanzar Five Doors Escape: " + e.getMessage());
			} finally {
				File archivoSalidaCompleta = new File(ARCHIVO_SALIDA_COMPLETA);
				if (archivoSalidaCompleta.exists()) {
					archivoSalidaCompleta.delete();
					// El jugador presiono "Salir" (no "Reintentar") en una pantalla final de
					// Escape -- cierra la aplicacion COMPLETA, no solo vuelve al menu.
					SwingUtilities.invokeLater(() -> System.exit(0));
				} else {
					SwingUtilities.invokeLater(alTerminar);
				}
			}
		}, "LanzadorEscape").start();
	}

	private static void escribirTraspaso(Idioma idioma, int vidasFinales) {
		File carpeta = new File(CARPETA_TRASPASO);
		if (!carpeta.exists()) {
			carpeta.mkdirs();
		}

		String json = "{\"idioma\":\"" + idioma.name() + "\",\"vidasFinales\":" + vidasFinales + "}";
		try (FileWriter escritor = new FileWriter(ARCHIVO_TRASPASO)) {
			escritor.write(json);
		} catch (IOException e) {
			System.out.println("[ESCAPE] No se pudo escribir el archivo de traspaso: " + e.getMessage());
		}
	}
}
