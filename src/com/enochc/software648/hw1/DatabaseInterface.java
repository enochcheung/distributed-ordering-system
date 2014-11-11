package com.enochc.software648.hw1;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

/**
 * DatabaseInterface is a remote object accessed through RMI that contains customer
 * and order data for the OrderingSystem
 */
public interface DatabaseInterface extends Remote{


    public DataPatch<HashMap<String, CustomerInfo>> getCustomerDBDataPatch(String customerDBVersionID) throws RemoteException;

    public boolean hasCustomer(String customerID) throws RemoteException;

    public boolean addCustomer(String customerID, CustomerInfo customerInfo)throws RemoteException;

    public boolean addOrderToCustomer(String customerID, String orderID)throws RemoteException;

    public CustomerInfo lookupCustomer(String customerID) throws RemoteException;

    public String lookupCustomerShipping(String customerID)throws RemoteException;

    public DataPatch<HashMap<String, String>> getLoginDBDataPatch(String loginDBVersionID) throws RemoteException;

    public String getToken(String customerID, String password)throws RemoteException;

    public HashMap<String, String> getAllTokens() throws RemoteException;

    public boolean putToken(String customerID, String password, String token) throws RemoteException;

    public boolean checkToken(String customerID, String token) throws RemoteException;

    public boolean newCustomerLogin(String customerID, String password) throws RemoteException;

    public Order lookupOrder(String orderID)throws RemoteException;

    public boolean putOrder(Order order) throws RemoteException;

    public DataPatch<HashMap<String, Order>> getOrderDBDataPatch(String orderDBVersion) throws RemoteException;
}
