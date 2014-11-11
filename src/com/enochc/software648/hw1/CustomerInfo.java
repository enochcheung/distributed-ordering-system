package com.enochc.software648.hw1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CustomerInfo implements Serializable {
    private static final long serialVersionUID = 820912094120L;

    private String customerID;
    private ArrayList<String> orders;
    private CustomerAddress customerAddress;

    public CustomerInfo(String customerID, String firstname, String lastname,
                        String street, String city, String state, String zipcode) {
        this.customerID = customerID;
        this.orders = new ArrayList<String>();
        this.customerAddress = new CustomerAddress(firstname, lastname,
                street, city, state, zipcode);
    }

    public CustomerInfo(JSONObject jsonObject) {
        this.customerID = (String) jsonObject.get("customerID");

        JSONArray jsonOrders = (JSONArray) jsonObject.get("orders");
        this.orders = new ArrayList<String>(jsonOrders);


        this.customerAddress = new CustomerAddress((JSONObject) jsonObject.get("address"));

    }

    public CustomerAddress getCustomerAddress() {
        return customerAddress;
    }

    public void addOrder(String orderID) {
        orders.add(orderID);
    }

    public List<String> orderHistory() {
        return orders;
    }

    public JSONObject toJsonObject() {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(orders);

        jsonObject.put("customerID", customerID);
        jsonObject.put("orders", jsonArray);
        jsonObject.put("address", customerAddress.toJsonObject());

        return jsonObject;
    }

    public String toString() {
        return String.format(
                "CustomerID: %s%nList of orders: %s%n%s", customerID,
                orders.toString(), customerAddress.toString());
    }

}
