package com.micasa.tutorial.handler;

import com.micasa.tutorial.service.CreditCard;
import com.micasa.tutorial.service.CreditCardService;
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
    public void handle(JobClient client, ActivatedJob job) throws Exception {
        var reference = (String) job.getVariable("orderReference");
        var amount = (Double) job.getVariable("orderAmount");
        var creditCard = new CreditCard(
            (String) job.getVariable("cardNumber"),
            YearMonth.parse((String) job.getVariable("cardExpiry"), DateTimeFormatter.ofPattern("MM/yyyy")),
            (String) job.getVariable("cardCVC")
        );

        var confirmationNumber = creditCardService.chargeCreditCard(reference, amount, creditCard);

        //  Inform Zeebe that the job was completed successfully and pass along the confirmation number
        var outputVariables = Map.of("confirmation", confirmationNumber);
        client.newCompleteCommand(job.getKey())
                .variables(outputVariables)
                .send()
                .join();
    }
}
