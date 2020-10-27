package mahjong_package;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;

public class Player {

	private String playerUname = "";
	private String playerUid = "";
	private Boolean chowAvailable = false; 		// Users ability to chow
	private Boolean pongAvailable = false; 		// Users ability to pong
	private Boolean kongAvailable = false; 		// Users ability to kong
	private Boolean mahjongAvailable = false; 	// Users ability to mahjong
	private Boolean requestResponse = false; 	// Users chance to respond
	private String playerResponse = "";	// Users responses
	private Hand hand = new Hand();		// player's hand
	private PossibleChowsPongsKongs ppk = new PossibleChowsPongsKongs();
	private int chosenIdx = -1;
	private int playerIdx = -1;
	private Boolean playerPlaying = false;
	private String gameMessage = "";

	public String getPlayerUid() { return this.playerUid; }
	public void setPlayerUid(String uid) { this.playerUid = uid; }
	public String getPlayerUname() { return this.playerUname; }
	public void setPlayerUname(String uname) { this.playerUname = uname; }
	public Boolean getChowAvailable() { return this.chowAvailable; }
	public void setChowAvailable(boolean b) { this.chowAvailable = b; }
	public Boolean getPongAvailable() { return this.pongAvailable; }
	public void setPongAvailable(boolean b) { this.pongAvailable = b; }
	public void setKongAvailable(boolean b) { this.kongAvailable = b; }
	public Boolean getKongAvailable() { return this.kongAvailable; }
	public void setMahjongAvailable(boolean b) { this.mahjongAvailable = b; }
	public Boolean getMahjongAvailable() { return this.mahjongAvailable; }
	public Boolean getRequestResponse() { return this.requestResponse; }
	public void setRequestResponse(boolean b) { this.requestResponse = b; }
	public void setPlayerResponse(String s) { this.playerResponse = s; }
	public String getPlayerResponse() { return this.playerResponse; }
	public Hand getHand() { return this.hand; }
	public void setHand(Hand hand) { this.hand = hand; }
	public PossibleChowsPongsKongs getPpk() { return this.ppk; }
	public void setPpk(PossibleChowsPongsKongs ppk) { this.ppk = ppk; }
	public Integer getChosenIdx() { return this.chosenIdx; }
	public void setChosenIdx(int c) { this.chosenIdx = c; }
	public Integer getPlayerIdx() { return this.playerIdx; }
	public void setPlayerIdx(int p) { this.playerIdx = p; }
	public Boolean getPlayerPlaying() { return this.playerPlaying; }
	public void setPlayerPlaying(boolean p) { this.playerPlaying = p; }
	public String getGameMessage() { return this.gameMessage; }
	public void setGameMessage(String s) { this.gameMessage = s; }

	private Player() {
		this.clearHand();
	}

	// this constructor is only used when we are adding tiles and know that they are not flowers - e.g. test vectors
	Player(String uname, String uid, int player_ID, ArrayList<Tile> start_tiles)
    {
		this();
		// assert ID is valid and assign
        if (!this.isValid(player_ID)) {
			Log.e("Player","Player: Player ID %" + player_ID + "is not valid\n");
        } else {
			this.playerUname = uname;
			this.playerUid = uid;
			this.playerIdx = player_ID;
			HandStatus hs = this.createHand(start_tiles);
			if (hs != HandStatus.ADD_SUCCESS) {
				Log.e("Player", "Issue creating player hand for overloaded constructor");
				Log.e("Player", "Hand status is " + hs + " for player " + player_ID);
			}
		}
	}

	// return latest added descriptor
	public String getLatestFlowersCollectedDescriptor() {
		ArrayList<Tile> flowers = this.hand.getFlowersCollected();
		if (flowers.isEmpty()) {
			return "No tile";
		} else {
			return flowers.get(flowers.size()-1).getDescriptor();
		}
	}

	public int getHiddenHandCount() { return this.hand.getHiddenHandSize(); }

	public int getFlowersCount() { return this.hand.getFlowersCollectedSize(); }

	public ArrayList<Tile> getFlowersCollected() {
		return this.hand.getFlowersCollected();
	}

	// clear the hand
	void clearHand() { this.hand.clearHand(); }

