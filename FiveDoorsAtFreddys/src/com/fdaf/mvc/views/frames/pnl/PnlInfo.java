package com.fdaf.mvc.views.frames.pnl;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.fdaf.util.CargarImagenes;
import com.fdaf.util.Fuentes;
import com.fdaf.util.InteraccionBoton;
import com.fdaf.util.TextosInfo;

// Pantalla de información paginada (CLAUDE.md §1.33-§1.34): una sola clase
// para la introducción inicial, "Cómo jugar" y "Acerca del proyecto". Fondo
// = el mismo GIF animado del menú (ya cargado en CargarImagenes) oscurecido,
// con una tarjeta central translúcida. La disposición se calcula en
// doLayout() a partir del tamaño real del panel, así que se adapta a
// cualquier resolución sin pasar por EscalarVista.
public class PnlInfo extends JPanel {

	private static final Color ACENTO = new Color(98, 96, 255); // violeta de NYDER
	private static final Color TEXTO = new Color(225, 225, 230);
	private static final Color TEXTO_TENUE = new Color(150, 150, 160);

	private final JLabel lblLogo = new JLabel();
	private final JLabel lblTitulo = new JLabel();
	private final JLabel lblCuerpo = new JLabel();
	private final JLabel lblIndicador = new JLabel("", SwingConstants.CENTER);
	private final JButton btnAnterior = crearBoton(34);
	private final JButton btnSiguiente = crearBoton(34);
	private final JButton btnOmitir = crearBoton(28);
	private final JPanel pnlPestanas = new JPanel();
	private JButton[] pestanas = new JButton[0];

	private String[] titulos = new String[0];
	private String[] cuerpos = new String[0];
	private String textoFinal = "";
	private int indice;
	private Runnable alSalir;

	private int tarjetaX, tarjetaY, tarjetaAncho, tarjetaAlto, lineaAcentoY;

	public PnlInfo() {
		setPreferredSize(new Dimension(1600, 900));
		setBackground(Color.BLACK);
		setOpaque(true);
		setLayout(null);

		lblTitulo.setForeground(Color.WHITE);
		lblTitulo.setFont(Fuentes.obtener(52, java.awt.Font.BOLD));
		lblCuerpo.setForeground(TEXTO);
		lblCuerpo.setFont(Fuentes.obtener(26));
		lblCuerpo.setVerticalAlignment(SwingConstants.TOP);
		lblIndicador.setForeground(TEXTO_TENUE);
		lblIndicador.setFont(Fuentes.obtener(26));
		lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
		btnSiguiente.setHorizontalAlignment(SwingConstants.RIGHT);
		btnAnterior.setHorizontalAlignment(SwingConstants.LEFT);
		btnOmitir.setHorizontalAlignment(SwingConstants.RIGHT);
		btnOmitir.setForeground(TEXTO_TENUE);
		pnlPestanas.setOpaque(false);

		btnAnterior.addActionListener(e -> mostrarPagina(indice - 1));
		btnSiguiente.addActionListener(e -> {
			if (indice >= titulos.length - 1) {
				salir();
			} else {
				mostrarPagina(indice + 1);
			}
		});
		btnOmitir.addActionListener(e -> salir());

		add(lblLogo);
		add(lblTitulo);
		add(lblCuerpo);
		add(lblIndicador);
		add(btnAnterior);
		add(btnSiguiente);
		add(btnOmitir);
		add(pnlPestanas);
	}

