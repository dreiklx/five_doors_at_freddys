package com.fdaf.mvc.models.juego;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;


public class Sonido {
	
	private Clip clip;
	
	
	
	public Sonido(AudioInputStream so) {
		
		AudioInputStream audio = so;
		
		try {
			clip = AudioSystem.getClip();
			clip.open(audio);
		} catch (LineUnavailableException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	public void play() {
		if (clip == null) return;
		stop();
		clip.start();
	}
	
	public void stop() {
		clip.stop();
		clip.setFramePosition(0);
	}

}
