package com.example.mahjong;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

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
    private ImageView[] hand_tiles = new ImageView[14];

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

        // set System.out in all classes to be TextView
        System.setOut(new PrintStream(new OutputStream() {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            @Override public void write(int oneByte) throws IOException {
                outputStream.write(oneByte);
                outputText.setText(new String(outputStream.toByteArray()));
            }
        }));

        // set System.Err to write to TextView
        System.setErr(new PrintStream(new OutputStream() {

            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

            @Override public void write(int oneByte) throws IOException {
                errorStream.write(oneByte);
                outputText.setText(new String(errorStream.toByteArray()));
            }
        }));

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
                //do something
                game.playGame();
                int resourceId;

                for (int i=0; i<n_players; i++) {
                    if (game.request_response[i]) {
                        //TODO: a function to update resource
                        for (int j=0; j<14; j++) {

                            resourceId = getResources().getIdentifier(game.getHandDescriptor(j), "drawable", "com.example.mahjong");
                            hand_tiles[j].setImageResource(resourceId);
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

