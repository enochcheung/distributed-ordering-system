package com.enochc.software648.hw1;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.BindException;
import java.rmi.AlreadyBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;
import java.util.Scanner;

import com.enochc.software648.hw1.suppliers.Supplier1Data;
import com.enochc.software648.hw1.suppliers.Supplier2Data;

public class Supplier2Server {

	private static final String SETTINGS_FILE = "settings.properties";

	public static void main(String[] args) {
		// load settings

		int port = 0;
		String host = "";
		try {
			Properties prop = new Properties();
			FileInputStream in = new FileInputStream(SETTINGS_FILE);
			prop.load(in);
			port = Integer.parseInt(prop.getProperty("port2"));
			host = prop.getProperty("host2");

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

		// bind Supplier2 to registry
		try {
			SupplierInterface supplier = new Supplier(new Supplier2Data());
			registry.bind("Supplier2", supplier);
			System.out.print("Supplier2 started.");

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
