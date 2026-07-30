package com.fdaf.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.fdaf.mvc.models.coleccionables.TipoColeccionable;
import com.fdaf.mvc.views.frames.VistaPrincipal;
import com.fdaf.mvc.views.frames.pnl.PnlConfirmacion;
import com.fdaf.mvc.views.frames.pnl.PnlGameOver;
import com.fdaf.mvc.views.frames.pnl.PnlIdioma;
import com.fdaf.mvc.views.frames.pnl.PnlWarning;
import com.fdaf.mvc.views.frames.pnl.PnlWin;
import javafx.embed.swing.JFXPanel;
import com.fdaf.mvc.views.frames.pnl.PnlJuego;
import com.fdaf.mvc.views.frames.pnl.PnlMenu;
import com.fdaf.mvc.views.frames.pnl.PnlNochePersonalizada;
import com.fdaf.mvc.views.frames.pnl.PnlOpciones;
import com.fdaf.mvc.views.frames.pnl.PnlTableta;
import com.fdaf.mvc.views.frames.pnl.PnlVolumen;



public class EscalarVista {



	public static ImageIcon getImagenEscalada(ImageIcon archivo, int ancho, int alto) {

		ImageIcon icon = archivo;
		Image img = icon.getImage();
		Image imgEscalada = img.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);

