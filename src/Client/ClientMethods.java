package Client;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
public class ClientMethods {
    // -- Client : Socketchannel , dove si collegano : IP del server percio Inet
    // qui definiamo tutti i metodi
    // CONNECTION
    void Connessione (int portaclient, String serverip, int serverport){
        // per la connessione IP address e port
        //passiamo dal file client_config serverop, serverport
//         InetSocketAddress CompleteAddress = new InetSocketAddress(IPserver,port);
        InetSocketAddress CompleteAddress = new InetSocketAddress(serverip,serverport);
        //------------------------------------------
        //     CREAZIONE DEL CHANNEL
        try(SocketChannel ChannelClient  =  SocketChannel.open();){ //
            // devo collegare il client al serverchannel
            // Errore : qui si chiude subito il canale con try with resources pero vogliamo che rimane aperto
            ChannelClient.connect(CompleteAddress);
            //------------------------------------------
            // si e' collegato all'indirizzo del server il canale del client
            System.out.println("------Il client e' connesso-------");
            // mandiamo subito il portaUDP al server con class portaUDP
            PortaUDP portamsg= new PortaUDP(portaclient);
            //------------------------------------------
            RICHIESTAMES(jtos(portamsg),ChannelClient);
            // si usa il metodo RICHIESTAMES : per inviare la richiesta al server
            while (true){
                Homepage(ChannelClient);
                menu(ChannelClient);
            }

        }catch ( IOException e ){
            System.out.println("Errore in canale");
        }
    }

