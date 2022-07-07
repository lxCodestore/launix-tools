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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A singleton helper class to deal with logging and different handlers.
 * <p>
 * The idea is that a parent logger is established and handlers are added to
 * this parent. Any subsequent logger instantiations are effected through the
 * static method and these loggers are created such that they all share this
 * parent and that they use the handlers defined for the parent as well. This
 * makes it rather easy to create many loggers with the same behaviour which
 * only requires one line of code in the sources each.
 *
 * Each logger can of course have additional own handlers on top.
 *
 * @author Dr. Matthias Laux
 */
public class LoggerFactory {

    private static Map<String, Logger> loggers = new HashMap<>();
    private static final String MASTER_LOGGER = "MasterLogger";
    private static final Logger MASTER = Logger.getLogger(MASTER_LOGGER);
    private static Level loggerLevel = Level.INFO;

    /**
     *
     */
    public enum XML {
        loggers, logger, type, handlers, handler, levels
    }

    /**
     *
     */
    public enum Type {
        CSV, Excel, HTML
    }

    /**
     *
     */
    public enum Parameter {
        logCSSLocation, logLevels
    }

    /**
     * Protect against instantiation (this is a singleton)
     */
    private LoggerFactory() {
    }

    /**
     *
     * @param level
     */
    public static void setLevel(Level level) {
        if (level == null) {
            throw new IllegalArgumentException("level may not be null");
        }
        loggerLevel = level;
    }

    /**
     * Add a handler to the parent logger. This will be re-used by all loggers
     * created subsequently
     *
     * @param handler
     */
    public static void addHandler(Handler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler may not be null");
        }
        MASTER.addHandler(handler);
    }

    /**
     *
     * @param loggerName
     * @return
     */
    public static Logger getLogger(String loggerName) {
        if (loggerName == null) {
            throw new IllegalArgumentException("loggerName may not be null");
        }
        if (!loggers.containsKey(loggerName)) {
            Logger logger = Logger.getLogger(loggerName);
            logger.setParent(MASTER);
            logger.setUseParentHandlers(true);
            logger.setLevel(loggerLevel);
            loggers.put(loggerName, logger);
        }
        return loggers.get(loggerName);
    }

}
