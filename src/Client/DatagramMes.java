package Client;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public class DatagramMes implements Runnable {
        // questo per il cliente e questo task di ricevere i datagrammi viene gestito da un thread separato
    public static volatile boolean nuovaPartita = false;
    private DatagramSocket socket;
        int length;
        byte [] buff;
        private boolean running = true;
        private  int port;
        // per ricevere il datagramma dal server
        public DatagramMes(){
           try {
               this.socket = new DatagramSocket();
               this.port = socket.getLocalPort();
               // questo va cambiato per il client
               System.out.println("Ascolta per la notifica sulla porta:"+port);

           }catch (IOException e){
               System.out.println(e.getMessage());
           }
           // porta per far sapere il main thread

        }
        // il metodo per restituire la port per inviare al server e viene salvato dal server per notificare
        public  int GetPortAscolto() {
            return this.port;
        }
        public void run(){
            this.length = 1024;
            // dimensione della notifica piccolo
            while(running) {
                // preparazione per ricevere e trasformare in datagramma decapsulare
                this.buff = new byte[length];
                // la risposta del sever
                DatagramPacket response = new DatagramPacket(this.buff, this.length);
                // per capire da dove arrivato il datagramma
                // creato il datagramma con i dati ricevuti dal server
                try {
                    socket.receive(response);
                    // ricevere il response e poi trasformare in stringa dal datagram response
                    String resmes = new String(response.getData(), 0 , response.getLength(), StandardCharsets.UTF_8);
                    System.out.println("La Notifica dal server: "+ resmes);
                    if(resmes.contains("Nuova partita!")){
                        nuovaPartita = true;
                    }
                } catch (IOException e) {
                    System.out.println("Errore in ricevere datagram "+e.getMessage());
                }
            }
        }

    }




