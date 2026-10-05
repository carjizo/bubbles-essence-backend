package com.bubblesessence.asistente.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request al endpoint /chat/completions de Groq. El formato es idéntico
 * al de OpenAI (Groq es "drop-in compatible"), así que si mañana cambias
 * de proveedor compatible con OpenAI, solo cambias GroqConfig, no esto.
 */
public record GroqChatRequest(
        String model,
        List<GroqMessage> messages,
        double temperature,
        @JsonProperty("max_tokens") int maxTokens
) {
}