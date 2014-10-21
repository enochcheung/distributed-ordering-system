package com.enochc.software648.hw1;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;

public class Order implements Serializable {
    private static final long serialVersionUID = -2778420502724580525L;

    private String customerID;
    private String orderID;
	private String date;
	private String status;
    private final HashMap<String,Integer> bikeQuantities = new HashMap<String, Integer>();
    private final HashMap<String,Integer> bikePrices = new HashMap<String, Integer>();
    private final HashMap<String,String> bikeNames = new HashMap<String, String>();
    private final HashMap<String,BigDecimal> bikePricesDollars = new HashMap<String, BigDecimal>();

    private int totalPrice;

	public Order(String customerID, String date, String itemNumber,
			String bikeName, int quantity, int price) {
		this.customerID = customerID;
		this.date = date;
		this.totalPrice = price;
        bikeQuantities.put(itemNumber, quantity);
        bikeNames.put(itemNumber,bikeName);

		setInProcess();
	}

    public Order(String customerID, String date, String orderID) {
        this.customerID = customerID;
        this.date = date;
        this.orderID = orderID;
        this.totalPrice = 0;

        setInProcess();
    }

	public Order(JSONObject jsonObject) {
		this.customerID = (String) jsonObject.get("customerID");
        this.orderID = (String) jsonObject.get("orderID");
        this.date = (String) jsonObject.get("date");
		this.status = (String) jsonObject.get("status");
        JSONArray bikesArray = (JSONArray) jsonObject.get("bikes");
        for (Object bikeObject1 : bikesArray) {
            JSONObject bikeObject = (JSONObject) bikeObject1;
            String itemNum = (String) bikeObject.get("itemNumber");
            String bikeName = (String) bikeObject.get("bikeName");
            int quantity = ((Number) bikeObject.get("quantity")).intValue();
            int price = ((Number) bikeObject.get("price")).intValue();
            bikeQuantities.put(itemNum,quantity);
            bikeNames.put(itemNum,bikeName);
            bikePrices.put(itemNum,price);

            BigDecimal priceDecimal = new BigDecimal(price);
            BigDecimal priceDollars = priceDecimal.divide(new BigDecimal(100));
            bikePricesDollars.put(itemNum,priceDollars);
        }
		this.totalPrice = ((Number) jsonObject.get("totalPrice")).intValue();

	}

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public void addBike(Bike bike, int quantity) {
        String itemNum = bike.getItemNumber();
        String bikeName = bike.getName();
        int bikePrice = bike.getPrice();
        bikeQuantities.put(itemNum, quantity);
        bikeNames.put(itemNum,bikeName);
        bikePrices.put(itemNum,bikePrice);
        bikePricesDollars.put(itemNum,bike.getPriceDollars());

        totalPrice += bikePrice*quantity;
    }

    /**
     * Gets the itemNumber of the bike in this order, for when this order only consists of one type of bike
     *
     * @return itemNumber
     */
    public String getItemNumber() {
        return bikeQuantities.keySet().iterator().next();
    }

    /**
     * Gets the bikeName of the bike in this order, for when this order only consists of one type of bike
     *
     * @return bikeName
     */
    public String getBikeName() {
        return bikeNames.get(this.getItemNumber());
    }

    /**
     * Gets the quantity of the bike in this order, for when this order only consists of one type of bike
     *
     * @return quantity
     */
    public int getQuantity() {
        return bikeQuantities.get(this.getItemNumber());
    }

    public HashMap<String, Integer> getBikeQuantities() {
        return bikeQuantities;
    }

    public HashMap<String, String> getBikeNames() {
        return bikeNames;
    }

    public HashMap<String, Integer> getBikePrices() {
        return bikePrices;
    }

    public HashMap<String, BigDecimal> getBikePricesDollars() {
        return bikePricesDollars;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public BigDecimal getTotalPriceDollars() {
        BigDecimal priceDecimal = new BigDecimal(totalPrice);
        return priceDecimal.divide(new BigDecimal(100));
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
        jsonObject.put("orderID", orderID);
        jsonObject.put("date", date);
		jsonObject.put("status", status);
        JSONArray bikesArray = new JSONArray();
        for (String itemNum : bikeQuantities.keySet()) {
            JSONObject bikeJson = new JSONObject();
            bikeJson.put("itemNumber", itemNum);
            bikeJson.put("bikeName", bikeNames.get(itemNum));
            bikeJson.put("price", bikePrices.get(itemNum));
            bikeJson.put("quantity", bikeQuantities.get(itemNum));
            bikesArray.add(bikeJson);
        }
        jsonObject.put("bikes",bikesArray);
		jsonObject.put("totalPrice", totalPrice);

		return jsonObject;
	}

	public String toString() {
		Double priceDollars = (double) totalPrice / 100;
		return String
				.format("%s%nStatus: %s%nCustomerID: %s%nItem Number: %s%n%s%nQuantity: %s%nPrice: $%.2f",
						date, status,customerID, this.getItemNumber(), this.getBikeName(), this.getQuantity(), priceDollars);
	}


}
