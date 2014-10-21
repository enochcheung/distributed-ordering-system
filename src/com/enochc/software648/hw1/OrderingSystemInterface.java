package com.enochc.software648.hw1;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
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
     * Places orders for bikes on the OrderingSystem.
     * Note that the order might fail later on the supplier side, which will be reflected
     * on the order of the status, which can be found by querying the order history of the customer
     *
     * @param customerID
     * @param bikeQuantities
     * @throws CustomerNotFoundException
     * @throws BikeNotFoundException
     * @throws InsufficientInventoryException
     * @throws RemoteException
     */
    public void purchase(String customerID, HashMap<String, Integer> bikeQuantities)
            throws CustomerNotFoundException, BikeNotFoundException, InsufficientInventoryException, RemoteException;

    /**
     * @param customerID
     * @param password
     * @param firstname
     * @param lastname
     * @param street
     * @param city
     * @param state
     * @param zipcode
     * @return True if successful, false if customer name already taken
     */
    public boolean newCustomer(String customerID, String password, String firstname, String lastname, String street,
                               String city, String state, String zipcode) throws RemoteException;


    /**
     * @param customerID
     * @param password
     * @return Token that can be used to authenticate a customer.
     *         Null if incorrect password or customer not found
     */
    public String getToken(String customerID, String password) throws RemoteException;

    /**
     *
     * @param customerID
     * @param token
     * @return true if matches, false if not
     */
    boolean checkToken(String customerID, String token) throws RemoteException;

    public CustomerInfo lookupCustomer(String customerID) throws RemoteException;

    /**
     * @param orderID
     * @return Order as specified by orderID. Null if not found.
     */
    public Order lookupOrder(String orderID) throws RemoteException;

    /**
     *
     * @param customerID
     * @return List of messages for give customer.
     */
    public ArrayList<String> getMessages(String customerID) throws RemoteException;

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
