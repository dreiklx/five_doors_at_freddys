import javax.swing.*; import java.awt.*; import java.awt.event.*; import java.lang.reflect.*; import javax.sound.sampled.Clip;
import com.fdaf.mvc.controllers.*; import com.fdaf.mvc.views.frames.VistaPrincipal; import com.fdaf.mvc.views.frames.pnl.*; import com.fdaf.util.*; import com.fdaf.mvc.models.juego.Noche;
// Prueba automatizada del sonido hover sobre los componentes REALES: simula MOUSE_ENTERED y comprueba si el
// Clip compartido de botones_menu.wav arrancó. Verifica que suene una vez al entrar, que no suene al moverse
// dentro, ni en controles deshabilitados/inactivos, y que abrir/cerrar pantallas no acumule listeners.
public class PruebaHover { static int ok = 0, fallos = 0; static Clip clip; static VistaPrincipal vp;
  static Object f(Object o, Class<?> c, String n) throws Exception { Field x = c.getDeclaredField(n); x.setAccessible(true); return x.get(o); }
  static void edt(Runnable r) throws Exception { SwingUtilities.invokeAndWait(r); }
  static void inv(Method m, Object o) throws Exception { edt(() -> { try { m.invoke(o); } catch (Exception e) { throw new RuntimeException(e); } }); }
  static boolean suena(Component c, int tipo) throws Exception {
    edt(() -> { clip.stop(); clip.setFramePosition(0); });
    edt(() -> c.dispatchEvent(new MouseEvent(c, tipo, System.currentTimeMillis(), 0, 5, 5, 0, false)));
    Thread.sleep(120); boolean s = clip.getFramePosition() > 0; edt(() -> clip.stop()); return s; }
  static void esperar(String nombre, Component c, boolean esperado) throws Exception { esperar(nombre, c, esperado, MouseEvent.MOUSE_ENTERED); }
  static void esperar(String nombre, Component c, boolean esperado, int tipo) throws Exception {
    boolean s = suena(c, tipo); boolean bien = s == esperado; if (bien) ok++; else fallos++;
    System.out.println((bien ? "OK    " : "FALLO ") + nombre + " -> " + (s ? "suena" : "no suena") + " (esperado: " + (esperado ? "suena" : "no suena") + ")"); }
  static int oyentes(Component c) { return c.getMouseListeners().length; }
  static void click(Component c) throws Exception { edt(() -> c.dispatchEvent(new MouseEvent(c, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 5, 5, 1, false))); }
  public static void main(String[] a) throws Exception {
    PreferenciasJuego.idiomaSeleccionado = Idioma.valueOf(a[0]); PreferenciasJuego.escapeDesbloqueado = false; PreferenciasJuego.nocheActual = Noche.NOCHE_1;
    clip = (Clip) f(InteraccionBoton.obtenerSonidoHover(), com.fdaf.mvc.views.multimedia.Sonido.class, "clip");
    final ControllerMenu[] cm = new ControllerMenu[1];
    edt(() -> { cm[0] = new ControllerMenu(true); cm[0].init(); });
    vp = (VistaPrincipal) f(cm[0], ControllerMenu.class, "vp"); PnlMenu menu = (PnlMenu) f(cm[0], ControllerMenu.class, "menu");
    PnlOpciones op = (PnlOpciones) f(cm[0], ControllerMenu.class, "opciones"); PnlIdioma id = (PnlIdioma) f(cm[0], ControllerMenu.class, "pantallaIdioma");
    PnlVolumen vol = (PnlVolumen) f(cm[0], ControllerMenu.class, "pantallaVolumen"); PnlNochePersonalizada np = (PnlNochePersonalizada) f(cm[0], ControllerMenu.class, "pantallaNochePersonalizada");
    Thread.sleep(800);
    System.out.println("== Menu principal");
    esperar("Continuar", menu.getBtnContinuar(), true); esperar("Nueva Partida", menu.getBtnNuevaPartida(), true); esperar("Opciones", menu.getBtnOpciones(), true);
    esperar("Como jugar", menu.getBtnComoJugar(), true); esperar("Acerca del proyecto", menu.getBtnAcercaDe(), true);
    esperar("Moverse dentro de Opciones (MOUSE_MOVED)", menu.getBtnOpciones(), false, MouseEvent.MOUSE_MOVED);
    edt(() -> menu.getBtnNuevaPartida().setEnabled(false)); esperar("Nueva Partida deshabilitado", menu.getBtnNuevaPartida(), false); edt(() -> menu.getBtnNuevaPartida().setEnabled(true));
    esperar("Dialogo: Confirmar", menu.getPnlConfirmacion().getBtnConfirmar(), true); esperar("Dialogo: Cancelar", menu.getPnlConfirmacion().getBtnCancelar(), true);
    System.out.println("== Opciones / Idioma / Volumen / Noche personalizada");
    int antesAtras = oyentes(op.getBtnAtras()), antesOpc = oyentes(menu.getBtnOpciones()), antesAtrasAl = op.getBtnAtras().getActionListeners().length;
    for (int i = 0; i < 5; i++) { edt(() -> menu.getBtnOpciones().doClick()); edt(() -> op.getBtnAtras().doClick()); }
    System.out.println("listeners raton Atras(Opciones) " + antesAtras + " -> " + oyentes(op.getBtnAtras()) + ", accion " + antesAtrasAl + " -> " + op.getBtnAtras().getActionListeners().length + ", Opciones " + antesOpc + " -> " + oyentes(menu.getBtnOpciones()) + " (tras abrir/cerrar Opciones 5 veces)");
    if (antesOpc == oyentes(menu.getBtnOpciones()) && antesAtras == oyentes(op.getBtnAtras()) && op.getBtnAtras().getActionListeners().length <= Math.max(1, antesAtrasAl)) ok++; else { fallos++; System.out.println("FALLO listeners acumulados"); }
    esperar("Opciones > Idioma", op.getBtnIdioma(), true); esperar("Opciones > Noche personalizada", op.getBtnCustomNight(), true); esperar("Opciones > Volumen", op.getBtnVolumen(), true);
    esperar("Opciones > Atras", op.getBtnAtras(), true); esperar("Opciones > Salir", op.getBtnSalir(), true);
    esperar("Idioma > Espanol", id.getBtnEspanol(), true); esperar("Idioma > English", id.getBtnIngles(), true); esperar("Idioma > Atras", id.getBtnAtras(), true);
    esperar("Volumen > Subir", vol.getBtnSubir(), true); esperar("Volumen > Bajar", vol.getBtnBajar(), true); esperar("Volumen > Atras", vol.getBtnAtras(), true);
    // jugador nuevo: las 5 noches bloqueadas no suenan (CLAUDE.md §1.66); al superar la Noche 1, la 1 vuelve a sonar
    for (int i = 0; i < 5; i++) esperar("Noche personalizada > Noche " + (i + 1) + " (bloqueada)", np.getBtnsNoche()[i], false);
    com.fdaf.util.PreferenciasJuego.nochesSuperadas[0] = true;
    esperar("Noche personalizada > Noche 1 (superada)", np.getBtnsNoche()[0], true);
    com.fdaf.util.PreferenciasJuego.nochesSuperadas[0] = false;
    esperar("Noche personalizada > Atras", np.getBtnAtras(), true); esperar("Noche personalizada > ESCAPE (bloqueado, responde con mensaje)", np.getBtnEscape(), true);
    System.out.println("== Cambio de idioma desde Opciones y controles despues");
    int antesNP = oyentes(menu.getBtnNuevaPartida());
    edt(() -> id.getBtnIngles().doClick()); System.out.println("tras English: Nueva Partida='" + menu.getBtnNuevaPartida().getText() + "'");
    edt(() -> id.getBtnEspanol().doClick()); System.out.println("tras Espanol: Nueva Partida='" + menu.getBtnNuevaPartida().getText() + "'");
    esperar("Nueva Partida tras cambiar idioma dos veces", menu.getBtnNuevaPartida(), true);
    System.out.println("listeners Nueva Partida " + antesNP + " -> " + oyentes(menu.getBtnNuevaPartida()));
    if (antesNP == oyentes(menu.getBtnNuevaPartida())) ok++; else fallos++;
    System.out.println("== Selector de idioma inicial, Como jugar, Acerca del proyecto");
    Method sel = ControllerMenu.class.getDeclaredMethod("mostrarSelectorIdioma"); sel.setAccessible(true); inv(sel, cm[0]);
    PnlSelectorIdioma ps = (PnlSelectorIdioma) vp.pnlContenido.getComponent(0);
    esperar("Selector: Espanol", ps.getBoton(Idioma.ESPANOL), true); esperar("Selector: English", ps.getBoton(Idioma.INGLES), true);
    Method cj = ControllerMenu.class.getDeclaredMethod("abrirComoJugar"); cj.setAccessible(true);
    PnlInfo info = (PnlInfo) f(cm[0], ControllerMenu.class, "pantallaInfo");
    for (int vez = 1; vez <= 3; vez++) inv(cj, cm[0]);
    JButton[] tabs = info.getPestanas(); System.out.println("Como jugar abierto 3 veces: " + tabs.length + " pestanas, listeners raton en la primera: " + oyentes(tabs[0]) + ", en Siguiente: " + oyentes(info.getBtnSiguiente()));
    for (int i = 0; i < tabs.length; i++) esperar("Como jugar > pestana '" + tabs[i].getText() + "'", tabs[i], true);
    esperar("Como jugar > Siguiente", info.getBtnSiguiente(), true); edt(() -> info.getBtnSiguiente().doClick()); esperar("Como jugar > Anterior", info.getBtnAnterior(), true); esperar("Como jugar > Volver al menu", info.getBtnOmitir(), true);
    Method ad = ControllerMenu.class.getDeclaredMethod("abrirAcercaDe"); ad.setAccessible(true); inv(ad, cm[0]);
    esperar("Acerca > Volver al menu", info.getBtnSiguiente(), true);
    System.out.println("== Partida (Noche 1): oficina, llamada, tableta");
    final ControllerInterfaz[] ci = new ControllerInterfaz[1];
    edt(() -> { ci[0] = new ControllerInterfaz(); ci[0].init(vp, menu, cm[0]); });
    PnlJuego j = (PnlJuego) f(ci[0], ControllerInterfaz.class, "pnlJuego"); PnlTableta t = (PnlTableta) f(ci[0], ControllerInterfaz.class, "pnlTableta");
    ControllerJuego cjg = (ControllerJuego) f(ci[0], ControllerInterfaz.class, "controllerJuego");
    edt(() -> vp.setContenido(j)); Thread.sleep(300);
    System.out.println("llamada activa al empezar: " + cjg.isLlamadaActiva());
    esperar("PUERTA izq durante la llamada", j.getBotonIzq(), false); esperar("LUZ der durante la llamada", j.getLuzDer(), false);
    esperar("COLGAR / MUTE CALL", j.getLblMuteCall(), true);
    click(j.getLblMuteCall()); click(j.getLblMuteCall());
    System.out.println("tras colgar (2 clics): llamada activa=" + cjg.isLlamadaActiva() + " boton visible=" + j.getLblMuteCall().isVisible());
    // §1.77: puertas, luces, objeto de la puerta, huecos sin obtener y barra de Rendirse no tienen hover
    esperar("PUERTA izq tras colgar (sin hover, 1.77)", j.getBotonIzq(), false); esperar("PUERTA der (sin hover)", j.getBotonDer(), false); esperar("LUZ izq (sin hover)", j.getLuzIzq(), false); esperar("LUZ der (sin hover)", j.getLuzDer(), false);
    esperar("COLGAR ya oculto", j.getLblMuteCall(), false);
    JLabel ov = j.getLblOverlay();
    edt(() -> { ov.setIcon(new ImageIcon(new java.awt.image.BufferedImage(10, 10, 2))); ov.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); ov.setVisible(true); });
    esperar("Objeto recogible revelado (sin hover, 1.77)", ov, false);
    edt(() -> ov.setCursor(Cursor.getDefaultCursor())); esperar("Overlay mostrando jumpscare", ov, false); edt(() -> { ov.setVisible(false); ov.setIcon(null); });
    edt(() -> vp.setContenido(t)); Thread.sleep(300);
    esperar("Tableta: hueco sin obtener (Peluche Freddy)", t.getPELUCHE_FREDDY(), false); esperar("Tableta: hueco con X en Noche 1 (Balloon Boy)", t.getBALLOONBOY(), false);
    edt(() -> { com.fdaf.mvc.models.coleccionables.Coleccionable c = cjg.getJuego().getInventario().buscarPorTipo(com.fdaf.mvc.models.coleccionables.TipoColeccionable.PELUCHE_FREDDY);
        if (c == null) { c = new com.fdaf.mvc.models.coleccionables.Coleccionable(com.fdaf.mvc.models.coleccionables.TipoColeccionable.PELUCHE_FREDDY); cjg.getJuego().getInventario().agregar(c); } c.setEncontrado(true); });
    esperar("Tableta: hueco ya obtenido (Peluche Freddy)", t.getPELUCHE_FREDDY(), true);
    esperar("Tableta: barra Rendirse (sin hover, 1.77)", t.getPbarRendirse(), false);
    edt(() -> cjg.setJuegoTerminado(true)); esperar("Rendirse con la partida terminada", t.getPbarRendirse(), false); esperar("Hueco obtenido con la partida terminada", t.getPELUCHE_FREDDY(), false);
    edt(() -> vp.setContenido(j)); esperar("PUERTA con la partida terminada", j.getBotonIzq(), false);
    edt(() -> ci[0].detenerSonidosJuego());
    System.out.println("RESULTADO: " + ok + " OK, " + fallos + " fallos"); System.exit(0); }
}
