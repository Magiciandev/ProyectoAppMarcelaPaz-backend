package com.mpaz.progreso.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenObjetivoDTO {
    private Long objetivoId;
    private Integer porcentajeAvance;
    private long intentosAceptados;
    private Integer limiteIntentos;
    private Integer mejorPorcentaje;
    private String nivel;
}
