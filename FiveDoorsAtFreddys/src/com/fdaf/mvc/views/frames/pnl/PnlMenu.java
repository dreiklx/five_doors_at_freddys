package com.fdaf.mvc.views.frames.pnl;

import javax.swing.JPanel;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import com.fdaf.mvc.views.frames.VistaPrincipal;
import com.fdaf.util.CargarImagenes;
import com.fdaf.util.EscalarVista;
import com.fdaf.util.Fuentes;

import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.JButton;
import java.awt.Cursor;
import java.awt.Dimension;

public class PnlMenu extends JPanel {
	private JPanel pnlMenu;
	private JButton btnContinuar;
	private JButton btnNuevaPartida;
	private JButton btnOpciones;
	private JLabel lblNocheActual;
	private JLabel lblMenu;
	private JLabel lbl;
	private JTextPane txtpnFiveDoorsAt;
	private PnlConfirmacion pnlConfirmacion;
	private JLabel lblVersion;
	private JLabel lblCreditos;

	public PnlMenu() {
		setPreferredSize(new Dimension(1600, 900));
		setBackground(Color.ORANGE);
		setLayout(null);
		
		pnlMenu = new PnlAplicarImagen(CargarImagenes.menu);
		pnlMenu.setBackground(Color.BLACK);
		pnlMenu.setPreferredSize(new Dimension(1600, 900));
		pnlMenu.setBounds(0, 0, 1600, 900);
		add(pnlMenu);
		pnlMenu.setLayout(null);
		
		txtpnFiveDoorsAt = new JTextPane();
		txtpnFiveDoorsAt.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		txtpnFiveDoorsAt.setFocusable(false);
		txtpnFiveDoorsAt.setOpaque(false);
		txtpnFiveDoorsAt.setFont(Fuentes.obtener(75));
		txtpnFiveDoorsAt.setText("Five\r\nDoors\r\nat\r\nFreddy's");
		txtpnFiveDoorsAt.setBounds(267, 50, 467, 370);
		txtpnFiveDoorsAt.setForeground(Color.WHITE);
		pnlMenu.add(txtpnFiveDoorsAt);

		btnContinuar = new JButton("Continuar");
		btnContinuar.setHorizontalAlignment(SwingConstants.LEFT);
		btnContinuar.setFocusPainted(false);
		btnContinuar.setBorderPainted(false);
		btnContinuar.setContentAreaFilled(false);
		btnContinuar.setOpaque(false);
		btnContinuar.setBorder(null);
		btnContinuar.setFont(new Font("Consolas", Font.PLAIN, 75));
		btnContinuar.setForeground(Color.WHITE);
		btnContinuar.setBounds(227, 534, 507, 84);
		pnlMenu.add(btnContinuar);

		lblNocheActual = new JLabel("Noche 1", SwingConstants.LEFT);
		lblNocheActual.setForeground(Color.WHITE);
		lblNocheActual.setFont(new Font("Consolas", Font.PLAIN, 32));
		lblNocheActual.setBounds(232, 590, 143, 39);
		lblNocheActual.setVisible(false);
		pnlMenu.add(lblNocheActual);

		btnNuevaPartida = new JButton("Nueva Partida");
		btnNuevaPartida.setHorizontalAlignment(SwingConstants.LEFT);
		btnNuevaPartida.setFocusPainted(false);
		btnNuevaPartida.setBorderPainted(false);
		btnNuevaPartida.setContentAreaFilled(false);
		btnNuevaPartida.setOpaque(false);
		btnNuevaPartida.setBorder(null);
		btnNuevaPartida.setFont(new Font("Consolas", Font.PLAIN, 75));
		btnNuevaPartida.setForeground(Color.WHITE);
		btnNuevaPartida.setBounds(227, 433, 598, 88);
		pnlMenu.add(btnNuevaPartida);
		
		btnOpciones = new JButton("Opciones");
		btnOpciones.setHorizontalAlignment(SwingConstants.LEFT);
		btnOpciones.setOpaque(false);
		btnOpciones.setFocusPainted(false);
		btnOpciones.setContentAreaFilled(false);
		btnOpciones.setBorderPainted(false);
		btnOpciones.setBorder(null);
		btnOpciones.setFont(new Font("Consolas", Font.PLAIN, 75));
		btnOpciones.setForeground(Color.WHITE);
		btnOpciones.setBounds(227, 631, 458, 88);
		pnlMenu.add(btnOpciones);
		
		// Tamaño derivado del texto real de los botones (no un número
		// fijo) -- mantiene la misma proporción visual (~0.77) que ya
		// tenía este panel entre flecha y texto, incluso si el tamaño de
		// los botones vuelve a ajustarse manualmente más adelante.
		final float PROPORCION_FLECHA_TEXTO = 0.77f;
		int tamanoBoton = btnContinuar.getFont().getSize();
		int tamanoFlecha = Math.round(tamanoBoton * PROPORCION_FLECHA_TEXTO);

		Font fuenteFlecha = Fuentes.obtener(tamanoFlecha);
		FontMetrics fmFlecha = getFontMetrics(fuenteFlecha);
		int anchoFlecha = fmFlecha.stringWidth(">>") + 10;
		int altoFlecha = fmFlecha.getHeight();

		lbl = new JLabel(">>");
		lbl.setForeground(Color.WHITE);
		lbl.setFont(fuenteFlecha);
		lbl.setBounds(0, 0, anchoFlecha, altoFlecha);
		lbl.setVisible(false);
		pnlMenu.add(lbl);

		lblMenu = new JLabel("");
		lblMenu.setBounds(0, 0, 1600, 900);
		pnlMenu.add(lblMenu);

		pnlConfirmacion = new PnlConfirmacion();
		pnlMenu.add(pnlConfirmacion);

		Color colorDiscreto = new Color(180, 180, 180);

		lblVersion = new JLabel("v 2.1");
		lblVersion.setForeground(Color.WHITE);
		lblVersion.setBackground(Color.WHITE);
		lblVersion.setFont(Fuentes.obtener(25));
		lblVersion.setBounds(20, 865, 200, 25);
		pnlMenu.add(lblVersion);

		lblCreditos = new JLabel("©2026 Derek, Naygell, Ramsey, Elmer (UCR)");
		lblCreditos.setForeground(Color.WHITE);
		lblCreditos.setBackground(Color.WHITE);
		lblCreditos.setFont(Fuentes.obtener(25));
		lblCreditos.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCreditos.setBounds(900, 865, 680, 25);
		pnlMenu.add(lblCreditos);
	}

