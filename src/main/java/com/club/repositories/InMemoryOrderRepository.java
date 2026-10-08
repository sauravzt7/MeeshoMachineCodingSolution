package com.club.repositories;

import com.club.models.Order;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryOrderRepository implements Repository<String, Order>{

    private final Map<String, Order> ordersMap;

    public InMemoryOrderRepository() {
        ordersMap = new HashMap<>();
    }


    @Override
    public void save(Order entity) {
        if(entity != null)
            ordersMap.put(entity.getId(), entity);
    }

    @Override
    public Optional<Order> findById(String s) {
        return Optional.ofNullable(ordersMap.get(s));
    }

    public void remove(String orderId) {
        ordersMap.remove(orderId);
    }
}
