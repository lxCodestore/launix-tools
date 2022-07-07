/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ml.tools.excel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ml.options.Options;
import org.ml.tools.ConnectionData;
import org.ml.tools.PropertyManager;
import org.ml.tools.logging.LoggerFactory;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author Dr. Matthias Laux
 */
public class PopulateDatabase {

    private final static Logger LOGGER = LoggerFactory.getLogger(PopulateDatabase.class.getName());

    /**
     *
     */
    private enum Key {
        excelFile, sheetName
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {

            Options options = new Options(args);
            options.setDefault(3, 4);
            options.getSet().setDataText(0, "XML Config File");
            options.getSet().setHelpText(0, "The XML config file with the details for the dtaabase connection");
            options.getSet().setDataText(1, "Excel file");
            options.getSet().setHelpText(1, "The Excel file to read the data from");
            options.getSet().setDataText(2, "Table Name");
            options.getSet().setHelpText(2, "The name of the table to create");
            options.getSet().setDataText(3, "Sheet Name");
            options.getSet().setHelpText(3, "The name of the sheet to use in the Excel workbook (if none given, the first sheet is taken)");

            if (options.check()) {

                File inputFile = new File(options.getSet().getData(0));
                SAXBuilder builder = new SAXBuilder();
                Document doc = builder.build(new BufferedReader(new FileReader(inputFile)));
                PopulateDatabase client = new PopulateDatabase();

                Path excelFile = Paths.get(options.getSet().getData(1));
                String tableName = options.getSet().getData(2);

                if (options.getSet().getDataCount() == 4) {
                    client.execute(new PropertyManager(doc.getRootElement()), excelFile, tableName, options.getSet().getData(3));
                } else {
                    client.execute(new PropertyManager(doc.getRootElement()), excelFile, tableName, null);
                }

            } else {
                LOGGER.log(Level.SEVERE, options.getCheckErrors());
                options.printHelp("Usage:", false, true);
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }

    }

    /**
     *
     * @param propertyManager
     * @param excelFile
     * @param tableName
     * @param sheetName
     * @throws Exception
     */
    public void execute(PropertyManager propertyManager, Path excelFile, String tableName, String sheetName) throws Exception {
        ConnectionData connectionData = new ConnectionData(propertyManager);
        Workbook workbook = ExcelTools.getWorkbook(excelFile);
        Sheet sheet;
        if (sheetName == null) {
            sheet = workbook.getSheetAt(0);
        } else {
            sheet = workbook.getSheet(sheetName);
        }
        ExcelDatabaseHandler handler = new ExcelDatabaseHandler(connectionData);
        handler.saveExcelSheetToDatabase(sheet, tableName);
    }
}
