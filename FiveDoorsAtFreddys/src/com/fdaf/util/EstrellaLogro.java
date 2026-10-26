package com.fdaf.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

import javax.swing.Icon;

// Estrella dorada de "Escape completado" (CLAUDE.md §1.66): la que aparece
// junto al título del menú una vez que el jugador ganó Five Doors Escape, y la
// que se presenta en la pantalla de agradecimiento la primera vez. Se dibuja
// en código (no es un PNG) para que quede nítida a cualquier resolución: cinco
// puntas, degradado dorado de arriba a abajo, borde oscuro fino (se lee sobre
// la estática clara y oscura del fondo del menú), un reflejo en la mitad
// superior y un resplandor cálido muy tenue detrás. Es deliberadamente
// distinta de la estrella violeta de cuatro puntas de NYDER: es un logro del
// jugador, no la marca del equipo.
public final class EstrellaLogro {

	private static final Color ORO_CLARO = new Color(255, 232, 140);
	private static final Color ORO = new Color(242, 184, 48);
	private static final Color ORO_OSCURO = new Color(176, 110, 18);
	private static final Color BORDE = new Color(60, 34, 4, 230);

	private EstrellaLogro() {
	}

	public static Path2D forma(double cx, double cy, double radio) {
		Path2D.Double p = new Path2D.Double();
		double interior = radio * 0.45;
		for (int i = 0; i < 10; i++) {
			double r = (i % 2 == 0) ? radio : interior;
			double a = -Math.PI / 2 + i * Math.PI / 5;
			double x = cx + r * Math.cos(a);
			double y = cy + r * Math.sin(a);
			if (i == 0) {
				p.moveTo(x, y);
			} else {
				p.lineTo(x, y);
			}
		}
		p.closePath();
		return p;
	}

	// resplandor: 0 = ninguno, 1 = el de la presentación (más fuerte).
	public static void pintar(Graphics2D g0, double cx, double cy, double radio, float resplandor) {
		Graphics2D g = (Graphics2D) g0.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		if (resplandor > 0f) {
			float rr = (float) (radio * (1.35 + 0.6 * resplandor));
			int a = Math.min(255, (int) (70 + 110 * resplandor));
			g.setPaint(new RadialGradientPaint((float) cx, (float) cy, rr, new float[] { 0f, 0.45f, 1f },
					new Color[] { new Color(255, 214, 110, a), new Color(255, 190, 60, a / 3), new Color(255, 170, 40, 0) }));
			g.fill(new Ellipse2D.Double(cx - rr, cy - rr, rr * 2, rr * 2));
		}
		Path2D estrella = forma(cx, cy, radio);
		g.setPaint(new GradientPaint((float) cx, (float) (cy - radio), ORO_CLARO, (float) cx, (float) (cy + radio * 0.85), ORO_OSCURO));
		g.fill(estrella);
		// reflejo: la mitad superior un poco más clara
		Graphics2D r = (Graphics2D) g.create();
		r.clip(estrella);
		r.setPaint(new GradientPaint((float) cx, (float) (cy - radio), new Color(255, 255, 255, 120), (float) cx, (float) cy,
				new Color(255, 255, 255, 0)));
		r.fill(new java.awt.geom.Rectangle2D.Double(cx - radio, cy - radio, radio * 2, radio));
		r.dispose();
		g.setColor(ORO);
		g.setStroke(new BasicStroke((float) Math.max(1.0, radio * 0.035)));
		g.draw(forma(cx, cy, radio * 0.86));
		g.setColor(BORDE);
		g.setStroke(new BasicStroke((float) Math.max(1.2, radio * 0.07), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.draw(estrella);
		g.dispose();
	}

	// Versión blanca del menú (CLAUDE.md §1.70): blanca como el título, con un
	// degradado muy leve, un borde oscuro fino y una sombra suave desplazada
	// para que se lea sobre las franjas claras de la estática. Sin resplandor:
	// un halo blanco la haría borrosa al lado del texto.
	public static void pintarBlanca(Graphics2D g0, double cx, double cy, double radio) {
		Graphics2D g = (Graphics2D) g0.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		double off = Math.max(1.0, radio * 0.06);
		g.setColor(new Color(0, 0, 0, 90));
		g.fill(forma(cx + off, cy + off, radio));
		Path2D estrella = forma(cx, cy, radio);
		g.setPaint(new GradientPaint((float) cx, (float) (cy - radio), Color.WHITE, (float) cx, (float) (cy + radio * 0.85),
				new Color(222, 222, 228)));
		g.fill(estrella);
		g.setColor(new Color(28, 28, 34, 210));
		g.setStroke(new BasicStroke((float) Math.max(1.2, radio * 0.05), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.draw(estrella);
		g.dispose();
	}

	// Icono para el menú cuyo contorno VISUAL mide "alto" píxeles de la punta
	// superior a las dos inferiores (una estrella de radio r mide 1.809 r de
	// alto y 1.902 r de ancho), para igualarla con la altura visual de una palabra.
	public static Icon iconoBlanco(final int alto) {
		final double r = alto / 1.809;
		final int ancho = (int) Math.ceil(1.902 * r) + 3;
		return new Icon() {
			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				pintarBlanca((Graphics2D) g, x + ancho / 2.0 - 1, y + r, r);
			}

			@Override
			public int getIconWidth() {
				return ancho;
			}

			@Override
			public int getIconHeight() {
				return alto + 3;
			}
		};
	}

	// Icono de lado fijo para un JLabel (la estrella dorada).
	public static Icon icono(final int lado) {
		return new Icon() {
			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				pintar((Graphics2D) g, x + lado / 2.0, y + lado / 2.0, lado * 0.40, 0.35f);
			}

			@Override
			public int getIconWidth() {
				return lado;
			}

			@Override
			public int getIconHeight() {
				return lado;
			}
		};
	}
}
