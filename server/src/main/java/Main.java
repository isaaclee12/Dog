import static spark.Spark.*;

import jdk.nashorn.internal.parser.JSONParser;
import junit.framework.Assert;
import org.apache.avro.data.Json;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.log4j.BasicConfigurator;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

class Candy {
    @Expose
    protected int SKU;

    @Expose
    protected int amountToOrder;

    @Override
    public String toString() {
        return "Candy => [" + this.SKU + this.amountToOrder + "]";
    }
}

class CandyToRestock {
    @Expose
    protected List<Candy> candyList; // = new List<Candy>()

    public List<Candy> getCandyList() {
        return candyList;
    }

    public void setCandyList(List<Candy> candyList) {
        this.candyList = candyList;
    }

    @Override
    public String toString() {
        return "CandyToRestock [candylist=" + candyList + "]";
    }
}

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

        // Get the first sheet
        Sheet inventorySheet = inventoryWorkbook.getSheetAt(0);

        // array
        // List<String> candiesToBuy = new List<String>();
        // JSON string:

        // format: [{0:"data"}, {1:"data"}, {name:data}, {name:data}, {name:data},];
        StringBuilder candiesToBuy = new StringBuilder("[");

        // get 25% or less inv. candies
        // Start at 1 to skip first row
        int n = inventorySheet.getPhysicalNumberOfRows() - 1;

        // index for JSON
        int JSONindex = 0;
        for (int i = 1; i <= n; i++) {

            // DEBUG: System.out.println("Row:" + i);

            Row row = inventorySheet.getRow(i);
            Cell candySKUCell = row.getCell(3);
            Cell candyNameCell = row.getCell(0);
            Cell currentStockCell = row.getCell(1);
            Cell capacityCell = row.getCell(2);

            DataFormatter formatter = new DataFormatter();

            float currentStock = Float.parseFloat(formatter.formatCellValue(currentStockCell));
            float capacity = Float.parseFloat(formatter.formatCellValue(capacityCell));

            if (currentStock/capacity < .25) {
                // append json string
                candiesToBuy
                        .append("{\"SKU\":\"").append(formatter.formatCellValue(candySKUCell)).append("\",")
                        .append("\"name\":\"").append(candyNameCell.getStringCellValue()).append("\",")
                        .append("\"stock\":\"").append(formatter.formatCellValue(currentStockCell)).append("\",")
                        .append("\"capacity\":\"").append(formatter.formatCellValue(capacityCell)).append("\"}");

                // iterate JSON index
                JSONindex++;

                // Add comma if not last row
                if (i != n) {
                    candiesToBuy.append(",");
                }
            }
        }

        // Add to end of string
        candiesToBuy.append("]");

        // DEBUG: It's correct :)
        System.out.println(candiesToBuy);

        // Returns JSON containing the candies for which the stock is less than 25% of it's capacity
        // NOTE: path = website path, e.g. localhost:4567/low-stock
        get("/low-stock", (request, response) -> {

            // Debug
            System.out.println("Executing low-stock" + request.body());

            // Return the value
            return candiesToBuy.toString();
        });

        // Returns JSON containing the total cost of restocking candy
        post("/restock-cost", (request, response) -> {

            // Debug
            System.out.println("Executing restock-cost");

            // This gets the data sent from js
            String data = request.body();
            System.out.println(data);

            // JSONSimple's parser failed to work, we are using Gson instead.
            try {

                // Create a list of all the candies
                Gson g = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                Type candyMapType = new TypeToken<Map<String, Candy>>() {}.getType();
                Map<String, Candy> nameEmployeeMap = g.fromJson(data, candyMapType);
                System.out.println("MAP: " + nameEmployeeMap);
                System.out.println("MAP NAME TEST: " + nameEmployeeMap.get("name"));
            } catch (Error e) {
                System.out.println(e.toString());
            }

            // Iterate over sheets in workbook
            Iterator<Sheet> sheetIterator = distributorsWorkbook.sheetIterator();
            System.out.println("Retrieving distributor Sheets using Iterator");
            while (sheetIterator.hasNext()) {
                Sheet sheet = sheetIterator.next();
                System.out.println("=> " + sheet.getSheetName());
            }



            // return something
            return null;

            // Old pseudocode:
            // get following data from input field in html:
                // get list of candy names
                // get desired amount of each candy

            // Establish dict of items:

            // For each sheet in workbook:
                // For each line in sheet after 1st line:
                    // If in list of requested candy names:
                        // If not in array already, add it in w/ the data
                        // If already in the array, compare the two's prices
                            // If the new price is lower, replace it
                            // Else, ignore it (no code needed)
                    // Else, ignore it (no code needed)

            // For each line in array:
                // Multiply best prices buy desired amount of each candy, and attach type + distributor
                // Append string with that data

            // return string
            //return null;
        });

    }
}
