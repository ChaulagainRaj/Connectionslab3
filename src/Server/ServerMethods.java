package Server;

import Client.DatagramMes;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMethods {
    DatiHM db;
    ConcurrentHashMap<SocketChannel, String> LoggedClinet = new ConcurrentHashMap<>();
    StatoGioco statoGioco = new StatoGioco();
    StatoGiocatore statogiocatore = new StatoGiocatore();
    // ora ti serve lo stato giocatore
    ConcurrentHashMap<SocketChannel, Integer> porteclient = new ConcurrentHashMap<>();
    Fileplayerstats fps; // player stats
    Playerstats ps;
    int port;
    public ServerMethods(DatiHM db, Fileplayerstats fps ,int port,StatoGioco statoGioco) {
        this.db = db;
        this.fps = fps;
        this.port = port;
        this.statoGioco = statoGioco;
    }


    public void SetupInizilae(String filename) { // non abbiamo usato il file
        System.out.println("--- Partita iniziale caricata! ---");
        // chiamo SetupIniziale perche facciamo tutto quello che server per iniziare
        // selector, serverchannel, accept , multiplexing etc : poi vedremo come assegnare il thread al cliente dal
        // threadpool
        // in questo metodo accettiamo tutti i clienti che vogliono connettere
        // il canale per connettere al server
        // scriviamo le partita sulla lavagna
        try {
            ServerSocketChannel serverchannel = ServerSocketChannel.open();
            Selector selector = Selector.open();
            // si apre anche il selector.
            InetSocketAddress serverIP = new InetSocketAddress(this.port);
            // accettiamo tutti i clienti che vengono attraverso questo canale
//           SocketChannel client = serverchannel.accept(); questo era il vecchio java IO
            serverchannel.configureBlocking(false); // java NIO non blocking
            // ora dobbiamo dire tutti i client che usano il canale serverchannel si manda qui
            serverchannel.bind(serverIP);
            serverchannel.register(selector, SelectionKey.OP_ACCEPT);
            // ora il selector ha solo il key che accetta i clienti che arrivano dal channel: serverchannel

            // creiamo il threadpool quando volta accettato passiamo il cliente a uno dei thread disponibili
            ExecutorService threadpool = Executors.newFixedThreadPool(20);
            boolean listen = true;
            System.out.println("LISTENING");
            while (listen) {
                // si ascolta tutti i clienti che arrivano pero dobbiamo tenere se vengono allora a cosa fare ?
                // percio si registra ogni volta che entrano
                // dobbiamo bloccare
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                // vogliamo una set  (insiemw) perche non vogliamo keys duplicati
                Iterator<SelectionKey> keysitertor = selectedKeys.iterator();
                // iterabile a questo key percio il keyitertor: iterare su selectedKeys
                while (keysitertor.hasNext()) { // se il keyiteratore ha le chiavi e ora dobbiamo fare la scelta
                    SelectionKey key = keysitertor.next();
                    keysitertor.remove();// lo togli subito perche il thread principale selector ha preso e ora registra
                    if (key.isAcceptable()) {
                        // qualcosa accettare : il codice che accetta il client
                        try {
                            ServerSocketChannel server = (ServerSocketChannel) key.channel();
                            SocketChannel client = server.accept();
                            System.out.println("il cliente e' connesso" + client.getRemoteAddress());
                            //TODO: IMPORTANTE , APPENA ARRIVA IL CLIENTE SI TIENE IN UNA LISTA ; IL METODO;LISCLIENTE
                            // si accetta il client al server pero si controlla prima la chiave
                            client.configureBlocking(false);
                            // REGISTRAZIONE del cliente
                            client.register(selector, SelectionKey.OP_READ);
                            //TODO : lettura e scrittura: chiamare una funzione che scambia messaggi con ByteBuffer
                            // da ricordare si usa lo stesso canale per leggere e scrivere per il cliente specifico
                        } catch (IOException e) {
                            System.out.println("Errore nella registrazione del canale"+e.getMessage());
                        }

                    } else if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        // Il problema qui : non capisco come registro il canale : non devo registrare !! si
                        // perche quando hai accettato hai gia registrato come read e write si fa solo in caso raro
                        // il server usa read per leggere i dati arrivati dal cliente usando il canale "client"
                        String s = MsgRecC(client);
                        // (Se il pacco è vuoto, ignoriamo)
                        if (s.isEmpty() || s.equals("niente")) continue;

                        threadpool.submit(() -> {
                            // runnable con lambda espressione
                            String nomeThread = Thread.currentThread().getName();
                            System.out.println("--- Task preso in carico dal thread: " + nomeThread + " ---");
                            MODTREE(s, client);

                        });
                        // qui viene assegnato un thread dal threadpool, qualsiasi libero
                        // questo ci fa ricevere i messaggi dal client
                        // fa: conversione usa model tree , tojson e scrive anche su databaseclient DBC info di clienti

                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Errore in  apertura del server channel");;
        }
    }

// potevo fare una class che gestisce il modtree 
    public void MODTREE(String s, SocketChannel clientchannel) {
       // questo metodo prende stringa e si prende elementi
        // e scrive in databasehash
        System.out.println(" sono entrato in modtree");
        // il metodo usato per deserializzare mode tree
        JsonElement nameroot = JsonParser.parseString(s);
        JsonObject details = nameroot.getAsJsonObject();
        JsonElement typetoken = details.get("operation");
        String typet = typetoken.getAsString();
        System.out.println("Il type:" + typet);
        switch (typet) {
            case "REGISTER": {
                JsonElement usernameroot = details.get("username");
                String uss = usernameroot.getAsString();
                JsonElement password = details.get("password");
                String pw = password.getAsString();
                // TODO puoi fare con il metodo gestisceRegister
                if (this.db == null) {
                    System.out.println("hashmap e' vuoto ");
                } else if (!this.db.VerificaReg(uss)) {
                    db.RegisterUtente(uss, pw);
                    // anche quelli registrati lo metto in clientloggati
                    LoggedClinet.put(clientchannel, uss);
                    // prima volta entrato nel gioco percio creo un nuovo stato  per il giocatore
                    System.out.println(" Tutti i cliente loggati:");
                    ArrayList<String> loggedusrname = new ArrayList<>();
                    for (String e : LoggedClinet.values()) {
                        loggedusrname.add(e);
                        System.out.println(" name:" + e);
                    }
                    StatoGiocatore stato = new StatoGiocatore();
                    stato.username = uss;
                    statoGioco.giocatori.put(uss, stato);
                    // nel hashmap giocatore metto suo stato e uss
//                    System.out.println("DB dopo registrazione: " + this.db.DC);
                    System.out.println("Scrivere su file con il metodo WriteDBreg()");
                    db.WriteDBCreg();
                    StatoGiocatore statogiocatore = new StatoGiocatore();
                    statogiocatore.username = uss; // prendiamo il username per avere il suo statistiche
                    statogiocatore.statistiche = fps.get(uss); //  carica stats dal file
                    statoGioco.giocatori.put(uss, statogiocatore);
                    // mettiamo lo stato del giocatore dal file e lo stesso quando si fa login
                    // facciamo capire che entrato nel gioco
                    MsgsendClient(clientchannel, "----------WELCOME-----------");
                    // una volta registrato faccio giocare
                    //-----------------------------------
                }
                // questo va inviato al client dal server con UDP
                // abbiamo gia la classe per inviare  i datagrammi udp, problema dobbiamo sapere il canale
                else {
                    System.out.println("Gia registrato utente "+uss);
                    MsgsendClient(clientchannel, "Utente gia registrato");
                }
                break;
            }
            case "login": {
                // se login allora ho gia preso operation ora devo verificare se esiste questo cliente
                JsonElement usernameroot = details.get("username");
                String uss = usernameroot.getAsString();
                JsonElement password = details.get("password");
                String pw = password.getAsString();
                // verifichiamo username e password
                if (this.db.VerificaReg(uss) && this.db.DC.get(uss).equals(pw)) {
                    // teniamo la traccia in LoggedClient
                    LoggedClinet.put(clientchannel, uss);
                    // per ricordare i giocatori li mettiamo nello hashmap dello stato gioco ovvero quello che
                    // stanno partecipando al gioco
                    statogiocatore.statistiche = fps.get(uss);
                    statoGioco.giocatori.putIfAbsent(uss, new StatoGiocatore());
                    statoGioco.giocatori.get(uss).username = uss;
                    if (!statoGioco.giocatori.containsKey(uss)) {
                        StatoGiocatore nuovoStato = new StatoGiocatore();
                        nuovoStato.username = uss;
                        nuovoStato.statistiche = fps.get(uss);
                        statoGioco.giocatori.put(uss, nuovoStato);
                    }
                    MsgsendClient(clientchannel, "---------------Logged--------------");
                    //-----------------------------------
                    // una volta entrato faccio giocare
//                    play(clientchannel);
                    // devi solo mostrare menu
                } else {
                    MsgsendClient(clientchannel, "Utente non registrato");
                    System.out.println("Utente non registrato!! "+uss);
                }
                break;
            }
            case "submitProposal": {
                // gestisci submit();
                // in altri caso prendo il user dal
                String username = LoggedClinet.get(clientchannel);
                StatoGiocatore statogiociatore = statoGioco.giocatori.get(username);
                // prendo il suo nome dal statoGioco.giocatori
                if (statogiociatore == null) {
                    MsgsendClient(clientchannel, "Errore: fai prima login");
                    break;
                }
                gamehandler.gestiscisubmit(clientchannel, s, fps, statogiociatore);
                // prendo come parametro fps, statogiocatore per aggiornare statistiche del giocatore
                // si prende username dal statogiocatore e si aggiorna fps su file
                // una volta sono entrati e per vedere chi e' il cliente , lo prendo il suo username dal loggedclient
                break;
            }
            case "portaUDP": {
                // dobbiamo salvare queste porte su un array e poi usare
                int porta = details.get("portaC").getAsInt();
                // prendiamo la porta e poi mettiamo in portecliente
                porteclient.put(clientchannel, porta);
                break;

            }
            case "play": {
                String username = LoggedClinet.get(clientchannel);
                // prendo il username dal client loggati
                StatoGiocatore statogiocatore = statoGioco.giocatori.get(username);
                System.out.println("Giocatore:" + username);
                if ((statogiocatore != null) && (statogiocatore.loss || statogiocatore.win)) {
                    if (!DatagramMes.nuovaPartita) {
                        MsgsendClient(clientchannel, "Aspettare la Nuova partita");
                        // esci non inviare le parole
                        return;
                    }
                }
                // se sei qui vuol dire fai giocare ancora la vecchia partita
                play(clientchannel);
                break;
            }
            case "logout": {
//                String username = LoggedClinet.get(clientchannel);
                LoggedClinet.remove(clientchannel);
//                porteclient.remove(clientchannel);
                // per ora puoi togliere questo perche si usa sempre lo stesso canale e poi lo stesso porta perche e'
                //lo stesso porta/processo
//                statoGioco.giocatori.remove(username);
                MsgsendClient(clientchannel, "Logout effettuato!");
                break;
            }
            case "requestPlayerStats": {
                String username = LoggedClinet.get(clientchannel);
//                managestat(); questo funzione deve gestire tutto fino a inviare i messaggi
                //il managestat() mi deve restituire il jsonstring ovvero soltanto string
                StatoGiocatore statogiocatore = statoGioco.giocatori.get(username);
                // prendiamo il giocatore dello gicoco corrente e dal giocatori che e'concorrente hashmap
                Playerstats ps = statogiocatore.statistiche;
                // prendiamo playerstats e username dal statogiocatore e passiamo al managestat

                String msg = managestat(username, ps);
                // managestat(username, ps) mi restituisce statistiche del giocatore
                MsgsendClient(clientchannel, msg);
                // prendiamo il cliente dal LoggedClient
                // creare hashmap cosi va a scrivere su file hashmap abbiamo ps

                // void scrittura su file() , facciamo in class Fileplayerstats
                break;
            }
            case "requestGameInfo": {
                String username = LoggedClinet.get(clientchannel);
                System.out.println("La richiesta fatto dal giocatore: "+ username);
                int gameid = statoGioco.gameId;
                MsgsendClient(clientchannel, String.valueOf(gameid));
                break;
            }
            case "updateCredentials": {
                JsonElement usernameroot = details.get("newname");
                String uss = usernameroot.getAsString();
                System.out.println("Name:" + uss);
                JsonElement password = details.get("newpassword");
                String pw = password.getAsString();
                // prendiamo il oldpw e ora chiamare il metodo che aggiorna credenziali
                manageupdate(uss, pw, clientchannel);
                // ora controlliamo controllo se ha modificato veramente
                if (this.db.VerificaReg(uss) && this.db.DC.get(uss).equals(pw)) {
                    // aggiorniamo prima sul loggedcliente il nuovo uss, username
                    LoggedClinet.put(clientchannel, uss);
                    MsgsendClient(clientchannel, "Aggiornamento Fatto!!");
                    // inviamo il messaggio al cliente e scriviamo anche su file dataclient
                    db.WriteDBCreg();
                } else {
                    MsgsendClient(clientchannel, "Errore in aggiornamento");
                    return;// esco senza scrivere su database
                }
                break;
            }
            case "requestLeaderboard": {
                // bisogna filtrare il operation perche ci sono 3
                System.out.println("Giocatori connessi: " + statoGioco.giocatori.size());
                Set<Map.Entry<String, StatoGiocatore>> coppie = statoGioco.giocatori.entrySet();
                // prendiamo la coppia per esempio {"rams" : statogiocatore}
                // mettiamo in una lista ordinabile
                ArrayList<Map.Entry<String, StatoGiocatore>> classfica = new ArrayList<>(coppie);
                for (Map.Entry<String,StatoGiocatore> player : coppie){
                    System.out.println("Name:"+player.getValue().username);
                }


                // le coppie in array ordinabile
                // getvalue() == statogiocatore
                classfica.sort((a, b) -> b.getValue().statistiche.PunteggioTotale - a.getValue().statistiche.PunteggioTotale);
                //abbiamo ordinato la classifica in modo decrescente a-b con il sort
                StringBuilder res = new StringBuilder();
                // facciamo i 3 casi per 3 tipi di richieste
                if (details.has("topPlayers")) {
                    // k top players
                    int k = details.get("topPlayers").getAsInt(); // ← fix!
                    for (int i = 0; i < Math.min(k, classfica.size()); i++) {
                        // prendiamo tutti entry fino a k e inviamo al cliente
                        Map.Entry<String, StatoGiocatore> entry = classfica.get(i);
                        res.append("Posizione:"+(i + 1) + " | "
                                +"Player:" + entry.getKey() +
                                " | " + " Punti:" + entry.getValue().statistiche.PunteggioTotale + "\n");
                    }
                } else if (details.has("playerName")) {
                    String targetplayer = details.get("playerName").getAsString();
                    // controlliamo se esiste il player
                    boolean trovato = false;
                    for (int i = 0; i < classfica.size(); i++) {
                        Map.Entry<String, StatoGiocatore> entry = classfica.get(i);
                        if (entry.getKey().equals(targetplayer)) {
                            res.append("Name:" + targetplayer+ " | " +
                                    "Posizione:" + (i + 1)+
                                    " | " + "Punteggio:" + classfica.get(i).getValue().statistiche.PunteggioTotale+"\n");
                            // se qui significa trovato
                            trovato = true;
                            break;
                        }
                    }
                    if(!trovato){
                        MsgsendClient(clientchannel, "Giocatore non logged");
                        return;
                    }
                } else {
                    // Tutta la classifica
                    for (int i = 0; i < classfica.size(); i++) {
                        Map.Entry<String, StatoGiocatore> entry = classfica.get(i);
                        res.append("Posizione:"+(i + 1) + " | "
                                + "Playername:" + entry.getKey() + " | " +
                                "Punti:"+entry.getValue().statistiche.PunteggioTotale + "\n");
                    }
                }
                MsgsendClient(clientchannel, res.toString());
            }
        }
    }

    // questo metodo riceve il messaggio dal cliente e restituisce IL STRING btos from byte to string
    public static String MsgRecC(SocketChannel clientchannel) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            int byteletti = clientchannel.read(buffer);
            if (byteletti == -1) {
                try {
                    clientchannel.close();
                } catch (IOException el) {
                    System.out.println("Errore nella chiusura del cliente" + el.getMessage());
                }
                //  Non c'è niente da leggere restituiamo stringa vuota
                return "";
                // gestito in modtree
            }
            if (byteletti == 0) {
                return "niente";
                // anche questo gestito in modtree
            }
            System.out.println("il numero di byte letti:" + byteletti);
            buffer.flip();// perche cambiato prima con read(buffer) e portare cursor all'inizio

            byte[] byteverodalbuffer = new byte[buffer.remaining()];
            buffer.get(byteverodalbuffer);
//            buffer.flip();// perche cambiato prima con read(buffer) e portare cursor all'inizio
            String btos = new String(byteverodalbuffer, StandardCharsets.UTF_8);
            System.out.println("il messagge decodificato:" + btos);
            return btos.trim(); // si toglie spazi inutili
        } catch (IOException e) {
            System.out.println("Il cliente e' crashato  o errore di rete, " + e.getMessage());
            try {
                clientchannel.close();
            } catch (IOException el) {
                System.out.println("Errore nella chiusura del cliente" + el.getMessage());
            }
        }
        return "";
        // gestito in modtree
    }


    // il metodo per rispondere al cliente
    public static void MsgsendClient(SocketChannel clientchannel, String msgtoC) {
        try {
            byte[] bytes = msgtoC.getBytes(StandardCharsets.UTF_8);
            // trasformazione in byte
            ByteBuffer buffer = ByteBuffer.allocate(4028);
            buffer.put(bytes); // mettiamo il byte trasformato in buffer
            buffer.flip(); // portiamo il curser all'inizio
            try {
                clientchannel.write(buffer);
            } catch (IOException e) {
                System.out.println("Errore nella scrittura in buffer per inviare il messaggio");
            }
        } catch (Exception e) {
            System.out.println("Errore nel buffer Bytebuffer");;
        }
    }


    // il play metodo ti fa giocare la partita al cliente perche fatto la richiesta dal client
    void play(SocketChannel clientchannel) {
        String username = LoggedClinet.get(clientchannel);
        // apro per la prima volta
        // LA NOSTRA LAVAGNA CONDIVISA

        // reset stato giocatore per nuova partita
        StatoGiocatore nuovoStato = new StatoGiocatore();
        nuovoStato.username = username;
        nuovoStato.statistiche = fps.get(username); // carica dal file!
        statoGioco.giocatori.put(username, nuovoStato);
        // il volatile
        ArrayList<String> parolecorrente = new ArrayList<>(statoGioco.parole16);
        Collections.shuffle(parolecorrente);
        // prendiamo il parolecorrente del giococorrente e inviamo
        System.out.println("Parolecorrente caricate: " + parolecorrente.size());
        Gson gson = new Gson();
        String risposta = gson.toJson(parolecorrente);
        // inviare 16 parole , preso dal gamefile(); dal classe gamehandler
        MsgsendClient(clientchannel, risposta);
        System.out.println("Inviato il 16 parole:");
    }

    String managestat(String username, Playerstats ps) {
        // dobbiamo prendere dal hashmap in cui abbiamo salvato la informazione di giocatore
        // facciamo prendere username e poi restituire il stats dal hashmap che abbiamo
        // prima aggiorniamo e poi scriviamo
        fps.aggiorna(username, ps);
        System.out.println("Aggiornato playerstats e scritto anche su file con il metodo savedb()");
        return statplayer(username,ps);
        // restituiamo statistiche del giocatore
    }

    void manageupdate(String newname, String newpassword, SocketChannel clientchannel) {
        // passo soltanto il nome e password nuovo
        //facciamo restituire il message , abbiamo detto che passiamo solo il newname e new password
        String oldname = LoggedClinet.get(clientchannel);
        // questo mi restituisce il username perche abbiamo logged client in questo momento
        // prendiamo il password dal db del dataclient in cui abbiamo memorizzato username e password
        String oldpw = db.DC.get(oldname);
        // newname e newpassword vengono passati dal case
        db.UpdateCredential(oldname, oldpw, newname, newpassword);
    }

    String statplayer(String username,Playerstats ps) {
        StringBuilder res = new StringBuilder();
        res.append("------ STATISTICHE DI " + username + " -------\n");
        res.append("Puzzles Completati: " + ps.PuzzledCompleted + "\n");
        res.append("Won: " + ps.puzzlewon + "\n");
        res.append("lsot: " + ps.puzzleloss + "\n");
        res.append("WinRate: " + ps.getWinRate() + "%\n");
        res.append("LossRate: " + ps.getLossRate() + "%\n");
        res.append("CurrentStreak: " + ps.currentstreak + "\n");
        res.append("MaxStreak: " + ps.maxstreak + "\n");
        res.append("PerfectPuzzles: " + ps.perfecetpuzzle + "\n");

//        res.append("Errori [0-4]: " + Arrays.toString(ps.mistakehistrogram));
        res.append("-------MISTAKE HISTOGRAM ------\n");
        String[] labels = {"0 errori", "1 errore", "2 errori", "3 errori", "4 errori (perso)"};
        for (int i = 0; i < 5; i++) {
            res.append(labels[i] + ": ");
            for (int j = 0; j < ps.mistakehistrogram[i]; j++) {
                res.append("#");
            }
            res.append(" " + ps.mistakehistrogram[i] + "\n");
        }
        return res.toString();
    }
}


