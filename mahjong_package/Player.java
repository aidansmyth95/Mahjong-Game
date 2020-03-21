package mahjong_package;

public class Player {
	
	// player's hand
	private Hand hand = new Hand();
			
	
	// constructor
    public Player(int player_ID)
    {

        // assert ID is valid and assign
        isValid(player_ID);
        this.state = PlayerState.WAITING;
    }
    
    
    // player ID valid
	public boolean isValid(int player_ID) {
		if (player_ID < 4) {
			return true;
		}
		else {
			return false;
		}
	}
	
	// create hand for player
	public void createHand(Tile[] tiles_drawn) {
		this.hand.createHand(tiles_drawn);
	}
	
	
	// check for win
	public boolean mahjong() {
		return this.hand.checkWin();
	}
	
	public void showHand() {
		this.hand.showHand();
	}
	
	public void addToHand(Tile tile) {
		this.hand.addToHand(tile);
	}
	
	public Tile discardTile(int idx) {
		return this.hand.discardTile(idx);
	}
	
	// player's turn to draw a tile
	public PlayerState state;
}







// a player has a hand of hand_size tiles at start

// a player can draw a tile from the deck. When drawing player can choose to keep or discard tile

// player scores hand's likeliness of producing a win, or hand's closeness to a win

// player also has an aim based on probability for different combinations of triples, pairs and sequences
