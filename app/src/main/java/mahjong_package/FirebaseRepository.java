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
import java.util.Iterator;


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
            WRITING TO FIREBASE USERS.
            Modes: inactive, joined (waiting) and playing
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
        Log.e("FirebaseRepository", "FirebaseRepository: updating Game");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        // fill empty arraylists so that they exist in Firebase too
        Log.e("FirebaseRepository", "FirebaseRepository: Filling empty array list prior to update game");
        updated_game.fillEmptyArrayLists();
        // write Game with a completion listener too reporting on the write success
        //FIXME: crashes here
        ref.child("multiplayer_games").child(updated_game.getGameID()).setValue(updated_game)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Log.e("FirebaseRepository", "FirebaseRepository: Error updating Game to firebase: " + task.getException().getMessage());
                        }
                    }
        });
        Log.e("FirebaseRepository", "FirebaseRepository: Cleaning empty array list after update game");
        // clean filled values from multiplayer games in Firebase
        updated_game.cleanEmptyArrayLists();
    }


    /**********************************************
            READING FIREBASE Game TO ARRAYLIST
     **********************************************/
    // add game if it is in paused state to arraylist
    public static Boolean getPausedGameFromFirebase(ArrayList<Game> games, DataSnapshot dataSnapshot) {
        Game g = getCurrGameDetailsFirebase(dataSnapshot);
        if (g.gameExists()) {
            // remove old game if exists
            for (Iterator<Game> iterator = games.iterator(); iterator.hasNext(); ) {
                Game tmp = iterator.next();
                if (g.getGameID().equals(tmp.getGameID())) {
                    Log.e("FirebaseRepository","FirebaseRepository: Removed game from paused games \n");
                    iterator.remove();
                }
            }
            if (g.getGameStatus() == GameStatus.PAUSED) {
                games.add(g);
                Log.e("FirebaseRepository","FirebaseRepository: Games is of size " + games.size() + " after adding the paused game\n");
                return true;
            }
        }
        return false;
    }

    // get modified Game, and if it exists in games<> update it
    public static Boolean getModifiedMultiplayerGameFromFirebase(ArrayList<Game> games, DataSnapshot dataSnapshot) {
        Game g = getCurrGameDetailsFirebase(dataSnapshot);
        if (g.gameExists()) {
            // first remove modified game
            for (Iterator<Game> iterator = games.iterator(); iterator.hasNext(); ) {
                Game tmp = iterator.next();
                if (g.getGameID().equals(tmp.getGameID())) {
                    iterator.remove();
                }
            }

            // add back in if still Paused or Waiting Room status
            if (g.getGameStatus() == GameStatus.PAUSED) {
                Log.e("FirebaseRepository","FirebaseRepository: Games is of size " + games.size() + " before updating modified\n");
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
            Log.e("FirebaseRepository","FirebaseRepository: Game is null");
        } else {
            // clean Game object empty ArrayList placeholders for Firebase if any
            g.cleanEmptyArrayLists();
            Log.e("FirebaseRepository","FirebaseRepository: Game "+g.getGameID()+" was successfully read");
        }
        return g;
    }

    // write an object of any kind to test
    public static void testWriteObjectFirebase(Object o) {
        FirebaseDatabase.getInstance().getReference("tiles").child("a").setValue(o);
    }

}
