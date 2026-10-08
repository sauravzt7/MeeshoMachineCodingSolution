******Problem statement:

Develop a program to manage the inventory for an e-commerce company. In this e-commerce website, Admin will create the products which will be visible to the customers, The customer can place an order via online payment. Before initiating online payment, we will block the inventory, until it completes the payment.



Methods to implement:

1. create product with given productid, name and inventory count

createProduct(String productld, String name, Integer count)



2. return the available quantity for given product

getinventory(String productid)


3. Will be called when the user initiates payment for an order.This will block the inventory count for the given product and for the given order reference

createOrder(List<String> productIds, List<Integer> quantityOrdered, String orderld)


4. Will be called when the user completes payment for his order. Reduce the ordered quantity permanently for the product corresponding to given orderld.

confirmOrder(String orderld)



5. If confirmOrder() is not called within 5min from createOrder(), the blocked quantity should be released back.



Note:

ith index of quantityOrdered will be be the quantity of the ith productIds

An order can have multiple products

You can choice of your IDE

You can use any library in you code******