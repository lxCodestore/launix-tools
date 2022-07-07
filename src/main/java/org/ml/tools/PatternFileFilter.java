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

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import org.jdom2.Element;

/**
 *
 * @author Dr. Matthias Laux
 */
public class PatternFileFilter implements FileFilter {

    private List<Pattern> patterns = new ArrayList<>();
    private List<Boolean> types = new ArrayList<>();

    /**
     *
     */
    private enum XML {

        type
    }

    private enum Type {

        accept, reject
    }

    /**
     *
     * @param patternElements
     */
    public PatternFileFilter(Collection<Element> patternElements) {
        if (patternElements == null) {
            throw new IllegalArgumentException("patternElements may not be null");
        }
        for (Element patternElement : patternElements) {
            patterns.add(Pattern.compile(patternElement.getTextTrim()));
            if (patternElement.getAttribute(XML.type.toString()) != null) {
                Type type = Type.valueOf(patternElement.getAttributeValue(XML.type.toString()));  // We assume schema validation
                switch (type) {
                    case accept:
                        types.add(Boolean.TRUE);
                        break;
                    case reject:
                        types.add(Boolean.FALSE);
                        break;
                }
            } else {
                types.add(Boolean.TRUE);  // Default type is "accept"
            }
        }
    }

    /**
     *
     * @param patternTexts
     */
    public PatternFileFilter(String... patternTexts) {
        if (patternTexts == null) {
            throw new IllegalArgumentException("patternTexts may not be null");
        }
        for (String patternText : patternTexts) {
            patterns.add(Pattern.compile(patternText));
            types.add(Boolean.TRUE);  // Default type is "accept"
        }
    }

    /**
     * For acceptance, at least one accept patterns needs to accept the name.
     * For rejection, at least one reject patterns needs to reject the file.
     * Since patterns are processed in the sequence in which they are specified,
     * pattern results can be overridden by subsequent patterns.
     *
     * @param pathName
     * @return
     */
    @Override
    public boolean accept(File pathName) {
        if (pathName == null) {
            throw new IllegalArgumentException("pathName may not be null");
        }
        boolean result = false;
        int index = 0;
        for (Pattern pattern : patterns) {
            if (pattern.matcher(pathName.getName()).find()) {
                result = types.get(index++);
            }
        }
        return result;
    }
}
