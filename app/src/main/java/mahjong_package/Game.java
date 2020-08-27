package mahjong_package;

import android.util.Log;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.Random;

//TODO: incorrect data structures (too many ArrayLists) when we could use maps or sets etc.

public class Game {

	private final long ACCEPTING_RESPONSE_TIME = 1000;	// n milliseconds after next player input for another player to have chance to interrupt
	private final int maxPlayers = 4;
	private String gameName;
	private String gameID;
	private GameState gameState; 		// state of Game
	private GameStatus gameStatus;			// status of Game - new, start, playing, saved ..(and more)... //TODO: use enums later for this
	private int numPlayers; 			// Number of players playing the game
	private Tiles tiles = new Tiles(); 	// tile deck
	private int playerTurn;				// player's turn to discard etc
	//TODO: implement this
	private int winnerIdx; 				// player who wins
	private int loserIdx; 				// player who loses
	//TODO: needed?
	public boolean updateDiscardedTileImage; // update discarded image tiles cue
	private Tile latestDiscard; 		// latest tile discarded
	private ArrayList<Player> player = new ArrayList<>(); // Players playing game
	private Boolean acceptingResponses;								// Game is still allowing responses
	private long tseCalledTime = 0;
	private Boolean tseCalled;

	// constructor
	public Game() {
		this.numPlayers = 0;
		this.winnerIdx = -1;
		this.loserIdx = -1;
		this.latestDiscard = new Tile();
		this.updateDiscardedTileImage = false;
		this.gameState = GameState.START;
		this.setAcceptingResponses(false);
		this.setGameName("NaN");
		this.setGameID("NaN");
		// reset and shuffle deck
		this.tiles.shuffleTiles();
		this.setGameStatus(GameStatus.PAUSED);
		this.setTseCalled(false);
	}

	public Boolean gameExists() {
		return !this.getGameID().equals("NaN");
	}

	/*
		Public getters and setters for Firebase storing
	 */
	public void setGameState(GameState g) { this.gameState = g; }
	public void setUpdateDiscardedTileImage(boolean b) { this.updateDiscardedTileImage = b; }
	public void setLatestDiscard(Tile t) { this.latestDiscard = t; }
	public void setNumPlayers(int n) { this.numPlayers = n; }
	public void setPlayer(ArrayList<Player> p) { this.player = p; }
	public void setTiles(Tiles t) { this.tiles = t; }
	public void setPlayerTurn(int t) { this.playerTurn = t; }
	public void setWinnerIdx(int w) { this.winnerIdx = w; }
	public void setLoserIdx(int l) { this.loserIdx = l; }
	public GameState getGameState() { return this.gameState; }
	public boolean getUpdateDiscardedTileImage() { return this.updateDiscardedTileImage; }
	public Tile getLatestDiscard() { return this.latestDiscard; }
	public Integer getNumPlayers() { return this.numPlayers; }
	public ArrayList<Player> getPlayer() { return this.player; }
	public Tiles getTiles() { return this.tiles; }
	public Integer getPlayerTurn() { return this.playerTurn; }
	public Integer getWinnerIdx() { return this.winnerIdx; }
	public Integer getLoserIdx() { return this.loserIdx; }
	public void setAcceptingResponses(boolean turn) { this.acceptingResponses = turn; }
	public Boolean getAcceptingResponses() { return this.acceptingResponses; }
	public void setGameName(String name) { this.gameName = name; }
	public String getGameName() { return this.gameName; }
	public void setGameID(String id) { this.gameID = id; }
	public String getGameID() { return this.gameID; }
	public void setGameStatus(GameStatus s) { this.gameStatus = s; }
	public GameStatus getGameStatus() { return this.gameStatus; }
	public void setTseCalledTime() { this.tseCalledTime = System.currentTimeMillis(); }
	public long getTseCalledTime() { return this.tseCalledTime; }
	public Boolean getTseCalled() { return this.tseCalled; }
	public void setTseCalled(Boolean tse) { this.tseCalled = tse; }
	public int getMaxPlayers() { return this.maxPlayers; }
	@Exclude
	public long getTseTimeElapsed() { return System.currentTimeMillis() - this.tseCalledTime; }

