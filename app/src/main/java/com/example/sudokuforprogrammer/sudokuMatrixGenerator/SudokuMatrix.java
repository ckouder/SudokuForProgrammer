package com.example.sudokuforprogrammer.sudokuMatrixGenerator;

import java.util.ArrayList;
import java.util.Random;

/** Generates a sudoku grid. */
public class SudokuMatrix {

    /** Throw no available slot exception when cannot visit the previous or the next slot. */
    private class NoAvailableSlotException extends Exception {
        /**
         * No available slot exception.
         * @param string message
         */
        NoAvailableSlotException(String string) {
            super(string);
        }
    }

    /** Initialization limit for the matrix, will reinitialize if surpass the limit. */
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
     * Initialise a SudokuMatrix with given tokens.
     * @param setBlockSize unit block size of the matrix
     * @param tokenList list of tokens used as sudoku tokens
     * @throws Exception if block size is too big
     */
    public SudokuMatrix(final int setBlockSize, final char[] tokenList) throws Exception {
        if (setBlockSize > 16) {
            throw new Exception("block size [" + setBlockSize + "] is too large");
        }
        if (tokenList.length != setBlockSize * setBlockSize) {
            throw new Exception("token list length doesn't match sudoku size");
        }
        blockSize = setBlockSize;
        size = (int) Math.pow(blockSize, 2);
        tokens = tokenList;

        initialize();
    }

    /**
     * Create sudokuMatrix with default tokens
     * @param setBlockSize unit block size of the matrix
     * @return SudokuMatrix
     * @throws Exception if block size is too big
     */
    public static SudokuMatrix createWithDefaultTokens(final int setBlockSize) throws Exception {
        char[] tokens = new char[] {
                'A', 'B', 'C', 'D',
                'E', 'F', 'G', 'H',
                'I', 'J', 'K', 'L',
                'M', 'N', 'O', 'P'
        };

        return new SudokuMatrix(setBlockSize, tokens);
    }

    /**
     * @return the paper
     */
    public char[][] getPaper() {
        return paper;
    }

    /**
     * return the paper in integer[][]
     * @return the paper
     */
    public Integer[][] getPaperInInteger() {
        Integer[][] integerPaper = new Integer[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                integerPaper[i][j] = Integer.parseInt("" + paper[i][j], 16);
            }
        }

        return integerPaper;
    }

    /**
     * Implement +toString(): String so that the matrix can be displayed.
     * @return String representation of the matrix
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                sb.append(paper[i][j] + " ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /** Initialize a sudoku matrix with given tokens and fill it up. */
    private void initialize() {

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
     * get a row of the matrix.
     * 
     * @param pos      position of the line in the matrix
     * @return a line in the matrix at position [pos]
     */
    private char[] getRow(final int pos) {
        return paper[pos];
    }

    /**
     * get a column of the matrix.
     *
     * @param pos      position of the line in the matrix
     * @return a line in the matrix at position [pos]
     */
    private char[] getColumn(final int pos) {
        char[] col = new char[size];

        for (int i = 0; i < size; i++) {
            col[i] = paper[i][pos];
        }

        return col;
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

        do {
            if (coordinate[0] == 0 && coordinate[1] > 0) {
                coordinate[0] = size - 1;
                coordinate[1] -= 1;
            }
            else if (coordinate[1] >= 0) {
                coordinate[0] -= 1;
            }
        } while (!posOutOfBlock(coordinate));

        return coordinate;
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

        do {
            if (coordinate[0] == size - 1 && coordinate[1] < size - 1) {
                coordinate[0] = 0;
                coordinate[1] += 1;
            }
            else if (coordinate[1] <= size - 1) {
                coordinate[0] += 1;
            }
        } while (!posOutOfBlock(coordinate));

        return coordinate;
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
            char[] colValues = getColumn(coordinate[0]);
            char[] rowValues = getRow(coordinate[1]);
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