package com.micasa.tutorial.start;

import com.micasa.tutorial.model.ExchangeRateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RESTController {

    @Autowired
    ZeebeController zeebeController;

    @GetMapping("/exchangeRate")
    public ResponseEntity<Void> getExchangeRate(@RequestParam("from") String from, @RequestParam("to") String to, @RequestParam("amount") int amount) {
        zeebeController.startProcess(new ExchangeRateRequest(from, to, amount));
        return ResponseEntity.accepted().build();
    }
}
