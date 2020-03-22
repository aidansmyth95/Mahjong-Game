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
	
	/*
	 *  Create a hand of tiles from an array of tiles drawn
	 */
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
	
	
	/*
	 * Add a tile to the empty space in hand
	 */
	public void addToHand(Tile tile) {
		// if no free space to place drawn tile
		if (this.free_idx == -1) {
			System.out.println("Error: No free space to place drawn tile. Code check required!\n");
			System.exit(0);
		}
		else {
			// assign tile to free space in idx
			this.hand[this.free_idx] = tile;
			
			// no free space now, so idx = -1
			this.free_idx = -1;
		}
	}
	
	
	/*
	 *  Discard a tile of a specified hand index
	 */
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
	
	
	//TODO: what if you already have 3 in your hand? Check 4 of a kind...

	
	/*
	 *  Check for a Pong with prospective a new tile as an argument
	 */
	public boolean checkPong(Tile t) {
        ArrayList<Tile> tmp_list = new ArrayList<Tile>();
        Collections.addAll(tmp_list, this.hand);
        
        ArrayList<Tile> triple_tiles= new ArrayList<Tile>();
        ArrayList<Tile> seq_tiles= new ArrayList<Tile>();
        
        Tile tmp_tile = new Tile();
        int num_match = 0;
        int num_seq = 0;

        // check for 3 in a row:
        // do 2 others have the same descriptor as incoming tile?
        while (tmp_list.size() > 0) {
        	tmp_tile = tmp_list.remove(0);
        	if (tmp_tile.descriptor == t.descriptor) {
        		num_match++;
        		triple_tiles.add(tmp_tile);
        	}
        }
        
        if (num_match == 2) {
        	// we have three of a kind
        	return true;
        } else {
        	triple_tiles.clear();
        }
        
        // refresh list of tiles
        tmp_list.clear();
        Collections.addAll(tmp_list, this.hand);
        
        // check for sequence - Suits only
        if (t instanceof Suits) {
        	// remove all non suits
            for (int i=0; i<this.hand_size; i++) {
            	if (tmp_list.get(i) instanceof Suits) {
            		// do nothing
            	} else {
            		// not needed if not suit, remove from list
            		tmp_list.remove(i);
            	}
            }
            
            // add new tile to list of suits
            tmp_list.add(t);
            
            // sort by rank
            Collections.sort(tmp_list, new RankComparator());
            
            // Tile to be compared
            Tile t1, t2, t3 = new Tile();
            
            // check if 3 with 1 difference in rank in hand
            for (int i=0; i<tmp_list.size()-3; i++) {
            	t1 = tmp_list.get(i);
            	t2 = tmp_list.get(i+1);
            	t3 = tmp_list.get(i+2);
            	// three same types in a row
            	if (t1.type == tmp_list.get(i+1).type && t2.type == t3.type) {
            		// difference of 2 between first and last sorted tiles
            		if (Math.abs(t1.rank - t3.rank) == 2) {
            			// one of these tiles is the potential pong tile
            			if (
            					t.descriptor.equals(t1.descriptor) || 
            					t.descriptor.equals(t2.descriptor) ||
            					t.descriptor.equals(t3.descriptor)
    					)	{
        						seq_tiles.add(t1);
        						seq_tiles.add(t2);
        						seq_tiles.add(t3);
        						num_seq++;
        					}
            		}
            	}
            }
            
        }
        
        if (num_seq > 0) {
        	return true;
        }
        
        seq_tiles.clear();
        return false;
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
	
		
	/*
	 *  Check for a Pong - three of a kind
	 */
	public boolean checkMahjong(Tile t) {
		//( 4 * sets of 3 ) + a double
		int n = 36;
		
        ArrayList<Tile> tmp_list = new ArrayList<Tile>();
        Collections.addAll(tmp_list, this.hand);
        tmp_list.add(t); 
                
		int num_set_3 = 0;
		boolean isDouble = false;
		int count_arr[];
		count_arr = new int [36];
		count_arr = this.countHand(tmp_list);
		
		// count number of doubles in hand - should be only one
		for (int i=0; i<n; i++) {
			// first double found is true
			if (count_arr[i] == 2 && isDouble == false) {
				isDouble = true;
			} else if (count_arr[i] == 2 && isDouble == true) {
				// there are two doubles, this is not a Mahjong
				return false;
			} else if (count_arr[i] == 3) {
				// count triples
				num_set_3++;
			}
		}
		
		// if no doubles were found, no Mahjong
		if (isDouble == false) {
			return false;
		}
		
		int bamboo_count[], dot_count[], char_count[];
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
		
		if (num_set_3 == 4 && isDouble == true) {
			System.out.println("MAHJONG!");
			return true;
		}
		
		return false;
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
	
	
    // poll tiles list
    public int[] countHand(ArrayList<Tile> tiles) {
    	    	
    	int count[];
    	int N = 36;
    	count = new int [N];
    	
    	int dot_ranks[];
    	dot_ranks = new int [9];
    	
    	int bamboo_ranks[];
    	bamboo_ranks = new int [9];
    	
    	int char_ranks[];
    	char_ranks = new int[9];
    	
    	int wind_ranks[];
    	wind_ranks = new int[4];
    	
    	int dragon_ranks[];
    	dragon_ranks = new int[3];
    	
    	int flower_ranks[];
    	flower_ranks = new int[4];
    	
    	int season_ranks[];
    	season_ranks = new int[4];
    	
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
    		} else if (i>=9 && i<18) {
    			count[i] = bamboo_ranks[i-9];
    		} else if (i>=18 && i<21) {
    			count[i] = char_ranks[i-18];
    		} else if (i>=21 && i<24) {
    			count[i] = wind_ranks[i-21];
    		} else if (i>=24 && i>27) {
    			count[i] = dragon_ranks[i-24];
    		} else if (i>=27 && i<31) {
    			count[i] = flower_ranks[i-27];
    		} else {
    			count[i] = season_ranks[i-31];
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
    public int[] countSuits(ArrayList<Tile> tiles, int type) {
    	
    	// dots 2, bamboo 1, chars 3
    	
    	int ranks[];
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

	
}
