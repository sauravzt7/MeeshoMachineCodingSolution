package com.club.repositories;

import com.club.models.Product;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryProductRepository implements Repository<String, Product>{
    private final Map<String, Product> productsMap = new ConcurrentHashMap<>();

    @Override
    public void save(Product entity) {
        if(entity != null)productsMap.put(entity.getId(), entity);
    }

    @Override
    public Optional<Product> findById(String s) {
        return Optional.ofNullable(productsMap.get(s));
    }

    public void remove(String productId) {
        productsMap.remove(productId);
    }

    public void addProduct(Product product){
        productsMap.put(product.getId(), product);
    }

    public Integer getAvailableQuantity(String productId){
        return productsMap.get(productId).getAvailableQty();
    }

}
