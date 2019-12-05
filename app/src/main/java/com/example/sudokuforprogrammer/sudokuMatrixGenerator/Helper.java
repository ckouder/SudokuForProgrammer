package com.example.sudokuforprogrammer.sudokuMatrixGenerator;

import java.util.Random;

class Helper {
    Helper() { }

    static char[] randomize(final char[] l) {
        if (l == null || l.length == 0) {
            return l;
        }
        char[] j = l;

        for (int i = 0; i < j.length; i++) {
            int k = new Random().nextInt(j.length - i) + i;
            char temp = j[i];
            j[i] = j[k];
            j[k] = temp;
        }

        return j;
    }

    static String generateId(final int[] coordinate) {
        return "" + coordinate[0] + " " + coordinate[1];
    }
}