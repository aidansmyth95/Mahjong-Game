package mahjong_package;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;

public class PossibleChowsPongsKongs {

    private final int MAX_CHOWS = 14;
    private int numChows;
    private Boolean kong;
    private Boolean pong;

    private ArrayList<String> possiblePongs = new ArrayList<>();
    private ArrayList<String> possibleChows = new ArrayList<>();
    private ArrayList<String> possibleKongs = new ArrayList<>();

    public PossibleChowsPongsKongs() {
        clearKongs();
        clearPongs();
        clearChows();
    }

    public void setKong(boolean k) { this.kong = k; }
    public Boolean getKong() { return this.kong; }
    public void setNumChows(int numChows) { this.numChows = numChows; }
    public int getNumChows() { return this.numChows; }
    public void setPong(boolean pong) { this.pong = pong; }
    public Boolean getPong() { return this.pong; }

    public void setPossibleKongs(ArrayList<String> val) { this.possibleKongs = val; }
    public ArrayList<String> getPossibleKongs() { return this.possibleKongs; }
    public void setPossiblePongs(ArrayList<String> val) { this.possiblePongs = val; }
    public ArrayList<String> getPossiblePongs() { return this.possiblePongs; }
    public void setPossibleChows(ArrayList<String> val) { this.possibleChows = val; }
    public ArrayList<String> getPossibleChows() { return this.possibleChows; }

    @Exclude
    public void setPossibleKong(int idx, String val) { this.possibleKongs.set(idx,val); }
    @Exclude
    public String getPossibleKong(int idx) { return this.possibleKongs.get(idx); }
    @Exclude
    public void setPossiblePong(int idx, String val) { this.possiblePongs.set(idx,val); }
    @Exclude
    public String getPossiblePong(int idx) { return this.possiblePongs.get(idx); }
    @Exclude
    public void setPossibleChow(int idx, String val) { this.possibleChows.set(idx,val); }
    @Exclude
    public String getPossibleChow(int idx) { return this.possibleChows.get(idx); }

    public void clearChows() {
        setNumChows(0);
        this.possibleChows.clear();
        for (int i = 0; i< MAX_CHOWS; i++) {
            this.possibleChows.add("NONE");
        }
    }

    public void clearPongs() {
        setPong(false);
        this.possiblePongs.clear();
        for (int i = 0; i<3; i++) {
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
