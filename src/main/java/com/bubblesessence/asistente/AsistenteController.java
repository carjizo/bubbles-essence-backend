package com.bubblesessence.asistente;

import com.bubblesessence.asistente.dto.PreguntaChatDTO;
import com.bubblesessence.asistente.dto.RespuestaChatDTO;
import com.bubblesessence.common.response.ApiResponse;
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
 */
@RestController
@RequestMapping("/api/v1/asistente")
@RequiredArgsConstructor
public class AsistenteController {

    private final AsistenteService asistenteService;

    @PostMapping("/chat")
    public ApiResponse<RespuestaChatDTO> chat(@Valid @RequestBody PreguntaChatDTO pregunta) {
        return ApiResponse.success(asistenteService.responder(pregunta));
    }
}
