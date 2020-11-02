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
		if (this.numPlayers != this.maxPlayers) {
			Log.i("Game","Game: Only " + this.numPlayers + " have joined out of " + this.maxPlayers);
			return false;
		}
		// return if no more tiles
		if (!this.wall.tilesLeft()) {
			System.out.println("No more tiles left.\n");
			this.gameState = GameState.GAME_OVER;
		}

		int next_player = (this.playerTurn + 1) % this.numPlayers;
		Log.i("Game","Game: "+this.gameState);
		Log.i("Game","Game:\tplayer turn " + this.playerTurn + "\tnext player " + next_player);

		switch (this.gameState) {
			case START:
				// check that all players have starting number of tiles. They might have removed flowers...
				for (int p=0; p<this.numPlayers; p++) {
					// player starting draws an extra tile
					final int starting_tile_count = 13;
					Log.d("Game", "Game: Adding " + starting_tile_count + " tiles to hand");
					// while tiles in hand less than correct starting amount
					while (this.player.get(p).getHiddenHandCount() < starting_tile_count) {
						// add tile from deck to hand
						Log.d("Game", "Game: Adding tile...");
						this.player.get(p).addToHand(this.wall.revealWallTile());
					}
				}
				// get random ID for player's turn
				this.playerTurn = this.getRandomPlayerID();
				this.gameState = GameState.DRAWING_TILE;
				break;

			case CHECKING_HAND:
				// check if players can Mahjong, Kong, Pong or Chow.
				for (int i = 0; i<this.numPlayers; i++) {
					ArrayList<ResponseRequestType> respTypes = new ArrayList<>();
					// player cannot interrupt their own turn - unless they are the only player playing!
					if (i != this.playerTurn || this.numPlayers == 1) {
						// check for Mahjong
						if (this.player.get(i).checkHandMahjong(this.latestDiscard)) {
							respTypes.add(ResponseRequestType.MAHJONG);
							this.gameOutput = "Player " + i + ": Mahjong?\t1=Mahjong\tother=No";
						} else if (this.player.get(i).checkHandKong(this.latestDiscard)) {
							respTypes.add(ResponseRequestType.KONG);
							this.gameOutput = "Player " + i + ": Kong?\t1=Kong\tother=No";
						} else if (this.player.get(i).checkHandPong(this.latestDiscard)) {
							respTypes.add(ResponseRequestType.PONG);
							this.gameOutput = "Player " + i + ": Pong?\t1=Pong\tother=No";
						} else if (i == next_player) {
							// if we next player can chow
							if (this.player.get(i).checkHandChow(this.latestDiscard)) {
								ResponseRequestType[] chows = {ResponseRequestType.CHOW_1, ResponseRequestType.CHOW_2, ResponseRequestType.CHOW_3};
								respTypes.addAll(Arrays.asList(chows).subList(0, this.player.get(i).getPpk().getNumChows()));
							}
							// next player can tse or draw
							respTypes.add(ResponseRequestType.TSE);
							respTypes.add(ResponseRequestType.DRAW);
							System.out.println("Player " + i + ": Tse? \t1=Tse\t0=No");
						}
					}
					// update player with its expected responses
					this.player.get(i).setResponseOpportunities(respTypes);
				}
				// notify activity that it is time to check for responses from affected users
				this.setAcceptingResponses(true);
				this.setGameState(GameState.CHECKING_RESPONSES);
				break;

			// Here we handle responses. The game will go immediately to next state if MJ, Pong, Kong response.
			case CHECKING_RESPONSES:
				// check for MJ
				for (int i=0; i<this.numPlayers; i++) {
					// any other player can respond with Mahjong - Highest priority
					if ((i != this.playerTurn || this.numPlayers == 1) && this.player.get(i).checkMahjongResponseOpportunity()) {
						ResponseReceivedType resp = this.player.get(i).getPlayerResponse();
						// if player responded to MJ correctly
						if (this.player.get(i).checkUserMahjong(resp)) {
							this.acceptingResponses = false;
							this.resetPlayerInput();
							// loser is player who discarded the tile that was then used for MJ
							this.loserIdx = this.playerTurn;
							this.playerTurn = i;
							this.player.get(this.playerTurn).Mahjong(this.latestDiscard);
							this.setGameState(GameState.MAHJONG);
							return false;
						}
					}
				}
				// check for Kong first - Second highest priority
				for (int i=0; i<this.numPlayers; i++) {
					if ((i != this.playerTurn || this.numPlayers == 1) && this.player.get(i).checkKongResponseOpportunity()) {
						ResponseReceivedType resp = this.player.get(i).getPlayerResponse();
						if (this.player.get(i).checkUserKong(resp)) {
							this.setGameState(GameState.KONG);
							this.playerTurn = i;
							this.resetPlayerInput();
							return false;
						}
					}
				}
				// check for Pong - 3rd highest priority
				for (int i=0; i<this.numPlayers; i++) {
					if ((i != this.playerTurn || this.numPlayers == 1) && this.player.get(i).checkPongResponseOpportunity()) {
						ResponseReceivedType resp = this.player.get(i).getPlayerResponse();
						if (this.player.get(i).checkUserPong(resp)) {
							this.setGameState(GameState.PONG);
							this.playerTurn = i;
							this.resetPlayerInput();
							return false;
						}
					}
				}
				// check for Chow - Penultimate priority
				for (int i=0; i<this.numPlayers; i++) {
					if (i == next_player && this.player.get(i).checkChowResponseOpportunity()) {
						ResponseReceivedType resp = this.player.get(i).getPlayerResponse();
						if (this.player.get(i).checkUserChow(resp)) {
							this.setGameState(GameState.CHOW);
							this.playerTurn = i;
							this.resetPlayerInput();
							return false;
						}
					}
				}
				// check for Draw tile or Tse or drawing new tile - Lowest priority, only vald after a time has passed
				ResponseReceivedType nextPlayerResp = this.player.get(next_player).getPlayerResponse();
				if (this.player.get(next_player).checkUserDraw(nextPlayerResp) || this.player.get(next_player).checkUserTse(nextPlayerResp)) {
					// start timer if next player input received for draw or tse, and it was not started already
					if (!this.tseOrDrawCalled) {
						this.tseOrDrawCalled = true;
						this.setTseCalledTime();
						Log.d("timer analysis", "Time started at : " + this.getTseCalledTime() + " milliseconds.");
					}
					Log.d("timer analysis", "Time elapsed : " +this.getTseTimeElapsed() + " milliseconds.");
					// n milliseconds after next player input for another player to have chance to interrupt
					long ACCEPTING_RESPONSE_TIME_MS = 1000;
					if (this.getTseTimeElapsed() >= ACCEPTING_RESPONSE_TIME_MS) {
						if (this.player.get(next_player).checkUserDraw(nextPlayerResp)) {
							this.setGameState(GameState.DRAWING_TILE);
							this.playerTurn = next_player;
							this.resetPlayerInput();
						} else if (this.player.get(next_player).checkUserTse(nextPlayerResp)) {
							this.setGameState(GameState.TSE);
							this.playerTurn = next_player;
							this.resetPlayerInput();
						}
					}
				}
				break;

			case MAHJONG:
				this.gameState = GameState.GAME_OVER;
				this.winnerIdx = this.playerTurn;
				this.gameOutput = "Player " + this.playerTurn + ": Mahjong!";
				break;

			case KONG:
				this.player.get(this.playerTurn).kong(this.latestDiscard);
				this.gameOutput = "Player " + this.playerTurn + ": Kong!";
				this.gameState = GameState.DRAWING_TILE;
				break;

			case PONG:
				this.player.get(this.playerTurn).pong(this.latestDiscard);
				this.gameOutput = "Player " + this.playerTurn + ": Pong!";
				this.gameState = GameState.DISCARD_OPTIONS;
				break;

			case CHOW:
				// by this stage the chosenChowIdx has been set in player class by user input
				this.player.get(this.playerTurn).chow(this.latestDiscard);
				this.gameOutput = "Player " + this.playerTurn + ": Chow!";
				this.gameState = GameState.DISCARD_OPTIONS;
				break;

			case TSE:
				this.gameOutput = "Tse!";
				this.player.get(this.playerTurn).tse(this.latestDiscard);
				this.gameState = GameState.DISCARD_OPTIONS;
				break;

			case DRAWING_TILE:
				this.gameOutput = "Player " + this.playerTurn + ": drawing tile";
				// last tile drawn
				Tile tile_drawn = this.wall.revealWallTile();
				// check if any more tiles to draw
				if (!tile_drawn.checkRealTile()) {
					this.gameOutput = "No hidden tiles in deck left to uncover. Please restart the game";
					this.gameState = GameState.GAME_OVER;
				} else {
					// check hand for a Mahjong
					if (this.player.get(this.playerTurn).checkHandMahjong(tile_drawn)) {
						this.gameOutput = "Player " + this.playerTurn + ": Mahjong By Your Own Hand!";
						this.player.get(this.playerTurn).Mahjong(tile_drawn);
						this.gameState = GameState.MAHJONG;
					} else {
						// add tile to hand MJ or not
						//TODO: DEBUG
						Log.d("Game", "Game: DEBUG " + tile_drawn.getDescriptor());
						if (this.player.get(this.playerTurn).addToHand(tile_drawn) == HandStatus.ADD_SUCCESS) {
							this.gameState = GameState.DISCARD_OPTIONS;
						}
					}
				}
				break;

			case DISCARD_OPTIONS:
				System.out.println("Player " + this.playerTurn + ": discard a hidden tile");
				// set request response for the player discarding to be true
				ArrayList<ResponseRequestType> resp_options = new ArrayList<>();
				resp_options.add(ResponseRequestType.DISCARD);
				this.player.get(this.playerTurn).setResponseOpportunities(resp_options);
				this.gameState = GameState.DISCARDING_TILE;
				// allow players to send responses to Game from MultiplayerActivity
				this.setAcceptingResponses(true);
				break;

			// Discard a tile
			case DISCARDING_TILE:
				ResponseReceivedType discard_resp = this.player.get(this.playerTurn).getPlayerResponse();
				// if there is response
				int discard_idx = this.player.get(this.playerTurn).checkValidDiscardResponse(discard_resp);
				if (discard_idx != -1) {
					this.latestDiscard = this.player.get(this.playerTurn).discardTile(discard_idx);
					this.wall.addUncoveredTile(this.latestDiscard);
					//System.out.println("Player " + this.playerTurn + " discarded " + this.latestDiscard.getDescriptor());
					this.gameState = GameState.CHECKING_HAND;
					this.resetPlayerInput();
				}
				break;

			case GAME_OVER:
				System.out.println("Game over!\n");
				this.setGameStatus(GameStatus.FINISHED);
				return true;

			default:
				System.out.println("ERROR: Game should not have reached this state.");
				this.gameState = GameState.GAME_OVER;
				return true;
		}
		return false;
	}

	/*
		Reset these values once a user input has been received
	 */
	private void resetPlayerInput() {
		for (int i=0; i<this.numPlayers; i++) {
			this.player.get(i).clearResponseOpportunites();
			this.player.get(i).setPlayerResponse(ResponseReceivedType.NONE);
			this.setTseOrDrawCalled(false);
			this.setAcceptingResponses(false);
		}
	}

	/*
		One liners
	 */
	public Boolean gameExists() { return !this.getGameID().equals("NaN"); }
	private long getTseTimeElapsed() { return System.currentTimeMillis() - this.tseCalledTime; }
	public void setPlayerResponse(int player_idx, ResponseReceivedType resp) { this.player.get(player_idx).setPlayerResponse(resp); }
	public String getGameMessage(int player_idx) { return this.player.get(player_idx).getGameMessage(); }
	public void setGameMessage(int player_idx, String msg) { this.player.get(player_idx).setGameMessage(msg); }
	public void setPlayerPlayingStatus(boolean status, int playerIdx) { this.player.get(playerIdx).setPlayerPlaying(status); }

	/*
		Handling empty ArrayLists in Firebase
	 */
	void fillEmptyArrayLists() {
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
		// hidden and uncovered Tiles?
		if (wall.getHiddenTiles().isEmpty()) {
			wall.setHiddenTiles(emptyTileArrayList);
		}
		if (wall.getUncoveredTiles().isEmpty()) {
			wall.setUncoveredTiles(emptyTileArrayList);
		}
	}

	// remove empty values added in above function
	void cleanEmptyArrayLists() {
		Tile emptyTile = new Tile();
		// empty arraylist to set items to be
		ArrayList<Tile> emptyTileArrayList = new ArrayList<>();
		// empty arraylist pattern to find
		for (int i=0; i<this.numPlayers; i++) {
			// hand items hiddenHand and revealedHand
			Hand playerHand = this.player.get(i).getHand();
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
			this.wall.getHiddenTiles().clear();
		}
		if (checkEmptyArrayList(this.wall.getUncoveredTiles())) {
			Log.d("Game", "Game: Removing empty wall placeholder for revealed tiles");
			this.wall.getUncoveredTiles().clear();
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
		return this.player.get(p).getFlowersCollected();
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
		return this.player.get(p).getLatestFlowersCollectedDescriptor().toLowerCase().replace(" ", "_");
	}

	public int getFlowersCount(int playerIdx) {
		return this.player.get(playerIdx).getFlowersCount();
	}

	@Exclude
	public String getLatestDiscardedDescriptorResource() {
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

	/*

		Player & Game interaction functionality

	 */

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
		if (this.maxPlayers != this.numPlayers) {
			return false;
		} else {
			for (int i=0; i<this.maxPlayers; i++) {
				if (!this.player.get(i).getPlayerPlaying()) {
					return false;
				}
			}
			return true;
		}
	}

	public int countNumPlayersPlaying() {
		int num_playing = 0;
		for (int i=0; i<this.numPlayers; i++) {
			if (this.player.get(i).getPlayerPlaying()) {
				num_playing++;
			}
		}
		return num_playing;
	}

	public Boolean allPlayersJoined() {
		return this.maxPlayers == this.numPlayers;
	}

	private int getRandomPlayerID() {
		// this way of generating random number may not be truly random...
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
		ArrayList<Tile> start_tiles = new ArrayList<>();
		for (int i=0; i<this.player.size(); i++) {
			if (this.player.get(i).getPlayerUname().equals(name) || this.player.get(i).getPlayerUid().equals(uid)) {
				System.out.println("Cannot add player " + name + ", player already exists!");
				return;
			}
		}
		for (int j = 0; j < 13; j++) {
			start_tiles.add(this.wall.revealWallTile());
		}
		this.player.add(new Player(name, uid, this.numPlayers, start_tiles));
		this.numPlayers++;
	}

	public int getPlayerIdx(String uid) {
		// return index of player that matches name
		for (int i=0; i<this.player.size(); i++) {
			if (uid.equals(this.player.get(i).getPlayerUid())) {
				return this.player.get(i).getPlayerIdx();   // -1 to account for "names" as first value in ArrayList
			}
		}
		return -1;
	}

	public Boolean checkPlayerJoined(String uid) {
		// check that a player has joined the game
		for (int i=0; i<this.player.size(); i++) {
			if (this.player.get(i).getPlayerUid().equals(uid)) {
				Log.i("Game", "Game: Game player idx was found");
				return true;
			}
		}
		return false;
	}

	public ArrayList<ResponseRequestType> getPlayersResponsesOpportunities(int p) {
		return this.player.get(p).getResponseOpportunities();
	}

}

