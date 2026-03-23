package Server;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sun.nio.sctp.AbstractNotificationHandler;

import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class gamehandler {
    // il gamehandler abbiamo funzioni
    //1 aprire il gamefile con descriptor reader
    // 2 leggere dal reader e restituire 16 words
    // 3 gestisce la proposta arrivata dal cliente
    String filename;
    public gamehandler(String filename){
        this.filename= filename;
    }

//apriamo il file
//static Playerstats playersta = new Playerstats();
   private  static JsonReader reader;
      static List<groups> Solution;
    // parola contiene 16 parole preso dal game
    public void opengamefile(){
        try {
            reader = new JsonReader(new FileReader(this.filename));
            reader.beginArray(); //  ora usa il reader STATICO
        } catch (IOException e) {
            System.out.println("Errore: " + e.getMessage());
        }
    }
    // metodo che restituisce il 16 parole in arraylist
   public static ArrayList<String> nextgame() {
        // apriamo il file
        ArrayList<String> parola = new ArrayList<>();
            Gson gson = new Gson();
            try {
                if (reader.hasNext()) {
                    // se ha da leggere allora prendi e trasforma in java object Game.class
                    Game g = gson.fromJson(reader, Game.class);
                    Solution = g.groups;
                    // otteniamo oggetto
                    for (groups groupwords : g.groups) {
                        parola.addAll(groupwords.words);
                    }
                }
            } catch (IOException e) {
                System.out.println("Game finito"+ e.getMessage());
            }
        return parola;
    }

    public static void  gestiscisubmit( SocketChannel channelclient,String submitString, Fileplayerstats fps, StatoGiocatore statogiocatore){
        // traduciamo il proposta dall cliente
        Gson gson = new Gson();
        submit proposta = gson.fromJson(submitString,submit.class);
        // appena arriva metto in array indovinati
        ArrayList<String> ans = new ArrayList<>(proposta.words);
       statogiocatore.Indovinati.addAll(proposta.words);
        // ora verificare con il hashset
        boolean trovato = false;
        for (groups gruppo : Solution){
            // il condizione per la prima volta
            if(new HashSet<>(gruppo.words).equals(new HashSet<>(proposta.words))){
                // la proposta arrivata va salvato anche in array // facciamo due array
                // 1 per risposte giuste e altre per risposte sbagliate
                //TODO : pero controllo qui se sono gia dentro Indovinati allora invio il messaggio ed esco
                if(statogiocatore.Indovinati.contains(ans)){
                    ServerMethods.MsgsendClient(channelclient,"already guessed");
                    return;
                }
//               String usrmane=  statogiocatore.username; // posso scrivere direttamente
//                System.out.println("correctans"+ statogiocatore.CorrectAns.getFirst());
                statogiocatore.GuessGruppo +=1;
                // per ogni prposta giusto 6 punti
                statogiocatore.punteggio+=6*statogiocatore.GuessGruppo;


                // controlliamo se ha vinto
                if(statogiocatore.GuessGruppo == 3){
                    statogiocatore.statistiche.PunteggioTotale +=statogiocatore.punteggio;
                    statogiocatore.win = true;
                    // mostriamo il punteggio
                    // se ha vinto allora devo aggiornare playerstats
//                    if (statogiocatore.statistiche == null) {
//                        statogiocatore.statistiche = new Playerstats();
//                    }
                    statogiocatore.statistiche.puzzlewon +=1;
                    // statogiocatore.statistiche e' null percio non prende
                    // devo dire guarda se nuovo allora crea lo
                    statogiocatore.statistiche.mistakehistrogram[statogiocatore.Error]++;
                    // qui dobbiamo aggionare tutto per avere statistiche del giocatore
                    // prendo
                    statogiocatore.statistiche.PuzzledCompleted+=1;
                    if(statogiocatore.Error == 0){
                        statogiocatore.statistiche.perfecetpuzzle +=1;
                    }
                    // aggiorna streak
                    statogiocatore.statistiche.currentstreak++;
                    if(statogiocatore.statistiche.currentstreak > statogiocatore.statistiche.maxstreak) {
                        statogiocatore.statistiche.maxstreak = statogiocatore.statistiche.currentstreak;
                    }
                    // prima di inviare il messaggio salviamo stats
                    fps.aggiorna(statogiocatore.username,statogiocatore.statistiche);
                    // la statistica del giocatore
                    ServerMethods.MsgsendClient(channelclient,
                            "HAI VINTO! Punteggio: " + statogiocatore.punteggio); // ← manda al client!
                    return;
                }
                trovato =  true;
                ServerMethods.MsgsendClient(channelclient,"Correct  Answer! THEME"+ gruppo.theme);
                break;
            }
        }
        if (!trovato) {
            statogiocatore.Error++;
            statogiocatore.vita--;
            String message = "SBAGLIATO!!  Rimangono "+ statogiocatore.vita +" tentativi";



            if(statogiocatore.vita ==0){
                statogiocatore.loss = true;
                statogiocatore.statistiche.puzzleloss +=1;
                String message1 = " HAI PERSO!";
                statogiocatore.statistiche.mistakehistrogram[4]++;
                // ogni volta che vedi questo messaggio vuol dire che giocato non mi importa se hai vinto o perso
                statogiocatore.statistiche.PuzzledCompleted +=1;
                statogiocatore.statistiche.currentstreak = 0;
                // aumento il numero di parite giocate e lo stesso quando vinci
                fps.aggiorna(statogiocatore.username,statogiocatore.statistiche);
//                HashMap<String,Playerstats> statfile = new HashMap<>();
                // lo metto su hashmap
                ServerMethods.MsgsendClient(channelclient, message1);
                return; // esci anche da qui 1
            }

            ServerMethods.MsgsendClient(channelclient, message);
        }
    }


}

class submit {
    List<String> words;
}



class Game {
    int gameId;
    List<groups> groups;
}
class groups{
    String theme;
    List< String> words;
}