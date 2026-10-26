import javax.swing.*; import java.awt.*; import java.awt.image.BufferedImage; import java.lang.reflect.*; import java.io.File; import javax.sound.sampled.*;
import com.fdaf.mvc.controllers.ControllerMenu; import com.fdaf.mvc.views.frames.VistaPrincipal; import com.fdaf.mvc.views.frames.pnl.*; import com.fdaf.util.*; import com.fdaf.mvc.views.multimedia.Sonido;
// Arranque real: selector -> NYDER (con destello) -> advertencia. Registra el brillo de la estrella en el tiempo, el arranque del
// clip del sonido, capturas sin y con destello (y su diferencia: debe quedar alrededor de la estrella), y el orden de pantallas.
// Args: carpeta idioma [saltar]  -- con "saltar", CTRL+S a mitad del destello: el sonido se corta y no queda timer.
public class PruebaDestelloNyder {
  static ControllerMenu cm; static VistaPrincipal vp; static int fallos = 0;
  static Object f(Object o, Class<?> c, String n) throws Exception { Field x = c.getDeclaredField(n); x.setAccessible(true); return x.get(o); }
  static void ok(boolean b, String s) { System.out.println((b ? "OK    " : "FALLO ") + s); if (!b) fallos++; }
  static String nombre() { Component c = vp.pnlContenido.getComponent(0); if (c instanceof PnlWarning) { try { return ((Double) f(c, PnlWarning.class, "fraccionAncho")) > 0 ? "NYDER" : "ADVERTENCIA"; } catch (Exception e) { } } return c.getClass().getSimpleName(); }
  static BufferedImage cap(Component c) throws Exception { BufferedImage img = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_RGB); SwingUtilities.invokeAndWait(() -> { Graphics2D g = img.createGraphics(); c.printAll(g); g.dispose(); }); return img; }
  public static void main(String[] a) throws Exception {
    String out = a[0]; Idioma id = Idioma.valueOf(a[1]); boolean saltar = a.length > 2;
    SwingUtilities.invokeAndWait(() -> { cm = new ControllerMenu(false); cm.init(); try { vp = (VistaPrincipal) f(cm, ControllerMenu.class, "vp"); } catch (Exception e) { throw new RuntimeException(e); } });
    Thread.sleep(800);
    SwingUtilities.invokeAndWait(() -> ((PnlSelectorIdioma) vp.pnlContenido.getComponent(0)).getBoton(id).doClick());
    long t0 = System.nanoTime(); final int[] arranques = {0}; final long[] tSon = {-1}; boolean enganchado = false;
    StringBuilder orden = new StringBuilder(); String ult = ""; float max = 0; long tMax = -1, tPrimero = -1, tUltimo = -1; BufferedImage sin = null, con = null; PnlWarning nyder = null; boolean saltado = false;
    while ((System.nanoTime() - t0) / 1e6 < 6500) {
      final String[] n = {null}; SwingUtilities.invokeAndWait(() -> n[0] = nombre());
      if (!n[0].equals(ult)) { orden.append(n[0]).append(String.format("(%.0f) ", (System.nanoTime() - t0) / 1e6)); ult = n[0]; }
      if (!enganchado) { Sonido s = (Sonido) f(cm, ControllerMenu.class, "sonidoDestelloNyder"); if (s != null) { Clip c = (Clip) f(s, Sonido.class, "clip"); if (c != null) { c.addLineListener(ev -> { if (ev.getType() == LineEvent.Type.START) { arranques[0]++; if (tSon[0] < 0) tSon[0] = System.nanoTime(); } }); enganchado = true; } } }
      if (n[0].equals("NYDER")) { nyder = (PnlWarning) vp.pnlContenido.getComponent(0); float b = (Float) f(nyder, PnlWarning.class, "brilloEstrella"); float al = (Float) f(nyder, PnlWarning.class, "alpha");
        long t = (System.nanoTime() - t0) / 1000000L; if (b > 0 && tPrimero < 0) tPrimero = t; if (b > 0) tUltimo = t; if (b > max) { max = b; tMax = t; }
        if (sin == null && al >= 0.999f && b == 0f) sin = cap(nyder);
        if (con == null && b >= 0.95f) con = cap(nyder);
        if (saltar && !saltado && b > 0.5f) { saltado = true; Thread.sleep(40); SwingUtilities.invokeAndWait(() -> vp.getRootPane().getActionMap().get("debugSaltarIntro").actionPerformed(null)); System.out.println("CTRL+S con brillo " + b); } }
      Thread.sleep(8);
    }
    System.out.println("orden: " + orden);
    System.out.printf("brillo: primero a %d ms, maximo %.2f a %d ms, ultimo >0 a %d ms; sonido arranco a %.0f ms; arranques=%d%n", tPrimero, max, tMax, tUltimo, tSon[0] < 0 ? -1 : (tSon[0] - t0) / 1e6, arranques[0]);
    if (!saltar) {
      ok(orden.toString().startsWith("NYDER") && orden.indexOf("ADVERTENCIA") > 0, "orden selector -> NYDER -> advertencia conservado");
      ok(max >= 0.99f, "el destello llega al maximo");
      ok(arranques[0] == 1, "el sonido suena una sola vez");
      ok(tSon[0] > 0 && Math.abs((tSon[0] - t0) / 1e6 - tMax) < 500, "el sonido arranca cerca del pico de luz (sube con la luz)");
      if (sin != null && con != null) { javax.imageio.ImageIO.write(sin, "png", new File(out, "nyder_sin_" + id + ".png")); javax.imageio.ImageIO.write(con, "png", new File(out, "nyder_con_" + id + ".png"));
        int minX = 99999, maxX = -1, minY = 99999, maxY = -1, cambios = 0;
        for (int y = 0; y < sin.getHeight(); y++) for (int x = 0; x < sin.getWidth(); x++) { int p = sin.getRGB(x, y), q = con.getRGB(x, y); int d = Math.abs(((p >> 16) & 255) - ((q >> 16) & 255)) + Math.abs(((p >> 8) & 255) - ((q >> 8) & 255)) + Math.abs((p & 255) - (q & 255)); if (d > 24) { cambios++; minX = Math.min(minX, x); maxX = Math.max(maxX, x); minY = Math.min(minY, y); maxY = Math.max(maxY, y); } }
        System.out.printf("diferencia con/sin destello: %d pixeles en x %d-%d, y %d-%d (pantalla %dx%d)%n", cambios, minX, maxX, minY, maxY, sin.getWidth(), sin.getHeight());
        ok(cambios > 0 && (maxX - minX) < sin.getWidth() * 0.22 && (maxY - minY) < sin.getHeight() * 0.40, "el destello queda localizado en la estrella"); }
      else ok(false, "capturas sin/con destello");
    } else {
      Object tim = f(cm, ControllerMenu.class, "timerDestelloNyder");
      ok(tim == null, "tras omitir no queda timer del destello");
      ok(f(cm, ControllerMenu.class, "sonidoDestelloNyder") == null, "tras omitir el sonido se corta y se suelta");
      ok(orden.indexOf("ADVERTENCIA") > 0, "tras omitir sigue la advertencia");
    }
    System.out.println("FALLOS: " + fallos); System.exit(0); }
}
