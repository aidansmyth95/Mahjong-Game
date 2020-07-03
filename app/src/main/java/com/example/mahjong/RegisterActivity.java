package com.example.mahjong;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import mahjong_package.User;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private Button registerButton;
    private EditText emailText, pwdText, nameText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        initializeUI();

        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                registerNewUser();
            }
        });
    }

    private void initializeUI() {
        registerButton = (Button) findViewById(R.id.register_button);
        emailText = (EditText) findViewById(R.id.emailText1);
        nameText = (EditText) findViewById(R.id.nameText1);
        pwdText = (EditText) findViewById(R.id.pwdText1);
    }

    private void registerNewUser() {
        String email, pwd, name;

        // copy and clear user input, sending it to game
        email = emailText.getText().toString();
        pwd = pwdText.getText().toString();
        name = nameText.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Please enter email...", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(pwd)) {
            Toast.makeText(getApplicationContext(), "Please enter password!", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(name)) {
            Toast.makeText(getApplicationContext(), "Please enter name!", Toast.LENGTH_LONG).show();
        } else {
            registerButton.setEnabled(false);
            pwdText.setText("");
            mAuth.createUserWithEmailAndPassword(email, pwd)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                sendVerificationEmail();
                                createRegisteredFirebaseUser();
                                Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                                // Do not need to log in again after registration
                                Intent intent = new Intent(RegisterActivity.this, GameModeActivity.class);
                                startActivity(intent);
                            }
                            else {
                                FirebaseAuthException e = (FirebaseAuthException)task.getException();
                                Toast.makeText(getApplicationContext(), "Registration failed! " +e.getMessage() + ". Please try again later", Toast.LENGTH_LONG).show();
                                // remove flags expecting user input and disable button
                                registerButton.setEnabled(true);
                            }
                        }
                    });
        }
    }

    private void sendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Email sent.");
                            }
                        }
                    });
        }
    }

    private void createRegisteredFirebaseUser() {
        User user = new User();
        FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
        if (userAuth != null) {
            user.setUid(userAuth.getUid());
            user.setUname(nameText.getText().toString());
            user.setProviderId(userAuth.getProviderId());
            user.setEmail(userAuth.getEmail());
            user.setEmailVerified(false);
            user.setUserStatus("inactive");
            user.setLastGameId("NaN");
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
            // This will NOT work unless you have getters and setters. You can push the Map or the object, all that differs is _ prepended in DB if Object
            usersRef.child(user.getUid()).setValue(user);
        }
    }

}
