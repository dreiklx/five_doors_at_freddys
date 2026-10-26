import java.awt.*; import java.awt.image.BufferedImage; import javax.swing.*; import java.io.File;
import com.fdaf.mvc.views.frames.pnl.*; import com.fdaf.util.*;
public class PaginasInfo { static int problemas = 0;
  public static void main(String[] a) throws Exception { SwingUtilities.invokeAndWait(() -> { try {
    new JLabel(); // inicializa Swing (DPI) antes de medir
    Icon logo = new ImageIcon(new ImageIcon(PaginasInfo.class.getResource("/branding/nyder_logo.png")).getImage().getScaledInstance(144, 144, Image.SCALE_SMOOTH));
    int[][] tamanos = {{1920,1080},{1920,1440},{2520,1080}};
    for (Idioma id : Idioma.values()) { PreferenciasJuego.idiomaSeleccionado = id;
      java.util.List<BufferedImage> hoja = new java.util.ArrayList<>();
      for (int[] t : tamanos) {
        String[][] modos = {{"intro"},{"guia0"},{"guia1"},{"acerca"}};
        for (String[] m : modos) { PnlInfo p = new PnlInfo(); p.setSize(t[0], t[1]);
          switch (m[0]) {
            case "intro": p.configurar(TextosInfo.introTitulos(), TextosInfo.introCuerpos(), null, null, TextosInfo.botonOmitir(), TextosInfo.botonIrAlMenu(), null); break;
            case "guia0": case "guia1": boolean e = m[0].equals("guia1"); p.configurar(TextosInfo.guiaTitulos(e), TextosInfo.guiaCuerpos(e), TextosInfo.guiaTitulos(e), null, TextosInfo.botonVolverAlMenu(), TextosInfo.botonVolverAlMenu(), null); break;
            default: p.configurar(new String[]{TextosInfo.acercaTitulo()}, new String[]{TextosInfo.acercaCuerpo()}, null, logo, null, TextosInfo.botonVolverAlMenu(), null);
          }
          for (int i = 0; i < p.getCantidadPaginas(); i++) {
            if (i > 0) p.getBtnSiguiente().doClick();
            p.doLayout(); revisar(id + " " + t[0] + "x" + t[1] + " " + m[0] + " pag" + (i+1), p);
            if (t[0] == 1920 && t[1] == 1080) { BufferedImage b = new BufferedImage(640, 360, BufferedImage.TYPE_INT_RGB); Graphics2D g = b.createGraphics(); g.scale(640.0/1920, 360.0/1080); p.printAll(g); g.dispose(); hoja.add(b); }
          }
        }
      }
      int cols = 4, filas = (hoja.size() + cols - 1) / cols; BufferedImage h = new BufferedImage(cols * 640, filas * 360, BufferedImage.TYPE_INT_RGB); Graphics2D g = h.createGraphics();
      for (int i = 0; i < hoja.size(); i++) g.drawImage(hoja.get(i), (i % cols) * 640, (i / cols) * 360, null);
      javax.imageio.ImageIO.write(h, "png", new File(a[0] + "_" + id + ".png"));
    }
    System.out.println("PROBLEMAS=" + problemas);
  } catch (Exception e) { e.printStackTrace(); } }); System.exit(0); }
  static void revisar(String ctx, PnlInfo p) {
    JLabel c = p.getLblCuerpo(); int pref = c.getPreferredSize().height; if (c.getPreferredSize().width > c.getWidth()) { problemas++; System.out.println("DESBORDA ANCHO cuerpo " + ctx + " " + c.getPreferredSize().width + ">" + c.getWidth()); }
    if (pref > c.getHeight()) { problemas++; System.out.println("DESBORDA cuerpo " + ctx + " pref=" + pref + " caja=" + c.getHeight()); }
    JLabel t = p.getLblTitulo(); int tw = t.getFontMetrics(t.getFont()).stringWidth(t.getText()); if (tw > t.getWidth()) { problemas++; System.out.println("DESBORDA titulo " + ctx); }
    java.util.List<JButton> bs = new java.util.ArrayList<>(java.util.Arrays.asList(p.getBtnAnterior(), p.getBtnSiguiente(), p.getBtnOmitir())); bs.addAll(java.util.Arrays.asList(p.getPestanas()));
    for (JButton b : bs) if (b.isVisible() && b.getText() != null) { int w = b.getFontMetrics(b.getFont()).stringWidth(b.getText()); if (w > b.getWidth()) { problemas++; System.out.println("DESBORDA boton '" + b.getText() + "' " + ctx + " " + w + ">" + b.getWidth()); } }
  }
}
