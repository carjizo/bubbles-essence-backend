package com.bubblesessence.seguridad.grupo;

import com.bubblesessence.seguridad.accion.dto.AccionResponseDTO;
import com.bubblesessence.seguridad.grupo.dto.GrupoRequestDTO;
import com.bubblesessence.seguridad.grupo.dto.GrupoResponseDTO;
import com.bubblesessence.seguridad.usuario.dto.UsuarioResponseDTO;

import java.util.List;

public interface GrupoService {

    List<GrupoResponseDTO> listar();

    GrupoResponseDTO obtenerPorId(Integer id);

    GrupoResponseDTO crear(GrupoRequestDTO request);

    GrupoResponseDTO actualizar(Integer id, GrupoRequestDTO request);

    void desactivar(Integer id);

    void activar(Integer id);

    // ---- acciones del grupo ----

    List<AccionResponseDTO> listarAcciones(Integer grupoId);

    void asignarAccion(Integer grupoId, Integer accionId);

    void quitarAccion(Integer grupoId, Integer accionId);

    // ---- usuarios del grupo ----

    List<UsuarioResponseDTO> listarUsuarios(Integer grupoId);

    void asignarUsuario(Integer grupoId, Long usuarioId);

    void quitarUsuario(Integer grupoId, Long usuarioId);
}
