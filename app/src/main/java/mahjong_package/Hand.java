package mahjong_package;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

public class Hand {
	
	// largest number of items in a hand
	private final int maxHandSize = 14;

	// tiles that remain hidden in hand to other players
	private ArrayList<Tile> hiddenHand = new ArrayList<>();
	// tiles that have been revealed already
	private ArrayList<Tile> revealedHand = new ArrayList<>();
	// tiles that are flowers collected
	private ArrayList<Tile> flowersCollected = new ArrayList<>();

	// some of these are for purpose of Firebase //TODO: verify they are all needed
	public void setMaxHandSize(int n) { }
	public void setHiddenHand(ArrayList<Tile> hand) { this.hiddenHand = hand; }
	public void setRevealedHand(ArrayList<Tile> hand) { this.revealedHand = hand; }
	public void setFlowersCollected(ArrayList<Tile> tiles) { this.flowersCollected = tiles; }
	public int getMaxHandSize() { return this.maxHandSize; }
	public ArrayList<Tile> getHiddenHand() { return this.hiddenHand; }
	public ArrayList<Tile> getRevealedHand() { return this.revealedHand; }
	public ArrayList<Tile> getFlowersCollected() { return this.flowersCollected; }

	// constructor
	Hand() {}

	/*
	 *  Create a hand of tiles from an array of tiles drawn
	 */
	HandStatus createHand(ArrayList<Tile> tiles_drawn) {
		HandStatus hs = HandStatus.ADD_FAILED;
		// clear hands
		this.clearHand();
		// check size of tiles_drawn[] input is correct
		if (tiles_drawn.size() != this.maxHandSize -1) {
			// all is not good
			Log.e("HAND","HAND: Unexpected number of tiles in hand created: " + tiles_drawn.size() + "\n");
			return hs;
		}
		hs = HandStatus.ADD_SUCCESS;
		for (int i=0; i<tiles_drawn.size(); i++) {
			// add tiles to hand and update hand status if a flower is added
			HandStatus tmp_hs = this.addToHand(tiles_drawn.get(i));
			if (tmp_hs == HandStatus.FLOWERS_ADDED) {
				hs = tmp_hs;
			}
		}
		return hs;
	}

	/*
	Get hidden hand size
	 */
	int getHiddenHandSize() {
		return this.hiddenHand.size();
	}

	int getRevealedHandSize() { return this.revealedHand.size(); }

	int getFlowersCollectedSize() { return this.flowersCollected.size(); }

	void clearHand() {
		this.hiddenHand.clear();
		this.revealedHand.clear();
	}

	private Boolean checkFlower(Tile t) {
		return t.getChildClass().equals("Bonus") && t.getType() == 2;
	}

	/*
	 * Add a tile to the empty space in hand
	 */
	HandStatus addToHand(Tile t) {
		if (this.checkFlower(t)) {
			this.flowersCollected.add(t);
			Log.i("Hand", "Hand: Added " + t.getDescriptor() + " to flower collection\n");
			return HandStatus.FLOWERS_ADDED;
		} else if (this.getHiddenHandSize() >= this.maxHandSize) {
			Log.e("Hand", "Hand: No free space to place drawn tile. Code check required!\n");
			return HandStatus.ADD_FAILED;
		} else {
			// assign tile to free space in idx
			this.hiddenHand.add(t);
			// order hidden hand
			Collections.sort(this.hiddenHand, new TileOrderComparator());
			Log.i("Hand", "Hand: Added tile " + t.getDescriptor() + " to hidden hand\n");
			return HandStatus.ADD_SUCCESS;
		}
	}
	
	/*
	 *  Discard a tile of a specified hand index
	 */
	Tile discardTile(int idx) {
		Tile tmp = new Tile();
		// verify index is valid
		if (idx < 0 || idx >= this.hiddenHand.size()) {
			System.out.printf("Hand index %d is not valid, please choose again\n", idx);
			return tmp;
		}
		// copy tile and remove from hand
		tmp = this.hiddenHand.remove(idx);
		// order hand
		Collections.sort(this.hiddenHand, new TileOrderComparator());
		Log.i("Hand","Hand: Discarded from hidden hand " + tmp.getDescriptor());
		return tmp;
	}

	// reveal all tiles in hidden hand
	void revealAllHiddenHandTiles() {
		// tmp list of tiles that will be discarded from hidden hand
		Tile tmp_tile;
		// put tiles in revealed from revealed index
		for (int i = 0; i<this.hiddenHand.size(); i++) {
			tmp_tile = this.discardTile(i);
			this.revealedHand.add(tmp_tile);
		}
		Log.i("Hand","Hand: Revealed entire hidden hand");
	}

