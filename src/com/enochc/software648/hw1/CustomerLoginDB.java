package com.enochc.software648.hw1;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Customer database that stores login information
 */
public class CustomerLoginDB {
    private static final String FILENAME = "data/CustomerLogin.txt";
    private static final String TMP_FILENAME = "data/CustomerLogin.tmp";
    private SecureRandom random = new SecureRandom();

    private final Map<String,String> tokenMap = new HashMap<String, String>();
    private final JSONObject database;


    public CustomerLoginDB() {
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
    }

    public boolean hasCustomer(String customerID) {
        return database.containsKey(customerID);
    }


    /**
     *
     * @param customerID
     * @param password
     * @return true if successful, false if customerID taken already
     */
    public boolean addCustomer(String customerID, String password) {
        if (hasCustomer(customerID)) {
            return false;
        }

        database.put(customerID, password);
        flushJSON();
        return true;
    }

    /**
     *
     * @param customerID
     * @param token
     * @return true if matches, false if not
     */
    public boolean checkToken(String customerID, String token) {
        String storedCustomer = tokenMap.get(token);
        if (storedCustomer == null) {return false;}
        return storedCustomer.equals(customerID);
    }

    private String getPassword(String customerID) {
        return (String) database.get(customerID);
    }

    private String randomString() {
        return new BigInteger(130, random).toString(32);
    }

    /**
     * @param customerID
     * @param password
     * @return Token that can be used to authenticate a customer.
     *         Null if incorrect password or customer not found
     */
    public String getToken(String customerID, String password) {
        String customerPassword = this.getPassword(customerID);
        if (password.equals(customerPassword)) {
            String token = this.randomString();
            tokenMap.put(token,customerID);
            return token;
        }
        return null;
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
