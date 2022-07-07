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
 * @author Dr. Matthias Laux
 */
public class CSVLogFormatter extends Formatter {

    private final static char SEP = ';';

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

        sb.append(record.getLevel());
        sb.append(SEP);
        sb.append(calcDate(record.getMillis()));
        sb.append(SEP);
        sb.append(record.getLoggerName());
        sb.append(SEP);

        String f = formatMessage(record);
        if (f != null) {
            sb.append(formatMessage(record).replaceAll("\n", " ").replaceAll(";", ","));
        } else {
            sb.append(record);
        }
        sb.append("\n");

        return sb.toString();
    }

    /**
     * @param millisecs
     * @return
     */
    protected String calcDate(long millisecs) {
        SimpleDateFormat date_format = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
        Date resultdate = new Date(millisecs);
        return date_format.format(resultdate);
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

        sb.append("Level;Time;Logger;Message\n");

        return sb.toString();
    }

}
