package com.bubblesessence.seguridad.acceso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrupoResumenDTO {
    private Integer id;
    private String codigo;
    private String nombre;
}
