package com.enochc.software648.hw1;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Supplier extends UnicastRemoteObject implements SupplierInterface {
	private static final long serialVersionUID = -3072627641347153761L;
	private static final int PAGE_SIZE = 10;

	private final SupplierData data;
	private final Map<String, Bike> inventory;
	private List<Bike> bikes;
	private List<Bike> bikesByPrice;
	private int numPages;

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
	public String getName() {
		return data.getName();
	}

	private void loadInventory() throws IOException {
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
	public String browsePage(int n) throws RemoteException {
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
	
	
	@Override
	public boolean purchase(String itemNumber, int n) throws RemoteException {
		Bike bike = inventory.get(itemNumber);
		int available = bike.getInventory();
		if (available < n) {
			// not enough available to complete order
			return false;
		}
		available = available -n;
		bike.setInventory(available);
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
