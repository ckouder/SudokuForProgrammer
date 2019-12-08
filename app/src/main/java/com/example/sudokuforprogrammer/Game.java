package com.example.sudokuforprogrammer;

import java.io.Serializable;
import java.util.Random;
import com.example.sudokuforprogrammer.sudokuMatrixGenerator.SudokuMatrix;

/**
 * The structure of a game is as follows:
 * 1. Difficulty: Will be set to easy, because it's already pretty hard.
 * 2. Answer Grid: The answer to the puzzle, will not change after being initialized.
 * 3. Puzzle Grid: The puzzle that is presented to the user at the beginning of the game.
 * 4. Pointer: Indicates which cell is currently selected.
 */
public class Game implements Serializable {

    /** The difficulty of the game. */
    public final int DIFFICULTY = Constants.DIFFICULTY_EASY;

    /** This is the answer to the puzzle. */
    public Grid answerGrid;

    /** The initial state of the game, will change as the user plays. */
    public Grid puzzleGrid;

    /** pointer that stored [int row, int col] pair. */
    public int[] pointer = new int[2];

    /**
     * A class to represent timer.
     * Functions that a timer should have:
     * 1. Start/Continue
     * 2. Stop/End
     * 3. Reset
     */
    public class Timer {

        /** Amount of time in ms recorded by Timer. */
        public long time;

        /** Start time of the timer. */
        public long start;

        /** End time of the timer. */
        public long end;

        /** Whether the timer is running. */
        public boolean isRunning;

        /** Start a timer. */
        public void start() {
            this.start = System.currentTimeMillis();
            isRunning = true;
        }

        /** Pause a timer. */
        public void pause() {
            this.end = System.currentTimeMillis();
            this.time += end - start;
            isRunning = false;
        }

        /** Reset a timer. */
        public void reset() {
            this.time = 0;
        }

        /**
         * Fetch timer's time.
         * @return time recorded
         */
        public long getTime() {
            return isRunning ? time + (System.currentTimeMillis() - start) : time;
        }

    }

    /** Timer for the game. */
    public Timer timer;

    /** Constructing a game object. */
    public Game() {
        try {
            // Store the generated Sudoku grid as a 2-D array of char
            char[][] charGrid = new SudokuMatrix(4, Constants.TOKENS).getPaper();
            // Turn that 2-D char array into a new grid
            this.answerGrid = new Grid(charGridToIntGrid(charGrid));
            // Initialize the puzzle grid
            this.puzzleGrid = this.answerGrid.clone();
            while (this.puzzleGrid.getNumberOfEmptyCells() < this.DIFFICULTY) {
                takeARandomCellOut();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.timer = new Timer();
        this.timer.start();
    }

    /**
     * Auxiliary function that convert a 2-D char array into a 2-D int array.
     * @param charGrid 2-D char array representing the grid
     * @return 2-D int array representing the grid
     */
    private int[][] charGridToIntGrid(char[][] charGrid) {
        int[][] intGrid = new int[16][16];
        for (int i = 0; i < charGrid.length; i++) {
            for (int j = 0; j < charGrid[i].length; j++) {
                intGrid[i][j] = Integer.parseInt(charGrid[i][j] + "", 16);
            }
        }
        return intGrid;
    }

    /**
     * Auxiliary function that generates a puzzle grid from an answer grid by taking random cells
     * out one at a time.
     */
    private void takeARandomCellOut() {
        Random random = new Random();
        // Keep removing blocks
        while (true) {
            int row = random.nextInt(Grid.DIMENSION);
            int column = random.nextInt(Grid.DIMENSION);
            // If this is not an empty cell
            if (this.puzzleGrid.cells[row][column].value != -1) {
                // Store the value in case things don't work out well.
                int savedValue = this.puzzleGrid.cells[row][column].value;
                // Remove it, copy the change to puzzleGrid, and flip the flag
                this.puzzleGrid.cells[row][column].value = -1;
                this.puzzleGrid.cells[row][column].confirmed = false;
                if (Solver.solve(puzzleGrid, System.currentTimeMillis())) {
                    // If it can be solved, done
                    return;
                } else {
                    // Else revert back
                    this.puzzleGrid.cells[row][column].value = savedValue;
                    this.puzzleGrid.cells[row][column].confirmed = true;
                }
            }
        }
    }

    /**
     * Main function to test the game object internally.
     * @param args useless
     */
    public static void main(String[] args) {
        Game newGame = new Game();
        newGame.puzzleGrid.printGrid();
        System.out.println("Final Puzzle!");
        Solver.solve(newGame.puzzleGrid, System.currentTimeMillis());
    }

}
