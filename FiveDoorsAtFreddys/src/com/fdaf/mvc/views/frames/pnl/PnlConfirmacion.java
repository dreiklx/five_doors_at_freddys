package com.fdaf.mvc.views.frames.pnl;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.fdaf.util.Fuentes;

public class PnlConfirmacion extends JPanel {

	private JLabel lblMensaje;
	private JButton btnConfirmar;
	private JButton btnCancelar;
	private JLabel lblFlecha;
	private Runnable accionConfirmar;

	public PnlConfirmacion() {
		setOpaque(false);
		setLayout(null);
		setVisible(false);
		setBounds(0, 0, 1600, 900);

		lblMensaje = new JLabel("", SwingConstants.CENTER);
		lblMensaje.setForeground(Color.WHITE);
		lblMensaje.setFont(Fuentes.obtener(32));
		lblMensaje.setBounds(400, 380, 800, 60);
		add(lblMensaje);

		Font fuenteBoton = Fuentes.obtener(50);

		btnConfirmar = new JButton("");
		estilizarBoton(btnConfirmar, fuenteBoton);
		btnConfirmar.setBounds(360, 470, 420, 70);
		add(btnConfirmar);

		btnCancelar = new JButton("");
		estilizarBoton(btnCancelar, fuenteBoton);
		btnCancelar.setBounds(820, 470, 420, 70);
		add(btnCancelar);

		FontMetrics fmFlecha = getFontMetrics(fuenteBoton);
		int anchoFlecha = fmFlecha.stringWidth(">>") + 10;
		int altoFlecha = fmFlecha.getHeight();

		lblFlecha = new JLabel(">>");
		lblFlecha.setForeground(Color.WHITE);
		lblFlecha.setFont(fuenteBoton);
		lblFlecha.setBounds(0, 0, anchoFlecha, altoFlecha);
		lblFlecha.setVisible(false);
		add(lblFlecha);

		btnConfirmar.addActionListener(e -> {
			Runnable accion = accionConfirmar;
			ocultar();
			if (accion != null) {
				accion.run();
			}
		});

		btnCancelar.addActionListener(e -> ocultar());
	}

	private void estilizarBoton(JButton boton, Font fuente) {
		boton.setFocusPainted(false);
		boton.setBorderPainted(false);
		boton.setContentAreaFilled(false);
		boton.setOpaque(false);
		boton.setBorder(null);
		boton.setForeground(Color.WHITE);
		boton.setFont(fuente);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setColor(new Color(0, 0, 0, 180));
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.dispose();
	}

	public void mostrar(String mensaje, String textoConfirmar, String textoCancelar, Runnable accionConfirmar) {
		lblMensaje.setText(mensaje);
		btnConfirmar.setText(textoConfirmar);
		btnCancelar.setText(textoCancelar);
		this.accionConfirmar = accionConfirmar;

		setVisible(true);
		if (getParent() != null) {
			getParent().setComponentZOrder(this, 0);
		}
	}

	public void ocultar() {
		setVisible(false);
	}

	public JLabel getLblMensaje() { return lblMensaje; }
	public JButton getBtnConfirmar() { return btnConfirmar; }
	public JButton getBtnCancelar() { return btnCancelar; }
	public JLabel getLblFlecha() { return lblFlecha; }
}