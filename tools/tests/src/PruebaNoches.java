import java.lang.reflect.*; import java.util.*;
import com.fdaf.mvc.controllers.*; import com.fdaf.mvc.models.juego.*; import com.fdaf.mvc.models.puerta.*; import com.fdaf.mvc.models.coleccionables.*; import com.fdaf.util.*;
// Sistema de noches a nivel de controlador (CLAUDE.md §1.82), sin ventana: vidas y coleccionables de cada noche,
// que no se pueda ganar antes de tiempo, una sola transicion de fin, derrota, tope de baterias y coleccionables ya
// recogidos que vuelven a aparecer tras regenerar el arbol. Usa ControllerJuego/Juego reales y un oyente que cuenta.
public class PruebaNoches {
  static int fallos = 0;
  static void ok(boolean b, String s) { System.out.println((b ? "OK    " : "FALLO ") + s); if (!b) fallos++; }
  static final Map<String, Integer> eventos = new HashMap<>();
  static JuegoListener oyente() { return (JuegoListener) Proxy.newProxyInstance(JuegoListener.class.getClassLoader(), new Class<?>[] { JuegoListener.class },
      (p, m, args) -> { eventos.merge(m.getName(), 1, Integer::sum); return null; }); }
  static int ev(String n) { return eventos.getOrDefault(n, 0); }
  static Object campo(Object o, String n) throws Exception { Field f = o.getClass().getDeclaredField(n); f.setAccessible(true); return f.get(o); }
  static void fijar(Object o, String n, Object v) throws Exception { Field f = o.getClass().getDeclaredField(n); f.setAccessible(true); f.set(o, v); }
  static void revelar(ControllerJuego cj, Puerta p) throws Exception { fijar(cj, "puertaPendienteIzq", p); Method m = ControllerJuego.class.getDeclaredMethod("evaluarRevelacion", String.class, Puerta.class); m.setAccessible(true); m.invoke(cj, "izq", p); }

  public static void main(String[] a) throws Exception {
    int[][] esperado = { { 4, 5 }, { 3, 6 }, { 3, 7 }, { 2, 8 }, { 2, 10 } };
    for (Noche n : Noche.values()) {
      PreferenciasJuego.nocheActual = n; eventos.clear();
      ControllerJuego cj = new ControllerJuego(); cj.setListener(oyente()); Juego j = cj.getJuego();
      List<Coleccionable> todos = new ArrayList<>(); for (TipoColeccionable t : TipoColeccionable.values()) { Coleccionable c = j.getInventario().buscarPorTipo(t); if (c != null) todos.add(c); }
      ok(cj.getVidas() == esperado[n.ordinal()][0] && todos.size() == esperado[n.ordinal()][1], n + ": " + cj.getVidas() + " vidas y " + todos.size() + " coleccionables (tabla §1.4)");
      for (int i = 0; i < todos.size() - 1; i++) j.marcarColeccionableEncontrado(todos.get(i));
      ok(!cj.gano(), n + ": con todos menos uno todavia no se gana");
      Coleccionable ultimo = todos.get(todos.size() - 1);
      // revelar y recoger el ultimo por el camino real del controlador -> una sola victoria
      revelar(cj, new Puerta(901, TipoPuerta.COLECCIONABLE, ultimo, null));
      ok(ev("alRevelarColeccionable") == 1, n + ": el ultimo coleccionable se revela");
      cj.recogerColeccionable(); cj.recogerColeccionable();
      ok(cj.gano() && ev("alGanar") == 1 && ev("alPerder") == 0, n + ": recogerlo gana UNA vez aunque se haga clic dos veces (alGanar=" + ev("alGanar") + ")");
      // un coleccionable ya recogido que vuelve a salir en una puerta se muestra como vacia
      cj.cancelarRevelacionPendiente(); fijar(cj, "ladoRevelandoActual", null); eventos.clear();
      Puerta repetida = new Puerta(902, TipoPuerta.COLECCIONABLE, todos.get(0), null); revelar(cj, repetida);
      ok(repetida.getTipo() == TipoPuerta.NADA && ev("alRevelarNada") == 1 && ev("alRevelarColeccionable") == 0, n + ": coleccionable ya recogido -> puerta vacia, no se vuelve a mostrar");
      cj.cancelarRevelacionPendiente(); fijar(cj, "ladoRevelandoActual", null);
      // uno pendiente sigue mostrandose normal
      PreferenciasJuego.nocheActual = n; ControllerJuego cj2 = new ControllerJuego(); eventos.clear(); cj2.setListener(oyente());
      Coleccionable pendiente = cj2.getJuego().getInventario().buscarPorTipo(TipoColeccionable.values()[0]);
      revelar(cj2, new Puerta(903, TipoPuerta.COLECCIONABLE, pendiente, null));
      ok(ev("alRevelarColeccionable") == 1, n + ": un coleccionable pendiente se sigue mostrando");
      cj2.cancelarRevelacionPendiente();
      // derrota: cada animatronico quita una vida; con 0 se pierde una sola vez
      PreferenciasJuego.nocheActual = n; ControllerJuego cj3 = new ControllerJuego(); eventos.clear(); cj3.setListener(oyente());
      int vidas = cj3.getVidas(); Method liberar = ControllerJuego.class.getDeclaredMethod("liberar"); liberar.setAccessible(true);
      for (int i = 0; i < vidas; i++) { revelar(cj3, new Puerta(910 + i, TipoPuerta.ANIMATRONICO, null, com.fdaf.mvc.models.animatronicos.Animatronico.values()[0])); cj3.cancelarRevelacionPendiente(); liberar.invoke(cj3); }
      ok(cj3.perdio() && ev("alPerder") == 1 && ev("alGanar") == 0, n + ": " + vidas + " animatronicos -> derrota una vez (alPerder=" + ev("alPerder") + ")");
      // baterias: nunca mas de 4
      PreferenciasJuego.nocheActual = n; Juego j4 = new Juego(); for (int i = 0; i < 6; i++) j4.ganarVidaExtra(); ok(j4.getVidas() == 4, n + ": las baterias extra topan en 4");
    }
    System.out.println("FALLOS: " + fallos); System.exit(0);
  }
}
