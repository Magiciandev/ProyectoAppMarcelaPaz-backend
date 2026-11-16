package com.mpaz.contenidos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjetivoRequest {

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 12, message = "El código admite hasta 12 caracteres")
    private String codigo;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    private String ejeCurricular;
    private String textoReferencia;
    private Integer orden;
}
