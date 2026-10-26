import javax.swing.*; import java.awt.*; import java.awt.image.BufferedImage; import java.lang.reflect.*; import java.io.File;
import com.fdaf.mvc.controllers.*; import com.fdaf.mvc.views.frames.VistaPrincipal; import com.fdaf.util.*; import com.fdaf.mvc.models.juego.Noche;
// Ejecución real de las intros de las 5 noches en el idioma dado: ruta usada, retardo del Timer, momento real
// en que se crea la partida, captura de la tarjeta y hilos "Image Animator" vivos antes/después.
// Args: carpetaSalida idioma
public class PruebaIntros {
  static Object f(Object o, Class<?> c, String n) throws Exception { Field x = c.getDeclaredField(n); x.setAccessible(true); return x.get(o); }
  static void edt(Runnable r) throws Exception { SwingUtilities.invokeAndWait(r); }
  static int animadores() { int n = 0; for (Thread t : Thread.getAllStackTraces().keySet()) if (t.getName().startsWith("Image Animator")) n++; return n; }
  public static void main(String[] a) throws Exception {
    PreferenciasJuego.idiomaSeleccionado = Idioma.valueOf(a[1]);
    final ControllerMenu[] cm = new ControllerMenu[1];
    edt(() -> { cm[0] = new ControllerMenu(true); cm[0].init(); });
    VistaPrincipal vp = (VistaPrincipal) f(cm[0], ControllerMenu.class, "vp"); Object menu = f(cm[0], ControllerMenu.class, "menu");
    Method intro = ControllerMenu.class.getDeclaredMethod("iniciarSecuenciaIntro"); intro.setAccessible(true);
    Field partida = ControllerMenu.class.getDeclaredField("partidaActiva"); partida.setAccessible(true);
    Thread.sleep(1500);
    for (int noche = 1; noche <= 5; noche++) {
      PreferenciasJuego.nocheActual = Noche.values()[noche - 1];
      edt(() -> { try { partida.set(cm[0], null); } catch (Exception e) { throw new RuntimeException(e); } });
      int antes = animadores(); long t0 = System.currentTimeMillis();
      edt(() -> { try { intro.invoke(cm[0]); } catch (Exception e) { throw new RuntimeException(e); } });
      String ruta = (String) f(cm[0], ControllerMenu.class, "rutaIntroActiva"); int delay = ((Timer) f(cm[0], ControllerMenu.class, "timerIntroActivo")).getInitialDelay();
      Component panel = vp.pnlContenido.getComponent(0);
      long momentoTarjeta = noche == 1 ? 12000 : 2500; Thread.sleep(Math.max(0, t0 + momentoTarjeta - System.currentTimeMillis()));
      final BufferedImage img = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
      edt(() -> { Graphics2D g = img.createGraphics(); panel.printAll(g); g.dispose(); });
      int x = (int) (560 * panel.getWidth() / 1600.0), y = (int) (300 * panel.getHeight() / 900.0), w = (int) (480 * panel.getWidth() / 1600.0), h = (int) (200 * panel.getHeight() / 900.0);
      javax.imageio.ImageIO.write(img.getSubimage(x, y, w, h), "png", new File(a[0] + "/intro_" + a[1] + "_n" + noche + ".png"));
      int durante = animadores();
      while (partida.get(cm[0]) == null && System.currentTimeMillis() - t0 < 30000) Thread.sleep(20);
      long tPartida = System.currentTimeMillis() - t0;
      Thread.sleep(1200); int despues = animadores();
      Object ci = partida.get(cm[0]);
      System.out.printf("Noche %d %s: %s  timer=%dms  partida a los %dms  animadores antes/durante/despues=%d/%d/%d  rutaActiva tras fin=%s%n",
          noche, a[1], ruta.substring(ruta.lastIndexOf('/') + 1), delay, tPartida, antes, durante, despues, f(cm[0], ControllerMenu.class, "rutaIntroActiva"));
      edt(() -> { try { ci.getClass().getMethod("detenerSonidosJuego").invoke(ci); vp.setContenido((JComponent) menu); } catch (Exception e) { throw new RuntimeException(e); } });
      Thread.sleep(500);
    }
    System.exit(0); }
}
