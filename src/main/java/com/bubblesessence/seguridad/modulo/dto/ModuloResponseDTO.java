package com.bubblesessence.seguridad.modulo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuloResponseDTO {

    private Integer id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Integer orden;
    private Boolean activo;
}
