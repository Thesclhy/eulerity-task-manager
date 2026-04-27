package com.eulerity.task_manager.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class OpenAiResponsesHttpApi implements OpenAiResponsesApi {

    private static final URI RESPONSES_URI = URI.create("https://api.openai.com/v1/responses");

    private final HttpClient httpClient;

    public OpenAiResponsesHttpApi() {
        this(HttpClient.newHttpClient());
    }

    OpenAiResponsesHttpApi(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String createResponse(String apiKey, String requestBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(RESPONSES_URI)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < HttpStatus.OK.value() || response.statusCode() >= HttpStatus.MULTIPLE_CHOICES.value()) {
            throw new IOException("OpenAI API returned status " + response.statusCode());
        }
        return response.body();
    }
}
