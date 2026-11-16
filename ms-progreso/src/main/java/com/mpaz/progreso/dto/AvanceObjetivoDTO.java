package com.mpaz.progreso.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvanceObjetivoDTO {
    private Long objetivoId;
    private Integer porcentajeAvance;
    private long intentosUsados;
    private long intentosRestantes;
    private Integer limiteIntentos;
}
