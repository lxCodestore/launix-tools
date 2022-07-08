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
package org.ml.tools.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.ml.tools.FileType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author osboxes
 */
public class ExcelTools {

    /**
     * @param type
     * @return
     */
    public static Workbook getNewWorkbook(FileType type) {
        if (type == null) {
            throw new NullPointerException("type may not be null");
        }
        switch (type) {
            case XLS:
                return new HSSFWorkbook();
            case XLSX:
                return new XSSFWorkbook();
            default:
                throw new UnsupportedOperationException("Unknown / unsupported file type: " + type.toString());
        }
    }

    /**
     * @param path
     * @return
     */
    public static Workbook getNewWorkbook(Path path) {
        if (path == null) {
            throw new NullPointerException("path may not be null");
        }
        File file = path.toFile();
        Workbook workbook;
        if (file.getName().endsWith(".xls")) {
            workbook = new HSSFWorkbook();
        } else if (file.getName().endsWith(".xlsx")) {
            workbook = new XSSFWorkbook();
        } else {
            throw new UnsupportedOperationException("Unknown / unsupported file type: " + file);
        }
        return workbook;
    }

    /**
     * @param path
     * @return
     */
    public static Workbook getWorkbook(Path path) {
        if (path == null) {
            throw new NullPointerException("path may not be null");
        }
        File file = path.toFile();
        Workbook workbook;
        if (file.exists()) {

            try {
                if (file.getName().endsWith(".xls")) {
                    workbook = new HSSFWorkbook(new FileInputStream(file));
                } else if (file.getName().endsWith(".xlsx")) {
                    OPCPackage workbookPackage = OPCPackage.open(file);
                    workbook = new XSSFWorkbook(workbookPackage);
                } else {
                    throw new UnsupportedOperationException("Unknown / unsupported file type: " + file);
                }
            } catch (IOException | InvalidFormatException ex) {
                workbook = getNewWorkbook(path);
            }

        } else {
            workbook = getNewWorkbook(path);

        }
        return workbook;
    }

    /**
     * Returns a list of lists (one for reach row) of a parsed sheet. The idea is that this can be used if Excel sheets contain for example
     * configuration data which is cleanly parsed into strings for further processing afterwards.
     * <p>
     * Completely empty rows are skipped
     * <p>
     * Rows where the first cell starts with a string starting with a '#' are also skipped as comments
     *
     * @param sheet
     * @return
     */
    public static List<List<String>> getParsedSheet(Sheet sheet) {
        if (sheet == null) {
            throw new NullPointerException("sheet may not be null");
        }

        List<List<String>> result = new ArrayList<>();

        //.... Search for the lowest start cell number such that we get a real matrix as the result as all lists start in the column
        int minColNumber = Integer.MAX_VALUE;
        for (int r = sheet.getFirstRowNum(); r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row != null) {
                int icol = row.getFirstCellNum();
                if (icol < minColNumber) {
                    minColNumber = icol;
                }
            }
        }

        //.... Now parse all the rows
        for (int r = sheet.getFirstRowNum(); r <= sheet.getLastRowNum(); r++) {

            Row row = sheet.getRow(r);

            if (row != null) {

                List<String> rowData = new ArrayList<>();
                boolean foundValidData = false;
                int lastColumnWithValidData = 0;

                for (int icol = minColNumber; icol <= row.getLastCellNum(); icol++) {

                    if (row.getCell(icol) != null) {

                        ExcelCellData exc = new ExcelCellData(row.getCell(icol));
                        String data = exc.getProcessedData().trim();
                        rowData.add(data);
                        if (data.length() > 0) {
                            //.... Abort here if first cell starts with a #
                            if (data.startsWith("#") && icol == minColNumber) {
                                break;
                            }
                            foundValidData = true;
                            lastColumnWithValidData = icol;
                        }
                    } else {
                        rowData.add("");
                    }

                }

                if (foundValidData) {
                    //.... Need to remove any trailing blanks / empty strings
                    result.add(rowData.subList(0, lastColumnWithValidData + 1));
                }
            }
        }
        return result;
    }

    /**
     * Different approach - this already does some filtering like any row starting with a cell with a # at the beginning
     * or rows that start with an empty cell
     *
     * @param sheet
     * @return
     */
    public static List<List<String>> getParsedSheet2(Sheet sheet) {
        if (sheet == null) {
            throw new NullPointerException("sheet may not be null");
        }

        List<List<String>> result = new ArrayList<>();

        for (int r = sheet.getFirstRowNum(); r <= sheet.getLastRowNum(); r++) {

            Row row = sheet.getRow(r);

            if (row != null) {

                int icol = row.getFirstCellNum();

                //.... First cell needs to contain data else everything after is skipped as well
                if (row.getCell(icol) != null) {

                    ExcelCellData exc = new ExcelCellData(row.getCell(icol++));
                    String data = exc.getProcessedData().trim();

                    //.... We also skip comment rows where the first cell starts with a #
                    if (data.length() > 0 && !data.startsWith("#")) {

                        List<String> rowData = new ArrayList<>();
                        result.add(rowData);
                        rowData.add(data);

                        //.... Add rest of the row
                        for (int c = icol; c <= row.getLastCellNum(); c++) {

                            if (row.getCell(c) != null) {
                                exc = new ExcelCellData(row.getCell(c));
                                data = exc.getProcessedData().trim();
                                if (data.length() > 0) {
                                    rowData.add(data);
                                }
                            }

                        }

                    }
                }
            }
        }
        return result;
    }
}
