package mahjong_package;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;

public class PossiblePongsKongs {

    private final int MAX_PONGS = 14;
    private int numPongs;
    private Boolean kong;

    private ArrayList<String> possiblePongs = new ArrayList<>();
    private ArrayList<String> possibleKongs = new ArrayList<>();

    public PossiblePongsKongs() {
        clearKongs();
        clearPongs();
    }

    public void setKong(boolean k) { this.kong = k; }
    public Boolean getKong() { return this.kong; }
    public void setNumPongs(int numPongs) { this.numPongs = numPongs; }
    public int getNumPongs() { return this.numPongs; }
    public void setPossibleKongs(ArrayList<String> val) { this.possibleKongs = val; }
    public ArrayList<String> getPossibleKongs() { return this.possibleKongs; }
    public void setPossiblePongs(ArrayList<String> val) { this.possiblePongs = val; }
    public ArrayList<String> getPossiblePongs() { return this.possiblePongs; }

    @Exclude
    public void setPossibleKong(int idx, String val) { this.possibleKongs.set(idx,val); }
    @Exclude
    public String getPossibleKong(int idx) { return this.possibleKongs.get(idx); }
    @Exclude
    public void setPossiblePong(int idx, String val) { this.possiblePongs.set(idx,val); }
    @Exclude
    public String getPossiblePong(int idx) { return this.possiblePongs.get(idx); }

    public void clearPongs() {
        setNumPongs(0);
        this.possiblePongs.clear();
        for (int i = 0; i< MAX_PONGS; i++) {
            this.possiblePongs.add("NONE");
        }
    }

    public void clearKongs() {
        setKong(false);
        this.possibleKongs.clear();
        for (int i=0; i<3; i++) {
            this.possibleKongs.add("NONE");
        }
    }
}
