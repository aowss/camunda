package com.micasa.tutorial.service;

import java.util.UUID;

import static java.lang.StringTemplate.STR;

public class CreditCardServiceMock {

    public String chargeCreditCard(String transactionNumber, double amount, CreditCard creditCard) {
        System.out.println(STR."Charging \{ amount } to credit card \{ creditCard} for transaction \{ transactionNumber }");
        return UUID.randomUUID().toString();
    }
}
