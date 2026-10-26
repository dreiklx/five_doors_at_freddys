import java.io.*; import java.lang.reflect.*; import java.nio.file.*; import java.util.*; import java.util.regex.*;
import com.fdaf.util.*; import com.fdaf.mvc.models.juego.Noche; import com.fdaf.mvc.models.coleccionables.TipoColeccionable; import com.fdaf.mvc.models.animatronicos.Animatronico;
// Verificación estática + dinámica de TODAS las rutas de recursos de FDAF contra el classpath real (las clases y
// recursos compilados). Args: carpetaSrc carpetaRecursos
public class VerificarRecursos { static int ok = 0, mal = 0; static Set<String> usados = new TreeSet<>();
  static boolean existe(String r) { return VerificarRecursos.class.getResource(r) != null; }
  static void exigir(String ctx, String r) { if (existe(r)) { ok++; usados.add(r); } else { mal++; System.out.println("NO EXISTE " + r + "   <- " + ctx); } }
  public static void main(String[] a) throws Exception {
    Set<String> ignorar = new HashSet<>(Arrays.asList("Abanico.gif", "abanico.gif", "win.png", "night_06_call_en.wav", "night_06_call_es.wav", "_jumpscare.gif", "/resources/sounds/putdown.wav"));
    Pattern lit = Pattern.compile("\"([^\"]*\\.(wav|png|gif|mp4))\"");
    // 1) literales del código
    for (Path p : (Iterable<Path>) Files.walk(Paths.get(a[0])).filter(x -> x.toString().endsWith(".java"))::iterator) {
      List<String> lineas = Files.readAllLines(p); for (int i = 0; i < lineas.size(); i++) { String l = lineas.get(i); if (l.trim().startsWith("//") || l.trim().startsWith("*")) continue;
        Matcher m = lit.matcher(l); while (m.find()) { String s = m.group(1); if (ignorar.contains(s) || s.contains("%")) continue; String ctx = p.getFileName() + ":" + (i + 1);
          if (s.startsWith("/")) exigir(ctx, s); else if (existe("/audio/" + s)) exigir(ctx, "/audio/" + s); else exigir(ctx, "/" + s); } } }
    // 2) rutas construidas por código
    for (Idioma id : Idioma.values()) { PreferenciasJuego.idiomaSeleccionado = id;
      for (int i = 0; i < 5; i++) { String r = CargarImagenes.rutaIntroPorNoche(i); if (r.contains("fallback")) { mal++; System.out.println("INTRO usa respaldo: noche " + (i+1) + " " + id); } else exigir("rutaIntroPorNoche", r); }
      for (int n = 1; n <= 5; n++) exigir("getEtiquetaNoche", String.format("/ui/hud/night_%02d_%s.png", n, id == Idioma.INGLES ? "en" : "es"));
      if (CargarImagenes.getEtiquetaNoche(3) == null || CargarImagenes.getEtiquetaEnergia() == null || CargarImagenes.getRotuloCamara() == null || CargarImagenes.getMuteCall() == null || CargarImagenes.getPanelBotonesIzq() == null) { mal++; System.out.println("getter localizado devuelve null en " + id); } else ok++;
      Method m = Class.forName("com.fdaf.mvc.controllers.ControllerInterfaz").getDeclaredMethod("rutaLlamadaNocheActual"); m.setAccessible(true);
      Object ci = sinConstructor(Class.forName("com.fdaf.mvc.controllers.ControllerInterfaz"));
      for (Noche n : Noche.values()) { PreferenciasJuego.nocheActual = n; exigir("rutaLlamadaNocheActual " + n + " " + id, "/audio/" + m.invoke(ci)); } }
    for (Animatronico an : Animatronico.values()) exigir("jumpscare " + an, "/jumpscares/" + an.name().toLowerCase(Locale.ROOT) + "_jumpscare.gif");
    for (TipoColeccionable t : TipoColeccionable.values()) { exigir("coleccionable " + t, "/" + t.getArchivoDesbloqueado()); exigir("coleccionable bloqueado " + t, "/" + t.getArchivoBloqueado()); }
    // 3) recursos del árbol que nadie referencia
    Path raiz = Paths.get(a[1]); List<String> sinUso = new ArrayList<>();
    for (Path p : (Iterable<Path>) Files.walk(raiz).filter(Files::isRegularFile)::iterator) { String r = "/" + raiz.relativize(p).toString().replace(File.separatorChar, '/'); if (!usados.contains(r)) sinUso.add(r); }
    System.out.println("Recursos en el árbol sin ninguna referencia: " + sinUso);
    System.out.println("RESULTADO: " + ok + " rutas OK, " + mal + " fallos (" + usados.size() + " archivos distintos referenciados)"); System.exit(0); }
  static Object sinConstructor(Class<?> c) throws Exception { Field f = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe"); f.setAccessible(true); return ((sun.misc.Unsafe) f.get(null)).allocateInstance(c); }
}
