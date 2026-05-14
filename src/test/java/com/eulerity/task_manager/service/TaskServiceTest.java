package com.eulerity.task_manager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eulerity.task_manager.dto.TaskRequest;
import com.eulerity.task_manager.dto.TaskResponse;
import com.eulerity.task_manager.model.Priority;
import com.eulerity.task_manager.model.Status;
import com.eulerity.task_manager.model.Task;
import com.eulerity.task_manager.repository.TaskRepository;

class TaskServiceTest {

    private TaskRepository taskRepository;
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        taskService = new TaskServiceImpl(taskRepository);
    }

    @Test
    void create_returnsCreatedTask() {
        TaskRequest request = new TaskRequest(
                "Prepare interview packet",
                "Draft examples and print copies",
                Status.TODO,
                Priority.HIGH,
                LocalDate.of(2026, 5, 1)
        );
        Task savedTask = task(
                1L,
                "Prepare interview packet",
                "Draft examples and print copies",
                Status.TODO,
                Priority.HIGH
        );
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        TaskResponse response = taskService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Prepare interview packet");
        assertThat(response.status()).isEqualTo(Status.TODO);
        assertThat(response.priority()).isEqualTo(Priority.HIGH);
    }

    @Test
    void findAll_returnsAllTasks() {
        Task firstTask = task(1L, "Prepare interview packet", "Draft examples", Status.TODO, Priority.HIGH);
        Task secondTask = task(2L, "Review notes", "Review system design", Status.IN_PROGRESS, Priority.MEDIUM);
        when(taskRepository.findAll()).thenReturn(List.of(firstTask, secondTask));

        List<TaskResponse> response = taskService.findAll();

        assertThat(response).hasSize(2);
        assertThat(response).extracting(TaskResponse::id).containsExactly(1L, 2L);
        assertThat(response).extracting(TaskResponse::title)
                .containsExactly("Prepare interview packet", "Review notes");
    }

    @Test
    void findById_returnsTaskWhenFound() {
        Task existingTask = task(1L, "Prepare interview packet", "Draft examples", Status.TODO, Priority.HIGH);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));

        TaskResponse response = taskService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Prepare interview packet");
        assertThat(response.description()).isEqualTo("Draft examples");
    }

    @Test
    void update_returnsUpdatedTask() {
        Task existingTask = task(1L, "Prepare interview packet", "Draft examples", Status.TODO, Priority.HIGH);
        Task updatedTask = task(
                1L,
                "Prepare updated interview packet",
                "Add backend API examples",
                Status.IN_PROGRESS,
                Priority.HIGH
        );
        TaskRequest updateRequest = new TaskRequest(
                "Prepare updated interview packet",
                "Add backend API examples",
                Status.IN_PROGRESS,
                Priority.HIGH,
                LocalDate.of(2026, 5, 3)
        );
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        TaskResponse response = taskService.update(1L, updateRequest);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Prepare updated interview packet");
        assertThat(response.status()).isEqualTo(Status.IN_PROGRESS);
        assertThat(response.priority()).isEqualTo(Priority.HIGH);
    }

    @Test
    void delete_removesTaskById() {
        Task existingTask = task(1L, "Prepare interview packet", "Draft examples", Status.TODO, Priority.HIGH);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));

        taskService.delete(1L);

        verify(taskRepository).delete(existingTask);
    }

    private Task task(Long id, String title, String description, Status status, Priority priority) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setPriority(priority);
        task.setDueDate(LocalDate.of(2026, 5, 1));
        task.setCreatedAt(LocalDateTime.of(2026, 4, 27, 10, 30));
        task.setUpdatedAt(LocalDateTime.of(2026, 4, 27, 10, 30));
        return task;
    }
}
