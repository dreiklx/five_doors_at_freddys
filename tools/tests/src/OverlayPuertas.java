import java.awt.*; import java.awt.event.MouseEvent; import java.awt.image.BufferedImage; import javax.swing.*; import java.io.File; import java.lang.reflect.*;
import com.fdaf.mvc.controllers.*; import com.fdaf.mvc.views.frames.VistaPrincipal; import com.fdaf.mvc.views.frames.pnl.*;
import com.fdaf.mvc.models.coleccionables.*; import com.fdaf.mvc.models.puerta.*; import com.fdaf.mvc.models.juego.Juego;
// Revela coleccionables en cada puerta con el paneo real, mide si el overlay tapa la barra de la tableta
// o el panel PUERTA/LUZ, qué componente recibe un clic sobre la barra, y recoge el objeto con un clic real.
public class OverlayPuertas {
  static ControllerInterfaz ci; static ControllerCamara cam; static ControllerJuego cj; static PnlJuego pj; static int fallos = 0;
  public static void main(String[] a) throws Exception {
    String out = a[0];
    SwingUtilities.invokeAndWait(() -> { try {
      VistaPrincipal vp = new VistaPrincipal(); ci = new ControllerInterfaz();
      cam = (ControllerCamara) campo(ci, "controllerCamara"); pj = (PnlJuego) campo(ci, "pnlJuego"); cj = (ControllerJuego) campo(ci, "controllerJuego");
      ci.init(vp, new PnlMenu(), null);
      Method m = ControllerInterfaz.class.getDeclaredMethod("colgarLlamada"); m.setAccessible(true); m.invoke(ci);
    } catch (Exception e) { throw new RuntimeException(e); } });
    for (TipoColeccionable t : TipoColeccionable.values()) for (String lado : new String[]{"izq", "der"}) probar(lado, t, out, t == TipoColeccionable.GUITARRA_BONNIE || t == TipoColeccionable.MICROFONO_FREDDY);
    System.out.println("FALLOS: " + fallos); System.exit(0);
  }
  static void probar(String lado, TipoColeccionable t, String out, boolean captura) throws Exception {
    SwingUtilities.invokeAndWait(() -> { pj.getLblOverlay().setVisible(false); if (lado.equals("der")) cam.derecha(40); else cam.izquierda(40); });
    Thread.sleep(1300);
    SwingUtilities.invokeAndWait(() -> { try {
      Juego j0 = (Juego) campo(cj, "juego"); Coleccionable c = new Coleccionable(t); j0.getInventario().agregar(c); if (lado.equals("izq") && t.ordinal() == 0) j0.getInventario().agregar(new Coleccionable(t)); /* uno sin encontrar: la noche nunca se gana durante la prueba */
      set(cj, lado.equals("izq") ? "puertaPendienteIzq" : "puertaPendienteDer", new Puerta(1, TipoPuerta.COLECCIONABLE, c, null));
      set(cj, "ladoRevelandoActual", lado);
      ci.alRevelarColeccionable(lado, c);
      JLabel ov = pj.getLblOverlay(); Rectangle r = ov.getBounds(); Rectangle pant = new Rectangle(0, 0, pj.getWidth(), pj.getHeight());
      Rectangle vis = r.intersection(pant); double frac = vis.isEmpty() ? 0 : (double) (vis.width * vis.height) / (r.width * r.height);
      JLabel tab = pj.getLblTabAbrir(); Rectangle tb = SwingUtilities.convertRectangle(tab.getParent(), tab.getBounds(), pj);
      JComponent panel = lado.equals("der") ? pj.getPanelDer() : pj.getPanelIzq(); Rectangle pn = SwingUtilities.convertRectangle(panel.getParent(), panel.getBounds(), pj);
      // ¿Algún punto de la barra de la tableta lo recibe el overlay?
      int robados = 0; for (int x = tb.x; x < tb.x + tb.width; x += 4) for (int y = tb.y; y < tb.y + tb.height; y += 4) if (SwingUtilities.getDeepestComponentAt(pj, x, y) == ov) robados++;
      Icon ic = ov.getIcon(); double aspIcono = (double) ic.getIconWidth() / ic.getIconHeight();
      boolean bien = frac > 0.97 && robados == 0 && !r.intersects(pn);
      // contenido visible: porcentaje de píxeles opacos del icono ya escalado
      BufferedImage bi = new BufferedImage(ic.getIconWidth(), ic.getIconHeight(), BufferedImage.TYPE_INT_ARGB); Graphics2D g0 = bi.createGraphics(); ic.paintIcon(null, g0, 0, 0); g0.dispose();
      int op = 0; for (int y = 0; y < bi.getHeight(); y++) for (int x = 0; x < bi.getWidth(); x++) if ((bi.getRGB(x, y) >>> 24) > 40) op++;
      if (captura) { BufferedImage img = new BufferedImage(pj.getWidth(), pj.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics(); pj.printAll(g); g.dispose(); javax.imageio.ImageIO.write(img, "png", new File(out + "_" + lado + "_" + t + ".png")); }
      // Recogida real: clic en el centro del overlay, despachado al componente que está en ese punto.
      Juego j = (Juego) campo(cj, "juego"); int antes = j.getColeccionablesEncontrados();
      Point cen = new Point(r.x + r.width / 2, r.y + r.height / 2); Component destino = SwingUtilities.getDeepestComponentAt(pj, cen.x, cen.y);
      Point p = SwingUtilities.convertPoint(pj, cen, destino);
      destino.dispatchEvent(new MouseEvent(destino, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, p.x, p.y, 1, false, MouseEvent.BUTTON1));
      int despues = j.getColeccionablesEncontrados();
      bien &= despues == antes + 1 && destino == ov && !ov.isVisible() && campo(cj, "ladoRevelandoActual") == null;
      if (!bien) fallos++;
      System.out.printf("%-4s %-22s overlay=%dx%d@%d,%d visible=%.0f%% barraRobada=%d panel=%s icono=%dx%d(asp %.2f) opaco=%d%% recogido=%d->%d %s%n", lado, t, r.width, r.height, r.x, r.y, frac * 100, robados, r.intersects(pn) ? "SOLAPA" : "ok", ic.getIconWidth(), ic.getIconHeight(), aspIcono, 100 * op / (bi.getWidth() * bi.getHeight()), antes, despues, bien ? "OK" : "FALLO");
    } catch (Exception e) { throw new RuntimeException(e); } });
    Thread.sleep(700);
  }
  static Object campo(Object o, String n) throws Exception { Field f = o.getClass().getDeclaredField(n); f.setAccessible(true); return f.get(o); }
  static void set(Object o, String n, Object v) throws Exception { Field f = o.getClass().getDeclaredField(n); f.setAccessible(true); f.set(o, v); }
}
