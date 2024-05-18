package com.micasa.tutorial.model;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

record Customer(String customerId, String name, String email, String phone) {}

record Item(String itemId, String description, int quantity, double price) {}

public record Order(String orderId, Customer customer, List<Item> items, double totalPrice, String status, LocalDate orderDate, LocalDate deliveryDate) {
    public Order(String orderId, Customer customer, List<Item> items, String status, LocalDate orderDate, LocalDate deliveryDate) {
        this(orderId, customer, items, items.stream().collect(Collectors.summingDouble(Item::price)), status, orderDate, deliveryDate);
    }
}