	// pestanas: nombres para saltar directo a cada página (null = sin
	// pestañas). logo: icono opcional sobre el título. textoOmitir: botón
	// superior derecho (null = oculto). textoFinal: texto del botón
	// "siguiente" en la última página, que ejecuta alSalir.
	public void configurar(String[] titulos, String[] cuerpos, String[] pestanas, Icon logo,
			String textoOmitir, String textoFinal, Runnable alSalir) {
		this.titulos = titulos;
		this.cuerpos = cuerpos;
		this.textoFinal = textoFinal;
		this.alSalir = alSalir;

		lblLogo.setIcon(logo);
		lblLogo.setVisible(logo != null);
		btnOmitir.setVisible(textoOmitir != null);
		btnOmitir.setText(textoOmitir);
		btnAnterior.setText(TextosInfo.botonAnterior());

		pnlPestanas.removeAll();
		if (pestanas != null) {
			this.pestanas = new JButton[pestanas.length];
			pnlPestanas.setLayout(new GridLayout(1, pestanas.length, 8, 0));
			for (int i = 0; i < pestanas.length; i++) {
				final int destino = i;
				JButton pestana = crearBoton(22, false);
				pestana.setHorizontalAlignment(SwingConstants.CENTER);
				pestana.setText(pestanas[i]);
				pestana.addActionListener(e -> mostrarPagina(destino));
				pestana.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseEntered(MouseEvent e) {
						pestana.setForeground(Color.WHITE);
					}

					@Override
					public void mouseExited(MouseEvent e) {
						pestana.setForeground(destino == indice ? ACENTO : TEXTO_TENUE);
					}
				});
				this.pestanas[i] = pestana;
				pnlPestanas.add(pestana);
			}
		} else {
			this.pestanas = new JButton[0];
		}
		pnlPestanas.setVisible(this.pestanas.length > 0);

		mostrarPagina(0);
	}

	private void salir() {
		if (alSalir != null) {
			alSalir.run();
		}
	}

	private void mostrarPagina(int nuevoIndice) {
		if (nuevoIndice < 0 || nuevoIndice >= titulos.length) {
			return;
		}
		indice = nuevoIndice;
		boolean ultima = indice == titulos.length - 1;
		lblTitulo.setText(titulos[indice]);
		btnAnterior.setVisible(indice > 0);
		btnSiguiente.setText(ultima ? textoFinal : TextosInfo.botonSiguiente());
		lblIndicador.setText(titulos.length > 1 ? (indice + 1) + " / " + titulos.length : "");
		for (int i = 0; i < pestanas.length; i++) {
			pestanas[i].setForeground(i == indice ? ACENTO : TEXTO_TENUE);
		}
		revalidate();
		doLayout();
		repaint();
	}

	public int getIndice() {
		return indice;
	}

	public int getCantidadPaginas() {
		return titulos.length;
	}

	public JButton getBtnAnterior() { return btnAnterior; }
	public JButton getBtnSiguiente() { return btnSiguiente; }
	public JButton getBtnOmitir() { return btnOmitir; }
	public JButton[] getPestanas() { return pestanas; }
	public JLabel getLblTitulo() { return lblTitulo; }
	public JLabel getLblCuerpo() { return lblCuerpo; }

	@Override
	public void doLayout() {
		int ancho = getWidth();
		int alto = getHeight();
		if (ancho <= 0 || alto <= 0) {
			return;
		}
		tarjetaAncho = (int) Math.min(ancho * 0.72, alto * 1.35);
		tarjetaAlto = (int) (alto * 0.84);
		tarjetaX = (ancho - tarjetaAncho) / 2;
		tarjetaY = (alto - tarjetaAlto) / 2;
		int margen = (int) (tarjetaAncho * 0.045);
		int interior = tarjetaAncho - 2 * margen;
		int y = tarjetaY + margen;

		int altoBoton = (int) (alto * 0.06);
		int anchoOmitir = (int) (tarjetaAncho * 0.30);
		btnOmitir.setBounds(tarjetaX + tarjetaAncho - margen - anchoOmitir, tarjetaY + margen / 3,
				anchoOmitir, altoBoton);

		if (lblLogo.isVisible() && lblLogo.getIcon() != null) {
			int lado = lblLogo.getIcon().getIconHeight();
			lblLogo.setBounds(tarjetaX, y, tarjetaAncho, lado);
			y += lado + margen / 3;
		}
		if (pnlPestanas.isVisible()) {
			int altoPestanas = (int) (alto * 0.055);
			pnlPestanas.setBounds(tarjetaX + margen, y + margen / 3, interior, altoPestanas);
			pnlPestanas.doLayout();
			y += altoPestanas + margen / 2;
		}

		int altoTitulo = (int) (alto * 0.08);
		lblTitulo.setHorizontalAlignment(lblLogo.isVisible() ? SwingConstants.CENTER : SwingConstants.LEFT);
		lblTitulo.setBounds(tarjetaX + margen, y, interior, altoTitulo);
		y += altoTitulo;
		lineaAcentoY = y + 4;
		y += margen / 2;

		int yBotones = tarjetaY + tarjetaAlto - margen / 2 - altoBoton;
		int anchoBoton = (int) (tarjetaAncho * 0.36);
		btnAnterior.setBounds(tarjetaX + margen, yBotones, anchoBoton, altoBoton);
		btnSiguiente.setBounds(tarjetaX + tarjetaAncho - margen - anchoBoton, yBotones, anchoBoton, altoBoton);
		lblIndicador.setBounds(tarjetaX + tarjetaAncho / 2 - 100, yBotones, 200, altoBoton);

		lblCuerpo.setBounds(tarjetaX + margen, y, interior, yBotones - y - margen / 3);
		// Un JLabel con HTML solo ajusta líneas si se le da un ancho CSS, y
		// Swing no lo interpreta como píxeles reales (medido: 400px CSS ->
		// 520px en pantalla). Se calibra midiendo: primero con el ancho
		// deseado y luego corrigiendo por la proporción observada.
		if (indice < cuerpos.length) {
			int deseado = interior - 12;
			lblCuerpo.setText(htmlCuerpo(cuerpos[indice], deseado));
			int real = lblCuerpo.getPreferredSize().width;
			if (real > 0 && real != deseado) {
				lblCuerpo.setText(htmlCuerpo(cuerpos[indice], (int) ((long) deseado * deseado / real)));
			}
		}
	}

	private static String htmlCuerpo(String cuerpo, int anchoCss) {
		return "<html><div style='width:" + anchoCss + "px'>" + cuerpo + "</div></html>";
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		Image fondo = CargarImagenes.menu != null ? CargarImagenes.menu.getImage() : null;
		if (fondo != null) {
			g2.drawImage(fondo, 0, 0, getWidth(), getHeight(), this);
		}
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, getWidth(), getHeight());

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int radio = Math.max(12, tarjetaAncho / 60);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.86f));
		g2.setColor(new Color(8, 8, 14));
		g2.fillRoundRect(tarjetaX, tarjetaY, tarjetaAncho, tarjetaAlto, radio, radio);
		g2.setComposite(AlphaComposite.SrcOver);
		g2.setColor(new Color(80, 80, 95));
		g2.setStroke(new BasicStroke(2f));
		g2.drawRoundRect(tarjetaX, tarjetaY, tarjetaAncho, tarjetaAlto, radio, radio);

		int margen = (int) (tarjetaAncho * 0.045);
		g2.setColor(ACENTO);
		g2.setStroke(new BasicStroke(3f));
		if (lblLogo.isVisible()) {
			int largo = tarjetaAncho / 5;
			g2.drawLine(tarjetaX + (tarjetaAncho - largo) / 2, lineaAcentoY, tarjetaX + (tarjetaAncho + largo) / 2,
					lineaAcentoY);
		} else {
			g2.drawLine(tarjetaX + margen, lineaAcentoY, tarjetaX + margen + tarjetaAncho / 6, lineaAcentoY);
		}
		g2.dispose();
	}

	private static JButton crearBoton(int tamanoFuente) {
		return crearBoton(tamanoFuente, true);
	}

	private static JButton crearBoton(int tamanoFuente, boolean resaltarAlPasar) {
		JButton boton = new JButton();
		boton.setContentAreaFilled(false);
		boton.setBorderPainted(false);
		boton.setFocusPainted(false);
		boton.setOpaque(false);
		boton.setBorder(null);
		boton.setForeground(Color.WHITE);
		boton.setFont(Fuentes.obtener(tamanoFuente));
		boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		final Color[] colorNormal = new Color[1];
		if (resaltarAlPasar) boton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				colorNormal[0] = boton.getForeground();
				boton.setForeground(ACENTO);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (colorNormal[0] != null) {
					boton.setForeground(colorNormal[0]);
				}
			}
		});
		// PnlInfo se reutiliza: si se sale con un clic, sin mouseExited el
		// botón conservaría el resaltado la próxima vez que se abra (§1.49).
		if (resaltarAlPasar) InteraccionBoton.restaurarAlOcultar(boton, () -> {
			if (colorNormal[0] != null) {
				boton.setForeground(colorNormal[0]);
				colorNormal[0] = null;
			}
		});
		// Mismo sonido de hover que el resto de los menús.
		InteraccionBoton.aplicarHoverYSonido(boton);
		return boton;
	}
}
