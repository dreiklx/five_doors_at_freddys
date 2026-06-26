package com.fdaf.mvc.controllers;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.fdaf.mvc.models.animatronicos.Animatronico;
import com.fdaf.mvc.models.coleccionables.Coleccionable;
import com.fdaf.util.CargarImagenes;
import com.fdaf.mvc.views.Jframe.VistaPrincipal;
import com.fdaf.mvc.views.Jframe.pnl.PnlJuego;
import com.fdaf.mvc.views.Jframe.pnl.PnlMenu;
import com.fdaf.mvc.views.Jframe.pnl.PnlTableta;

// ControllerInterfaz es DUEÑO de los paneles y de los demás controladores,
// conecta los listeners de gameplay, y ejecuta TODAS las órdenes visuales
// que ControllerJuego le pide vía JuegoListener. No toma decisiones de
// reglas. Se crea NUEVA en cada partida (ver ControllerMenu), por lo que
// sus paneles y listeners siempre arrancan limpios.
public class ControllerInterfaz implements JuegoListener {

	private PnlJuego pnlJuego;
	private PnlTableta pnlTableta;

	private ControllerJuego controllerJuego;
	private ControllerCamara controllerCamara;

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

		conectarPuertas();
		conectarLuces();
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
	 * ==== Implementación de JuegoListener (ejecución visual pura) ====
	 */

	@Override
	public void alAbrirPuerta(String lado) {
		if ("izq".equals(lado)) {
			pnlJuego.getBotonIzq().setIcon(CargarImagenes.btnIzq1);
		} else {
			pnlJuego.getBotonDer().setIcon(CargarImagenes.btnDer1);
		}
	}

	@Override
	public void alEncenderLuz(String lado) {
		if ("izq".equals(lado)) {
			pnlJuego.getLuzIzq().setIcon(CargarImagenes.luzIzq1);
		} else {
			pnlJuego.getLuzDer().setIcon(CargarImagenes.luzDer1);
		}
	}

	@Override
	public void alRevelarAnimatronico(String lado, Animatronico animatronico) {
		String nombre = animatronico.name();
		String capitalizado = nombre.charAt(0) + nombre.substring(1).toLowerCase();
		String ruta = "/gifs/jumpscares/Jumpscare" + capitalizado + ".gif";
		mostrarEnOverlay(ruta);
	}

	@Override
	public void alRevelarColeccionable(String lado, Coleccionable coleccionable) {
		String ruta = "/images/" + coleccionable.getArchivoImagen();
		mostrarEnOverlay(ruta);
	}

	@Override
	public void alRevelarNada(String lado) {
		// Sin ícono: la oscuridad se mantiene.
	}

	private void mostrarEnOverlay(String ruta) {
		java.net.URL recurso = getClass().getResource(ruta);
		if (recurso == null) {
			System.out.println("[RECURSO NO ENCONTRADO] " + ruta);
			return;
		}
		JLabel overlay = pnlJuego.getLblOverlay();
		overlay.setIcon(new ImageIcon(recurso));
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
		JLabel item = new JLabel(new ImageIcon(recurso));
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
			pnlJuego.getLuzIzq().setIcon(CargarImagenes.luzIzq0);
			pnlJuego.getBotonIzq().setIcon(CargarImagenes.btnIzq0);
		} else {
			pnlJuego.getLuzDer().setIcon(CargarImagenes.luzDer0);
			pnlJuego.getBotonDer().setIcon(CargarImagenes.btnDer0);
		}
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