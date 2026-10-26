import javax.swing.*; import java.awt.*; import java.awt.event.*; import java.lang.reflect.*;
import com.fdaf.mvc.controllers.*; import com.fdaf.mvc.views.frames.VistaPrincipal; import com.fdaf.mvc.views.frames.pnl.*; import com.fdaf.util.*;
// Reproduce el bug "el botón sigue azul al volver al menú": hover real (MOUSE_ENTERED) + clic que cambia de
// pantalla + vuelta, 10 veces, para Cómo jugar, Acerca del proyecto, Opciones (flecha >>) y el botón de volver de
// PnlInfo. Cuenta listeners para detectar acumulación.
public class PruebaHoverVuelta { static int ok = 0, mal = 0;
  static Object f(Object o, Class<?> c, String n) throws Exception { Field x = c.getDeclaredField(n); x.setAccessible(true); return x.get(o); }
  static void edt(Runnable r) throws Exception { SwingUtilities.invokeAndWait(r); }
  static void entrar(Component c) throws Exception { edt(() -> c.dispatchEvent(new MouseEvent(c, MouseEvent.MOUSE_ENTERED, System.currentTimeMillis(), 0, 5, 5, 0, false))); }
  static void check(String n, boolean cond) { if (cond) ok++; else { mal++; System.out.println("FALLO " + n); } }
  public static void main(String[] a) throws Exception {
    PreferenciasJuego.idiomaSeleccionado = Idioma.valueOf(a[0]);
    final ControllerMenu[] cm = new ControllerMenu[1];
    edt(() -> { cm[0] = new ControllerMenu(true); cm[0].init(); });
    VistaPrincipal vp = (VistaPrincipal) f(cm[0], ControllerMenu.class, "vp"); PnlMenu menu = (PnlMenu) f(cm[0], ControllerMenu.class, "menu");
    PnlInfo info = (PnlInfo) f(cm[0], ControllerMenu.class, "pantallaInfo"); PnlOpciones op = (PnlOpciones) f(cm[0], ControllerMenu.class, "opciones");
    Thread.sleep(800);
    Color normal = menu.getBtnComoJugar().getForeground();
    int hlC = menu.getBtnComoJugar().getHierarchyListeners().length, mlC = menu.getBtnComoJugar().getMouseListeners().length;
    for (int i = 0; i < 10; i++) {
      for (JButton b : new JButton[]{menu.getBtnComoJugar(), menu.getBtnAcercaDe()}) {
        entrar(b); boolean resaltado = !b.getForeground().equals(normal);
        edt(b::doClick); boolean enInfo = vp.pnlContenido.getComponent(0) == info;
        Color volver = info.getBtnSiguiente().getForeground(); // "Volver al menú" en Acerca / pág. 1 Siguiente en Cómo jugar
        JButton salir = info.getBtnOmitir().isVisible() ? info.getBtnOmitir() : info.getBtnSiguiente();
        Color antesSalir = salir.getForeground(); entrar(salir); edt(salir::doClick);
        boolean enMenu = vp.pnlContenido.getComponent(0) == menu;
        check("vuelta " + i + " " + b.getText() + ": resaltado al entrar=" + resaltado + " en info=" + enInfo + " en menu=" + enMenu, resaltado && enInfo && enMenu);
        check("vuelta " + i + " " + b.getText() + " color normal al volver (" + b.getForeground() + ")", b.getForeground().equals(normal));
        check("vuelta " + i + " boton de salida de PnlInfo vuelve a su color (" + salir.getForeground() + " vs " + antesSalir + ")", salir.getForeground().equals(antesSalir));
      }
      entrar(menu.getBtnOpciones()); boolean flecha = menu.getLbl().isVisible(); edt(menu.getBtnOpciones()::doClick); edt(op.getBtnAtras()::doClick);
      check("vuelta " + i + " flecha visible al entrar en Opciones=" + flecha + " y oculta al volver=" + !menu.getLbl().isVisible(), flecha && !menu.getLbl().isVisible());
    }
    System.out.println("listeners Cómo jugar: jerarquía " + hlC + "->" + menu.getBtnComoJugar().getHierarchyListeners().length + ", ratón " + mlC + "->" + menu.getBtnComoJugar().getMouseListeners().length);
    check("sin acumulación de listeners", hlC == menu.getBtnComoJugar().getHierarchyListeners().length && mlC == menu.getBtnComoJugar().getMouseListeners().length);
    // Hover real al volver: si el cursor está encima, el siguiente MOUSE_ENTERED vuelve a resaltar.
    entrar(menu.getBtnComoJugar()); check("hover sigue funcionando tras las vueltas", !menu.getBtnComoJugar().getForeground().equals(normal));
    System.out.println("RESULTADO: " + ok + " OK, " + mal + " fallos"); System.exit(0); }
}
