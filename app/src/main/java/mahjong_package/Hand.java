package mahjong_package;


import java.util.ArrayList;
import java.util.Collections;


public class Hand {
	
	// largest number of items in a hand
	private final int max_hand_size = 14;
	
	// hand of hidden and revealed tiles - empty unassigned Tile
	private Tile[] hidden_hand = new Tile[max_hand_size];
	private Tile[] revealed_hand = new Tile[max_hand_size];
	private int revealed_put;
	int num_hidden;
		

	// constructor
	Hand() {
		
		this.num_hidden = this.max_hand_size-1;
		this.revealed_put = 0;
	}
	
	/*
	 *  Create a hand of tiles from an array of tiles drawn
	 */
	void createHand(Tile[] tiles_drawn) {
		
		// check size of tiles_drawn[] input is correct
		if (tiles_drawn.length != this.max_hand_size-1) {
			// all is not good
			System.err.println("Unexpected number of tiles in hand created: " + tiles_drawn.length + "\n");
			System.exit(0);;
		}
		
		// assign to hand[]
		System.arraycopy(tiles_drawn, 0, this.hidden_hand, 0, this.max_hand_size - 1);
		
		// last tile is empty
		this.hidden_hand[this.max_hand_size-1] = new Tile();
	}
	
	
	/*
	 * Add a tile to the empty space in hand
	 */
	void addToHand(Tile tile) {
		// if no free space to place drawn tile
		if (this.num_hidden >= this.max_hand_size) {
			System.out.println("Error: No free space to place drawn tile. Code check required!\n");
			System.exit(0);
		}
		else {
			// assign tile to free space in idx
			this.hidden_hand[this.num_hidden] = tile;
			
			// increment number of hidden
			this.num_hidden++;
		}
	}
	
	
	/*
	 *  Discard a tile of a specified hand index
	 */
	Tile discardTile(int idx) {

		Tile tmp = new Tile();

		// verify index is valid
		if (idx < 0 || idx >= this.num_hidden) {
			System.out.printf("Hand index %d is not valid, please choose again\n", idx);
			return tmp;
		}
		
		// copy tile and remove from hand
		tmp = this.hidden_hand[idx];
		this.hidden_hand[idx] = new Tile();
		
		// shift back if needed to cover removed tile
		for (int i=idx; i<this.num_hidden; i++) {
			
			if (i != this.max_hand_size-1) {
				// if not last tile, shift back by copying next tile
				this.hidden_hand[i] = this.hidden_hand[i+1];
			} else {
				// last tile in hand, so assign an empty one
				this.hidden_hand[i] = new Tile();				
			}
		}
				
		// decrement hand number
		this.num_hidden--;
		
		return tmp;
	}
	
	
	// add n tiles to revealed
	public void revealTiles(Tile[] tiles, int n) {
		
		// should be at least two tiles per time
		if (n > 2) {
			System.out.println("Error: Should not reveal less that 2 tiles at at time!\n");
			System.exit(0);
		}
		
		// put tiles in revealed from revealed index
		if (n >= 0) System.arraycopy(tiles, 0, this.revealed_hand, this.revealed_put, n);
		
		this.revealed_put += n;
	}
	
	
	// reveal all tiles in hidden hand
	void revealTiles() {
		
		// tmp list of tiles that will be discarded from hidden hand
		Tile tmp_tile = new Tile();
		int num_hidden = this.num_hidden;
		
		// put tiles in revealed from revealed index
		for (int i=0; i<this.num_hidden; i++) {
			tmp_tile = this.discardTile(i);
			this.revealed_hand[this.revealed_put+i] = tmp_tile;
		}
		
		this.revealed_put += num_hidden;
	}
	
	
	// reveal tiles of hand given a hidden_hand idx
	void revealTiles(int[] hand_idx, int n) {
		
		Tile tmp_tile = new Tile();
		
		// should be at least two tiles per time
		if (n < 2) {
			System.out.println("Error: Should not reveal less that 2 tiles at at time!\n");
			System.exit(0);
		}
		
		// put tiles in revealed from revealed index
		for (int i=0; i<n; i++) {
			tmp_tile = this.discardTile(hand_idx[i]);
			this.revealed_hand[this.revealed_put+i] = tmp_tile;
		}
		
		this.revealed_put += n;
	}
	
	
	/*
	 *  Check for a Kong with prospective a new tile as an argument
	 *  Index refers to hand index of potential Kong
	 */
	//TODO: arraylst a bit overkill?
	ArrayList<int[]> checkKong(Tile t) {
		
		// ArrayList of integers for hidden hand index of Kongs
		ArrayList<int[]> kongs = new ArrayList<int[]>();
		
		// arrays of Kong matches
		int[] match_idx = new int[3];
		
		// find hand idx of four-of-a-kind matches if any
		int len = 0;
		int[] hand_idx = new int[this.max_hand_size];
		hand_idx = this.findHiddenIndex(t);
		for (int i=0; i<this.num_hidden; i++) {
			if (hand_idx[i] > 0 && i < 3) {
				match_idx[len] = hand_idx[len];
				len ++;
			}
		}
		
		// if 3 were found, add to arraylist to be returned
		if (len >= 3) {
			kongs.add(match_idx);
		}
		
		return kongs;
	}
	
	
	/*
	 *  Check for a Pong with prospective a new tile as an argument
	 *  Index refers to hand index of potential Pong
	 */
	ArrayList<int[]> checkPong(Tile t) {
		
		// ArrayList of integers for hidden hand index combos of pongs
		ArrayList<int[]> pongs = new ArrayList<int[]>();
		
		// arrays of pong matches or sequences in the hidden hand
		int[] match_idx = new int[2];
		int[] seq_idx = new int[2];
		
		// find hand idx of triple matches if any
		int len = 0;
		int[] hand_idx = new int[this.max_hand_size];
		hand_idx = this.findHiddenIndex(t);
		for (int i=0; i<this.num_hidden; i++) {
			if (hand_idx[i] > 0 && i < 2) {
				match_idx[len] = hand_idx[len];
				len ++;
			}
		}
		if (len >= 2) {
			pongs.add(match_idx);
		}
		
		if (t instanceof Suits) {
			// proceed
		} else {
			return pongs;
		}
		
        ArrayList<Tile> tmp_list = new ArrayList<Tile>();
        Collections.addAll(tmp_list, this.hidden_hand);
        
    	int loop_size = tmp_list.size();
		Tile tmp_tile = new Tile();
        
        // check for sequence - Suits only
    	while (tmp_list.size() > 0 && loop_size > 0) {
        	tmp_tile = tmp_list.get(0);
        	if (tmp_tile instanceof Suits) {
        		// do nothing
        	} else {
        		// not needed if not suit, remove from list
        		tmp_list.remove(0);
        	}
        	loop_size--;
    	}
            
        // add new tile to list of suits
        tmp_list.add(t);

        // sort by rank
        Collections.sort(tmp_list, new RankComparator());

        // Tile to be compared
        Tile[] tn = new Tile[3];
            
        // check if 3 with 1 difference in rank in hand
        for (int i=0; i<tmp_list.size()-2; i++) {
        	tn[0] = tmp_list.get(i);
        	tn[1] = tmp_list.get(i+1);
        	tn[2] = tmp_list.get(i+2);
        	// three same types in a row
        	if (tn[0].type == tn[1].type && tn[1].type == tn[2].type) {
        		// difference of 2 between first and last sorted tiles
        		if (tn[2].rank - tn[0].rank == 2) {
        			// one of these tiles is the potential Pong tile
        			if (t.descriptor.equals(tn[0].descriptor) || 
        				t.descriptor.equals(tn[1].descriptor) ||
        				t.descriptor.equals(tn[2].descriptor) )	
        			{
        				System.out.println(tn[0].descriptor + tn[1].descriptor + tn[2].descriptor);
        				// record those that are not t       				
    					if (t.descriptor.equals(tn[0].descriptor)) {
            				seq_idx[0] = this.findHiddenIndex(tn[1])[0];
        					seq_idx[1] = this.findHiddenIndex(tn[2])[0];
    					} else if (t.descriptor.equals(tn[1].descriptor)) {
    						seq_idx[0] = this.findHiddenIndex(tn[0])[0];
        					seq_idx[1] = this.findHiddenIndex(tn[2])[0];
    					} else {
    						seq_idx[0] = this.findHiddenIndex(tn[0])[0];
        					seq_idx[1] = this.findHiddenIndex(tn[1])[0];
    					}
    					pongs.add(seq_idx);
        			}
        		}
        	}           
        }

        return pongs;
	}
	
		
	/*
	 *  Check for a Mahjong - (4 * sets of 3) + a double
	 */
	boolean checkMahjong(Tile t) {
		
		// list for purpose of counting suits in hand
		ArrayList<Tile> tmp_list = new ArrayList<Tile>();
        Collections.addAll(tmp_list, this.hidden_hand);
        tmp_list.add(t); 
                
        // number of triples
		int num_set_3 = 0;
		// double present to finish on MJ?
		boolean isDouble = false;
		
		// consider revealed tiles - would be 3s and maybe 4s (which count as 3s)
		num_set_3 += (int) (this.revealed_put/3);
		
		// get histogram of all possible tiles for hand
		int n = 36;
		int[] count_arr;
		count_arr = this.countHand(tmp_list);
		
		// count number of doubles in hand - should be only one
		for (int i=0; i<n; i++) {
			// first double found is true
			if (count_arr[i] == 2 && !isDouble) {
				isDouble = true;
			} else if (count_arr[i] == 2 && isDouble) {
				// there are two doubles, this is not a Mahjong
				return false;
			} else if (count_arr[i] == 3) {
				// count triples
				num_set_3++;
			}
		}
		
		// if no doubles were found, no Mahjong
		if (!isDouble) {
			return false;
		}
		
		int[] bamboo_count;
		int[] dot_count;
		int[] char_count;
		bamboo_count = this.countSuits(tmp_list, 1);
		dot_count = this.countSuits(tmp_list, 2);
		char_count = this.countSuits(tmp_list, 3);
		
		// count number of sequence 3s
		for (int i=0; i<9-2; i++) {
			if (bamboo_count[i] == 1 && bamboo_count[i+1] == 1 && bamboo_count[i+2] == 1) {
				num_set_3++;
			}
			if (dot_count[i] == 1 && dot_count[i+1] == 1 && dot_count[i+2] == 1) {
				num_set_3++;
			}
			if (char_count[i] == 1 && char_count[i+1] == 1 && char_count[i+2] == 1) {
				num_set_3++;
			}
		}

		//TODO: probably don't do this here
		if (num_set_3 == 4) {
			return true;
		}
		
		return false;
	}
	
	
	// display a hand's contents
	void showHand() {
		System.out.println("\nHand: ");
		// for all tiles in hand, print a tile descriptor
		for (int i=0; i<this.max_hand_size; i++) {
			System.out.println(i + ": " + hidden_hand[i].descriptor);
		}
	}


