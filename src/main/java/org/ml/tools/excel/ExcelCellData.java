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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.ml.tools.DataType;
import org.ml.tools.logging.LoggerFactory;

import java.util.logging.Logger;

/**
 *
 * @author mlaux
 */
public class ExcelCellData {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelCellData.class.getName());
    public static final ExcelCellData EMPTY_CELL = new ExcelCellData();
//    public static final String ERROR_CONDITION_NUMBER_FORMAT = "ERROR-NUMBER-FORMAT";
//    public static final String ERROR_CONDITION_ILLEGAL_STATE = "ERROR-ILLEGAL-STATE";
//    public static final double ERROR_CONDITION_VALUE = Double.NEGATIVE_INFINITY;
    private Cell cell;
    private boolean emptyCell = true;
    private final DataFormatter dataFormatter = new DataFormatter();

    /**
     *
     * @param cell
     */
    public ExcelCellData(Cell cell) {
        if (cell == null) {
            throw new NullPointerException("cell may not be null");
        }
        this.cell = cell;
        this.emptyCell = false;
    }

    /**
     * Just for internal use to create an empty cell
     */
    private ExcelCellData() {
    }

    /**
     * Make a best effort to extract a Comparable object from the cell data
     *
     * @return
     */
    public Comparable getValue() {
        throw new UnsupportedOperationException("Not yet implemented");
//        rawData = dataFormatter.formatCellValue(cell).trim();
//        return null;
    }

    /**
     * Make a best effort to extract a String object from the cell data
     *
     * @return
     */
    public String getProcessedData() {
        if (isEmptyCell()) {
            return (String) DataType.TypeString.getDefaultValue();
        }
        switch (cell.getCellType()) {
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:      // One of CellType.NUMERIC, CellType.STRING, CellType.BOOLEAN, CellType.ERROR
                int a1 = 0;
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (IllegalStateException ex1) {
                    try {
                        return String.valueOf(cell.getBooleanCellValue());
                    } catch (IllegalStateException ex2) {
                        return cell.getStringCellValue();
                    }
                }
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BLANK:
            case STRING:
                return cell.getStringCellValue();
            case ERROR:
            case _NONE:
                throw new UnsupportedOperationException("Unsupported POI cell type: " + cell.getCellType());
            default:
                throw new UnsupportedOperationException("Unknown POI cell type: " + cell.getCellType());
        }
    }

    /**
     *
     * @return
     */
    public String getRawData() {
        if (isEmptyCell()) {
            return (String) DataType.TypeString.getDefaultValue();
        } else {
            return dataFormatter.formatCellValue(cell).trim();
        }
    }

    /**
     *
     * @return
     */
    public double getNumberData() {
        if (isEmptyCell()) {
            return (Double) DataType.TypeDouble.getDefaultValue();
        }

        switch (cell.getCellType()) {
            case STRING:
                int a0 = 0;   // Dummy for correct Netbeans formatting
                try {
                    return Double.valueOf(cell.getStringCellValue());
                } catch (NumberFormatException ex) {
                    return (Double) DataType.TypeDouble.getDefaultValue();
                }
            case NUMERIC:
                return cell.getNumericCellValue();
            case FORMULA:      // One of CellType.NUMERIC, CellType.STRING, CellType.BOOLEAN, CellType.ERROR
                int a1 = 0;   // Dummy for correct Netbeans formatting
                try {
                    return cell.getNumericCellValue();
                } catch (IllegalStateException ex1) {
                    try {
                        return Double.valueOf(cell.getStringCellValue());
                    } catch (IllegalStateException ex2) {
                        return (Double) DataType.TypeDouble.getDefaultValue();
                    }
                }
            case BOOLEAN:
            case BLANK:
                return (Double) DataType.TypeDouble.getDefaultValue();
            case ERROR:
            case _NONE:
                throw new UnsupportedOperationException("Unsupported POI cell type: " + cell.getCellType());
            default:
                throw new UnsupportedOperationException("Unknown POI cell type: " + cell.getCellType());
        }

    }

    /**
     * Try to extract a Comparable object of the target DataType from the cell
     * data
     *
     * @param targetDataType
     * @return
     */
    public Comparable getValue(DataType targetDataType) {
        if (targetDataType == null) {
            throw new NullPointerException("targetDataType may not be null");
        }

        //.... We have no data, therefore we return the default
        if (isEmptyCell()) {
            return targetDataType.getDefaultValue();
        }

        String rawData = dataFormatter.formatCellValue(cell).trim();
        CellType cellType = cell.getCellType();
        if (cell.getCellType().equals(CellType.FORMULA)) {
            cellType = cell.getCachedFormulaResultType();   // One of CellType.NUMERIC, CellType.STRING, CellType.BOOLEAN, CellType.ERROR
        }

        switch (cellType) {

            case NUMERIC:

                switch (targetDataType) {
                    case TypeDouble:
                        return (Double) cell.getNumericCellValue();
                    case TypeDoublePercentage:
                        return (Double) (100.0d * cell.getNumericCellValue());
                    case TypeInteger:
                        return (int) cell.getNumericCellValue();
                    case TypeIntegerPercentage:
                        return  (int)Math.round(100.0d * cell.getNumericCellValue());
                    case TypeString:
                        return String.valueOf(cell.getNumericCellValue());
                    default:
                        throw new UnsupportedOperationException("Can not extract " + targetDataType + " from raw data " + rawData);
                }

            case BOOLEAN:

                switch (targetDataType) {
                    case TypeBoolean:
                        return (Boolean) cell.getBooleanCellValue();
                    case TypeString:
                        return String.valueOf(cell.getBooleanCellValue());
                    case TypeInteger:
                        return cell.getBooleanCellValue() ? 1 : 0;
                    default:
                        throw new UnsupportedOperationException("Can not extract " + targetDataType + " from raw data " + rawData);
                }

            case BLANK:

                return targetDataType.getDefaultValue();

            case STRING:

                String stringValue = "";
                try {
                     stringValue = cell.getStringCellValue().trim();
                } catch (Exception exp) {
                    System.out.println(exp.getMessage());
                    exp.printStackTrace();
                    System.exit(2);
                }
                switch (targetDataType) {
                    case TypeDouble:
                        int a1 = 0;   // Dummy for correct Netbeans formatting
                        try {
                            return Double.valueOf(stringValue);
                        } catch (NumberFormatException ex) {
                            return targetDataType.getDefaultValue();
                        }
                    case TypeDoublePercentage:
                        int a2 = 0;   // Dummy for correct Netbeans formatting
                        try {
                            return 100.d * Double.valueOf(stringValue);
                        } catch (NumberFormatException ex) {
                            return targetDataType.getDefaultValue();
                        }
                    case TypeInteger:
                        int a3 = 0;   // Dummy for correct Netbeans formatting
                        try {
                            return Integer.valueOf(stringValue);
                        } catch (NumberFormatException ex) {
                            return targetDataType.getDefaultValue();
                        }
                    case TypeIntegerPercentage:
                        int a4 = 0;   // Dummy for correct Netbeans formatting
                        try {
                            return 100 * Integer.valueOf(stringValue);
                        } catch (NumberFormatException ex) {
                            return targetDataType.getDefaultValue();
                        }
                    case TypeBoolean:
                        int a5 = 0;   // Dummy for correct Netbeans formatting
                        try {
                            return Boolean.valueOf(stringValue);
                        } catch (NumberFormatException ex) {
                            return targetDataType.getDefaultValue();
                        }
                    case TypeEmail:
                    case TypeString:
                        return stringValue;
                    case TypeUndefined:
                    default:
                        throw new UnsupportedOperationException("Can not extract " + targetDataType + " from raw data " + rawData);
                }

            case ERROR:

            case _NONE:

                throw new UnsupportedOperationException("Unsupported POI cell type: " + cellType);

            default:

                throw new UnsupportedOperationException("Unknown POI cell type: " + cellType);

        }
    }

    /**
     *
     * @return
     */
    public boolean isEmptyCell() {
        return emptyCell;
    }
}
