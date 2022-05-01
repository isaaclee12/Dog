import static spark.Spark.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.log4j.BasicConfigurator;

import java.io.IOException;
import java.io.File;

public class Main {

    // ApachePOI - Excel reader
    // TODO: Make this work
    public static void getExcel() throws IOException {
        POIFSFileSystem fs = new POIFSFileSystem(new File("server/resources/Distributors.xlsx"));
        HSSFWorkbook wb = new HSSFWorkbook(fs.getRoot(), true);
        fs.close();
    }

    public static void main(String[] args) {

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

        //This is required to allow the React app to communicate with this API
        before((request, response) -> response.header("Access-Control-Allow-Origin", "http://localhost:3000"));

        /*
         * Spark Stuff
         * TODO: Figure this stuff out then erase this comment
         * */

        //TODO: Return JSON containing the candies for which the stock is less than 25% of it's capacity
        get("/low-stock", (request, response) -> {
            // Pseudo code:
            // Establish JSON
            // Get the stock for each candy from Inventory excel
            // percentStockLeft = current stock/capacity
            // for each candy type:
            // if percent < 0.25:
            // add to JSON
            // return the JSON
            return null;
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
