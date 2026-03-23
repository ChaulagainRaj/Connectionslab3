package Client;
import java.util.*;

import com.google.gson.*;

public class ClientMain {
    static void main() {
        ConfigClinet configC = new ConfigClinet("/Users/chaulagain/Downloads/idea/.idea/client_config.properties");
        // fornire il path assoluto non va bene solo il nome del file
        DatagramMes taskUDP = new DatagramMes();
        Thread UDPListen = new Thread(taskUDP);
        UDPListen.start();
        int portaUDP = taskUDP.GetPortAscolto();

       System.out.println("Main: Il thread UDP mi ha detto che è sulla porta " + portaUDP);
       // soltanto delle chiamate
       //1: CONNESSIONE
       ClientMethods client = new ClientMethods();
        System.out.println("--------------------------------------");
       System.out.println("---------------HOMEPAGE----------------");
       System.out.println("---------------------------------------");
       client.Connessione(portaUDP,configC.getServerIp(),configC.getServerPort());
       // vanno a prendere dal file.properties
    }
}




