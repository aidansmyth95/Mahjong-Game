package mahjong_package;

import android.util.Log;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

//TODO: optimize data structures (too many ArrayLists) - we could use maps or sets instead etc. Keeping for now as working with Firebase

public class Game {

	private int maxPlayers = 1;										// Maximum number of players that can play the game.
	private int numPlayers = 0; 									// Number of players playing the game
	private String gameName = "NaN";
	private String gameID = "NaN";
	private GameState gameState = GameState.START; 					// state of Game
	private GameStatus gameStatus = GameStatus.PAUSED;				// status of Game
	private Tiles wall = new Tiles(); 								// tile deck
	private int playerTurn;											// player's turn to discard etc
	private int winnerIdx = -1; 									// player who wins
	private int loserIdx = -1; 										// player who loses
	private Tile latestDiscard = new Tile(); 						// latest tile discarded
	private ArrayList<Player> player = new ArrayList<>(); 			// Players playing game
	private Boolean acceptingResponses = false;						// Game is still allowing responses
	private long tseCalledTime = 0;									// time at which tse was called
	private Boolean tseOrDrawCalled = false;						// true if a tse has been called
	private String gameOutput = "Welcome to the game";				// String output for the Game

	// constructor
	public Game() {
		this(1);
	}

	// constructor
	public Game(int maxPlayers) {
		// set maximum number of players
		this.setMaxPlayers(maxPlayers);
		// reset and shuffle deck
		this.wall.shuffleTiles();
	}

	/*
		Public getters and setters for Firebase storing
	 */
	public void setGameState(GameState g) { this.gameState = g; }
	public void setLatestDiscard(Tile t) { this.latestDiscard = t; }
	public void setNumPlayers(int n) { this.numPlayers = n; }
	public void setPlayer(ArrayList<Player> p) { this.player = p; }
	public void setWall(Tiles t) { this.wall = t; }
	public void setPlayerTurn(int t) { this.playerTurn = t; }
	public void setWinnerIdx(int w) { this.winnerIdx = w; }
	public void setLoserIdx(int l) { this.loserIdx = l; }
	public GameState getGameState() { return this.gameState; }
	public Tile getLatestDiscard() { return this.latestDiscard; }
	public Integer getNumPlayers() { return this.numPlayers; }
	public ArrayList<Player> getPlayer() { return this.player; }
	public Player getPlayer(int p) { return this.player.get(p); }
	public Tiles getWall() { return this.wall; }
	public Integer getPlayerTurn() { return this.playerTurn; }
	public Integer getWinnerIdx() { return this.winnerIdx; }
	public Integer getLoserIdx() { return this.loserIdx; }
	public void setAcceptingResponses(boolean b) { this.acceptingResponses = b; }
	public Boolean getAcceptingResponses() { return this.acceptingResponses; }
	public void setGameName(String name) { this.gameName = name; }
	public String getGameName() { return this.gameName; }
	public void setGameID(String id) { this.gameID = id; }
	public String getGameID() { return this.gameID; }
	public void setGameStatus(GameStatus s) { this.gameStatus = s; }
	public GameStatus getGameStatus() { return this.gameStatus; }
	public void setTseCalledTime() { this.tseCalledTime = System.currentTimeMillis(); }
	public long getTseCalledTime() { return this.tseCalledTime; }
	public Boolean getTseOrDrawCalled() { return this.tseOrDrawCalled; }
	public void setTseOrDrawCalled(boolean tse) { this.tseOrDrawCalled = tse; }
	public int getMaxPlayers() { return this.maxPlayers; }
	public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
	public void setGameOutput(String out) { this.gameOutput = out; }
	public String getGameOutput() { return this.gameOutput; }

