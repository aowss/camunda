package com.micasa.tutorial.controller;

import com.micasa.tutorial.model.Order;
import com.micasa.tutorial.service.ZeebeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.micasa.tutorial.Constants.MESSAGE_NEW_ORDER;
import static com.micasa.tutorial.Constants.MESSAGE_UPDATED_ORDER;

@RestController
public class ProcessController {

    @Autowired
    ZeebeService zeebeService;

    @PostMapping("/order")
    public ResponseEntity<Void> createOrder(@RequestBody Order order) {
        var response = zeebeService.startProcess(order, MESSAGE_NEW_ORDER);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/order")
    public ResponseEntity<Void> updateOrder(@RequestBody Order order) {
        var response = zeebeService.startProcess(order, MESSAGE_UPDATED_ORDER);
        return ResponseEntity.accepted().build();
    }

}
