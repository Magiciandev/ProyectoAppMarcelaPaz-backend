package com.mpaz.progreso.dto;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InformeRequest {
    @NotNull(message = "El estudiante es obligatorio")
    private Long estudianteId;
    private Long objetivoId;
}
