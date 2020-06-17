package mahjong_package;

import java.util.ArrayList;

public class LikeAGame {

    // state of Game
    private GameState gameState;
    // Number of players playing the game
    private int numPlayers;

    public LikeAGame() { }

    // constructor
    //TODO:random initial player turn
    public LikeAGame(int n_players) {
        this.gameState = GameState.START;
        this.numPlayers = n_players;
    }

    public void setGameState(GameState g) { this.gameState = g; }
//    public void setPongAvailable(ArrayList<Boolean> b) { this.pongAvailable = b; }
//    public void setKongAvailable(ArrayList<Boolean> b) { this.kongAvailable = b; }
//    public void setMahjongAvailable(ArrayList<Boolean> b) { this.mahjongAvailable = b; }
//    public void setRequestResponse(ArrayList<Boolean> b) { this.requestResponse = b; }
//    public void setPlayerInput(ArrayList<String> s) { this.playerInput = s; }
//    public void setUpdateDiscardedTileImage(boolean b) { this.updateDiscardedTileImage = b; }
//    public void setLatestDiscard(Tile t) { this.latestDiscard = t; }
    public void setNumPlayers(int n) { this.numPlayers = n; }
//    public void setPlayer(ArrayList<Player> p) { this.player = p; }
//    public void setTiles(Tiles t) { this.tiles = t; }
//    public void setPlayerTurn(int t) { this.playerTurn = t; }
//    public void setWinnerIdx(int w) { this.winnerIdx = w; }
//    public void setLosGameStateerIdx(int l) { this.loserIdx = l; }
    public GameState getGameState() { return this.gameState; }
//    public ArrayList<Boolean> getPongAvailable() { return this.pongAvailable; }
//    public ArrayList<Boolean> getKongAvailable() { return this.kongAvailable; }
//    public ArrayList<Boolean> getMahjongAvailable() { return this.mahjongAvailable; }
//    public ArrayList<Boolean> getRequestResponse() { return this.requestResponse; }
//    public ArrayList<String> getPlayerInput() { return this.playerInput; }
//    public boolean getUpdateDiscardedTileImage() { return this.updateDiscardedTileImage; }
//    public Tile getLatestDiscard() { return this.latestDiscard; }
    public Integer getNumPlayers() { return this.numPlayers; }
//    public ArrayList<Player> getPlayer() { return this.player; }
//    public Tiles getTiles() { return this.tiles; }
//    public Integer getPlayerTurn() { return this.playerTurn; }
//    public Integer getWinnerIdx() { return this.winnerIdx; }
//    public Integer getLoserIdx() { return this.loserIdx; }

}
