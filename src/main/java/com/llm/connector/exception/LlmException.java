package com.llm.connector.exception;

/**
 * Base exception for all LLM SDK errors.
 */
public class LlmException extends RuntimeException {
    public LlmException(String message) {
        super(message);
    }

    public LlmException(String message, Throwable cause) {
        super(message, cause);
    }
}
