package com.mpaz.evaluaciones.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreguntaRequest {

    @NotNull(message = "El objetivo de aprendizaje es obligatorio")
    private Long objetivoId;

    @NotBlank(message = "El tipo es obligatorio")
    private String tipo;

    @NotBlank(message = "El enunciado es obligatorio")
    private String enunciado;

    private String explicacion;

    @NotEmpty(message = "La pregunta necesita opciones")
    @Valid
    private List<OpcionDTO> opciones;
}
