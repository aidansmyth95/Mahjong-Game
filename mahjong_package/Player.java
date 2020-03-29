package mahjong_package;

import java.util.ArrayList;
import java.util.Scanner;

public class Player {
	
	// player's turn to draw a tile
	public PlayerState state;
	
	// Scanner for player interaction with game
	public Scanner scan = new Scanner(System.in);
	
	// player's hand
	private Hand hand = new Hand();
	
	
	// constructor
    public Player(int player_ID)
    {

        // assert ID is valid and assign
        if (this.isValid(player_ID) != true) {
        	System.out.printf("Error: Player ID %d is not valid\n");
        	System.exit(0);
        }
        this.state = PlayerState.WAITING;
    }
    
    
    // player ID valid
	public boolean isValid(int player_ID) {
		if (player_ID < 4) {
			return true;
		}
		else {
			return false;
		}
	}
	
	
	// create hand for player
	public void createHand(Tile[] tiles_drawn) {
		this.hand.createHand(tiles_drawn);
	}
	
	
	// check for win
	public boolean Mahjong(Tile t) {
		
		boolean mj = false;
		mj = this.hand.checkMahjong(t);
		
		// if Mahjong, add tile to hand and reveal all tiles
		if (mj == true) {
			this.hand.addToHand(t);
			this.hand.revealTiles();
			return true;
		}
		
		return false;
	}
	
	
	// check for Pong given a potential tile
	public boolean pong(Tile t) {
		ArrayList<int[]> pongs = new ArrayList<int[]>();
		pongs = this.hand.checkPong(t);
		
		// if no Pong arrays in this ArrayList (size 0), return false
		if (pongs.size() == 0) {
			return false;
		}
		
		// if Pong option(s), list options & ask user to choose pong, or decline
		this.hand.showHand();
		System.out.println("You can pong. Please choose an option:");
		for (int i=0; i<pongs.size(); i++) {
			System.out.printf("\t%d. idx {%d %d %d}\n", i, pongs.get(i)[0], pongs.get(i)[1], pongs.get(i)[2]);
		}
		System.out.printf("\t%d. Skip pong\n", pongs.size());
		int resp = this.scan.nextInt();
		
		// if last idx, no Pong
		if (resp == pongs.size()) {
			return false;
		}
		
		// reveal selected Pong
		System.out.println("Pong!\n");
		this.hand.revealTiles(pongs.get(resp), 3);	
		return true;
	}
	
	
	// a tse has been called. Add tile and discard a chosen tile.
	public Tile tse(Tile t) {
		
		// check which tile to discard
		System.out.println("Discard tile with index: ");
		int discard_idx = -1;
		
		while (discard_idx >= this.hand.num_hidden || discard_idx < 0) {
			discard_idx = this.scan.nextInt();
			if (discard_idx >= this.hand.num_hidden || discard_idx < 0) {
				System.out.println("Please choose a valid discard_idx\n");
			}
		}
		
		// if user will discard a tile of discard_idx from their hand
		Tile out = new Tile();
		out = this.discardTile(discard_idx);
		
		// add Tse claimed tile to hand
		this.addToHand(t);
		
		// return Tile that was discarded from hand
		return out;
	}
	
	
	// show Hand
	public void showHand() {
		this.hand.showHand();
	}
	
	
	// add a tile to hand
	public void addToHand(Tile tile) {
		this.hand.addToHand(tile);
	}
	
	
	// discard tile from hand
	public Tile discardTile(int idx) {
		return this.hand.discardTile(idx);
	}
	
}


//TODO: these
// a player has a hand of hand_size tiles at start

// a player can draw a tile from the deck. When drawing player can choose to keep or discard tile

// player scores hand's likeliness of producing a win, or hand's closeness to a win

// player also has an aim based on probability for different combinations of triples, pairs and sequences
