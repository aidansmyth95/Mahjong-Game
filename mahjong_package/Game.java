package mahjong_package;


public class Game {
	
	// Number of players playing the game
	private int num_players;
			
	// Players playing game
	private Player player[] = new Player[4];
	
	// tile deck
	private Tiles tiles = new Tiles();
	
	// last tile drawn
	private Tile tile_drawn = new Tile();
	
	// player's turn to discard etc
	private int player_turn;
	
	// opportunity to scan interrupts
	private int interrupt_chance;
	// Player ID who interrupted
	private int interrupt_idx;
	private PlayerInterruptStates interrupt_type;
	
	// player who wins and player who gives them tile to win
	private int winner_idx;
	private int loser_idx;
	
	// constructor
	public Game(int n_players) {
		// temporary array for hand tiles drawn
		Tile start_tiles[] = new Tile[13];

		// player IDs and winner assigned
		//TODO:random initial player turn
		this.num_players = n_players;
		this.player_turn = 0;
		this.winner_idx = -1;
		this.loser_idx = -1;
		this.interrupt_chance = 0;
		this.interrupt_idx = -1;
		this.interrupt_type = PlayerInterruptStates.NONE;
		
		// reset and shuffle deck
		this.tiles.shuffleTiles();
		
		// for each player...
		for (int i=0; i<this.num_players; i++) {
			this.player[i] = new Player(i);
			// draw 13 tiles and create player's hand
			for (int j=0; j<13; j++) {
				start_tiles[j] = tiles.revealTile();
			}
			this.player[i].createHand(start_tiles);
		}
		
		System.out.println("Are you ready?! Let's play Mahjong!\n");
	}
	
	
	/*
	 * Return player index of winner.
	 */
	public int playGame() {
		
		// first player will start in Drawing tile state
		this.player[this.player_turn].state = PlayerState.DRAWING_TILE;
		
		// most recently discarded Tile
		Tile latest = new Tile();
						
		// while no winner and still hidden tiles
		while (this.winner_idx == -1 && this.tiles.tilesLeft() == true) {
			
			// chance for all players to interrupt
			//TODO: implement concurrently, not in for loop. Unfair on last player
			if (this.interrupt_chance == 1) {
				System.out.println("Checking for interrupt...");
				this.interrupt_idx = -1;
				
				for (int i=0; i<this.num_players; i++) {
					// player cannot interrupt their own turn
					if (i != this.player_turn) {
						// check for Mahjong
						System.out.printf("Player %d: Checking MJ and pong\n", i);
						if (this.player[i].Mahjong(latest)) {
							this.interrupt_idx = i;
							this.loser_idx = this.player_turn;
							this.interrupt_type = PlayerInterruptStates.MAHJONG;
							break;
						} else if (this.player[i].kong(latest)){
							this.interrupt_idx = i;
							this.interrupt_type = PlayerInterruptStates.KONG;
						} else if (this.player[i].pong(latest)) {
							this.interrupt_idx = i;
							this.interrupt_type = PlayerInterruptStates.PONG;
							break;
						} else {
							// no interrupt from this player, move on
						}
					}
				}
				this.interrupt_chance = 0;
			}
			
			// function to start with player who's turn it is
			switch (this.player[this.player_turn].state) {

				case DRAWING_TILE:
					// draw a tile
					System.out.println("Player " + this.player_turn + " drawing tile");
					this.tile_drawn = this.tiles.revealTile();
					
					// check for a Mahjong
					if (this.player[this.player_turn].Mahjong(this.tile_drawn) == true) {
						this.interrupt_type = PlayerInterruptStates.MAHJONG;
					}
					
					this.player[this.player_turn].addToHand(tile_drawn);
					// perform an action based on tile drawn
					this.player[this.player_turn].state = PlayerState.DISCARDING_TILE;
					break;
					
				case DISCARDING_TILE:
					// Discard a tile
					latest = this.player[this.player_turn].discardTile();
					this.tiles.uncovered_tiles.add(latest);
					System.out.println("Player " + this.player_turn + " discarded " + latest.descriptor);
					this.interrupt_chance = 1;
					this.player[this.player_turn].state = PlayerState.WAITING;
					break;
					
				// the current player will wait here until they are given their turn again or interrupt
				// all players have had a chance to claim discarded tile
				case WAITING:					
					// next player's turn determined by interrupt or not
					if (this.interrupt_idx != -1) {
						this.player_turn = this.interrupt_idx;
						this.player[this.player_turn].state = PlayerState.PROCESS_INTERRUPT;
					} else {
						this.player_turn = (this.player_turn + 1) % this.num_players;
						
						// ask player if they want the recently uncovered/discarded tile
						System.out.printf("Player %d: Tse? [y/n]\n", this.player_turn);
						String resp = this.player[this.player_turn].scan.nextLine();
						if (resp.toLowerCase().equals("y")) {
							// proceed with Tse - update latest discarded tile
							latest = this.player[this.player_turn].tse(latest);
						} else {
							// no tse 
						}
						
						this.player[this.player_turn].state = PlayerState.DRAWING_TILE;
					}					
					break;
					
				// a player just interrupted. Process their request here
				case PROCESS_INTERRUPT:
					// take most recent tile for player's hand
					Tile claimed = this.tiles.claimLatestTile();
					//error check
					if (claimed.descriptor.equals(latest.descriptor)) {
						// all good, add to hand
						this.player[this.player_turn].addToHand(claimed);
						System.out.println("Player " + this.player_turn + " claims " + claimed.descriptor);
					}
					else {
						// error, fix this
						System.out.println("Error in latest discareded tile not equalling claimed\n");
						System.out.println(claimed.descriptor + " != " + latest.descriptor);
						System.exit(0);
					}
					
					// was it a Mahjong or a Pong?
					if (this.interrupt_type == PlayerInterruptStates.MAHJONG) {
						this.player[this.player_turn].state = PlayerState.WINNER;
						this.player[this.loser_idx].state = PlayerState.LOSER;
					} else if (this.interrupt_type == PlayerInterruptStates.KONG) {
						// they will need to draw a tile before discarding a tile
						this.player[this.player_turn].state = PlayerState.DRAWING_TILE;
					} else if (this.interrupt_type == PlayerInterruptStates.PONG){
						// they will need to discard if they did not win
						this.player[this.player_turn].state = PlayerState.DISCARDING_TILE;
					} else if (this.interrupt_type == PlayerInterruptStates.TSE) {
						// Player has interrupted with Tse
						latest = this.player[this.player_turn].tse(latest);
						this.tiles.uncovered_tiles.add(latest);
						System.out.printf("Player %d: discarding %s\n", this.player_turn, latest.descriptor);
						this.interrupt_chance = 1;
						this.player[this.player_turn].state = PlayerState.WAITING;
						break;
					} else {
						System.out.println("Interrupt state not supported");
						System.exit(0);
					}
					break;
					
				case WINNER:
					// Winner, game over! Wait here until game restarted
					System.out.println("Player " + this.player_turn + " has won the game!\n");
					System.out.println("They had the following hand. ");
					this.player[this.player_turn].showHand();
					this.winner_idx = this.player_turn;
					break;
					
				case LOSER:
					// Loser, game over. Wait here until game is restarted
					if (this.loser_idx != -1) {
						System.out.println("Player " + this.player_turn + " has lost the game!\n");
					}
					break;
					
				default:
					// ERROR, message containing state
					break;
			}
		}
		
		return this.winner_idx;
	}
	
	
	
	/*
	 * Main function
	 */
	public static void main(String[] args) {
		
		// Create game
		Game game = new Game(4);
		
		// Play the game
		int win;
		win = game.playGame();
				
		System.out.println("End of game, winner is " + win + "\n");
		return;
	}
}

