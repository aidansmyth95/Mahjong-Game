package mahjong_package;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public final class FirebaseRepository {

    /**********************************************
     Get Uid
     **********************************************/
    public static String getCurrentUserUid() {
        FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        if (userAuth != null) {
            return userAuth.getUid();
        }
        return "";
    }

    /**********************************************
            WRITING TO FIREBASE USERS
     **********************************************/
    // update user to indicate that they are waiting to join a game
    public static void userInactiveFirebaseUser() {
        FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        if (userAuth != null) {
            String uid = userAuth.getUid();
            FirebaseDatabase.getInstance().getReference("users/"+uid+"/userStatus").setValue("inactive");
        }
    }

    // update user to indicate that they are waiting to join a game
    public static void userJoinedGameFirebase() {
        FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        if (userAuth != null) {
            String uid = userAuth.getUid();
            FirebaseDatabase.getInstance().getReference("users/"+uid+"/userStatus").setValue("joined");
        }
    }

    // update user to indicate that they are waiting to join a game
    public static void userPlayingGameFirebase() {
        FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        if (userAuth != null) {
            String uid = userAuth.getUid();
            FirebaseDatabase.getInstance().getReference("users/"+uid+"/userStatus").setValue("playing");
        }
    }

    // update user to indicate that they are waiting to join a game
    public static void addUserLastGameIDFirebase(String last_game_ID) {
        FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        if (userAuth != null) {
            String uid = userAuth.getUid();
            FirebaseDatabase.getInstance().getReference("users/"+uid+"/lastGameId").setValue(last_game_ID);
        }
    }


    /**********************************************
            WRITING FIREBASE Game
     **********************************************/
    // add a new Game
    public static String createNewMultiplayerGame() {
        // create game ID unique to others created, add it and name
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        return database.getReference("multiplayer_games").push().getKey();
    }

    // update a Game
    public static void updateMultiplayerGame(Game updated_game) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("multiplayer_games").child(updated_game.getGameID()).setValue(updated_game);
    }

    //TODO: may be needed in a later menu
    /*
    // remove user from a game
    public static void removeInactiveUserFromWaitingRoomFirebase(User user, Game game) {
        if (game.getGameID().equals(user.getLastGameId())) {
            // edit game Object here and update Firebase
            game.removePlayer(user.getUid());
            updateMultiplayerGame(game);
            return;
        }
    }
    */

    // update user to indicate that they are waiting to join a game
    public static void startMultiplayerGame(String gameID) {
        FirebaseDatabase.getInstance().getReference("multiplayer_games/"+gameID+"/gameStatus").setValue("start");
    }

    /**********************************************
     MANIPULATING GAMES ARRAYLIST
     **********************************************/

    /**********************************************
            READING FIREBASE Game TO ARRAYLIST
     **********************************************/
    // update list of game DB info
    public static void getNewMultiplayerGameFromFirebase(ArrayList<Game> games, DataSnapshot dataSnapshot) {
        games.add(dataSnapshot.getValue(Game.class));
    }

    public static void getModifiedMultiplayerGameFromFirebase(ArrayList<Game> games, DataSnapshot dataSnapshot) {
        Game changed_game = dataSnapshot.getValue(Game.class);
        for (int i=0; i<games.size(); i++) {
            assert changed_game != null;
            if (games.get(i).getGameID().equals(changed_game.getGameID())) {
                games.remove(i);
                games.add(changed_game);
                break;
            }
        }
    }

    // update list of Games based on Firebase listener
    public static void removeDeletedMultiplayerGameFromFirebase(ArrayList<Game> games, DataSnapshot dataSnapshot) {
        Game changed_game = dataSnapshot.getValue(Game.class);
        for (int i=0; i<games.size(); i++) {
            assert changed_game != null;
            if (games.get(i).getGameID().equals(changed_game.getGameID())) {
                games.remove(i);
                break;
            }
        }
    }

    // update User based on Frirebase Listener
    public static User getCurrUserDetailsFirebase(DataSnapshot dataSnapshot) {
        return dataSnapshot.getValue(User.class);
    }

    // update multiplayer game based on firebase listener
    public static Game getCurrGameDetailsFirebase(DataSnapshot dataSnapshot) {
        return dataSnapshot.getValue(Game.class);
    }

    // write an object of any kind to test
    public static void testWriteObjectFirebase(Object o) {
        FirebaseDatabase.getInstance().getReference("tiles").child("a").setValue(o);
    }
}
