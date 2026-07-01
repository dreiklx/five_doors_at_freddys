package com.fdaf.mvc.views.multimedia;

import java.net.URL;

import javax.sound.sampled.*;

public class Sonido {

	private Clip clip;

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

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	public void play() {
		if (clip == null) return;
		stop();
		clip.start();
	}

	public void stop() {
		if (clip == null) return;
		clip.stop();
		clip.setFramePosition(0);
	}

	public void loop() {
		if (clip == null) return;
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}

	// Duración exacta del clip en milisegundos. Se usa para encadenar
	// sonidos sin superponerlos (ej. contestar_telefono.wav -> llamada),
	public long getDuracionMs() {
		if (clip == null) return 0;
		return clip.getMicrosecondLength() / 1000;
	}

	public void setVolumen(float db) {
	    if (clip == null) return;

	    FloatControl volumen =
	        (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

	    volumen.setValue(db);
	}

}