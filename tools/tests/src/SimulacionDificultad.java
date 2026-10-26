import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Random;

import com.fdaf.mvc.models.juego.Juego;
import com.fdaf.mvc.models.juego.Noche;
import com.fdaf.mvc.models.puerta.Puerta;
import com.fdaf.mvc.models.puerta.TipoPuerta;
import com.fdaf.util.PreferenciasJuego;

/**
 * Simulacion Monte Carlo de la dificultad de FDAF (CLAUDE.md §1.78) con las clases REALES del juego: Juego,
 * GeneradorArbol, Arbol y la tabla de baterias. Solo se fijan las semillas (reproducible) y se juega con la politica
 * del jugador: en cada paso abre una puerta al azar (izquierda o derecha: no hay informacion previa) y la revela, y
 * recoge lo que salga (coleccionable o bateria). Asi juegan igual un jugador nuevo y uno veterano, porque el juego no
 * ofrece ninguna pista antes de revelar.
 *
 * Uso: java -cp <clases de FDAF>;<esto> SimulacionDificultad [partidas=20000] [semilla=1] [variante]
 * variante "bateria_desde_noche3": anula la tabla en las noches 1 y 2 (el cambio local ajeno pendiente de Juego.java).
 * variante "tabla:a1,b1,a2,b2,...,a5,b5": prueba otra tabla de baterias [1 vida, 2 vidas] por noche (solo en memoria).
 */
public class SimulacionDificultad {

    public static void main(String[] a) throws Exception {
        int partidas = a.length > 0 ? Integer.parseInt(a[0]) : 20000;
        long semilla = a.length > 1 ? Long.parseLong(a[1]) : 1L;
        String variante = a.length > 2 ? a[2] : "publicada";
        Field fGen = Juego.class.getDeclaredField("generador");
        fGen.setAccessible(true);
        Field fRndGen = Class.forName("com.fdaf.mvc.models.juego.GeneradorArbol").getDeclaredField("random");
        fRndGen.setAccessible(true);
        Field fRndBat = Juego.class.getDeclaredField("randomBateria");
        fRndBat.setAccessible(true);
        Field mods = Field.class.getDeclaredField("modifiers");
        mods.setAccessible(true);
        mods.setInt(fRndBat, fRndBat.getModifiers() & ~Modifier.FINAL);
        Field fTabla = Juego.class.getDeclaredField("PROBABILIDAD_BATERIA");
        fTabla.setAccessible(true);
        double[][] tabla = (double[][]) fTabla.get(null);
        if (variante.equals("bateria_desde_noche3")) {
            tabla[0][0] = tabla[0][1] = tabla[1][0] = tabla[1][1] = 0;
        } else if (variante.startsWith("tabla:")) {
            String[] v = variante.substring(6).split(",");
            for (int i = 0; i < 10; i++) {
                tabla[i / 2][i % 2] = Double.parseDouble(v[i]);
            }
        }
        System.out.println("variante " + variante + ", " + partidas + " partidas por noche, semilla " + semilla);
        System.out.println("tabla de baterias [1 vida, 2 vidas]: " + java.util.Arrays.deepToString(tabla));
        System.out.println("noche | vidas | objetos | gana  | puertas reveladas (media, p90) | animatronicos (media) | baterias (media) | vidas al ganar (media) | pierde con >=80% de objetos | pierde con <=20% | arboles (media)");
        for (Noche noche : Noche.values()) {
            PreferenciasJuego.nocheActual = noche;
            Random elegir = new Random(semilla * 1000 + noche.ordinal());
            int ganadas = 0, perdidasCasi = 0, perdidasPronto = 0;
            long sumaPuertas = 0, sumaAnim = 0, sumaBat = 0, sumaVidasFin = 0, sumaArboles = 0;
            int[] puertasHist = new int[2000];
            for (int p = 0; p < partidas; p++) {
                Juego juego = new Juego();
                fRndGen.set(fGen.get(juego), new Random(semilla * 7919 + noche.ordinal() * 100003L + p));
                fRndBat.set(null, new Random(semilla * 104729 + noche.ordinal() * 1000003L + p));
                juego.reiniciar();   // regenera el primer arbol con las semillas fijadas
                int puertas = 0, anim = 0, bat = 0, arboles = 1;
                Object arbolAntes = juego.getArbol();
                while (!juego.gano() && !juego.perdio() && puertas < 5000) {
                    Puerta pu = elegir.nextBoolean() ? juego.elegirIzquierda() : juego.elegirDerecha();
                    if (juego.getArbol() != arbolAntes) {
                        arboles++;
                        arbolAntes = juego.getArbol();
                    }
                    if (pu == null) {
                        continue;
                    }
                    puertas++;
                    if (pu.getTipo() == TipoPuerta.ANIMATRONICO) {
                        juego.perderVidaPorAnimatronico();
                        anim++;
                    } else if (pu.getTipo() == TipoPuerta.COLECCIONABLE) {
                        juego.marcarColeccionableEncontrado(pu.getColeccionable());
                    } else if (pu.getTipo() == TipoPuerta.BATERIA) {
                        juego.ganarVidaExtra();
                        bat++;
                    }
                }
                int total = noche.getCantidadColeccionables();
                int encontrados = juego.getColeccionablesEncontrados();
                if (juego.gano()) {
                    ganadas++;
                    sumaVidasFin += juego.getVidas();
                } else {
                    if (encontrados >= Math.ceil(total * 0.8)) perdidasCasi++;
                    if (encontrados <= Math.floor(total * 0.2)) perdidasPronto++;
                }
                sumaPuertas += puertas;
                sumaAnim += anim;
                sumaBat += bat;
                sumaArboles += arboles;
                puertasHist[Math.min(puertas, puertasHist.length - 1)]++;
            }
            int acum = 0, p90 = 0;
            for (int i = 0; i < puertasHist.length; i++) {
                acum += puertasHist[i];
                if (acum >= partidas * 0.9) { p90 = i; break; }
            }
            System.out.printf("%s |   %d   |   %2d    | %5.1f%% | %5.1f, %3d | %5.2f | %4.2f | %4.2f | %5.1f%% | %5.1f%% | %4.2f%n",
                    noche, noche.getVidas(), noche.getCantidadColeccionables(), 100.0 * ganadas / partidas,
                    sumaPuertas / (double) partidas, p90, sumaAnim / (double) partidas, sumaBat / (double) partidas,
                    ganadas > 0 ? sumaVidasFin / (double) ganadas : 0, 100.0 * perdidasCasi / partidas,
                    100.0 * perdidasPronto / partidas, sumaArboles / (double) partidas);
        }
    }
}
