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
import com.fdaf.mvc.views.frames.pnl.PnlJuego;
import com.fdaf.mvc.views.frames.pnl.PnlMenu;
import com.fdaf.mvc.views.frames.pnl.PnlOpciones;
import com.fdaf.mvc.views.frames.pnl.PnlTableta;



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
		
		JButton btnAtras = opciones.getBtnAtras();
		JButton btnSalir = opciones.getBtnSalir();
		JButton btnEspanol = opciones.getBtnEspanol();
		JButton btnIngles = opciones.getBtnIngles();
		JLabel lblIdiomaSeleccionado = opciones.getLblIdiomaSeleccionado();
		JButton[] btnsNoche = opciones.getBtnsNoche();
		JLabel lblNocheSeleccionada = opciones.getLblNocheSeleccionada();
		
		opciones.setBounds(0, 0, ancho, alto);
		opciones.getPnlOpciones().setBounds(0, 0, ancho, alto);

		opciones.getBtnAtras().setBounds(
				getEscalaX(btnAtras.getX()), getEscalaY(btnAtras.getY()),
				getEscalaX(btnAtras.getWidth()), getEscalaY(btnAtras.getHeight()));

		opciones.getBtnSalir().setBounds(
				getEscalaX(btnSalir.getX()), getEscalaY(btnSalir.getY()),
				getEscalaX(btnSalir.getWidth()), getEscalaY(btnSalir.getHeight()));

		opciones.getBtnEspanol().setBounds(
				getEscalaX(btnEspanol.getX()), getEscalaY(btnEspanol.getY()),
				getEscalaX(btnEspanol.getWidth()), getEscalaY(btnEspanol.getHeight()));

		opciones.getBtnIngles().setBounds(
				getEscalaX(btnIngles.getX()), getEscalaY(btnIngles.getY()),
				getEscalaX(btnIngles.getWidth()), getEscalaY(btnIngles.getHeight()));

		opciones.getLblIdiomaSeleccionado().setBounds(
				getEscalaX(lblIdiomaSeleccionado.getX()), getEscalaY(lblIdiomaSeleccionado.getY()),
				getEscalaX(lblIdiomaSeleccionado.getWidth()), getEscalaY(lblIdiomaSeleccionado.getHeight()));

		// CAMBIO: selector de dificultad, mismo patrón exacto.
		for (JButton btnNoche : btnsNoche) {
			opciones.getBtnsNoche()[indexOf(btnsNoche, btnNoche)].setBounds(
					getEscalaX(btnNoche.getX()), getEscalaY(btnNoche.getY()),
					getEscalaX(btnNoche.getWidth()), getEscalaY(btnNoche.getHeight()));
		}

		opciones.getLblNocheSeleccionada().setBounds(
				getEscalaX(lblNocheSeleccionada.getX()), getEscalaY(lblNocheSeleccionada.getY()),
				getEscalaX(lblNocheSeleccionada.getWidth()), getEscalaY(lblNocheSeleccionada.getHeight()));
	}

	private static int indexOf(JButton[] arreglo, JButton buscado) {
		for (int i = 0; i < arreglo.length; i++) {
			if (arreglo[i] == buscado) return i;
		}
		return -1;
	}

	public static void adaptarMenu(VistaPrincipal vp, PnlMenu menu) {
		Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
		int alto = pantalla.height; 
		int ancho = pantalla.width;
		
		JButton btnOpciones = menu.getBtnOpciones();

		vp.setBounds(0, 0, ancho, alto);
		menu.setBounds(0, 0, ancho, alto);
		menu.getPnlMenu().setBounds(0, 0, ancho, alto);
		menu.getLblMenu().setBounds(0, 0, ancho, alto);

		menu.getBtnOpciones().setBounds(
				getEscalaX(btnOpciones.getX()), getEscalaY(btnOpciones.getY()), 
				getEscalaX(btnOpciones.getWidth()), getEscalaY(btnOpciones.getHeight()));
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

		tablet.getPbarRendirse().setBounds(
				getEscalaX(barra.getX()), getEscalaY(barra.getY()),
				getEscalaX(barra.getWidth()), getEscalaY(barra.getHeight()));

		tablet.getLblTabletCerrar().setBounds(
				getEscalaX(cerrar.getX()), getEscalaY(cerrar.getY()),
				getEscalaX(cerrar.getWidth()), getEscalaY(cerrar.getHeight()));

		tablet.getLblTabletCerrar().setIcon(getImagenEscalada(CargarImagenes.barraTableta,
				tablet.getLblTabletCerrar().getWidth(), tablet.getLblTabletCerrar().getHeight()));

		// pnlColeccionables NUNCA se escalaba antes de ahora. Se
		// escala como contenedor único; el GridLayout interno reparte el
		// espacio entre los 10 slots automáticamente.
		tablet.getPnlColeccionables().setBounds(
				getEscalaX(pnlColeccionables.getX()), getEscalaY(pnlColeccionables.getY()),
				getEscalaX(pnlColeccionables.getWidth()), getEscalaY(pnlColeccionables.getHeight()));

		// Forzamos que el GridLayout calcule YA los tamaños reales de cada
		// slot (igual que se lee getWidth()/getHeight() de bateria/power
		// después de fijar sus bounds), para poder escalar cada ícono
		// bloqueado al tamaño de celda correcto.
		tablet.getPnlColeccionables().doLayout();

		TipoColeccionable[] catalogo = TipoColeccionable.values();
		JLabel[] slots = tablet.getLblColeccionables();

		for (int i = 0; i < catalogo.length; i++) {

			JLabel slot = slots[i];
			String ruta = "/images/" + catalogo[i].getArchivoBloqueado();
			URL recurso = EscalarVista.class.getResource(ruta);

			if (recurso == null) {
				System.out.println("[RECURSO NO ENCONTRADO] " + ruta);
				continue;
			}

			slot.setIcon(getImagenEscalada(new ImageIcon(recurso),
					slot.getWidth(), slot.getHeight()));
		}
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
	}

	public static class GifEscalado implements Icon {
        private final ImageIcon gifOriginal;
        private final int ancho;
        private final int alto;

        public GifEscalado(ImageIcon gifOriginal, int ancho, int alto) {
            this.gifOriginal = gifOriginal;
            this.ancho = ancho;
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