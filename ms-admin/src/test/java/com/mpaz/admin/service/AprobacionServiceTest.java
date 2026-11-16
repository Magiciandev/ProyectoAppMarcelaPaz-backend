package com.mpaz.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mpaz.admin.cliente.ContenidosClient;
import com.mpaz.admin.cliente.EvaluacionesClient;
import com.mpaz.admin.dto.AprobacionRequest;
import com.mpaz.admin.dto.AprobacionResponse;
import com.mpaz.admin.dto.PaqueteContenidos;
import com.mpaz.admin.dto.PaqueteEvaluaciones;
import com.mpaz.admin.exception.BadGatewayException;
import com.mpaz.admin.exception.BadRequestException;
import com.mpaz.admin.exception.ConflictException;
import com.mpaz.admin.model.HistorialAprobacion;
import com.mpaz.admin.repository.HistorialRepository;

class AprobacionServiceTest {

    private static final String TOKEN = "Bearer abc";

    private ContenidosClient contenidos;
    private EvaluacionesClient evaluaciones;
    private HistorialRepository historial;
    private AprobacionService service;

    @BeforeEach
    void preparar() {
        contenidos = mock(ContenidosClient.class);
        evaluaciones = mock(EvaluacionesClient.class);
        historial = mock(HistorialRepository.class);
        service = new AprobacionService(contenidos, evaluaciones, historial);
    }

    private PaqueteContenidos paquete(String estado, int cantidadContenidos) {
        List<Object> lista = cantidadContenidos == 0 ? List.of() : List.of("c1");
        return new PaqueteContenidos(new PaqueteContenidos.ObjetivoRemoto(1L, estado), lista);
    }

    private PaqueteEvaluaciones evaluacionConPrueba(boolean conPreguntas) {
        List<Object> preguntas = conPreguntas ? List.of("p1") : List.of();
        return new PaqueteEvaluaciones(new PaqueteEvaluaciones.PruebaRemota(5L, preguntas));
    }

    private AprobacionRequest pedido(String accion) {
        return new AprobacionRequest("OBJETIVO", 1L, accion, "ok");
    }

    @Test
    void autorizarUnObjetivoEnRevisionCambiaAmbosServiciosYRegistraHistorial() {
        when(contenidos.obtenerPaquete(1L, TOKEN)).thenReturn(paquete("EN_REVISION", 1));

        AprobacionResponse r = service.aplicar(pedido("AUTORIZAR"), 9L, TOKEN);

        assertThat(r.getEstadoAnterior()).isEqualTo("EN_REVISION");
        assertThat(r.getEstadoNuevo()).isEqualTo("AUTORIZADO");
        verify(contenidos).fijarEstado(1L, "AUTORIZADO", TOKEN);
        verify(evaluaciones).fijarEstado(1L, "AUTORIZADO", TOKEN);
        verify(historial).save(any(HistorialAprobacion.class));
    }

    @Test
    void transicionInvalidaEsConflictoYNoCambiaNada() {
        when(contenidos.obtenerPaquete(1L, TOKEN)).thenReturn(paquete("BORRADOR", 1));

        assertThatThrownBy(() -> service.aplicar(pedido("PUBLICAR"), 9L, TOKEN))
                .isInstanceOf(ConflictException.class);
        verify(contenidos, never()).fijarEstado(any(), anyString(), anyString());
        verify(historial, never()).save(any());
    }

    @Test
    void enviarARevisionSinContenidoEsConflicto() {
        when(contenidos.obtenerPaquete(1L, TOKEN)).thenReturn(paquete("BORRADOR", 0));

        assertThatThrownBy(() -> service.aplicar(pedido("ENVIAR_REVISION"), 9L, TOKEN))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("contenido");
    }

    @Test
    void enviarARevisionSinPruebaConPreguntasEsConflicto() {
        when(contenidos.obtenerPaquete(1L, TOKEN)).thenReturn(paquete("BORRADOR", 1));
        when(evaluaciones.obtenerPaquete(1L, TOKEN)).thenReturn(evaluacionConPrueba(false));

        assertThatThrownBy(() -> service.aplicar(pedido("ENVIAR_REVISION"), 9L, TOKEN))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("prueba");
        verify(contenidos, never()).fijarEstado(any(), anyString(), anyString());
    }

    @Test
    void enviarARevisionCumpliendoLosRequisitosFunciona() {
        when(contenidos.obtenerPaquete(1L, TOKEN)).thenReturn(paquete("BORRADOR", 1));
        when(evaluaciones.obtenerPaquete(1L, TOKEN)).thenReturn(evaluacionConPrueba(true));

        AprobacionResponse r = service.aplicar(pedido("ENVIAR_REVISION"), 9L, TOKEN);

        assertThat(r.getEstadoNuevo()).isEqualTo("EN_REVISION");
    }

    @Test
    void siFallaElSegundoServicioSeRevierteElPrimeroYNoSeRegistra() {
        when(contenidos.obtenerPaquete(1L, TOKEN)).thenReturn(paquete("EN_REVISION", 1));
        doThrow(new BadGatewayException("caido")).when(evaluaciones).fijarEstado(eq(1L), eq("AUTORIZADO"), eq(TOKEN));

        assertThatThrownBy(() -> service.aplicar(pedido("AUTORIZAR"), 9L, TOKEN))
                .isInstanceOf(BadGatewayException.class);

        verify(contenidos).fijarEstado(1L, "AUTORIZADO", TOKEN);
        verify(contenidos, times(1)).fijarEstado(1L, "EN_REVISION", TOKEN);
        verify(historial, never()).save(any());
    }

    @Test
    void soloSeApruebanObjetivosDeAprendizaje() {
        AprobacionRequest otro = new AprobacionRequest("UNIDAD", 1L, "AUTORIZAR", null);

        assertThatThrownBy(() -> service.aplicar(otro, 9L, TOKEN)).isInstanceOf(BadRequestException.class);
    }
}
