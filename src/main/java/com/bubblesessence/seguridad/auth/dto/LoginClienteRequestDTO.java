package com.bubblesessence.seguridad.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginClienteRequestDTO {
    
    @NotBlank(message = "El documento es requerido")
    private String documento;
    
    @NotBlank(message = "La contraseña es requerida")
    private String clave;
}
