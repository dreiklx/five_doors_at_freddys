package com.fdaf.util;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class CargarSonido {
	
	public AudioInputStream butonMenu() {
		try {
			return AudioSystem.getAudioInputStream(getClass().getResource("/resources/sounds/putdown.wav"));
		} catch (UnsupportedAudioFileException e) {
			System.out.println(e);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
}
