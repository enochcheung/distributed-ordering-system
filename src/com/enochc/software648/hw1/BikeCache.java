package com.enochc.software648.hw1;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BikeCache {
    private static final long WAIT_TIME = 3000L;
    private final String host;
    private final int port;
    private final String remoteName;
    private SupplierInterface supplier;
    boolean connected = false;
    private String supplierName;
    private final ConcurrentHashMap<String, Bike> cache = new ConcurrentHashMap<String, Bike>();
    private String dataVersion;
    private int lastPull;   // time of the last pull
    private final Object writeLock = new Object(); // lock for updating bikeCache and updating dataVersion
    private final ConcurrentLinkedQueue<PurchaseRequest> purchaseQueue = new ConcurrentLinkedQueue<PurchaseRequest>();
    // queueLock should be obtained between peeking and then removing an element form the queue
    private final Object queueLock = new Object();


    public BikeCache(SupplierInterface supplier, String host, int port, String remoteName) {
        this.supplier = supplier;
        this.host = host;
        this.port = port;
        this.remoteName = remoteName;

        this.dataVersion = "";

        try {
            supplierName = supplier.getName();
        } catch (RemoteException e) {
            System.out.println("could not load name from supplier");
            e.printStackTrace();
        }
        connected = true;
        pullBikes();

    }

    public ArrayList<Bike> browseBikes() {
        return new ArrayList<Bike>(cache.values());
    }

    /**
     *
     * @param itemNum
     * @return Bike corresponding to itemNum. Null if not found
     */
    public Bike lookupBike(String itemNum) {
        return cache.get(itemNum);
    }

    /**
     * Attempt to reconnect to supplier. Does nothing if failed (try again later).
     */
    private void reconnectSupplier() {
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            supplier = (SupplierInterface) registry.lookup(remoteName);
            connected = true;
            System.out.println("Reconnected to "+supplierName);
        } catch (NotBoundException | RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pull bike data from supplier to bring cache up to date
     */
    private void pullBikes() {
        try {
            if (!connected) {
                this.reconnectSupplier();
            }

            synchronized (this.writeLock) {
                if (!this.dataVersion.equals(supplier.getDataVersion())) {
                    SupplierDataPatch dataPatch = supplier.getNewBikes(dataVersion);
                    for (Bike bike : dataPatch.getBikes()) {
                        cache.put(bike.getItemNumber(), bike);
                    }
                    dataVersion = dataPatch.getDataVersion();
                }
            }
        } catch (RemoteException e) {
            System.out.println("Cannot load new bikes from supplier, keep using cached data");
            e.printStackTrace();
            connected = false;   // connection lost so remove reference to supplier, reacquire later
        }
    }

    /**
     * Queue up a request to purchase a bike in the buffer
     */
    public void purchase(PurchaseRequest request) {
        purchaseQueue.offer(request);
    }

    /**
     * Flush the all buffered requests in purchaseQueue to supplier. If unable to communicate with supplier,
     * then the request remains at the head of the queue. If communication is successful, but purchase is not
     * possible (i.e. out of stock), then request is removed and callback method to OrderingSystem
     * is called to signal the failure.
     */
    private void pushPurchases() {
        if (purchaseQueue.isEmpty()) {
            return;
        }

        if (!connected) {
            this.reconnectSupplier();
        }

        while (true) {
            synchronized (queueLock) {
                PurchaseRequest request = purchaseQueue.peek();
                if (request == null) {
                    // finished pushing all buffered
                    break;
                }

                boolean success = false;
                try {
                    success = supplier.purchase(request.getItemNumber(), request.getQuantity());

                } catch (RemoteException e) {
                    System.out.println("Unable to communicate with supplier to push request");
                    e.printStackTrace();
                    connected=false;
                    break;
                }

                // purchase either was successful, or denied by supplier, so remove it from buffer
                purchaseQueue.poll();
                if (!success) {
                    request.failed();
                }
            }
        }
    }

    /**
     * Returns a Runnable that loops and periodically tries to push the buffer to the supplier, and update the cache
     */
    public Runnable getRunnable() {
        return new Runnable(){

            @Override
            public void run() {
                while (true) {
                    pushPurchases();
                    pullBikes();
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
