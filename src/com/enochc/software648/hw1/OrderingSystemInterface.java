package com.enochc.software648.hw1;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface OrderingSystemInterface extends Remote {


    /**
     * @return Set of available suppliers (by name)
     */
    public ArrayList<String> getAvailableSuppliers() throws RemoteException;

    /**
     * Browse all the bikes
     *
     * @return List of Bikes
     */
    public ArrayList<Bike> browse() throws RemoteException;

    public boolean hasBike(String itemNum) throws RemoteException;

    public Bike lookupBike(String itemNum) throws RemoteException;

    /**
     * Places an order on some bikes, returning the orderID.
     * If the order fails preliminary tests, then return null.
     * Note that the order might fail later on the supplier side, which will be reflected
     * on the order of the status, which can be found by querying the orderID
     *
     * @param customerID
     * @param itemNum
     * @param quantity
     * @return orderID of the order if the order is placed, null if it fails
     * @throws RemoteException
     */
    public String purchase(String customerID, String itemNum, int quantity) throws RemoteException;

    /**
     * @param customerID
     * @param firstname
     * @param lastname
     * @param street
     * @param city
     * @param state
     * @param zipcode
     * @return True if successful, false if customer name already taken
     */
    public boolean newCustomer(String customerID, String firstname, String lastname, String street,
                               String city, String state, String zipcode) throws RemoteException;


    public CustomerInfo lookupCustomer(String customerID) throws RemoteException;

    /**
     * @param orderID
     * @return Order as specified by orderID. Null if not found.
     */
    public Order lookupOrder(String orderID) throws RemoteException;

    public void completeOrder(String orderID) throws RemoteException;

    /**
     * @param customerID
     * @return Shipping info for given customerID. Null if not found.
     */
    public String lookupShipping(String customerID) throws RemoteException;

    /**
     * @param customerID
     * @return List of Orders for given customerID. Null if customer not found.
     */
    public ArrayList<Order> orderHistory(String customerID) throws RemoteException;

    /**
     * @return List of customers (by customerID)
     */
    public ArrayList<String> listCustomers() throws RemoteException;


}
