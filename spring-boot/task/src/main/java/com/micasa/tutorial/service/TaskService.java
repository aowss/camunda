package com.micasa.tutorial.service;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskSearch;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.exception.TaskListException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class TaskService {

    @Autowired
    CamundaTaskListClient taskListClient;

    public Optional<List<Task>> getTasks(Map<String,String> parameters) throws TaskListException {
        var searchCriteria = new TaskSearch();
        parameters.forEach((name, value) -> {
            switch (name) {
                case "assignee" -> searchCriteria.setAssignee(value);
                case "group" -> searchCriteria.setGroup(value);
                case "state" -> {
                    switch (value.toUpperCase()) {
                        case "CREATED" -> searchCriteria.setState(TaskState.CREATED);
                        case "COMPLETED" -> searchCriteria.setState(TaskState.COMPLETED);
                        case "CANCELED" -> searchCriteria.setState(TaskState.CANCELED);
                    }
                }
                default -> {
                    try {
                        searchCriteria.addVariableFilter(name, value);
                    } catch (TaskListException tle) {
                        System.out.println(STR."Invalid value for variable \{ name }: \{ value }");
                    }
                }
            }
        });
        var tasks = taskListClient.getTasks(searchCriteria).getItems();
        if (tasks == null || tasks.isEmpty()) return Optional.empty();
        else return Optional.of(tasks);
    }

}
