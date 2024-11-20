package edu.epsevg.prop.lab.c4;

import java.util.HashMap;

/**
 * Classe que implementa un jugador per al joc Connecta 4.
 * Aquest jugador utilitza l'algoritme Minimax optimitzat amb poda alpha-beta 
 * i una taula d'estats per evitar repetir càlculs. 
 * 
 * A més, inclou una heurística personalitzada per avaluar l'estat del tauler 
 * basant-se en línies, diagonals i posicions estratègiques cercant en la
 * profunditat seleccionada per l'usuari.
 * 
 * @author Denis Vera Iriyari
 */
public class C4Beast implements Jugador, IAuto {
    private String nom; // Nom del jugador
    private int profunditatMax; // Profunditat màxima de la cerca Minimax
    private int colorJugador; // Color del jugador actual (1 o -1)
    private int midaTauler; // Mida del tauler actual
    private HashMap<String, Integer> taulaEstats; // Memòria d'estats ja calculats
    private int nodesExplorats; // Comptador de nodes explorats durant la cerca

    private static final int MAX_SCORE = 10000; // Valor heurístic per a una victòria

    /**
     * Constructor de la classe C4Beast.
     * 
     * @param profunditat Profunditat màxima que explorarà l'algoritme Minimax.
     */
    public C4Beast(int profunditat) {
        this.nom = "C4Beast";
        this.profunditatMax = profunditat;
        this.taulaEstats = new HashMap<>();
    }

    /**
     * Retorna el nom del jugador.
     * 
     * @return El nom del jugador com a cadena de text.
     */
    @Override
    public String nom() {
        return nom;
    }

    /**
     * Determina el millor moviment possible en l'estat actual del tauler.
     * Utilitza l'algoritme Minimax amb poda alpha-beta per trobar la millor jugada.
     * 
     * @param t Estat actual del tauler.
     * @param color Color del jugador que mou (1 per jugador actual, -1 per rival).
     * @return Índex de la columna on realitzar el moviment.
     */
    @Override
    public int moviment(Tauler t, int color) {
        this.colorJugador = color;
        this.midaTauler = t.getMida();
        this.taulaEstats.clear();
        this.nodesExplorats = 0;

        int millorMoviment = -1;
        int millorValor = Integer.MIN_VALUE;

        System.out.println("Iniciant moviment en profunditat " + profunditatMax);

        // Iterem per les columnes segons un ordre predeterminat
        for (int col : ordreNodes()) {
            if (t.movpossible(col)) {
                Tauler nouTauler = new Tauler(t);
                nouTauler.afegeix(col, colorJugador);

                int valor = minMax(nouTauler, profunditatMax - 1, false, Integer.MIN_VALUE, Integer.MAX_VALUE);

                if (valor > millorValor) {
                    millorValor = valor;
                    millorMoviment = col;
                }
            }
        }

        System.out.println("Nodes explorats: " + nodesExplorats);
        System.out.println("Moviment escollit: columna " + millorMoviment + " amb valor " + millorValor);

        return millorMoviment;
    }

