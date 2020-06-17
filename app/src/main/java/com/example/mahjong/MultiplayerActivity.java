package com.example.mahjong;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import mahjong_package.Game;
import mahjong_package.GameDB;
import mahjong_package.Hand;
import mahjong_package.LikeAGame;
import mahjong_package.Player;
import mahjong_package.Suits;
import mahjong_package.Tile;
import mahjong_package.Tiles;
import mahjong_package.User;

import static mahjong_package.FirebaseRepository.addCurrGameDetailsFirebase;
import static mahjong_package.FirebaseRepository.addCurrUserDetailsFirebase;
import static mahjong_package.FirebaseRepository.getCurrentUserUid;
import static mahjong_package.FirebaseRepository.testWriteObjectFirebase;


public class MultiplayerActivity extends AppCompatActivity {

    // Firebase listeners for User and GameDB
    private GameDB curr_gdb = new GameDB();
    private User curr_user = new User();

    private Game game;
    private Handler handler = new Handler();
    private Runnable runnable;

    private Button sendButton;
    private EditText[] userInputText = new EditText[4];
    private TextView outputText;
    private TextView outputTurn, outputHand;
    private ImageView discardedImage;
    private final int n_total_tiles = 15;
    private ImageView[] hand_tiles = new ImageView[15];

    private final int n_players = 4;
    private final int delay = 1000; //Delay game by this many milliseconds.


    private void redirectGameSystemOut(final TextView tv) {
        // set System.out in all classes to be TextView
        System.setOut(new PrintStream(new OutputStream() {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            @Override public void write(int oneByte) {
                outputStream.write(oneByte);
                tv.setText(new String(outputStream.toByteArray()));
            }
        }));
    }

    private void redirectGameSystemErr(final TextView tv) {
        // set System.Err to write to TextView
        System.setErr(new PrintStream(new OutputStream() {

            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

            @Override public void write(int oneByte) {
                errorStream.write(oneByte);
                tv.setText(new String(errorStream.toByteArray()));
            }
        }));
    }

    private boolean runAllGameTestVectors() {
        game = new Game(n_players);
        boolean clear_output = game.test_true_pong();
        if (clear_output) {
            clear_output = game.test_false_pong();
        }
        if (clear_output) {
            clear_output = game.test_true_kong();
        }
        if (clear_output) {
            clear_output = game.test_false_kong();
        }
        if (clear_output) {
            clear_output = game.test_true_mahjong();
        }
        if (clear_output) {
            outputText.setText("");
            // reset output stream link to text box
            System.out.flush();
            redirectGameSystemOut(outputText);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer);

        //Tile t_write = new Tile(); //result: PASS
        //Suits t_write = new Suits(); //result: PASS
        //Hand t_write = new Hand(); //result: PASS
        //Tiles t_write = new Tiles(); //result: PASS
        //Player t_write = new Player(); //result: PASS
        //Game t_write = new Game(1);   //result: PASS
        Game t_write = new Game(1);

        testWriteObjectFirebase(t_write);


        /*
        setCurrUserListener();

        redirectGameSystemErr(outputText);
        redirectGameSystemOut(outputText);

        // test the test vectors before Game
        boolean passed_tests = runAllGameTestVectors();

        // start Game
        if (passed_tests) {
            game = new Game(n_players);
        }


        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // check user input for all players
                for (int i=0; i<n_players; i++) {
                    // if there is text, use it as player_input
                    if (!TextUtils.isEmpty(userInputText[i].getText().toString())) {
                        // copy and clear user input, sending it to game
                        game.playerInput[i] = userInputText[i].getText().toString();
                        userInputText[i].setText("");
                        // remove flags expecting user input and disable button
                        game.requestResponse[i] = false;
                        sendButton.setEnabled(false);
                    } else {
                        // print an empty input text warning in outputText if we expect user input
                        if (game.requestResponse[i]) {
                            outputText.setText(R.string.empty_edit_text);
                        }
                    }
                }
            }
        });
    }


    //TODO: to be replaced by Firebase listener event rather than periodic onResume call :)
    @Override
    protected void onResume() {
        //start handler as activity become visible
        handler.postDelayed( runnable = new Runnable() {
            public void run() {
                // play the Game
                game.playGame();
                updateUI();
                handler.postDelayed(runnable, delay);
            }
        }, delay);
        super.onResume();
        *
         */
    }


