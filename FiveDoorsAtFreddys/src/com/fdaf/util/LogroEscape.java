package com.fdaf.util;

import java.io.File;

// Victoria de Five Doors Escape -> estrella permanente y agradecimiento en FDAF
// (CLAUDE.md §1.66). Escape y FDAF son procesos distintos (Architecture.md
// #7): Escape escribe ~/.fivedoorsatfreddys/escape_victoria.flag en el
// instante en que la victoria queda registrada de verdad (GameplayScreen,
// "escapado", al terminar o saltar la cinemática del auto), con escritura a un
// archivo temporal y renombrado, así que nunca se lee a medias. FDAF lo
// consume aquí la próxima vez que va a mostrar el menú: tras volver de Escape
// el proceso se reinicia (ReiniciadorJuego) y el menú del proceso nuevo pasa
// por aquí; si FDAF se hubiera cerrado mientras Escape seguía abierto, se
// consume en el siguiente arranque. Solo FDAF escribe config.properties (Escape
// solo escribe su propia señal), así que los dos procesos nunca escriben el
// mismo archivo. Abrir la puerta, rendirse, perder o cerrar Escape no escriben
// la señal.
public final class LogroEscape {

	public static final int SIN_VICTORIA = 0;
	public static final int VICTORIA_REPETIDA = 1;
	public static final int PRIMERA_VICTORIA = 2;

	private static final String ARCHIVO_SENAL = System.getProperty("user.home") + File.separator
			+ ".fivedoorsatfreddys" + File.separator + "escape_victoria.flag";

	private LogroEscape() {
	}

	// Una victoria por señal: el archivo se borra DESPUÉS de guardar el logro,
	// así que una victoria no se cuenta dos veces y tampoco se pierde si el
	// guardado fallara a medias (la señal seguiría ahí para el próximo intento).
	public static int consumirVictoriaPendiente() {
		File senal = new File(ARCHIVO_SENAL);
		if (!senal.isFile()) {
			return SIN_VICTORIA;
		}
		boolean primera = !PreferenciasJuego.escapeCompletado;
		if (primera) {
			PreferenciasJuego.escapeCompletado = true;
			PersistenciaJuego.guardar();
		}
		if (!senal.delete()) {
			System.out.println("[LOGRO] No se pudo borrar la señal de victoria de Escape: " + senal);
		}
		System.out.println("[LOGRO] Victoria de Escape registrada" + (primera ? " (estrella desbloqueada)" : ""));
		return primera ? PRIMERA_VICTORIA : VICTORIA_REPETIDA;
	}
}