	/*
	 * Return true if winner, false otherwise
	 */
	public Boolean playGame() {
		// check all players are registered
		if (this.getNumPlayers() != this.getMaxPlayers()) {
			Log.i("Game","Game: Only " + this.getNumPlayers() + " have joined out of " + this.getMaxPlayers());
			return false;
		}
		// return if no more tiles
		if (!this.wall.tilesLeft()) {
			this.setGameOutput("No more tiles left.\n");
			this.setGameState(GameState.GAME_OVER);
		}

		int next_player = (this.getPlayerTurn() + 1) % this.getNumPlayers();

		switch (this.getGameState()) {
			case START:
				// check that all players have starting number of tiles. They might have removed flowers...
				for (int p=0; p<this.getNumPlayers(); p++) {
					// player starting draws an extra tile
					final int starting_tile_count = 13;
					Log.d("Game", "Game: Adding " + starting_tile_count + " tiles to hand");
					// while tiles in hand less than correct starting amount
					while (this.getPlayer(p).getHiddenHandCount() < starting_tile_count) {
						// add tile from deck to hand
						Log.d("Game", "Game: Adding tile...");
						this.getPlayer(p).addToHand(this.getWall().revealWallTile());
					}
				}
				// get random ID for player's turn
				this.setPlayerTurn(this.getRandomPlayerID());
				this.setGameState(GameState.DRAWING_TILE);
				break;

			case CHECKING_HAND:
				// check if players can Mahjong, Kong, Pong or Chow.
				for (int i = 0; i<this.getNumPlayers(); i++) {
					ArrayList<ResponseRequestType> respTypes = new ArrayList<>();
					// player cannot interrupt their own turn - unless they are the only player playing!
					if (i != this.getPlayerTurn() || this.getNumPlayers() == 1) {
						// check for Mahjong
						if (this.getPlayer(i).checkHandMahjong(this.getLatestDiscard())) {
							respTypes.add(ResponseRequestType.MAHJONG);
							this.setGameOutput("Player " + i + ": Mahjong?");
						} else if (this.getPlayer(i).checkHandKong(this.latestDiscard)) {
							respTypes.add(ResponseRequestType.KONG);
							this.setGameOutput("Player " + i + ": Kong?");
						} else if (this.getPlayer(i).checkHandPong(this.latestDiscard)) {
							respTypes.add(ResponseRequestType.PONG);
							this.setGameOutput("Player " + i + ": Pong?");
						} else if (i == next_player) {
							// if we next player can chow
							if (this.getPlayer(i).checkHandChow(this.getLatestDiscard())) {
								ResponseRequestType[] chows = {ResponseRequestType.CHOW_1, ResponseRequestType.CHOW_2, ResponseRequestType.CHOW_3};
								respTypes.addAll(Arrays.asList(chows).subList(0, this.getPlayer(i).getPpk().getNumChows()));
							}
							// next player can tse or draw
							respTypes.add(ResponseRequestType.TSE);
							respTypes.add(ResponseRequestType.DRAW);
							this.setGameOutput("Player " + i + ": Tse? \t1=Tse\t0=No");
						}
					}
					// update player with its expected responses
					this.getPlayer(i).setResponseOpportunities(respTypes);
				}
				// notify activity that it is time to check for responses from affected users
				this.setAcceptingResponses(true);
				this.setGameState(GameState.CHECKING_RESPONSES);
				break;

			// Here we handle responses. The game will go immediately to next state if MJ, Pong, Kong response.
			case CHECKING_RESPONSES:
				// check for MJ
				for (int i=0; i<this.getNumPlayers(); i++) {
					// any other player can respond with Mahjong - Highest priority
					if ((i != this.getPlayerTurn() || this.getNumPlayers() == 1) && this.player.get(i).checkMahjongResponseOpportunity()) {
						ResponseReceivedType resp = this.player.get(i).getPlayerResponse();
						// if player responded to MJ correctly
						if (this.getPlayer(i).checkUserMahjong(resp)) {
							this.setAcceptingResponses(false);
							this.resetPlayerInput();
							// loser is player who discarded the tile that was then used for MJ
							this.setLoserIdx(this.getPlayerTurn());
							this.setPlayerTurn(i);
							this.getPlayer(this.playerTurn).Mahjong(this.getLatestDiscard());
							this.setGameState(GameState.MAHJONG);
							return false;
						}
					}
				}
				// check for Kong first - Second highest priority
				for (int i=0; i<this.getNumPlayers(); i++) {
					if ((i != this.getPlayerTurn() || this.getNumPlayers() == 1) && this.getPlayer(i).checkKongResponseOpportunity()) {
						ResponseReceivedType resp = this.getPlayer(i).getPlayerResponse();
						if (this.getPlayer(i).checkUserKong(resp)) {
							this.setGameState(GameState.KONG);
							this.setPlayerTurn(i);
							this.resetPlayerInput();
							return false;
						}
					}
				}
				// check for Pong - 3rd highest priority
				for (int i=0; i<this.getNumPlayers(); i++) {
					if ((i != this.getPlayerTurn() || this.getNumPlayers() == 1) && this.getPlayer(i).checkPongResponseOpportunity()) {
						ResponseReceivedType resp = this.getPlayer(i).getPlayerResponse();
						if (this.getPlayer(i).checkUserPong(resp)) {
							this.setGameState(GameState.PONG);
							this.setPlayerTurn(i);
							this.resetPlayerInput();
							return false;
						}
					}
				}
				// check for Chow - Penultimate priority
				for (int i=0; i<this.getNumPlayers(); i++) {
					if (i == next_player && this.getPlayer(i).checkChowResponseOpportunity()) {
						ResponseReceivedType resp = this.getPlayer(i).getPlayerResponse();
						if (this.getPlayer(i).checkUserChow(resp)) {
							this.setGameState(GameState.CHOW);
							this.setPlayerTurn(i);
							this.resetPlayerInput();
							return false;
						}
					}
				}
				// check for Draw tile or Tse or drawing new tile - Lowest priority, only vald after a time has passed
				ResponseReceivedType nextPlayerResp = this.player.get(next_player).getPlayerResponse();
				if (this.getPlayer(next_player).checkUserDraw(nextPlayerResp) || this.getPlayer(next_player).checkUserTse(nextPlayerResp)) {
					// start timer if next player input received for draw or tse, and it was not started already
					if (!this.getTseOrDrawCalled()) {
						this.setTseOrDrawCalled(true);
						this.setTseCalledTime();
						Log.d("timer analysis", "Time started at : " + this.getTseCalledTime() + " milliseconds.");
					}
					Log.d("timer analysis", "Time elapsed : " + this.getTseTimeElapsed() + " milliseconds.");
					// n milliseconds after next player input for another player to have chance to interrupt
					long ACCEPTING_RESPONSE_TIME_MS = 1000;
					if (this.getTseTimeElapsed() >= ACCEPTING_RESPONSE_TIME_MS) {
						if (this.getPlayer(next_player).checkUserDraw(nextPlayerResp)) {
							this.setGameState(GameState.DRAWING_TILE);
							this.setPlayerTurn(next_player);
							this.resetPlayerInput();
						} else if (this.getPlayer(next_player).checkUserTse(nextPlayerResp)) {
							this.setGameState(GameState.TSE);
							this.setPlayerTurn(next_player);
							this.resetPlayerInput();
						}
					}
				}
				break;

			case MAHJONG:
				this.setGameState(GameState.GAME_OVER);
				this.setWinnerIdx(this.playerTurn);
				this.setGameOutput("Player " + this.playerTurn + ": Mahjong!");
				break;

			case KONG:
				this.getPlayer(this.getPlayerTurn()).kong(this.getLatestDiscard());
				this.setGameOutput("Player " + this.playerTurn + ": Kong!");
				this.setGameState(GameState.DRAWING_TILE);
				break;

			case PONG:
				this.getPlayer(this.getPlayerTurn()).pong(this.getLatestDiscard());
				this.setGameOutput("Player " + this.playerTurn + ": Pong!");
				this.setGameState(GameState.DISCARD_OPTIONS);
				break;

			case CHOW:
				// by this stage the chosenChowIdx has been set in player class by user input
				this.getPlayer(this.getPlayerTurn()).chow(this.getLatestDiscard());
				this.setGameOutput("Player " + this.getPlayerTurn() + ": Chow!");
				this.setGameState(GameState.DISCARD_OPTIONS);
				break;

			case TSE:
				this.setGameOutput("Tse!");
				this.getPlayer(this.getPlayerTurn()).tse(this.getLatestDiscard());
				this.setGameState(GameState.DISCARD_OPTIONS);
				break;

			case DRAWING_TILE:
				this.setGameOutput("Player " + this.getPlayerTurn() + ": drawing tile");
				// last tile drawn
				Tile tile_drawn = this.getWall().revealWallTile();
				// check if any more tiles to draw
				if (!tile_drawn.checkRealTile()) {
					this.setGameOutput("No hidden tiles in deck left to uncover. Please restart the game");
					this.setGameState(GameState.GAME_OVER);
				} else {
					// check hand for a Mahjong
					if (this.getPlayer(this.getPlayerTurn()).checkHandMahjong(tile_drawn)) {
						this.setGameOutput("Player " + this.getPlayerTurn() + ": Mahjong By Your Own Hand!");
						this.getPlayer(this.getPlayerTurn()).Mahjong(tile_drawn);
						this.setGameState(GameState.MAHJONG);
					} else {
						// add tile to hand MJ or not
						if (this.getPlayer(this.playerTurn).addToHand(tile_drawn) == HandStatus.ADD_SUCCESS) {
							this.setGameState(GameState.DISCARD_OPTIONS);
						}
					}
				}
				break;

			case DISCARD_OPTIONS:
				this.setGameOutput("Player " + this.getPlayerTurn() + ": discard a hidden tile");
				// set request response for the player discarding to be true
				ArrayList<ResponseRequestType> resp_options = new ArrayList<>();
				resp_options.add(ResponseRequestType.DISCARD);
				this.getPlayer(this.getPlayerTurn()).setResponseOpportunities(resp_options);
				this.setGameState(GameState.DISCARDING_TILE);
				// allow players to send responses to Game from MultiplayerActivity
				this.setAcceptingResponses(true);
				break;

			// Discard a tile
			case DISCARDING_TILE:
				ResponseReceivedType discard_resp = this.getPlayer(this.getPlayerTurn()).getPlayerResponse();
				// if there is response
				int discard_idx = this.getPlayer(this.getPlayerTurn()).checkValidDiscardResponse(discard_resp);
				if (discard_idx != -1) {
					this.setLatestDiscard(this.getPlayer(this.getPlayerTurn()).discardTile(discard_idx));
					this.wall.addUncoveredTile(this.getLatestDiscard());
					this.setGameOutput("Player " + this.getPlayerTurn() + " discarded " + this.latestDiscard.getDescriptor());
					this.setGameState(GameState.CHECKING_HAND);
					this.resetPlayerInput();
				}
				break;

			case GAME_OVER:
				this.setGameOutput("Game over!");
				this.setGameStatus(GameStatus.FINISHED);
				return true;

			default:
				System.err.println("ERROR: Game should not have reached this state.");
				this.setGameState(GameState.GAME_OVER);
				return true;
		}
		return false;
	}

