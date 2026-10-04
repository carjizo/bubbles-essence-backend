package com.bubblesessence.seguridad.accion.dto;

import jakarta.validation.constraints.NotBlank;
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
public class AccionRequestDTO {

    @NotNull(message = "El moduloId es obligatorio")
    private Integer moduloId;

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 60, message = "El código no puede superar 60 caracteres")
    private String codigo; // ej. "btn-editar-pedido"

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50)
    private String nombre;

    @Size(max = 200)
    private String descripcion;

    private Boolean activo;
}
