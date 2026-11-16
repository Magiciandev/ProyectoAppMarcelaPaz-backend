package com.mpaz.admin.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Lo que ms-admin lee de GET /contenidos/objetivos/{id}/paquete. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaqueteContenidos(ObjetivoRemoto objetivo, List<Object> contenidos) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ObjetivoRemoto(Long id, String estado) {
    }

    public int cantidadContenidos() {
        return contenidos == null ? 0 : contenidos.size();
    }
}
