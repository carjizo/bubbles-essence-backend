package com.bubblesessence.asistente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PreguntaChatDTO {

    @NotBlank(message = "La pregunta no puede estar vacía")
    @Size(max = 500, message = "La pregunta es demasiado larga (máximo 500 caracteres)")
    private String pregunta;
}
