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
import java.util.logging.LogRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 *
 * @author Dr. Matthias Laux
 */
public class ExcelLogFormatter extends Formatter {

    private int rowIndex = 0;
    private Sheet sheet = null;

    /**
     *
     * @param sheet
     */
    public void setSheet(Sheet sheet) {
        if (sheet == null) {
            throw new IllegalArgumentException("sheet may not be null");
        }
        this.sheet = sheet;
        addTextCellRow(sheet, rowIndex, 0, "Level", "Time", "Logger", "Message");
        rowIndex++;
    }

    /**
     *
     * @return
     */
    public boolean isInitialized() {
        return sheet != null;
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

        int colIndex = 0;
        Row row = sheet.createRow(rowIndex++);
        Cell cell = row.createCell(colIndex++);
        cell.setCellValue(record.getLevel().toString());
        cell = row.createCell(colIndex++);
        cell.setCellValue(calcDate(record.getMillis()));
        cell = row.createCell(colIndex++);
        cell.setCellValue(record.getLoggerName());
        cell = row.createCell(colIndex);
        cell.setCellValue(formatMessage(record).replaceAll("\n", " "));

        return "";
    }

    /**
     *
     * @param millisecs
     * @return
     */
    protected String calcDate(long millisecs) {
        SimpleDateFormat date_format = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
        Date resultdate = new Date(millisecs);
        return date_format.format(resultdate);
    }

    /**
     *
     * @param sheet
     * @param rowIndex
     * @param colStartIndex
     * @param values
     */
    private void addTextCellRow(Sheet sheet, int rowIndex, int colStartIndex, String... values) {
        if (sheet == null) {
            throw new IllegalArgumentException("sheet  may not be null");
        }
        if (rowIndex < 0) {
            throw new IllegalArgumentException("rowIndex may not be < 0");
        }
        if (colStartIndex < 0) {
            throw new IllegalArgumentException("colStartIndex may not be < 0");
        }
        if (values == null) {
            throw new IllegalArgumentException("values may not be null");
        }

        Row row = sheet.createRow(rowIndex);
        Cell cell;
        for (String value : values) {
            cell = row.createCell(colStartIndex++);
            cell.setCellValue(value);
        }
    }

}
