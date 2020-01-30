package com.example.sudokuforprogrammer;

public class Constants {

    /** Privatize constructor. */
    private Constants() {}

    /** Life num of the game. */
    final static int LIFE_NUM = 8;

    /** Difficulty represented as the number of blocks taken off the grid. */
    final static int DIFFICULTY_EXPERT = 181;

    /** Difficulty represented as the number of blocks taken off the grid. */
    final static int DIFFICULTY_HARD = 176;

    /** Difficulty represented as the number of blocks taken off the grid. */
    final static int DIFFICULTY_MEDIUM = 161;

    /** Difficulty represented as the number of blocks taken off the grid. */
    final static int DIFFICULTY_EASY = 136;

    /** Tokens used for representing numerical values in a grid. */
    final static char[] TOKENS = {
            '0', '1', '2', '3',
            '4', '5', '6', '7',
            '8', '9', 'A', 'B',
            'C', 'D', 'E', 'F'
    };

    final static float KEY_PRESSED_VOLUME = 0.1f;

    final static float MOUSE_CLICKED_VOLUME = 1.3f;

}
