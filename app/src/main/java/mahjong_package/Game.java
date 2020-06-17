package mahjong_package;


import java.util.ArrayList;


//TODO: incorrect data structures (too many ArrayLists) for quick start with FireBase. Improve this over time


public class Game {

	// state of Game
	private GameState gameState;
	// Number of players playing the game
	private int numPlayers;
	// Players playing game
	//private Player[] player = new Player[4];
	private ArrayList<Player> player = new ArrayList<>();
	// tile deck
	private Tiles tiles = new Tiles();
	// player's turn to discard etc
	private int playerTurn;
	// player who wins and player who gives them tile to win
	//TODO: implement this
	private int winnerIdx;
	private int loserIdx;
	// Users ability to pong/kong/mahjong
	private ArrayList<Boolean> pongAvailable = new ArrayList<>();
	private ArrayList<Boolean> kongAvailable = new ArrayList<>();
	private ArrayList<Boolean> mahjongAvailable = new ArrayList<>();
	// are we expecting a player's response?
	private ArrayList<Boolean> requestResponse = new ArrayList<>();
	// was the most recent response valid, pending?
	private ArrayList<String> playerInput = new ArrayList<>();
	// update discarded image tiles cue
	public boolean updateDiscardedTileImage;
	// latest tile discarded
	private Tile latestDiscard;

	// Game constructor - empty as required by Firebase
	public Game() {
		// defaults to 1 player
		this(1);
	}

	// constructor
	//TODO:random initial player turn
	public Game(int n_players) {
		Tile[] start_tiles = new Tile[13];
		this.latestDiscard = new Tile();
		this.updateDiscardedTileImage = false;
		this.gameState = GameState.START;
		this.numPlayers = n_players;
		this.playerTurn = 0;
		this.winnerIdx = -1;
		this.loserIdx = -1;
		// reset and shuffle deck
		this.tiles.shuffleTiles();
		// create hand for each player
		for (int i = 0; i < this.numPlayers; i++) {
			// draw 13 tiles and create player's hand
			for (int j = 0; j < 13; j++) {
				start_tiles[j] = this.tiles.revealTile();
			}
			this.player.add(new Player(i));
			this.player.get(i).createHand(start_tiles);
			this.playerInput.add("");
			this.requestResponse.add(false);
			this.pongAvailable.add(false);
			this.kongAvailable.add(false);
			this.mahjongAvailable.add(false);
		}
	}

	/*
		Public getters and setters for Firebase storing
	 */
	public void setGameState(GameState g) { this.gameState = g; }
	public void setPongAvailable(ArrayList<Boolean> b) { this.pongAvailable = b; }
	public void setKongAvailable(ArrayList<Boolean> b) { this.kongAvailable = b; }
	public void setMahjongAvailable(ArrayList<Boolean> b) { this.mahjongAvailable = b; }
	public void setRequestResponse(ArrayList<Boolean> b) { this.requestResponse = b; }
	public void setPlayerInput(ArrayList<String> s) { this.playerInput = s; }
	public void setUpdateDiscardedTileImage(boolean b) { this.updateDiscardedTileImage = b; }
	public void setLatestDiscard(Tile t) { this.latestDiscard = t; }
	public void setNumPlayers(int n) { this.numPlayers = n; }
	public void setPlayer(ArrayList<Player> p) { this.player = p; }
	public void setTiles(Tiles t) { this.tiles = t; }
	public void setPlayerTurn(int t) { this.playerTurn = t; }
	public void setWinnerIdx(int w) { this.winnerIdx = w; }
	public void setLoserIdx(int l) { this.loserIdx = l; }
	public GameState getGameState() { return this.gameState; }
	public ArrayList<Boolean> getPongAvailable() { return this.pongAvailable; }
	public ArrayList<Boolean> getKongAvailable() { return this.kongAvailable; }
	public ArrayList<Boolean> getMahjongAvailable() { return this.mahjongAvailable; }
	public ArrayList<Boolean> getRequestResponse() { return this.requestResponse; }
	public ArrayList<String> getPlayerInput() { return this.playerInput; }
	public boolean getUpdateDiscardedTileImage() { return this.updateDiscardedTileImage; }
	public Tile getLatestDiscard() { return this.latestDiscard; }
	public Integer getNumPlayers() { return this.numPlayers; }
	public ArrayList<Player> getPlayer() { return this.player; }
	public Tiles getTiles() { return this.tiles; }
	public Integer getPlayerTurn() { return this.playerTurn; }
	public Integer getWinnerIdx() { return this.winnerIdx; }
	public Integer getLoserIdx() { return this.loserIdx; }

