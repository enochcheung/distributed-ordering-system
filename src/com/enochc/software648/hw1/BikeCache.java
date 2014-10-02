package com.enochc.software648.hw1;

import java.rmi.RemoteException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BikeCache {
    private static final long WAIT_TIME = 3000L;
    private final SupplierInterface supplier;
    private String supplierName;
    private final ConcurrentHashMap<String, Bike> cache = new ConcurrentHashMap<String, Bike>();
    private String dataVersion;
    private int lastPull;   // time of the last pull
    private final Object writeLock = new Object(); // lock for updating bikeCache and updating dataVersion
    private final ConcurrentLinkedQueue<PurchaseRequest> purchaseQueue = new ConcurrentLinkedQueue<PurchaseRequest>();
    // queueLock should be obtained between peeking and then removing an element form the queue
    private final Object queueLock = new Object();


    public BikeCache(SupplierInterface supplier) {
        this.supplier = supplier;
        this.dataVersion = "";

        try {
            supplierName = supplier.getName();
        } catch (RemoteException e) {
            System.out.println("could not load name from supplier");
            e.printStackTrace();
        }
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
     * Pull bike data from supplier to bring cache up to date
     */
    private void pullBikes() {
        try {
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