	public String getDescriptor(int pos) {
		return this.hidden_hand[pos].descriptor;
	}
	
	
    // poll tiles list
	private int[] countHand(ArrayList<Tile> tiles) {
    	    	
    	int[] count;
    	int N = 42;
    	count = new int [N];
    	
    	int[] dot_ranks;
    	dot_ranks = new int [9];	//0-8
    	
    	int[] bamboo_ranks;
    	bamboo_ranks = new int [9];	//8-17
    	
    	int[] char_ranks;
    	char_ranks = new int[9];	//18-26
    	
    	int[] wind_ranks;
    	wind_ranks = new int[4];	//26-29
    	
    	int[] dragon_ranks;
    	dragon_ranks = new int[3];	//30-33
    	
    	int[] flower_ranks;
    	flower_ranks = new int[4];	//33-37
    	
    	int[] season_ranks;
    	season_ranks = new int[4];	//38-42
    	
    	Honors tmp_h;
    	Bonus tmp_b;
    	Suits tmp_s;
    	
    	bamboo_ranks = this.countSuits(tiles, 1);
    	dot_ranks = this.countSuits(tiles, 2);
    	char_ranks = this.countSuits(tiles, 3);
    	 	 	
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
    	
    	for (int i=0; i<N; i++) {
    		if (i<9) {
    			count[i] = dot_ranks[i];
    		} else if (i < 18) {
    			count[i] = bamboo_ranks[i-9];
    		} else if (i < 27) {
    			count[i] = char_ranks[i-18];
    		} else if (i < 31) {
    			count[i] = wind_ranks[i-27];
    		} else if (i < 34) {
    			count[i] = dragon_ranks[i-31];
    		} else if (i < 38) {
    			count[i] = flower_ranks[i-34];
    		} else {
    			count[i] = season_ranks[i-38];
    		}
    	}
    	
    	return count;
    	
    	/*
		String s;
		s = "";
		s += "Bamboo:\n";
		for (int i=1; i<=9; i++)
			s += "\t" + i + " -> " + bamboo_ranks[i-1] + "\n";
		s += "Dots:\n";
		for (int i=1; i<=9; i++)
			s += "\t" + i + " -> " + dot_ranks[i-1] + "\n";   
		s += "Characters:\n";
		for (int i=1; i<=9; i++)
			s += "\t" + i + " -> " + char_ranks[i-1] + "\n";
		s += "Winds:\n";
		s += "\tNorth -> " + wind_ranks[0] + "\n";
		s += "\tEast -> " + wind_ranks[1] + "\n";
		s += "\tSouth -> " + wind_ranks[2] + "\n";
		s += "\tWest -> " + wind_ranks[3] + "\n";
		s += "Dragons:\n";
		s += "\tRed -> " + dragon_ranks[0] + "\n";
		s += "\tGreen -> " + dragon_ranks[1] + "\n";
		s += "\tWhite -> " + dragon_ranks[2] + "\n";
		s += "Seasons:\n";
		s += "\tSpring -> " + season_ranks[0] + "\n";
		s += "\tSummer -> " + season_ranks[1] + "\n";
		s += "\tAutumn -> " + season_ranks[2] + "\n";
		s += "\tWinter -> " + season_ranks[3] + "\n";
		s += "Flowers:\n";
		s += "\tPlum -> " + flower_ranks[0] + "\n";
		s += "\tOrchid -> " + flower_ranks[1] + "\n";
		s += "\tChrysanthemum -> " + flower_ranks[2] + "\n";
		s += "\tBamboo -> " + flower_ranks[3] + "\n";
    	
    	System.out.println(s);
    	return s;
    	*/
    }

    
    /*
     * Count number of suits in ArrayList for a given suit type
     */
	private int[] countSuits(ArrayList<Tile> tiles, int type) {
    	
    	// dots 2, bamboo 1, chars 3
    	
    	int[] ranks;
    	ranks = new int[9];
    	
    	Suits tmp_s;
    	
    	// for all elements in list, increment respective ranks and types
    	for(Tile tmp : tiles) {
    	    // if Suits
    		if (tmp instanceof Suits) {
    			tmp_s = (Suits) tmp;
    			if (tmp_s.getType() == type) {
    				ranks[tmp_s.getRank()-1]++;
    			}
    		}
    	}
    	return ranks;
    }

    
    // find index of a tile in hand
	private int[] findHiddenIndex(Tile t) {
    	
    	// index of matches to be returned
    	int[] idx = new int[this.max_hand_size];
    	
    	// add every match to array
    	int num_found = 0;
    	for (int i=0; i<this.max_hand_size; i++) {
    	    // descriptors will match
    		if (t.descriptor.equals(this.hidden_hand[i].descriptor)) {
    			idx[num_found] = i;
    			num_found++;
    		}
    	}
    	
    	return idx;
    }
	
}
