package com.example.mahjong;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LoginActivity extends AppCompatActivity {
    private Button loginButton;
    private EditText emailText, pwdText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        initializeUI();

        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void initializeUI() {
        loginButton = (Button) findViewById(R.id.login_button);
        emailText = (EditText) findViewById(R.id.emailText2);
        pwdText = (EditText) findViewById(R.id.pwdText2);
    }

    //https://blog.usejournal.com/firebase-email-and-password-authentication-for-android-e335c81a1dad
    private void loginUser() {

        String email, password;
        email = emailText.getText().toString();
        password = pwdText.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Please enter email...", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Please enter password!", Toast.LENGTH_LONG).show();
            return;
        }

        loginButton.setEnabled(false);

        emailText.setText("");
        pwdText.setText("");

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(LoginActivity.this, GameModeActivity.class);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Login failed! Please try again later", Toast.LENGTH_LONG).show();
                        loginButton.setEnabled(true);
                    }
                }
            });
    }
}
