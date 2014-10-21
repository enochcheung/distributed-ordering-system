package com.enochc.software648.hw1;


public class CustomerNotFoundException extends Exception {
    private static final long serialVersionUID = 6453116862708292036L;

    private final String customer;

    public CustomerNotFoundException(String customerID) {
        super("Customer "+customerID+" not found.");
        this.customer = customerID;
    }

    public String getCustomer(){
        return customer;
    }
}