	// provide access to player responses
	public Boolean getRequestResponse(int player_idx) { return this.player.get(player_idx).getRequestResponse(); }
	public void setRequestResponse(int player_idx, boolean b) { this.player.get(player_idx).setRequestResponse(b); }
	public void setPlayerResponse(int player_idx, String resp) { this.player.get(player_idx).setPlayerResponse(resp); }

	public void fillEmptyArrayLists() {
		ArrayList<int[]> emptyIntArrayArrayList = new ArrayList<>();
		int [] intArr = new int[1];
		intArr[0] = -1;
		emptyIntArrayArrayList.add(intArr);
		Tile emptyTile = new Tile();
		ArrayList<Tile> emptyTileArrayList = new ArrayList<>();
		emptyTileArrayList.add(emptyTile);

		for (int i=0; i<this.numPlayers; i++) {
			// hand items hiddenHand and revealedHand
			Hand playerHand = this.player.get(i).getHand();
			if (playerHand.getHiddenHand().isEmpty()) {
				playerHand.setHiddenHand(emptyTileArrayList);
			}
			if (playerHand.getRevealedHand().isEmpty()) {
				playerHand.setRevealedHand(emptyTileArrayList);
			}
		}

		//TODO: hidden and uncovered Tiles?
		if (tiles.getHiddenTiles().isEmpty()) {
			tiles.setHiddenTiles(emptyTileArrayList);
		}
		if (tiles.getUncoveredTiles().isEmpty()) {
			tiles.setUncoveredTiles(emptyTileArrayList);
		}
	}

	// remove empty values added in above function
	public void cleanEmptyArrayLists() {
		ArrayList<int[]> emptyIntArrayArrayList = new ArrayList<>();
		//ArrayList<int[]> tmp;
		int [] intArr = new int[1];
		intArr[0] = -1;
		emptyIntArrayArrayList.add(intArr);
		Tile emptyTile = new Tile();
		ArrayList<Tile> emptyTileArrayList = new ArrayList<>();
		ArrayList<Tile> emptyTileArrayListPattern = new ArrayList<>();
		emptyTileArrayListPattern.add(emptyTile);

		for (int i=0; i<this.numPlayers; i++) {
			// hand items hiddenHand and revealedHand
			Hand playerHand = this.player.get(i).getHand();
			// if empty match
			if (playerHand.getHiddenHand().equals(emptyTileArrayListPattern)) {
				playerHand.setHiddenHand(emptyTileArrayList);
			}
			// if empty match
			if (playerHand.getRevealedHand().equals(emptyTileArrayListPattern)) {
				playerHand.setRevealedHand(emptyTileArrayList);
			}
		}
		// hidden and uncovered Tiles?
		if (this.tiles.getHiddenTiles().equals(emptyTileArrayListPattern)) {
			this.tiles.getHiddenTiles().clear();
		}
		if (this.tiles.getUncoveredTiles().equals(emptyTileArrayListPattern)) {
			this.tiles.getUncoveredTiles().clear();
		}

	}

	public void setPlayerPlayingStatus(boolean status, int playerIdx) {
		this.player.get(playerIdx).setPlayerPlaying(status);
	}

	public String namePlayersPlaying() {
		ArrayList<String> names = new ArrayList<>();
		for (int i=0; i<this.numPlayers; i++) {
			if (this.player.get(i).getPlayerPlaying()) {
				names.add(this.player.get(i).getPlayerUname());
			}
		}
		if (names.size() == 0) {
			return "None";
		}
		StringBuilder s = new StringBuilder();
		for (int i=0; i<names.size(); i++) {
			s.append(names.get(i)).append("  ");
		}
		return s.toString();
	}

	public String namePlayersNotPlaying() {
		ArrayList<String> names = new ArrayList<>();
		for (int i=0; i<this.numPlayers; i++) {
			if (!this.player.get(i).getPlayerPlaying()) {
				names.add(this.player.get(i).getPlayerUname());
			}
		}
		if (names.size() == 0) {
			return "None";
		}
		StringBuilder s = new StringBuilder();
		for (int i=0; i<names.size(); i++) {
			s.append(names.get(i)).append("  ");
		}
		return s.toString();
	}

	public Boolean allPlayersPlaying() {
		for (int i=0; i<this.numPlayers; i++) {
			if (!this.player.get(i).getPlayerPlaying()) {
				return false;
			}
		}
		return true;
	}

