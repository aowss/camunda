package com.micasa.tutorial.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "apis")
public record APIConfig(String persistURL, String generateURL, String notifyURL, String notificationDelay, String dispatchURL) {}