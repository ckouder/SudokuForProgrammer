package com.example.sudokuforprogrammer;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class NewGameActivity extends AppCompatActivity
        implements SudokuBlock.OnFragmentInteractionListener {

    /** The game object. */
    public Game game;

    /** pointer that stored [int row, int col] pair. */
    public int[] pointer = new int[2];

    private View[][] fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialize a new game
        this.game = new Game();
        setEventListenerForNumberButtons();

        // Update UI
        renderGrid();

    }

    public void onFragmentInteraction(Uri uri) {

    }

    public void setEventListenerForNumberButtons() {

        Button btn_Num0 = findViewById(R.id.btn_Num0);
        btn_Num0.setOnClickListener(v -> buttonAction("0"));

        Button btn_Num1 = findViewById(R.id.btn_Num1);
        btn_Num1.setOnClickListener(v -> buttonAction("1"));

        Button btn_Num2 = findViewById(R.id.btn_Num2);
        btn_Num2.setOnClickListener(v -> buttonAction("2"));

        Button btn_Num3 = findViewById(R.id.btn_Num3);
        btn_Num3.setOnClickListener(v -> buttonAction("3"));

        Button btn_Num4 = findViewById(R.id.btn_Num4);
        btn_Num4.setOnClickListener(v -> buttonAction("4"));

        Button btn_Num5 = findViewById(R.id.btn_Num5);
        btn_Num5.setOnClickListener(v -> buttonAction("5"));

        Button btn_Num6 = findViewById(R.id.btn_Num6);
        btn_Num6.setOnClickListener(v -> buttonAction("6"));

        Button btn_Num7 = findViewById(R.id.btn_Num7);
        btn_Num7.setOnClickListener(v -> buttonAction("7"));

        Button btn_Num8 = findViewById(R.id.btn_Num8);
        btn_Num8.setOnClickListener(v -> buttonAction("8"));

        Button btn_Num9 = findViewById(R.id.btn_Num9);
        btn_Num9.setOnClickListener(v -> buttonAction("9"));

        Button btn_NumA = findViewById(R.id.btn_NumA);
        btn_NumA.setOnClickListener(v -> buttonAction("A"));

        Button btn_NumB = findViewById(R.id.btn_NumB);
        btn_NumB.setOnClickListener(v -> buttonAction("B"));

        Button btn_NumC = findViewById(R.id.btn_NumC);
        btn_NumC.setOnClickListener(v -> buttonAction("C"));

        Button btn_NumD = findViewById(R.id.btn_NumD);
        btn_NumD.setOnClickListener(v -> buttonAction("D"));

        Button btn_NumE = findViewById(R.id.btn_NumE);
        btn_NumE.setOnClickListener(v -> buttonAction("E"));

        Button btn_NumF = findViewById(R.id.btn_NumF);
        btn_NumF.setOnClickListener(v -> buttonAction("F"));

    }

    /**
     * This is what happens when a 0~F button is clicked.
     * @param option the value displayed on the button
     */
    public void buttonAction(String option) {
        // Fetch pointer and option values
        int row = pointer[0];
        int column = pointer[1];
        int number = Integer.parseInt(option, 16);
        // If this is an empty cell and it is the right number indeed…
        if ((game.puzzleGrid.cells[row][column].value == -1
                && number == game.answerGrid.cells[row][column].value)) {
            // Update puzzle grid
            Solver.confirmCell(game.puzzleGrid, game.puzzleGrid.cells[row][column], number);
            // Reflect the update on UI
            renderGrid();
        }
        // Else… Do nothing for the moment being
        // TODO: Add some code if the player makes a mistake.
    }

    /** Renders the grid with player's current progress. */
    public void renderGrid() {
        // Fetch the grid UI
        TableLayout gridUI = findViewById(R.id.sudokuPaper);
        for (int i = 0; i < Grid.BASE_INDEX; i++) {
            TableRow blockRow = (TableRow) gridUI.getChildAt(i);
            for (int j = 0; j < Grid.BASE_INDEX; j++) {
                View block = blockRow.getChildAt(j);
                Grid.Cell[] blockValues = game.puzzleGrid.getBlock(new int[] {i, j});
                // The int variable blockIndex indicates which block is currently selected
                for (int blockIndex = 0; blockIndex < blockValues.length; blockIndex++) {
                    int rowInBlock = blockIndex / Grid.BASE_INDEX;
                    int columnInBlock = blockIndex % Grid.BASE_INDEX;
                    String idSuffix = "" + rowInBlock + columnInBlock;
                    int id = getResources().getIdentifier("sudokuUnit_" + idSuffix,
                            "id", getPackageName());
                    int value = blockValues[blockIndex].value;
                    TextView sudokuUnit = (TextView) block.findViewById(id);
                    if (value == -1) {
                        sudokuUnit.setText(" ");
                    } else {
                        sudokuUnit.setText(Integer.toHexString(value).toUpperCase());
                    }
                    // Set event listener for each cell
                    int blockIndicator = blockIndex;
                    sudokuUnit.setOnClickListener(v -> {
                        // Change the row value of the pointer
                        this.pointer[0] = blockIndicator / Grid.BASE_INDEX * Grid.BASE_INDEX
                                + rowInBlock;
                        // Change the column value of the pointer
                        this.pointer[1] = blockIndicator % Grid.BASE_INDEX * Grid.BASE_INDEX
                                + columnInBlock;
                    });
                }
            }
        }
    }
}
