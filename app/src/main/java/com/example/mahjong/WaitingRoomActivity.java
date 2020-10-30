package com.example.mahjong;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import mahjong_package.Game;
import mahjong_package.User;

import static mahjong_package.FirebaseRepository.getCurrGameDetailsFirebase;
import static mahjong_package.FirebaseRepository.getCurrUserDetailsFirebase;
import static mahjong_package.FirebaseRepository.getCurrentUserUid;
import static mahjong_package.FirebaseRepository.userInactiveFirebaseUser;
import static mahjong_package.FirebaseRepository.userJoinedGameFirebase;


public class WaitingRoomActivity extends AppCompatActivity {

    private Button start_game;
    private TableLayout game_table_view;
    private User curr_user = new User();
    private Game curr_game = new Game(-1);
    private static final String TAG = "WaitingRoomActivity";

    private DatabaseReference dbRef;
    private ValueEventListener userListener;
    private ValueEventListener gameListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, TAG+": onCreate");

        setContentView(R.layout.activity_waiting_room);
        game_table_view = findViewById(R.id.single_game_table_layout);
        start_game = findViewById(R.id.start_game);
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.button_sound);
        start_game.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (curr_user.userExists()) {
                    mp.start();
                    // move to playing game
                    startGame();
                }
            }
        });

        // set rulebook listener
        ImageButton rulebook = findViewById(R.id.rules_at_waiting_room);
        rulebook.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mp.start();
                Intent intent = new Intent(WaitingRoomActivity.this, RulebookActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, TAG+": onStart");
        // set Firebase database listeners
        setCurrUserListener();
        // user is inactive if they are starting this activity - not joined or active in game yet
        userInactiveFirebaseUser();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, TAG+": onPause");
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
        Log.i(TAG, TAG+": onStop");
    }

    private void updateUI() {
        // populate a row for game name, game state, players involved etc...
        game_table_view.removeAllViews();
        // row containing Game name
        TableRow game_name_row = new TableRow(this);
        TextView game_name_text = new TextView(this);
        game_name_text.setText(getString(R.string.waiting_room_game_name, curr_game.getGameName()));
        game_name_row.addView(game_name_text);
        game_name_row.setBackgroundResource(R.drawable.border);
        game_table_view.addView(game_name_row);

        // row containing game members
        TableRow game_members_playing_row = new TableRow(this);
        TextView game_members_playing_text = new TextView(this);
        game_members_playing_text.setText(getString(R.string.waiting_room_players_ready, curr_game.namePlayersPlaying()));
        game_members_playing_row.addView(game_members_playing_text);
        game_name_row.setBackgroundResource(R.drawable.border);
        game_table_view.addView(game_members_playing_row);

        // row containing game members
        TableRow game_members_not_playing_row = new TableRow(this);
        TextView game_members_not_playing_text = new TextView(this);
        game_members_not_playing_text.setText(getString(R.string.waiting_room_players_missing, curr_game.namePlayersNotPlaying()));
        game_members_not_playing_row.addView(game_members_not_playing_text);
        game_name_row.setBackgroundResource(R.drawable.border);
        game_table_view.addView(game_members_not_playing_row);

        // row containing game state
        TableRow game_state_row = new TableRow(this);
        TextView game_state_text = new TextView(this);
        game_state_text.setText(getString(R.string.waiting_room_game_state, curr_game.getGameState()));
        game_state_row.addView(game_state_text);
        game_table_view.addView(game_state_row);

        // row containing number of players joined out of max players
        TableRow game_players_joined_row = new TableRow(this);
        TextView game_players_joined_text = new TextView(this);
        game_players_joined_text.setText(getString(R.string.players_joined_out_of_max, curr_game.getNumPlayers(), curr_game.getMaxPlayers()));
        game_players_joined_row.addView(game_players_joined_text);
        game_table_view.addView(game_players_joined_row);

        // row containing player turn
        TableRow game_turn_row = new TableRow(this);
        TextView game_turn_text = new TextView(this);
        game_turn_text.setText(getString(R.string.waiting_room_players_turn, curr_game.getPlayerTurn()));
        game_turn_row.addView(game_turn_text);
        game_table_view.addView(game_turn_row);
    }

    private void startGame() {
        Intent intent = new Intent(WaitingRoomActivity.this, MultiplayerActivity.class);
        startActivity(intent);
    }

    // add a listener for users database that updates dynamic table with users
    private void setCurrUserListener() {
        dbRef = FirebaseDatabase.getInstance().getReference();
        String uid = getCurrentUserUid();
        Log.i(TAG, TAG+": Setting up listener for " + "users/"+uid);
        userListener = dbRef.child("users").child(uid).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG,TAG+": child nodes changed for user = "+dataSnapshot.getChildrenCount());
                curr_user = getCurrUserDetailsFirebase(dataSnapshot);
                Log.d(TAG,TAG+": User identified as " + curr_user.getUid());
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
                Log.d(dataSnapshot.getKey(), TAG+": child nodes changed for Game = "+dataSnapshot.getChildrenCount());
                curr_game = getCurrGameDetailsFirebase(dataSnapshot);
                if (curr_game.gameExists()) {
                    updateUI();
                }
                // enable progression if enough players have signed up to / joined the Game
                start_game.setEnabled(curr_game.allPlayersJoined());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, TAG+": onResume");
        userJoinedGameFirebase();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, TAG+": onRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, TAG+": onDestroy");
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, TAG+": onBackPressed");
        Intent intent = new Intent(WaitingRoomActivity.this, CreateJoinGameActivity.class);
        startActivity(intent);
        finish();
    }
}
