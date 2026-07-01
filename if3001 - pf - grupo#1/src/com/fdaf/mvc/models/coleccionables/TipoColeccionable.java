package com.fdaf.mvc.models.coleccionables;

// Catálogo maestro de los 10 coleccionables del juego, en orden canónico
// fijo. Ese orden es literalmente el orden de los 10 espacios de la
// tablet, y "cantidad de esta noche" siempre toma los primeros N de esta
// lista (progresión acumulativa, nunca subconjuntos distintos).
public enum TipoColeccionable {

	PELUCHE_FREDDY("coleccionables/peluche_freddy.png", "coleccionables/bloqueados/peluche_freddy_bloq.png"),
	CUPCAKE("coleccionables/cupcake.png", "coleccionables/bloqueados/cupcake_bloq.png"),
	MICROFONO_FREDDY("coleccionables/microfono_freddy.png", "coleccionables/bloqueados/microfono_freddy_bloq.png"),
	GUITARRA_BONNIE("coleccionables/guitarra_bonnie.png", "coleccionables/bloqueados/guitarra_bonnie_bloq.png"),
	GARFIO_FOXY("coleccionables/garfio_foxy.png", "coleccionables/bloqueados/garfio_foxy_bloq.png"),
	PELUCHE_BONNIE("coleccionables/peluche_bonnie.png", "coleccionables/bloqueados/peluche_bonnie_bloq.png"),
	PELUCHE_CHICA("coleccionables/peluche_chica.png", "coleccionables/bloqueados/peluche_chica_bloq.png"),
	PELUCHE_FOXY("coleccionables/peluche_foxy.png", "coleccionables/bloqueados/peluche_foxy_bloq.png"),
	PELUCHE_GOLDEN_FREDDY("coleccionables/peluche_golden_freddy.png", "coleccionables/bloqueados/peluche_golden_freddy_bloq.png"),
	BALLOONBOY("coleccionables/juguete_balloonboy.png", "coleccionables/bloqueados/juguete_balloonboy_bloq.png");

	private final String archivoDesbloqueado;
	private final String archivoBloqueado;

	TipoColeccionable(String archivoDesbloqueado, String archivoBloqueado) {
		this.archivoDesbloqueado = archivoDesbloqueado;
		this.archivoBloqueado = archivoBloqueado;
	}

	public String getArchivoDesbloqueado() {
		return archivoDesbloqueado;
	}

	public String getArchivoBloqueado() {
		return archivoBloqueado;
	}
}