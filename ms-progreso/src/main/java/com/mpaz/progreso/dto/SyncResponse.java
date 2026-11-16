package com.mpaz.progreso.dto;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncResponse {
    private List<ResultadoIntento> intentos;
    private List<ResultadoProgreso> progreso;
    private List<MejorObjetivoDTO> mejorPorObjetivo;
    private Instant ultimoReinicio;
}
