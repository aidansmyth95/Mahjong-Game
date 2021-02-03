package com.aidansmyth95.mahjong;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleObserver;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import mahjong_package.Game;
import mahjong_package.User;

import static mahjong_package.FirebaseRepository.addUserLastGameIDFirebase;
import static mahjong_package.FirebaseRepository.getCurrUserDetailsFirebase;
import static mahjong_package.FirebaseRepository.getModifiedMultiplayerGameFromFirebase;
import static mahjong_package.FirebaseRepository.getPausedGameFromFirebase;
import static mahjong_package.FirebaseRepository.getDeletedMultiplayerGameFromFirebase;
import static mahjong_package.FirebaseRepository.updateMultiplayerGame;


public class JoinGameActivity extends AppCompatActivity implements LifecycleObserver {

    private TableLayout tableLayout;
    private ArrayList<Game> games;
    private String selected_game;
    private FirebaseUser userRef;
    private User curr_user;
    private static final String TAG = "JoinGameActivity";

    private DatabaseReference dbRef;
    private ValueEventListener usersListener;
    private ChildEventListener gameListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);
        Log.i(TAG,TAG+": onCreate \n");

        userRef = FirebaseAuth.getInstance().getCurrentUser();
        games = new ArrayList<>();
        curr_user = new User();
        tableLayout = findViewById(R.id.game_table_layout);
        Button join_button = findViewById(R.id.join_game);
        // UI creation
        initializeUI();
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.button_sound);
        join_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curr_user.userExists()) {
                    if (join_new_game()) {
                        mp.start();
                        Intent intent = new Intent(JoinGameActivity.this, WaitingRoomActivity.class);
                        startActivity(intent);
                    }
                }
            }
        });
        // set rulebook listener
        ImageButton rulebook = findViewById(R.id.rules_at_game_select);
        rulebook.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mp.start();
                Intent intent = new Intent(JoinGameActivity.this, RulebookActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, TAG+": onStart");

        games.clear();
        selected_game = "";

        // listen to current user (so we can modify selected Game's details when we join or create)
        setCurrUserListener();
        Log.i(TAG,TAG+": User listener set \n");
        // listen to all games (for table display)
        setMultiplayerGamesListener();
        Log.i(TAG,TAG+": Games listener set \n");
    }

    @Override
    protected void onStop() {
        if (usersListener != null) {
            dbRef.removeEventListener(usersListener);
        }
        if (gameListener != null) {
            dbRef.removeEventListener(gameListener);
        }
        super.onStop();
        Log.i(TAG, TAG+": onStop");
    }

    // update dynamically created table of games and users
    private void initializeUI() {
        // add header
        tableLayout.removeAllViews();
        TableRow header_row = new TableRow(this);
        TextView game_name_header = new TextView(this);
        game_name_header.setText(R.string.game_name);
        game_name_header.setTextColor(Color.BLACK);
        header_row.addView(game_name_header);
        TextView game_members_header = new TextView(this);
        game_members_header.setText(R.string.game_members);
        game_members_header.setTextColor(Color.BLACK);
        header_row.addView(game_members_header);
        header_row.setBackgroundResource(R.drawable.border);
        tableLayout.addView(header_row);
        // add data for each game
        Log.i(TAG,TAG+": Games is of size "+games.size());
        for (int i = 0; i < games.size(); i++) {
            final Game game_tmp = games.get(i);
            // check box
            TableRow row_i = new TableRow(this);
            CheckBox game_name_chb_i = new CheckBox(this);
            game_name_chb_i.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(((CompoundButton) view).isChecked()){
                        selected_game = game_tmp.getGameID();
                        initializeUI();
                    } else {
                        selected_game = "";
                    }
                }
            });
            game_name_chb_i.setText(getString(R.string.chb_game_name, i, game_tmp.getGameName()));
            game_name_chb_i.setTextColor(Color.BLUE);
            game_name_chb_i.setGravity(Gravity.CENTER);
            if (selected_game.equals(game_tmp.getGameID())) {
                game_name_chb_i.setChecked(true);
            } else {
                game_name_chb_i.setChecked(false);
            }
            row_i.addView(game_name_chb_i);
            // text views
            TextView members_text_i = new TextView(this);
            members_text_i.setText(game_tmp.listAllPlayer());
            members_text_i.setTextColor(Color.BLUE);
            members_text_i.setGravity(Gravity.CENTER);
            row_i.addView(members_text_i);
            row_i.setBackgroundResource(R.drawable.border);
            tableLayout.addView(row_i);
        }
    }

    // join an existing game
    private boolean join_new_game() {
        // check selected checkbox and find corresponding GameID in that row
        if (selected_game.equals("")) {
            Toast.makeText(getApplicationContext(), "Please select a game to join first.", Toast.LENGTH_LONG).show();
            return false;
        } else {
            // get game using gameID
            Game gameJoined = new Game(-1);
            for (int i=0; i<games.size(); i++) {
                gameJoined = games.get(i);
                if (gameJoined.getGameID().equals(selected_game)) {
                    break; // for loop break
                }
            }
            // add user details to game members that require it
            FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
            if (userAuth == null) {
                return false;
            } else {
                String uid = curr_user.getUid();
                // if already joined, update and proceed
                if (gameJoined.checkPlayerJoined(uid)) {
                    Log.i(TAG,TAG+": User uid " + uid + " already joined game. Proceeding to next activity");
                    //update user to reflect that this is the last game they played
                    addUserLastGameIDFirebase(gameJoined.getGameID());
                    return true;
                } else {
                    Log.i(TAG,TAG+": User uid " + uid + " has not yet joined game. Attempting to join");
                    // check that there are < 4 players already joined
                    if (gameJoined.getNumPlayers() >= gameJoined.getMaxPlayers()) {
                        Log.i(TAG,TAG+": Maximum number of players joined. Please join another Game.");
                        Toast.makeText(getApplicationContext(), "Maximum number of players joined. Please join another Game.", Toast.LENGTH_LONG).show();
                        return false;
                    } else {
                        // add player to game if not already joined - we don't care if they have joined before or not
                        gameJoined.addPlayer(curr_user.getUname(), uid);
                        // update Firebase to reflect this change in game object by updating child
                        updateMultiplayerGame(gameJoined);
                        //update user to reflect that this is the last game they played
                        addUserLastGameIDFirebase(gameJoined.getGameID());
                        Toast.makeText(getApplicationContext(), "Joined " + gameJoined.getGameName()+"...", Toast.LENGTH_LONG).show();
                        return true;
                    }
                }
            }
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

    // add a listener for multiplayer games database that updates dynamic table with users
    private void setMultiplayerGamesListener() {
        dbRef = FirebaseDatabase.getInstance().getReference();
        gameListener = dbRef.child("multiplayer_games").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                Log.d(TAG,TAG+": Game to be added ");
                getPausedGameFromFirebase(games, dataSnapshot);
                Log.d(TAG,TAG+": Paused Game was added");
                initializeUI();
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                Log.d(TAG,TAG+": "+dataSnapshot.getChildrenCount()+" GameSelectActivity: Game items changed");
                getModifiedMultiplayerGameFromFirebase(games, dataSnapshot);
                initializeUI();
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG,TAG+": Game items removed");
                getDeletedMultiplayerGameFromFirebase(games, dataSnapshot);
                initializeUI();
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                Log.d(TAG,TAG + ": " + dataSnapshot.getChildrenCount() + " game items moved");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
        Intent intent = new Intent(JoinGameActivity.this, CreateJoinGameActivity.class);
        startActivity(intent);
    }
}