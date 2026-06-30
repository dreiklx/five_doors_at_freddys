package com.fdaf.mvc.controllers;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import com.fdaf.mvc.models.animatronicos.Animatronico;
import com.fdaf.mvc.models.coleccionables.Coleccionable;
import com.fdaf.util.CargarImagenes;
import com.fdaf.util.EscalarVista;
import com.fdaf.mvc.views.Jframe.VistaPrincipal;
import com.fdaf.mvc.views.Jframe.pnl.PnlJuego;
import com.fdaf.mvc.views.Jframe.pnl.PnlMenu;
import com.fdaf.mvc.views.Jframe.pnl.PnlTableta;

public class ControllerInterfaz implements JuegoListener {

	private PnlJuego pnlJuego;
	private PnlTableta pnlTableta;

	private ControllerJuego controllerJuego;
	private ControllerCamara controllerCamara;

	private EscalarVista.GifEscalado gifOficinaNormal;
	private EscalarVista.GifEscalado gifOficinaLuzIzq;
	private EscalarVista.GifEscalado gifOficinaLuzDer;

	public ControllerInterfaz() {
		pnlJuego = new PnlJuego();
		pnlTableta = new PnlTableta();

		controllerJuego = new ControllerJuego();
		controllerJuego.setListener(this);
		controllerJuego.init();

		controllerCamara = new ControllerCamara(pnlJuego, pnlTableta);
	}

	public void init(VistaPrincipal vp, PnlMenu menu) {
		controllerCamara.init(vp, menu);

	
		prepararInstanciasPreEscaladas();

		conectarPuertas();
		conectarLuces();
	}

	private void prepararInstanciasPreEscaladas() {
		int anchoOficina = pnlJuego.getLblImgOficina().getWidth();
		int altoOficina = pnlJuego.getLblImgOficina().getHeight();

		// Guardamos las mutaciones gráficas una sola vez
		this.gifOficinaNormal = new EscalarVista.GifEscalado(CargarImagenes.fondoJuego, anchoOficina, altoOficina);
		this.gifOficinaLuzIzq = new EscalarVista.GifEscalado(CargarImagenes.juegoLuzIzq, anchoOficina, altoOficina);
		this.gifOficinaLuzDer = new EscalarVista.GifEscalado(CargarImagenes.juegoLuzDer, anchoOficina, altoOficina);
	}

	private void conectarPuertas() {
		pnlJuego.getBotonIzq().addActionListener(e -> controllerJuego.abrirIzquierda());
		pnlJuego.getBotonDer().addActionListener(e -> controllerJuego.abrirDerecha());
	}

