package mahjong_package;

public enum GameStatus {
    WAITING_ROOM,       // Game has been selected by > 0 users and is waiting to start
    ACTIVE,             // Game is started and actively being played
    PAUSED,             // Game was paused
    FINISHED            // Game has concluded
}
