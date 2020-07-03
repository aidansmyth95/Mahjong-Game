package com.example.mahjong;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import static mahjong_package.FirebaseRepository.createNewMultiplayerGame;
import static mahjong_package.FirebaseRepository.getCurrUserDetailsFirebase;
import static mahjong_package.FirebaseRepository.getModifiedMultiplayerGameFromFirebase;
import static mahjong_package.FirebaseRepository.getNewMultiplayerGameFromFirebase;
import static mahjong_package.FirebaseRepository.removeDeletedMultiplayerGameFromFirebase;
import static mahjong_package.FirebaseRepository.updateMultiplayerGame;
import static mahjong_package.FirebaseRepository.userJoinedGameFirebase;

//TODO: add completion / failure callbacks for all db writes in pkg

public class GameSelectActivity extends AppCompatActivity implements LifecycleObserver {

    private TableLayout tableLayout;
    private EditText new_game;

    private ArrayList<Game> games = new ArrayList<>();
    private String selected_game = "";

    FirebaseUser userRef = FirebaseAuth.getInstance().getCurrentUser();
    private User curr_user = new User();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_select);

        tableLayout = (TableLayout) findViewById(R.id.game_table_layout);
        Button join_button = (Button) findViewById(R.id.join_game);
        Button create_button = (Button) findViewById(R.id.create_game);
        new_game = (EditText) findViewById(R.id.new_game_text);

        initializeUI(false);

        create_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (curr_user.userExists()) {
                    if (create_new_game()) {
                        Intent intent = new Intent(GameSelectActivity.this, GameWaitingRoomActivity.class);
                        startActivity(intent);
                    }
                }
            }
        });

        join_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curr_user.userExists()) {
                    if (join_new_game()) {
                        Intent intent = new Intent(GameSelectActivity.this, GameWaitingRoomActivity.class);
                        startActivity(intent);
                    }
                }
            }
        });

        // listen to all games (for table display)
        setMultiplayerGamesListener();
        // listen to current user (so we can modify selected Game's details when we join or create)
        setCurrUserListener();
    }


    // update dynamically created table of games and users
    private void initializeUI(boolean uncheck_others) {
        //TODO: fixed header

        // add header
        tableLayout.removeAllViews();
        TableRow row0 = new TableRow(this);
        TextView tv0 = new TextView(this);
        tv0.setText(R.string.game_name);
        tv0.setTextColor(Color.BLACK);
        row0.addView(tv0);
        TextView tv1 = new TextView(this);
        tv1.setText(R.string.status);
        tv1.setTextColor(Color.BLACK);
        row0.addView(tv1);
        TextView tv2 = new TextView(this);
        tv2.setText(R.string.game_members);
        tv2.setTextColor(Color.BLACK);
        row0.addView(tv2);
        row0.setBackgroundResource(R.drawable.border);
        tableLayout.addView(row0);
        // add data for each game
        for (int i = 0; i < games.size(); i++) {
            final Game game_tmp = games.get(i);
            // check box
            TableRow row = new TableRow(this);
            CheckBox chb = new CheckBox(this);
            chb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(((CompoundButton) view).isChecked()){
                        selected_game = game_tmp.getGameID();
                        initializeUI(true);
                    } else {
                        selected_game = "";
                    }
                }
            });
            chb.setText(i + ": " + game_tmp.getGameName());
            chb.setTextColor(Color.BLUE);
            chb.setGravity(Gravity.CENTER);
            if (selected_game.equals(game_tmp.getGameID())) {
                chb.setChecked(true);
            } else {
                chb.setChecked(false);
            }
            row.addView(chb);
            // text views
            TextView t1n = new TextView(this);
            t1n.setText(R.string.paused);
            t1n.setTextColor(Color.BLUE);
            t1n.setGravity(Gravity.CENTER);
            row.addView(t1n);
            TextView t2n = new TextView(this);
            t2n.setText(game_tmp.listAllPlayer());
            t2n.setTextColor(Color.BLUE);
            t2n.setGravity(Gravity.CENTER);
            row.addView(t2n);
            row.setBackgroundResource(R.drawable.border);
            tableLayout.addView(row);
        }
    }

    // create a new game and join as current user
    private boolean create_new_game() {
        String new_game_name;
        new_game_name = new_game.getText().toString();
        // take text if any from EditText
        if (TextUtils.isEmpty(new_game_name)) {
            Toast.makeText(getApplicationContext(), "Please enter a game name.", Toast.LENGTH_LONG).show();
            return false;
        }
        new_game.setText("");
        Game game_info = new Game();
        FirebaseUser userRef = FirebaseAuth.getInstance().getCurrentUser();
        if (userRef != null) {
            // create game ID unique to others created, add it and name
            String game_ID = createNewMultiplayerGame();
            Log.e("1"," MADE IT HERE");
            game_info.addPlayer(curr_user.getUname(), curr_user.getUid());
            game_info.setGameName(new_game_name);
            game_info.setGameID(game_ID);
            // update games.
            updateMultiplayerGame(game_info);
            // This is now last game user played
            addUserLastGameIDFirebase(game_info.getGameID());
            // user state is now joined
            userJoinedGameFirebase();
            return true;
        } else {
            Toast.makeText(getApplicationContext(), "Failed to make new game. Please be non null user.", Toast.LENGTH_LONG).show();
            return false;
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
            Game gameSelected = new Game();
            for (int i=0; i<games.size(); i++) {
                gameSelected = games.get(i);
                if (gameSelected.getGameID().equals(selected_game)) {
                    break; // for loop break
                }
            }
            // add user details to game members that require it
            FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
            if (userAuth == null) {
                return false;
            } else {
                // check that there are < 4 players already joined
                if (gameSelected.getNumPlayers() >= 4) {
                    Toast.makeText(getApplicationContext(), "Maximum number of players joined.", Toast.LENGTH_LONG).show();
                    return false;

                } else {
                    gameSelected.addPlayer(curr_user.getUname(), curr_user.getUid());
                    // update Firebase to reflect this change in game object by updating child
                    updateMultiplayerGame(gameSelected);
                    //update user to reflect that this is the last game they played
                    addUserLastGameIDFirebase(gameSelected.getGameID());
                    // user state is now joined
                    userJoinedGameFirebase();
                    Toast.makeText(getApplicationContext(), "Joined " + gameSelected.getGameName()+"...", Toast.LENGTH_LONG).show();
                    return true;
                }
            }
        }
    }

    // add a listener for users database that updates dynamic table with users
    private void setCurrUserListener() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference().child("users").child(userRef.getUid());
        usersRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(dataSnapshot.getKey(),dataSnapshot.getChildrenCount() + " CHILD NODES CHANGED FOR CURRENT USER");
                curr_user = getCurrUserDetailsFirebase(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    // add a listener for multiplayer games database that updates dynamic table with users
    private void setMultiplayerGamesListener() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference gameRef = database.getReference().child("multiplayer_games");
        gameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                Log.e(dataSnapshot.getKey(),"GAME ADDED ");
                getNewMultiplayerGameFromFirebase(games, dataSnapshot);
                initializeUI(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                Log.e(dataSnapshot.getKey(),dataSnapshot.getChildrenCount() + "ITEMS IN GAME CHANGED ");
                getModifiedMultiplayerGameFromFirebase(games, dataSnapshot);
                initializeUI(false);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.e(dataSnapshot.getKey(),"GAME REMOVED ");
                removeDeletedMultiplayerGameFromFirebase(games, dataSnapshot);
                initializeUI(false);
            }

            @Override
            //TODO: do not handle this right, study how to do this right
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                Log.e(dataSnapshot.getKey(),dataSnapshot.getChildrenCount() + "GAME MOVED ");
                removeDeletedMultiplayerGameFromFirebase(games, dataSnapshot);
                initializeUI(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}