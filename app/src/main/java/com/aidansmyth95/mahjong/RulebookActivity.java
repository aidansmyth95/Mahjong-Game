package com.aidansmyth95.mahjong;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

@SuppressLint("SetJavaScriptEnabled")
public class RulebookActivity extends AppCompatActivity {

    private final String TAG = "RulebookActivity";
    private Button leaveButton;
    private WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rulebook);

        // init the html file view
        webview  = this.findViewById(R.id.rulebook_html);
        displayHtmlFile("file:///android_asset/mj_rules.html");

        // init the button to leave rulebook
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.button_sound);
        leaveButton = findViewById(R.id.leave_rules_button);
        leaveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mp.start();
                // go back to previous activity
                RulebookActivity.super.onBackPressed();
            }
        });
    }

    private void displayHtmlFile(final String filename) {
        webview.getSettings().setJavaScriptEnabled(true);

        // FILE WAS TOO LARGE, SO https://stackoverflow.com/questions/23057988/file-size-exceeds-configured-limit-2560000-code-insight-features-not-availabl
        // converted from .odt to .pdf, and then to .html using Convertio (free, best results for images)
        webview.loadUrl(filename);

        webview.setVerticalScrollBarEnabled(true);
        webview.setHorizontalScrollBarEnabled(false);

        // Fix the width so it is not moveable horizontally
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setUseWideViewPort(true);

        // allow for pinching and zooming of document
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setDisplayZoomControls(false);
    }
}
