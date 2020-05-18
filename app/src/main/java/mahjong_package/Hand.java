package mahjong_package;


import java.util.ArrayList;
import java.util.Collections;


public class Hand {
	
	// largest number of items in a hand
	private final int max_hand_size = 14;

	// hand of hidden and revealed tiles - empty unassigned Tile
	private Tile[] hidden_hand = new Tile[max_hand_size];
	//TODO: display in Game Activity
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
		
		// copy to hand
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
		
		Tile tmp_tile;
		
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
	ArrayList<int[]> checkKong(Tile t) {
		
		// ArrayList of integers for hidden hand index of Kongs
		ArrayList<int[]> kongs = new ArrayList<>();
		
		// arrays of Kong matches
		int[] match_idx = new int[3];
		
		// find hand idx of four-of-a-kind matches if any
		int len = 0;
		int[] hand_idx;
		hand_idx = this.findHiddenIndex(t);
		for (int i=0; i<this.num_hidden; i++) {
			if (hand_idx[i] >= 0 && i < 3) {
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
		ArrayList<int[]> pongs = new ArrayList<>();

		// arrays of pong matches or sequences in the hidden hand
		int[] match_idx = new int[2];
		int[] seq_idx = new int[2];
		
		// find hand idx of triple matches if any
		int len = 0;
		int[] hand_idx;
		hand_idx = this.findHiddenIndex(t);
		for (int i=0; i<this.num_hidden; i++) {
			if (hand_idx[i] >= 0 && i < 2) {
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

        ArrayList<Tile> tmp_list = new ArrayList<>();
        Collections.addAll(tmp_list, this.hidden_hand);
        
    	int loop_size = tmp_list.size();
		Tile tmp_tile;
        
        // check for sequence - Suits only
		int idx = 0;
    	while (tmp_list.size() > 0 && loop_size > 0) {
        	tmp_tile = tmp_list.get(idx);
        	if (tmp_tile.type == t.type && tmp_tile instanceof Suits) {
        		// do nothing
				idx++;
				//System.out.println(tmp_tile.descriptor);
        	} else {
        		// not needed if not suit, remove from list
        		tmp_list.remove(idx);
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
			boolean t_is_member = false;
			for (int n=0; n<3; n++) {
				if (tn[n].descriptor.equals(t.descriptor)) {
					t_is_member = true;
					break;
				}
			}
			if (t_is_member) {
				/* System.out.println("DEBUG: T is member!");
				for (int p=0; p<3; p++) {
					System.out.println(tn[p].descriptor + "\t");
				}
				System.out.println("\n"); */
				if (tn[0].type == tn[1].type && tn[1].type == tn[2].type) {
					// difference of 2 between first and last sorted tiles
					if (tn[2].rank - tn[0].rank == 2) {
						// one of these tiles is the potential Pong tile
						if (t.descriptor.equals(tn[0].descriptor) ||
								t.descriptor.equals(tn[1].descriptor) ||
								t.descriptor.equals(tn[2].descriptor) )
						{
							//System.out.println("DEBUG: " + tn[0].descriptor + tn[1].descriptor + tn[2].descriptor);
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
							//System.out.println("DEBUG: Added!");
							pongs.add(seq_idx);
						}
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

		// check how many 3s (or 4s) are already revealed
		// we need to check how many 3 sequence suits there are
		// then we need to check how many triples
		// these should add up to 4
		// finally we need a double

		// we need to count all tiles
		int[] suit_count;
		int[] bonus_count;
		int[] honors_count;

		// list for purpose of counting suits in hand
		ArrayList<Tile> tmp_list = new ArrayList<>();
        Collections.addAll(tmp_list, this.hidden_hand);
        tmp_list.add(t);

		// number of triples
		int num_triple = 0;
		// number of doubles
		int num_pair = 0;
		// number revealed
		int num_revealed = 0;
		//number of sequences
		int num_seq = 0;
		
		// consider revealed tiles - would be 3s and maybe 4s (which count as 3s)
		num_revealed += this.revealed_put/3;

		// for all 3 types of suits
		// bamboo is type 1, dots is type 2, chars is type 3
		for (int n=0; n<3; n++) {
			suit_count = this.countSuits(tmp_list, n+1);
			// for all suits of a type
			for (int i=0; i<9; i++) {
				if (i < 9-2 && suit_count[i] == 1 && suit_count[i+1] == 1 && suit_count[i+2] == 1) {
					// count number of sequence 3s
					num_seq++;
				} else if (suit_count[i] == 3) {
					// count number of triples
					num_triple++;
				} else if (suit_count[i] == 2) {
					// count number of doubles
					num_pair++;
				}
			}
		}

		//TODO: how do we handle four of a kind in hand?

		// count number of triples for Bonus and Honors
		for (int n=0; n<2; n++) {
			bonus_count = this.countBonus(tmp_list, n+1);
			honors_count = this.countHonors(tmp_list, n+1);

			for (int i=0; i<4; i++) {
				if (bonus_count[i] == 3) {
					num_triple++;
				} else if (bonus_count[i] == 2) {
					num_pair++;
				}
				if (honors_count[i] == 3) {
					num_triple++;
				} else if (honors_count[i] == 2) {
					num_pair++;
				}
			}
		}

		//System.out.println("DEBUG: Already revealed " + num_revealed);
		//System.out.println("DEBUG: Three of a kind = " + num_triple);
		//System.out.println("DEBUG: Sequence of three = " + num_seq);
		//System.out.println("DEBUG: Two of a kind = " + num_pair);

		// if meets Mahjong criteria return true
		return (num_revealed + num_triple + num_seq) == 4 && num_pair == 1;
	}
	
	
	// display a hand's contents
	void showHand() {
		System.out.println("\nHand: ");
		// for all tiles in hand, print a tile descriptor
		for (int i=0; i<this.max_hand_size; i++) {
			System.out.println(i + ": " + hidden_hand[i].descriptor);
		}
	}


	// get descriptor for a tile in hidden hand at a specified hand position
	String getDescriptor(int pos) {
		return this.hidden_hand[pos].descriptor;
	}
	

    /*
     * Count number of suits in ArrayList for a given suit type
     */
	private int[] countSuits(ArrayList<Tile> tiles, int type) {
    	
    	// dots 2, bamboo 1, chars 3
    	int[] ranks = new int[9];
    	
    	Suits tmp_s;
    	
    	// for all elements in list, increment respective ranks and types
    	for (Tile tmp : tiles) {
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


	/*
	 * Count number of suits in ArrayList for a given suit type
	 */
	private int[] countBonus(ArrayList<Tile> tiles, int type) {

		int[] ranks = new int[4];

		Bonus tmp_b;

		// for all elements in list, increment respective ranks and types
		for (Tile tmp : tiles) {
			// if Bonus
			if (tmp instanceof Bonus) {
				tmp_b = (Bonus) tmp;
				if (tmp_b.getType() == type) {
					// seasons = 1
					ranks[tmp_b.getRank()-1]++;
				}
			}
		}
		return ranks;
	}


	/*
	 * Count number of suits in ArrayList for a given suit type
	 */
	private int[] countHonors(ArrayList<Tile> tiles, int type) {

		int[] ranks = new int[4];

		Honors tmp_h;

		// for all elements in list, increment respective ranks and types
		for (Tile tmp : tiles) {
			// if Honors
			if (tmp instanceof Honors) {
				tmp_h = (Honors) tmp;
				if (tmp_h.getType() == type) {
					// wind = 1, dragon = 2
					ranks[tmp_h.getRank()-1]++;
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
			idx[i] = -1;
    		if (t.descriptor.equals(this.hidden_hand[i].descriptor)) {
    			idx[num_found] = i;
    			num_found++;
    		}
    	}
    	return idx;
    }
	
}