	private void conectarLuces() {
		pnlJuego.getLuzIzq().addActionListener(e -> controllerJuego.revelarIzquierda());
		pnlJuego.getLuzDer().addActionListener(e -> controllerJuego.revelarDerecha());

		pnlJuego.getLblOverlay().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				controllerJuego.recogerColeccionable();
			}
		});
	}

	/*
	 * ==== Implementación de JuegoListener ====
	 */

	@Override
	public void alAbrirPuerta(String lado) {
		if ("izq".equals(lado)) {
			pnlJuego.getBotonIzq().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.btnIzq1,
					pnlJuego.getBotonIzq().getWidth(), pnlJuego.getBotonIzq().getHeight()));
			pnlJuego.getLblPuertaIzq().setIcon(new EscalarVista.GifEscalado(
					CargarImagenes.puertaIzq1,
					pnlJuego.getLblPuertaIzq().getWidth(),
					pnlJuego.getLblPuertaIzq().getHeight()
			));
		} else {
			pnlJuego.getBotonDer().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.btnDer1,
					pnlJuego.getBotonDer().getWidth(), pnlJuego.getBotonDer().getHeight()));
			pnlJuego.getLblPuertaDer().setIcon(new EscalarVista.GifEscalado(
					CargarImagenes.puertaDer1,
					pnlJuego.getLblPuertaDer().getWidth(),
					pnlJuego.getLblPuertaDer().getHeight()
			));
		}
	}

	@Override
	public void alEncenderLuz(String lado) {
		if ("izq".equals(lado)) {
			pnlJuego.getLuzIzq().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.luzIzq1,
					pnlJuego.getLuzIzq().getWidth(), pnlJuego.getLuzIzq().getHeight()));
			
			pnlJuego.getLblImgOficina().setIcon(gifOficinaLuzIzq);

		} else {
			pnlJuego.getLuzDer().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.luzDer1,
					pnlJuego.getLuzDer().getWidth(), pnlJuego.getLuzDer().getHeight()));

			pnlJuego.getLblImgOficina().setIcon(gifOficinaLuzDer);
		}
		
		pnlJuego.revalidate();
		pnlJuego.repaint();
	}

	@Override
	public void alRevelarAnimatronico(String lado, Animatronico animatronico) {
		String nombre = animatronico.name();
		String capitalizado = nombre.charAt(0) + nombre.substring(1).toLowerCase();
		String ruta = "/gifs/jumpscares/Jumpscare" + capitalizado + ".gif";
		mostrarEnOverlay(ruta, 1, lado);
	}

	@Override
	public void alRevelarColeccionable(String lado, Coleccionable coleccionable) {
		String ruta = "/images/" + coleccionable.getArchivoImagen();
		mostrarEnOverlay(ruta, 0, lado);
	}

	@Override
	public void alRevelarNada(String lado) {
	}

	private void mostrarEnOverlay(String ruta, int c, String lado) {
		java.net.URL recurso = getClass().getResource(ruta);
		if (recurso == null) {
			System.out.println("[RECURSO NO ENCONTRADO] " + ruta);
			return;
		}

		JLabel overlay = pnlJuego.getLblOverlay();

		if (c == 1) {
			overlay.setCursor(Cursor.getDefaultCursor());
			overlay.setBounds(0, 0, EscalarVista.getEscalaX(1610), EscalarVista.getEscalaY(910));
			overlay.setIcon(new EscalarVista.GifEscalado(
					new ImageIcon(recurso), 
					overlay.getWidth(), 
					overlay.getHeight()
			));
			
		} else {
			overlay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			
			if ("izq".equals(lado)) {
				overlay.setBounds(EscalarVista.getEscalaX(350),EscalarVista.getEscalaY(710),
						EscalarVista.getEscalaX(80),EscalarVista.getEscalaY(80));
				overlay.setIcon(EscalarVista.getImagenEscalada(new ImageIcon(recurso),overlay.getWidth(), overlay.getHeight()));
			}else {
				overlay.setBounds(EscalarVista.getEscalaX(1200),EscalarVista.getEscalaY(740),
						EscalarVista.getEscalaX(80),EscalarVista.getEscalaY(80));
				overlay.setIcon(EscalarVista.getImagenEscalada(new ImageIcon(recurso),overlay.getWidth(), overlay.getHeight()));
			}
		}

		overlay.setVisible(true);
		pnlJuego.setComponentZOrder(overlay, 0);
		overlay.repaint();
		pnlJuego.repaint();
	}

	@Override
	public void alRecogerColeccionable(Coleccionable coleccionable) {
		String ruta = "/images/" + coleccionable.getArchivoImagen();
		java.net.URL recurso = getClass().getResource(ruta);
		if (recurso == null) {
			System.out.println("[RECURSO NO ENCONTRADO] " + ruta);
			return;
		}
		JLabel item = new JLabel(EscalarVista.getImagenEscalada(new ImageIcon(recurso), 80, 80));
		item.setPreferredSize(new Dimension(80, 80));
		pnlTableta.getPnlColeccionables().add(item);
		pnlTableta.getPnlColeccionables().revalidate();
		pnlTableta.getPnlColeccionables().repaint();
	}

	@Override
	public void alLiberar(String lado) {
		JLabel overlay = pnlJuego.getLblOverlay();
		overlay.setVisible(false);
		overlay.setIcon(null);

		if ("izq".equals(lado)) {
			pnlJuego.getLuzIzq().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.luzIzq0,
					pnlJuego.getLuzIzq().getWidth(), pnlJuego.getLuzIzq().getHeight()));

			//cerrar puerta sin animacion
			pnlJuego.getLblPuertaIzq().setIcon(new EscalarVista.GifEscalado(
					CargarImagenes.puertaIzq0,
					pnlJuego.getLblPuertaIzq().getWidth(),
					pnlJuego.getLblPuertaIzq().getHeight()
			));
			////////////////////////////////
			pnlJuego.getLblImgOficina().setIcon(gifOficinaNormal);

			pnlJuego.getBotonIzq().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.btnIzq0,
					pnlJuego.getBotonIzq().getWidth(), pnlJuego.getBotonIzq().getHeight()));
		} else {
			pnlJuego.getLuzDer().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.luzDer0,
					pnlJuego.getLuzDer().getWidth(), pnlJuego.getLuzDer().getHeight()));

			//cerrar puerta sin animacion
			pnlJuego.getLblPuertaDer().setIcon(new EscalarVista.GifEscalado(
					CargarImagenes.puertaDer0,
					pnlJuego.getLblPuertaDer().getWidth(),
					pnlJuego.getLblPuertaDer().getHeight()
			));


			//////////////////////////////
			pnlJuego.getLblImgOficina().setIcon(gifOficinaNormal);

			pnlJuego.getBotonDer().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.btnDer0,
					pnlJuego.getBotonDer().getWidth(), pnlJuego.getBotonDer().getHeight()));
		}
		pnlJuego.revalidate();
		pnlJuego.repaint();
	}

	@Override
	public void alPerder() {
		deshabilitarControles();
		JOptionPane.showMessageDialog(null, "GAME OVER");
	}

	@Override
	public void alGanar() {
		deshabilitarControles();
		JOptionPane.showMessageDialog(null, "¡GANASTE! Encontraste todos los coleccionables.");
	}

	private void deshabilitarControles() {
		pnlJuego.getBotonIzq().setEnabled(false);
		pnlJuego.getBotonDer().setEnabled(false);
		pnlJuego.getLuzIzq().setEnabled(false);
		pnlJuego.getLuzDer().setEnabled(false);
	}

	public PnlJuego getPnlJuego() {
		return pnlJuego;
	}

	public PnlTableta getPnlTableta() {
		return pnlTableta;
	}
}