	/*
		Reset these values once a user input has been received
	 */
	private void resetPlayerInput() {
		for (int i=0; i<this.getNumPlayers(); i++) {
			this.getPlayer(i).clearResponseOpportunites();
			this.getPlayer(i).setPlayerResponse(ResponseReceivedType.NONE);
			this.setTseOrDrawCalled(false);
			this.setAcceptingResponses(false);
		}
	}

	/*
		One liners
	 */
	public Boolean gameExists() { return !this.getGameID().equals("NaN"); }
	private long getTseTimeElapsed() { return System.currentTimeMillis() - this.tseCalledTime; }
	public void setPlayerResponse(int player_idx, ResponseReceivedType resp) { this.getPlayer(player_idx).setPlayerResponse(resp); }
	public String getGameMessage(int player_idx) { return this.getPlayer(player_idx).getGameMessage(); }
	public void setGameMessage(int player_idx, String msg) { this.getPlayer(player_idx).setGameMessage(msg); }
	public void setPlayerPlayingStatus(boolean status, int playerIdx) { this.getPlayer(playerIdx).setPlayerPlaying(status); }

	/*
		Handling empty ArrayLists in Firebase
	 */
	void fillEmptyArrayLists() {
		Tile emptyTile = new Tile();
		ArrayList<Tile> emptyTileArrayList = new ArrayList<>();
		emptyTileArrayList.add(emptyTile);
		for (int i=0; i<this.getNumPlayers(); i++) {
			// hand items hiddenHand and revealedHand
			Hand playerHand = this.getPlayer(i).getHand();
			if (playerHand.getHiddenHand().isEmpty()) {
				playerHand.setHiddenHand(emptyTileArrayList);
			}
			if (playerHand.getRevealedHand().isEmpty()) {
				playerHand.setRevealedHand(emptyTileArrayList);
			}
		}
		// hidden and uncovered Tiles?
		if (this.getWall().getHiddenTiles().isEmpty()) {
			this.getWall().setHiddenTiles(emptyTileArrayList);
		}
		if (this.getWall().getUncoveredTiles().isEmpty()) {
			this.getWall().setUncoveredTiles(emptyTileArrayList);
		}
	}

