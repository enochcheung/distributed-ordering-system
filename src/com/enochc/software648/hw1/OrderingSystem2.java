package com.enochc.software648.hw1;


import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class OrderingSystem2 extends OrderingSystem {
    private static final long serialVersionUID = -8506536804430644922L;

    public OrderingSystem2() throws RemoteException {
        super();
    }

    protected static String getMyHost() {
        return RemoteConnect.getInstance().getOrderingSystem2Host();
    }

    protected static int getMyPort() {
        return RemoteConnect.getInstance().getOrderingSystem2Port();
    }



    public static void main(String[] args) {

        // create the registry
        Registry registry = null;

        try {
            int port = getMyPort();
            registry = LocateRegistry.createRegistry(port);

            // bind orderingsystem to registry

            OrderingSystemInterface orderingSystem = new OrderingSystem2();
            registry.bind("OrderingSystem2", orderingSystem);
            System.out.println("OrderingSystem2 started.");
        } catch (RemoteException | AlreadyBoundException e) {
            e.printStackTrace();
        }

    }
}
