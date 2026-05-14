package com.eulerity.task_manager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.eulerity.task_manager.model.Priority;
import com.eulerity.task_manager.model.Status;
import com.eulerity.task_manager.model.Task;

class TaskUrgencyNotificationServiceTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-05-14T12:00:00Z"), ZoneId.of("America/New_York"));

    @Test
    void createNotification_countsActiveHighPriorityTasksTowardScore() throws Exception {
        Object notification = createNotification(List.of(
                activeTask("High Priority One", Priority.HIGH, Status.TODO, null),
                activeTask("High Priority Two", Priority.HIGH, Status.IN_PROGRESS, null),
                activeTask("High Priority Three", Priority.HIGH, Status.TODO, null)
        ));

        assertThat(notification).as("three active HIGH priority tasks should cross the threshold").isNotNull();
        assertThat(scoreOf(notification)).isEqualTo(6);
        assertThat(urgentTaskCountOf(notification)).isEqualTo(3);
        assertThat(messageOf(notification)).isNotBlank();
    }

    @Test
    void createNotification_countsActiveOverdueTasksTowardScore() throws Exception {
        Object notification = createNotification(List.of(
                activeTask("Overdue One", Priority.MEDIUM, Status.TODO, LocalDate.of(2026, 5, 13)),
                activeTask("Overdue Two", Priority.LOW, Status.IN_PROGRESS, LocalDate.of(2026, 5, 10))
        ));

        assertThat(notification).as("two overdue active tasks should cross the threshold").isNotNull();
        assertThat(scoreOf(notification)).isEqualTo(6);
        assertThat(urgentTaskCountOf(notification)).isEqualTo(2);
    }

    @Test
    void createNotification_countsTasksDueWithin24HoursTowardScore() throws Exception {
        Object notification = createNotification(List.of(
                activeTask("Due Today One", Priority.MEDIUM, Status.TODO, LocalDate.of(2026, 5, 14)),
                activeTask("Due Today Two", Priority.LOW, Status.IN_PROGRESS, LocalDate.of(2026, 5, 14)),
                activeTask("Due Today Three", Priority.MEDIUM, Status.TODO, LocalDate.of(2026, 5, 14))
        ));

        assertThat(notification).as("three active tasks due within 24 hours should cross the threshold").isNotNull();
        assertThat(scoreOf(notification)).isEqualTo(6);
        assertThat(urgentTaskCountOf(notification)).isEqualTo(3);
    }

    @Test
    void createNotification_countsTasksDueWithinSevenDaysTowardScore() throws Exception {
        Object notification = createNotification(List.of(
                activeTask("Due In Three Days One", Priority.MEDIUM, Status.TODO, LocalDate.of(2026, 5, 17)),
                activeTask("Due In Three Days Two", Priority.LOW, Status.IN_PROGRESS, LocalDate.of(2026, 5, 17)),
                activeTask("Due In Three Days Three", Priority.MEDIUM, Status.TODO, LocalDate.of(2026, 5, 17)),
                activeTask("Due In Three Days Four", Priority.LOW, Status.TODO, LocalDate.of(2026, 5, 17)),
                activeTask("Due In Three Days Five", Priority.MEDIUM, Status.IN_PROGRESS, LocalDate.of(2026, 5, 17)),
                activeTask("Due In Three Days Six", Priority.LOW, Status.TODO, LocalDate.of(2026, 5, 17))
        ));

        assertThat(notification).as("six active tasks due within seven days should cross the threshold").isNotNull();
        assertThat(scoreOf(notification)).isEqualTo(6);
        assertThat(urgentTaskCountOf(notification)).isEqualTo(6);
    }

    @Test
    void createNotification_ignoresCompletedTasks() throws Exception {
        Object notification = createNotification(List.of(
                activeTask("Done And High", Priority.HIGH, Status.DONE, LocalDate.of(2026, 5, 14)),
                activeTask("Done And Overdue", Priority.MEDIUM, Status.DONE, LocalDate.of(2026, 5, 10)),
                activeTask("Done And Near Due", Priority.LOW, Status.DONE, LocalDate.of(2026, 5, 17))
        ));

        assertThat(notification).as("DONE tasks should not contribute to urgency").isNull();
    }

    @Test
    void createNotification_returnsNullWhenScoreIsBelowThreshold() throws Exception {
        Object notification = createNotification(List.of(
                activeTask("Single High Priority Task", Priority.HIGH, Status.TODO, null),
                activeTask("Single Due Soon Task", Priority.LOW, Status.IN_PROGRESS, LocalDate.of(2026, 5, 17))
        ));

        assertThat(notification).as("a score below six should not create a notification").isNull();
    }

    @Test
    void createNotification_returnsNotificationWhenScoreReachesThreshold() throws Exception {
        Object notification = createNotification(List.of(
                activeTask("High Priority This Week", Priority.HIGH, Status.TODO, LocalDate.of(2026, 5, 17)),
                activeTask("Already Overdue", Priority.MEDIUM, Status.IN_PROGRESS, LocalDate.of(2026, 5, 13))
        ));

        assertThat(notification).as("a score of six or more should create a notification").isNotNull();
        assertThat(scoreOf(notification)).isEqualTo(6);
        assertThat(urgentTaskCountOf(notification)).isEqualTo(2);
        assertThat(messageOf(notification))
                .isEqualTo("You have several urgent tasks due soon. Consider prioritizing or rescheduling some of them.");
    }

    @Test
    void createNotification_usesCustomThresholdWhenProvided() throws Exception {
        Object notification = createNotification(
                List.of(
                        activeTask("Single High Priority Task", Priority.HIGH, Status.TODO, null),
                        activeTask("Single Due Soon Task", Priority.LOW, Status.IN_PROGRESS, LocalDate.of(2026, 5, 17))
                ),
                3
        );

        assertThat(notification).as("a custom threshold should allow lower scores to trigger notifications").isNotNull();
        assertThat(scoreOf(notification)).isEqualTo(3);
        assertThat(urgentTaskCountOf(notification)).isEqualTo(2);
    }

    private Object createNotification(List<Task> tasks) throws Exception {
        return createNotification(tasks, 6);
    }

    private Object createNotification(List<Task> tasks, int notificationThreshold) throws Exception {
        Class<?> serviceClass = loadServiceClass();
        Object service = instantiateService(serviceClass, notificationThreshold);
        Method method = serviceClass.getMethod("createNotification", List.class);
        return method.invoke(service, tasks);
    }

    private Class<?> loadServiceClass() {
        try {
            return Class.forName("com.eulerity.task_manager.service.TaskUrgencyNotificationService");
        } catch (ClassNotFoundException exception) {
            fail("Expected TaskUrgencyNotificationService to be implemented for urgency scoring", exception);
            return null;
        }
    }

    private Object instantiateService(Class<?> serviceClass, int notificationThreshold) throws Exception {
        try {
            Constructor<?> constructor = serviceClass.getDeclaredConstructor(Clock.class, int.class);
            constructor.setAccessible(true);
            return constructor.newInstance(FIXED_CLOCK, notificationThreshold);
        } catch (NoSuchMethodException ignored) {
        }

        try {
            Constructor<?> constructor = serviceClass.getDeclaredConstructor(Clock.class);
            constructor.setAccessible(true);
            return constructor.newInstance(FIXED_CLOCK);
        } catch (NoSuchMethodException ignored) {
            Constructor<?> constructor = serviceClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        }
    }

    private int scoreOf(Object notification) throws Exception {
        Method method = notification.getClass().getMethod("score");
        return ((Number) method.invoke(notification)).intValue();
    }

    private int urgentTaskCountOf(Object notification) throws Exception {
        Method method = notification.getClass().getMethod("urgentTaskCount");
        return ((Number) method.invoke(notification)).intValue();
    }

    private String messageOf(Object notification) throws Exception {
        Method method = notification.getClass().getMethod("message");
        return (String) method.invoke(notification);
    }

    private Task activeTask(String title, Priority priority, Status status, LocalDate dueDate) {
        Task task = new Task();
        task.setTitle(title);
        task.setPriority(priority);
        task.setStatus(status);
        task.setDueDate(dueDate);
        task.setCreatedAt(LocalDateTime.of(2026, 5, 1, 9, 0));
        task.setUpdatedAt(LocalDateTime.of(2026, 5, 1, 9, 0));
        return task;
    }
}
