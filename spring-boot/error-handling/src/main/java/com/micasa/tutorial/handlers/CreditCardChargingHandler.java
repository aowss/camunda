package com.micasa.tutorial.handlers;

import com.micasa.tutorial.exceptions.CreditCardServiceException;
import com.micasa.tutorial.exceptions.InvalidCreditCardException;
import com.micasa.tutorial.services.CreditCard;
import com.micasa.tutorial.services.CreditCardService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class CreditCardChargingHandler {

    @Autowired
    private CreditCardService creditCardService;

    @JobWorker(type = "chargeCreditCard", autoComplete = true)
    public Map<String, Object> handle(ActivatedJob job) throws CreditCardServiceException {
        System.out.println("charge credit card [ retries = " + job.getRetries() + " ]");
        var reference = (String) job.getVariable("orderReference");
        var amount = (Double) job.getVariable("orderAmount");
        var creditCard = new CreditCard(
                (String) job.getVariable("cardNumber"),
                YearMonth.parse((String) job.getVariable("cardExpiry"), DateTimeFormatter.ofPattern("MM/yyyy")),
                (String) job.getVariable("cardCVC")
        );

        try {
            var confirmationNumber = creditCardService.chargeCreditCard(reference, amount, creditCard);
            return Map.of("confirmation", confirmationNumber);
        } catch (InvalidCreditCardException icce) {
            throw new ZeebeBpmnError("invalidCreditCardException", icce.getMessage());
        }
    }
}
