package com.club.models;

import com.club.enums.OrderStatus;

import java.util.List;

public class Order {
    private String id;
    private List<OrderItem> orderItems;
    private long expiryTime;
    private OrderStatus status;

    public Order(String id, List<OrderItem> orderItems, long expiryTime) {
        this.id = id;
        this.orderItems = orderItems;
        this.expiryTime = expiryTime;
        this.status = OrderStatus.RESERVED;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }


}
