package Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Gametimer implements Runnable {
    final long durata;
    gamehandler gh; // non lo so
    StatoGioco statoGioco; // per capire gameid
    ConcurrentHashMap<SocketChannel, Integer> porteclient = new ConcurrentHashMap<>();

    public Gametimer(int durata, gamehandler gh, ConcurrentHashMap<SocketChannel, Integer> porteclient, StatoGioco statoGioco) {
        this.durata = durata;
        this.gh = gh;
        this.porteclient = porteclient;
        this.statoGioco = statoGioco;
    }

    public void run() {
        while(true) {
            try {
                Thread.sleep(durata * 1000);

                ArrayList<String> parola = gamehandler.nextgame();
                statoGioco.soluzione = (ArrayList<groups>) gamehandler.Solution;
                statoGioco.parole16 = parola;
//                public volatile ArrayList<String> parole16;

//                statoGioco.giocatori.clear();
                // quando scade il tempo devo risettare tutti i giocatori che si trovano dentro questo hashmap giocatore
                statoGioco.giocatori.forEach((username, statoGiocatore)->{
                    statoGiocatore.win = false;
                    statoGiocatore.loss= false;
                    statoGiocatore.vita = 4;
                    statoGiocatore.Error = 0;
                    statoGiocatore.punteggio = 0;
                    statoGiocatore.GuessGruppo = 0;
                    statoGiocatore.indovintati.clear();


                });

                // risettiamo tutti i giocatori
                // iniziamo la nuovo partita
                // prendiamo le porte e iniziamo a inviare msg ad ogni cliente percio mi serve inetaddress
                // la macchina e la porta

                for (SocketChannel canale : porteclient.keySet()) {
                    int porta = porteclient.get(canale);        // porta UDP
                    InetAddress addr;
                    try {
                        addr = ((InetSocketAddress) canale.getRemoteAddress()).getAddress(); // IP

                        System.out.println("Timer scaduto! Nuova partita gameId: " + (statoGioco.gameId+1));
                        DatagramSend udp = new DatagramSend(addr, "Nuova partita!", porta);
                        // inviare il messaggio datagram
                        udp.Send();
                        System.out.println("Nuova partita iniziata!");
                    } catch (IOException e) {
//                        porteclient.remove(canale);
                        System.out.println("Errore a inviare il messaggio udp");;
                    }
                }
                statoGioco.gameId+=1;
            } catch (  InterruptedException e) {
                System.out.println("THREDSLEEPING");
            }
        }
    }
}