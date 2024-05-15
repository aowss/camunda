package com.micasa.tutorial;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ImportAutoConfiguration({
        io.camunda.connector.runtime.InboundConnectorsAutoConfiguration.class,
        io.camunda.connector.runtime.OutboundConnectorsAutoConfiguration.class
})
@Deployment(resources = "classpath*:*.bpmn")
@ConfigurationPropertiesScan(basePackages = {"com.micasa.tutorial.config"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}