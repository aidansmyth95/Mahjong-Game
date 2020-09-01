package com.example.mahjong;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class PopUpMenuActivityTmp extends AppCompatActivity {

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private Button leaveButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up_menu_tmp);
        leaveButton = findViewById(R.id.show_popup_button);
        leaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createLeaveGameDialog();
            }
        });
    }

    public void createLeaveGameDialog() {
        dialogBuilder = new AlertDialog.Builder(this);
        final ViewGroup nullParent = null;
        final View leaveGamePopupView = getLayoutInflater().inflate(R.layout.popup, nullParent);
        leaveButton = (Button) leaveGamePopupView.findViewById(R.id.leave_button);

        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(leaveGamePopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        leaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //define leave button
                dialog.dismiss();
            }
        });
    }
}
