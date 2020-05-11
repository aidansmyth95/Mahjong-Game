package mahjong_package;


public class GameStatus {

    GameState gameState;

    // are we expecting a player's response?
    public boolean request_response[] = new boolean[4];

    // was the most recent response valid, pending?
    public String player_input[] = new String[4];

    public String game_output[] = new String[4];

    // constructor
    public GameStatus() {

        // for every player
        for (int i=0; i<4; i++) {
            this.player_input[i] = "";
            this.game_output[i] = "";
            this.request_response[i] = false;
        }

        // for game
        this.gameState = GameState.START;
    }
}
