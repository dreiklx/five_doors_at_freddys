import javax.swing.*; import java.awt.*; import java.awt.image.BufferedImage; import java.lang.reflect.*; import java.io.*; import java.nio.file.*; import java.util.*;
import com.fdaf.mvc.controllers.*; import com.fdaf.mvc.views.frames.VistaPrincipal; import com.fdaf.mvc.views.frames.pnl.*; import com.fdaf.util.*; import com.fdaf.mvc.models.juego.Noche;
// Flujo real de fin de noche (CLAUDE.md §1.81) con ControllerMenu/ControllerInterfaz reales y perfil aislado:
//   noche 1-4: alGanar -> video de victoria (se comprueba que se dibuja) -> reinicio del proceso (System.exit del juego)
//   noche 5:   alGanar -> video -> glitch -> Five Doors Escape real (distribucion: user.dir = carpeta FiveDoorsAtFreddys)
//   custom:    ESCAPE de Noche personalizada -> glitch -> Five Doors Escape real
// Con Escape: espera "Nivel preparado" en logs/escape.log, comprueba que es hijo directo de este proceso (sin cmd) y lo
// cierra; el juego vuelve por su camino real (reinicio). El arnes termina cuando el juego llama a System.exit.
// Ejecutar con JAVA_TOOL_OPTIONS=-Duser.home=<aislado> (lo hereda Escape) y -Dprueba.reinicio=<marca>.
// Args: modo(1-5|custom) idioma carpetaCapturas
public class PruebaFlujoVictoria {
  static VistaPrincipal vp; static ControllerMenu cm; static PnlMenu menu; static int fallos = 0; static long t0; static String out, modo;
  static File dir() { return new File(System.getProperty("user.home"), ".fivedoorsatfreddys"); }
  static Object f(Object o, Class<?> c, String n) throws Exception { Field x = c.getDeclaredField(n); x.setAccessible(true); return x.get(o); }
  static void ok(boolean b, String s) { System.out.println((b ? "OK    " : "FALLO ") + s); if (!b) fallos++; }
  static void edt(Runnable r) throws Exception { SwingUtilities.invokeAndWait(r); }
  static long ms() { return System.currentTimeMillis() - t0; }
  static Component actual() throws Exception { final Component[] c = {null}; edt(() -> c[0] = vp.pnlContenido.getComponent(0)); return c[0]; }
  /** Pinta el panel actual fuera de pantalla y cuenta pixeles claros (texto blanco del video). */
  static int claros(Component c, String nombre) throws Exception {
    int w = Math.max(1, c.getWidth()), h = Math.max(1, c.getHeight()); BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    edt(() -> { Graphics2D g = img.createGraphics(); c.printAll(g); g.dispose(); });
    int n = 0; for (int y = 0; y < h; y += 2) for (int x = 0; x < w; x += 2) { int p = img.getRGB(x, y); if (((p >> 16) & 255) > 200 && ((p >> 8) & 255) > 200 && (p & 255) > 200) n++; }
    if (nombre != null) javax.imageio.ImageIO.write(img, "png", new File(out, nombre + ".png"));
    return n;
  }
  static String ps(String cmd) throws Exception {
    Process p = new ProcessBuilder("powershell", "-NoProfile", "-Command", cmd).redirectErrorStream(true).start(); StringBuilder sb = new StringBuilder();
    try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) { String l; while ((l = r.readLine()) != null) sb.append(l).append('\n'); } p.waitFor(); return sb.toString().trim();
  }
  public static void main(String[] a) throws Exception {
    modo = a[0]; Idioma idioma = Idioma.valueOf(a[1]); out = a[2]; new File(out).mkdirs();
    String pid = java.lang.management.ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    System.out.println("[PRUEBA] modo=" + modo + " pid=" + pid + " user.dir=" + System.getProperty("user.dir") + " home=" + System.getProperty("user.home"));
    Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("[PRUEBA] el juego cerro este proceso a los " + ms() + " ms (camino real: reinicio). FALLOS: " + fallos)));
    dir().mkdirs(); int n = modo.equals("custom") ? 5 : Integer.parseInt(modo);
    Files.write(new File(dir(), "config.properties").toPath(), ("idioma=" + idioma + "\nnocheActual=NOCHE_" + n + "\nvolumen=3\nescapeDesbloqueado=true\nnochesSuperadas=" + String.join(",", Arrays.asList("NOCHE_1","NOCHE_2","NOCHE_3","NOCHE_4","NOCHE_5").subList(0, n - 1)) + "\n").getBytes());
    RegistroArchivo.instalar(); PersistenciaJuego.cargar();
    edt(() -> { cm = new ControllerMenu(true); cm.init(); try { vp = (VistaPrincipal) f(cm, ControllerMenu.class, "vp"); menu = (PnlMenu) f(cm, ControllerMenu.class, "menu"); } catch (Exception e) { throw new RuntimeException(e); } });
    Thread.sleep(1500);
    t0 = System.currentTimeMillis();
    if (modo.equals("custom")) {
      Method m = ControllerMenu.class.getDeclaredMethod("lanzarEscapeDesdeCustomNight"); m.setAccessible(true);
      edt(() -> { try { m.invoke(cm); } catch (Exception e) { throw new RuntimeException(e); } });
      Thread.sleep(1500); ok(actual() instanceof PnlWin, "ESCAPE desde Noche personalizada: pantalla de transicion (glitch) visible");
      claros(actual(), "custom_glitch");
    } else {
      PreferenciasJuego.nocheActual = Noche.values()[n - 1]; final ControllerInterfaz[] ci = {null};
      edt(() -> { ci[0] = new ControllerInterfaz(); ci[0].init(vp, menu, cm); }); Thread.sleep(300);
      t0 = System.currentTimeMillis();
      edt(() -> ci[0].alGanar());
      Thread.sleep(200); Component c = actual(); ok(c instanceof PnlWin, "noche " + n + ": alGanar muestra PnlWin");
      int max = 0; int muestras = 0, conImagen = 0;
      while (ms() < 9300) { Thread.sleep(700); int k = claros(c, muestras % 4 == 0 ? "noche" + n + "_video_" + ms() : null); muestras++; if (k > 50) conImagen++; max = Math.max(max, k); }
      System.out.println("   video: " + conImagen + "/" + muestras + " muestras con imagen (max " + max + " px claros)");
      ok(conImagen >= muestras / 2, "noche " + n + ": el video de victoria se dibuja durante su reproduccion");
      Properties p = new Properties(); try (InputStream in = new FileInputStream(new File(dir(), "config.properties"))) { p.load(in); }
      ok(n == 5 ? "NOCHE_5".equals(p.getProperty("nocheActual")) : ("NOCHE_" + (n + 1)).equals(p.getProperty("nocheActual")), "noche " + n + ": progreso guardado (nocheActual=" + p.getProperty("nocheActual") + ")");
      if (n < 5) { Thread.sleep(8000); ok(false, "noche " + n + ": el juego no se reinicio tras el video"); System.out.println("FALLOS: " + fallos); System.exit(1); }
    }
    // ---- Escape real
    File log = new File(dir(), "logs" + File.separator + "escape.log"); boolean listo = false;
    while (ms() < 150000 && !listo) { Thread.sleep(1000); if (log.isFile()) { String s = new String(Files.readAllBytes(log.toPath()), "UTF-8"); listo = s.contains("Nivel preparado"); } }
    String hijos = ps("Get-CimInstance Win32_Process -Filter \"ParentProcessId=" + pid + "\" | ForEach-Object { $_.ProcessId.ToString() + ' ' + $_.Name + ' ' + $_.CommandLine }");
    System.out.println("   hijos: " + hijos.replace('\n', '|'));
    ok(listo, "Escape cargo sus recursos y el nivel (escape.log: 'Nivel preparado') a los " + ms() + " ms");
    ok(hijos.contains("javaw.exe") && hijos.contains("Lwjgl3Launcher") && !hijos.toLowerCase().contains("cmd.exe"), "Escape es hijo directo (javaw + Lwjgl3Launcher, sin cmd)");
    String escLog = log.isFile() ? new String(Files.readAllBytes(log.toPath()), "UTF-8") : "";
    ok(!escLog.contains("Exception"), "escape.log sin excepciones");
    String hp = null; for (String l : hijos.split("\n")) if (l.contains("Lwjgl3Launcher")) hp = l.trim().split(" ")[0];
    Thread.sleep(3000);
    System.out.println("   cerrando Escape (pid " + hp + ") para comprobar el regreso");
    if (hp != null) ps("Stop-Process -Id " + hp + " -Force");
    Thread.sleep(20000); ok(false, "el juego no volvio por su camino real tras cerrar Escape"); System.out.println("FALLOS: " + fallos); System.exit(1);
  }
}
