package com.bubblesessence.seguridad.accion;

import com.bubblesessence.seguridad.accion.dto.AccionResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class AccionMapper {

    public AccionResponseDTO toResponseDTO(Accion accion) {
        return AccionResponseDTO.builder()
                .id(accion.getId())
                .moduloId(accion.getModulo().getId())
                .moduloCodigo(accion.getModulo().getCodigo())
                .codigo(accion.getCodigo())
                .nombre(accion.getNombre())
                .descripcion(accion.getDescripcion())
                .activo(accion.getActivo())
                .build();
    }
}
