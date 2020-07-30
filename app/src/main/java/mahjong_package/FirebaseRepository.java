package mahjong_package;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    public static String createNewMultiplayerGameRef() {
        // create game ID unique to others created, add it and name
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        return database.getReference("multiplayer_games").push().getKey();
    }

    // update a Game
    public static void updateMultiplayerGame(Game updated_game) {
        Log.e("firebase", "FirebaseRepository: About to update game");
        //TODO: PRIORITY: Make sure empty arraylist is populated by an EMPTY value. Inversely, when we get Game make sure we remove this
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        // fill empty arraylists so that they exist in Firebase too
        updated_game.fillEmptyArrayLists();
        Log.e("firebase", "FirebaseRepository: Filled empty array list prior to update game");
        // write Game with a completion listener too reporting on the write success
        //FIXME: crashes here
        ref.child("multiplayer_games").child(updated_game.getGameID()).setValue(updated_game)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Log.e(
                                    "firebase",
                                    "FirebaseRepository: Error updating Game to firebase: " + task.getException().getMessage());
                        }
                    }
        });
        Log.e("firebase", "FirebaseRepository: Firebase just got command to update game");
        // clean filled values from multiplayer games in Firebase
        updated_game.cleanEmptyArrayLists();
        Log.e("firebase", "FirebaseRepository: Cleaned empty array list after update game");
    }


    /**********************************************
     MANIPULATING GAMES ARRAYLIST
     **********************************************/

    /**********************************************
            READING FIREBASE Game TO ARRAYLIST
     **********************************************/
    // add game if it is in paused state to arraylist
    public static Boolean getPausedGameFromFirebase(ArrayList<Game> games, DataSnapshot dataSnapshot) {
        Game g = getCurrGameDetailsFirebase(dataSnapshot);
        if (g.gameExists()) {
            // remove old game if exists
            for (int i=0; i<games.size(); i++) {
                if (games.get(i).getGameID().equals(g.getGameID())) {
                    games.remove(i);
                    Log.e("firebase","FirebaseRepository: Removed \n");
                }
            }
            if (g.getGameStatus() == GameStatus.PAUSED || g.getGameStatus() == GameStatus.WAITING_ROOM) {
                Log.e("firebase","FirebaseRepository: Games is of size " + games.size() + " before adding paused game\n");
                games.add(g);
                return true;
            }
        }
        return false;
    }

    // get modified Game, and if it exists in games<> update it
    public static Boolean getModifiedMultiplayerGameFromFirebase(ArrayList<Game> games, DataSnapshot dataSnapshot) {
        Game g = getCurrGameDetailsFirebase(dataSnapshot);
        if (g.gameExists()) {
            for (int i=0; i<games.size(); i++) {
                if (games.get(i).getGameID().equals(g.getGameID())) {
                    // first remove modified game
                    Log.e("firebase", "FirebaseRepository: Games is of size " + games.size() + " before removing before modifying\n");
                    games.remove(i);
                }
            }
            // add back in if still Paused or Waiting Room status
            if (g.getGameStatus() == GameStatus.PAUSED || g.getGameStatus() == GameStatus.WAITING_ROOM) {
                Log.e("firebase","FirebaseRepository: Games is of size " + games.size() + " before updating modified\n");
                games.add(g);
                return true;
            }
        }
        return false;
    }

    // update list of Games based on Firebase listener
    public static Boolean getDeletedMultiplayerGameFromFirebase(ArrayList<Game> games, DataSnapshot dataSnapshot) {
        Game g = getCurrGameDetailsFirebase(dataSnapshot);
        if (g.gameExists()) {
            for (int i=0; i<games.size(); i++) {
                if (games.get(i).getGameID().equals(g.getGameID())) {
                    Log.e("firebase","FirebaseRepository: Games is of size " + games.size() + " before removing\n");
                    games.remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    // update User based on Firebase Listener
    public static User getCurrUserDetailsFirebase(DataSnapshot dataSnapshot) {
        return dataSnapshot.getValue(User.class);
    }

    // update multiplayer game based on firebase listener
    public static Game getCurrGameDetailsFirebase(DataSnapshot dataSnapshot) {
        //TODO: find way to make sure we handle unexpected types
        Game g = dataSnapshot.getValue(Game.class);
        if (g == null) {
            Log.e("firebase","FirebaseRepository: Game is null");
        } else {
            // clean Game object empty ArrayList placeholders for Firebase if any
            g.cleanEmptyArrayLists();
            Log.e("firebase","FirebaseRepository: Game "+g.getGameID()+" was successfully read");
        }
        return g;
    }

    // write an object of any kind to test
    public static void testWriteObjectFirebase(Object o) {
        FirebaseDatabase.getInstance().getReference("tiles").child("a").setValue(o);
    }

}
