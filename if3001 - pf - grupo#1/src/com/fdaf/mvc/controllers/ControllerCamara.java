package com.fdaf.mvc.controllers;



import java.awt.Color;

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

	private int xlbl;

	private int xpnlDer;

	private int xpnlIzq;

	private int progreso;

	private boolean tablet;



	// CAMBIO FDAF - Refactor: paneles inyectados desde ControllerInterfaz.

	public ControllerCamara(PnlJuego pnlJuego, PnlTableta pnlTableta) {

		this.pnlJuego = pnlJuego;

		this.pnlTableta = pnlTableta;

		this.tablet = false;


	}



	public void init(VistaPrincipal vp, PnlMenu menu) {

		EscalarVista.adaptarJuego(vp, pnlJuego, pnlTableta);
		this.xpnlDer = pnlJuego.getPanelDer().getX();
	    this.xpnlIzq = pnlJuego.getPanelIzq().getX();
		
		
		vp.setContenido(pnlJuego);





		zonaUnoDer();

		zonaUnoIzq();

		zonaDosDer();

		zonaDosIzq();

		zonaTresDer();

		zonaTresIzq();



		abrirTableta(vp);

		cerrarTableta(vp);

		rendirse(vp, menu);

	}



	/*

	 * Acciones de pnlTableta

	 */



	public void abrirTableta(VistaPrincipal vp) {

		pnlJuego.getLblTabAbrir().addMouseListener(new MouseAdapter() {

			@Override

			public void mouseEntered(MouseEvent evt) {

				if (!tablet) {

					tablet = true;

					vp.setContenido(pnlTableta);

				}

			}

			@Override

			public void mouseExited(MouseEvent e) {

				tablet = false;

			}

		});

	}



	public void cerrarTableta(VistaPrincipal vp) {

		pnlTableta.getLblTabletCerrar().addMouseListener(new MouseAdapter() {

			@Override

			public void mouseEntered(MouseEvent evt) {

				if (tablet) {

					tablet = false;

					vp.setContenido(pnlJuego);



				}

			}

			@Override

			public void mouseExited(MouseEvent e) {

				tablet = true;

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



		pnlTableta.getPbarRendirse().addMouseListener(new MouseAdapter() {

			@Override

			public void mousePressed(MouseEvent e) {

				tiempo.start();

			}

			@Override

			public void mouseReleased(MouseEvent e) {

				tiempo.stop();

				progreso = 0;

				pnlTableta.getPbarRendirse().setValue(0);

			}

		});

	}



	/*

	 * Movimiento

	 */



	public void izquierda(int v) {

		izquierda = new Timer(v, e -> {

			xlbl = pnlJuego.getLblImgOficina().getX();

			int ylbl = pnlJuego.getLblImgOficina().getY();

			int ypnl = pnlJuego.getPanelIzq().getY();



			if (xlbl == 0) {

				izquierda.stop();

			} else {

				xlbl++;

				xpnlIzq++;

				xpnlDer++;

			}



			pnlJuego.getPanelIzq().setLocation(xpnlIzq, ypnl);

			pnlJuego.getPanelDer().setLocation(xpnlDer, ypnl);

			pnlJuego.getLblImgOficina().setLocation(xlbl, ylbl);

		});

		izquierda.start();

	}



	public void derecha(int v) {

		derecha = new Timer(v, e -> {

			xlbl = pnlJuego.getLblImgOficina().getX();

			int ylbl = pnlJuego.getLblImgOficina().getY();

			int ypnl = pnlJuego.getPanelDer().getY();



			if (xlbl == EscalarVista.getEscalaX(-602)) {

				derecha.stop();

			} else {

				xlbl--;

				xpnlIzq--;

				xpnlDer--;

			}



			pnlJuego.getPanelIzq().setLocation(xpnlIzq, ypnl);

			pnlJuego.getPanelDer().setLocation(xpnlDer, ypnl);

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

				derecha(-100);

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