package com.micasa.tutorial.handlers;

import com.micasa.tutorial.services.CustomerService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import java.util.Map;

import io.camunda.zeebe.client.api.worker.JobHandler;

public class CreditDeductionHandler implements JobHandler {

    private final CustomerService customerService;

    public CreditDeductionHandler(CustomerService customerService) {
        this.customerService = customerService;
    }

    public CreditDeductionHandler() {
        this(new CustomerService());
    }

    @Override
    public void handle(JobClient client, ActivatedJob job) {
        var customerCredit = (double) job.getVariable("customerCredit");
        var amount = (double) job.getVariable("orderAmount");

        var openAmount = customerService.deductCredit(customerCredit, amount);

        job.getVariablesAsMap().put("openAmount", openAmount);

        client.newCompleteCommand(job)
                .variables(job.getVariablesAsMap())
                .send()
                .exceptionally(throwable -> {
                    throw new RuntimeException("Could not complete job " + job, throwable);
                });
    }
}
