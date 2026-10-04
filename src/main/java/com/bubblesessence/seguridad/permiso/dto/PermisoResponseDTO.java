package com.bubblesessence.seguridad.permiso.dto;

import com.bubblesessence.seguridad.permiso.TipoPermiso;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermisoResponseDTO {

    private Long id;
    private Integer accionId;
    private String accionCodigo;
    private String accionNombre;
    private TipoPermiso tipo;
}