	/*
	 * Return player index of winner.
	 */
	public void playGame() {

		//TODO: change playGame to return an int on game over. For now we game over.
		//TODO: remove all System.exit calls in package

		// return if game ended
		if (this.gameState == GameState.GAME_OVER) {
			return;
		}

		// return if no more tiles
		if (!this.tiles.tilesLeft()) {
			System.out.println("No more tiles left.\n");
			this.gameState = GameState.GAME_OVER;
		}

		// return if we are waiting on any user for game input? If so do not proceed with game.
		for (int i = 0; i<this.numPlayers; i++) {
			if (this.requestResponse.get(i)) {
				return;
			}
		}

		int next_player = (this.playerTurn + 1) % this.numPlayers;

		switch (this.gameState) {

			case START:
				System.out.println("Welcome to the game\n");
				this.gameState = GameState.DRAWING_TILE;
				break;

			case CHECKING_HAND:
				// check if players can Mahjong, Kong or Pong.
				for (int i = 0; i<this.numPlayers; i++) {
					// player cannot interrupt their own turn
					if (i != this.playerTurn) {
						// check for Mahjong
						if (this.player.get(i).checkHandMahjong(this.latestDiscard)) {
							this.mahjongAvailable.set(i, true);
							this.requestResponse.set(i, true);
							System.out.println("Player " + i + ": Mahjong? \t1=Mahjong\tother=No");
						} else if (this.player.get(i).checkHandKong(this.latestDiscard)) {
							this.kongAvailable.set(i, true);
							this.requestResponse.set(i, true);
							System.out.println("Player " + i + ": Kong? \t1=Kong\tother=No");
						} else if (this.player.get(i).checkHandPong(this.latestDiscard)) {
							this.pongAvailable.set(i, true);
							this.requestResponse.set(i, true);
							this.player.get(i).showHiddenHand();
						}
					}
				}

				// next player will also have chance to tse
				this.requestResponse.set(next_player, true);
				System.out.println("Player " + next_player + ": Tse? \t1=Tse\tother=No");
				this.gameState = GameState.CHECKING_RESPONSES;
				break;

			case CHECKING_RESPONSES:
				// it is in this state that we allow the users to respond over a certain time
				//TODO: make sure not biased towards Player 0...
				boolean tse_opportunity = true;
				for (int i = 0; i<this.numPlayers; i++) {
					// skip for current player
					if (this.playerTurn != i) {
						if (this.mahjongAvailable.get(i) && this.player.get(i).checkUserMahjong(this.playerInput.get(i))) {
							this.gameState = GameState.MAHJONG;
							this.playerTurn = i;
							tse_opportunity = false;
							break;	// stop for loop of players early
						} else if (this.kongAvailable.get(i) && this.player.get(i).checkUserKong(this.playerInput.get(i))) {
							this.gameState = GameState.KONG;
							this.playerTurn = i;
							tse_opportunity = false;
						} else if (this.pongAvailable.get(i) && this.player.get(i).checkUserPong(this.playerInput.get(i))) {
							// if not kong already
							if (this.gameState != GameState.KONG) {
								this.gameState = GameState.PONG;
								this.playerTurn = i;
								tse_opportunity = false;
							}
						}
					}
				}
				if (tse_opportunity) {
					// check if next player Tse
					if (this.playerInput.get(next_player).equals("1")) {
						this.playerTurn = next_player;
						this.gameState = GameState.TSE;
					} else {
						this.gameState = GameState.DRAWING_TILE;
					}
					this.playerTurn = next_player;
				}
				// reset all available booleans when we leave here
				this.reset_interrupt_chances();
				break;

			case MAHJONG:
				this.gameState = GameState.GAME_OVER;
				System.out.println("Player " + this.playerTurn + ": Mahjong!");
				break;

			case KONG:
				this.player.get(this.playerTurn).kong(this.latestDiscard);
				System.out.println("Player " + this.playerTurn + ": Kong!");
				this.gameState = GameState.DRAWING_TILE;
				break;

			case PONG:
				this.player.get(this.playerTurn).pong(this.latestDiscard);
				System.out.println("Player " + this.playerTurn + ": Pong!");
				this.gameState = GameState.DISCARD_OPTIONS;
				break;

			case TSE:
				System.out.println("Tse!");
				this.player.get(this.playerTurn).tse(this.latestDiscard);
				this.gameState = GameState.DISCARD_OPTIONS;
				break;

			case DRAWING_TILE:
				System.out.println("Player " + this.playerTurn + ": drawing tile");
				// last tile drawn
				Tile tile_drawn = this.tiles.revealTile();
				// check hand for a Mahjong
				//TODO: a user response to confirm/notice this
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
				System.out.println("Player " + this.playerTurn + ": discard a tile from the following:");
				this.player.get(this.playerTurn).showHiddenHand();
				this.requestResponse.set(this.playerTurn, true);
				this.gameState = GameState.DISCARDING_TILE;
				break;

			// Discard a tile
			case DISCARDING_TILE:
				this.latestDiscard = this.player.get(this.playerTurn).discardTile(this.playerInput.get(this.playerTurn));

				if (this.latestDiscard.getDescriptor().equals("No tile")) {
					System.out.println("No tile discarded yet");
				} else {
					this.tiles.addUncoveredTile(this.latestDiscard);
					System.out.println("Player " + this.playerTurn + " discarded " + this.latestDiscard.getDescriptor());
					this.updateDiscardedTileImage = true;
					this.gameState = GameState.CHECKING_HAND;
				}
				break;

			case GAME_OVER:
				System.out.println("Game over!\n");
				break;

			default:
				System.out.println("ERROR: Game should not have reached this state.");
				this.gameState = GameState.GAME_OVER;
				break;
		}

	}


