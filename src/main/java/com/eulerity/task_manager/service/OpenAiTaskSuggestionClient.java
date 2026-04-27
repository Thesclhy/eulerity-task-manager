package com.eulerity.task_manager.service;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.eulerity.task_manager.dto.TaskSuggestRequest;
import com.eulerity.task_manager.dto.TaskSuggestResponse;
import com.eulerity.task_manager.model.Priority;
import com.eulerity.task_manager.model.Status;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Component
public class OpenAiTaskSuggestionClient implements TaskSuggestionClient {

    private static final String DEFAULT_MODEL = "gpt-4o-mini";

    private final OpenAiResponsesApi openAiResponsesApi;
    private final TaskSuggestionFallbackParser fallbackParser;
    private final ObjectMapper objectMapper;
    private final String openAiApiKey;
    private final String model;
    private final Clock clock;

    @Autowired
    public OpenAiTaskSuggestionClient(
            OpenAiResponsesApi openAiResponsesApi,
            TaskSuggestionFallbackParser fallbackParser,
            @Value("${OPENAI_API_KEY:}") String openAiApiKey,
            @Value("${OPENAI_MODEL:gpt-4o-mini}") String model
    ) {
        this(
                openAiResponsesApi,
                fallbackParser,
                JsonMapper.builder().findAndAddModules().build(),
                openAiApiKey,
                model,
                Clock.systemDefaultZone()
        );
    }

    OpenAiTaskSuggestionClient(
            OpenAiResponsesApi openAiResponsesApi,
            TaskSuggestionFallbackParser fallbackParser,
            ObjectMapper objectMapper,
            String openAiApiKey,
            String model,
            Clock clock
    ) {
        this.openAiResponsesApi = openAiResponsesApi;
        this.fallbackParser = fallbackParser;
        this.objectMapper = objectMapper;
        this.openAiApiKey = openAiApiKey;
        this.model = (model == null || model.isBlank()) ? DEFAULT_MODEL : model;
        this.clock = clock;
    }

    @Override
    public TaskSuggestResponse suggest(TaskSuggestRequest request) {
        try {
            String responseBody = openAiResponsesApi.createResponse(openAiApiKey, buildRequestBody(request));
            return validateSuggestion(request, parseResponse(responseBody));
        } catch (Exception ignored) {
            return fallbackParser.suggest(request);
        }
    }

    private String buildRequestBody(TaskSuggestRequest request) throws IOException {
        LocalDate today = LocalDate.now(clock);
        JsonNode schema = objectMapper.createObjectNode()
                .put("type", "object")
                .set("properties", objectMapper.createObjectNode()
                        .set("title", objectMapper.createObjectNode().put("type", "string"))
                        .set("description", objectMapper.createObjectNode().put("type", "string"))
                        .set("dueDate", objectMapper.createObjectNode().put("type", "string").put("format", "date"))
                        .set("priority", objectMapper.createObjectNode()
                                .put("type", "string")
                                .set("enum", objectMapper.createArrayNode().add("LOW").add("MEDIUM").add("HIGH")))
                        .set("status", objectMapper.createObjectNode()
                                .put("type", "string")
                                .set("enum", objectMapper.createArrayNode().add("TODO").add("IN_PROGRESS").add("DONE"))))
                .set("required", objectMapper.createArrayNode()
                        .add("title")
                        .add("description")
                        .add("dueDate")
                        .add("priority")
                        .add("status"))
                .put("additionalProperties", false);

        JsonNode requestJson = objectMapper.createObjectNode()
                .put("model", model)
                .put("instructions", ("""
                        You suggest one task from the user's prompt.
                        Return only valid JSON that matches the provided schema.
                        Today's date is %s.
                        Interpret any relative dates in the user's prompt based on today's date.
                        Never return a dueDate in the past.
                        Choose a practical dueDate and use one of LOW, MEDIUM, HIGH for priority
                        and TODO, IN_PROGRESS, DONE for status.
                        """.formatted(today)).strip())
                .put("input", request.prompt())
                .set("text", objectMapper.createObjectNode()
                        .set("format", objectMapper.createObjectNode()
                                .put("type", "json_schema")
                                .put("name", "task_suggestion")
                                .put("strict", true)
                                .set("schema", schema)));

        return objectMapper.writeValueAsString(requestJson);
    }

    private TaskSuggestResponse parseResponse(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        String jsonText = extractStructuredOutput(root);
        if (jsonText == null || jsonText.isBlank()) {
            throw new IOException("OpenAI response did not contain structured output text");
        }

        JsonNode payload = objectMapper.readTree(jsonText);
        String dueDateText = payload.path("dueDate").isTextual() ? payload.path("dueDate").asText() : null;
        LocalDate dueDate = (dueDateText == null || dueDateText.isBlank()) ? null : LocalDate.parse(dueDateText);
        return new TaskSuggestResponse(
                payload.path("title").asText(),
                payload.path("description").asText(),
                dueDate,
                Priority.valueOf(payload.path("priority").asText().toUpperCase(Locale.ROOT)),
                Status.valueOf(payload.path("status").asText().toUpperCase(Locale.ROOT))
        );
    }

    private TaskSuggestResponse validateSuggestion(TaskSuggestRequest request, TaskSuggestResponse response) {
        LocalDate today = LocalDate.now(clock);
        LocalDate fallbackDueDate = fallbackParser.suggest(request).dueDate();
        LocalDate dueDate = response.dueDate();
        if (dueDate == null || dueDate.isBefore(today)) {
            dueDate = fallbackDueDate != null ? fallbackDueDate : today.plusDays(3);
        }

        return new TaskSuggestResponse(
                response.title(),
                response.description(),
                dueDate,
                response.priority(),
                response.status()
        );
    }

    private String extractStructuredOutput(JsonNode root) {
        JsonNode outputText = root.path("output_text");
        if (outputText.isTextual()) {
            return outputText.asText();
        }

        JsonNode output = root.path("output");
        if (!output.isArray()) {
            return null;
        }

        for (JsonNode item : output) {
            JsonNode content = item.path("content");
            if (!content.isArray()) {
                continue;
            }
            for (JsonNode contentItem : content) {
                JsonNode textNode = contentItem.path("text");
                if (textNode.isTextual()) {
                    return textNode.asText();
                }
            }
        }

        return null;
    }
}