	// remove empty values added in above function
	void cleanEmptyArrayLists() {
		Tile emptyTile = new Tile();
		// empty arraylist to set items to be
		ArrayList<Tile> emptyTileArrayList = new ArrayList<>();
		// empty arraylist pattern to find
		for (int i=0; i<this.getNumPlayers(); i++) {
			// hand items hiddenHand and revealedHand
			Hand playerHand = this.getPlayer(i).getHand();
			// if empty match
			if (checkEmptyArrayList(playerHand.getHiddenHand())) {
				Log.d("Game", "Game: Removing empty ArrayList placeholder for hidden tiles");
				playerHand.setHiddenHand(emptyTileArrayList);
			}
			// if empty match
			if (checkEmptyArrayList(playerHand.getRevealedHand())) {
				Log.d("Game", "Game: Removing empty ArrayList placeholder for revealed tiles");
				playerHand.setRevealedHand(emptyTileArrayList);
			}
		}
		// hidden and uncovered Tiles?
		if (checkEmptyArrayList(this.wall.getHiddenTiles())) {
			Log.d("Game", "Game: Removing empty wall placeholder for hidden tiles");
			this.getWall().getHiddenTiles().clear();
		}
		if (checkEmptyArrayList(this.wall.getUncoveredTiles())) {
			Log.d("Game", "Game: Removing empty wall placeholder for revealed tiles");
			this.getWall().getUncoveredTiles().clear();
		}
	}

