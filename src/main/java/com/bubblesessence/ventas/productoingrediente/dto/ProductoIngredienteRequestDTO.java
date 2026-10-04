package com.bubblesessence.ventas.productoingrediente.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductoIngredienteRequestDTO {

    @NotNull(message = "El producto es obligatorio")
    private Integer productoId;

    @NotNull(message = "El ingrediente es obligatorio")
    private Integer ingredienteId;

    @Size(max = 30, message = "La cantidad referencial no puede superar los 30 caracteres")
    private String cantidadReferencial;
}
