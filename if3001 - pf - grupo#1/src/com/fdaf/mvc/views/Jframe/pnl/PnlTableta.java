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

	// CAMBIO FDAF - contenedor visual de coleccionables encontrados.
	// ControllerCamara le agrega un JLabel con el icono del coleccionable
	// cada vez que el jugador hace clic sobre uno revelado.
	private JPanel pnlColeccionables;

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

		// CAMBIO FDAF - contenedor de coleccionables encontrados, ubicado
		// en el espacio libre central del fondo de la tablet.
		pnlColeccionables = new JPanel();
		pnlColeccionables.setOpaque(false);
		pnlColeccionables.setBounds(49, 140, 900, 380);
		pnlColeccionables.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15));
		panel.add(pnlColeccionables);

	}

	// CAMBIO FDAF - getter/setter del contenedor de coleccionables
	public JPanel getPnlColeccionables() {
		return pnlColeccionables;
	}

	public void setPnlColeccionables(JPanel pnlColeccionables) {
		this.pnlColeccionables = pnlColeccionables;
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