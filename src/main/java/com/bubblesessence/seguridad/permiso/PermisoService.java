package com.bubblesessence.seguridad.permiso;

import com.bubblesessence.seguridad.permiso.dto.AsignarPermisoRequestDTO;
import com.bubblesessence.seguridad.permiso.dto.PermisoResponseDTO;

import java.util.List;

public interface PermisoService {

    List<PermisoResponseDTO> listarPorUsuario(Long usuarioId);

    PermisoResponseDTO asignar(Long usuarioId, AsignarPermisoRequestDTO request);

    void quitar(Long usuarioId, Integer accionId);
}
