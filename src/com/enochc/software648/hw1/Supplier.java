package com.enochc.software648.hw1;

import com.enochc.software648.hw1.request.PurchaseRequest;

import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Supplier extends UnicastRemoteObject implements SupplierInterface {
    private static final long serialVersionUID = -3072627641347153761L;
    private static final int PAGE_SIZE = 10;
    private static final long RETRY_DELAY = 3000L;

    private static final String SETTINGS_FILE = "settings.properties";

    private final SupplierData data;
    private final Map<String, Bike> inventory;
    private ArrayList<Bike> bikes;
    private ArrayList<Bike> bikesByPrice;
    private int numPages;
    private OrderingSystemInterface orderingSystem;
    private final String orderingHost;
    private final int orderingPort;

    private String currentDataVersion;
    private String oldDataVersion;
    private ArrayList<Bike> versionDifference;


    public Supplier(SupplierData data) throws RemoteException {
        super();
        this.data = data;
        inventory = new HashMap<String, Bike>();
        bikes = new ArrayList<Bike>();
        try {
            loadInventory();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String orderingHostTmp = "";
        int orderingPortTmp = 0;

        // load settings
        try {
            Properties prop = new Properties();
            FileInputStream in = new FileInputStream(SETTINGS_FILE);
            prop.load(in);
            orderingHostTmp = prop.getProperty("orderingsystem.host");
            orderingPortTmp = Integer.parseInt(prop.getProperty("orderingsystem.port"));

            in.close();
        } catch (IOException e) {
            System.out.println("Unable to load settings.");
            e.printStackTrace();
        }

        orderingHost = orderingHostTmp;
        orderingPort = orderingPortTmp;

    }

    /**
     * Complete an order after a random delay
     */
    private void completeOrderAfterDelay(final String orderID) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                boolean completed1 = false;
                boolean completed2 = false;

                Random random = new Random();
                long minute = 60000L;    // 1 minute
                long randomDelay = (long) (minute + random.nextDouble() * minute);
                try {
                    Thread.sleep(randomDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                while (!(completed1 && completed2)) {
                    if (!completed1) {
                        try {
                            OrderingSystemInterface orderingSystem1 = RemoteConnect.getInstance().getOrderingSystem1();

                            orderingSystem1.completeOrder(orderID);


                            completed1 = true;

                        } catch (NotBoundException | RemoteException e) {
                            System.out.println("Unable to connect to OrderingSystem1. Retry later.");
                            e.printStackTrace();

                        }
                    }

                    if (!completed2) {
                        try {
                            OrderingSystemInterface orderingSystem2 = RemoteConnect.getInstance().getOrderingSystem2();

                            orderingSystem2.completeOrder(orderID);


                            completed2 = true;

                        } catch (NotBoundException | RemoteException e) {
                            System.out.println("Unable to connect to OrderingSystem2. Retry later.");
                            e.printStackTrace();

                        }
                    }
                    try {
                        Thread.sleep(RETRY_DELAY);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }

            }
        };

        (new Thread(runnable)).start();

    }

    @Override
    public String getSupplierPrefix() {
        return data.getSupplierPrefix();
    }

    @Override
    public String getName() {
        return data.getName();
    }

    private synchronized void loadInventory() throws IOException {
        currentDataVersion = UUID.randomUUID().toString();
        Map<String, Integer> inventoryMap = data.getInventory();
        data.openReader();
        Bike bike;
        bike = data.readBike();
        while (bike != null) {
            Integer n = inventoryMap.get(bike.getItemNumber());
            if (n != null) {
                bike.setInventory(n);
            }

            inventory.put(bike.getItemNumber(), bike);
            bikes.add(bike);

            bike = data.readBike();
        }

        data.closeReader();

        numPages = (int) Math.ceil((double) bikes.size() / PAGE_SIZE);
    }

    @Override
    public ArrayList<Bike> browse() {
        return new ArrayList<Bike>(bikes);
    }

    @Override
    public String browsePage(int n) {
        StringBuilder sb = new StringBuilder();

        int start = (n - 1) * PAGE_SIZE;
        int end = Math.min(n * PAGE_SIZE, bikes.size());
        for (int i = start; i < end; i++) {
            sb.append(bikes.get(i).toString());
        }

        sb.append(String.format("-- Page %d of %d --%n%n", n, numPages));

        return sb.toString();
    }

    @Override
    public DataPatch<ArrayList<Bike>> getNewBikes(String dataVersion) {
        if ((dataVersion).equals(currentDataVersion)) {
            // version is already up to date
            return new DataPatch<ArrayList<Bike>>(new ArrayList<Bike>(), currentDataVersion);
        }
        if (dataVersion.equals(oldDataVersion)) {
            // version is oldDataVersion, use cached versionDifference
            return new DataPatch<ArrayList<Bike>>(versionDifference, currentDataVersion);
        }

        // version not found, return everything
        return new DataPatch<ArrayList<Bike>>(bikes, currentDataVersion);
    }

    @Override
    public String getDataVersion() {
        return currentDataVersion;
    }

    @Override
    public String browseByPricePage(int n) throws RemoteException {
        if (bikesByPrice == null) {
            makeBikesByPrice();
        }

        StringBuilder sb = new StringBuilder();

        int start = (n - 1) * PAGE_SIZE;
        int end = Math.min(n * PAGE_SIZE, bikesByPrice.size());
        for (int i = start; i < end; i++) {
            sb.append(bikesByPrice.get(i).toString());
        }

        sb.append(String.format("-- Page %d of %d --", n, numPages));

        return sb.toString();
    }

    @Override
    public boolean validItemNumber(String itemNumber) throws RemoteException {
        return inventory.get(itemNumber) != null;
    }

    @Override
    public String lookup(String itemNumber) throws RemoteException {
        return inventory.get(itemNumber).toString();
    }

    @Override
    public Bike lookupBike(String itemNumber) throws RemoteException {
        return inventory.get(itemNumber);
    }

    @Override
    public String lookupName(String itemNumber) throws RemoteException {
        return inventory.get(itemNumber).getName();
    }

    ;

    @Override
    public int lookupInventory(String itemNumber) throws RemoteException {
        return inventory.get(itemNumber).getInventory();
    }

    @Override
    public int lookupPrice(String itemNumber) throws RemoteException {
        return inventory.get(itemNumber).getPrice();
    }

    @Override
    public int getNumPages() throws RemoteException {
        return numPages;
    }

    private void generateNewDataVersion() {
        oldDataVersion = currentDataVersion;
        currentDataVersion = UUID.randomUUID().toString();
        versionDifference = new ArrayList<Bike>();
    }

    @Override
    public synchronized boolean purchase(PurchaseRequest request) throws RemoteException {
        HashMap<String, Integer> bikeQuantities = request.getBikeQuantities();

        // check first that there are enough inventory
        for (Map.Entry<String, Integer> entry : bikeQuantities.entrySet()) {
            String itemNumber = entry.getKey();
            int quantity = entry.getValue();
            Bike bike = inventory.get(itemNumber);

            int available = bike.getInventory();
            if (available < quantity) {
                // not enough available to complete order
                return false;
            }
        }

        // request is valid, proceed
        // start a new data version
        this.generateNewDataVersion();

        for (Map.Entry<String, Integer> entry : bikeQuantities.entrySet()) {
            String itemNumber = entry.getKey();
            int quantity = entry.getValue();
            Bike bike = inventory.get(itemNumber);

            int available = bike.getInventory();
            available = available - quantity;
            bike.setInventory(available);

            versionDifference.add(bike);

            // write to inventory
            try {
                data.writeInventory(bike, available);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        completeOrderAfterDelay(request.getOrderID());
        return true;
    }

    private void makeBikesByPrice() {
        bikesByPrice = new ArrayList<Bike>(bikes);
        Comparator<Bike> priceComparator = new Comparator<Bike>() {

            @Override
            public int compare(Bike bike1, Bike bike2) {
                double diff = bike1.getPrice() - bike2.getPrice();
                if (diff > 0.001) {
                    return 1;
                }
                if (diff < -0.001) {
                    return -1;
                }
                return 0;
            }

        };

        Collections.sort(bikesByPrice, priceComparator);
    }

    ;

}
