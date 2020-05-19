package com.example.mahjong;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import mahjong_package.Game;


public class MainActivity extends AppCompatActivity {

    private Game game;
    private Handler handler = new Handler();
    private Runnable runnable;

    private Button sendButton;
    private EditText[] userInputText = new EditText[4];
    private TextView outputText;
    private TextView outputTurn;
    private ImageView discardedImage;
    private final int n_total_tiles = 15;
    private ImageView[] hand_tiles = new ImageView[15];

    private final int n_players = 4;
    private final int delay = 1000; //Delay game by this many milliseconds.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendButton = (Button) findViewById(R.id.send_button);
        sendButton.setEnabled(false);
        userInputText[0] = (EditText) findViewById(R.id.P0_input);
        userInputText[1] = (EditText) findViewById(R.id.P1_input);
        userInputText[2] = (EditText) findViewById(R.id.P2_input);
        userInputText[3] = (EditText) findViewById(R.id.P3_input);
        outputText = (TextView) findViewById(R.id.textViewOut);
        outputTurn = (TextView) findViewById(R.id.p_turn);
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

        // set System.out in all classes to be TextView
        System.setOut(new PrintStream(new OutputStream() {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            @Override public void write(int oneByte) {
                outputStream.write(oneByte);
                outputText.setText(new String(outputStream.toByteArray()));
            }
        }));

        // set System.Err to write to TextView
        System.setErr(new PrintStream(new OutputStream() {

            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

            @Override public void write(int oneByte) {
                errorStream.write(oneByte);
                outputText.setText(new String(errorStream.toByteArray()));
            }
        }));

        // tests before Game
        game = new Game(n_players);
        boolean clear_output = true;
        clear_output = game.test_true_pong();
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
            System.setOut(new PrintStream(new OutputStream() {

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                @Override public void write(int oneByte) {
                    outputStream.write(oneByte);
                    outputText.setText(new String(outputStream.toByteArray()));
                }
            }));
        }

        // start Game
        game = new Game(n_players);

        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // check user input for all players
                for (int i=0; i<n_players; i++) {
                    // if there is text, use it as player_input
                    if (!TextUtils.isEmpty(userInputText[i].getText().toString())) {
                        // copy and clear user input, sending it to game
                        game.player_input[i] = userInputText[i].getText().toString();
                        userInputText[i].setText("");
                        // remove flags expecting user input and disable button
                        game.request_response[i] = false;
                        sendButton.setEnabled(false);
                    } else {
                        // print an empty input text warning in outputText if we expect user input
                        if (game.request_response[i]) {
                            outputText.setText(R.string.empty_edit_text);
                        }
                    }
                }
            }
        });
    }


    @Override
    protected void onResume() {
        //start handler as activity become visible

        handler.postDelayed( runnable = new Runnable() {
            public void run() {
                // play the Game
                game.playGame();
                int resourceId;

                for (int i=0; i<n_players; i++) {

                    if (game.request_response[i]) {

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
                        sendButton.setEnabled(true);
                    }
                }

                if (game.update_discarded_tile_image) {
                    // get descriptor & its resource ID
                    //TODO: use hash map?
                    resourceId = getResources().getIdentifier(game.getDiscardedDescriptor(), "drawable", "com.example.mahjong");
                    // update discarded tile image
                    discardedImage.setImageResource(resourceId);
                    game.update_discarded_tile_image = false;
                }

                handler.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();
    }

    // If onPause() is not included the threads will double up when you reload the activity
    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }

}

