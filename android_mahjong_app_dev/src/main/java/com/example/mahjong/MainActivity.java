package com.example.mahjong;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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
import mahjong_package.TextUpdater;


public class MainActivity extends AppCompatActivity {

    private Button sendButton;
    private EditText userInputText;
    private TextView outputText;
    private int counter = 0;
    private Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendButton = (Button) findViewById(R.id.button);
        userInputText = (EditText) findViewById(R.id.textViewIn);
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

        game = new Game(4, outputText);

        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // check user input
                //FIXME: this still sets msg to be null.
                if (!TextUtils.isEmpty(userInputText.getText().toString())) {
                    String msg = getString(R.string.response_to_button, counter, userInputText.getText().toString());
                    outputText.setText(msg);
                    userInputText.setText("");
                    incrementCounter();
                    //game.playGame();
                } else {
                    // print an empty input text warning in outputText
                    outputText.setText(R.string.empty_edit_text);
                }
            }
        });
    }


    public void sendMessage(View v) {
        incrementCounter();
    }


    public void incrementCounter() {
        counter++;
    }













/*    // Allows classes to append text to the TextView UI output
    @Override
    public void updateTextView(String s) {
        //outputText = (TextView) findViewById(R.id.textViewOut);
        //outputText.append(s);
        //outputText.setText("THIS WAS SET.\n");
    }


    @Override
    public void updateTextView(String s, Object ...args) {
        //String s_final = String.format(s, args);
        //outputText = (TextView) findViewById(R.id.textViewOut);
        //outputText.append(s_final);
        //outputText.setText("THIS WAS SET.\n");
    }*/
}