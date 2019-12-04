package com.example.sudokuforprogrammer;

import java.util.Random;

public class Game {

    /** The difficulty of the game. */
    public final int DIFFICULTY = Constants.DIFFICULTY_EASY;

    // @CHANGE
    /** This is the answer to the puzzle. */
    public Grid answerGrid = Grid.importGridFromStringArray(new String[] {
            "A, C, 3, E, F, 2, 0, 1, B, 7, 9, 6, 4, 5, D, 8",
            "F, D, 2, 1, 4, B, C, 5, A, 3, 8, 0, 6, E, 9, 7",
            "7, 6, 9, B, 3, E, 8, D, 2, 1, 5, 4, A, 0, C, F",
            "4, 5, 8, 0, A, 7, 9, 6, F, D, E, C, 2, 1, 3, B",
            "B, 2, D, 9, C, 6, 5, 7, 1, E, 0, F, 8, A, 4, 3",
            "1, 7, F, 6, 9, 3, D, 4, 5, A, C, 8, B, 2, 0, E",
            "3, 4, E, A, 8, 0, 1, 2, 6, 9, B, 7, F, D, 5, C",
            "0, 8, C, 5, B, A, E, F, D, 2, 4, 3, 9, 7, 1, 6",
            "C, B, 5, D, E, 4, 6, A, 8, 0, 2, 9, 3, F, 7, 1",
            "9, A, 6, 4, 5, D, B, 0, 3, F, 7, 1, C, 8, E, 2",
            "E, F, 1, 8, 7, 9, 2, 3, C, 4, D, A, 0, 6, B, 5",
            "2, 3, 0, 7, 1, C, F, 8, E, B, 6, 5, D, 9, A, 4",
            "8, E, 4, F, 2, 1, 3, C, 0, 5, A, D, 7, B, 6, 9",
            "D, 0, B, 3, 6, F, 7, E, 9, C, 1, 2, 5, 4, 8, A",
            "5, 9, 7, C, 0, 8, A, B, 4, 6, F, E, 1, 3, 2, D"
    });

    /**
     * This is a temporary grid to obtain the puzzle grid.
     * This will change along as the user plays.
     */
    public Grid temporaryGrid;

    /**
     * This is the puzzleGrid that we are trying to create.
     * It will then be sent to render, and this will stay the same after the
     * game is created.
     */
    public Grid puzzleGrid;

    /**
     * Originally all the three grids are completed puzzles, initialize them to
     * get puzzle grids.
     */
    public void initializePuzzleGrid() {
        this.puzzleGrid = answerGrid.clone();
        this.temporaryGrid = answerGrid.clone();
        while (this.puzzleGrid.getNumberOfEmptyCells() < this.DIFFICULTY) {
            takeARandomCellOut();
        }
    }

    /** Auxiliary function. */
    private void takeARandomCellOut() {
        Random random = new Random();
        // Flag for whether the removal action is done.
        boolean done = false;
        while (!done) {
            int row = random.nextInt(16);
            int column = random.nextInt(16);
            // If this is not an already removed cell
            if (this.temporaryGrid.cells[row][column].value != -1) {
                // Remove it, copy the change to puzzleGrid, and flip the flag
                this.temporaryGrid.cells[row][column].value = -1;
                this.puzzleGrid.cells[row][column].value = -1;
                done = true;
            }
        }
    }

    public static void main(String[] args) {
        // @CHANGE
        // TODO: Write a constructor for Game.
        Game newGame = new Game();
        newGame.initializePuzzleGrid();
        newGame.puzzleGrid.printGrid();
    }

}
