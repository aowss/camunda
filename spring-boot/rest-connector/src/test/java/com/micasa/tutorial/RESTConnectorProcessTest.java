package com.micasa.tutorial;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.micasa.tutorial.model.ExchangeRateRequest;
import com.micasa.tutorial.start.ZeebeController;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
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

@SpringBootTest
@ZeebeSpringTest
@WireMockTest(httpPort = 9999)
@ActiveProfiles("test")
@DisplayName("REST Connector Test")
class RESTConnectorProcessTest {

    @Autowired
    private ZeebeController controller;

    @Test
    @DisplayName("Using the Zeebe Controller")
    void zeebeController() throws Exception {
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

        PublishMessageResponse response = controller.startProcess(new ExchangeRateRequest("USD", "CAD", 1000));

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

}
