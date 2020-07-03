package com.example.mahjong;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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
import mahjong_package.GameState;
import mahjong_package.User;

import static mahjong_package.FirebaseRepository.getCurrGameDetailsFirebase;
import static mahjong_package.FirebaseRepository.getCurrUserDetailsFirebase;
import static mahjong_package.FirebaseRepository.getCurrentUserUid;


public class MultiplayerActivity extends AppCompatActivity {

    // Firebase listeners for User and Game
    private Game currGame = new Game();
    private User currUser = new User();
    private int playerIdx;
    private boolean passed_tests = false;
    private Button sendButton;
    private EditText userInputText;
    private TextView outputText;
    private TextView outputTurn;
    private ImageView discardedImage;
    private final int nTotalTiles = 15;
    private ImageView[] handTiles = new ImageView[15];
    Handler gamePlayHandler = new Handler();
    Runnable gamePlayRunnable;
    private long gameDelay = 500;

    @Override
    public void onPause() {
        super.onPause();
        gamePlayHandler.removeCallbacks(gamePlayRunnable);
        //TODO: update Game status
        //TODO: in a runnable, all users check for this happening and go back to Game Select where they can join current or new game
    }

    @Override
    public void onResume() {
        if (passed_tests) {
            gamePlayHandler.postDelayed(gamePlayRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.e("Game analysis", "START GAME " + playerIdx);
                    currGame.playGame();
                    // player can respond
                    Log.e("Game analysis", "END GAME " + playerIdx);
                    // update UI - revealed hand, hidden hand, unused tile space, text
                    if (currGame.getAcceptingResponses()) {
                        updateUI();
                    }
                    // update most recently discarded tile
                    if (currGame.getUpdateDiscardedTileImage()) {
                        // get descriptor & its resource ID
                        //TODO: use hash map?
                        int resourceId = getResources().getIdentifier(currGame.getDiscardedDescriptor(), "drawable", "com.example.mahjong");
                        // update discarded tile image
                        discardedImage.setImageResource(resourceId);
                        discardedImage.setVisibility(View.VISIBLE);
                        currGame.setUpdateDiscardedTileImage(false);
                    }
                gamePlayHandler.postDelayed(gamePlayRunnable, gameDelay);
                }
            }, gameDelay);
        }
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer);
        // set listener on user - and in turn Game
        setCurrUserListener();
        initializeUI();
        // redirect Game system out and system error
        redirectGameSystemOut(outputText);
        redirectGameSystemErr(outputText);
        // test the test vectors before Game
        passed_tests = runAllGameTestVectors();
    }

    private void initializeUI() {
        outputTurn = (TextView) findViewById(R.id.p_turn);
        outputText = (TextView) findViewById(R.id.textViewOut);
        outputText.setMovementMethod(new ScrollingMovementMethod());
        sendButton = (Button) findViewById(R.id.send_button);
        sendButton.setEnabled(false);
        userInputText = (EditText) findViewById(R.id.p_input);
        discardedImage = (ImageView) findViewById(R.id.discarded);
        handTiles[0] = (ImageView) findViewById(R.id.h0);
        handTiles[1] = (ImageView) findViewById(R.id.h1);
        handTiles[2] = (ImageView) findViewById(R.id.h2);
        handTiles[3] = (ImageView) findViewById(R.id.h3);
        handTiles[4] = (ImageView) findViewById(R.id.h4);
        handTiles[5] = (ImageView) findViewById(R.id.h5);
        handTiles[6] = (ImageView) findViewById(R.id.h6);
        handTiles[7] = (ImageView) findViewById(R.id.h7);
        handTiles[8] = (ImageView) findViewById(R.id.h8);
        handTiles[9] = (ImageView) findViewById(R.id.h9);
        handTiles[10] = (ImageView) findViewById(R.id.h10);
        handTiles[11] = (ImageView) findViewById(R.id.h11);
        handTiles[12] = (ImageView) findViewById(R.id.h12);
        handTiles[13] = (ImageView) findViewById(R.id.h13);
        handTiles[14] = (ImageView) findViewById(R.id.h14);
        for (int h=0; h<nTotalTiles; h++) {
            handTiles[h].setVisibility(View.INVISIBLE);
        }
        outputTurn.setVisibility(View.INVISIBLE);
        discardedImage.setVisibility(View.INVISIBLE);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // if there is text, use it as player_input
                if (!TextUtils.isEmpty(userInputText.getText().toString())) {
                    // copy and clear user input, sending it to game
                    currGame.setPlayerResponse(playerIdx, userInputText.getText().toString());
                    userInputText.setText("");
                    // remove flags expecting user input and disable button
                    currGame.setRequestResponse(playerIdx, false);
                    sendButton.setEnabled(false);
                } else {
                    // print an empty input text warning in outputText if we expect user input
                    if (currGame.getRequestResponse(playerIdx)) {
                        outputText.setText(R.string.empty_edit_text);
                    }
                }
            }
        });
    }

    private void updateUI() {
        // update visualization of hidden hand
        ArrayList<String> hidden_tile_paths = currGame.descriptorToDrawablePath(currGame.getHiddenDescriptors());
        for (int j=0; j<hidden_tile_paths.size(); j++) {
            int resourceId = getResources().getIdentifier(hidden_tile_paths.get(j), "drawable", "com.example.mahjong");
            handTiles[j].setImageResource(resourceId);
            handTiles[j].setVisibility(View.VISIBLE);
        }
        // update revealed hand
        ArrayList<String> revealed_tile_paths = currGame.descriptorToDrawablePath(currGame.getRevealedDescriptors());
        for (int j=0; j<revealed_tile_paths.size(); j++) {
            int resourceId = getResources().getIdentifier(revealed_tile_paths.get(j), "drawable", "com.example.mahjong");
            handTiles[hidden_tile_paths.size()+j].setImageResource(resourceId);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) handTiles[j].getLayoutParams();
            //TODO: modify this?
            params.setMargins(0, 200, 0, 0) ;//left, top, right, bottom
            handTiles[j].setLayoutParams(params);
            handTiles[j].setVisibility(View.VISIBLE);
        }
        // update unused tiles
        for (int j=hidden_tile_paths.size()+revealed_tile_paths.size(); j<nTotalTiles; j++) {
            handTiles[j].setVisibility(View.INVISIBLE);
        }
        // update player's turn textview
        outputTurn.setText("Player " + currGame.getPlayerTurn() + " turn.");
        outputTurn.setVisibility(View.VISIBLE);
        // enable send button and its functionality if player's input is requested
        sendButton.setEnabled(currGame.getRequestResponse(playerIdx));
    }

    // add a listener for users database that updates dynamic table with users
    private void setCurrUserListener() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference().child("users").child(getCurrentUserUid());
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currUser = getCurrUserDetailsFirebase(dataSnapshot);
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
        DatabaseReference gameRef = database.getReference().child("multiplayer_games").child(currUser.getLastGameId());
        gameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(dataSnapshot.getKey(),dataSnapshot.getChildrenCount() + " CHILD NODES CHANGED FOR CURRENT GAME");
                currGame = getCurrGameDetailsFirebase(dataSnapshot);
                playerIdx = currGame.getPlayerIdx(currUser.getUid());
                Log.e("Response analysis", "PlayerIdx=" + playerIdx);
                if (passed_tests) {
                    currGame.playGame();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

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
        Game testGame = new Game();
        testGame.addPlayer("dummy_name", "dummy_uid");
        boolean clear_output = testGame.test_true_pong();
        if (clear_output) {
            clear_output = testGame.test_false_pong();
        }
        if (clear_output) {
            clear_output = testGame.test_true_kong();
        }
        if (clear_output) {
            clear_output = testGame.test_false_kong();
        }
        if (clear_output) {
            clear_output = testGame.test_true_mahjong();
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

}