	private void reset_interrupt_chances() {

		for (int i=0; i<4; i++) {
			this.pongAvailable.set(i, false);
			this.kongAvailable.set(i, false);
			this.mahjongAvailable.set(i, false);
		}

	}


	public String getDiscardedDescriptor() {
		return this.latestDiscard.getDescriptor().toLowerCase().replace(" ", "_");
	}


	public int getTurn() { return this.playerTurn; }


	public ArrayList<String> getRevealedDescriptors() {
		return this.player.get(playerTurn).getRevealedDescriptors();
	}


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
				System.out.println("Passed test " + i);
			} else {
				this.player.get(0).showHiddenHand();
				System.out.println(disc_tile.getDescriptor());
				System.out.println("Failed test " + i);
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
				System.out.println("Passed test " + i);
			} else {
				this.player.get(0).showHiddenHand();
				System.out.println(disc_tile.getDescriptor());
				System.out.println("Failed test " + i);
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
				System.out.println("Passed test " + i);
			} else {
				this.player.get(0).showHiddenHand();
				System.out.println(disc_tile.getDescriptor());
				System.out.println("Failed test " + i);
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
				System.out.println("Passed test " + i);
			} else {
				this.player.get(0).showHiddenHand();
				System.out.println(disc_tile.getDescriptor());
				System.out.println("Failed test " + i);
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
				System.out.println("Passed test " + i);
			} else {
				this.player.get(0).showHiddenHand();
				System.out.println(disc_tile.getDescriptor());
				System.out.println("Failed test " + i);
				return false;
			}
		}

		return true;
	}


	//TODO: resetGame (if not already existing somewhere in the code)

	//TODO: get revealed descriptor
}

