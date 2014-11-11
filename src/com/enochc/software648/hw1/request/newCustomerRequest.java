package com.enochc.software648.hw1.request;


import com.enochc.software648.hw1.CustomerInfo;

public class NewCustomerRequest {
    private final CustomerInfo customerInfo;
    private final String customerID;

    public NewCustomerRequest(String customerID, CustomerInfo customerInfo) {
        this.customerID = customerID;
        this.customerInfo = customerInfo;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void failed() {
        System.out.println("NewCustomerRequest failed for "+customerID);
    }
}
