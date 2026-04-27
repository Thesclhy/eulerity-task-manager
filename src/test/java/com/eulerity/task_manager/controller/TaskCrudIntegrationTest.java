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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Prepare interview packet"))
                .andExpect(jsonPath("$.description").value("Draft examples and print copies"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.dueDate").value("2026-05-01"))
                .andReturn();

        long taskId = extractId(createResult.getResponse().getContentAsString());

        assertThat(taskRepository.count()).isEqualTo(1);
        assertThat(taskRepository.findById(taskId)).isPresent();
        assertThat(taskRepository.findById(taskId).orElseThrow().getDueDate()).isEqualTo(LocalDate.of(2026, 5, 1));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(taskId))
                .andExpect(jsonPath("$[0].title").value("Prepare interview packet"));

        mockMvc.perform(get("/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Prepare interview packet"))
                .andExpect(jsonPath("$.description").value("Draft examples and print copies"));

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
                .andExpect(jsonPath("$.dueDate").value("2026-05-03"));

        assertThat(taskRepository.findById(taskId)).isPresent();
        assertThat(taskRepository.findById(taskId).orElseThrow().getTitle())
                .isEqualTo("Prepare updated interview packet");

        mockMvc.perform(delete("/tasks/{id}", taskId))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.findById(taskId)).isEmpty();
        assertThat(taskRepository.count()).isZero();
    }

    private long extractId(String responseBody) {
        Matcher matcher = ID_PATTERN.matcher(responseBody);
        assertThat(matcher.find()).isTrue();
        return Long.parseLong(matcher.group(1));
    }
}
