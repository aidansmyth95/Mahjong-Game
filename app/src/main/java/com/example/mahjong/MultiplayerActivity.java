package com.example.mahjong;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import mahjong_package.FirebaseRepository;
import mahjong_package.Game;
import mahjong_package.GameStatus;
import mahjong_package.User;

import static mahjong_package.FirebaseRepository.getCurrGameDetailsFirebase;
import static mahjong_package.FirebaseRepository.getCurrUserDetailsFirebase;
import static mahjong_package.FirebaseRepository.getCurrentUserUid;
import static mahjong_package.FirebaseRepository.updateMultiplayerGame;


//TODO: a pull up from bottom to see all the previous discards
//TODO: an interactive feel, remove text box. No need for player_input or system.out.print methods, will simplify Game code

public class MultiplayerActivity extends AppCompatActivity {

    private static final String TAG = "MultiplayerActivity";
    private Game currGame = new Game(-1);
    private User currUser = new User();
    private final long gameDelayMs = 500;
    private int playerIdx;
    private boolean passed_tests = false;
    private boolean gamePausedAwaitingPlayers;
    private Button sendButton;
    private EditText userInputText;
    private TextView outputText, outputTurn;
    private CircularTextView numFlowersText;
    private ImageView discardedImage, flowerPileImage;
    private  ArrayList<String> hidden_descriptors = new ArrayList<>();
    private  ArrayList<String> revealed_descriptors = new ArrayList<>();
    private ImageView[] handTiles = new ImageView[15];
    private ImageView[] revealedTiles = new ImageView[15];
    private Handler gamePlayHandler = new Handler();
    private Runnable gamePlayRunnable;
    private DatabaseReference dbRef;
    private ValueEventListener userListener, gameListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, TAG+": onCreate");
        setContentView(R.layout.activity_multiplayer);
        // initialize the UI components
        initializeUI();
        // redirect Game system out and system error
        redirectGameSystemOut(outputText);
        redirectGameSystemErr(outputText);
        // test the test vectors before Game
        passed_tests = runAllGameTestVectors();
    }

    // initialize the UI components and their touch listeners, visibility etc.
    private void initializeUI() {
        outputTurn = findViewById(R.id.p_turn);
        outputText = findViewById(R.id.textViewOut);
        outputText.setMovementMethod(new ScrollingMovementMethod());
        sendButton = findViewById(R.id.send_button);
        sendButton.setEnabled(false);
        userInputText = findViewById(R.id.p_input);
        discardedImage = findViewById(R.id.discarded);
        outputTurn.setVisibility(View.INVISIBLE);
        discardedImage.setVisibility(View.INVISIBLE);
        discardedImage.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                Log.i(TAG, TAG + ": ");
                // if visible and enough hidden descriptors to be valid
                if (discardedImage.getVisibility() == View.VISIBLE) {
                    String s = currGame.getLatestDiscard().getDescriptor();
                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });
        flowerPileImage = findViewById(R.id.flower_pile);
        flowerPileImage.setVisibility(View.INVISIBLE);
        flowerPileImage.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                Log.i(TAG, TAG + ": ");
                // if visible and enough hidden descriptors to be valid
                if (flowerPileImage.getVisibility() == View.VISIBLE) {
                    String s = currGame.getFlowersCollectedString(playerIdx);
                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });
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
        revealedTiles[0] = findViewById(R.id.r0);
        revealedTiles[1] = findViewById(R.id.r1);
        revealedTiles[2] = findViewById(R.id.r2);
        revealedTiles[3] = findViewById(R.id.r3);
        revealedTiles[4] = findViewById(R.id.r4);
        revealedTiles[5] = findViewById(R.id.r5);
        revealedTiles[6] = findViewById(R.id.r6);
        revealedTiles[7] = findViewById(R.id.r7);
        revealedTiles[8] = findViewById(R.id.r8);
        revealedTiles[9] = findViewById(R.id.r9);
        revealedTiles[10] = findViewById(R.id.r10);
        revealedTiles[11] = findViewById(R.id.r11);
        revealedTiles[12] = findViewById(R.id.r12);
        revealedTiles[13] = findViewById(R.id.r13);
        revealedTiles[14] = findViewById(R.id.r14);
        int nTotalTileSlots = 15;
        for (int h = 0; h< nTotalTileSlots; h++) {
            handTiles[h].setVisibility(View.INVISIBLE);
            revealedTiles[h].setVisibility(View.INVISIBLE);
            final int finalH = h;
            handTiles[h].setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    Log.i(TAG, TAG + ": ");
                    // if visible and enough hidden descriptors to be valid
                    if (handTiles[finalH].getVisibility() == View.VISIBLE && finalH <= hidden_descriptors.size()) {
                        Toast.makeText(getApplicationContext(), "Tile " + finalH + ": " + hidden_descriptors.get(finalH), Toast.LENGTH_LONG).show();
                    }
                    return false;
                }
            });
            revealedTiles[h].setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    Log.i(TAG, TAG + ": ");
                    // if visible and enough hidden descriptors to be valid
                    if (revealedTiles[finalH].getVisibility() == View.VISIBLE && finalH <= revealed_descriptors.size()) {
                        Toast.makeText(getApplicationContext(), "Tile " + finalH + ": " + revealed_descriptors.get(finalH), Toast.LENGTH_LONG).show();
                    }
                    return false;
                }
            });
        }
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
        numFlowersText = findViewById(R.id.circularFlowersTextView);
        numFlowersText.setStrokeWidth(1);
        numFlowersText.setStrokeColor("#ffffff");
        numFlowersText.setSolidColor("#F41B1B");
        numFlowersText.setVisibility(View.INVISIBLE);
        // set rulebook listener
        ImageButton rulebook = findViewById(R.id.rules_at_multiplayer_game);
        rulebook.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MultiplayerActivity.this, RulebookActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, TAG+": onStart");
        // set listener on user - and in turn Game
        setCurrUserListener();
        // user is at least joined when they start this activity
        FirebaseRepository.userJoinedGameFirebase();
        gamePausedAwaitingPlayers = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, TAG+": onPause");
    }

    @Override
    protected void onStop() {
        /*
            Remove listeners.
            Set game status to paused (it previously was playing)
            Update user's status based on what they are doing.
            Save Game text if any.
            Update game if needed.
         */

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
            Log.i(TAG, TAG + ": Pausing game.");
            currGame.setGameStatus(GameStatus.PAUSED);
            currGame.setPlayerPlayingStatus(false, playerIdx);
            update_game = true;
        }
        if (gamePausedAwaitingPlayers) {
            // if the game was paused for not having enough players
            FirebaseRepository.userJoinedGameFirebase();
        } else {
            // otherwise player leaving Game and not waiting to resume
            FirebaseRepository.userInactiveFirebaseUser();
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
        // create a runnable
        if (passed_tests) {
            gamePlayHandler.postDelayed(gamePlayRunnable = new Runnable() {
                @Override
                public void run() {
                    // this player is playing when we are in this state
                    currGame.setPlayerPlayingStatus(true, playerIdx);
                    // if all players are playing and game was previously paused
                    if (currGame.allPlayersPlaying() && currGame.getGameStatus() == GameStatus.PAUSED) {
                        Log.i(TAG, TAG + ": All " + currGame.countNumPlayersPlaying() + " are playing");
                        // set game to be active and user to be playing
                        currGame.setGameStatus(GameStatus.ACTIVE);
                        FirebaseRepository.userPlayingGameFirebase();
                    } else if (!currGame.allPlayersPlaying()) {
                        // otherwise all players are not playing and we need to pause game
                        Log.i(TAG, TAG + ": Only " + currGame.countNumPlayersPlaying() + " are playing");
                        gamePausedAwaitingPlayers = true;
                        // go to pause game activity
                        pauseGame();
                    }
                    // Do not play game if game is not active
                    if (currGame.getGameStatus() != GameStatus.ACTIVE) {
                        return;
                    }
                    if (currGame.playGame()) {
                        // if the game is over
                        if (currGame.getWinnerIdx() == playerIdx) {
                            int userWins = currUser.getWinTallies();
                            userWins++;
                            currUser.setWinTallies(userWins);
                            FirebaseRepository.updateUser(currUser);
                        }
                    }
                    // update record of hidden & revealed descriptors
                    hidden_descriptors = currGame.getHiddenDescriptors();
                    revealed_descriptors = currGame.getRevealedDescriptors();
                    // update UI - revealed hand, hidden hand, unused tile space, text
                    if (currGame.getUpdateUI()) {
                        updateUI();
                        currGame.setUpdateUI(false);
                    }
                    // enable send button and its functionality if player's input is requested
                    sendButton.setEnabled(currGame.getRequestResponse(playerIdx) && currGame.getAcceptingResponses());
                    // debug info on Game status and values of concern
                    logcatGameStatus(currGame, playerIdx);
                    // delay runnable by gameDelayMs milliseconds - reduces annoying refresh
                    gamePlayHandler.postDelayed(gamePlayRunnable, gameDelayMs);
                }
            }, gameDelayMs);

        }
    }

    /* Update UI */
    private void updateUI() {
        Log.i(TAG, TAG + " Updating UI...");
        // set all handTiles to be invisible
        for (int i=0; i<14; i++) {
            handTiles[i].setVisibility(View.INVISIBLE);
            revealedTiles[i].setVisibility(View.INVISIBLE);
        }
        // update visualization of hidden hand, set visible
        ArrayList<String> hidden_tile_paths = currGame.descriptorToDrawablePath(hidden_descriptors);
        for (int j=0; j<hidden_tile_paths.size(); j++) {
            String hidden_tile_path = hidden_tile_paths.get(j);
            Log.e(TAG, TAG + " hidden tile " + hidden_tile_path);

            int resourceId = getResources().getIdentifier(hidden_tile_path, "drawable", "com.example.mahjong");
            if (resourceId == 0) {
                // if resource does not exist
                Log.e(TAG, TAG + " Resource ID for hidden tile " + j + " is " + resourceId);
            } else {
                handTiles[j].setImageResource(resourceId);
                handTiles[j].setVisibility(View.VISIBLE);
            }
            // set invisible if no tile
            if (hidden_tile_path.equals("no_tile")) {
                // we have a no tile, set invisible
                handTiles[j].setVisibility(View.INVISIBLE);
            }
        }
        // update revealed hand
        ArrayList<String> revealed_tile_paths = currGame.descriptorToDrawablePath(currGame.getRevealedDescriptors());
        for (int j=0; j<revealed_tile_paths.size(); j++) {
            String revealed_tile_path = revealed_tile_paths.get(j);
            Log.e(TAG, TAG + " revealed tile " + revealed_tile_path);

            int resourceId = getResources().getIdentifier(revealed_tile_path, "drawable", "com.example.mahjong");
            if (resourceId == 0) {
                // if resource does not exist
                Log.e(TAG, TAG + " Resource ID for revealed tile " + j + " is " + resourceId);
            } else {
                revealedTiles[j].setImageResource(resourceId);
                revealedTiles[j].setVisibility(View.VISIBLE);
            }
            // set invisible if no tile
            if (revealed_tile_path.equals("no_tile")) {
                // we have a no tile, set invisible
                revealedTiles[j].setVisibility(View.INVISIBLE);
            }
        }
        // update player's turn textview
        outputTurn.setText(getString(R.string.waiting_room_players_turn, currGame.getPlayerTurn()));
        outputTurn.setVisibility(View.VISIBLE);
        // show flower pile if there are any for that player
        String latest_flower = currGame.getLatestFlowersCollectedDescriptorResource(this.playerIdx);
        int resourceId = getResources().getIdentifier(latest_flower, "drawable", "com.example.mahjong");
        if (resourceId == 0) {
            // if resource does not exist
            Log.e(TAG, TAG + ": Resource ID for latest flower is " + resourceId);
        } else {
            flowerPileImage.setImageResource(resourceId);
            numFlowersText.setText(Integer.toString(currGame.getFlowersCount(playerIdx)));
            flowerPileImage.setVisibility(View.VISIBLE);
            numFlowersText.setVisibility(View.VISIBLE);
        }
        // if no tile or no valid resource, set invisible
        if (latest_flower.equals("no_tile") || resourceId == 0) {
            numFlowersText.setText(Integer.toString(0));
            numFlowersText.setVisibility(View.INVISIBLE);
            flowerPileImage.setVisibility(View.INVISIBLE);
        }
        // update most recently discarded tile
        String latest_discard = currGame.getLatestDiscardedDescriptorResource();
        resourceId = getResources().getIdentifier(currGame.getLatestDiscardedDescriptorResource(), "drawable", "com.example.mahjong");
        if (resourceId == 0) {
            // if resource does not exist
            Log.e(TAG, TAG + ": Resource ID for latest discard is " + resourceId);
        } else {
            discardedImage.setImageResource(resourceId);
            discardedImage.setVisibility(View.VISIBLE);
        }
        // if no tile or no valid resource, set invisible
        if (latest_discard.equals("no_tile") || resourceId == 0) {
            discardedImage.setVisibility(View.INVISIBLE);
        }
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
            clear_output = testGame.test_true_chow();
        }
        if (clear_output) {
            clear_output = testGame.test_false_chow();
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
        if (gamePausedAwaitingPlayers) {
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

    public static void logcatGameStatus(Game game, int playerIdx) {
        Log.i(TAG, TAG + ": \n---------- Status update  ----------");
        Log.i(TAG, TAG + ": Game name is " + game.getGameName());
        Log.i(TAG, TAG + ": Number of players playing is " + game.countNumPlayersPlaying() + " are playing");
        Log.i(TAG, TAG + ": All players are playing? = " + game.allPlayersPlaying());
        Log.i(TAG, TAG + ": Players playing are " + game.namePlayersPlaying());
        Log.i(TAG, TAG + ": Players not playing are " + game.namePlayersNotPlaying());
        Log.i(TAG, TAG + ": Game status is " + game.getGameStatus());
        Log.i(TAG, TAG + ": Game accepting responses? " + game.getAcceptingResponses());
        Log.i(TAG, TAG + ": Players asked to respond? " + game.listRequestResponse());
        Log.i(TAG, TAG + ": This Player's ID? " + playerIdx);
        Log.i(TAG, TAG + ": Latest flower descriptor? " + game.getLatestFlowersCollectedDescriptorResource(playerIdx));
        Log.i(TAG, TAG + ": \n-------------------------------\n");
    }
}

