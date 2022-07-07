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
package org.ml.tools.token;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ml.tools.logging.LoggerFactory;

/**
 *
 * @author Dr. Matthias Laux
 */
public class BasicTokenResolver implements ITokenResolver {

    private final static Logger LOGGER = LoggerFactory.getLogger(BasicTokenResolver.class.getName());
    private Map<String, String> replacements ;
    private boolean debug = false;

    /**
     *
     * @param replacements
     * @param debug
     */
    public BasicTokenResolver(Map<String, String> replacements, boolean debug) {
        if (replacements == null) {
            throw new IllegalArgumentException("replacements may not be null");
        }
        this.replacements = replacements;
        this.debug = debug;
    }

    /**
     *
     * @param replacements
     */
    public BasicTokenResolver(Map<String, String> replacements) {
        this(replacements, false);
    }

    /**
     *
     * @param tokenName
     * @return
     */
    @Override
    public String resolveToken(String tokenName) {
        if (tokenName == null) {
            throw new IllegalArgumentException("tokenName may not be null");
        }
        if (replacements.containsKey(tokenName)) {
            if (debug) {
                LOGGER.log(Level.INFO, "Replacing token ''{0}'' with ''{1}''", new Object[]{tokenName, replacements.get(tokenName)});
            }
            return replacements.get(tokenName);
        } else {
            throw new IllegalArgumentException("Unknown token: " + tokenName);
        }
    }

}
