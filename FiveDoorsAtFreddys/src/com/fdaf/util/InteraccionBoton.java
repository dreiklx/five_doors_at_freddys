package com.fdaf.util;

import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.fdaf.mvc.views.multimedia.Sonido;

public class InteraccionBoton {

	// BUG REAL encontrado y corregido en esta sesion ("los sonidos de botones dejaron de sonar
	// en todo el proyecto"): esta es una instancia COMPARTIDA y de VIDA LARGA (unico punto real
	// de reproduccion del sonido de hover de todo el proyecto -- menu, idioma, Custom Night,
	// abrir tablet, botones de la tableta, Game Over) que se reproduce muchas veces sobre si
	// misma a lo largo de toda la sesion, exactamente el mismo patron que 'respiracion' en
	// ControllerInterfaz (ver Sonido.java, parametro 'reutilizable'). Sin marcarla reutilizable,
	// el LineListener de Sonido.java (agregado para el fix real de OutOfMemoryError de sesiones
	// anteriores) cerraba el Clip en el PRIMER STOP natural -- el primer hover de toda la sesion
	// (dura ~200ms) ya dejaba silenciosamente sordos a TODOS los hovers posteriores del resto de
	// la partida, sin ninguna excepcion ni log. Reproducido y confirmado real antes de este fix
	// (ver CLAUDE.md). Marcar 'reutilizable=true' no introduce una fuga nueva -- sigue siendo una
	// unica instancia acotada (nunca se crean mas), igual que los demas sonidos reutilizables ya
	// establecidos del proyecto (luces, llamada, ambiente de oficina).
	private static final Sonido SONIDO_HOVER = new Sonido("botones/botones_menu.wav", true);
	private static final int MARGEN_FLECHA = 6;

	// Sobrecargas existentes: delegan a la única implementación real, sin
	// duplicar lógica de hover en ningún lado.
	public static void aplicarHoverYSonido(JButton boton, JLabel flecha, int offsetX) {
		aplicarHoverYSonido(boton, flecha, offsetX, null);
	}

	public static void aplicarHoverYSonido(JButton boton) {
		aplicarHoverYSonido(boton, null, 0, null);
	}

	// elementoAdicional: componente opcional que aparece/desaparece junto
	// con la flecha -- usado hoy solo por btnContinuar (lblNocheActual),
	// pero reutilizable para cualquier caso futuro similar sin tocar esta
	// clase de nuevo.
	public static void aplicarHoverYSonido(JButton boton, JLabel flecha, int offsetX, JComponent elementoAdicional) {
		boton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				reproducirSonidoHover();

				if (flecha != null) {
					int inicioTexto = calcularInicioTexto(boton);
					int x = inicioTexto - flecha.getWidth() - MARGEN_FLECHA;
					int y = (boton.getY() + boton.getHeight() / 2) - (flecha.getHeight() / 2);
					flecha.setLocation(x, y);
					flecha.setVisible(true);
				}

				if (elementoAdicional != null) {
					elementoAdicional.setVisible(true);
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (flecha != null) {
					flecha.setVisible(false);
				}
				if (elementoAdicional != null) {
					elementoAdicional.setVisible(false);
				}
			}
		});
	}

	private static int calcularInicioTexto(JButton boton) {
		FontMetrics fm = boton.getFontMetrics(boton.getFont());
		int anchoTexto = fm.stringWidth(boton.getText());

		Insets insets = boton.getInsets();
		int anchoContenido = boton.getWidth() - insets.left - insets.right;

		int alineacion = boton.getHorizontalAlignment();

		if (alineacion == SwingConstants.LEFT) {
			return boton.getX() + insets.left;
		} else if (alineacion == SwingConstants.RIGHT) {
			return boton.getX() + boton.getWidth() - insets.right - anchoTexto;
		} else {
			return boton.getX() + insets.left + (anchoContenido - anchoTexto) / 2;
		}
	}

	public static Sonido obtenerSonidoHover() {
		return SONIDO_HOVER;
	}
	// Único punto de reproducción del sonido de hover en todo el
	// proyecto -- aplica el volumen vigente antes de sonar, así ningún
	// llamador futuro puede volver a caer en el bug de volumen congelado.
	public static void reproducirSonidoHover() {
		SONIDO_HOVER.aplicarNivelGlobal(PreferenciasJuego.volumenGeneral);
		SONIDO_HOVER.play();
	}
	
}