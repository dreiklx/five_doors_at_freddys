package com.fdaf.mvc.controllers;



import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Timer;
import com.fdaf.mvc.views.Jframe.VistaPrincipal;
import com.fdaf.mvc.views.Jframe.pnl.PnlJuego;
import com.fdaf.mvc.views.Jframe.pnl.PnlMenu;
import com.fdaf.mvc.views.Jframe.pnl.PnlTableta;
import com.fdaf.util.EscalarVista;



// CAMBIO FDAF - Refactor: ControllerCamara quedó EXCLUSIVAMENTE como

// controlador de cámara/UI: movimiento horizontal de la oficina, zonas de

// interferencia, tablet y desplazamiento visual. No conoce puertas, luces,

// revelaciones, coleccionables, victoria, derrota ni overlays. Los paneles

// se le INYECTAN (no los crea), para que exista una única instancia

// compartida con ControllerInterfaz.

public class ControllerCamara {



	private PnlJuego pnlJuego;

	private PnlTableta pnlTableta;



	private Timer derecha;

	private Timer izquierda;

	private Timer tiempo;
	private Timer soltar;

	private int xlbl;

	private int xpnlDer;
	private int xpnlIzq;
	
	private int xPDer;
	private int xPIzq;
		

	private int progreso;

	private Timer bloquear;
	private boolean bloqueado;
	private int tiempoBloqueado;



	// CAMBIO FDAF - Refactor: paneles inyectados desde ControllerInterfaz.

	public ControllerCamara(PnlJuego pnlJuego, PnlTableta pnlTableta) {
		this.pnlJuego = pnlJuego;

		this.pnlTableta = pnlTableta;

		this.bloqueado = false;


	}



	public void init(VistaPrincipal vp, PnlMenu menu) {
		
		EscalarVista.adaptarTablet(vp, pnlTableta);
		EscalarVista.adaptarJuego(vp, pnlJuego);
		
		this.xpnlDer = pnlJuego.getPanelDer().getX();
	    this.xpnlIzq = pnlJuego.getPanelIzq().getX();
		this.xPDer=pnlJuego.getLblPuertaDer().getX();
		this.xPIzq=pnlJuego.getLblPuertaIzq().getX();
		
		vp.setContenido(pnlJuego);





		zonaUnoDer();

		zonaUnoIzq();

		zonaDosDer();

		zonaDosIzq();

		zonaTresDer();

		zonaTresIzq();


		eventosTablet(vp);


		rendirse(vp, menu);
		javax.swing.Timer loopRepintado = new javax.swing.Timer(33, ev -> {
	        if (pnlJuego != null && pnlJuego.isShowing()) {
	            pnlJuego.repaint();
	        }
	    });
	    loopRepintado.start();
	}



	/*

	 * Acciones de pnlTableta

	 */

	public void eventosTablet(VistaPrincipal vp) {

	    bloquear = new Timer(20, e -> {
	        tiempoBloqueado++;
	        
	        if (tiempoBloqueado >= 10) { 
	            bloqueado = false;
	            tiempoBloqueado = 0;
	            bloquear.stop();
	        }
	    });
	    

	    pnlJuego.getLblTabAbrir().addMouseListener(new MouseAdapter() {
	        @Override
	        public void mouseEntered(MouseEvent evt) {
	            
	            if (!bloqueado) {
	                bloqueado = true; 
	                vp.setContenido(pnlTableta);
	                
	                bloquear.stop();
	                tiempoBloqueado = 0;
	                bloquear.start();
	            }
	        }
	        

	    });
	    
	    // EVENTO: CERRAR TABLETA
	    pnlTableta.getLblTabletCerrar().addMouseListener(new MouseAdapter() {
	        @Override
	        public void mouseEntered(MouseEvent evt) {

	            if (!bloqueado) {
	                bloqueado = true;
	                vp.setContenido(pnlJuego);
	                
	                bloquear.stop(); 
	                tiempoBloqueado = 0; 
	                bloquear.start();
	            }
	        }
	    });
	}
		
	


	


