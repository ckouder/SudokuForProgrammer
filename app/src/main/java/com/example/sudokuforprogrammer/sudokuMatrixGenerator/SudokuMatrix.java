package com.example.sudokuforprogrammer.sudokuMatrixGenerator;

import java.util.ArrayList;
import java.util.Random;

/**
 * generate a sudoku matrix
 */
public class SudokuMatrix {

    /**
     * throw no available slot exception when cannot visit previous. or next slot
     */
    private class NoAvailableSlotException extends Exception {

        /**
         * No available slot exception
         * 
         * @param string message
         */
        NoAvailableSlotException(String string) {
            super(string);
        }
    }

    /**
     * Initialization limit for the matrix, will reinitialize if surpass the limit.
     */
    private static final int INITIALIZATION_LIMIT = 1000000;

    /** ASCII code start point for tokens, default 'A'. */
    private static final int TOKEN_START_POINT = 65;

    /** Unit block size of the matrix. */
    private final int blockSize;

    /** Size of the matrix, equals to the square of blockSize. */
    private final int size;

    /** Tokens of the matrix. */
    private final char[] tokens;

    /** Paper to be draw. */
    private char[][] paper;

    /** Internal counter to count the limit. */
    private int counter;

    /** Chain to store coordinate and available tokens. */
    private Chain<ArrayList<Character>> posTokensChain = new Chain<>();

    /**
     * Initialize a SudokuMatrix.
     * 
     * @param setBlockSize unit block size of the matrix
     * @throws Exception if block size is too big
     */
    SudokuMatrix(final int setBlockSize) throws Exception {
        if (setBlockSize > 16) {
            throw new Exception("block size [" + setBlockSize + "] is too large");
        }

        blockSize = setBlockSize;
        size = (int) Math.pow(blockSize, 2);
        tokens = new char[size];

        // fill up tokens list
        for (int i = 0; i < size; i++) {
            final char token = (char) (i + TOKEN_START_POINT);
            tokens[i] = token;
        }

        initialize();
    }

    /**
     * Initialise a SudokuMatrix with given tokens.
     *
     * @param setBlockSize unit block size of the matrix
     * @param tokenList list of tokens used as sudoku tokens
     * @throws Exception if block size is too big
     */
    SudokuMatrix(final int setBlockSize, final char[] tokenList) throws Exception {
        if (setBlockSize > 16) {
            throw new Exception("block size [" + setBlockSize + "] is too large");
        }
        blockSize = setBlockSize;

        size = (int) Math.pow(blockSize, 2);

        if (tokenList.length != size) {
            throw new Exception("token list length doesn't match sudoku size");
        }
        tokens = tokenList;

        initialize();
    }

