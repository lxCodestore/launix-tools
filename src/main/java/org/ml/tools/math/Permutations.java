/*
 * The MIT License
 *
 * Copyright 2019 Dr. Matthias Laux.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.ml.tools.math;

import java.util.Arrays;

/**
 * Generates all permutations of 0 .. n-1 without repetition with order
 *
 * @author Dr. Matthias Laux
 */
public class Permutations {

    private int[][] permutations;
    private int resultIndex = 0;

    /**
     * Generates all permutations of 0 .. n-1 without repetition with order. The
     * results are collected in an integer array whose first dimension is equal
     * to n! which is the number of permutations. The second dimension of the
     * array contains the actual n permutation indexes
     *
     * @param n
     * @return
     */
    public int[][] generate(int n) {
        int permutationCount = Tools.factorial(n);
        permutations = new int[permutationCount][n];
        int[] d = new int[n];
        for (int i = 0; i < n; i++) {
            d[i] = i;
        }
        return generate(d);
    }

    /**
     *
     * @param input
     * @return
     */
    public int[][] generate(String input) {
        int[] d = new int[input.length()];
        for (int i = 0; i < input.length(); i++) {
            d[i] = input.charAt(i);
        }
        return generate(d);
    }

    /**
     *
     * @param input
     * @return
     */
    public int[][] generate(int[] input) {
        if (input == null) {
            throw new NullPointerException("input may not be null");
        }
        int n = input.length;
        int permutationCount = Tools.factorial(n);
        permutations = new int[permutationCount][n];
        permutate(new int[n], 0, input);
        return permutations;
    }

    /**
     * Recursively create the permutations based on the actual values in d[]
     *
     * @param resultCollector
     * @param resultCollectorIndex
     * @param permutableValues
     */
    private void permutate(int[] resultCollector, int resultCollectorIndex, int[] permutableValues) {

        if (permutableValues.length > 0) {
            //.... There are permutable values left, start the next level of the recursion with them, reducing the permutable value list accordingly
            for (int i = 0; i < permutableValues.length; i++) {
                int[] nextResultCollector = Arrays.copyOf(resultCollector, resultCollector.length);
                nextResultCollector[resultCollectorIndex] = permutableValues[i];
                permutate(nextResultCollector, resultCollectorIndex + 1, reduce(permutableValues, i));
            }
        } else {
            //.... We have reached the end of a tree walk, story the assembled result in the global collector and increase the global index
            System.arraycopy(resultCollector, 0, permutations[resultIndex], 0, resultCollector.length);
            resultIndex++;
        }
    }

    /**
     * Create a new int[] from d[] while removing the entry at index k
     *
     * @param d
     * @param k
     * @return
     */
    private int[] reduce(int[] d, int k) {
        int[] r = new int[d.length - 1];
        int ind = 0;
        for (int i = 0; i < d.length; i++) {
            if (i != k) {
                r[ind++] = d[i];
            }
        }
        return r;
    }

}