	// player ID valid
	private boolean isValid(int player_ID) {
		return player_ID < 4;
	}

	// create hand for player
	public HandStatus createHand(ArrayList<Tile> tiles_drawn) { return this.hand.createHand(tiles_drawn); }

	// a Tse has been called. Add tile and discard a chosen tile.
	void tse(Tile t) { this.addToHand(t); }

	// show Hand
	void showHiddenHand() {
		this.hand.showHiddenHand();
	}

	// add a tile to hand
	HandStatus addToHand(Tile tile) { return this.hand.addToHand(tile); }

	// discard tile from hand
	Tile discardTile(String player_input) {
		int discard_idx = getUserDiscardIdx(player_input);
		return this.hand.discardTile(discard_idx);
	}

	// Parse User input for discard idx
	int getUserDiscardIdx(String player_input) {
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
		if (discard_idx >= this.hand.getHiddenHandSize() || discard_idx < 0) {
			System.out.println("Please choose a valid discard_idx, " + player_input + " is invalid\n");
			discard_idx = -1;
		}
		return discard_idx;
	}

	// check hand for Pong
	// check for Pong given a potential tile - Three-of-a-kind or a sequence of three.
	boolean checkHandChow(Tile t) {
		this.ppk.clearChows();
		//this.possiblePongs.clear();
		ArrayList<int[]> chows;
		chows = this.hand.checkChow(t);
		// if no Pong arrays in this ArrayList (size 0), return false
		if (chows.size() > 0) {
			// if Pong option(s), list options & ask user to choose Pong, or decline
			System.out.println("Player " + this.playerIdx + ": Chow?");
			for (int i=0; i<chows.size(); i++) {
				System.out.printf("\t%d. idx {%d %d}\n", i, chows.get(i)[0], chows.get(i)[1]);
				//this.possiblePongs.add(pongs.get(i));
				String stringVal = Arrays.toString(chows.get(i));
				this.ppk.setPossibleChow(i, stringVal);
			}
			System.out.printf("\t%d. Skip pong\n", chows.size());
			return true;
		}
		return false;
	}

	// check hand for kong, update options for kong, return true if chance for kong
	boolean checkHandKong(Tile t) {
		this.ppk.clearKongs();
		ArrayList<int[]> kongs;
		kongs = this.hand.checkKong(t);
		// if no Kong arrays in this ArrayList (size 0), return false
		if (kongs.size() > 0) {
			String stringVal = Arrays.toString(kongs.get(0));
			this.ppk.setPossibleKong(0, stringVal);
			return true;
		}
		return false;
	}

	// check hand for kong, update options for kong, return true if chance for kong
	boolean checkHandPong(Tile t) {
		this.ppk.clearPongs();
		ArrayList<int[]> pongs;
		pongs = this.hand.checkPong(t);
		// if no Pong arrays in this ArrayList (size 0), return false
		if (pongs.size() > 0) {
			String stringVal = Arrays.toString(pongs.get(0));
			this.ppk.setPossiblePong(0, stringVal);
			return true;
		}
		return false;
	}

	// check hand for MJ
	boolean checkHandMahjong(Tile t) {
		return this.hand.checkMahjong(t);
	}

