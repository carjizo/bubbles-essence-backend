package com.bubblesessence.seguridad.usuario;

import com.bubblesessence.common.response.ApiResponse;
import com.bubblesessence.seguridad.usuario.dto.UsuarioRequestDTO;
import com.bubblesessence.seguridad.usuario.dto.UsuarioResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gestión del personal interno. Solo ADMIN puede crear, editar, desactivar
 * o listar usuarios: los demás roles ven/administran sus propios módulos
 * (pedidos, catálogo, entregas) pero no la lista de cuentas del sistema.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ApiResponse<List<UsuarioResponseDTO>> listar(
            @RequestParam(required = false) Boolean activo) {
        return ApiResponse.success(usuarioService.listar(activo));
    }

    @GetMapping("/{id}")
    public ApiResponse<UsuarioResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ApiResponse.success(usuarioService.obtenerPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UsuarioResponseDTO> crear(@Valid @RequestBody UsuarioRequestDTO request) {
        return ApiResponse.success("Usuario creado correctamente", usuarioService.crear(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<UsuarioResponseDTO> actualizar(
            @PathVariable Long id, @Valid @RequestBody UsuarioRequestDTO request) {
        return ApiResponse.success("Usuario actualizado correctamente", usuarioService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> desactivar(@PathVariable Long id) {
        usuarioService.desactivar(id);
        return ApiResponse.success("Usuario desactivado correctamente", null);
    }

    @PatchMapping("/{id}/activar")
    public ApiResponse<Void> activar(@PathVariable Long id) {
        usuarioService.activar(id);
        return ApiResponse.success("Usuario activado correctamente", null);
    }
}
