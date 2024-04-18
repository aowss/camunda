package com.micasa.tutorial.start;

import com.micasa.tutorial.config.BeanConfig;
import com.micasa.tutorial.model.ExchangeRateRequest;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class ZeebeController {

    @Autowired
    BeanConfig config;

    @Autowired
    ZeebeClient client;

    public PublishMessageResponse startProcess(ExchangeRateRequest request) {
        var processVariables = Map.of(
            "from", request.from(),
            "to", request.to(),
            "amount", request.amount(),
            "apiURL", config.url
        );

        return client
            .newPublishMessageCommand()
            .messageName("Exchange Rate Request")
            .correlationKey(UUID.randomUUID().toString())
            .variables(processVariables)
            .send()
            .join();
    }

}
