package com.fdaf.mvc.views.Jframe.pnl;

import javax.swing.JPanel;

import com.fdaf.util.CargarImagenes;

import java.awt.FlowLayout;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JProgressBar;

public class PnlTableta extends JPanel {
	private JLabel lblTabletCerrar;
	private JProgressBar pbarRendirse;

	/**
	 * Create the panel.
	 */
	public PnlTableta() {
		setLayout(null);
		
		JPanel panel = new PnlAplicarImagen(CargarImagenes.fondoCamara);
		panel.setBounds(0, 0, 1000, 600);
		add(panel);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(CargarImagenes.puntoCamara);
		lblNewLabel.setBounds(49, 50, 60, 61);
		panel.add(lblNewLabel);
		
		lblTabletCerrar = new JLabel("");
		lblTabletCerrar.setIcon(CargarImagenes.barraTableta);
		lblTabletCerrar.setBounds(204, 534, 375, 42);
		panel.add(lblTabletCerrar);
		
		pbarRendirse = new JProgressBar();
		pbarRendirse.setBounds(741, 50, 223, 23);
		panel.add(pbarRendirse);

	}

	public JLabel getLblTabletCerrar() {
		return lblTabletCerrar;
	}

	public void setLblTabletCerrar(JLabel lblTabletCerrar) {
		this.lblTabletCerrar = lblTabletCerrar;
	}

	public JProgressBar getPbarRendirse() {
		return pbarRendirse;
	}

	public void setPbarRendirse(JProgressBar pbarRendirse) {
		this.pbarRendirse = pbarRendirse;
	}
}
