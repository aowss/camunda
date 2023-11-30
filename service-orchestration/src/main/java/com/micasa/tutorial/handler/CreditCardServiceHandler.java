package com.micasa.tutorial.handler;

import com.micasa.tutorial.service.CreditCard;
import com.micasa.tutorial.service.CreditCardServiceMock;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class CreditCardServiceHandler implements JobHandler {

    CreditCardServiceMock service = new CreditCardServiceMock();

    @Override
    public void handle(JobClient client, ActivatedJob job) throws Exception {
        var confirmationNumber = service.chargeCreditCard(
            (String) job.getVariable("reference"),
            (Double) job.getVariable("amount"),
            new CreditCard(
                (String) job.getVariable("cardNumber"),
                YearMonth.parse((String) job.getVariable("cardExpiry"), DateTimeFormatter.ofPattern("MM/yyyy")),
                (String) job.getVariable("cardCVC")
            )
        );

        //  Inform Zeebe that the job was completed successfully and pass along the confirmation number
        var outputVariables = Map.of("confirmation", confirmationNumber);
        client.newCompleteCommand(job.getKey())
                .variables(outputVariables)
                .send()
                .join();
    }
}
