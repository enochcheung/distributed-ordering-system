package com.enochc.software648.hw1;

import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;
import java.util.Scanner;

import com.enochc.software648.hw1.suppliers.Supplier1Data;

public class Supplier1Server {

    private static final String SETTINGS_FILE = "settings.properties";
    public static final String REGISTRY_NAME = "Supplier1";

    public static void main(String[] args) {

		int port = 0;
		String host = "";
		try {
			Properties prop = new Properties();
			FileInputStream in = new FileInputStream(SETTINGS_FILE);
			prop.load(in);
			port = Integer.parseInt(prop.getProperty("supplier1.port"));
			host = prop.getProperty("supplier1.host");

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
        SupplierInterface supplier;
		try {
			supplier = new Supplier(new Supplier1Data());
			registry.bind(REGISTRY_NAME, supplier);
			System.out.println(REGISTRY_NAME+" started.");

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		}


        /*
		// wait until user says quit
		Scanner scanner = new Scanner(System.in);
		String line="";
		while (true) {
			line = scanner.nextLine();
            if (line.equals("quit")) {
                try {
                    Remote remote = registry.lookup(REGISTRY_NAME);
                    registry.unbind(REGISTRY_NAME);
                    if (remote instanceof UnicastRemoteObject) {
                        UnicastRemoteObject.unexportObject(remote, true);
                    }                }
                catch (RemoteException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
                break;
			}
		}
		*/
	}
}
