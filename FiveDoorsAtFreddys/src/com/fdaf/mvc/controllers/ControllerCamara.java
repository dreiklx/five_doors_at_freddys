 package com.fdaf.mvc.controllers;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Timer;

import com.fdaf.mvc.views.frames.VistaPrincipal;
import com.fdaf.mvc.views.frames.pnl.PnlJuego;
import com.fdaf.mvc.views.frames.pnl.PnlMenu;
import com.fdaf.mvc.views.frames.pnl.PnlTableta;
import com.fdaf.util.EscalarVista;

public class ControllerCamara {

	private ControllerInterfaz interfaz;
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
	private int xOverlay;

	private int progreso;

	private Timer bloquear;
	private boolean bloqueado;
	private int tiempoBloqueado;
	private Timer loopRepintado;

	public ControllerCamara(PnlJuego pnlJuego, PnlTableta pnlTableta, ControllerInterfaz interfaz) {
	    this.pnlJuego = pnlJuego;
	    this.pnlTableta = pnlTableta;
	    this.bloqueado = false;
	    this.interfaz = interfaz;
	}

	public void init(VistaPrincipal vp, PnlMenu menu) {
		
		EscalarVista.adaptarTablet(vp, pnlTableta);
		EscalarVista.adaptarJuego(vp, pnlJuego);
		
		this.xpnlDer = pnlJuego.getPanelDer().getX();
	    this.xpnlIzq = pnlJuego.getPanelIzq().getX();
		this.xPDer=pnlJuego.getLblPuertaDer().getX();
		this.xPIzq=pnlJuego.getLblPuertaIzq().getX();
		this.xOverlay=pnlJuego.getLblOverlay().getX();
		
		vp.setContenido(pnlJuego);

		zonaUnoDer();
		zonaUnoIzq();
		zonaDosDer();
		zonaDosIzq();
		zonaTresDer();
		zonaTresIzq();

		eventosTablet(vp);
		rendirse(vp, menu);

		loopRepintado = new Timer(33, ev -> {
	        if (pnlJuego != null && pnlJuego.isShowing()) {
	            pnlJuego.repaint();
	        }
	    });
	    loopRepintado.start();
	}

	// Se llama al terminar cualquier partida (victoria, derrota, o
	// rendirse) -- sin esto, cada partida jugada dejaba un Timer de 33ms
	// corriendo para siempre, reteniendo su propio PnlJuego en memoria.
	public void detenerLoopRepintado() {
		if (loopRepintado != null && loopRepintado.isRunning()) {
			loopRepintado.stop();
		}
	}

	// Ajusta la posición ACTUAL de panelIzq una sola vez -- no la fija, no
		// bloquea nada, no agrega condiciones a izquierda()/derecha(). Después
		// de este ajuste puntual, el botón sigue moviéndose con la cámara
		// exactamente igual que siempre (como ya lo hace panelDer).
		public void moverPanelIzquierdoInicialmente(int deltaX) {
			xpnlIzq += deltaX;
			pnlJuego.getPanelIzq().setLocation(xpnlIzq, pnlJuego.getPanelIzq().getY());
		}
		// Detiene cualquier movimiento de cámara que ya estuviera en curso --
		// se llama en el instante exacto en que arranca CUALQUIER transición
		// de tablet (apertura o cierre), para el caso de "estaba moviendo la
		// cámara y en ese momento saqué la tablet".
//// Mismo criterio que ControllerInterfaz.siPartidaActiva() --
		// consulta el estado real de la partida a través de interfaz,
		// sin depender de setEnabled/setVisible.
	private boolean juegoTerminado() {
		return interfaz.getControllerJuego().isJuegoTerminado();
	}

	public void detenerMovimientoCamara() {
		if (izquierda != null && izquierda.isRunning()) {
			izquierda.stop();
		}
		if (derecha != null && derecha.isRunning()) {
			derecha.stop();
		}
	}

