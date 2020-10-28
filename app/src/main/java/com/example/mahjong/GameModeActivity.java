package com.example.mahjong;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import static mahjong_package.FirebaseRepository.userInactiveFirebaseUser;
import static mahjong_package.FirebaseRepository.writeDummyGame;


public class GameModeActivity extends AppCompatActivity {

    private Button spButton, mpButton;
    private static final String TAG = "GameModeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_mode);

        // A temporary fix if we need to create a dummy game
        //writeDummyGame();

        initializeUI();
        userInactiveFirebaseUser();
        spButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                spButton.setEnabled(false);
                Intent intent = new Intent(GameModeActivity.this, SinglePlayerActivity.class);
                startActivity(intent);
            }
        });
        mpButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(GameModeActivity.this, GameSelectActivity.class);
                startActivity(intent);
            }
        });
        // set rulebook listener
        ImageButton rulebook = findViewById(R.id.rules_at_game_mode);
        rulebook.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(GameModeActivity.this, RulebookActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initializeUI() {
        spButton = findViewById(R.id.single_player_button);
        mpButton = findViewById(R.id.multiplayer_button);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, TAG+": onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, TAG+": onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, TAG+": onPause");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, TAG+": onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, TAG+": onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, TAG+": onDestroy");
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, TAG+": onBackPressed");
        Intent intent = new Intent(GameModeActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
