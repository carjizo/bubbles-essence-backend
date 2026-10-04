package com.bubblesessence.common.config;

import com.bubblesessence.seguridad.auth.ApiKeyAuthenticationFilter;
import com.bubblesessence.seguridad.auth.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Reglas de autorización de toda la API. Dos mundos conviven acá:
 *
 * 1) PERSONAL INTERNO (admin/operador/vendedor/repartidor): inicia sesión en
 *    /api/v1/auth/login, recibe un JWT, y lo manda en cada request como
 *    "Authorization: Bearer <token>". El rol autorizado por endpoint se
 *    afina con @PreAuthorize en cada controller (ver UsuarioController,
 *    PedidoController, etc), no acá; acá solo se decide qué es público.
 *
 * 2) INVITADOS (clientes sin cuenta): nunca inician sesión. Solo pueden
 *    tocar las rutas explícitamente permitAll de abajo: crear un pedido,
 *    hacer seguimiento de SU pedido (con código + teléfono) y navegar el
 *    catálogo. Todo lo demás exige un JWT válido.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    // Leer la variable CORS_ALLOWED_ORIGINS del entorno del application.yml
    @Value("${cors.allowed-origins}")
    private String corsAllowedOrigins;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // --- Documentación ---
                        .requestMatchers(
                                "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**"
                        ).permitAll()

                        // --- Login del personal interno ---
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // --- Catálogo: un invitado necesita poder ver productos e
                        //     ingredientes para armar su pedido, sin loguearse ---
                        .requestMatchers(HttpMethod.GET, "/api/v1/productos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/ingredientes/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/producto-ingredientes/**").permitAll()

                        // --- Pedidos de invitado: crear y hacer seguimiento sin
                        //     cuenta. El resto de /api/v1/pedidos/** (listar todo,
                        //     cambiar estado, cancelar por staff) exige login y se
                        //     restringe por rol con @PreAuthorize en el controller. ---
                        .requestMatchers(HttpMethod.POST, "/api/v1/pedidos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/pedidos/seguimiento").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/pedidos/seguimiento/cancelar").permitAll()

                        // --- Todo lo demás requiere estar logueado como personal interno
                        //     (JWT) o traer un X-API-KEY válido (ver ApiKeyAuthenticationFilter,
                        //     pensado para llamadas máquina-a-máquina o como puente a un IdP
                        //     externo tipo Cognito más adelante) ---
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(apiKeyAuthenticationFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // el "*" por la URL exacta (ej. https://bubblesessence.pe).
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(corsAllowedOrigins));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
