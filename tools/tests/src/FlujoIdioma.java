import javax.swing.*; import java.awt.*; import java.lang.reflect.*; import java.nio.file.*;
import com.fdaf.mvc.controllers.ControllerMenu; import com.fdaf.mvc.views.frames.VistaPrincipal; import com.fdaf.mvc.views.frames.pnl.*; import com.fdaf.util.*;
public class FlujoIdioma { static ControllerMenu cm; static VistaPrincipal vp; static PnlMenu menu;
  static Object f(Object o, Class<?> c, String n) throws Exception { Field x = c.getDeclaredField(n); x.setAccessible(true); return x.get(o); }
  static String pantalla() { Component c = vp.pnlContenido.getComponent(0); String n = c.getClass().getSimpleName();
    try { if (c instanceof PnlWarning) { double fr = (Double) f(c, PnlWarning.class, "fraccionAncho"); Image im = (Image) f(c, PnlWarning.class, "imagenAdvertencia");
        n = fr > 0 ? "NYDER" : "ADVERTENCIA(imagen " + im.getWidth(null) + "px = " + (im.getWidth(null) == 1672 ? "ESPANOL" : "INGLES") + ")"; }
      if (c instanceof PnlSelectorIdioma) n = "SELECTOR[resaltado=" + ((PnlSelectorIdioma) c).getResaltado() + " titulo='" + ((PnlSelectorIdioma) c).getLblTitulo().getText() + "']";
      if (c instanceof PnlInfo) { PnlInfo p = (PnlInfo) c; n = "INFO[" + p.getLblTitulo().getText() + " | " + p.getBtnSiguiente().getText() + " | " + p.getBtnOmitir().getText() + "]"; } } catch (Exception e) { n += " ERR " + e; } return n; }
  static String leer() throws Exception { final String[] p = {null}; SwingUtilities.invokeAndWait(() -> p[0] = pantalla()); return p[0]; }
  static String cfg() throws Exception { for (String l : Files.readAllLines(Paths.get(System.getProperty("user.home"), ".fivedoorsatfreddys", "config.properties"))) if (l.startsWith("idioma")) return l; return "?"; }
  public static void main(String[] a) throws Exception {
    PersistenciaJuego.cargar(); System.out.println("preferencia guardada al abrir: " + PreferenciasJuego.idiomaSeleccionado);
    Idioma elegir = Idioma.valueOf(a[0]);
    SwingUtilities.invokeAndWait(() -> { cm = new ControllerMenu(false); cm.init(); try { vp = (VistaPrincipal) f(cm, ControllerMenu.class, "vp"); menu = (PnlMenu) f(cm, ControllerMenu.class, "menu"); } catch (Exception e) { throw new RuntimeException(e); } });
    Thread.sleep(600); System.out.println("t0 " + leer());
    // CTRL+S no debe saltar el selector
    SwingUtilities.invokeAndWait(() -> vp.getRootPane().getActionMap().get("debugSaltarIntro").actionPerformed(null)); System.out.println("tras CTRL+S " + leer());
    // hover sobre la otra opcion cambia el titulo
    SwingUtilities.invokeAndWait(() -> { PnlSelectorIdioma s = (PnlSelectorIdioma) vp.pnlContenido.getComponent(0); JButton b = s.getBoton(elegir); for (java.awt.event.MouseListener ml : b.getMouseListeners()) ml.mouseEntered(new java.awt.event.MouseEvent(b, 0, 0, 0, 1, 1, 0, false)); }); System.out.println("hover " + elegir + " " + leer());
    SwingUtilities.invokeAndWait(() -> ((PnlSelectorIdioma) vp.pnlContenido.getComponent(0)).getBoton(elegir).doClick());
    System.out.println("elegido " + elegir + " -> preferencia=" + PreferenciasJuego.idiomaSeleccionado + " archivo: " + cfg());
    String ultimo = ""; for (int i = 0; i < 300; i++) { String s = leer(); String tipo = s.replaceAll("[(\\[].*", ""); if (!tipo.equals(ultimo)) System.out.println("  " + s); ultimo = tipo; if (tipo.equals("INFO")) break; Thread.sleep(50); }
    SwingUtilities.invokeAndWait(() -> { PnlInfo p = (PnlInfo) vp.pnlContenido.getComponent(0); p.getBtnSiguiente().doClick(); System.out.println("  " + pantalla()); p.getBtnSiguiente().doClick(); System.out.println("  " + pantalla()); p.getBtnSiguiente().doClick(); });
    SwingUtilities.invokeAndWait(() -> System.out.println("  MENU: " + menu.getBtnNuevaPartida().getText() + " / " + menu.getBtnContinuar().getText() + " / " + menu.getBtnOpciones().getText() + " / " + menu.getBtnComoJugar().getText() + " / " + menu.getBtnAcercaDe().getText() + " / " + menu.getLblVersion().getText()));
    SwingUtilities.invokeAndWait(() -> menu.getBtnComoJugar().doClick()); System.out.println("  Como jugar: " + leer());
    SwingUtilities.invokeAndWait(() -> { try { ((PnlInfo) vp.pnlContenido.getComponent(0)).getBtnOmitir().doClick(); menu.getBtnAcercaDe().doClick(); } catch (Exception e) {} }); System.out.println("  Acerca: " + leer());
    SwingUtilities.invokeAndWait(() -> { try { ((PnlInfo) vp.pnlContenido.getComponent(0)).getBtnSiguiente().doClick();
      PnlOpciones op = (PnlOpciones) f(cm, ControllerMenu.class, "opciones"); PnlIdioma pi = (PnlIdioma) f(cm, ControllerMenu.class, "pantallaIdioma");
      menu.getBtnOpciones().doClick(); System.out.println("  Opciones titulo: " + op.getLblTitulo().getText() + " / " + op.getBtnSalir().getText());
      op.getBtnIdioma().doClick(); (elegir == Idioma.ESPANOL ? pi.getBtnIngles() : pi.getBtnEspanol()).doClick();
      System.out.println("  Cambio en Opciones -> preferencia=" + PreferenciasJuego.idiomaSeleccionado + " archivo: " + cfg() + " | menu: " + menu.getBtnNuevaPartida().getText() + " / " + menu.getBtnComoJugar().getText() + " | opciones: " + op.getLblTitulo().getText());
    } catch (Exception e) { e.printStackTrace(); } });
    SwingUtilities.invokeAndWait(() -> { try { menu.getBtnComoJugar().doClick(); } catch (Exception e) {} }); System.out.println("  Como jugar tras cambio: " + leer());
    System.exit(0); } }
