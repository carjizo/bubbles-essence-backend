package com.bubblesessence.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Solo expone el encoder de contraseñas. No se agrega spring-boot-starter-security
 * a propósito: eso activaría un filtro de autenticación por defecto (login básico)
 * que bloquearía las pruebas en Postman antes de tener un módulo de auth real.
 */
@Configuration
public class SecurityBeansConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
