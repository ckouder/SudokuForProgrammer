package com.example.sudokuforprogrammer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.fonts.FontFamily;
import android.icu.text.SimpleDateFormat;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.pig.impl.util.ObjectSerializer;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Array;
import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class NewGameActivity extends AppCompatActivity
        implements SudokuBlock.OnFragmentInteractionListener {

    /** The game object. */
    public Game game;

    // UI elements in new game activity
    private Button quitButton;
    private TextView timerText;
    private ImageButton timerControlButton;
    private ImageButton gameControlButton;

    private Button left;
    private Button right;
    private Button up;
    private Button down;

    private TableLayout gridUI;

    // Sound effect in new game effect
    private SoundPool soundPool;
    private int clickSound;
    private int fillSound;
    private float volume = 0.5f;

    private Handler timerHandler = new Handler();
    private Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            int timeInSeconds = (int) game.timer.getTime() / 1000;
            int minutes = timeInSeconds / 60;
            int seconds = timeInSeconds % 60;
            timerText.setText(String.format(Locale.US,"%02d:%02d", minutes, seconds));

            timerHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialize a new game
        Intent intent = getIntent();
        String gameType = intent.getStringExtra("game");
        if (gameType == null || gameType.equals(getString(R.string.start_new_game))) {
            this.game = new Game();
        } else if (gameType.equals(getString(R.string.continue_game))) {
            this.game = (Game) intent.getExtras().get("gameContent");
        }

        // Set up sound effect
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        fillSound = soundPool.load(this, R.raw.keyboard, 2);
        clickSound = soundPool.load(this, R.raw.mouse, 1);

        quitButton = findViewById(R.id.btn_gameQuit);
        timerText = findViewById(R.id.text_timer);
        timerControlButton = findViewById(R.id.btn_gameTimerControl);
        gameControlButton = findViewById(R.id.btn_gameControl);

        gridUI = findViewById(R.id.sudokuPaper);

        left = findViewById(R.id.sudokuControlLeft);
        down = findViewById(R.id.sudokuControlDown);
        up = findViewById(R.id.sudokuControlUp);
        right = findViewById(R.id.sudokuControlRight);

        setEventListenersForNumberButtons();
        setEventListenersForGameControlButtons();
        setEventListenersForDirectionButtons();

        if (!game.timer.isRunning) {
            game.timer.start();
        }
        // Update UI
        renderGrid();
    }

    public void onFragmentInteraction(Uri uri) { }

    /** Set event listeners for game control buttons. */
    public void setEventListenersForGameControlButtons() {
        // set listener for timer control
        final String TIMER_IS_SHOWN = getResources().getString(R.string.game_timer_state_shown);
        final String TIMER_IS_HIDED = getResources().getString(R.string.game_timer_state_hided);
        timerControlButton.setOnClickListener(v -> {

            if (timerControlButton.getTag().equals(TIMER_IS_SHOWN)) {
                timerText.setVisibility(View.INVISIBLE);
                timerControlButton.setImageDrawable(getDrawable(R.drawable.ic_timer_hide));
                timerControlButton.setTag(TIMER_IS_HIDED);
                timerHandler.removeCallbacks(timeRunnable);

            } else if (timerControlButton.getTag().equals(TIMER_IS_HIDED)) {
                timerText.setVisibility(View.VISIBLE);
                timerControlButton.setImageDrawable(getDrawable(R.drawable.ic_timer_show));
                timerControlButton.setTag(TIMER_IS_SHOWN);
                timerHandler.postDelayed(timeRunnable, 0);
            }
        });

        // set listener for quit Button
        quitButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.dialog);
            builder.setTitle("<Are you sure?/>")
                    .setMessage("We'll save your game for your return.")
                    .setNegativeButton("Let me think", (DialogInterface dialog, int id) -> { })
                    .setPositiveButton("I'm determined", (DialogInterface dialog, int id) -> {
                        Intent mainMenu = new Intent(this, MainActivity.class);
                        save(game);
                        startActivity(mainMenu);
                        finish();
                    });
            builder.create();
            AlertDialog quitDialog = builder.show();
            // no better way to set its font family
            ((TextView) quitDialog.findViewById(android.R.id.message)).setTextAppearance(R.style.AppTheme);
        });

        // set listener for game control button
        gameControlButton.setOnClickListener(v -> {
            if (game.timer.isRunning) {
                game.timer.pause();
                gameControlButton.setImageDrawable(getDrawable(R.drawable.ic_resume));
                eraseGrid();
            } else {
                game.timer.start();
                gameControlButton.setImageDrawable(getDrawable(R.drawable.ic_pause));
                renderGrid();
            }
        });
    }

    /** Set event listeners for each token used in game. */
    public void setEventListenersForNumberButtons() {
        for (char c : Constants.TOKENS) {
            if (!game.timer.isRunning) {
                return;
            }
            int id = getResources().getIdentifier("btn_Num" + c, "id", getPackageName());
            findViewById(id).setOnClickListener(v -> {
                soundPool.play(fillSound, volume, volume, 1, 0, 1.0f);
                buttonAction("" + c);
            });
        }
    }

    /** Set event listeners for direction buttons. */
    public void setEventListenersForDirectionButtons() {
        // Set event listener for left button.
        // TODO: set long click listener
        left.setOnClickListener(v -> {
            if (!game.timer.isRunning) {
                return;
            }
            soundPool.play(clickSound, volume, volume, 1, 0, 1.0f);
            // If exceeds boundary, revert
            if (--game.pointer[1] < 0) {
                game.pointer[1]++;
            }
            renderGrid();
        });
        // Set event listener for down button.
        // TODO: set long click listener
        down.setOnClickListener(v -> {
            if (!game.timer.isRunning) {
                return;
            }
            soundPool.play(clickSound, volume, volume, 1, 0, 1.0f);
            // If exceeds boundary, revert
            if (++game.pointer[0] >= Grid.DIMENSION) {
                game.pointer[0]--;
            }
            renderGrid();
        });
        // Set event listener for up button.
        // TODO: set long click listener
        up.setOnClickListener(v -> {
            if (!game.timer.isRunning) {
                return;
            }
            soundPool.play(clickSound, volume, volume, 1, 0, 1.0f);
            // If exceeds boundary, revert
            if (--game.pointer[0] < 0) {
                game.pointer[0]++;
            }
            renderGrid();
        });
        // Set event listener for right button.
        // TODO: set long click listener
        right.setOnClickListener(v -> {
            if (!game.timer.isRunning) {
                return;
            }
            soundPool.play(clickSound, volume, volume, 1, 0, 1.0f);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.dialog);
                builder.setTitle("<Congrats!/>")
                        .setMessage("You have finished the puzzle!")
                        .setPositiveButton("OK", (DialogInterface dialog, int id) -> {
                            Intent mainMenu = new Intent(this, MainActivity.class);
                            startActivity(mainMenu);
                            finish();
                        });
                timerHandler.removeCallbacks(timeRunnable);
                SharedPreferences savedGame = getSharedPreferences(getString(
                        R.string.app_saved_game), MODE_PRIVATE);
                SharedPreferences highestScore = getSharedPreferences(getString(
                        R.string.app_highest_score), MODE_PRIVATE);
                SharedPreferences.Editor savedGameEditor = savedGame.edit();
                SharedPreferences.Editor highestScoreEditor = highestScore.edit();
                try {
                    // erase game storage if stored game is finished
                    Game gameObject = (Game) ObjectSerializer.deserialize(savedGame.getString(
                            getString(R.string.app_saved_game), null));

                    if (gameObject != null && gameObject.getId() == game.getId()){
                        savedGameEditor.putString(getString(R.string.app_saved_game), ObjectSerializer.serialize(null));
                        savedGameEditor.apply();
                    }

                    // calculate highest score
                    long previousHighestScore = highestScore.getLong(getString(R.string.app_highest_score), 0);
                    if (previousHighestScore == 0 || previousHighestScore > game.timer.getTime()) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();

                        highestScoreEditor.putLong(getString(R.string.app_highest_score), game.timer.getTime());
                        highestScoreEditor.putString(getString(R.string.highest_score_date), dateFormat.format(date));
                        highestScoreEditor.apply();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                builder.create();
                AlertDialog finishDialog = builder.show();
                // no better way to set its font family
                ((TextView) finishDialog.findViewById(android.R.id.message)).setTextAppearance(R.style.AppTheme);
            }
        }
        // Else… Do nothing for the moment being
        // TODO: Add some code if the player makes a mistake.
    }

    /**
     * map sudoku blocks with given function
     * @param action action taken on each sudoku block,
     *               View = view of block
     *               int[] = coordinates of block
     */
    public void forEachSudokuBlock(BiConsumer<View, int[]> action) {
        for (int blockRow = 0; blockRow < Grid.BASE_INDEX; blockRow++) {
            TableRow row = (TableRow) gridUI.getChildAt(blockRow);
            for (int blockCol = 0; blockCol < Grid.BASE_INDEX; blockCol++) {
                View block = row.getChildAt(blockCol);
                action.accept(block, new int[] { blockRow, blockCol });
            }
        }
    }

    /**
     * Execute action on each sudoku unit.
     * @param action action needed
     */
    public void forEachSudokuUnit(
            BiFunction<View,                /* block view */
                    int[],                  /* block coordinate */
                    BiFunction<
                            TextView,       /* cell view */
                            Grid.Cell,      /* cell value */
                            Consumer<int[]  /* cell coordinate */>>> action) {

        forEachSudokuBlock((block, blockCoordinate) -> {
            Grid.Cell[] cells = game.puzzleGrid.getBlock(blockCoordinate);
            for (int i = 0; i < cells.length; i++) {
                int row = i / 4;
                int column = i % 4;
                int id = getResources().getIdentifier(
                        String.format(Locale.US, "sudokuUnit_%d%d", row, column),
                        "id", getPackageName());
                TextView sudokuUnit = block.findViewById(id);

                action.apply(block, blockCoordinate)
                      .apply(sudokuUnit, cells[i])
                      .accept(new int[] { row, column });
            }
        });
    }

    /**
     * Erase grid content when the game is paused.
     */
    public void eraseGrid() {
        forEachSudokuUnit((block, blockCoordinate) -> (sudokuUnit, cell) -> (cellCoordinate) -> {
            sudokuUnit.setText(" ");
            sudokuUnit.setBackgroundColor(getColor(R.color.colorPrimary__50));
        });
    }

    /** Renders the grid with player's current progress. */
    public void renderGrid() {
        forEachSudokuUnit((block, blockCoordinate) -> (sudokuUnit, cell) -> (cellCoordinate) -> {
            if (cell.value == -1) {
                sudokuUnit.setText(" ");
            } else {
                sudokuUnit.setText(Integer.toHexString(cell.value).toUpperCase());
            }
            // Set event listener for each cell
            int blockIndicator = blockCoordinate[0] * Grid.BASE_INDEX + blockCoordinate[1];
            sudokuUnit.setOnClickListener(v -> {
                if (!game.timer.isRunning) {
                    return;
                }
                // Change the row value of the pointer
                game.pointer[0] = blockIndicator / Grid.BASE_INDEX * Grid.BASE_INDEX
                        + cellCoordinate[0];
                // Change the column value of the pointer
                game.pointer[1] = blockIndicator % Grid.BASE_INDEX * Grid.BASE_INDEX
                        + cellCoordinate[1];

                // add highlight to the cell
                renderGrid();
            });

            // Add highlight to current pointer position
            int rowInPuzzle = blockCoordinate[0] * Grid.BASE_INDEX + cellCoordinate[0];
            int columnInPuzzle = blockCoordinate[1] * Grid.BASE_INDEX + cellCoordinate[1];
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
                    || (game.pointer[0] / Grid.BASE_INDEX == blockCoordinate[0]
                    && game.pointer[1] / Grid.BASE_INDEX == blockCoordinate[1])) {
                sudokuUnit.setTextColor(getColor(R.color.colorPrimary__50));
                sudokuUnit.setBackground(getDrawable(R.drawable.border__sm__50__with_p_color__20));
                // Remove highlight for all cells that missed the conditions above
            } else {
                sudokuUnit.setTextColor(getColor(R.color.colorPrimary__50));
                sudokuUnit.setBackground(getDrawable(R.drawable.border__sm__30));
            }
        });

        for (char c : Constants.TOKENS) {
            // If all instances of one token has been filled on board
            if (game.puzzleGrid.getNumberOfInstancesOnBoard(c) == Grid.DIMENSION) {
                // Make it disappear
                int id = getResources().getIdentifier("btn_Num" + c, "id", getPackageName());
                ((Button) findViewById(id)).setText("");
                // Let it do nothing
                ((Button) findViewById(id)).setOnClickListener(v -> { });
            }
        }
    }

    /**
     * <E> Save game to sharedPreference
     */
    public void save(Serializable something) {
        SharedPreferences store = getSharedPreferences(getString(R.string.app_saved_game), MODE_PRIVATE);
        SharedPreferences.Editor storeEditor = store.edit();

        try {
            storeEditor.putString(getString(R.string.app_saved_game), ObjectSerializer.serialize(something));
            storeEditor.apply();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "<error>Save game failed.</error>", Toast.LENGTH_SHORT).show();
        }
    }
}
