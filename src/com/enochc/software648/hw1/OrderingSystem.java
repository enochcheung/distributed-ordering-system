package com.enochc.software648.hw1;

import java.io.*;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderingSystem extends UnicastRemoteObject implements OrderingSystemInterface {
    private static final String SETTINGS_FILE = "settings.properties";
    private static final Pattern itemNumberPattern = Pattern
            .compile("^([a-zA-Z0-9]{2})-([0-9]{2}-[0-9]{4}$)");
    private final Map<String, SupplierInterface> suppliersMap;
    private final HashMap<String, BikeCache> suppliersCache;
    private final Map<String, String> suppliersPrefixMap;
    private final CustomerDB customerDB;
    private final CustomerLoginDB customerLoginDB;
    private final OrderDB orderDB;

    /**
     * Constructor that starts the OrderingSystem which is accesed through the console
     *
     * @param suppliers
     */
    public OrderingSystem(List<SupplierInterface> suppliers) throws RemoteException {
        this();
        System.out.println("Available suppliers: "
                + getAvailableSuppliers().toString());

        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.println("type \"help\" for help");

            String line = "";
            try {
                line = inputReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String[] args = line.split(" ");
            if (args.length == 0) {
                System.out.println("Invalid input.");
            } else if (args[0].equals("help")) {
                System.out.println("see readme.txt");
            } else if (args[0].equals("browse")) {
                parseBrowse(stripHead(args));
            } else if (args[0].equals("browseByPrice")) {
                browseByPrice(stripHead(args));
            } else if (args[0].equals("lookupBike")) {
                parseLookupBike(stripHead(args));
            } else if (args[0].equals("purchase")) {
                purchase(stripHead(args));
            } else if (args[0].equals("newCustomer")) {
                parseNewCustomer(stripHead(args));
            } else if (args[0].equals("lookupCustomer")) {
                parseLookupCustomer(stripHead(args));
            } else if (args[0].equals("lookupOrder")) {
                parseLookupOrder(stripHead(args));
            } else if (args[0].equals("completeOrder")) {
                parseCompleteOrder(stripHead(args));
            } else if (args[0].equals("orderHistory")) {
                parseOrderHistory(stripHead(args));
            } else if (args[0].equals("listCustomers")) {
                parseListCustomers(stripHead(args));
            } else if (args[0].equals("quit") && args.length == 1) {
                break;
            } else {
                System.out.println("Invalid input.");
            }
        }
    }

    /**
     * Constructs a new OrderingSystem to be accessed through a servlet
     */
    public OrderingSystem() throws RemoteException {
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
        customerDB = new CustomerDB();
        customerLoginDB = new CustomerLoginDB();
        orderDB = new OrderDB();

        // look up suppliers
        List<SupplierInterface> suppliers = new ArrayList<SupplierInterface>();
        try {
            Registry registry1 = LocateRegistry.getRegistry(host1, port1);
            SupplierInterface supplier1 = (SupplierInterface) registry1.lookup("Supplier1");

            suppliers.add(supplier1);
            String name1 = supplier1.getName();
            suppliersMap.put(name1, supplier1);
            suppliersPrefixMap.put(supplier1.getSupplierPrefix(), name1);
            BikeCache bikeCache = new BikeCache(supplier1, host1,port1,"Supplier1");
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
            BikeCache bikeCache = new BikeCache(supplier2, host2,port2,"Supplier2");
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
    public String purchase(String customerID, String itemNum, int quantity) {
        CustomerInfo customer = this.lookupCustomer(customerID);
        Bike bike = this.lookupBike(itemNum);
        String supplierName = this.extractSupplierName(itemNum);
        BikeCache cache = suppliersCache.get(supplierName);

        if (customer==null||bike==null) {
            return null;
        }

        if (quantity > bike.getInventory()) {
            return null;
        }

        int price = bike.getPrice()*quantity;
        String orderID = orderDB.newOrder(customerID,itemNum,bike.getName(),quantity,price);
        customerDB.addOrder(customerID,orderID);
        PurchaseRequest request = new PurchaseRequest(itemNum,quantity,orderID,this);
        cache.purchase(request);

        return orderID;
    }

    private void purchase(String[] args) {
        if (args.length != 4) {
            System.out
                    .println("Invalid number of parameters. Format: purchase supplier_name item_number quantity customerID");
            return;
        }
        SupplierInterface supplier = suppliersMap.get(args[0]);
        if (supplier == null) {
            System.out.println(args[0] + " is not a valid supplier.");
            return;
        }

        try {
            // Find item
            String itemNumber = args[1];
            if (!supplier.validItemNumber(itemNumber)) {
                System.out.println(args[1] + " not found.");
                return;
            }

            String bikeName = supplier.lookupName(itemNumber);
            int pricePerBike = supplier.lookupPrice(itemNumber);
            int inventory = supplier.lookupInventory(itemNumber);

            // Determine quantity
            int quantity = 0;
            try {
                quantity = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                System.out.println(args[1] + " is not a valid quantity.");
                return;
            }

            if (quantity > inventory) {
                System.out.println("Item not in stock. Quantity requested: "
                        + quantity + " Inventory: " + inventory);
                return;
            }

            String customerID = args[3];
            if (!customerDB.hasCustomer(customerID)) {
                System.out
                        .println("Customer "
                                + customerID
                                + " not found. (Use newCustomer to add customer first)");
                return;
            }

            int price = pricePerBike * quantity;

            // make purchase
            boolean success = supplier.purchase(itemNumber, quantity);
            if (success) {
                // add to OrdersDB
                String orderID = orderDB.newOrder(customerID, itemNumber,
                        bikeName, quantity, price);
                System.out.println("Purchase successful! OrderID: " + orderID);

                // add order to CustomerDB
                customerDB.addOrder(customerID, orderID);
            } else {
                System.out.println("Purchase unsuccessful.");
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void parseNewCustomer(String[] args) {
        if (args.length != 1) {
            System.out
                    .println("Invalid number of parameters. Format: newCustomer");
            return;
        }

        String customerID = args[0];
        if (customerDB.hasCustomer(customerID)) {
            System.out.println(customerID + " already taken.");
            return;
        }
        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("First name?");
            String firstname = inputReader.readLine();
            System.out.println("Last name?");
            String lastname = inputReader.readLine();

            System.out.println("Street address?");
            String street = inputReader.readLine();

            System.out.println("City?");
            String city = inputReader.readLine();

            System.out.println("State?");
            String state = inputReader.readLine();

            System.out.println("Zip code?");
            String zipcode = inputReader.readLine();

            newCustomer(customerID, "hunter2", firstname,
                    lastname, street, city, state, zipcode);
            System.out.println("Customer " + customerID + " added.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean newCustomer(String customerID, String password, String firstname, String lastname, String street,
                               String city, String state, String zipcode) {
        if (customerDB.hasCustomer(customerID) || customerLoginDB.hasCustomer(customerID)) {
            System.out.println(customerID + " already taken.");
            return false;
        }
        CustomerInfo customerInfo = new CustomerInfo(customerID, firstname,
                lastname, street, city, state, zipcode);
        customerDB.addCustomer(customerID, customerInfo);
        customerLoginDB.addCustomer(customerID, password);
        System.out.println("Customer " + customerID + " added.");
        return true;
    }

    @Override
    public String getToken(String customerID, String password) {
        return customerLoginDB.getToken(customerID, password);
    }

    @Override
    public boolean checkToken(String customerID, String token) {
        return customerLoginDB.checkToken(customerID,token);
    }

    @Override
    public CustomerInfo lookupCustomer(String customerID) {
        return customerDB.lookup(customerID);
    }

    private void parseLookupCustomer(String[] args) {
        if (args.length != 1) {
            System.out
                    .println("Invalid number of parameters. Format: lookupCustomer customerID");
            return;
        }
        String customerID = args[0];

        CustomerInfo customerInfo = customerDB.lookup(customerID);
        if (customerInfo == null) {
            System.out.println("Customer " + customerID
                    + " not found. (Use newCustomer to add customer first)");
            return;
        }
        System.out.println(customerInfo.toString() + "\n");
    }

    private void parseLookupOrder(String[] args) {
        if (args.length != 1) {
            System.out
                    .println("Invalid number of parameters. Format: lookupOrder orderID");
            return;
        }

        String orderID = args[0];
        Order order = orderDB.lookupOrder(orderID);
        if (order == null) {
            System.out.println(orderID + " not found.");
            return;
        }

        String customerID = order.getCustomerID();
        System.out.println(String.format("OrderID: %s%n%s", orderID,
                order.toString()));

        String shippingInfo = customerDB.lookupShipping(customerID);
        if (shippingInfo == null) {
            System.out.println("Shipping info not found.");
            return;
        }
        System.out.println("Shipping Info:");
        System.out.println(shippingInfo);
        System.out.println();

    }

    @Override
    public Order lookupOrder(String orderID) throws RemoteException {
        return orderDB.lookupOrder(orderID);
    }


    @Override
    public void completeOrder(String orderID) throws RemoteException {
        orderDB.completeOrder(orderID);
    }

    @Override
    public String lookupShipping(String customerID) {
        return customerDB.lookupShipping(customerID);
    }

    private void parseCompleteOrder(String[] args) {
        if (args.length != 1) {
            System.out
                    .println("Invalid number of parameters. Format: completeOrder orderID");
            return;
        }

        String orderID = args[0];
        if (!orderDB.validOrderID(orderID)) {
            System.out.println(orderID + " not found.");
            return;
        }
        orderDB.completeOrder(orderID);
        System.out.println("Order completed!");
        parseLookupOrder(args);

    }

    private void parseOrderHistory(String[] args) {
        if (args.length != 1) {
            System.out
                    .println("Invalid number of parameters. Format: orderHistory customerID");
            return;
        }
        String customerID = args[0];

        CustomerInfo customerInfo = customerDB.lookup(customerID);
        if (customerInfo == null) {
            System.out.println("Customer " + customerID
                    + " not found. (Use newCustomer to add customer first)");
            return;
        }
        List<String> orderHistory = customerInfo.orderHistory();
        for (String orderID : orderHistory) {
            String[] orderIDArray = new String[1];
            orderIDArray[0] = orderID;
            parseLookupOrder(orderIDArray);
        }
    }

    @Override
    public ArrayList<Order> orderHistory(String customerID) throws RemoteException {
        CustomerInfo customerInfo = customerDB.lookup(customerID);
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
        System.out.println(customerDB.listCustomers().toString());
    }

    @Override
    public ArrayList<String> listCustomers() throws RemoteException {
        return new ArrayList<String>(customerDB.listCustomers());
    }

    public void notifyFailed(String orderID) {
        orderDB.failedOrder(orderID);

    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("console")) {
            consoleMain(args);
            return;
        }

        // look up host and port of target RMI registry
        int port = 0;
        String host = "";
        try {
            Properties prop = new Properties();
            FileInputStream in = new FileInputStream(SETTINGS_FILE);
            prop.load(in);
            port = Integer.parseInt(prop.getProperty("orderingsystem.port"));
            host = prop.getProperty("orderingsystem.host");

            in.close();
        } catch (IOException e) {
            System.out.println("Unable to open " + SETTINGS_FILE);
        }

        // create the registry
        Registry registry = null;

        try {
            registry = LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        // bind orderingsystem to registry
        try {
            OrderingSystemInterface orderingSystem = new OrderingSystem();
            registry.bind("OrderingSystem", orderingSystem);
            System.out.println("OrderingSystem started.");

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }


    }

    public static void consoleMain(String[] args) {
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
            e.printStackTrace();
        }

        // look up suppliers

        List<SupplierInterface> suppliers = new ArrayList<SupplierInterface>();
        try {
            Registry registry1 = LocateRegistry.getRegistry(host1, port1);
            suppliers.add((SupplierInterface) registry1.lookup("Supplier1"));
        } catch (NotBoundException | RemoteException e) {
            System.out.println("Unable to connect to Supplier1");
            e.printStackTrace();
        }

        try {
            Registry registry2 = LocateRegistry.getRegistry(host2, port2);
            suppliers.add((SupplierInterface) registry2.lookup("Supplier2"));
        } catch (NotBoundException | RemoteException e) {
            System.out.println("Unable to connect to Supplier2");
            e.printStackTrace();
        }

        try {
            OrderingSystem orderingSystem = new OrderingSystem(suppliers);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}
