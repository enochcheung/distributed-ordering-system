package com.enochc.software648.hw1;


import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class OrderingSystem1 extends OrderingSystem {
    private static final long serialVersionUID = -8506536804430644922L;

    public OrderingSystem1() throws RemoteException {
        super();
    }

    protected static String getMyHost() {
        return RemoteConnect.getInstance().getOrderingSystem1Host();
    }

    protected static int getMyPort() {
        return RemoteConnect.getInstance().getOrderingSystem1Port();
    }



    public static void main(String[] args) {
        try {
            int port = getMyPort();
            Registry registry = LocateRegistry.createRegistry(port);

            // bind orderingsystem to registry

            OrderingSystemInterface orderingSystem = new OrderingSystem1();
            registry.bind("OrderingSystem1", orderingSystem);
            System.out.println("OrderingSystem1 started.");
        } catch (RemoteException | AlreadyBoundException e) {
            e.printStackTrace();
        }

    }
}
