package com.mpaz.evaluaciones.dto;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreguntaEnActividadDTO {

    @NotNull(message = "preguntaId es obligatorio")
    private Long preguntaId;

    private Integer orden;
}
