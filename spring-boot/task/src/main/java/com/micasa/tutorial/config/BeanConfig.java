package com.micasa.tutorial.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {
    @Value("${api.url}")
    public String url;
}
