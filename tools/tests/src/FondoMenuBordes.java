import java.awt.*; import java.awt.image.*; import java.io.*; import javax.imageio.*;
import com.fdaf.mvc.views.frames.pnl.PnlAplicarImagen;
// Dibuja cada cuadro del GIF del menu como antes (estirado) y con dibujarFondoMenu, a varias resoluciones, y cuenta pixeles casi blancos en los 4 px del borde.
public class FondoMenuBordes {
  public static void main(String[] a) throws Exception {
    File dir=new File(a[0]); int[][] res={{1920,1080},{1600,900},{1366,768},{2560,1440},{1280,1024}};
    int antesTot=0, despuesTot=0; int cuadrosAntes=0, cuadrosDespues=0;
    for(int[] r: res){ for(int i=0;i<70;i++){ BufferedImage f=ImageIO.read(new File(dir,String.format("f%02d.png",i)));
      int antes=bordes(f,r[0],r[1],false), desp=bordes(f,r[0],r[1],true); antesTot+=antes; despuesTot+=desp; if(antes>0) cuadrosAntes++; if(desp>0) cuadrosDespues++;
      if(i==61 && r[0]==1920){ ImageIO.write(pintar(f,r[0],r[1],false),"png",new File(dir,"render_antes_61.png")); ImageIO.write(pintar(f,r[0],r[1],true),"png",new File(dir,"render_despues_61.png")); } } }
    System.out.println("pixeles blancos en el borde: antes "+antesTot+" ("+cuadrosAntes+" cuadros*res), despues "+despuesTot+" ("+cuadrosDespues+")");
  }
  static BufferedImage pintar(BufferedImage f,int w,int h,boolean nuevo){ BufferedImage o=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB); Graphics2D g=o.createGraphics(); g.setColor(Color.BLACK); g.fillRect(0,0,w,h);
    if(nuevo) PnlAplicarImagen.dibujarFondoMenu(g,f,w,h,null); else g.drawImage(f,0,0,w,h,null); g.dispose(); return o; }
  static int bordes(BufferedImage f,int w,int h,boolean nuevo){ BufferedImage o=pintar(f,w,h,nuevo); int n=0;
    for(int y=0;y<h;y++) for(int x=0;x<w;x++){ if(x>=4&&x<w-4&&y>=4&&y<h-4) continue; int c=o.getRGB(x,y); int R=(c>>16)&255,G=(c>>8)&255,B=c&255; if(Math.min(R,Math.min(G,B))>170) n++; } return n; }
}
