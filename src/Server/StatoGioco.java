
package Server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class StatoGioco {
    int gameId;
    public volatile ArrayList soluzione;
    public volatile ArrayList<String> parole16;
    // tutti leggono da qui
 // per la lavagna
    ConcurrentHashMap<String, StatoGiocatore> giocatori = new ConcurrentHashMap<>();
    // nome del giocatore e il suo stato
}