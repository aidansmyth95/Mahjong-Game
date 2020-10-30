package com.example.mahjong;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

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
import static mahjong_package.FirebaseRepository.userPlayingGameFirebase;

public class PausedGameActivity extends AppCompatActivity {

    private static final String TAG = "PausedGameActivity";
    private Button leave_game;
    private Handler handler;
    private Runnable runnable;
    private final long checkPlayersDelay = 500;
    private Game currGame = new Game(-1);
    private User currUser = new User();
    int playerIdx = -1;
    private DatabaseReference dbRef;
    private ValueEventListener userListener, gameListener;
    private Boolean resumingGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, TAG + ": onCreate");
        setContentView(R.layout.activity_paused_game_activity);
        handler = new Handler();
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.button_sound);
        leave_game = findViewById(R.id.leave_game_button);
        leave_game.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mp.start();
                // go back to the GameModeActivity
                Intent intent = new Intent(PausedGameActivity.this, JoinGameActivity.class);
                startActivity(intent);
            }
        });
        // set rulebook listener
        ImageButton rulebook = findViewById(R.id.rules_at_paused_game);
        rulebook.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(PausedGameActivity.this, RulebookActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, TAG + ": onStart");
        setCurrUserListener();
        resumingGame = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, TAG + ": onPause");
    }

    @Override
    protected void onStop() {
        // remove runnable callback of handler
        handler.removeCallbacks(runnable);
        // remove listeners
        if (userListener != null) {
            dbRef.removeEventListener(userListener);
        }
        if (gameListener != null) {
            dbRef.removeEventListener(gameListener);
        }
        // if not resuming game but leaving activity
        if (!resumingGame) {
            userInactiveFirebaseUser();
            // player is no longer playing or waiting to play, update Game
            currGame.setPlayerPlayingStatus(false, playerIdx);
            updateMultiplayerGame(currGame);
        }
        Log.i(TAG, TAG + ": onStop");
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, TAG + ": onResume");
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                // this player is playing when we are in this state
                currGame.setPlayerPlayingStatus(true, playerIdx);
                // check that all players are playing. If not, popup window
                if (currGame.allPlayersPlaying() && currGame.getGameStatus() == GameStatus.PAUSED) {
                    Log.i(TAG, TAG + ": All " + currGame.countNumPlayersPlaying() + " are playing");
                    Log.i(TAG, TAG + ": Dismissing leave game popup menu");
                    currGame.setGameStatus(GameStatus.ACTIVE);
                    // user is now active and about to start playing the game
                    userPlayingGameFirebase();
                    // go back to Game
                    Intent intent = new Intent(PausedGameActivity.this, MultiplayerActivity.class);
                    startActivity(intent);
                }
                MultiplayerActivity.logcatGameStatus(currGame, playerIdx);
                handler.postDelayed(runnable, checkPlayersDelay);
            }
        }, checkPlayersDelay);

    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, TAG + ": onDestroy");
        super.onDestroy();
    }

    // add a listener for multiplayer games database that updates dynamic table with users
    private void setCurrMulitplayerGameListener() {
        dbRef = FirebaseDatabase.getInstance().getReference();
        gameListener = dbRef.child("multiplayer_games").child(currUser.getLastGameId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG,TAG+": child nodes changed for game = "+dataSnapshot.getChildrenCount());
                currGame = getCurrGameDetailsFirebase(dataSnapshot);
                if (currGame.gameExists()) {
                    playerIdx = currGame.getPlayerIdx(currUser.getUid());
                    if (currGame.allPlayersPlaying()) {
                        // go back to Game
                        resumingGame = true;
                        Intent intent = new Intent(PausedGameActivity.this, MultiplayerActivity.class);
                        startActivity(intent);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    // add a listener for users database that updates dynamic table with users
    private void setCurrUserListener() {
        dbRef = FirebaseDatabase.getInstance().getReference();
        userListener = dbRef.child("users").child(getCurrentUserUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currUser = getCurrUserDetailsFirebase(dataSnapshot);
                // now we have last Game ID, so set current game listener
                setCurrMulitplayerGameListener();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, TAG+": onBackPressed");
        new AlertDialog.Builder(this)
                .setTitle("Really Exit Game?")
                .setMessage("Are you sure you want to exit the game?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(PausedGameActivity.this, CreateJoinGameActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }).create().show();
    }

}
