package com.fdaf.mvc.views.animaciones;

import javax.swing.JLabel;
import javax.swing.Timer;

public class AnimacionPuerta {

	private Timer timer;
	private JLabel label;
	private int pasos;

	public AnimacionPuerta(JLabel label) {
		this.label = label;
		this.pasos = 0;
	}

	public void abrir() {
		if (timer != null && timer.isRunning()) {
			return;
		}
		pasos = 0;
		timer = new Timer(10, e -> {
			int x = label.getX();
			x -= 5;
			label.setLocation(x, label.getY());
			pasos++;
			if (pasos >= 30) {
				timer.stop();
			}
		});
		timer.start();
	}

	public void detener() {
		if (timer != null && timer.isRunning()) {
			timer.stop();
			pasos = 0;
		}
	}

}