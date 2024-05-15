package com.micasa.tutorial.model;

import java.time.LocalDate;
import java.util.List;

record Customer(String customerId, String name, String email, String phone) {}

record Item(String itemId, String description, int quantity, float price) {}

public record Order(String orderId, boolean update, Customer customer, List<Item> items, float totalPrice, String status, LocalDate orderDate, LocalDate deliveryDate) {}
