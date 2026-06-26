package com.fdaf.util;

import java.awt.Dimension;
import java.awt.Toolkit;

import com.fdaf.mvc.views.Jframe.VistaPrincipal;

public class EscalarVista {
	
	public static void adaptarVista(VistaPrincipal vp) {
		 Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
		 
		 	
		 int ancho = pantalla.width;
	        int alto = pantalla.height; 
	        int nAncho;
	        int nAlto;
	        
	        if(ancho>1000) {
	        	nAncho=(ancho-1000)/2;
	    		vp.pnlDer.setPreferredSize(new Dimension(nAncho, 10));
	    		vp.pnlIzq.setPreferredSize(new Dimension(nAncho, 10));
	        	if(alto>600) {
	        		nAlto=(alto-600)/2;
	        		vp.pnlAba.setPreferredSize(new Dimension(10, nAlto));
	        		vp.pnlArr.setPreferredSize(new Dimension(10, nAlto));
	        	}else {

	        	}
	        }else {

	        }
	        



	}
	

}