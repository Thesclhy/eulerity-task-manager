package com.eulerity.task_manager.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.eulerity.task_manager.dto.TaskUrgencyNotificationResponse;
import com.eulerity.task_manager.model.Priority;
import com.eulerity.task_manager.model.Status;
import com.eulerity.task_manager.model.Task;
import com.eulerity.task_manager.repository.TaskRepository;

@Service
public class TaskUrgencyNotificationService {

    static final int DEFAULT_NOTIFICATION_THRESHOLD = 6;
    static final String DEFAULT_MESSAGE =
            "You have several urgent tasks due soon. Consider prioritizing or rescheduling some of them.";

    private final TaskRepository taskRepository;
    private final Clock clock;
    private final int notificationThreshold;

    @Autowired
    public TaskUrgencyNotificationService(
            TaskRepository taskRepository,
            @Value("${task.urgency.notification-threshold:6}") int notificationThreshold
    ) {
        this(taskRepository, Clock.systemDefaultZone(), notificationThreshold);
    }

    private TaskUrgencyNotificationService(TaskRepository taskRepository, Clock clock, int notificationThreshold) {
        this.taskRepository = taskRepository;
        this.clock = clock;
        this.notificationThreshold = notificationThreshold;
    }

    TaskUrgencyNotificationService(Clock clock) {
        this(null, clock, DEFAULT_NOTIFICATION_THRESHOLD);
    }

    TaskUrgencyNotificationService(Clock clock, int notificationThreshold) {
        this(null, clock, notificationThreshold);
    }

    public TaskUrgencyNotificationResponse createNotificationForCurrentWorkload() {
        if (taskRepository == null) {
            throw new IllegalStateException("TaskRepository is required to evaluate the current workload");
        }
        return createNotification(taskRepository.findAll());
    }

    public TaskUrgencyNotificationResponse createNotification(List<Task> tasks) {
        LocalDate today = LocalDate.now(clock);
        int score = 0;
        int urgentTaskCount = 0;

        for (Task task : tasks) {
            if (!isActive(task)) {
                continue;
            }

            int taskScore = scoreTask(task, today);
            if (taskScore > 0) {
                urgentTaskCount++;
                score += taskScore;
            }
        }

        if (score < notificationThreshold) {
            return null;
        }

        return new TaskUrgencyNotificationResponse(score, urgentTaskCount, DEFAULT_MESSAGE);
    }

    private boolean isActive(Task task) {
        return task.getStatus() != Status.DONE;
    }

    private int scoreTask(Task task, LocalDate today) {
        int taskScore = 0;

        if (task.getPriority() == Priority.HIGH) {
            taskScore += 2;
        }

        LocalDate dueDate = task.getDueDate();
        if (dueDate == null) {
            return taskScore;
        }

        if (dueDate.isBefore(today)) {
            return taskScore + 3;
        }

        if (dueDate.isEqual(today)) {
            return taskScore + 2;
        }

        if (dueDate.isBefore(today.plusDays(8))) {
            return taskScore + 1;
        }

        return taskScore;
    }
}
