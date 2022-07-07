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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ml.tools.ConnectionData;
import org.ml.tools.ConnectionManager;
import org.ml.tools.logging.LoggerFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import static org.apache.poi.ss.usermodel.CellType.NUMERIC;
import static org.apache.poi.ss.usermodel.CellType.STRING;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.ml.tools.FileType;

/**
 *
 * @author osboxes
 */
public class ExcelDatabaseHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelDatabaseHandler.class.getName());
    private ConnectionData connectionData;

    /**
     *
     * @param connectionData
     */
    public ExcelDatabaseHandler(ConnectionData connectionData) {
        if (connectionData == null) {
            throw new NullPointerException("connectionData may not be null");
        }
        this.connectionData = connectionData;
        ConnectionManager.init(connectionData);
    }

    /**
     *
     * @param tableNames
     * @return
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws SQLException
     * @throws InstantiationException
     */
    public Workbook loadExcelFromDatabase(Set<String> tableNames) throws ClassNotFoundException, IllegalAccessException, SQLException, InstantiationException {
        if (tableNames == null) {
            throw new NullPointerException("tableNames may not be null");
        }

        Connection connection = ConnectionManager.getInstance().getConnection();
        DatabaseMetaData metadata = connection.getMetaData();

        //.... Collect tablenames in DB
        String[] names = {"TABLE"};   // Could also use null instead here as argument
        ResultSet rs = metadata.getTables(null, null, null, names);
        Set<String> databaseTableNames = new HashSet<>();
        while (rs.next()) {
            databaseTableNames.add(rs.getString("TABLE_NAME"));
        }

        //.... Process each table into a separate sheet
        Workbook workbook = ExcelTools.getNewWorkbook(FileType.XLSX);
        for (String tableName : tableNames) {
            if (databaseTableNames.contains(tableName)) {
                addSheet(workbook, tableName, metadata, connection);
            } else {
                LOGGER.log(Level.SEVERE, "Database does not contain a table named {0}", tableName);
            }
        }

        return workbook;

    }

    /**
     *
     * @param tableNames
     * @return
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws SQLException
     * @throws InstantiationException
     */
    public Map<String, Workbook> loadExcelsFromDatabase(Set<String> tableNames) throws ClassNotFoundException, IllegalAccessException, SQLException, InstantiationException {
        if (tableNames == null) {
            throw new NullPointerException("tableNames may not be null");
        }

        Connection connection = ConnectionManager.getInstance().getConnection();
        DatabaseMetaData metadata = connection.getMetaData();

        //.... Collect tablenames in DB
        String[] names = {"TABLE"};   // Could also use null instead here as argument
        ResultSet rs = metadata.getTables(null, null, null, names);
        Set<String> databaseTableNames = new HashSet<>();
        while (rs.next()) {
            databaseTableNames.add(rs.getString("TABLE_NAME"));
        }

        //.... Process each table into a separate workbook
        Map<String, Workbook> workbooks = new HashMap<>();
        for (String tableName : tableNames) {
            Workbook workbook = ExcelTools.getNewWorkbook(FileType.XLSX);
            if (databaseTableNames.contains(tableName)) {
                addSheet(workbook, tableName, metadata, connection);
                workbooks.put(tableName, workbook);
            } else {
                LOGGER.log(Level.SEVERE, "Database does not contain a table named {0}", tableName);
            }
        }

        return workbooks;

    }

    /**
     * Read one table into a new sheet of the given workbook
     *
     * @param workbook
     * @param tableName
     * @param metadata
     * @param connection
     * @throws SQLException
     */
    private void addSheet(Workbook workbook, String tableName, DatabaseMetaData metadata, Connection connection) throws SQLException {
        if (workbook == null) {
            throw new NullPointerException("workbook may not be null");
        }
        if (tableName == null) {
            throw new NullPointerException("tableName may not be null");
        }
        if (metadata == null) {
            throw new NullPointerException("metadata may not be null");
        }
        if (connection == null) {
            throw new NullPointerException("connection may not be null");
        }

        Sheet sheet = workbook.createSheet(tableName);
        Map<String, CellType> columnTypes = new HashMap<>();
        List<String> columnNames = new ArrayList<>();

        //.... Header row
        int r = 0;
        int c = 0;
        Row row = sheet.createRow(r++);
        ResultSet rs = metadata.getColumns(null, null, tableName, null);
        while (rs.next()) {
            String columnName = rs.getString("COLUMN_NAME");
            columnNames.add(columnName);
            Cell cell = row.createCell(c++);
            cell.setCellValue(columnName);
            switch (rs.getString("TYPE_NAME")) {
                case "VARCHAR":
                    columnTypes.put(columnName, STRING);
                    break;
                case "DOUBLE":
                    columnTypes.put(columnName, CellType.NUMERIC);
                    break;
                default:
                    columnTypes.put(columnName, CellType.STRING);
            }
        }

        //.... Data rows
        rs = connection.createStatement().executeQuery("SELECT * FROM " + tableName);
        while (rs.next()) {
            row = sheet.createRow(r++);
            c = 0;
            for (String columnName : columnNames) {
                Cell cell = row.createCell(c++);
                switch (columnTypes.get(columnName)) {
                    case NUMERIC:
                        cell.setCellValue(rs.getDouble(columnName));
                        break;
                    case STRING:
                        cell.setCellValue(rs.getString(columnName));
                        break;
                    default:
                        cell.setCellValue(rs.getString(columnName));
                }
            }
        }
    }

    /**
     * This can be used to write the entire data of an Excel sheet to a database
     * table. The sheet is assumed to have a rectangular shape, i. e. first row
     * is the header with the column names and all subsequent rows are data
     * rows. There are no merged cells or other data outside the rectangular
     * shape.
     *
     * It tries to determine the DB types It does not support Apache POI
     * CellType yet ... one could derive information from a first data row and
     * then go for e. g. VARCHAR or INTEGER column types in the table, but this
     * makes some additional assumptions (which data row to look at etc.) such
     * that for simplicity, right now all cells are interpreted as strings.
     *
     * Note that the table specified is reserved exclusively for this method's
     * use, i. e. if it exists, it is first deleted and then recreated!
     *
     * @param sheet
     * @param tableName
     * @param varcharLength
     * @throws java.sql.SQLException
     * @throws java.lang.ClassNotFoundException
     * @throws java.lang.IllegalAccessException
     * @throws java.lang.InstantiationException
     */
    public void saveExcelSheetToDatabase(Sheet sheet, String tableName, int varcharLength) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (sheet == null) {
            throw new NullPointerException("sheet may not be null");
        }
        if (tableName == null) {
            throw new NullPointerException("tableName may not be null");
        }
        if (varcharLength <= 0) {
            throw new IllegalArgumentException("varcharLength must be > 0");
        }
        if (sheet.getRow(sheet.getFirstRowNum()) == null) {
            throw new UnsupportedOperationException("Sheet does not contain any data, not even a header row");
        }

        Connection connection = ConnectionManager.getInstance().getConnection();

        //.... Find all tables that exist and delete the one named 'tableName' if it exists
        DatabaseMetaData metadata = connection.getMetaData();
        String[] names = {"TABLE"};
        ResultSet rs = metadata.getTables(null, null, null, names);
        while (rs.next()) {
            if (rs.getString("TABLE_NAME").equals(tableName)) {
                LOGGER.log(Level.INFO, "Dropping table {0}", tableName);
                connection.createStatement().executeUpdate("DROP TABLE " + tableName);
                break;
            }
        }

        //.... Collect the column names
        Row row = sheet.getRow(sheet.getFirstRowNum());
        int startCol = row.getFirstCellNum();
        int endCol = row.getLastCellNum();
        String[] columnNames = new String[endCol - startCol];

        int i = 0;
        for (int c = startCol; c < endCol; c++) {
            String columnName = row.getCell(c).getStringCellValue().trim().replaceAll("\\s+", "_").replaceAll("[-\\)\\(/#.]", "_");
            columnNames[i++] = columnName.toUpperCase();
        }

        //.... Check if the sheet contains at least one data row - we can use that to determine column types
        row = sheet.getRow(sheet.getFirstRowNum() + 1);
        CellType[] cellTypes = new CellType[endCol - startCol];
        i = 0;
        boolean hasData = false;
        if (row != null) {
            hasData = true;
            for (int c = startCol; c < endCol; c++) {
                if (row.getCell(c) != null) {
                    cellTypes[i++] = row.getCell(c).getCellType();
                } else {
                    cellTypes[i++] = CellType.STRING;
                }
            }
        } else {
            for (int c = startCol; c < endCol; c++) {
                cellTypes[i++] = CellType.STRING;
            }
        }

        //.... Create the table
        StringBuilder sb = new StringBuilder(500);
        sb.append("CREATE TABLE ");
        sb.append(tableName);
        sb.append(" (");
        for (int k = 0; k < columnNames.length; k++) {
            //         sb.append("'");
            sb.append(columnNames[k]);
            //       sb.append("'");
            switch (cellTypes[k]) {
                case STRING:
                    sb.append(" VARCHAR(");
                    sb.append(varcharLength);
                    sb.append("),");
                    break;
                case NUMERIC:
                    sb.append(" DOUBLE,");
                    break;
                default:
                    sb.append(" VARCHAR(");
                    sb.append(varcharLength);
                    sb.append("),");
            }
        }
        sb.setLength(sb.length() - 1);
        sb.append(")");
        LOGGER.log(Level.INFO, "Creating table {0}", tableName);
        System.out.println(sb.toString());
        connection.createStatement().executeUpdate(sb.toString());

        //.... Now store the table data
        sb = new StringBuilder(500);
        sb.append("INSERT INTO ");
        sb.append(tableName);
        sb.append(" (");
        for (String columnName : columnNames) {
            sb.append(columnName);
            sb.append(",");
        }
        sb.setLength(sb.length() - 1);
        sb.append(") VALUES (");
        for (int k = 0; k < columnNames.length; k++) {
            sb.append("?,");
        }
        sb.setLength(sb.length() - 1);
        sb.append(")");

        PreparedStatement insertStatement = ConnectionManager.getInstance().getStatement(sb.toString());

        for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
            i = 1;
            int k = 0;
            row = sheet.getRow(r);
            for (int c = startCol; c < endCol; c++) {
                if (row.getCell(c) != null) {
                    System.out.println("xx " + row.getCell(c));
                    switch (cellTypes[k++]) {
                        case FORMULA:
                            insertStatement.setDouble(i++, row.getCell(c).getNumericCellValue());
                            break;
                        case NUMERIC:
                            insertStatement.setDouble(i++, row.getCell(c).getNumericCellValue());
                            break;
                        case STRING:
                            insertStatement.setString(i++, row.getCell(c).getStringCellValue());
                            break;
                        default:
                            insertStatement.setString(i++, row.getCell(c).getStringCellValue());

                    }
                } else {
                    insertStatement.setString(i++, "");
                }
            }
            insertStatement.executeUpdate();
        }
    }

    /**
     * Uses a default VARCHAR length setting of 200
     *
     * @param sheet
     * @param tableName
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public void saveExcelSheetToDatabase(Sheet sheet, String tableName) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        saveExcelSheetToDatabase(sheet, tableName, 200);
    }

}
