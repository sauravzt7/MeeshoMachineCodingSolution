package com.club.models;

public class Product {
    private String id;
    private String name;
    private Integer availableQty;


    public Product(String id, String name, Integer availableQty){
        this.id = id;
        this.name = name;
        this.availableQty = availableQty;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAvailableQty() {
        return availableQty;
    }

    public void setAvailableQty(Integer availableQty) {
        this.availableQty = availableQty;
    }
}
