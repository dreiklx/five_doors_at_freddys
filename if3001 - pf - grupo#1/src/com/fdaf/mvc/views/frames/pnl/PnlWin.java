package com.fdaf.mvc.views.frames.pnl;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.util.Duration;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import com.fdaf.mvc.views.frames.VistaPrincipal;
import com.fdaf.util.EscalarVista;
import com.fdaf.util.Fuentes;

public class PnlWin extends JPanel {

	private Image imagenActual;
	private JFXPanel panelVideo;
	private JButton btnJugarDeNuevo;
	private JButton btnSalir;
	private JLabel lblFlecha;

	private MediaPlayer mediaPlayer;

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
	}

	public void mostrarFondo(ImageIcon icono) {
		panelVideo.setVisible(false);
		this.imagenActual = (icono != null) ? icono.getImage() : null;
		repaint();
	}

	public void reproducirVideo(String rutaClasspath, Runnable alTerminar) {
		Platform.runLater(() -> {
			try {
				URL recurso = getClass().getResource(rutaClasspath);
				if (recurso == null) {
					System.out.println("[RECURSO NO ENCONTRADO] " + rutaClasspath);
					SwingUtilities.invokeLater(alTerminar);
					return;
				}

				Media media = new Media(recurso.toExternalForm());
				mediaPlayer = new MediaPlayer(media);
				MediaView vista = new MediaView(mediaPlayer);
				vista.setFitWidth(panelVideo.getWidth());
				vista.setFitHeight(panelVideo.getHeight());
				vista.setPreserveRatio(false);
				vista.setOpacity(0.0);

				StackPane raiz = new StackPane(vista);
				raiz.setStyle("-fx-background-color: black;");
				panelVideo.setScene(new Scene(raiz));

				mediaPlayer.setOnEndOfMedia(() -> {
					mediaPlayer.stop();
					mediaPlayer.dispose();
					SwingUtilities.invokeLater(alTerminar);
				});

				mediaPlayer.setOnError(() -> {
					System.out.println("[ERROR VIDEO] " + mediaPlayer.getError());
					mediaPlayer.dispose();
					SwingUtilities.invokeLater(alTerminar);
				});

				mediaPlayer.play();

				FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), vista);
				fadeIn.setFromValue(0.0);
				fadeIn.setToValue(1.0);
				fadeIn.play();

			} catch (Exception e) {
				e.printStackTrace();
				SwingUtilities.invokeLater(alTerminar);
			}
		});
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
}