package mahjong_package;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

class Player {
	
	// player's turn to draw a tile
	PlayerState state;
	
	// Scanner for player interaction with game
	Scanner scan = new Scanner(System.in);
	
	// player's hand
	private Hand hand = new Hand();
	
	
	// constructor
	Player(int player_ID)
    {

        // assert ID is valid and assign
        if (!this.isValid(player_ID)) {
			System.out.print("Error: Player ID %d is not valid\n");
        	System.exit(0);
        }
        this.state = PlayerState.WAITING;
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
	
	
	// check for Kong given a potential tile - Four-of-a-kind.
	boolean kong(Tile t) {
		
		ArrayList<int[]> kongs = new ArrayList<int[]>();
		kongs = this.hand.checkKong(t);

		// if no Pong arrays in this ArrayList (size 0), return false
		if (kongs.size() == 0) {
			return false;
		}
		
		this.hand.showHand();
		
		//TODO: ask user would they like to Kong - assuming now that they would want to
		
		// reveal selected Kong
		System.out.println("Kong!\n");
		this.hand.addToHand(t);
		int[] hand_idx = new int[4];
		hand_idx[0] = kongs.get(0)[0];
		hand_idx[1] = kongs.get(0)[1];
		hand_idx[2] = kongs.get(0)[2];
		hand_idx[3] = this.hand.num_hidden-1;
		this.hand.revealTiles(hand_idx, 4);	
		
		return true;
	}
	
	
	// check for Pong given a potential tile - Three-of-a-kind or a sequence of three.
	boolean pong(Tile t) {

		ArrayList<int[]> pongs = new ArrayList<int[]>();
		pongs = this.hand.checkPong(t);

		// if no Pong arrays in this ArrayList (size 0), return false
		if (pongs.size() == 0) {
			return false;
		}
		
		// if Pong option(s), list options & ask user to choose Pong, or decline
		this.hand.showHand();
		System.out.println("You can pong for " + t.descriptor + ".\nPlease choose an option to pong with:");
		for (int i=0; i<pongs.size(); i++) {
			System.out.printf("\t%d. idx {%d %d}\n", i, pongs.get(i)[0], pongs.get(i)[1]);
		}
		System.out.printf("\t%d. Skip pong\n", pongs.size());
		
		//TODO: a while loop for a correct response. Currently defaults to skip Pong response.
		int resp;
		try {
			resp = Integer.parseInt(this.scan.nextLine());
		} catch (InputMismatchException ex){
			// in case not a valid integer idx
			resp = pongs.size();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			resp = pongs.size();
		}
		
		// if last idx, no Pong
		if (resp == pongs.size()) {
			return false;
		}
		
		// reveal selected Pong
		System.out.println("Pong!\n");
		this.hand.addToHand(t);
		int[] hand_idx = new int[3];
		hand_idx[0] = pongs.get(resp)[0];
		hand_idx[1] = pongs.get(resp)[1];
		hand_idx[2] = this.hand.num_hidden-1;
		this.hand.revealTiles(hand_idx, 3);	
		return true;
	}
	
	
	// a Tse has been called. Add tile and discard a chosen tile.
	Tile tse(Tile t) {
		
		// check which tile to discard
		System.out.println("Discard tile with index: ");
		int discard_idx = getUserDiscardIdx();
		
		// if user will discard a tile of discard_idx from their hand
		Tile out = new Tile();
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
	Tile discardTile() {
		
		this.hand.showHand();
		
		System.out.println("Discard tile with index: ");
		int discard_idx = getUserDiscardIdx();
		
		return this.hand.discardTile(discard_idx);
	}
	
	
	// scan User input for discard idx
	private int getUserDiscardIdx() {
		
		int discard_idx = -1;
		
		while (discard_idx >= this.hand.num_hidden || discard_idx < 0) {
			try {
				discard_idx = Integer.parseInt(this.scan.nextLine());
			} catch (InputMismatchException ex){
				// in case not a valid integer idx
				discard_idx = -1;
			} catch (NumberFormatException e) {
				e.printStackTrace();
				discard_idx = -1;
			}
			if (discard_idx >= this.hand.num_hidden || discard_idx < 0) {
				System.out.println("Please choose a valid discard_idx\n");
			}
		}
		
		return discard_idx;
	}
	
}



//TODO: these
// player scores hand's likeliness of producing a win, or hand's closeness to a win
// player also has an aim based on probability for different combinations of triples, pairs and sequences
