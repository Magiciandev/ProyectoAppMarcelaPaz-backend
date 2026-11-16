package com.mpaz.progreso.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgresoSync {
    private Long objetivoId;
    private Integer contenidosCompletados;
    private Integer actividadesCompletadas;
    private Integer desafiosCompletados;
    private Integer porcentajeAvance;
    private Instant marcaTiempo;
}
