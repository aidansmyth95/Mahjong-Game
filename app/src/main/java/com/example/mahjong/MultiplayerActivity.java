package com.example.mahjong;

import android.content.DialogInterface;
import android.content.Intent;
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
import androidx.appcompat.app.AlertDialog;
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
import mahjong_package.GameStatus;
import mahjong_package.User;

import static mahjong_package.FirebaseRepository.getCurrGameDetailsFirebase;
import static mahjong_package.FirebaseRepository.getCurrUserDetailsFirebase;
import static mahjong_package.FirebaseRepository.getCurrentUserUid;
import static mahjong_package.FirebaseRepository.updateMultiplayerGame;
import static mahjong_package.FirebaseRepository.userInactiveFirebaseUser;
import static mahjong_package.FirebaseRepository.userJoinedGameFirebase;
import static mahjong_package.FirebaseRepository.userPlayingGameFirebase;

//TODO: touch listener for tile to get its descriptor as toast form
//TODO: a scrolling activity for rules etc

public class MultiplayerActivity extends AppCompatActivity {

    private final int nTotalTiles = 15;
    private final long gameDelay = 500;
    private Game currGame = new Game(-1);
    private User currUser = new User();
    private int playerIdx;
    private boolean passed_tests = false;
    private Button sendButton;
    private EditText userInputText;
    private TextView outputText;
    private TextView outputTurn;
    private ImageView discardedImage;
    private ImageView[] handTiles = new ImageView[15];
    private Handler gamePlayHandler;
    private Runnable gamePlayRunnable;
    private static final String TAG = "MultiplayerActivity";

