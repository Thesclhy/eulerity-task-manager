package com.eulerity.task_manager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.eulerity.task_manager.dto.TaskSuggestRequest;
import com.eulerity.task_manager.dto.TaskSuggestResponse;

@Service
public class TaskSuggestionServiceImpl implements TaskSuggestionService {

    private final TaskSuggestionClient taskSuggestionClient;
    private final TaskSuggestionFallbackParser fallbackParser;
    private final String openAiApiKey;

    public TaskSuggestionServiceImpl(
            TaskSuggestionClient taskSuggestionClient,
            TaskSuggestionFallbackParser fallbackParser,
            @Value("${OPENAI_API_KEY:}") String openAiApiKey
    ) {
        this.taskSuggestionClient = taskSuggestionClient;
        this.fallbackParser = fallbackParser;
        this.openAiApiKey = openAiApiKey;
    }

    @Override
    public TaskSuggestResponse suggest(TaskSuggestRequest request) {
        if (StringUtils.hasText(openAiApiKey)) {
            return taskSuggestionClient.suggest(request);
        }
        return fallbackParser.suggest(request);
    }
}
