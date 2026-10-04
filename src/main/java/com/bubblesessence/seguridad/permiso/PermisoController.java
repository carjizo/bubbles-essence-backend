package com.bubblesessence.seguridad.permiso;

import com.bubblesessence.common.response.ApiResponse;
import com.bubblesessence.seguridad.permiso.dto.AsignarPermisoRequestDTO;
import com.bubblesessence.seguridad.permiso.dto.PermisoResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Permisos PUNTUALES por usuario (excepción a lo que le da su grupo).
 * Ej. "Pedro es de VENTAS pero además dale acceso a btn-editar-producto",
 * o "a Maria quítale btn-cancelar-pedido aunque su grupo lo tenga".
 * Uso poco frecuente, pensado para casos borde — lo normal es manejarlo
 * por grupo (ver GrupoController).
 */
@RestController
@RequestMapping("/api/v1/usuarios/{usuarioId}/permisos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PermisoController {

    private final PermisoService permisoService;

    @GetMapping
    public ApiResponse<List<PermisoResponseDTO>> listar(@PathVariable Long usuarioId) {
        return ApiResponse.success(permisoService.listarPorUsuario(usuarioId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PermisoResponseDTO> asignar(
            @PathVariable Long usuarioId, @Valid @RequestBody AsignarPermisoRequestDTO request) {
        return ApiResponse.success("Permiso asignado correctamente", permisoService.asignar(usuarioId, request));
    }

    @DeleteMapping("/{accionId}")
    public ApiResponse<Void> quitar(@PathVariable Long usuarioId, @PathVariable Integer accionId) {
        permisoService.quitar(usuarioId, accionId);
        return ApiResponse.success("Permiso quitado correctamente", null);
    }
}
