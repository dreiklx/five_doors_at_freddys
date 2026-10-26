import javax.swing.*; import java.awt.*; import java.awt.event.*; import java.awt.image.BufferedImage; import java.lang.reflect.*; import java.io.*; import java.nio.file.*; import java.util.*;
import com.fdaf.mvc.controllers.*; import com.fdaf.mvc.views.frames.VistaPrincipal; import com.fdaf.mvc.views.frames.pnl.*; import com.fdaf.util.*; import com.fdaf.mvc.models.juego.Noche;
// Estrella de Escape, agradecimiento y noches personalizadas bloqueadas, con ControllerMenu real y perfil aislado (-Duser.home).
// Args: carpetaCapturas idioma
public class PruebaLogro {
  static ControllerMenu cm; static VistaPrincipal vp; static PnlMenu menu; static int fallos = 0; static String out;
  static File dir() { return new File(System.getProperty("user.home"), ".fivedoorsatfreddys"); }
  static File flag() { return new File(dir(), "escape_victoria.flag"); }
  static Properties cfg() throws Exception { Properties p = new Properties(); try (InputStream in = new FileInputStream(new File(dir(), "config.properties"))) { p.load(in); } return p; }
  static Object f(Object o, Class<?> c, String n) throws Exception { Field x = c.getDeclaredField(n); x.setAccessible(true); return x.get(o); }
  static void ok(boolean b, String s) { System.out.println((b ? "OK    " : "FALLO ") + s); if (!b) fallos++; }
  static void edt(Runnable r) throws Exception { SwingUtilities.invokeAndWait(r); }
  static Component actual() throws Exception { final Component[] c = {null}; edt(() -> c[0] = vp.pnlContenido.getComponent(0)); return c[0]; }
  static void cap(Component c, String n) throws Exception { BufferedImage img = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_RGB); edt(() -> { Graphics2D g = img.createGraphics(); c.printAll(g); g.dispose(); }); javax.imageio.ImageIO.write(img, "png", new File(out, n + ".png")); }
  static void tecla(int k) throws Exception { edt(() -> { JRootPane r = vp.getRootPane(); KeyEvent e = new KeyEvent(r, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, k, KeyEvent.CHAR_UNDEFINED); SwingUtilities.processKeyBindings(e); }); }
  static void escribirFlag() throws Exception { dir().mkdirs(); Files.write(flag().toPath(), "123".getBytes()); }
  static void entrar(JButton b) throws Exception { edt(() -> { for (MouseListener ml : b.getMouseListeners()) ml.mouseEntered(new MouseEvent(b, MouseEvent.MOUSE_ENTERED, 0, 0, 1, 1, 0, false)); }); }
  static void salir(JButton b) throws Exception { edt(() -> { for (MouseListener ml : b.getMouseListeners()) ml.mouseExited(new MouseEvent(b, MouseEvent.MOUSE_EXITED, 0, 0, 1, 1, 0, false)); }); }
  public static void main(String[] a) throws Exception {
    out = a[0]; Idioma idioma = Idioma.valueOf(a[1]);
    // ---- 1. migracion conservadora: archivo viejo con escapeDesbloqueado=true y sin claves nuevas
    dir().mkdirs(); Files.write(new File(dir(), "config.properties").toPath(), ("idioma=" + idioma + "\nnocheActual=NOCHE_5\nvolumen=7\nescapeDesbloqueado=true\n").getBytes());
    PersistenciaJuego.cargar();
    ok(!PreferenciasJuego.nocheSuperada(Noche.NOCHE_1) && !PreferenciasJuego.nocheSuperada(Noche.NOCHE_4) && PreferenciasJuego.nocheSuperada(Noche.NOCHE_5) && !PreferenciasJuego.escapeCompletado,
       "migracion: archivo viejo con Escape desbloqueado -> solo Noche 5 superada, sin estrella");
    // ---- 2. jugador nuevo
    Files.write(new File(dir(), "config.properties").toPath(), ("idioma=" + idioma + "\n").getBytes()); PersistenciaJuego.cargar();
    ok(!PreferenciasJuego.escapeDesbloqueado && Arrays.toString(PreferenciasJuego.nochesSuperadas).equals("[false, false, false, false, false]"), "jugador nuevo: ninguna noche superada");
    edt(() -> { cm = new ControllerMenu(true); cm.init(); try { vp = (VistaPrincipal) f(cm, ControllerMenu.class, "vp"); menu = (PnlMenu) f(cm, ControllerMenu.class, "menu"); } catch (Exception e) { throw new RuntimeException(e); } });
    Thread.sleep(1500);
    ok(actual() == menu && !menu.getLblEstrellaEscape().isVisible(), "sin victoria: menu directo y sin estrella");
    PnlNochePersonalizada noches = (PnlNochePersonalizada) f(cm, ControllerMenu.class, "pantallaNochePersonalizada");
    edt(() -> vp.setContenido(noches)); Thread.sleep(300);
    String textos = ""; for (JButton b : noches.getBtnsNoche()) textos += b.getText() + " | "; System.out.println("   botones: " + textos);
    boolean grises = true; for (JButton b : noches.getBtnsNoche()) grises &= b.getForeground().getRed() == 140;
    ok(grises && textos.contains(idioma == Idioma.INGLES ? "(Locked)" : "(Bloqueada)"), "las 5 noches bloqueadas se ven en gris con el indicador");
    Noche antes = PreferenciasJuego.nocheActual;
    edt(() -> noches.getBtnsNoche()[2].doClick()); Thread.sleep(150);
    ok(PreferenciasJuego.nocheActual == antes && noches.getLblEscapeBloqueado().isVisible(), "clic en Noche 3 bloqueada: no la elige y muestra '" + noches.getLblEscapeBloqueado().getText().replaceAll("<[^>]+>", "") + "'");
    ok(!new File(dir(), "config.properties").exists() || !cfg().getProperty("nocheActual", "NOCHE_1").equals("NOCHE_3"), "el clic bloqueado no cambia el archivo");
    entrar(noches.getBtnsNoche()[2]); ok(!noches.getLbl().isVisible(), "hover sobre una noche bloqueada: sin flecha"); salir(noches.getBtnsNoche()[2]);
    FontMetrics fm = noches.getBtnsNoche()[0].getFontMetrics(noches.getBtnsNoche()[0].getFont()); int anchoMax = 0;
    for (JButton b : noches.getBtnsNoche()) anchoMax = Math.max(anchoMax, fm.stringWidth("> " + b.getText().replace("> ", "")));
    ok(anchoMax <= noches.getBtnsNoche()[0].getWidth() && noches.getBtnsNoche()[0].getX() + noches.getBtnsNoche()[0].getWidth() <= noches.getBtnEscape().getX(),
       "el texto bloqueado mas largo cabe (" + anchoMax + " <= " + noches.getBtnsNoche()[0].getWidth() + ") sin invadir la columna de ESCAPE");
    cap(noches, "noches_bloqueadas_" + idioma);
    // ---- 3. supera Noche 1 y Noche 2 (alGanar real marca el historial) -> se desbloquean
    for (int n = 0; n < 2; n++) { PreferenciasJuego.nocheActual = Noche.values()[n]; final ControllerInterfaz[] ci = {null};
      edt(() -> { ci[0] = new ControllerInterfaz(); ci[0].init(vp, menu, cm); }); Thread.sleep(300);
      edt(() -> ci[0].alGanar()); Thread.sleep(400);
      edt(() -> { try { ci[0].detenerSonidosJuego(); } catch (Exception e) { } }); }
    Properties p = cfg(); System.out.println("   config tras ganar 1 y 2: nochesSuperadas=" + p.getProperty("nochesSuperadas") + " nocheActual=" + p.getProperty("nocheActual"));
    ok("NOCHE_1,NOCHE_2".equals(p.getProperty("nochesSuperadas")) && "NOCHE_3".equals(p.getProperty("nocheActual")), "ganar Noche 1 y 2 las marca como superadas y avanza a la 3");
    edt(() -> { cm.actualizarTextosIdioma(); vp.setContenido(noches); }); Thread.sleep(200);
    edt(() -> noches.getBtnsNoche()[0].doClick()); Thread.sleep(100);
    ok(PreferenciasJuego.nocheActual == Noche.NOCHE_1 && noches.getBtnsNoche()[0].getForeground().equals(Color.WHITE) && noches.getBtnsNoche()[2].getForeground().getRed() == 140, "Noche 1 superada se puede elegir; la 3 sigue bloqueada");
    entrar(noches.getBtnsNoche()[1]); ok(noches.getLbl().isVisible(), "hover sobre una noche desbloqueada: con flecha (como antes)"); salir(noches.getBtnsNoche()[1]);
    cap(noches, "noches_1y2_" + idioma);
    // ---- 4. idioma y volumen no afectan el progreso
    Method cambiar = ControllerMenu.class.getDeclaredMethod("cambiarIdioma", Idioma.class); cambiar.setAccessible(true);
    edt(() -> { try { cambiar.invoke(cm, idioma == Idioma.ESPANOL ? Idioma.INGLES : Idioma.ESPANOL); cambiar.invoke(cm, idioma); } catch (Exception e) { throw new RuntimeException(e); } });
    edt(() -> { ConfiguracionAudio.setNivelVolumen(4); });
    ok("NOCHE_1,NOCHE_2".equals(cfg().getProperty("nochesSuperadas")), "cambiar idioma y volumen conserva los desbloqueos");
    // ---- 5. primera victoria de Escape
    escribirFlag(); Method mostrar = ControllerMenu.class.getDeclaredMethod("mostrarMenuPrincipal"); mostrar.setAccessible(true);
    edt(() -> { try { mostrar.invoke(cm); } catch (Exception e) { throw new RuntimeException(e); } }); Thread.sleep(200);
    Component c = actual(); boolean esAgr = c instanceof PnlAgradecimiento;
    ok(esAgr && ((PnlAgradecimiento) c).isPrimeraVez(), "victoria pendiente: aparece el agradecimiento, primera vez");
    ok(!flag().exists() && "true".equals(cfg().getProperty("escapeCompletado")), "la estrella se guarda y la senal se consume");
    Thread.sleep(1100); cap(c, "agradecimiento_1_" + idioma);
    tecla(KeyEvent.VK_ENTER); Thread.sleep(200); ok(actual() == c, "ENTER antes de que aparezca Continuar no la cierra");
    Thread.sleep(1800); cap(c, "agradecimiento_2_" + idioma);
    tecla(KeyEvent.VK_ESCAPE); Thread.sleep(900);
    ok(actual() == menu && menu.getLblEstrellaEscape().isVisible(), "ESC con Continuar visible -> menu con la estrella");
    Thread.sleep(300); cap(menu, "menu_estrella_" + idioma);
    Rectangle e = menu.getLblEstrellaEscape().getBounds(); boolean choca = false;
    for (Component k : menu.getPnlMenu().getComponents()) if (k != menu.getLblEstrellaEscape() && k.isVisible() && k instanceof AbstractButton && k.getBounds().intersects(e)) choca = true;
    System.out.println("   estrella " + e + "  titulo " + menu.getTxtpnFiveDoorsAt().getBounds() + "  tooltip '" + menu.getLblEstrellaEscape().getToolTipText() + "'");
    ok(!choca, "la estrella no tapa ningun boton");
    // ---- 6. segunda victoria: agradecimiento otra vez, sin presentacion de estrella, la estrella sigue
    escribirFlag(); edt(() -> { try { mostrar.invoke(cm); } catch (Exception ex) { throw new RuntimeException(ex); } }); Thread.sleep(200);
    c = actual(); ok(c instanceof PnlAgradecimiento && !((PnlAgradecimiento) c).isPrimeraVez() && !flag().exists(), "segunda victoria: agradecimiento otra vez, sin presentar la estrella");
    Thread.sleep(1500); cap(c, "agradecimiento_repetida_" + idioma); Thread.sleep(1500);
    JButton cont = (JButton) f(c, PnlAgradecimiento.class, "btnContinuar"); ok(cont.isVisible(), "Continuar visible a los ~3 s"); edt(cont::doClick); Thread.sleep(900);
    ok(actual() == menu && menu.getLblEstrellaEscape().isVisible(), "Continuar -> menu, la estrella sigue");
    edt(() -> { try { mostrar.invoke(cm); } catch (Exception ex) { throw new RuntimeException(ex); } }); Thread.sleep(200); ok(actual() == menu, "sin senal nueva no vuelve a salir el agradecimiento");
    // ---- 7. persistencia tras volver a abrir: recargar del archivo
    PreferenciasJuego.escapeCompletado = false; PersistenciaJuego.cargar(); ok(PreferenciasJuego.escapeCompletado, "la estrella se recupera al volver a abrir (cargar del archivo)");
    // ---- 8. Nueva Partida: borra noches, NO la estrella
    edt(() -> { menu.getBtnNuevaPartida().doClick(); menu.getPnlConfirmacion().getBtnConfirmar().doClick(); }); Thread.sleep(500);
    p = cfg(); ok("".equals(p.getProperty("nochesSuperadas")) && "true".equals(p.getProperty("escapeCompletado")) && "NOCHE_1".equals(p.getProperty("nocheActual")), "Nueva Partida reinicia noches y progreso pero conserva la estrella");
    ok(!new File(dir(), "config.properties.tmp").exists(), "no queda el temporal del guardado atomico");
    System.out.println("FALLOS: " + fallos); System.exit(0); }
}
