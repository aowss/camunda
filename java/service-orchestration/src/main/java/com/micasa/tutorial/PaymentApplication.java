package com.micasa.tutorial;

import com.micasa.tutorial.handler.CreditCardChargingHandler;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;

import java.time.Duration;
import java.util.Map;

public class PaymentApplication {

    private static final String ZEEBE_ADDRESS = "4784612d-1495-4f2a-951d-049c8b985f06.ont-1.zeebe.camunda.io:443";
    private static final String ZEEBE_CLIENT_ID = "jMNcIFp.GJ1-ZYK~Av-8zLHM7xmYCTLs";
    private static final String ZEEBE_CLIENT_SECRET = "dbz--V.YX9JzRzF_44iL9DNz.h0_1S-qpPbl~2xYt6f0ua5h3CWQ2wVulQ68b-Rm";
    private static final String ZEEBE_AUTHORIZATION_SERVER_URL = "https://login.cloud.camunda.io/oauth/token";
    private static final String ZEEBE_TOKEN_AUDIENCE = "zeebe.camunda.io";

    private static final int WORKER_TIMEOUT = 10;

    public static void main(String[] args) {

        //  Needed for the client to authenticate with the cluster
        final var credentialsProvider = new OAuthCredentialsProviderBuilder()
            .authorizationServerUrl(ZEEBE_AUTHORIZATION_SERVER_URL)
            .audience(ZEEBE_TOKEN_AUDIENCE)
            .clientId(ZEEBE_CLIENT_ID)
            .clientSecret(ZEEBE_CLIENT_SECRET)
            .build();

        try (final ZeebeClient client = ZeebeClient.newClientBuilder()
                             .gatewayAddress(ZEEBE_ADDRESS)
                             .credentialsProvider(credentialsProvider)
                             .build()) {

            var variables = Map.of(
                "orderReference", "C8_12345",
                    "orderAmount", Double.valueOf(100.00),
                    "cardNumber", "1234567812345678",
                    "cardExpiry", "12/2023",
                    "cardCVC", "123"
            );

            //  Start the process
            client.newCreateInstanceCommand()
                    .bpmnProcessId("paymentProcess")
                    .latestVersion()
                    .variables(variables)
                    .send()
                    .join();

            //  Register & start this job worker to handle 'chardCreditCard' jobs ( all jobs of this type will be assigned to this worker )
            var creditCardChargingWorker = client.newWorker()
                    .jobType("chargeCreditCard")
                    .handler(new CreditCardChargingHandler()) // Uses the handler to process and complete the job
                    .timeout(Duration.ofSeconds(WORKER_TIMEOUT).toMillis()) // if the job is not completed quick enough, the worker releases the job which can then be picked up by another worker
                    .open();

            Thread.sleep(10000);

            creditCardChargingWorker.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}