package com.example.mahjong;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mahjong_package.Game;
import mahjong_package.User;

import static mahjong_package.FirebaseRepository.addUserLastGameIDFirebase;
import static mahjong_package.FirebaseRepository.createNewMultiplayerGameRef;
import static mahjong_package.FirebaseRepository.getCurrUserDetailsFirebase;
import static mahjong_package.FirebaseRepository.updateMultiplayerGame;

public class CreateGameActivity extends AppCompatActivity {

    private EditText new_game;
    private RadioGroup radioPlayersGroup;
    private RadioButton radioPlayersButton;
    private User curr_user;
    private DatabaseReference dbRef;
    private ValueEventListener usersListener;
    FirebaseUser userRef = FirebaseAuth.getInstance().getCurrentUser();

    private static final String TAG = "CreateGameActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);
        Log.i(TAG,TAG+": onCreate \n");

        // UI creation
        initializeUI();

        // A temporary fix if we need to create a dummy game
        //writeDummyGame();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, TAG+": onStart");

        // listen to current user (so we can modify selected Game's details when we join or create)
        setCurrUserListener();
        Log.i(TAG,TAG+": User listener set \n");
    }

    @Override
    protected void onStop() {
        if (usersListener != null) {
            dbRef.removeEventListener(usersListener);
        }
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

    // initialize the UI components and their listeners
    private void initializeUI() {
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.button_sound);
        new_game = findViewById(R.id.create_game_name);
        radioPlayersGroup = findViewById(R.id.radioGroup);
        Button create_button = findViewById(R.id.create_game_complete);
        create_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (curr_user.userExists()) {
                    mp.start();
                    int selectedId= radioPlayersGroup.getCheckedRadioButtonId();
                    radioPlayersButton = findViewById(selectedId);
                    int num_players = extractIntFromString(radioPlayersButton.getText().toString());
                    if (create_new_game(num_players)) {
                        Intent intent = new Intent(CreateGameActivity.this, WaitingRoomActivity.class);
                        startActivity(intent);
                    }
                }
            }
        });

        // set rulebook listener
        ImageButton rulebook = findViewById(R.id.rules_at_create_game);
        rulebook.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mp.start();
                Intent intent = new Intent(CreateGameActivity.this, RulebookActivity.class);
                startActivity(intent);
            }
        });
    }

    // create a new game and join as current user
    private boolean create_new_game(int max_players) {
        String new_game_name = new_game.getText().toString();
        // take text if any from EditText
        if (TextUtils.isEmpty(new_game_name)) {
            Toast.makeText(getApplicationContext(), "Please enter a game name.", Toast.LENGTH_LONG).show();
            return false;
        }
        // clear Game name
        new_game.setText("");
        Game new_game = new Game(max_players);
        if (userRef != null) {
            // create game ID unique to others created, add it and name
            String game_ID = createNewMultiplayerGameRef();
            new_game.addPlayer(curr_user.getUname(), curr_user.getUid());
            new_game.setGameName(new_game_name);
            new_game.setGameID(game_ID);
            // update games.
            updateMultiplayerGame(new_game);
            // This is now last game user played
            addUserLastGameIDFirebase(new_game.getGameID());
            Toast.makeText(getApplicationContext(), "Joined " + new_game.getGameName()+"...", Toast.LENGTH_LONG).show();
            return true;
        } else {
            Toast.makeText(getApplicationContext(), "Failed to make new game. Please be non null user.", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    // add a listener for users database that updates dynamic table with users
    private void setCurrUserListener() {
        dbRef = FirebaseDatabase.getInstance().getReference();
        usersListener = dbRef.child("users").child(userRef.getUid()).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i(dataSnapshot.getKey(),TAG+":" + dataSnapshot.getChildrenCount() + " child nodes changed for user");
                curr_user = getCurrUserDetailsFirebase(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private Integer extractIntFromString(String s) {
        Matcher matcher = Pattern.compile("\\d+").matcher(s);
        matcher.find();
        return Integer.parseInt(matcher.group());
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, TAG+": onBackPressed");
        Intent intent = new Intent(CreateGameActivity.this, WelcomeActivity.class);
        startActivity(intent);
    }
}
