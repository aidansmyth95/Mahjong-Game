package com.example.mahjong;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class CreateJoinGameActivity extends AppCompatActivity {

    private Button create_button, join_button;
    private static final String TAG = "CreateJoinGame";
    private ImageButton rulebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_join_game);
        Log.i(TAG, TAG+": onCreate");

        initializeUI();
    }

    private void initializeUI() {
        create_button = findViewById(R.id.create_game_button);
        join_button = findViewById(R.id.join_game_button);
        rulebook = findViewById(R.id.rules_at_create_join_game_activity);
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.button_sound);
        create_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mp.start();
                Intent intent = new Intent(CreateJoinGameActivity.this, CreateGameActivity.class);
                startActivity(intent);
            }
        });
        join_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mp.start();
                Intent intent = new Intent(CreateJoinGameActivity.this, JoinGameActivity.class);
                startActivity(intent);
            }
        });
        rulebook.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mp.start();
                Intent intent = new Intent(CreateJoinGameActivity.this, RulebookActivity.class);
                startActivity(intent);
            }
        });
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
        Intent intent = new Intent(CreateJoinGameActivity.this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }
}
