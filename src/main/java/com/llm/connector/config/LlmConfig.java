package com.llm.connector.config;

import java.time.Duration;

/**
 * Configuration for an LLM provider.
 * Use valid defaults where appropriate.
 */
public record LlmConfig(
        String apiKey,
        String model,
        Duration timeout,
        Double temperature,
        Integer maxTokens) {
    public LlmConfig {
        if (timeout == null)
            timeout = Duration.ofSeconds(30);
        if (temperature == null)
            temperature = 0.7;
    }

    // Builder-like static factory for convenience if needed,
    // or just rely on canonical constructor.
}
