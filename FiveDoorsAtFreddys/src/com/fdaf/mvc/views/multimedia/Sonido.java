package com.fdaf.mvc.views.multimedia;

import java.net.URL;

import javax.sound.sampled.*;
import javax.swing.Timer;

import com.fdaf.util.ConfiguracionAudio;

public class Sonido {

	// CAUSA RAIZ REAL de un segundo OutOfMemoryError confirmado 2026-08-06 (stack trace real
	// del usuario: OOM dentro de DirectAudioDevice$DirectClip.open() al crear el Sonido de
	// LesToreadorsRemix.wav, justo al iniciar el glitch de la transicion Noche5->Escape).
	// Investigado sin asumir que era el mismo leak que el fix de Clip.close() de arriba (ese ya
	// estaba corregido) -- causa real DISTINTA: Clip.open() SIEMPRE decodifica el audio COMPLETO
	// a un buffer en memoria antes de poder reproducirlo (a diferencia de SourceDataLine, que
	// solo bufferea unos pocos KB a la vez) -- LesToreadorsRemix.wav mide ~50.8MB de PCM crudo
	// (5 minutos estéreo 44.1kHz/16-bit, confirmado con AudioInputStream.getFrameLength()*
	// getFrameSize()), y esa asignación de memoria de una sola vez fallaba justo en el momento
	// en que se crea (heap maximo real 247MB, 168MB ya en uso por los assets de la transicion
	// de Noche 5 en el stack trace del usuario). El siguiente archivo mas grande del proyecto
	// (phoneguy_ingles.wav) pesa 9.9MB -- UMBRAL_STREAMING_BYTES deja margen amplio para que
	// NINGUN otro Sonido existente (+30 sitios de uso) cambie de comportamiento; solo un
	// archivo genuinamente enorme como este pasa al camino de streaming de abajo.
	private static final long UMBRAL_STREAMING_BYTES = 15_000_000L;

	private Clip clip;

	// CAUSA RAIZ REAL de un OutOfMemoryError confirmado con profiling real 2026-08-06
	// (jmap -histo:live sobre una sesion larga simulada: 835 instancias VIVAS de
	// com.sun.media.sound.DirectAudioDevice$DirectClip, 243MB en arrays de bytes -- los
	// buffers de audio PCM decodificados de cada Clip nunca liberado). Un Clip abierto queda
	// registrado en el mixer nativo (DirectAudioDevice) indefinidamente sin importar si el
	// objeto Java Sonido en si se volvio inalcanzable -- clip.close() es obligatorio, no
	// opcional, para que el recurso nativo se libere de verdad (ya documentado como deuda
	// tecnica conocida en CLAUDE.md #1.8 antes de esta sesion, sin crash confirmado hasta
	// ahora). reiniciando evita que el clip se cierre cuando play() se llama de nuevo sobre la
	// MISMA instancia para reiniciarla (unos pocos sonidos reutilizables del proyecto, como
	// los de luces/llamada/ambiente de oficina) -- la inmensa mayoria de los sonidos del
	// proyecto son de un solo uso (new Sonido(ruta).play(), nunca se llama stop() ni play() de
	// nuevo sobre esa misma instancia), asi que el LineListener es lo unico que puede cerrarlos
	// -- nunca nadie mas tiene la referencia para hacerlo explicitamente.
	private volatile boolean reiniciando = false;

	// CAUSA RAIZ REAL de "la respiracion agitada deja de sonar despues de algunos jumpscares"
	// (investigado 2026-08-09): 'reiniciando' solo protege el reinicio SINCRONO que hace play()
	// sobre un clip que YA esta sonando -- no cubre el caso real de 'respiracion', que se crea
	// UNA VEZ (campo de instancia) y se vuelve a llamar play() sobre la MISMA instancia varias
	// veces a lo largo de la partida, cada vez DESPUES de que el clip ya termino de sonar solo
	// (STOP natural de fin de medio, no un stop() nuestro). Ese STOP natural, con reiniciando en
	// false, disparaba el cierre del Clip vía el LineListener de abajo -- el PRIMER jumpscare
	// sobrevivido cerraba el clip para siempre, y cada play() posterior entraba en el guard
	// "!clip.isOpen() -> return" de forma silenciosa (sin excepcion, sin log). 'reutilizable'
	// marca explícitamente las pocas instancias (como 'respiracion') que el propio llamador
	// vuelve a reproducir despues de que terminen solas -- el LineListener nunca las cierra por
	// su cuenta; el llamador debe cerrarlas explícitamente con cerrar() cuando de verdad terminó
	// de usarlas (fin de partida), igual que ya se hace con detenerSonidosJuego().
	private final boolean reutilizable;