	// reveal tiles of hand given a hidden_hand idx
	Boolean revealIndexedHiddenTiles(int[] hand_idx, int n) {
		Tile tmp_tile;
		// should be at least two tiles per time
		if (n < 2) {
			Log.e("Hand","Error: Should not reveal less that 2 tiles at at time!");
			return false;
		} else {
			// put tiles in revealed from revealed index
			for (int i=0; i<n; i++) {
				tmp_tile = this.discardTile(hand_idx[i]);
				this.revealedHand.add(tmp_tile);
				Log.i("Hand","Hand: Added to revealed hand " + tmp_tile.getDescriptor());
			}
		}
		return true;
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
		for (int i = 0; i<this.getHiddenHandSize(); i++) {
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
		// find hand idx of triple matches if any
		int len = 0;
		int[] hand_idx;
		hand_idx = this.findHiddenIndex(t);
		for (int i=0; i<this.getHiddenHandSize(); i++) {
			if (hand_idx[i] >= 0 && i < 2) {
				match_idx[len] = hand_idx[len];
				len ++;
			}
		}
		if (len >= 2) {
			pongs.add(match_idx);
		}
		return pongs;
	}

	/*
		Check for chow - a meld of three suits in a sequence of consecutive order
	 */
	ArrayList<int[]> checkChow(Tile t) {
		// ArrayList of integers for hidden hand index combos of pongs
		ArrayList<int[]> chows = new ArrayList<>();
		// arrays of pong matches or sequences in the hidden hand
		int[] seq_idx = new int[2];
		ArrayList<Tile> tmp_hand_list = new ArrayList<>(this.hiddenHand);
		int loop_size = tmp_hand_list.size();
		Tile tmp_tile;
		// if it is not a suit we cannot chow
		if (!t.getChildClass().equals("Suits")) {
			return chows;
		}
		// check for sequence - Suits only
		int idx = 0;
		while (tmp_hand_list.size() > 0 && loop_size > 0) {
			tmp_tile = tmp_hand_list.get(idx);
			if (tmp_tile.getType() == t.getType() && tmp_tile.getChildClass().equals("Suits")) {
				// do nothing
				idx++;
			} else {
				// not needed if not suit, remove from list
				tmp_hand_list.remove(idx);
			}
			loop_size--;
		}
		// add new tile to list of suits
		tmp_hand_list.add(t);
		// sort by rank
		Collections.sort(tmp_hand_list, new RankComparator());
		// Tile to be compared
		Tile[] tn = new Tile[3];
		// check if 3 with 1 difference in rank in hand
		for (int i=0; i<tmp_hand_list.size()-2; i++) {
			tn[0] = tmp_hand_list.get(i);
			tn[1] = tmp_hand_list.get(i+1);
			tn[2] = tmp_hand_list.get(i+2);
			// three same types in a row
			boolean t_is_member = false;
			for (int n=0; n<3; n++) {
				if (tn[n].getDescriptor().equals(t.getDescriptor())) {
					t_is_member = true;
					break;
				}
			}
			if (t_is_member) {
				if (tn[0].getType() == tn[1].getType() && tn[1].getType() == tn[2].getType()) {
					// difference of 2 between first and last sorted tiles
					if (tn[2].getRank() - tn[0].getRank() == 2) {
						// one of these tiles is the potential Pong tile
						if (t.getDescriptor().equals(tn[0].getDescriptor()) ||
								t.getDescriptor().equals(tn[1].getDescriptor()) ||
								t.getDescriptor().equals(tn[2].getDescriptor()) )
						{
							// record those that are not t
							if (t.getDescriptor().equals(tn[0].getDescriptor())) {
								seq_idx[0] = this.findHiddenIndex(tn[1])[0];
								seq_idx[1] = this.findHiddenIndex(tn[2])[0];
							} else if (t.getDescriptor().equals(tn[1].getDescriptor())) {
								seq_idx[0] = this.findHiddenIndex(tn[0])[0];
								seq_idx[1] = this.findHiddenIndex(tn[2])[0];
							} else {
								seq_idx[0] = this.findHiddenIndex(tn[0])[0];
								seq_idx[1] = this.findHiddenIndex(tn[1])[0];
							}
							chows.add(seq_idx);
						}
					}
				}
			}
		}
		return chows;
	}

	/*
	 *  Check for a Mahjong - (4 * sets of 3) + a double
	 * 	- check how many 3s (or 4s) are already revealed
		- we need to check how many 3 sequence suits there are
		- then we need to check how many triples
		- these should add up to 4
		- finally we need a double
	 */
	boolean checkMahjong(Tile t) {
		// list for purpose of counting suits in hand
		ArrayList<Tile> tmp_list = new ArrayList<>(this.hiddenHand);
		tmp_list.add(t);
		// we need to count all tiles
		int[] suit_count;
		int[] bonus_count;
		int[] honors_count;
		//number of tiles grouped
		int num_quadruple = 0;
		int num_triple = 0;
		int num_pair = 0;
		int num_revealed = 0;
		int num_seq = 0;
		// consider revealed tiles - would be 3s and maybe 4s (which count as 3s)
		num_revealed += this.revealedHand.size() / 3;
		// for all 3 types of suits - bamboo is type 1, dots is type 2, chars is type 3
		for (int n=0; n<3; n++) {
			suit_count = this.countSuits(tmp_list, n+1);
			// for all suits of a type
			for (int i=0; i<9; i++) {
				if (i < 9-2 && suit_count[i] == 1 && suit_count[i+1] == 1 && suit_count[i+2] == 1) {
					num_seq++;
				} else if (suit_count[i] == 4) {
					num_quadruple++;
				} else if (suit_count[i] == 3) {
					num_triple++;
				} else if (suit_count[i] == 2) {
					num_pair++;
				}
			}
		}

		// count number of triples for Bonus and Honors
		for (int n=0; n<2; n++) {
			bonus_count = this.countBonus(tmp_list, n+1);
			honors_count = this.countHonors(tmp_list, n+1);
			for (int i=0; i<4; i++) {
				if (bonus_count[i] == 4) {
					num_quadruple++;
				} else if (bonus_count[i] == 3) {
					num_triple++;
				} else if (bonus_count[i] == 2) {
					num_pair++;
				}
				if (honors_count[i] == 4) {
					num_quadruple++;
				} else if (honors_count[i] == 3) {
					num_triple++;
				} else if (honors_count[i] == 2) {
					num_pair++;
				}
			}
		}
		Log.d("Hand", "Hand: Already revealed " + num_revealed);
		Log.d("Hand", "Hand: Four of a kind = " + num_quadruple);
		Log.d("Hand", "Hand: Three of a kind = " + num_triple);
		Log.d("Hand", "Hand: Sequence of three = " + num_seq);
		Log.d("Hand", "Hand: Two of a kind = " + num_pair);
		// if meets Mahjong criteria return true
		return (num_quadruple + num_revealed + num_triple + num_seq) == 4 && num_pair == 1;
	}
	
	// display a hand's contents
	void showHiddenHand() {
		// for all tiles in hand, print a tile descriptor
		System.out.println("\nHand: ");
		for (int i=0; i<this.getHiddenHandSize(); i++) {
			System.out.println(i + ": " + this.hiddenHand.get(i).getDescriptor());
		}
	}

	// get all descriptors for tiles in revealed hand
	ArrayList<String> getRevealedDescriptors() {
		ArrayList<String> descriptors = new ArrayList<>();
		for (int i = 0; i<this.revealedHand.size(); i++) {
			descriptors.add(this.revealedHand.get(i).getDescriptor());
		}
		return descriptors;
	}

	// get all descriptors for tiles in hidden hand
	ArrayList<String> getHiddenDescriptors() {
		ArrayList<String> descriptors = new ArrayList<>();
		for (int i = 0; i<this.hiddenHand.size(); i++) {
			descriptors.add(this.hiddenHand.get(i).getDescriptor());
		}
		return descriptors;
	}

	/*
     * Count number of suits in ArrayList for a given suit type
     */
	private int[] countSuits(ArrayList<Tile> tiles, int type) {
    	// dots 2, bamboo 1, chars 3
    	int[] ranks = new int[9];
    	// for all elements in list, increment respective ranks and types
    	for (Tile tmp : tiles) {
    	    // if Suits
    		if (tmp.getChildClass().equals("Suits") && tmp.getType() == type) {
    			ranks[tmp.getRank()-1]++;
    		}
    	}
    	return ranks;
    }

	/*
	 * Count number of suits in ArrayList for a given suit type
	 */
	private int[] countBonus(ArrayList<Tile> tiles, int type) {
		int[] ranks = new int[4];
		// for all elements in list, increment respective ranks and types
		for (Tile tmp : tiles) {
			// if Bonus
			if (tmp.getChildClass().equals("Bonus") && tmp.getType() == type) {
				// seasons = 1
				ranks[tmp.getRank()-1]++;
			}
		}
		return ranks;
	}

	/*
	 * Count number of suits in ArrayList for a given suit type
	 */
	private int[] countHonors(ArrayList<Tile> tiles, int type) {
		int[] ranks = new int[4];
		// for all elements in list, increment respective ranks and types
		for (Tile tmp : tiles) {
			// if Honors
			if (tmp.getChildClass().equals("Honors") && tmp.getType() == type) {
				// wind = 1, dragon = 2
				ranks[tmp.getRank()-1]++;
			}
		}
		return ranks;
	}
    
    // find index of a tile in hand
	private int[] findHiddenIndex(Tile t) {
    	// index of matches to be returned
    	int[] idx = new int[this.maxHandSize];
    	// add every match to array
    	int num_found = 0;
    	for (int i=0; i<this.getHiddenHandSize(); i++) {
    	    // descriptors will match
			idx[i] = -1;
    		if (t.getDescriptor().equals(this.hiddenHand.get(i).getDescriptor())) {
    			idx[num_found] = i;
    			num_found++;
    		}
    	}
    	return idx;
    }

}