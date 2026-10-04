package com.bubblesessence.ventas.productoingrediente.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoIngredienteResponseDTO {

    private Long id;
    private Integer productoId;
    private String productoNombre;
    private Integer ingredienteId;
    private String ingredienteNombre;
    private String cantidadReferencial;
}
