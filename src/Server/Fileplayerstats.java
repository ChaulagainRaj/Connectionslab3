package Server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;

public class Fileplayerstats {
     String filename;
    HashMap<String,Playerstats> statfile = new HashMap<>();
    public Fileplayerstats(String filename){
        this.filename = filename;
    }

    void FOStats(){
        // apriamo il file
      try (FileReader fr = new FileReader(this.filename);){
          Type operation = new TypeToken<HashMap<String,Playerstats>>(){}.getType();
          Gson gson = new Gson();
          this.statfile = gson.fromJson(fr,operation);
          // per la prima volta e' sempre vuoto percio this.DC diventa null e ora creare il DC
          if (this.statfile == null) {
              System.out.println(" Il file c'è ma è vuoto. Inizializzo la mappa.");
              this.statfile = new HashMap<>();
          } else {
              // il file e' vuoto dobbiamo gestire quel caso si crea il database e poi si avvia il server
              System.out.println(" Caricati " + this.statfile.size() + " utenti dal database.");
          }
      }catch ( IOException e){
          System.out.println("Errore nella lettura del file ");
      }
    }
// il metodo per scrivere su file palyerstats
    void savedb (){
        try (
            FileWriter fr= new FileWriter(filename);){
            Gson gson = new Gson();

             gson.toJson(statfile, fr);
             // non ce il bisogno di cambiare
        } catch (Exception e) {
            System.out.println("Errore nella scrittura ");
        }
    }
    // il metodo per prendere la statistica di un giocatore
    public  Playerstats get (String username){
        return statfile.getOrDefault(username, new Playerstats());
    }

    // metodo per aggiornare stats del giocatore dopo aver finito la partita
    public  synchronized void  aggiorna( String username, Playerstats stat){
        statfile.put(username,stat);
        // questo carica su ram dal file
        // mettiamo su hashmap e poi salviamo o scriviamo su file
        savedb();
    }

}