	/*
	 * Return player index of winner.
	 */
	public void playGame() {

		//TODO: change playGame to return an int on game over. For now we game over.
		//TODO: remove all System.exit calls in package. Maybe replace with some kind of error flag?

		// return if no more tiles
		if (!this.tiles.tilesLeft()) {
			System.out.println("No more tiles left.\n");
			this.gameState = GameState.GAME_OVER;
		}

		int next_player = (this.playerTurn + 1) % this.numPlayers;

		switch (this.gameState) {

			case START:
				Log.e(String.valueOf(1),"STATE STATUS: START");
				System.out.println("Welcome to the game\n");
				this.playerTurn = this.getRandomPlayerID();
				this.gameState = GameState.DRAWING_TILE;
				break;

			case CHECKING_HAND:
				Log.e(String.valueOf(1),"STATE STATUS: CHECKING_HAND");
				// check if players can Mahjong, Kong or Pong.
				for (int i = 0; i<this.numPlayers; i++) {
					// player cannot interrupt their own turn
					if (i != this.playerTurn) {
						// check for Mahjong
						if (this.player.get(i).checkHandMahjong(this.latestDiscard)) {
							this.player.get(i).setMahjongAvailable(true);
							this.player.get(i).setRequestResponse(true);
							System.out.println("Player " + i + ": Mahjong? \t1=Mahjong\tother=No");
						} else if (this.player.get(i).checkHandKong(this.latestDiscard)) {
							this.player.get(i).setKongAvailable(true);
							this.player.get(i).setRequestResponse(true);
							System.out.println("Player " + i + ": Kong? \t1=Kong\tother=No");
						} else if (this.player.get(i).checkHandPong(this.latestDiscard)) {
							this.player.get(i).setPongAvailable(true);
							this.player.get(i).setRequestResponse(true);
							this.player.get(i).showHiddenHand();
						}
					}
				}
				// next player will also have chance to tse
				this.player.get(next_player).setRequestResponse(true);
				this.setAcceptingResponses(true);
				System.out.println("Player " + next_player + ": Tse? \t1=Tse\t0=No");
				this.setGameState(GameState.CHECKING_RESPONSES);
				break;

			// Here we handle responses. The game will go immediately to next state if MJ, Pong, Kong response.
			//TODO: these might be eventually replaced by buttons...? How would that work for multiple choice?
			case CHECKING_RESPONSES:
				// check for MJ
				for (int i=0; i<this.numPlayers; i++) {
					// if player can respond
					if (i != this.playerTurn && this.getRequestResponse(i)) {
						String resp = this.player.get(i).getPlayerResponse();
						// if player could MJ
						if (this.player.get(i).getMahjongAvailable()) {
							// if player responded to MJ correctly
							if (this.player.get(i).checkUserMahjong(resp)) {
								this.acceptingResponses = false;
								this.setGameState(GameState.MAHJONG);
								this.playerTurn = i;
								this.resetPlayerInput();
								return;
							} else {
								System.out.println("Incorrect input, please enter a correct response for Mahjong.");
								this.setRequestResponse(next_player, true);
								this.setPlayerResponse(next_player, "");
							}
						}
					}
				}
				// check for Kong
				for (int i=0; i<this.numPlayers; i++) {
					if (i != this.playerTurn && this.getRequestResponse(i)) {
						String resp = this.player.get(i).getPlayerResponse();
						if (this.player.get(i).getKongAvailable()) {
							if (this.player.get(i).checkUserKong(resp)) {
								this.setGameState(GameState.KONG);
								this.playerTurn = i;
								this.resetPlayerInput();
								return;
							} else {
								System.out.println("Incorrect input, please enter a correct response for Kong.");
								this.setRequestResponse(next_player, true);
								this.setPlayerResponse(next_player, "");
							}
						}
					}
				}
				// check for Pong
				for (int i=0; i<this.numPlayers; i++) {
					if (i != this.playerTurn && this.getRequestResponse(i)) {
						String resp = this.player.get(i).getPlayerResponse();
						if (this.player.get(i).getPongAvailable()) {
							if (this.player.get(i).checkUserPong(resp)) {
								this.setGameState(GameState.PONG);
								this.playerTurn = i;
								this.resetPlayerInput();
								return;
							} else {
								System.out.println("Incorrect input, please enter a correct response for Pong.");
								this.setRequestResponse(next_player, true);
								this.setPlayerResponse(next_player, "");
							}
						}
					}
				}
				// check for Draw tile or Tse or drawing new tile
				String nextPlayerResp = this.player.get(next_player).getPlayerResponse();
				if (nextPlayerResp.equals("0") ||nextPlayerResp.equals("1")) {
					// start timer if next player input
					if (!this.tseCalled) {
						this.tseCalled = true;
						this.setTseCalledTime();
						Log.e("timer analysis", "Time started at : " + this.getTseCalledTime() + " milliseconds.");
					}
					Log.e("timer analysis", "Time elapsed : " +this.getTseTimeElapsed() + " milliseconds.");
					// check that time has elapsed before moving on
					if (this.getTseTimeElapsed() >= ACCEPTING_RESPONSE_TIME) {
						if (this.player.get(next_player).getPlayerResponse().equals("0")) {
							this.setGameState(GameState.DRAWING_TILE);
						} else if (this.player.get(next_player).getPlayerResponse().equals("1")) {
							this.setGameState(GameState.TSE);
						}
						this.playerTurn = next_player;
						this.resetPlayerInput();
					}
				} else if (nextPlayerResp.equals("")) {
					break;
				}
				else {
					System.out.println("Incorrect input, please enter a correct response for Tse.");
					this.setRequestResponse(next_player, true);
					this.setPlayerResponse(next_player, "");
				}
				break;

			case MAHJONG:
				Log.e(String.valueOf(1),"STATE STATUS: MJ");
				this.gameState = GameState.GAME_OVER;
				System.out.println("Player " + this.playerTurn + ": Mahjong!");
				break;

			case KONG:
				Log.e(String.valueOf(1),"STATE STATUS: KONG");
				this.player.get(this.playerTurn).kong(this.latestDiscard);
				System.out.println("Player " + this.playerTurn + ": Kong!");
				this.gameState = GameState.DRAWING_TILE;
				break;

			case PONG:
				Log.e(String.valueOf(1),"STATE STATUS: PONG");
				this.player.get(this.playerTurn).pong(this.latestDiscard);
				System.out.println("Player " + this.playerTurn + ": Pong!");
				this.gameState = GameState.DISCARD_OPTIONS;
				break;

			case TSE:
				Log.e(String.valueOf(1),"STATE STATUS: TSE");
				System.out.println("Tse!");
				this.player.get(this.playerTurn).tse(this.latestDiscard);
				this.gameState = GameState.DISCARD_OPTIONS;
				break;

			case DRAWING_TILE:
				Log.e(String.valueOf(1),"STATE STATUS: DRAWING TILE");
				System.out.println("Player " + this.playerTurn + ": drawing tile");
				// last tile drawn
				Tile tile_drawn = this.tiles.revealTile();
				// check hand for a Mahjong //TODO: a user response to confirm/notice this
				if (this.player.get(this.playerTurn).checkHandMahjong(tile_drawn)) {
					System.out.println("Player " + this.playerTurn + ": Mahjong By Your Own Hand!");
					this.gameState = GameState.MAHJONG;
				} else {
					// add tile to hand MJ or not
					this.player.get(this.playerTurn).addToHand(tile_drawn);
					this.gameState = GameState.DISCARD_OPTIONS;
				}
				break;

			case DISCARD_OPTIONS:
				Log.e(String.valueOf(1),"STATE STATUS: DISCARD OPTIONS");
				System.out.println("Player " + this.playerTurn + ": discard a tile from the following:");
				this.player.get(this.playerTurn).showHiddenHand();
				this.player.get(this.playerTurn).setRequestResponse(true);
				this.setAcceptingResponses(true);
				this.gameState = GameState.DISCARDING_TILE;
				break;

			// Discard a tile
			case DISCARDING_TILE:
				Log.e(String.valueOf(1),"STATE STATUS: DISCARDING TILE");
				String resp = this.player.get(this.playerTurn).getPlayerResponse();
				// if there is response
				if (!resp.equals("")) {
					int valid_idx = this.player.get(this.playerTurn).getUserDiscardIdx(resp);
					// if the response is valid
					if (valid_idx == -1) {
						this.setRequestResponse(next_player, true);
						this.setPlayerResponse(next_player, "");
					} else {
						this.latestDiscard = this.player.get(this.playerTurn).discardTile(resp);
						this.tiles.addUncoveredTile(this.latestDiscard);
						System.out.println("Player " + this.playerTurn + " discarded " + this.latestDiscard.getDescriptor());
						this.updateDiscardedTileImage = true;
						this.gameState = GameState.CHECKING_HAND;
						this.resetPlayerInput();
					}
				}
				break;

			case GAME_OVER:
				Log.e(String.valueOf(1),"STATE STATUS: GAME OVER");
				System.out.println("Game over!\n");
				this.setGameStatus(GameStatus.FINISHED);
				break;

			default:
				System.out.println("ERROR: Game should not have reached this state.");
				this.gameState = GameState.GAME_OVER;
				break;
		}

	}


