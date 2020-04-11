package com.example.mahjong;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button sendButton;
    private EditText userInputText;
    private TextView outputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendButton = (Button) findViewById(R.id.button);
        userInputText = (EditText) findViewById(R.id.textViewIn);
        outputText = (TextView) findViewById(R.id.textViewOut);

        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // check user input
                //FIXME: this still sets msg to be null.
                if (!TextUtils.isEmpty(userInputText.getText().toString())) {
                    String msg = getString(R.string.response_to_button, userInputText.getText().toString());
                    outputText.setText(msg);
                    userInputText.setText("");
                } else {
                    // print an empty input text warning in outputText
                    outputText.setText(R.string.empty_edit_text);
                }
            }
        });
    }

}