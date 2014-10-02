package com.enochc.software648.hw1;

import org.json.simple.JSONObject;

import java.io.Serializable;

public class Order implements Serializable {
    private static final long serialVersionUID = 39429849349L;

    private String customerID;
	private String date;
	private String status;
	private String itemNumber;
	private String bikeName;
	private int quantity;
	private int price;

	public Order(String customerID, String date, String itemNumber,
			String bikeName, int quantity, int price) {
		this.customerID = customerID;
		this.date = date;
		this.itemNumber = itemNumber;
		this.bikeName = bikeName;
		this.quantity = quantity;
		this.price = price;

		setInProcess();
	}

	public Order(JSONObject jsonObject) {
		this.customerID = (String) jsonObject.get("customerID");
		this.date = (String) jsonObject.get("date");
		this.status = (String) jsonObject.get("status");
		this.itemNumber = (String) jsonObject.get("itemNumber");
		this.bikeName = (String) jsonObject.get("bikeName");
		this.quantity = ((Number) jsonObject.get("quantity")).intValue();
		this.price = ((Number) jsonObject.get("price")).intValue();

	}

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public String getBikeName() {
        return bikeName;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getPrice() {
        return price;
    }

    public String getCustomerID() {
		return customerID;
	}

	public void setInProcess() {
		this.status = "In process";
	}

	public void setComplete() {
		this.status = "Complete";
	}

    public void setFailed() {
        this.status = "Failed";
    }

    public JSONObject toJsonObject() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("customerID", customerID);		
		jsonObject.put("date", date);
		jsonObject.put("status", status);
		jsonObject.put("itemNumber", itemNumber);
		jsonObject.put("bikeName", bikeName);
		jsonObject.put("quantity", quantity);
		jsonObject.put("price", price);

		return jsonObject;
	}

	public String toString() {
		Double priceDollars = (double) price / 100;
		return String
				.format("%s%nStatus: %s%nCustomerID: %s%nItem Number: %s%n%s%nQuantity: %s%nPrice: $%.2f",
						date, status,customerID, itemNumber, bikeName, quantity, priceDollars);
	}

}
