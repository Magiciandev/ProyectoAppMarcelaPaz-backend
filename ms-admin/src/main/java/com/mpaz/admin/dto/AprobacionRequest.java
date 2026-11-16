package com.mpaz.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AprobacionRequest {

    @NotBlank(message = "La entidad es obligatoria")
    private String entidad;

    @NotNull(message = "El identificador de la entidad es obligatorio")
    private Long entidadId;

    @NotBlank(message = "La acción es obligatoria")
    private String accion;

    @Size(max = 255, message = "La observación admite hasta 255 caracteres")
    private String observacion;
}
