package com.enochc.software648.hw1.request;


public class AddOrderToHistoryRequest {
    private final String orderID;
    private final String customerID;

    public AddOrderToHistoryRequest(String customerID, String orderID) {
        this.customerID = customerID;
        this.orderID = orderID;
    }

    public String getOrderID() {
        return orderID;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void failed() {
        System.out.println("AddOrderToHistoryRequest failed for "+customerID+", "+orderID);
    }
}
