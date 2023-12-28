package com.micasa.tutorial;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.micasa.tutorial.Utils.*;
import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;
import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.waitForProcessInstanceCompleted;

@SpringBootTest
@ZeebeSpringTest
@DisplayName("Payment Process Test")
class PaymentProcessTest {

    @Autowired
    private ZeebeTestEngine engine;

    @Autowired
    private ZeebeClient client;

    @Test
    @DisplayName("Successful payment")
    void success() throws Exception {
        var variables = Map.of(
            "orderAmount", 60.0,
            "orderReference", "Order-1",
            "cardExpiry", "01/2026",
            "cardNumber", "1234567812345678",
            "cardCVC", "111"
        );

        ProcessInstanceEvent processInstance = startProcess(client, "PaymentProcess", variables);
        waitForProcessInstanceCompleted(processInstance);

        assertThat(processInstance)
                .hasPassedElement("Task_ChargeCreditCard")
                .hasPassedElement("EndEvent_PaymentCompleted")
                .isCompleted();
    }

    @Test
    @DisplayName("Unexpected handler failure, e.g. missing field -> incident")
    void failure() throws Exception {
        var variables = Map.of(
            "orderAmount", 60.0,
            "cardExpiry", "01/2026",
            "cardNumber", "1234567812345678",
            "cardCVC", "111"
        );

        ProcessInstanceEvent processInstance = startProcess(client, "PaymentProcess", variables);
        engine.waitForIdleState(Duration.ofSeconds(10));

        assertThat(processInstance)
                .hasNotPassedElement("Task_ChargeCreditCard")
                .hasNotPassedElement("EndEvent_PaymentCompleted");

        checkAndResolveIncident(processInstance, client, "The variable orderReference is not available");
    }

    @Test
    @DisplayName("Service failure -> incident")
    void incident() throws Exception {
        var variables = Map.of(
            "orderAmount", 60.0,
            "orderReference", "invalid",
            "cardExpiry", "01/2026",
            "cardNumber", "1234567812345678",
            "cardCVC", "111"
        );

        ProcessInstanceEvent processInstance = startProcess(client, "PaymentProcess", variables);
        engine.waitForIdleState(Duration.ofSeconds(10));

        assertThat(processInstance)
                .hasNotPassedElement("Task_ChargeCreditCard")
                .hasNotPassedElement("EndEvent_PaymentCompleted");

        checkAndResolveIncident(processInstance, client, "The transaction number is invalid: invalid");
    }

    @Test
    @DisplayName("Invalid expiry date -> BPMN Error with fix")
    void errorWithFix() throws Exception {
        Map<String, Object> variables = Map.of(
            "orderAmount", 60.0,
            "orderReference", "Order-1",
            "cardExpiry", "01/2023",
            "cardNumber", "1234567812345678",
            "cardCVC", "111"
        );

        ProcessInstanceEvent processInstance = startProcess(client, "PaymentProcess", variables);
        engine.waitForIdleState(Duration.ofSeconds(1));

        assertThat(processInstance)
                .hasNotPassedElement("Task_ChargeCreditCard")
                .hasNoIncidents()
                .isActive();

        var newVariables = new HashMap<>(variables);
        newVariables.replace("cardExpiry", "01/2033");
        newVariables.put("isValidCreditCard", true);

        completeUserTask(client, newVariables);

        waitForProcessInstanceCompleted(processInstance);

        assertThat(processInstance)
                .hasPassedElement("Task_ChargeCreditCard")
                .hasPassedElement("EndEvent_PaymentCompleted")
                .hasNoIncidents()
                .isCompleted();
    }

    @Test
    @DisplayName("Invalid expiry date -> BPMN Error without fix")
    void errorWithoutFix() throws Exception {
        Map<String, Object> variables = Map.of(
                "orderAmount", 60.0,
                "orderReference", "Order-1",
                "cardExpiry", "01/2023",
                "cardNumber", "1234567812345678",
                "cardCVC", "111"
        );

        ProcessInstanceEvent processInstance = startProcess(client, "PaymentProcess", variables);
        engine.waitForIdleState(Duration.ofSeconds(1));

        assertThat(processInstance)
                .hasNotPassedElement("Task_ChargeCreditCard")
                .hasNoIncidents()
                .isActive();

        var newVariables = new HashMap<>(variables);
        newVariables.put("isValidCreditCard", false);

        completeUserTask(client, newVariables);

        waitForProcessInstanceCompleted(processInstance);

        assertThat(processInstance)
                .hasNotPassedElement("Task_ChargeCreditCard")
                .hasNotPassedElement("EndEvent_PaymentCompleted")
                .hasPassedElement("EndEvent_PaymentCancelled")
                .hasNoIncidents()
                .isCompleted();
    }

}
