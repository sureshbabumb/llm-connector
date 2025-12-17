package com.llm.connector.core;

/**
 * Represents a generic request to an LLM provider.
 * This can override config defaults if fields are present.
 */
public record LlmRequest(
        String prompt,
        Double temperature,
        Integer maxTokens) {
    public static LlmRequest of(String prompt) {
        return new LlmRequest(prompt, null, null);
    }
}
