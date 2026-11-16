package com.mpaz.admin.cliente;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.mpaz.admin.dto.PaqueteContenidos;
import com.mpaz.admin.exception.BadGatewayException;
import com.mpaz.admin.exception.ResourceNotFoundException;

/** Llama directamente a ms-contenidos reenviando el Authorization del usuario que aprueba. */
@Component
public class ContenidosClient {

    private final RestClient rest;

    public ContenidosClient(@Value("${mpaz.servicios.contenidos.url}") String baseUrl) {
        this.rest = RestClient.builder().baseUrl(baseUrl).build();
    }

    public PaqueteContenidos obtenerPaquete(Long objetivoId, String authorization) {
        try {
            return rest.get()
                    .uri("/contenidos/objetivos/{id}/paquete", objetivoId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .body(PaqueteContenidos.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Objetivo no encontrado");
        } catch (RestClientException e) {
            throw new BadGatewayException("ms-contenidos no respondió");
        }
    }

    public void fijarEstado(Long objetivoId, String estado, String authorization) {
        try {
            rest.put()
                    .uri("/contenidos/interno/objetivos/{id}/estado", objetivoId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("estado", estado))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new BadGatewayException("ms-contenidos no pudo cambiar el estado");
        }
    }
}
