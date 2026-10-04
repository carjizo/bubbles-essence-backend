package com.bubblesessence.seguridad.grupo;

import com.bubblesessence.common.response.ApiResponse;
import com.bubblesessence.seguridad.accion.dto.AccionResponseDTO;
import com.bubblesessence.seguridad.grupo.dto.GrupoRequestDTO;
import com.bubblesessence.seguridad.grupo.dto.GrupoResponseDTO;
import com.bubblesessence.seguridad.usuario.dto.UsuarioResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Mantenimiento de grupos y sus asignaciones. Todo el módulo de "quién
 * puede hacer qué" (activar usuarios, dar permisos a módulos/acciones,
 * armar grupos) vive acá. Solo ADMIN administra esto; para CONSULTAR los
 * accesos ya resueltos de un usuario (lo que necesita el front) ver
 * AccesoController, que sí es de lectura para cualquier usuario logueado.
 */
@RestController
@RequestMapping("/api/v1/grupos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class GrupoController {

    private final GrupoService grupoService;

    // ---------------------------------------------------------------- CRUD

    @GetMapping
    public ApiResponse<List<GrupoResponseDTO>> listar() {
        return ApiResponse.success(grupoService.listar());
    }

    @GetMapping("/{id}")
    public ApiResponse<GrupoResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ApiResponse.success(grupoService.obtenerPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GrupoResponseDTO> crear(@Valid @RequestBody GrupoRequestDTO request) {
        return ApiResponse.success("Grupo creado correctamente", grupoService.crear(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<GrupoResponseDTO> actualizar(
            @PathVariable Integer id, @Valid @RequestBody GrupoRequestDTO request) {
        return ApiResponse.success("Grupo actualizado correctamente", grupoService.actualizar(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    public ApiResponse<Void> desactivar(@PathVariable Integer id) {
        grupoService.desactivar(id);
        return ApiResponse.success("Grupo desactivado correctamente", null);
    }

    @PatchMapping("/{id}/activar")
    public ApiResponse<Void> activar(@PathVariable Integer id) {
        grupoService.activar(id);
        return ApiResponse.success("Grupo activado correctamente", null);
    }

    // ------------------------------------------------------ acciones del grupo

    @GetMapping("/{id}/acciones")
    public ApiResponse<List<AccionResponseDTO>> listarAcciones(@PathVariable Integer id) {
        return ApiResponse.success(grupoService.listarAcciones(id));
    }

    @PostMapping("/{id}/acciones/{accionId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> asignarAccion(@PathVariable Integer id, @PathVariable Integer accionId) {
        grupoService.asignarAccion(id, accionId);
        return ApiResponse.success("Acción asignada al grupo correctamente", null);
    }

    @DeleteMapping("/{id}/acciones/{accionId}")
    public ApiResponse<Void> quitarAccion(@PathVariable Integer id, @PathVariable Integer accionId) {
        grupoService.quitarAccion(id, accionId);
        return ApiResponse.success("Acción quitada del grupo correctamente", null);
    }

    // ------------------------------------------------------ usuarios del grupo

    @GetMapping("/{id}/usuarios")
    public ApiResponse<List<UsuarioResponseDTO>> listarUsuarios(@PathVariable Integer id) {
        return ApiResponse.success(grupoService.listarUsuarios(id));
    }

    @PostMapping("/{id}/usuarios/{usuarioId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> asignarUsuario(@PathVariable Integer id, @PathVariable Long usuarioId) {
        grupoService.asignarUsuario(id, usuarioId);
        return ApiResponse.success("Usuario asignado al grupo correctamente", null);
    }

    @DeleteMapping("/{id}/usuarios/{usuarioId}")
    public ApiResponse<Void> quitarUsuario(@PathVariable Integer id, @PathVariable Long usuarioId) {
        grupoService.quitarUsuario(id, usuarioId);
        return ApiResponse.success("Usuario quitado del grupo correctamente", null);
    }
}
