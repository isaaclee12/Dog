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

class DistributorCandyPrice {
    protected String distributorName;
    protected String candyName;
    protected int id;
    protected float cost;

    public DistributorCandyPrice(String distributorName, String candyName, int id, float cost) {
        this.distributorName = distributorName;
        this.candyName = candyName;
        this.id = id;
        this.cost = cost;
    }

    public String getDistributorName() {
        return distributorName;
    }

    public String getCandyName() {
        return candyName;
    }

    public int getId() {
        return id;
    }

    public float getCost() {
        return cost;
    }

    public String toString() {
        return "Distributor => [" + this.distributorName + "," + this.candyName + "," + this.id + "," + this.cost + "]"; // removed: this.SKU +
    }
}

class Candy {
/*    @Expose
    protected int SKU;*/
    // Note: Name is in the map

    @Expose
    protected int amountToOrder;

    public int getAmountToOrder() {
        return amountToOrder;
    }

    @Override
    public String toString() {
        return "Candy => [" + this.amountToOrder + "]"; // removed: this.SKU +
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
    public static void main(String[] args) throws IOException, InvalidFormatException, NullPointerException {

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


        /*
        * Pre-load data from excel
        * */
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

        // Establish jsonString
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
        System.out.println("Low Stock Cost Candies, pre-loaded:" + candiesToBuy);


        /*
        * Get low stock items
        * */
        // Returns JSON containing the candies for which the stock is less than 25% of it's capacity
        // NOTE: path = website path, e.g. localhost:4567/low-stock
        get("/low-stock", (request, response) -> {

            // Debug
            System.out.println("Executing low-stock" + request.body());

            // Return the value
            return candiesToBuy.toString();
        });


        /*
        * Determines best cost for candy requested
        * */
        // Returns int containing the total cost of restocking candy
        post("/restock-cost", (request, response) -> {

            // Debug
            System.out.println("Executing restock-cost");

            // This gets the data sent from js
            String data = request.body();

            // JSONSimple's parser failed to work, we are using Gson instead.
            // Create a list of all the candies, map them.
            Gson g = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            Type candyMapType = new TypeToken<Map<String, Candy>>() {}.getType();
            Map<String, Candy> candyListMap = g.fromJson(data, candyMapType);

            // Turn candy-map names & respective amounts requested into arraylists for later use
            ArrayList<String> requestedCandyNames = new ArrayList<>(candyListMap.keySet());

            // Establish some vars
            String distributorName;
            ArrayList<DistributorCandyPrice> bestDistributorPrices = new ArrayList<>();
            ArrayList<String> listNamesScanned = new ArrayList<>();

            // Iterate over sheets in workbook
            Iterator<Sheet> sheetIterator = distributorsWorkbook.sheetIterator();
            while (sheetIterator.hasNext()) {

                // Get the next sheet
                Sheet currentSheet = sheetIterator.next();

                // get distributor name
                distributorName = currentSheet.getSheetName();

                // get num rows
                int rows = currentSheet.getPhysicalNumberOfRows() - 1;

                // For each line in current sheet after 1st line:
                for (int i = 1; i <= rows; i++) {

                    // Get rows from sheet
                    Row row = currentSheet.getRow(i);

                    // Get cells from row
                    Cell candyNameCell = row.getCell(0);
                    Cell IDCell = row.getCell(1);
                    Cell costCell = row.getCell(2);

                    // Establish cell to data auto-formatter
                    DataFormatter formatter = new DataFormatter();

                    // Get data from cells, formatted
                    String candyName = formatter.formatCellValue(candyNameCell);
                    int id = Integer.parseInt(formatter.formatCellValue(IDCell));
                    float cost = Float.parseFloat(formatter.formatCellValue(costCell));

                    // If in list of requested candy names:
                    if (requestedCandyNames.contains(candyName)){

                        // Make new data obj
                        DistributorCandyPrice newData = new DistributorCandyPrice(distributorName, candyName, id, cost);

                        // If not in array already, add it in w/ the data
                        if (!listNamesScanned.contains(candyName)) {
                            listNamesScanned.add(candyName);


                            // System.out.println("Item not in array");

                            // Add to array
                            bestDistributorPrices.add(newData);
                        }
                        // If already in the array, compare the two's prices
                        else {

                            // If array bestDistributorPrices has a DistributorCandyPrice with the same name as the current DistributorCandyPrice newData object
                            for (DistributorCandyPrice candyPrice : bestDistributorPrices) {
                                if (candyPrice.getCandyName().equals(newData.getCandyName())) {

                                    // If the new price is lower, replace it
                                    if (newData.cost < candyPrice.getCost()) {
                                        bestDistributorPrices.remove(candyPrice);
                                        bestDistributorPrices.add(newData);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            // sum for prices
            float sum = 0;

            // Get prices
            for (DistributorCandyPrice candyPrice : bestDistributorPrices) {
                int amountReq = 0;

                // Get amount ordered respective to the candy's name
                for (Map.Entry<String, Candy> pair : candyListMap.entrySet()) {
                    if (pair.getKey().equals(candyPrice.getCandyName())) { // key = name here
                        amountReq = pair.getValue().getAmountToOrder(); // Get amount to order from data
                        break; // Once we got it, we exit the loop
                    }
                }

                //candyListMap
                sum += candyPrice.cost * amountReq;
            }

            // Print for debug help
            System.out.println("Final Cost: " + sum);

            // return it.
            return sum;
        });

    }
}
