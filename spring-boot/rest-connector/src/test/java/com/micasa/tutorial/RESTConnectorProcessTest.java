package com.micasa.tutorial;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.micasa.tutorial.model.ExchangeRateRequest;
import com.micasa.tutorial.start.ZeebeController;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import io.camunda.connector.e2e.app.TestConnectorRuntimeApplication;

@SpringBootTest(
    classes = { TestConnectorRuntimeApplication.class, Application.class },
    properties = { "spring.main.allow-bean-definition-overriding=true", "camunda.connector.polling.enabled=true" },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ZeebeSpringTest
@DisplayName("REST Connector Test")
class RESTConnectorProcessTest {

    @RegisterExtension
    WireMockExtension wm = WireMockExtension.newInstance()
            .options(wireMockConfig().bindAddress("127.0.0.1").port(9999))
            .proxyMode(true)
            .build();

//    @Autowired
//    private ZeebeTestEngine engine;
//
//    @Autowired
//    private ZeebeClient client;

    @Autowired
    private ZeebeController controller;

    @Test
    @DisplayName("Using the Zeebe Controller")
    void zeebeController() throws Exception {
        wm.stubFor(
            get("/api")
                .withHost(equalTo("prod-rates"))
                .willReturn(ok())
        );

        PublishMessageResponse response = controller.startProcess(new ExchangeRateRequest("USD", "CAD", 1000));

        BpmnAssert.assertThat(response)
                .hasCreatedProcessInstance()
                .extractingProcessInstance()
                .hasPassedElement("Start_ExchangeRateRequest")
                .hasVariableWithValue("apiURL", "http://prod-rates:9999/api")
                .hasVariableWithValue("toCurrency", "CAD")
                .hasVariableWithValue("fromCurrency", "USD")
                .hasVariableWithValue("fromAmount", 1000)
                .isWaitingAtElements("Task_CallExchangeRateAPI")
//                .hasAnyIncidents()
                .isActive();
//                .hasNotPassedElement(REMIND_REVIEWER)
//                .hasNotPassedElement(MAKE_CHANGES)
//                .hasVariableWithValue(REVIEW_RESULT_VAR, "approved")
//                .extractingLatestCalledProcess()
//                .hasPassedElement(AUTOMATED_TESTS_RUN_TESTS, 3)
//                .isCompleted();
    }

}
