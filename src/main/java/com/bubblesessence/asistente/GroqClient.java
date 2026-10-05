package com.bubblesessence.asistente;

import com.bubblesessence.asistente.dto.GroqChatRequest;
import com.bubblesessence.asistente.dto.GroqChatResponse;
import com.bubblesessence.asistente.dto.GroqMessage;
import com.bubblesessence.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroqClient {

    private final RestClient groqRestClient;

    @Value("${app.groq.model}")
    private String model;

    /**
     * Manda system prompt + pregunta del usuario a Groq y devuelve solo el
     * texto de la respuesta. Cualquier falla (sin API key, rate limit de
     * Groq, timeout) se traduce a BusinessException para que el
     * GlobalExceptionHandler la muestre como un 409 legible, en vez de un
     * 500 crudo.
     */
    public String completar(String systemPrompt, String preguntaUsuario) {
        GroqChatRequest request = new GroqChatRequest(
                model,
                List.of(
                        new GroqMessage("system", systemPrompt),
                        new GroqMessage("user", preguntaUsuario)
                ),
                0.4,
                400
        );

        try {
            GroqChatResponse response = groqRestClient.post()
                    .uri("/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(GroqChatResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new BusinessException("El asistente no devolvió una respuesta. Intenta de nuevo.");
            }
            return response.choices().get(0).message().content();

        } catch (RestClientException e) {
            // Logueamos el detalle real (API key inválida, 429 de Groq,
            // timeout, modelo inexistente, etc.) para verlo en los logs de
            // Render; al cliente solo le llega el mensaje genérico de abajo.
            log.error("Fallo al llamar a Groq: {}", e.getMessage(), e);
            throw new BusinessException("El asistente no está disponible en este momento. Intenta más tarde.");
        }
    }
}