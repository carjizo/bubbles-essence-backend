package com.bubblesessence.maestros.cliente.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponseDTO {

    private Long id;
    private String nombreCompleto;
    private String telefono;
    private String correo;
    private String direccion;
    private String instagram;
    private LocalDateTime fechaRegistro;
}
