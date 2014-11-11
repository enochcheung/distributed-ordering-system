package com.enochc.software648.hw1;

import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Properties;

public class RemoteConnect {
    private static final String SETTINGS_FILE = "settings.properties";
    private String databasehost;
    private int databaseport;
    private int system1port;
    private String system1host;
    private String system2host;
    private int system2port;
    private String supplier1host;
    private int supplier1port;
    private String supplier2host;
    private int supplier2port;
    private static RemoteConnect instance = null;


    public static RemoteConnect getInstance() {
        if (instance == null) {
            instance = new RemoteConnect();
        }
        return instance;
    }

    private RemoteConnect() {
        try {
            Properties prop = new Properties();
            FileInputStream in = new FileInputStream(SETTINGS_FILE);
            prop.load(in);
            system1host = prop.getProperty("orderingsystem1.host");
            system1port = Integer.parseInt(prop.getProperty("orderingsystem1.port"));
            system2host = prop.getProperty("orderingsystem2.host");
            system2port = Integer.parseInt(prop.getProperty("orderingsystem2.port"));

            supplier1host = prop.getProperty("supplier1.host");
            supplier1port = Integer.parseInt(prop.getProperty("supplier1.port"));
            supplier2host = prop.getProperty("supplier2.host");
            supplier2port = Integer.parseInt(prop.getProperty("supplier2.port"));

            databasehost = prop.getProperty("database.host");
            databaseport = Integer.parseInt(prop.getProperty("database.port"));

            in.close();
        } catch (IOException e) {
            System.out.println("Unable to load settings.");
            e.printStackTrace();
        }


        this.instance = this;
    }


    public OrderingSystemInterface getOrderingSystem1() throws RemoteException, NotBoundException {
        OrderingSystemInterface orderingSystem1 = null;

        Registry registry = LocateRegistry.getRegistry(system1host, system1port);
        orderingSystem1 = (OrderingSystemInterface) registry.lookup("OrderingSystem1");

        return orderingSystem1;
    }

    public OrderingSystemInterface getOrderingSystem2() throws RemoteException, NotBoundException {
        OrderingSystemInterface orderingSystem2 = null;

        Registry registry = LocateRegistry.getRegistry(system2host, system2port);
        orderingSystem2 = (OrderingSystemInterface) registry.lookup("OrderingSystem2");

        return orderingSystem2;
    }

    public SupplierInterface getSupplier1() throws RemoteException, NotBoundException {
        SupplierInterface supplier1 = null;

        Registry registry = LocateRegistry.getRegistry(supplier1host, supplier1port);
        supplier1 = (SupplierInterface) registry.lookup("Supplier1");

        return supplier1;
    }


    public SupplierInterface getSupplier2() {

        SupplierInterface supplier2 = null;

        try {
            Registry registry = LocateRegistry.getRegistry(supplier2host, supplier2port);
            supplier2 = (SupplierInterface) registry.lookup("Supplier2");
        } catch (NotBoundException | RemoteException e) {
            System.out.println("Unable to connect to Supplier2");
            e.printStackTrace();
        }
        return supplier2;
    }

    public DatabaseInterface getDatabase() throws RemoteException, NotBoundException {

        DatabaseInterface database = null;


        Registry registry = LocateRegistry.getRegistry(databasehost, databaseport);
        database = (DatabaseInterface) registry.lookup("Database");

        return database;
    }

    public ArrayList<OrderingSystemInterface> getOrderingSystems() throws RemoteException, NotBoundException {
        ArrayList<OrderingSystemInterface> list = new ArrayList<OrderingSystemInterface>();
        list.add(this.getOrderingSystem1());
        list.add(this.getOrderingSystem2());
        return list;
    }

    public int getOrderingSystem1Port() {
        return system1port;
    }

    public String getOrderingSystem1Host() {
        return system1host;
    }

    public String getOrderingSystem2Host() {
        return system2host;
    }

    public int getOrderingSystem2Port() {
        return system2port;
    }

    public String getDatabasehost() {
        return databasehost;
    }

    public int getDatabasePort() {
        return databaseport;
    }
}
