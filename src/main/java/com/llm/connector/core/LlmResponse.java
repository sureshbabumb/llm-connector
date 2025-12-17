package com.llm.connector.core;

import java.util.Map;

/**
 * Represents a generic response from an LLM provider.
 */
public record LlmResponse(
                String text,
                Map<String, Object> metadata) {
}