    void Homepage (SocketChannel ChannelClinet){
        while(true){
            System.out.println("---------------Welcome------------------");
            System.out.println(" 1.Login");
            System.out.println("2. Register");
            try {
                Scanner in = new Scanner(System.in);
                int choose = in.nextInt();
                switch (choose) {
                    case 1: {
//                String reslog = login();// questo mi restituisce la stringa da inviare
                        RICHIESTAMES(login(), ChannelClinet);
                        // se arriva il messaggio giusto ovvero sei loggato allora menu() altrimenti register
                        // controllare il messaggio d'arrivo
//                "Utente non registrato" se ricevo questo allora mandiamo a fare registrazione
                        String risposta = MSGFSERVER(ChannelClinet);
                        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------");
                        System.out.println(risposta);
                        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------");
                        //se loggato allora avro 16 parole pero questo lo voglio solo in menu quando faccio 1 play
                        // Soluzione non leggere qui leggi nel menu
                        if (risposta.contains("Logged")) {
                            // risposta dovra contenere 16 parole
//                        menu(ChannelClinet);
                            return;
                        }
                        // in menu dobbiamo leggere la risposta e avviar il gioco
                        break;
                    }
                    case 2: {
                        String res = register();
                        RICHIESTAMES(res, ChannelClinet);
                        String msgfserver = MSGFSERVER(ChannelClinet);
                        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------");
                        System.out.println(msgfserver);
                        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------");
                        if (msgfserver.contains("WELCOME")) {
                            System.out.println(msgfserver);
                            // quando arriva il messaggio welcome, mostriamo il menu
                            // ora qui deve fare la richiesta per giocare clicking 1
//                        menu(ChannelClinet);
                            return;// ora non devo chiamare menu perche abbiamo il loop
                            // questo mi devo portare al menu e quando clicco dovrei vedere 16 words
                        }
                        break;
                    }
                    default:
                        break;
                }
            }catch (InputMismatchException e){
                System.out.println("Inserire un intero");
            }
        }
    }
    void menu (SocketChannel ChannelClinet){
        // inseriamo i numeri per diverse richieste
        while(true){
            if (DatagramMes.nuovaPartita) {
                System.out.println("Nuova partita disponibile!");
                DatagramMes.nuovaPartita = false; // ← resetta subito!
            }
            System.out.println("--------------------------:");
            System.out.println(": 1. play                 :");
            System.out.println(": 2. reqgameid            :");
            System.out.println(": 4. requestLeaderboard   :");
            System.out.println(": 3. updatecredentials    :");
            System.out.println(": 6. logout               :");
            System.out.println(": 7. playerstats          :");
            System.out.println("--------------------------:");




            try {
                Scanner in = new Scanner(System.in);
                int scelta = in.nextInt();
                switch (scelta) {
                    case 1: {
                        // qui va implementato tutto quello che si e' fatto in register
                        HashMap<String, String> play = new HashMap<>();
                        play.put("operation", "play");
//                      jtos(play);
                        RICHIESTAMES(jtos(play), ChannelClinet);
                        // una volta ricevuto il messaggio entra a giocare
                        ///  questo TODO , ti fa mettere di nuovo inserisici 4 parole perche tu chiami di nuovo ingame()
                        String mes = MSGFSERVER(ChannelClinet);
                        System.out.println("============================================================Words================================================================================");
                        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------");
                        System.out.println(mes);
                        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------");



                        if (mes.contains("Aspettare la Nuova partita")) {
                            break; // questo esce dal switch e rimane in menu()
                            // il bug la chiamata di msgfserver due volte , si risolve con una variabile
                        }
                        // dobbiamo tirare fuori tutte le parole e mostrare in un rettangolo
                        DatagramMes.nuovaPartita = false;

                        // faccio la stessa cosa appena arriva il messaggio mostro il menu esco solo quando nuovapartita diventa true
                        // e poi mostro ingam
                        ingame(ChannelClinet);
                        // ingame far giocare
                        break;

                    }

                    case 2:{
                        // fatto il metodo che invece di fare hashmap ogni volta
                        HashMap<String,String > reqgameid = new HashMap<>();
                        reqgameid.put("operation","requestGameInfo");
                        String msggameID = jtos(reqgameid);
//                 String msggameID=op2("operation","requestGameInfo"); // questo mi restituisce stringa
//                    System.out.println("inviare il messaggio per il gameid con op2"+msggameID);

                        RICHIESTAMES(msggameID,ChannelClinet);
                        System.out.println("---------------------------------------------------------------------------");
                        System.out.println("GameID:"+MSGFSERVER(ChannelClinet));
                        System.out.println("---------------------------------------------------------------------------");
                        break;
                    }
                    case 3:{
                        RICHIESTAMES(upadate(),ChannelClinet);
                        System.out.println("---------------------------------------------------------------------------");
                        System.out.println(MSGFSERVER(ChannelClinet));
                        System.out.println("---------------------------------------------------------------------------");

                        break;
                    }
                    case 4: {
                        RICHIESTAMES(requestLeaderboard(),ChannelClinet);
                        System.out.println("---------------------------------------------------------------------------");
                        System.out.println(MSGFSERVER(ChannelClinet));
                        System.out.println("---------------------------------------------------------------------------");


                        break;
                    }
                    case 6: {
                        HashMap<String, String> logout = new HashMap<>();
                        logout.put("operation", "logout");
                        RICHIESTAMES(jtos(logout), ChannelClinet);
                        MSGFSERVER(ChannelClinet);
                        System.out.println("-----------LOGGING OUT------------");
                        return; // il connessione gestisce tutto
                        // chiusura del canale viene gia fatto quando esci dal connessione();
                        // percio devo ritornare al main del client

                    }
                    case 7: {
                        HashMap<String, String> playerstat = new HashMap<>();
                        playerstat.put("operation", "requestPlayerStats");
                        RICHIESTAMES(jtos(playerstat), ChannelClinet);
                        // qui abbiamo iinviato la richiesta per stats
                        System.out.println("---------------------------------------------------------------------------");
                        System.out.println(MSGFSERVER(ChannelClinet));
                        System.out.println("---------------------------------------------------------------------------");
                        // riceviamo il messaggio ovvero mi devo mostrare tutto, non facciamo una alla volta ma si mostra
                        //tutto insieme
                        break;
                    }
                }
            }catch ( InputMismatchException e ){
                System.out.println(":-----------------------|");
                System.out.println(":   Inserire un numero  |");
                System.out.println(":-----------------------|");

            }

        }

    }
    void  ingame(SocketChannel ChannelClinet){

        for (int i = 0; i< 10;i++){
            // controlla prima se e' null
            String words4 = submitproposal();
            if(words4==null){
                return; // timer scaduto esci

            }
            RICHIESTAMES(words4,ChannelClinet);
            // ricevere il messaggio e ascolta il messaggio dal server percio chiamo la funzione
            String risposta =  MSGFSERVER(ChannelClinet);
            System.out.println("---------------");
            System.out.println(risposta);
            System.out.println("---------------");

            if (risposta.contains("HAI VINTO") || risposta.contains("HAI PERSO")) {
                System.out.println("                         ---------------                          ");
                System.out.println("                         Partita finita!                          ");
                System.out.println("                         ---------------                          ");


                // ti porta al menu pero dobbiamo tenere lo stato del giocatore se prova a giocare di nuovo la
                // partita bisogna dire aspettare
//                menu(ChannelClinet);
                break;// not break devo proprio uscire e tornare al ciclo menu
//                return;
            }

            // devo ascoltare per 4 parole
        }



    }
    // LOGIN

