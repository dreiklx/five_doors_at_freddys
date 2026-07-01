package com.fdaf.mvc.controllers;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import com.fdaf.mvc.models.animatronicos.Animatronico;
import com.fdaf.mvc.models.coleccionables.Coleccionable;
import com.fdaf.mvc.views.frames.VistaPrincipal;
import com.fdaf.mvc.views.frames.pnl.PnlJuego;
import com.fdaf.mvc.views.frames.pnl.PnlMenu;
import com.fdaf.mvc.views.frames.pnl.PnlTableta;
import com.fdaf.mvc.views.multimedia.Sonido;
import com.fdaf.util.CargarImagenes;
import com.fdaf.util.EscalarVista;
import com.fdaf.util.Idioma;
import com.fdaf.util.PreferenciasJuego;

public class ControllerInterfaz implements JuegoListener {

	private Sonido sonido;
	private Sonido sonidoJuego1;
	private Sonido sonidoJuego2;
	
	private Timer timerSonido;
	
	private PnlJuego pnlJuego;
	private PnlTableta pnlTableta;

	private ControllerJuego controllerJuego;
	private ControllerCamara controllerCamara;

	private EscalarVista.GifEscalado gifOficinaNormal;
	private EscalarVista.GifEscalado gifOficinaLuzIzq;
	private EscalarVista.GifEscalado gifOficinaLuzDer;

	private Sonido sonidoLlamada;
	private Timer timerLlamada;
	private boolean llamadaColgada = false;

	
	
	//sonidos precargados
	
	private Sonido respiracion=new Sonido("varios/respiracion_agitada.wav");
	public ControllerInterfaz() {
		pnlJuego = new PnlJuego();
		pnlTableta = new PnlTableta();

		controllerJuego = new ControllerJuego();
		controllerJuego.setListener(this);
		controllerJuego.init();

		controllerCamara = new ControllerCamara(
		        pnlJuego,
		        pnlTableta,
		        this
		);
	}

	public void init(VistaPrincipal vp, PnlMenu menu) {
	    controllerCamara.init(vp, menu);

	    sonidoJuego1 = new Sonido("fondo/sonido_ambiente_oficina.wav");
	    sonidoJuego1.loop();

	    timerSonido = new Timer(3000, e -> {
	        sonidoJuego2 = new Sonido("fondo/sonido_fondo_oficina.wav");
	        sonidoJuego2.loop();
	        sonidoJuego2.setVolumen(-9.0f);
	    });

	    timerSonido.setRepeats(false);
	    timerSonido.start();

	    prepararInstanciasPreEscaladas();

	    conectarPuertas();
	    conectarLuces();
	    narizFreddy();
	    mutecall();

	    alActualizarVidas(controllerJuego.getVidas());

	    iniciarLlamada();
	}

