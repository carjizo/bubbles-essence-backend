package com.bubblesessence.asistente;

import com.bubblesessence.asistente.dto.PreguntaChatDTO;
import com.bubblesessence.asistente.dto.RespuestaChatDTO;
import com.bubblesessence.common.exception.RateLimitExceededException;
import com.bubblesessence.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Público a propósito: lo usa cualquier visitante de la tienda (invitado
 * o cliente logueado), igual que el catálogo. Ver SecurityConfig: se
 * agrega como permitAll junto a los demás endpoints públicos de catálogo.
 *
 * Como es público y cada pregunta gasta cupo real de la API de Groq, se
 * protege con un límite por IP (AsistenteRateLimiter) — ver ahí el motivo.
 */
@RestController
@RequestMapping("/api/v1/asistente")
@RequiredArgsConstructor
public class AsistenteController {

    private final AsistenteService asistenteService;
    private final AsistenteRateLimiter rateLimiter;

    @PostMapping("/chat")
    public ApiResponse<RespuestaChatDTO> chat(@Valid @RequestBody PreguntaChatDTO pregunta,
                                              HttpServletRequest request) {
        String ip = obtenerIpCliente(request);
        if (!rateLimiter.permitir(ip)) {
            throw new RateLimitExceededException(
                    "Has hecho demasiadas preguntas en poco tiempo. Espera un momento antes de volver a intentar.");
        }
        return ApiResponse.success(asistenteService.responder(pregunta));
    }

    /**
     * Render corre tu app detrás de un proxy, así que
     * request.getRemoteAddr() devolvería la IP del proxy (la misma para
     * TODOS los visitantes), no la del visitante real -> el límite
     * terminaría aplicando a todos juntos en vez de a cada uno por
     * separado. La IP real del visitante viaja en el header
     * X-Forwarded-For, que el proxy sí respeta.
     */
    private String obtenerIpCliente(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}