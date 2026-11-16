package com.mpaz.contenidos.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnidadRequest {

    /** Obligatorio al crear; se ignora al editar. */
    private Long asignaturaId;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 120, message = "El título admite hasta 120 caracteres")
    private String titulo;

    private Integer orden;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}
