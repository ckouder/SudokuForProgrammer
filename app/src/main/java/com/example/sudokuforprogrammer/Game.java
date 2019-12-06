package com.example.sudokuforprogrammer;

import java.util.Random;
import com.example.sudokuforprogrammer.sudokuMatrixGenerator.SudokuMatrix;

/**
 * The structure of a game is as follows:
 * 1. Difficulty: Will be set to easy, because it's already pretty hard.
 * 2. Answer Grid: The answer to the puzzle, will not change after being initialized.
 * 3. Temporary Grid: The grid that the user is currently working on.
 * 4. Puzzle Grid: The puzzle that is presented to the user at the beginning of the game.
 */
public class Game {

    /** The difficulty of the game. */
    public final int DIFFICULTY = Constants.DIFFICULTY_EASY;

    /** This is the answer to the puzzle. */
    public Grid answerGrid;

    /** This grid starts being the same as puzzle grid, but will change as the player plays. */
    public Grid temporaryGrid;

    /** The initial state of the game, will not change. */
    public Grid puzzleGrid;

    /** Constructing a game object. */
    public Game() {
        // Token used for grid generation
        char[] token = {
                '0', '1', '2', '3',
                '4', '5', '6', '7',
                '8', '9', 'A', 'B',
                'C', 'D', 'E', 'F'
        };
        try {
            // Store the generated Sudoku grid as a 2-D array of char
            char[][] charGrid = new SudokuMatrix(4, token).getPaper();
            // Turn that 2-D char array into a new grid
            this.answerGrid = new Grid(charGridToIntGrid(charGrid));
            // Initialize the puzzle grid
            this.puzzleGrid = this.answerGrid.clone();
            this.temporaryGrid = this.answerGrid.clone();
            while (this.puzzleGrid.getNumberOfEmptyCells() < this.DIFFICULTY) {
                takeARandomCellOut();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        // Flag for whether the removal action is done.
        boolean done = false;
        while (!done) {
            int row = random.nextInt(16);
            int column = random.nextInt(16);
            // If this is not an empty cell
            if (this.temporaryGrid.cells[row][column].value != -1) {
                // Store the value in case things don't work out well.
                int savedValue = this.temporaryGrid.cells[row][column].value;
                // Remove it, copy the change to puzzleGrid, and flip the flag
                this.temporaryGrid.cells[row][column].value = -1;
                this.temporaryGrid.cells[row][column].confirmed = false;
                this.puzzleGrid.cells[row][column].value = -1;
                this.puzzleGrid.cells[row][column].confirmed = false;
                if (Solver.solve(puzzleGrid, System.currentTimeMillis())) {
                    // If it can be solved, done
                    done = true;
                } else {
                    // Else revert back
                    this.temporaryGrid.cells[row][column].value = savedValue;
                    this.temporaryGrid.cells[row][column].confirmed = true;
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
