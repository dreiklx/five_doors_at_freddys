import java.awt.*; import java.awt.image.*; import java.io.*; import javax.imageio.*; import javax.imageio.metadata.*; import javax.imageio.stream.*;
import org.w3c.dom.*;
import com.fdaf.mvc.views.frames.pnl.PnlAplicarImagen;
// Dibuja cada cuadro del GIF del menu como antes (estirado) y con dibujarFondoMenu, a varias resoluciones, y cuenta
// pixeles casi blancos en los 4 px del borde (CLAUDE.md §1.68). Los cuadros se extraen del propio recurso del juego
// (/backgrounds/menu_background.gif), compuestos sobre un lienzo como los muestra un visor (§1.75). args: carpeta salida
public class FondoMenuBordes {
  public static void main(String[] a) throws Exception {
    File dir=new File(a[0]); dir.mkdirs(); int[][] res={{1920,1080},{1600,900},{1366,768},{2560,1440},{1280,1024}};
    java.util.List<BufferedImage> cuadros=extraer("/backgrounds/menu_background.gif");
    int antesTot=0, despuesTot=0; int cuadrosAntes=0, cuadrosDespues=0;
    for(int[] r: res){ for(int i=0;i<cuadros.size();i++){ BufferedImage f=cuadros.get(i);
      int antes=bordes(f,r[0],r[1],false), desp=bordes(f,r[0],r[1],true); antesTot+=antes; despuesTot+=desp; if(antes>0) cuadrosAntes++; if(desp>0) cuadrosDespues++;
      if(i==61 && r[0]==1920){ ImageIO.write(pintar(f,r[0],r[1],false),"png",new File(dir,"render_antes_61.png")); ImageIO.write(pintar(f,r[0],r[1],true),"png",new File(dir,"render_despues_61.png")); } } }
    System.out.println("cuadros del GIF: "+cuadros.size());
    System.out.println("pixeles blancos en el borde: antes "+antesTot+" ("+cuadrosAntes+" cuadros*res), despues "+despuesTot+" ("+cuadrosDespues+")");
    boolean ok=despuesTot==0 && cuadros.size()>0;
    System.out.println((ok?"OK    ":"FALLO ")+"dibujarFondoMenu no deja la linea blanca del GIF en ningun borde ("+despuesTot+" pixeles)");
    System.out.println("FALLOS: "+(ok?0:1));
  }
  /** Cuadros completos del GIF: cada uno se pinta en su posicion sobre el lienzo acumulado (disposicion restoreToBackground: se borra su zona despues). */
  static java.util.List<BufferedImage> extraer(String recurso) throws Exception {
    java.util.List<BufferedImage> out=new java.util.ArrayList<>();
    ImageReader rd=ImageIO.getImageReadersByFormatName("gif").next();
    try(ImageInputStream in=ImageIO.createImageInputStream(FondoMenuBordes.class.getResourceAsStream(recurso))){
      rd.setInput(in,false);
      int w=-1,h=-1; IIOMetadata sm=rd.getStreamMetadata();
      if(sm!=null){ Node lsd=buscar(sm.getAsTree("javax_imageio_gif_stream_1.0"),"LogicalScreenDescriptor"); if(lsd!=null){ w=Integer.parseInt(attr(lsd,"logicalScreenWidth")); h=Integer.parseInt(attr(lsd,"logicalScreenHeight")); } }
      BufferedImage lienzo=null; int n=rd.getNumImages(true);
      for(int i=0;i<n;i++){ BufferedImage f=rd.read(i); Node raiz=rd.getImageMetadata(i).getAsTree("javax_imageio_gif_image_1.0");
        Node id=buscar(raiz,"ImageDescriptor"); int x=Integer.parseInt(attr(id,"imageLeftPosition")), y=Integer.parseInt(attr(id,"imageTopPosition"));
        Node gce=buscar(raiz,"GraphicControlExtension"); String disp=gce!=null?attr(gce,"disposalMethod"):"none";
        if(lienzo==null){ if(w<=0){ w=f.getWidth()+x; h=f.getHeight()+y; } lienzo=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB); }
        Graphics2D g=lienzo.createGraphics(); g.drawImage(f,x,y,null); g.dispose();
        BufferedImage copia=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB); Graphics2D gc=copia.createGraphics(); gc.drawImage(lienzo,0,0,null); gc.dispose(); out.add(copia);
        if("restoreToBackgroundColor".equals(disp)){ Graphics2D gb=lienzo.createGraphics(); gb.setComposite(AlphaComposite.Clear); gb.fillRect(x,y,f.getWidth(),f.getHeight()); gb.dispose(); } } }
    return out;
  }
  static Node buscar(Node n,String nombre){ if(n.getNodeName().equals(nombre)) return n; for(Node c=n.getFirstChild();c!=null;c=c.getNextSibling()){ Node r=buscar(c,nombre); if(r!=null) return r; } return null; }
  static String attr(Node n,String k){ Node v=n.getAttributes().getNamedItem(k); return v==null?null:v.getNodeValue(); }
  static BufferedImage pintar(BufferedImage f,int w,int h,boolean nuevo){ BufferedImage o=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB); Graphics2D g=o.createGraphics(); g.setColor(Color.BLACK); g.fillRect(0,0,w,h);
    if(nuevo) PnlAplicarImagen.dibujarFondoMenu(g,f,w,h,null); else g.drawImage(f,0,0,w,h,null); g.dispose(); return o; }
  static int bordes(BufferedImage f,int w,int h,boolean nuevo){ BufferedImage o=pintar(f,w,h,nuevo); int n=0;
    for(int y=0;y<h;y++) for(int x=0;x<w;x++){ if(x>=4&&x<w-4&&y>=4&&y<h-4) continue; int c=o.getRGB(x,y); int R=(c>>16)&255,G=(c>>8)&255,B=c&255; if(Math.min(R,Math.min(G,B))>170) n++; } return n; }
}
