# AIConnector - Java LLM SDK

A lightweight, framework-agnostic Java SDK designed to connect seamlessly to major LLM providers (Google Gemini, OpenAI, Anthropic) and local models (Ollama).

This SDK is built with **Java 17** and aims to provide a unified interface for text generation, keeping your business logic decoupled from specific provider implementations. It carries minimal dependencies (Jackson for JSON, SLF4J for logging) and contains **no Spring/Spring Boot dependencies** in the core, making it suitable for any Java environment.

## Features
*   **Unified API**: Switch between Gemini, OpenAI, Claude, and Ollama with a single configuration change.
*   **Framework Agnostic**: Pure Java implementation usable in CLI tools, utility, libraries, or Spring Boot apps.
*   **Zero-Overhead**: No bloat. Just the HTTP Client and JSON parsing.
*   **Extensible**: Strategy pattern design allows easy addition of new providers or custom implementations.

## Supported Providers & Defaults
| Provider | Key Type | Default Model |
| :--- | :--- | :--- |
| **Gemini** | `GEMINI_API_KEY` | `gemini-2.0-flash` |
| **OpenAI** | `OPENAI_API_KEY` | `gpt-3.5-turbo` |
| **Anthropic** | `ANTHROPIC_API_KEY` | `claude-3-sonnet-20240229` |
| **Ollama** | *None* | `llama2` |

## Installation
Ensure you have **Java 17+** and **Maven** installed.

Build the project locally:
```bash
mvn clean package

This will create a jar file in the target directory.

Use the jar file in your project.
```

## Quick Start
The entry point is the `LlmClient`, built using a fluent builder pattern.

```java
import com.llm.connector.client.LlmClient;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize Client (e.g., for Gemini)
        LlmClient client = LlmClient.builder()
            .provider(LlmClient.ProviderType.GEMINI)
            .apiKey(System.getenv("GEMINI_API_KEY")) // Recommended: Use Env Vars
            .build();

        // 2. Generate Text
        String response = client.generate("Explain quantum computing in 5 words.");
        System.out.println(response);
    }
}
```

## Configuration

### Environment Variables
We primarily recommend using Environment Variables to manage credentials securely:
*   `GEMINI_API_KEY`
*   `OPENAI_API_KEY`
*   `ANTHROPIC_API_KEY`

### 2. Client Configuration Options
You can fine-tune the client behavior via the builder:

| Option | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `apiKey` | String | `null` | The API Key for the provider. |
| `model` | String | *Provider Default* | Override default model (e.g., `gpt-4`). |
| `temperature` | Double | `0.7` | Controls randomness (0.0=Deterministic, 1.0=Creative). |
| `maxTokens` | Integer | `null` | Limits response length. |
| `timeout` | Duration | `30s` | HTTP connection/read timeout. |

**Example with Configuration:**
```java
LlmClient client = LlmClient.builder()
    .provider(LlmClient.ProviderType.OPENAI)
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .model("gpt-4")
    .temperature(0.2)
    .timeout(Duration.ofSeconds(60))
    .build();
```

## Architecture
This project follows strictly modular design principles:

*   **`com.llm.connector.core`**: Contains the `LlmProvider` interface and domain models (`LlmRequest`, `LlmResponse`).
*   **`com.llm.connector.provider`**: Concrete implementations for each service.
*   **`com.llm.connector.client`**: The `LlmClient` acts as the Facade/Context for the Strategy pattern.

To add a new provider, implement `LlmProvider` and register it in the `LlmClient` builder.

## Troubleshooting

### Common Issues
*   **InterruptedException**: If chaining multiple provider calls in a single thread, ensure you handle thread interrupts properly. If one provider fails and interrupts the thread, call `Thread.interrupted()` to clear the status before retrying or calling another provider.
*   **404 Not Found**: Often indicates an invalid `model` name for the given provider region or account tier.
*   **429 Too Many Requests**: You have hit the rate limit or quota for your API key.

### Dependencies
Runtime requirements:
*   `com.fasterxml.jackson.core:jackson-databind`
*   `org.slf4j:slf4j-api`