    String login(){
        // devo mandare il messaggio al server e server deve salvare questo e ricordare
        System.out.println("-------------------");
        Scanner Input = new Scanner(System.in);
        System.out.print("Enter the username:");
        String username = Input.nextLine();
        // devo trasformare in String
        System.out.print("Enter your password:");
        String password = Input.nextLine();
        System.out.println("-------------------");


        Login login = new Login( "login",username,password);
        return  jtos(login); // jtos prende oggetto tipo login
    }
    // questo messaggio viene salvato in String IN e viene fatto tutto trasformazione e inviato al server
    // devo usare il Gson per trasformare dal java oggetto a json
    // si  definisce separatamente il metodo per convertire tutto
    // INVIARE IL MESSAGGIO : PRENDE UNA STRING TRASFORMATO E INVIA IL MESSAGGIO // SCRITTURA
    // IL PROCESSO DI SERIALIZZAZIONE
    public  void RICHIESTAMES ( String stoj, SocketChannel channel){ // stoj == stringtojson
        ByteBuffer buffer = ByteBuffer.allocate(1024); // per ora mettiamo cosi la capacita
        // trasformare string in byte
        byte [] stob = stoj.getBytes(StandardCharsets.UTF_8);
        // mettere dentro buffer
        buffer.put(stob);
        // devo fare il flip per scrivere perche dobbiamo portare il cursor all'inizio
        buffer.flip();
        // spediamo al canale
        try {
            channel.write(buffer);

        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    // Il metodo per ricevere il messaggio RISPOSTA DEL SERVER
    public String MSGFSERVER ( SocketChannel clientchannel  ){
        // da fare lettura del msg : fare in modo generale e viene usato questo per ogni messaggio ricevuto
        // preparazione
        String risposta = "";
        ByteBuffer buffer = ByteBuffer.allocate(4028);
        try {
            int byteLetti = 0;
            // aspetta finché arriva qualcosa
            while (byteLetti == 0) {
                byteLetti = clientchannel.read(buffer);
            }
//           clientchannel.read(buffer);
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            risposta = new String(bytes, StandardCharsets.UTF_8);
//            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------");
//            System.out.println( risposta);
//            System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------");



        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return risposta;
    }

    // IL METODO PER INVIARE PROPSOTE
    String submitproposal (){
        Scanner in = new Scanner(System.in);
        List<String> parola = new ArrayList<>();
        System.out.println(" Inserire  le 4 parole alla volta");
        for (int i = 1; i < 5 ; i++) {
            // facciamo controllo solo in menu
            if(DatagramMes.nuovaPartita){
                // arrivato la notifica percio esco da qui
                System.out.println("---------------------------");
                System.out.println("Nuova partita disponibile! ");
                System.out.println("---------------------------");
                DatagramMes.nuovaPartita=false;
                return  null;
            }
            System.out.print( i + " "+"Word:");
            parola.add(in.nextLine().toUpperCase());
        }
// trasformiamo in object
        submitProposal submitProposal = new submitProposal(parola);
        return jtos(submitProposal);
        // serializzazione java object to jtos
    }

    String register(){
        // lo stesso // devo mandare il messaggio al server e server deve salvare questo e ricordare
        System.out.print("Enter the username:");
        Scanner INput = new Scanner(System.in);
        // devo trasformare in String
        String username    = INput.nextLine();
        System.out.print("Enter your password:");

        String password = INput.nextLine();
        register register = new register( "REGISTER",username,password);
        // fatto dopo: creiamo subito la string .... NOOOO!!! se non devo creare per ogni richiesta
        // percio va bene una funzione separato per trasformare oggetto
        return  jtos(register);
        // troppo forte : questo regtojson mi restituisce il stringtojson
    }

    String upadate(){
        System.out.print("Enter newname:  ");
        Scanner input = new Scanner(System.in);
        String newname = input.nextLine();
        System.out.print("Enter newpassword:  ");
        String newpassword = input.nextLine();
        updatecred updatecred = new updatecred("updateCredentials", newname,newpassword);
        return jtos(updatecred);
    }
    // TRASFORMA OGGETTO AL TOSTRINGJSON
    String jtos (Object o){ // idea applicare jtos all'oggetto e questo restituisce string jtos
        Gson gson = new Gson();
        return gson.toJson(o); // tos = to string
    }



    // TODO: il metodo per inviare le rihcieste in forma {operation:""""} .. fatto
//    String op2( String operation, String second){
//        HashMap<String,String> op = new HashMap<>();
//        op.put("operation",second);
//       return jtos(op);
//    }

    // facciamo la chiamata sopra nel switch
    String requestLeaderboard (){
        System.out.println(":-----------------------|");
        System.out.println(":1.Tutta la classifica  |");
        System.out.println(":2.playername           |");
        System.out.println(":3.top k player         |");
        System.out.println(":-----------------------|");

        Scanner input = new Scanner(System.in);
        switch (input.nextInt()){
            case 1 : {
                requestLeaderboard classifca = new requestLeaderboard();
                // questo mi invia operation e restituisce tutta la classifica
                return jtos(classifca);
            }
            case 2:{
                Scanner playname = new Scanner(System.in);
                System.out.println(":----------------------:");
                System.out.print(":PlayerName:");
                String playername = playname.nextLine();
                System.out.println(":----------------------:");
//                Scanner playname = new Scanner(System.in);
                requestLeaderboard player = new requestLeaderboard(playername);
                return jtos(player);
            }
            case 3: {
                Scanner num = new Scanner(System.in);
                System.out.println(":-----------------------:");
                System.out.print(":Quanti top players?     :");
                int k = num.nextInt();
                System.out.println(":-----------------------:");

                requestLeaderboard topk = new requestLeaderboard(k);
                return jtos(topk);
            }
            default: break;

        }
        requestLeaderboard reqlead = new requestLeaderboard();
        System.out.print("Enter the player name :");
        String playername = input.nextLine();
        System.out.print("Enter numero for topplayer :");
        int topPlayer = input.nextInt() ;
        System.out.println("------------------------------------------------------------------------------------------------");

        return  jtos(reqlead);
    }
} // questo e' la parentesi del CLIENT



class requestLeaderboard {
    String operation = "requestLeaderboard";
    String playerName;
    Integer topPlayers;
    public requestLeaderboard(){}; // mostrare tutti
    public requestLeaderboard(String playerName){
        this.playerName = playerName;
    };// solo il olayer
    public requestLeaderboard( int topPlayers){ // k player della classifica
        this.topPlayers = topPlayers;
    }
}

class updatecred {
    String operation;
    String newname;
    String newpassword;
    public updatecred( String operation, String newname, String newpassword){
        this.operation = operation;
        this.newname = newname;
        this.newpassword = newpassword;
    }

}
// CLASSE CHE GENERA OGGETTO DA INVIARE : POSSO USARE SEMPRE QUESTO : EREDITARE

class LOGREG {
    // abbiamo usato logreg perche login e Client.register avevano lo stesso formato
    String operation;
    String username;
    String password;
    public LOGREG ( String operation,String username,String password){
        this.operation = operation;
        this.username = username;
        this.password = password;
    }
}
class Login extends  LOGREG {
    public Login(String operation, String username, String password) {
        super(operation, username, password);
    }
}
class register extends LOGREG{
    public register (String operation, String username, String password){
        super(operation,username, password);
    }
}

class submitProposal {
    String operation = "submitProposal";
    List<String> words;
    public submitProposal(List<String> words){
        this.words = words;
    }
}

class PortaUDP {
    String operation = "portaUDP";
    int portaC;
    public PortaUDP( int portaC){
        this.portaC = portaC;
    }
}


class ConfigClinet {
    private Properties config = new Properties();
    public ConfigClinet (String filename){
        try{
            config.load (new FileReader(filename));
        } catch (IOException e) {
            System.out.println("Errore in configurazione");
        }
    }
    public String getServerIp() {
        return config.getProperty("server.ip", "localhost").trim();
    }

    public int getServerPort() {
        return Integer.parseInt(config.getProperty("server.port", "9999").trim());
    }
}