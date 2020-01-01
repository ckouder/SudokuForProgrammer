package com.example.sudokuforprogrammer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Button historyBack = findViewById(R.id.btn_historyBack);
        TextView historyDate = findViewById(R.id.text_recordDate);
        TextView historyRecord = findViewById(R.id.text_record);
        SharedPreferences highestScore = getSharedPreferences(
                getString(R.string.app_highest_score), MODE_PRIVATE);

        String highestScoreDate = highestScore.getString(getString(R.string.highest_score_date), null);
        long highestScoreRecord = highestScore.getLong(getString(R.string.app_highest_score), 0);

        if (highestScoreDate != null && highestScoreRecord != 0) {
            historyDate.setText(highestScoreDate);
            historyRecord.setText(String.format(Locale.US, "%dms", highestScoreRecord));
        }


        historyBack.setOnClickListener(v -> {
            finish();
        });
    }
}
