package com.fdaf.mvc.views.multimedia;

import java.net.URL;

import javax.sound.sampled.*;
import javax.swing.Timer;

import com.fdaf.util.ConfiguracionAudio;

public class Sonido {

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

	public Sonido(String archivo) {

	    try {

	        URL url = getClass().getResource("/sounds/" + archivo);


	        if (url == null) {
	            return;
	        }

	        AudioInputStream audio =
	                AudioSystem.getAudioInputStream(url);

	        clip = AudioSystem.getClip();
	        clip.open(audio);

	        clip.addLineListener(evento -> {
	            if (evento.getType() == LineEvent.Type.STOP && !reiniciando && clip.isOpen()) {
	                clip.close();
	            }
	        });

	        // Aplica el volumen global vigente apenas se abre el Clip --
	        // cubre automáticamente los +30 puntos del proyecto donde ya
	        // se crea un Sonido, sin que ninguno de ellos necesite cambiar.
	        ConfiguracionAudio.aplicarVolumenInicial(this);

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	public void play() {
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

	public void stop() {
		if (clip == null) return;
		clip.stop();
		clip.setFramePosition(0);
		ConfiguracionAudio.desregistrarLoop(this);
	}

	public void loop() {
		if (clip == null || !clip.isOpen()) return;
		clip.loop(Clip.LOOP_CONTINUOUSLY);
		ConfiguracionAudio.registrarLoop(this);
	}

	public long getDuracionMs() {
		if (clip == null || !clip.isOpen()) return 0;
		return clip.getMicrosecondLength() / 1000;
	}

	public void setVolumen(float db) {
	    if (clip == null || !clip.isOpen()) return;
	    try {
	        FloatControl volumen =
	            (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
	        volumen.setValue(db);
	    } catch (Exception e) {
	        // El clip se cerro (auto-cierre real via LineListener, ver el campo reiniciando
	        // arriba) justo entre el chequeo de isOpen() y este punto -- se ignora, mismo
	        // criterio defensivo que subirVolumen/aplicarNivelGlobal.
	    }
	}

	public void subirVolumen(float incrementoDb) {
		if (clip == null) return;
		try {
			FloatControl volumen = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			float nuevo = volumen.getValue() + incrementoDb;
			float maximo = volumen.getMaximum();
			float minimo = volumen.getMinimum();
			volumen.setValue(Math.max(minimo, Math.min(nuevo, maximo)));
		} catch (Exception e) {
			// Esta línea de audio no soporta control de volumen; se ignora.
		}
	}

	public void fadeOut(int duracionMs) {
		if (clip == null) return;

		final FloatControl volumen;
		try {
			volumen = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		} catch (Exception e) {
			return;
		}

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
				// El clip se cerro (fin natural o stop() externo) a mitad del fade -- ya no
				// hay nada que atenuar, se detiene el timer sin propagar la excepcion.
				((Timer) e.getSource()).stop();
			}
		});
		fade.start();
	}

	// Aplica el nivel global (0-10) al Clip real de esta instancia,
	// calculando el dB según el mínimo técnico REAL de este canal
	// específico (no un valor asumido). Público porque ConfiguracionAudio
	// lo invoca desde fuera, tanto al construir como al actualizar en vivo.
	public void aplicarNivelGlobal(int nivel) {
		if (clip == null) return;
		try {
			FloatControl volumen = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			float minimo = volumen.getMinimum();
			float db = ConfiguracionAudio.calcularDb(minimo, nivel);
			volumen.setValue(db);
		} catch (Exception e) {
			// Esta línea de audio no soporta control de volumen; se ignora.
		}
	}

}