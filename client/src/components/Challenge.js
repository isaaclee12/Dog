import React from "react";


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

// Event handlers
export function findLowStockItemsHandler() {
    // call lowStock in Main.java
    console.log("Low Stock");
}

export const reOrderHandler = () => {
    // call reOrder in Main.java
    console.log("Reorder");
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
    </>
  );
}
