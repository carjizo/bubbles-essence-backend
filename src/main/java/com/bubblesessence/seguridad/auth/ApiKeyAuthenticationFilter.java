package com.bubblesessence.seguridad.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Autenticación alternativa a JWT, vía header "X-API-KEY", pensada para
 * llamadas máquina-a-máquina (o como puente mientras se integra un IdP
 * externo tipo AWS Cognito).
 *
 * IMPORTANTE — por qué existe este filtro y cómo migrar a Cognito después:
 * la autenticación (¿quién eres, es válido tu token/clave?) está separada
 * a propósito de la autorización (¿qué puedes hacer?). Este filtro SOLO
 * prueba identidad; nunca decide permisos. Los permisos siempre se resuelven
 * después, consultando grp_seg.tbl_seg_grupo / tbl_seg_accion / etc (ver
 * AccesoService), sin importar CÓMO se autenticó la request.
 *
 * Cuando llegue Cognito: se agrega un filtro nuevo (ej.
 * CognitoJwtAuthenticationFilter) que valida el JWT de Cognito contra su
 * JWKS público y, si es válido, busca al Usuario correspondiente en
 * tbl_seg_usuario (por correo o por un cognitoSub que se agregue a la
 * tabla) y arma el mismo UsuarioPrincipal que ya arma JwtAuthenticationFilter.
 * Ni AccesoService, ni GrupoController, ni ningún @PreAuthorize del proyecto
 * necesitan cambiar: todos dependen del Authentication ya armado, no de
 * cómo se armó.
 */
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-API-KEY";

    /** Vacío por defecto = deshabilitado. Se activa configurando la env var API_KEY. */
    private final String apiKeyConfigurada;

    public ApiKeyAuthenticationFilter(@Value("${app.security.api-key:}") String apiKeyConfigurada) {
        this.apiKeyConfigurada = apiKeyConfigurada;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        boolean apiKeyHabilitada = apiKeyConfigurada != null && !apiKeyConfigurada.isBlank();
        boolean yaAutenticado = SecurityContextHolder.getContext().getAuthentication() != null;

        if (apiKeyHabilitada && !yaAutenticado) {
            String apiKeyRecibida = request.getHeader(HEADER);
            if (apiKeyConfigurada.equals(apiKeyRecibida)) {
                var authToken = new UsernamePasswordAuthenticationToken(
                        "api-client", null, List.of(new SimpleGrantedAuthority("ROLE_SYSTEM")));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
