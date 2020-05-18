package mahjong_package;



public class Game {

	private GameState gameState;

	private boolean[] pong_available = new boolean[4];

	private boolean[] kong_available = new boolean[4];

	private boolean[] mahjong_available = new boolean[4];

	// are we expecting a player's response?
	public boolean[] request_response = new boolean[4];

	// was the most recent response valid, pending?
	public String[] player_input = new String[4];

	public boolean update_discarded_tile_image;

	// latest tile discarded
	private Tile latest_discard;

	// Number of players playing the game
	private int num_players;

	// Players playing game
	private Player[] player = new Player[4];

	// tile deck
	private Tiles tiles = new Tiles();

	// player's turn to discard etc
	private int player_turn;

	// player who wins and player who gives them tile to win
	//TODO: implement this
	private int winner_idx;
	private int loser_idx;


	// constructor
	public Game(int n_players) {

		// temporary array for hand tiles drawn
		Tile[] start_tiles = new Tile[13];

		// player IDs and winner assigned
		//TODO:random initial player turn
		this.num_players = n_players;
		this.player_turn = 0;
		this.winner_idx = -1;
		this.loser_idx = -1;

		// reset and shuffle deck
		this.tiles.shuffleTiles();

		// for each player...
		for (int i = 0; i < this.num_players; i++) {
			this.player[i] = new Player(i);
			// draw 13 tiles and create player's hand
			for (int j = 0; j < 13; j++) {
				start_tiles[j] = tiles.revealTile();
			}
			this.player[i].createHand(start_tiles);
		}
		this.latest_discard = new Tile();
		this.update_discarded_tile_image = false;

		// for every player
		for (int i=0; i<4; i++) {
			this.player_input[i] = "";
			this.request_response[i] = false;
			this.pong_available[i] = false;
			this.kong_available[i] = false;
			this.mahjong_available[i] = false;
		}

		// for game
		this.gameState = GameState.START;
	}


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
		for (int i=0; i<this.num_players; i++) {
			if (this.request_response[i]) {
				return;
			}
		}

		int next_player = (this.player_turn + 1) % this.num_players;

		switch (this.gameState) {

			case START:
				System.out.println("Welcome to the game\n");
				this.gameState = GameState.DRAWING_TILE;
				break;

			case CHECKING_HAND:
				// check if players can Mahjong, Kong or Pong.
				for (int i=0; i<this.num_players; i++) {
					// player cannot interrupt their own turn
					if (i != this.player_turn) {
						// check for Mahjong
						if (this.player[i].checkHandMahjong(this.latest_discard)) {
							this.mahjong_available[i] = true;
							this.request_response[i] = true;
							System.out.println("Player " + i + ": Mahjong? \t1=Mahjong\tother=No");
						} else if (this.player[i].checkHandKong(this.latest_discard)) {
							this.kong_available[i] = true;
							this.request_response[i] = true;
							System.out.println("Player " + i + ": Kong? \t1=Kong\tother=No");
						} else if (this.player[i].checkHandPong(this.latest_discard)) {
							this.pong_available[i] = true;
							this.request_response[i] = true;
							this.player[i].showHand();
						}
					}
				}

				// next player will also have chance to tse
				this.request_response[next_player] = true;
				System.out.println("Player " + next_player + ": Tse? \t1=Tse\tother=No");
				this.gameState = GameState.CHECKING_RESPONSES;
				break;

			case CHECKING_RESPONSES:
				// it is in this state that we allow the users to respond over a certain time
				//TODO: make sure not biased towards Player 0...
				boolean tse_opportunity = true;
				for (int i=0; i<this.num_players; i++) {
					// skip for current player
					if (this.player_turn != i) {
						if (this.mahjong_available[i] && this.player[i].checkUserMahjong(this.player_input[i])) {
							this.gameState = GameState.MAHJONG;
							this.player_turn = i;
							tse_opportunity = false;
							break;	// stop for loop of players early
						} else if (this.kong_available[i] && this.player[i].checkUserKong(this.player_input[i])) {
							this.gameState = GameState.KONG;
							this.player_turn = i;
							tse_opportunity = false;
						} else if (this.pong_available[i] && this.player[i].checkUserPong(this.player_input[i])) {
							// if not kong already
							if (this.gameState != GameState.KONG) {
								this.gameState = GameState.PONG;
								this.player_turn = i;
								tse_opportunity = false;
							}
						}
					}
				}
				if (tse_opportunity) {
					// check if next player Tse
					if (this.player_input[next_player].equals("1")) {
						this.player_turn = next_player;
						this.gameState = GameState.TSE;
					} else {
						this.gameState = GameState.DRAWING_TILE;
					}
					this.player_turn = next_player;
				}
				// reset all available booleans when we leave here
				this.reset_interrupt_chances();
				break;

			case MAHJONG:
				this.gameState = GameState.GAME_OVER;
				System.out.println("Player " + this.player_turn + ": Mahjong!");
				break;

			case KONG:
				this.player[this.player_turn].kong(this.latest_discard);
				System.out.println("Player " + this.player_turn + ": Kong!");
				this.gameState = GameState.DRAWING_TILE;
				break;

			case PONG:
				this.player[this.player_turn].pong(this.latest_discard);
				System.out.println("Player " + this.player_turn + ": Pong!");
				this.gameState = GameState.DISCARD_OPTIONS;
				break;

			case TSE:
				System.out.println("Tse!");
				this.player[this.player_turn].tse(this.latest_discard);
				this.gameState = GameState.DISCARD_OPTIONS;
				break;

			case DRAWING_TILE:
				System.out.println("Player " + this.player_turn + ": drawing tile");
				// last tile drawn
				Tile tile_drawn = this.tiles.revealTile();
				// check hand for a Mahjong
				//TODO: a user response to confirm/notice this
				if (this.player[this.player_turn].checkHandMahjong(tile_drawn)) {
					System.out.println("Player " + this.player_turn + ": Mahjong By Your Own Hand!");
					this.gameState = GameState.MAHJONG;
				} else {
					// add tile to hand MJ or not
					this.player[this.player_turn].addToHand(tile_drawn);
					this.gameState = GameState.DISCARD_OPTIONS;
				}
				break;

			case DISCARD_OPTIONS:
				System.out.println("Player " + this.player_turn + ": discard a tile from the following:");
				this.player[this.player_turn].showHand();
				this.request_response[this.player_turn] = true;
				this.gameState = GameState.DISCARDING_TILE;
				break;

			// Discard a tile
			case DISCARDING_TILE:
				this.latest_discard = this.player[this.player_turn].discardTile(this.player_input[this.player_turn]);

				if (this.latest_discard.descriptor.equals("No tile")) {
					System.out.println("No tile discarded yet");
				} else {
					this.tiles.uncovered_tiles.add(this.latest_discard);
					System.out.println("Player " + this.player_turn + " discarded " + this.latest_discard.descriptor);
					this.update_discarded_tile_image = true;
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
			this.pong_available[i] = false;
			this.kong_available[i] = false;
			this.mahjong_available[i] = false;
		}

	}


