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
import java.util.List;
import java.util.Map;

/**
 * Implementation for OpenAI.
 */
public class OpenAiProvider implements LlmProvider {

    private final HttpClient httpClient;

    public OpenAiProvider() {
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public LlmResponse generate(LlmRequest request, LlmConfig config) {
        String apiKey = config.apiKey();
        String model = config.model() != null ? config.model() : "gpt-3.5-turbo";
        String defaultUrl = "https://api.openai.com/v1/chat/completions";
        // Allow Base URL override for proxies (useful for corporate envs)
        // But config doesn't have baseUrl yet? Ah, I should have added it.
        // I'll stick to default for now, unless I modify LlmConfig.

        Map<String, Object> message = Map.of("role", "user", "content", request.prompt());

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("model", model);
        payloadMap.put("messages", List.of(message));

        if (request.temperature() != null)
            payloadMap.put("temperature", request.temperature());
        else if (config.temperature() != null)
            payloadMap.put("temperature", config.temperature());

        if (request.maxTokens() != null)
            payloadMap.put("max_tokens", request.maxTokens());
        else if (config.maxTokens() != null)
            payloadMap.put("max_tokens", config.maxTokens());

        String jsonBody = JsonUtil.toJson(payloadMap);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(defaultUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(config.timeout())
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new ProviderException("OpenAI API error: " + response.statusCode() + " - " + response.body());
            }

            return parseResponse(response.body());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProviderException("Failed to call OpenAI API", e);
        }
    }

    @SuppressWarnings("unchecked")
    private LlmResponse parseResponse(String responseBody) {
        try {
            Map<String, Object> root = JsonUtil.fromJson(responseBody, Map.class);
            List<?> choices = (List<?>) root.get("choices");
            if (choices == null || choices.isEmpty()) {
                return new LlmResponse("", root);
            }

            Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
            Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
            String content = (String) message.get("content");

            return new LlmResponse(content, root);
        } catch (Exception e) {
            throw new ProviderException("Failed to parse OpenAI response", e);
        }
    }
}
