package com.mpaz.evaluaciones.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreguntaEnPruebaDTO {

    @NotNull(message = "preguntaId es obligatorio")
    private Long preguntaId;

    private Integer ordenEnPrueba;

    private BigDecimal puntaje;
}
