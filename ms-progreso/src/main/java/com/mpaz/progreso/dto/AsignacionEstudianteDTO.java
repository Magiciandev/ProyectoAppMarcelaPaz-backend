package com.mpaz.progreso.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionEstudianteDTO {
    private Long asignacionId;
    private Long objetivoId;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private Integer limiteIntentos;
    private LocalDate fechaHabilitacion;
    private LocalDate fechaRendicion;
    private long intentosUsados;
}
