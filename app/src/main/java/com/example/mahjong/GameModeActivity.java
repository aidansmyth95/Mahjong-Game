package com.example.mahjong;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static mahjong_package.FirebaseRepository.userInactiveFirebaseUser;


public class GameModeActivity extends AppCompatActivity {

    private Button spButton, mpButton;

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
}
