package com.example.sudokuforprogrammer;

import java.util.ArrayList;

/** A class to represent the Sudoku grid. */
public class Grid implements Cloneable {

    /** A class to encapsulate one Sudoku cell. */
    public class Cell {

        /** Number value of the cell. */
        public int value;

        /** Whether the cell has been confirmed. Cells came with the problem are confirmed. */
        public boolean confirmed;

        /** The possibilities that an empty cell possesses. */
        public ArrayList<Integer> possibilities;

        /** Marks the row of the cell, starting with 0. */
        public int row;

        /** Marks the column of the cell, starting with 0. */
        public int column;

        /** Marks the block of the cell, starting with 0, left to right, top to bottom. */
        public int block;

        /**
         * Instantiate a cell with the given properties.
         * @param value value of the cell
         * @param row the cell's row
         * @param column the cell's column
         */
        public Cell(int value, int row, int column) {
            this.value = value;
            this.confirmed = value != -1;
            this.row = row;
            this.column = column;
            this.block = row / BASE_INDEX * BASE_INDEX + column / BASE_INDEX;
        }

    }

    /** Cells in a Sudoku grid. */
    // This configuration is for 16x16 Sudoku specifically.
    public Cell[][] cells = new Cell[16][16];

    /** Base index of the game. */
    // This configuration is for 16x16 Sudoku specifically.
    public final int BASE_INDEX = 4;

    /** Dimension of the game. */
    public final int DIMENSION = BASE_INDEX * BASE_INDEX;

    /**
     * A constructor for cells with input array.
     * @param array input array representing the Sudoku grid
     */
    public Grid(int[][] array) {
        for (int i = 0; i < this.DIMENSION; i++) {
            for (int j = 0; j < this.DIMENSION; j++) {
                cells[i][j] = new Cell(array[i][j], i, j);
            }
        }
    }

    /**
     * Create a grid from the given String array
     * @param rawArray the raw String array
     */
    public static Grid importGridFromStringArray(String[] rawArray) {
        // Specifically set for 16x16 Sudoku
        // This will be used as a parameter to another constructor
        int[][] array = new int[16][16];
        for (int i = 0; i < rawArray.length; i++) {
            int[] processed = new int[16];
            // A single row in a puzzle
            String[] raw = rawArray[i].split(",");
            for (int j = 0; j < raw.length; j++) {
                // "." can be other things as well, such as "*" or even "$"
                if (raw[j].trim().equals(".")) {
                    processed[j] = -1;
                } else {
                    processed[j] = Integer.parseInt(raw[j].trim(), 16);
                }
            }
            array[i] = processed;
        }
        return new Grid(array);
    }

    /** A method to view the grid. */
    public void printGrid() {
        for (int i = 0; i < this.DIMENSION; i++) {
            System.out.print("[");
            for (int j = 0; j < this.DIMENSION; j++) {
                if (cells[i][j].value == -1) {
                    System.out.print(".");
                } else {
                    System.out.print(Integer.toHexString(cells[i][j].value).toUpperCase());
                }
                System.out.print(", ");
            }
            System.out.println("\b\b]");
        }
    }

    /**
     * Check whether the puzzle is solved (means no cell is empty).
     * @return whether the puzzle is solved or not
     */
    public boolean isSolved() {
        return this.getNumberOfEmptyCells() == 0;
    }

    /**
     * Returns how many unsolved cells this puzzle grid has.
     * @return the number of empty cells
     */
    public int getNumberOfEmptyCells() {
        int count = 0;
        for (int i = 0; i < this.DIMENSION; i++) {
            for (int j = 0; j < this.DIMENSION; j++) {
                if (this.cells[i][j].value == -1) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * get a line in grid.
     *
     * @param pos position of the line
     * @param vertical if the line is vertical
     * @return cells in the grid
     */
    public Cell[] getLine(int pos, boolean vertical) {
        Cell[] line = new Cell[DIMENSION];

        if (vertical) {
            for (int i = 0; i < DIMENSION; i++) {
                line[i] = cells[i][pos];
            }

        } else {
            line = cells[pos];
        }

        return line;
    }

    /**
     * get a block in the grid.
     *
     * @param coordinate [int row, int col] pair
     * @return cells in a block
     */
    public Cell[] getBlock(int[] coordinate) {
        final Cell[] block = new Cell[DIMENSION];

        int j = 0;
        for (int row = coordinate[0] * BASE_INDEX; row < (coordinate[0] + 1) * BASE_INDEX; row++) {

            for (int col = coordinate[1] * BASE_INDEX; col < (coordinate[1] + 1) * BASE_INDEX; col++) {

                block[j] = cells[row][col];
                j++;
            }

        }

        return block;
    }

    /**
     * set a block.
     *
     * @param values values you want to add to grid.
     * @param coordinate coordinate of the cell
     */
    public void setBlock(Cell[] values, int[] coordinate) {
        int j = 0;

        for (int i = coordinate[0] * BASE_INDEX; i < (coordinate[0] + 1) * BASE_INDEX; i++) {

            for (int k = coordinate[1] * BASE_INDEX; k < (coordinate[1] + 1) * BASE_INDEX; k++) {

                cells[i][k] = values[j];
                j++;
            }
        }
    }

    /**
     * Make a deep copy of the current grid. The idea is to initialize another grid with an
     * int 2-dimensional array.
     * @return a copy of the grid
     */
    @Override
    protected Grid clone() {
        int[][] array = new int[16][16];
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                array[i][j] = this.cells[i][j].value;
            }
        }
        return new Grid(array);
    }

}
