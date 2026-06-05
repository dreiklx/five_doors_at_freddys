package com.fdaf.mvc.views.multimedia;

import javax.sound.sampled.*;

public class Sonido {

	private Clip clip;

	public Sonido(String archivo) {
		try {
			AudioInputStream audio = AudioSystem.getAudioInputStream(
			        getClass().getResource("/com/fdaf/resources/sounds/" + archivo)
			);
			clip = AudioSystem.getClip();
			clip.open(audio);

		} catch (Exception e) {}
	}

	public void play() {
		stop();
		clip.start();
	}

	public void stop() {
		clip.stop();
		clip.setFramePosition(0);
	}

	public void loop() {
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}

}