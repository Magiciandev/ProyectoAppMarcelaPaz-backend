package com.mpaz.progreso.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionRequest {
    @NotNull(message = "El objetivo es obligatorio")
    private Long objetivoId;
    private Long cursoId;
    private Long estudianteId;
    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaDesde;
    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaHasta;
    @Min(value = 1, message = "El límite de intentos debe ser al menos 1")
    private Integer limiteIntentos;
    private LocalDate fechaHabilitacion;
    private LocalDate fechaRendicion;
    private Boolean disponible;
}
