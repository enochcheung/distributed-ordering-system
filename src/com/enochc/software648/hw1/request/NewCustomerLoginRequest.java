package com.enochc.software648.hw1.request;


public class NewCustomerLoginRequest {
    private final String password;
    private final String customerID;

    public NewCustomerLoginRequest(String customerID, String password) {
        this.customerID = customerID;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void failed() {
        System.out.println("NewCustomerLoginRequest failed for "+customerID+", ");
    }
}
