package com.eulerity.task_manager.service;

import java.io.IOException;

public interface OpenAiResponsesApi {

    String createResponse(String apiKey, String requestBody) throws IOException, InterruptedException;
}
