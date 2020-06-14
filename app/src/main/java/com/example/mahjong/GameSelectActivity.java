package com.example.mahjong;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleObserver;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import mahjong_package.GameDB;
import mahjong_package.User;

import static mahjong_package.FirebaseRepository.addCurrUserDetailsFirebase;
import static mahjong_package.FirebaseRepository.addNewGameDBFromFirebase;
import static mahjong_package.FirebaseRepository.addUserLastGameIDFirebase;
import static mahjong_package.FirebaseRepository.changeGameDBFromFirebase;
import static mahjong_package.FirebaseRepository.createNewGameIDFirebase;
import static mahjong_package.FirebaseRepository.removeGameDBFromFirebase;
import static mahjong_package.FirebaseRepository.updateGameDBFirebase;
import static mahjong_package.FirebaseRepository.userJoinedGameFirebase;

//TODO: add completion / failure callbacks for all db writes in pkg

public class GameSelectActivity extends AppCompatActivity implements LifecycleObserver {

    private TableLayout tableLayout;
    private EditText new_game;

    private ArrayList<GameDB> games = new ArrayList<>();
    private int selected_game = -1;

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

        initializeUI();

        create_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (curr_user.userExists()) {
                    if (create_new_game()) {
                        Intent intent = new Intent(GameSelectActivity.this, GameWaitingRoomActivity.class);
                        intent.putExtra("User", curr_user);
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
                        intent.putExtra("User", curr_user);
                        startActivity(intent);
                    }
                }
            }
        });

        // listen to all games (for table display)
        setMultiplayerGamesListener();
        // listen to current user (so we can modify selected GameDB's details when we join or create)
        setCurrUserListener();
    }


    // update dynamically created table of games and users
    private void initializeUI() {
        //TODO: fixed header

        // add header
        tableLayout.removeAllViews();
        TableRow row0 = new TableRow(this);
        TextView tv0 = new TextView(this);
        tv0.setText(R.string.game_name);
        tv0.setTextColor(Color.BLACK);
        row0.addView(tv0);
        TextView tv1 = new TextView(this);
        tv1.setText(R.string.game_members);
        tv1.setTextColor(Color.BLACK);
        row0.addView(tv1);
        TextView tv2 = new TextView(this);
        tv2.setText(R.string.status);
        tv2.setTextColor(Color.BLACK);
        row0.addView(tv2);
        row0.setBackgroundResource(R.drawable.border);
        tableLayout.addView(row0);
        // add data for each game
        for (int i = 0; i < games.size(); i++) {
            GameDB game_tmp = games.get(i);
            // check box
            final int chb_index = i*3;
            final int finalI = i;
            TableRow row = new TableRow(this);
            CheckBox chb = new CheckBox(this);
            chb.setTag(chb_index);
            chb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(((CompoundButton) view).isChecked()){
                        selected_game = finalI;
                    } else {
                        selected_game = -1;
                    }
                }
            });
            chb.setText(i + ": " + game_tmp.getGameName());
            chb.setTextColor(Color.BLUE);
            chb.setGravity(Gravity.CENTER);
            if (selected_game == i) {
                chb.setChecked(true);
            } else {
                chb.setChecked(false);
            }
            row.addView(chb);
            // text views
            TextView t1v = new TextView(this);
            final int t1v_index = i*3 + 1;
            t1v.setTag(t1v_index);
            t1v.setText(game_tmp.listAllPlayer());
            t1v.setTextColor(Color.BLUE);
            t1v.setGravity(Gravity.CENTER);
            row.addView(t1v);
            TextView t2v = new TextView(this);
            final int t2v_index = i*3 + 2;
            t1v.setTag(t2v_index);
            t2v.setText(R.string.paused);
            t2v.setTextColor(Color.BLUE);
            t2v.setGravity(Gravity.CENTER);
            row.addView(t2v);
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
        GameDB game_info = new GameDB();
        FirebaseUser userRef = FirebaseAuth.getInstance().getCurrentUser();
        if (userRef != null) {
            // create game ID unique to others created, add it and name
            String game_ID = createNewGameIDFirebase();
            ArrayList<String> uid_list = new ArrayList<>();
            ArrayList<String> uname = new ArrayList<>();
            uid_list.add("uids");
            uid_list.add(curr_user.get_uid());
            uname.add("names");
            uname.add(curr_user.get_uname());
            ArrayList<Integer> resp = new ArrayList<>();
            for (int i=0; i<5; i++) {
                resp.add(0);
            }
            game_info.setGameName(new_game_name);
            game_info.setGameMembersName(uname);
            game_info.setGameMembersUid(uid_list);
            game_info.setGameID(game_ID);
            game_info.setGameState("begin");
            game_info.setExpectedResponses(resp);
            // update games.
            updateGameDBFirebase(game_info);
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
        if (selected_game == -1) {
            Toast.makeText(getApplicationContext(), "Please select a game to join first.", Toast.LENGTH_LONG).show();
            return false;
        } else {
            // get game ID using the selected reference
            GameDB gameSelected = games.get(selected_game);
            // add user details to game DB members that require it
            FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();
            if (userAuth == null) {
                return false;
            } else {
                // check that there are < 4 players already joined
                if (gameSelected.numRealMembers() >= 4) {
                    Toast.makeText(getApplicationContext(), "Maximum number of players joined.", Toast.LENGTH_LONG).show();
                    return false;

                } else {
                    gameSelected.addGameMember(curr_user.get_uname(), curr_user.get_uid());
                    // update Firebase to reflect this change in game DB object by updating child
                    updateGameDBFirebase(gameSelected);
                    //update user to reflect that this is the last game they played
                    addUserLastGameIDFirebase(gameSelected.getGameID());
                    // user state is now joined
                    userJoinedGameFirebase();
                    Toast.makeText(getApplicationContext(), "Joined "+gameSelected.getGameName()+"...", Toast.LENGTH_LONG).show();
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
                curr_user = addCurrUserDetailsFirebase(dataSnapshot);
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
                addNewGameDBFromFirebase(games, dataSnapshot);
                initializeUI();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                Log.e(dataSnapshot.getKey(),dataSnapshot.getChildrenCount() + "ITEMS IN GAME CHANGED ");
                changeGameDBFromFirebase(games, dataSnapshot);
                initializeUI();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.e(dataSnapshot.getKey(),"GAME REMOVED ");
                removeGameDBFromFirebase(games, dataSnapshot);
                initializeUI();
            }

            @Override
            //TODO: do not handle this right, study how to do this right
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                Log.e(dataSnapshot.getKey(),dataSnapshot.getChildrenCount() + "GAME MOVED ");
                removeGameDBFromFirebase(games, dataSnapshot);
                initializeUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
