package com.enochc.software648.hw1;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderingSystem {
    private static final String SETTINGS_FILE = "settings.properties";
    private final Map<String, SupplierInterface> suppliersMap;
    private final CustomerDB customerDB;
    private final OrderDB orderDB;

    /**
     * Constructor that starts the OrderingSystem which is accesed through the console
     *
     * @param suppliers
     */
    public OrderingSystem(List<SupplierInterface> suppliers) {
        suppliersMap = new HashMap<String, SupplierInterface>();
        customerDB = new CustomerDB();
        orderDB = new OrderDB();

        try {
            for (SupplierInterface supplier : suppliers) {
                suppliersMap.put(supplier.getName(), supplier);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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
                lookupBike(stripHead(args));
            } else if (args[0].equals("purchase")) {
                purchase(stripHead(args));
            } else if (args[0].equals("newCustomer")) {
                newCustomer(stripHead(args));
            } else if (args[0].equals("lookupCustomer")) {
                lookupCustomer(stripHead(args));
            } else if (args[0].equals("lookupOrder")) {
                lookupOrder(stripHead(args));
            } else if (args[0].equals("completeOrder")) {
                completeOrder(stripHead(args));
            } else if (args[0].equals("orderHistory")) {
                orderHistory(stripHead(args));
            } else if (args[0].equals("listCustomers")) {
                listCustomers(stripHead(args));
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
    public OrderingSystem() {
        // load settings
        String host1 = "", host2 = "";
        int port1 = 0, port2 = 0;
        try {
            Properties prop = new Properties();
            FileInputStream in = new FileInputStream(SETTINGS_FILE);
            prop.load(in);
            host1 = prop.getProperty("host1");
            port1 = Integer.parseInt(prop.getProperty("port1"));
            host2 = prop.getProperty("host2");
            port2 = Integer.parseInt(prop.getProperty("port2"));

            in.close();
        } catch (IOException e) {
        }

        // look up suppliers
        List<SupplierInterface> suppliers = new ArrayList<SupplierInterface>();
        try {
            Registry registry1 = LocateRegistry.getRegistry(host1, port1);
            suppliers.add((SupplierInterface) registry1.lookup("Supplier1"));
        } catch (NotBoundException | RemoteException e) {
            System.out.println("Unable to connect to Supplier1");
        }

        try {
            Registry registry2 = LocateRegistry.getRegistry(host2, port2);
            suppliers.add((SupplierInterface) registry2.lookup("Supplier2"));
        } catch (NotBoundException | RemoteException e) {
            System.out.println("Unable to connect to Supplier2");
        }

        // initiate and populate fields
        suppliersMap = new HashMap<String, SupplierInterface>();
        customerDB = new CustomerDB();
        orderDB = new OrderDB();

        try {
            for (SupplierInterface supplier : suppliers) {
                suppliersMap.put(supplier.getName(), supplier);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public Set<String> getAvailableSuppliers() {
        return suppliersMap.keySet();
    }

    private String[] stripHead(String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
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

    private void lookupBike(String[] args) {
        if (args.length != 2) {
            System.out
                    .println("Invalid number of parameters. Format: lookupBike supplier_name item_number");
            return;
        }
        SupplierInterface supplier = suppliersMap.get(args[0]);
        if (supplier == null) {
            System.out.println(args[0] + " is not a valid supplier.");
            return;
        }

        try {

            if (!supplier.validItemNumber(args[1])) {
                System.out.println(args[1] + " not found.");
                return;
            }

            Bike bike = supplier.lookupBike(args[1]);
            System.out.print(bike.toString());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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

    private void newCustomer(String[] args) {
        if (args.length != 1) {
            System.out
                    .println("Invalid number of parameters. Format: listCustomers");
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

            CustomerInfo customerInfo = new CustomerInfo(customerID, firstname,
                    lastname, street, city, state, zipcode);
            customerDB.addCustomer(customerID, customerInfo);

            System.out.println("Customer " + customerID + " added.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void lookupCustomer(String[] args) {
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

    private void lookupOrder(String[] args) {
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

    private void completeOrder(String[] args) {
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
        lookupOrder(args);

    }

    private void orderHistory(String[] args) {
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
            lookupOrder(orderIDArray);
        }
    }

    private void listCustomers(String[] args) {
        if (args.length != 0) {
            System.out
                    .println("Invalid number of parameters. Format: orderHistory customerID");
            return;
        }
        System.out.println(customerDB.listCustomers().toString());
    }

    public static void main(String[] args) {
        // load settings
        String host1 = "", host2 = "";
        int port1 = 0, port2 = 0;
        try {
            Properties prop = new Properties();
            FileInputStream in = new FileInputStream(SETTINGS_FILE);
            prop.load(in);
            host1 = prop.getProperty("host1");
            port1 = Integer.parseInt(prop.getProperty("port1"));
            host2 = prop.getProperty("host2");
            port2 = Integer.parseInt(prop.getProperty("port2"));

            in.close();
        } catch (IOException e) {
        }

        // look up suppliers

        List<SupplierInterface> suppliers = new ArrayList<SupplierInterface>();
        try {
            Registry registry1 = LocateRegistry.getRegistry(host1, port1);
            suppliers.add((SupplierInterface) registry1.lookup("Supplier1"));
        } catch (NotBoundException | RemoteException e) {
            System.out.println("Unable to connect to Supplier1");
        }

        try {
            Registry registry2 = LocateRegistry.getRegistry(host2, port2);
            suppliers.add((SupplierInterface) registry2.lookup("Supplier2"));
        } catch (NotBoundException | RemoteException e) {
            System.out.println("Unable to connect to Supplier2");
        }

        OrderingSystem orderingSystem = new OrderingSystem(suppliers);

    }
}
