package com.eulerity.task_manager.controller;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import com.eulerity.task_manager.dto.TaskResponse;
import com.eulerity.task_manager.model.Priority;
import com.eulerity.task_manager.model.Status;
import com.eulerity.task_manager.service.TaskService;
import com.eulerity.task_manager.service.TaskSuggestionService;
import com.eulerity.task_manager.service.TaskUrgencyNotificationService;

@WebMvcTest(TaskController.class)
@Import(TaskControllerIntegrationTest.MockConfig.class)
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskUrgencyNotificationService taskUrgencyNotificationService;

    @BeforeEach
    void resetMocks() {
        Mockito.reset(taskService);
        Mockito.reset(taskUrgencyNotificationService);
    }

    @Test
    void postTasks_returnsCreatedTask() throws Exception {
        when(taskService.create(Mockito.any())).thenReturn(taskResponse(
                1L,
                "Prepare interview packet",
                "Draft examples and print copies",
                Status.TODO,
                Priority.HIGH
        ));
        when(taskUrgencyNotificationService.createNotificationForCurrentWorkload()).thenReturn(null);

        mockMvc.perform(post("/tasks")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Prepare interview packet",
                                  "description": "Draft examples and print copies",
                                  "status": "TODO",
                                  "priority": "HIGH",
                                  "dueDate": "2026-05-01"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.task.id").value(1))
                .andExpect(jsonPath("$.task.title").value("Prepare interview packet"))
                .andExpect(jsonPath("$.task.status").value("TODO"))
                .andExpect(jsonPath("$.task.priority").value("HIGH"))
                .andExpect(jsonPath("$.notification").isEmpty());
    }

    @Test
    void getTasks_returnsTaskList() throws Exception {
        when(taskService.findAll()).thenReturn(List.of(
                taskResponse(1L, "Prepare interview packet", "Draft examples", Status.TODO, Priority.HIGH),
                taskResponse(2L, "Review notes", "Review system design", Status.IN_PROGRESS, Priority.MEDIUM)
        ));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].title").value("Review notes"));
    }

    @Test
    void getTaskById_returnsTask() throws Exception {
        when(taskService.findById(1L)).thenReturn(taskResponse(
                1L,
                "Prepare interview packet",
                "Draft examples",
                Status.TODO,
                Priority.HIGH
        ));

        mockMvc.perform(get("/tasks/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Prepare interview packet"))
                .andExpect(jsonPath("$.description").value("Draft examples"));
    }

    @Test
    void putTask_returnsUpdatedTask() throws Exception {
        when(taskService.update(Mockito.eq(1L), Mockito.any())).thenReturn(taskResponse(
                1L,
                "Prepare updated interview packet",
                "Add backend API examples",
                Status.IN_PROGRESS,
                Priority.HIGH
        ));

        mockMvc.perform(put("/tasks/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Prepare updated interview packet",
                                  "description": "Add backend API examples",
                                  "status": "IN_PROGRESS",
                                  "priority": "HIGH",
                                  "dueDate": "2026-05-03"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Prepare updated interview packet"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void deleteTask_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/tasks/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    private TaskResponse taskResponse(Long id, String title, String description, Status status, Priority priority) {
        return new TaskResponse(
                id,
                title,
                description,
                status,
                priority,
                LocalDate.of(2026, 5, 1),
                LocalDateTime.of(2026, 4, 27, 10, 30),
                LocalDateTime.of(2026, 4, 27, 10, 30)
        );
    }

    @TestConfiguration
    static class MockConfig {

        @Bean
        TaskService taskService() {
            return Mockito.mock(TaskService.class);
        }

        @Bean
        TaskSuggestionService taskSuggestionService() {
            return Mockito.mock(TaskSuggestionService.class);
        }

        @Bean
        TaskUrgencyNotificationService taskUrgencyNotificationService() {
            return Mockito.mock(TaskUrgencyNotificationService.class);
        }
    }
}
