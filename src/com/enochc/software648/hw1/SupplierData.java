package com.enochc.software648.hw1;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public interface SupplierData {

    /**
	 * Reads the next bike. Returns null at the end of file
	 * 
	 * @throws IOException
	 */
	public Bike readBike() throws IOException;

    public String getSupplierPrefix();

    public void openReader() throws FileNotFoundException;

	public void closeReader();

	public String getName();

    public Map<String,Integer> getInventory() throws IOException;

    public void writeInventory(Bike bike, Integer inventory) throws IOException;
}
