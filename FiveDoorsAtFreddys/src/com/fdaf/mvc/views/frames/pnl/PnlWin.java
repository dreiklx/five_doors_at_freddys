package com.fdaf.mvc.views.frames.pnl;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;


import javafx.embed.swing.JFXPanel;

import com.fdaf.mvc.views.frames.VistaPrincipal;
import com.fdaf.util.EscalarVista;
import com.fdaf.util.Fuentes;
import com.fdaf.util.ReproductorVideoGif;

public class PnlWin extends JPanel {

	private Image imagenActual;
	private JFXPanel panelVideo;
	private JButton btnJugarDeNuevo;
	private JButton btnSalir;
	private JLabel lblFlecha;
	private JLabel lblCargando;

	// Video de victoria (CLAUDE.md §1.82, v2.19): videos/victory.gif (1920x1080, mismos 289 fotogramas y tiempos que
	// victory.mp4) + el audio en un WAV aparte, sincronizados por ReproductorVideoGif con la posicion real del audio.
	// Sin JavaFX ni codecs del sistema (§1.81). panelVideo (JFXPanel) se conserva solo porque EscalarVista.adaptarWin
	// (congelada, §1.7) lo dimensiona.
	private static final String RUTA_GIF_VICTORIA = "/videos/victory.gif";
	private static final String RUTA_AUDIO_VICTORIA = "cinematics/victory_video_audio.wav";

	private ReproductorVideoGif reproductor;

	public PnlWin() {
		setPreferredSize(new Dimension(1600, 900));
		setBackground(Color.BLACK);
		setOpaque(true);
		setLayout(null);

		panelVideo = new JFXPanel();
		panelVideo.setBounds(0, 0, 1600, 900);
		add(panelVideo);

		Font fuenteBoton = Fuentes.obtener(50);

		// Geometría horizontal tipo PnlConfirmacion: 2 botones lado a
		// lado, misma Y, mismo ancho.
		btnJugarDeNuevo = new JButton("Volver a jugar");
		estilizarBoton(btnJugarDeNuevo, fuenteBoton);
		btnJugarDeNuevo.setBounds(260, 650, 520, 80);
		btnJugarDeNuevo.setVisible(false);
		add(btnJugarDeNuevo);

		btnSalir = new JButton("Salir del juego");
		estilizarBoton(btnSalir, fuenteBoton);
		btnSalir.setBounds(820, 650, 520, 80);
		btnSalir.setVisible(false);
		add(btnSalir);

		FontMetrics fmFlecha = getFontMetrics(fuenteBoton);
		int anchoFlecha = fmFlecha.stringWidth(">>") + 10;
		int altoFlecha = fmFlecha.getHeight();

		lblFlecha = new JLabel(">>");
		lblFlecha.setForeground(Color.WHITE);
		lblFlecha.setFont(fuenteBoton);
		lblFlecha.setBounds(0, 0, anchoFlecha, altoFlecha);
		lblFlecha.setVisible(false);
		add(lblFlecha);

		// Mensaje mostrado solo en la transicion Noche 5 -> Five Doors
		// Escape, mientras el proceso LibGDX arranca en segundo plano
		// (placeholder honesto hasta que exista el efecto de "glitch"
		// real -- ver memoria de Claude "project-libgdx-office-spawn-exit-design").
		lblCargando = new JLabel("", SwingConstants.CENTER);
		lblCargando.setForeground(Color.WHITE);
		lblCargando.setFont(Fuentes.obtener(36));
		lblCargando.setBounds(0, 650, 1600, 80);
		lblCargando.setVisible(false);
		add(lblCargando);
	}

	private void estilizarBoton(JButton boton, Font fuente) {
		boton.setFocusPainted(false);
		boton.setBorderPainted(false);
		boton.setContentAreaFilled(false);
		boton.setOpaque(false);
		boton.setBorder(null);
		boton.setFont(fuente);
		boton.setForeground(Color.WHITE);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (imagenActual != null) {
			g.drawImage(imagenActual, 0, 0, getWidth(), getHeight(), this);
		}
		ReproductorVideoGif r = reproductor;
		if (r != null && r.isEnCurso()) {
			r.pintar((Graphics2D) g, getWidth(), getHeight());
		}
	}

	public void mostrarFondo(ImageIcon icono) {
		panelVideo.setVisible(false);
		this.imagenActual = (icono != null) ? icono.getImage() : null;
		repaint();
	}

	/**
	 * Reproduce el video de victoria y llama a alTerminar UNA sola vez en el EDT al acabar. Si faltara el GIF, se salta
	 * el video y la partida sigue. Una segunda llamada se ignora (nunca dos transiciones encadenadas).
	 */
	public void reproducirVideoVictoria(Runnable alTerminar) {
		if (reproductor != null) {
			return;
		}
		panelVideo.setVisible(false);
		reproductor = new ReproductorVideoGif(RUTA_GIF_VICTORIA, RUTA_AUDIO_VICTORIA, this, alTerminar);
		reproductor.iniciar();
	}

	/** Corta el video sin continuar el flujo de victoria (arneses de prueba). Despues no se puede volver a iniciar. */
	public void detenerVideo() {
		if (reproductor == null) {
			reproductor = new ReproductorVideoGif(RUTA_GIF_VICTORIA, RUTA_AUDIO_VICTORIA, this, null);
		}
		reproductor.terminar(false);
	}

	/** El reproductor en curso (o null); lo consultan las pruebas de sincronia (§1.82). */
	public ReproductorVideoGif getReproductor() {
		return reproductor;
	}

	public void init(VistaPrincipal vp, PnlWin pantalla) {
		EscalarVista.adaptarWin(vp, pantalla);
		this.revalidate();
		this.repaint();
	}

	public JFXPanel getPanelVideo() { return panelVideo; }
	public void setPanelVideo(JFXPanel panelVideo) { this.panelVideo = panelVideo; }
	public JButton getBtnJugarDeNuevo() { return btnJugarDeNuevo; }
	public void setBtnJugarDeNuevo(JButton btnJugarDeNuevo) { this.btnJugarDeNuevo = btnJugarDeNuevo; }
	public JButton getBtnSalir() { return btnSalir; }
	public void setBtnSalir(JButton btnSalir) { this.btnSalir = btnSalir; }
	public JLabel getLblFlecha() { return lblFlecha; }
	public void setLblFlecha(JLabel lblFlecha) { this.lblFlecha = lblFlecha; }
	public JLabel getLblCargando() { return lblCargando; }
	public void setLblCargando(JLabel lblCargando) { this.lblCargando = lblCargando; }
}