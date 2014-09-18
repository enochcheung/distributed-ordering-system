package com.enochc.software648.hw1;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CustomerDBTest {
	CustomerDB db;

	@Before
	public void setUp() throws Exception {
		db = new CustomerDB();
	}

	@Test
	public void test() {
		CustomerInfo address = new CustomerInfo("enoch", "Enoch", "Cheung",
				"5000 Forbes Ave", "Pittsburgh", "PA", "15213");
		db.addCustomer("enoch", address);

		CustomerDB db2 = new CustomerDB();
		assert (db2.hasCustomer("enoch"));
	}

}