    // If onPause() is not included the threads will double up when you reload the activity
    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }

    private void initializeUI() {
        sendButton = (Button) findViewById(R.id.send_button);
        sendButton.setEnabled(false);
        userInputText[0] = (EditText) findViewById(R.id.p_input);
        outputText = (TextView) findViewById(R.id.textViewOut);
        outputTurn = (TextView) findViewById(R.id.p_turn);
        outputHand = (TextView) findViewById(R.id.p_hand);
        outputText.setMovementMethod(new ScrollingMovementMethod());
        discardedImage = (ImageView) findViewById(R.id.discarded);
        hand_tiles[0] = (ImageView) findViewById(R.id.h0);
        hand_tiles[1] = (ImageView) findViewById(R.id.h1);
        hand_tiles[2] = (ImageView) findViewById(R.id.h2);
        hand_tiles[3] = (ImageView) findViewById(R.id.h3);
        hand_tiles[4] = (ImageView) findViewById(R.id.h4);
        hand_tiles[5] = (ImageView) findViewById(R.id.h5);
        hand_tiles[6] = (ImageView) findViewById(R.id.h6);
        hand_tiles[7] = (ImageView) findViewById(R.id.h7);
        hand_tiles[8] = (ImageView) findViewById(R.id.h8);
        hand_tiles[9] = (ImageView) findViewById(R.id.h9);
        hand_tiles[10] = (ImageView) findViewById(R.id.h10);
        hand_tiles[11] = (ImageView) findViewById(R.id.h11);
        hand_tiles[12] = (ImageView) findViewById(R.id.h12);
        hand_tiles[13] = (ImageView) findViewById(R.id.h13);
        hand_tiles[14] = (ImageView) findViewById(R.id.h14);

        for (int h=0; h<n_total_tiles; h++) {
            hand_tiles[h].setVisibility(View.INVISIBLE);
        }
        outputTurn.setVisibility(View.INVISIBLE);
        outputHand.setVisibility(View.INVISIBLE);
        discardedImage.setVisibility(View.INVISIBLE);
    }

    private void updateUI() {
        int resourceId;
        for (int i=0; i<n_players; i++) {

            if (game.getRequestResponse().get(i)) {

                // hidden hand
                ArrayList<String> hidden_tile_paths = game.descriptorToDrawablePath(game.getHiddenDescriptors());
                for (int j=0; j<hidden_tile_paths.size(); j++) {
                    //System.out.println("DEBUG: " + hidden_tile_paths.get(j));
                    resourceId = getResources().getIdentifier(hidden_tile_paths.get(j), "drawable", "com.example.mahjong");
                    hand_tiles[j].setImageResource(resourceId);
                    hand_tiles[j].setVisibility(View.VISIBLE);
                }

                // revealed hand
                ArrayList<String> revealed_tile_paths = game.descriptorToDrawablePath(game.getRevealedDescriptors());
                for (int j=0; j<revealed_tile_paths.size(); j++) {
                    resourceId = getResources().getIdentifier(revealed_tile_paths.get(j), "drawable", "com.example.mahjong");
                    hand_tiles[hidden_tile_paths.size()+j].setImageResource(resourceId);
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) hand_tiles[j].getLayoutParams();
                    // revealed 0, 200, 0, 0
                    params.setMargins(0, 200, 0, 0) ;//left, top, right, bottom
                    hand_tiles[j].setLayoutParams(params);
                    hand_tiles[j].setVisibility(View.VISIBLE);
                }

                // unused tiles (usually one)
                for (int j=hidden_tile_paths.size()+revealed_tile_paths.size(); j<n_total_tiles; j++) {
                    hand_tiles[j].setVisibility(View.INVISIBLE);
                }

                outputTurn.setText(new String("Player " + game.getTurn() + " turn."));
                outputTurn.setVisibility(View.VISIBLE);
                outputHand.setText(new String("Player " + i + " hand contents."));
                outputHand.setVisibility(View.VISIBLE);
                sendButton.setEnabled(true);
            }
        }

        if (game.updateDiscardedTileImage) {
            // get descriptor & its resource ID
            //TODO: use hash map for tile images?
            resourceId = getResources().getIdentifier(game.getDiscardedDescriptor(), "drawable", "com.example.mahjong");
            // update discarded tile image
            discardedImage.setImageResource(resourceId);
            discardedImage.setVisibility(View.VISIBLE);
            game.updateDiscardedTileImage = false;
        }
        //TODO: an else making it invisinle again?
    }


    //TODO: store Game object (and its children) in GameDB


    // add a listener for users database that updates dynamic table with users
    private void setCurrUserListener() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference().child("users").child(getCurrentUserUid());
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(dataSnapshot.getKey(),dataSnapshot.getChildrenCount() + " CHILD NODES CHANGED FOR CURRENT USER");
                curr_user = addCurrUserDetailsFirebase(dataSnapshot);
                // now we have last Game ID, so set current game listener
                setCurrMulitplayerGameListener();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    // add a listener for multiplayer games database that updates dynamic table with users
    private void setCurrMulitplayerGameListener() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference gameRef = database.getReference().child("multiplayer_games").child(curr_user.get_last_game_id());
        gameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(dataSnapshot.getKey(),dataSnapshot.getChildrenCount() + " CHILD NODES CHANGED FOR CURRENT GAME");
                curr_gdb = addCurrGameDetailsFirebase(dataSnapshot);
                initializeUI();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

}

