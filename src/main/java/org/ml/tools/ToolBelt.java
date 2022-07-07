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

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dr. Matthias Laux
 */
public class ToolBelt {

    public static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;
    private static final Pattern MAP_PATTERN = Pattern.compile("\\(\\s*?\"(.+?)\"\\s*,\\s*\"(.+?)\"\\s*\\)");
    private static final Pattern LIST_PATTERN = Pattern.compile("\"(.+?)\"");

    /**
     *
     */
    public enum XML {

        include
    }

    /**
     * @param fileName
     * @param charSet
     * @return
     * @throws FileNotFoundException
     */
    public static BufferedWriter getBufferedWriter(String fileName, Charset charSet) throws FileNotFoundException {
        if (fileName == null) {
            throw new IllegalArgumentException("fileName may not be null");
        }
        return getBufferedWriter(new File(fileName), charSet);
    }

    /**
     * @param file
     * @param charSet
     * @return
     * @throws FileNotFoundException
     */
    public static BufferedWriter getBufferedWriter(File file, Charset charSet) throws FileNotFoundException {
        if (file == null) {
            throw new IllegalArgumentException("file may not be null");
        }
        FileOutputStream stream = new FileOutputStream(file);
        Charset set = DEFAULT_CHARSET;
        if (charSet != null) {
            set = charSet;
        }
        OutputStreamWriter streamWriter = new OutputStreamWriter(stream, set);
        return new BufferedWriter(streamWriter);
    }

    /**
     * @param fileName
     * @return
     * @throws FileNotFoundException
     */
    public static BufferedWriter getBufferedWriter(String fileName) throws FileNotFoundException {
        return getBufferedWriter(fileName, null);
    }

