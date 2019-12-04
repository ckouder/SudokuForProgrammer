package com.example.sudokuforprogrammer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View decorView = getWindow().getDecorView();

        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        Button mainHistroy = findViewById(R.id.btn_mainHistory);

        mainHistroy.setOnClickListener(v -> {
            Intent historyActivity = new Intent(this, HistoryActivity.class);
            startActivity(historyActivity);
        });

        // Process of initiating a new game
        Button newGameButton = findViewById(R.id.btn_mainNewGame);
        newGameButton.setOnClickListener(v -> {
            Intent newGameActivity = new Intent(this, NewGameActivity.class);
            // @CHANGE
            newGameActivity.putExtra("difficulty", 36);
            startActivity(newGameActivity);
        });
    }
}
