package com.mpaz.admin.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AprobacionResponse {
    private String entidad;
    private Long entidadId;
    private String estadoAnterior;
    private String estadoNuevo;
    private Instant fecha;
}
