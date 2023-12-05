package com.micasa.tutorial.services;

import java.time.YearMonth;

public record CreditCard(String cardNumber, YearMonth expiryDate, String CVC) {
}
