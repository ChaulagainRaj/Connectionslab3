package Server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

public class DatiHM {
    // niente costruttore per ora , soltanto i metodi data: jan 29
    String filename;
    HashMap< String,String> DC;
    public DatiHM(String filename){
        this.filename = filename;
    }

    // IL METODO PER CARICARE IL FILE E TRASFORMARE IN OGGETTO JAVA DA JSON
    void caricadati(){
        Gson gson = new Gson(); // il traduttore
        // apriamo il file per caricare
       try(FileReader fr = new FileReader(this.filename);){
           Type operation = new TypeToken<HashMap<String,String>>(){}.getType();
           // facciamo leggere il file
           this.DC = gson.fromJson(fr,operation);
           // per la prima volta e' sempre vuoto percio this.DC diventa null e ora creare il DC
           if (this.DC == null) {
               System.out.println(" Il file c'è ma è vuoto. Inizializzo la mappa.");
               this.DC = new HashMap<>();
           } else {
               // il file e' vuoto dobbiamo gestire quel caso si crea il database e poi si avvia il server
               System.out.println(" Caricati " + this.DC.size() + " utenti dal databaseclient.");
           }
           // ora scrivere sul hashmap
           // scriviamo : parametri sono fonte e type
       } catch (Exception e) {
           System.out.println("Errore in reader"+ e.getMessage());;
       }

    }

    // METODO PER PROTARE IL DATABASECLIENTE DBC per confrontare per il login
    synchronized void  RegisterUtente( String username, String password){
        DC.put(username,password); // deve essere sincronizzato perche utilizzano una alla volta
        // carica un hashmap dalla DBC al programma
    }
    // METODO : PER IL CONTROLLO DEL KEY
    // PER SOLO CONTROLLARE NON SERVE SINCRONIZZAZIONE
    public boolean VerificaReg (String key){
        if(!DC.containsKey(key)){
            return  false;
        }
        return this.DC.containsKey(key);// questo metodo per controllare login e register
    }

    // aggiornamento: per ora facciamo synchronized questo aggiorna il vecchio password
    synchronized  void UpdateCredential(String oldname  , String oldpassword, String newname, String newpassoword){
        if(!this.DC.containsKey(oldname)){
            System.out.println("Il nome non e' registrato");
//            return "Il nome non e' registrato";
        }
        this.DC.replace(oldname, oldpassword,newpassoword);
        this.DC.remove(oldname);
        this.DC.put(newname,newpassoword);
        System.out.println("Aggiornato credenziali ");
//        return "Aggionrato credenziali";
    }





//     METODO PER SCRIVERE SUL DATABASE : quello che ti confonde non pensare collegare ora : definire i metodi generale per ora
    synchronized void WriteDBCreg (){ // scrivi nel DBC info di clienti
        // feb 3: MA SI SA CHE TUTTI APRONO LO STESSO FILE PER SCRIVERE percio quando si chiama
        // la questa funzione facciamo aprire e scrivere subito al file
        try(FileWriter f = new FileWriter (this.filename);){
            Gson gson = new Gson();
            gson.toJson(this.DC,f);
            // scrviamo quello del DC
        }catch ( IOException e ){
            System.out.println("Errore scrittura su file");
            // writer.flush() e close() sono automatici grazie al try-with-resources
        }
        // apri il file e scrivi in hm FINALE
        // vediamo dopo ; per finire: tutti aprono lo stesso file per quello usiamo synchronized
    }
}



















