package com.micasa.tutorial.service;

import com.micasa.tutorial.config.APIConfig;
import com.micasa.tutorial.model.Order;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

import static com.micasa.tutorial.Constants.MESSAGE_NEW_ORDER;
import static com.micasa.tutorial.Constants.MESSAGE_UPDATED_ORDER;

@Component
public class ZeebeService {

    @Autowired
    APIConfig config;

    @Autowired
    ZeebeClient client;

    public PublishMessageResponse startNewProcess(Order request) {
        var variables = Map.of(
            "orderId", request.orderId(),
            "orderDetails", request,
            "config", config
        );
        return startProcess(variables, request.orderId(), MESSAGE_NEW_ORDER);
    }

    public PublishMessageResponse startUpdateProcess(String orderId, LocalDate deliveryDate) {
        var variables = Map.of("deliveryDate", deliveryDate);
        return startProcess(variables, orderId, MESSAGE_UPDATED_ORDER);
    }

    private PublishMessageResponse startProcess(Map variables, String correlationKey, String messageName) {
        var command = client
                .newPublishMessageCommand()
                .messageName(messageName)
                .correlationKey(correlationKey)
                .variables(variables);

        return command
                .send()
                .join();
    }

}