	public void rendirse(VistaPrincipal vp, PnlMenu menu) {



		tiempo = new Timer(50, e -> {

			progreso++;

			pnlTableta.getPbarRendirse().setValue(progreso);



			if (progreso >= 100) {

				tiempo.stop();

				progreso = 0;

				pnlTableta.getPbarRendirse().setValue(progreso);

				vp.setContenido(menu);

			}

		});
		
		soltar=new Timer(50, e -> {
			progreso--;
			pnlTableta.getPbarRendirse().setValue(progreso);
			if(progreso==0) {
				soltar.stop();
			}
		});


		pnlTableta.getPbarRendirse().addMouseListener(new MouseAdapter() {

	

			@Override

			public void mousePressed(MouseEvent e) {
				soltar.stop();
				tiempo.start();

			}

			@Override

			public void mouseReleased(MouseEvent e) {
				tiempo.stop();
				soltar.start();
				

				

			}

		});

	}
	
	/*
	 * 
	 */
	


	/*

	 * Movimiento

	 */



	public void izquierda(int v) {

		izquierda = new Timer(v, e -> {

			xlbl = pnlJuego.getLblImgOficina().getX();

			int ylbl = pnlJuego.getLblImgOficina().getY();

			int ypnl = pnlJuego.getPanelIzq().getY();
			int yPuerta=pnlJuego.getLblPuertaDer().getY();




			if (xlbl == 0) {

				izquierda.stop();

			} else {

				xlbl++;

				xpnlIzq++;

				xpnlDer++;
				
				xPDer++;
				xPIzq++;
			}



			pnlJuego.getPanelIzq().setLocation(xpnlIzq, ypnl);

			pnlJuego.getPanelDer().setLocation(xpnlDer, ypnl);
			pnlJuego.getLblPuertaIzq().setLocation(xPIzq, yPuerta);
			pnlJuego.getLblPuertaDer().setLocation(xPDer, yPuerta);
			pnlJuego.getLblImgOficina().setLocation(xlbl, ylbl);

		});

		izquierda.start();

	}



	public void derecha(int v) {

		derecha = new Timer(v, e -> {

			xlbl = pnlJuego.getLblImgOficina().getX();

			int ylbl = pnlJuego.getLblImgOficina().getY();

			int ypnl = pnlJuego.getPanelDer().getY();
			int yPuerta=pnlJuego.getLblPuertaDer().getY();



			if (xlbl == EscalarVista.getEscalaX(-602)) {

				derecha.stop();

			} else {

				xlbl--;

				xpnlIzq--;

				xpnlDer--;
				
				xPDer--;
				xPIzq--;

			}



			pnlJuego.getPanelIzq().setLocation(xpnlIzq, ypnl);

			pnlJuego.getPanelDer().setLocation(xpnlDer, ypnl);
			pnlJuego.getLblPuertaIzq().setLocation(xPIzq, yPuerta);
			pnlJuego.getLblPuertaDer().setLocation(xPDer, yPuerta);
			pnlJuego.getLblImgOficina().setLocation(xlbl, ylbl);

		});

		derecha.start();

	}



	/*

	 * Primera zona de interferencia

	 */



	public void zonaUnoDer() {

		pnlJuego.getLblDerUno().addMouseListener(new MouseAdapter() {

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

		pnlJuego.getLblIzqUno().addMouseListener(new MouseAdapter() {

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

	 * Segunda zona de interferencia

	 */



	public void zonaDosDer() {

		pnlJuego.getLblDerDos().addMouseListener(new MouseAdapter() {

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

		pnlJuego.getLblIzqDos().addMouseListener(new MouseAdapter() {

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

	 * Tercera zona de interferencia

	 */



	public void zonaTresDer() {

		pnlJuego.getLblDerTres().addMouseListener(new MouseAdapter() {

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

		pnlJuego.getLblIzqTres().addMouseListener(new MouseAdapter() {

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