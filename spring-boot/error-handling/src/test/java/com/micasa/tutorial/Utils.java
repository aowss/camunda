package com.micasa.tutorial;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivateJobsResponse;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;

import java.util.Map;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;

public class Utils {

    private static final String USER_TASK = "io.camunda.zeebe:userTask";

    public static ProcessInstanceEvent startProcess(ZeebeClient client, String processId, Map variables) {
        ProcessInstanceEvent processInstance = client.newCreateInstanceCommand()
                .bpmnProcessId(processId)
                .latestVersion()
                .variables(variables)
                .send()
                .join();

        BpmnAssert.assertThat(processInstance).isStarted();

        return processInstance;
    }

    public static ProcessInstanceEvent startProcessBefore(ZeebeClient client, String processId, String startingPointId, Map variables) {
        ProcessInstanceEvent processInstance = client.newCreateInstanceCommand()
                .bpmnProcessId(processId)
                .latestVersion()
                .variables(variables)
                .startBeforeElement(startingPointId)
                .send()
                .join();

        BpmnAssert.assertThat(processInstance).isStarted();

        return processInstance;
    }

    public static void completeServiceTask(ZeebeClient client, String jobType, JobHandler handler) throws Exception {
        ActivateJobsResponse activateJobsResponse = client.newActivateJobsCommand()
                .jobType(jobType)
                .maxJobsToActivate(1)
                .send()
                .join();

        ActivatedJob firstJob = activateJobsResponse.getJobs().get(0);
        handler.handle(client, firstJob);
    }

    public static void completeUserTask(ZeebeClient client, Map<String, Object> variables) throws Exception {
        ActivateJobsResponse activateJobsResponse = client.newActivateJobsCommand()
                .jobType(USER_TASK)
                .maxJobsToActivate(1)
                .send()
                .join();

        ActivatedJob firstJob = activateJobsResponse.getJobs().get(0);
        client.newCompleteCommand(firstJob)
                .variables(variables)
                .send()
                .join();
    }

    public static void checkAndResolveIncident(ProcessInstanceEvent processInstance, ZeebeClient client, String message) {
        var incident = assertThat(processInstance)
                .extractingLatestIncident();

        incident.extractingErrorMessage()
                .contains(message);

        incident.isUnresolved();

        client
                .newResolveIncidentCommand(incident.getIncidentKey())
                .send()
                .join();

        incident.isResolved();
    }

}
