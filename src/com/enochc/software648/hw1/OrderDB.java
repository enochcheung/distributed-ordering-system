package com.enochc.software648.hw1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class OrderDB {
	private static final String FILENAME = "data/OrdersDB.txt";
	private static final String TMP_FILENAME = "data/OrdersDB.tmp";
	private final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"MM/dd/yyyy HH:mm");

	private final JSONObject database;

	public OrderDB() {
		File file = new File(FILENAME);
		if (file.exists()) {
			// if the file already exists, load the data from it
			FileInputStream fis;
			byte[] data = null;
			try {
				fis = new FileInputStream(file);
				data = new byte[(int) file.length()];
				fis.read(data);
				fis.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			String fileContent = new String(data, StandardCharsets.UTF_8);
			database = (JSONObject) JSONValue.parse(fileContent);
		} else {
			// create the file
			database = new JSONObject();
			// start counting order number
			database.put("currentOrderID", 0);
			flushJSON();
		}
	}

	/**
	 * Warning: This method does not update the json file. Should call
	 * flushJSON() after this.
	 * 
	 * @return An unused orderID.
	 */
	private String getNewOrderID() {
		Integer orderIDint = ((Number) database.get("currentOrderID"))
				.intValue();
		String orderIDString = orderIDint.toString();
		database.put("currentOrderID", orderIDint + 1);

		return orderIDString;
	}

	/**
	 * 
	 * @param customerID
	 * @param itemNumber
	 * @param bikeName
	 * @param quantity
	 * @param price
	 * @return orderID for this order
	 */
	public String newOrder(String customerID, String itemNumber,
			String bikeName, int quantity, int price) {
		String orderID = getNewOrderID();
		String date = (dateFormat.format(new Date()));
		Order order = new Order(customerID, date, itemNumber, bikeName,
				quantity, price);

		database.put(orderID, order.toJsonObject());
		flushJSON();
		return orderID;
	}

	public boolean validOrderID(String orderID) {
		return database.containsKey(orderID);
	}

	/**
	 * 
	 * @param orderID
	 * @return Info for this order. Null if not found.
	 */
	public Order lookupOrder(String orderID) {
		JSONObject orderObj = (JSONObject) database.get(orderID);
		if (orderObj == null) {
			return null;
		}
		Order order = new Order(orderObj);
		return order;
	}

	public void completeOrder(String orderID) {
		JSONObject orderObj = (JSONObject) database.get(orderID);
		Order order = new Order(orderObj);
		order.setComplete();
		orderObj = order.toJsonObject();
		database.put(orderID, orderObj);
		flushJSON();
	}

	public void orderHistory(String customerID) {

	}

	/**
	 * Write JSONObject into file
	 */
	private void flushJSON() {
		PrintWriter writer = null;
		try {
			File tmpFile = new File(TMP_FILENAME);
			writer = new PrintWriter(tmpFile);
			writer.print(database.toJSONString());
			writer.flush();
			writer.close();

			File jsonFile = new File(FILENAME);
			jsonFile.delete();
			tmpFile.renameTo(jsonFile);

		} catch (IOException e) {
			e.printStackTrace();
			if (writer != null) {
				writer.close();
			}
		}
	}
}
