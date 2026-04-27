package com.eulerity.task_manager.dto;

import java.time.LocalDate;

import com.eulerity.task_manager.model.Priority;
import com.eulerity.task_manager.model.Status;

public record TaskSuggestResponse(
        String title,
        String description,
        LocalDate dueDate,
        Priority priority,
        Status status
) {
}
