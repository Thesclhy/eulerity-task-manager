package com.eulerity.task_manager.dto;

public record TaskUrgencyNotificationResponse(
        int score,
        int urgentTaskCount,
        String message
) {
}
