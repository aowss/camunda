package com.micasa.tutorial;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.micasa.tutorial.model.Order;
import com.micasa.tutorial.service.ZeebeService;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.inspections.InspectionUtility;
import io.camunda.zeebe.process.test.inspections.model.InspectedProcessInstance;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.created;
import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.waitForProcessInstanceHasPassedElement;
import static java.time.temporal.ChronoUnit.DAYS;

@SpringBootTest
@AutoConfigureMockMvc
@ZeebeSpringTest
@WireMockTest(httpPort = 9999)
@ActiveProfiles("test")
@DisplayName("Cancellation Test")
//@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Testcontainers
class DockerTest {

    private static final String processId = "Process_Cancellation";
    private static final int ZEEBE_PORT = 26500;
    private static final int TASKLIST_PORT = 8082;
    private static final int IDENTITY_PORT = 8084;
    private static final int CONNECTORS_PORT = 8085;
    private static final int KEYCLOAK_PORT = 18080;
    private static final int POSTGRES_PORT = 5432;
    private static final int ELASTICSEARCH_PORT = 9200;

    private static final Network network = Network.newNetwork();

    // Elasticsearch container configuration
    @Container
    private static final GenericContainer<?> elasticSearchContainer = new GenericContainer<>(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.9.2"))
            .withNetwork(network)
            .withNetworkAliases("elasticsearch")
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false")
            .withEnv("ES_JAVA_OPTS", "-Xms1024m -Xmx1024m")
            .withExposedPorts(ELASTICSEARCH_PORT, 9300);

    // Zeebe container configuration
    @Container
    private static final GenericContainer<?> zeebeContainer = new GenericContainer<>(DockerImageName.parse("camunda/zeebe:8.4.0"))
            .withNetwork(network)
            .withNetworkAliases("zeebe")
            .withExposedPorts(ZEEBE_PORT)
            .withEnv("ZEEBE_BROKER_NETWORK_HOST", "zeebe")
            .withEnv("ZEEBE_BROKER_EXPORTERS_ELASTICSEARCH_CLASSNAME", "io.camunda.zeebe.exporter.ElasticsearchExporter")
            .withEnv("ZEEBE_BROKER_EXPORTERS_ELASTICSEARCH_ARGS_URL", "http://elasticsearch:" + ELASTICSEARCH_PORT)
            .dependsOn(elasticSearchContainer);

    // Tasklist container configuration
    @Container
    private static final GenericContainer<?> tasklistContainer = new GenericContainer<>(DockerImageName.parse("camunda/tasklist:8.4.0"))
            .withNetwork(network)
            .withExposedPorts(TASKLIST_PORT)
            .withEnv("camunda.tasklist.elasticsearch.clusterName", "elasticsearch")
            .withEnv("camunda.tasklist.elasticsearch.url", "http://elasticsearch:" + ELASTICSEARCH_PORT)
            .withEnv("camunda.tasklist.zeebe.gatewayAddress", "zeebe:" + ZEEBE_PORT)
            .withEnv("camunda.tasklist.zeebeElasticsearch.url", "http://elasticsearch:" + ELASTICSEARCH_PORT)
            .withEnv("CAMUNDA_TASKLIST_ZEEBE_RESTADDRESS", "http://zeebe:" + TASKLIST_PORT)
            .withEnv("camunda.tasklist.elasticsearch.ssl.selfSigned", "false")
            .withEnv("camunda.tasklist.elasticsearch.ssl.verifyHostname", "false")
            .dependsOn(elasticSearchContainer, zeebeContainer);

    @BeforeAll
    static void startContainers() {
        elasticSearchContainer.start();
        zeebeContainer.start();
        tasklistContainer.start();
    }

    @AfterAll
    static void stopCamundaPlatform() {
        elasticSearchContainer.stop();
        zeebeContainer.stop();
        tasklistContainer.stop();
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
    void preferentialRate() throws Exception {
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
    }
}
