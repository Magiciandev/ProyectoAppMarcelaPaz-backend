package com.mpaz.progreso.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntentoDTO {
    private Long id;
    private String uuid;
    private Long objetivoId;
    private Long pruebaId;
    private Integer numeroIntento;
    private BigDecimal puntajeObtenido;
    private BigDecimal puntajeMaximo;
    private Integer porcentaje;
    private Instant fechaHora;
    private boolean aceptado;
    private String motivoRechazo;
    private List<RespuestaDTO> respuestas;
}
