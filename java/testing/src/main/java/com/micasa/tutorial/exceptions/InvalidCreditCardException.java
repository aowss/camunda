package com.micasa.tutorial.exceptions;

public class InvalidCreditCardException extends Exception {

    public InvalidCreditCardException() {
        this("Invalid credit card");
    }

    public InvalidCreditCardException(String message) {
        super(message);
    }

}