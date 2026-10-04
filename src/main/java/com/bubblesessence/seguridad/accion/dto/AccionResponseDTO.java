package com.bubblesessence.seguridad.accion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccionResponseDTO {

    private Integer id;
    private Integer moduloId;
    private String moduloCodigo;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Boolean activo;
}
