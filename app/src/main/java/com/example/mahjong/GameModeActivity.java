package com.example.mahjong;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import static mahjong_package.FirebaseRepository.userInactiveFirebaseUser;


public class GameModeActivity extends AppCompatActivity {

    private Button spButton, mpButton;
    private static final String TAG = "GameModeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_mode);

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
    }

    private void initializeUI() {
        spButton = (Button) findViewById(R.id.single_player_button);
        mpButton = (Button) findViewById(R.id.multiplayer_button);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, TAG+": onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, TAG+": onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, TAG+": onPause");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, TAG+": onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, TAG+": onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, TAG+": onDestroy");
    }

    @Override
    public void onBackPressed() {
        Log.e(TAG, TAG+": onBackPressed");
        Intent intent = new Intent(GameModeActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
