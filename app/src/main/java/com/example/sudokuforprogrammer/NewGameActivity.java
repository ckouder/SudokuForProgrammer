package com.example.sudokuforprogrammer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class NewGameActivity extends AppCompatActivity
        implements SudokuBlock.OnFragmentInteractionListener {

    /** The game object. */
    public Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialize a new game
        this.game = new Game();
        setEventListenersForNumberButtons();
        setEventListenersForGameControlButtons();
        setEventListenersForDirectionButtons();

        // Update UI
        renderGrid();

    }

    public void onFragmentInteraction(Uri uri) {

    }

    /** Set event listeners for game control buttons. */
    public void setEventListenersForGameControlButtons() {
        Button quitButton = findViewById(R.id.btn_gameQuit);
        TextView timerText = findViewById(R.id.text_timer);
        ImageButton timerControlButton = findViewById(R.id.btn_gameTimerControl);
        ImageButton gameControlButton = findViewById(R.id.btn_gameControl);

        // set listener for timer control
        final String TIMER_IS_SHOWN = getResources().getString(R.string.game_timer_state_shown);
        final String TIMER_IS_HIDED = getResources().getString(R.string.game_timer_state_hided);
        timerControlButton.setOnClickListener(v -> {

            if (timerControlButton.getTag().equals(TIMER_IS_SHOWN)) {
                timerText.setVisibility(View.INVISIBLE);
                timerControlButton.setImageDrawable(getDrawable(R.drawable.ic_timer_hide));
                timerControlButton.setTag(TIMER_IS_HIDED);

            } else if (timerControlButton.getTag().equals(TIMER_IS_HIDED)) {
                timerText.setVisibility(View.VISIBLE);
                timerControlButton.setImageDrawable(getDrawable(R.drawable.ic_timer_show));
                timerControlButton.setTag(TIMER_IS_SHOWN);
            }
        });
    }

    /** Set event listeners for each token used in game. */
    public void setEventListenersForNumberButtons() {
        for (char c : Constants.TOKENS) {
            int id = getResources().getIdentifier("btn_Num" + c, "id", getPackageName());
            findViewById(id).setOnClickListener(v -> buttonAction("" + c));
        }
    }

    /** Set event listeners for direction buttons. */
    public void setEventListenersForDirectionButtons() {
        // Set event listener for left button.
        Button left = findViewById(R.id.sudokuControlLeft);
        left.setOnClickListener(v -> {
            // If exceeds boundary, revert
            if (--game.pointer[1] < 0) {
                game.pointer[1]++;
            }
            renderGrid();
        });
        // Set event listener for down button.
        Button down = findViewById(R.id.sudokuControlDown);
        down.setOnClickListener(v -> {
            // If exceeds boundary, revert
            if (++game.pointer[0] >= Grid.DIMENSION) {
                game.pointer[0]--;
            }
            renderGrid();
        });
        // Set event listener for up button.
        Button up = findViewById(R.id.sudokuControlUp);
        up.setOnClickListener(v -> {
            // If exceeds boundary, revert
            if (--game.pointer[0] < 0) {
                game.pointer[0]++;
            }
            renderGrid();
        });
        // Set event listener for right button.
        Button right = findViewById(R.id.sudokuControlRight);
        right.setOnClickListener(v -> {
            // If exceeds boundary, revert
            if (++game.pointer[1] >= Grid.DIMENSION) {
                game.pointer[1]--;
            }
            renderGrid();
        });
    }

    /**
     * This is what happens when a 0~F button is clicked.
     * @param option the value displayed on the button
     */
    public void buttonAction(String option) {
        // Fetch pointer and option values
        int row = game.pointer[0];
        int column = game.pointer[1];
        int number = Integer.parseInt(option, 16);
        // If this is an empty cell and it is the right number indeed…
        if ((game.puzzleGrid.cells[row][column].value == -1
                && number == game.answerGrid.cells[row][column].value)) {
            // Update puzzle grid
            Solver.confirmCell(game.puzzleGrid, game.puzzleGrid.cells[row][column], number);
            // Reflect the update on UI
            renderGrid();
            // If the user wins, open up a new dialogue which redirects back to main menu
            if (game.puzzleGrid.isSolved()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Congratulations! You have finished the puzzle!")
                        .setPositiveButton("OK", (DialogInterface dialog, int id) -> {
                            Intent mainMenu = new Intent(this, MainActivity.class);
                            startActivity(mainMenu);
                            finish();
                        });
                builder.create();
                builder.show();
            }
        }
        // Else… Do nothing for the moment being
        // TODO: Add some code if the player makes a mistake.
    }

    /** Renders the grid with player's current progress. */
    public void renderGrid() {
        // Fetch the grid UI
        TableLayout gridUI = findViewById(R.id.sudokuPaper);
        // Renders the grid with 4x4 blocks
        // i indicates a block's row
        for (int i = 0; i < Grid.BASE_INDEX; i++) {
            TableRow blockRow = (TableRow) gridUI.getChildAt(i);
            // j indicates a block's column
            for (int j = 0; j < Grid.BASE_INDEX; j++) {
                View block = blockRow.getChildAt(j);
                Grid.Cell[] blockValues = game.puzzleGrid.getBlock(new int[] {i, j});
                for (int k = 0; k < blockValues.length; k++) {
                    int rowInBlock = k / Grid.BASE_INDEX;
                    int columnInBlock = k % Grid.BASE_INDEX;
                    int rowInPuzzle = i * Grid.BASE_INDEX + rowInBlock;
                    int columnInPuzzle = j * Grid.BASE_INDEX + columnInBlock;
                    String idSuffix = "_" + rowInBlock + columnInBlock;
                    int id = getResources().getIdentifier("sudokuUnit" + idSuffix,
                            "id", getPackageName());
                    int value = blockValues[k].value;
                    TextView sudokuUnit = block.findViewById(id);
                    if (value == -1) {
                        sudokuUnit.setText(" ");
                    } else {
                        sudokuUnit.setText(Integer.toHexString(value).toUpperCase());
                    }
                    // Set event listener for each cell
                    int blockIndicator = i * Grid.BASE_INDEX + j;
                    sudokuUnit.setOnClickListener(v -> {
                        // Change the row value of the pointer
                        game.pointer[0] = blockIndicator / Grid.BASE_INDEX * Grid.BASE_INDEX
                                + rowInBlock;
                        // Change the column value of the pointer
                        game.pointer[1] = blockIndicator % Grid.BASE_INDEX * Grid.BASE_INDEX
                                + columnInBlock;

                        // add highlight to the cell
                        renderGrid();
                    });

                    // Add highlight to current pointer position
                    if (rowInPuzzle == game.pointer[0] && columnInPuzzle == game.pointer[1]) {
                        sudokuUnit.setBackgroundColor(getColor(R.color.colorPrimary__100));
                        sudokuUnit.setTextColor(getColor(R.color.colorBackground));
                    // Add highlight to cells containing the same numerical value
                    } else if (Integer.toHexString(game.puzzleGrid.cells[game.pointer[0]][game.pointer[1]].value)
                            .toUpperCase()
                            .equals(sudokuUnit.getText())) {
                        sudokuUnit.setTextColor(getColor(R.color.colorPrimary__100));
                        sudokuUnit.setBackground(getDrawable(R.drawable.border__sm__50__with_p_color__20));
                    // Add highlight to cells in the same row and column as the current cell
                    } else if (rowInPuzzle == game.pointer[0]
                            || columnInPuzzle == game.pointer[1]
                            || (game.pointer[0] / Grid.BASE_INDEX == i
                                && game.pointer[1] / Grid.BASE_INDEX == j)) {
                        sudokuUnit.setTextColor(getColor(R.color.colorPrimary__50));
                        sudokuUnit.setBackground(getDrawable(R.drawable.border__sm__50__with_p_color__20));
                    // Remove highlight for all cells that missed the conditions above
                    } else {
                        sudokuUnit.setTextColor(getColor(R.color.colorPrimary__50));
                        sudokuUnit.setBackground(getDrawable(R.drawable.border__sm__30));
                    }
                }
            }
        }
        for (char c : Constants.TOKENS) {
            // If all instances of one token has been filled on board
            if (game.puzzleGrid.getNumberOfInstancesOnBoard(c) == Grid.DIMENSION) {
                // Make it disappear
                int id = getResources().getIdentifier("btn_Num" + c, "id", getPackageName());
                ((Button) findViewById(id)).setText("");
            }
        }
    }
}
