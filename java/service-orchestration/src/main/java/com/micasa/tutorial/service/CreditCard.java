package com.micasa.tutorial.service;

import java.time.YearMonth;

public record CreditCard(String cardNumber, YearMonth expiryDate, String CVC) {
}
