package com.mpaz.progreso.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaDTO {
    private Long preguntaId;
    private String respuesta;
    private boolean esCorrecta;
    private BigDecimal puntajeObtenido;
}
