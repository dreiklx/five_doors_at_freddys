package com.fdaf.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.fdaf.mvc.views.multimedia.Sonido;

/**
 * Video = GIF animado + audio WAV aparte, sincronizados por la posicion real del audio (CLAUDE.md §1.82).
 *
 * Por que no se anima el GIF con AWT (como los demas GIF del juego, §1.7): AWT reproduce los GIF grandes con casi
 * un segundo de retraso acumulado respecto a sus retardos (medido en §1.64), y no deja elegir el fotograma; con un
 * audio aparte eso es una desincronizacion visible. Aqui cada fotograma se elige por la posicion del Clip de audio
 * (el reloj del dispositivo, no el de Java), asi que imagen y sonido arrancan juntos y no se separan aunque el
 * audio tarde en empezar o un fotograma llegue tarde: en ese caso se repite el anterior y el siguiente ya cae en
 * su sitio. No usa JavaFX ni ningun codec del sistema: solo ImageIO (GIF) y javax.sound (WAV).
 *
 * Decodificacion: si cada fotograma del GIF es completo (el del video lo es, §1.82), 1-3 hilos daemon los
 * decodifican en paralelo unos pocos por delante; si no, un hilo los compone en orden. Solo se guardan en memoria
 * los fotogramas cercanos al actual (~8 MB cada uno a 1920x1080).
 */
public final class ReproductorVideoGif {

	private static final int POR_DELANTE = 6;
	private static final int INTERVALO_MS = 10;
	private static final int ESPERA_PRIMER_FOTOGRAMA_MS = 3000;
	/** Si el audio no empieza a sonar en este tiempo (sin dispositivo), la imagen sigue sola. El arranque en frio
	 * del dispositivo medido fue de hasta 1.7 s (§1.82). */
	private static final int ESPERA_AUDIO_MS = 3000;

	private final String rutaGif;
	private final String rutaAudio;
	private final Component destino;
	private final Runnable alTerminar;

	private final ConcurrentHashMap<Integer, BufferedImage> cache = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Integer, Boolean> enProceso = new ConcurrentHashMap<>();
	private final AtomicBoolean terminado = new AtomicBoolean(false);
	private final List<Thread> hilos = new ArrayList<>();
	private volatile int pedido = 0;
	private volatile boolean enCurso = false;
	private int total;
	private long[] inicioMs;
	private long duracionMs;
	private Sonido audio;
	private Timer timer;
	private long t0Nanos;
	private long ultimoT = 0;
	private long ultimoAudio = -1;
	/** Ultima posicion leida del Clip (-2: todavia no se leyo; -1: no hay Clip abierto) y cuando se leyo. */
	private volatile long posicionAudio = -2;
	private volatile long muestraNanos;
	private boolean sinAudio = false;
	private long posicionAnterior = -1;
	private long paredAvance;
	private long inicioSinAudio;
	private long paredUltimoAudio;

	// estadisticas para las pruebas (§1.82)
	private volatile int pintados = 0;
	private volatile int pintadosConRetraso = 0;
	private volatile long maxRetrasoMs = 0;
	private volatile int ultimoPintado = -1;
	private volatile long arranqueAudioMs = -1;
	private volatile boolean entregado = false;
	private volatile long maxSaltoReloj = 0;
	private volatile long tickMaxMs = 0;
	private long ultimoTick = -1;
	private final java.util.BitSet mostradosSet = new java.util.BitSet();
	private volatile long pintadoMaxNanos = 0;
	private volatile long pintadoSumaNanos = 0;
	private volatile int pintadoVeces = 0;

	public ReproductorVideoGif(String rutaGif, String rutaAudio, Component destino, Runnable alTerminar) {
		this.rutaGif = rutaGif;
		this.rutaAudio = rutaAudio;
		this.destino = destino;
		this.alTerminar = alTerminar;
	}

	/** Prepara fuera del EDT (abre el GIF, decodifica el primer fotograma y abre el audio) y arranca en el EDT. */
	public void iniciar() {
		Thread prep = new Thread(this::preparar, "video-gif-prep");
		prep.setDaemon(true);
		prep.start();
	}