	public void init(VistaPrincipal vp, PnlMenu menu) {
		EscalarVista.adaptarMenu(vp, menu);
		this.revalidate();
		this.repaint();
	}

	public JPanel getPnlMenu() { return pnlMenu; }
	public void setPnlMenu(JPanel pnlMenu) { this.pnlMenu = pnlMenu; }
	public JLabel getLblMenu() { return lblMenu; }
	public void setLblMenu(JLabel lblMenu) { this.lblMenu = lblMenu; }
	public JTextPane getTxtpnFiveDoorsAt() { return txtpnFiveDoorsAt; }
	public void setTxtpnFiveDoorsAt(JTextPane txtpnFiveDoorsAt) { this.txtpnFiveDoorsAt = txtpnFiveDoorsAt; }
	public JLabel getLbl() { return lbl; }
	public void setLbl(JLabel lbl) { this.lbl = lbl; }
	public JButton getBtnContinuar() { return btnContinuar; }
	public void setBtnContinuar(JButton btnContinuar) { this.btnContinuar = btnContinuar; }
	public JButton getBtnNuevaPartida() { return btnNuevaPartida; }
	public void setBtnNuevaPartida(JButton btnNuevaPartida) { this.btnNuevaPartida = btnNuevaPartida; }
	public JLabel getLblNocheActual() { return lblNocheActual; }
	public void setLblNocheActual(JLabel lblNocheActual) { this.lblNocheActual = lblNocheActual; }
	public PnlConfirmacion getPnlConfirmacion() { return pnlConfirmacion; }
	public void setPnlConfirmacion(PnlConfirmacion pnlConfirmacion) { this.pnlConfirmacion = pnlConfirmacion; }
	public JButton getBtnOpciones() { return btnOpciones; }
	public void setBtnOpciones(JButton btnOpciones) { this.btnOpciones = btnOpciones; }
	public JLabel getLblVersion() { return lblVersion; }
	public void setLblVersion(JLabel lblVersion) { this.lblVersion = lblVersion; }
	public JLabel getLblCreditos() { return lblCreditos; }
	public void setLblCreditos(JLabel lblCreditos) { this.lblCreditos = lblCreditos; }
}