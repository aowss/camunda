package com.micasa.tutorial;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.micasa.tutorial.model.ExchangeRateRequest;
import com.micasa.tutorial.start.ZeebeController;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.inspections.InspectionUtility;
import io.camunda.zeebe.process.test.inspections.model.InspectedProcessInstance;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.waitForProcessInstanceCompleted;

import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@SpringBootTest
@ZeebeSpringTest
@WireMockTest(httpPort = 9999)
@ActiveProfiles("test")
@DisplayName("REST Connector Test")
class RESTConnectorProcessTest {

    @Autowired
    private ZeebeTestEngine engine;

    @Autowired
    private ZeebeController controller;

    @Test
    @DisplayName("Using the Zeebe Controller")
    void zeebeController() {
        stubFor(
            get(urlPathEqualTo("/exchangeRates"))
                .withQueryParam("fromCurrency", equalTo("USD"))
                .withQueryParam("toCurrency", equalTo("CAD"))
                .withQueryParam("amount", equalTo("1000"))
            .willReturn(
                okJson("""
                    {
                        "from": "USD",
                        "to": "CAD",
                        "rate": 1.32466,
                        "time": "2023-12-26T12:58.30Z",
                        "fromAmount": 500,
                        "toAmount": 662.33
                    }
                    """
                )
            )
        );

        controller.startProcess(new ExchangeRateRequest("USD", "CAD", 1000));

        InspectedProcessInstance processInstance = InspectionUtility
                .findProcessInstances()
                .withBpmnProcessId("Process_RESTConnector")
                .findFirstProcessInstance()
                .get();

        waitForProcessInstanceCompleted(processInstance);

        BpmnAssert.assertThat(processInstance)
                .hasPassedElement("Start_ExchangeRateRequest")
                .hasVariableWithValue("apiURL", "http://localhost:9999/exchangeRates")
                .hasVariableWithValue("toCurrency", "CAD")
                .hasVariableWithValue("fromCurrency", "USD")
                .hasVariableWithValue("fromAmount", 1000)
                .hasVariableWithValue("exchangeRate", 1.32466);
    }

    @Test
    @DisplayName("API error -> default rate")
    void error() {
        stubFor(
            get(urlPathEqualTo("/exchangeRates"))
                .withQueryParam("fromCurrency", equalTo("INVALID"))
                .withQueryParam("toCurrency", equalTo("CAD"))
                .withQueryParam("amount", equalTo("1000"))
            .willReturn(serverError())
        );

        controller.startProcess(new ExchangeRateRequest("INVALID", "CAD", 1000));

        InspectedProcessInstance processInstance = InspectionUtility
                .findProcessInstances()
                .withBpmnProcessId("Process_RESTConnector")
                .findFirstProcessInstance()
                .get();

        waitForProcessInstanceCompleted(processInstance);

        BpmnAssert.assertThat(processInstance)
                .hasPassedElement("Task-DefaultRate")
                .hasVariableWithValue("fromCurrency", "INVALID")
                .hasVariableWithValue("exchangeRate", 1.3); // default rate
    }

    @Test
    @DisplayName("Request error -> incident")
    void incident() throws InterruptedException, TimeoutException {
        stubFor(
            get(urlPathEqualTo("/exchangeRates"))
                .withQueryParam("fromCurrency", equalTo("USD"))
                .withQueryParam("toCurrency", equalTo("CAD"))
                .withQueryParam("amount", equalTo("0"))
            .willReturn(badRequest())
        );

        controller.startProcess(new ExchangeRateRequest("USD", "CAD", 0));

        InspectedProcessInstance processInstance = InspectionUtility
                .findProcessInstances()
                .withBpmnProcessId("Process_RESTConnector")
                .findFirstProcessInstance()
                .get();

        engine.waitForBusyState(Duration.ofSeconds(10));

        BpmnAssert.assertThat(processInstance)
                .hasNotPassedElement("Task_CallExchangeRateAPI")
                .hasAnyIncidents();
    }

}
