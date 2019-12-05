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
     * use a chain data type to chain coordinate and its available tokens. it is an
     * incomplete version of linked list.
     * 
     * @param <E> anything you want to chain
     */
    private class Chain<E> {

        /**
         * internal class of chain to store what ever you want.
         */
        private class Ring {

            /** Give an id to value you want to store. */
            private final String id;

            /** Value you want to store. */
            private E value;

            /** Previous ring on the chain. */
            private Ring previous;

            /**
             * create a ring with given value.
             * 
             * @param setId       id of the ring
             * @param setValue    value of the ring
             * @param setPrevious previous ring
             */
            Ring(final String setId, final E setValue, final Ring setPrevious) {

                id = setId;
                value = setValue;
                previous = setPrevious;
            }

            /**
             * Get id of the ring.
             */
            public String getId() {
                return id;
            }

            /**
             * Get value of the ring.
             */
            public E getValue() {
                return value;
            }

            /**
             * Set ring value.
             * 
             * @param setValue value you want to store.
             */
            public void setValue(final E setValue) {
                value = setValue;
            }

            /**
             * Get previous ring.
             * 
             * @return previous ring
             */
            public Ring getPrevious() {
                return previous;
            }

            /**
             * Set previous ring.
             * 
             * @param setPrevious Ring you want to be the previous one.
             */
            public void setPrevious(final Ring setPrevious) {
                previous = setPrevious;
            }
        }

        /** The root ring on chain. */
        private Ring root;

        /** Construct an empty chain. */
        Chain() { }

        /**
         * Get root ring.
         * 
         * @return root
         */
        public Ring getRoot() {
            return root;
        }

        /**
         * Set root ring.
         * 
         * @param setRoot root Ring you want to be
         */
        public void setRoot(Ring setRoot) {
            root = setRoot;
        }

        /**
         * Add a ring to chain.
         * 
         * @param id    id of the ring
         * @param value value you want to store
         * @return Ring itself
         */
        public Ring add(final String id, final E value) {

            Ring ring = new Ring(id, value, root);
            root = ring;

            return ring;
        }

        /**
         * Get a ring from a specific ring on chain.
         * 
         * @param current ring you want to start search
         * @param id      id of the ring
         * @return Ring if found. null if not
         */
        private Ring getFromId(final Ring current, final String id) {

            if (current == null || current.getId().equals(id)) {
                return current;
            }

            return getFromId(current.previous, id);
        }

        /**
         * Get a ring by its id.
         * 
         * @param id id of the ring
         * @return Ring if found, null if not
         */
        public Ring getById(final String id) {
            return getFromId(root, id);
        }


        public void printStack() {
            Ring current = root;

            while(current != null) {
                // // System.out.println("the current ring id is: " + current.getId());
                current = current.getPrevious();
            }
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
            posTokensChain.setRoot(posTokensChain.root.getPrevious());
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
            if (!isPosInDiagonalBlocks(c)) {
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

            if (!isPosInDiagonalBlocks(c)) {
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
    private boolean isPosInDiagonalBlocks(final int[] coordinate) {
        for (int i = 0; i < blockSize; i++) {
            if ((coordinate[0] >= blockSize * i) && (coordinate[0] < blockSize * (i + 1))
                    && (coordinate[1] >= blockSize * i) && (coordinate[1] < blockSize * (i + 1))) {

                return true;
            }
        }
        return false;
    }

    /**
     * Get available tokens for a given slot and store it in posTokensChain
     * 
     * @param coordinate [int col, int row] pair
     * @return list of available tokens
     */
    private ArrayList<Character> getAvailableTokensForSlot(final int[] coordinate) {
        SudokuMatrix.Chain<ArrayList<Character>>.Ring ts = posTokensChain.getById(Helper.generateId(coordinate));

        if (ts == null) {
            ArrayList<Character> tokenValues = new ArrayList<>();
            char[] colValues = getLine(coordinate[0], true);
            char[] rowValues = getLine(coordinate[1], false);
            char[] blockValues = getBlock(new int[] { coordinate[0] / blockSize, coordinate[1] / blockSize, });

            for (char t : tokens) {
                tokenValues.add(t);
            }

            // System.out.println("getting available tokens for [" + coordinate[1] + ", " + coordinate[0] + "]");

            // System.out.println("token values are: " + tokenValues);
            for (Character c : colValues) {
                // System.out.println("remove " + c + " from column values");
                tokenValues.remove(c);
            }
            // System.out.println("after remove column values: " + tokenValues);

            for (Character r : rowValues) {
                // System.out.println("remove " + r + " from row values");
                tokenValues.remove(r);
            }
            // System.out.println("after remove row values: " + tokenValues);

            for (Character b : blockValues) {
                // System.out.println("remove " + b + " from block values");
                tokenValues.remove(b);
            }
            // System.out.println("after remove block values: " + tokenValues);

            // System.out.println();

            ts = posTokensChain.add(Helper.generateId(coordinate), tokenValues);

        //     System.out.println("Generate ring with id [" + Helper.generateId(coordinate) + "] now.");
        //     System.out.println("Ring values " + (ArrayList<Character>) ts.getValue());
        //     System.out.println();

        // } else {
        //     System.out.println("Ring with id [" + Helper.generateId(coordinate) + "] is found.");
        //     System.out.println("Ring values " + (ArrayList<Character>) ts.getValue());
        //     System.out.println();
        }

        // // System.out.println("get available tokens for [" + coordinate[1] + ", " + coordinate[0] + "]: " + ts.value);

        return (ArrayList<Character>) ts.getValue();
    }
}