	private void resetPlayerInput() {
		for (int i=0; i<this.numPlayers; i++) {
			this.player.get(i).setMahjongAvailable(false);
			this.player.get(i).setKongAvailable(false);
			this.player.get(i).setPongAvailable(false);
			this.player.get(i).setRequestResponse(false);
			this.player.get(i).setPlayerResponse("");
			this.setTseCalled(false);
			this.setAcceptingResponses(false);
		}
	}

	@Exclude
	public String getDiscardedDescriptor() {
		return this.latestDiscard.getDescriptor().toLowerCase().replace(" ", "_");
	}

	@Exclude
	public ArrayList<String> getRevealedDescriptors() {
		return this.player.get(playerTurn).getRevealedDescriptors();
	}

	@Exclude
	public ArrayList<String> getHiddenDescriptors() {
		return this.player.get(this.playerTurn).getHiddenDescriptors();
	}

	public ArrayList<String> descriptorToDrawablePath(ArrayList<String> descriptors) {
		// Dragon Green -> dragon_green
		ArrayList<String> drawables = new ArrayList<>();
		for (int i=0; i<descriptors.size(); i++) {
			drawables.add(descriptors.get(i).toLowerCase().replace(" ", "_"));
		}
		return drawables;
	}

	public boolean test_true_pong() {
		for (int i=0; i<2; i++) {
			this.player.get(0).clearHand();
			Tile[] all_tiles = this.player.get(0).createTruePongTestVectorHand(i);
			Tile disc_tile = all_tiles[13];
			Tile[] hand_tiles = new Tile[13];
			System.arraycopy(all_tiles, 0, hand_tiles, 0, 13);
			this.player.get(0).createHand(hand_tiles);
			this.player.get(0).showHiddenHand();
			boolean success = this.player.get(0).checkHandPong(disc_tile);
			if (success) {
				System.out.println("Passed true pong test " + i);
			} else {
				this.player.get(0).showHiddenHand();
				System.out.println(disc_tile.getDescriptor());
				System.out.println("Failed true pong test " + i);
				return false;
			}
		}
		return true;
	}

