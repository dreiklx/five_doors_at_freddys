import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.fdaf.mvc.views.frames.pnl.PnlWarning;

/**
 * Exporta la presentacion de NYDER GAMES (fundido de entrada, destello de la estrella de la D y fundido de salida)
 * como secuencia PNG con transparencia real (CLAUDE.md §1.69). Dibuja con el MISMO PnlWarning que usa el juego (el
 * mismo fundido lineal, la misma mascara de la estrella y el mismo halo/chispazo), pero sobre un lienzo transparente
 * en lugar del negro de la pantalla de inicio. Los tiempos son los de ControllerMenu (§1.31, §1.65):
 *   entrada 800 ms (alfa 0 -> 1), visible 1300 ms, salida 650 ms (alfa 1 -> 0);
 *   destello dentro de lo visible: espera 120, subida 420 (cuadratica), pico 60, bajada 600 (cuadratica).
 * No incluye los 250 ms de negro previos (en el juego son pantalla vacia).
 *
 * Uso (con las clases de FDAF compiladas en el classpath, ver exportar_nyder_alfa.sh):
 *   java ExportarNyderAlfa <carpeta_salida> [ancho=1920] [alto=800] [fps=30]
 */
public class ExportarNyderAlfa {

    static final int ENTRADA_MS = 800, VISIBLE_MS = 1300, SALIDA_MS = 650;
    static final int ESPERA_MS = 120, SUBIDA_MS = 420, PICO_MS = 60, BAJADA_MS = 600;

    public static void main(String[] a) throws Exception {
        File salida = new File(a[0]);
        salida.mkdirs();
        int ancho = a.length > 1 ? Integer.parseInt(a[1]) : 1920;
        int alto = a.length > 2 ? Integer.parseInt(a[2]) : 800;
        int fps = a.length > 3 ? Integer.parseInt(a[3]) : 30;
        Image logo = new ImageIcon(ExportarNyderAlfa.class.getResource("/branding/nyder_games_splash.png")).getImage();
        // fraccion 1.0: el logo ocupa todo el ancho; el alto del lienzo deja margen arriba y abajo para el chispazo
        PnlWarning panel = new PnlWarning(logo, 1.0);
        panel.setOpaque(false);
        panel.setSize(ancho, alto);
        panel.prepararDestelloEstrella();
        long limite = System.currentTimeMillis() + 10000;
        while (!panel.isDestelloEstrellaListo() && System.currentTimeMillis() < limite) {
            Thread.sleep(20);
        }
        if (!panel.isDestelloEstrellaListo()) {
            throw new IllegalStateException("no se pudo preparar la mascara de la estrella");
        }
        int totalMs = ENTRADA_MS + VISIBLE_MS + SALIDA_MS;
        // del primer al ultimo cuadro con algo visible: los extremos exactos del fundido (alfa 0) serian cuadros
        // vacios, asi que la secuencia empieza en t = 1/fps y termina un cuadro antes del final de la salida
        int cuadros = Math.round(totalMs * fps / 1000f) - 1;
        for (int i = 0; i < cuadros; i++) {
            float t = (i + 1) * 1000f / fps;
            float alfa;
            if (t < ENTRADA_MS) {
                alfa = t / ENTRADA_MS;
            } else if (t < ENTRADA_MS + VISIBLE_MS) {
                alfa = 1f;
            } else {
                alfa = Math.max(0f, 1f - (t - ENTRADA_MS - VISIBLE_MS) / SALIDA_MS);
            }
            panel.setAlpha(alfa);
            panel.setBrilloEstrella(brillo(t - ENTRADA_MS));
            BufferedImage img = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setComposite(AlphaComposite.Clear);
            g.fillRect(0, 0, ancho, alto);
            g.setComposite(AlphaComposite.SrcOver);
            panel.paint(g);
            g.dispose();
            ImageIO.write(img, "png", new File(salida, String.format("nyder_games_%04d.png", i + 1)));
        }
        System.out.println("cuadros " + cuadros + " a " + fps + " fps (" + totalMs + " ms), " + ancho + "x" + alto);
        System.exit(0);
    }

    /** La misma curva que ControllerMenu.iniciarDestelloNyder, en ms desde que termina el fundido de entrada. */
    static float brillo(float t) {
        float u = t - ESPERA_MS;
        if (t < 0f || u <= 0f) {
            return 0f;
        }
        if (u < SUBIDA_MS) {
            float k = u / SUBIDA_MS;
            return k * k;
        }
        if (u < SUBIDA_MS + PICO_MS) {
            return 1f;
        }
        float k = Math.min(1f, (u - SUBIDA_MS - PICO_MS) / BAJADA_MS);
        return (1f - k) * (1f - k);
    }
}
