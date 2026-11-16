package com.mpaz.progreso.cliente;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.mpaz.progreso.exception.BadGatewayException;

/** Pregunta directamente a ms-contenidos, reenviando el token del docente, si un objetivo le es visible. */
@Component
public class ContenidosClient {

    private final RestClient rest;

    public ContenidosClient(@Value("${mpaz.servicios.contenidos.url}") String baseUrl) {
        this.rest = RestClient.builder().baseUrl(baseUrl).build();
    }

    /** True si el objetivo existe y el rol del token puede verlo (para un docente: AUTORIZADO o PUBLICADO). */
    public boolean objetivoVisible(Long objetivoId, String authorization) {
        try {
            rest.get()
                    .uri("/contenidos/objetivos/{id}", objetivoId)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (RestClientException e) {
            throw new BadGatewayException("ms-contenidos no respondió");
        }
    }
}
