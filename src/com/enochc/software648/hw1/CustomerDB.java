package com.enochc.software648.hw1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class CustomerDB {
	private static final String FILENAME = "data/CustomerDB.txt";
	private static final String TMP_FILENAME = "data/CustomerDB.tmp";
    private static final int MAX_VERSIONS = 5;

    private final JSONObject database;
    private final LinkedHashMap<String, HashMap<String,CustomerInfo>> databaseVersionDiffs;
    private String currentVersionID;

    public CustomerDB() {
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
			flushJSON();
		}

        databaseVersionDiffs = new LinkedHashMap<String,HashMap<String,CustomerInfo>>(){
            private static final long serialVersionUID = 76149408267525205L;

            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > MAX_VERSIONS;
            }
        };

        currentVersionID = UUID.randomUUID().toString();
        databaseVersionDiffs.put(currentVersionID,new HashMap<String, CustomerInfo>());
	}

	public boolean hasCustomer(String customerID) {
		return database.containsKey(customerID);
	}

    synchronized private void updateVersionDiff(String customerID, CustomerInfo customerInfo) {
        for (HashMap<String,CustomerInfo> map : databaseVersionDiffs.values()) {
            map.put(customerID,customerInfo);
        }

        currentVersionID = UUID.randomUUID().toString();
        databaseVersionDiffs.put(currentVersionID,new HashMap<String, CustomerInfo>());

    }

    public DataPatch<HashMap<String, CustomerInfo>> getDataPatch(String versionID) {
        if (currentVersionID.equals(versionID)) {
            return null;
        }

        if (databaseVersionDiffs.containsKey(versionID)) {
            return new DataPatch<HashMap<String, CustomerInfo>>(databaseVersionDiffs.get(versionID),currentVersionID);
        }

        return getAllInfo();
    }

	/**
	 * 
	 * @param customerID, customerInfo
	 * @return true if successful, false if customerID taken already
	 */
	public boolean addCustomer(String customerID, CustomerInfo customerInfo) {
		if (hasCustomer(customerID)) {
			return false;
		}

		database.put(customerID, customerInfo.toJsonObject());
		flushJSON();

        updateVersionDiff(customerID, customerInfo);

		return true;
	}

	public boolean addOrder(String customerID, String orderID) {
		if (!hasCustomer(customerID)) {
			return false;
		}
		JSONObject customerObj = (JSONObject) database.get(customerID);
		CustomerInfo customerInfo = new CustomerInfo(customerObj);
		customerInfo.addOrder(orderID);
		database.put(customerID, customerInfo.toJsonObject());

        updateVersionDiff(customerID, customerInfo);

        flushJSON();
		return true;
	}

	/**
	 * @param customerID
	 * @return Customer information. Null if not found
	 */
	public CustomerInfo lookup(String customerID) {
		if (!hasCustomer(customerID)) {
			return null;
		}
		JSONObject jsonInfo = (JSONObject) database.get(customerID);
		CustomerInfo customerInfo = new CustomerInfo(jsonInfo);

		return customerInfo;

	}

    /**
     * @return All customer info
     */
    public DataPatch<HashMap<String,CustomerInfo>> getAllInfo() {
        HashMap<String,CustomerInfo> map = new HashMap<String, CustomerInfo>();
        for (Object key : database.keySet()) {
            String customerID = (String) key;
            JSONObject jsonInfo = (JSONObject) database.get(customerID);
            CustomerInfo customerInfo = new CustomerInfo(jsonInfo);

            map.put(customerID,customerInfo);

        }
        return new DataPatch<HashMap<String, CustomerInfo>>(map,currentVersionID);
    }



    /**
	 * @param customerID
	 * @return Customer information. Null if not found
	 */
	public String lookupShipping(String customerID) {
		if (!hasCustomer(customerID)) {
			return null;
		}
		JSONObject jsonInfo = (JSONObject) database.get(customerID);
		CustomerInfo customerInfo = new CustomerInfo(jsonInfo);

		return customerInfo.getCustomerAddress().toString();

	}

    /**
     * @return List of customers
     */
	public List<String> listCustomers() {
		return new ArrayList<String>(database.keySet());
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
