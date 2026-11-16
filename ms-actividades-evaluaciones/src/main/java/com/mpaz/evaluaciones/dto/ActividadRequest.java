package com.mpaz.evaluaciones.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActividadRequest {

    @NotNull(message = "El objetivo de aprendizaje es obligatorio")
    private Long objetivoId;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 120, message = "El título admite hasta 120 caracteres")
    private String titulo;

    @NotBlank(message = "El tipo es obligatorio")
    private String tipo;

    @Min(value = 1, message = "La dificultad va de 1 a 3")
    @Max(value = 3, message = "La dificultad va de 1 a 3")
    private Integer dificultad;

    private Integer orden;
}
