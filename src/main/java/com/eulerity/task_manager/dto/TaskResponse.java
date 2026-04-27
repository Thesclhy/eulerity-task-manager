package com.eulerity.task_manager.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.eulerity.task_manager.model.Priority;
import com.eulerity.task_manager.model.Status;

public record TaskResponse(
        Long id,
        String title,
        String description,
        Status status,
        Priority priority,
        LocalDate dueDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
