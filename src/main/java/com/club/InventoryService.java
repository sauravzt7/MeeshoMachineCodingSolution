package com.club;

import com.club.enums.OrderStatus;
import com.club.models.Order;
import com.club.models.OrderItem;
import com.club.models.Product;
import com.club.repositories.InMemoryOrderRepository;
import com.club.repositories.InMemoryProductRepository;
import com.club.repositories.Repository;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class InventoryService {

    private static final long RESERVATION_TIME_MINUTES = 5;


    private final Repository<String, Order> ordersRepository = new InMemoryOrderRepository();
    private final Repository<String, Product> productsRepository = new InMemoryProductRepository();

    private final ReentrantLock inventoryLock = new ReentrantLock();
    private final ReentrantLock writerLock = new ReentrantLock();

    // Used to automatically expire orders after 5 minutes
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    // =========================================================
    // 1. Create Product
    // =========================================================

    public void createProduct(String productId, String name, Integer count) {
        writerLock.lock();
        try {
            if (productId == null || name == null || count == null) throw new IllegalArgumentException("Invalid product details");
            if (count < 0) throw new IllegalArgumentException("Inventory cannot be negative");
            if (productsRepository.findById(productId).isPresent()) throw new IllegalArgumentException("Product already exists: " + productId);
            productsRepository.save(new Product(productId, name, count));
        } finally {
            writerLock.unlock();
        }
    }


    // =========================================================
    // 2. Get Available Inventory
    // =========================================================

    public int getInventory(String productId) {

        inventoryLock.lock();
        try {
            Product product = productsRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
            return product.getAvailableQty();
        } finally {
            inventoryLock.unlock();
        }
    }


    // =========================================================
    // 3. Create Order
    // =========================================================

    public void createOrder(List<String> productIds, List<Integer> quantityOrdered, String orderId) {

        inventoryLock.lock();
        try {
            // ---------- Validation ----------
            if (productIds == null || quantityOrdered == null || orderId == null) throw new IllegalArgumentException("Invalid order details");
            if (productIds.size() != quantityOrdered.size()) throw new IllegalArgumentException("Product IDs and quantities must have same size");
            if (ordersRepository.findById(orderId).isPresent()) throw new IllegalArgumentException("Order already exists: " + orderId);

            // ---------- First check EVERYTHING ----------

            for (int i = 0; i < productIds.size(); i++) {
                String productId = productIds.get(i);
                Integer quantity = quantityOrdered.get(i);


                if (quantity == null || quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than zero");
                if (productsRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId)).getAvailableQty() < quantity) throw new IllegalStateException("Insufficient inventory for product: " + productId);
            }

            // ---------- Reserve inventory ----------
            List<OrderItem> orderItems = new ArrayList<>();
            for (int i = 0; i < productIds.size(); i++) {
                String productId = productIds.get(i);
                int quantity = quantityOrdered.get(i);
                Product product = productsRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
                // Temporarily block inventory
                int availableQty = product.getAvailableQty();
                if(availableQty < quantity) throw new IllegalStateException("Insufficient inventory for product: " + productId);
                product.setAvailableQty(availableQty - quantity);
                orderItems.add(new OrderItem(productId, quantity));
            }


            // ---------- Create order ----------

            Order order = new Order(orderId, orderItems, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(RESERVATION_TIME_MINUTES));

            ordersRepository.save(order);
            // ---------- Schedule expiry ----------
            scheduler.schedule(() -> expireOrder(orderId), RESERVATION_TIME_MINUTES, TimeUnit.MINUTES);

        } finally {
            inventoryLock.unlock();
        }
    }


    // =========================================================
    // 4. Confirm Order
    // =========================================================

    public void confirmOrder(String orderId) {

        inventoryLock.lock();

        try {

            Order order = ordersRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId)) ;



            if (order.getStatus() != OrderStatus.RESERVED) throw new IllegalStateException("Order is already " + order.getStatus());

            /*
             * Inventory was already deducted during createOrder().

             *
             * Therefore we DON'T deduct it again here.
             *
             * We simply convert:
             *
             * RESERVED -> CONFIRMED
             */

            order.setStatus(OrderStatus.CONFIRMED);

        } finally {
            inventoryLock.unlock();
        }
    }


    // =========================================================
    // 5. Expire Order
    // =========================================================

    private void expireOrder(String orderId) {

        inventoryLock.lock();

        try {
            Order order = ordersRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
            // Order may already have been confirmed
            if (order == null) return;

            // Payment completed before expiry
            if (order.getStatus() != OrderStatus.RESERVED) return;


            // Release inventory

            for (OrderItem item : order.getOrderItems()) {
                Product product = productsRepository.findById(item.getProductId()).orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProductId()));
                product.setAvailableQty(product.getAvailableQty() + item.getQuantity());
            }
            // Mark order expired
            order.setStatus(OrderStatus.EXPIRED);
            ordersRepository.remove(orderId);
        } finally {
            inventoryLock.unlock();
        }
    }


    // =========================================================
    // Helper
    // =========================================================


    public static void main(String[] args)
            throws InterruptedException {

        InventoryService inventory = new InventoryService();
        // ---------------------------------------------
        // Admin creates products
        // ---------------------------------------------
        inventory.createProduct("P1", "iPhone", 10);
        inventory.createProduct("P2", "AirPods", 20);

        System.out.println("Initial P1 inventory = " + inventory.getInventory("P1"));

        // ---------------------------------------------
        // Customer creates order
        // ---------------------------------------------

        inventory.createOrder(Arrays.asList("P1", "P2"), Arrays.asList(3, 5), "ORDER-1");


        System.out.println("After ORDER-1 reservation:");

        System.out.println("P1 = " + inventory.getInventory("P1"));

        System.out.println(
                "P2 = "
                        + inventory.getInventory("P2")
        );


        // ---------------------------------------------
        // Payment succeeds
        // ---------------------------------------------

        inventory.confirmOrder("ORDER-1");


        System.out.println("After ORDER-1 confirmation:");
        System.out.println("P1 = " + inventory.getInventory("P1"));

        System.out.println("P2 = " + inventory.getInventory("P2"));


        // ---------------------------------------------
        // Create another order
        // Don't confirm it.
        // It will expire after 5 minutes.
        // ---------------------------------------------

        inventory.createOrder(List.of("P1"), List.of(2), "ORDER-2");


        System.out.println("After ORDER-2 reservation:");
        System.out.println("P1 = " + inventory.getInventory("P1"));
    }
}