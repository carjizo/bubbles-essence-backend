package com.bubblesessence.seguridad.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Lee el header "Authorization: Bearer <token>" en cada request. Si el token
 * es válido, autentica al usuario en el SecurityContext para esa request.
 * 
 * Maneja dos tipos de tokens:
 * - USUARIO: staff (usuario/clave) → carga de BD via UsuarioDetailsService
 * - CLIENTE: cliente externo (documento/clave) → crea principal desde JWT
 * 
 * Si no hay header (caso típico de un invitado pegándole a un endpoint
 * público), simplemente deja pasar sin autenticar: SecurityConfig decide
 * si ese endpoint necesita autenticación o no.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(PREFIJO_BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(PREFIJO_BEARER.length());
        try {
            String tipo = jwtService.extraerTipo(token);
            String usuario = jwtService.extraerUsuario(token);
            
            log.info("JWT Filter - tipo: {}, usuario: {}", tipo, usuario);
            
            if (usuario != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Caso 1: CLIENTE (documento/clave externo)
                if ("CLIENTE".equals(tipo)) {
                    Long clienteId = jwtService.extraerIdUsuario(token);
                    log.info("JWT Filter - Cliente detectado, clienteId: {}", clienteId);
                    if (clienteId != null && jwtService.esValido(token, usuario)) {
                        log.info("JWT Filter - Token válido para cliente: {}", usuario);
                        // Crear un UserDetails simple para el cliente desde el JWT
                        var clientePrincipal = new ClientePrincipal(clienteId, usuario);
                        var authToken = new UsernamePasswordAuthenticationToken(
                                clientePrincipal, null, clientePrincipal.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.info("JWT Filter - Cliente autenticado: {} con autoridades: {}", 
                            usuario, clientePrincipal.getAuthorities());
                    } else {
                        log.warn("JWT Filter - Token inválido o clienteId null para: {}", usuario);
                    }
                }
                // Caso 2: USUARIO (staff)
                else {
                    UserDetails userDetails = usuarioDetailsService.loadUserByUsername(usuario);
                    if (jwtService.esValido(token, userDetails.getUsername())) {
                        var authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        } catch (Exception ex) {
            // Token inválido/expirado/manipulado: no se autentica.
            // El endpoint decide si eso es aceptable (público) o no (401/403).
            log.error("JWT Filter - Error procesando token: {}", ex.getMessage(), ex);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
