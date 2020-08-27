package com.example.mahjong;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class WelcomeActivity extends AppCompatActivity {

    private Button loginRedirectButton, registerRedirectButton;
    private static final String TAG = "WelcomeActivity";

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        Log.e(TAG, TAG+": onCreate");

        setContentView(R.layout.activity_welcome);
        initializeUI();
        loginRedirectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        registerRedirectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initializeUI() {
        loginRedirectButton = (Button) findViewById(R.id.login_redirect_button);
        registerRedirectButton = (Button) findViewById(R.id.register_redirect_button);
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

    public void onBackPressed() {
        Log.e(TAG, TAG+": onBackPressed");
        Intent intent = new Intent(WelcomeActivity.this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }

}