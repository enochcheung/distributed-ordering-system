package com.enochc.software648.hw1;

import java.io.Serializable;
import java.math.BigDecimal;

public class Bike implements Serializable{
	private static final long serialVersionUID = 7292343596751701212L;
	
	private int price; // in cents
	private String name;
	private String description;
	private String itemNumber;
    private String supplierName;
    private String category;
	private int inventory;

	public Bike(int price, String name, String description,
			String itemNumber, String supplierName, String category, int inventory) {
		this.price = price;
		this.name = name;
		this.description = description;
		this.itemNumber = itemNumber;
        this.supplierName = supplierName;
		this.category = category;
		this.inventory = inventory;
		// this.inventoryLineNumber = inventoryLineNumber;
	}

	/**
	 * 
	 * @return price in cents
	 */
	public int getPrice() {
		return price;
	}
	
	/**
	 * 
	 * @return price in dollars
	 */
	public BigDecimal getPriceDollars() {
		BigDecimal priceDecimal = new BigDecimal(price);
		return priceDecimal.divide(new BigDecimal(100));
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getItemNumber() {
		return itemNumber;
	}

    public String getSupplierName() {
        return supplierName;
    }

    public String getCategory() {
		return category;
	}

	public int getInventory() {
		return inventory;
	}

	public void setInventory(int n) {
		inventory = n;
	}

	public String toString() {
		Double priceDollars = (double) price / 100;
		return String
				.format("%s%n\tPrice: $%.2f%n\tInventory: %d%n\t%s%n\tItem Number: %s%n\tSupplier: %s%n\tCategory: %s%n%n",
						name, priceDollars, inventory, description, itemNumber, supplierName,
						category);
	}
}
