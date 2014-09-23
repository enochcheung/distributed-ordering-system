package com.enochc.software648.hw1;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CustomerInfo {
	private String customerID;
	private List<String> orders;
	private String firstname;
	private String lastname;
	private String address;
	private String city;
	private String state;
	private String zipcode;

	public CustomerInfo(String customerID, String firstname, String lastname,
			String address, String city, String state, String zipcode) {
		this.customerID = customerID;
		this.orders = new ArrayList<String>();
		this.firstname = firstname;
		this.lastname = lastname;
		this.address = address;
		this.city = city;
		this.state = state;
		this.zipcode = zipcode;
	}

	public CustomerInfo(JSONObject jsonObject) {
		this.customerID = (String) jsonObject.get("customerID");
		
		JSONArray jsonOrders = (JSONArray) jsonObject.get("orders");
		this.orders = new ArrayList<String>(jsonOrders);
		
		this.firstname = (String) jsonObject.get("firstname");
		this.lastname = (String) jsonObject.get("lastname");
		this.address = (String) jsonObject.get("address");
		this.city = (String) jsonObject.get("city");
		this.state = (String) jsonObject.get("state");
		this.zipcode = (String) jsonObject.get("zipcode");

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
		jsonObject.put("firstname", firstname);
		jsonObject.put("lastname", lastname);
		jsonObject.put("address", address);
		jsonObject.put("city", city);
		jsonObject.put("state", state);
		jsonObject.put("zipcode", zipcode);

		return jsonObject;
	}

	public String toString() {
		return String.format(
				"CustomerID: %s%nList of orders: %s%n%s %s%n%s%n%s, %s %s", customerID,
				orders.toString(), firstname, lastname, address, city, state,
				zipcode);
	}

	public String shippingInfo() {
		return String.format("%s %s%n%s%n%s, %s %s", firstname, lastname,
				address, city, state, zipcode);
	}
}
