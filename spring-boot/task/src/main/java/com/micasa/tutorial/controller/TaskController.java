package com.micasa.tutorial.controller;

import com.micasa.tutorial.service.TaskService;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.exception.TaskListException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/task/{taskId}")
    public ResponseEntity<Task> getTask(@PathVariable("taskId") String taskId) {
        try {
            var task = taskService.getTask(taskId);
            return task
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.noContent().build());
        } catch (TaskListException tle) {
            return ResponseEntity.internalServerError().build();
        }
    }

    enum Status {
        CLAIMED, RELEASED
    }

    record StatusUpdate(Status status) {}

    @PutMapping("/task/{taskId}/status")
    public ResponseEntity<Task> updateStatus(@PathVariable("taskId") String taskId, @RequestBody StatusUpdate status) {
        try {
            var updatedTask = switch (status.status) {
                case CLAIMED -> taskService.claimTask(taskId, "user-id"); // TODO: take the user from the token
                case RELEASED -> taskService.releaseTask(taskId);
            };
            return ResponseEntity.ok(updatedTask);
        } catch (TaskListException tle) {
            return ResponseEntity.internalServerError().build();
        }
    }

    record RateUpdate(float rate) {}

    @PutMapping("/task/{taskId}")
    public ResponseEntity<Task> setRate(@PathVariable("taskId") String taskId, @RequestBody RateUpdate rate) {
        try {
            var updatedTask = taskService.completeTask(taskId, rate.rate);
            return ResponseEntity.ok(updatedTask);
        } catch (TaskListException tle) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