	private Boolean checkEmptyArrayList(ArrayList<Tile> arr) {
		if (arr.size() == 1) {
			Tile tmp = arr.get(0);
			// if not a real tile, then ArrayList was empty
			return !tmp.checkRealTile();
		}
		return false;
	}

	/*

		Visual aid functions

	 */

	public ArrayList<Tile> getFlowersCollected(int p){
		return this.getPlayer(p).getFlowersCollected();
	}

	public String getFlowersCollectedString(int p) {
		StringBuilder s = new StringBuilder();
		s.append("Flowers collected: ");
		ArrayList<Tile> flowers = this.getFlowersCollected(p);
		for (int i=0; i<flowers.size(); i++) {
			s.append("\t").append(flowers.get(i).getDescriptor()).append("\n");
		}
		return s.toString();
	}

	public String getLatestFlowersCollectedDescriptorResource(int p){
		return this.getPlayer(p).getLatestFlowersCollectedDescriptor().toLowerCase().replace(" ", "_");
	}

	public int getFlowersCount(int playerIdx) {
		return this.getPlayer(playerIdx).getFlowersCount();
	}

	@Exclude
	public String getLatestDiscardedDescriptorResource() {
		return this.latestDiscard.getDescriptor().toLowerCase().replace(" ", "_");
	}

