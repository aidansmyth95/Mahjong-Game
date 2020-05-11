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
    
    // claim a tile by popping from uncovered tiles
	Tile claimLatestTile() {
    	// remove from uncovered tiles and return
    	int pop_idx = this.uncovered_tiles.size() - 1;
    	return this.uncovered_tiles.remove(pop_idx);
    }

    // reveal tile
	Tile revealTile() {

        if (this.hidden_tiles.isEmpty()) {
            System.out.println("\nNo hidden tiles in deck left to uncover. Please restart the game :)");
            //System.out.println("Uncovered tiles " + this.uncovered_tiles.size());
            System.exit(0);
        }

        Tile t = this.hidden_tiles.remove(0);

        // check this is actually a legitimate tile
        if (t instanceof Suits || t instanceof Honors || t instanceof Bonus) {
            // all is good
        	//System.out.println(t.descriptor + " tile uncovered.\n");
        } else {
            // problem
            System.out.println("Error: This tile does not exist\n");
            System.exit(0);
        }

        // put revealed tile in ArrayList
        uncovered_tiles.add(t);

        return t;
    }


    
    // poll tiles list
    public String listTiles(ArrayList<Tile> tiles) {

    	int[] dot_ranks;
    	dot_ranks = new int [9];
    	
    	int[] bamboo_ranks;
    	bamboo_ranks = new int [9];
    	
    	int[] char_ranks;
    	char_ranks = new int[9];
    	
    	int[] wind_ranks;
    	wind_ranks = new int[4];
    	
    	int[] dragon_ranks;
    	dragon_ranks = new int[3];
    	
    	int[] flower_ranks;
    	flower_ranks = new int[4];
    	
    	int[] season_ranks;
    	season_ranks = new int[4];
    	
    	Honors tmp_h;
    	Bonus tmp_b;
    	Suits tmp_s;
    	
    	 	
    	// for all elements in list, increment respective ranks and types
    	for(Tile tmp : tiles) {
    	    // if honor
    		if (tmp instanceof Honors) {
    			tmp_h = (Honors) tmp;
    			if (tmp_h.getType() == 1) {
    				wind_ranks[tmp_h.getRank()-1]++;
    			}
    			else if (tmp_h.getType() == 2) {
    				dragon_ranks[tmp_h.getRank()-1]++;
    			}
    		}
    		// if bonus
    		else if (tmp instanceof Bonus) {
    			tmp_b = (Bonus) tmp;
    			if (tmp_b.getType() == 1) {
    				season_ranks[tmp_b.getRank()-1]++;
    			}
    			else if (tmp_b.getType() == 2) {
    				flower_ranks[tmp_b.getRank()-1]++;
    			}
    		}    		
    		// if suit
    		else if (tmp instanceof Suits) {
    			tmp_s = (Suits) tmp;
    			if (tmp_s.getType() == 1) {
    				bamboo_ranks[tmp_s.getRank()-1]++;
    			}
    			else if (tmp_s.getType() == 2) {
    				dot_ranks[tmp_s.getRank()-1]++;
    			}
    			else if (tmp_s.getType() == 3 ) {
    				char_ranks[tmp_s.getRank()-1]++;
    			}
    		}
    	}
    	
		StringBuilder s;
		s = new StringBuilder();
		s.append("Bamboo:\n");
		for (int i=1; i<=9; i++)
			s.append("\t").append(i).append(" -> ").append(bamboo_ranks[i - 1]).append("\n");
		s.append("Dots:\n");
		for (int i=1; i<=9; i++)
			s.append("\t").append(i).append(" -> ").append(dot_ranks[i - 1]).append("\n");
		s.append("Characters:\n");
		for (int i=1; i<=9; i++)
			s.append("\t").append(i).append(" -> ").append(char_ranks[i - 1]).append("\n");
		s.append("Winds:\n");
		s.append("\tNorth -> ").append(wind_ranks[0]).append("\n");
		s.append("\tEast -> ").append(wind_ranks[1]).append("\n");
		s.append("\tSouth -> ").append(wind_ranks[2]).append("\n");
		s.append("\tWest -> ").append(wind_ranks[3]).append("\n");
		s.append("Dragons:\n");
		s.append("\tRed -> ").append(dragon_ranks[0]).append("\n");
		s.append("\tGreen -> ").append(dragon_ranks[1]).append("\n");
		s.append("\tWhite -> ").append(dragon_ranks[2]).append("\n");
		s.append("Seasons:\n");
		s.append("\tSpring -> ").append(season_ranks[0]).append("\n");
		s.append("\tSummer -> ").append(season_ranks[1]).append("\n");
		s.append("\tAutumn -> ").append(season_ranks[2]).append("\n");
		s.append("\tWinter -> ").append(season_ranks[3]).append("\n");
		s.append("Flowers:\n");
		s.append("\tPlum -> ").append(flower_ranks[0]).append("\n");
		s.append("\tOrchid -> ").append(flower_ranks[1]).append("\n");
		s.append("\tChrysanthemum -> ").append(flower_ranks[2]).append("\n");
		s.append("\tBamboo -> ").append(flower_ranks[3]).append("\n");
    	
    	System.out.println(s);
    	return s.toString();
    }
    
    boolean tilesLeft() {
    	return !this.hidden_tiles.isEmpty();
    }
    
}
