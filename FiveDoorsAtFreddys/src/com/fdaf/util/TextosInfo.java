package com.fdaf.util;

// Textos bilingües de la introducción inicial, "Cómo jugar" y "Acerca del
// proyecto" (CLAUDE.md §1.33-§1.34). Mismo mecanismo de idioma que el resto
// del proyecto: PreferenciasJuego.idiomaSeleccionado, leído en el momento de
// pedir cada texto (si el jugador cambia el idioma en Opciones, la próxima
// vez que se abra cada pantalla ya sale en el idioma nuevo). Agrupados aquí
// -- igual que TipoColeccionable.getDescripcion(ingles) -- en vez de
// repartidos por las clases visuales.
//
// Todo lo que describe el juego se verificó contra el código real
// (ControllerJuego, ControllerCamara, ControllerInterfaz, Noche, Juego y,
// para Escape, GameplayScreen) -- no hay controles ni mecánicas inventadas.
public final class TextosInfo {

	private TextosInfo() {
	}

	private static boolean ingles() {
		return PreferenciasJuego.idiomaSeleccionado == Idioma.INGLES;
	}

	private static String t(String es, String en) {
		return ingles() ? en : es;
	}

	/* ===================== Botones y etiquetas ===================== */

	public static String botonAnterior() { return t("Anterior", "Back"); }
	public static String botonSiguiente() { return t("Siguiente", "Next"); }
	public static String botonOmitir() { return t("Omitir", "Skip"); }
	public static String botonIrAlMenu() { return t("Ir al menú", "Go to Menu"); }
	public static String botonVolverAlMenu() { return t("Volver al menú", "Back to Menu"); }
	public static String menuComoJugar() { return t("Cómo jugar", "How to Play"); }
	public static String menuAcercaDe() { return t("Acerca del proyecto", "About the Project"); }

	/* ===================== Selector de idioma (§1.35) ===================== */

	// Aquí el idioma todavía no está elegido: el título se pide para el
	// idioma de la opción resaltada, no para la preferencia guardada.
	public static String tituloSelectorIdioma(Idioma idioma) {
		return idioma == Idioma.INGLES ? "Choose your language" : "Elige tu idioma";
	}

	// Cada idioma se nombra en su propio idioma (endónimo), como es habitual.
	public static String nombreIdioma(Idioma idioma) {
		return idioma == Idioma.INGLES ? "English" : "Español";
	}

	/* ===================== Introducción inicial ===================== */

	// Introducción breve (CLAUDE.md §1.51): dos páginas con lo imprescindible
	// para un jugador nuevo; el detalle está en "Cómo jugar". Las llamadas van
	// en una sola línea discreta al final: no son una mecánica central.
	public static String[] introTitulos() {
		return new String[] {
				t("ANTES DE EMPEZAR", "BEFORE YOU BEGIN"),
				t("CÓMO SE JUEGA", "HOW IT WORKS") };
	}

	public static String[] introCuerpos() {
		return new String[] {
				t("<p><b>Five Doors at Freddy's</b> es un proyecto <b>fan-made</b> inspirado en "
						+ "<i>Five Nights at Freddy's</i>, con un gameplay distinto al original.</p><br>"
						+ "<p>Te recomendamos llegar hasta la <b>Noche 5</b>: al final hay una sorpresa.</p>",
						"<p><b>Five Doors at Freddy's</b> is a <b>fan-made</b> project inspired by "
						+ "<i>Five Nights at Freddy's</i>, with gameplay different from the original.</p><br>"
						+ "<p>We recommend reaching <b>Night 5</b>: there's a surprise at the end.</p>"),
				t("<p><b>Objetivo:</b> encuentra todos los coleccionables de la noche antes de quedarte sin "
						+ "baterías.</p><br>"
						+ "<ul>"
						+ "<li>Lleva el ratón a los bordes para mirar a cada lado.</li>"
						+ "<li>Abre una puerta con <b>PUERTA</b> y enciende <b>LUZ</b> para ver qué hay detrás.</li>"
						+ "<li>¿Animatrónico? Pierdes una batería. ¿Objeto? <b>Haz clic en él</b> antes de que la "
						+ "puerta se cierre.</li>"
						+ "<li>La barra inferior abre la tableta con tu inventario.</li>"
						+ "</ul><br>"
						+ "<p><font color='#a0a0b4'>Las llamadas pueden darte pistas; escucharlas es opcional. "
						+ "La noche empieza cuando terminan o al pulsar <b>COLGAR</b>.</font></p>",
						"<p><b>Goal:</b> find every collectible of the night before you run out of batteries.</p><br>"
						+ "<ul>"
						+ "<li>Move the mouse to the screen edges to look around.</li>"
						+ "<li>Open a door with <b>DOOR</b> and turn on <b>LIGHT</b> to see what's behind it.</li>"
						+ "<li>Animatronic? You lose a battery. Item? <b>Click it</b> before the door closes.</li>"
						+ "<li>The bottom bar opens the tablet with your inventory.</li>"
						+ "</ul><br>"
						+ "<p><font color='#a0a0b4'>Calls may give you tips; listening is optional. "
						+ "The night starts when the call ends or you press <b>MUTE CALL</b>.</font></p>") };
	}

