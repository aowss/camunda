package com.micasa.tutorial.model;

import java.time.LocalDateTime;

public record ExchangeRateResponse(String from, String to, float rate, LocalDateTime time, int fromAmount, float toAmount) {
}