	private void preparar() {
		DecodificadorGif principal = DecodificadorGif.abrir(rutaGif);
		if (principal == null || principal.getTotalFotogramas() == 0) {
			System.out.println("[VIDEO] no se pudo abrir " + rutaGif + ": se omite el video");
			if (principal != null) {
				principal.cerrar();
			}
			SwingUtilities.invokeLater(() -> terminar(true));
			return;
		}
		total = principal.getTotalFotogramas();
		inicioMs = new long[total];
		long acumulado = 0;
		int[] retardos = principal.getRetardosMs();
		for (int i = 0; i < total; i++) {
			inicioMs[i] = acumulado;
			acumulado += retardos[i];
		}
		long duracionGif = acumulado;
		boolean independientes = principal.isIndependientes();
		int cantidad = independientes ? Math.max(1, Math.min(3, Runtime.getRuntime().availableProcessors() - 1)) : 1;
		try {
			for (int h = 0; h < cantidad; h++) {
				DecodificadorGif lector = principal.otroLector();
				Runnable trabajo = independientes ? () -> decodificarEnParalelo(lector) : () -> decodificarEnOrden(lector);
				Thread t = new Thread(trabajo, "video-gif-" + h);
				t.setDaemon(true);
				hilos.add(t);
			}
		} catch (IOException e) {
			System.out.println("[VIDEO] no se pudo preparar el GIF: " + e);
		} finally {
			principal.cerrar();
		}
		hilos.forEach(Thread::start);
		long limite = System.currentTimeMillis() + ESPERA_PRIMER_FOTOGRAMA_MS;
		while (!cache.containsKey(0) && System.currentTimeMillis() < limite && !terminado.get()) {
			dormir(2);
		}
		Sonido s = new Sonido(rutaAudio);
		duracionMs = Math.max(duracionGif, s.getDuracionMs());
		System.out.println("[VIDEO] " + rutaGif + ": " + total + " fotogramas, " + duracionGif + " ms de imagen, "
				+ s.getDuracionMs() + " ms de audio, " + cantidad + " hilo(s)" + (independientes ? " en paralelo" : " en orden"));
		SwingUtilities.invokeLater(() -> arrancar(s));
	}

	private void arrancar(Sonido s) {
		if (terminado.get()) {
			cerrarAudio(s);
			return;
		}
		audio = s;
		enCurso = true;
		audio.play();
		t0Nanos = System.nanoTime();
		// La posicion del Clip se lee en su propio hilo: getMicrosecondPosition() se BLOQUEA mientras el dispositivo
		// de audio arranca (medido: 0.8-1.6 s la primera vez, §1.82). En el EDT eso congelaba la pantalla.
		Thread muestreo = new Thread(() -> {
			while (!terminado.get()) {
				Sonido a = audio;
				long p = a == null ? -1 : a.getPosicionMs();
				muestraNanos = System.nanoTime();
				posicionAudio = p;
				dormir(2);
			}
		}, "video-gif-reloj");
		muestreo.setDaemon(true);
		hilos.add(muestreo);
		muestreo.start();
		timer = new Timer(INTERVALO_MS, e -> {
			long t = reloj();
			if (t >= duracionMs) {
				terminar(true);
				return;
			}
			pedido = indiceEn(t);
			destino.repaint();
		});
		timer.start();
		destino.repaint();
	}

