package com.example.sudokuforprogrammer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Button historyBack = findViewById(R.id.btn_historyBack);

        historyBack.setOnClickListener(v -> {
            finish();
        });
    }
}
