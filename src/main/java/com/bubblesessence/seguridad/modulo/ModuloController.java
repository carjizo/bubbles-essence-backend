package com.bubblesessence.seguridad.modulo;

import com.bubblesessence.common.response.ApiResponse;
import com.bubblesessence.seguridad.modulo.dto.ModuloRequestDTO;
import com.bubblesessence.seguridad.modulo.dto.ModuloResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Catálogo de módulos del sistema (mantenimiento). Es el "maestro" que
 * alimenta tanto el menú del front como el catálogo de acciones
 * (ver AccionController), así que solo ADMIN lo toca.
 */
@RestController
@RequestMapping("/api/v1/modulos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ModuloController {

    private final ModuloService moduloService;

    @GetMapping
    public ApiResponse<List<ModuloResponseDTO>> listar() {
        return ApiResponse.success(moduloService.listar());
    }

    @GetMapping("/{id}")
    public ApiResponse<ModuloResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ApiResponse.success(moduloService.obtenerPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ModuloResponseDTO> crear(@Valid @RequestBody ModuloRequestDTO request) {
        return ApiResponse.success("Módulo creado correctamente", moduloService.crear(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ModuloResponseDTO> actualizar(
            @PathVariable Integer id, @Valid @RequestBody ModuloRequestDTO request) {
        return ApiResponse.success("Módulo actualizado correctamente", moduloService.actualizar(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    public ApiResponse<Void> desactivar(@PathVariable Integer id) {
        moduloService.desactivar(id);
        return ApiResponse.success("Módulo desactivado correctamente", null);
    }

    @PatchMapping("/{id}/activar")
    public ApiResponse<Void> activar(@PathVariable Integer id) {
        moduloService.activar(id);
        return ApiResponse.success("Módulo activado correctamente", null);
    }
}
