package com.micasa.tutorial;

import com.micasa.tutorial.handlers.CreditCardChargingHandler;
import com.micasa.tutorial.handlers.CreditDeductionHandler;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static com.micasa.tutorial.Utils.*;
import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;

@ZeebeProcessTest
@DisplayName("Payment Process Test")
public class PaymentProcessTest {

    private ZeebeTestEngine engine;
    private ZeebeClient client;

    @BeforeEach
    public void setup() {
        DeploymentEvent deploymentEvent = client.newDeployResourceCommand()
                .addResourceFromClasspath("paymentProcess.bpmn")
                .send()
                .join();

        assertThat(deploymentEvent).containsProcessesByBpmnProcessId("PaymentProcess");
    }

    @Test
    @DisplayName("Use customer credit")
    public void payFromCredit() throws Exception {
        var variables = Map.of(
            "orderAmount", 42.0,
            "customerCredit", 50.0
        );

        ProcessInstanceEvent processInstance = startProcess(client, "PaymentProcess", variables);
        completeServiceTask(client, "deductCredit", new CreditDeductionHandler());

        //  Wait for the engine to progress through the flow
        engine.waitForIdleState(Duration.ofSeconds(1));

        assertThat(processInstance)
                .hasVariableWithValue("openAmount", 0.0)
                .hasNotPassedElement("Task_ChargeCreditCard")
                .hasPassedElement("EndEvent_PaymentCompleted")
                .isCompleted();
    }

    @Test
    @DisplayName("Use credit card")
    public void payWithCreditCard() throws Exception {
        var variables = Map.of(
            "orderAmount", 60.0,
            "customerCredit", 50.0,
            "orderReference", "Order-1",
            "cardExpiry", "01/2026",
            "cardNumber", "1234567812345678",
            "cardCVC", "111"
        );

        ProcessInstanceEvent processInstance = startProcess(client, "PaymentProcess", variables);
        completeServiceTask(client, "deductCredit", new CreditDeductionHandler());
        completeUserTask(client, Map.of());
        completeServiceTask(client, "chargeCreditCard", new CreditCardChargingHandler());

        //  Wait for the engine to progress through the flow
        engine.waitForIdleState(Duration.ofSeconds(1));

        assertThat(processInstance)
                .hasPassedElement("Task_DeductCredit")
                .hasPassedElement("Task_ChargeCreditCard")
                .hasPassedElement("EndEvent_PaymentCompleted")
                .isCompleted();
    }

    @Test
    @DisplayName("Use credit card assuming the credit is insufficient")
    public void payWithCreditCardWithoutDeductingCredit() throws Exception {
        var variables = Map.of(
            "orderAmount", 60.0,
            "openAmount", 60.0, // the output from the Deduct Credit task
            "orderReference", "Order-1",
            "cardExpiry", "01/2026",
            "cardNumber", "1234567812345678",
            "cardCVC", "111"
        );

        //  Start the process after the Deduct Credit task
        ProcessInstanceEvent processInstance = startProcessBefore(client, "PaymentProcess", "Gateway_CreditSufficient", variables);
        //  No need for the CreditDeductionHandler since the process starts after the Deduct Credit task
        completeUserTask(client, Map.of());
        completeServiceTask(client, "chargeCreditCard", new CreditCardChargingHandler());

        //  Wait for the engine to progress through the flow
        engine.waitForIdleState(Duration.ofSeconds(1));

        assertThat(processInstance)
                .hasPassedElement("Task_ChargeCreditCard")
                .hasPassedElement("EndEvent_PaymentCompleted")
                .isCompleted();
    }

}
