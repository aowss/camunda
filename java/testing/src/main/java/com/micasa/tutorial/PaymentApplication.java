package com.micasa.tutorial;

import java.time.Duration;
import java.util.Scanner;

import com.micasa.tutorial.handlers.CreditCardChargingHandler;
import com.micasa.tutorial.handlers.CreditDeductionHandler;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;

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

            // Start the Credit Deduction Worker
            final JobWorker creditDeductionWorker = client.newWorker()
                            .jobType("deductCredit")
                            .handler(new CreditDeductionHandler())
                            .timeout(Duration.ofSeconds(WORKER_TIMEOUT).toMillis())
                            .open();

            // Start the Credit Deduction Worker
            final JobWorker creditCardChargingWorker = client.newWorker()
                            .jobType("chargeCreditCard")
                            .handler(new CreditCardChargingHandler())
                            .timeout(Duration.ofSeconds(WORKER_TIMEOUT).toMillis())
                            .open();

            System.out.println("Type anything to exit");
            Scanner sc = new Scanner(System.in);
            sc.next();
            sc.close();

            creditDeductionWorker.close();
            creditCardChargingWorker.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}