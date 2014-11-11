package com.enochc.software648.hw1.request;


import com.enochc.software648.hw1.Order;

public class NewOrderRequest {
    private final Order order;

    public NewOrderRequest(Order order) {
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }

    public void failed() {
        System.out.println("NewOrderRequest failed for "+order.getOrderID());
    }
}
