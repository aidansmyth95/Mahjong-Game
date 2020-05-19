package mahjong_package;

import java.util.ArrayList;
import java.util.InputMismatchException;

class Player {

	// player's hand
	private Hand hand = new Hand();

	private ArrayList<int[]> possible_pongs = new ArrayList<int[]>();
	private ArrayList<int[]> possible_kongs = new ArrayList<int[]>();

	private int chosen_idx;
	private int player_id;
	
	// constructor
	Player(int player_ID)
    {
        // assert ID is valid and assign
        if (!this.isValid(player_ID)) {
			System.out.print("Error: Player ID %" + player_ID + "is not valid\n");
        	System.exit(0);
        }
        this.player_id = player_ID;
		this.clearKongsPongs();
    }


    // reset pong/kong interactions
	private void clearKongsPongs() {
		// pong identified and waiting user response
		this.chosen_idx = -1;
	}

    
    // player ID valid
	private boolean isValid(int player_ID) {
		return player_ID < 4;
	}
	
	
	// create hand for player
	void createHand(Tile[] tiles_drawn) {
		this.hand.createHand(tiles_drawn);
	}



	// a Tse has been called. Add tile and discard a chosen tile.
	void tse(Tile t) {

		// add Tse claimed tile to hand
		this.addToHand(t);
	}


	// show Hand
	void showHand() {
		this.hand.showHiddenHand();
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
			ex.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

		// check if int is valid in hand
		if (discard_idx >= this.hand.num_hidden || discard_idx < 0) {
			System.out.println("Please choose a valid discard_idx, " + player_input + " is invalid\n");
			discard_idx = -1;
		}

		return discard_idx;
	}


	// check hand for Pong
	// check for Pong given a potential tile - Three-of-a-kind or a sequence of three.
	boolean checkHandPong(Tile t) {
		ArrayList<int[]> pongs;

		pongs = this.hand.checkPong(t);

		// if no Pong arrays in this ArrayList (size 0), return false
		if (pongs.size() > 0) {
			// if Pong option(s), list options & ask user to choose Pong, or decline
			System.out.println("Player " + this.player_id + ": Pong?");
			for (int i=0; i<pongs.size(); i++) {
				System.out.printf("\t%d. idx {%d %d}\n", i, pongs.get(i)[0], pongs.get(i)[1]);
				this.possible_pongs.add(pongs.get(i));
			}
			System.out.printf("\t%d. Skip pong\n", pongs.size());
			return true;
		}
		return false;
	}


	// check hand for Kong
	// check hand for kong, update options for kong, return true if chance for kong
	boolean checkHandKong(Tile t) {
		ArrayList<int[]> kongs;
		kongs = this.hand.checkKong(t);

		// if no Pong arrays in this ArrayList (size 0), return false
		if (kongs.size() > 0) {
			this.possible_kongs.addAll(kongs);
			return true;
		}
		return false;
	}


	// check hand for MJ
	boolean checkHandMahjong(Tile t) {
		this.showHand();
		return this.hand.checkMahjong(t);
	}


