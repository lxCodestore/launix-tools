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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 *
 * @author Dr. Matthias Laux
 */
public class HTMLLogFormatter extends Formatter {

    public final String DEFAULT_LOG_CSS_LOCATION = "log.css";
    private String logCSSLocation;

    /**
     *
     */
    public HTMLLogFormatter() {
        logCSSLocation = DEFAULT_LOG_CSS_LOCATION;
    }

    /**
     *
     * @param logCSSLocation
     */
    public HTMLLogFormatter(String logCSSLocation) {
        if (logCSSLocation == null) {
            throw new IllegalArgumentException("logCSSLocation may not be null");
        }
        this.logCSSLocation = logCSSLocation;
    }

    /**
     * This method is called for every log record
     *
     * @param record
     * @return
     */
    @Override
    public String format(LogRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("record may not be null");
        }

        StringBuilder sb = new StringBuilder(500);

        sb.append("<tr class=");
        sb.append(record.getLevel());
        sb.append("><td>");
        sb.append(record.getLevel());
        sb.append("</td><td>");
        sb.append(calcDate(record.getMillis()));
        sb.append("</td><td>");
        sb.append(record.getLoggerName());
        sb.append("</td><td>");
        sb.append(formatMessage(record));
        sb.append("</td>");
        sb.append("</tr>\n");

        return sb.toString();
    }

    /**
     *
     * @param millisecs
     * @return
     */
    protected String calcDate(long millisecs) {
        SimpleDateFormat date_format = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
        Date resultdate = new Date(millisecs);
        return date_format.format(resultdate).replaceAll(" ", "&nbsp;");
    }

    /**
     * This method is called just after the handler using this formatter is
     * created
     *
     * @param handler
     * @return
     */
    @Override
    public String getHead(Handler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler may not be null");
        }

        StringBuilder sb = new StringBuilder(500);

        sb.append("<html>\n");
        sb.append("<head>\n");
        sb.append("<link rel=\"stylesheet\" href=\"");
        sb.append(logCSSLocation);
        sb.append("\">");
        sb.append("</head>\n");
        sb.append("<html>\n");
        sb.append("<table border=1>\n");
        sb.append("<tr>\n");
        sb.append("<th>Level</th>\n");
        sb.append("<th>Time</th>\n");
        sb.append("<th>Logger</th>\n");
        sb.append("<th>Message</th>\n");
        sb.append("</tr>\n");

        return sb.toString();
    }

    /**
     * This method is called just after the handler using this formatter is
     * closed
     *
     * @param handler
     * @return
     */
    @Override
    public String getTail(Handler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler may not be null");
        }

        StringBuilder sb = new StringBuilder(500);

        sb.append("</table>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");

        return sb.toString();
    }
}
