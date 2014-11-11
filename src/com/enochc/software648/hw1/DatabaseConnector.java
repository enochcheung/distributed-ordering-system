package com.enochc.software648.hw1;

import com.enochc.software648.hw1.request.*;

import java.math.BigInteger;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DatabaseConnector {
    private static final long WAIT_TIME = 3000L;
    private SecureRandom random = new SecureRandom();

    private DatabaseInterface database;
    boolean connected = false;

    private String customerDataVersion = "";
    private final ConcurrentHashMap<String, CustomerInfo> customerCache = new ConcurrentHashMap<String, CustomerInfo>();
    private final ConcurrentLinkedQueue<NewCustomerRequest> newCustomerQueue
            = new ConcurrentLinkedQueue<NewCustomerRequest>();
    private final ConcurrentLinkedQueue<AddOrderToHistoryRequest> customerOrderQueue
            = new ConcurrentLinkedQueue<AddOrderToHistoryRequest>();

    private String loginDataVersion = "";
    private final ConcurrentHashMap<String, String> loginCache = new ConcurrentHashMap<String, String>();
    private final ConcurrentLinkedQueue<NewCustomerLoginRequest> loginQueue
            = new ConcurrentLinkedQueue<NewCustomerLoginRequest>();


    private final ConcurrentHashMap<String, String> tokenCache = new ConcurrentHashMap<String, String>();
    private final ConcurrentLinkedQueue<NewTokenRequest> tokenQueue
            = new ConcurrentLinkedQueue<NewTokenRequest>();

    private String orderDataVersion = "";
    private HashSet<String> orderIDs = new HashSet<String>();
    private final ConcurrentHashMap<String, Order> orderCache = new ConcurrentHashMap<String, Order>();
    private final ConcurrentLinkedQueue<NewOrderRequest> orderQueue
            = new ConcurrentLinkedQueue<NewOrderRequest>();



    public DatabaseConnector() {

        connectToDatabase();

        Thread thread = new Thread(this.getRunnable());
        thread.start();
    }

    public boolean hasCustomer(String customerID) {
        return customerCache.containsKey(customerID);
    }

    public CustomerInfo lookupCustomer(String customerID) {
        return customerCache.get(customerID);
    }

    public String lookupShipping(String customerID) {
        CustomerInfo customerInfo = customerCache.get(customerID);
        return customerInfo.getCustomerAddress().toString();
    }

    public ArrayList<String> listCustomers() {
        return new ArrayList<String>(customerCache.keySet());
    }

    public boolean addCustomer(String customerID, CustomerInfo customerInfo) {
        if (hasCustomer(customerID)) {
            return false;
        }


        newCustomerQueue.offer(new NewCustomerRequest(customerID, customerInfo));
        customerCache.put(customerID, customerInfo);

        return true;
    }


    public boolean addOrderToCustomer(String customerID, String orderID) {
        if (!hasCustomer(customerID)) {
            return false;
        }

        CustomerInfo customerInfo = customerCache.get(customerID);
        customerInfo.addOrder(orderID);

        customerOrderQueue.offer(new AddOrderToHistoryRequest(customerID, orderID));
        customerCache.put(customerID, customerInfo);

        return true;
    }

    public Order lookupOrder(String orderID) {

        return orderCache.get(orderID);

    }

    public boolean putOrder(Order order) {

        orderCache.put(order.getOrderID(), order);

        orderQueue.offer(new NewOrderRequest(order));
        return true;
    }

    private String randomString() {
        return new BigInteger(130, random).toString(32);
    }

    private String randomShortString() {
        return new BigInteger(32, random).toString(32);
    }

    public String getNewOrderID() {
        String orderID;
        synchronized(orderIDs) {
            do {
                orderID = randomShortString();
            } while (orderIDs.contains(orderID));

            // orderID is new
            orderIDs.add(orderID);
        }
        return orderID;
    }

    public void completeOrder(String orderID){
        Order order = orderCache.get(orderID);
        if (order==null) {
            return;
        }

        order.setComplete();

        orderCache.put(orderID,order);
        orderQueue.offer(new NewOrderRequest(order));

    }

    public void failedOrder(String orderID){
        Order order = orderCache.get(orderID);
        if (order==null) {
            return;
        }

        order.setFailed();

        orderCache.put(orderID,order);
        orderQueue.offer(new NewOrderRequest(order));

    }

    public boolean hasCustomerLogin(String customerID) {
        return loginCache.containsKey(customerID);
    }

    /**
     * @param customerID
     * @param password
     * @return Token that can be used to authenticate a customer.
     *         Null if incorrect password or customer not found
     */
    public String getToken(String customerID, String password) {
        String customerPassword = loginCache.get(customerID);
        if (customerPassword!= null && customerPassword.equals(password)) {
            // password correct
            String token = this.randomString();
            tokenQueue.offer(new NewTokenRequest(customerID,token));
            tokenCache.put(token,customerID);
            return token;
        }

        return null;
    }

    public boolean checkToken(String customer, String token) {
        String tokenOwner = tokenCache.get(token);
        return (tokenOwner!=null && tokenOwner.equals(customer));
    }


    public boolean addCustomerLogin(String customerID, String password) {
        if (loginCache.containsKey(customerID)) {
            return false;
        }

        loginQueue.add(new NewCustomerLoginRequest(customerID,password));
        loginCache.put(customerID,password);
        return true;
    }


    /**
     * Pull data from database to bring customerCache up to date
     */
    private void pullData() {
        try {
            if (!connected) {
                this.connectToDatabase();
            }

            pullCustomerData();
            pullLoginData();
            pullTokenData();
            pullOrderData();
        } catch (RemoteException e) {
            System.out.println("Cannot load new data from database, keep using cached data");
            e.printStackTrace();
            connected = false;   // connection lost so remove reference to supplier, reacquire later
        }
    }

    private void pullCustomerData() throws RemoteException {
        synchronized (customerDataVersion) {
            DataPatch<HashMap<String, CustomerInfo>> dataPatch = database.getCustomerDBDataPatch(customerDataVersion);
            if (dataPatch == null) {
                // up to date
            } else {
                for (Map.Entry<String, CustomerInfo> entry : dataPatch.getData().entrySet()) {
                    customerCache.put(entry.getKey(), entry.getValue());
                }
                customerDataVersion = dataPatch.getDataVersion();
            }
        }
    }


    private void pullLoginData() throws RemoteException {
        synchronized (loginDataVersion) {
            DataPatch<HashMap<String, String>> dataPatch = database.getLoginDBDataPatch(loginDataVersion);
            if (dataPatch == null) {
                // up to date
            } else {
                for (Map.Entry<String, String> entry : dataPatch.getData().entrySet()) {
                    loginCache.put(entry.getKey(), entry.getValue());
                }
                loginDataVersion = dataPatch.getDataVersion();
            }
        }
    }

    private void pullTokenData() throws RemoteException {
        synchronized (tokenCache) {
            HashMap<String, String> tokens = database.getAllTokens();

            tokenCache.clear();
            tokenCache.putAll(tokens);
        }
    }

    private void pullOrderData() throws RemoteException {
        synchronized (orderDataVersion) {
            DataPatch<HashMap<String, Order>> dataPatch = database.getOrderDBDataPatch(orderDataVersion);
            if (dataPatch == null) {
                // up to date
            } else {
                for (Map.Entry<String, Order> entry : dataPatch.getData().entrySet()) {
                    orderCache.put(entry.getKey(), entry.getValue());
                }
                orderDataVersion = dataPatch.getDataVersion();
            }
        }
    }
    /**
     * Flush the all buffered to database. If unable to communicate with database,
     * then the request remains at the head of the queue. If communication is successful, but purchase is not
     * possible (i.e. out of stock, username taken), then request is removed and callback method to OrderingSystem
     * is called to signal the failure.
     */
    private void pushData() {
        if (!connected) {
            this.connectToDatabase();
        }

        try {
            pushNewCustomer();
            pushCustomerOrder();
            pushNewCustomerLogin();
            pushTokens();
            pushOrders();
        } catch (RemoteException e) {
            System.out.println("Unable to communicate with database to push requests");
            e.printStackTrace();
            connected = false;
        }
    }

    private void pushCustomerOrder() throws RemoteException {
        if (customerOrderQueue.isEmpty()) {
            return;
        }
        while (true) {
            synchronized (customerOrderQueue) {
                AddOrderToHistoryRequest request = customerOrderQueue.peek();
                if (request == null) {
                    // finished pushing all buffered requests
                    break;
                }

                boolean success = false;
                success = database.addOrderToCustomer(request.getCustomerID(), request.getOrderID());


                // purchase either was successful, or denied by supplier, so remove it from buffer
                customerOrderQueue.poll();
                if (!success) {
                    request.failed();
                    break;
                }
            }
        }
    }

    private void pushNewCustomer() throws RemoteException {
        if (newCustomerQueue.isEmpty()) {
            return;
        }
        while (true) {
            synchronized (newCustomerQueue) {
                NewCustomerRequest request = newCustomerQueue.peek();
                if (request == null) {
                    // finished pushing all buffered requests
                    break;
                }

                boolean success = false;
                success = database.addCustomer(request.getCustomerID(), request.getCustomerInfo());


                // purchase either was successful, or denied by supplier, so remove it from buffer
                newCustomerQueue.poll();
                if (!success) {
                    request.failed();
                    break;
                }
            }
        }
    }

    private void pushNewCustomerLogin() throws RemoteException {
        if (loginQueue.isEmpty()) {
            return;
        }
        while (true) {
            synchronized (loginQueue) {
                NewCustomerLoginRequest request = loginQueue.peek();
                if (request == null) {
                    // finished pushing all buffered requests
                    break;
                }

                boolean success = false;
                success = database.newCustomerLogin(request.getCustomerID(), request.getPassword());


                // purchase either was successful, or denied by supplier, so remove it from buffer
                loginQueue.poll();
                if (!success) {
                    request.failed();
                    break;
                }
            }
        }
    }

    private void pushTokens() throws RemoteException {
        if (tokenQueue.isEmpty()) {
            return;
        }
        while (true) {
            synchronized (tokenQueue) {
                NewTokenRequest request = tokenQueue.peek();
                if (request == null) {
                    // finished pushing all buffered requests
                    break;
                }

                String customerID = request.getCustomerID();
                boolean success = false;
                success = database.putToken(customerID, loginCache.get(customerID), request.getToken());


                // purchase either was successful, or denied by supplier, so remove it from buffer
                loginQueue.poll();
                if (!success) {
                    request.failed();
                    break;
                }
            }
        }
    }

    private void pushOrders() throws RemoteException {
        if (orderQueue.isEmpty()) {
            return;
        }
        while (true) {
            synchronized (orderQueue) {
                NewOrderRequest request = orderQueue.peek();
                if (request == null) {
                    // finished pushing all buffered requests
                    break;
                }

                boolean success = false;
                success = database.putOrder(request.getOrder());


                // purchase either was successful, or denied by supplier, so remove it from buffer
                orderQueue.poll();
                if (!success) {
                    request.failed();
                    break;
                }
            }
        }
    }

    private void connectToDatabase() {
        try {
            database = RemoteConnect.getInstance().getDatabase();
            System.out.println("Connected to database");
            connected = true;
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a Runnable that loops and periodically tries to push the buffer to the database, and update the cache
     */
    private Runnable getRunnable() {
        return new Runnable() {

            @Override
            public void run() {
                while (true) {
                    pushData();
                    pullData();
                    try {
                        Thread.sleep(WAIT_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }
}
