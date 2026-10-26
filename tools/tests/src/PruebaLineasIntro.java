import javax.swing.*; import java.awt.*; import java.awt.image.BufferedImage; import java.lang.reflect.*; import javax.sound.sampled.*;
import com.fdaf.mvc.controllers.*; import com.fdaf.mvc.views.frames.VistaPrincipal; import com.fdaf.util.*; import com.fdaf.mvc.models.juego.Noche;
// Intros reales de las 5 noches: momento en que se VEN las franjas blancas (captura del panel cada ~15 ms) frente al
// momento en que arranca el Clip del sonido de la tableta (LineListener START); cuantas veces suena; duracion de la intro.
// Args: idioma [noches]   Prueba ademas que CTRL+S (finalizar) antes de las franjas cancela el sonido (noche 1).
public class PruebaLineasIntro {
  static Object f(Object o, Class<?> c, String n) throws Exception { Field x = c.getDeclaredField(n); x.setAccessible(true); return x.get(o); }
  static void edt(Runnable r) throws Exception { SwingUtilities.invokeAndWait(r); }
  static volatile long tSonido = -1; static volatile int arranques = 0;
  static boolean franjas(BufferedImage img) { int w=img.getWidth(), h=img.getHeight(), filas=0; for (int y=0;y<h;y+=2){ int b=0; for(int x=0;x<w;x+=4){ int c=img.getRGB(x,y); if(((c>>16)&255)>200&&((c>>8)&255)>200&&(c&255)>200) b++; } if(b*4*2>w) filas++; } return filas*2 >= h/15; }
  public static void main(String[] a) throws Exception {
    PreferenciasJuego.idiomaSeleccionado = Idioma.valueOf(a[0]);
    Field fs = InteraccionBoton.class.getDeclaredField("SONIDO_HOVER"); fs.setAccessible(true); Object son = fs.get(null);
    Clip clip = (Clip) f(son, son.getClass(), "clip");
    clip.addLineListener(ev -> { if (ev.getType() == LineEvent.Type.START) { arranques++; if (tSonido < 0) tSonido = System.nanoTime(); } });
    final ControllerMenu[] cm = new ControllerMenu[1];
    edt(() -> { cm[0] = new ControllerMenu(true); cm[0].init(); });
    VistaPrincipal vp = (VistaPrincipal) f(cm[0], ControllerMenu.class, "vp"); Object menu = f(cm[0], ControllerMenu.class, "menu");
    Method intro = ControllerMenu.class.getDeclaredMethod("iniciarSecuenciaIntro"); intro.setAccessible(true);
    Method fin = ControllerMenu.class.getDeclaredMethod("finalizarIntroYComenzarPartida"); fin.setAccessible(true);
    Field partida = ControllerMenu.class.getDeclaredField("partidaActiva"); partida.setAccessible(true);
    Thread.sleep(2500); int fallos = 0;
    String noches = a.length > 1 ? a[1] : "12345";
    for (char cn : noches.toCharArray()) { int noche = cn - '0';
      PreferenciasJuego.nocheActual = Noche.values()[noche - 1];
      edt(() -> { try { partida.set(cm[0], null); } catch (Exception e) { throw new RuntimeException(e); } });
      Thread.sleep(400); tSonido = -1; arranques = 0;
      long t0 = System.nanoTime();
      edt(() -> { try { intro.invoke(cm[0]); } catch (Exception e) { throw new RuntimeException(e); } });
      int duracion = ((Timer) f(cm[0], ControllerMenu.class, "timerIntroActivo")).getInitialDelay();
      Component panel = vp.pnlContenido.getComponent(0); long tVisto = -1;
      final BufferedImage img = new BufferedImage(panel.getWidth()/2, panel.getHeight()/2, BufferedImage.TYPE_INT_RGB);
      long limite = (noche == 1 ? 13000 : 3000);
      while ((System.nanoTime() - t0) / 1e6 < limite) {
        edt(() -> { Graphics2D g = img.createGraphics(); g.scale(0.5, 0.5); panel.paint(g); g.dispose(); });
        if (tVisto < 0 && franjas(img)) tVisto = System.nanoTime();
        Thread.sleep(5);
      }
      while (partida.get(cm[0]) == null && (System.nanoTime() - t0) / 1e6 < 30000) Thread.sleep(20);
      long tPartida = (System.nanoTime() - t0) / 1000000L;
      double visto = tVisto < 0 ? -1 : (tVisto - t0) / 1e6, sonido = tSonido < 0 ? -1 : (tSonido - t0) / 1e6;
      boolean ok = tVisto > 0 && tSonido > 0 && Math.abs(sonido - visto) < 150 && arranques == 1 && Math.abs(tPartida - duracion) < 400;
      if (!ok) fallos++;
      System.out.printf("%s Noche %d %s: franjas visibles a %.0f ms, sonido a %.0f ms (diferencia %+.0f ms), arranques del clip=%d, intro %d ms (timer %d)%n",
          ok ? "OK   " : "FALLO", noche, a[0], visto, sonido, sonido - visto, arranques, tPartida, duracion);
      Object ci = partida.get(cm[0]);
      edt(() -> { try { ci.getClass().getMethod("detenerSonidosJuego").invoke(ci); vp.setContenido((JComponent) menu); } catch (Exception e) { throw new RuntimeException(e); } });
    }
    // CTRL+S (finalizar) antes de las franjas de la Noche 1: no debe sonar despues
    PreferenciasJuego.nocheActual = Noche.NOCHE_1;
    edt(() -> { try { partida.set(cm[0], null); } catch (Exception e) { throw new RuntimeException(e); } });
    Thread.sleep(400); tSonido = -1; arranques = 0;
    edt(() -> { try { intro.invoke(cm[0]); } catch (Exception e) { throw new RuntimeException(e); } });
    Thread.sleep(3000); edt(() -> { try { fin.invoke(cm[0]); } catch (Exception e) { throw new RuntimeException(e); } });
    Thread.sleep(9500); Object ci = partida.get(cm[0]);
    boolean ok = arranques == 0 && !((java.util.concurrent.atomic.AtomicBoolean) f(cm[0], ControllerMenu.class, "sonidoFranjasPendiente")).get(); if (!ok) fallos++;
    System.out.println((ok ? "OK    " : "FALLO ") + "saltar la intro de la Noche 1 a los 3 s cancela el sonido (arranques=" + arranques + ")");
    edt(() -> { try { ci.getClass().getMethod("detenerSonidosJuego").invoke(ci); } catch (Exception e) { } });
    System.out.println("FALLOS: " + fallos); System.exit(0); }
}
