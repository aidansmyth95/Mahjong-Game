package mahjong_package;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

public class Player {

	private String playerUname = "";
	private String playerUid = "";
	private ArrayList<ResponseRequestType> responseOpportunities = new ArrayList<>(); 	// Users chance to respond
	private ResponseReceivedType playerResponse = ResponseReceivedType.NONE;	// Users responses
	private Hand hand = new Hand();		// player's hand
	private PossibleChowsPongsKongs ppk = new PossibleChowsPongsKongs();
	private int chosenChowIdx = -1;
	private int playerIdx = -1;
	private Boolean playerPlaying = false;
	private String gameMessage = "";

	public String getPlayerUid() { return this.playerUid; }
	public void setPlayerUid(String uid) { this.playerUid = uid; }
	public String getPlayerUname() { return this.playerUname; }
	public void setPlayerUname(String uname) { this.playerUname = uname; }
	public ArrayList<ResponseRequestType> getResponseOpportunities() { return this.responseOpportunities; }
	public void setResponseOpportunities(ArrayList<ResponseRequestType> opp) { this.responseOpportunities = opp; }
	public void clearResponseOpportunites() { this.responseOpportunities.clear(); }
	public Boolean checkTseResponseOpportunity() { return this.responseOpportunities.contains(ResponseRequestType.TSE); }
	public Boolean checkDrawResponseOpportunity() { return this.responseOpportunities.contains(ResponseRequestType.DRAW); }
	public Boolean checkChowResponseOpportunity() { return this.responseOpportunities.contains(ResponseRequestType.CHOW_1); }
	public Boolean checkPongResponseOpportunity() { return this.responseOpportunities.contains(ResponseRequestType.PONG); }
	public Boolean checkKongResponseOpportunity() { return this.responseOpportunities.contains(ResponseRequestType.KONG); }
	public Boolean checkMahjongResponseOpportunity() { return this.responseOpportunities.contains(ResponseRequestType.MAHJONG); }

	public void setPlayerResponse(ResponseReceivedType s) { this.playerResponse = s; }
	public ResponseReceivedType getPlayerResponse() { return this.playerResponse; }
	public int checkValidDiscardResponse(ResponseReceivedType resp) {
		ResponseReceivedType[] valid_discards = {
				ResponseReceivedType.DISCARD_0,
				ResponseReceivedType.DISCARD_1,
				ResponseReceivedType.DISCARD_2,
				ResponseReceivedType.DISCARD_3,
				ResponseReceivedType.DISCARD_4,
				ResponseReceivedType.DISCARD_5,
				ResponseReceivedType.DISCARD_6,
				ResponseReceivedType.DISCARD_7,
				ResponseReceivedType.DISCARD_8,
				ResponseReceivedType.DISCARD_9,
				ResponseReceivedType.DISCARD_10,
				ResponseReceivedType.DISCARD_11,
				ResponseReceivedType.DISCARD_12,
				ResponseReceivedType.DISCARD_13,
				ResponseReceivedType.DISCARD_14,
		};
		for (int i=0; i<15; i++) {
			if (resp == valid_discards[i]) {
				// check if int is valid in hand
				if (i < this.hand.getHiddenHandSize()) {
					return i;
				}
			}
		}
		return -1;
	}

	public Hand getHand() { return this.hand; }
	public void setHand(Hand hand) { this.hand = hand; }
	public PossibleChowsPongsKongs getPpk() { return this.ppk; }
	public void setPpk(PossibleChowsPongsKongs ppk) { this.ppk = ppk; }
	public Integer getChosenChowIdx() { return this.chosenChowIdx; }
	public void setChosenChowIdx(int c) { this.chosenChowIdx = c; }
	public Integer getPlayerIdx() { return this.playerIdx; }
	public void setPlayerIdx(int p) { this.playerIdx = p; }
	public Boolean getPlayerPlaying() { return this.playerPlaying; }
	public void setPlayerPlaying(boolean p) { this.playerPlaying = p; }
	public String getGameMessage() { return this.gameMessage; }
	public void setGameMessage(String s) { this.gameMessage = s; }

	Player() {
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
	Tile discardTile(int discard_idx) {
		return this.hand.discardTile(discard_idx);
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

	/*
		Check user responses
	 */
	// check user response for Pong
	boolean checkUserChow(ResponseReceivedType player_input) {
		int num_chows = this.ppk.getNumChows();
		if (num_chows == 0) { return false; }
		ResponseReceivedType[] valid_chows = {ResponseReceivedType.CHOW_1, ResponseReceivedType.CHOW_2, ResponseReceivedType.CHOW_3};
		for (int i=0; i<num_chows; i++) {
			// find a match
			if (player_input == valid_chows[i]) {
				this.chosenChowIdx = i;
				return true;
			}
		}
		return false;
	}

	// check user response for Kong
	boolean checkUserPong(ResponseReceivedType player_input) {
		return player_input == ResponseReceivedType.PONG;
	}

	// check user response for Kong
	boolean checkUserKong(ResponseReceivedType player_input) {
		return player_input == ResponseReceivedType.KONG;
	}

	// check user response for MJ
	boolean checkUserMahjong(ResponseReceivedType player_input) {
		return player_input == ResponseReceivedType.MAHJONG;
	}

	// check user response for MJ
	boolean checkUserDraw(ResponseReceivedType player_input) {
		return player_input == ResponseReceivedType.DRAW;
	}

	// check user response for MJ
	boolean checkUserTse(ResponseReceivedType player_input) {
		return player_input == ResponseReceivedType.TSE;
	}

	// check for win
	public void Mahjong(Tile t) {
		// if Mahjong, add tile to hand and reveal all tiles
		this.hand.addToHand(t);
		this.hand.revealAllHiddenHandTiles();
	}

	public void kong(Tile t) {
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
	public void pong(Tile t) {
		this.hand.addToHand(t);
		int[] hand_idx = new int[3];
		// parse from string to get array elements
		hand_idx[0] = Integer.parseInt(this.ppk.getPossiblePong(0));
		hand_idx[1] = Integer.parseInt(this.ppk.getPossiblePong(1));
		hand_idx[2] = this.hand.getHiddenHandSize() - 1;
		this.hand.revealIndexedHiddenTiles(hand_idx, 3);
	}

	public void chow(Tile t) {
		this.hand.addToHand(t);
		int[] hand_idx = new int[3];
		int[] chows;
		chows = fromString(this.ppk.getPossibleChow(this.chosenChowIdx));
		hand_idx[0] = chows[0];
		hand_idx[1] = chows[1];
		hand_idx[2] = this.hand.getHiddenHandSize()-1; // the last idx where the new tile to pong will be
		this.hand.revealIndexedHiddenTiles(hand_idx, 3);
	}

	// get arraylist of Revealed tile descriptors
	ArrayList<String> getRevealedDescriptors() { return this.hand.getRevealedDescriptors(); }

	// get arraylist of Hidden tile descriptors
	ArrayList<String> getHiddenDescriptors() { return this.hand.getHiddenDescriptors(); }

	private static int[] fromString(String string) {
		String[] strings = string.replace("[", "").replace("]", "").split(", ");
		int[] result = new int[strings.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Integer.parseInt(strings[i]);
		}
		return result;
	}
}