	// Se llama al terminar la partida por cualquier vía (ganar, perder,
	// o rendirse) -- detiene movimiento de cámara Y la barra de
	// rendirse. Sin esto, "tiempo" podía llegar a 100% después de que
	// ya se mostró Victoria/Game Over y sobrescribirla con
	// vp.setContenido(menu), la misma clase de carrera que la tablet.
	public void detenerTimersDePartida() {
		detenerMovimientoCamara();
		if (tiempo != null && tiempo.isRunning()) {
			tiempo.stop();
		}
		if (soltar != null && soltar.isRunning()) {
			soltar.stop();
		}
	}
	
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

	            if (!bloqueado && !interfaz.isTransicionTabletActiva() && !juegoTerminado()) {
	                bloqueado = true;

	                interfaz.mostrarTransicionTablet("/gifs/fondos/abrirTablet.gif", () -> {
	                    vp.setContenido(pnlTableta);
	                });
	                bloquear.stop();
	                tiempoBloqueado = 0;
	                bloquear.start();
	            }
	        }
	    });
	    
	    pnlTableta.getLblTabletCerrar().addMouseListener(new MouseAdapter() {
	        @Override
	        public void mouseEntered(MouseEvent evt) {

	            if (!bloqueado && !interfaz.isTransicionTabletActiva() && !juegoTerminado()) {
	                bloqueado = true;

	                interfaz.mostrarTransicionTablet("/gifs/fondos/CerrarTablet.gif",
	                        () -> vp.setContenido(pnlJuego), // acción inmediata: se ve el gif, no el inventario
	                        null);
	                bloquear.stop(); 
	                tiempoBloqueado = 0; 
	                bloquear.start();
	            }
	        }
	    });
	}

	public void rendirse(VistaPrincipal vp, PnlMenu menu) {

		tiempo = new Timer(50, e -> {

			progreso+=2;
			pnlTableta.getPbarRendirse().setValue(progreso);

			if (progreso >= 100) {

				tiempo.stop();
			    interfaz.detenerSonidosJuego();

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
				if (juegoTerminado()) {
					return;
				}
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

	public void izquierda(int v) {

		// Único punto de bloqueo: mientras dure CUALQUIER transición de
		// tablet (apertura, cierre, o el margen de botones ocultos que la
		// rodea), no se arranca movimiento nuevo. "Tablet abierta" no
		// necesita este chequeo -- ahí pnlJuego ya está desconectado de
		// la ventana y no puede recibir estos eventos de todos modos.
		//
		// CAUSA RAIZ REAL investigada desde cero 2026-08-06 (reporte del usuario: "las zonas de
		// interferencia siguen sin funcionar"): "zonas de interferencia" son las zonaUno/Dos/
		// TresIzq/Der (los hover que disparan este mismo metodo), no contenido visual del gif en
		// si. juegoTerminado() SI debia bloquear esto en alGanar()/rendirse() (ahi pnlJuego deja
		// de estar en pantalla de inmediato, asi que no importaba) pero tambien bloqueaba
		// alPerder(), donde pnlJuego SIGUE visible ~20s completos durante todo el Game Over --
		// contradiciendo el comentario original mas abajo ("el movimiento de camara... sigue
		// funcionando exactamente igual") que documentaba la intencion real pero el codigo nunca
		// la cumplia. Quitado de aqui -- el bloqueo de puertas/luces/tablet durante Game Over ya
		// esta cubierto por sus propios guards en ControllerJuego, independientes de este.
		if (interfaz.isTransicionTabletActiva()) {
			return;
		}

		izquierda = new Timer(10, e -> {

			xlbl = pnlJuego.getLblImgOficina().getX();
			int ylbl = pnlJuego.getLblImgOficina().getY();
			xOverlay=pnlJuego.getLblOverlay().getX();
			int yOverlay=pnlJuego.getLblOverlay().getY();
			int ypnl = pnlJuego.getPanelIzq().getY();
			int yPuerta=pnlJuego.getLblPuertaDer().getY();

			if (xlbl >= 0) {
				izquierda.stop();
			} else {
				xlbl+=v;
				xpnlIzq+=v;
				xpnlDer+=v;
				xPDer+=v;
				xPIzq+=v;
				
				if(xOverlay!=0) {
					xOverlay+=v;
				}
			}

			pnlJuego.getPanelIzq().setLocation(xpnlIzq, ypnl);

			pnlJuego.getPanelDer().setLocation(xpnlDer, ypnl);
			pnlJuego.getLblPuertaIzq().setLocation(xPIzq, yPuerta);
			pnlJuego.getLblPuertaDer().setLocation(xPDer, yPuerta);
			pnlJuego.getLblImgOficina().setLocation(xlbl, ylbl);
			pnlJuego.getLblOverlay().setLocation(xOverlay, yOverlay);


		});

		izquierda.start();
	}

	public void derecha(int v) {

		// Ver el comentario en izquierda() -- mismo bloqueo (solo tablet, no juegoTerminado).
		if (interfaz.isTransicionTabletActiva()) {
			return;
		}

		derecha = new Timer(10, e -> {

			xlbl = pnlJuego.getLblImgOficina().getX();
			xOverlay=pnlJuego.getLblOverlay().getX();
			int yOverlay=pnlJuego.getLblOverlay().getY();
			int ylbl = pnlJuego.getLblImgOficina().getY();
			int ypnl = pnlJuego.getPanelDer().getY();
			int yPuerta=pnlJuego.getLblPuertaDer().getY();

			if (xlbl <= EscalarVista.getEscalaX(-602)) {
				derecha.stop();
			} else {
				xlbl-=v;
				xpnlIzq-=v;
				xpnlDer-=v;
				xPDer-=v;
				xPIzq-=v;
				
				if(xOverlay!=0) {
					xOverlay-=v;
				}
			}

			pnlJuego.getPanelIzq().setLocation(xpnlIzq, ypnl);

			pnlJuego.getPanelDer().setLocation(xpnlDer, ypnl);
			pnlJuego.getLblPuertaIzq().setLocation(xPIzq, yPuerta);
			pnlJuego.getLblPuertaDer().setLocation(xPDer, yPuerta);
			pnlJuego.getLblImgOficina().setLocation(xlbl, ylbl);
			pnlJuego.getLblOverlay().setLocation(xOverlay, yOverlay);

		});

		derecha.start();
	}

	public void zonaUnoDer() {
		pnlJuego.getLblDerUno().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent evt) { derecha(5); }
			@Override
			public void mouseExited(MouseEvent evt) { if (derecha != null) derecha.stop(); }
			
		});
	}

	public void zonaUnoIzq() {
		pnlJuego.getLblIzqUno().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent evt) { izquierda(5); }
			@Override
			public void mouseExited(MouseEvent evt) { if (izquierda != null) izquierda.stop(); }

		});
	}

	public void zonaDosDer() {
		pnlJuego.getLblDerDos().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent evt) { derecha(13); }
			@Override
			public void mouseExited(MouseEvent evt) { if (derecha != null) derecha.stop(); }
			
		});
	}

	public void zonaDosIzq() {
		pnlJuego.getLblIzqDos().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent evt) { izquierda(13); }
			@Override
			public void mouseExited(MouseEvent evt) { if (izquierda != null) izquierda.stop(); }
			
		});
	}

	public void zonaTresDer() {
		pnlJuego.getLblDerTres().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent evt) { derecha(20); }
			@Override
			public void mouseExited(MouseEvent evt) { if (derecha != null) derecha.stop(); }
			
		});
	}

	public void zonaTresIzq() {
		pnlJuego.getLblIzqTres().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent evt) { izquierda(20); }
			@Override
			public void mouseExited(MouseEvent evt) { if (izquierda != null) izquierda.stop(); }
			
		});
	}
}