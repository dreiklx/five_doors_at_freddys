import javax.swing.*; import java.awt.*; import java.lang.reflect.*; import java.io.*; import java.nio.file.*; import java.util.*;
import com.fdaf.mvc.controllers.*; import com.fdaf.mvc.views.frames.VistaPrincipal; import com.fdaf.mvc.views.frames.pnl.*; import com.fdaf.util.*;
import com.fdaf.mvc.models.juego.Noche;
// Perfil del jugador desde cero (CLAUDE.md §1.82): config.properties ausente, vacio, corrupto, de una version
// anterior, partida nueva y existente, idioma tras reabrir, sin permisos y guardado interrumpido. Un escenario por
// proceso (la carpeta del perfil se fija al cargar la clase), siempre con -Duser.home aislado.
// Uso: java -Duser.home=<vacio> PruebaPerfil <escenario> [fase]
public class PruebaPerfil {
  static int fallos = 0; static ControllerMenu cm; static VistaPrincipal vp; static PnlMenu menu; static PnlNochePersonalizada noches;
  static File dir() { return new File(System.getProperty("user.home"), ".fivedoorsatfreddys"); }
  static File cfgFile() { return new File(dir(), "config.properties"); }
  static Properties cfg() throws Exception { Properties p = new Properties(); try (InputStream in = new FileInputStream(cfgFile())) { p.load(in); } return p; }
  static void escribir(String txt) throws Exception { dir().mkdirs(); Files.write(cfgFile().toPath(), txt.getBytes("ISO-8859-1")); }
  static void ok(boolean b, String s) { System.out.println((b ? "OK    " : "FALLO ") + s); if (!b) fallos++; }
  static void edt(Runnable r) throws Exception { SwingUtilities.invokeAndWait(r); }
  static Object f(Object o, Class<?> c, String n) throws Exception { Field x = c.getDeclaredField(n); x.setAccessible(true); return x.get(o); }
  static String disponibles() { StringBuilder sb = new StringBuilder(); for (Noche n : Noche.values()) if (PreferenciasJuego.nocheDisponible(n)) sb.append(n.ordinal() + 1); return sb.toString(); }
  static String estado() { return "idioma=" + PreferenciasJuego.idiomaSeleccionado + " actual=" + PreferenciasJuego.nocheActual + " maxima=" + PreferenciasJuego.nocheMaxima + " vol=" + PreferenciasJuego.volumenGeneral + " disponibles=" + disponibles() + " escape=" + PreferenciasJuego.escapeDesbloqueado; }
  static void menuReal() throws Exception {
    edt(() -> { cm = new ControllerMenu(true); cm.init(); try { vp = (VistaPrincipal) f(cm, ControllerMenu.class, "vp"); menu = (PnlMenu) f(cm, ControllerMenu.class, "menu");
      noches = (PnlNochePersonalizada) f(cm, ControllerMenu.class, "pantallaNochePersonalizada"); } catch (Exception e) { throw new RuntimeException(e); } });
    Thread.sleep(1200);
  }
  static void ganar(Noche n) throws Exception { PreferenciasJuego.nocheActual = n; final ControllerInterfaz[] ci = {null};
    edt(() -> { ci[0] = new ControllerInterfaz(); ci[0].init(vp, menu, cm); }); Thread.sleep(300); edt(() -> ci[0].alGanar()); Thread.sleep(300);
    edt(() -> { Component v = vp.pnlContenido.getComponent(0); if (v instanceof PnlWin) ((PnlWin) v).detenerVideo(); try { ci[0].detenerSonidosJuego(); } catch (Exception e) { } }); }

