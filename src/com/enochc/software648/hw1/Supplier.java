package com.enochc.software648.hw1;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Supplier extends UnicastRemoteObject implements SupplierInterface {
	private static final long serialVersionUID = -3072627641347153761L;
	private static final int PAGE_SIZE = 10;

	private final SupplierData data;
	private final Map<String, Bike> inventory;
	private ArrayList<Bike> bikes;
	private ArrayList<Bike> bikesByPrice;
	private int numPages;

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

	}


    @Override
    public String getSupplierPrefix() {
        return data.getSupplierPrefix();
    }

	@Override
	public String getName() {
		return data.getName();
	}

	private void loadInventory() throws IOException {
        currentDataVersion = UUID.randomUUID().toString();
		data.openReader();
		Bike bike;
		bike = data.readBike();
		while (bike != null) {
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
    public SupplierDataPatch getNewBikes(String dataVersion) {
        if ((dataVersion).equals(currentDataVersion)) {
            // version is already up to date
            return new SupplierDataPatch(new ArrayList<Bike>(),currentDataVersion);
        }
        if (dataVersion.equals(oldDataVersion)) {
            // version is oldDataVersion, use cached versionDifference
            return new SupplierDataPatch(versionDifference,currentDataVersion);
        }

        // version not found, return everything
        return new SupplierDataPatch(bikes,currentDataVersion);
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
	public String lookupName(String itemNumber) throws RemoteException{
		return inventory.get(itemNumber).getName();
	};

	@Override
	public int lookupInventory(String itemNumber) throws RemoteException{
		return inventory.get(itemNumber).getInventory();
	}

	@Override
	public int lookupPrice(String itemNumber) throws RemoteException{
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
	public boolean purchase(String itemNumber, int n) throws RemoteException {
		Bike bike = inventory.get(itemNumber);
		int available = bike.getInventory();
		if (available < n) {
			// not enough available to complete order
			return false;
		}

        // start a new data version
        this.generateNewDataVersion();

		available = available -n;
		bike.setInventory(available);
        versionDifference.add(bike);
		// update database as well
		try {
			data.writeInventory(bike, available);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
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
	};

}
