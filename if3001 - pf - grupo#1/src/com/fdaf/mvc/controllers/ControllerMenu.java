package com.fdaf.mvc.controllers;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.fdaf.mvc.views.Jframe.VistaPrincipal;
import com.fdaf.mvc.views.Jframe.pnl.PnlMenu;
import com.fdaf.mvc.views.Jframe.pnl.PnlOpciones;
import com.fdaf.util.EscalarVista;

public class ControllerMenu {

	private PnlMenu menu;
	private PnlOpciones opciones;
	private VistaPrincipal vp;

	public ControllerMenu() {
		menu = new PnlMenu();
		opciones = new PnlOpciones();
		vp = new VistaPrincipal();
	}

	public void init() {
		EscalarVista.adaptarVista(vp);
		vp.setContenido(menu);
		vp.setColor(new Color(0, 0, 8));
		botones();
		encima();
		vp.init();
	}

	public void botones() {
		menu.getBtnOpciones().addActionListener(e -> {
			vp.setContenido(opciones);
			opciones();
		});

		// Cada vez que se presiona "Empezar" se crea una ControllerInterfaz
		// COMPLETAMENTE NUEVA: paneles nuevos (sin listeners viejos),
		// ControllerJuego nuevo (sin vidas/coleccionables/árbol del intento
		// anterior), y los objetos de la partida previa quedan sin
		// referencias para que el garbage collector los elimine (incluidos
		// sus timers ya detenidos). Esto elimina de raíz la acumulación de
		// listeners, los estados zombis y los timers vivos.
		menu.getBtnEmpezar().addActionListener(e -> {
			ControllerInterfaz interfaz = new ControllerInterfaz();
			interfaz.init(vp, menu);
		});
	}

	public void opciones() {
		opciones.getBtnAtras().addActionListener(e -> vp.setContenido(menu));
		opciones.getBtnSalir().addActionListener(e -> System.exit(0));
	}

	private void encima() {
		menu.getBtnEmpezar().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent evt) {
				int x = menu.getBtnEmpezar().getX() - 40;
				int y = menu.getBtnEmpezar().getY();
				menu.getLbl().setLocation(x, y);
				menu.getLbl().setVisible(true);
			}
			@Override
			public void mouseExited(MouseEvent evt) {
				menu.getLbl().setVisible(false);
			}
		});

		menu.getBtnOpciones().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent evt) {
				int x = menu.getBtnOpciones().getX() - 30;
				int y = menu.getBtnOpciones().getY();
				menu.getLbl().setLocation(x, y);
				menu.getLbl().setVisible(true);
			}
			@Override
			public void mouseExited(MouseEvent evt) {
				menu.getLbl().setVisible(false);
			}
		});

		opciones.getBtnAtras().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent evt) {
				int x = opciones.getBtnAtras().getX() - 30;
				int y = opciones.getBtnAtras().getY();
				opciones.getLbl().setLocation(x, y);
				opciones.getLbl().setVisible(true);
			}
			@Override
			public void mouseExited(MouseEvent evt) {
				opciones.getLbl().setVisible(false);
			}
		});

		opciones.getBtnSalir().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent evt) {
				int x = opciones.getBtnSalir().getX() - 30;
				int y = opciones.getBtnSalir().getY();
				opciones.getLbl().setLocation(x, y);
				opciones.getLbl().setVisible(true);
			}
			@Override
			public void mouseExited(MouseEvent evt) {
				opciones.getLbl().setVisible(false);
			}
		});
	}

}