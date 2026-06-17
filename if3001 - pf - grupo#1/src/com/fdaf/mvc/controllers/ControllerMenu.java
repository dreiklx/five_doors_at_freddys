package com.fdaf.mvc.controllers;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

import com.fdaf.mvc.views.Jframe.VistaPrincipal;
import com.fdaf.mvc.views.Jframe.pnl.PnlMenu;
import com.fdaf.mvc.views.Jframe.pnl.PnlOpciones;

public class ControllerMenu {
	
	private PnlMenu menu;
	private ControllerCamara camara;
	private PnlOpciones opciones;
	private VistaPrincipal vp;
	
	public ControllerMenu() {
		menu=new PnlMenu();
		camara=new ControllerCamara();
		opciones=new PnlOpciones();
		vp=new VistaPrincipal();
		
	}
	
	
	public void init() {
		vp.setContenido(menu);
		vp.setColor(new Color(0, 0, 8));
		botones();
		encima();
		vp.init();
		
	}
	
	public void botones() {
		menu.getBtnOpciones().addActionListener(e-> {
			vp.setContenido(opciones);
			opciones();
			});
		
		menu.getBtnEmpezar().addActionListener(e-> camara.init(vp));
	}
	public void opciones() {
		opciones.getBtnAtras().addActionListener(e-> vp.setContenido(menu));
		opciones.getBtnSalir().addActionListener(e-> System.exit(0));
	}
	
	private void encima() {
		/*
		 * MENU
		 */
		menu.getBtnEmpezar().addMouseListener(new MouseAdapter() {
			@Override
	        public void mouseEntered(MouseEvent evt) {
				int x=menu.getBtnEmpezar().getX()-40;
				int y=menu.getBtnEmpezar().getY();
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
				int x=menu.getBtnOpciones().getX()-30;
				int y=menu.getBtnOpciones().getY();
				menu.getLbl().setLocation(x, y);
				menu.getLbl().setVisible(true);
				
	        }

	        @Override
	        public void mouseExited(MouseEvent evt) {
	        	menu.getLbl().setVisible(false);
	        }
		});
		
		/*
		 * 		
		 * OPCIONES
		 */
		opciones.getBtnAtras().addMouseListener(new MouseAdapter() {
			@Override
	        public void mouseEntered(MouseEvent evt) {
				int x=opciones.getBtnAtras().getX()-30;
				int y=opciones.getBtnAtras().getY();
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
				int x=opciones.getBtnSalir().getX()-30;
				int y=opciones.getBtnSalir().getY();
				opciones.getLbl().setLocation(x, y);
				opciones.getLbl().setVisible(true);
				opciones.getLbl().setVisible(true);
				
	        }

	        @Override
	        public void mouseExited(MouseEvent evt) {
	        	opciones.getLbl().setVisible(false);
	        }
		});
		
		
	}

}
