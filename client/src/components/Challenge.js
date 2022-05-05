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
    // call reOrder in Main.java
    console.log("Reorder");

    // Get order amounts from inputs
    let restockInputs = document.getElementsByClassName("restock-input");

    let restockData = [];

    // VALIDATE DATA to make sure it's not more than capacity
    for (let i = 0; i <= restockInputs.length - 1; i++) {
        let restockValue = restockInputs[i].value;
        // If there is a value to restock AND the restock amount exceeds diff. of capacity and stock
        if (restockValue !== null && restockValue > (data[i].capacity - data[i].stock)) {
            console.log("Error: Amount requested would cause stock to exceed capacity.");
            return (
                <p>Error: Amount requested would cause stock to exceed capacity.</p>
            );
        }
    }

    for (let i = 0; i <= restockInputs.length - 1; i++) {
        // console.log("item:", inputs[i].value);
        restockData[i] = {
            "SKU": data[i].SKU,
            "name": data[i].name,
            "amountToOrder": restockInputs[i].value,
        }
    }

    // console.log("data:", data[i].name);
    console.log(restockData);



    // AJAX

    // Creating Our XMLHttpRequest object
    var xhr = new XMLHttpRequest();

    // Making our connection
    var url = 'http://localhost:3000/restock-cost';
    xhr.open("GET", url, true);

    // function execute after request is successful
    xhr.onreadystatechange = function () {
        if (this.readyState === 4 && this.status === 200) {
            console.log(this.responseText);
        }
    }

    // Sending our request
    xhr.send(restockData);




    // TODO: send restockData to Java
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
      <div>Total Cost: </div>
    </>
    );
}
