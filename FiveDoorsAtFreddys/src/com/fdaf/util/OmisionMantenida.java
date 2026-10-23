package com.fdaf.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * Omitir una cinematica manteniendo ESC ~1.2 s (CLAUDE.md §1.62). Un toque no hace nada: el progreso avanza solo
 * mientras la tecla sigue apretada, se reinicia al soltarla y dispara la omision una unica vez. Alternativa: mantener
 * apretado el raton sobre el propio indicador (esquina inferior derecha). Dibuja un circulo pequeno que se completa
 * y el texto "Manten ESC para omitir" / "Hold ESC to skip". El progreso lo avanza el Timer de la propia cinematica
 * con actualizar(dt): no crea hilos ni timers propios.
 */
public final class OmisionMantenida {

	public static final float SEGUNDOS_PARA_OMITIR = 1.2f;

	private final Runnable alOmitir;
	private volatile boolean teclaApretada = false;
	private volatile boolean ratonApretado = false;
	private float progreso = 0f;
	private boolean disparada = false;
	private final Rectangle zona = new Rectangle();

	private OmisionMantenida(Runnable alOmitir) {
		this.alOmitir = alOmitir;
	}

	/** Instala las teclas (pressed/released ESCAPE) y el raton sobre el componente; alOmitir se ejecuta en el EDT. */
	public static OmisionMantenida instalar(JComponent componente, Runnable alOmitir) {
		OmisionMantenida o = new OmisionMantenida(alOmitir);
		componente.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "omision-apretar");
		componente.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true), "omision-soltar");
		componente.getActionMap().put("omision-apretar", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				o.teclaApretada = true;   // la repeticion automatica del sistema solo vuelve a poner true
			}
		});
		componente.getActionMap().put("omision-soltar", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				o.teclaApretada = false;
			}
		});
		MouseAdapter raton = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				o.ratonApretado = o.zona.contains(e.getPoint());
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				o.ratonApretado = false;
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (!o.zona.contains(e.getPoint())) {
					o.ratonApretado = false;
				}
			}
		};
		componente.addMouseListener(raton);
		componente.addMouseMotionListener(raton);
		return o;
	}

	/** Avanza (o reinicia) el progreso. Devuelve true el unico frame en que se completa. */
	public boolean actualizar(float dt) {
		if (disparada) {
			return false;
		}
		if (teclaApretada || ratonApretado) {
			progreso += dt;
			if (progreso >= SEGUNDOS_PARA_OMITIR) {
				disparada = true;
				alOmitir.run();
				return true;
			}
		} else {
			progreso = 0f;
		}
		return false;
	}

	public float getFraccion() {
		return Math.min(1f, progreso / SEGUNDOS_PARA_OMITIR);
	}

	public boolean isDisparada() {
		return disparada;
	}

	/** Indicador discreto abajo a la derecha: texto tenue y un circulo que se completa mientras se mantiene. */
	public void pintar(Graphics2D g, int w, int h, boolean ingles) {
		String texto = ingles ? "Hold ESC to skip" : "Mantén ESC para omitir";
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setFont(Fuentes.obtener(Math.max(12, h / 58)));
		FontMetrics fm = g.getFontMetrics();
		int r = Math.max(9, h / 70);
		int margen = Math.max(14, w / 60);
		int cx = w - margen - r;
		int cy = h - margen - r;
		float f = getFraccion();
		float alfa = 0.42f + 0.5f * f;
		g.setColor(new Color(1f, 1f, 1f, alfa));
		int tx = cx - r - 10 - fm.stringWidth(texto);
		g.drawString(texto, tx, cy + fm.getAscent() / 2 - 2);
		g.setStroke(new BasicStroke(Math.max(2f, r / 4f)));
		g.setColor(new Color(1f, 1f, 1f, 0.25f));
		g.draw(new Ellipse2D.Float(cx - r, cy - r, 2 * r, 2 * r));
		if (f > 0f) {
			g.setColor(new Color(1f, 1f, 1f, 0.95f));
			g.draw(new Arc2D.Float(cx - r, cy - r, 2 * r, 2 * r, 90f, -360f * f, Arc2D.OPEN));
		}
		zona.setBounds(tx - 8, cy - r - 8, w - (tx - 8), 2 * r + 16);
	}
}
