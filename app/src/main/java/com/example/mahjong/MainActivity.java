package com.example.mahjong;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import mahjong_package.Game;


public class MainActivity extends AppCompatActivity {

    private Button sendButton;
    private EditText userInputText[] = new EditText[4];
    private TextView outputText;

    private Game game;
    Handler handler = new Handler();
    Runnable runnable;
    final int delay = 1000; //Delay game by this many milliseconds.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendButton = (Button) findViewById(R.id.button);
        sendButton.setEnabled(false);
        userInputText[0] = (EditText) findViewById(R.id.P0_input);
        userInputText[1] = (EditText) findViewById(R.id.P1_input);
        userInputText[2] = (EditText) findViewById(R.id.P2_input);
        userInputText[3] = (EditText) findViewById(R.id.P3_input);
        outputText = (TextView) findViewById(R.id.textViewOut);
        outputText.setMovementMethod(new ScrollingMovementMethod());

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

        game = new Game(4);

        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // check user input for all players
                for (int i=0; i<4; i++) {
                    // if there is text, use it as player_input
                    if (!TextUtils.isEmpty(userInputText[i].getText().toString())) {
                        // copy and clear user input, sending it to game
                        game.gs.player_input[i] = userInputText[i].getText().toString();
                        userInputText[i].setText("");
                        // remove flags expecting user input and disable button
                        game.gs.request_response[i] = false;
                        sendButton.setEnabled(false);
                    } else {
                        // print an empty input text warning in outputText if we expect user input
                        if (game.gs.request_response[i]) {
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

                for (int i=0; i<4; i++) {
                    if (game.gs.request_response[i] == true) {
                        sendButton.setEnabled(true);
                    }
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