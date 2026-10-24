package com.fdaf.mvc.views.frames.pnl;

import javax.swing.JPanel;
import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import com.fdaf.mvc.views.frames.VistaPrincipal;
import com.fdaf.util.CargarImagenes;
import com.fdaf.util.EscalarVista;
import com.fdaf.util.Fuentes;
import com.fdaf.util.Version;

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
	private JButton btnComoJugar;
	private JButton btnAcercaDe;
	private JLabel lblNocheActual;
	private JLabel lblMenu;
	private JLabel lbl;
	private JTextPane txtpnFiveDoorsAt;
	private PnlConfirmacion pnlConfirmacion;
	private JLabel lblVersion;
	private JLabel lblLogoNyder;
	private ImageIcon logoNyderOriginal;
	private JLabel lblEstrellaEscape;

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
		btnContinuar.setFont(Fuentes.obtener(75));
		btnContinuar.setForeground(Color.WHITE);
		btnContinuar.setBounds(227, 534, 507, 84);
		pnlMenu.add(btnContinuar);

		lblNocheActual = new JLabel("Noche 1", SwingConstants.LEFT);
		lblNocheActual.setForeground(Color.WHITE);
		lblNocheActual.setFont(Fuentes.obtener(32));
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
		btnNuevaPartida.setFont(Fuentes.obtener(75));
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
		btnOpciones.setFont(Fuentes.obtener(75));
		btnOpciones.setForeground(Color.WHITE);
		btnOpciones.setBounds(227, 631, 458, 88);
		pnlMenu.add(btnOpciones);

		// Navegación secundaria (CLAUDE.md §1.36): "Cómo jugar" y "Acerca del
		// proyecto" van en una fila al pie, alineada con la columna de los
		// botones principales y a la altura de la versión -- informativas,
		// claramente por debajo de Nueva Partida/Continuar/Opciones. La
		// posición se calcula en ubicarNavegacionSecundaria().
		btnComoJugar = crearBotonSecundario();
		btnAcercaDe = crearBotonSecundario();
		lblSeparadorNav = new JLabel("·", SwingConstants.CENTER);
		lblSeparadorNav.setForeground(COLOR_NAV_SECUNDARIA);
		lblSeparadorNav.setFont(Fuentes.obtener(30));
		pnlMenu.add(lblSeparadorNav);
		
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

		lblVersion = new JLabel(Version.texto());
		lblVersion.setForeground(Color.WHITE);
		lblVersion.setBackground(Color.WHITE);
		lblVersion.setFont(Fuentes.obtener(25));
		lblVersion.setBounds(20, 865, 200, 25);
		pnlMenu.add(lblVersion);

		// Identidad de NYDER en la esquina inferior derecha, donde antes
		// estaban los créditos con nuestros nombres (CLAUDE.md §1.30) --
		// simétrica a la versión (esquina inferior izquierda). El icono se
		// asigna ya escalado en EscalarVista.adaptarMenu.
		lblLogoNyder = new JLabel();
		lblLogoNyder.setBounds(1460, 768, 120, 120);
		java.net.URL recursoLogo = PnlMenu.class.getResource("/branding/nyder_logo.png");
		if (recursoLogo != null) {
			logoNyderOriginal = new ImageIcon(recursoLogo);
		}
		pnlMenu.add(lblLogoNyder);

		// Estrella de "Escape completado" (CLAUDE.md §1.66): oculta hasta la
		// primera victoria de Five Doors Escape. Se coloca en
		// ubicarEstrellaEscape(), justo a la derecha de "Freddy's".
		lblEstrellaEscape = new JLabel();
		lblEstrellaEscape.setVisible(false);
		pnlMenu.add(lblEstrellaEscape, 0);
	}

	// Como en FNaF 1, el logro aparece junto al título: a la derecha de la
	// última palabra ("Freddy's"), centrada en la altura de sus mayúsculas, del
	// tamaño de esas mayúsculas -- se ve, pero no compite con el título ni toca
	// ningún botón (el título termina muy por encima de "Nueva Partida"). Se
	// calcula con la posición real del final del texto en el JTextPane (o, si
	// todavía no se puede medir, con sus métricas), así que sigue al escalado.
	public void ubicarEstrellaEscape(boolean visible, String tooltip) {
		lblEstrellaEscape.setVisible(visible);
		lblEstrellaEscape.setToolTipText(visible ? tooltip : null);
		if (!visible) {
			return;
		}
		FontMetrics fm = txtpnFiveDoorsAt.getFontMetrics(txtpnFiveDoorsAt.getFont());
		int mayuscula = Math.round(fm.getAscent() * 0.72f);
		int lado = Math.round(mayuscula * 1.25f);
		java.awt.Rectangle fin = null;
		try {
			fin = txtpnFiveDoorsAt.modelToView(txtpnFiveDoorsAt.getDocument().getLength());
		} catch (Exception e) {
			fin = null;
		}
		int x;
		int base;
		if (fin != null && fin.height > 0 && fin.x > 0) {
			x = txtpnFiveDoorsAt.getX() + fin.x;
			base = txtpnFiveDoorsAt.getY() + fin.y + fm.getAscent();
		} else {
			java.awt.Insets in = txtpnFiveDoorsAt.getInsets();
			x = txtpnFiveDoorsAt.getX() + in.left + fm.stringWidth("Freddy's");
			base = txtpnFiveDoorsAt.getY() + in.top + fm.getHeight() * 3 + fm.getAscent();
		}
		x += Math.round(mayuscula * 0.45f);
		int centroY = base - mayuscula / 2;
		lblEstrellaEscape.setIcon(com.fdaf.util.EstrellaLogro.icono(lado));
		lblEstrellaEscape.setBounds(x, centroY - lado / 2, lado, lado);
	}

	public JLabel getLblEstrellaEscape() { return lblEstrellaEscape; }

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
	public JLabel getLblLogoNyder() { return lblLogoNyder; }
	public JButton getBtnComoJugar() { return btnComoJugar; }
	public JButton getBtnAcercaDe() { return btnAcercaDe; }

	private static final Color COLOR_NAV_SECUNDARIA = new Color(185, 185, 195);
	private static final Color COLOR_NAV_HOVER = new Color(98, 96, 255); // violeta de NYDER
	private JLabel lblSeparadorNav;

	// El texto lo fija ControllerMenu.actualizarTextosIdioma(); la posición,
	// ubicarNavegacionSecundaria(). Hover: resaltado violeta (sin la flecha
	// ">>", que es de los botones principales).
	private JButton crearBotonSecundario() {
		JButton boton = new JButton();
		boton.setHorizontalAlignment(SwingConstants.LEFT);
		boton.setOpaque(false);
		boton.setFocusPainted(false);
		boton.setContentAreaFilled(false);
		boton.setBorderPainted(false);
		boton.setBorder(null);
		boton.setFont(Fuentes.obtener(30));
		boton.setForeground(COLOR_NAV_SECUNDARIA);
		boton.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
		boton.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseEntered(java.awt.event.MouseEvent e) {
				boton.setForeground(COLOR_NAV_HOVER);
			}

			@Override
			public void mouseExited(java.awt.event.MouseEvent e) {
				boton.setForeground(COLOR_NAV_SECUNDARIA);
			}
		});
		// Al abrir "Cómo jugar"/"Acerca del proyecto" el menú se retira sin
		// mouseExited: sin esto el botón volvía violeta (§1.49).
		com.fdaf.util.InteraccionBoton.restaurarAlOcultar(boton, () -> boton.setForeground(COLOR_NAV_SECUNDARIA));
		pnlMenu.add(boton);
		return boton;
	}

	// Coloca la fila secundaria con las coordenadas YA escaladas de los
	// elementos que la enmarcan: empieza en la x de los botones principales
	// y comparte el centro vertical de la versión. Los anchos salen del
	// texto real, así que sirve para ambos idiomas; se llama después de
	// adaptarMenu() y cada vez que cambia el idioma.
	public void ubicarNavegacionSecundaria() {
		int alto = EscalarVista.getEscalaY(40);
		int y = lblVersion.getY() + lblVersion.getHeight() / 2 - alto / 2;
		int separacion = EscalarVista.getEscalaX(18);
		int finComoJugar = colocarEnFila(btnComoJugar, btnNuevaPartida.getX(), y, alto);
		int anchoSeparador = lblSeparadorNav.getFontMetrics(lblSeparadorNav.getFont()).stringWidth("·") + 2 * separacion;
		lblSeparadorNav.setBounds(finComoJugar, y, anchoSeparador, alto);
		colocarEnFila(btnAcercaDe, finComoJugar + anchoSeparador, y, alto);
	}

	private static int colocarEnFila(JButton boton, int x, int y, int alto) {
		int ancho = boton.getFontMetrics(boton.getFont()).stringWidth(boton.getText()) + 4;
		boton.setBounds(x, y, ancho, alto);
		return x + ancho;
	}

	public JLabel getLblSeparadorNav() { return lblSeparadorNav; }
	// Se consume una sola vez (EscalarVista.adaptarMenu): devuelve el icono
	// original y suelta la referencia, para no retener el PNG de 1254px.
	public ImageIcon tomarLogoNyderOriginal() {
		ImageIcon original = logoNyderOriginal;
		logoNyderOriginal = null;
		return original;
	}
}