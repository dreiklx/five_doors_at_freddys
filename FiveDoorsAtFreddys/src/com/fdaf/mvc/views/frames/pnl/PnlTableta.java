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
import javax.swing.JTextField;
import javax.swing.SwingConstants;


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
	private JPanel panel;
	private JLabel lblPuntoCam;
	private JLabel CUPCAKE;
	private JLabel GUITARRA_BONNIE;
	private JLabel MICROFONO_FREDDY;
	private JLabel PELUCHE_FREDDY;
	private JLabel PELUCHE_FOXY;
	private JLabel PELUCHE_CHICA;
	private JLabel PELUCHE_BONNIE;
	private JLabel GARFIO_FOXY;
	private JLabel PELUCHE_GOLDEN_FREDDY;
	private JLabel BALLOONBOY;
	private JLabel lblMostrar;
	private JLabel lblCopyPowerLeft;
	private JLabel lblCopyBateria;
	private JLabel lblDescripcion;
	/**
	 * Create the panel.
	 */
	public PnlTableta() {
		setPreferredSize(new Dimension(1600, 900));
		setLayout(null);
		
		panel = new PnlAplicarImagen(CargarImagenes.camara);
		panel.setOpaque(false);
		panel.setBounds(0, 0, 1600, 900);
		add(panel);
		
		lblPuntoCam = new JLabel("");
		lblPuntoCam.setBounds(80,72,55,55);
		
		lblTabletCerrar = new JLabel("");
		lblTabletCerrar.setBounds(465, 808, 800, 55);
		lblTabletCerrar.setPreferredSize(new Dimension(690, 77));
		lblTabletCerrar.setIcon(CargarImagenes.barraTableta);
		
		pbarRendirse = new JProgressBar();
		pbarRendirse.setOpaque(false);
	
		pbarRendirse.setBounds(1319,824,236,31);

		pnlColeccionables = new PnlAplicarImagen(CargarImagenes.inventario);
		pnlColeccionables.setBounds(0, 0, 1600, 900);
		pnlColeccionables.setOpaque(false);


		panel.setLayout(null);
		panel.add(lblPuntoCam);
		panel.add(pbarRendirse);
		panel.add(pnlColeccionables);
		pnlColeccionables.setLayout(null);
	
		/*
		 * INVENTARIO
		 */
		
		//PRIMERA LINEA 
		CUPCAKE = new JLabel("");
		CUPCAKE.setBounds(69, 504, 74, 68);
		pnlColeccionables.add(CUPCAKE);
		
		GUITARRA_BONNIE = new JLabel("");
		GUITARRA_BONNIE.setBounds(143, 504, 74, 68);
		pnlColeccionables.add(GUITARRA_BONNIE);
		
		MICROFONO_FREDDY = new JLabel("");
		MICROFONO_FREDDY.setBounds(217, 504, 74, 68);
		pnlColeccionables.add(MICROFONO_FREDDY);
		
		GARFIO_FOXY = new JLabel("");
		GARFIO_FOXY.setBounds(291, 504, 74, 68);
		pnlColeccionables.add(GARFIO_FOXY);
		
		
		//SEGUNDA LINEA
		PELUCHE_FREDDY = new JLabel("");
		PELUCHE_FREDDY.setBounds(69, 590, 74, 68);
		pnlColeccionables.add(PELUCHE_FREDDY);
		
		PELUCHE_FOXY = new JLabel("");
		PELUCHE_FOXY.setBounds(143, 590,  74, 68);
		pnlColeccionables.add(PELUCHE_FOXY);
		
		PELUCHE_CHICA = new JLabel("");
		PELUCHE_CHICA.setBounds(217, 590, 74, 68);
		pnlColeccionables.add(PELUCHE_CHICA);
		
		PELUCHE_BONNIE = new JLabel("");
		PELUCHE_BONNIE.setBounds(291, 590, 74, 68);
		pnlColeccionables.add(PELUCHE_BONNIE);
		
		
		//TERCERA LINEA
		PELUCHE_GOLDEN_FREDDY = new JLabel("");
		PELUCHE_GOLDEN_FREDDY.setBounds(69, 676, 74, 68);
		pnlColeccionables.add(PELUCHE_GOLDEN_FREDDY);
		
		BALLOONBOY = new JLabel("");
		BALLOONBOY.setBounds(143, 676, 74, 68);
		pnlColeccionables.add(BALLOONBOY);
		
		
		//examinar
		lblMostrar = new JLabel("");
		lblMostrar.setBounds(101, 227, 232, 136);
		pnlColeccionables.add(lblMostrar);

		lblDescripcion = new JLabel("");
		lblDescripcion.setBounds(69,398,296,68);
		lblDescripcion.setHorizontalAlignment(SwingConstants.CENTER);
		lblDescripcion.setForeground(java.awt.Color.WHITE);
		lblDescripcion.setFont(com.fdaf.util.Fuentes.obtener(22));
		pnlColeccionables.add(lblDescripcion);
		panel.add(lblTabletCerrar);
		
		
		
		/*
		 * Informacion
		 */
		
		lblCopyPowerLeft = new JLabel("");
		lblCopyPowerLeft.setBounds(1326, 314, 200, 55);
		pnlColeccionables.add(lblCopyPowerLeft);
		
		lblCopyBateria = new JLabel("");
		lblCopyBateria.setBounds(1340, 393, 100, 48);
		pnlColeccionables.add(lblCopyBateria);
		


	}
	public JLabel getCUPCAKE() {
		return CUPCAKE;
	}
	public void setCUPCAKE(JLabel cUPCAKE) {
		CUPCAKE = cUPCAKE;
	}
	public JLabel getGUITARRA_BONNIE() {
		return GUITARRA_BONNIE;
	}
	public void setGUITARRA_BONNIE(JLabel gUITARRA_BONNIE) {
		GUITARRA_BONNIE = gUITARRA_BONNIE;
	}
	public JLabel getMICROFONO_FREDDY() {
		return MICROFONO_FREDDY;
	}
	public void setMICROFONO_FREDDY(JLabel mICROFONO_FREDDY) {
		MICROFONO_FREDDY = mICROFONO_FREDDY;
	}
	public JLabel getPELUCHE_FREDDY() {
		return PELUCHE_FREDDY;
	}
	public void setPELUCHE_FREDDY(JLabel pELUCHE_FREDDY) {
		PELUCHE_FREDDY = pELUCHE_FREDDY;
	}
	public JLabel getPELUCHE_FOXY() {
		return PELUCHE_FOXY;
	}
	public void setPELUCHE_FOXY(JLabel pELUCHE_FOXY) {
		PELUCHE_FOXY = pELUCHE_FOXY;
	}
	public JLabel getPELUCHE_CHICA() {
		return PELUCHE_CHICA;
	}
	public void setPELUCHE_CHICA(JLabel pELUCHE_CHICA) {
		PELUCHE_CHICA = pELUCHE_CHICA;
	}
	public JLabel getPELUCHE_BONNIE() {
		return PELUCHE_BONNIE;
	}
	public void setPELUCHE_BONNIE(JLabel pELUCHE_BONNIE) {
		PELUCHE_BONNIE = pELUCHE_BONNIE;
	}
	public JLabel getGARFIO_FOXY() {
		return GARFIO_FOXY;
	}
	public void setGARFIO_FOXY(JLabel gARFIO_FOXY) {
		GARFIO_FOXY = gARFIO_FOXY;
	}
	public JLabel getPELUCHE_GOLDEN_FREDDY() {
		return PELUCHE_GOLDEN_FREDDY;
	}
	public void setPELUCHE_GOLDEN_FREDDY(JLabel pELUCHE_GOLDEN_FREDDY) {
		PELUCHE_GOLDEN_FREDDY = pELUCHE_GOLDEN_FREDDY;
	}
	public JLabel getBALLOONBOY() {
		return BALLOONBOY;
	}
	public void setBALLOONBOY(JLabel bALLOONBOY) {
		BALLOONBOY = bALLOONBOY;
	}
	public JLabel getLblMostrar() {
		return lblMostrar;
	}
	public void setLblMostrar(JLabel lblMostrar) {
		this.lblMostrar = lblMostrar;
	}
	public JLabel getLblCopyPowerLeft() {
		return lblCopyPowerLeft;
	}
	public void setLblCopyPowerLeft(JLabel lblCopyPowerLeft) {
		this.lblCopyPowerLeft = lblCopyPowerLeft;
	}
	public JLabel getLblCopyBateria() {
		return lblCopyBateria;
	}
	public void setLblCopyBateria(JLabel lblCopyBateria) {
		this.lblCopyBateria = lblCopyBateria;
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

	public JLabel getlblPuntoCam() {
		return lblPuntoCam;
	}

	public void setlblPuntoCam(JLabel lblPuntoCam) {
		this.lblPuntoCam = lblPuntoCam;
	}

	public JPanel getPnlColeccionables() {
		return pnlColeccionables;
	}

	public void setPnlColeccionables(JPanel pnlColeccionables) {
		this.pnlColeccionables = pnlColeccionables;
	}

	public JLabel getLblDescripcion() {
		return lblDescripcion;
	}

	public void setLblDescripcion(JLabel lblDescripcion) {
		this.lblDescripcion = lblDescripcion;
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