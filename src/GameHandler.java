import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameHandler {
    static void main() {
        sottogh first = new sottogh();
        first.gamefile();
        for(groups groupword: first.solution){
            System.out.println(" array "+ groupword);
        }
        System.out.println(first.solution);

    }
}

// sto usando per fare il test
class sottogh{
    ArrayList<String> parole = new ArrayList<String>();
    List<groups> solution;

//    ArrayList<String> parole = new ArrayList<>();
    // metodo che restituisce il 16 parole in arraylist
   public void   gamefile () {
        // apriamo il file
        try(JsonReader reader = new JsonReader(new FileReader("/Users/chaulagain/Downloads/idea/src/Server/game.json"));){
            Gson gson = new Gson();
            reader.beginArray();
            while (reader.hasNext()){
                Game g = gson.fromJson(reader,Game.class);
                // otteniamo oggetto
                solution = g.groups;
                // mettiamo il groups
                for( groups gwords : g.groups){
                    parole.addAll(gwords.words);
                }
            }
        }
        catch (IOException e){
            System.out.println("Il file non ho trovato ");

        }
    }
}


class Game {
    int gameId;
    List<groups> groups;
}
class groups{
    String theme;
    List< String> words;
}