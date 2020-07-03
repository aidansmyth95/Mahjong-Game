package mahjong_package;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

public class Tiles {

    // number of total Mahjong tiles in this deck = 160
    // (3 x 9 suits) * 4 = 108
    // (3 dragons + 4 winds) * 4 = 28
    // (4 flowers) * 2 = 8
    // (4 seasons) * 4 = 16
    private final int totalTiles = 160;
    
    // ArrayList of all tiles in game (n)
	private ArrayList<Tile> hiddenTiles = new ArrayList<>();
    private ArrayList<Tile> uncoveredTiles = new ArrayList<>();

    public Tiles() {
        newTiles();
        shuffleTiles();
    }

    public void setTotalTiles(int n) {}
    public void setHiddenTiles(ArrayList<Tile> hand) { this.hiddenTiles = hand; }
    public void setUncoveredTiles(ArrayList<Tile> hand) { this.uncoveredTiles = hand; }
    public int getTotalTiles() { return this.totalTiles; }
    public ArrayList<Tile> getHiddenTiles() { return this.hiddenTiles; }
    public ArrayList<Tile> getUncoveredTiles() { return this.uncoveredTiles; }

    // check number of hidden tiles yet to be revealed
    boolean tilesLeft() {
        return !this.hiddenTiles.isEmpty();
    }

    // shuffle tiles
    void shuffleTiles() {
        Collections.shuffle(hiddenTiles);
    }

    // add a tile to uncovered tile
    void addUncoveredTile(Tile t) {
        this.uncoveredTiles.add(t);
    }

    private void newTiles() {
        // clear hidden and uncovered tiles
        hiddenTiles.clear();
        uncoveredTiles.clear();

        // generate all Suits, Honors and Bonus tiles
        // Suits
        int i=0;
        for (int type=0; type<3; type++) {      // 3 types
            for (int rank=0; rank<9; rank++) {  // 9 ranks
                for (int ID=0; ID<4; ID++) {    // 4 duplicates
                    Log.e("DEBUG", "Game debug type " + type);
                    hiddenTiles.add(new Suits(type+1, rank+1, ID));
                    i++;
                }
            }
        }

        // Honors
        // wind
        i=0;
        for (int rank=0; rank<4; rank++) {  // 4 winds
            for (int ID=0; ID<4; ID++) {    // 4 duplicates
                hiddenTiles.add(new Honors(1, rank+1, ID));
                i++;
            }
        }
        // dragons
        for (int rank=0; rank<3; rank++) {  // 3 dragons
            for (int ID=0; ID<4; ID++) {    // 4 duplicates
                hiddenTiles.add(new Honors(2, rank+1, ID));
                i++;
            }
        }

        // Bonus
        // seasons
        i=0;
        for (int rank=0; rank<4; rank++) {  // 4 seasons
            for (int ID=0; ID<4; ID++) {    // 4 duplicates
                hiddenTiles.add(new Bonus(1, rank+1, ID));
                i++;
            }
        }
        // flowers
        for (int rank=0; rank<4; rank++) {  // 4 flowers
            for (int ID=0; ID<2; ID++) {    // 2 duplicates
                hiddenTiles.add(new Bonus(2, rank+1, ID));
                i++;
            }
        }

        if (hiddenTiles.size() != totalTiles || uncoveredTiles.size() != 0) {
            System.err.println("ERROR: Hidden tiles or uncovered tiles size not correct");
            System.err.println("Hidden = " + hiddenTiles.size());
            System.err.println("Uncovered = " + uncoveredTiles.size());
        }

    }

	// reveal tile
	Tile revealTile() {

        Tile t = new Tile();

        if (this.hiddenTiles.isEmpty()) {
            System.out.println("\nNo hidden tiles in deck left to uncover. Please restart the game :)");
            //System.exit(0);
            return t;
        }

        t = this.hiddenTiles.remove(0);

        // check this is actually a legitimate tile
        if (!t.getChildClass().equals("Suits") && !t.getChildClass().equals("Honors") && t.getChildClass().equals("Bonus")) {
            // problem
            System.out.println("Error: This tile does not exist: " + t.getDescriptor());
            //System.exit(0);
            return t;
        }

        // put revealed tile in ArrayList
        uncoveredTiles.add(t);
        return t;
    }
}
