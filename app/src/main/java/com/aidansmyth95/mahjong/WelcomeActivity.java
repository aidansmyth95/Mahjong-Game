package com.aidansmyth95.mahjong;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;


public class WelcomeActivity extends AppCompatActivity {

    private Button loginRedirectButton, registerRedirectButton;
    private static final String TAG = "WelcomeActivity";
    private ImageButton rulebook;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        Log.i(TAG, TAG+": onCreate");

        setContentView(R.layout.activity_welcome);
        initializeUI();
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.button_sound);

        loginRedirectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mp.start();
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        registerRedirectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mp.start();
                Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        rulebook.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, RulebookActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initializeUI() {
        loginRedirectButton = findViewById(R.id.login_redirect_button);
        registerRedirectButton = findViewById(R.id.register_redirect_button);
        rulebook = findViewById(R.id.rules_at_welcome_activity);
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

    public void onBackPressed() {
        Log.i(TAG, TAG+": onBackPressed");
        finishAffinity();
        System.exit(0);
    }

}