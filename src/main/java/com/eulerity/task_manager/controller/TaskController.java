package com.eulerity.task_manager.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eulerity.task_manager.dto.TaskRequest;
import com.eulerity.task_manager.dto.TaskResponse;
import com.eulerity.task_manager.dto.TaskSuggestRequest;
import com.eulerity.task_manager.dto.TaskSuggestResponse;
import com.eulerity.task_manager.service.TaskService;
import com.eulerity.task_manager.service.TaskSuggestionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskSuggestionService taskSuggestionService;

    public TaskController(TaskService taskService, TaskSuggestionService taskSuggestionService) {
        this.taskService = taskService;
        this.taskSuggestionService = taskSuggestionService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request));
    }

    @GetMapping
    public List<TaskResponse> findAll() {
        return taskService.findAll();
    }

    @GetMapping("/{id}")
    public TaskResponse findById(@PathVariable Long id) {
        return taskService.findById(id);
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return taskService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/suggest")
    public TaskSuggestResponse suggest(@Valid @RequestBody TaskSuggestRequest request) {
        return taskSuggestionService.suggest(request);
    }
}
