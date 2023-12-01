package com.micasa.tutorial.handlers;

import com.micasa.tutorial.exceptions.InvalidCreditCardException;
import com.micasa.tutorial.services.CreditCard;
import com.micasa.tutorial.services.CreditCardService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class CreditCardChargingHandler implements JobHandler {

    private final CreditCardService creditCardService;

    public CreditCardChargingHandler(CreditCardService creditCardService) {
        this.creditCardService = creditCardService;
    }

    public CreditCardChargingHandler() {
        this(new CreditCardService());
    }

    @Override
    public void handle(JobClient client, ActivatedJob job) {
        var reference = (String) job.getVariable("orderReference");
        var amount = (Double) job.getVariable("orderAmount");
        var creditCard = new CreditCard(
            (String) job.getVariable("cardNumber"),
            YearMonth.parse((String) job.getVariable("cardExpiry"), DateTimeFormatter.ofPattern("MM/yyyy")),
            (String) job.getVariable("cardCVC")
        );

        try {

            var confirmationNumber = creditCardService.chargeCreditCard(reference, amount, creditCard);

            var outputVariables = Map.of("confirmation", confirmationNumber);
            client.newCompleteCommand(job.getKey())
                    .variables(outputVariables)
                    .send()
                    .exceptionally(throwable -> {
                        throw new RuntimeException("Could not complete job " + job, throwable);
                    });

        } catch (InvalidCreditCardException e) {
            client.newFailCommand(job)
                    .retries(0)
                    .errorMessage(e.getMessage())
                    .send()
                    .exceptionally(throwable -> {
                        throw new RuntimeException("Could not fail job " + job, throwable);
                    });
        }
    }

}