package com.eulerity.task_manager.service;

import com.eulerity.task_manager.dto.TaskSuggestRequest;
import com.eulerity.task_manager.dto.TaskSuggestResponse;

public interface TaskSuggestionClient {

    TaskSuggestResponse suggest(TaskSuggestRequest request);
}
