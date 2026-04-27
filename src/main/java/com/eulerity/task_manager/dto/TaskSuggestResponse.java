package com.eulerity.task_manager.dto;

import com.eulerity.task_manager.model.Priority;

public record TaskSuggestResponse(
        String title,
        String description,
        Priority priority
) {
}
