package com.example.sudokuforprogrammer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.apache.pig.impl.util.ObjectSerializer;
import org.json.JSONException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String QUOTATION_URL = "https://api.forismatic.com/api/1.0/?method=getQuote&key=4&format=json&lang=en";

    private static final String QUOTATION_TYPE_CONTENT = "content";

    private static final String QUOTATION_TYPE_AUTHOR = "author";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View decorView = getWindow().getDecorView();

        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        Button mainHistory = findViewById(R.id.btn_mainHistory);

        mainHistory.setOnClickListener(v -> {
            Intent historyActivity = new Intent(this, HistoryActivity.class);
            startActivity(historyActivity);
        });

        // Process of initiating a new game
        Button newGameButton = findViewById(R.id.btn_mainNewGame);
        newGameButton.setOnClickListener(v -> {
            Intent newGameActivity = new Intent(this, NewGameActivity.class);
            // @CHANGE
            newGameActivity.putExtra("game", getString(R.string.start_new_game));
            newGameActivity.putExtra("difficulty", Constants.DIFFICULTY_EASY);
            startActivity(newGameActivity);
        });

        // Process of continuing a saved game
        Button continueGameButton = findViewById(R.id.btn_mainContinue);
        SharedPreferences savedGame = getSharedPreferences(getString(R.string.app_saved_game), MODE_PRIVATE);
        try {
            Game gameObject = (Game) ObjectSerializer.deserialize(savedGame.getString(
                    getString(R.string.app_saved_game), null));

            if (gameObject == null) {
                continueGameButton.setVisibility(View.INVISIBLE);
            } else {
                continueGameButton.setOnClickListener(v -> {
                    System.out.println("continue button is clicked");
                    Intent newGameActivity = new Intent(this, NewGameActivity.class);
                    // @CHANGE
                    newGameActivity.putExtra("game", getString(R.string.continue_game));
                    newGameActivity.putExtra("gameContent", gameObject);
                    startActivity(newGameActivity);
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        renderQuote();
    }

    private void renderQuote() {
        final TextView quoteContentContainer = findViewById(R.id.text_mainMaxim);
        final TextView quoteAuthorContainer = findViewById(R.id.text_maximFrom);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        JsonObjectRequest requestQuote = new JsonObjectRequest(Request.Method.GET, QUOTATION_URL, null,

            response -> {
                try {
                    String quoteContent = formatQuote(response.getString("quoteText"), QUOTATION_TYPE_CONTENT);
                    String quoteAuthor = formatQuote(response.getString("quoteAuthor"), QUOTATION_TYPE_AUTHOR);
                    if (!quoteContent.equals("")) {
                        quoteContentContainer.setText(quoteContent);
                    } else {
                        quoteContentContainer.setText(getResources().getText(R.string.quote_content_default));
                        quoteAuthorContainer.setText(getResources().getText(R.string.quote_author_default));
                    }
                    System.out.println("[" + quoteAuthor.trim() + "]");
                    if (!quoteAuthor.trim().equals("")) {
                        quoteAuthorContainer.setText(quoteAuthor);
                    } else {
                        quoteAuthorContainer.setText(formatQuote("Anonymous", QUOTATION_TYPE_AUTHOR));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                    // : )
                    System.out.println("Rip bro you done fucked up.");
                }
            },
            error -> {
                error.printStackTrace();
                quoteContentContainer.setText(getResources().getText(R.string.quote_content_default));
                quoteAuthorContainer.setText(getResources().getText(R.string.quote_author_default));
            });

        // Add the request to the RequestQueue.
        queue.add(requestQuote);
    }

    private String formatQuote(String rawString, String formatType) {
        if (formatType.equals(QUOTATION_TYPE_CONTENT)) {
            return "\"" + rawString.trim() + "\"";

        } else if (formatType.equals(QUOTATION_TYPE_AUTHOR)) {
            return "by " + rawString;

        } else {
            return rawString;
        }
    }
}
