package com.eulerity.task_manager.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.eulerity.task_manager.dto.TaskSuggestRequest;
import com.eulerity.task_manager.dto.TaskSuggestResponse;
import com.eulerity.task_manager.model.Priority;
import com.eulerity.task_manager.model.Status;

@Component
public class TaskSuggestionFallbackParser {

    private final Clock clock;

    public TaskSuggestionFallbackParser() {
        this(Clock.systemDefaultZone());
    }

    TaskSuggestionFallbackParser(Clock clock) {
        this.clock = clock;
    }

    public TaskSuggestResponse suggest(TaskSuggestRequest request) {
        String prompt = request.prompt().trim();
        return new TaskSuggestResponse(
                buildTitle(prompt),
                buildDescription(prompt),
                inferDueDate(prompt),
                inferPriority(prompt),
                Status.TODO
        );
    }

    private String buildTitle(String prompt) {
        String[] words = prompt.replaceAll("[^A-Za-z0-9\\s]", " ").trim().split("\\s+");
        String baseTitle = Arrays.stream(words)
                .filter(word -> !word.isBlank())
                .limit(4)
                .map(this::capitalize)
                .reduce((left, right) -> left + " " + right)
                .orElse("Suggested Task");
        return baseTitle.length() > 60 ? baseTitle.substring(0, 60).trim() : baseTitle;
    }

    private String buildDescription(String prompt) {
        return "Suggested from prompt: " + prompt;
    }

    private LocalDate inferDueDate(String prompt) {
        String normalized = prompt.toLowerCase(Locale.ROOT);
        LocalDate today = LocalDate.now(clock);
        if (normalized.contains("today")) {
            return today;
        }
        if (normalized.contains("tomorrow")) {
            return today.plusDays(1);
        }
        if (normalized.contains("next week") || normalized.contains("weekly") || normalized.contains("this week")) {
            return today.plusDays(7);
        }
        if (normalized.contains("weekend")) {
            int daysUntilSaturday = (DayOfWeek.SATURDAY.getValue() - today.getDayOfWeek().getValue() + 7) % 7;
            return today.plusDays(daysUntilSaturday == 0 ? 7 : daysUntilSaturday);
        }
        return today.plusDays(3);
    }

    private Priority inferPriority(String prompt) {
        String normalized = prompt.toLowerCase(Locale.ROOT);
        if (containsAny(normalized, "urgent", "asap", "deadline", "interview", "important", "critical")) {
            return Priority.HIGH;
        }
        if (containsAny(normalized, "later", "eventually", "whenever", "someday", "low priority")) {
            return Priority.LOW;
        }
        return Priority.MEDIUM;
    }

    private boolean containsAny(String value, String... needles) {
        return Arrays.stream(needles).anyMatch(value::contains);
    }

    private String capitalize(String value) {
        if (value.isEmpty()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1).toLowerCase(Locale.ROOT);
    }
}
