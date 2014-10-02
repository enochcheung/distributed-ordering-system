package com.enochc.software648.hw1.suppliers;

import static org.junit.Assert.*;

import java.io.IOException;
import java.rmi.RemoteException;

import org.junit.Before;
import org.junit.Test;

import com.enochc.software648.hw1.Supplier;
import com.enochc.software648.hw1.SupplierData;

public class Supplier1DataTest {
	SupplierData data;
	Supplier supplier;

	@Before
	public void setUp() throws Exception {
		data = new Supplier1Data();
		supplier = new Supplier(data);
	}

	@Test
	public void testSupplierBrowse() {
			System.out.print(supplier.browsePage(1));
			System.out.print(supplier.browsePage(2));
			System.out.print(supplier.browsePage(7));
			System.out.print(supplier.browsePage(13));


	}

	@Test
	public void testSupplierBrowseByPrice() {
		try {
			System.out.print(supplier.browseByPricePage(1));
			System.out.print(supplier.browseByPricePage(2));
			System.out.print(supplier.browseByPricePage(7));
			System.out.print(supplier.browseByPricePage(13));
		} catch (RemoteException e) {
		}

	}

}
