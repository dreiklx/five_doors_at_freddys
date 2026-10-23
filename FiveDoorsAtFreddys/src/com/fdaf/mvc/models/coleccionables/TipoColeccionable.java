package com.fdaf.mvc.models.coleccionables;

// Catálogo maestro de los 10 coleccionables del juego, en orden canónico
// fijo. Cada entrada es la fuente de verdad completa de ese
// coleccionable: sus 2 imágenes y su descripción en ambos idiomas. Nadie
// más en el proyecto (ni ControllerMenu, ni actualizarTextosIdioma)
// necesita conocer descripciones individuales.
public enum TipoColeccionable {

	PELUCHE_FREDDY("collectibles/freddy_plush.png", "collectibles/locked/freddy_plush_locked.png",
			"El peluche original de Freddy Fazbear.", "The original Freddy Fazbear plushie."),
	CUPCAKE("collectibles/cupcake.png", "collectibles/locked/cupcake_locked.png",
			"El famoso cupcake parlante de la pizzería.", "The pizzeria's famous talking cupcake."),
	MICROFONO_FREDDY("collectibles/freddy_microphone.png", "collectibles/locked/freddy_microphone_locked.png",
			"El micrófono que Freddy usa en el escenario.", "The microphone Freddy uses on stage."),
	GUITARRA_BONNIE("collectibles/bonnie_guitar.png", "collectibles/locked/bonnie_guitar_locked.png",
			"La guitarra roja de Bonnie the Bunny.", "Bonnie the Bunny's red guitar."),
	GARFIO_FOXY("collectibles/foxy_hook.png", "collectibles/locked/foxy_hook_locked.png",
			"El garfio característico del capitán Foxy.", "Captain Foxy's signature hook."),
	PELUCHE_BONNIE("collectibles/bonnie_plush.png", "collectibles/locked/bonnie_plush_locked.png",
			"Un peluche de Bonnie the Bunny.", "A Bonnie the Bunny plushie."),
	PELUCHE_CHICA("collectibles/chica_plush.png", "collectibles/locked/chica_plush_locked.png",
			"Un peluche de Chica the Chicken.", "A Chica the Chicken plushie."),
	PELUCHE_FOXY("collectibles/foxy_plush.png", "collectibles/locked/foxy_plush_locked.png",
			"Un peluche del zorro pirata Foxy.", "A plushie of Foxy the Pirate Fox."),
	PELUCHE_GOLDEN_FREDDY("collectibles/golden_freddy_plush.png", "collectibles/locked/golden_freddy_plush_locked.png",
			"El esquivo peluche de Golden Freddy.", "The elusive Golden Freddy plushie."),
	BALLOONBOY("collectibles/balloon_boy_toy.png", "collectibles/locked/balloon_boy_toy_locked.png",
			"El juguete de Balloon Boy.", "The Balloon Boy toy.");

	private final String archivoDesbloqueado;
	private final String archivoBloqueado;
	private final String descripcionEs;
	private final String descripcionIngles;

	TipoColeccionable(String archivoDesbloqueado, String archivoBloqueado,
			String descripcionEs, String descripcionIngles) {
		this.archivoDesbloqueado = archivoDesbloqueado;
		this.archivoBloqueado = archivoBloqueado;
		this.descripcionEs = descripcionEs;
		this.descripcionIngles = descripcionIngles;
	}

	public String getArchivoDesbloqueado() {
		return archivoDesbloqueado;
	}

	public String getArchivoBloqueado() {
		return archivoBloqueado;
	}

	// boolean simple (no el enum Idioma) para no acoplar este paquete de
	// modelo con com.fdaf.util -- el llamador ya hace esa conversión con
	// el mismo patrón que usa actualizarTextosIdioma() en todo el proyecto.
	public String getDescripcion(boolean ingles) {
		return ingles ? descripcionIngles : descripcionEs;
	}
}