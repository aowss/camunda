package com.micasa.tutorial.controller;

import com.micasa.tutorial.service.TaskService;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.exception.TaskListException;
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
    TaskService taskService;

    @GetMapping("/task")
    public ResponseEntity<List<Task>> getTasks(@RequestParam Map<String,String> parameters) {
        try {
            var tasks = taskService.getTasks(parameters);
            return tasks
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.noContent().build());
        } catch (TaskListException tle) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
