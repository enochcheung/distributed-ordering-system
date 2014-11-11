package com.enochc.software648.hw1;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Customer database that stores login information
 */
public class CustomerLoginDB {
    private static final String FILENAME = "data/CustomerLogin.txt";
    private static final String TMP_FILENAME = "data/CustomerLogin.tmp";
    private static final int MAX_VERSIONS = 5;

    private final JSONObject database;
    private SecureRandom random = new SecureRandom();

    private final ConcurrentHashMap<String,String> tokenMap = new ConcurrentHashMap<String, String>();

    private final LinkedHashMap<String, HashMap<String,String>> databaseVersionDiffs;
    private String currentVersionID;

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

        databaseVersionDiffs = new LinkedHashMap<String,HashMap<String,String>>(){
            private static final long serialVersionUID = -1389243436686787763L;

            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > MAX_VERSIONS;
            }
        };

        currentVersionID = UUID.randomUUID().toString();
        databaseVersionDiffs.put(currentVersionID,new HashMap<String, String>());
    }

    synchronized private void updateVersionDiff(String customerID, String password) {
        for (HashMap<String,String> map : databaseVersionDiffs.values()) {
            map.put(customerID,password);
        }

        currentVersionID = UUID.randomUUID().toString();
        databaseVersionDiffs.put(currentVersionID,new HashMap<String, String>());

    }


    public DataPatch<HashMap<String, String>> getDataPatch(String versionID) {
        if (currentVersionID.equals(versionID)) {
            return null;
        }

        if (databaseVersionDiffs.containsKey(versionID)) {
            return new DataPatch<HashMap<String, String>>(databaseVersionDiffs.get(versionID),currentVersionID);
        }

        return getAllInfo();
    }


    /**
     * @return All customer login info
     */
    synchronized public DataPatch<HashMap<String,String>> getAllInfo() {
        HashMap<String,String> map = new HashMap<String, String>();
        for (Object key : database.keySet()) {
            String customerID = (String) key;
            String password = (String) database.get(customerID);

            map.put(customerID,password);

        }
        return new DataPatch<HashMap<String, String>>(map,currentVersionID);
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

        this.updateVersionDiff(customerID, password);
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


    public boolean putToken(String customerID, String password, String token) {
        String customerPassword = this.getPassword(customerID);
        if (password.equals(customerPassword)) {
            tokenMap.put(token,customerID);
            return true;
        }
        return false;
    }

    public HashMap<String, String> getAllTokens() {
        HashMap<String, String> map = new HashMap<String, String>(tokenMap);
        return map;
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
