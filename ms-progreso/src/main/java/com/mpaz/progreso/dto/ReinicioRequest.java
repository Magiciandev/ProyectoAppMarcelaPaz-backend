package com.mpaz.progreso.dto;

import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReinicioRequest {
    private Long objetivoId;
    @Size(max = 255, message = "El motivo admite hasta 255 caracteres")
    private String motivo;
}
