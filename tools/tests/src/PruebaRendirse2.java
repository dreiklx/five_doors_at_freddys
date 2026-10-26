import java.awt.*; import java.awt.event.*; import java.awt.image.BufferedImage; import javax.swing.*; import java.io.File; import java.lang.reflect.*;
import com.fdaf.mvc.views.frames.VistaPrincipal; import com.fdaf.util.*;
// Rendirse v2: preparacion con barra retro, respuesta de la camara al raton, toque de ESC (no omite), ESC mantenido
// (omite una vez), raton mantenido sobre el indicador (omite), fin natural. Ventana fuera de pantalla.
// Uso: PruebaRendirse2 <salida> <modo: camara|toque|mantener|raton|natural> <ESPANOL|INGLES>
public class PruebaRendirse2 {
  static VistaPrincipal vp; static CinematicaRendirse cin; static int fallos = 0; static volatile int finales = 0; static long tFin;
  static void ok(boolean b, String s) { System.out.println((b ? "OK    " : "FALLO ") + s); if (!b) fallos++; }
  static Object f(String n) throws Exception { Field x = CinematicaRendirse.class.getDeclaredField(n); x.setAccessible(true); return x.get(cin); }
  static JComponent capa() { for (Component c : vp.getLayeredPane().getComponentsInLayer(JLayeredPane.POPUP_LAYER)) if (c instanceof JPanel) return (JComponent) c; return null; }
  static void accion(String n) throws Exception { SwingUtilities.invokeAndWait(() -> { JComponent p = capa(); if (p != null) p.getActionMap().get(n).actionPerformed(new ActionEvent(p, 0, "")); }); }
  static void captura(String out, String nombre) throws Exception { SwingUtilities.invokeAndWait(() -> { try { JLayeredPane lp = vp.getLayeredPane();
      BufferedImage img = new BufferedImage(lp.getWidth(), lp.getHeight(), BufferedImage.TYPE_INT_RGB); Graphics2D g = img.createGraphics(); lp.paintAll(g); g.dispose();
      javax.imageio.ImageIO.write(img, "png", new File(out + "_" + nombre + ".png")); } catch (Exception e) { throw new RuntimeException(e); } }); }
  static boolean hilo(String n) { for (Thread t : Thread.getAllStackTraces().keySet()) if (t.getName().equals(n) && t.isAlive()) return true; return false; }
  public static void main(String[] a) throws Exception {
    String out = a[0], modo = a[1]; PreferenciasJuego.idiomaSeleccionado = Idioma.valueOf(a[2]);
    SwingUtilities.invokeAndWait(() -> { vp = new VistaPrincipal(); vp.setVisible(true); vp.setExtendedState(Frame.NORMAL); vp.setBounds(-5000, 0, 1920, 1080);
      JPanel oficina = new JPanel(); oficina.setBackground(new Color(70, 40, 40)); vp.setContenido(oficina); });
    Thread.sleep(500);
    long t0 = System.nanoTime();
    SwingUtilities.invokeAndWait(() -> { cin = new CinematicaRendirse(); cin.iniciar(vp, () -> { finales++; tFin = System.nanoTime(); }); });
    Thread.sleep(60); captura(out, "preparacion");
    while ((Boolean) f("preparando") && finales == 0) Thread.sleep(5);
    System.out.println("      la linea de tiempo arranco a los " + (System.nanoTime() - t0) / 1000000 + " ms");
    if (modo.equals("camara")) {
      Thread.sleep(1200);
      // mover el raton al 95 % del ancho: objetivo 1.0
      for (int[] caso : new int[][] { { 95, 100 }, { 5, 0 } }) {
        float inicio = (Float) f("paneo"); float objetivo = caso[1] / 100f;
        SwingUtilities.invokeAndWait(() -> { JComponent p = capa(); for (MouseMotionListener l : p.getMouseMotionListeners()) l.mouseMoved(new MouseEvent(p, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, p.getWidth() * caso[0] / 100, 500, 0, false)); });
        long m0 = System.nanoTime(); float prev = inicio; float maxSalto = 0; long llego = -1;
        while ((System.nanoTime() - m0) / 1e6 < 2500) { Thread.sleep(8); float v = (Float) f("paneo"); maxSalto = Math.max(maxSalto, Math.abs(v - prev)); prev = v;
          if (llego < 0 && Math.abs(v - objetivo) <= 0.05f * Math.abs(objetivo - inicio)) llego = System.nanoTime(); }
        System.out.println(String.format("      raton a %d%%: paneo %.2f -> %.2f, 95%% del recorrido en %d ms, mayor salto entre lecturas %.3f", caso[0], inicio, objetivo, llego < 0 ? -1 : (llego - m0) / 1000000, maxSalto));
        ok(llego > 0 && (llego - m0) / 1e6 < 450, "la mirada llega al borde en menos de 0.45 s (antes ~1.6 s)");
        ok(maxSalto < 0.12f, "sin saltos bruscos (mayor paso entre lecturas de 8 ms < 0.12 del ancho)");
        captura(out, "camara_" + caso[0]);
      }
      Thread.sleep(2200); float g = (Float) f("paneo"); System.out.println("      1.4 s sin raton -> el guion retoma, paneo " + g); long e0 = System.nanoTime(); while (finales == 0 && (System.nanoTime() - e0) / 1e6 < 8000) Thread.sleep(10); ok(finales == 1, "termina sola despues de mover la camara");
    } else if (modo.equals("toque")) {
      Thread.sleep(800); accion("omision-apretar"); Thread.sleep(250); accion("omision-soltar"); Thread.sleep(1500);
      ok(finales == 0, "un toque de 0.25 s no omite"); accion("omision-apretar"); Thread.sleep(700); accion("omision-soltar"); Thread.sleep(900);
      ok(finales == 0, "mantener 0.7 s y soltar no omite (el progreso se reinicia)");
      accion("omision-apretar"); Thread.sleep(700); captura(out, "omitiendo"); Thread.sleep(800); accion("omision-soltar"); Thread.sleep(300);
      ok(finales == 1, "despues de reiniciar, mantener 1.5 s omite una sola vez (finales=" + finales + ")");
    } else if (modo.equals("mantener")) {
      Thread.sleep(500); long h0 = System.nanoTime(); accion("omision-apretar");
      while (finales == 0 && (System.nanoTime() - h0) / 1e6 < 3000) Thread.sleep(5);
      long ms = (tFin - h0) / 1000000; accion("omision-soltar"); Thread.sleep(600);
      System.out.println("      omitida tras " + ms + " ms de ESC mantenido");
      ok(finales == 1 && ms >= 1150 && ms < 1400, "mantener ESC omite a los ~1.2 s, una sola vez");
      accion("omision-apretar"); Thread.sleep(1400); accion("omision-soltar"); ok(finales == 1, "seguir manteniendo despues no dispara otra vez");
    } else if (modo.equals("raton")) {
      Thread.sleep(500); final Point zona = new Point();
      SwingUtilities.invokeAndWait(() -> { JComponent p = capa(); zona.setLocation(p.getWidth() - 30, p.getHeight() - 30);
        for (MouseListener l : p.getMouseListeners()) l.mousePressed(new MouseEvent(p, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, zona.x, zona.y, 1, false)); });
      long h0 = System.nanoTime(); while (finales == 0 && (System.nanoTime() - h0) / 1e6 < 3000) Thread.sleep(5);
      ok(finales == 1 && (tFin - h0) / 1e6 > 1100, "mantener el raton sobre el indicador omite (" + (tFin - h0) / 1000000 + " ms)");
    } else {
      long n0 = System.nanoTime(); while (finales == 0 && (System.nanoTime() - n0) / 1e6 < 16000) Thread.sleep(10);
      ok(finales == 1, "fin natural, una sola vez (" + (tFin - n0) / 1000000 + " ms desde el inicio de la linea de tiempo)");
    }
    Thread.sleep(300);
    ok(!hilo("cinematica-rendirse") && !hilo("cinematica-rendirse-prep"), "no quedan hilos de la cinematica");
    SwingUtilities.invokeAndWait(() -> ok(capa() == null, "la capa se retiro"));
    System.out.println("FALLOS: " + fallos); System.exit(0);
  }
}
