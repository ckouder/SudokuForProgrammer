package com.example.sudokuforprogrammer;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class NewGameActivity extends AppCompatActivity
        implements SudokuBlock.OnFragmentInteractionListener {


    /** pointer that stored [int row, int col] pair. */
    public int[] pointer = new int[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

    }

    public void onFragmentInteraction(Uri uri) {

    }
}
