import javax.swing.*; import java.awt.*; import java.awt.image.BufferedImage; import java.lang.reflect.*; import java.io.File;
import com.fdaf.mvc.controllers.*; import com.fdaf.mvc.views.frames.VistaPrincipal; import com.fdaf.mvc.views.frames.pnl.*; import com.fdaf.util.*; import com.fdaf.mvc.models.juego.Noche;
// Ejecución real: intro de la noche con el gif real (Toolkit, animado en tiempo real), capturas del panel en
// distintos momentos, inicio de la partida, llamada que bloquea la puerta, fin NATURAL de la llamada y desbloqueo.
// Args: carpetaSalida idioma noche
public class PruebaIntroLlamada {
  static Object f(Object o, Class<?> c, String n) throws Exception { Field x = c.getDeclaredField(n); x.setAccessible(true); return x.get(o); }
  static void edt(Runnable r) throws Exception { SwingUtilities.invokeAndWait(r); }
  public static void main(String[] a) throws Exception {
    PreferenciasJuego.idiomaSeleccionado = Idioma.valueOf(a[1]); PreferenciasJuego.nocheActual = Noche.values()[Integer.parseInt(a[2]) - 1];
    final ControllerMenu[] cm = new ControllerMenu[1];
    edt(() -> { cm[0] = new ControllerMenu(true); cm[0].init(); });
    VistaPrincipal vp = (VistaPrincipal) f(cm[0], ControllerMenu.class, "vp");
    Method intro = ControllerMenu.class.getDeclaredMethod("iniciarSecuenciaIntro"); intro.setAccessible(true);
    long t0 = System.currentTimeMillis();
    edt(() -> { try { intro.invoke(cm[0]); } catch (Exception e) { throw new RuntimeException(e); } });
    Component panel = vp.pnlContenido.getComponent(0); System.out.println("panel de intro: " + panel.getClass().getSimpleName());
    long[] momentos = {5000, 12000, 14950, 15150, 16500};
    for (long m : momentos) { long espera = t0 + m - System.currentTimeMillis(); if (espera > 0) Thread.sleep(espera);
      final BufferedImage img = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
      edt(() -> { Graphics2D g = img.createGraphics(); panel.printAll(g); g.dispose(); });
      int x = (int) (560 * panel.getWidth() / 1600.0), y = (int) (300 * panel.getHeight() / 900.0), w = (int) (480 * panel.getWidth() / 1600.0), h = (int) (200 * panel.getHeight() / 900.0);
      javax.imageio.ImageIO.write(img.getSubimage(x, y, w, h), "png", new File(a[0] + "/vivo_" + a[1] + "_n" + a[2] + "_" + m + ".png"));
      System.out.println("captura t=" + (System.currentTimeMillis() - t0) + "ms"); }
    long espera = t0 + 20600 - System.currentTimeMillis(); if (espera > 0) Thread.sleep(espera);
    ControllerInterfaz ci = (ControllerInterfaz) f(cm[0], ControllerMenu.class, "partidaActiva");
    System.out.println("t=" + (System.currentTimeMillis() - t0) + "ms partida creada: " + (ci != null));
    ControllerJuego cj = (ControllerJuego) f(ci, ControllerInterfaz.class, "controllerJuego"); PnlJuego j = (PnlJuego) f(ci, ControllerInterfaz.class, "pnlJuego");
    Object amb = f(ci, ControllerInterfaz.class, "sonidosAmbientales");
    System.out.println("llamada activa=" + cj.isLlamadaActiva() + " ambiente activo=" + f(amb, amb.getClass(), "activo"));
    edt(() -> j.getBotonIzq().doClick());
    System.out.println("clic PUERTA izq durante la llamada -> puertaIzqAbierta=" + f(cj, ControllerJuego.class, "puertaIzqAbierta"));
    long inicioPartida = System.currentTimeMillis(); int cambios = 0; boolean anterior = true;
    while (System.currentTimeMillis() - inicioPartida < 130000) { boolean act = cj.isLlamadaActiva(); if (act != anterior) { cambios++; anterior = act;
        System.out.println("llamada terminó sola a los " + (System.currentTimeMillis() - inicioPartida) + "ms de partida; MUTE visible=" + j.getLblMuteCall().isVisible() + " ambiente activo=" + f(amb, amb.getClass(), "activo")); }
      if (!act && System.currentTimeMillis() - inicioPartida > 0 && cambios > 0) { Thread.sleep(3000); break; } Thread.sleep(100); }
    System.out.println("cambios de estado de la llamada: " + cambios + " (esperado 1) ; llamada activa ahora=" + cj.isLlamadaActiva());
    edt(() -> j.getBotonIzq().doClick());
    System.out.println("clic PUERTA izq tras la llamada -> puertaIzqAbierta=" + f(cj, ControllerJuego.class, "puertaIzqAbierta"));
    edt(() -> ci.detenerSonidosJuego()); System.exit(0); }
}
