package com.micasa.tutorial.services;

import com.micasa.tutorial.exceptions.InvalidCreditCardException;

import java.time.YearMonth;
import java.util.UUID;

public class CreditCardService {

    public String chargeCreditCard(String transactionNumber, double amount, CreditCard creditCard) throws InvalidCreditCardException {
        System.out.println(STR. "Charging \{ amount } to credit card \{ creditCard } for transaction \{ transactionNumber }" );
        if (creditCard.expiryDate().isBefore(YearMonth.now())) {
            System.out.println("The credit card's expiry date is invalid: " + creditCard.expiryDate());
            throw new InvalidCreditCardException();
        }
        return UUID.randomUUID().toString();
    }

}