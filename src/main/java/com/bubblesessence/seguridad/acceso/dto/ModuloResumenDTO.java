package com.bubblesessence.seguridad.acceso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuloResumenDTO {
    private Integer id;
    private String codigo;
    private String nombre;
    private Integer orden;
    private java.util.List<String> acciones;
}
