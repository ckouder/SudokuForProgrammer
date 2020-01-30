package com.example.sudokuforprogrammer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.icu.text.SimpleDateFormat;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
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

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class NewGameActivity extends AppCompatActivity
        implements SudokuBlock.OnFragmentInteractionListener {

    /** The game object. */
    public Game game;

    // UI elements in new game activity
    private TextView lifeIndicator;

    private Button quitButton;
    private TextView timerText;
    private ImageButton timerControlButton;
    private ImageButton gameControlButton;

    private String[] directionBtnNames = new String[] { "left", "right", "up", "down" };
    private Button left;
    private Button right;
    private Button up;
    private Button down;

    private TableLayout gridUI;

    // Sound effect in new game effect
    private SoundPool soundPool;
    private int mouseClickedSound;
    private int keyPressedSound;
    private float volume = 0.5f;

    // Setup handler for timer
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

    // Setup handler for direction button
    private Handler directionButtonHandler = new Handler();
    private Runnable directionButtonRunnable;

    // Setup animation
    private AnimatedVectorDrawable animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialize a new game
        Intent intent = getIntent();
        String gameType = intent.getStringExtra("game");
        if (gameType == null || gameType.equals(getString(R.string.start_new_game))) {
            this.game = new Game(intent.getIntExtra("difficulty", Constants.DIFFICULTY_EASY));
        } else if (gameType.equals(getString(R.string.continue_game))) {
            this.game = (Game) intent.getExtras().get("gameContent");
        }

        // Set up sound effect
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        keyPressedSound = soundPool.load(this, R.raw.keyboard, 2);
        mouseClickedSound = soundPool.load(this, R.raw.mouse, 1);

        // Bind UI components to property
        lifeIndicator = findViewById(R.id.game_title_1);

        quitButton = findViewById(R.id.btn_gameQuit);
        timerText = findViewById(R.id.text_timer);
        timerControlButton = findViewById(R.id.btn_gameTimerControl);
        gameControlButton = findViewById(R.id.btn_gameControl);

        gridUI = findViewById(R.id.sudokuPaper);

        left = findViewById(R.id.sudokuControlLeft);
        down = findViewById(R.id.sudokuControlDown);
        up = findViewById(R.id.sudokuControlUp);
        right = findViewById(R.id.sudokuControlRight);

        // Setup event listeners
        setEventListenersForNumberButtons();
        setEventListenersForGameControlButtons();
        setDirectionButtonActions();

        // recover game states
        if (!game.timer.isRunning) {
            game.timer.start();
        }
        // Update UI
        renderLife();
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
            // This disables the default sound effect that comes with Android
            findViewById(id).setSoundEffectsEnabled(false);
            findViewById(id).setOnClickListener(v -> {
                soundPool.play(keyPressedSound, Constants.KEY_PRESSED_VOLUME,
                        Constants.KEY_PRESSED_VOLUME, 1, 0, 1.0f);
                buttonAction("" + c);
            });
        }
    }

    public void directionButtonActions(String btnCase) {
        if (!game.timer.isRunning) {
            return;
        }
        soundPool.play(keyPressedSound, Constants.KEY_PRESSED_VOLUME,
                Constants.KEY_PRESSED_VOLUME, 1, 0, 1.0f);
        switch (btnCase) {
            case "left":
                if (--game.pointer[1] < 0) {
                    game.pointer[1]++;
                }
                break;
            case "down":
                if (++game.pointer[0] >= Grid.DIMENSION) {
                    game.pointer[0]--;
                }
                break;
            case "up":
                if (--game.pointer[0] < 0) {
                    game.pointer[0]++;
                }
                break;
            case "right":
                if (++game.pointer[1] >= Grid.DIMENSION) {
                    game.pointer[1]--;
                }
                break;
        }
        renderGrid();
    }

    /** Set event listeners for direction buttons. */
    @SuppressLint("ClickableViewAccessibility")
    public void setDirectionButtonActions() {
        for (String directionBtnName : directionBtnNames) {
            try {
                // get field in class which defines direction button views
                Field directionBtnField = this.getClass().getDeclaredField(directionBtnName);
                // because the field is private, we need to manually set its accessibility
                directionBtnField.setAccessible(true);
                try {
                    // get value stored in the field
                    Button directionBtn = ((Button) directionBtnField.get(this));
                    // direction button should never be null
                    assert directionBtn != null;
                    directionBtn.setOnClickListener(v -> directionButtonActions(directionBtnName));
                    directionBtn.setOnTouchListener((v, event) -> {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                directionButtonRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        directionBtn.performClick();
                                        directionButtonHandler.postDelayed(
                                                directionButtonRunnable, 150);
                                    }
                                };
                                directionButtonHandler.postDelayed(
                                        directionButtonRunnable, 150);
                                break;
                            case MotionEvent.ACTION_UP:
                                directionButtonHandler.removeCallbacks(directionButtonRunnable);
                                // touch event will intercept the click event by default
                                // so we need to pass the event to click listener manually
                                directionBtn.performClick();
                                break;
                        }
                        return true;
                    });
                } catch (Exception e) { e.printStackTrace(); }
            } catch (NoSuchFieldException e1) { e1.printStackTrace(); }
        }
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
                finishDialog.setCancelable(false);
                finishDialog.setCanceledOnTouchOutside(false);
                // no better way to set its font family
                ((TextView) finishDialog.findViewById(android.R.id.message)).setTextAppearance(R.style.AppTheme);
            }
        } else if (game.puzzleGrid.cells[row][column].value == -1
                && number != game.answerGrid.cells[row][column].value) {
            game.reduceLife();
            renderLife();
            //reduceLifeAnimation();

            if (game.life == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.dialog);
                builder.setTitle("<Oh..No!/>")
                        .setMessage("Do you wanna try again?")
                        .setPositiveButton("Sure!", (DialogInterface dialog, int id) -> {
                            game.reset();
                            renderLife();
                            renderGrid();
                        })
                        .setNegativeButton("Nope..", (DialogInterface dialog, int id) -> {
                            Intent mainMenu = new Intent(this, MainActivity.class);
                            startActivity(mainMenu);
                            finish();
                        });
                AlertDialog failedDialog = builder.show();
                failedDialog.setCancelable(false);
                failedDialog.setCanceledOnTouchOutside(false);
                ((TextView) failedDialog.findViewById(android.R.id.message)).setTextAppearance(R.style.AppTheme);
            }
        }
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
     * Execute action on each Sudoku cell.
     * @param action action needed
     */
    public void forEachSudokuCell(
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
        forEachSudokuCell((block, blockCoordinate) -> (sudokuCell, cell) -> (cellCoordinate) -> {
            sudokuCell.setText(" ");
            sudokuCell.setBackgroundColor(getColor(R.color.colorPrimary__50));
        });
    }

    /** Renders the grid with player's current progress. */
    public void renderGrid() {
        forEachSudokuCell((block, blockCoordinate) -> (sudokuCell, cell) -> (cellCoordinate) -> {
            if (cell.value == -1) {
                sudokuCell.setText(" ");
            } else {
                sudokuCell.setText(Integer.toHexString(cell.value).toUpperCase());
            }
            // Set event listener for each cell
            int blockIndicator = blockCoordinate[0] * Grid.BASE_INDEX + blockCoordinate[1];
            sudokuCell.setOnClickListener(v -> {
                // Play the sound whenever a cell is selected
                soundPool.play(mouseClickedSound, Constants.MOUSE_CLICKED_VOLUME,
                        Constants.MOUSE_CLICKED_VOLUME, 1, 0, 1.0f);
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
                sudokuCell.setBackgroundColor(getColor(R.color.colorPrimary__100));
                sudokuCell.setTextColor(getColor(R.color.colorBackground));
                // Add highlight to cells containing the same numerical value
            } else if (Integer.toHexString(game.puzzleGrid.cells[game.pointer[0]][game.pointer[1]].value)
                    .toUpperCase()
                    .equals(sudokuCell.getText())) {
                sudokuCell.setTextColor(getColor(R.color.colorPrimary__100));
                sudokuCell.setBackground(getDrawable(R.drawable.border__sm__50__with_p_color__20));
                // Add highlight to cells in the same row and column as the current cell
            } else if (rowInPuzzle == game.pointer[0]
                    || columnInPuzzle == game.pointer[1]
                    || (game.pointer[0] / Grid.BASE_INDEX == blockCoordinate[0]
                    && game.pointer[1] / Grid.BASE_INDEX == blockCoordinate[1])) {
                sudokuCell.setTextColor(getColor(R.color.colorPrimary__50));
                sudokuCell.setBackground(getDrawable(R.drawable.border__sm__50__with_p_color__20));
                // Remove highlight for all cells that missed the conditions above
            } else {
                sudokuCell.setTextColor(getColor(R.color.colorPrimary__50));
                sudokuCell.setBackground(getDrawable(R.drawable.border__sm__30));
            }
        });

        for (char c : Constants.TOKENS) {
            // If all instances of one token has been filled on board
            if (game.puzzleGrid.getNumberOfInstancesOnBoard(c) == Grid.DIMENSION) {
                // Make it disappear
                int id = getResources().getIdentifier("btn_Num" + c, "id", getPackageName());
                ((Button) findViewById(id)).setText("");
                // Let it do nothing
                findViewById(id).setOnClickListener(v -> { });
            }
        }
    }

    public void renderLife() {
        String lifeString = lifeIndicator.getText().toString();
        SpannableString life = new SpannableString(lifeString);
        life.setSpan(new ForegroundColorSpan(getColor(R.color.colorPrimary__20)),
                game.life,
                Constants.LIFE_NUM,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        lifeIndicator.setText(life, TextView.BufferType.SPANNABLE);
    }

//    public void reduceLifeAnimation() {
//        View lifeReducer = findViewById(R.id.lifeReducer);
//        lifeReducer.setVisibility(View.VISIBLE);;
//        animation = (AnimatedVectorDrawable) lifeReducer.getBackground();
//        animation.start();
//    }

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
