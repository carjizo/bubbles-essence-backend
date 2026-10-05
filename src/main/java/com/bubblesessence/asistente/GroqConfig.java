package com.bubblesessence.asistente;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GroqConfig {

    @Value("${app.groq.base-url}")
    private String baseUrl;

    @Value("${app.groq.api-key}")
    private String apiKey;

    @Bean
    public RestClient groqRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}