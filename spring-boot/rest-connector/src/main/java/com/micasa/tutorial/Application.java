package com.micasa.tutorial;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@ImportAutoConfiguration({
        io.camunda.connector.runtime.InboundConnectorsAutoConfiguration.class,
        io.camunda.connector.runtime.OutboundConnectorsAutoConfiguration.class,
        io.camunda.connector.runtime.WebhookConnectorAutoConfiguration.class
})
@Deployment(resources = "classpath*:*.bpmn")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}