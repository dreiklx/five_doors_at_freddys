import java.awt.*; import java.awt.image.BufferedImage; import javax.swing.*; import java.io.File; import java.lang.reflect.*;
import com.fdaf.mvc.controllers.*; import com.fdaf.mvc.views.frames.VistaPrincipal; import com.fdaf.mvc.views.frames.pnl.*; import com.fdaf.util.*; import com.fdaf.mvc.models.juego.Noche;
// Renderiza el HUD de la oficina y la tableta reales (init real de ControllerInterfaz) en el idioma/noche dados.
public class RenderHud { public static void main(String[] a) throws Exception {
  PreferenciasJuego.idiomaSeleccionado = Idioma.valueOf(a[1]); PreferenciasJuego.nocheActual = Noche.values()[Integer.parseInt(a[2]) - 1];
  SwingUtilities.invokeAndWait(() -> { try {
    VistaPrincipal vp = new VistaPrincipal(); ControllerInterfaz ci = new ControllerInterfaz(); ci.init(vp, new PnlMenu(), null);
    PnlJuego j = (PnlJuego) campo(ci, "pnlJuego"); PnlTableta t = (PnlTableta) campo(ci, "pnlTableta");
    guardar(j, a[0] + "_juego.png"); guardar(t, a[0] + "_tableta.png");
    JLabel n = t.getLblNocheActual(); System.out.println("noche label icon=" + (n.getIcon()==null?"null":n.getIcon().getIconWidth()+"x"+n.getIcon().getIconHeight()) + " caja=" + n.getWidth()+"x"+n.getHeight()+" texto='"+n.getText()+"'");
    JLabel p = j.getLblPowerLeft(); System.out.println("energia HUD icon=" + p.getIcon().getIconWidth()+"x"+p.getIcon().getIconHeight()+" caja="+p.getBounds());
    JLabel pc = t.getLblCopyPowerLeft(); System.out.println("energia tableta icon=" + pc.getIcon().getIconWidth()+"x"+pc.getIcon().getIconHeight()+" caja="+pc.getBounds());
  } catch (Exception e) { e.printStackTrace(); } }); Thread.sleep(500); System.exit(0); }
  static Object campo(Object o, String n) throws Exception { Field f = o.getClass().getDeclaredField(n); f.setAccessible(true); return f.get(o); }
  static void guardar(JComponent c, String ruta) throws Exception { c.setSize(1920, 1080); c.doLayout(); BufferedImage img = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
    // drawImage escalado de un GIF animado es asincrono: la primera pasada sobre un panel nunca mostrado no dibuja nada
    // (la camara de la tableta salia en blanco, CLAUDE.md §2.88). Una pasada de calentamiento y una espera lo resuelven.
    Graphics2D g0 = img.createGraphics(); c.printAll(g0); g0.dispose(); Thread.sleep(600);
    Graphics2D g = img.createGraphics(); c.printAll(g); g.dispose(); javax.imageio.ImageIO.write(img, "png", new File(ruta)); }
}
