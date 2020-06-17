package mahjong_package;

import android.util.Log;

import java.util.ArrayList;

//TODO: start of with Game inside GameDB, but eventually merge to be just GameDB, then rename to Game

public class GameDB {

    private String gameName;
    private String gameID;
    private ArrayList<String> gameMembersName;
    private ArrayList<String> gameMembersUid;
    private int playerTurn;
    private String gameState;   // begin, start, play, saved, finished

    public GameDB() {
        this.setGameName("NaN");
        this.setGameID("NaN");
        this.setGameState("begin");
        this.setPlayerTurn(-1);
        this.gameMembersUid = new ArrayList<>();
        this.gameMembersName = new ArrayList<>();
    }

    // non getter setter methods
    public String listAllPlayer() {
        if (this.gameMembersName.size() <= 1) {
            return "No members";
        }
        StringBuilder s = new StringBuilder();
        for (int i=0; i<this.gameMembersName.size(); i++) {
            if (!this.gameMembersName.get(i).equals("names")) {
                Log.e(String.valueOf(1),i + " " + this.gameMembersName.get(i) + " ADDED NAME");
                s.append(this.gameMembersName.get(i)).append("  ");
            }
        }
        return s.toString();
    }

    public void addGameMember(String name, String uid) {
        // do not allow same user to join the same game twice
        if (!this.gameMembersUid.contains(uid) && !this.gameMembersName.contains(name)) {
            this.gameMembersName.add(name);
            this.gameMembersUid.add(uid);
        }
    }

    // remove a game member
    public void removeGameMember(String name, String uid) {
        this.gameMembersUid.remove(uid);
        this.gameMembersName.remove(name);
    }

    // count number of active players
    public Integer numRealMembers() {
        int count = 0;
        //Log.e(String.valueOf(1),this.gameMembersName.size() + " IS THE SIZE FOR THIS GAME 1");
        for (int i=0; i<this.gameMembersName.size(); i++) {
            if (!this.gameMembersName.get(i).equals("names")) {
                count++;
            }
        }
        //Log.e(String.valueOf(1),count + " IS THE SIZE FOR THIS GAME 1");
        return count;
    }


    // all the getter and setters are needed for Firebase

    public void setGameName(String name) { this.gameName = name; }
    public String getGameName() { return this.gameName; }

    public void setGameMembersName(ArrayList<String> member_names) { this.gameMembersName.addAll(member_names); }
    public ArrayList<String> getGameMembersName() { return this.gameMembersName; }

    public void setGameMembersUid(ArrayList<String> member_uids) { this.gameMembersUid.addAll(member_uids); }
    public ArrayList<String> getGameMembersUid() { return this.gameMembersUid; }

    public void setGameID(String id) { this.gameID = id; }
    public String getGameID() { return this.gameID; }

    public void setGameState(String game_state) { this.gameState = game_state; }
    public String getGameState() { return this.gameState; }

    public void setPlayerTurn(Integer player_turn) { this.playerTurn = player_turn; }
    public Integer getPlayerTurn() { return this.playerTurn; }
}


