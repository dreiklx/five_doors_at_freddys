package com.fdaf.mvc.models.coleccionables;

// Catálogo maestro de los 10 coleccionables del juego, en orden canónico
// fijo. Cada entrada es la fuente de verdad completa de ese
// coleccionable: sus 2 imágenes y su descripción en ambos idiomas. Nadie
// más en el proyecto (ni ControllerMenu, ni actualizarTextosIdioma)
// necesita conocer descripciones individuales.
public enum TipoColeccionable {

	PELUCHE_FREDDY("coleccionables/peluche_freddy.png", "coleccionables/bloqueados/peluche_freddy_bloq.png",
			"El peluche original de Freddy Fazbear.", "The original Freddy Fazbear plushie."),
	CUPCAKE("coleccionables/cupcake.png", "coleccionables/bloqueados/cupcake_bloq.png",
			"El famoso cupcake parlante de la pizzería.", "The pizzeria's famous talking cupcake."),
	MICROFONO_FREDDY("coleccionables/microfono_freddy.png", "coleccionables/bloqueados/microfono_freddy_bloq.png",
			"El micrófono que Freddy usa en el escenario.", "The microphone Freddy uses on stage."),
	GUITARRA_BONNIE("coleccionables/guitarra_bonnie.png", "coleccionables/bloqueados/guitarra_bonnie_bloq.png",
			"La guitarra roja de Bonnie the Bunny.", "Bonnie the Bunny's red guitar."),
	GARFIO_FOXY("coleccionables/garfio_foxy.png", "coleccionables/bloqueados/garfio_foxy_bloq.png",
			"El garfio característico del capitán Foxy.", "Captain Foxy's signature hook."),
	PELUCHE_BONNIE("coleccionables/peluche_bonnie.png", "coleccionables/bloqueados/peluche_bonnie_bloq.png",
			"Un peluche de Bonnie the Bunny.", "A Bonnie the Bunny plushie."),
	PELUCHE_CHICA("coleccionables/peluche_chica.png", "coleccionables/bloqueados/peluche_chica_bloq.png",
			"Un peluche de Chica the Chicken.", "A Chica the Chicken plushie."),
	PELUCHE_FOXY("coleccionables/peluche_foxy.png", "coleccionables/bloqueados/peluche_foxy_bloq.png",
			"Un peluche del zorro pirata Foxy.", "A plushie of Foxy the Pirate Fox."),
	PELUCHE_GOLDEN_FREDDY("coleccionables/peluche_golden_freddy.png", "coleccionables/bloqueados/peluche_golden_freddy_bloq.png",
			"El esquivo peluche de Golden Freddy.", "The elusive Golden Freddy plushie."),
	BALLOONBOY("coleccionables/juguete_balloonboy.png", "coleccionables/bloqueados/juguete_balloonboy_bloq.png",
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