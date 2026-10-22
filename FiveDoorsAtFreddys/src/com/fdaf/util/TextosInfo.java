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

	/* ===================== Introducción inicial ===================== */

	public static String[] introTitulos() {
		return new String[] {
				t("ANTES DE EMPEZAR", "BEFORE YOU BEGIN"),
				t("CÓMO SE JUEGA", "HOW IT WORKS"),
				t("UN ÚLTIMO CONSEJO", "ONE LAST TIP") };
	}

	public static String[] introCuerpos() {
		return new String[] {
				t("<p><b>Five Doors at Freddy's</b> es un proyecto fan-made inspirado en el universo de "
						+ "<i>Five Nights at Freddy's</i>.</p><br>"
						+ "<p>No es el juego original ni pretende reproducir exactamente sus mecánicas. Ofrece una "
						+ "experiencia diferente, con sus propios desafíos y una progresión que irás descubriendo "
						+ "mientras juegas.</p>",
						"<p><b>Five Doors at Freddy's</b> is a fan-made project inspired by the "
						+ "<i>Five Nights at Freddy's</i> universe.</p><br>"
						+ "<p>It is not the original game and does not attempt to reproduce its mechanics exactly. "
						+ "It offers a different gameplay experience, with its own challenges and a progression "
						+ "for you to discover as you play.</p>"),
				t("<p>Pasas la noche en una oficina con dos puertas. Para superarla debes <b>encontrar todos los "
						+ "coleccionables</b> de esa noche antes de quedarte sin baterías.</p><br>"
						+ "<ul>"
						+ "<li>Lleva el ratón hacia los bordes de la pantalla para mirar a cada lado. Junto a cada "
						+ "puerta hay un botón <b>DOOR</b> y otro <b>LIGHT</b>.</li>"
						+ "<li>Abre una puerta y enciende su luz para descubrir qué hay detrás.</li>"
						+ "<li>Si aparece un animatrónico, pierdes una batería. Si aparece un objeto, "
						+ "<b>haz clic en él</b> antes de que la puerta se cierre.</li>"
						+ "<li>Pasa el ratón por la barra inferior para abrir la tableta: noche, inventario y "
						+ "opción de rendirte.</li>"
						+ "</ul><br>"
						+ "<p>Todo se juega con el ratón. Encontrarás la guía completa en <b>Cómo jugar</b>, "
						+ "en el menú principal.</p>",
						"<p>You spend the night in an office with two doors. To survive it you must <b>find every "
						+ "collectible</b> of that night before you run out of batteries.</p><br>"
						+ "<ul>"
						+ "<li>Move the mouse to the edges of the screen to look to each side. Next to each door "
						+ "there is a <b>DOOR</b> button and a <b>LIGHT</b> button.</li>"
						+ "<li>Open a door and turn on its light to find out what is behind it.</li>"
						+ "<li>If an animatronic shows up, you lose a battery. If an item shows up, "
						+ "<b>click it</b> before the door closes.</li>"
						+ "<li>Hover over the bottom bar to open the tablet: night, inventory and the "
						+ "option to surrender.</li>"
						+ "</ul><br>"
						+ "<p>Everything is played with the mouse. The full guide is in <b>How to Play</b>, "
						+ "in the main menu.</p>"),
				t("<p>Te recomendamos <b>superar la Noche 5</b>, aunque al principio parezca difícil.</p><br>"
						+ "<p>Ten paciencia, aprende las mecánicas y no te rindas demasiado pronto.</p><br>"
						+ "<p>Hay una sorpresa esperándote al final.</p>",
						"<p>We recommend <b>completing Night 5</b>, even if it seems difficult at first.</p><br>"
						+ "<p>Be patient, learn the mechanics and don't give up too soon.</p><br>"
						+ "<p>There's a surprise waiting for you at the end.</p>") };
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
						+ "<table cellpadding='2'>"
						+ "<tr><td>Noche 1</td><td>4 baterías</td><td>5 coleccionables</td></tr>"
						+ "<tr><td>Noche 2</td><td>3 baterías</td><td>6 coleccionables</td></tr>"
						+ "<tr><td>Noche 3</td><td>3 baterías</td><td>7 coleccionables</td></tr>"
						+ "<tr><td>Noche 4</td><td>2 baterías</td><td>8 coleccionables</td></tr>"
						+ "<tr><td>Noche 5</td><td>2 baterías</td><td>10 coleccionables</td></tr>"
						+ "</table><br>"
						+ "<p>Al empezar cada noche suena una llamada: mientras dura, las puertas y las luces no "
						+ "responden. Puedes cortarla con <b>MUTE CALL</b>.</p>"
						+ "<p>Tu progreso se guarda solo. <b>Continuar</b> retoma tu noche actual y "
						+ "<b>Opciones &gt; Custom Night</b> te deja jugar cualquier noche.</p>",
						"<p>The game has <b>5 nights</b>. You win a night by finding <b>all of its "
						+ "collectibles</b> and lose it if you run <b>out of batteries</b>.</p><br>"
						+ "<table cellpadding='2'>"
						+ "<tr><td>Night 1</td><td>4 batteries</td><td>5 collectibles</td></tr>"
						+ "<tr><td>Night 2</td><td>3 batteries</td><td>6 collectibles</td></tr>"
						+ "<tr><td>Night 3</td><td>3 batteries</td><td>7 collectibles</td></tr>"
						+ "<tr><td>Night 4</td><td>2 batteries</td><td>8 collectibles</td></tr>"
						+ "<tr><td>Night 5</td><td>2 batteries</td><td>10 collectibles</td></tr>"
						+ "</table><br>"
						+ "<p>Each night starts with a phone call: while it plays, doors and lights do not "
						+ "respond. You can cut it short with <b>MUTE CALL</b>.</p>"
						+ "<p>Your progress is saved automatically. <b>Continue</b> resumes your current night and "
						+ "<b>Options &gt; Custom Night</b> lets you play any night.</p>"),
				t("<ul>"
						+ "<li><b>DOOR</b> abre o cierra la puerta. <b>LIGHT</b> enciende o apaga su luz.</li>"
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
						+ "<b>Power left</b>.</li>"
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
						+ "<li><b>Clic en DOOR / LIGHT:</b> puerta y luz de ese lado.</li>"
						+ "<li><b>Clic en un objeto revelado:</b> recogerlo.</li>"
						+ "<li><b>Pasar el ratón por la barra inferior:</b> abrir o cerrar la tableta.</li>"
						+ "<li><b>Mantener pulsada la barra Rendirse</b> (tableta): abandonar la noche y "
						+ "volver al menú.</li>"
						+ "<li><b>Clic en MUTE CALL:</b> cortar la llamada.</li>"
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
								+ "volver a jugarla desde <b>Opciones &gt; Custom Night &gt; ESCAPE</b>.</p><br>"
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
								+ "again from <b>Options &gt; Custom Night &gt; ESCAPE</b>.</p><br>"
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
