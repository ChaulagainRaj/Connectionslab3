package Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StatoGiocatore {
    String username; // chi e'
    int Error =0;
    int GuessGruppo = 0;
    int punteggio =0;
    boolean win = false;
    boolean loss = false;
    int vita = 4; // puo fare fino a 4 guess
    Playerstats statistiche = new Playerstats();

    ArrayList<String> Indovinati= new ArrayList<>(); // per tenere il gruppo inviato , pero si mette solo quelli indovinate
    // correttamente
    ArrayList<String> WrongAns = new ArrayList<>();
}
