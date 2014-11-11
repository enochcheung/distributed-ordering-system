package com.enochc.software648.hw1.request;


public class NewTokenRequest {
    private final String token;
    private final String customerID;

    public NewTokenRequest(String customerID, String token) {
        this.customerID = customerID;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void failed() {
        System.out.println("NewCustomerLoginRequest failed for "+customerID+", "+token);
    }
}
