package mahjong_package;

import java.util.Scanner;


public class Game {
	
	private final int num_players = 4;
		
	// Scanner for player interaction with game
	private Scanner scan = new Scanner(System.in);
	
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
	
	// constructor	//TODO: num_players defined here?
	public Game() {
		// temporary array for hand tiles drawn
		Tile start_tiles[] = new Tile[13];

		// player IDs and winner assigned
		//TODO:random initial player turn
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
		
		// most recently discarded Tile
		Tile latest = new Tile();
		
		// hard set tile to be discarded to be index 13 (14th and last tile)
		int discard_idx = 13;
				
		// while no winner and still hidden tiles
		while (this.winner_idx == -1 && this.tiles.tilesLeft() == true) {
			
			// chance for all players to interrupt
			//TODO: implement concurrently, not in for loop. Unfair on last player
			if (this.interrupt_chance == 1) {
				this.interrupt_idx = -1;
				
				for (int i=0; i<this.num_players; i++) {
					// player cannot interrupt their own turn
					if (i != this.player_turn) {
						// TODO: check for a potential Mahjong
						if (this.player[i].checkMahjong(latest) == true) {
							this.interrupt_idx = i;
							this.interrupt_type = PlayerInterruptStates.MAHJONG;
							break;
						} else if (this.player[i].pong(latest)) {
							// check for a potential Pong
							System.out.println("\nPlayer " + i + ": Pong? (y/n)");
							String resp = this.scan.nextLine();
							if (resp.toLowerCase().equals("y") || resp.toLowerCase().equals("yes")) {
								this.interrupt_idx = i;
								this.interrupt_type = PlayerInterruptStates.PONG;
								break;
							}
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
					
					//TODO: check for a Mahjong
					
					this.player[this.player_turn].addToHand(tile_drawn);
					// perform an action based on tile drawn
					this.player[this.player_turn].state = PlayerState.DISCARDING_TILE;
					break;
					
				case DISCARDING_TILE:
					// Discard a tile
					latest = this.player[this.player_turn].discardTile(discard_idx);
					this.tiles.uncovered_tiles.add(latest);
					System.out.println("Player " + this.player_turn + " discarding " + latest.descriptor);
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
						
						//TODO: a TSE state
						
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
						//TODO: who is the loser? Who gave player the tile to win? 
					} else if (this.interrupt_type == PlayerInterruptStates.PONG){
						// they will need to discard if they did not win
						//TODO: pong must reveal tiles
						this.player[this.player_turn].state = PlayerState.DISCARDING_TILE;
					} else if (this.interrupt_type == PlayerInterruptStates.TSE) {
						//TODO: Tse functionality - any needed once tile already claimed?
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
		Game game = new Game();
		
		// Play the game
		int win;
		win = game.playGame();
				
		System.out.println("End of game, winner is " + win + "\n");
		return;
	}
}

