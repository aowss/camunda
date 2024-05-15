package com.micasa.tutorial.service;

import com.micasa.tutorial.config.APIConfig;
import com.micasa.tutorial.model.Order;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class ZeebeService {

    @Autowired
    APIConfig config;

    @Autowired
    ZeebeClient client;

    public PublishMessageResponse startProcess(Order request, String messageName) {
        var variables = Map.of(
            "orderId", request.orderId(),
            "orderDetails", request,
            "config", config
        );

        var command = client
            .newPublishMessageCommand()
            .messageName(messageName)
            .correlationKey(request.orderId())
            .variables(variables);

        return command
            .send()
            .join();
    }

}
