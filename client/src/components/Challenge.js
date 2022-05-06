import React from "react";

// JSON DATA OBJ
let data = [];

function httpGet(url) {
    let xmlHttpReq = new XMLHttpRequest();
    xmlHttpReq.open("GET", url, false);
    xmlHttpReq.send(null);
    return xmlHttpReq.responseText;
}

// Event handlers
export const findLowStockItemsHandler = () => {
    document.getElementById("table").style.display = "inline";
/*    let isVisible = (document.getElementById("table").style.display !== "none");

    console.log(isVisible);

    if (isVisible) {
        document.getElementById("table").style.display = "none";
    } else {
        document.getElementById("table").style.display = "inline";
    }*/
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
    console.log(restockData);

    // set html
    document.getElementById("dataToSend").innerHTML = restockData;

    // this code executes post(restock-cost) in Main.java
    let xhr = new XMLHttpRequest();
    xhr.open("POST", "http://localhost:4567/restock-cost");

    xhr.setRequestHeader("Accept", "application/json");
    xhr.setRequestHeader("Content-Type", "application/json");

    xhr.onload = () => console.log(xhr.responseText);

    // send the data
     xhr.send(restockData);

    console.log("test");

    // Get data back
    // let restockCost = httpGet('http://localhost:4567/restock-cost');
    let restockCost = xhr.responseText;
    console.log("Return:", restockCost);

    // Set HTML.
    document.getElementById("restockCost").innerHTML = restockCost;
}


// Main function
export default function Challenge() {
    // call lowStock in Main.java
    // Get the low-stock items
    let dataString = "";

    // Pre-load data
    try {
        dataString = httpGet('http://localhost:4567/low-stock');
    } catch(err) {
        console.log("failed to parse data");
    }

    data = JSON.parse(dataString);
    console.log("JSON:", data);
    // console.log("JSON:", testData);

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
      {/* TODO: Add event handlers to these buttons that use the Java API to perform their relative actions.*/}
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
          {/* TODO: Create an <ItemRow /> component that's rendered for every inventory item. The component
          will need an input element in the Order Amount column that will take in the order amount and
          update the application state appropriately. */}
          {/*<ItemRow/>*/}
      </table>
      {/* TODO: Display total cost returned from the server */}
      <div id="error"> </div>
      <div>Total Cost: </div>
      {/*Invisible div for java to get data from*/}
      <div id="dataToSend"> </div>
      {/*Same thing but for restock*/}
      <div id="restockCost"> </div>
    </>
    );
}