	/* ===================== Cómo jugar (guía del menú) ===================== */

	public static String[] guiaTitulos(boolean escapeDesbloqueado) {
		return new String[] {
				t("Noches", "Nights"),
				t("Puertas y luces", "Doors and Lights"),
				t("Objetos", "Items"),
				t("Controles", "Controls"),
				escapeDesbloqueado ? "Escape" : t("Noche 5", "Night 5") };
	}

	public static String[] guiaCuerpos(boolean escapeDesbloqueado) {
		return new String[] {
				t("<p>El juego tiene <b>5 noches</b>. En cada una ganas cuando encuentras <b>todos sus "
						+ "coleccionables</b> y pierdes si te quedas <b>sin baterías</b>.</p><br>"
						+ "<p>"
						+ "<b>Noche 1:</b> 4 baterías &middot; 5 coleccionables<br>"
						+ "<b>Noche 2:</b> 3 baterías &middot; 6 coleccionables<br>"
						+ "<b>Noche 3:</b> 3 baterías &middot; 7 coleccionables<br>"
						+ "<b>Noche 4:</b> 2 baterías &middot; 8 coleccionables<br>"
						+ "<b>Noche 5:</b> 2 baterías &middot; 10 coleccionables"
						+ "</p><br>"
						+ "<p>Al empezar cada noche suena una llamada: mientras dura, las puertas y las luces no "
						+ "responden. Puedes cortarla con <b>COLGAR</b>.</p>"
						+ "<p>Tu progreso se guarda solo. <b>Continuar</b> retoma tu noche actual y "
						+ "<b>Opciones&nbsp;&gt;&nbsp;Noche&nbsp;personalizada</b> te deja jugar cualquier noche.</p>",
						"<p>The game has <b>5 nights</b>. You win a night by finding <b>all of its "
						+ "collectibles</b> and lose it if you run <b>out of batteries</b>.</p><br>"
						+ "<p>"
						+ "<b>Night 1:</b> 4 batteries &middot; 5 collectibles<br>"
						+ "<b>Night 2:</b> 3 batteries &middot; 6 collectibles<br>"
						+ "<b>Night 3:</b> 3 batteries &middot; 7 collectibles<br>"
						+ "<b>Night 4:</b> 2 batteries &middot; 8 collectibles<br>"
						+ "<b>Night 5:</b> 2 batteries &middot; 10 collectibles"
						+ "</p><br>"
						+ "<p>Each night starts with a phone call: while it plays, doors and lights do not "
						+ "respond. You can cut it short with <b>MUTE CALL</b>.</p>"
						+ "<p>Your progress is saved automatically. <b>Continue</b> resumes your current night and "
						+ "<b>Options&nbsp;&gt;&nbsp;Custom&nbsp;Night</b> lets you play any night.</p>"),
				t("<ul>"
						+ "<li><b>PUERTA</b> abre o cierra la puerta. <b>LUZ</b> enciende o apaga su luz.</li>"
						+ "<li>Lo que hay detrás solo se revela con la puerta <b>abierta</b> y la luz "
						+ "<b>encendida</b> a la vez, en cualquier orden.</li>"
						+ "<li>Cada vez que abres una puerta puede haber algo distinto: un animatrónico, un "
						+ "objeto o nada. No hay dos partidas iguales.</li>"
						+ "<li>Después de revelar algo, la puerta se cierra sola.</li>"
						+ "<li>Si es un <b>animatrónico</b>, sufres su susto y pierdes una batería.</li>"
						+ "</ul>",
						"<ul>"
						+ "<li><b>DOOR</b> opens or closes the door. <b>LIGHT</b> turns its light on or off.</li>"
						+ "<li>What is behind a door is only revealed when the door is <b>open</b> and the light "
						+ "is <b>on</b> at the same time, in any order.</li>"
						+ "<li>Every time you open a door there may be something different: an animatronic, an "
						+ "item or nothing at all. No two games are the same.</li>"
						+ "<li>After something is revealed, the door closes by itself.</li>"
						+ "<li>If it is an <b>animatronic</b>, you get jumpscared and lose a battery.</li>"
						+ "</ul>"),
				t("<ul>"
						+ "<li><b>Coleccionables:</b> haz clic en el objeto antes de que la puerta se cierre "
						+ "(tienes unos segundos). Se guardan en el inventario de la tableta; haz clic en uno "
						+ "para ver su descripción.</li>"
						+ "<li>En el inventario, una silueta indica un coleccionable que aún te falta y una "
						+ "<b>X</b> indica uno que no existe en esa noche.</li>"
						+ "<li><b>Baterías extra:</b> cuando te quedan pocas baterías, a veces aparece una detrás "
						+ "de una puerta. Haz clic en ella para recuperar una batería (máximo 4).</li>"
						+ "<li>Las baterías que te quedan se ven abajo a la izquierda, junto a "
						+ "<b>Energía restante</b>.</li>"
						+ "</ul>",
						"<ul>"
						+ "<li><b>Collectibles:</b> click the item before the door closes (you have a few "
						+ "seconds). They are stored in the tablet's inventory; click one to read its "
						+ "description.</li>"
						+ "<li>In the inventory, a silhouette marks a collectible you are still missing and an "
						+ "<b>X</b> marks one that does not exist in that night.</li>"
						+ "<li><b>Extra batteries:</b> when you are running low, one may appear behind a door. "
						+ "Click it to recover a battery (up to 4).</li>"
						+ "<li>Your remaining batteries are shown at the bottom left, next to "
						+ "<b>Power left</b>.</li>"
						+ "</ul>"),
				t("<p>Las noches se juegan <b>solo con el ratón</b>:</p>"
						+ "<ul>"
						+ "<li><b>Mover el ratón a los bordes:</b> mirar a la izquierda o a la derecha.</li>"
						+ "<li><b>Clic en PUERTA / LUZ:</b> puerta y luz de ese lado.</li>"
						+ "<li><b>Clic en un objeto revelado:</b> recogerlo.</li>"
						+ "<li><b>Pasar el ratón por la barra inferior:</b> abrir o cerrar la tableta.</li>"
						+ "<li><b>Mantener pulsada la barra Rendirse</b> (tableta): abandonar la noche y "
						+ "volver al menú.</li>"
						+ "<li><b>Clic en COLGAR:</b> cortar la llamada.</li>"
						+ "</ul><br>"
						+ "<p>En los menús, <b>ESC</b> vuelve a la pantalla anterior.</p>",
						"<p>Nights are played <b>with the mouse only</b>:</p>"
						+ "<ul>"
						+ "<li><b>Move the mouse to the edges:</b> look left or right.</li>"
						+ "<li><b>Click DOOR / LIGHT:</b> that side's door and light.</li>"
						+ "<li><b>Click a revealed item:</b> pick it up.</li>"
						+ "<li><b>Hover over the bottom bar:</b> open or close the tablet.</li>"
						+ "<li><b>Hold the Surrender bar</b> (tablet): give up the night and go back to the "
						+ "menu.</li>"
						+ "<li><b>Click MUTE CALL:</b> cut the phone call short.</li>"
						+ "</ul><br>"
						+ "<p>In the menus, <b>ESC</b> goes back to the previous screen.</p>"),
				escapeDesbloqueado
						? t("<p><b>Escape</b> es la modalidad que desbloqueaste al superar la Noche 5. Puedes "
								+ "volver a jugarla desde <b>Opciones&nbsp;&gt;&nbsp;Noche&nbsp;personalizada&nbsp;&gt;&nbsp;ESCAPE</b>.</p><br>"
								+ "<ul>"
								+ "<li><b>W A S D:</b> moverte. <b>Ratón:</b> mirar.</li>"
								+ "<li><b>CTRL:</b> encender o apagar la linterna.</li>"
								+ "<li><b>E:</b> interactuar con lo que apunta la mira.</li>"
								+ "<li><b>ESC:</b> pausa.</li>"
								+ "</ul><br>"
								+ "<p>Encuentra la <b>llave</b> y llega a la <b>puerta de salida</b>. Tienes 3 vidas; "
								+ "si te atrapan, pierdes una y también la llave. Los latidos te avisan cuando "
								+ "hay un animatrónico cerca.</p>",
								"<p><b>Escape</b> is the mode you unlocked by completing Night 5. You can play it "
								+ "again from <b>Options&nbsp;&gt;&nbsp;Custom&nbsp;Night&nbsp;&gt;&nbsp;ESCAPE</b>.</p><br>"
								+ "<ul>"
								+ "<li><b>W A S D:</b> move. <b>Mouse:</b> look around.</li>"
								+ "<li><b>CTRL:</b> turn the flashlight on or off.</li>"
								+ "<li><b>E:</b> interact with what the crosshair points at.</li>"
								+ "<li><b>ESC:</b> pause.</li>"
								+ "</ul><br>"
								+ "<p>Find the <b>key</b> and reach the <b>exit door</b>. You have 3 lives; if you "
								+ "get caught you lose one, and the key too. Heartbeats warn you when an "
								+ "animatronic is close.</p>")
						: t("<p>Completar la <b>Noche 5</b> merece la pena, aunque al principio parezca "
								+ "difícil.</p><br>"
								+ "<p>Al final te espera una sorpresa. Cuando la descubras, esta sección te "
								+ "explicará cómo funciona.</p>",
								"<p>Completing <b>Night 5</b> is worth it, even if it seems difficult at first.</p><br>"
								+ "<p>A surprise awaits you at the end. Once you discover it, this section will "
								+ "explain how it works.</p>") };
	}