    /**
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    public static BufferedWriter getBufferedWriter(File file) throws FileNotFoundException {
        return getBufferedWriter(file, null);
    }

    /**
     * @param propertiesFile
     * @return
     * @throws IOException
     */
    public static Properties loadProperties(File propertiesFile) throws IOException {
        if (propertiesFile == null) {
            throw new IllegalArgumentException("propertiesFile may not be null");
        }
        Properties properties = new Properties();
        if (propertiesFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(propertiesFile))) {
                properties.load(reader);
            }
        }
        return properties;
    }

    /**
     * @param properties
     * @param propertiesFile
     * @throws IOException
     */
    public static void saveProperties(Properties properties, File propertiesFile) throws IOException {
        if (properties == null) {
            throw new IllegalArgumentException("properties may not be null");
        }
        if (propertiesFile == null) {
            throw new IllegalArgumentException("propertiesFile may not be null");
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(propertiesFile))) {
            properties.store(writer, null);
        }
    }

    /**
     * @param properties
     * @param propertiesFileName
     * @throws IOException
     */
    public static void saveProperties(Properties properties, String propertiesFileName) throws IOException {
        if (propertiesFileName == null) {
            throw new IllegalArgumentException("propertiesFileName may not be null");
        }
        saveProperties(properties, new File(propertiesFileName));
    }

    /**
     * @param sourceElement
     * @return
     * @throws JDOMException
     * @throws IOException
     */
    public static Element resolveIncludes(Element sourceElement) throws JDOMException, IOException {
        if (sourceElement == null) {
            throw new IllegalArgumentException("sourceElement may not be null");
        }
        SAXBuilder builder = new SAXBuilder();
        if (sourceElement.getChildren(XML.include.toString()) != null) {
            List<Element> includeContent = new ArrayList<>();

            //.... Collect the content to include
            for (Element includeElement : sourceElement.getChildren(XML.include.toString())) {
                Document doc = builder.build(new BufferedReader(new FileReader(includeElement.getTextTrim())));
                includeContent.add(doc.getRootElement());
            }

            //.... Perform the include - this retains the original <include> tags
            //     This needs to be separate to avoid a ConcurrentModificationException
            for (Element include : includeContent) {
                include.getParent().removeContent(include);
                sourceElement.addContent(include);
            }
        }
        return sourceElement;
    }

    /**
     * @param inputString
     * @param chars
     * @return
     */
    public static boolean containsChars(String inputString, String chars) {
        if (inputString == null) {
            throw new IllegalArgumentException("inputString may not be null");
        }
        if (chars == null) {
            throw new IllegalArgumentException("chars may not be null");
        }
        for (int i = 0; i < chars.length(); i++) {
            char c = chars.charAt(i);
            for (int j = 0; j < inputString.length(); j++) {
                if (c == inputString.charAt(j)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Extract a map from a property string
     * <p>
     * Input format looks like this:
     * <p>
     * &lt;property name="productMap"&gt; ("SOO" , "Product 1"); ("Wc2000",
     * "Product 2"); ("SEP", "Product 3") &lt;/property&gt;
     *
     * @param data The input string compliant with the format described above
     * @return
     */
    public static Map<String, String> extractMap(String data) {
        if (data == null) {
            throw new IllegalArgumentException("data may not be null");
        }

        Map<String, String> map = new TreeMap<>();
        String[] mapElements = data.replaceAll("\\n", " ").trim().split(";");

        for (String mapElement : mapElements) {
            Matcher matcher = MAP_PATTERN.matcher(mapElement.trim());
            if (matcher.matches()) {
                map.put(matcher.group(1), matcher.group(2));
            }
        }
        return map;
    }

    /**
     * @param data
     * @return
     */
    public static Map<String, Set<String>> extractMapSet(String data) {
        if (data == null) {
            throw new IllegalArgumentException("data may not be null");
        }

        Map<String, Set<String>> map = new TreeMap<>();
        String[] mapElements = data.replaceAll("\\n", " ").trim().split(";");

        for (String mapElement : mapElements) {
            Matcher matcher = MAP_PATTERN.matcher(mapElement.trim());
            if (matcher.matches()) {
                String key = matcher.group(1);
                if (!map.containsKey(key)) {
                    map.put(key, new HashSet<>());
                }
                map.get(key).add(matcher.group(2));
            }
        }
        return map;
    }

    /**
     * This is similar to extractMap() but it retains the order in which the
     * data appears in the source string
     *
     * @param data
     * @return
     */
    public static List<String[]> extractMapAsList(String data) {
        if (data == null) {
            throw new IllegalArgumentException("data may not be null");
        }

        List<String[]> list = new ArrayList<>();
        String[] listElements = data.replaceAll("\\n", " ").trim().split(";");

        for (String listElement : listElements) {
            Matcher matcher = MAP_PATTERN.matcher(listElement.trim());
            if (matcher.matches()) {
                String[] s = new String[2];
                s[0] = matcher.group(1);
                s[1] = matcher.group(2);
                list.add(s);
            }
        }
        return list;
    }

    /**
     * Input string format: comma-separated list of strings within apostrophe,
     * surrounded by ()
     * <p>
     * Example: ( "a", "b", "c" )
     *
     * @param data
     * @return
     */
    public static List<String> extractList(String data) {
        if (data == null) {
            throw new IllegalArgumentException("data may not be null");
        }

        List<String> list = new ArrayList<>();

        String d = data.replaceAll("\\n", " ").trim();
        if (d.length() > 0) {    // Empty string returns an empty list
            String[] listElements = d.substring(1, d.length() - 1).split(",");
            for (String listElement : listElements) {
                Matcher matcher = LIST_PATTERN.matcher(listElement.trim());
                if (matcher.matches()) {
                    list.add(matcher.group(1));
                }
            }
        }
        return list;
    }

    /**
     * Try to extract a Comparable object of the target DataType
     *
     * @param targetDataType
     * @param data
     * @return
     */
    public static Comparable getComparableValue(String data, DataType targetDataType) {
        if (data == null) {
            throw new NullPointerException("data may not be null");
        }
        if (targetDataType == null) {
            throw new NullPointerException("targetDataType may not be null");
        }

        switch (targetDataType) {
            case TypeDouble:
            case TypeDoublePercentage:
                return Double.valueOf(data);
            case TypeInteger:
            case TypeIntegerPercentage:
                return Integer.valueOf(data);
            case TypeString:
                return data;
            case TypeBoolean:
                return Boolean.valueOf(data);
            default:
                throw new UnsupportedOperationException("Can not extract " + targetDataType + " from raw data " + data);
        }

    }
}
