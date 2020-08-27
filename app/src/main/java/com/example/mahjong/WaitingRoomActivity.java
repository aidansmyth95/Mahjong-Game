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


public class WaitingRoomActivity extends AppCompatActivity {

    private TableLayout game_table_view;
    private User curr_user = new User();
    private Game curr_game = new Game();
    private static final String TAG = "WaitingRoomActivity";

    private DatabaseReference dbRef;
    private ValueEventListener userListener;
    private ValueEventListener gameListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, TAG+": onCreate");

        setContentView(R.layout.activity_waiting_room);
        game_table_view = (TableLayout) findViewById(R.id.single_game_table_layout);
        Button start_game = (Button) findViewById(R.id.start_game);
        start_game.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (curr_user.userExists()) {
                    // move to playing game
                    startGame();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, TAG+": onStart");
        // set Firebase database listeners
        setCurrUserListener();
        // user is inactive if they are starting this activity - not joined or active in game yet
        userInactiveFirebaseUser();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, TAG+": onPause");
    }

    @Override
    protected void onStop() {
        if (userListener != null) {
            dbRef.removeEventListener(userListener);
        }
        if (gameListener != null) {
            dbRef.removeEventListener(gameListener);
        }
        super.onStop();
        Log.e(TAG, TAG+": onStop");
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
        TableRow game_members_playing_row = new TableRow(this);
        TextView tv1 = new TextView(this);
        tv1.setText("Players ready: " + curr_game.namePlayersPlaying());
        game_members_playing_row.addView(tv1);
        game_name_row.setBackgroundResource(R.drawable.border);
        game_table_view.addView(game_members_playing_row);

        // row containing game members
        TableRow game_members_not_playing_row = new TableRow(this);
        TextView tv2 = new TextView(this);
        tv2.setText("Players missing: " + curr_game.namePlayersNotPlaying());
        game_members_not_playing_row.addView(tv2);
        game_name_row.setBackgroundResource(R.drawable.border);
        game_table_view.addView(game_members_not_playing_row);

        // row containing game state
        TableRow game_state_row = new TableRow(this);
        TextView tv3 = new TextView(this);
        tv3.setText("Game state: " + curr_game.getGameState());
        game_state_row.addView(tv3);
        game_table_view.addView(game_state_row);

        // row containing player turn
        TableRow game_turn_row = new TableRow(this);
        TextView tv4 = new TextView(this);
        tv4.setText("Player's turn: " + curr_game.getPlayerTurn().toString());
        game_turn_row.addView(tv4);
        game_table_view.addView(game_turn_row);
    }

    private void startGame() {
        userPlayingGameFirebase();
        // update Game status
        curr_game.setGameStatus(GameStatus.ACTIVE);
        updateMultiplayerGame(curr_game);
        Intent intent = new Intent(WaitingRoomActivity.this, MultiplayerActivity.class);
        startActivity(intent);
    }

    // add a listener for users database that updates dynamic table with users
    private void setCurrUserListener() {
        dbRef = FirebaseDatabase.getInstance().getReference();
        String uid = getCurrentUserUid();
        Log.e(TAG, TAG+": Setting up listener for " + "users/"+uid);
        userListener = dbRef.child("users").child(uid).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG,TAG+": child nodes changed for user = "+dataSnapshot.getChildrenCount());
                curr_user = getCurrUserDetailsFirebase(dataSnapshot);
                Log.e(TAG,TAG+": User identified as " + curr_user.getUid());
                // now we have last Game ID, so set current game listener
                setCurrMulitplayerGameListener();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    // add a listener for multiplayer games database that updates dynamic table with users
    private void setCurrMulitplayerGameListener() {
        dbRef = FirebaseDatabase.getInstance().getReference();
        gameListener = dbRef.child("multiplayer_games").child(curr_user.getLastGameId()).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(dataSnapshot.getKey(), TAG+": child nodes changed for Game = "+dataSnapshot.getChildrenCount());
                curr_game = getCurrGameDetailsFirebase(dataSnapshot);
                if (curr_game.gameExists()) {
                    initializeUI();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, TAG+": onResume");
        userJoinedGameFirebase();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, TAG+": onRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, TAG+": onDestroy");
    }

    @Override
    public void onBackPressed() {
        Log.e(TAG, TAG+": onBackPressed");
        Intent intent = new Intent(WaitingRoomActivity.this, GameSelectActivity.class);
        startActivity(intent);
        finish();
    }
}
