package com.enochc.software648.hw1;

import com.enochc.software648.hw1.request.PurchaseRequest;

import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class OrderingSystem extends UnicastRemoteObject implements OrderingSystemInterface {
    private static final long serialVersionUID = -7463687943606009814L;

    private static final String SETTINGS_FILE = "settings.properties";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "MM/dd/yyyy HH:mm");
    private static final Pattern itemNumberPattern = Pattern
            .compile("^([a-zA-Z0-9]{2})-([0-9]{2}-[0-9]{4}$)");
    private final Map<String, SupplierInterface> suppliersMap;
    private final HashMap<String, BikeCache> suppliersCache;
    private final Map<String, String> suppliersPrefixMap;
    private final ConcurrentHashMap<String, ArrayList<String>> inboxes = new ConcurrentHashMap<String,ArrayList<String>>();

    private final DatabaseConnector database;

    /**
     * Constructs a new OrderingSystem to be accessed through a servlet
     */
    public OrderingSystem() throws RemoteException {
        super();    //TODO is this needed?
        // load settings
        String host1 = "", host2 = "";
        int port1 = 0, port2 = 0;
        try {
            Properties prop = new Properties();
            FileInputStream in = new FileInputStream(SETTINGS_FILE);
            prop.load(in);
            host1 = prop.getProperty("supplier1.host");
            port1 = Integer.parseInt(prop.getProperty("supplier1.port"));
            host2 = prop.getProperty("supplier2.host");
            port2 = Integer.parseInt(prop.getProperty("supplier2.port"));

            in.close();
        } catch (IOException e) {
            System.out.println("Unable to load settings.");
            e.printStackTrace();
        }

        // initiate and populate fields
        suppliersMap = new HashMap<String, SupplierInterface>();
        suppliersPrefixMap = new HashMap<String, String>();
        suppliersCache = new HashMap<String, BikeCache>();
        database = new DatabaseConnector();

        // look up suppliers
        List<SupplierInterface> suppliers = new ArrayList<SupplierInterface>();
        try {
            Registry registry1 = LocateRegistry.getRegistry(host1, port1);
            SupplierInterface supplier1 = (SupplierInterface) registry1.lookup("Supplier1");

            suppliers.add(supplier1);
            String name1 = supplier1.getName();
            suppliersMap.put(name1, supplier1);
            suppliersPrefixMap.put(supplier1.getSupplierPrefix(), name1);
            BikeCache bikeCache = new BikeCache(supplier1, host1, port1, "Supplier1");
            suppliersCache.put(name1, bikeCache);

            // create new thread to run the bikeCache periodic update
            (new Thread(bikeCache.getRunnable())).start();

        } catch (NotBoundException | RemoteException e) {
            System.out.println("Unable to connect to Supplier1");
            e.printStackTrace();
        }

        try {
            Registry registry2 = LocateRegistry.getRegistry(host2, port2);
            SupplierInterface supplier2 = (SupplierInterface) registry2.lookup("Supplier2");

            suppliers.add(supplier2);

            String name2 = supplier2.getName();
            suppliersMap.put(name2, supplier2);
            suppliersPrefixMap.put(supplier2.getSupplierPrefix(), name2);
            BikeCache bikeCache = new BikeCache(supplier2, host2, port2, "Supplier2");
            suppliersCache.put(name2, bikeCache);

            // create new thread to run the bikeCache periodic update
            (new Thread(bikeCache.getRunnable())).start();
        } catch (NotBoundException | RemoteException e) {
            System.out.println("Unable to connect to Supplier2");
            e.printStackTrace();
        }


    }

    @Override
    public ArrayList<String> getAvailableSuppliers() throws RemoteException {
        ArrayList<String> list = new ArrayList<String>(suppliersMap.keySet());
        Collections.sort(list);
        return list;
    }

    private String[] stripHead(String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }

    /**
     * @return SupplierInterface for supplier holding given item number. Null if not found.
     */
    private String extractSupplierName(String itemNumber) {
        Matcher matcher = itemNumberPattern.matcher(itemNumber);
        if (!matcher.matches()) {
            return null;
        }
        String prefix = matcher.group(1);
        return suppliersPrefixMap.get(prefix);
    }

    private void parseBrowse(String[] args) {
        if (args.length != 2) {
            System.out
                    .println("Invalid number of parameters. Format: browse supplier_name page_num");
            return;
        }
        String supplierName = args[0];
        if (args[1].equals("all")) {
            browseAll(supplierName);
        } else {
            try {
                int page = Integer.parseInt(args[1]);
                browsePage(supplierName, page);
            } catch (NumberFormatException e) {
                System.out.println(args[1] + " is not a valid page.");
            }
        }
    }

    @Override
    public ArrayList<Bike> browse() {
        ArrayList<Bike> bikes = new ArrayList<Bike>();
        for (String supplierName : suppliersMap.keySet()) {
            bikes.addAll(browseSupplier(supplierName));
        }
        return bikes;
    }

    /**
     * Returns the inventory of given supplier, either from the supplier or from the cache
     *
     * @param supplierName
     * @return
     */
    private ArrayList<Bike> browseSupplier(String supplierName) {
        ArrayList<Bike> bikes;
        SupplierInterface supplier = suppliersMap.get(supplierName);
        BikeCache cache = suppliersCache.get(supplierName);
        return cache.browseBikes();
    }

    private void browseAll(String supplierName) {
        SupplierInterface supplier = suppliersMap.get(supplierName);
        if (supplier == null) {
            System.out.println(supplierName + " is not a valid supplier.");
            return;
        }
        try {
            int totalPages = supplier.getNumPages();
            for (int i = 1; i <= totalPages; i++) {
                System.out.print(supplier.browsePage(i));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void browsePage(String supplierName, int pageNum) {
        SupplierInterface supplier = suppliersMap.get(supplierName);
        if (supplier == null) {
            System.out.println(supplierName + " is not a valid supplier.");
            return;
        }
        try {
            System.out.print(supplier.browsePage(pageNum));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void browseByPrice(String[] args) {
        if (args.length != 2) {
            System.out
                    .println("Invalid number of parameters. Format: browseByPrice supplier_name page_num");
            return;
        }
        SupplierInterface supplier = suppliersMap.get(args[0]);
        if (supplier == null) {
            System.out.println(args[0] + " is not a valid supplier.");
            return;
        }

        try {
            if (args[1].equals("all")) {
                int totalPages = supplier.getNumPages();
                for (int i = 1; i <= totalPages; i++) {
                    System.out.print(supplier.browseByPricePage(i));
                }

            } else {
                try {
                    int page = Integer.parseInt(args[1]);
                    System.out.print(supplier.browseByPricePage(page));
                } catch (NumberFormatException e) {
                    System.out.println(args[1] + " is not a valid page.");
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void parseLookupBike(String[] args) {
        if (args.length != 1) {
            System.out
                    .println("Invalid number of parameters. Format: lookupBike item_number");
            return;
        }


        Bike bike = lookupBike(args[0]);

        if (bike == null) {
            System.out.println(args[0] + " not found.");
            return;
        }

        System.out.print(bike.toString());

    }

    @Override
    public boolean hasBike(String itemNum) throws RemoteException {
        return lookupBike(itemNum) != null;
    }

    @Override
    public Bike lookupBike(String itemNum) {
        System.out.println("Searching for " + itemNum);
        String supplierName = extractSupplierName(itemNum);
        if (supplierName == null) {
            return null;
        }
        BikeCache cache = suppliersCache.get(supplierName);

        return cache.lookupBike(itemNum);
    }

    @Override
    public void purchase(String customerID, HashMap<String, Integer> bikeQuantities)
            throws CustomerNotFoundException, BikeNotFoundException, InsufficientInventoryException {
        CustomerInfo customer = this.lookupCustomer(customerID);
        if (customer == null) {
            throw new CustomerNotFoundException(customerID);
        }

        HashMap<String, Order> orders = new HashMap<String, Order>();
        HashMap<String, PurchaseRequest> purchaseRequests = new HashMap<String, PurchaseRequest>();

        for (Map.Entry<String, Integer> entry : bikeQuantities.entrySet()) {
            String itemNum = entry.getKey();
            int quantity = entry.getValue();
            Bike bike = this.lookupBike(itemNum);
            String supplierName = this.extractSupplierName(itemNum);

            if (bike == null || supplierName == null) {
                throw new BikeNotFoundException(itemNum);
            }

            if (quantity > bike.getInventory()) {
                throw new InsufficientInventoryException(itemNum);
            }

            PurchaseRequest request = purchaseRequests.get(supplierName);
            Order order = orders.get(supplierName);
            if (request == null) {
                String orderID = database.getNewOrderID();
                String date = this.getDate();
                order = new Order(customerID, date, orderID);
                request = new PurchaseRequest(customerID, orderID, supplierName, this);
                purchaseRequests.put(supplierName, request);
                orders.put(supplierName, order);
            }

            request.addBike(itemNum, quantity);
            order.addBike(bike, quantity);
        }

        // all bikes added to requests and orders, partitioned by suppliers
        // add order to orderDB, and queue up requests onto bikeCache, for each supplier
        for (String supplierName : purchaseRequests.keySet()) {
            BikeCache bikeCache = suppliersCache.get(supplierName);
            PurchaseRequest purchaseRequest = purchaseRequests.get(supplierName);
            Order order = orders.get(supplierName);
            String orderID = order.getOrderID();
            database.addOrderToCustomer(customerID, orderID);
            database.putOrder(order);

            bikeCache.purchase(purchaseRequest);

        }
    }

    private String getDate(){
        return dateFormat.format(new Date());
    }

    @Override
    public boolean newCustomer(String customerID, String password, String firstname, String lastname, String street,
                               String city, String state, String zipcode) {
        if (database.hasCustomer(customerID) || database.hasCustomerLogin(customerID)) {
            System.out.println(customerID + " already taken.");
            return false;
        }
        CustomerInfo customerInfo = new CustomerInfo(customerID, firstname,
                lastname, street, city, state, zipcode);
        database.addCustomer(customerID, customerInfo);
        database.addCustomerLogin(customerID, password);
        System.out.println("Customer " + customerID + " added.");
        return true;
    }

    @Override
    public String getToken(String customerID, String password) {
        return database.getToken(customerID, password);
    }

    @Override
    public boolean checkToken(String customerID, String token) {
        return database.checkToken(customerID, token);
    }

    @Override
    public CustomerInfo lookupCustomer(String customerID) {
        return database.lookupCustomer(customerID);
    }

    private void parseLookupCustomer(String[] args) {
        if (args.length != 1) {
            System.out
                    .println("Invalid number of parameters. Format: lookupCustomer customerID");
            return;
        }
        String customerID = args[0];

        CustomerInfo customerInfo = database.lookupCustomer(customerID);
        if (customerInfo == null) {
            System.out.println("Customer " + customerID
                    + " not found. (Use newCustomer to add customer first)");
            return;
        }
        System.out.println(customerInfo.toString() + "\n");
    }

    @Override
    public Order lookupOrder(String orderID) throws RemoteException {
        return database.lookupOrder(orderID);
    }

    private void sendMessage(String customerID, String message) {
        ArrayList<String> inbox = inboxes.get(customerID);
        if (inbox== null) {
            inbox = new ArrayList<String>();
            inboxes.put(customerID,inbox);
        }

        inbox.add(message);
    }

    @Override
    public ArrayList<String> getMessages(String customerID) {
        ArrayList<String> inbox = inboxes.get(customerID);
        if (inbox == null) {
            inbox = new ArrayList<String>();
        }
        return inbox;
    }

    @Override
    public void completeOrder(String orderID) throws RemoteException {
        Order order = database.lookupOrder(orderID);
        if (order != null) {
            database.completeOrder(orderID);

            String customerID = order.getCustomerID();

            StringBuilder sb = new StringBuilder();
            sb.append("Your order for ");
            HashMap<String, String> bikeNames = order.getBikeNames();
            for (String bikeName : bikeNames.values()) {
                sb.append(bikeName+", ");
            }
            sb.append(" has completed!");
            String message = sb.toString();
            this.sendMessage(customerID, message);

            System.out.println("Order " + orderID + " completed!");
        }
    }

    @Override
    public String lookupShipping(String customerID) {
        return database.lookupShipping(customerID);
    }

    @Override
    public ArrayList<Order> orderHistory(String customerID) throws RemoteException {
        CustomerInfo customerInfo = database.lookupCustomer(customerID);
        if (customerInfo == null) {
            System.out.println("Customer " + customerID
                    + " not found. (Use newCustomer to add customer first)");
            return null;
        }
        List<String> orderHistory = customerInfo.orderHistory();
        ArrayList<Order> ordersList = new ArrayList<Order>();
        for (String orderID : orderHistory) {
            ordersList.add(lookupOrder(orderID));
        }
        return ordersList;

    }

    private void parseListCustomers(String[] args) {
        if (args.length != 0) {
            System.out
                    .println("Invalid number of parameters. Format: orderHistory customerID");
            return;
        }
        System.out.println(database.listCustomers().toString());
    }

    @Override
    public ArrayList<String> listCustomers() throws RemoteException {
        return new ArrayList<String>(database.listCustomers());
    }

    public void notifyFailed(String orderID) {
        database.failedOrder(orderID);

    }
}
