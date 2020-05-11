package mahjong_package;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

class Player {

	// player's hand
	private Hand hand = new Hand();

	// pong identified and waiting user response
	boolean pong_identified;

	ArrayList<int[]> possible_pongs = new ArrayList<int[]>();
	ArrayList<int[]> possible_kongs = new ArrayList<int[]>();

	String user_kong_input;
	String user_pong_input;
	int chosen_idx;
	
	// constructor
	Player(int player_ID)
    {
        // assert ID is valid and assign
        if (!this.isValid(player_ID)) {
			System.out.print("Error: Player ID %d is not valid\n");
        	System.exit(0);
        }
		this.clearKongsPongs();
    }


    // reset pong/kong interactions
    public void clearKongsPongs() {
		this.pong_identified = false;
		this.chosen_idx = -1;
		this.user_kong_input = "";
		this.user_pong_input = "";
	}

    
    // player ID valid
	private boolean isValid(int player_ID) {
		return player_ID < 4;
	}
	
	
	// create hand for player
	void createHand(Tile[] tiles_drawn) {
		this.hand.createHand(tiles_drawn);
	}
	
	
	// check for win
	boolean Mahjong(Tile t) {

		boolean mj = this.hand.checkMahjong(t);

		// if Mahjong, add tile to hand and reveal all tiles
		if (mj) {
			this.hand.addToHand(t);
			this.hand.revealTiles();
		}
		return mj;
	}


	// check hand for kong, update options for kong, return true if chance for kong
	boolean checkHandKong(Tile t) {
		ArrayList<int[]> kongs = new ArrayList<int[]>();
		kongs = this.hand.checkKong(t);

		// if no Pong arrays in this ArrayList (size 0), return false
		if (kongs.size() > 0) {
			for (int i=0; i<kongs.size(); i++) {
				this.possible_kongs.add(kongs.get(i));
			}
			return true;
		}

		return false;
	}


	boolean checkUserKong(String player_input) {

		if (player_input.equals("Kong")) {
			System.out.println("Kong!\n");
			this.user_kong_input = player_input;
			return true;
		}
		return false;
	}

	//TODO: no need boolean
	boolean kong(Tile t) {
		this.hand.addToHand(t);
		int[] hand_idx = new int[4];
		hand_idx[0] = this.possible_kongs.get(0)[0];
		hand_idx[1] = this.possible_kongs.get(0)[1];
		hand_idx[2] = this.possible_kongs.get(0)[2];
		hand_idx[3] = this.hand.num_hidden-1;
		this.hand.revealTiles(hand_idx, 4);
		return true;
	}


	// check for Pong given a potential tile - Three-of-a-kind or a sequence of three.
	boolean checkHandPong(Tile t) {
		ArrayList<int[]> pongs = new ArrayList<int[]>();

		pongs = this.hand.checkPong(t);

		// if no Pong arrays in this ArrayList (size 0), return false
		if (pongs.size() > 0) {
			// if Pong option(s), list options & ask user to choose Pong, or decline
			this.hand.showHand();
			System.out.println("You can pong for " + t.descriptor + ".\nPlease choose an option to pong with:");
			for (int i=0; i<pongs.size(); i++) {
				System.out.printf("\t%d. idx {%d %d}\n", i, pongs.get(i)[0], pongs.get(i)[1]);
				this.possible_pongs.add(pongs.get(i));
			}
			System.out.printf("\t%d. Skip pong\n", pongs.size());
			return true;
		}
		return false;
	}


	boolean checkUserPong(String player_input) {
		int resp;
		try {
			resp = Integer.parseInt(player_input);
		} catch (InputMismatchException ex) {
			// in case not a valid integer idx
			resp = this.possible_pongs.size();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			resp = this.possible_pongs.size();
		}

		// if last idx, no Pong...
		if (resp < this.possible_pongs.size() && resp >= 0) {
			this.user_pong_input = player_input;
			this.chosen_idx = resp;
			return true;
		}
		return false;
	}


	//TODO: no need boolean
	boolean pong(Tile t) {
		int[] hand_idx = new int[3];
		this.hand.addToHand(t);
		hand_idx[0] = this.possible_pongs.get(this.chosen_idx)[0];
		hand_idx[1] = this.possible_pongs.get(this.chosen_idx)[1];
		hand_idx[2] = this.hand.num_hidden-1;
		this.hand.revealTiles(hand_idx, 3);
		return true;
	}


	
	// a Tse has been called. Add tile and discard a chosen tile.
	Tile tse(Tile t, String player_input) {
		
		// check which tile to discard
		System.out.println("Discard tile with index: ");
		int discard_idx = getUserDiscardIdx(player_input);
		
		// if user will discard a tile of discard_idx from their hand
		Tile out;
		out = this.hand.discardTile(discard_idx);
		
		// add Tse claimed tile to hand
		this.addToHand(t);
		
		// return Tile that was discarded from hand
		return out;
	}
	
	
	// show Hand
	void showHand() {
		this.hand.showHand();
	}
	
	
	// add a tile to hand
	void addToHand(Tile tile) {
		this.hand.addToHand(tile);
	}
	
	
	// discard tile from hand
	Tile discardTile(String player_input) {
		
		System.out.println("Discard tile with index: ");
		int discard_idx = getUserDiscardIdx(player_input);
		
		return this.hand.discardTile(discard_idx);
	}
	
	
	// Parse User input for discard idx
	private int getUserDiscardIdx(String player_input) {

		int discard_idx = -1;

		// try parse for a valid int
		try {
			discard_idx = Integer.parseInt(player_input);
		} catch (InputMismatchException ex){
			// in case not a valid integer idx
			discard_idx = -1;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			discard_idx = -1;
		}

		// check if int is valid in hand
		if (discard_idx >= this.hand.num_hidden || discard_idx < 0) {
			System.out.println("Please choose a valid discard_idx, " + player_input + " is invalid\n");
			discard_idx = -1;
		}

		return discard_idx;
	}
}



//TODO: these
// player scores hand's likeliness of producing a win, or hand's closeness to a win
// player also has an aim based on probability for different combinations of triples, pairs and sequences

