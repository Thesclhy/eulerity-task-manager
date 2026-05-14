package com.eulerity.task_manager.controller;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import com.eulerity.task_manager.dto.TaskSuggestResponse;
import com.eulerity.task_manager.model.Priority;
import com.eulerity.task_manager.model.Status;
import com.eulerity.task_manager.service.TaskService;
import com.eulerity.task_manager.service.TaskSuggestionService;
import com.eulerity.task_manager.service.TaskUrgencyNotificationService;

import java.time.LocalDate;

@WebMvcTest(TaskController.class)
@Import(TaskSuggestionControllerTest.MockConfig.class)
class TaskSuggestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskSuggestionService taskSuggestionService;

    @BeforeEach
    void resetMocks() {
        Mockito.reset(taskSuggestionService);
    }

    @Test
    void postTasksSuggest_returnsSuggestedTaskFromAiService() throws Exception {
        when(taskSuggestionService.suggest(Mockito.any())).thenReturn(new TaskSuggestResponse(
                "Plan weekly applications",
                "Block time for applications, follow-ups, and study sessions",
                LocalDate.of(2026, 5, 4),
                Priority.HIGH,
                Status.TODO
        ));

        mockMvc.perform(post("/tasks/suggest")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "prompt": "I need to plan my week for job applications and study time"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Plan weekly applications"))
                .andExpect(jsonPath("$.description")
                        .value("Block time for applications, follow-ups, and study sessions"))
                .andExpect(jsonPath("$.dueDate").value("2026-05-04"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void postTasksSuggest_withBlankPrompt_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/tasks/suggest")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "prompt": "   "
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(taskSuggestionService);
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
