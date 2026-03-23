package Server;
public class Playerstats {
    String name;// dobbiamo sapere chi e'
    int PuzzledCompleted=0; // questo partite giocate
    double maxstreak = 0;
    double currentstreak=0;
    int perfecetpuzzle=0;
    int puzzlewon=0;
    int puzzleloss=0;
    int PunteggioTotale =0;
    int [] mistakehistrogram = new int[5];


    // creo dei metodi e ogni volta un giocatore gioca o logged in associare con class playerstats.json
    public double getWinRate() {
        // questo puzzle viene aggiornato tramite statogiocatore
        if (this.PuzzledCompleted == 0) return 0;
        return (double) Math.round((double) puzzlewon / PuzzledCompleted * 100);
    }
    public double getLossRate(){
        if(this.PuzzledCompleted == 0) return  0;
        return (double) Math.round((double) puzzleloss / PuzzledCompleted * 100);
    }


}
