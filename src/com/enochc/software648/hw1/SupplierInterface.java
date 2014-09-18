package com.enochc.software648.hw1;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SupplierInterface extends Remote {

	public String browsePage(int n) throws RemoteException;

	public String browseByPricePage(int n) throws RemoteException;

	public int getNumPages() throws RemoteException;

	/**
	 * @param itemNumber
	 * @return Information for the bike in the supplier's inventory with given
	 *         itemNumber. Null if not found.
	 * @throws RemoteException
	 */
	public String lookup(String itemNumber) throws RemoteException;

	public String lookupName(String itemNumber) throws RemoteException;

	public int lookupInventory(String itemNumber) throws RemoteException;
	
	public int lookupPrice(String itemNumber) throws RemoteException;

	public String getName() throws RemoteException;

	public boolean validItemNumber(String itemNumber) throws RemoteException;

	/**
	 * 
	 * @param itemNumber of the bike
	 * @param n is the quantity desired
	 * @return true if successful, false if unsuccessful
	 * @throws RemoteException
	 */
	public boolean purchase(String itemNumber, int n) throws RemoteException;

	
}
