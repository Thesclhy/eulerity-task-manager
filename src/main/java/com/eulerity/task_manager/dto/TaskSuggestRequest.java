package com.eulerity.task_manager.dto;

import jakarta.validation.constraints.NotBlank;

public record TaskSuggestRequest(@NotBlank String prompt) {
}
