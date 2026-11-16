package com.mpaz.contenidos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContenidoRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 120, message = "El título admite hasta 120 caracteres")
    private String titulo;

    @NotBlank(message = "La explicación es obligatoria")
    private String explicacion;

    private String instrucciones;
    private String ejemplosGuiados;
    private String imagenUrl;
    private String archivoAudio;
    private Integer orden;

    @NotBlank(message = "La autoría (responsableOrigen) es obligatoria")
    private String responsableOrigen;

    @NotBlank(message = "La licencia es obligatoria")
    private String licencia;
}
