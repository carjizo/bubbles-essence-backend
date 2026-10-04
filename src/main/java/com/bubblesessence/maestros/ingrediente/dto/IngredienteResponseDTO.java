package com.bubblesessence.maestros.ingrediente.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredienteResponseDTO {

    private Integer id;
    private String nombre;
    private String descripcion;
    private Boolean activo;
}
