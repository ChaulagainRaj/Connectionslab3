package Server;

import java.util.ArrayList;

public class MainServer {  // CONNESSIONE E SELECTOR
     static void main() {
       Configserver config = new Configserver("src/Server/server_config.properties");
         String dataclient = config.getuserfile();
       //------------------------
         // prendiamo il file e mettiamo in datihm
       // prima cerchiamo il db per contenere il dati
       // prende il file datacliente e lo mette in databasereg
       // creare il databasereg e caricare
       DatiHM databasereg = new DatiHM(dataclient);
       databasereg.caricadati();

       //--------------------------------------
       String gamejson = config.getgamefile();
       gamehandler gh = new gamehandler(gamejson);
       gh.opengamefile();
       // prendiamo il file game.json e carichiamo il gioco nuovo
       //----------------------------------------
       StatoGioco statoGioco = new StatoGioco();
       statoGioco.parole16 = gamehandler.nextgame();
       statoGioco.soluzione = (ArrayList) gamehandler.Solution;
       // carichiamo le parole16 da inviare tramite il metodo nextgame() e soluzione


         int gametime = config.getGameDuration();
         String palsta = config.playerstatsfile();
         int serverport = config.getport();
       // ----------------------------------------------
       // il file per il playerstats= palsta
         Fileplayerstats fps = new Fileplayerstats(palsta);
         fps.FOStats();
         ServerMethods metodi = new ServerMethods(databasereg,fps,serverport,statoGioco);
       // durata e' un intero
         Gametimer timer = new Gametimer(gametime, gh, metodi.porteclient, metodi.statoGioco);

       // una partita dura 1 mins

       Thread timerThread = new Thread(timer);
       timerThread.setDaemon(true);
       timerThread.start();
       metodi.SetupInizilae(gamejson);
   }
}

