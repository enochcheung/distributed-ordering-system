package com.enochc.software648.hw1;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Database extends UnicastRemoteObject implements DatabaseInterface{
    private static final long serialVersionUID = 8424653301383286359L;
    private final CustomerDB customerDB;
    private final CustomerLoginDB customerLoginDB;
    private final OrderDB orderDB;


    public Database() throws RemoteException {
        super();

        customerDB = new CustomerDB();
        customerLoginDB = new CustomerLoginDB();
        orderDB = new OrderDB();
    }

    @Override
    public DataPatch<HashMap<String, CustomerInfo>> getCustomerDBDataPatch(String customerDBVersionID) {
        return customerDB.getDataPatch(customerDBVersionID);
    }

    @Override
    public boolean hasCustomer(String customerID) {
        return customerDB.hasCustomer(customerID);
    }

    @Override
    public boolean addCustomer(String customerID, CustomerInfo customerInfo) {
        return customerDB.addCustomer(customerID, customerInfo);
    }

    @Override
    public boolean addOrderToCustomer(String customerID, String orderID) {
        return customerDB.addOrder(customerID, orderID);
    }

    @Override
    public CustomerInfo lookupCustomer(String customerID) {
        return customerDB.lookup(customerID);
    }

    @Override
    public String lookupCustomerShipping(String customerID) {
        return customerDB.lookupShipping(customerID);
    }

    @Override
    public DataPatch<HashMap<String, String>> getLoginDBDataPatch(String loginDBVersion) {
        return customerLoginDB.getDataPatch(loginDBVersion);
    }

    @Override
    public String getToken(String customerID, String password) {
        return customerLoginDB.getToken(customerID,password);
    }

    @Override
    public HashMap<String, String> getAllTokens() {
        return customerLoginDB.getAllTokens();
    }

    @Override
    public boolean putToken(String customerID, String password, String token) {
        return customerLoginDB.putToken(customerID, password, token);
    }


    @Override
    public boolean checkToken(String customerID, String token) {
        return customerLoginDB.checkToken(customerID, token);
    }

    @Override
    public boolean newCustomerLogin(String customerID, String password) {
        return customerLoginDB.addCustomer(customerID, password);
    }

    @Override
    public Order lookupOrder(String orderID) {
        return orderDB.lookupOrder(orderID);
    }

    @Override
    public boolean putOrder(Order order) {
        return orderDB.putOrder(order);
    }

    @Override
    public DataPatch<HashMap<String, Order>> getOrderDBDataPatch(String orderDBVersion) {
        return orderDB.getDataPatch(orderDBVersion);
    }

    public static void main(String[] args) {
        try {
            int port = RemoteConnect.getInstance().getDatabasePort();
            Registry registry = LocateRegistry.createRegistry(port);

            // bind this to registry

            DatabaseInterface database = new Database();
            registry.bind("Database", database);
            System.out.println("Database started.");
        } catch (RemoteException | AlreadyBoundException e) {
            e.printStackTrace();
        }

    }



}
