package com.enochc.software648.hw1;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

@Path("/orderingsystem")
public class OrderingSystemController {
    public static final String RMI_ERROR = "RMI connection error";
    private OrderingSystemInterface orderingSystem;
    private static final String SETTINGS_FILE = "settings.properties";

    /**
     * Connects to OrderingSystem
     */
    private void init() {
        // load settings
        String host = "";
        int port = 0;
        try {
            Properties prop = new Properties();
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(SETTINGS_FILE);
            prop.load(in);
            host = prop.getProperty("orderingsystem.host");
            port = Integer.parseInt(prop.getProperty("orderingsystem.port"));

            in.close();
        } catch (IOException e) {
            System.out.println("Unable to load settings.");
            e.printStackTrace();
            return;
        }

        // look up orderingsystem
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            orderingSystem = (OrderingSystemInterface) registry.lookup("OrderingSystem");
        } catch (NotBoundException | RemoteException e) {
            System.out.println("Unable to connect to OrderingSystem");
            e.printStackTrace();
            return;
        }
    }

    private void connect() {
        while (orderingSystem == null) {
            init();
            if (orderingSystem != null) {
                break;
            }
            try {
                wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private JSONObject bikeToJson(Bike bike) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", bike.getName());
        jsonObject.put("itemNumber", bike.getItemNumber());
        jsonObject.put("supplier", bike.getSupplierName());
        jsonObject.put("description", bike.getDescription());
        jsonObject.put("category", bike.getCategory());
        jsonObject.put("price", bike.getPriceDollars());
        jsonObject.put("inv", bike.getInventory());
        return jsonObject;
    }

    private List<JSONObject> orderToJsonList(Order order) {
        String customerID = order.getCustomerID();
        String orderID = order.getOrderID();
        String status = order.getStatus();
        String date = order.getDate();
        Number totalPrice = order.getTotalPriceDollars();

        List<JSONObject> list = new ArrayList<JSONObject>();
        HashMap<String, Integer> bikeQuantities = order.getBikeQuantities();
        HashMap<String, String> bikeNames = order.getBikeNames();
        HashMap<String, BigDecimal> bikePrices = order.getBikePricesDollars();
        for (String itemNum : bikeQuantities.keySet()) {
            String bikeName = bikeNames.get(itemNum);
            int quantity = bikeQuantities.get(itemNum);
            Number bikePrice = bikePrices.get(itemNum);

            JSONObject jsonObject = new JSONObject();

            jsonObject.put("customer", customerID);
            jsonObject.put("itemNumber", itemNum);
            jsonObject.put("bikeName", bikeName);
            jsonObject.put("price", bikePrice);
            jsonObject.put("quantity", quantity);
            jsonObject.put("orderId", orderID);
            jsonObject.put("date", date);
            jsonObject.put("status", status);
            jsonObject.put("totalPrice", totalPrice);

            list.add(jsonObject);
        }

        return list;
    }

    /*
    private JSONObject customerInfoJSON(CustomerInfo customer) {
        CustomerAddress address = customer.getCustomerAddress();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("firstName", address.);
        jsonObject.put("itemNumber", bike.getItemNumber());
        jsonObject.put("supplier", bike.getSupplierName());
        jsonObject.put("description", bike.getDescription());
        jsonObject.put("category", bike.getCategory());
        jsonObject.put("price", bike.getPriceDollars());
        jsonObject.put("inv", bike.getInventory());
        return jsonObject;
    }
    */

    @GET
    @Path("/hello")
    @Produces("text/html")
    public String hello() {
        return "hello";
    }

    /**
     * Retrieves a authorization token, providing customerID and password
     */
    @GET
    @Path("/auth")
    @Produces("text/html")
    public String auth(@HeaderParam("customerID") String customerID, @HeaderParam("password") String password) {
        connect();
        String token = null;
        try {
            token = orderingSystem.getToken(customerID, password);
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new WebApplicationException(404);
        }
        if (token == null) {
            throw new WebApplicationException(403);
        }

        return token;
    }

    @GET
    @Path("/bike")
    @Produces("application/json")
    public String browse() {
        connect();
        try {
            ArrayList<Bike> bikes = orderingSystem.browse();
            JSONArray jsonArray = new JSONArray();

            for (Bike bike : bikes) {
                jsonArray.add(bikeToJson(bike));
            }

            return jsonArray.toJSONString();


        } catch (RemoteException e) {
            e.printStackTrace();
            throw new WebApplicationException(404);
        }

    }

    @GET
    @Path("/bike/{itemNumber}")
    @Produces("application/json")
    public String lookupBike(@PathParam("itemNumber") String itemNumber) {
        connect();
        try {
            Bike bike = orderingSystem.lookupBike(itemNumber);

            if (bike == null) {
                throw new WebApplicationException(404);
            }

            return bikeToJson(bike).toJSONString();


        } catch (RemoteException e) {
            e.printStackTrace();
            return RMI_ERROR;
        }

    }

    @POST
    @Path("/buy")
    @Produces("text/plain")
    public String purchaseBike(@HeaderParam("customerID") String customerID,
                               @HeaderParam("token") String token,
                               @FormParam("jsonString") String jsonString) {
        connect();
        try {
            if (customerID == null | token == null | !orderingSystem.checkToken(customerID, token)) {
                throw new WebApplicationException(403);
            }

            if (jsonString == null) {
                throw new WebApplicationException(404);
            }

            JSONObject jsonObj = (JSONObject) JSONValue.parse(jsonString);

            HashMap<String, Integer> itemQuantities = new HashMap<String, Integer>();
            for (Object object : jsonObj.keySet()) {
                String itemNum = (String) object;

                int quantity = 0;
                quantity = ((Number) jsonObj.get(itemNum)).intValue();

                itemQuantities.put(itemNum, quantity);

            }

            try {
                orderingSystem.purchase(customerID, itemQuantities);
            } catch (CustomerNotFoundException e) {
                return "Failed: Customer " + e.getCustomer() + " not found";
            } catch (BikeNotFoundException e) {
                return "Failed: Bike " + e.getItemNum() + " not found";
            } catch (InsufficientInventoryException e) {
                return "Failed: Insufficient stock for " + e.getItemNum();
            }

            return "Order successfully placed.";


        } catch (RemoteException e) {
            e.printStackTrace();
            throw new WebApplicationException(404);
        }

    }

    @GET
    @Path("/customer/{customerID}")
    @Produces("text/plain")
    public String lookupCustomer(@HeaderParam("customerID") String customerID,
                                 @HeaderParam("token") String token,
                                 @PathParam("customerID") String customerID2) {
        connect();

        try {
            if (customerID == null | token == null | !orderingSystem.checkToken(customerID, token)) {
                throw new WebApplicationException(403);
            }

            CustomerInfo customerInfo = orderingSystem.lookupCustomer(customerID);
            if (customerInfo == null) {
                throw new WebApplicationException(404);
            }

            return customerID + "\n" + customerInfo.getCustomerAddress().toString();


        } catch (RemoteException e) {
            e.printStackTrace();
            throw new WebApplicationException(404);
        }

    }

    @POST
    @Path("/customer/new/")
    @Produces("text/plain")
    public String lookupBike(@FormParam("customerID") String customerID,
                             @FormParam("password") String password,
                             @FormParam("first") String firstname,
                             @FormParam("last") String lastname,
                             @FormParam("street") String street,
                             @FormParam("city") String city,
                             @FormParam("state") String state,
                             @FormParam("zip") String zipcode) {
        connect();
        try {
            if (customerID == null || firstname == null
                    || lastname == null || street == null
                    || city == null || state == null
                    || zipcode == null) {
                System.out.println("null field encountered when adding customer");
                return "Fields missing.";
            }
            boolean successful = orderingSystem.newCustomer(customerID, password, firstname,
                    lastname, street, city, state, zipcode);

            if (!successful) {
                return "Failed: CustomerID taken";
            }

            return "Successfully added.";


        } catch (RemoteException e) {
            e.printStackTrace();
            throw new WebApplicationException(404);
        }

    }


    @GET
    @Path("/suppliers")
    @Produces("text/html")
    public String getAvailableSuppliers() {
        connect();
        try {
            return orderingSystem.getAvailableSuppliers().toString();
        } catch (RemoteException e) {
            e.printStackTrace();
            return RMI_ERROR;
        }
    }

    @GET
    @Path("/orderID/{orderID}")
    @Produces("text/html")
    public String lookupOrder(@PathParam("orderID") String orderId) {
        connect();
        StringBuilder sb = new StringBuilder();
        try {
            Order order = orderingSystem.lookupOrder(orderId);
            if (order == null) {
                return orderId + " not found.";
            }

            String customerID = order.getCustomerID();
            sb.append(String.format("OrderID: %s%n%s%n", orderId,
                    order.toString()));

            String shippingInfo = orderingSystem.lookupShipping(customerID);
            if (shippingInfo == null) {
                sb.append("Shipping info not found.");
                return sb.toString();
            }
            sb.append("Shipping Info:\n");
            sb.append(shippingInfo);
            return sb.toString();

        } catch (RemoteException e) {
            e.printStackTrace();
            return RMI_ERROR;
        }
    }

    @GET
    @Path("/orderhistory/{customerID}")
    @Produces("application/json")
    public String orderHistory(@HeaderParam("customerID") String customerID,
                               @HeaderParam("token") String token,
                               @PathParam("customerID") String customerID2) {
        connect();
        try {
            if (customerID == null | token == null | !orderingSystem.checkToken(customerID, token)) {
                throw new WebApplicationException(403);
            }

            ArrayList<Order> orders = orderingSystem.orderHistory(customerID);
            if (orders == null) {
                throw new WebApplicationException(404);
            }
            JSONArray jsonArray = new JSONArray();

            for (Order order : orders) {
                for (JSONObject jsonObj : orderToJsonList(order)) {
                    jsonArray.add(jsonObj);
                }
            }

            return jsonArray.toJSONString();
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new WebApplicationException(404);
        }
    }


    @GET
    @Path("/inbox/{customerID}")
    @Produces("text/html")
    public String getInbox(@HeaderParam("customerID") String customerID,
                               @HeaderParam("token") String token,
                               @PathParam("customerID") String customerID2) {
        connect();
        try {
            if (customerID == null | token == null | !orderingSystem.checkToken(customerID, token)) {
                throw new WebApplicationException(403);
            }

            ArrayList<String> messages = orderingSystem.getMessages(customerID);
            if (messages == null) {
                throw new WebApplicationException(404);
            }

            StringBuilder sb = new StringBuilder();
            for (String message : messages) {
                sb.append(message+"\n");
            }

            return sb.toString();
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new WebApplicationException(404);
        }
    }

    @PUT
    @Path("/completeorder/{orderID}")
    public void completeOrder(@PathParam("orderID") String orderID) {
        connect();
        try {
            orderingSystem.completeOrder(orderID);

        } catch (RemoteException e) {
            e.printStackTrace();
            throw new WebApplicationException(404);
        }
    }
}
