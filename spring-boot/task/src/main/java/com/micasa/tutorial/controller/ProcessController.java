package com.micasa.tutorial.controller;

import com.micasa.tutorial.model.ExchangeRateRequest;
import com.micasa.tutorial.service.ZeebeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ProcessController {

    @Autowired
    ZeebeService zeebeService;

    @GetMapping("/exchangeRate")
    public ResponseEntity<Void> getExchangeRate(@RequestParam("from") String from, @RequestParam("to") String to, @RequestParam("amount") int amount) {
        zeebeService.startProcess(new ExchangeRateRequest(from, to, amount));
        return ResponseEntity.accepted().build();
    }

}
