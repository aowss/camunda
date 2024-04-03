package com.micasa.tutorial.controller;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class TaskController {

    @Autowired
    CamundaTaskListClient taskListClient;

    @GetMapping("/task")
    public ResponseEntity<List<Task>> getTasks(@RequestParam Map<String,String> parameters) {
        var searchCriteria = new TaskSearch();
        parameters.forEach((name, value) -> switch (name) {
            case "assignee":
        });
        return ResponseEntity.accepted().build();
    }
}
