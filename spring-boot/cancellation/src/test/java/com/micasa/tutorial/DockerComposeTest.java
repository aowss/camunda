package com.micasa.tutorial;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.micasa.tutorial.service.ZeebeService;
import com.micasa.tutorial.model.Order;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.inspections.InspectionUtility;
import io.camunda.zeebe.process.test.inspections.model.InspectedProcessInstance;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import org.junit.ClassRule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

//import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.micasa.tutorial.Constants.MESSAGE_NEW_ORDER;
import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.waitForProcessInstanceCompleted;
import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.waitForProcessInstanceHasPassedElement;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@SpringBootTest
@AutoConfigureMockMvc
@ZeebeSpringTest
@WireMockTest(httpPort = 9999)
@ActiveProfiles("test")
@DisplayName("Cancellation Test")
//@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Testcontainers
class DockerComposeTest {

    private static final String processId = "Process_Cancellation";

    @ClassRule
    public static DockerComposeContainer environment =
            new DockerComposeContainer(new File("src/test/resources/camunda/docker-compose.yaml"))
                    .withEnv(fromEnv("src/test/resources/camunda/.env"));
//                    .withExposedService("zeebe", ZEEBE_PORT, Wait.forListeningPort())
//                    .withExposedService("operate", OPERATE_PORT, Wait.forListeningPort())
//                    .withExposedService("tasklist", TASKLIST_PORT, Wait.forListeningPort())
//                    .withExposedService("connectors", CONNECTORS_PORT, Wait.forListeningPort())
////                    .withExposedService("optimize", OPTIMIZE_PORT)
//                    .withExposedService("identity", IDENTITY_PORT, Wait.forListeningPort())
//                    .withExposedService("postgres", POSTGRES_PORT, Wait.forListeningPort())
//                    .withExposedService("keycloak", KEYCLOAK_PORT, Wait.forListeningPort())
//                    .withExposedService("elasticsearch", ELASTICSEARCH_PORT, Wait.forListeningPort())
////                    .withExposedService("kibana", KIBANA_PORT)
//                    .withLocalCompose(true);

    @BeforeAll
    static void startCamundaPlatform() {
        environment.start();
    }

    @AfterAll
    static void stopCamundaPlatform() {
        environment.stop();
    }

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
    @DisplayName("Process and task are cancelled on update")
    void processCancellation() throws Exception {
        stubFor(post(urlPathEqualTo("/order")).willReturn(created()));
        stubFor(post(urlPathEqualTo("/invoice")).willReturn(created()));
        stubFor(post(urlPathEqualTo("/notification")).willReturn(created()));
        stubFor(post(urlPathEqualTo("/dispatch")).willReturn(created()));

        zeebeService.startNewProcess(
            new Order("O-1234", null, List.of(), 150.0, "PENDING", LocalDate.now(), LocalDate.now().plus(2, DAYS))
        );

        InspectedProcessInstance processInstance = InspectionUtility
                .findProcessInstances()
                .withBpmnProcessId(processId)
                .findFirstProcessInstance()
                .get();

        waitForProcessInstanceHasPassedElement(processInstance, "Task_PersistOrder");

//        BpmnAssert.assertThat(processInstance)
//                .isWaitingAtElements("UserTask_PreferentialRate");
//
//        mvc.perform(get(UNASSIGNED_URL))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(1))
//                .andExpect(jsonPath("$.[0].taskState").value("CREATED"))
//                .andExpect(jsonPath("$.[0].candidateGroups[0]").value("manager"))
//                .andExpect(jsonPath("$.[0].assignee").value(null))
//                .andExpect(jsonPath("$.[0].variables[?(@.name == 'fromAmount')].value").value(2000))
//                .andExpect(jsonPath("$.[0].variables[?(@.name == 'fromCurrency')].value").value("CAD"))
//                .andExpect(jsonPath("$.[0].variables[?(@.name == 'toCurrency')].value").value("USD"));
    }

    private static Map<String, String> fromEnv(String path) {
        try {
            var envValues = new Properties();
            envValues.load(new FileReader(path));
            return new HashMap<>((Map<String, String>) ((Map) envValues));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