  public static void main(String[] a) throws Exception {
    String e = a[0]; String fase = a.length > 1 ? a[1] : "";
    System.out.println("[PRUEBA] escenario " + e + " " + fase + " home=" + System.getProperty("user.home"));
    switch (e) {
      case "A": {   // primera ejecucion
        ok(!dir().exists(), "perfil vacio antes de arrancar");
        PersistenciaJuego.cargar();
        ok(cfgFile().isFile(), "config.properties creado en el primer arranque");
        Properties p = cfg();
        ok("ESPANOL".equals(p.getProperty("idioma")) && "NOCHE_1".equals(p.getProperty("nocheActual")) && "NOCHE_1".equals(p.getProperty("nocheMaxima"))
            && "10".equals(p.getProperty("volumen")) && "".equals(p.getProperty("nochesSuperadas")) && "false".equals(p.getProperty("escapeDesbloqueado")) && "false".equals(p.getProperty("escapeCompletado")),
            "valores por defecto validos en el archivo: " + p);
        ok(disponibles().equals("1"), "solo la Noche 1 disponible (" + estado() + ")");
        menuReal();
        ok(menu.getLblNocheActual().getText().endsWith("1"), "el menu muestra '" + menu.getLblNocheActual().getText() + "'");
        boolean bien = true; for (int i = 0; i < 5; i++) { String t = noches.getBtnsNoche()[i].getText(); boolean libre = noches.getBtnsNoche()[i].getForeground().equals(Color.WHITE); if (libre != (i == 0)) bien = false; System.out.println("   noche personalizada: " + t + (libre ? " [libre]" : " [bloqueada]")); }
        ok(bien, "Noche personalizada: la 1 libre, 2-5 bloqueadas");
        edt(() -> noches.getBtnsNoche()[3].doClick()); Thread.sleep(150);
        ok(PreferenciasJuego.nocheActual == Noche.NOCHE_1 && "NOCHE_1".equals(cfg().getProperty("nocheActual")), "clic en la Noche 4 bloqueada: no cambia nada");
        final ControllerInterfaz[] ci = {null}; edt(() -> { ci[0] = new ControllerInterfaz(); ci[0].init(vp, menu, cm); }); Thread.sleep(300);
        ok(ci[0].getControllerJuego().getVidas() == 4 && ci[0].getControllerJuego().getJuego().getInventario() != null, "la Noche 1 arranca: 4 vidas");
        edt(() -> { try { ci[0].detenerSonidosJuego(); } catch (Exception x) { } });
        break; }
      case "B": {   // archivos vacios
        escribir(""); PersistenciaJuego.cargar();
        ok(PreferenciasJuego.nocheActual == Noche.NOCHE_1 && PreferenciasJuego.volumenGeneral == 10 && disponibles().equals("1"), "archivo vacio: valores por defecto (" + estado() + ")");
        escribir("\n\n# solo comentarios\n   \n"); PersistenciaJuego.cargar();
        ok(PreferenciasJuego.nocheActual == Noche.NOCHE_1 && disponibles().equals("1"), "archivo con lineas en blanco y comentarios: valores por defecto");
        PersistenciaJuego.guardar(); ok("NOCHE_1".equals(cfg().getProperty("nocheMaxima")), "el siguiente guardado deja un archivo completo");
        menuReal(); ok(menu.isShowing() || vp.isShowing(), "el menu abre");
        break; }
      case "C": {   // corruptos
        escribir("idioma=INGLES\nnocheActual=\\uZZZZ\n"); PersistenciaJuego.cargar();
        ok(PreferenciasJuego.nocheActual == Noche.NOCHE_1, "escape Unicode mal formado: no se cae, valores por defecto");
        ok(new File(dir(), "config.properties.corrupto").isFile() && cfg().getProperty("nocheMaxima") != null, "el archivo danado se aparta (.corrupto) y se escribe uno valido");
        escribir("idioma=KLINGON\nnocheActual=NOCHE_9\nvolumen=abc\nnochesSuperadas=FOO,NOCHE_2,,NOCHE_2\nnocheMaxima=BAR\nescapeDesbloqueado=quizas\nescapeCompletado=1\n=sinclave\nlinea sin igual\n"); PersistenciaJuego.cargar();
        ok(PreferenciasJuego.idiomaSeleccionado == Idioma.ESPANOL && PreferenciasJuego.volumenGeneral == 10 && !PreferenciasJuego.escapeDesbloqueado && !PreferenciasJuego.escapeCompletado,
           "valores invalidos: cada uno vuelve a su valor por defecto (" + estado() + ")");
        ok(PreferenciasJuego.nocheSuperada(Noche.NOCHE_2) && PreferenciasJuego.nocheMaxima == Noche.NOCHE_3 && disponibles().equals("23") && PreferenciasJuego.nocheActual == Noche.NOCHE_3,
           "lo valido se conserva: Noche 2 superada -> progreso en la 3 (" + estado() + ")");
        escribir("volumen=999\n"); PersistenciaJuego.cargar(); ok(PreferenciasJuego.volumenGeneral == 10, "volumen 999 -> 10");
        escribir("volumen=-5\n"); PersistenciaJuego.cargar(); ok(PreferenciasJuego.volumenGeneral == 1, "volumen -5 -> 1");
        dir().mkdirs(); Files.write(cfgFile().toPath(), new byte[] { (byte) 0xFF, (byte) 0xFE, 0, 1, 2, (byte) 0x80, 0x5C, 0x75, 0x31 }); PersistenciaJuego.cargar();
        ok(PreferenciasJuego.nocheActual == Noche.NOCHE_1 && cfg().getProperty("idioma") != null, "bytes basura: no se cae y queda un archivo valido");
        escribir("nocheActual=NOCHE_2\nnochesSuperadas=NOCHE_4\nnocheMaxima=NOCHE_1\n"); PersistenciaJuego.cargar();
        ok(PreferenciasJuego.nocheMaxima == Noche.NOCHE_5 && PreferenciasJuego.nocheActual == Noche.NOCHE_5, "datos contradictorios (eligio la 2 sin haberla superado): pasa al progreso (" + estado() + ")");
        menuReal(); ok(vp.isShowing(), "el menu abre tras los archivos corruptos");
        break; }
      case "D": {   // actualizacion desde versiones anteriores
        escribir("#v2.17\nidioma=INGLES\nnocheActual=NOCHE_4\nvolumen=6\nescapeDesbloqueado=false\nnochesSuperadas=NOCHE_1,NOCHE_2,NOCHE_3\nescapeCompletado=false\n"); PersistenciaJuego.cargar();
        ok(PreferenciasJuego.idiomaSeleccionado == Idioma.INGLES && PreferenciasJuego.volumenGeneral == 6 && PreferenciasJuego.nocheActual == Noche.NOCHE_4 && PreferenciasJuego.nocheMaxima == Noche.NOCHE_4 && disponibles().equals("1234"),
           "perfil de v2.17 (sin nocheMaxima): progreso en la 4, noches 1-4 disponibles (" + estado() + ")");
        PersistenciaJuego.guardar(); ok("NOCHE_4".equals(cfg().getProperty("nocheMaxima")) && "NOCHE_1,NOCHE_2,NOCHE_3".equals(cfg().getProperty("nochesSuperadas")), "al guardar se agrega nocheMaxima sin perder nada");
        escribir("idioma=ESPANOL\nnocheActual=NOCHE_3\nvolumen=7\n"); PersistenciaJuego.cargar();
        ok(PreferenciasJuego.nocheActual == Noche.NOCHE_3 && PreferenciasJuego.nocheMaxima == Noche.NOCHE_3 && disponibles().equals("3"), "perfil anterior a v2.12 (sin historial): Continuar sigue en la 3 (" + estado() + ")");
        escribir("idioma=ESPANOL\nnocheActual=NOCHE_5\nvolumen=7\nescapeDesbloqueado=true\n"); PersistenciaJuego.cargar();
        ok(PreferenciasJuego.nocheMaxima == Noche.NOCHE_5 && PreferenciasJuego.escapeDesbloqueado && disponibles().equals("5"), "perfil anterior con Escape desbloqueado: Noche 5 y ESCAPE (" + estado() + ")");
        break; }
      case "E": {   // partida nueva sobre un perfil con progreso
        escribir("idioma=ESPANOL\nnocheActual=NOCHE_5\nnocheMaxima=NOCHE_5\nvolumen=5\nescapeDesbloqueado=true\nnochesSuperadas=NOCHE_1,NOCHE_2,NOCHE_3,NOCHE_4,NOCHE_5\nescapeCompletado=true\n"); PersistenciaJuego.cargar();
        menuReal();
        edt(() -> { menu.getBtnNuevaPartida().doClick(); menu.getPnlConfirmacion().getBtnConfirmar().doClick(); }); Thread.sleep(600);
        Properties p = cfg();
        ok("NOCHE_1".equals(p.getProperty("nocheActual")) && "NOCHE_1".equals(p.getProperty("nocheMaxima")) && "".equals(p.getProperty("nochesSuperadas")) && "false".equals(p.getProperty("escapeDesbloqueado")),
           "Nueva Partida: Noche 1, sin noches superadas, ESCAPE bloqueado: " + p);
        ok(disponibles().equals("1") && "true".equals(p.getProperty("escapeCompletado")) && "5".equals(p.getProperty("volumen")), "solo la Noche 1; se conservan la estrella y el volumen");
        break; }
      case "F": {   // partida existente: repetir una noche no hace perder el progreso
        escribir("idioma=ESPANOL\nnocheActual=NOCHE_4\nvolumen=5\nnochesSuperadas=NOCHE_1,NOCHE_2,NOCHE_3\n"); PersistenciaJuego.cargar();
        menuReal();
        boolean bien = true; for (int i = 0; i < 5; i++) { boolean libre = noches.getBtnsNoche()[i].getForeground().equals(Color.WHITE); if (libre != (i < 4)) bien = false; }
        ok(bien, "Noche personalizada coincide con el registro: 1-3 superadas y la 4 (progreso) libres, la 5 bloqueada");
        edt(() -> noches.getBtnsNoche()[4].doClick()); Thread.sleep(150); ok(PreferenciasJuego.nocheActual == Noche.NOCHE_4, "la Noche 5 bloqueada no se puede elegir");
        edt(() -> noches.getBtnsNoche()[0].doClick()); Thread.sleep(150);
        ok(PreferenciasJuego.nocheActual == Noche.NOCHE_1 && "NOCHE_4".equals(cfg().getProperty("nocheMaxima")), "elegir la Noche 1 no cambia el progreso guardado");
        ganar(Noche.NOCHE_1);
        Properties p = cfg();
        ok("NOCHE_4".equals(p.getProperty("nocheActual")) && "NOCHE_4".equals(p.getProperty("nocheMaxima")), "ganar la Noche 1 repetida vuelve al progreso (Noche 4), no a la 2: " + p.getProperty("nocheActual"));
        edt(() -> noches.getBtnsNoche()[3].doClick()); Thread.sleep(150); ganar(Noche.NOCHE_4);
        p = cfg(); ok("NOCHE_5".equals(p.getProperty("nocheActual")) && "NOCHE_5".equals(p.getProperty("nocheMaxima")) && p.getProperty("nochesSuperadas").contains("NOCHE_4"), "ganar la 4 avanza a la 5");
        ok(!new File(dir(), "config.properties.tmp").exists(), "no queda el temporal");
        break; }
      case "G": {   // idioma tras cerrar y abrir
        PersistenciaJuego.cargar();
        Method cambiar = ControllerMenu.class.getDeclaredMethod("cambiarIdioma", Idioma.class); cambiar.setAccessible(true);
        if (fase.equals("1")) { menuReal(); edt(() -> { try { cambiar.invoke(cm, Idioma.INGLES); } catch (Exception x) { throw new RuntimeException(x); } });
          ok("INGLES".equals(cfg().getProperty("idioma")) && menu.getBtnContinuar().getText().equals("Continue"), "cambiar a ingles: guardado y menu en ingles ('" + menu.getBtnContinuar().getText() + "')"); }
        else if (fase.equals("2")) { ok(PreferenciasJuego.idiomaSeleccionado == Idioma.INGLES, "al reabrir sigue en ingles"); menuReal();
          ok(menu.getBtnContinuar().getText().equals("Continue"), "menu reabierto en ingles"); edt(() -> { try { cambiar.invoke(cm, Idioma.ESPANOL); } catch (Exception x) { throw new RuntimeException(x); } }); }
        else { ok(PreferenciasJuego.idiomaSeleccionado == Idioma.ESPANOL, "al reabrir otra vez vuelve a estar en espanol"); }
        break; }
      case "H": {   // sin permisos de escritura
        dir().getParentFile().mkdirs(); Files.write(dir().toPath(), "no soy una carpeta".getBytes());   // la carpeta del perfil es un archivo
        PersistenciaJuego.cargar(); PreferenciasJuego.idiomaSeleccionado = Idioma.INGLES; PersistenciaJuego.guardar();
        ok(PreferenciasJuego.nocheActual == Noche.NOCHE_1, "perfil inutilizable: arranca con valores por defecto sin caerse");
        Files.delete(dir().toPath()); PreferenciasJuego.idiomaSeleccionado = Idioma.ESPANOL;
        escribir("idioma=ESPANOL\nnocheActual=NOCHE_2\nnochesSuperadas=NOCHE_1\nvolumen=4\n"); PersistenciaJuego.cargar();
        ok(cfgFile().setReadOnly(), "config.properties de solo lectura");
        PreferenciasJuego.volumenGeneral = 9; PersistenciaJuego.guardar();
        ok("4".equals(cfg().getProperty("volumen")) && PreferenciasJuego.nocheActual == Noche.NOCHE_2, "guardar sobre un archivo de solo lectura: no se cae y el archivo queda intacto");
        cfgFile().setWritable(true); new File(dir(), "config.properties.tmp").delete();
        break; }
      case "I": {   // guardado interrumpido
        escribir("idioma=INGLES\nnocheActual=NOCHE_3\nnochesSuperadas=NOCHE_1,NOCHE_2\nvolumen=4\n");
        Files.write(new File(dir(), "config.properties.tmp").toPath(), "idioma=ESPA".getBytes());
        PersistenciaJuego.cargar();
        ok(PreferenciasJuego.idiomaSeleccionado == Idioma.INGLES && PreferenciasJuego.nocheActual == Noche.NOCHE_3, "un .tmp a medias no se lee: vale el ultimo guardado completo");
        ok(!new File(dir(), "config.properties.tmp").exists(), "y el .tmp viejo se borra");
        break; }
      default: System.out.println("escenario desconocido");
    }
    System.out.println("FALLOS: " + fallos);
    System.exit(0);
  }
}
