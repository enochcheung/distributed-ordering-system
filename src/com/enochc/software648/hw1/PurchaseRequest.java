package com.enochc.software648.hw1;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Represents a request to purchase some quantity of a Bike, from OrderingSystem to Supplier.
 * Before serialization, contains a callback method to tell the ordering system that it has failed.
 * On the supplier side, failed() should not be called, because callback reference is transient.
 **/
public class PurchaseRequest implements Serializable{
    private static final long serialVersionUID = -4127555326115095091L;

    private final String supplierName;
    private final String customerID;
    private final HashMap<String, Integer> bikeQuantities = new HashMap<String, Integer>();
    private final String orderID;
    transient private final OrderingSystem orderingSystem;



    public PurchaseRequest(String customerID, String orderID, String supplierName, OrderingSystem orderingSystem) {
        this.supplierName = supplierName;
        this.customerID = customerID;
        this.orderID = orderID;
        this.orderingSystem = orderingSystem;
    }

    public void addBike(String itemNum, int quantity) {
        Integer amount = bikeQuantities.get(itemNum);
        if (amount!= null) {
            bikeQuantities.put(itemNum,amount+quantity);
        } else {
            bikeQuantities.put(itemNum, quantity);
        }
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getCustomerID() {
        return customerID;
    }

    public HashMap<String, Integer> getBikeQuantities() {
        return bikeQuantities;
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
