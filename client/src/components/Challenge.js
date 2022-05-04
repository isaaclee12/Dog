import React from "react";
//import {NodePath as $http} from "@babel/traverse";


// Excel
export function getExcel() {
    // TODO: Do stuff witht he java thing
}


// ItemRow
export class ItemRow extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            /*their SKU, name, amount in stock, and the capacity
            in the store for that item*/
            SKU: "",
            name: "",
            amountInStock: "",
            capacity: "",
            // Input for order amount
            orderAmount: "",
        };
    }

    // Render the row
    render() {
        return (
            <tr>
                <td>{this.state.SKU}</td>
                <td>{this.state.name}</td>
                <td>{this.state.amount}</td>
                <td>{this.state.capacity}</td>
                <td>
                    {/*TODO: Have this input sent to api*/}
                    <form>
                        <input type="text" id="orderAmount" name="orderAmount"/>
                    </form>
                </td>
                {/*<td>{this.state.orderAmount}</td>*/}
            </tr>
        )
    }
}

function httpGet(url) {
    let xmlHttpReq = new XMLHttpRequest();
    xmlHttpReq.open("GET", url, false);
    xmlHttpReq.send(null);
    return xmlHttpReq.responseText;
}

// Event handlers
export const findLowStockItemsHandler = () => {
    // call lowStock in Main.java
    console.log("Low Stock");

    // Get the low-stock items

    let data = "";

    try {
        data = httpGet('http://localhost:4567/low-stock');
    } catch(err) {
        console.log("failed to parse data");
    }

    // console.log(data);
    data = JSON.parse(data);
    // console.log("JSON:", data);
    document.getElementById("test").style.visibility = "hidden";
    // console.log(data.candiesToBuy);

    //init array
    let toBuyString = "";

    /*for (let i = 0; i <= data.candiesToBuy.length - 1; i++) {
        // get all names
        console.log(data.candiesToBuy[i].name);
        toBuyString += data.candiesToBuy[i].name + "\n";
    }*/

    // Set to html
    document.getElementById("low-stock").innerHTML = toBuyString;
}

export const reOrderHandler = () => {
    // call reOrder in Main.java
    console.log("Reorder");
    document.getElementById("test").style.visibility = "visible";
}


// Main function
export default function Challenge() {
  return (
    <>
      <table>
        <thead>
          <tr>
            <td>SKU</td>
            <td>Item Name</td>
            <td>Amount in Stock</td>
            <td>Capacity</td>
            <td>Order Amount</td>
          </tr>
        </thead>
        <tbody>
            <ItemRow/>
          {/* // DO THIS NEXT YO
          TODO: Create an <ItemRow /> component that's rendered for every inventory item. The component
          will need an input element in the Order Amount column that will take in the order amount and 
          update the application state appropriately.
          */}
        </tbody>
      </table>
      {/* TODO: Display total cost returned from the server */}
      <div>Total Cost: </div>
      {/* 
      TODO: Add event handlers to these buttons that use the Java API to perform their relative actions.
      */}
      <button onClick={findLowStockItemsHandler}>Get Low-Stock Items</button>
      <button onClick={reOrderHandler}>Determine Re-Order Cost</button>
      <p id="test">test</p>
      <p id="low-stock">a</p>
    </>
  );
}