	/* ===================== Acerca del proyecto ===================== */

	public static String acercaTitulo() {
		return t("ACERCA DEL PROYECTO", "ABOUT THE PROJECT");
	}

	public static String acercaCuerpo() {
		return t("<p><b>Five Doors at Freddy's</b> fue creado como proyecto académico por estudiantes de "
				+ "segundo año de la carrera de <b>Informática Empresarial</b> de la <b>Universidad de Costa "
				+ "Rica</b>, Recinto de Siquirres, en Siquirres, Limón, Costa Rica.</p><br>"
				+ "<p>Todo el juego fue programado en <b>Java</b>, utilizando <b>Eclipse IDE</b>.</p><br>"
				+ "<p>Es un proyecto fan-made inspirado en <i>Five Nights at Freddy's</i>, con una experiencia de "
				+ "juego diferente a la del título original.</p><br>"
				+ "<p>Te recomendamos completar la Noche 5, aunque parezca difícil, para descubrir una "
				+ "sorpresa.</p>",
				"<p><b>Five Doors at Freddy's</b> was created as an academic project by second-year "
				+ "<b>Business Informatics</b> students at the <b>University of Costa Rica</b>, Siquirres Campus, "
				+ "located in Siquirres, Limón, Costa Rica.</p><br>"
				+ "<p>The whole game was programmed in <b>Java</b>, using the <b>Eclipse IDE</b>.</p><br>"
				+ "<p>It is a fan-made project inspired by <i>Five Nights at Freddy's</i>, featuring a gameplay "
				+ "experience that differs from the original title.</p><br>"
				+ "<p>We recommend completing Night 5, even if it seems difficult, to discover a surprise.</p>");
	}
}
