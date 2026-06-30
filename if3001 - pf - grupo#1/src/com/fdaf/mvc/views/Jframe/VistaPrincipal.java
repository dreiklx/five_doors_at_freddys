package com.fdaf.mvc.views.Jframe;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.fdaf.util.CargarImagenes;

import java.awt.Color;

import java.awt.Dimension;

import java.awt.Frame;
import java.awt.Window.Type;


public class VistaPrincipal extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JPanel contentPane;
	public JPanel pnlContenido;
	public JPanel pnlIzq;
	public JPanel pnlDer;
	public JPanel pnlArr;
	public JPanel pnlAba;
	public JPanel panel;

	/**
	 * Create the frame.
	 */
	public VistaPrincipal() {
		setUndecorated(true);
		setExtendedState(Frame.MAXIMIZED_BOTH);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1000, 600);
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(0, 0));
		contentPane.setBorder(null);
		setContentPane(contentPane);
	
		
		panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		

		pnlContenido = new JPanel();
		pnlContenido.setBackground(Color.PINK);
		
		panel.add(pnlContenido, BorderLayout.CENTER);
		pnlContenido.setLayout(new BorderLayout(0, 0));



	}

	public void init() {
		this.setVisible(true);
		this.setLocationRelativeTo(null);
	}
	public void setContenido(JComponent c) {


		pnlContenido.removeAll();
		pnlContenido.add(c,BorderLayout.CENTER);
		pnlContenido.repaint(); 
		pnlContenido.revalidate();
		
		
		
	}
	

	
	
}
