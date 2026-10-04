package com.bubblesessence.seguridad.usuario.dto;

import com.bubblesessence.seguridad.usuario.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {

    private Long id;
    private String nombreCompleto;
    private String usuario;
    private RolUsuario rol;
    private String telefono;
    private String correo;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private String usuarioCreador;
}
