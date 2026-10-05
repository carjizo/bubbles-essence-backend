package com.bubblesessence.asistente.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GroqChatResponse(List<Choice> choices) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Choice(GroqMessage message) {
    }
}
