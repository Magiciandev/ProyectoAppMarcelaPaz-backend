package com.mpaz.evaluaciones.dto;

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
public class PruebaRequest {

    @NotNull(message = "El objetivo de aprendizaje es obligatorio")
    private Long objetivoId;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 120, message = "El título admite hasta 120 caracteres")
    private String titulo;

    private String criteriosPuntuacion;

    @Min(value = 1, message = "El límite de intentos debe ser al menos 1")
    private Integer limiteIntentos;
}