    private DatabaseReference dbRef;
    private ValueEventListener userListener, gameListener;
    private Boolean gamePaused;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, TAG+": onCreate");
        setContentView(R.layout.activity_multiplayer);
        initializeUI();
        // redirect Game system out and system error
        redirectGameSystemOut(outputText);
        redirectGameSystemErr(outputText);
        // test the test vectors before Game
        passed_tests = runAllGameTestVectors();
        gamePlayHandler = new Handler();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, TAG+": onStart");
        // set listener on user - and in turn Game
        setCurrUserListener();
        // user is at least joined when they start this activity
        userJoinedGameFirebase();
        gamePaused = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, TAG+": onPause");
    }

    @Override
    protected void onStop() {
        Log.i(TAG, TAG+": onStop");
        // remove handler runnable
        boolean update_game = false;
        gamePlayHandler.removeCallbacks(gamePlayRunnable);
        // remove listeners
        if (userListener != null) {
            dbRef.removeEventListener(userListener);
        }
        if (gameListener != null) {
            dbRef.removeEventListener(gameListener);
        }
        // update Game status if not already Paused
        if (currGame.getGameStatus() != GameStatus.PAUSED) {
            Log.i(TAG, TAG+": Pausing game.");
            currGame.setGameStatus(GameStatus.PAUSED);
            currGame.setPlayerPlayingStatus(false, playerIdx);
            update_game = true;
        }
        if (gamePaused) {
            userJoinedGameFirebase();
        } else {
            // if player leaving Game and it is not because it is paused
            userInactiveFirebaseUser();
            currGame.setPlayerPlayingStatus(false, playerIdx);
            update_game = true;
        }
        // save Game text output for reload on resume
        if (!outputText.getText().toString().isEmpty()) {
            currGame.setGameMessage(playerIdx, outputText.getText().toString());
            update_game = true;
        }
        // if we need to update game, update it in Firebase DB
        if (update_game) {
            updateMultiplayerGame(currGame);
        }
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, TAG+": onResume");
        // check if game has been paused by another user
        if (passed_tests) {
            gamePlayHandler.postDelayed(gamePlayRunnable = new Runnable() {
                @Override
                public void run() {
                    // this player is playing when we are in this state
                    currGame.setPlayerPlayingStatus(true, playerIdx);
                    // if all players are playing and game was previously paused, set game to be active and user to be playing
                    if (currGame.allPlayersPlaying() && currGame.getGameStatus() == GameStatus.PAUSED) {
                        Log.i(TAG, TAG + ": All " + currGame.countNumPlayersPlaying() + " are playing");
                        currGame.setGameStatus(GameStatus.ACTIVE);
                        userPlayingGameFirebase();
                    } else if (!currGame.allPlayersPlaying()) {
                        Log.i(TAG, TAG + ": Only " + currGame.countNumPlayersPlaying() + " are playing");
                        gamePaused = true;
                        // go to pause game activity
                        pauseGame();
                    }
                    // Do not play game if game is not active
                    if (currGame.getGameStatus() != GameStatus.ACTIVE) {
                        return;
                    }
                    currGame.playGame();
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
                    logGameStatus(currGame);
                    gamePlayHandler.postDelayed(gamePlayRunnable, gameDelay);
                }
            }, gameDelay);

        }
    }

    private void initializeUI() {
        outputTurn = findViewById(R.id.p_turn);
        outputText = findViewById(R.id.textViewOut);
        outputText.setMovementMethod(new ScrollingMovementMethod());
        sendButton = findViewById(R.id.send_button);
        sendButton.setEnabled(false);
        userInputText = findViewById(R.id.p_input);
        discardedImage = findViewById(R.id.discarded);
        handTiles[0] = findViewById(R.id.h0);
        handTiles[1] = findViewById(R.id.h1);
        handTiles[2] = findViewById(R.id.h2);
        handTiles[3] = findViewById(R.id.h3);
        handTiles[4] = findViewById(R.id.h4);
        handTiles[5] = findViewById(R.id.h5);
        handTiles[6] = findViewById(R.id.h6);
        handTiles[7] = findViewById(R.id.h7);
        handTiles[8] = findViewById(R.id.h8);
        handTiles[9] = findViewById(R.id.h9);
        handTiles[10] = findViewById(R.id.h10);
        handTiles[11] = findViewById(R.id.h11);
        handTiles[12] = findViewById(R.id.h12);
        handTiles[13] = findViewById(R.id.h13);
        handTiles[14] = findViewById(R.id.h14);
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
                    updateMultiplayerGame(currGame);
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
        outputTurn.setText(getString(R.string.waiting_room_players_turn, currGame.getPlayerTurn()));
        outputTurn.setVisibility(View.VISIBLE);
        // enable send button and its functionality if player's input is requested
        sendButton.setEnabled(currGame.getRequestResponse(playerIdx));
    }

    // add a listener for users database that updates dynamic table with users
    private void setCurrUserListener() {
        dbRef = FirebaseDatabase.getInstance().getReference();
        userListener = dbRef.child("users").child(getCurrentUserUid()).addValueEventListener(new ValueEventListener() {
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
        dbRef = FirebaseDatabase.getInstance().getReference();
        gameListener = dbRef.child("multiplayer_games").child(currUser.getLastGameId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG,TAG+": child nodes changed for game = "+dataSnapshot.getChildrenCount());
                currGame = getCurrGameDetailsFirebase(dataSnapshot);
                if (currGame.gameExists()) {
                    playerIdx = currGame.getPlayerIdx(currUser.getUid());
                    // save Game text output for reload on resume
                    if (outputText.getText().toString().isEmpty()) {
                        outputText.setText(currGame.getGameMessage(playerIdx));
                        currGame.setGameMessage(playerIdx,"");
                    }
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
        Log.i(TAG, TAG+": Testing Game");
        Game testGame = new Game(1);
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
            Log.i(TAG, TAG+": Passed Game tests.");
            return true;
        } else {
            Log.i(TAG, TAG+": Failed Game tests.");
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, TAG+": onDestroy");
    }

    @Override
    public void onRestart() {
        super.onRestart();
        Log.i(TAG, TAG+": onRestart");
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, TAG+": onBackPressed");
        new AlertDialog.Builder(this)
                .setTitle("Really Exit Game?")
                .setMessage("Are you sure you want to exit the game?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(MultiplayerActivity.this, GameModeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }).create().show();
    }

    public void pauseGame() {
        if (gamePaused) {
            Log.i(TAG, "Pausing game and moving to PausedGameActivity...");
            // update game status
            currGame.setGameStatus(GameStatus.PAUSED);
            updateMultiplayerGame(currGame);
            // go to new activity
            Intent intent = new Intent(MultiplayerActivity.this, PausedGameActivity.class);
            startActivity(intent);
        } else {
            Log.e(TAG, "Cannot pause Game if Boolean pauseGame value is false");
        }
    }

    public static void logGameStatus(Game game) {
        Log.i(TAG, TAG + ": \n---------- Status update  ----------");
        Log.i(TAG, TAG + ": Game name is " + game.getGameName());
        Log.i(TAG, TAG + ": Number of players playing is " + game.countNumPlayersPlaying() + " are playing");
        Log.i(TAG, TAG + ": All players are playing? = " + game.allPlayersPlaying());
        Log.i(TAG, TAG + ": Players playing are " + game.namePlayersPlaying());
        Log.i(TAG, TAG + ": Players not playing are " + game.namePlayersNotPlaying());
        Log.i(TAG, TAG + ": Game status is " + game.getGameStatus());
        Log.i(TAG, TAG + ": -------------------------------\n");
    }
}