    /**
     * @return the paper
     */
    char[][] getPaper() {
        return paper;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                s.append(paper[i][j]);
                s.append(" ");
            }
            s.append("\n");
        }

        return s.toString();
    }

    /**
     * initialize a sudoku matrix with given tokens and fill it up.
     */
    void initialize() {

        // initizalize paper with placeholder tokens
        paper = new char[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                paper[i][j] = ' ';
            }
        }

        // initialize posTokenChain
        posTokensChain = new Chain<>();

        // initialize counter to zero
        counter = 0;

        // fill up diagonal blocks
        fillDiagWithRandomTokens();

        // fill up remaining blocks
        fillAll();
    }

    /**
     * get a line of the matrix.
     * 
     * @param pos      position of the line in the matrix
     * @param vertical if the wanted line is a column
     * @return a line in the matrix at position [pos]
     */
    private char[] getLine(final int pos, final boolean vertical) {
        char[] line = new char[size];

        if (vertical) {
            for (int i = 0; i < size; i++) {
                line[i] = paper[i][pos];
            }

        } else {
            line = paper[pos];
        }

        return line;
    }

    /**
     * get values within a unit block at the given coordinate.
     * 
     * @param coordinate [int col, int row] pair
     * @return values in a block
     */
    private char[] getBlock(final int[] coordinate) {
        final char[] block = new char[size];

        int j = 0;
        for (int row = coordinate[1] * blockSize; row < (coordinate[1] + 1) * blockSize; row++) {

            for (int col = coordinate[0] * blockSize; col < (coordinate[0] + 1) * blockSize; col++) {

                block[j] = paper[row][col];
                j++;
            }

        }

        return block;
    }

    /**
     * fill up a unit sudoku block.
     * 
     * @param values     values fill to the block
     * @param coordinate [int col, int row] pair
     */
    private void setBlock(final char[] values, final int[] coordinate) {
        int j = 0;

        for (int i = coordinate[0] * blockSize; i < (coordinate[0] + 1) * blockSize; i++) {

            for (int k = coordinate[1] * blockSize; k < (coordinate[1] + 1) * blockSize; k++) {

                paper[i][k] = values[j];
                j++;
            }

        }
    }

    /**
     * Fill an available slot on paper.
     * 
     * @param coordinate [int col, int row] pair
     * @return if fill one slot successfully.
     */
    private boolean fillOne(final int[] coordinate) {
        ArrayList<Character> tokenList = getAvailableTokensForSlot(coordinate);

        if (tokenList.size() == 0) {
            // posTokensChain.printStack();
            posTokensChain.setRoot(posTokensChain.getRoot().getPrevious());
            paper[coordinate[1]][coordinate[0]] = ' ';
            return false;

        } else {
            int i = new Random().nextInt(tokenList.size());
            paper[coordinate[1]][coordinate[0]] = tokenList.get(i);

            tokenList.remove(i);

            return true;
        }
    }

    /**
     * fill up diagonal blocks with random tokens.
     */
    private void fillDiagWithRandomTokens() {
        for (int i = 0; i < blockSize; i++) {
            final char[] ts = Helper.randomize(tokens);
            setBlock(ts, new int[] { i, i });
        }
    }

    /**
     * Fill all slots on paper.
     */
    private void fillAll() {
        int[] coordinate = new int[] { 0, 0 };
        try {
            coordinate = getNextSlot(coordinate);

        } catch (NoAvailableSlotException e1) {
            e1.printStackTrace();
        }

        while (true) {
            try {

                // System.out.println("Try filling up [" + coordinate[1] + ", " + coordinate[0] + "]");

                if (fillOne(coordinate)) {
                    coordinate = getNextSlot(coordinate);

                } else {
                    coordinate = getPreviousSlot(coordinate);
                }

                counter++;

                if (counter > INITIALIZATION_LIMIT) {
                    initialize();
                    return;
                }

            } catch (final NoAvailableSlotException e) {
                break;
            }
        }
    }

    /**
     * Get previous available slot for filling up.
     * 
     * @param coordinate [int col, int row] pair
     * @return [int col, int row] pair
     * @throws NoAvailableSlotException if no available slot before
     */
    private int[] getPreviousSlot(final int[] coordinate) throws NoAvailableSlotException {
        if (coordinate[0] == blockSize && coordinate[1] == 0) {
            throw new NoAvailableSlotException(
                    "No available slot before [" + coordinate[1] + ", " + coordinate[0] + "].");
        }

        int[] c = coordinate;

        // System.out.println("Finding previous slot for: " + c[1] + ", " + c[0]);
        while (true) {
            if (c[0] == 0 && c[1] > 0) {
                c[0] = size - 1;
                c[1] -= 1;
            }

            else if (c[1] >= 0) {
                c[0] -= 1;
            }
            if (posOutOfBlock(c)) {
                break;
            }
        }

        // System.out.println("Found previous slot: " + c[1] + ", " + c[0] + "\n");

        return c;
    }

    /**
     * Get next available slot for filling up.
     * 
     * @param coordinate [int col, int row] pair
     * @return [int col, int row] pair
     * @throws NoAvailableSlotException if no available slot after
     */
    private int[] getNextSlot(final int[] coordinate) throws NoAvailableSlotException {
        if (coordinate[1] == size - 1 && coordinate[0] == size - blockSize - 1) {
            throw new NoAvailableSlotException(
                    "No available slot after [" + coordinate[1] + ", " + coordinate[0] + "].");
        }

        int[] c = coordinate;
        while (true) {
            if (c[0] == size - 1 && c[1] < size - 1) {
                c[0] = 0;
                c[1] += 1;
            }

            else if (c[1] <= size - 1) {
                c[0] += 1;
            }

            if (posOutOfBlock(c)) {
                break;
            }
        }

        return c;
    }

    /**
     * Determine if given coordinate is in diagonal blocks.
     * 
     * @param coordinate [int col, int row] pair
     * @return if given coordinate is in diagonal blocks
     */
    private boolean posOutOfBlock(final int[] coordinate) {
        for (int i = 0; i < blockSize; i++) {
            if ((coordinate[0] >= blockSize * i) && (coordinate[0] < blockSize * (i + 1))
                    && (coordinate[1] >= blockSize * i) && (coordinate[1] < blockSize * (i + 1))) {

                return false;
            }
        }
        return true;
    }

    /**
     * Get available tokens for a given slot and store it in posTokensChain
     * 
     * @param coordinate [int col, int row] pair
     * @return list of available tokens
     */
    private ArrayList<Character> getAvailableTokensForSlot(final int[] coordinate) {
        Chain<ArrayList<Character>>.Ring ts = posTokensChain.getById(Helper.generateId(coordinate));

        if (ts == null) {
            ArrayList<Character> tokenValues = new ArrayList<>();
            char[] colValues = getLine(coordinate[0], true);
            char[] rowValues = getLine(coordinate[1], false);
            char[] blockValues = getBlock(new int[] { coordinate[0] / blockSize, coordinate[1] / blockSize, });

            for (char t : tokens) {
                tokenValues.add(t);
            }
            
            for (Character c : colValues) {
                tokenValues.remove(c);
            }

            for (Character r : rowValues) {
                tokenValues.remove(r);
            }

            for (Character b : blockValues) {
                tokenValues.remove(b);
            }

            ts = posTokensChain.add(Helper.generateId(coordinate), tokenValues);
        }

        return (ArrayList<Character>) ts.getValue();
    }
}