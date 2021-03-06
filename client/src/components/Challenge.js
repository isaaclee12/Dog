import React from "react";

// JSON DATA OBJ
let data = [];

// Helper function for xjr get requests
function httpGet(url) {
    let xmlHttpReq = new XMLHttpRequest();
    xmlHttpReq.open("GET", url, false);
    xmlHttpReq.send(null);
    return xmlHttpReq.responseText;
}

// Event handlers
export const findLowStockItemsHandler = () => {
    document.getElementById("table").style.display = "inline";
}

export const reOrderHandler = () => {
    // Get order amounts from inputs
    let restockInputs = document.getElementsByClassName("restock-input");

    // VALIDATE INPUT DATA
    for (let i = 0; i <= restockInputs.length - 1; i++) {
        let restockValue = restockInputs[i].value;

        // If there is a value to restock AND the restock amount exceeds diff. of capacity and stock
        if (restockValue !== null && restockValue > (data[i].capacity - data[i].stock)) {
            console.log("Error: Amount requested would cause stock to exceed capacity.");
            document.getElementById("error").innerHTML = "Error: Amount requested would cause stock to exceed capacity";
            return
        }

        // If there is a negative
        else if (restockValue < 0) {
            console.log("Error: Order amounts cannot be negative.");
            document.getElementById("error").innerHTML = "Error: Order amounts cannot be negative.";

            return
        }

        // If there is a decimal (i.e. value mod 1 is not 0)
        else if (restockValue % 1 !== 0) {
            console.log("Error: Order amounts must be whole numbers.");
            document.getElementById("error").innerHTML = "Error: Order amounts must be whole numbers.";
            return
        }

        // If all good, set error back to blank
        else {
            document.getElementById("error").innerHTML = " ";
        }
    }


    // String to be parsed as JSON in Java
    // NOTE: Don't include [] brackets b/c parse will fail in java
    let restockData = "{";

    let n = restockInputs.length - 1;
    for (let i = 0; i <= n; i++) {
        // console.log("item:", inputs[i].value);
        let restockAmount = parseInt(restockInputs[i].value);

        // console.log(typeof(restockAmount));

        // Catch null, negative values for restock as 0
        if (isNaN(restockAmount)) {
            restockAmount = 0;
        }

        // console.log(restockAmount);

        // Append data to string
        restockData += "\"" + data[i].name + "\":{\"amountToOrder\":\"" + restockAmount + "\"}"; // removed "\": {\"SKU\":\"" + data[i].SKU +

        // Add comma to all but last line
        if (i !== n) {
            restockData += ",";
        }
    }

    restockData += "}";

    // console.log("data:", data[i].name);
    // console.log(restockData);

    // this code executes post(restock-cost) in Main.java
    let xhr = new XMLHttpRequest();
    xhr.open("POST", "http://localhost:4567/restock-cost");

    xhr.setRequestHeader("Accept", "application/json");
    xhr.setRequestHeader("Content-Type", "application/json");

    let restockCost = 0;

    // send the data
    xhr.send(restockData);

    // xhr.onload = () => console.log(xhr.responseText);
    xhr.onload = () => document.getElementById("restockCost").innerHTML = "$" + Number(xhr.responseText).toFixed(2);
}


// Main function
export default function Challenge() {
    // Get the low-stock items
    let dataString = "";

    // Pre-load data
    try {
        dataString = httpGet('http://localhost:4567/low-stock');
    } catch(err) {
        console.log("failed to parse data");
    }

    // Parse data from string to JSON
    data = JSON.parse(dataString);

    // Map data from JSON to table
    const ItemRow=data.map(
        (info)=>{
            return(
                <tbody>
                <tr>
                    <td>{info.SKU}</td>
                    <td>{info.name}</td>
                    <td>{info.stock}</td>
                    <td>{info.capacity}</td>
                    <td>
                        <form>
                            <input type="text" class="restock-input"/>
                        </form>
                    </td>
                </tr>
                </tbody>
            )
        }
    )

    return (
    <>
      <h1>Instructions:</h1>
      <h3>Click "Get Low-Stock Items" to show items with stock quantities under 25% of capacity.
          Enter the amount of each candy to order in the order amount column,
          Then click "Determine Re-Order Cost" and the cost will appear below the table.</h3>
      <button onClick={findLowStockItemsHandler}>Get Low-Stock Items</button>
      <button onClick={reOrderHandler}>Determine Re-Order Cost</button>

        <br/>
        <br/>

      <table id="table" style={{display: "none"}}>
        <thead>
          <tr>
            <td>SKU</td>
            <td>Item Name</td>
            <td>Amount in Stock</td>
            <td>Capacity</td>
            <td>Order Amount</td>
          </tr>
        </thead>
          {ItemRow}
      </table>
      {/*Div to display table input errors*/}
      <div id="error"> </div>
      <div>Total Cost: </div>
      {/*Div to display cost to restock*/}
      <div id="restockCost"> </div>
    </>
    );
}
