package com.mpaz.progreso.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InformeRespuestaDTO {
    private Long objetivoId;
    private Long preguntaId;
    private boolean esCorrecta;
    private BigDecimal puntaje;
}
