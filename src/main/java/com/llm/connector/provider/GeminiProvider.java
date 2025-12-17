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

import java.util.List;
import java.util.Map;

/**
 * Implementation for Google Gemini Gen AI.
 */
public class GeminiProvider implements LlmProvider {

    private final HttpClient httpClient;

    public GeminiProvider() {
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public LlmResponse generate(LlmRequest request, LlmConfig config) {
        String apiKey = config.apiKey();
        String model = config.model() != null ? config.model() : "gemini-2.0-flash";
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key="
                + apiKey;

        // Gemini Request Body Structure
        // { "contents": [{ "parts": [{ "text": "..." }] }], "generationConfig": ... }

        var parts = Map.of("text", request.prompt());
        var contents = Map.of("parts", List.of(parts));

        // Simple config mapping
        Map<String, Object> genConfig = null;
        if (request.temperature() != null || request.maxTokens() != null) {
            genConfig = new java.util.HashMap<>();
            if (request.temperature() != null)
                genConfig.put("temperature", request.temperature());
            if (request.maxTokens() != null)
                genConfig.put("maxOutputTokens", request.maxTokens());
        } else if (config.temperature() != null || config.maxTokens() != null) {
            genConfig = new java.util.HashMap<>();
            if (config.temperature() != null)
                genConfig.put("temperature", config.temperature());
            if (config.maxTokens() != null)
                genConfig.put("maxOutputTokens", config.maxTokens());
        }

        Map<String, Object> payloadMap = new java.util.HashMap<>();
        payloadMap.put("contents", List.of(contents));
        if (genConfig != null) {
            payloadMap.put("generationConfig", genConfig);
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
                throw new ProviderException("Gemini API error: " + response.statusCode() + " - " + response.body());
            }

            return parseResponse(response.body());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProviderException("Failed to call Gemini API", e);
        }
    }

    @SuppressWarnings("unchecked")
    private LlmResponse parseResponse(String responseBody) {
        try {
            Map<String, Object> root = JsonUtil.fromJson(responseBody, Map.class);

            // Navigate: candidates[0].content.parts[0].text
            List<?> candidates = (List<?>) root.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return new LlmResponse("", root);
            }

            Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");
            List<?> parts = (List<?>) content.get("parts");
            Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);
            String text = (String) firstPart.get("text");

            return new LlmResponse(text, root);
        } catch (Exception e) {
            throw new ProviderException("Failed to parse Gemini response", e);
        }
    }
}
