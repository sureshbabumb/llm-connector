package com.llm.connector.core;

import com.llm.connector.config.LlmConfig;

/**
 * Strategy interface for LLM providers.
 */
public interface LlmProvider {
    /**
     * Generates text based on the request using the given config.
     * 
     * @param request The prompt and implementation-specific overrides.
     * @param config  The base configuration (api key, model, etc).
     * @return The response containing the generated text.
     */
    LlmResponse generate(LlmRequest request, LlmConfig config);
}
