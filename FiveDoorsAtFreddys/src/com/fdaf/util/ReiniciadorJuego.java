package com.fdaf.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import com.fdaf.mvc.views.frames.VistaPrincipal;

// Reinicio COMPLETO del proceso al terminar una noche (ganar 1-4, perder o
// rendirse) -- decision explicita del usuario 2026-08-09 tras varias
// rondas de parches puntuales de memoria (CLAUDE.md #1.7, #1.13-#1.15):
// en vez de seguir cazando fugas de Swing/AWT/audio una por una, matar el
// proceso entero al final de cada noche garantiza que el sistema operativo
// libere TODO (hilos "Image Animator" de gifs, lineas de audio nativas,
// lo que sea) sin depender de que cada recurso se haya cerrado bien a
// mano. El progreso real (noche/idioma/volumen) ya vive en
// ~/.fivedoorsatfreddys/config.properties via PersistenciaJuego, escrito
// ANTES de este punto en los 3 caminos que llaman a reiniciar() -- la
// nueva instancia lo recupera sola en su arranque normal (Main.main ya
// llama PersistenciaJuego.cargar() antes de nada), sin necesitar pasarle
// nada por linea de comandos.
public class ReiniciadorJuego {

	// Da tiempo a que la nueva instancia arranque (JVM + Swing + carga de
	// assets iniciales) antes de que la vieja desaparezca -- evita el
	// instante en que no hay NINGUNA ventana visible, que se veria como
	// si el juego se hubiera cerrado solo. 900ms es mas que suficiente en
	// la practica (un arranque limpio de Main tarda un par de cientos de
	// ms) sin alargar la transicion de forma perceptible.
	private static final int ESPERA_ANTES_DE_CERRAR_MS = 900;

	private ReiniciadorJuego() {
	}

	// vp: para mostrar la pantalla de carga mientras se relanza.
	// siFalla: que hacer si el relanzamiento no pudo iniciarse (p. ej. no
	// se encontro java.exe) -- nunca se debe dejar al jugador congelado en
	// la pantalla de carga para siempre; cada llamador pasa el mismo
	// comportamiento que tenia antes de existir este mecanismo (volver al
	// menu dentro del proceso actual).
	public static void reiniciar(VistaPrincipal vp, Runnable siFalla) {
		mostrarPantallaDeCarga(vp);

		Timer t = new Timer(ESPERA_ANTES_DE_CERRAR_MS, null);
		t.addActionListener(e -> {
			((Timer) e.getSource()).stop();
			if (lanzarNuevaInstancia()) {
				System.exit(0);
			} else {
				siFalla.run();
			}
		});
		t.setRepeats(false);
		t.start();
	}

	private static void mostrarPantallaDeCarga(VistaPrincipal vp) {
		boolean ingles = (PreferenciasJuego.idiomaSeleccionado == Idioma.INGLES);

		JPanel pantalla = new JPanel(new BorderLayout());
		pantalla.setBackground(Color.BLACK);

		JLabel lbl = new JLabel(ingles ? "Loading..." : "Cargando...", SwingConstants.CENTER);
		lbl.setForeground(Color.WHITE);
		lbl.setFont(Fuentes.obtener(40));
		pantalla.add(lbl, BorderLayout.CENTER);

		vp.setContenido(pantalla);
		pantalla.revalidate();
		pantalla.repaint();
	}

	// Relanza exactamente el mismo main (com.fdaf.init.Main) con el mismo
	// java.home/classpath/directorio de trabajo que ya esta usando este
	// proceso -- no depende de gradlew ni de ninguna ruta ajena a este
	// proyecto (a diferencia de LanzadorEscape, que sí necesita resolver
	// otro repo). Sin inheritIO/redirect a proposito: este proceso esta a
	// punto de morir (System.exit() unos milisegundos despues de esta
	// llamada); si el hijo escribe a stdout despues de que el padre ya
	// termino, esa escritura simplemente falla en silencio (comportamiento
	// normal y ya tolerado por PrintStream), no hace falta redirigirla a
	// ningun lado.
	private static boolean lanzarNuevaInstancia() {
		try {
			String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java.exe";
			String classpath = System.getProperty("java.class.path");

			if (!new File(javaBin).isFile()) {
				System.out.println("[REINICIO] No se encontro java.exe en " + javaBin);
				return false;
			}

			// "--reinicio-interno": le dice a Main.main() que esta instancia nace de un
			// reinicio de proceso, no de un lanzamiento externo real -- evita que la
			// advertencia inicial (PnlWarning) reaparezca en cada noche (pedido explicito
			// del usuario 2026-08-09, ver ControllerMenu).
			ProcessBuilder pb = new ProcessBuilder(javaBin, "-cp", classpath, "com.fdaf.init.Main", "--reinicio-interno");
			pb.directory(new File(System.getProperty("user.dir")));
			pb.start();
			return true;
		} catch (IOException e) {
			System.out.println("[REINICIO] No se pudo relanzar el proceso: " + e.getMessage());
			return false;
		}
	}
}
