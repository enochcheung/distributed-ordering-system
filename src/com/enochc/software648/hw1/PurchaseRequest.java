package com.enochc.software648.hw1;

import java.io.Serializable;

/**
 * Represents a request to purchase some quantity of a Bike, from OrderingSystem to Supplier.
 * Before serialization, contains a callback method to tell the ordering system that it has failed.
 * On the supplier side, failed() should not be called, because callback reference is transient.
 **/
public class PurchaseRequest implements Serializable{
    private static final long serialVersionUID = -4127555326115095091L;
    private final String itemNumber;
    private final int quantity;
    private final String orderID;
    transient private final OrderingSystem orderingSystem;



    public PurchaseRequest(String itemNumber, int quantity, String orderID, OrderingSystem orderingSystem) {

        this.itemNumber = itemNumber;
        this.quantity = quantity;
        this.orderID = orderID;
        this.orderingSystem = orderingSystem;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getOrderID() {
        return orderID;
    }

    /**
     * Callback method to indicate that the order has failed. Should only be called from OrderingSystem side,
     * because it will not work after serialization.
     */
    public void failed(){
        if (orderingSystem != null) {
            orderingSystem.notifyFailed(orderID);
        }
    }
}
