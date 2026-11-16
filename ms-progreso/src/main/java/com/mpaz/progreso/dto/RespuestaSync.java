package com.mpaz.progreso.dto;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaSync {
    private String uuid;
    private Long preguntaId;
    private String respuesta;
    private Boolean esCorrecta;
    private BigDecimal puntajeObtenido;
    private Instant marcaTiempo;
}
