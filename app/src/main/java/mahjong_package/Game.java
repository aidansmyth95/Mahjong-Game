package mahjong_package;

import android.widget.TextView;

//TODO: I know this is not good practce for TextView access in classes. But it's a start for now. Improve this with interface later

public class Game {

	// latest tile discarded
	Tile latest_discard;

	// Number of players playing the game
	private int num_players;

	// Players playing game
	private Player[] player = new Player[4];

	// tile deck
	private Tiles tiles = new Tiles();

	// player's turn to discard etc
	private int player_turn;

	// player who wins and player who gives them tile to win
	private int winner_idx;
	private int loser_idx;

	private long start_time, curr_time;

	private final long interrupt_time_ms = 3000;

	public GameStatus gs = new GameStatus();


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

		System.out.println("Are you ready?! Let's play Mahjong!\n");

		// first player will start in Drawing tile state


		this.latest_discard = new Tile();
		this.start_time = 0;
		this.curr_time = 0;
	}


	/*
	 * Return player index of winner.
	 */
	public void playGame() {

		// return if we are waiting on any user for game input? If so do not proceed with game.
		for (int i=0; i<this.num_players; i++) {
			if (this.gs.request_response[i] == true) {
				return;
			}
		}

		//TODO: only if tiles let
		//this.tiles.tilesLeft()

		switch (this.gs.gameState) {

			//TODO: not needed? Needed for stuff we don't do in constructor?
			case START:
				System.out.println("Welcome to the game\n");
				this.start_time = System.nanoTime();
				this.gs.gameState = GameState.DRAWING_TILE;
				break;


			case CHECKING_HAND:
				boolean tse;
				int next_player = (this.player_turn + 1) % this.num_players;

				for (int i = 0; i < this.num_players; i++) {
					// player cannot interrupt their own turn
					if (i != this.player_turn) {
						// check for Mahjong
						if (this.player[i].Mahjong(this.latest_discard)) {
							//this.loser_idx = this.player_turn;
							this.gs.gameState = GameState.MAHJONG;
						} else if (this.player[i].checkHandKong(this.latest_discard)) {
							// check user input if Kong chance has been accepted
							if (this.player[this.player_turn].checkUserKong(this.gs.player_input[this.player_turn])) {
								// game moves to Kong state
								this.gs.gameState = GameState.KONG;
							}
						} else if (this.player[i].checkHandPong(this.latest_discard)) {
							// check user input if Pong chance has been accepted
							if (this.player[this.player_turn].checkUserPong(this.gs.player_input[this.player_turn])) {
								// game moves to Pong state
								this.gs.gameState = GameState.PONG;
							}
						}
					}
				}
				// update GameStatus to reflect the results of the Mahjong, Kong and Pong checks.
				this.curr_time = System.nanoTime() - this.start_time;
				if (this.curr_time > this.interrupt_time_ms) {
					// check game state and move on
					if (this.gs.player_input[next_player].equals("Tse")) {
						this.gs.gameState = GameState.TSE;
					}
					this.player_turn = next_player;
					this.gs.gameState = GameState.DRAWING_TILE;
				}
				break;

			case MAHJONG:
				//this.player[this.player_turn].Mahjong(this.latest_discard);
				this.gs.gameState = GameState.GAME_OVER;
				break;

			case KONG:
				this.player[this.player_turn].kong(this.latest_discard);
				this.gs.gameState = GameState.DRAWING_TILE;
				break;

			case PONG:
				this.player[this.player_turn].pong(this.latest_discard);
				this.gs.gameState = GameState.DISCARDING_TILE;
				break;

			case TSE:
				this.player[this.player_turn].tse(this.latest_discard, this.gs.player_input[this.player_turn]);
				this.gs.gameState = GameState.DISCARDING_TILE;
				break;

			case DRAWING_TILE:
				System.out.println("Player " + this.player_turn + " drawing tile");
				// last tile drawn
				Tile tile_drawn = this.tiles.revealTile();


				// check for a Mahjong
				if (this.player[this.player_turn].Mahjong(tile_drawn)) {
					this.gs.gameState = GameState.MAHJONG;
				} else {
					System.out.println("Player " + this.player_turn + " must now discard a tile from the following:");
					this.player[this.player_turn].showHand();
					this.gs.gameState = GameState.DISCARDING_TILE;
					this.gs.request_response[this.player_turn] = true;
				}
				// add tile to hand MJ or not
				this.player[this.player_turn].addToHand(tile_drawn);

				break;

			// Discard a tile
			case DISCARDING_TILE:
				this.latest_discard = this.player[this.player_turn].discardTile(this.gs.player_input[this.player_turn]);

				if (this.latest_discard.descriptor.equals("No tile")) {
					System.out.println("No tile discarded yet");
				} else {
					this.tiles.uncovered_tiles.add(this.latest_discard);
					System.out.println("Player " + this.player_turn + " discarded " + this.latest_discard.descriptor);
					this.gs.gameState = GameState.CHECKING_HAND;
					this.start_time = System.nanoTime();
					/*try {
						Thread.sleep(100000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}*/
				}
				break;

			case GAME_OVER:
				System.out.println("Game over!\n");
				break;

			default:
				System.out.println("ERROR: Game should not have reached this state.");
				System.exit(0);
				break;
		}

	}

}

