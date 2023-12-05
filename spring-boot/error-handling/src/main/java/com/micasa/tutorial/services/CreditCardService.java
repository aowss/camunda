package com.micasa.tutorial.services;

import com.micasa.tutorial.exceptions.CreditCardServiceException;
import com.micasa.tutorial.exceptions.InvalidCreditCardException;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.UUID;

@Component
public class CreditCardService {

    public String chargeCreditCard(String transactionNumber, double amount, CreditCard creditCard) throws InvalidCreditCardException, CreditCardServiceException {
        System.out.println(STR. "Charging \{ amount } to credit card \{ creditCard } for transaction \{ transactionNumber }" );
        if (creditCard.expiryDate().isBefore(YearMonth.now())) {
            System.out.println("The credit card's expiry date is invalid: " + creditCard.expiryDate());
            throw new InvalidCreditCardException();
        }
        if (transactionNumber.equalsIgnoreCase("invalid")) {
            var message = "The transaction number is invalid: " + transactionNumber;
            System.out.println(message);
            throw new CreditCardServiceException(message);
        }
        return UUID.randomUUID().toString();
    }

}