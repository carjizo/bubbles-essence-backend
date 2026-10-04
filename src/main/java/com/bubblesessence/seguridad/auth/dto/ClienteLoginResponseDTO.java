package com.bubblesessence.seguridad.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteLoginResponseDTO {
    
    private String token;
    private String tipoToken;
    private Long id;
    private String nombreCompleto;
    private String documento;
    private String correo;
    private String telefono;
}
