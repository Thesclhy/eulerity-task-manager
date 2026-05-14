package com.eulerity.task_manager.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.eulerity.task_manager.model.Priority;
import com.eulerity.task_manager.model.Status;
import com.eulerity.task_manager.repository.TaskRepository;

@SpringBootTest
@AutoConfigureMockMvc
class TaskCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void taskCrudFlow_worksEndToEnd() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/tasks")
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
                .andExpect(jsonPath("$.task.id").isNumber())
                .andExpect(jsonPath("$.task.title").value("Prepare interview packet"))
                .andExpect(jsonPath("$.task.description").value("Draft examples and print copies"))
                .andExpect(jsonPath("$.task.status").value("TODO"))
                .andExpect(jsonPath("$.task.priority").value("HIGH"))
                .andExpect(jsonPath("$.task.dueDate").value("2026-05-01"))
                .andExpect(jsonPath("$.notification").isEmpty())
                .andReturn();

        long taskId = extractId(createResult.getResponse().getContentAsString());

        assertThat(taskRepository.count()).isEqualTo(1);
        assertThat(taskRepository.findById(taskId)).isPresent();
        assertThat(taskRepository.findById(taskId).orElseThrow().getDueDate()).isEqualTo(LocalDate.of(2026, 5, 1));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(taskId))
                .andExpect(jsonPath("$[0].title").value("Prepare interview packet"))
                .andExpect(jsonPath("$[0].task").doesNotExist());

        mockMvc.perform(get("/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Prepare interview packet"))
                .andExpect(jsonPath("$.description").value("Draft examples and print copies"))
                .andExpect(jsonPath("$.task").doesNotExist());

        mockMvc.perform(put("/tasks/{id}", taskId)
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
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Prepare updated interview packet"))
                .andExpect(jsonPath("$.description").value("Add backend API examples"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.dueDate").value("2026-05-03"))
                .andExpect(jsonPath("$.task").doesNotExist());

        assertThat(taskRepository.findById(taskId)).isPresent();
        assertThat(taskRepository.findById(taskId).orElseThrow().getTitle())
                .isEqualTo("Prepare updated interview packet");

        mockMvc.perform(delete("/tasks/{id}", taskId))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.findById(taskId)).isEmpty();
        assertThat(taskRepository.count()).isZero();
    }

    @Test
    void createTask_returnsNotificationNullWhenUrgencyScoreIsBelowThreshold() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Read one article",
                                  "description": "Low urgency reading task",
                                  "status": "TODO",
                                  "priority": "LOW",
                                  "dueDate": "2026-06-15"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.task.id").isNumber())
                .andExpect(jsonPath("$.task.title").value("Read one article"))
                .andExpect(jsonPath("$.notification").isEmpty());
    }

    @Test
    void createTask_returnsNotificationWhenActiveWorkloadReachesThreshold() throws Exception {
        taskRepository.save(task("Finish tax packet", "Existing high priority task", Status.TODO, Priority.HIGH,
                LocalDate.of(2026, 5, 17)));
        taskRepository.save(task("Submit overdue form", "Existing overdue task", Status.IN_PROGRESS, Priority.MEDIUM,
                LocalDate.of(2026, 5, 13)));

        mockMvc.perform(post("/tasks")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "New task during busy week",
                                  "description": "This create request should surface an urgency notification",
                                  "status": "TODO",
                                  "priority": "LOW",
                                  "dueDate": "2026-06-15"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.task.id").isNumber())
                .andExpect(jsonPath("$.task.title").value("New task during busy week"))
                .andExpect(jsonPath("$.notification.score").value(6))
                .andExpect(jsonPath("$.notification.urgentTaskCount").value(2))
                .andExpect(jsonPath("$.notification.message").value(
                        "You have several urgent tasks due soon. Consider prioritizing or rescheduling some of them."
                ));
    }

    @Test
    void createTask_withBlankTitle_returnsBadRequestAndDoesNotCreateTask() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "   ",
                                  "description": "test",
                                  "dueDate": "2026-05-20",
                                  "priority": "LOW",
                                  "status": "TODO"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.task").doesNotExist())
                .andExpect(jsonPath("$.notification").doesNotExist());

        assertThat(taskRepository.count()).isZero();
    }

    @Test
    void createTask_missingStatus_returnsBadRequestAndDoesNotCreateTask() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Missing status task",
                                  "description": "test",
                                  "dueDate": "2026-05-20",
                                  "priority": "LOW"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.task").doesNotExist())
                .andExpect(jsonPath("$.notification").doesNotExist());

        assertThat(taskRepository.count()).isZero();
    }

    @Test
    void createTask_withInvalidPriority_returnsBadRequestAndDoesNotCreateTask() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Invalid priority task",
                                  "description": "test",
                                  "dueDate": "2026-05-20",
                                  "priority": "URGENT",
                                  "status": "TODO"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.task").doesNotExist())
                .andExpect(jsonPath("$.notification").doesNotExist());

        assertThat(taskRepository.count()).isZero();
    }

    @Test
    void createTask_withMalformedJson_returnsBadRequestAndDoesNotCreateTask() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Malformed task",
                                  "description": "test",
                                  "dueDate": "2026-05-20",
                                  "priority": "LOW",
                                  "status": "TODO",
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.task").doesNotExist())
                .andExpect(jsonPath("$.notification").doesNotExist());

        assertThat(taskRepository.count()).isZero();
    }

    @Test
    void getTaskById_withNonexistentId_returnsNotFound() throws Exception {
        mockMvc.perform(get("/tasks/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.task").doesNotExist());
    }

    @Test
    void updateTask_withNonexistentId_returnsNotFound() throws Exception {
        mockMvc.perform(put("/tasks/{id}", 999L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Update missing task",
                                  "description": "Add backend API examples",
                                  "status": "IN_PROGRESS",
                                  "priority": "HIGH",
                                  "dueDate": "2026-05-03"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.task").doesNotExist());
    }

    @Test
    void updateTask_withBlankTitle_returnsBadRequest() throws Exception {
        com.eulerity.task_manager.model.Task existingTask = taskRepository.save(task(
                "Existing task",
                "Existing description",
                Status.TODO,
                Priority.MEDIUM,
                LocalDate.of(2026, 5, 20)
        ));

        mockMvc.perform(put("/tasks/{id}", existingTask.getId())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "   ",
                                  "description": "Updated description",
                                  "status": "IN_PROGRESS",
                                  "priority": "HIGH",
                                  "dueDate": "2026-05-21"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.task").doesNotExist());

        assertThat(taskRepository.findById(existingTask.getId())).isPresent();
        assertThat(taskRepository.findById(existingTask.getId()).orElseThrow().getTitle()).isEqualTo("Existing task");
    }

    @Test
    void deleteTask_withNonexistentId_returnsNotFound() throws Exception {
        mockMvc.perform(delete("/tasks/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.task").doesNotExist());
    }

    private long extractId(String responseBody) {
        Matcher matcher = ID_PATTERN.matcher(responseBody);
        assertThat(matcher.find()).isTrue();
        return Long.parseLong(matcher.group(1));
    }

    private com.eulerity.task_manager.model.Task task(
            String title,
            String description,
            com.eulerity.task_manager.model.Status status,
            com.eulerity.task_manager.model.Priority priority,
            LocalDate dueDate
    ) {
        com.eulerity.task_manager.model.Task task = new com.eulerity.task_manager.model.Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setPriority(priority);
        task.setDueDate(dueDate);
        task.setCreatedAt(LocalDateTime.of(2026, 5, 1, 9, 0));
        task.setUpdatedAt(LocalDateTime.of(2026, 5, 1, 9, 0));
        return task;
    }
}