	// --- Camino de streaming (solo para archivos por encima de UMBRAL_STREAMING_BYTES) ---
	// SourceDataLine.open() solo reserva un buffer interno pequeño (unos pocos KB), nunca el
	// archivo completo -- por eso resuelve el OOM de raiz para audio grande. lineaStreaming
	// != null es la señal de que esta instancia usa este camino en vez de Clip.
	private URL urlStreaming;
	private AudioFormat formatoStreaming;
	private SourceDataLine lineaStreaming;
	private Thread hiloStreaming;
	private volatile boolean detenerHiloStreaming = false;

	public Sonido(String archivo) {
		this(archivo, false);
	}

	// reutilizable=true: el LineListener nunca cierra el Clip por su cuenta al llegar a un STOP
	// natural -- el llamador es responsable de invocar cerrar() cuando de verdad terminó de
	// reutilizar esta instancia (ver comentario de 'reutilizable' arriba).
	public Sonido(String archivo, boolean reutilizable) {
		this.reutilizable = reutilizable;

		try {

			URL url = getClass().getResource("/sounds/" + archivo);

			if (url == null) {
				return;
			}

			AudioInputStream sondeo = AudioSystem.getAudioInputStream(url);
			AudioFormat formato = sondeo.getFormat();
			long bytesPcmEstimados = sondeo.getFrameLength() * (long) formato.getFrameSize();
			sondeo.close();

			if (bytesPcmEstimados > UMBRAL_STREAMING_BYTES) {
				urlStreaming = url;
				formatoStreaming = formato;
				DataLine.Info info = new DataLine.Info(SourceDataLine.class, formato);
				lineaStreaming = (SourceDataLine) AudioSystem.getLine(info);
				lineaStreaming.open(formato);
			} else {
				AudioInputStream audio = AudioSystem.getAudioInputStream(url);

				clip = AudioSystem.getClip();
				clip.open(audio);

				clip.addLineListener(evento -> {
					if (evento.getType() == LineEvent.Type.STOP && !reiniciando && !this.reutilizable && clip.isOpen()) {
						clip.close();
					}
				});
			}

			// Aplica el volumen global vigente apenas se abre el Clip/la línea de streaming --
			// cubre automáticamente los +30 puntos del proyecto donde ya
			// se crea un Sonido, sin que ninguno de ellos necesite cambiar.
			ConfiguracionAudio.aplicarVolumenInicial(this);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean esStreaming() {
		return lineaStreaming != null;
	}

	public void play() {
		if (esStreaming()) {
			reproducirStreaming();
			return;
		}
		if (clip == null || !clip.isOpen()) return;
		// Reinicio interno -- NUNCA pasa por stop() (que dispara el cierre real via el
		// LineListener de arriba) porque play() puede llamarse varias veces sobre la MISMA
		// instancia para reiniciar un sonido reutilizable ya en curso.
		reiniciando = true;
		clip.stop();
		clip.setFramePosition(0);
		ConfiguracionAudio.desregistrarLoop(this);
		reiniciando = false;
		clip.start();
	}

	// Reabre la linea si stop() la habia cerrado, corta cualquier hilo de reproduccion anterior
	// en curso (mismo espiritu que el reinicio de Clip.play() de arriba, aunque en la practica
	// ningun archivo de streaming del proyecto vuelve a llamar play() dos veces) y arranca un
	// hilo daemon propio que lee el archivo en bloques pequeños (8KB) y los escribe a la linea
	// -- nunca decodifica el audio completo a memoria de una sola vez.
	private void reproducirStreaming() {
		detenerHiloStreaming = true;
		if (hiloStreaming != null) {
			try {
				hiloStreaming.join(500);
			} catch (InterruptedException ignorado) {
				Thread.currentThread().interrupt();
			}
		}
		detenerHiloStreaming = false;

		try {
			if (!lineaStreaming.isOpen()) {
				lineaStreaming.open(formatoStreaming);
			}
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			return;
		}

		hiloStreaming = new Thread(() -> {
			try (AudioInputStream audio = AudioSystem.getAudioInputStream(urlStreaming)) {
				lineaStreaming.start();
				byte[] buffer = new byte[8192];
				int leidos;
				while (!detenerHiloStreaming && (leidos = audio.read(buffer)) != -1) {
					lineaStreaming.write(buffer, 0, leidos);
				}
				if (!detenerHiloStreaming) {
					lineaStreaming.drain();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, "Sonido-Streaming");
		hiloStreaming.setDaemon(true);
		hiloStreaming.start();
	}

	public void stop() {
		if (esStreaming()) {
			detenerHiloStreaming = true;
			if (lineaStreaming != null && lineaStreaming.isOpen()) {
				lineaStreaming.stop();
				lineaStreaming.flush();
				lineaStreaming.close();
			}
			ConfiguracionAudio.desregistrarLoop(this);
			return;
		}
		if (clip == null) return;
		clip.stop();
		clip.setFramePosition(0);
		ConfiguracionAudio.desregistrarLoop(this);
	}

	// Cierre explícito y definitivo -- necesario para instancias 'reutilizable' (ver comentario
	// arriba), que el LineListener nunca cierra por su cuenta. Llamar solo cuando de verdad no se
	// va a volver a reproducir esta instancia (p. ej. detenerSonidosJuego(), fin de partida).
	public void cerrar() {
		stop();
		if (!esStreaming() && clip != null && clip.isOpen()) {
			clip.close();
		}
	}

	public void loop() {
		// El streaming no soporta loop -- ningun archivo del proyecto por encima de
		// UMBRAL_STREAMING_BYTES lo necesita hoy (LesToreadorsRemix se reproduce una sola vez).
		if (esStreaming() || clip == null || !clip.isOpen()) return;
		clip.loop(Clip.LOOP_CONTINUOUSLY);
		ConfiguracionAudio.registrarLoop(this);
	}

	public long getDuracionMs() {
		if (esStreaming()) {
			if (formatoStreaming == null || formatoStreaming.getFrameRate() <= 0) return 0;
			return 0; // no usado hoy por ningun archivo de streaming; sin dato de longitud barato.
		}
		if (clip == null || !clip.isOpen()) return 0;
		return clip.getMicrosecondLength() / 1000;
	}

	public void setVolumen(float db) {
		FloatControl volumen = obtenerControlVolumen();
		if (volumen == null) return;
		try {
			volumen.setValue(db);
		} catch (Exception e) {
			// El clip/la linea se cerró justo entre el chequeo y este punto -- se ignora,
			// mismo criterio defensivo que subirVolumen/aplicarNivelGlobal.
		}
	}

	public void subirVolumen(float incrementoDb) {
		FloatControl volumen = obtenerControlVolumen();
		if (volumen == null) return;
		try {
			float nuevo = volumen.getValue() + incrementoDb;
			float maximo = volumen.getMaximum();
			float minimo = volumen.getMinimum();
			volumen.setValue(Math.max(minimo, Math.min(nuevo, maximo)));
		} catch (Exception e) {
			// Esta línea de audio no soporta control de volumen; se ignora.
		}
	}

	public void fadeOut(int duracionMs) {
		final FloatControl volumen = obtenerControlVolumen();
		if (volumen == null) return;

		final float inicio = volumen.getValue();
		final float minimo = volumen.getMinimum();

		final int pasoMs = 50;
		final int pasos = Math.max(1, duracionMs / pasoMs);
		final float decremento = (inicio - minimo) / pasos;
		final int[] contador = {0};

		Timer fade = new Timer(pasoMs, null);
		fade.addActionListener(e -> {
			try {
				contador[0]++;
				float nuevoValor = inicio - (decremento * contador[0]);
				if (nuevoValor <= minimo || contador[0] >= pasos) {
					volumen.setValue(minimo);
					((Timer) e.getSource()).stop();
				} else {
					volumen.setValue(nuevoValor);
				}
			} catch (Exception ex) {
				// El clip/la linea se cerro (fin natural o stop() externo) a mitad del fade --
				// ya no hay nada que atenuar, se detiene el timer sin propagar la excepcion.
				((Timer) e.getSource()).stop();
			}
		});
		fade.start();
	}

	// Aplica el nivel global (0-10) al Clip/línea real de esta instancia,
	// calculando el dB según el mínimo técnico REAL de este canal
	// específico (no un valor asumido). Público porque ConfiguracionAudio
	// lo invoca desde fuera, tanto al construir como al actualizar en vivo.
	public void aplicarNivelGlobal(int nivel) {
		FloatControl volumen = obtenerControlVolumen();
		if (volumen == null) return;
		try {
			float minimo = volumen.getMinimum();
			float db = ConfiguracionAudio.calcularDb(minimo, nivel);
			volumen.setValue(db);
		} catch (Exception e) {
			// Esta línea de audio no soporta control de volumen; se ignora.
		}
	}

	// Único punto de acceso al control de volumen, sea Clip o SourceDataLine -- evita repetir
	// la rama esStreaming()/clip en cada método de volumen de arriba.
	private FloatControl obtenerControlVolumen() {
		try {
			if (esStreaming()) {
				if (lineaStreaming == null || !lineaStreaming.isOpen()) return null;
				return (FloatControl) lineaStreaming.getControl(FloatControl.Type.MASTER_GAIN);
			}
			if (clip != null) {
				return (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			}
		} catch (Exception e) {
			// Esta línea de audio no soporta control de volumen; se ignora.
		}
		return null;
	}

}
