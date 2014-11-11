package com.enochc.software648.hw1;

import com.enochc.software648.hw1.request.PurchaseRequest;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface SupplierInterface extends Remote {

	public ArrayList<Bike> browse() throws RemoteException;

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
	
	/**
	 * @param itemNumber
	 * @return Bike object
	 * @throws RemoteException
	 */
	public Bike lookupBike(String itemNumber) throws RemoteException;

	public String lookupName(String itemNumber) throws RemoteException;

	public int lookupInventory(String itemNumber) throws RemoteException;
	
	public int lookupPrice(String itemNumber) throws RemoteException;

    public String getSupplierPrefix() throws RemoteException;

    public String getName() throws RemoteException;

	public boolean validItemNumber(String itemNumber) throws RemoteException;

	/**
	 * 
	 * @param request
	 * @return true if successful, false if unsuccessful
	 * @throws RemoteException
	 */
	public boolean purchase(PurchaseRequest request) throws RemoteException;


    /**
     * Given the dataVersion number of the cache, supplier will send bike info for the bikes that have been updated,
     * along with an updated version number
     * @param dataVersion string identifying the current version of data the cache posses
     * @return SupplierDataPatch for updating the orderingsystem's cache
     * @throws RemoteException
     */
    public DataPatch getNewBikes(String dataVersion) throws RemoteException;

    public String getDataVersion() throws RemoteException;
}