	public String getDiscardedDescriptor() {
		return this.latest_discard.descriptor.toLowerCase().replace(" ", "_");
	}


	public int getTurn() { return this.player_turn; }


	public String getHandDescriptor(int idx) {
		// Descriptor example = Dragon Green
		// We want dragon_green
		// lowercase all and replace space with _
		String in = this.player[this.player_turn].getDescriptor(idx);
		return in.toLowerCase().replace(" ", "_");
	}


	public boolean test_true_pong() {

		for (int i=0; i<2; i++) {
			Tile[] all_tiles = this.player[0].createTruePongTestVectorHand(i);
			Tile disc_tile = all_tiles[13];
			Tile[] hand_tiles = new Tile[13];
			System.arraycopy(all_tiles, 0, hand_tiles, 0, 13);

			this.player[0].createHand(hand_tiles);
			boolean success = this.player[0].checkHandPong(disc_tile);

			if (success) {
				System.out.println("Passed test " + i);
			} else {
				this.player[0].showHand();
				System.out.println(disc_tile.descriptor);
				System.out.println("Failed test " + i);
				return false;
			}
		}

		return true;
	}


	public boolean test_false_pong() {

		for (int i=0; i<2; i++) {
			Tile[] all_tiles = this.player[0].createFalsePongTestVectorHand(i);
			Tile disc_tile = all_tiles[13];
			Tile[] hand_tiles = new Tile[13];
			System.arraycopy(all_tiles, 0, hand_tiles, 0, 13);

			this.player[0].createHand(hand_tiles);
			boolean success = this.player[0].checkHandPong(disc_tile);

			if (!success) {
				System.out.println("Passed test " + i);
			} else {
				this.player[0].showHand();
				System.out.println(disc_tile.descriptor);
				System.out.println("Failed test " + i);
				return false;
			}
		}

		return true;
	}


	public boolean test_true_kong() {

		for (int i=0; i<2; i++) {
			Tile[] all_tiles = this.player[0].createTrueKongTestVectorHand(i);
			Tile disc_tile = all_tiles[13];
			Tile[] hand_tiles = new Tile[13];
			System.arraycopy(all_tiles, 0, hand_tiles, 0, 13);

			this.player[0].createHand(hand_tiles);
			boolean success = this.player[0].checkHandKong(disc_tile);

			if (success) {
				System.out.println("Passed test " + i);
			} else {
				this.player[0].showHand();
				System.out.println(disc_tile.descriptor);
				System.out.println("Failed test " + i);
				return false;
			}
		}

		return true;
	}


	public boolean test_false_kong() {

		for (int i=0; i<2; i++) {
			Tile[] all_tiles = this.player[0].createFalseKongTestVectorHand(i);
			Tile disc_tile = all_tiles[13];
			Tile[] hand_tiles = new Tile[13];
			System.arraycopy(all_tiles, 0, hand_tiles, 0, 13);
			this.player[0].createHand(hand_tiles);

			boolean success = this.player[0].checkHandKong(disc_tile);

			if (!success) {
				System.out.println("Passed test " + i);
			} else {
				this.player[0].showHand();
				System.out.println(disc_tile.descriptor);
				System.out.println("Failed test " + i);
				return false;
			}
		}

		return true;
	}


	public boolean test_true_mahjong() {

		for (int i=0; i<2; i++) {
			Tile[] all_tiles = this.player[0].createTrueMahjongTestVectorHand(i);

			Tile disc_tile = all_tiles[13];
			Tile[] hand_tiles = new Tile[13];
			System.arraycopy(all_tiles, 0, hand_tiles, 0, 13);

			this.player[0].createHand(hand_tiles);
			boolean success = this.player[0].checkHandMahjong(disc_tile);

			if (success) {
				System.out.println("Passed test " + i);
			} else {
				this.player[0].showHand();
				System.out.println(disc_tile.descriptor);
				System.out.println("Failed test " + i);
				return false;
			}
		}

		return true;
	}


	//TODO: resetGame (if not already existing somewhere in the code)
}

