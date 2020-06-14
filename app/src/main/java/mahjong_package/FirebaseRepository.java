package mahjong_package;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public final class FirebaseRepository {


    /**********************************************
            WRITING TO FIREBASE USERS
     **********************************************/
    // update user to indicate that they are waiting to join a game
    public static void userInactiveFirebaseUser() {
        FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        if (userAuth != null) {
            String uid = userAuth.getUid();
            FirebaseDatabase.getInstance().getReference("users/"+uid+"/_user_status").setValue("inactive");
        }
    }

    // update user to indicate that they are waiting to join a game
    public static void userJoinedGameFirebase() {
        FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        if (userAuth != null) {
            String uid = userAuth.getUid();
            FirebaseDatabase.getInstance().getReference("users/"+uid+"/_user_status").setValue("joined");
        }
    }

    // update user to indicate that they are waiting to join a game
    public static void userPlayingGameFirebase() {
        FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        if (userAuth != null) {
            String uid = userAuth.getUid();
            FirebaseDatabase.getInstance().getReference("users/"+uid+"/_user_status").setValue("playing");
        }
    }

    // update user to indicate that they are waiting to join a game
    public static void addUserLastGameIDFirebase(String last_game_ID) {
        FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        if (userAuth != null) {
            String uid = userAuth.getUid();
            FirebaseDatabase.getInstance().getReference("users/"+uid+"/_last_game_id").setValue(last_game_ID);
        }
    }


    /**********************************************
            WRITING FIREBASE GAMEDB
     **********************************************/
    // add a new Game
    public static String createNewGameIDFirebase() {
        // create game ID unique to others created, add it and name
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        return database.getReference("multiplayer_games").push().getKey();
    }

    // update a Game
    public static void updateGameDBFirebase(GameDB updated_game) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("multiplayer_games").child(updated_game.getGameID()).setValue(updated_game);
    }

    // remove user from a game
    public static void removeInactiveUserFromWaitingRoomFirebase(User user, GameDB game) {
        if (game.getGameID().equals(user.get_last_game_id())) {
            // edit game Object here and update Firebase
            game.removeGameMember(user.get_uname(), user.get_uid());
            updateGameDBFirebase(game);
            return;
        }
    }

    // update user to indicate that they are waiting to join a game
    public static void startGameFirebase(String gameID) {
        FirebaseDatabase.getInstance().getReference("multiplayer_games/"+gameID+"/_game_status").setValue("start");
    }

    /**********************************************
     MANIPULATING GAMES ARRAYLIST
     **********************************************/

    /**********************************************
            READING FIREBASE GAMEDB TO ARRAYLIST
     **********************************************/
    // update list of game DB info
    public static void addNewGameDBFromFirebase(ArrayList<GameDB> games, DataSnapshot dataSnapshot) {
        games.add(dataSnapshot.getValue(GameDB.class));
    }

    public static void changeGameDBFromFirebase(ArrayList<GameDB> games, DataSnapshot dataSnapshot) {
        GameDB changed_game = dataSnapshot.getValue(GameDB.class);
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
    public static void removeGameDBFromFirebase(ArrayList<GameDB> games, DataSnapshot dataSnapshot) {
        GameDB changed_game = dataSnapshot.getValue(GameDB.class);
        for (int i=0; i<games.size(); i++) {
            assert changed_game != null;
            if (games.get(i).getGameID().equals(changed_game.getGameID())) {
                games.remove(i);
                break;
            }
        }
    }

    // update User based on Frirebase Listener
    public static User addCurrUserDetailsFirebase(DataSnapshot dataSnapshot) {
        return dataSnapshot.getValue(User.class);
    }

    // update GameDB based on firebase listener
    public static GameDB addCurrGameDetailsFirebase(DataSnapshot dataSnapshot) {
        return dataSnapshot.getValue(GameDB.class);
    }


    /*
    DEPRECATED
     */
    /*
        public static String getCurrentUserUid() {
        FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        if (userAuth != null) {
            return userAuth.getUid();
        }
        return "";
    }

    // get a User from list of Users we are listening to
    public static User getUserFromUid(ArrayList<User> users, String uid) {
        for (int i=0; i<users.size(); i++) {
            if (uid.equals(users.get(i).get_uid())) {
                return users.get(i);
            }
        }
        return new User();
    }

    public static void removeUserFromFirebase(ArrayList<User> users, DataSnapshot dataSnapshot) {
        User changed_user = dataSnapshot.getValue(User.class);
        for (int i=0; i<users.size(); i++) {
            assert changed_user != null;
            if (users.get(i).get_uid().equals(changed_user.get_uid())) {
                users.remove(i);
                return;
            }
        }
    }

        // update Users list with a new User added
    // update list of user info
    public static void addNewUserFromFirebase(ArrayList<User> users, DataSnapshot dataSnapshot) {
        users.add(dataSnapshot.getValue(User.class));
        Log.e(dataSnapshot.getKey(), "New user added");
    }

    // update Users list with a changed User
    public static void changeUserFromFirebase(ArrayList<User> users, DataSnapshot dataSnapshot) {
        User changed_user = dataSnapshot.getValue(User.class);
        for (int i=0; i<users.size(); i++) {
            assert changed_user != null;
            if (users.get(i).get_uid().equals(changed_user.get_uid())) {
                users.remove(i);
                users.add(changed_user);
                break;
            }
        }
    }

     */
}