	@Exclude
	public ArrayList<String> getRevealedDescriptors() {
		return this.getPlayer(this.getPlayerTurn()).getRevealedDescriptors();
	}

	@Exclude
	public ArrayList<String> getHiddenDescriptors() {
		return this.getPlayer(this.getPlayerTurn()).getHiddenDescriptors();
	}

	public ArrayList<String> descriptorToDrawablePath(ArrayList<String> descriptors) {
		// Dragon Green -> dragon_green
		ArrayList<String> drawables = new ArrayList<>();
		for (int i=0; i<descriptors.size(); i++) {
			drawables.add(descriptors.get(i).toLowerCase().replace(" ", "_"));
		}
		return drawables;
	}

	/*

		Player & Game interaction functionality

	 */

	public String namePlayersPlaying() {
		ArrayList<String> names = new ArrayList<>();
		for (int i=0; i<this.getNumPlayers(); i++) {
			if (this.getPlayer(i).getPlayerPlaying()) {
				names.add(this.getPlayer(i).getPlayerUname());
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
		for (int i=0; i<this.getNumPlayers(); i++) {
			if (!this.getPlayer(i).getPlayerPlaying()) {
				names.add(this.getPlayer(i).getPlayerUname());
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
		if (this.getMaxPlayers() != this.getNumPlayers()) {
			return false;
		} else {
			for (int i=0; i<this.getMaxPlayers(); i++) {
				if (!this.getPlayer(i).getPlayerPlaying()) {
					return false;
				}
			}
			return true;
		}
	}

	public int countNumPlayersPlaying() {
		int num_playing = 0;
		for (int i=0; i<this.getNumPlayers(); i++) {
			if (this.getPlayer(i).getPlayerPlaying()) {
				num_playing++;
			}
		}
		return num_playing;
	}

	public Boolean allPlayersJoined() {
		return this.getMaxPlayers() == this.getNumPlayers();
	}

	private int getRandomPlayerID() {
		// this way of generating random number may not be truly random...
		Random rand = new Random();
		int max = this.getNumPlayers();
		int min = 0;
		int nextPlayerIdx = rand.nextInt((max - min) + 1) + min;
		if (nextPlayerIdx == this.getNumPlayers()) {
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
			Log.e(String.valueOf(1),i + " " + this.getPlayer(i).getPlayerUname() + " ADDED NAME");
			s.append(this.getPlayer(i).getPlayerUname()).append("  ");
		}
		return s.toString();
	}

	public Boolean addPlayer(String name, String uid) {
		// check player does not already exist in game
		ArrayList<Tile> start_tiles = new ArrayList<>();
		for (int i=0; i<this.player.size(); i++) {
			if (this.getPlayer(i).getPlayerUname().equals(name) || this.getPlayer(i).getPlayerUid().equals(uid)) {
				return false;
			}
		}
		for (int j = 0; j < 13; j++) {
			start_tiles.add(this.wall.revealWallTile());
		}
		this.player.add(new Player(name, uid, this.numPlayers, start_tiles));
		this.numPlayers++;
		return true;
	}

	public int getPlayerIdx(String uid) {
		// return index of player that matches name
		for (int i=0; i<this.player.size(); i++) {
			if (uid.equals(this.getPlayer(i).getPlayerUid())) {
				return this.getPlayer(i).getPlayerIdx();   // -1 to account for "names" as first value in ArrayList
			}
		}
		return -1;
	}

	public Boolean checkPlayerJoined(String uid) {
		// check that a player has joined the game
		for (int i=0; i<this.player.size(); i++) {
			if (this.getPlayer(i).getPlayerUid().equals(uid)) {
				Log.i("Game", "Game: Game player idx was found");
				return true;
			}
		}
		return false;
	}

	public ArrayList<ResponseRequestType> getPlayersResponsesOpportunities(int p) {
		return this.getPlayer(p).getResponseOpportunities();
	}

}

