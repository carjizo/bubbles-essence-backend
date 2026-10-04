package com.bubblesessence.seguridad.modulo;

import com.bubblesessence.seguridad.modulo.dto.ModuloResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ModuloMapper {

    public ModuloResponseDTO toResponseDTO(Modulo modulo) {
        return ModuloResponseDTO.builder()
                .id(modulo.getId())
                .codigo(modulo.getCodigo())
                .nombre(modulo.getNombre())
                .descripcion(modulo.getDescripcion())
                .orden(modulo.getOrden())
                .activo(modulo.getActivo())
                .build();
    }
}
