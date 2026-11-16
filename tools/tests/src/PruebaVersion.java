import com.fdaf.util.Version;
// Regla de versiones (CLAUDE.md §1.82): recorre la secuencia desde v2.18 y la compara con la esperada.
public class PruebaVersion {
  static int fallos = 0;
  static void ok(boolean b, String s) { System.out.println((b ? "OK    " : "FALLO ") + s); if (!b) fallos++; }
  public static void main(String[] a) {
    String[] esperada = { "2.18", "2.19", "2.20", "2.20.1", "2.20.2", "2.20.3", "2.20.4", "2.20.5", "3.0.0", "3.0.1", "3.0.2", "3.0.3", "3.0.4", "3.0.5", "3.1", "3.2" };
    int[] v = { 2, 18, -1 }; StringBuilder sb = new StringBuilder(); boolean igual = true;
    for (String e : esperada) { sb.append(Version.formato(v)).append(' '); if (!Version.formato(v).equals(e)) igual = false; v = Version.siguiente(v); }
    ok(igual, "secuencia desde v2.18: " + sb.toString().trim());
    v = new int[] { 3, 19, -1 }; sb = new StringBuilder();
    for (int i = 0; i < 8; i++) { sb.append(Version.formato(v)).append(' '); v = Version.siguiente(v); }
    ok(sb.toString().trim().equals("3.19 3.20 3.20.1 3.20.2 3.20.3 3.20.4 3.20.5 4.0.0"), "ejemplo del usuario: " + sb.toString().trim());
    // recorrido largo: ninguna version se repite y cada mayor pasa por x.20.5 -> (x+1).0.0
    java.util.Set<String> vistas = new java.util.HashSet<>(); v = new int[] { 2, 18, -1 }; boolean sinRepetir = true; int mayores = 0;
    for (int i = 0; i < 200; i++) { if (!vistas.add(Version.formato(v))) sinRepetir = false; int[] s = Version.siguiente(v); if (s[0] > v[0]) { mayores++; if (!(v[1] == 20 && v[2] == 5 && s[1] == 0 && s[2] == 0)) sinRepetir = false; } v = s; }
    ok(sinRepetir && mayores >= 5, "200 publicaciones seguidas sin repetir ni saltarse la regla (" + mayores + " cambios de mayor, llega a v" + Version.formato(v) + ")");
    ok(Version.etiqueta().equals("v" + Version.formato(Version.actual())) && Version.texto().equals("v " + Version.formato(Version.actual())), "texto del menu '" + Version.texto() + "' y etiqueta '" + Version.etiqueta() + "' coinciden");
    System.out.println("FALLOS: " + fallos);
  }
}
