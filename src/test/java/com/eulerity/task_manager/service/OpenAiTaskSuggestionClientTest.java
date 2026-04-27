package com.eulerity.task_manager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import com.eulerity.task_manager.dto.TaskSuggestRequest;
import com.eulerity.task_manager.dto.TaskSuggestResponse;
import com.eulerity.task_manager.model.Priority;
import com.eulerity.task_manager.model.Status;

import tools.jackson.databind.json.JsonMapper;

class OpenAiTaskSuggestionClientTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-04-27T10:00:00Z"), ZoneId.of("America/New_York"));

    @Test
    void suggest_parsesStructuredJsonResponse() throws Exception {
        OpenAiResponsesApi responsesApi = mock(OpenAiResponsesApi.class);
        TaskSuggestionFallbackParser fallbackParser = new TaskSuggestionFallbackParser(FIXED_CLOCK);
        OpenAiTaskSuggestionClient client = new OpenAiTaskSuggestionClient(
                responsesApi,
                fallbackParser,
                JsonMapper.builder().findAndAddModules().build(),
                "test-key",
                "test-model",
                FIXED_CLOCK
        );
        TaskSuggestRequest request = new TaskSuggestRequest("help me organize interview prep");
        when(responsesApi.createResponse(eq("test-key"), anyString()))
                .thenReturn("""
                        {
                          "output_text": "{\\"title\\":\\"Interview Prep\\",\\"description\\":\\"Review notes and rehearse examples\\",\\"dueDate\\":\\"2026-05-01\\",\\"priority\\":\\"HIGH\\",\\"status\\":\\"TODO\\"}"
                        }
                        """);

        TaskSuggestResponse response = client.suggest(request);

        assertThat(response.title()).isEqualTo("Interview Prep");
        assertThat(response.description()).isEqualTo("Review notes and rehearse examples");
        assertThat(response.dueDate()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(response.priority()).isEqualTo(Priority.HIGH);
        assertThat(response.status()).isEqualTo(Status.TODO);
        verify(responsesApi).createResponse(
                eq("test-key"),
                argThat(body -> body.contains("Today's date is 2026-04-27.")
                        && body.contains("Interpret any relative dates in the user's prompt based on today's date."))
        );
    }

    @Test
    void suggest_fallsBackWhenApiReturnsInvalidJson() throws Exception {
        OpenAiResponsesApi responsesApi = mock(OpenAiResponsesApi.class);
        TaskSuggestionFallbackParser fallbackParser = new TaskSuggestionFallbackParser(FIXED_CLOCK);
        OpenAiTaskSuggestionClient client = new OpenAiTaskSuggestionClient(
                responsesApi,
                fallbackParser,
                JsonMapper.builder().findAndAddModules().build(),
                "test-key",
                "test-model",
                FIXED_CLOCK
        );
        TaskSuggestRequest request = new TaskSuggestRequest("urgent interview prep for tomorrow");
        when(responsesApi.createResponse(eq("test-key"), anyString()))
                .thenReturn("{\"output_text\":\"not-json\"}");

        TaskSuggestResponse response = client.suggest(request);

        assertThat(response.title()).isEqualTo("Urgent Interview Prep For");
        assertThat(response.dueDate()).isEqualTo(LocalDate.of(2026, 4, 28));
        assertThat(response.priority()).isEqualTo(Priority.HIGH);
        assertThat(response.status()).isEqualTo(Status.TODO);
    }

    @Test
    void suggest_replacesPastAiDueDateWithDeterministicFallbackDueDate() throws Exception {
        OpenAiResponsesApi responsesApi = mock(OpenAiResponsesApi.class);
        TaskSuggestionFallbackParser fallbackParser = new TaskSuggestionFallbackParser(FIXED_CLOCK);
        OpenAiTaskSuggestionClient client = new OpenAiTaskSuggestionClient(
                responsesApi,
                fallbackParser,
                JsonMapper.builder().findAndAddModules().build(),
                "test-key",
                "test-model",
                FIXED_CLOCK
        );
        TaskSuggestRequest request = new TaskSuggestRequest("remind me to submit the quarterly report before Friday");
        when(responsesApi.createResponse(eq("test-key"), anyString()))
                .thenReturn("""
                        {
                          "output_text": "{\\"title\\":\\"Submit Quarterly Report\\",\\"description\\":\\"Finish and submit the report before Friday\\",\\"dueDate\\":\\"2023-10-06\\",\\"priority\\":\\"HIGH\\",\\"status\\":\\"TODO\\"}"
                        }
                        """);

        TaskSuggestResponse response = client.suggest(request);

        assertThat(response.title()).isEqualTo("Submit Quarterly Report");
        assertThat(response.dueDate()).isEqualTo(LocalDate.of(2026, 4, 30));
        assertThat(response.priority()).isEqualTo(Priority.HIGH);
        assertThat(response.status()).isEqualTo(Status.TODO);
    }

    @Test
    void suggest_replacesMissingAiDueDateWithDeterministicFallbackDueDate() throws Exception {
        OpenAiResponsesApi responsesApi = mock(OpenAiResponsesApi.class);
        TaskSuggestionFallbackParser fallbackParser = new TaskSuggestionFallbackParser(FIXED_CLOCK);
        OpenAiTaskSuggestionClient client = new OpenAiTaskSuggestionClient(
                responsesApi,
                fallbackParser,
                JsonMapper.builder().findAndAddModules().build(),
                "test-key",
                "test-model",
                FIXED_CLOCK
        );
        TaskSuggestRequest request = new TaskSuggestRequest("plan my week");
        when(responsesApi.createResponse(eq("test-key"), anyString()))
                .thenReturn("""
                        {
                          "output_text": "{\\"title\\":\\"Plan My Week\\",\\"description\\":\\"Organize the week\\" ,\\"priority\\":\\"MEDIUM\\",\\"status\\":\\"TODO\\"}"
                        }
                        """);

        TaskSuggestResponse response = client.suggest(request);

        assertThat(response.dueDate()).isEqualTo(LocalDate.of(2026, 4, 30));
        assertThat(response.priority()).isEqualTo(Priority.MEDIUM);
        assertThat(response.status()).isEqualTo(Status.TODO);
    }
}
