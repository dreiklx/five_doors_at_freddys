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
	private JPanel contentPane;
	private JPanel pnlContenido;
	private JPanel pnlIzq;
	private JPanel pnlDer;
	private JPanel pnlArr;
	private JPanel pnlAba;
	private JPanel panel;

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
		
		pnlIzq = new JPanel();
		pnlIzq.setBackground(new Color(0, 0, 8));
		panel.add(pnlIzq, BorderLayout.WEST);
		
		pnlDer = new JPanel();
		pnlDer.setBackground(new Color(0, 0, 8));
		panel.add(pnlDer, BorderLayout.EAST);
		
		pnlArr = new JPanel();
		pnlArr.setBackground(new Color(0, 0, 8));
		panel.add(pnlArr, BorderLayout.NORTH);
		
		pnlAba = new JPanel();
		pnlAba.setBackground(new Color(0, 0, 8));
		panel.add(pnlAba, BorderLayout.SOUTH);
		
		pnlContenido = new JPanel();
		
		panel.add(pnlContenido, BorderLayout.CENTER);
		pnlContenido.setLayout(new BorderLayout(0, 0));

		setRecortada();

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
	
	public void setRecortada() {
		pnlAba.setPreferredSize(new Dimension(10, 84));
		pnlArr.setPreferredSize(new Dimension(10, 84));
		pnlDer.setPreferredSize(new Dimension(183, 132));
		pnlIzq.setPreferredSize(new Dimension(183, 132));
	}
	public void setCompleta() {
		pnlAba.setPreferredSize(new Dimension(0,0));
		pnlArr.setPreferredSize(new Dimension(0,0));
		pnlDer.setPreferredSize(new Dimension(0,0));
		pnlIzq.setPreferredSize(new Dimension(0,0));
	}
}