	// check user response for Pong
	boolean checkUserChow(String player_input) {
		int resp = -1;
		try {
			resp = Integer.parseInt(player_input);
		} catch (InputMismatchException e) {
			// in case not a valid integer idx
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		// if last idx, no chow...
		if (resp < this.ppk.getNumChows() && resp >= 0) {
			this.chosenIdx = resp;
			return true;
		}
		return false;
	}

	// check user response for Kong
	boolean checkUserPong(String player_input) {
		return player_input.equals("1");
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
	public void Mahjong(Tile t) {
		// if Mahjong, add tile to hand and reveal all tiles
		this.hand.addToHand(t);
		this.hand.revealAllHiddenHandTiles();
	}

	void kong(Tile t) {
		this.hand.addToHand(t);
		int[] hand_idx = new int[4];
		// parse from string to get array elements
		hand_idx[0] = Integer.parseInt(this.ppk.getPossibleKong(0));
		hand_idx[1] = Integer.parseInt(this.ppk.getPossibleKong(1));
		hand_idx[2] = Integer.parseInt(this.ppk.getPossibleKong(2));
		hand_idx[3] = this.hand.getHiddenHandSize()-1;
		this.hand.revealIndexedHiddenTiles(hand_idx, 4);
	}

	// We will transform string array to int array
	void pong(Tile t) {
		this.hand.addToHand(t);
		int[] hand_idx = new int[3];
		// parse from string to get array elements
		hand_idx[0] = Integer.parseInt(this.ppk.getPossiblePong(0));
		hand_idx[1] = Integer.parseInt(this.ppk.getPossiblePong(1));
		hand_idx[2] = this.hand.getHiddenHandSize() - 1;
		this.hand.revealIndexedHiddenTiles(hand_idx, 3);
	}

	void chow(Tile t) {
		this.hand.addToHand(t);
		int[] hand_idx = new int[3];
		int[] chows;
		chows = fromString(this.ppk.getPossibleChow(this.chosenIdx));
		hand_idx[0] = chows[0];
		hand_idx[1] = chows[1];
		hand_idx[2] = this.hand.getHiddenHandSize()-1; // the last idx where the new tile to pong will be
		this.hand.revealIndexedHiddenTiles(hand_idx, 3);
	}

	// get arraylist of Revealed tile descriptors
	ArrayList<String> getRevealedDescriptors() { return this.hand.getRevealedDescriptors(); }

	// get arraylist of Hidden tile descriptors
	ArrayList<String> getHiddenDescriptors() { return this.hand.getHiddenDescriptors(); }


	//TODO: test vector class

	/*

		Test vectors - Important that last tile is part of the pong/kong/chow/mahjong

	 */

	ArrayList<Tile> createTrueChowTestVectorHand(int test_num) {
		ArrayList<Tile> test_vector = new ArrayList<>();
		switch (test_num) {
			// tests three of a kind hand
			case 0:
				test_vector.add(0,new Bonus(1,1,1));
				test_vector.add(1, new Bonus(1,2,2));
				test_vector.add(2, new Suits(1,1,1)); // Bamboo 1 #1
				test_vector.add(3, new Suits(2,1,1));
				test_vector.add(4, new Suits(3,1,1));
				test_vector.add(5, new Suits(1,7,1));
				test_vector.add(6, new Suits(1,2,1)); // Bamboo 2 #1
				test_vector.add(7, new Suits(2,3,1));
				test_vector.add(8, new Suits(3,3,1));
				test_vector.add(9, new Suits(1,5,1));
				test_vector.add(10, new Suits(1,9,1));
				test_vector.add(11, new Suits(2,9,1));
				test_vector.add(12, new Suits(3,9,1));
				test_vector.add(13, new Suits(1,3,1)); // Bamboo 3 #1
				break;
			case 1:
				test_vector.add(0, new Bonus(1,1,1));
				test_vector.add(1, new Bonus(1,2,2));
				test_vector.add(2, new Suits(3,9,1));
				test_vector.add(3, new Suits(2,1,1));
				test_vector.add(4, new Suits(3,1,0));
				test_vector.add(5, new Suits(1,7,1));
				test_vector.add(6, new Suits(2,7,1)); // Dots 7 #1
				test_vector.add(7, new Suits(2,3,1));
				test_vector.add(8, new Suits(3,3,1));
				test_vector.add(9, new Suits(1,5,1));
				test_vector.add(10, new Suits(1,9,1));
				test_vector.add(11, new Suits(2,9,1));
				test_vector.add(12, new Suits(2,6,1)); // Dots 6 #1
				test_vector.add(13, new Suits(2,8,1)); // Dots 8 #1
				break;
			default:
				createTrueChowTestVectorHand(0);
				break;
		}
		return test_vector;
	}

	ArrayList<Tile> createFalseChowTestVectorHand(int test_num) {
		ArrayList<Tile> test_vector = new ArrayList<>();
		switch (test_num) {
			case 0:	// nothing
				test_vector.add(0, new Bonus(1,1,1)); // Spring #1
				test_vector.add(1, new Bonus(1,1,2)); // Spring #2
				test_vector.add(2, new Bonus(1,1,3)); // Summer #3
				test_vector.add(3, new Bonus(1,1,0)); // Summer #0
				test_vector.add(4, new Suits(3,1,1));
				test_vector.add(5, new Suits(1,7,1));
				test_vector.add(6, new Suits(1,3,1));
				test_vector.add(7, new Suits(2,3,1));
				test_vector.add(8, new Suits(3,3,1));
				test_vector.add(9, new Suits(1,5,1));
				test_vector.add(10, new Suits(1,9,1));
				test_vector.add(11, new Suits(2,9,1));
				test_vector.add(12, new Suits(3,9,1));
				test_vector.add(13, new Suits(1,1,1));
				break;
			case 1:	// Pong
				test_vector.add(0, new Bonus(1,1,1));
				test_vector.add(1, new Bonus(1,2,2));
				test_vector.add(2, new Suits(1,1,1)); // Bamboo 1 #1
				test_vector.add(3, new Suits(2,1,1));
				test_vector.add(4, new Suits(3,1,1));
				test_vector.add(5, new Suits(1,7,1));
				test_vector.add(6, new Suits(1,1,2)); // Bamboo 1 #2
				test_vector.add(7, new Suits(2,3,1));
				test_vector.add(8, new Suits(3,3,1));
				test_vector.add(9, new Suits(1,5,1));
				test_vector.add(10, new Suits(1,9,1));
				test_vector.add(11, new Suits(2,9,1));
				test_vector.add(12, new Suits(3,9,1));
				test_vector.add(13, new Suits(1,1,3)); // Bamboo 1 #3
				break;
			default:
				createTruePongTestVectorHand(0);
				break;
		}
		return test_vector;
	}

	// a separate test vector class
	ArrayList<Tile> createTruePongTestVectorHand(int test_num) {
		ArrayList<Tile> test_vector = new ArrayList<>();
		switch (test_num) {
			// tests three of a kind hand
			case 0:
				test_vector.add(0, new Bonus(1,1,1)); //Spring #1
				test_vector.add(1, new Bonus(1,1,2)); //Spring #2
				test_vector.add(2, new Suits(1,1,1));
				test_vector.add(3, new Suits(2,1,1));
				test_vector.add(4, new Suits(3,1,1));
				test_vector.add(5, new Suits(1,7,1));
				test_vector.add(6, new Suits(1,3,1));
				test_vector.add(7, new Suits(2,3,1));
				test_vector.add(8, new Suits(3,3,1));
				test_vector.add(9, new Suits(1,5,1));
				test_vector.add(10, new Suits(1,9,1));
				test_vector.add(11, new Suits(2,9,1));
				test_vector.add(12, new Suits(3,9,1));
				test_vector.add(13, new Bonus(1,1,3)); //Spring #3
				break;
			case 1:
				test_vector.add(0, new Bonus(1,1,1)); // Spring #1
				test_vector.add(1, new Bonus(1,1,2)); // Spring #2
				test_vector.add(2, new Bonus(1,1,3)); // Spring #3
				test_vector.add(3, new Suits(2,1,1));
				test_vector.add(4, new Suits(3,1,1));
				test_vector.add(5, new Suits(1,7,1));
				test_vector.add(6, new Suits(1,3,1));
				test_vector.add(7, new Suits(2,3,1));
				test_vector.add(8, new Suits(3,3,1));
				test_vector.add(9, new Suits(1,5,1));
				test_vector.add(10, new Suits(1,9,1));
				test_vector.add(11, new Suits(1,1,3)); // Bamboo 1 - #3
				test_vector.add(12, new Suits(1,1,2)); // Bamboo 1 - #2
				test_vector.add(13, new Suits(1,1,1)); // Bamboo 1 - #1
				break;
			default:
				createTruePongTestVectorHand(0);
				break;
		}
		return test_vector;
	}

	ArrayList<Tile> createFalsePongTestVectorHand(int test_num) {
		ArrayList<Tile> test_vector = new ArrayList<>();
		switch (test_num) {
			// tests three of a kind hand
			case 0:	// kong
				test_vector.add(0, new Bonus(1,1,1)); // Spring #1
				test_vector.add(1, new Bonus(1,1,2)); // Spring #2
				test_vector.add(2, new Bonus(1,1,3)); // Spring #3
				test_vector.add(3, new Bonus(1,1,0)); // Spring #4
				test_vector.add(4, new Suits(3,1,1));
				test_vector.add(5, new Suits(1,7,1));
				test_vector.add(6, new Suits(1,3,1));
				test_vector.add(7, new Suits(2,3,1));
				test_vector.add(8, new Suits(3,3,1));
				test_vector.add(9, new Suits(1,5,1));
				test_vector.add(10, new Suits(1,9,1));
				test_vector.add(11, new Suits(2,9,1));
				test_vector.add(12, new Suits(3,9,1));
				test_vector.add(13, new Suits(1,1,1));
				break;
			case 1:	// Chow
				test_vector.add(0, new Bonus(1,1,1));
				test_vector.add(1, new Bonus(1,2,2));
				test_vector.add(2, new Suits(1,1,1)); // Bamboo 1 #1
				test_vector.add(3, new Suits(2,1,1));
				test_vector.add(4, new Suits(3,1,1));
				test_vector.add(5, new Suits(1,7,1));
				test_vector.add(6, new Suits(1,2,1)); // Bamboo 2 #1
				test_vector.add(7, new Suits(2,3,1));
				test_vector.add(8, new Suits(3,3,1));
				test_vector.add(9, new Suits(1,5,1));
				test_vector.add(10, new Suits(1,9,1));
				test_vector.add(11, new Suits(2,9,1));
				test_vector.add(12, new Suits(1,3,1)); // Bamboo 3 #1
				test_vector.add(13, new Suits(3,9,1));
				break;
			default:
				createTruePongTestVectorHand(0);
				break;
		}
		return test_vector;
	}

	ArrayList<Tile> createTrueKongTestVectorHand(int test_num) {
		ArrayList<Tile> test_vector = new ArrayList<>();
		switch (test_num) {
			// tests three of a kind hand
			case 0:
				test_vector.add(0, new Bonus(1,1,1)); //Spring
				test_vector.add(1, new Bonus(1,1,2)); //Spring
				test_vector.add(2, new Bonus(1,1,3)); //Spring
				test_vector.add(3, new Suits(2,1,1));
				test_vector.add(4, new Suits(3,1,1));
				test_vector.add(5, new Suits(1,7,1));
				test_vector.add(6, new Suits(1,3,1));
				test_vector.add(7, new Suits(2,3,1));
				test_vector.add(8, new Suits(3,3,1));
				test_vector.add(9, new Suits(1,5,1));
				test_vector.add(10, new Suits(1,9,1));
				test_vector.add(11, new Suits(2,9,1));
				test_vector.add(12, new Suits(3,9,1));
				test_vector.add(13, new Bonus(1,1,0)); //Spring
				break;
			case 1:
				test_vector.add(0, new Bonus(1,2,1)); //Summer
				test_vector.add(1, new Bonus(1,2,2)); //Summer
				test_vector.add(2, new Bonus(1,2,3)); //Summer
				test_vector.add(3, new Suits(3,9,2));
				test_vector.add(4, new Suits(3,1,1));
				test_vector.add(5, new Suits(1,7,1));
				test_vector.add(6, new Suits(1,3,1));
				test_vector.add(7, new Suits(2,3,1));
				test_vector.add(8, new Suits(3,3,1));
				test_vector.add(9, new Suits(1,5,1));
				test_vector.add(10, new Suits(1,9,1));
				test_vector.add(11, new Suits(2,9,1));
				test_vector.add(12, new Suits(3,9,1));
				test_vector.add(13, new Bonus(1,2,0)); //Summer
				break;
			default:
				createTrueKongTestVectorHand(0);
				break;
		}
		return test_vector;
	}

	ArrayList<Tile> createFalseKongTestVectorHand(int test_num) {
		ArrayList<Tile> test_vector = new ArrayList<>();
		switch (test_num) {
			// tests three of a kind hand
			case 0:	//TODO: thinks this is a kong
				test_vector.add(0, new Bonus(1,1,1));
				test_vector.add(1, new Bonus(1,2,2));
				test_vector.add(2, new Suits(1,1,1)); // Bamboo 1 #1
				test_vector.add(3, new Suits(1,1,2)); // Bamboo 1 #2
				test_vector.add(4, new Suits(1,1,0)); // Bamboo 4 #4
				test_vector.add(5, new Suits(1,7,1));
				test_vector.add(6, new Suits(1,2,1));
				test_vector.add(7, new Suits(2,3,1));
				test_vector.add(8, new Suits(3,3,1));
				test_vector.add(9, new Suits(1,5,1));
				test_vector.add(10, new Suits(1,9,1));
				test_vector.add(11, new Suits(2,9,1));
				test_vector.add(12, new Suits(1,1,3)); // Bamboo 1 #3
				test_vector.add(13, new Suits(3,1,1));
				break;
			case 1:
				test_vector.add(0, new Bonus(1,1,1)); //1
				test_vector.add(1, new Bonus(1,2,2));
				test_vector.add(2, new Suits(1,1,1));
				test_vector.add(3, new Bonus(1,1,2)); //2
				test_vector.add(4, new Bonus(1,1,0)); //0
				test_vector.add(5, new Suits(1,7,1));
				test_vector.add(6, new Suits(1,2,1));
				test_vector.add(7, new Suits(2,3,1));
				test_vector.add(8, new Suits(3,3,1));
				test_vector.add(9, new Suits(1,5,1));
				test_vector.add(10, new Suits(1,9,1));
				test_vector.add(11, new Suits(2,9,1));
				test_vector.add(12, new Bonus(1,1,3)); //3
				test_vector.add(13, new Suits(3,1,1));
				break;
			default:
				createFalseKongTestVectorHand(0);
				break;
		}
		return test_vector;
	}

	ArrayList<Tile> createTrueMahjongTestVectorHand(int test_num) {
		ArrayList<Tile> test_vector = new ArrayList<>();
		switch (test_num) {
			// tests three of a kind hand
			case 0:
				test_vector.add(0, new Bonus(1,1,1)); //Spring
				test_vector.add(1, new Bonus(1,1,2)); //Spring
				test_vector.add(2, new Bonus(1,2,0)); //Summer
				test_vector.add(3, new Bonus(1,2,1)); //Summer
				test_vector.add(4, new Bonus(1,2,2)); //Summer
				test_vector.add(5, new Bonus(1,3,0)); //Autumn
				test_vector.add(6, new Bonus(1,3,1)); //Autumn
				test_vector.add(7, new Bonus(1,3,2)); //Autumn
				test_vector.add(8, new Suits(3,3,1)); //3
				test_vector.add(9, new Suits(3,4,1)); //4
				test_vector.add(10, new Suits(3,5,1)); //5
				test_vector.add(11, new Suits(2,7,1)); //7
				test_vector.add(12, new Bonus(1,1,0)); //Spring
				test_vector.add(13, new Suits(2,7,1)); //7
				break;
			case 1:
				test_vector.add(0, new Bonus(1,1,1)); //Spring
				test_vector.add(1, new Bonus(1,1,2)); //Spring
				test_vector.add(2, new Bonus(1,2,0)); //Summer
				test_vector.add(3, new Bonus(1,2,1)); //Summer
				test_vector.add(4, new Bonus(1,2,2)); //Summer
				test_vector.add(5, new Bonus(1,3,0)); //Autumn
				test_vector.add(6, new Bonus(1,3,1)); //Autumn
				test_vector.add(7, new Bonus(1,3,2)); //Autumn
				test_vector.add(8, new Suits(3,1,1)); //1
				test_vector.add(9, new Suits(3,2,1)); //2
				test_vector.add(10, new Suits(3,3,1)); //3
				test_vector.add(11, new Suits(2,7,1)); //7
				test_vector.add(12, new Bonus(1,1,0)); //Spring
				test_vector.add(13, new Suits(2,7,1)); //7
				break;
			default:
				createTrueMahjongTestVectorHand(0);
				break;
		}
		return test_vector;
	}

	private static int[] fromString(String string) {
		String[] strings = string.replace("[", "").replace("]", "").split(", ");
		int[] result = new int[strings.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Integer.parseInt(strings[i]);
		}
		return result;
	}
}