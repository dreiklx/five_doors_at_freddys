import javax.swing.*; import java.awt.*; import java.awt.image.BufferedImage; import java.io.File;
import com.fdaf.mvc.views.frames.pnl.PnlWin; import com.fdaf.util.*;
// Video de victoria GIF + WAV (CLAUDE.md §1.82) en una ventana real de 1920x1080: duracion, fotogramas mostrados,
// sincronia con la posicion del audio, un solo fin, hilos y audio liberados, y capturas comparadas con el GIF.
// Args: carpetaCapturas [repeticiones]
public class PruebaVideoVictoria {
  static int fallos = 0;
  static void ok(boolean b, String s) { System.out.println((b ? "OK    " : "FALLO ") + s); if (!b) fallos++; }
  static int vivos(String prefijo) { int n = 0; for (Thread t : Thread.getAllStackTraces().keySet()) if (t.getName().startsWith(prefijo) && t.isAlive()) n++; return n; }

  public static void main(String[] a) throws Exception {
    File out = new File(a[0]); out.mkdirs(); int reps = a.length > 1 ? Integer.parseInt(a[1]) : 2;
    // referencia: los fotogramas del propio GIF
    DecodificadorGif ref = DecodificadorGif.abrir("/videos/victory.gif");
    ok(ref != null && ref.getTotalFotogramas() == 289 && ref.getAncho() == 1920 && ref.getAlto() == 1080 && ref.isIndependientes(), "GIF: 289 fotogramas 1920x1080 independientes");
    long durGif = 0; for (int r : ref.getRetardosMs()) durGif += r; ok(Math.abs(durGif - 9633) <= 10, "GIF: " + durGif + " ms (video original 9633 ms)");
    for (int rep = 1; rep <= reps; rep++) {
      final PnlWin[] p = { null }; final JFrame[] f = { null }; final long[] fin = { 0 }; final int[] fines = { 0 };
      SwingUtilities.invokeAndWait(() -> { f[0] = new JFrame("prueba video"); f[0].setUndecorated(true); p[0] = new PnlWin(); p[0].setLayout(null);
        f[0].setContentPane(p[0]); f[0].setSize(1920, 1080); f[0].setVisible(true); });
      long t0 = System.currentTimeMillis();
      SwingUtilities.invokeAndWait(() -> p[0].reproducirVideoVictoria(() -> { fines[0]++; fin[0] = System.currentTimeMillis(); }));
      ReproductorVideoGif r = p[0].getReproductor();
      // captura a mitad: lo dibujado tiene que ser exactamente un fotograma del GIF cercano al que toca
      Thread.sleep(5200);
      BufferedImage cap = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
      final int[] mostrado = { -1 };
      SwingUtilities.invokeAndWait(() -> { Graphics2D g = cap.createGraphics(); p[0].paint(g); g.dispose(); mostrado[0] = r.getUltimoFotogramaMostrado(); });
      if (rep == 1) javax.imageio.ImageIO.write(cap, "png", new File(out, "video_gif_mitad.png"));
      DecodificadorGif lector = ref.otroLector(); BufferedImage esperado = lector.fotograma(mostrado[0]); lector.cerrar();
      long dif = 0; for (int y = 0; y < 1080; y += 3) for (int x = 0; x < 1920; x += 3) { int c1 = cap.getRGB(x, y), c2 = esperado.getRGB(x, y);
        dif = Math.max(dif, Math.max(Math.abs((c1 >> 16 & 255) - (c2 >> 16 & 255)), Math.max(Math.abs((c1 >> 8 & 255) - (c2 >> 8 & 255)), Math.abs((c1 & 255) - (c2 & 255))))); }
      ok(dif == 0, "rep " + rep + ": lo pintado a los ~5.2 s es el fotograma " + mostrado[0] + " del GIF, pixel a pixel (dif. max " + dif + ")");
      while (fines[0] == 0 && System.currentTimeMillis() - t0 < 20000) Thread.sleep(20);
      long dur = fin[0] - t0;
      Thread.sleep(600);
      System.out.println("   rep " + rep + ": " + r.getFotogramasMostrados() + " fotogramas distintos mostrados, retraso max de imagen " + r.getMaxRetrasoMs()
          + " ms (" + r.getPintadosConRetraso() + " repintados con retraso), audio empezo a los " + r.getArranqueAudioMs() + " ms, fin a los " + dur + " ms, pintado medio "
          + String.format("%.1f", r.getPintadoMs()[0]) + " ms / max " + String.format("%.1f", r.getPintadoMs()[1]) + " ms");
      System.out.println("   rep " + rep + ": salto max del reloj " + r.getSaltosReloj()[0] + " ms, tick max " + r.getSaltosReloj()[1] + " ms, no mostrados: " + r.getNoMostrados());
      ok(fines[0] == 1, "rep " + rep + ": alTerminar se llama una sola vez (" + fines[0] + ")");
      long arranque = r.getArranqueAudioMs();
      ok(dur >= arranque + 9550 && dur <= arranque + 10100, "rep " + rep + ": dura lo que el audio desde que suena (" + dur + " ms = " + arranque + " de arranque del dispositivo + " + (dur - arranque) + "; audio 9664 ms)");
      ok(r.getFotogramasMostrados() >= 270, "rep " + rep + ": se mostraron " + r.getFotogramasMostrados() + " de 289 fotogramas distintos");
      ok(r.getMaxRetrasoMs() <= 40, "rep " + rep + ": la imagen nunca va mas de un fotograma detras del audio (" + r.getMaxRetrasoMs() + " ms)");
      ok(arranque >= 0 && arranque < 2500, "rep " + rep + ": el reloj es la posicion del audio (empezo a avanzar a los " + arranque + " ms; hasta entonces, primer fotograma quieto)");
      for (int i = 0; i < 60 && (r.hilosVivos() || vivos("video-gif") > 0); i++) Thread.sleep(50);   // el de muestreo sale cuando el Clip termina de cerrarse
      ok(!r.hilosVivos() && vivos("video-gif") == 0, "rep " + rep + ": hilos de decodificacion y de muestreo terminados");
      SwingUtilities.invokeAndWait(() -> p[0].reproducirVideoVictoria(() -> fines[0]++)); Thread.sleep(300);
      ok(fines[0] == 1, "rep " + rep + ": una segunda llamada no vuelve a reproducir ni a terminar");
      SwingUtilities.invokeAndWait(() -> f[0].dispose());
    }
    // cortar a mitad (detenerVideo): no llama al fin y libera todo
    final PnlWin[] p = { null }; final int[] fines = { 0 };
    SwingUtilities.invokeAndWait(() -> { JFrame f = new JFrame(); p[0] = new PnlWin(); f.setContentPane(p[0]); f.setSize(800, 450); f.setVisible(true); p[0].reproducirVideoVictoria(() -> fines[0]++); });
    Thread.sleep(2500); SwingUtilities.invokeAndWait(() -> p[0].detenerVideo()); Thread.sleep(800);
    ok(fines[0] == 0 && !p[0].getReproductor().hilosVivos() && !p[0].getReproductor().isEnCurso(), "detenerVideo a mitad: sin fin, hilos terminados");
    Runtime rt = Runtime.getRuntime(); System.gc(); Thread.sleep(300); System.gc();
    long usado = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
    ok(usado < 200, "memoria viva tras " + reps + " reproducciones: " + usado + " MB");
    ref.cerrar();
    System.out.println("FALLOS: " + fallos); System.exit(0);
  }
}
