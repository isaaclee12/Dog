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
                            <input type="text"/>
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
