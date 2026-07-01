package com.fdaf.mvc.views.frames.pnl;

import javax.swing.JPanel;

import com.fdaf.mvc.models.coleccionables.TipoColeccionable;
import com.fdaf.mvc.views.frames.VistaPrincipal;
import com.fdaf.util.CargarImagenes;
import com.fdaf.util.EscalarVista;

import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import java.awt.Dimension;


public class PnlTableta extends JPanel {
	private JLabel lblTabletCerrar;
	private JProgressBar pbarRendirse;

	// pnlColeccionables ahora contiene 10 slots FIJOS, creados una
	// sola vez, en el orden canónico de TipoColeccionable. Empiezan todos
	// bloqueados (el ícono real se asigna en EscalarVista.adaptarTablet,
	// igual que bateria/powerLeft en PnlJuego). ControllerInterfaz solo
	// intercambia el ícono del slot correspondiente al encontrar uno; ya
	// no se agrega nada al final.
	private JPanel pnlColeccionables;
	private JLabel[] lblColeccionables;
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
		
		lblTabletCerrar = new JLabel("");
		lblTabletCerrar.setBounds(288, 808, 800, 55);
		lblTabletCerrar.setPreferredSize(new Dimension(690, 77));
		lblTabletCerrar.setIcon(CargarImagenes.barraTableta);
		
		pbarRendirse = new JProgressBar();
		pbarRendirse.setBounds(1291, 50, 254, 33);

		pnlColeccionables = new JPanel();
		pnlColeccionables.setBounds(43, 132, 1516, 365);
		pnlColeccionables.setOpaque(false);
		pnlColeccionables.setLayout(new GridLayout(2, 5, 15, 15));

		TipoColeccionable[] catalogo = TipoColeccionable.values();
		lblColeccionables = new JLabel[catalogo.length];

		for (int i = 0; i < catalogo.length; i++) {
			JLabel slot = new JLabel("");
			slot.setOpaque(false);
			lblColeccionables[i] = slot;
			pnlColeccionables.add(slot);
		}

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

	public JPanel getPnlColeccionables() {
		return pnlColeccionables;
	}

	public void setPnlColeccionables(JPanel pnlColeccionables) {
		this.pnlColeccionables = pnlColeccionables;
	}

	// CAMBIO: acceso a los 10 slots fijos, en orden canónico.
	public JLabel[] getLblColeccionables() {
		return lblColeccionables;
	}

	public void setLblColeccionables(JLabel[] lblColeccionables) {
		this.lblColeccionables = lblColeccionables;
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