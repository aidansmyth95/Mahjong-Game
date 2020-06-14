package com.example.mahjong;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class WelcomeActivity extends AppCompatActivity {

    private Button loginRedirectButton, registerRedirectButton;


    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
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

}