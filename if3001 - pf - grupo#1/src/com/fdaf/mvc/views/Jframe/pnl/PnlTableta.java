package com.fdaf.mvc.views.Jframe.pnl;

import javax.swing.JPanel;

import com.fdaf.mvc.views.Jframe.VistaPrincipal;
import com.fdaf.util.CargarImagenes;
import com.fdaf.util.EscalarVista;

import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import java.awt.Dimension;


public class PnlTableta extends JPanel {
	private JLabel lblTabletCerrar;
	private JProgressBar pbarRendirse;

	// CAMBIO FDAF - contenedor visual de coleccionables encontrados.
	// ControllerCamara le agrega un JLabel con el icono del coleccionable
	// cada vez que el jugador hace clic sobre uno revelado.
	private JPanel pnlColeccionables;
	private JPanel panel;
	private JLabel lblNewLabel;

	/**
	 * Create the panel.
	 */
	public PnlTableta() {
		setPreferredSize(new Dimension(1600, 900));
		setLayout(null);
		
		panel = new PnlAplicarImagen(CargarImagenes.estatica);
		panel.setOpaque(false);
		panel.setBounds(0, 0, 1600, 900);
		add(panel);
		
		lblNewLabel = new JLabel("");
		lblNewLabel.setBounds(74, 50, 80, 70);
		//lblNewLabel.setIcon(CargarImagenes.puntoCamara);
		
		lblTabletCerrar = new JLabel("");
		lblTabletCerrar.setBounds(288, 808, 800, 55);
		lblTabletCerrar.setPreferredSize(new Dimension(690, 77));
		lblTabletCerrar.setIcon(CargarImagenes.barraTableta);
		
		pbarRendirse = new JProgressBar();
		pbarRendirse.setBounds(1291, 50, 254, 33);

		// CAMBIO FDAF - contenedor de coleccionables encontrados, ubicado
		// en el espacio libre central del fondo de la tablet.
		pnlColeccionables = new JPanel();
		pnlColeccionables.setBounds(43, 132, 1516, 365);
		pnlColeccionables.setOpaque(false);
		pnlColeccionables.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15));
		panel.setLayout(null);
		panel.add(lblNewLabel);
		panel.add(pbarRendirse);
		panel.add(pnlColeccionables);
		panel.add(lblTabletCerrar);

	}
	public void init(VistaPrincipal vp,PnlTableta tablet) {
		EscalarVista.adaptarTablet(vp, tablet);
		this.revalidate();
		this.repaint();
	}

	public JPanel getPanel() {
		return panel;
	}

	public void setPanel(JPanel panel) {
		this.panel = panel;
	}

	public JLabel getLblNewLabel() {
		return lblNewLabel;
	}

	public void setLblNewLabel(JLabel lblNewLabel) {
		this.lblNewLabel = lblNewLabel;
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