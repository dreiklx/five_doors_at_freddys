package com.fdaf.mvc.controllers;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.fdaf.mvc.views.frames.VistaPrincipal;
import com.fdaf.mvc.views.frames.pnl.PnlMenu;
import com.fdaf.mvc.views.frames.pnl.PnlOpciones;
import com.fdaf.mvc.views.multimedia.Sonido;
import com.fdaf.util.EscalarVista;
import com.fdaf.util.Idioma;
import com.fdaf.util.PreferenciasJuego;

public class ControllerMenu {

	private PnlMenu menu;
	private PnlOpciones opciones;
	private VistaPrincipal vp;
	private Sonido musicaMenu;

	public ControllerMenu() {

	    menu = new PnlMenu();
	    opciones = new PnlOpciones();
	    vp = new VistaPrincipal();

	    EscalarVista.adaptarMenu(vp, menu);
	    EscalarVista.adaptarOpciones(vp, opciones);

	    ponerMusicaMenu();
	}

	public void init() {
		vp.setContenido(menu);
		botones();
		encima();
		vp.init();
	}
	
	private void ponerMusicaMenu() {
	    musicaMenu = new Sonido("fondo/musica_fondo_menu.wav");
	    musicaMenu.loop();
	}

	//varaibles inciializadas para el sonido de botones_menu
	private Sonido entrarBotones = new Sonido("botones/botones_menu.wav");
	public void botones() {
		menu.getBtnOpciones().addActionListener(e -> {
			vp.setContenido(opciones);
			opciones();
		});

		// Cada vez que se presiona "Empezar" se crea una ControllerInterfaz
		//  NUEVA: paneles nuevos (sin listeners viejos),
		// ControllerJuego nuevo (sin vidas/coleccionables/árbol del intento
		// anterior), y los objetos de la partida previa quedan sin
		// referencias para que el garbage collector los elimine (incluidos
		// sus timers ya detenidos).
		
		// Sonido de botones_menu al entrar a los botones 
		menu.getBtnEmpezar().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
			    entrarBotones.play();
			
			}
		});
		menu.getBtnOpciones().addMouseListener(new MouseAdapter() {
		    @Override
		    public void mouseEntered(MouseEvent e) {
		        entrarBotones.play();
		    }
		});
		
		opciones.getBtnAtras().addMouseListener(new MouseAdapter() {
			 @Override
			 public void mouseEntered(MouseEvent e) {
			    entrarBotones.play();
			}
		});
		
		opciones.getBtnSalir().addMouseListener(new MouseAdapter() {
			 @Override
			 public void mouseEntered(MouseEvent e) {
			    entrarBotones.play();
			}
		});

		// Selección de idioma: guarda la preferencia en memoria y
		// actualiza el texto que la muestra en el panel de Opciones.
		opciones.getBtnEspanol().addActionListener(e -> {
			PreferenciasJuego.idiomaSeleccionado = Idioma.ESPANOL;
			opciones.getLblIdiomaSeleccionado().setText("Español");
		});

		opciones.getBtnIngles().addActionListener(e -> {
			PreferenciasJuego.idiomaSeleccionado = Idioma.INGLES;
			opciones.getLblIdiomaSeleccionado().setText("Inglés");
		});

		// selector de dificultad/noche, mismo patrón que idioma.
		com.fdaf.mvc.models.juego.Noche[] noches = com.fdaf.mvc.models.juego.Noche.values();
		for (int i = 0; i < opciones.getBtnsNoche().length; i++) {
			com.fdaf.mvc.models.juego.Noche noche = noches[i];
			String etiqueta = "Noche " + (i + 1);
			opciones.getBtnsNoche()[i].addActionListener(e -> {
				PreferenciasJuego.nocheSeleccionada = noche;
				opciones.getLblNocheSeleccionada().setText(etiqueta);
			});
		}
		
		// al darle al boton empezar se detiene la musica del menu
		menu.getBtnEmpezar().addActionListener(e -> {

		    if (musicaMenu != null) {
		        musicaMenu.stop();
		    }

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