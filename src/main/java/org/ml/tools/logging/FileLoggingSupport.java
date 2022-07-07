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
package org.ml.tools.logging;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;

import org.jdom2.Element;
import org.jdom2.JDOMException;

/**
 * @author Dr. Matthias Laux
 */
@Deprecated
public class FileLoggingSupport extends LoggingSupport {

    /**
     *
     */
    private enum Property {

        logFile, logPath
    }

    /**
     * @param configFileName
     * @throws JDOMException
     * @throws IOException
     */
    public FileLoggingSupport(String configFileName) throws JDOMException, IOException {
        super(new File(configFileName));
    }

    /**
     * @param configFile
     * @throws JDOMException
     * @throws IOException
     */
    public FileLoggingSupport(File configFile) throws JDOMException, IOException {
        super(configFile);
    }

    /**
     * @param element
     * @throws JDOMException
     * @throws IOException
     */
    public FileLoggingSupport(Element element) throws JDOMException, IOException {
        super(element);
    }

    /**
     * @param formatter
     * @return
     * @throws IOException
     */
    public Handler getHandlerWithFormatter(Formatter formatter) throws IOException {
        Handler handler = new FileHandler(getLogFileName());
        handler.setFormatter(formatter);
        return handler;
    }

    /**
     * @return @throws IOException
     */
    public String getLogFileName() {
        if (!containsProperty(Property.logPath.toString())) {
            throw new IllegalArgumentException("Missing required property: " + Property.logPath.toString());
        }
        if (!containsProperty(Property.logFile.toString())) {
            throw new IllegalArgumentException("Missing required property: " + Property.logFile.toString());
        }
        String logPath = getProperty(Property.logPath.toString());
        File path = new File(logPath);
        if (!path.exists()) {
            path.mkdirs();
        }
        String logFile = getProperty(Property.logFile.toString());
        return logPath + File.separatorChar + logFile;
    }
}