	public boolean test_false_pong() {
		for (int i=0; i<2; i++) {
			this.player.get(0).clearHand();
			Tile[] all_tiles = this.player.get(0).createFalsePongTestVectorHand(i);
			Tile disc_tile = all_tiles[13];
			Tile[] hand_tiles = new Tile[13];
			System.arraycopy(all_tiles, 0, hand_tiles, 0, 13);
			this.player.get(0).createHand(hand_tiles);
			boolean success = this.player.get(0).checkHandPong(disc_tile);
			if (!success) {
				System.out.println("Passed false pong test " + i);
			} else {
				this.player.get(0).showHiddenHand();
				System.out.println(disc_tile.getDescriptor());
				System.out.println("Failed false pong test " + i);
				return false;
			}
		}
		return true;
	}

	public boolean test_true_kong() {
		for (int i=0; i<2; i++) {
			this.player.get(0).clearHand();
			Tile[] all_tiles = this.player.get(0).createTrueKongTestVectorHand(i);
			Tile disc_tile = all_tiles[13];
			Tile[] hand_tiles = new Tile[13];
			System.arraycopy(all_tiles, 0, hand_tiles, 0, 13);
			this.player.get(0).createHand(hand_tiles);
			boolean success = this.player.get(0).checkHandKong(disc_tile);
			if (success) {
				System.out.println("Passed true kong test " + i);
			} else {
				this.player.get(0).showHiddenHand();
				System.out.println(disc_tile.getDescriptor());
				System.out.println("Failed true kong test " + i);
				return false;
			}
		}
		return true;
	}

