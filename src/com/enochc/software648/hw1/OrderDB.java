package com.enochc.software648.hw1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class OrderDB {
	private static final String FILENAME = "data/OrdersDB.txt";
	private static final String TMP_FILENAME = "data/OrdersDB.tmp";
	private final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"MM/dd/yyyy HH:mm");
    private static final int MAX_VERSIONS = 5;

    private final LinkedHashMap<String, HashMap<String,Order>> databaseVersionDiffs;
    private String currentVersionID;

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

        databaseVersionDiffs = new LinkedHashMap<String,HashMap<String,Order>>(){
            private static final long serialVersionUID = 3077590306842883188L;

            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > MAX_VERSIONS;
            }
        };

        currentVersionID = UUID.randomUUID().toString();
        databaseVersionDiffs.put(currentVersionID,new HashMap<String, Order>());
	}


    synchronized private void updateVersionDiff(String orderID, Order order) {
        for (HashMap<String,Order> map : databaseVersionDiffs.values()) {
            map.put(orderID,order);
        }

        currentVersionID = UUID.randomUUID().toString();
        databaseVersionDiffs.put(currentVersionID,new HashMap<String, Order>());

    }


    public DataPatch<HashMap<String, Order>> getDataPatch(String versionID) {
        if (currentVersionID.equals(versionID)) {
            return null;
        }

        if (databaseVersionDiffs.containsKey(versionID)) {
            return new DataPatch<HashMap<String, Order>>(databaseVersionDiffs.get(versionID),currentVersionID);
        }

        return getAllInfo();
    }

    synchronized public DataPatch<HashMap<String,Order>> getAllInfo() {
        HashMap<String,Order> map = new HashMap<String, Order>();
        for (Object key : database.keySet()) {
            String orderID = (String) key;
            if (!orderID.equals("currentOrderID")) {
                // skip the above

                JSONObject orderJson = (JSONObject) database.get(orderID);
                Order order = new Order(orderJson);

                map.put(orderID, order);
            }

        }
        return new DataPatch<HashMap<String, Order>>(map,currentVersionID);
    }

    /**
	 * @return A new orderID.
	 */
	public String getNewOrderID() {
		Integer orderIDint = ((Number) database.get("currentOrderID"))
				.intValue();
		String orderIDString = orderIDint.toString();
		database.put("currentOrderID", orderIDint + 1);
        flushJSON();
		return orderIDString;
	}

    public String getDate() {
        return dateFormat.format(new Date());
    }

    public boolean putOrder(Order order) {
        String orderID = order.getOrderID();
        database.put(orderID,order.toJsonObject());
        flushJSON();
        return true;
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


    public void failedOrder(String orderID) {
        JSONObject orderObj = (JSONObject) database.get(orderID);
        Order order = new Order(orderObj);
        order.setFailed();
        orderObj = order.toJsonObject();
        database.put(orderID, orderObj);
        flushJSON();
    }

	/**
	 * Write JSONObject into file
	 */
	private synchronized void flushJSON() {
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
