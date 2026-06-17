package com.fdaf.mvc.controllers;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Timer;

import com.fdaf.mvc.views.Jframe.VistaPrincipal;
import com.fdaf.mvc.views.Jframe.pnl.PnlJuego;
import com.fdaf.mvc.views.multimedia.Sprite;
import com.fdaf.util.CargarImagenes;
 

public class ControllerCamara {
	private PnlJuego juego;
	private Timer derecha;
	private Timer izquierda;
	private Sprite barraTableta;
	private int xlbl;
	private int xpnlDer;
	private int xpnlIzq;


	 
	 
	
	public ControllerCamara() {
		
		juego=new PnlJuego();
		 xpnlDer=juego.getPanelDer().getX();
		 xpnlIzq=juego.getPanelIzq().getX();

	}
	
	public void init(VistaPrincipal vp) {
		vp.setContenido(juego);
		vp.setColor(Color.BLACK);
		
		zonaUnoDer();
		zonaUnoIzq();
		zonaDosDer();
		zonaDosIzq();
		zonaTresDer();
		zonaTresIzq();
		juego.btnNewButton.addActionListener(e ->{
			System.exit(0);
		});
		tableta();
	}
	
	
	/*
	 * 
	 * Acciones de tableta
	 * 
	 */
	
	public void tableta() {
		barraTableta=new Sprite(juego.getLblTabAbrir());
		barraTableta.setImagen(CargarImagenes.barraTableta);
	}
	
	
	
	
	
	
	/*
	 * 
	 * Movimiento
	 * 
	 */
	
	public void izquierda(int v) {
		izquierda=new Timer(v,e ->{ 
			 xlbl=juego.getLblOficina().getX();
			 
			int ylbl=juego.getLblOficina().getY();
			int ypnl=juego.getPanelIzq().getY();
			
			if(xlbl==0) {
				izquierda.stop();
			}else {
				xlbl++;
				xpnlIzq++;
				xpnlDer++;
				
			}
			
			juego.getPanelIzq().setLocation(xpnlIzq, ypnl);
			juego.getPanelDer().setLocation(xpnlDer, ypnl);
			juego.getLblOficina().setLocation(xlbl, ylbl);
			
		});
		izquierda.start();
	}
	
	public void derecha(int v) {
		derecha=new Timer(v,e ->{ 
			 xlbl=juego.getLblOficina().getX();

			int ylbl=juego.getLblOficina().getY();
			int ypnl=juego.getPanelDer().getY();
			if(xlbl==-594) {
				derecha.stop();

			}else {
				xlbl--;
				xpnlIzq--;
				xpnlDer--;
				
			}
			
			juego.getPanelIzq().setLocation(xpnlIzq, ypnl);
			juego.getPanelDer().setLocation(xpnlDer, ypnl);
			juego.getLblOficina().setLocation(xlbl, ylbl);

		});
		derecha.start();
	}
	
	
	
	
	/*
	 * 
	 * Primara zona de interferencia
	 * 
	 */
	
	public void zonaUnoDer() {
	juego.getLblDerUno().addMouseListener(new MouseAdapter() {
		@Override
        public void mouseEntered(MouseEvent evt) {
				derecha(10);
			
        }

        @Override
        public void mouseExited(MouseEvent evt) {
        	derecha.stop();
        }
	});
	}
	
	
	public void zonaUnoIzq() {
		juego.getLblIzqUno().addMouseListener(new MouseAdapter() {
			@Override
	        public void mouseEntered(MouseEvent evt) {
				izquierda(10);
	        }

	        @Override
	        public void mouseExited(MouseEvent evt) {
	        	izquierda.stop();

	        }
		});
		}
	

		
		/*
		 * 
		 * Segunda zona de interferencia
		 * 
		 */
	
	
	public void zonaDosDer() {
	juego.getLblDerDos().addMouseListener(new MouseAdapter() {
		@Override
        public void mouseEntered(MouseEvent evt) {
			derecha(0);
        }

        @Override
        public void mouseExited(MouseEvent evt) {
        	derecha.stop();
        }
	});
	}
	
	
	public void zonaDosIzq() {
		juego.getLblIzqDos().addMouseListener(new MouseAdapter() {
			@Override
	        public void mouseEntered(MouseEvent evt) {
				izquierda(0);
	        }

	        @Override
	        public void mouseExited(MouseEvent evt) {
	        	izquierda.stop();

	        }
		});
		}
		
		
		
		/*
		 * 
		 * Tercera zona de interferencia
		 * 
		 */
	

	
	public void zonaTresDer() {
	juego.getLblDerTres().addMouseListener(new MouseAdapter() {
		@Override
        public void mouseEntered(MouseEvent evt) {
			derecha(0);
        }

        @Override
        public void mouseExited(MouseEvent evt) {
        	derecha.stop();
        }
	});
	}
	
	
	public void zonaTresIzq() {
		juego.getLblIzqTres().addMouseListener(new MouseAdapter() {
			@Override
	        public void mouseEntered(MouseEvent evt) {
				izquierda(0);
	        }

	        @Override
	        public void mouseExited(MouseEvent evt) {
	        	izquierda.stop();

	        }
		});
		}
}