	/**
	 * Reloj del video en ms, sin bloquear nunca (se llama en el EDT):
	 * - mientras el audio no empieza a sonar, 0 (primer fotograma quieto: la imagen espera al sonido);
	 * - mientras suena, su posicion real, extrapolada con el reloj de pared desde la ultima muestra (como mucho
	 *   250 ms, por si una lectura tarda);
	 * - cuando el audio termino (Clip cerrado), el reloj de pared desde la ultima posicion;
	 * - sin audio (archivo ausente o sin dispositivo, o si no empieza en ESPERA_AUDIO_MS), el reloj de pared.
	 */
	private long reloj() {
		long ahora = System.nanoTime();
		long pared = (ahora - t0Nanos) / 1_000_000L;
		long pa = posicionAudio;
		long t;
		if (pa > 0 && pa != posicionAnterior) {
			posicionAnterior = pa;
			paredAvance = pared;
		}
		boolean estancado = pa > 0 && pared - paredAvance > 150;   // el Clip llego al final (o se trabo): no esperar a que cierre
		if (pa > 0 && !estancado) {
			long edad = Math.min(250L, Math.max(0L, (ahora - muestraNanos) / 1_000_000L));
			if (arranqueAudioMs < 0) {
				arranqueAudioMs = pared - pa - edad;   // lo que tardo el dispositivo en empezar a sonar
			}
			ultimoAudio = pa + edad;
			paredUltimoAudio = pared;
			t = ultimoAudio;
		} else if (ultimoAudio > 0) {
			t = ultimoAudio + (pared - paredUltimoAudio);   // el Clip ya cerro: se sigue con el reloj de pared
		} else if (pa != -1 && pared < ESPERA_AUDIO_MS) {
			t = 0;                                         // el audio todavia no suena: primer fotograma quieto
		} else {
			if (!sinAudio) {
				sinAudio = true;
				inicioSinAudio = pa == -1 && pared < ESPERA_AUDIO_MS ? pared : ESPERA_AUDIO_MS;
				System.out.println("[VIDEO] el audio no avanza: la imagen sigue con el reloj de pared");
			}
			t = pared - inicioSinAudio;                    // sin audio: la imagen sigue sola desde el primer fotograma
		}
		t = Math.max(t, ultimoT);
		if (ultimoT > 0 || t > 0) {
			maxSaltoReloj = Math.max(maxSaltoReloj, t - ultimoT);
		}
		if (ultimoTick >= 0) {
			tickMaxMs = Math.max(tickMaxMs, pared - ultimoTick);
		}
		ultimoTick = pared;
		ultimoT = t;
		return t;
	}

	private int indiceEn(long t) {
		int lo = 0, hi = total - 1;
		while (lo < hi) {
			int mid = (lo + hi + 1) >>> 1;
			if (inicioMs[mid] <= t) {
				lo = mid;
			} else {
				hi = mid - 1;
			}
		}
		return lo;
	}

