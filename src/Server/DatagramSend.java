package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

// viene inviato dal server
public class DatagramSend   {
    // creare il datagramma cosa ti serve?
   private DatagramPacket request;
   private DatagramSocket socket;
   private InetAddress addressC;
   String message;
   byte [] buffer;
   int port;
   private int length ;
   public  DatagramSend(InetAddress addressC, String message,int port ){
       this.addressC = addressC; // indirizzo del destinatario client
       this.message = message;
       this.port = port;

       // creato il socket per inviare il datagramma ; si usa questo per inviare
    try{
        this.socket = new DatagramSocket();
    }catch ( IOException e){
        System.out.println("Errore in socket"+ e.getMessage());
    }

   }
// ora abbiamo il socket in cui vogliamo inviare il datagramma e anche la port , ora creiamo datagramma
    // per ora lasciamo la parte per scegliere il datagramma

   public void Send(){
       // dovrei avere delle condizioni per creare il datagramma pero faro in un altro class
       this.buffer = this.message.getBytes(StandardCharsets.UTF_8);
       // abbiamo riempito il buffer con il message
       this.request = new DatagramPacket(buffer,buffer.length,addressC,port);
       // creato il datapacket per inviare all'indirizio client e port

     try{
         this.socket.send(this.request);
//         this.socket.close();
     }catch ( IOException e){
         System.out.println("Errore in sending"+ e.getMessage());
     }

    }
}
