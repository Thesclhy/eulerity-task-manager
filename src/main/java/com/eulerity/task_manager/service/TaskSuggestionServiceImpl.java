package com.eulerity.task_manager.service;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.eulerity.task_manager.dto.TaskSuggestRequest;
import com.eulerity.task_manager.dto.TaskSuggestResponse;

@Service
public class TaskSuggestionServiceImpl implements TaskSuggestionService {

    @Override
    public TaskSuggestResponse suggest(TaskSuggestRequest request) {
        throw new ResponseStatusException(
                HttpStatus.NOT_IMPLEMENTED,
                "Task suggestion service is not implemented yet"
        );
    }
}
