package com.micasa.tutorial;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.jayway.jsonpath.JsonPath;
import com.micasa.tutorial.model.ExchangeRateRequest;
import com.micasa.tutorial.service.ZeebeService;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.inspections.InspectionUtility;
import io.camunda.zeebe.process.test.inspections.model.InspectedProcessInstance;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

//import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.waitForProcessInstanceCompleted;
import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.waitForProcessInstanceHasPassedElement;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@SpringBootTest
@AutoConfigureMockMvc
@ZeebeSpringTest
//@WireMockTest(httpPort = 9999)
@ActiveProfiles("test")
@DisplayName("User Task Test")
//@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class TaskProcessTest {

    private static final String processId = "Process_UserTask";

    @Autowired
    private ZeebeTestEngine engine;

    @Autowired
    private ZeebeService zeebeService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setup() {
        //Init MockMvc Object and build
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    public static final String UNASSIGNED_URL = "/task?state=created&assigned=false";
    @Test
    @DisplayName("Amount >= 1000 --> preferential rate")
    void preferentialRate() throws Exception {
        zeebeService.startProcess(new ExchangeRateRequest("USD", "CAD", 1000));

        InspectedProcessInstance processInstance = InspectionUtility
                .findProcessInstances()
                .withBpmnProcessId(processId)
                .findFirstProcessInstance()
                .get();

        waitForProcessInstanceHasPassedElement(processInstance, "Gateway_LargeAmount");

        BpmnAssert.assertThat(processInstance)
                .isWaitingAtElements("UserTask_PreferentialRate");

        mvc.perform(get(UNASSIGNED_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0].taskState").value("CREATED"))
                .andExpect(jsonPath("$.[0].candidateGroups[0]").value("manager"))
                .andExpect(jsonPath("$.[0].assignee").value(null))
                .andExpect(jsonPath("$.[0].variables[?(@.name == 'fromAmount')].value").value(2000))
                .andExpect(jsonPath("$.[0].variables[?(@.name == 'fromCurrency')].value").value("CAD"))
                .andExpect(jsonPath("$.[0].variables[?(@.name == 'toCurrency')].value").value("USD"));
    }

    @Test
    @Disabled
    @DisplayName("Claim task")
    void claimTask() throws Exception {
        zeebeService.startProcess(new ExchangeRateRequest("USD", "CAD", 1000));

        InspectedProcessInstance processInstance = InspectionUtility
                .findProcessInstances()
                .withBpmnProcessId(processId)
                .findFirstProcessInstance()
                .get();

        waitForProcessInstanceHasPassedElement(processInstance, "Gateway_LargeAmount");

        BpmnAssert.assertThat(processInstance)
                .isWaitingAtElements("UserTask_PreferentialRate");

        var result = mvc.perform(get(UNASSIGNED_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0].taskState").value("CREATED"))
                .andReturn();

        String id = JsonPath.read(result.getResponse().getContentAsString(), "$.[0].id");

        mvc.perform(put("/task/{taskId}/status", id)
                    .content("""
                    {
                        "status": "CLAIMED"
                    }
                    """)
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskState").value("CREATED"))
                .andExpect(jsonPath("$.assignee").value("user-id"));

        BpmnAssert.assertThat(processInstance)
                .isWaitingAtElements("UserTask_PreferentialRate");

        mvc.perform(get("/task/{taskId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskState").value("CREATED"))
                .andExpect(jsonPath("$.assignee").value("user-id"));

    }

}
