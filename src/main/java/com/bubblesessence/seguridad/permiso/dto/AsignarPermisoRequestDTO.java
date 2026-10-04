package com.bubblesessence.seguridad.permiso.dto;

import com.bubblesessence.seguridad.permiso.TipoPermiso;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AsignarPermisoRequestDTO {

    @NotNull(message = "El accionId es obligatorio")
    private Integer accionId;

    @NotNull(message = "El tipo (GRANT o DENY) es obligatorio")
    private TipoPermiso tipo;
}
