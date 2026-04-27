package com.eulerity.task_manager.dto;

import java.time.LocalDate;

import com.eulerity.task_manager.model.Priority;
import com.eulerity.task_manager.model.Status;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TaskRequest(
        @NotBlank String title,
        String description,
        @NotNull Status status,
        @NotNull Priority priority,
        LocalDate dueDate
) {
}
