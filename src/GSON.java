import com.google.gson.*;

public class GSON {
   public static void main(String[] args) {
      String jsonString =
               "{\"name\":\"Mario Rossi\"," +
                       " \"age\":21," +
                       "\"verified\":false," +
                       "\"marks\":[30,28,25]}";
      // obiettivo e' stampare i nomi , age , verified and marks
       JsonElement rootnode = JsonParser.parseString(jsonString);
       // immagino che questo mi dia al rootnode  qualcosa la radice JSONELEMENT
       JsonObject details = rootnode.getAsJsonObject();
       // dal namenode prendo il nome dall'oggetto
       JsonElement namenode = details.get("name");
       // si prende il name e si stampa
       System.out.println("Name:"+ namenode.getAsString());
       JsonElement agenode = details.get("age");
       System.out.println("Age:"+ agenode.getAsInt());
       JsonElement verifiednode = details.get("verified");
       System.out.println("Verified:"+ (verifiednode.getAsBoolean() ?
               "Yes":"No")) ;



       JsonArray marksnode = details.getAsJsonArray("marks");
       // ora per stampare fai un ciclo
       for ( int i = 0; i< marksnode.size();i++){
           JsonPrimitive value = marksnode.get(i).getAsJsonPrimitive();
           System.out.println("Value:"+ value.getAsInt());
       }
    }
}