	public boolean test_false_kong() {
		for (int i=0; i<2; i++) {
			this.player.get(0).clearHand();
			Tile[] all_tiles = this.player.get(0).createFalseKongTestVectorHand(i);
			Tile disc_tile = all_tiles[13];
			Tile[] hand_tiles = new Tile[13];
			System.arraycopy(all_tiles, 0, hand_tiles, 0, 13);
			this.player.get(0).createHand(hand_tiles);
			boolean success = this.player.get(0).checkHandKong(disc_tile);
			if (!success) {
				System.out.println("Passed false kong test " + i);
			} else {
				this.player.get(0).showHiddenHand();
				System.out.println(disc_tile.getDescriptor());
				System.out.println("Failed false kong test " + i);
				return false;
			}
		}
		return true;
	}

	public boolean test_true_mahjong() {
		for (int i=0; i<2; i++) {
			this.player.get(0).clearHand();
			Tile[] all_tiles = this.player.get(0).createTrueMahjongTestVectorHand(i);
			Tile disc_tile = all_tiles[13];
			Tile[] hand_tiles = new Tile[13];
			System.arraycopy(all_tiles, 0, hand_tiles, 0, 13);
			this.player.get(0).createHand(hand_tiles);
			boolean success = this.player.get(0).checkHandMahjong(disc_tile);
			if (success) {
				System.out.println("Passed MJ test " + i);
			} else {
				this.player.get(0).showHiddenHand();
				System.out.println(disc_tile.getDescriptor());
				System.out.println("Failed MJ test " + i);
				return false;
			}
		}
		return true;
	}

	private int getRandomPlayerID() {
		//TODO: this way of generating random number may not be truly random...
		Random rand = new Random();
		int max = this.numPlayers;
		int min = 0;
		int nextPlayerIdx = rand.nextInt((max - min) + 1) + min;
		if (nextPlayerIdx == this.numPlayers) {
			nextPlayerIdx--;
		}
		return nextPlayerIdx;
	}

	// non getter setter methods
	public String listAllPlayer() {
		if (this.player.size() == 0) {
			return "No members";
		}
		StringBuilder s = new StringBuilder();
		for (int i=0; i<this.player.size(); i++) {
			Log.e(String.valueOf(1),i + " " + this.player.get(i).getPlayerUname() + " ADDED NAME");
			s.append(this.player.get(i).getPlayerUname()).append("  ");
		}
		return s.toString();
	}

	public void addPlayer(String name, String uid) {
		// check player does not already exist in game
		for (int i=0; i<this.player.size(); i++) {
			if (this.player.get(i).getPlayerUname().equals(name) || this.player.get(i).getPlayerUid().equals(uid)) {
				System.out.println("Cannot add player " + name + ", player already exists!");
				return;
			}
		}
		Tile[] start_tiles = new Tile[13];
		for (int j = 0; j < 13; j++) {
			start_tiles[j] = this.tiles.revealTile();
		}
		this.player.add(new Player(name, uid, this.numPlayers, start_tiles));
		this.numPlayers++;
	}

	//TODO: may be needed in later menus
	/*
	public void removePlayer(int player_idx) {
		try {
			this.player.remove(player_idx);
			this.numPlayers--;
		} catch (Exception e) {
			System.out.println("Error: Encountered issue removing player " + player_idx + " from game.");
		}
	}

	public void removePlayer(String uid) {
		for (int i=0; i<this.player.size(); i++) {
			if (uid.equals(this.player.get(i).getPlayerUid())) {
				this.player.remove(i);
				this.numPlayers--;
				return;
			}
		}
	}
	*/

	// return index of player that matches name
	public int getPlayerIdx(String uid) {
		for (int i=0; i<this.player.size(); i++) {
			if (this.player.get(i).getPlayerUid().equals(uid))
				Log.e("comparing_names", "Getting player idx " + uid + " == " + this.player.get(i).getPlayerUid() + " ?");
			if (uid.equals(this.player.get(i).getPlayerUid())) {
				return this.player.get(i).getPlayerIdx();   // -1 to account for "names" as first value in ArrayList
			}
		}
		// if no match, return -1
		return -1;
	}

	//TODO: resetGame
}

