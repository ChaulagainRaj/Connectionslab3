
package Server;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Configserver {
    private Properties config = new Properties();

    public Configserver(String filename) {
        // 1. STAMPIAMO IL PERCORSO PRIMA DI FARE QUALSIASI COSA
        System.out.println("🔍 Sto cercando il file di configurazione ESATTAMENTE in:");
        System.out.println("👉 " + new File(filename).getAbsolutePath());

        // 2. Apriamo il file in modo sicuro (con try-with-resources così si chiude da solo)
        try (FileReader fr = new FileReader(filename)) {
            config.load(fr);
            System.out.println("✅ File di configurazione trovato e caricato!");
        } catch (IOException e) {
            System.out.println("⚠️ Attenzione: File non trovato. Uso i valori di default per non crashare.");
        }
    }

    // --- I METODI PROTETTI CON VALORI DI DEFAULT ---

    public int getport() {
        return Integer.parseInt(config.getProperty("server.port", "9999").trim());
    }

    public String getgamefile() {
        // Aggiunto il default "game.json"
        return config.getProperty("game.file", "/src/Server/game.json").trim();
    }

    public String getuserfile() {
        // Perfetto, avevi già messo "DataClient"
        return config.getProperty("users.file", "DataClient").trim();
    }

    public String playerstatsfile() {
            return config.getProperty("stats.file", "src/Server/playerstats.json").trim();

    }

    public int getGameDuration() {
        return Integer.parseInt(config.getProperty("game.duration", "120").trim());
    }
}
