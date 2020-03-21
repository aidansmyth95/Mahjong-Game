package mahjong_package;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner; 


public class Hand {
	
	// largest number of items in a hand
	private final int hand_size = 14;
	
	// hand of tiles - empty unassigned Tile
	private Tile hand[] = new Tile[hand_size];
	
	// free  idx in hand
	private int free_idx;
	
	// user input if incorrect discard idx
	private Scanner scan = new Scanner(System.in);

	// constructor
	public Hand() {
		this.free_idx = this.hand_size-1;
	}
	
	// create a hand of tiles
	public void createHand(Tile[] tiles_drawn) {
		
		// check size of tiles_drawn[] input is correct
		if (tiles_drawn.length == this.hand_size-1) {
			// all is good
		}
		else {
			// all is not good
			System.out.println("Unexpected number of tiles in hand created: " + tiles_drawn.length + "\n");
			return;
		}
		
		// assign to hand[]
		for (int i=0; i<this.hand_size-1; i++) {
			hand[i] = tiles_drawn[i];
		}
	}
	
	// draw tile and place in empty spot
	public void addToHand(Tile tile) {
		// if no free space to place drawn tile
		if (this.free_idx == -1) {
			System.out.println("Error: No free space to place drawn tile. Code check required!\n");
			return;
		}
		else {
			// assign tile to free space in idx
			this.hand[this.free_idx] = tile;
			
			// no free space now, so idx = -1
			this.free_idx = -1;
		}
	}
	
	
	// discard tile function
	public Tile discardTile(int idx) {
		
		// verify index is valid
		while (idx < 0 || idx >= this.hand_size) {
			System.out.printf("Hand index " + idx + " is not valid, please choose again\n");
			//TODO: verify and test this works
			idx = scan.nextInt();
		}
		
		// copy tile and remove from hand
		Tile tmp = this.hand[idx];
		this.hand[idx] = new Tile();
		
		// new tile drawn will be placed in this now free idx
		this.free_idx = idx;
		
		return tmp;
	}
	
	
	// check for triple in hand
	public boolean checkTriple() {
        ArrayList<Tile> tmp_list = new ArrayList<Tile>();
        Collections.addAll(tmp_list, this.hand);

        Tile tmp_tile;
        int num_match = 0;	// number of triples matched
        int count = 0;		// number of a tile found

        // need at least 3 tiles
        while (tmp_list.size() > 2) {
            // Draw a tile to compare to the others for a match
            tmp_tile = tmp_list.remove(0);
            count = 0;

            // check list for a triple
        	for (int i=0; i<tmp_list.size()-1; i++) {
        		        		
        		Tile t = tmp_list.get(i);
        		
                // compare String descriptor for match
                if(t != null && t.descriptor.equals(tmp_tile.descriptor)) {
                    // remove matched item from list
                    tmp_list.remove(t);
                    count++;
                    // if triple found, time to check for another match
                    if (count == 2) {
                    	num_match++;
                    	break;
                    }
                }
                //something here
            }
        }
        
        if (num_match > 0) {
        	System.out.println("Number of triples is " + num_match + "\n");
        	return true;
        }
        else {
        	return false;
        }
	}
	
	
	// check for three in a row sequence for Suits
	public int checkThreeSequence() {
		
		System.out.println("Checking for sequence of three consecutive suit types in hand. Hand:");
		showHand();
		
		// place hand in ArrayList
        ArrayList<Tile> tmp_list = new ArrayList<Tile>();
        Collections.addAll(tmp_list, this.hand);
        
        int num_seq = 0;
        
        // count number of suits - proceed if it least 3
        for (int i=0; i<this.hand_size; i++) {
        	if (tmp_list.get(i) instanceof Suits) {
        		// do nothing
        	} else {
        		// not needed if not suit, remove from list
        		tmp_list.remove(i);
        	}
        }
        
        if (tmp_list.size() < 3) {
        	// not enough suits to form sequence of 3
        	return num_seq;
        }

        // sort suits
        Collections.sort(tmp_list, new RankComparator());
        
        // check if 3 with 1 difference in rank in hand
        for (int i=0; i<tmp_list.size()-3; i++) {
        	// if types are same and ranks are within 1 of each other
        	if (tmp_list.get(i).type == tmp_list.get(i+1).type && tmp_list.get(i+1).type == tmp_list.get(i+2).type) {
        		// three same types in a row
        		if (Math.abs(tmp_list.get(i).rank - tmp_list.get(i+2).rank) == 2) {
        			// difference of 2 between first and last sorted tiles
        			num_seq++;
        		}
        	}
        }
        
        return 0;
	}
	
	// check win
	//TODO: temporarily - a win is getting a triple. Copy idea of matches i Tiles.java
	public boolean checkWin() {
		return this.checkTriple();
	}
	
	
	// display a hand's contents
	public void showHand() {
		System.out.println("\nHand: ");
		// for all tiles in hand, print a tile descriptor
		for (int i=0; i<hand.length-1; i++) {
			System.out.print(i + ": ");
			if (hand[i] != null) {
				System.out.println(hand[i].descriptor);
			}
			else {
				System.out.println("No tile\n");
			}
		}
		
		if (hand[hand.length-1] != null) {
			System.out.println(hand.length-1 + ": " + hand[hand.length-1].descriptor);
		}
		else {
			System.out.println("No tile\n");
		}
	}
	
	
	
}
