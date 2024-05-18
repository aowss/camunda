package com.micasa.tutorial.controller;

import com.micasa.tutorial.model.Order;
import com.micasa.tutorial.service.ZeebeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
public class ProcessController {

    @Autowired
    ZeebeService zeebeService;

    @PostMapping("/order")
    public ResponseEntity<Void> createOrder(@RequestBody Order order) {
        var response = zeebeService.startNewProcess(order);
        return ResponseEntity.accepted().build();
    }

    record DeliveryDateUpdateRequest(LocalDate deliveryDate) {}

    @PutMapping("/order/{orderId}")
    public ResponseEntity<Void> updateOrder(@PathVariable("orderId") String orderId, @RequestBody DeliveryDateUpdateRequest request) {
        var response = zeebeService.startUpdateProcess(orderId, request.deliveryDate());
        return ResponseEntity.accepted().build();
    }

}
