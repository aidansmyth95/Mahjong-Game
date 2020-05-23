package mahjong_package;

import java.util.ArrayList;
import java.util.Collections;

public class Tiles {

    // number of total Mahjong tiles in this deck = 160
    //private final int total_tiles = 160;
    
    // ArrayList of all tiles in game (n)
	private ArrayList<Tile> hidden_tiles = new ArrayList<>();
    ArrayList<Tile> uncovered_tiles = new ArrayList<>();

    // (3 x 9 suits) * 4 = 108
	private Suits[] suits = new Suits[108];

    // (3 dragons + 4 winds) * 4 = 28
	private Honors[] honors = new Honors[28];

    // (4 flowers) * 2 = 8
    // (4 seasons) * 4 = 16
	private Bonus[] bonus = new Bonus[24];


    Tiles() {
        reset();
        shuffleTiles();
    }

    private void reset() {
        // generate all Suits, Honors and Bonus tiles
        // Suits
        int i=0;
        for (int type=0; type<3; type++) {      // 3 types
            for (int rank=0; rank<9; rank++) {  // 9 ranks
                for (int ID=0; ID<4; ID++) {    // 4 duplicates
                    suits[i] = new Suits(type+1, rank+1, ID);
                    hidden_tiles.add(suits[i]);
                    i++;
                }
            }
        }

        // Honors
        // wind
        i=0;
        for (int rank=0; rank<4; rank++) {  // 4 winds
            for (int ID=0; ID<4; ID++) {    // 4 duplicates
                honors[i] = new Honors(1, rank+1, ID);
                hidden_tiles.add(honors[i]);
                i++;
            }
        }
        // dragons
        for (int rank=0; rank<3; rank++) {  // 3 dragons
            for (int ID=0; ID<4; ID++) {    // 4 duplicates
                honors[i] = new Honors(2, rank+1, ID);
                hidden_tiles.add(honors[i]);
                i++;
            }
        }

        // Bonus
        // seasons
        i=0;
        for (int rank=0; rank<4; rank++) {  // 4 seasons
            for (int ID=0; ID<4; ID++) {    // 4 duplicates
                bonus[i] = new Bonus(1, rank+1, ID);
                hidden_tiles.add(bonus[i]);
                i++;
            }
        }
        // flowers
        for (int rank=0; rank<4; rank++) {  // 4 flowers
            for (int ID=0; ID<2; ID++) {    // 2 duplicates
                bonus[i] = new Bonus(2, rank+1, ID);
                hidden_tiles.add(bonus[i]);
                i++;
            }
        }
    }

    // shuffle tiles
	void shuffleTiles() {
        Collections.shuffle(hidden_tiles);
    }

	// reveal tile
	Tile revealTile() {

        if (this.hidden_tiles.isEmpty()) {
            System.out.println("\nNo hidden tiles in deck left to uncover. Please restart the game :)");
            System.exit(0);
        }

        Tile t = this.hidden_tiles.remove(0);

        // check this is actually a legitimate tile
        if (t instanceof Suits || t instanceof Honors || t instanceof Bonus) {
            // all is good
        } else {
            // problem
            System.out.println("Error: This tile does not exist\n");
            System.exit(0);
        }

        // put revealed tile in ArrayList
        uncovered_tiles.add(t);

        return t;
    }


	boolean tilesLeft() {
    	return !this.hidden_tiles.isEmpty();
    }
    
}
