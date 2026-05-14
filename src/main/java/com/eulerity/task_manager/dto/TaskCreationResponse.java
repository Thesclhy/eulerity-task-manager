package com.eulerity.task_manager.dto;

public record TaskCreationResponse(
        TaskResponse task,
        TaskUrgencyNotificationResponse notification
) {
}