		return new ImageIcon(imgEscalada);
	}



	public static int getEscalaX(double componente){
		Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();

		int ancho= pantalla.width;
		double escalaX = (double) ancho / 1599;
		
		return (int)(componente * escalaX);
	}



	public static int getEscalaY(double componente){
		Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();

		int alto = pantalla.height; 
		double escalaY = (double) alto / 900;
		
		return (int)(componente * escalaY);
	}



	public static void adaptarOpciones(VistaPrincipal vp, PnlOpciones opciones) {
		Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
		int alto = pantalla.height; 
		int ancho = pantalla.width;

		JLabel lblTitulo = opciones.getLblTitulo();
		JButton btnIdioma = opciones.getBtnIdioma();
		JButton btnCustomNight = opciones.getBtnCustomNight();
		JButton btnVolumen = opciones.getBtnVolumen();
		JButton btnAtras = opciones.getBtnAtras();
		JButton btnSalir = opciones.getBtnSalir();
		JLabel lblFlecha = opciones.getLbl();

		opciones.setBounds(0, 0, ancho, alto);
		opciones.getPnlOpciones().setBounds(0, 0, ancho, alto);

		opciones.getLblTitulo().setBounds(
				getEscalaX(lblTitulo.getX()), getEscalaY(lblTitulo.getY()),
				getEscalaX(lblTitulo.getWidth()), getEscalaY(lblTitulo.getHeight()));

		opciones.getBtnIdioma().setBounds(
				getEscalaX(btnIdioma.getX()), getEscalaY(btnIdioma.getY()),
				getEscalaX(btnIdioma.getWidth()), getEscalaY(btnIdioma.getHeight()));

		opciones.getBtnCustomNight().setBounds(
				getEscalaX(btnCustomNight.getX()), getEscalaY(btnCustomNight.getY()),
				getEscalaX(btnCustomNight.getWidth()), getEscalaY(btnCustomNight.getHeight()));

		opciones.getBtnVolumen().setBounds(
				getEscalaX(btnVolumen.getX()), getEscalaY(btnVolumen.getY()),
				getEscalaX(btnVolumen.getWidth()), getEscalaY(btnVolumen.getHeight()));

		opciones.getBtnAtras().setBounds(
				getEscalaX(btnAtras.getX()), getEscalaY(btnAtras.getY()),
				getEscalaX(btnAtras.getWidth()), getEscalaY(btnAtras.getHeight()));

		opciones.getBtnSalir().setBounds(
				getEscalaX(btnSalir.getX()), getEscalaY(btnSalir.getY()),
				getEscalaX(btnSalir.getWidth()), getEscalaY(btnSalir.getHeight()));

		opciones.getLbl().setSize(getEscalaX(lblFlecha.getWidth()), getEscalaY(lblFlecha.getHeight()));
	}

	public static void adaptarVolumen(VistaPrincipal vp, PnlVolumen pantalla) {
		Dimension pantallaFisica = Toolkit.getDefaultToolkit().getScreenSize();
		int alto = pantallaFisica.height;
		int ancho = pantallaFisica.width;

		JLabel lblTitulo = pantalla.getLblTitulo();
		JLabel lblNivel = pantalla.getLblNivel();
		JButton btnSubir = pantalla.getBtnSubir();
		JButton btnBajar = pantalla.getBtnBajar();
		JButton btnAtras = pantalla.getBtnAtras();
		JLabel lblFlecha = pantalla.getLbl();

		pantalla.setBounds(0, 0, ancho, alto);
		pantalla.getPnlVolumen().setBounds(0, 0, ancho, alto);

		pantalla.getLblTitulo().setBounds(
				getEscalaX(lblTitulo.getX()), getEscalaY(lblTitulo.getY()),
				getEscalaX(lblTitulo.getWidth()), getEscalaY(lblTitulo.getHeight()));

		pantalla.getLblNivel().setBounds(
				getEscalaX(lblNivel.getX()), getEscalaY(lblNivel.getY()),
				getEscalaX(lblNivel.getWidth()), getEscalaY(lblNivel.getHeight()));

		pantalla.getBtnSubir().setBounds(
				getEscalaX(btnSubir.getX()), getEscalaY(btnSubir.getY()),
				getEscalaX(btnSubir.getWidth()), getEscalaY(btnSubir.getHeight()));

		pantalla.getBtnBajar().setBounds(
				getEscalaX(btnBajar.getX()), getEscalaY(btnBajar.getY()),
				getEscalaX(btnBajar.getWidth()), getEscalaY(btnBajar.getHeight()));

		pantalla.getBtnAtras().setBounds(
				getEscalaX(btnAtras.getX()), getEscalaY(btnAtras.getY()),
				getEscalaX(btnAtras.getWidth()), getEscalaY(btnAtras.getHeight()));

		pantalla.getLbl().setSize(getEscalaX(lblFlecha.getWidth()), getEscalaY(lblFlecha.getHeight()));
	}
	
	public static void adaptarNochePersonalizada(VistaPrincipal vp, PnlNochePersonalizada pantalla) {
		Dimension pantallaFisica = Toolkit.getDefaultToolkit().getScreenSize();
		int alto = pantallaFisica.height;
		int ancho = pantallaFisica.width;

		JLabel lblTitulo = pantalla.getLblTitulo();
		JLabel lblNocheSeleccionada = pantalla.getLblNocheSeleccionada();
		JButton btnAtras = pantalla.getBtnAtras();
		JLabel lblFlecha = pantalla.getLbl();

		pantalla.setBounds(0, 0, ancho, alto);
		pantalla.getPnlNochePersonalizada().setBounds(0, 0, ancho, alto);

		pantalla.getLblTitulo().setBounds(
				getEscalaX(lblTitulo.getX()), getEscalaY(lblTitulo.getY()),
				getEscalaX(lblTitulo.getWidth()), getEscalaY(lblTitulo.getHeight()));

		pantalla.getLblNocheSeleccionada().setBounds(
				getEscalaX(lblNocheSeleccionada.getX()), getEscalaY(lblNocheSeleccionada.getY()),
				getEscalaX(lblNocheSeleccionada.getWidth()), getEscalaY(lblNocheSeleccionada.getHeight()));

		for (JButton btnNoche : pantalla.getBtnsNoche()) {
			btnNoche.setBounds(
					getEscalaX(btnNoche.getX()), getEscalaY(btnNoche.getY()),
					getEscalaX(btnNoche.getWidth()), getEscalaY(btnNoche.getHeight()));
		}

		pantalla.getBtnAtras().setBounds(
				getEscalaX(btnAtras.getX()), getEscalaY(btnAtras.getY()),
				getEscalaX(btnAtras.getWidth()), getEscalaY(btnAtras.getHeight()));

		pantalla.getLbl().setSize(getEscalaX(lblFlecha.getWidth()), getEscalaY(lblFlecha.getHeight()));
	}
	
	public static void adaptarIdioma(VistaPrincipal vp, PnlIdioma pantalla) {
		Dimension pantallaFisica = Toolkit.getDefaultToolkit().getScreenSize();
		int alto = pantallaFisica.height;
		int ancho = pantallaFisica.width;

		JLabel lblTitulo = pantalla.getLblTitulo();
		JLabel lblIdiomaActual = pantalla.getLblIdiomaActual();
		JButton btnEspanol = pantalla.getBtnEspanol();
		JButton btnIngles = pantalla.getBtnIngles();
		JButton btnAtras = pantalla.getBtnAtras();
		JLabel lblFlecha = pantalla.getLbl();

		pantalla.setBounds(0, 0, ancho, alto);
		pantalla.getPnlIdioma().setBounds(0, 0, ancho, alto);

		pantalla.getLblTitulo().setBounds(
				getEscalaX(lblTitulo.getX()), getEscalaY(lblTitulo.getY()),
				getEscalaX(lblTitulo.getWidth()), getEscalaY(lblTitulo.getHeight()));

		pantalla.getLblIdiomaActual().setBounds(
				getEscalaX(lblIdiomaActual.getX()), getEscalaY(lblIdiomaActual.getY()),
				getEscalaX(lblIdiomaActual.getWidth()), getEscalaY(lblIdiomaActual.getHeight()));

		pantalla.getBtnEspanol().setBounds(
				getEscalaX(btnEspanol.getX()), getEscalaY(btnEspanol.getY()),
				getEscalaX(btnEspanol.getWidth()), getEscalaY(btnEspanol.getHeight()));

		pantalla.getBtnIngles().setBounds(
				getEscalaX(btnIngles.getX()), getEscalaY(btnIngles.getY()),
				getEscalaX(btnIngles.getWidth()), getEscalaY(btnIngles.getHeight()));

		pantalla.getBtnAtras().setBounds(
				getEscalaX(btnAtras.getX()), getEscalaY(btnAtras.getY()),
				getEscalaX(btnAtras.getWidth()), getEscalaY(btnAtras.getHeight()));

		pantalla.getLbl().setSize(getEscalaX(lblFlecha.getWidth()), getEscalaY(lblFlecha.getHeight()));
	}
	public static void adaptarMenu(VistaPrincipal vp, PnlMenu menu) {
		Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
		int alto = pantalla.height; 
		int ancho = pantalla.width;

		JButton btnContinuar = menu.getBtnContinuar();
		JLabel lblNocheActual = menu.getLblNocheActual();
		JButton btnNuevaPartida = menu.getBtnNuevaPartida();
		JButton btnOpciones = menu.getBtnOpciones();

		PnlConfirmacion confirmacion = menu.getPnlConfirmacion();
		JLabel lblMensaje = confirmacion.getLblMensaje();
		JButton btnConfirmar = confirmacion.getBtnConfirmar();
		JButton btnCancelar = confirmacion.getBtnCancelar();

		vp.setBounds(0, 0, ancho, alto);
		menu.setBounds(0, 0, ancho, alto);
		menu.getPnlMenu().setBounds(0, 0, ancho, alto);
		menu.getLblMenu().setBounds(0, 0, ancho, alto);

		menu.getBtnContinuar().setBounds(
				getEscalaX(btnContinuar.getX()), getEscalaY(btnContinuar.getY()),
				getEscalaX(btnContinuar.getWidth()), getEscalaY(btnContinuar.getHeight()));

		menu.getLblNocheActual().setBounds(
				getEscalaX(lblNocheActual.getX()), getEscalaY(lblNocheActual.getY()),
				getEscalaX(lblNocheActual.getWidth()), getEscalaY(lblNocheActual.getHeight()));

		menu.getBtnNuevaPartida().setBounds(
				getEscalaX(btnNuevaPartida.getX()), getEscalaY(btnNuevaPartida.getY()),
				getEscalaX(btnNuevaPartida.getWidth()), getEscalaY(btnNuevaPartida.getHeight()));

		menu.getBtnOpciones().setBounds(
				getEscalaX(btnOpciones.getX()), getEscalaY(btnOpciones.getY()), 
				getEscalaX(btnOpciones.getWidth()), getEscalaY(btnOpciones.getHeight()));

		confirmacion.setBounds(0, 0, ancho, alto);

		confirmacion.getLblMensaje().setBounds(
				getEscalaX(lblMensaje.getX()), getEscalaY(lblMensaje.getY()),
				getEscalaX(lblMensaje.getWidth()), getEscalaY(lblMensaje.getHeight()));

		confirmacion.getBtnConfirmar().setBounds(
				getEscalaX(btnConfirmar.getX()), getEscalaY(btnConfirmar.getY()),
				getEscalaX(btnConfirmar.getWidth()), getEscalaY(btnConfirmar.getHeight()));

		confirmacion.getBtnCancelar().setBounds(
				getEscalaX(btnCancelar.getX()), getEscalaY(btnCancelar.getY()),
				getEscalaX(btnCancelar.getWidth()), getEscalaY(btnCancelar.getHeight()));

		// La flecha se reposiciona dinámicamente en cada hover, pero su
		// TAMAÑO nunca pasaba por el sistema de escalado -- se captura el
		// tamaño base real (el que PnlMenu calculó con FontMetrics) antes
		// de mutarlo, mismo patrón que el resto de este método.
		JLabel lblFlechaMenu = menu.getLbl();
		menu.getLbl().setSize(getEscalaX(lblFlechaMenu.getWidth()), getEscalaY(lblFlechaMenu.getHeight()));

		JLabel lblFlechaConfirmacion = confirmacion.getLblFlecha();
		confirmacion.getLblFlecha().setSize(
				getEscalaX(lblFlechaConfirmacion.getWidth()), getEscalaY(lblFlechaConfirmacion.getHeight()));

		JLabel lblVersion = menu.getLblVersion();
		JLabel lblCreditos = menu.getLblCreditos();

		menu.getLblVersion().setBounds(
				getEscalaX(lblVersion.getX()), getEscalaY(lblVersion.getY()),
				getEscalaX(lblVersion.getWidth()), getEscalaY(lblVersion.getHeight()));

		menu.getLblCreditos().setBounds(
				getEscalaX(lblCreditos.getX()), getEscalaY(lblCreditos.getY()),
				getEscalaX(lblCreditos.getWidth()), getEscalaY(lblCreditos.getHeight()));
	}
	
	public static void adaptarTablet(VistaPrincipal vp, PnlTableta tablet) {
		Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
		int alto = pantalla.height; 
		int ancho = pantalla.width;
		
		JProgressBar barra = tablet.getPbarRendirse();
		JLabel cerrar = tablet.getLblTabletCerrar();
		JPanel pnlColeccionables = tablet.getPnlColeccionables();
		
		tablet.setBounds(0, 0, ancho, alto);
		tablet.getPanel().setBounds(0, 0, ancho, alto);
		
		tablet.getLblCopyBateria().setBounds(
				getEscalaX(1142), getEscalaY(91),
				getEscalaX(83), getEscalaY(40));
		
		tablet.getLblCopyBateria().setIcon(getImagenEscalada(CargarImagenes.bateria4,
				tablet.getLblCopyBateria().getWidth(), tablet.getLblCopyBateria().getHeight()));
		
		
		tablet.getLblCopyPowerLeft().setBounds(
				getEscalaX(1132), getEscalaY(52),
				getEscalaX(182), getEscalaY(32));
		tablet.getLblCopyPowerLeft().setIcon(getImagenEscalada(CargarImagenes.powerLeft,
				tablet.getLblCopyPowerLeft().getWidth(), tablet.getLblCopyPowerLeft().getHeight()));

		

		tablet.getPbarRendirse().setBounds(
				getEscalaX(barra.getX()), getEscalaY(barra.getY()),
				getEscalaX(barra.getWidth()), getEscalaY(barra.getHeight()));

		tablet.getLblTabletCerrar().setBounds(
				getEscalaX(cerrar.getX()), getEscalaY(cerrar.getY()),
				getEscalaX(cerrar.getWidth()), getEscalaY(cerrar.getHeight()));

		tablet.getLblTabletCerrar().setIcon(getImagenEscalada(CargarImagenes.barraTableta,
				tablet.getLblTabletCerrar().getWidth(), tablet.getLblTabletCerrar().getHeight()));

		tablet.getPnlColeccionables().setBounds(
				getEscalaX(pnlColeccionables.getX()), getEscalaY(pnlColeccionables.getY()),
				getEscalaX(pnlColeccionables.getWidth()), getEscalaY(pnlColeccionables.getHeight()));
		////////////////////////////////ESCALAR TODOS LOS JLABEL DEL INVENTARIO///////////////////////////////////////////////////
		JLabel pChica=tablet.getPELUCHE_CHICA();
		tablet.getPELUCHE_CHICA().setBounds(getEscalaX(pChica.getX()),getEscalaY(pChica.getY()), getEscalaX(pChica.getWidth()), getEscalaY(pChica.getHeight()));
		
		JLabel pFreddy=tablet.getPELUCHE_FREDDY();
		tablet.getPELUCHE_FREDDY().setBounds(getEscalaX(pFreddy.getX()),getEscalaY(pFreddy.getY()), getEscalaX(pFreddy.getWidth()), getEscalaY(pFreddy.getHeight()));
		
		JLabel pBonnie=tablet.getPELUCHE_BONNIE();
		tablet.getPELUCHE_BONNIE().setBounds(getEscalaX(pBonnie.getX()),getEscalaY(pBonnie.getY()), getEscalaX(pBonnie.getWidth()), getEscalaY(pBonnie.getHeight()));
		
		JLabel pFoxy=tablet.getPELUCHE_FOXY();
		tablet.getPELUCHE_FOXY().setBounds(getEscalaX(pFoxy.getX()),getEscalaY(pFoxy.getY()), getEscalaX(pFoxy.getWidth()), getEscalaY(pFoxy.getHeight()));
		
		JLabel pGolden=tablet.getPELUCHE_GOLDEN_FREDDY();
		tablet.getPELUCHE_GOLDEN_FREDDY().setBounds(getEscalaX(pGolden.getX()),getEscalaY(pGolden.getY()), getEscalaX(pGolden.getWidth()), getEscalaY(pGolden.getHeight()));
		
		JLabel cupcake=tablet.getCUPCAKE();
		tablet.getCUPCAKE().setBounds(getEscalaX(cupcake.getX()),getEscalaY(cupcake.getY()), getEscalaX(cupcake.getWidth()), getEscalaY(cupcake.getHeight()));
		
		JLabel garfio=tablet.getGARFIO_FOXY();
		tablet.getGARFIO_FOXY().setBounds(getEscalaX(garfio.getX()),getEscalaY(garfio.getY()), getEscalaX(garfio.getWidth()), getEscalaY(garfio.getHeight()));
		
		JLabel guitar=tablet.getGUITARRA_BONNIE();
		tablet.getGUITARRA_BONNIE().setBounds(getEscalaX(guitar.getX()),getEscalaY(guitar.getY()), getEscalaX(guitar.getWidth()), getEscalaY(guitar.getHeight()));
		
		JLabel boy=tablet.getBALLOONBOY();
		tablet.getBALLOONBOY().setBounds(getEscalaX(boy.getX()),getEscalaY(boy.getY()), getEscalaX(boy.getWidth()), getEscalaY(boy.getHeight()));
//		
		JLabel microfono=tablet.getMICROFONO_FREDDY();
		tablet.getMICROFONO_FREDDY().setBounds(getEscalaX(microfono.getX()),getEscalaY(microfono.getY()), getEscalaX(microfono.getWidth()), getEscalaY(microfono.getHeight()));

		JLabel mostrar = tablet.getLblMostrar();
		tablet.getLblMostrar().setBounds(getEscalaX(mostrar.getX()), getEscalaY(mostrar.getY()),
				getEscalaX(mostrar.getWidth()), getEscalaY(mostrar.getHeight()));

		JLabel descripcion = tablet.getLblDescripcion();
		tablet.getLblDescripcion().setBounds(getEscalaX(descripcion.getX()), getEscalaY(descripcion.getY()),
				getEscalaX(descripcion.getWidth()), getEscalaY(descripcion.getHeight()));
//		
//		
//		
//		
//		
		
		
		
		
		
		
		

	}
	
	public static void adaptarJuego(VistaPrincipal vp, PnlJuego juego) {
		Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
		int alto = pantalla.height; 
		int ancho = pantalla.width;

		JLabel zonaUnoIzq = juego.getLblIzqUno();
		JLabel zonaDosIzq = juego.getLblIzqDos();
		JLabel zonaTresIzq = juego.getLblIzqTres();
		JLabel zonaUnoDer = juego.getLblDerUno();
		JLabel zonaDosDer = juego.getLblDerDos();
		JLabel zonaTresDer = juego.getLblDerTres();


		JLabel tableta = juego.getLblTabAbrir();
		JPanel pIzq = juego.getPanelIzq();
		JPanel pDer = juego.getPanelDer();
		JCheckBox bIzq = juego.getBotonIzq();
		JCheckBox lIzq = juego.getLuzIzq();
		JCheckBox bDer = juego.getBotonDer();
		JCheckBox lDer = juego.getLuzDer();
		JLabel muteCall = juego.getLblMuteCall();


				juego.setBounds(0, 0, ancho, alto);
				juego.getPnlComponentes().setBounds(0, 0, ancho, alto);
				
				juego.getLblImgOficina().setBounds(getEscalaX(-300), getEscalaY(-83), getEscalaX(2200), getEscalaY(1020));
				
				juego.getLblImgOficina().setIcon(new EscalarVista.GifEscalado(
						CargarImagenes.fondoJuego,
						juego.getLblImgOficina().getWidth(),
						juego.getLblImgOficina().getHeight()
				));

				juego.getLblAbanico().setBounds(
						getEscalaX(1093), getEscalaY(443),
						getEscalaX(166), getEscalaY(243));
				
				juego.getLblAbanico().setIcon(new EscalarVista.GifEscalado(
						CargarImagenes.abanico, 
						juego.getLblAbanico().getWidth(), 
						juego.getLblAbanico().getHeight()));
				juego.getLblPuertaDer().setBounds(
						getEscalaX(1435), getEscalaY(0), 
						getEscalaX(203), getEscalaY(900));
				juego.getLblPuertaDer().setIcon(new EscalarVista.GifEscalado(
						CargarImagenes.puertaDer0,
						juego.getLblPuertaDer().getWidth(),
						juego.getLblPuertaDer().getHeight()
				));
				
				juego.getLblPuertaIzq().setBounds(
						getEscalaX(-39), getEscalaY(0), 
						getEscalaX(203), getEscalaY(900));
				juego.getLblPuertaIzq().setIcon(new EscalarVista.GifEscalado(
						CargarImagenes.puertaIzq0,
						juego.getLblPuertaIzq().getWidth(),
						juego.getLblPuertaIzq().getHeight()
				));
				
				juego.getLblPowerLeft().setBounds(
						getEscalaX(57), getEscalaY(808),
						getEscalaX(194), getEscalaY(35));
				juego.getLblPowerLeft().setIcon(getImagenEscalada(CargarImagenes.powerLeft,
						juego.getLblPowerLeft().getWidth(), juego.getLblPowerLeft().getHeight()));
				
				juego.getLblBateria().setBounds(
						getEscalaX(67), getEscalaY(854),
						getEscalaX(89), getEscalaY(30));
				juego.getLblBateria().setIcon(getImagenEscalada(CargarImagenes.bateria4,
						juego.getLblBateria().getWidth(), juego.getLblBateria().getHeight()));

		juego.getLblNariz().setBounds(getEscalaX(965), getEscalaY(360),
										 getEscalaX(11), getEscalaY(8));

		juego.getLblMuteCall().setBounds(
				getEscalaX(muteCall.getX()), getEscalaY(muteCall.getY()),
				getEscalaX(muteCall.getWidth()), getEscalaY(muteCall.getHeight()));
		juego.getLblMuteCall().setIcon(getImagenEscalada(CargarImagenes.mutecall,
				juego.getLblMuteCall().getWidth(), juego.getLblMuteCall().getHeight()));

				
		juego.getLblTabAbrir().setBounds(
				getEscalaX(tableta.getX()), getEscalaY(tableta.getY()),
				getEscalaX(tableta.getWidth()), getEscalaY(tableta.getHeight()));
		juego.getLblTabAbrir().setIcon(getImagenEscalada(CargarImagenes.barraTableta,
				juego.getLblTabAbrir().getWidth(), juego.getLblTabAbrir().getHeight()));

		juego.getPanelIzq().setBounds(getEscalaX(pIzq.getX()), getEscalaY(pIzq.getY()), getEscalaX(pIzq.getWidth()), getEscalaY(pIzq.getHeight()));
		juego.getBotonIzq().setBounds(getEscalaX(bIzq.getX()), getEscalaY(bIzq.getY()), getEscalaX(bIzq.getWidth()), getEscalaY(bIzq.getHeight()));
		juego.getBotonIzq().setIcon(getImagenEscalada(CargarImagenes.btnIzq0, juego.getBotonIzq().getWidth(), juego.getBotonIzq().getHeight()));
		juego.getLuzIzq().setBounds(getEscalaX(lIzq.getX()), getEscalaY(lIzq.getY()), getEscalaX(lIzq.getWidth()), getEscalaY(lIzq.getHeight()));
		juego.getLuzIzq().setIcon(getImagenEscalada(CargarImagenes.luzIzq0, juego.getLuzIzq().getWidth(), juego.getLuzIzq().getHeight()));

		juego.getPanelDer().setBounds(getEscalaX(pDer.getX()), getEscalaY(pDer.getY()), getEscalaX(pDer.getWidth()), getEscalaY(pDer.getHeight()));
		juego.getBotonDer().setBounds(getEscalaX(bDer.getX()), getEscalaY(bDer.getY()), getEscalaX(bDer.getWidth()), getEscalaY(bDer.getHeight()));
		juego.getBotonDer().setIcon(getImagenEscalada(CargarImagenes.btnDer0, juego.getBotonDer().getWidth(), juego.getBotonDer().getHeight()));
		juego.getLuzDer().setBounds(getEscalaX(lDer.getX()), getEscalaY(lDer.getY()), getEscalaX(lDer.getWidth()), getEscalaY(lDer.getHeight()));
		juego.getLuzDer().setIcon(getImagenEscalada(CargarImagenes.luzDer0, juego.getLuzDer().getWidth(), juego.getLuzDer().getHeight()));

		juego.getLblIzqUno().setBounds(getEscalaX(zonaUnoIzq.getX()), getEscalaY(zonaUnoIzq.getY()), getEscalaX(zonaUnoIzq.getWidth()), getEscalaY(zonaUnoIzq.getHeight()));
		juego.getLblIzqDos().setBounds(getEscalaX(zonaDosIzq.getX()), getEscalaY(zonaDosIzq.getY()), getEscalaX(zonaDosIzq.getWidth()), getEscalaY(zonaDosIzq.getHeight()));
		juego.getLblIzqTres().setBounds(getEscalaX(zonaTresIzq.getX()), getEscalaY(zonaTresIzq.getY()), getEscalaX(zonaTresIzq.getWidth()), getEscalaY(zonaTresIzq.getHeight()));
		juego.getLblIzqTres().setIcon(getImagenEscalada(CargarImagenes.zonaTresIzq, juego.getLblIzqTres().getWidth(), juego.getLblIzqTres().getHeight()));

		juego.getLblDerUno().setBounds(getEscalaX(zonaUnoDer.getX()), getEscalaY(zonaUnoDer.getY()), getEscalaX(zonaUnoDer.getWidth()), getEscalaY(zonaUnoDer.getHeight()));
		juego.getLblDerDos().setBounds(getEscalaX(zonaDosDer.getX()), getEscalaY(zonaDosDer.getY()), getEscalaX(zonaDosDer.getWidth()), getEscalaY(zonaDosDer.getHeight()));
		juego.getLblDerTres().setBounds(getEscalaX(zonaTresDer.getX()), getEscalaY(zonaTresDer.getY()), getEscalaX(zonaTresDer.getWidth()), getEscalaY(zonaTresDer.getHeight()));
		juego.getLblDerTres().setIcon(getImagenEscalada(CargarImagenes.zonaTresDer, juego.getLblDerTres().getWidth(), juego.getLblDerTres().getHeight()));

		// Transición de tablet (abrir/cerrar): pantalla completa, mismo
		// lienzo base 1600x900 que el resto de la oficina.
		juego.getLblTabletTransicion().setBounds(getEscalaX(0), getEscalaY(0), getEscalaX(1600), getEscalaY(900));
		}

	public static void adaptarWarning(VistaPrincipal vp, PnlWarning pantalla) {
		Dimension pantallaFisica = Toolkit.getDefaultToolkit().getScreenSize();
		int alto = pantallaFisica.height;
		int ancho = pantallaFisica.width;
		pantalla.setBounds(0, 0, ancho, alto);
	}
	
	public static void adaptarGameOver(VistaPrincipal vp, PnlGameOver pantalla) {
		Dimension pantallaFisica = Toolkit.getDefaultToolkit().getScreenSize();
		int alto = pantallaFisica.height;
		int ancho = pantallaFisica.width;

		JLabel titulo = pantalla.getLblTitulo();
		JButton reintentar = pantalla.getBtnReintentar();
		JButton salir = pantalla.getBtnSalir();

		pantalla.setBounds(0, 0, ancho, alto);

		pantalla.getLblTitulo().setBounds(
				getEscalaX(titulo.getX()), getEscalaY(titulo.getY()),
				getEscalaX(titulo.getWidth()), getEscalaY(titulo.getHeight()));

		pantalla.getBtnReintentar().setBounds(
				getEscalaX(reintentar.getX()), getEscalaY(reintentar.getY()),
				getEscalaX(reintentar.getWidth()), getEscalaY(reintentar.getHeight()));

		pantalla.getBtnSalir().setBounds(
				getEscalaX(salir.getX()), getEscalaY(salir.getY()),
				getEscalaX(salir.getWidth()), getEscalaY(salir.getHeight()));
	}
	
	public static void adaptarWin(VistaPrincipal vp, PnlWin pantalla) {
		Dimension pantallaFisica = Toolkit.getDefaultToolkit().getScreenSize();
		int alto = pantallaFisica.height;
		int ancho = pantallaFisica.width;

		JFXPanel video = pantalla.getPanelVideo();
		JButton jugar = pantalla.getBtnJugarDeNuevo();
		JButton salir = pantalla.getBtnSalir();

		pantalla.setBounds(0, 0, ancho, alto);

		pantalla.getPanelVideo().setBounds(
				getEscalaX(video.getX()), getEscalaY(video.getY()),
				getEscalaX(video.getWidth()), getEscalaY(video.getHeight()));

		pantalla.getBtnJugarDeNuevo().setBounds(
				getEscalaX(jugar.getX()), getEscalaY(jugar.getY()),
				getEscalaX(jugar.getWidth()), getEscalaY(jugar.getHeight()));

		pantalla.getBtnSalir().setBounds(
				getEscalaX(salir.getX()), getEscalaY(salir.getY()),
				getEscalaX(salir.getWidth()), getEscalaY(salir.getHeight()));
	}
	
	public static class GifEscalado implements Icon {
        private final ImageIcon gifOriginal;
        private final int ancho;
        private final int alto;

        public GifEscalado(ImageIcon gifOriginal, int ancho, int alto) {
            this.gifOriginal = gifOriginal;
            this.ancho = ancho;//
            this.alto = alto;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.drawImage(gifOriginal.getImage(), x, y, ancho, alto, gifOriginal.getImageObserver());
        }

        @Override
        public int getIconWidth() { return this.ancho; }
        @Override
        public int getIconHeight() { return this.alto; }
    }

}