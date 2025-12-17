package com.llm.connector.client;

import com.llm.connector.config.LlmConfig;
import com.llm.connector.core.LlmProvider;
import com.llm.connector.core.LlmRequest;
import com.llm.connector.core.LlmResponse;
import com.llm.connector.exception.LlmException;
import com.llm.connector.provider.AnthropicProvider;
import com.llm.connector.provider.GeminiProvider;
import com.llm.connector.provider.OllamaProvider;
import com.llm.connector.provider.OpenAiProvider;

/**
 * Main entry point for the LLM SDK.
 */
public class LlmClient {
    private final LlmProvider provider;
    private final LlmConfig config;

    private LlmClient(LlmProvider provider, LlmConfig config) {
        this.provider = provider;
        this.config = config;
    }

    /**
     * Generates text for the given prompt using default settings.
     * 
     * @param prompt The input text prompt.
     * @return The generated response text.
     */
    public String generate(String prompt) {
        LlmRequest request = LlmRequest.of(prompt);
        LlmResponse response = provider.generate(request, config);
        return response.text();
    }

    /**
     * Generates text for the given request object, allowing per-request overrides.
     * 
     * @param request The full request object.
     * @return The full response object.
     */
    public LlmResponse generate(LlmRequest request) {
        return provider.generate(request, config);
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum ProviderType {
        GEMINI, OPENAI, ANTHROPIC, OLLAMA, CUSTOM
    }

    public static class Builder {
        private ProviderType providerType;
        private LlmProvider customProvider;
        private LlmConfig config;

        // Config builder fields
        private String apiKey;
        private String model;
        private Double temperature;
        private Integer maxTokens;
        private java.time.Duration timeout;

        public Builder provider(ProviderType type) {
            this.providerType = type;
            return this;
        }

        public Builder customProvider(LlmProvider provider) {
            this.providerType = ProviderType.CUSTOM;
            this.customProvider = provider;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder timeout(java.time.Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder config(LlmConfig config) {
            this.config = config;
            return this;
        }

        public LlmClient build() {
            if (config == null) {
                config = new LlmConfig(apiKey, model, timeout, temperature, maxTokens);
            }

            LlmProvider selectedProvider;
            if (providerType == null) {
                throw new LlmException("Provider type must be set.");
            }

            switch (providerType) {
                case GEMINI -> selectedProvider = new GeminiProvider();
                case OPENAI -> selectedProvider = new OpenAiProvider();
                case ANTHROPIC -> selectedProvider = new AnthropicProvider();
                case OLLAMA -> selectedProvider = new OllamaProvider();
                case CUSTOM -> {
                    if (customProvider == null)
                        throw new LlmException("Custom provider implementation must be provided for CUSTOM type.");
                    selectedProvider = customProvider;
                }
                default -> throw new LlmException("Unknown provider type");
            }

            return new LlmClient(selectedProvider, config);
        }
    }
}
