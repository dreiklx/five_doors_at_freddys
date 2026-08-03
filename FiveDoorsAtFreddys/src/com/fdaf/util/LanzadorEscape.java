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

	// Ruta relativa al propio proceso (Architecture.md #7): los dos
	// proyectos son repos Git hermanos bajo la misma carpeta "git" del
	// usuario -- five_doors_at_freddys/FiveDoorsAtFreddys (este
	// proyecto, de donde se ejecuta) y five_doors_escape (LibGDX),
	// confirmado en disco. Si esta suposicion deja de cumplirse en otra
	// maquina, este es el unico lugar que hace falta ajustar.
	private static final String RUTA_RELATIVA_ESCAPE =
			".." + File.separator + ".." + File.separator + "five_doors_escape";

	// El JDK del PATH por defecto en esta maquina es Java 17; Five Doors
	// Escape requiere Java 21 (Architecture.md, independiente del Java 8
	// de este proyecto). Mismo JAVA_HOME usado para verificar el
	// proyecto LibGDX durante toda su sesion de desarrollo.
	private static final String JAVA_HOME_JDK21 = "C:\\Program Files\\Java\\jdk-21";

	private LanzadorEscape() {
	}

	// alTerminar se invoca en el EDT de Swing cuando el proceso de
	// Escape termina, sin importar por que salida (Game Over, boton
	// manual o victoria) -- todas comparten el mismo mecanismo real de
	// salida (cierre del proceso), asi que este metodo no necesita
	// distinguir entre ellas.
	public static void lanzar(Idioma idioma, int vidasFinales, Runnable alTerminar) {
		escribirTraspaso(idioma, vidasFinales);

		new Thread(() -> {
			try {
				File carpetaEscape = new File(System.getProperty("user.dir"), RUTA_RELATIVA_ESCAPE).getCanonicalFile();
				File gradlew = new File(carpetaEscape, "gradlew.bat");
				ProcessBuilder pb = new ProcessBuilder("cmd", "/c", gradlew.getAbsolutePath(), "lwjgl3:run");
				pb.directory(carpetaEscape);
				pb.environment().put("JAVA_HOME", JAVA_HOME_JDK21);
				pb.inheritIO();

				Process proceso = pb.start();
				proceso.waitFor();
			} catch (IOException | InterruptedException e) {
				System.out.println("[ESCAPE] No se pudo lanzar Five Doors Escape: " + e.getMessage());
			} finally {
				SwingUtilities.invokeLater(alTerminar);
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
