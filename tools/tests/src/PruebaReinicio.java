import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

/**
 * El relanzamiento del juego (ReiniciadorJuego, CLAUDE.md §1.74) conserva las opciones de la JVM: arrancada con
 * -Duser.home aislado y una marca, la instancia nueva debe llevar las dos (antes salia sin ellas y abria el progreso
 * real). Solo Windows (usa la lista de procesos de PowerShell). Cierra unicamente el proceso hijo con la marca.
 * Ejecutar con: java -Duser.home=<aislado> -Dprueba.reinicio=<marca> -cp ... PruebaReinicio
 */
public class PruebaReinicio {
    static int fallos = 0;

    static void ok(boolean b, String s) {
        System.out.println((b ? "OK    " : "FALLO ") + s);
        if (!b) fallos++;
    }

    static String powershell(String comando) throws Exception {
        Process p = new ProcessBuilder("powershell", "-NoProfile", "-Command", comando).redirectErrorStream(true).start();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String l;
            while ((l = r.readLine()) != null) sb.append(l).append('\n');
        }
        p.waitFor();
        return sb.toString();
    }

    public static void main(String[] a) throws Exception {
        String marca = System.getProperty("prueba.reinicio");
        String home = System.getProperty("user.home");
        if (marca == null) {
            System.out.println("FALLO falta -Dprueba.reinicio=<marca>");
            System.out.println("FALLOS: 1");
            return;
        }
        Method m = Class.forName("com.fdaf.util.ReiniciadorJuego").getDeclaredMethod("lanzarNuevaInstancia");
        m.setAccessible(true);
        ok((Boolean) m.invoke(null), "el relanzamiento arranca un proceso nuevo");
        // solo procesos java: el propio comando de PowerShell tambien contiene la marca
        String filtro = "Get-CimInstance Win32_Process | Where-Object { $_.Name -like 'java*' -and $_.CommandLine -like '*prueba.reinicio=" + marca
                + "*' -and $_.CommandLine -like '*--reinicio-interno*' }";
        String cmd = "";
        for (int i = 0; i < 20 && cmd.trim().isEmpty(); i++) {
            Thread.sleep(250);
            cmd = powershell(filtro + " | ForEach-Object { $_.CommandLine }");
        }
        System.out.println("   hijo: " + cmd.trim().replaceAll("\\s+", " "));
        ok(cmd.contains("--reinicio-interno"), "el hijo es el juego relanzado (--reinicio-interno)");
        ok(cmd.contains("-Duser.home=" + home), "el hijo conserva -Duser.home (perfil aislado, no el real)");
        ok(cmd.contains("-Dprueba.reinicio=" + marca), "el hijo conserva las demas opciones de la JVM");
        powershell(filtro + " | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }");
        Thread.sleep(500);
        ok(powershell(filtro + " | ForEach-Object { $_.ProcessId }").trim().isEmpty(), "el hijo de la prueba quedo cerrado");
        System.out.println("FALLOS: " + fallos);
    }
}
