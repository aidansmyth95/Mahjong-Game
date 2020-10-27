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

    private final static String TAG = "FirebaseRepository";

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

    // update user to indicate that they have joined a game but are waiting for all players to be joined to start playing
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
        WRITING FIREBASE User
     **********************************************/
    public static void createRegisteredFirebaseUser(String nameText) {
        FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        if (userAuth != null) {
            User user = new User();
            user.setUid(userAuth.getUid());
            user.setUname(nameText);
            user.setProviderId(userAuth.getProviderId());
            user.setEmail(userAuth.getEmail());
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
            // This will NOT work unless you have getters and setters.
            usersRef.setValue(user);
        }
    }

    public static void updateUser(final User user) {
        Log.d("FirebaseRepository", "FirebaseRepository: updating User");
        // to make sure it is not an empty user we are writing
        if (!user.userExists()) {
            Log.e(TAG, TAG+": User does not exist. Skipping.");
            return;
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        // write Game with a completion listener too reporting on the write success
        ref.setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, TAG + ": Error updating User to Firebase: " + task.getException().getMessage());
                        } else {
                            Log.e(TAG, TAG + ": Successfully updated User " + user.getUid());
                        }
                    }
                });
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
        Log.d(TAG, TAG + ": updating Game");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        // fill empty arraylists so that they exist in Firebase too
        Log.d(TAG, TAG + ": Filling empty array list prior to update game");
        updated_game.fillEmptyArrayLists();
        // write Game with a completion listener too reporting on the write success
        ref.child("multiplayer_games").child(updated_game.getGameID()).setValue(updated_game)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, TAG + ": Error updating Game to firebase: " + task.getException().getMessage());
                        }
                    }
        });
        Log.d(TAG, TAG + ": Cleaning empty array list after update game");
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
                    Log.d(TAG,TAG + ": Removed game from paused games \n");
                    iterator.remove();
                }
            }
            if (g.getGameStatus() == GameStatus.PAUSED) {
                games.add(g);
                Log.d(TAG,TAG + ": Games is of size " + games.size() + " after adding the paused game\n");
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
                Log.d(TAG,TAG + ": Games is of size " + games.size() + " before updating modified");
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
        User u = dataSnapshot.getValue(User.class);
        if (u == null) {
            Log.e(TAG,TAG + ": Game is null");
        } else {
            // clean Game object empty ArrayList placeholders for Firebase if any
            Log.d(TAG,TAG + ": Game " + u.getUid() + " was successfully read");
        }
        return u;
    }

    // update multiplayer game based on firebase listener
    public static Game getCurrGameDetailsFirebase(DataSnapshot dataSnapshot) {
        Game g = dataSnapshot.getValue(Game.class);
        if (g == null) {
            Log.e(TAG,TAG + ": Game is null");
        } else {
            // clean Game object empty ArrayList placeholders for Firebase if any
            g.cleanEmptyArrayLists();
            Log.d(TAG,TAG + ": Game " + g.getGameID() + " was successfully read");
        }
        return g;
    }

}
