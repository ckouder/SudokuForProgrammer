package com.example.sudokuforprogrammer;

public class Constants {

    /** Privatize constructor. */
    private Constants() {}

    /** Difficulty represented as thee number of blocks taken off the grid. */
    final static int DIFFICULTY_EXPERT = 181;

    /** Difficulty represented as thee number of blocks taken off the grid. */
    final static int DIFFICULTY_HARD = 176;

    /** Difficulty represented as thee number of blocks taken off the grid. */
    final static int DIFFICULTY_MEDIUM = 161;

    /** Difficulty represented as thee number of blocks taken off the grid. */
    final static int DIFFICULTY_EASY = 136;

    /** Tells the program when to abort solving the puzzle. (means no solution) */
    final static long SOLVE_TIME_LIMIT = 20;

    /** Tokens used for representing numerical values in a grid. */
    final static char[] TOKENS = {
            '0', '1', '2', '3',
            '4', '5', '6', '7',
            '8', '9', 'A', 'B',
            'C', 'D', 'E', 'F'
    };

}
