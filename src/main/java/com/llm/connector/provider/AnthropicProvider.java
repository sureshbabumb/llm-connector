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
 * Implementation for Anthropic (Claude).
 */
public class AnthropicProvider implements LlmProvider {

    private final HttpClient httpClient;

    public AnthropicProvider() {
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public LlmResponse generate(LlmRequest request, LlmConfig config) {
        String apiKey = config.apiKey();
        String model = config.model() != null ? config.model() : "claude-3-sonnet-20240229"; // Default to a recent
                                                                                             // model
        String url = "https://api.anthropic.com/v1/messages";

        Map<String, Object> message = Map.of("role", "user", "content", request.prompt());

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("model", model);
        payloadMap.put("messages", List.of(message));

        // Anthropic requires max_tokens to be set usually
        Integer maxTokens = request.maxTokens() != null ? request.maxTokens() : config.maxTokens();
        if (maxTokens == null)
            maxTokens = 1024; // Default if not provided
        payloadMap.put("max_tokens", maxTokens);

        if (request.temperature() != null)
            payloadMap.put("temperature", request.temperature());
        else if (config.temperature() != null)
            payloadMap.put("temperature", config.temperature());

        String jsonBody = JsonUtil.toJson(payloadMap);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(config.timeout())
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new ProviderException("Anthropic API error: " + response.statusCode() + " - " + response.body());
            }

            return parseResponse(response.body());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProviderException("Failed to call Anthropic API", e);
        }
    }

    @SuppressWarnings("unchecked")
    private LlmResponse parseResponse(String responseBody) {
        try {
            Map<String, Object> root = JsonUtil.fromJson(responseBody, Map.class);

            // Response structure: { content: [ { type: "text", text: "..." } ] }
            List<?> content = (List<?>) root.get("content");
            if (content == null || content.isEmpty()) {
                return new LlmResponse("", root);
            }

            // content gives list of blocks. type="text"
            Map<?, ?> firstBlock = (Map<?, ?>) content.get(0);
            String text = (String) firstBlock.get("text");

            return new LlmResponse(text, root);
        } catch (Exception e) {
            throw new ProviderException("Failed to parse Anthropic response", e);
        }
    }
}
