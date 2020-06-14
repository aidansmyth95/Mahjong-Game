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

import mahjong_package.GameDB;
import mahjong_package.User;

import static mahjong_package.FirebaseRepository.addCurrGameDetailsFirebase;
import static mahjong_package.FirebaseRepository.addCurrUserDetailsFirebase;
import static mahjong_package.FirebaseRepository.removeInactiveUserFromWaitingRoomFirebase;
import static mahjong_package.FirebaseRepository.startGameFirebase;
import static mahjong_package.FirebaseRepository.userInactiveFirebaseUser;
import static mahjong_package.FirebaseRepository.userPlayingGameFirebase;

public class GameWaitingRoomActivity extends AppCompatActivity {
    private TableLayout game_table_view;
    private User curr_user = new User();
    private GameDB curr_game = new GameDB();

    private boolean go_back_to_game_select = false;

    @Override
    protected void onPause() {
        super.onPause();
        // user is inactive - no longer joined in a game
        userInactiveFirebaseUser();
        // User is removed from that game.
        removeInactiveUserFromWaitingRoomFirebase(curr_user, curr_game);
        this.go_back_to_game_select = true;
    }

    @Override
    protected void onRestart() {
        // using restart to move back an intent
        super.onRestart();
        if (this.go_back_to_game_select) {
            // Go back to chooing a game to join when you return
            this.go_back_to_game_select = false;
            Intent intent = new Intent(GameWaitingRoomActivity.this, GameSelectActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_waiting_room);

        // To retrieve object in second Activity
        curr_user = (User) getIntent().getSerializableExtra("User");

        // set Firebase database listeners
        setCurrMulitplayerGameListener();
        setCurrUserListener();

        game_table_view = (TableLayout) findViewById(R.id.single_game_table_layout);
        Button start_game = (Button) findViewById(R.id.start_game);

        start_game.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // move to playing game
                startGame(true);
            }
        });
    }

    private void initializeUI() {

        //TODO: here we would check game status and see if somebody else started the game
        if (curr_game.getGameState().equals("start")) {
            startGame(false);
        }

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
        //TODO: do this for all users, not just those that clicked! Otherwise they will be stuck in waiting room!
        userPlayingGameFirebase();
        boolean proceed = false;

        // get last_game_id of user and update GameDB to be playing - if initiator
        if (initiator) {
            String game_id = curr_user.get_last_game_id();
            startGameFirebase(game_id);
            proceed = true;
        } else {
            // check if game has been started
            if (curr_game.getGameState().equals("start")) {
                proceed = true;
            }
        }

        if (proceed) {
            // in GameDB listener, if change, user's last_game_idx is updated and intent is moved (no need to update Game)
            Intent intent = new Intent(GameWaitingRoomActivity.this, MultiplayerActivity.class);
            startActivity(intent);
        }
    }

    // add a listener for users database that updates dynamic table with users
    private void setCurrUserListener() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference().child("users").child(curr_user.get_uid());
        usersRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(dataSnapshot.getKey(),dataSnapshot.getChildrenCount() + " CHILD NODES CHANGED FOR CURRENT USER");
                curr_user = addCurrUserDetailsFirebase(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    // add a listener for multiplayer games database that updates dynamic table with users
    private void setCurrMulitplayerGameListener() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference gameRef = database.getReference().child("multiplayer_games").child(curr_user.get_last_game_id());
        gameRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(dataSnapshot.getKey(),dataSnapshot.getChildrenCount() + " CHILD NODES CHANGED FOR CURRENT GAME");
                curr_game = addCurrGameDetailsFirebase(dataSnapshot);
                initializeUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}
