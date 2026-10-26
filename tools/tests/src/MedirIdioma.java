import javax.swing.*; import java.awt.*; import java.lang.reflect.*;
import com.fdaf.mvc.controllers.*; import com.fdaf.mvc.views.frames.VistaPrincipal; import com.fdaf.mvc.views.frames.pnl.*; import com.fdaf.util.*;
// Con el ControllerMenu real (textos reales del idioma), lista TODOS los textos visibles de cada pantalla
// y marca los que no caben en su componente. Arg: idioma.
public class MedirIdioma { static int desbordes = 0;
  static Object f(Object o, Class<?> c, String n) throws Exception { Field x = c.getDeclaredField(n); x.setAccessible(true); return x.get(o); }
  public static void main(String[] a) throws Exception { PreferenciasJuego.idiomaSeleccionado = Idioma.valueOf(a[0]); PreferenciasJuego.escapeDesbloqueado = Boolean.parseBoolean(a[1]);
    SwingUtilities.invokeAndWait(() -> { try {
      ControllerMenu cm = new ControllerMenu(true); cm.init();
      for (String n : new String[]{"menu","opciones","pantallaIdioma","pantallaVolumen","pantallaNochePersonalizada"}) revisar(n, (Container) f(cm, ControllerMenu.class, n));
      ControllerInterfaz ci = new ControllerInterfaz(); PnlGameOver g = new PnlGameOver(); EscalarVista.adaptarGameOver(null, g);
      Method m = ControllerInterfaz.class.getDeclaredMethod("aplicarIdiomaGameOver", PnlGameOver.class); m.setAccessible(true); m.invoke(ci, g); revisar("gameOver", g);
      Method e = ControllerMenu.class.getDeclaredMethod("mostrarMensajeEscapeBloqueado"); e.setAccessible(true); PreferenciasJuego.escapeDesbloqueado = false; e.invoke(cm);
      JLabel lb = ((PnlNochePersonalizada) f(cm, ControllerMenu.class, "pantallaNochePersonalizada")).getLblEscapeBloqueado(); System.out.println("  mensajeEscapeBloqueado: " + lb.getText());
    } catch (Exception ex) { ex.printStackTrace(); } });
    System.out.println("DESBORDES=" + desbordes); System.exit(0); }
  static void revisar(String p, Container c) { for (Component k : c.getComponents()) {
      if (k instanceof Container && !(k instanceof JLabel) && !(k instanceof AbstractButton)) revisar(p, (Container) k);
      String t = k instanceof JLabel ? ((JLabel) k).getText() : k instanceof AbstractButton ? ((AbstractButton) k).getText() : k instanceof javax.swing.text.JTextComponent ? ((javax.swing.text.JTextComponent) k).getText().replace("\r\n"," ") : null;
      if (t == null || t.trim().isEmpty() || t.equals(">>")) continue;
      String marca = "";
      if (!t.startsWith("<html") && !(k instanceof javax.swing.text.JTextComponent) && k.getWidth() > 0) { FontMetrics fm = k.getFontMetrics(k.getFont()); int w = fm.stringWidth(t);
        Insets in = ((JComponent) k).getInsets(); if (w > k.getWidth() - in.left - in.right) { marca = "  <<< DESBORDA " + w + ">" + k.getWidth(); desbordes++; } }
      System.out.println("  " + p + (k.isVisible() ? "" : " (oculto)") + ": '" + t + "'" + marca); } }
}
