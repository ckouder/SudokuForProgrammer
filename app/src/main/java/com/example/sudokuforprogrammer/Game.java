package com.example.sudokuforprogrammer;

import java.util.Random;
import com.example.sudokuforprogrammer.sudokuMatrixGenerator.SudokuMatrix;

// A game is consisted of three grids, namely the answer grid, the user's grid, and temporary grid.
public class Game {

    /** The difficulty of the game. */
    public final int DIFFICULTY = Constants.DIFFICULTY_EASY;

    /** This is the answer to the puzzle. */
    public Grid answerGrid;

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

    /** Initialize an answer grid from the sudoku grid generator. */
    public void initializeAnswerGrid() {
        try {
            // Token used for grid generation
            char[] token = {
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
            };
            // Store the generated Sudoku grid as a 2-D array of char
            char[][] charGrid = new SudokuMatrix(4, token).getPaper();
            // Turn that 2-D char array
            this.answerGrid = new Grid(charGridToIntGrid(charGrid));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int[][] charGridToIntGrid(char[][] charGrid) {
        int[][] intGrid = new int[16][16];
        for (int i = 0; i < charGrid.length; i++) {
            for (int j = 0; j < charGrid[i].length; j++) {
                intGrid[i][j] = Integer.parseInt(charGrid[i][j] + "", 16);
            }
        }
        return intGrid;
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

    public static void main(String[] args) {
        // @CHANGE
        // TODO: Write a constructor for Game.
        Game newGame = new Game();
        newGame.initializeAnswerGrid();
        newGame.initializePuzzleGrid();
        newGame.puzzleGrid.printGrid();
        System.out.println("Final Puzzle!");
        Solver.solve(newGame.puzzleGrid, System.currentTimeMillis());
    }

}
