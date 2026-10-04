package com.bubblesessence.seguridad.auth;

import com.bubblesessence.maestros.cliente.Cliente;
import com.bubblesessence.seguridad.usuario.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Function;

/**
 * Genera y valida los JWT de sesión para:
 * - Personal interno (usuarios de grp_seg.tbl_seg_usuario)
 * - Clientes externos (usuarios de grp_mge.tbl_mge_cliente)
 */
@Component
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMinutos;

    public JwtService(
            @Value("${app.jwt.secret}") String secreto,
            @Value("${app.jwt.expiration-minutes}") long expiracionMinutos) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMinutos = expiracionMinutos;
    }

    public String generarToken(Usuario usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getUsuario())
                .claim("id", usuario.getId())
                .claim("tipo", "USUARIO")
                .claim("rol", usuario.getRol().name())
                .claim("nombreCompleto", usuario.getNombreCompleto())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(expiracionMinutos, ChronoUnit.MINUTES)))
                .signWith(clave)
                .compact();
    }

    public String generarTokenCliente(Cliente cliente) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(cliente.getDocumento())
                .claim("id", cliente.getId())
                .claim("tipo", "CLIENTE")
                .claim("documento", cliente.getDocumento())
                .claim("nombreCompleto", cliente.getNombreCompleto())
                .claim("correo", cliente.getCorreo())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(expiracionMinutos, ChronoUnit.MINUTES)))
                .signWith(clave)
                .compact();
    }

    public String extraerUsuario(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    public Long extraerIdUsuario(String token) {
        return extraerTodosLosClaims(token).get("id", Long.class);
    }

    public String extraerRol(String token) {
        return extraerTodosLosClaims(token).get("rol", String.class);
    }

    public String extraerTipo(String token) {
        return extraerTodosLosClaims(token).get("tipo", String.class);
    }

    public boolean esValido(String token, String usuarioEsperado) {
        String usuarioDelToken = extraerUsuario(token);
        return usuarioDelToken.equals(usuarioEsperado) && !haExpirado(token);
    }

    private boolean haExpirado(String token) {
        return extraerClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extraerClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extraerTodosLosClaims(token));
    }

    private Claims extraerTodosLosClaims(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
