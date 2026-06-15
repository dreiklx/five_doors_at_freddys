package com.fdaf.mvc.controllers;

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
	private int xbtnDer;
	private int xbtnIzq;
	private int xluzDer;
	private int xluzIzq;

	 
	 
	
	public ControllerCamara() {
		
		juego=new PnlJuego();
		 xbtnDer=juego.getBotonDer().getX();
		 xbtnIzq=juego.getBotonIzq().getX();
		 xluzDer=juego.getLuzDer().getX();
		 xluzIzq=juego.getLuzIzq().getX();
	}
	
	public void init(VistaPrincipal vp) {
		vp.setContenido(juego);
		
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
			int yluz=juego.getLuzIzq().getY();
			int ybtn=juego.getBotonIzq().getY();
			
			if(xlbl==0) {
				izquierda.stop();
			}else {
				xlbl++;
				xbtnIzq++;
				xbtnDer++;
				xluzIzq++;
				xluzDer++;
				
			}
			
			juego.getBotonIzq().setLocation(xbtnIzq, ybtn);
			juego.getBotonDer().setLocation(xbtnDer, ybtn);
			juego.getLuzIzq().setLocation(xluzIzq, yluz);
			juego.getLuzDer().setLocation(xluzDer, yluz);
			juego.getLblOficina().setLocation(xlbl, ylbl);
			
		});
		izquierda.start();
	}
	
	public void derecha(int v) {
		derecha=new Timer(v,e ->{ 
			 xlbl=juego.getLblOficina().getX();

			int ylbl=juego.getLblOficina().getY();
			int yluz=juego.getLuzDer().getY();
			int ybtn=juego.getBotonDer().getY();
			if(xlbl==-594) {
				derecha.stop();

			}else {
				xlbl--;
				xbtnDer--;
				xbtnIzq--;
				xluzDer--;
				xluzIzq--;
			}
			
			juego.getBotonDer().setLocation(xbtnDer, ybtn);
			juego.getBotonIzq().setLocation(xbtnIzq, ybtn);
			juego.getLuzDer().setLocation(xluzDer, yluz);
			juego.getLuzIzq().setLocation(xluzIzq, yluz);
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
