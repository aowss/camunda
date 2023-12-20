package com.micasa.tutorial;

import com.micasa.tutorial.config.BeanConfig;
import com.micasa.tutorial.model.ExchangeRateRequest;
import com.micasa.tutorial.start.ZeebeController;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.micasa.tutorial.Utils.completeUserTask;
import static com.micasa.tutorial.Utils.startProcess;
import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;
import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.waitForProcessInstanceCompleted;

@SpringBootTest
@ZeebeSpringTest
@DisplayName("REST Connector Test")
class RESTConnectorProcessTest {

    @Autowired
    private ZeebeTestEngine engine;

    @Autowired
    private ZeebeClient client;

    @Autowired
    private ZeebeController controller;

    @Test
    @DisplayName("Using the Zeebe Controller")
    void zeebeController() throws Exception {
        PublishMessageResponse response = controller.startProcess(new ExchangeRateRequest("USD", "CAD", 1000));
        BpmnAssert.assertThat(response)
                .hasCreatedProcessInstance()
                .extractingProcessInstance()
//                .hasPassedElementsInOrder(REQUEST_REVIEW, MERGE_CODE, DEPLOY_SNAPSHOT)
//                .hasNotPassedElement(REMIND_REVIEWER)
//                .hasNotPassedElement(MAKE_CHANGES)
//                .hasVariableWithValue(REVIEW_RESULT_VAR, "approved")
//                .extractingLatestCalledProcess()
//                .hasPassedElement(AUTOMATED_TESTS_RUN_TESTS, 3)
                .isCompleted();
    }

}
