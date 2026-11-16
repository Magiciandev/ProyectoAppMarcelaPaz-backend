package com.mpaz.progreso.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MejorObjetivoDTO {
    private Long objetivoId;
    private Integer mejorPorcentaje;
    private long intentosAceptados;
    private Integer limiteIntentos;
}
