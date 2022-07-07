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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.ml.tools.PropertyManager;
import org.ml.tools.ToolBelt;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author Dr. Matthias Laux
 */
@Deprecated
public class LoggingSupport extends PropertyManager {

    public static final Level DEFAULT_LEVEL = Level.INFO;
    private final List<Handler> handlers = new ArrayList<>();
    private final Map<Integer, Set<Integer>> addedHandlers = new HashMap<>();
    private static LoggingSupport internalDefaultLoggingSupport = new LoggingSupport();

    /**
     *
     */
    public enum Property {

        logLevel
    }

    /**
     *
     */
    public LoggingSupport() {
    }

    /**
     *
     * @param element
     * @throws JDOMException
     * @throws IOException
     */
    public LoggingSupport(Element element) throws JDOMException, IOException {
        if (element == null) {
            throw new IllegalArgumentException("element may not be null");
        }
        ToolBelt.resolveIncludes(element);
        if (PropertyManager.containsPropertiesElement(element)) {
            setProperties(PropertyManager.extractProperties(element));
        }
    }

    /**
     *
     * @param configFile
     * @throws JDOMException
     * @throws IOException
     */
    public LoggingSupport(File configFile) throws JDOMException, IOException {
        if (configFile == null) {
            throw new IllegalArgumentException("configFile may not be null");
        }
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new BufferedReader(new FileReader(configFile)));
        Element element = doc.getRootElement();
        ToolBelt.resolveIncludes(element);
        if (PropertyManager.containsPropertiesElement(element)) {
            setProperties(PropertyManager.extractProperties(element));
        }
    }

    /**
     * @return the defaultLoggingSupport
     */
    public static LoggingSupport getDefaultLoggingSupport() {
        return internalDefaultLoggingSupport;
    }

    /**
     * @param defaultLoggingSupport the defaultLoggingSupport to set
     */
    public static void setDefaultLoggingSupport(LoggingSupport defaultLoggingSupport) {
        if (defaultLoggingSupport == null) {
            throw new IllegalArgumentException("defaultLoggingSupport may not be null");
        }
        internalDefaultLoggingSupport = defaultLoggingSupport;
    }

    /**
     *
     * @return
     */
    public List<Handler> getHandlers() {
        return handlers;
    }

    /**
     *
     * @param logger
     */
    public void setupLogger(Logger logger) {
        if (logger == null) {
            throw new IllegalArgumentException("logger may not be null");
        }

        //.... No handlers have been added yet for this logger
        int loggerHashCode = logger.hashCode();
        if (!addedHandlers.containsKey(loggerHashCode)) {
            addedHandlers.put(loggerHashCode, new HashSet<>());
            for (Handler handler : getHandlers()) {
                logger.addHandler(handler);
                addedHandlers.get(loggerHashCode).add(handler.hashCode());
            }

            //.... Check to see that we don't add the same handler to the same logger twice (which would create multiple outputs of the same messages)
        } else {
            for (Handler handler : getHandlers()) {
                if (!addedHandlers.get(loggerHashCode).contains(handler.hashCode())) {
                    logger.addHandler(handler);
                    addedHandlers.get(loggerHashCode).add(handler.hashCode());
                }
            }
        }

        //.... Set the level (can of course be overriden)
        logger.setLevel(getLevel());
    }

    /**
     *
     * @param handler
     */
    public void addHandler(Handler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler may not be null");
        }
        handlers.add(handler);
    }

    /**
     *
     * @return
     */
    public Level getLevel() {
        if (!containsProperty(Property.logLevel.toString())) {
            return DEFAULT_LEVEL;
        } else {
            return Level.parse(getProperty(Property.logLevel.toString()));
        }
    }

    /**
     * For debugging purposes
     */
    public static void analyzeLogManager() {
        LogManager logManager = LogManager.getLogManager();
        for (Enumeration<String> e = logManager.getLoggerNames(); e.hasMoreElements();) {
            String loggerName = e.nextElement();
            Logger logger = logManager.getLogger(loggerName);
            System.out.println("Logger " + loggerName);
            System.out.println("  Level  = " + logger.getLevel());
            System.out.println("  Parent = " + logger.getParent());
            System.out.println("  Filter = " + logger.getFilter());
            for (Handler handler : logger.getHandlers()) {
                System.out.println("  Handler " + handler.toString());
                System.out.println("    Level     = " + handler.getLevel());
                System.out.println("    Formatter = " + handler.getFormatter());
                System.out.println("    Filter    = " + handler.getFilter());
            }
        }
    }
}
