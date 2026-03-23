package Server;

import java.util.*;

public class StatoGiocatore {
    String username; // chi e'
    int Error =0;
    int GuessGruppo = 0;
    int punteggio =0;
    boolean win = false;
    boolean loss = false;
    int vita = 4; // puo fare fino a 4 guess
    Playerstats statistiche = new Playerstats();
    Set<String> indovintati = new HashSet<>();
    // correttamente
}
