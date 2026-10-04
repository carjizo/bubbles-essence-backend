package com.bubblesessence.seguridad.accion;

import com.bubblesessence.common.response.ApiResponse;
import com.bubblesessence.seguridad.accion.dto.AccionRequestDTO;
import com.bubblesessence.seguridad.accion.dto.AccionResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Catálogo de acciones (mantenimiento). El campo "codigo" (ej.
 * 'btn-editar-pedido') es el que el front usa tal cual, así que solo ADMIN
 * puede crear/editar acciones — cambiarlo a la ligera rompe el front.
 */
@RestController
@RequestMapping("/api/v1/acciones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AccionController {

    private final AccionService accionService;

    @GetMapping
    public ApiResponse<List<AccionResponseDTO>> listar(@RequestParam(required = false) Integer moduloId) {
        return ApiResponse.success(accionService.listar(moduloId));
    }

    @GetMapping("/{id}")
    public ApiResponse<AccionResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ApiResponse.success(accionService.obtenerPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AccionResponseDTO> crear(@Valid @RequestBody AccionRequestDTO request) {
        return ApiResponse.success("Acción creada correctamente", accionService.crear(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AccionResponseDTO> actualizar(
            @PathVariable Integer id, @Valid @RequestBody AccionRequestDTO request) {
        return ApiResponse.success("Acción actualizada correctamente", accionService.actualizar(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    public ApiResponse<Void> desactivar(@PathVariable Integer id) {
        accionService.desactivar(id);
        return ApiResponse.success("Acción desactivada correctamente", null);
    }

    @PatchMapping("/{id}/activar")
    public ApiResponse<Void> activar(@PathVariable Integer id) {
        accionService.activar(id);
        return ApiResponse.success("Acción activada correctamente", null);
    }
}