	private void prepararInstanciasPreEscaladas() {
		int anchoOficina = pnlJuego.getLblImgOficina().getWidth();
		int altoOficina = pnlJuego.getLblImgOficina().getHeight();

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
	 * ==== Llamada telefónica ====
	 */

	private void iniciarLlamada() {

		llamadaColgada = false;

		sonidoLlamada = new Sonido("phoneguy/contestar_telefono.wav");
		sonidoLlamada.play();

		//aoparece cuando empieza la llamada.
		pnlJuego.getLblMuteCall().setVisible(true);

		long duracionMs = sonidoLlamada.getDuracionMs();
		if (duracionMs <= 0) duracionMs = 2000;

		timerLlamada = new Timer((int) duracionMs, e -> {
			timerLlamada.stop();

			if (llamadaColgada) return;

			String archivo = (PreferenciasJuego.idiomaSeleccionado == Idioma.INGLES)
					? "phoneguy/phoneguy_ingles.wav"
					: "phoneguy/phoneguy_espanol.wav";

			sonidoLlamada = new Sonido(archivo);
			sonidoLlamada.play();

			long duracionLlamadaMs = sonidoLlamada.getDuracionMs();
			if (duracionLlamadaMs <= 0) duracionLlamadaMs = 3000;

			//2. si termina sola (nadie colgó), se oculta el botón.
			timerLlamada = new Timer((int) duracionLlamadaMs, ev -> {
				timerLlamada.stop();
				if (!llamadaColgada) {
					pnlJuego.getLblMuteCall().setVisible(false);
				}
			});
			timerLlamada.setRepeats(false);
			timerLlamada.start();
		});
		timerLlamada.setRepeats(false);
		timerLlamada.start();
	}

	private void colgarLlamada() {

		llamadaColgada = true;

		if (timerLlamada != null && timerLlamada.isRunning()) {
			timerLlamada.stop();
		}

		if (sonidoLlamada != null) {
			sonidoLlamada.stop();
		}

		//1. colgado manual, se oculta
		pnlJuego.getLblMuteCall().setVisible(false);
	}

	private void mutecall() {
		pnlJuego.getLblMuteCall().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				colgarLlamada();
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
	public void alCerrarPuerta(String lado) {
		if ("izq".equals(lado)) {
			pnlJuego.getBotonIzq().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.btnIzq0,
					pnlJuego.getBotonIzq().getWidth(), pnlJuego.getBotonIzq().getHeight()));
			pnlJuego.getLblPuertaIzq().setIcon(new EscalarVista.GifEscalado(
					CargarImagenes.puertaIzq0,
					pnlJuego.getLblPuertaIzq().getWidth(),
					pnlJuego.getLblPuertaIzq().getHeight()
			));
		} else {
			pnlJuego.getBotonDer().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.btnDer0,
					pnlJuego.getBotonDer().getWidth(), pnlJuego.getBotonDer().getHeight()));
			pnlJuego.getLblPuertaDer().setIcon(new EscalarVista.GifEscalado(
					CargarImagenes.puertaDer0,
					pnlJuego.getLblPuertaDer().getWidth(),
					pnlJuego.getLblPuertaDer().getHeight()
			));
		}
		pnlJuego.revalidate();
		pnlJuego.repaint();
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
	public void alApagarLuz(String lado) {
		if ("izq".equals(lado)) {
			pnlJuego.getLuzIzq().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.luzIzq0,
					pnlJuego.getLuzIzq().getWidth(), pnlJuego.getLuzIzq().getHeight()));
		} else {
			pnlJuego.getLuzDer().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.luzDer0,
					pnlJuego.getLuzDer().getWidth(), pnlJuego.getLuzDer().getHeight()));
		}

		pnlJuego.getLblImgOficina().setIcon(gifOficinaNormal);

		pnlJuego.revalidate();
		pnlJuego.repaint();
	}
	
	@Override
	public void alRevelarAnimatronico(String lado, Animatronico animatronico) {
		sonido = new Sonido("animatronicos/jumpscare.wav");
		sonido.play();
		
	    String nombre = animatronico.name();
	    String capitalizado = nombre.charAt(0) + nombre.substring(1).toLowerCase();
	    String ruta = "/gifs/jumpscares/Jumpscare" + capitalizado + ".gif";

	    mostrarEnOverlay(ruta, 1, lado);

	    Timer timerRespiracion = new Timer(3000, e -> {
	        respiracion.play();
	    });

	    timerRespiracion.setRepeats(false);
	    timerRespiracion.start();
	}

	@Override
	public void alRevelarColeccionable(String lado, Coleccionable coleccionable) {
		sonido = new Sonido("coleccionables/coleccionable_encontrado.wav");
		sonido.play();
		
		String ruta = "/images/" + coleccionable.getArchivoImagen();
		mostrarEnOverlay(ruta, 0, lado);
	}

	@Override
	public void alRevelarNada(String lado) {
	}

	private void mostrarEnOverlay(String ruta, int c, String lado) {
		URL recurso = getClass().getResource(ruta);
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
		URL recurso = getClass().getResource(ruta);
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

			pnlJuego.getLblPuertaIzq().setIcon(new EscalarVista.GifEscalado(
					CargarImagenes.puertaIzq0,
					pnlJuego.getLblPuertaIzq().getWidth(),
					pnlJuego.getLblPuertaIzq().getHeight()
			));
			pnlJuego.getLblImgOficina().setIcon(gifOficinaNormal);

			pnlJuego.getBotonIzq().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.btnIzq0,
					pnlJuego.getBotonIzq().getWidth(), pnlJuego.getBotonIzq().getHeight()));
		} else {
			pnlJuego.getLuzDer().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.luzDer0,
					pnlJuego.getLuzDer().getWidth(), pnlJuego.getLuzDer().getHeight()));

			pnlJuego.getLblPuertaDer().setIcon(new EscalarVista.GifEscalado(
					CargarImagenes.puertaDer0,
					pnlJuego.getLblPuertaDer().getWidth(),
					pnlJuego.getLblPuertaDer().getHeight()
			));

			pnlJuego.getLblImgOficina().setIcon(gifOficinaNormal);

			pnlJuego.getBotonDer().setIcon(EscalarVista.getImagenEscalada(CargarImagenes.btnDer0,
					pnlJuego.getBotonDer().getWidth(), pnlJuego.getBotonDer().getHeight()));
		}
		pnlJuego.revalidate();
		pnlJuego.repaint();
	}

	@Override
	public void alActualizarVidas(int vidas) {

		ImageIcon icono;

		switch (vidas) {
			case 4: icono = CargarImagenes.bateria4; break;
			case 3: icono = CargarImagenes.bateria3; break;
			case 2: icono = CargarImagenes.bateria2; break;
			case 1: icono = CargarImagenes.bateria1; break;
			default: icono = CargarImagenes.bateria0; break; // 0 (o menos, por seguridad)
		}

		pnlJuego.getLblBateria().setIcon(EscalarVista.getImagenEscalada(icono,
				pnlJuego.getLblBateria().getWidth(), pnlJuego.getLblBateria().getHeight()));

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

	public void narizFreddy() {
	    pnlJuego.getLblNariz().addMouseListener(new MouseAdapter() {
	        @Override
	        public void mouseClicked(MouseEvent e) {
	            Sonido sonido = new Sonido("varios/nariz_freddy.wav");
	            sonido.play();
	        }
	    });
	}
	public void detenerSonidosJuego() {
	    System.out.println("DETENIENDO SONIDOS");

	    if (sonidoJuego1 != null) {
	        sonidoJuego1.stop();
	    }

	    if (sonidoJuego2 != null) {
	        sonidoJuego2.stop();
	    }

	    if (respiracion != null) {
	        respiracion.stop();
	    }
	    if (timerSonido != null) {
	        timerSonido.stop();
	    }

	    colgarLlamada();
	}
	
	public PnlJuego getPnlJuego() {
		return pnlJuego;
	}

	public PnlTableta getPnlTableta() {
		return pnlTableta;
	}
}