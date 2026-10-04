package com.bubblesessence.seguridad.usuario;

import com.bubblesessence.seguridad.usuario.dto.UsuarioResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nombreCompleto(usuario.getNombreCompleto())
                .usuario(usuario.getUsuario())
                .rol(usuario.getRol())
                .telefono(usuario.getTelefono())
                .correo(usuario.getCorreo())
                .activo(usuario.getActivo())
                .fechaCreacion(usuario.getFechaCreacion())
                .usuarioCreador(usuario.getUsuarioCreador())
                .build();
    }
}
