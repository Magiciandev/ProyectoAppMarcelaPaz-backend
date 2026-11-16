package com.mpaz.progreso.dto;

import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidacionRequest {
    @Size(max = 2000, message = "La observación admite hasta 2000 caracteres")
    private String observacion;
    private Boolean validar;
}
