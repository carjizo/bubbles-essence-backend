package com.bubblesessence.seguridad.grupo;

import com.bubblesessence.seguridad.grupo.dto.GrupoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class GrupoMapper {

    public GrupoResponseDTO toResponseDTO(Grupo grupo) {
        return GrupoResponseDTO.builder()
                .id(grupo.getId())
                .codigo(grupo.getCodigo())
                .nombre(grupo.getNombre())
                .descripcion(grupo.getDescripcion())
                .activo(grupo.getActivo())
                .fechaCreacion(grupo.getFechaCreacion())
                .build();
    }
}
