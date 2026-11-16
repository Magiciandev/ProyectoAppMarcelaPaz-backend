package com.mpaz.admin.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Lo que ms-admin lee de GET /evaluaciones/objetivos/{id}/paquete. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaqueteEvaluaciones(PruebaRemota prueba) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PruebaRemota(Long id, List<Object> preguntas) {
    }

    public boolean tienePruebaConPreguntas() {
        return prueba != null && prueba.preguntas() != null && !prueba.preguntas().isEmpty();
    }
}
