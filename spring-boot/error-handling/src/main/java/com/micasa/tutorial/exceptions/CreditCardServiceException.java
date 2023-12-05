package com.micasa.tutorial.exceptions;

public class CreditCardServiceException extends RuntimeException {

    public CreditCardServiceException(String message) {
        super(message);
    }

}
