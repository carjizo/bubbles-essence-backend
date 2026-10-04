package com.bubblesessence.seguridad.usuario.dto;

import com.bubblesessence.seguridad.usuario.RolUsuario;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestDTO {

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 100)
    private String nombreCompleto;

    @NotBlank(message = "El usuario (login) es obligatorio")
    @Size(max = 30)
    private String usuario;

    /**
     * Contraseña en texto plano SOLO en el request. El servicio la
     * hashea con BCrypt antes de persistir; nunca se guarda ni se
     * devuelve tal cual. Es obligatoria al crear; en una actualización
     * se puede omitir y se conserva la clave anterior (el servicio
     * valida esto explícitamente, ver UsuarioServiceImpl).
     */
    @Size(min = 6, max = 100, message = "La contraseña debe tener al menos 6 caracteres")
    private String clave;

    @NotNull(message = "El rol es obligatorio")
    private RolUsuario rol;

    @Size(max = 20)
    private String telefono;

    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 100)
    private String correo;

    private Boolean activo;
}
