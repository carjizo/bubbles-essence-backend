package com.bubblesessence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableScheduling: necesario para que corra la limpieza periódica de
// AsistenteRateLimiter (y cualquier otra tarea @Scheduled que se agregue
// a futuro).
@EnableScheduling
@SpringBootApplication
public class BubblesEssenceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BubblesEssenceApplication.class, args);
    }
}