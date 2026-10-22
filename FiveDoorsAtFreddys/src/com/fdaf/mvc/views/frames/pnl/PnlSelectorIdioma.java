package com.fdaf.mvc.views.frames.pnl;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import com.fdaf.util.CargarImagenes;
import com.fdaf.util.Fuentes;
import com.fdaf.util.Idioma;
import com.fdaf.util.InteraccionBoton;
import com.fdaf.util.TextosInfo;

// Selector de idioma del inicio normal (CLAUDE.md §1.35): lo primero que se
// ve al abrir el juego, antes de NYDER. Misma identidad visual que PnlInfo
// (fondo animado del menú oscurecido, logo de NYDER, acento violeta). La
// opción resaltada al entrar es el idioma guardado; pasar el cursor por una
// opción la resalta y muestra el título en ese idioma. Un clic (o Enter)
// elige y continúa, sin pasos de confirmación. Teclado: izquierda/derecha.
public class PnlSelectorIdioma extends JPanel {

	private static final Color ACENTO = new Color(98, 96, 255);
	private static final Color TEXTO_TENUE = new Color(150, 150, 160);
	private static final Idioma[] OPCIONES = { Idioma.ESPANOL, Idioma.INGLES };

	private final JLabel lblLogo = new JLabel("", SwingConstants.CENTER);
	private final JLabel lblTitulo = new JLabel("", SwingConstants.CENTER);
	private final JButton[] botones = new JButton[OPCIONES.length];
	private final Consumer<Idioma> alElegir;
	private int resaltado;
	private boolean elegido = false;
	private int lineaX1, lineaX2, lineaY;

	public PnlSelectorIdioma(Icon logo, Idioma inicial, Consumer<Idioma> alElegir) {
		this.alElegir = alElegir;
		setPreferredSize(new Dimension(1600, 900));
		setBackground(Color.BLACK);
		setOpaque(true);
		setLayout(null);

		lblLogo.setIcon(logo);
		lblTitulo.setForeground(Color.WHITE);
		lblTitulo.setFont(Fuentes.obtener(52, java.awt.Font.BOLD));
		add(lblLogo);
		add(lblTitulo);

		for (int i = 0; i < OPCIONES.length; i++) {
			final int indice = i;
			JButton boton = new JButton(TextosInfo.nombreIdioma(OPCIONES[i]));
			boton.setContentAreaFilled(false);
			boton.setBorderPainted(false);
			boton.setFocusPainted(false);
			boton.setOpaque(false);
			boton.setBorder(null);
			boton.setFont(Fuentes.obtener(60));
			boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			boton.addActionListener(e -> elegir(indice));
			boton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					resaltar(indice);
				}
			});
			InteraccionBoton.aplicarHoverYSonido(boton);
			botones[i] = boton;
			add(boton);
		}

		InputMap teclas = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		teclas.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "idiomaAnterior");
		teclas.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "idiomaSiguiente");
		teclas.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "idiomaElegir");
		getActionMap().put("idiomaAnterior", accion(() -> resaltar((resaltado + OPCIONES.length - 1) % OPCIONES.length)));
		getActionMap().put("idiomaSiguiente", accion(() -> resaltar((resaltado + 1) % OPCIONES.length)));
		getActionMap().put("idiomaElegir", accion(() -> elegir(resaltado)));

		resaltar(inicial == Idioma.INGLES ? 1 : 0);
	}

	private static AbstractAction accion(Runnable r) {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				r.run();
			}
		};
	}

	private void resaltar(int indice) {
		resaltado = indice;
		lblTitulo.setText(TextosInfo.tituloSelectorIdioma(OPCIONES[indice]));
		for (int i = 0; i < botones.length; i++) {
			botones[i].setForeground(i == indice ? Color.WHITE : TEXTO_TENUE);
		}
		doLayout();
		repaint();
	}

	// Un solo disparo: un doble clic o Enter repetido no puede iniciar la
	// secuencia de inicio dos veces.
	private void elegir(int indice) {
		if (elegido) {
			return;
		}
		elegido = true;
		resaltar(indice);
		alElegir.accept(OPCIONES[indice]);
	}

	public JButton getBoton(Idioma idioma) {
		return botones[idioma == Idioma.INGLES ? 1 : 0];
	}

	public Idioma getResaltado() {
		return OPCIONES[resaltado];
	}

	public JLabel getLblTitulo() {
		return lblTitulo;
	}

	@Override
	public void doLayout() {
		int ancho = getWidth();
		int alto = getHeight();
		if (ancho <= 0 || alto <= 0) {
			return;
		}
		int y = (int) (alto * 0.27);
		if (lblLogo.getIcon() != null) {
			int lado = lblLogo.getIcon().getIconHeight();
			lblLogo.setBounds(0, y, ancho, lado);
			y += lado + (int) (alto * 0.03);
		}
		int altoTitulo = (int) (alto * 0.08);
		lblTitulo.setBounds(0, y, ancho, altoTitulo);
		y += altoTitulo + (int) (alto * 0.08);

		// Dos opciones del mismo ancho, centradas, con un hueco entre ellas.
		int anchoOpcion = (int) Math.min(ancho * 0.22, alto * 0.42);
		int hueco = (int) (anchoOpcion * 0.25);
		int altoOpcion = (int) (alto * 0.09);
		int x = (ancho - (2 * anchoOpcion + hueco)) / 2;
		for (int i = 0; i < botones.length; i++) {
			botones[i].setHorizontalAlignment(SwingConstants.CENTER);
			botones[i].setBounds(x + i * (anchoOpcion + hueco), y, anchoOpcion, altoOpcion);
		}
		// Subrayado violeta bajo la opción resaltada.
		JButton actual = botones[resaltado];
		int anchoTexto = actual.getFontMetrics(actual.getFont()).stringWidth(actual.getText());
		int centro = actual.getX() + actual.getWidth() / 2;
		lineaX1 = centro - anchoTexto / 2;
		lineaX2 = centro + anchoTexto / 2;
		lineaY = actual.getY() + actual.getHeight() + 4;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		Image fondo = CargarImagenes.menu != null ? CargarImagenes.menu.getImage() : null;
		if (fondo != null) {
			g2.drawImage(fondo, 0, 0, getWidth(), getHeight(), this);
		}
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.setComposite(AlphaComposite.SrcOver);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(ACENTO);
		g2.setStroke(new BasicStroke(4f));
		g2.drawLine(lineaX1, lineaY, lineaX2, lineaY);
		g2.dispose();
	}
}
