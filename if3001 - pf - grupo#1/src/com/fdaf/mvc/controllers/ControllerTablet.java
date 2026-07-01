package com.fdaf.mvc.controllers;

import java.awt.event.MouseListener;

import com.fdaf.mvc.views.frames.VistaPrincipal;
import com.fdaf.mvc.views.frames.pnl.PnlJuego;
import com.fdaf.mvc.views.frames.pnl.PnlTableta;

public class ControllerTablet {
	private VistaPrincipal vp;
	private PnlTableta tablet;
	
	
	public ControllerTablet(VistaPrincipal vp,PnlJuego juego) {
		this.vp=vp;
		tablet=new PnlTableta();
		rendirse();
	}
	public void init() {
		
	}
	
	public void abrirTablet() {
		
		vp.setContenido(tablet);
		
	}
	
	public void rendirse() {
		tablet.getPbarRendirse().addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(java.awt.event.MouseEvent e) {
				System.out.println("hola");
				
			}
			
			@Override
			public void mousePressed(java.awt.event.MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(java.awt.event.MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(java.awt.event.MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}


}
