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
public class InformeDTO {
    private Long id;
    private Long estudianteId;
    private Long cursoId;
    private Long objetivoId;
    private BigDecimal puntajeGlobal;
    private String nivel;
    private Instant fechaEmision;
    private boolean validado;
    private Long validadoPor;
    private Instant fechaValidacion;
    private String observacionDocente;
    private List<InformeObjetivoDTO> porObjetivo;
    private List<Long> areasRefuerzo;
    private List<InformeRespuestaDTO> respuestas;
}
