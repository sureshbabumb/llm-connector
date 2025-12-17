package com.llm.connector.provider;

import com.llm.connector.config.LlmConfig;
import com.llm.connector.core.LlmProvider;
import com.llm.connector.core.LlmRequest;
import com.llm.connector.core.LlmResponse;
import com.llm.connector.exception.ProviderException;
import com.llm.connector.util.JsonUtil;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation for Ollama (Local LLM).
 */
public class OllamaProvider implements LlmProvider {

    private final HttpClient httpClient;

    public OllamaProvider() {
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public LlmResponse generate(LlmRequest request, LlmConfig config) {
        // Ollama usually runs on localhost:11434
        // We will allow the apiKey field to potentially hold the base URL if needed,
        // or just assume standard if not provided in encoded form.
        // Actually, for Ollama, 'apiKey' is unused. We should probably use 'apiKey' to
        // store the Base URL if it's not standard?
        // Or better, let's assume the user might pass the base URL in the apiKey field
        // if they are hacking it,
        // but cleaner is to use a specific config.
        // Given LlmConfig has fixed fields, I'll default to localhost:11434 if apiKey
        // is null/empty or doesn't look like a URL.
        // Wait, LlmConfig doesn't have baseUrl. I'll check if apiKey looks like a URL.

        String baseUrl = "http://localhost:11434";
        if (config.apiKey() != null && config.apiKey().startsWith("http")) {
            baseUrl = config.apiKey();
        }

        String url = baseUrl + "/api/generate";
        String model = config.model() != null ? config.model() : "llama2"; // Default generic

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("model", model);
        payloadMap.put("prompt", request.prompt());
        payloadMap.put("stream", false); // Important for non-streaming

        Map<String, Object> options = new HashMap<>();
        if (request.temperature() != null)
            options.put("temperature", request.temperature());
        else if (config.temperature() != null)
            options.put("temperature", config.temperature());

        if (request.maxTokens() != null)
            options.put("num_predict", request.maxTokens());
        else if (config.maxTokens() != null)
            options.put("num_predict", config.maxTokens());

        if (!options.isEmpty()) {
            payloadMap.put("options", options);
        }

        String jsonBody = JsonUtil.toJson(payloadMap);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(config.timeout())
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new ProviderException("Ollama API error: " + response.statusCode() + " - " + response.body());
            }

            return parseResponse(response.body());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProviderException("Failed to call Ollama API", e);
        }
    }

    @SuppressWarnings("unchecked")
    private LlmResponse parseResponse(String responseBody) {
        try {
            Map<String, Object> root = JsonUtil.fromJson(responseBody, Map.class);
            String response = (String) root.get("response");
            return new LlmResponse(response, root);
        } catch (Exception e) {
            throw new ProviderException("Failed to parse Ollama response", e);
        }
    }
}
