package com.example.mahjong;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class WelcomeActivity extends AppCompatActivity {

    private Button welcomeButton;
    private TextView welcomeTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        welcomeButton = (Button) findViewById(R.id.welcome_button);
        welcomeTxt = (TextView) findViewById(R.id.welcome_txt);

        welcomeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                welcomeButton.setEnabled(false);

                // Intents are objects of the android.content.Intent type. Your code can send them
                // to the Android system defining the components you are targeting.
                // Intent to start an activity called SecondActivity with the following code:

                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);

                // start the activity connect to the specified class
                startActivity(intent);
            }
        });
    }
}
