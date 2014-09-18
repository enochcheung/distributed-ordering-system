package com.enochc.software648.hw1;

import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;
import java.util.Scanner;

import com.enochc.software648.hw1.suppliers.Supplier1Data;

public class Supplier1Server {

	private static final String SETTINGS_FILE = "settings.properties";

	public static void main(String[] args) {

		int port = 0;
		String host = "";
		try {
			Properties prop = new Properties();
			FileInputStream in = new FileInputStream(SETTINGS_FILE);
			prop.load(in);
			port = Integer.parseInt(prop.getProperty("port1"));
			host = prop.getProperty("host1");

			in.close();
		} catch (IOException e) {
		}
		
		// create the registry
		Registry registry = null;

		try {
			registry = LocateRegistry.createRegistry(port);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		// bind supplier1 to registry
		try {
			SupplierInterface supplier = new Supplier(new Supplier1Data());
			registry.bind("Supplier1", supplier);
			System.out.print("Supplier1 started.");

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		}
		
		// wait until user says quit
		Scanner scanner = new Scanner(System.in);
		String line="";
		while (true) {
			line = scanner.nextLine();
			if (line.equals("quit")) {
				scanner.close();
				break;
			}
		}
	}
}
