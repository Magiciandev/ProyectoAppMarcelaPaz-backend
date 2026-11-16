package com.mpaz.admin.cliente;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.mpaz.admin.dto.PaqueteEvaluaciones;
import com.mpaz.admin.exception.BadGatewayException;

/** Llama directamente a ms-actividades-evaluaciones reenviando el Authorization del usuario que aprueba. */
@Component
public class EvaluacionesClient {

    private final RestClient rest;

    public EvaluacionesClient(@Value("${mpaz.servicios.evaluaciones.url}") String baseUrl) {
        this.rest = RestClient.builder().baseUrl(baseUrl).build();
    }

    public PaqueteEvaluaciones obtenerPaquete(Long objetivoId, String authorization) {
        try {
            return rest.get()
                    .uri("/evaluaciones/objetivos/{id}/paquete", objetivoId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .body(PaqueteEvaluaciones.class);
        } catch (RestClientException e) {
            throw new BadGatewayException("ms-actividades-evaluaciones no respondió");
        }
    }

    public void fijarEstado(Long objetivoId, String estado, String authorization) {
        try {
            rest.put()
                    .uri("/evaluaciones/interno/objetivos/{id}/estado", objetivoId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("estado", estado))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new BadGatewayException("ms-actividades-evaluaciones no pudo cambiar el estado");
        }
    }
}
