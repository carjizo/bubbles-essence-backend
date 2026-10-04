package com.bubblesessence.seguridad.auth.dto;

import com.bubblesessence.seguridad.usuario.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    private String token;
    private String tipoToken; // "Bearer"
    private Long id;
    private String nombreCompleto;
    private String usuario;
    private RolUsuario rol;
}
