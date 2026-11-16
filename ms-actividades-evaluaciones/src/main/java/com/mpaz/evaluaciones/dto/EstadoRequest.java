package com.mpaz.evaluaciones.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadoRequest {

    @NotBlank(message = "El estado es obligatorio")
    private String estado;
}
