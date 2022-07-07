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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Dr. Matthias Laux
 */
@Deprecated
public class ExcelFileHandler extends Handler {

    public final static String DEFAULT_SHEET_NAME = "logMessages";
    private Workbook workbook;
    private Sheet sheet;
    private String excelFileName ;

    /**
     *
     * @param excelFileName
     * @throws Exception
     */
    public ExcelFileHandler(String excelFileName) throws Exception {
        this(excelFileName, DEFAULT_SHEET_NAME);
    }

    /**
     *
     * @param excelFileName
     * @param sheetName
     * @throws Exception
     */
    public ExcelFileHandler(String excelFileName, String sheetName) {
        if (excelFileName == null) {
            throw new IllegalArgumentException("excelFileName may not be null");
        }
        if (sheetName == null) {
            throw new IllegalArgumentException("sheetName  may not be null");
        }
        this.excelFileName = excelFileName;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet(sheetName);
    }

    /**
     *
     * @param formatter
     */
    public void setFormatter(ExcelLogFormatter formatter) {
        super.setFormatter(formatter);
        formatter.setSheet(sheet);
    }

    /**
     *
     * @param record
     */
    @Override
    public void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        try {
            getFormatter().format(record);
        } catch (Exception ex) {
            reportError(null, ex, ErrorManager.FORMAT_FAILURE);
        }
    }

    /**
     *
     */
    @Override
    public void flush() {
        File file = new File(excelFileName);

        //.... Check if the base directory exists; create it if not
        file.getParentFile().mkdirs();

        //.... Write the data to the file now
        try (FileOutputStream stream = new FileOutputStream(file)) {
            workbook.write(stream);
            stream.flush();
        } catch (IOException ex) {
            Logger.getLogger(ExcelFileHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     *
     * @throws SecurityException
     */
    @Override
    public void close() throws SecurityException {
        try {
            flush();
            workbook.close();
        } catch (IOException ex) {
            Logger.getLogger(ExcelFileHandler.class.getName()).log(Level.SEVERE, "Could not close workbook properly", ex);
        }
    }
}
