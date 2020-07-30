package com.example.mahjong;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import mahjong_package.Game;
import mahjong_package.GameStatus;
import mahjong_package.User;

import static mahjong_package.FirebaseRepository.getCurrGameDetailsFirebase;
import static mahjong_package.FirebaseRepository.getCurrUserDetailsFirebase;
import static mahjong_package.FirebaseRepository.getCurrentUserUid;
import static mahjong_package.FirebaseRepository.updateMultiplayerGame;
import static mahjong_package.FirebaseRepository.userInactiveFirebaseUser;
import static mahjong_package.FirebaseRepository.userJoinedGameFirebase;
import static mahjong_package.FirebaseRepository.userPlayingGameFirebase;


public class GameWaitingRoomActivity extends AppCompatActivity {

    private TableLayout game_table_view;
    private User curr_user = new User();
    private Game curr_game = new Game();
    private boolean go_back_to_game_select = false;
    private boolean go_back_activity = true;

    @Override
    protected void onPause() {
        super.onPause();
        // if we did not click start game and came here, the user has left the app
        if (go_back_activity) {
            // user is inactive - go back to waiting room
            userInactiveFirebaseUser();
            // Game is no longer pending
            curr_game.setGameStatus(GameStatus.PAUSED);
            // when (or if) activity resumes, go back to previos activity
            this.go_back_to_game_select = true;
        }
    }

    @Override
    protected void onRestart() {
        // using restart to move back an intent
        super.onRestart();
        if (this.go_back_to_game_select) {
            // Go back to choosing a game to join when you return
            this.go_back_to_game_select = false;
            Intent intent = new Intent(GameWaitingRoomActivity.this, GameSelectActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_waiting_room);

        // set Firebase database listeners
        setCurrUserListener();

        // user state is now joined - user listener now kicks in and updates curr_user and curr_game
        userJoinedGameFirebase();

        game_table_view = (TableLayout) findViewById(R.id.single_game_table_layout);
        Button start_game = (Button) findViewById(R.id.start_game);

        start_game.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (curr_user.userExists()) {
                    Log.e("a_start","The Game has been started by this player");
                    // move to playing game
                    startGame(true);
                }
            }
        });
    }

    private void initializeUI() {

        // populate a row for game name, game state, players involved etc...
        game_table_view.removeAllViews();
        // row containing Game name
        TableRow game_name_row = new TableRow(this);
        TextView tv0 = new TextView(this);
        tv0.setText("Game name: " + curr_game.getGameName());
        game_name_row.addView(tv0);
        game_name_row.setBackgroundResource(R.drawable.border);
        game_table_view.addView(game_name_row);

        // row containing game members
        TableRow game_members_row = new TableRow(this);
        TextView tv1 = new TextView(this);
        tv1.setText("Players: " + curr_game.listAllPlayer());
        game_members_row.addView(tv1);
        game_name_row.setBackgroundResource(R.drawable.border);
        game_table_view.addView(game_members_row);

        // row containing game state
        TableRow game_state_row = new TableRow(this);
        TextView tv2 = new TextView(this);
        tv2.setText("Game state: " + curr_game.getGameState());
        game_state_row.addView(tv2);
        game_table_view.addView(game_state_row);

        // row containing player turn
        TableRow game_turn_row = new TableRow(this);
        TextView tv3 = new TextView(this);
        tv3.setText("Player's turn: " + curr_game.getPlayerTurn().toString());
        game_turn_row.addView(tv3);
        game_table_view.addView(game_turn_row);
    }

    private void startGame(boolean initiator) {
        userPlayingGameFirebase();
        boolean proceed = false;

        // get last_game_id of user and update Game to be playing - if initiator
        if (initiator) {
            String game_id = curr_user.getLastGameId();
            curr_game.setGameStatus(GameStatus.ACTIVE);
            Log.e("Game analysis", "STARTING GAME ACTIVITY FROM WAITING ROOM");
            proceed = true;
        } else {
            // check if game has been started
            if (curr_game.getGameStatus().equals(GameStatus.ACTIVE)) {
                proceed = true;
            }
        }

        if (proceed) {
            // do not delete user from game as we move to next intent
            go_back_activity = false;
            //FIXME: this does not seem to be updating
            // update Game - other users in waiting room can now see it is active and join
            updateMultiplayerGame(curr_game);
            // in Game listener, if change, user's last_game_idx is updated and intent is moved (no need to update Game)
            Log.e("Game analysis", "PROGRESSING WITH GAME ACTIVITY " + curr_game.getGameStatus());
            Intent intent = new Intent(GameWaitingRoomActivity.this, MultiplayerActivity.class);
            startActivity(intent);
        }
    }

    // add a listener for users database that updates dynamic table with users
    private void setCurrUserListener() {
        Log.e("user_listener","Setting up listener");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String uid = getCurrentUserUid();
        DatabaseReference usersRef = database.getReference().child("users").child(uid);
        Log.e("user_listener","Setting up listener for " + "users/"+uid);

        usersRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(dataSnapshot.getKey(),dataSnapshot.getChildrenCount() + " child nodes changed for user");
                curr_user = getCurrUserDetailsFirebase(dataSnapshot);
                Log.e(dataSnapshot.getKey(),"Child nodes belong to " + curr_user.getUid());
                // now we have last Game ID, so set current game listener
                setCurrMulitplayerGameListener();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    // add a listener for multiplayer games database that updates dynamic table with users
    private void setCurrMulitplayerGameListener() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference gameRef = database.getReference().child("multiplayer_games").child(curr_user.getLastGameId());
        gameRef.addValueEventListener(new ValueEventListener() {

            //FIXME: ok, problem is that Game State is reverting to waiting room and the other players in waiting room do not see a change. Probably happening in MP Activity
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(dataSnapshot.getKey(),dataSnapshot.getChildrenCount() + " CHILD NODES CHANGED FOR CURRENT GAME");

                curr_game = getCurrGameDetailsFirebase(dataSnapshot);
                if (curr_game.gameExists()) {
                    // Game is in waiting room prior to start
                    if (!curr_game.getGameStatus().equals(GameStatus.WAITING_ROOM)) {
                        curr_game.setGameStatus(GameStatus.WAITING_ROOM);
                        // update game in Firebase
                        updateMultiplayerGame(curr_game);
                    }
                    // here we would check game status and see if somebody else started the game
                    if (curr_game.getGameStatus().equals(GameStatus.ACTIVE)) {
                        Log.e("a_start","The Game has been started by another player");
                        startGame(false);
                    }
                    initializeUI();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}