	// check user response for Pong
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
			this.chosen_idx = resp;
			return true;
		}
		return false;
	}


	// check user response for Kong
	boolean checkUserKong(String player_input) {

		return player_input.equals("1");
	}


	// check user response for MJ
	boolean checkUserMahjong(String player_input) {

		return player_input.equals("1");

	}


	// check for win
	//TODO: use this in Gameplay
	void Mahjong(Tile t) {
		// if Mahjong, add tile to hand and reveal all tiles
		this.hand.addToHand(t);
		this.hand.revealTiles();
	}


	void kong(Tile t) {
		this.hand.addToHand(t);
		int[] hand_idx = new int[4];
		hand_idx[0] = this.possible_kongs.get(0)[0];
		hand_idx[1] = this.possible_kongs.get(0)[1];
		hand_idx[2] = this.possible_kongs.get(0)[2];
		hand_idx[3] = this.hand.num_hidden-1;
		this.hand.revealTiles(hand_idx, 4);
	}


	void pong(Tile t) {
		int[] hand_idx = new int[3];
		this.hand.addToHand(t);
		hand_idx[0] = this.possible_pongs.get(this.chosen_idx)[0];
		hand_idx[1] = this.possible_pongs.get(this.chosen_idx)[1];
		hand_idx[2] = this.hand.num_hidden-1;
		this.hand.revealTiles(hand_idx, 3);
	}


	// get arraylist of Revealed tile descriptors
	ArrayList<String> getRevealedDescriptors() { return this.hand.getRevealedDescriptors(); }


	// get arraylist of Hidden tile descriptors
	ArrayList<String> getHiddenDescriptors() { return this.hand.getHiddenDescriptors(); }


	//TODO: a separate test vector class
	Tile[] createTruePongTestVectorHand(int test_num) {
		Tile[] test_vector = new Tile[14];
		switch (test_num) {
			// tests three of a kind hand
			case 0:
				test_vector[0] = new Bonus(1,1,1); //Spring
				test_vector[1] = new Bonus(1,1,2); //Spring
				test_vector[2] = new Suits(1,1,1);
				test_vector[3] = new Suits(2,1,1);
				test_vector[4] = new Suits(3,1,1);
				test_vector[5] = new Suits(1,7,1);
				test_vector[6] = new Suits(1,3,1);
				test_vector[7] = new Suits(2,3,1);
				test_vector[8] = new Suits(3,3,1);
				test_vector[9] = new Suits(1,5,1);
				test_vector[10] = new Suits(1,9,1);
				test_vector[11] = new Suits(2,9,1);
				test_vector[12] = new Suits(3,9,1);
				test_vector[13] = new Bonus(1,1,3); //Spring
				break;

			case 1:
				test_vector[0] = new Bonus(1,1,1);
				test_vector[1] = new Bonus(1,2,2);
				test_vector[2] = new Suits(1,1,1); //1
				test_vector[3] = new Suits(2,1,1);
				test_vector[4] = new Suits(3,1,1);
				test_vector[5] = new Suits(1,7,1);
				test_vector[6] = new Suits(1,2,1); //2
				test_vector[7] = new Suits(2,3,1);
				test_vector[8] = new Suits(3,3,1);
				test_vector[9] = new Suits(1,5,1);
				test_vector[10] = new Suits(1,9,1);
				test_vector[11] = new Suits(2,9,1);
				test_vector[12] = new Suits(3,9,1);
				test_vector[13] = new Suits(1,3,1); //3
				break;

			default:
				createTruePongTestVectorHand(0);
				break;
		}

		return test_vector;
	}


	Tile[] createFalsePongTestVectorHand(int test_num) {
		Tile[] test_vector = new Tile[14];
		switch (test_num) {
			// tests three of a kind hand
			case 0:
				test_vector[0] = new Bonus(1,1,1); //Spring
				test_vector[1] = new Bonus(1,1,2); //Spring
				test_vector[2] = new Bonus(1,1,3); //Spring
				test_vector[3] = new Suits(2,1,1);
				test_vector[4] = new Suits(3,1,1);
				test_vector[5] = new Suits(1,7,1);
				test_vector[6] = new Suits(1,3,1);
				test_vector[7] = new Suits(2,3,1);
				test_vector[8] = new Suits(3,3,1);
				test_vector[9] = new Suits(1,5,1);
				test_vector[10] = new Suits(1,9,1);
				test_vector[11] = new Suits(2,9,1);
				test_vector[12] = new Suits(3,9,1);
				test_vector[13] = new Suits(1,1,1);
				break;

			case 1:
				test_vector[0] = new Bonus(1,1,1);
				test_vector[1] = new Bonus(1,2,2);
				test_vector[2] = new Suits(1,1,1); //1
				test_vector[3] = new Suits(2,1,1);
				test_vector[4] = new Suits(3,1,1);
				test_vector[5] = new Suits(1,7,1);
				test_vector[6] = new Suits(1,2,1); //2
				test_vector[7] = new Suits(2,3,1);
				test_vector[8] = new Suits(3,3,1);
				test_vector[9] = new Suits(1,5,1);
				test_vector[10] = new Suits(1,9,1);
				test_vector[11] = new Suits(2,9,1);
				test_vector[12] = new Suits(1,3,1); //3
				test_vector[13] = new Suits(3,9,1);
				break;

			default:
				createTruePongTestVectorHand(0);
				break;
		}

		return test_vector;
	}



	Tile[] createTrueKongTestVectorHand(int test_num) {
		Tile[] test_vector = new Tile[14];
		switch (test_num) {
			// tests three of a kind hand
			case 0:
				test_vector[0] = new Bonus(1,1,1); //Spring
				test_vector[1] = new Bonus(1,1,2); //Spring
				test_vector[2] = new Bonus(1,1,3); //Spring
				test_vector[3] = new Suits(2,1,1);
				test_vector[4] = new Suits(3,1,1);
				test_vector[5] = new Suits(1,7,1);
				test_vector[6] = new Suits(1,3,1);
				test_vector[7] = new Suits(2,3,1);
				test_vector[8] = new Suits(3,3,1);
				test_vector[9] = new Suits(1,5,1);
				test_vector[10] = new Suits(1,9,1);
				test_vector[11] = new Suits(2,9,1);
				test_vector[12] = new Suits(3,9,1);
				test_vector[13] = new Bonus(1,1,0); //Spring
				break;

			case 1:
				test_vector[0] = new Bonus(1,1,1);
				test_vector[1] = new Bonus(1,2,2);
				test_vector[2] = new Suits(1,1,1); //1
				test_vector[3] = new Suits(1,1,2); //2
				test_vector[4] = new Suits(3,1,1);
				test_vector[5] = new Suits(1,7,1);
				test_vector[6] = new Suits(1,2,1);
				test_vector[7] = new Suits(2,3,1);
				test_vector[8] = new Suits(3,3,1);
				test_vector[9] = new Suits(1,5,1);
				test_vector[10] = new Suits(1,9,1);
				test_vector[11] = new Suits(2,9,1);
				test_vector[12] = new Suits(1,1,3); //3
				test_vector[13] = new Suits(1,1,0); //4 //TODO: ID zero indexing but type and rank do not. Fix this
				break;

			default:
				createTrueKongTestVectorHand(0);
				break;
		}

		return test_vector;
	}


	Tile[] createFalseKongTestVectorHand(int test_num) {
		Tile[] test_vector = new Tile[14];
		switch (test_num) {
			// tests three of a kind hand
			case 0:
				test_vector[0] = new Bonus(1,1,1); //Spring
				test_vector[1] = new Bonus(1,1,2); //Spring
				test_vector[2] = new Bonus(1,1,3); //Spring
				test_vector[3] = new Bonus(1,1,0); //Spring
				test_vector[4] = new Suits(3,1,1);
				test_vector[5] = new Suits(1,7,1);
				test_vector[6] = new Suits(1,3,1);
				test_vector[7] = new Suits(2,3,1);
				test_vector[8] = new Suits(3,3,1);
				test_vector[9] = new Suits(1,5,1);
				test_vector[10] = new Suits(1,9,1);
				test_vector[11] = new Suits(2,9,1);
				test_vector[12] = new Suits(3,9,1);
				test_vector[13] = new Suits(3,9,2);
				break;

			case 1:
				test_vector[0] = new Bonus(1,1,1);
				test_vector[1] = new Bonus(1,2,2);
				test_vector[2] = new Suits(1,1,1); //1
				test_vector[3] = new Suits(1,1,2); //2
				test_vector[4] = new Suits(1,1,0); //4 //TODO: ID zero indexing but type and rank do not. Fix this
				test_vector[5] = new Suits(1,7,1);
				test_vector[6] = new Suits(1,2,1);
				test_vector[7] = new Suits(2,3,1);
				test_vector[8] = new Suits(3,3,1);
				test_vector[9] = new Suits(1,5,1);
				test_vector[10] = new Suits(1,9,1);
				test_vector[11] = new Suits(2,9,1);
				test_vector[12] = new Suits(1,1,3); //3
				test_vector[13] = new Suits(3,1,1);
				break;

			default:
				createFalseKongTestVectorHand(0);
				break;
		}

		return test_vector;
	}


	Tile[] createTrueMahjongTestVectorHand(int test_num) {
		Tile[] test_vector = new Tile[14];
		switch (test_num) {
			// tests three of a kind hand
			case 0:
				test_vector[0] = new Bonus(1,1,1); //Spring
				test_vector[1] = new Bonus(1,1,2); //Spring
				test_vector[2] = new Bonus(1,2,0); //Summer
				test_vector[3] = new Bonus(1,2,1); //Summer
				test_vector[4] = new Bonus(1,2,2); //Summer
				test_vector[5] = new Bonus(1,3,0); //Autumn
				test_vector[6] = new Bonus(1,3,1); //Autumn
				test_vector[7] = new Bonus(1,3,2); //Autumn
				test_vector[8] = new Suits(3,3,1); //3
				test_vector[9] = new Suits(3,4,1); //4
				test_vector[10] = new Suits(3,5,1); //5
				test_vector[11] = new Suits(2,7,1); //7
				test_vector[12] = new Bonus(1,1,0); //Spring
				test_vector[13] = new Suits(2,7,1); //7
				break;

			case 1:
				test_vector[0] = new Bonus(1,1,1); //Spring
				test_vector[1] = new Bonus(1,1,2); //Spring
				test_vector[2] = new Bonus(1,2,0); //Summer
				test_vector[3] = new Bonus(1,2,1); //Summer
				test_vector[4] = new Bonus(1,2,2); //Summer
				test_vector[5] = new Bonus(1,3,0); //Autumn
				test_vector[6] = new Bonus(1,3,1); //Autumn
				test_vector[7] = new Bonus(1,3,2); //Autumn
				test_vector[8] = new Suits(3,1,1); //1
				test_vector[9] = new Suits(3,2,1); //2
				test_vector[10] = new Suits(3,3,1); //3
				test_vector[11] = new Suits(2,7,1); //7
				test_vector[12] = new Bonus(1,1,0); //Spring
				test_vector[13] = new Suits(2,7,1); //7
				break;

			default:
				createTrueMahjongTestVectorHand(0);
				break;
		}

		return test_vector;
	}


}



//TODO: these
// player scores hand's likeliness of producing a win, or hand's closeness to a win
// player also has an aim based on probability for different combinations of triples, pairs and sequences

