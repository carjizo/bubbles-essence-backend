package com.bubblesessence.maestros.ingrediente;

import com.bubblesessence.maestros.ingrediente.dto.IngredienteResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class IngredienteMapper {

    public IngredienteResponseDTO toResponseDTO(Ingrediente ingrediente) {
        return IngredienteResponseDTO.builder()
                .id(ingrediente.getId())
                .nombre(ingrediente.getNombre())
                .descripcion(ingrediente.getDescripcion())
                .activo(ingrediente.getActivo())
                .build();
    }
}
