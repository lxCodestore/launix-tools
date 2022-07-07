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
import java.io.IOException;
import java.util.Properties;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * This class supports loading different files via file selectors with the
 * additional benefit of storing the paths and file names in a config file such
 * that they are re-used on subsequent invocations
 *
 * @author Dr. Matthias Laux
 */
public class SimpleSelectionHandler {

    private final static String DEFAULT_FILE_KEY = "default_file";
    private String configFileName ;
    private Properties properties;
    private String title = "";
    private int selectionMode = JFileChooser.FILES_ONLY;

    /**
     *
     * @param configFileName The name of the config file to be used
     * @throws IOException
     */
    public SimpleSelectionHandler(String configFileName) throws IOException {
        if (configFileName == null) {
            throw new IllegalArgumentException("configFileName may not be null");
        }
        this.configFileName = configFileName;
        File configFile = new File(configFileName);
        if (configFile.exists()) {
            properties = ToolBelt.loadProperties(configFile);
        } else {
            properties = new Properties();
        }
    }

    /**
     * Select a file
     *
     * @param fileFilter A filter controlling the choices; may be null
     * @return
     * @throws IOException
     */
    public File select(FileFilter fileFilter) throws IOException {
        return select(DEFAULT_FILE_KEY, fileFilter);
    }

    /**
     *
     * @param fileKey
     * @param fileFilter A filter controlling the choices; may be null
     * @return
     * @throws IOException
     */
    public File select(String fileKey, FileFilter fileFilter) throws IOException {
        if (fileKey == null) {
            throw new IllegalArgumentException("fileKey may not be null");
        }
        File file = null;
        if (properties.containsKey(fileKey)) {
            file = new File(properties.getProperty(fileKey));
        }

        //.... Setup the chooser
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(selectionMode);
        chooser.setSelectedFile(file);
        if (fileFilter != null) {
            chooser.setFileFilter(fileFilter);
        }

        //.... Actual selection process
        int retVal = chooser.showOpenDialog(chooser);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            properties.setProperty(fileKey, file.getPath());
            ToolBelt.saveProperties(properties, configFileName);
            return file;
        }
        return null;
    }

    /**
     *
     * @param title
     */
    public void setTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("title may not be null");
        }
        this.title = title;
    }

    /**
     *
     * @param selectionMode
     */
    public void setSelectionMode(int selectionMode) {
        this.selectionMode = selectionMode;
    }
}
