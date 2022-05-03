import static spark.Spark.*;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.log4j.BasicConfigurator;

import org.json.simple.JSONObject;

import java.io.*;
import java.util.*;

public class Main {

    // ApachePOI - Excel reader
    // TODO: Make this work
    /*public static void getExcel() throws IOException {

        // Get files
        POIFSFileSystem inventoryFile = new POIFSFileSystem(new File("server/resources/Inventory.xlsx"));
        POIFSFileSystem distributorsFile = new POIFSFileSystem(new File("server/resources/Distributors.xlsx"));

        // Create workbook objects from files
        XSSFWorkbook inventoryWorkbook = new XSSFWorkbook(inventoryFile.getRoot(), true);
        XSSFWorkbook distributorsWorkbook = new XSSFWorkbook(distributorsFile.getRoot(), true);

        // Close files
        inventoryFile.close();
        distributorsFile.close();

        // Return
//        return (inventoryWorkbook, distributorsWorkbook);
    }*/

    public static void main(String[] args) throws IOException, InvalidFormatException {

        // Configure log4j
        BasicConfigurator.configure();

        //This is required to allow GET and POST requests with the header 'content-type'
        options("/*",
                (request, response) -> {
                    response.header("Access-Control-Allow-Headers",
                            "content-type");

                    response.header("Access-Control-Allow-Methods",
                            "GET, POST");

                    return "OK";
                });

        // Get the excel items, let's see if we can do that hear or if it needs to be a
        // Separate function

        // Get files
        OPCPackage inventoryFile = OPCPackage.open(new File("server/resources/Inventory.xlsx"));
        OPCPackage distributorsFile = OPCPackage.open(new File("server/resources/Distributors.xlsx"));

        // Create workbook objects from files
        XSSFWorkbook inventoryWorkbook = new XSSFWorkbook(inventoryFile);
        XSSFWorkbook distributorsWorkbook = new XSSFWorkbook(distributorsFile);

        // Close files
        inventoryFile.close();
        distributorsFile.close();

        //This is required to allow the React app to communicate with this API
        before((request, response) -> response.header("Access-Control-Allow-Origin", "http://localhost:3000"));

        // Iterate over sheets in workbook
        Iterator<Sheet> sheetIterator = inventoryWorkbook.sheetIterator();
        System.out.println("Retrieving Sheets using Iterator");
        while (sheetIterator.hasNext()) {
            Sheet sheet = sheetIterator.next();
            System.out.println("=> " + sheet.getSheetName());
        }

        // Get the first sheet
        Sheet inventorySheet = inventoryWorkbook.getSheetAt(0);

        // array
        // ArrayList<String> candiesToBuy = new ArrayList<String>();
        // JSON string:

        StringBuilder candiesToBuy = new StringBuilder("candiesToBuy = [\n");
//        JSONObject candiesToBuy = new JSONObject();

        // get 25% or less inv. candies
        // Start at 1 to skip first row
        for (int i = 1; i <= inventorySheet.getPhysicalNumberOfRows() - 1; i++) {

            // DEBUG: System.out.println("Row:" + i);

            Row row = inventorySheet.getRow(i);
            Cell candyNameCell = row.getCell(0);
            Cell currentStockCell = row.getCell(1);
            Cell capacityCell = row.getCell(2);

            DataFormatter formatter = new DataFormatter();

            float currentStock = Float.parseFloat(formatter.formatCellValue(currentStockCell));
            float capacity = Float.parseFloat(formatter.formatCellValue(capacityCell));

            if (currentStock/capacity < .25) {
                candiesToBuy.append("{name: \"").append(candyNameCell.getStringCellValue()).append("\"},\n");
            }
        }

        // Add to end of string
        candiesToBuy.append("];");

        // DEBUG: It's correct :)
        System.out.println(candiesToBuy);

        /*
         * Spark Stuff
         * TODO: Figure this stuff out then erase this comment
         * */

        //TODO: Return JSON containing the candies for which the stock is less than 25% of it's capacity
        // NOTE: path = website path, e.g. localhost:4567/low-stock
        get("/low-stock", (request, response) -> {

            // Convert to JSON
//            JSONObject candiesJSON = new JSONObject(candiesToBuy.toString());

            // Return the value
            return candiesToBuy.toString();

            // Establish JSON
            // Get the stock for each candy from Inventory excel
            // percentStockLeft = current stock/capacity
            // for each candy type:
            // if percent < 0.25:
            // add to JSON
            // return the JSON

//            return null;
        });

        //TODO: Return JSON containing the total cost of restocking candy
        post("/restock-cost", (request, response) -> {
            // for each candy type:
            // Calculate difference between capacity and current stock
            // BEFORE HAND have an excel with the best prices
            // Using a list of the best prices, map stockToBuy to candy type to price
            // Return: Sum all products of stockToBuy * bestPriceForThatCandy
            return null;
        });

    }
}
