package com.bubblesessence.seguridad.usuario;

import com.bubblesessence.seguridad.usuario.dto.UsuarioRequestDTO;
import com.bubblesessence.seguridad.usuario.dto.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioService {

    List<UsuarioResponseDTO> listar(Boolean activo);

    UsuarioResponseDTO obtenerPorId(Long id);

    UsuarioResponseDTO crear(UsuarioRequestDTO request);

    UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO request);

    void desactivar(Long id);

    void activar(Long id);
}
