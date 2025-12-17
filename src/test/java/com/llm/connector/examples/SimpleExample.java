package com.llm.connector.examples;

import com.llm.connector.client.LlmClient;

public class SimpleExample {
    public static void main(String[] args) {
        System.out.println("=== Starting LLM Provider Test ===\n");

        // 1. Ollama (Local)
        System.out.println("--- Testing Ollama ---");
        try {
            LlmClient ollama = LlmClient.builder()
                    .provider(LlmClient.ProviderType.OLLAMA)
                    .model("llama2")
                    .build();
            String response = ollama.generate("Why is the sky blue?");
            System.out.println("Ollama Response: " + response);
        } catch (Exception e) {
            System.out.println("Ollama Status: Failed/Skipped (Ensure Ollama is running). Details: " + e.getMessage());
        }
        System.out.println();

        // 2. Gemini
        System.out.println("--- Testing Gemini ---");
        // Clear any interrupt status from previous failures (e.g. Ollama)
        Thread.interrupted();

        String geminiKey = System.getenv("GEMINI_API_KEY");

        if (geminiKey != null && !geminiKey.isEmpty()) {
            try {
                LlmClient gemini = LlmClient.builder()
                        .provider(LlmClient.ProviderType.GEMINI)
                        .apiKey(geminiKey)
                        .model("gemini-2.5-flash")
                        .build();
                String response = gemini.generate("Explain quantum computing in 1 sentence.");
                System.out.println("Gemini Response: " + response);
            } catch (Exception e) {
                System.out.println("Gemini Status: Failed. Details: " + e.getMessage());
            }
        } else {
            System.out.println("Gemini Status: Skipped (GEMINI_API_KEY not set)");
        }
        System.out.println();

        // 3. OpenAI
        System.out.println("--- Testing OpenAI ---");
        String openAiKey = System.getenv("OPENAI_API_KEY");
        if (openAiKey != null && !openAiKey.isEmpty()) {
            try {
                LlmClient openAi = LlmClient.builder()
                        .provider(LlmClient.ProviderType.OPENAI)
                        .apiKey(openAiKey)
                        .model("gpt-3.5-turbo")
                        .build();
                String response = openAi.generate("Tell me a java joke.");
                System.out.println("OpenAI Response: " + response);
            } catch (Exception e) {
                System.out.println("OpenAI Status: Failed. Details: " + e.getMessage());
            }
        } else {
            System.out.println("OpenAI Status: Skipped (OPENAI_API_KEY not set)");
        }
        System.out.println();

        // 4. Anthropic
        System.out.println("--- Testing Anthropic ---");
        String anthropicKey = System.getenv("ANTHROPIC_API_KEY");
        if (anthropicKey != null && !anthropicKey.isEmpty()) {
            try {
                LlmClient anthropic = LlmClient.builder()
                        .provider(LlmClient.ProviderType.ANTHROPIC)
                        .apiKey(anthropicKey)
                        .model("claude-3-sonnet-20240229")
                        .build();
                String response = anthropic.generate("Haiku about coding.");
                System.out.println("Anthropic Response: " + response);
            } catch (Exception e) {
                System.out.println("Anthropic Status: Failed. Details: " + e.getMessage());
            }
        } else {
            System.out.println("Anthropic Status: Skipped (ANTHROPIC_API_KEY not set)");
        }

        System.out.println("\n=== Test Complete ===");
    }
}
