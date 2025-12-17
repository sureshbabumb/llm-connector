package com.llm.connector.exception;

/**
 * Exception thrown when an underlying provider fails.
 */
public class ProviderException extends LlmException {
    public ProviderException(String message) {
        super(message);
    }

    public ProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
