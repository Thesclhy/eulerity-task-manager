package com.eulerity.task_manager.service;

import java.util.List;

import com.eulerity.task_manager.dto.TaskRequest;
import com.eulerity.task_manager.dto.TaskResponse;

public interface TaskService {

    TaskResponse create(TaskRequest request);

    List<TaskResponse> findAll();

    TaskResponse findById(Long id);

    TaskResponse update(Long id, TaskRequest request);

    void delete(Long id);
}
