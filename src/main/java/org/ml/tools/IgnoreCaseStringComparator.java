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
package org.ml.tools;

import java.util.Comparator;

/**
 *
 * @author Dr. Matthias Laux
 */
public class IgnoreCaseStringComparator implements Comparator<String> {

    private final static String CLASS = "IgnoreCaseStringComparator";

    /**
     *
     * @param s1
     * @param s2
     * @return
     */
    @Override
    public int compare(String s1, String s2) {
        if (s1 == null) {
            throw new IllegalArgumentException(CLASS + ": s1 may not be null");
        }
        if (s2 == null) {
            throw new IllegalArgumentException(CLASS + ": s2 may not be null");
        }
        return s1.compareToIgnoreCase(s2);
    }
}
