# AIConnector - Java LLM SDK

A lightweight, framework-agnostic Java SDK designed to connect seamlessly to major LLM providers (Google Gemini, OpenAI, Anthropic) and local models (Ollama).

This SDK is built with **Java 17** and aims to provide a unified interface for text generation, keeping your business logic decoupled from specific provider implementations. It carries minimal dependencies (Jackson for JSON, SLF4J for logging) and contains **no Spring/Spring Boot dependencies** in the core, making it suitable for any Java environment.

----

## Table of Contents
- [Features](#features)
- [Supported Providers](#supported-providers--defaults)
- [Getting Started](#getting-started)
  - [Installation](#installation)
  - [UsingExistingJar](#using-the-existing-jar-file)
  - [Quick Start](#quick-start)
- [Usage Examples](#usage-examples)
- [Configuration](#configuration)
- [Architecture](#architecture)
- [Troubleshooting](#troubleshooting)
- [Dependencies](#dependencies)

---

## Features
*   **Unified API**: Switch between Gemini, OpenAI, Claude, and Ollama with a single configuration change.
*   **Framework Agnostic**: Pure Java implementation usable in CLI tools, utility libraries, or Spring Boot apps.
*   **Zero-Overhead**: No bloat. Uses native `java.net.http.HttpClient` and Jackson.
*   **Extensible**: Strategy pattern design allows easy addition of new providers.

## Supported Providers & Defaults
| Provider | Key Type | Default Model |
| :--- | :--- | :--- |
| **Gemini** | `GEMINI_API_KEY` | `gemini-2.0-flash` |
| **OpenAI** | `OPENAI_API_KEY` | `gpt-3.5-turbo` |
| **Anthropic** | `ANTHROPIC_API_KEY` | `claude-3-sonnet-20240229` |
| **Ollama** | *None* | `llama2` |

---

## Getting Started

### Installation
Ensure you have **Java 17+** and **Maven** installed. Build the project locally:
```bash
mvn clean package

This will create a jar file in the target directory.

Use the jar file in your project.
```

### Using the existing jar file
```bash
If you do not want to make any code changes and want to use the existing JAR:

You can copy the llm-java-sdk-1.0.0-SNAPSHOT.jar from the /target folder

Use it in your Java project.
```

### Quick Start
The entry point is the `LlmClient`, built using a fluent builder pattern.

```java
import com.llm.connector.client.LlmClient;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize Client (e.g., for Gemini)
        LlmClient client = LlmClient.builder()
            .provider(LlmClient.ProviderType.GEMINI)
            .apiKey(System.getenv("GEMINI_API_KEY")) 
            .build();

        // 2. Generate Text
        String response = client.generate("Explain quantum computing in 5 words.");
        System.out.println(response);
    }
}
```

---

## Usage Examples

Below is a consolidated example demonstrating how to invoke different providers using the unified client.

```java
import com.llm.connector.client.LlmClient;

public class Examples {
    public static void main(String[] args) {
        
        // 1. Local LLM via Ollama
        LlmClient ollama = LlmClient.builder()
                .provider(LlmClient.ProviderType.OLLAMA)
                .model("llama2")
                .build();
        System.out.println("Ollama: " + ollama.generate("Hi!"));

        // 2. Google Gemini
        LlmClient gemini = LlmClient.builder()
                .provider(LlmClient.ProviderType.GEMINI)
                .apiKey(System.getenv("GEMINI_API_KEY"))
                .model("gemini-2.5-flash")
                .build();
        System.out.println("Gemini: " + gemini.generate("Hi!"));

        // 3. OpenAI GPT
        LlmClient openAi = LlmClient.builder()
                .provider(LlmClient.ProviderType.OPENAI)
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .build();
        System.out.println("OpenAI: " + openAi.generate("Hi!"));
    }
}
```

---

## Configuration

### 1. Credentials
We recommend using Environment Variables to manage credentials securely:
*   `GEMINI_API_KEY`
*   `OPENAI_API_KEY`
*   `ANTHROPIC_API_KEY`

### 2. Client Options
Fine-tune client behavior via the builder:

| Option | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `apiKey` | String | `null` | Provider API Key. |
| `model` | String | *Provider Default* | Override default model (e.g., `gpt-4`). |
| `temperature` | Double | `0.7` | Randomness (0.0=Deterministic, 1.0=Creative). |
| `maxTokens` | Integer | `null` | Limits response length. |
| `timeout` | Duration | `30s` | HTTP connection/read timeout. |

**Advanced Config Example:**
```java
LlmClient client = LlmClient.builder()
    .provider(LlmClient.ProviderType.OPENAI)
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .model("gpt-4")
    .temperature(0.2)
    .timeout(Duration.ofSeconds(60))
    .build();
```

---

## Architecture
The SDK follows a clean, modular design:
- **`com.llm.connector.core`**: Core interfaces (`LlmProvider`) and models (`LlmRequest`).
- **`com.llm.connector.provider`**: Concrete adapter implementations for Gemini/OpenAI/etc.
- **`com.llm.connector.client`**: The public entry point (`LlmClient`) implementing the Strategy pattern.

To add a new provider, simply implement `LlmProvider` and register it in the `LlmClient` builder.

---

## Troubleshooting
- **InterruptedException**: If chaining calls, use `Thread.interrupted()` to clear status if a previous call failed.
- **401/403 Errors**: Check if your API key is correct and has sufficient quota.
- **404 Errors**: Ensure the model name (e.g., `gpt-4`) is correct and available in your region.

## Dependencies
- `com.fasterxml.jackson.core:jackson-databind`
- `org.slf4j:slf4j-api`
