package com.mpaz.progreso.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntentoSync {
    private String uuid;
    private Long pruebaId;
    private Long objetivoId;
    private Integer numeroIntento;
    private BigDecimal puntajeObtenido;
    private BigDecimal puntajeMaximo;
    private Instant fechaHora;
    private List<RespuestaSync> respuestas;
}