	private void decodificarEnParalelo(DecodificadorGif lector) {
		try {
			while (!terminado.get()) {
				int base = pedido;
				soltarAnteriores(base);
				int objetivo = -1;
				for (int f = base; f < Math.min(total, base + POR_DELANTE); f++) {
					if (!cache.containsKey(f) && enProceso.putIfAbsent(f, Boolean.TRUE) == null) {
						objetivo = f;
						break;
					}
				}
				if (objetivo < 0) {
					dormir(3);
					continue;
				}
				try {
					BufferedImage img = lector.fotograma(objetivo);
					if (!terminado.get() && objetivo >= pedido - 1) {
						cache.put(objetivo, img);
					}
				} catch (IOException e) {
					System.out.println("[VIDEO] fotograma " + objetivo + " ilegible: " + e);
					cache.put(objetivo, new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
				} finally {
					enProceso.remove(objetivo);
				}
			}
		} finally {
			lector.cerrar();
		}
	}

	private void decodificarEnOrden(DecodificadorGif lector) {
		try {
			int siguiente = 0;
			while (!terminado.get() && siguiente < total) {
				if (siguiente > pedido + POR_DELANTE) {
					dormir(3);
					continue;
				}
				BufferedImage img = lector.siguiente();
				if (img == null) {
					break;
				}
				if (siguiente >= pedido - 1) {
					cache.put(siguiente, img);
				}
				siguiente++;
				soltarAnteriores(pedido);
			}
		} catch (IOException e) {
			System.out.println("[VIDEO] error al decodificar el GIF: " + e);
		} finally {
			lector.cerrar();
		}
	}

	private void soltarAnteriores(int base) {
		for (Integer k : cache.keySet()) {
			if (k < base - 1) {
				cache.remove(k);
			}
		}
	}

	/** Dibuja el fotograma que toca ahora, a pantalla completa. Llamar desde paintComponent. */
	public void pintar(Graphics2D g, int w, int h) {
		long inicioPintado = System.nanoTime();
		try {
			pintarFotograma(g, w, h);
		} finally {
			long d = System.nanoTime() - inicioPintado;
			pintadoVeces++;
			pintadoSumaNanos += d;
			pintadoMaxNanos = Math.max(pintadoMaxNanos, d);
		}
	}

	private void pintarFotograma(Graphics2D g, int w, int h) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, w, h);
		if (!enCurso) {
			return;
		}
		int esperado = indiceEn(ultimoT);
		BufferedImage img = null;
		int mostrado = esperado;
		for (; mostrado >= Math.max(0, esperado - POR_DELANTE) && img == null; mostrado--) {
			img = cache.get(mostrado);
		}
		mostrado++;
		if (img == null || img.getWidth() <= 1) {
			return;
		}
		if (img.getWidth() != w || img.getHeight() != h) {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		}
		g.drawImage(img, 0, 0, w, h, null);
		mostradosSet.set(mostrado);
		if (mostrado != ultimoPintado) {
			pintados++;
			ultimoPintado = mostrado;
		}
		long retraso = inicioMs[esperado] - inicioMs[mostrado];
		if (retraso > 0) {
			pintadosConRetraso++;
			maxRetrasoMs = Math.max(maxRetrasoMs, retraso);
		}
	}

	public boolean isEnCurso() {
		return enCurso;
	}

	/** Corta el video y libera todo; con entregarFin=true llama a alTerminar (una sola vez en total). */
	public void terminar(boolean entregarFin) {
		if (!terminado.compareAndSet(false, true)) {
			return;
		}
		enCurso = false;
		if (timer != null) {
			timer.stop();
		}
		hilos.forEach(Thread::interrupt);
		Sonido s = audio;
		audio = null;
		cerrarAudio(s);
		cache.clear();
		destino.repaint();
		System.out.println("[VIDEO] terminado: " + pintados + " fotogramas distintos mostrados de " + total
				+ ", retraso maximo de imagen " + maxRetrasoMs + " ms, el audio empezo a sonar a los " + arranqueAudioMs + " ms");
		if (entregarFin && alTerminar != null) {
			entregado = true;
			alTerminar.run();
		}
	}

	private static void cerrarAudio(Sonido s) {
		if (s == null) {
			return;
		}
		// Clip.stop() de Java 8 puede bloquear ~2 s al final del clip (§1.59): se cierra fuera del EDT.
		Thread cierre = new Thread(s::cerrar, "video-gif-audio");
		cierre.setDaemon(true);
		cierre.start();
	}

	private static void dormir(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	// --- consultas para las pruebas ---
	public int getTotalFotogramas() {
		return total;
	}

	public int getFotogramasMostrados() {
		return pintados;
	}

	public int getPintadosConRetraso() {
		return pintadosConRetraso;
	}

	public long getMaxRetrasoMs() {
		return maxRetrasoMs;
	}

	public long getArranqueAudioMs() {
		return arranqueAudioMs;
	}

	/** Mayor avance del reloj entre dos ticks (ms) y mayor intervalo real entre ticks (ms). */
	public long[] getSaltosReloj() {
		return new long[] { maxSaltoReloj, tickMaxMs };
	}

	/** Rangos de fotogramas que nunca se pintaron, p. ej. "3-5 40". */
	public String getNoMostrados() {
		StringBuilder sb = new StringBuilder();
		int i = mostradosSet.nextClearBit(0);
		while (i < total) {
			int fin = Math.min(total, mostradosSet.nextSetBit(i) < 0 ? total : mostradosSet.nextSetBit(i)) - 1;
			sb.append(i == fin ? String.valueOf(i) : i + "-" + fin).append(' ');
			i = mostradosSet.nextClearBit(fin + 1);
		}
		return sb.toString().trim();
	}

	/** Tiempo de pintado de un fotograma: {medio, maximo} en ms. */
	public double[] getPintadoMs() {
		return new double[] { pintadoVeces == 0 ? 0 : pintadoSumaNanos / 1e6 / pintadoVeces, pintadoMaxNanos / 1e6 };
	}

	public int getUltimoFotogramaMostrado() {
		return ultimoPintado;
	}

	public boolean isFinEntregado() {
		return entregado;
	}

	public boolean hilosVivos() {
		for (Thread t : hilos) {
			if (t.isAlive()) {
				return true;
			}
		}
		return false;
	}
}
