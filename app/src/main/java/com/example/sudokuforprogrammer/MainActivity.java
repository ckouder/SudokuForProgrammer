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
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

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
                    quoteContentContainer.setText(formatQuote(response.getString("quoteText"), QUOTATION_TYPE_CONTENT));
                    quoteAuthorContainer.setText(formatQuote(response.getString("quoteAuthor"), QUOTATION_TYPE_AUTHOR));

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
            return "\"" + rawString + "\"";

        } else if (formatType.equals(QUOTATION_TYPE_AUTHOR)) {
            return "by " + rawString;

        } else {
            return rawString;
        }
    }
}
