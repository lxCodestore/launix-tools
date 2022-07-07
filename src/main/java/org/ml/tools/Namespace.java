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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dr. Matthias Laux
 */
public class Namespace implements Comparable<Namespace>, Serializable {

    static final long serialVersionUID = 1127987667L;
    private String id;
    private int hashCode = 0;
    private static final char SEPARATOR_CHAR = ':';
    private List<String> keys = new ArrayList<>();

    /**
     *
     * @param keys
     */
    public Namespace(String... keys) {
        if (keys == null) {
            throw new IllegalArgumentException("keys may not be null");
        }

        StringBuilder sb = new StringBuilder(200);
        int i = 0;
        for (String key : keys) {
            sb.append(key);
            sb.append(SEPARATOR_CHAR);
            this.keys.add(keys[i++]);
        }

        id = sb.toString();
        id = id.substring(0, id.length() - 1);
        hashCode = id.hashCode();
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     *
     * @param namespace
     * @return
     */
    @Override
    public boolean equals(Object namespace) {
        if (namespace == null) {
            return false;
        }
        if (namespace == this) {
            return true;
        }
        if (namespace.getClass() != getClass()) {
            return false;
        }
        return namespace.toString().equals(id);
    }

    /**
     *
     * @param extensionKey
     * @return
     */
    public Namespace derive(String extensionKey) {
        if (extensionKey == null) {
            throw new IllegalArgumentException("extensionKey may not be null");
        }
        String[] newKeys = new String[keys.size() + 1];
        int i = 0;
        for (String key : keys) {
            newKeys[i++] = key;
        }
        newKeys[i] = extensionKey;
        return new Namespace(newKeys);
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return id;
    }

    /**
     *
     * @param namespace
     * @return
     */
    @Override
    public int compareTo(Namespace namespace) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace may not be null");
        }
        return this.toString().compareTo(namespace.toString());
    }
}