    /**
     * Implementació recursiva de l'algoritme Minimax amb poda alpha-beta.
     * 
     * @param t Estat actual del tauler.
     * @param profunditat Profunditat restant per explorar.
     * @param maximitza Si és el torn del jugador que maximitza el valor heurístic.
     * @param alpha El valor alfa per a la poda alpha-beta.
     * @param beta El valor beta per a la poda alpha-beta.
     * @return Valor heurístic del millor moviment des de l'estat actual.
     */
    private int minMax(Tauler t, int profunditat, boolean maximitza, int alpha, int beta) {
        nodesExplorats++;

        // Generem una clau per identificar aquest estat
        String estat = generaClauEstat(t, profunditat);

        // Si ja hem calculat aquest estat, retornem el valor emmagatzemat
        if (taulaEstats.containsKey(estat)) {
            return taulaEstats.get(estat);
        }

        // Cas base: profunditat 0 o tauler sense moviments possibles
        if (profunditat == 0 || !t.espotmoure()) {
            int valor = calculaHeuristica(t);
            taulaEstats.put(estat, valor);
            return valor;
        }

        int millorValor = maximitza ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int colorActual = maximitza ? colorJugador : -colorJugador;

        // Explorem les columnes en ordre preferit
        for (int col : ordreNodes()) {
            if (t.movpossible(col)) {
                Tauler nouTauler = new Tauler(t);
                nouTauler.afegeix(col, colorActual);

                int valor = minMax(nouTauler, profunditat - 1, !maximitza, alpha, beta);

                if (maximitza) {
                    millorValor = Math.max(millorValor, valor);
                    alpha = Math.max(alpha, millorValor);
                } else {
                    millorValor = Math.min(millorValor, valor);
                    beta = Math.min(beta, millorValor);
                }

                if (beta <= alpha) break; // Poda alpha-beta
            }
        }

        // Guardem el valor calculat per aquest estat
        taulaEstats.put(estat, millorValor);
        return millorValor;
    }

    /**
     * Calcula una heurística del tauler actual per al jugador.
     * 
     * @param t Estat actual del tauler.
     * @return Valor heurístic del tauler.
     */
    private int calculaHeuristica(Tauler t) {
        int score = 0;

        // Avaluem les línies per al jugador i restem les del rival
        score += analitzaLinies(t, colorJugador);
        score -= analitzaLinies(t, -colorJugador);

        return score;
    }

    /**
     * Analitza i retorna el valor heurístic de les línies per a un color concret.
     * 
     * @param t Estat actual del tauler.
     * @param color Color a analitzar (1 o -1).
     * @return Valor heurístic de les línies del color analitzat.
     */
    private int analitzaLinies(Tauler t, int color) {
        int score = 0;

        // Avaluem les quatre direccions possibles
        score += analitzaDireccio(t, color, 1, 0); // Horitzontals
        score += analitzaDireccio(t, color, 0, 1); // Vertical
        score += analitzaDireccio(t, color, 1, 1); // Diagonal /
        score += analitzaDireccio(t, color, 1, -1); // Diagonal \

        return score;
    }

    /**
     * Avaluació heurística d'una direcció concreta al tauler.
     * 
     * @param t Estat actual del tauler.
     * @param color Color a analitzar (1 o -1).
     * @param dx Increment horitzontal de la direcció.
     * @param dy Increment vertical de la direcció.
     * @return Valor heurístic de les línies en aquesta direcció.
     */
    private int analitzaDireccio(Tauler t, int color, int dx, int dy) {
        int score = 0;

        for (int fila = 0; fila < midaTauler; fila++) {
            for (int col = 0; col < midaTauler; col++) {
                int count = 0;
                int buits = 0;

                for (int k = 0; k < 4; k++) {
                    int x = fila + k * dx;
                    int y = col + k * dy;

                    if (x >= 0 && x < midaTauler && y >= 0 && y < midaTauler) {
                        int valor = t.getColor(x, y);
                        if (valor == color) count++;
                        else if (valor == 0) buits++;
                        else break;
                    }
                }

                if (count == 4) score += MAX_SCORE;
                else if (count == 3 && buits == 1) score += 50;
                else if (count == 2 && buits == 2) score += 10;
            }
        }
        return score;
    }

    /**
     * Genera una clau única per representar l'estat del tauler i la profunditat.
     * 
     * @param t Estat actual del tauler.
     * @param profunditat Profunditat restant de la cerca.
     * @return Clau única per a aquest estat.
     */
    private String generaClauEstat(Tauler t, int profunditat) {
        return t.toString() + ":" + profunditat;
    }

    /**
     * Defineix l'ordre de prioritat per explorar les columnes
     * 
     * @return Array d'índexs de columnes ordenades per prioritat 
     */
    private int[] ordreNodes() {
        return new int[]{3, 4, 2, 5, 1, 6, 0};
    }
}